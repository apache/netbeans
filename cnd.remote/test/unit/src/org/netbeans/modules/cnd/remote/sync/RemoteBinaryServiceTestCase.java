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
package org.netbeans.modules.cnd.remote.sync;

import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import org.netbeans.modules.remote.api.RemoteBinaryService.RemoteBinaryID;
import org.netbeans.modules.cnd.remote.support.*;
import java.io.File;
import junit.framework.Test;
import org.netbeans.modules.remote.api.RemoteBinaryService;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;

/**
 */
public class RemoteBinaryServiceTestCase extends RemoteTestBase {

    public RemoteBinaryServiceTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    @org.netbeans.api.annotations.common.SuppressWarnings("RV")
    public void testBinaryService() throws Exception {

        ExecutionEnvironment execEnv = getTestExecutionEnvironment();

        // setup: create a temp file and copy /bin/ls into it
        ExitStatus rcs = ProcessUtils.execute(execEnv, "mktemp");
        assertTrue("mktemp is not successful " + rcs.exitCode + " rc=" + rcs.getErrorString(), rcs.isOK());
        String remotePath = rcs.getOutputString();
        assertNotNull(remotePath);
        if (remotePath.endsWith("\n")) {
            remotePath = remotePath.substring(0, remotePath.length() - 1);
        }
        assertTrue(remotePath.length() > 0);
        rcs = ProcessUtils.execute(execEnv, "cp", "/bin/ls", remotePath);
        assertTrue("cp /bin/ls " + remotePath + "is not successful " + rcs.exitCode + " rc=" + rcs.getErrorString(), rcs.isOK());

        String localPath;
        File localFile = null;
        RemoteBinaryServiceImpl.resetDownloadCount();
        int expectedDownloadCount = 1;
        for (int i = 0; i < 5; i++) {
            if (i == 3) {
                localFile.delete();
                expectedDownloadCount++;
            } else if (i == 4) {
                rcs = ProcessUtils.execute(execEnv, "touch", remotePath);
                assertTrue("touch " + remotePath + "is not successful " + rcs.exitCode + " rc=" + rcs.getErrorString(), rcs.isOK());
                expectedDownloadCount++;
            }

            RemoteBinaryID remoteBinaryID = RemoteBinaryService.getRemoteBinary(execEnv, remotePath);
            assertNotNull(remoteBinaryID);

            Boolean result = RemoteBinaryService.getResult(remoteBinaryID).get();
            assertTrue(result);

            localPath = RemoteBinaryService.getFileName(remoteBinaryID);
            localFile = new File(localPath);
            assertTrue("file doesn't exists " + localFile, localFile.exists());
            assertTrue("file is empty " + localFile, localFile.length() > 0);
            assertEquals("Download Count differs", expectedDownloadCount, RemoteBinaryServiceImpl.getDownloadCount());
        }
    }
    
    public static Test suite() {
        return new RemoteDevelopmentTest(RemoteBinaryServiceTestCase.class);
    }
}
