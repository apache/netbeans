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

package org.netbeans.modules.cnd.remote.pbuild;

import org.netbeans.modules.cnd.remote.test.RemoteBuildTestBase;
import java.util.concurrent.TimeUnit;
import junit.framework.Test;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.spi.project.ActionProvider;

/**
 */
public class LinuxTouchRfsBuildTestCase extends RemoteBuildTestBase {

    public LinuxTouchRfsBuildTestCase(String testName) {
        super(testName);
    }

    public LinuxTouchRfsBuildTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);       
    }

    private void doTest(Sync sync, Toolchain toolchain) throws Exception {
        final ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        MakeProject makeProject = openProject("touch-on-linux", execEnv, sync, toolchain);
        buildProject(makeProject, ActionProvider.COMMAND_BUILD, getSampleBuildTimeout(), TimeUnit.SECONDS);
    }

    @ForAllEnvironments
    public void testTouchRfs_gnu() throws Exception {
        doTest(Sync.RFS, Toolchain.GNU);
    }

//    @ForAllEnvironments
//    public void testTouchRfs_sunstudio() throws Exception {
//        doTest(Sync.RFS, Toolchain.SUN);
//    }

    public static Test suite() {
        return new RemoteDevelopmentTest(LinuxTouchRfsBuildTestCase.class);
    }
}
