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
import java.io.File;
import java.util.concurrent.TimeUnit;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Utilities;

/**
 *
 */
public class RemoteBuildLinksTestCase extends RemoteBuildTestBase {

    public RemoteBuildLinksTestCase(String testName) {
        super(testName);
    }

    public RemoteBuildLinksTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void postCopyProject(File origBase, File copiedBase, String projectName) throws Exception {
        String dir = copiedBase.getAbsolutePath();
        String scriptName = "create_links.sh";
        ProcessUtils.ExitStatus rc = ProcessUtils.executeInDir(dir, ExecutionEnvironmentFactory.getLocal(), "sh", scriptName);
        assertTrue("Can not execute " + scriptName + " in " + dir + ":\n" + rc.getErrorString(), rc.isOK());
    }

    private void doTest(Toolchain toolchain) throws Exception {
        if (Utilities.isWindows()) {
            System.out.printf("%s: skipping test on Windows\n", getName());
        }
        final ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        MakeProject project = openProject("makefile_proj_w_links", execEnv, Sync.RFS, toolchain);
        buildProject(project, ActionProvider.COMMAND_BUILD, getSampleBuildTimeout(), TimeUnit.SECONDS);
    }

    @ForAllEnvironments
    public void testSymLinksBuild() throws Exception {
        if (Utilities.isWindows()) {
            System.err.printf("Skipping %s on Windows\n", getName());
            return;
        }
        doTest(Toolchain.GNU);
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(RemoteBuildLinksTestCase.class);
    }
}
