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

import java.io.File;
import org.netbeans.modules.cnd.remote.test.RemoteBuildTestBase;
import junit.framework.Test;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.openide.filesystems.FileUtil;
/**
 *
 */
public class RemoteBuildSamplesTestCase extends RemoteBuildTestBase {

    public RemoteBuildSamplesTestCase(String testName) {
        super(testName);
    }

    public RemoteBuildSamplesTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

//    @ForAllEnvironments
//    public void testBuildSample_Rfs_Gnu_Arguments_Once() throws Exception {
//        buildSample(Sync.RFS, Toolchain.GNU, "Arguments", "Args_Rfs_Gnu_Once", 1);
//    }
//
//    @ForAllEnvironments
//    public void testBuildSample_Rfs_Gnu_Arguments_Multy() throws Exception {
//        buildSample(Sync.RFS, Toolchain.GNU, "Arguments", "Args_Rfs_Gnu_Multy", 3, getSampleBuildTimeout(), getSampleBuildTimeout()/3);
//    }
//
//    @ForAllEnvironments
//    public void testBuildSample_Sftp_Gnu_Arguments_Once() throws Exception {
//        buildSample(Sync.FTP, Toolchain.GNU, "Arguments", "Args_Sftp_Gnu_Once", 1);
//    }
//
//    @ForAllEnvironments
//    public void testBuildSample_Sftp_Gnu_Arguments_Multy() throws Exception {
//        buildSample(Sync.FTP, Toolchain.GNU, "Arguments", "Args_Sftp_Gnu_Multy", 3, getSampleBuildTimeout(), getSampleBuildTimeout()/3);
//    }

    private class ExecutableSetter implements ProjectProcessor{
        private void setExecutable(File f) {
            if (f.isDirectory()) {
                for (File child : f.listFiles()) {
                    setExecutable(child);
                }
            } else {
                f.setExecutable(true);
            }
        }

        @Override
        public void processProject(MakeProject project) throws Exception {
            setExecutable(FileUtil.toFile(project.getProjectDirectory()));
        }
    }

    @ForAllEnvironments
    public void testBuildSample_Sftp_Gnu_Arguments_With_Space_Once() throws Exception {
        buildSample(Sync.FTP, Toolchain.GNU, "Arguments", "Args Sftp Gnu WithSpace Once", 1,
                getSampleBuildTimeout(), getSampleBuildTimeout()/3, new ExecutableSetter());
    }

    @ForAllEnvironments
    public void testBuildSample_Sftp_Gnu_Arguments_With_Space_Multy() throws Exception {
        buildSample(Sync.FTP, Toolchain.GNU, "Arguments", "Args Sftp Gnu With Space Multy", 3, 
                getSampleBuildTimeout(), getSampleBuildTimeout()/3, new ExecutableSetter());
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(RemoteBuildSamplesTestCase.class);
    }
}
