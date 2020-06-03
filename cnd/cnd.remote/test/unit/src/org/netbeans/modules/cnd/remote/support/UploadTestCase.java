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
package org.netbeans.modules.cnd.remote.support;

import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import junit.framework.Test;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;

/**
 */
public class UploadTestCase extends RemoteTestBase {


    public UploadTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);        
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createRemoteTmpDir();
    }

    @Override
    protected void tearDown() throws Exception {
        clearRemoteTmpDir(); // before disconnection!
        super.tearDown();
    }

    @ForAllEnvironments
    public void testCopyTo() throws Exception {
        File localFile = File.createTempFile("cnd", ".cnd"); //NOI18N
        localFile.deleteOnExit();
        FileWriter fstream = new FileWriter(localFile);
        StringBuilder sb = new StringBuilder("File from "); //NOI18N
        try {
            InetAddress addr = InetAddress.getLocalHost();
            sb.append( addr.getHostName() );
        } catch (UnknownHostException e) {
        }
        sb.append("\ntime: ").append(System.currentTimeMillis());//.append("\n"); //NOI18N
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(sb.toString());
        out.close();
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();        
        String remoteFile = getRemoteTmpDir() + "/" + localFile.getName(); //NOI18N
        int rc = CommonTasksSupport.uploadFile(localFile.getAbsolutePath(), execEnv, remoteFile, 0770).get().getExitCode();
        assertEquals("Upload RC for " + localFile.getAbsolutePath(), 0, rc);
        assert HostInfoProvider.fileExists(execEnv, remoteFile) : "Error copying file " + remoteFile + " to " + execEnv + " : file does not exist";
        ProcessUtils.ExitStatus rcs2 = ProcessUtils.execute(execEnv, "cat", remoteFile);
//            assert rcs2.run() == 0; // add more output
        rc = rcs2.exitCode;
        if (rc != 0) {
            assert false : "RemoteCommandSupport: " + "cat " + remoteFile + " returned " + rc + " on " + execEnv;
        }
        assert rcs2.getOutputString().equals(sb.toString());
        ProcessUtils.ExitStatus rc3 = ProcessUtils.execute(execEnv, "rm", remoteFile);
        assert rc3.exitCode == 0;
        localFile.delete();
    }
    
    public static Test suite() {
        return new RemoteDevelopmentTest(UploadTestCase.class);
    }

    @ForAllEnvironments
    @org.netbeans.api.annotations.common.SuppressWarnings("RV")
    public void testCopyManyFilesTo() throws Exception {
        File dir = new File(getNetBeansPlatformDir(), "modules");
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
        File[] files = dir.listFiles();
        assert files != null;
        long totalSize = 0;
        int totalCount = 0;
        long totalTime = System.currentTimeMillis();
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(execEnv);

        File tmpFile = File.createTempFile("copy_small_files", ".dat");
        String remoteDir = getRemoteTmpDir() + "/" + tmpFile.getName();
        tmpFile.delete();

        Future<Integer> mkDirTask = CommonTasksSupport.mkDir(execEnv, remoteDir, new PrintWriter(System.err));
        System.out.printf("Mkdir %s\n", remoteDir);
        int rc = mkDirTask.get(30, TimeUnit.SECONDS);
        System.out.printf("mkdir %s done, rc=%d\n", remoteDir, rc);
        assertEquals(0, rc);
        //Thread.sleep(2000);

        for (File localFile : files) {
            if (localFile.isFile()) {
                totalCount++;
                totalSize += localFile.length();
                assertTrue(localFile.exists());
                String remoteFile = remoteDir + "/" + localFile.getName(); //NOI18N
                long time = System.currentTimeMillis();
                rc = CommonTasksSupport.uploadFile(localFile.getAbsolutePath(), execEnv, remoteFile, 0770).get().getExitCode();
                assertEquals("Upload RC for " + localFile.getAbsolutePath(), 0, rc);
                time = System.currentTimeMillis() - time;
                System.out.printf("File %s copied to %s:%s in %d ms\n", localFile, execEnv, remoteFile, time);
            }
        }
        totalTime = System.currentTimeMillis() - totalTime;
        System.out.printf("%d Kb in %d files to %s in %d ms\n", totalSize/1024, totalCount, execEnv, totalTime);
        assertEquals("Can't remove " + remoteDir + " on remote host", 0, CommonTasksSupport.rmDir(execEnv, remoteDir, true, new PrintWriter(System.err)).get().intValue());
    }

}
