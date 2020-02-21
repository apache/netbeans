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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.ui.wizard;

import java.util.Collection;
import java.util.List;
import junit.framework.Test;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.openide.util.Exceptions;

/**
 * Tests for setting up a remote host
 */
public class HostSetupTestCase extends RemoteTestBase {

    public HostSetupTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    private RcFile getRcFileWithNonEmptySection(String section) throws Exception {
        try {
            RcFile rcFile = getRemoteRcFile();
            Collection<String> keys = rcFile.getKeys(section);
            if (!keys.isEmpty()) {
                return rcFile;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return getLocalRcFile();
    }

    @ForAllEnvironments
    public void testHostSetup() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        setupHost(execEnv);
        CompilerSetManager csm = CompilerSetManager.get(execEnv);
        final List<CompilerSet> compilerSets = csm.getCompilerSets();
        dumpCompilerSets(execEnv, compilerSets);

        String mspec = NativeExecutionTestSupport.getMspec(execEnv);

        String section = "remote." + mspec + ".compilerSets";
        RcFile rcFile = getRcFileWithNonEmptySection(section);
        Collection<String> compilerSetNames = rcFile.getKeys(section);
        for (String csReferenceName : compilerSetNames) {
            boolean found = false;
            for (CompilerSet cs : compilerSets) {
                if (csReferenceName.equals(cs.getName())) {
                    found = true;
                    String csReferenceDir = rcFile.get(section, csReferenceName);
                    assertEquals("Directory differs for compiler set " + cs.getName() +
                            " at " + mspec + " (" + execEnv + "): ",
                            csReferenceDir, cs.getDirectory());
                    break;
                }
            }
            if (!found) {
                assertTrue("Compiler set " + csReferenceName + " at " + mspec + " (" + execEnv + ") not found", false);
            }
        }
        //Runnable runOnFinish = validator.getRunOnFinish();
    }

    private static void dumpCompilerSets(ExecutionEnvironment execEnv, List<CompilerSet> compilerSets) {
        System.err.printf("\nCompiler sets for %s (%s)\n", NativeExecutionTestSupport.getMspec(execEnv), execEnv);
        for (CompilerSet cs : compilerSets) {
            cs.getName();
            cs.getDirectory();
            System.err.printf("\tNAME=%s FLAVOR=%s DIR=%s\n", cs.getName(), cs.getCompilerFlavor(), cs.getDirectory());
        }
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(HostSetupTestCase.class);
    }
}
