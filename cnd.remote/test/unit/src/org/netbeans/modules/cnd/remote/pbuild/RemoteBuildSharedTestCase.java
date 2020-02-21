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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.remote.test.RemoteBuildTestBase;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.openide.util.Utilities;
/**
 *
 */
public class RemoteBuildSharedTestCase extends RemoteBuildTestBase {

    private File sharedWorkDir;
    
    public RemoteBuildSharedTestCase(String testName) {
        super(testName);
    }

    public RemoteBuildSharedTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String sharedHome = getSharedHome();
        sharedWorkDir = (sharedHome == null) ? null : new File(sharedHome);
    }

    @Override
    public File getWorkDir() throws IOException {
        return (sharedWorkDir == null) ? super.getWorkDir() : sharedWorkDir;
    }

    @Override
    public String getWorkDirPath() {
        return (sharedWorkDir == null) ? super.getWorkDirPath() : sharedWorkDir.getAbsolutePath();
    }

    @ForAllEnvironments
    public void testBuildSample_Shared_Gnu_Arguments_Once() throws Exception {
        if (sharedWorkDir == null) {
            System.err.printf("Can not get shared home for %s\n", getTestExecutionEnvironment());
            return;
        }
        buildSample(Sync.SHARED, Toolchain.GNU, "Arguments", "Args_01", 1);
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(RemoteBuildSharedTestCase.class);
    }
    
    protected String getSharedHome() {
        String user = ExecutionEnvironmentFactory.getLocal().getUser();
        String[] sharedDirectories = getSharedDirectories();
        for (String dir : sharedDirectories) {
            String home = dir + '/' + user;
            ExitStatus rc = ProcessUtils.execute(ExecutionEnvironmentFactory.getLocal(), "test", "-w", home);
            if (rc.isOK()) {
                return home;
            }
        }
        return null;
    }
            
    protected String[] getSharedDirectories() {
        // Linux & Solaris:
        // /usr/sbin/exportfs
        // -               /export/home   rw   "export/home"
        // -               /export/opt   rw   "export/opt"
        if (Utilities.isUnix()) {
            ExitStatus rc = ProcessUtils.execute(ExecutionEnvironmentFactory.getLocal(), "/usr/sbin/exportfs");
            if (rc.isOK()) {
                List<String> res = new ArrayList<>();
                String[] lines = rc.getOutputString().split("\n");
                Pattern pattern = Pattern.compile("\t+| +"); // NOI18N
                for (String line : lines) {
                    String[] parts = pattern.split(line);
                    if (parts.length > 1) {
                        res.add(parts[1]);
                    }
                }
                return res.toArray(new String[res.size()]);
            }
        }
        return new String[0];
    }
}
