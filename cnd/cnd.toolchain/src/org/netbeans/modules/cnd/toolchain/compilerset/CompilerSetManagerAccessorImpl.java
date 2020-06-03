/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
