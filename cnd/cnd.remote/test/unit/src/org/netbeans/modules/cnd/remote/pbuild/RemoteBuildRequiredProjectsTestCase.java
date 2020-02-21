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
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.spi.project.ActionProvider;

/**
 *
 */
public class RemoteBuildRequiredProjectsTestCase extends RemoteBuildTestBase {

    public RemoteBuildRequiredProjectsTestCase(String testName) {
        super(testName);
    }

    public RemoteBuildRequiredProjectsTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    private void doTest(Toolchain toolchain) throws Exception {
        final ExecutionEnvironment execEnv = getTestExecutionEnvironment();        
        MakeProject projectB = openProject("B", execEnv, Sync.RFS, toolchain);
        MakeProject projectA = (MakeProject) ProjectManager.getDefault().findProject(projectB.getProjectDirectory().getParent().getFileObject("A"));
        assertNotNull(projectA);
        ensureMakefilesWritten(projectA);
        buildProject(projectB, ActionProvider.COMMAND_BUILD, getSampleBuildTimeout(), TimeUnit.SECONDS);
    }

    @ForAllEnvironments
    public void testRemoteRequiredProjectsBuild() throws Exception {
        doTest(Toolchain.GNU);
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(RemoteBuildRequiredProjectsTestCase.class);
    }
}
