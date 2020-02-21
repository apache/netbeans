/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.toolchain.compilerset;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.spi.toolchain.CSMNotifier;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
//import org.openide.DialogDescriptor;
//import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 */
public class CompilerSetManagerAccessorImpl {

    private static final HashMap<ExecutionEnvironment, CompilerSetManagerImpl> managers = new HashMap<ExecutionEnvironment, CompilerSetManagerImpl>();
    private static final Object MASTER_LOCK = new Object();

    private CompilerSetManagerAccessorImpl() {
    }

    /**
     * Find or create a default CompilerSetManager for the given key. A default
     * CSM is one which is active in the system. A non-default is one which gets
     * created but has no affect unless its made default.
     *
     * For instance, the Build Tools tab (on C/C++ Tools->Options) creates a non-Default
     * CSM and only makes it default if the OK button is pressed. If Cancel is pressed,
     * it never becomes default.
     *
     * @param env specifies execution environment
     * @return A default CompilerSetManager for the given key
     */
    public static CompilerSetManager getDefault(ExecutionEnvironment env) {
        return getDefaultImpl(env, true);
    }

    /** Create a CompilerSetManager which may be registered at a later time via CompilerSetManager.setDefault() */
    public static CompilerSetManagerImpl create(ExecutionEnvironment env) {
        CompilerSetManagerImpl newCsm = new CompilerSetManagerImpl(env);
        if (newCsm.getCompilerSets().size() == 1 && newCsm.getCompilerSets().get(0).getName().equals(CompilerSetImpl.None)) {
            newCsm.remove(newCsm.getCompilerSets().get(0));
        }
        return newCsm;
    }

    /** Replace the default CompilerSetManager. Let registered listeners know its been updated */
    public static void setManagers(Collection<CompilerSetManager> csms, List<ExecutionEnvironment> liveServers) {
        synchronized (MASTER_LOCK) {
            CompilerSetPreferences.storeExecutionEnvironmentList(liveServers);
            managers.clear();
            for (CompilerSetManager csm : csms) {
                CompilerSetManagerImpl impl = (CompilerSetManagerImpl) csm;
                impl.completeCompilerSets();
                CompilerSetPreferences.saveToDisk(impl);
                managers.put(impl.getExecutionEnvironment(), impl);
            }
        }
    }

    public static void save(CompilerSetManagerImpl csm) {
        synchronized (MASTER_LOCK) {
            CompilerSetPreferences.saveToDisk(csm);
        }
    }

    public static CompilerSetManager getDeepCopy(ExecutionEnvironment execEnv, boolean initialize) {
        return ((CompilerSetManagerImpl)getDefaultImpl(execEnv, initialize)).deepCopy();
    }

    private static CompilerSetManager getDefaultImpl(ExecutionEnvironment env, boolean initialize) {
        CompilerSetManagerImpl csm;
        boolean no_compilers = false;

        synchronized (MASTER_LOCK) {
            csm = managers.get(env);
            if (csm == null) {
                csm = CompilerSetPreferences.restoreFromDisk(env);
                if (csm != null && csm.getDefaultCompilerSet() == null) {
                    CompilerSetPreferences.saveToDisk(csm);
                }
                //why cannot I validate in headless mode???
                if (csm != null && !(CndUtils.isUnitTestMode() || CndUtils.isStandalone())) {
                    ToolchainValidator.INSTANCE.validate(env, csm);
                }
            }
            if (csm == null) {
                csm = new CompilerSetManagerImpl(env, initialize);
                if (csm.isValid()) {
                    CompilerSetPreferences.saveToDisk(csm);
                } else if (!csm.isPending() && !csm.isUninitialized()) {
                    no_compilers = true;
                }
            }
            if (csm != null) {
                managers.put(env, csm);
            }
        }

        if (false && no_compilers && !CompilerSetManagerImpl.DISABLED) {
            // workaround to fix IZ#164028: Full IDE freeze when opening GizmoDemo project on Linux
            // we postpone dialog displayer until EDT is free to process
            CSMNotifier.getInstance().notifyNoCompilerSet(getString("NO_COMPILERS_FOUND_TITLE"));//NOI18N
        }
        return csm;
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(CompilerSetManagerAccessorImpl.class, s);
    }
}
