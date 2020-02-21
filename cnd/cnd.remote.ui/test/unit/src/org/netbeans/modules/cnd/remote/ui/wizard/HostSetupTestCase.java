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
