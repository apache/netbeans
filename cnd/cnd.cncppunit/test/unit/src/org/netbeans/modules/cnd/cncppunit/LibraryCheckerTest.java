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
package org.netbeans.modules.cnd.cncppunit;

import org.junit.Test;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import org.netbeans.modules.cnd.test.CndBaseTestSuite;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;

/**
 */
public class LibraryCheckerTest extends RemoteTestBase {

    public LibraryCheckerTest(String name) {
        super(name);
    }

    public LibraryCheckerTest(String name, ExecutionEnvironment execEnv) {
        super(name, execEnv);
    }

    @SuppressWarnings("unchecked")
    public static junit.framework.Test suite() {
        return new CndBaseTestSuite(LibraryCheckerTest.class);
    }

    @Test
    public void testIsLibraryAvailableLocal() throws Exception {
        doTestIsLibraryAvailable(ExecutionEnvironmentFactory.getLocal());
    }

    @Test
    @ForAllEnvironments(section = "remote.platforms")
    public void testIsLibraryAvailable() throws Exception {
        doTestIsLibraryAvailable(getTestExecutionEnvironment());
    }

    private void doTestIsLibraryAvailable(ExecutionEnvironment execEnv) throws Exception {
        setupHost(execEnv);
        CompilerSetManager compilerSetManager = CompilerSetManager.get(execEnv);
        assertFalse(compilerSetManager.isPending());
        for (CompilerSet compilerSet : compilerSetManager.getCompilerSets()) {
            AbstractCompiler cCompiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCompiler);
            assertTrue(LibraryChecker.isLibraryAvailable("m", cCompiler));
            assertFalse(LibraryChecker.isLibraryAvailable("foo", cCompiler));

            AbstractCompiler cppCompiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
            assertTrue(LibraryChecker.isLibraryAvailable("m", cppCompiler));
            assertFalse(LibraryChecker.isLibraryAvailable("foo", cppCompiler));
        }
    }
}
