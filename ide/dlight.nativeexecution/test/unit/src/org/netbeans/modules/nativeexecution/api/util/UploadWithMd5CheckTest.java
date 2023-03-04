/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import java.io.PrintWriter;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

/**
 *
 * @author Vladimir Kvashin
 */
public class UploadWithMd5CheckTest extends NativeExecutionBaseTestCase {

    public UploadWithMd5CheckTest(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name, testExecutionEnvironment);
    }

    @SuppressWarnings("unchecked")
    public static Test suite() {
        return new NativeExecutionBaseTestSuite(UploadWithMd5CheckTest.class);
    }

    @ForAllEnvironments(section = "remote.platforms")
    public void testUploadWithMd5Check() throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(env);
        clearRemoteTmpDir();
        String remoteTmpDir = createRemoteTmpDir()  + "/inexistent_subdir";
        File localFile = getIdeUtilJar();
        String remotePath = remoteTmpDir + "/" + localFile.getName();
        int rc = CommonTasksSupport.rmDir(env, remoteTmpDir, true, new PrintWriter(System.err)).get();
        assertEquals("Can not delete directory " + remoteTmpDir, 0, rc);
        assertFalse("File " + env + ":" + remoteTmpDir + " should not exist at this moment", HostInfoUtils.fileExists(env, remotePath));
        assertFalse("File " + env + ":" + remotePath + " should not exist at this moment", HostInfoUtils.fileExists(env, remotePath));
        int uploadCount = SftpSupport.getUploadCount();
        long firstTime = System.currentTimeMillis();
        UploadStatus res = CommonTasksSupport.uploadFile(localFile, env, remotePath, 0777, true).get();
        assertEquals("Error uploading file " + localFile.getAbsolutePath() + " to " + getTestExecutionEnvironment() + ":" + remotePath, 0, rc);
        firstTime = System.currentTimeMillis() - firstTime;
        assertEquals("Error copying " + localFile + " file to " + env + ":" + remotePath + ' ' + res.getError(), 0, res.getExitCode());
        assertTrue("File " + env + ":" + remotePath + " should exist at this moment", HostInfoUtils.fileExists(env, remotePath));
        assertEquals("Uploads count", ++uploadCount, SftpSupport.getUploadCount());
        System.err.printf("First copying %s to %s took %d ms\n", localFile.getAbsolutePath(), remotePath, firstTime);

        for (int pass = 0; pass < 8; pass++) {
            if (pass % 3 == 1) {
                if (pass == 1) {
                    CommonTasksSupport.rmFile(env, remotePath, null).get();
                } else {
                    ProcessUtils.execute(env, "cp", "/bin/ls", remotePath);
                }
                uploadCount++;
            }
            long currTime = System.currentTimeMillis();
            UploadStatus uploadStatus = CommonTasksSupport.uploadFile(localFile, env, remotePath, 0777, true).get();
            firstTime = System.currentTimeMillis() - currTime;
            assertEquals("Error copying " + localFile + " file to " + env + ":" + remotePath + ' ' + uploadStatus.getError(), 0, uploadStatus.getExitCode());
            assertTrue("File " + env + ":" + remotePath + " should exist at this moment", HostInfoUtils.fileExists(env, remotePath));
            assertEquals("Uploads count on pass " + pass, uploadCount, SftpSupport.getUploadCount());
            System.err.printf("Copying (pass %d) %s to %s took %d ms\n", pass, localFile.getAbsolutePath(), remotePath, firstTime);
        }
        clearRemoteTmpDir();
    }
}
