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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;

/**
 * Pseudo-test for download speed measurements
 */
public class DownloadSpeedTestCase extends RemoteTestBase {

    public DownloadSpeedTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testBatchDownload() throws Exception {
        File listFile = new File("/tmp/download.list");
        assertTrue(listFile.exists());
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(listFile)));
        List<String> remoteFiles = new ArrayList<>(1000);
        String line;
        while ((line = br.readLine()) != null) {
            if (line.length() > 0) { // ignore empty lines (usually trailing)
                remoteFiles.add(line);
            }
        }
        ExecutionEnvironment env = getTestExecutionEnvironment();
        PrintWriter err = new PrintWriter(System.err);
        File destDir = getWorkDir(); //  + "/download-speed-test"
        destDir.mkdirs();
        assertTrue(destDir.exists());
        long time = System.currentTimeMillis();
        int cnt = 0;
        for (String remotePath : remoteFiles) {
            String name = CndPathUtilities.getBaseName(remotePath);
            File localFile = new File(destDir, name + '.' + (cnt++));
            Future<Integer> task = CommonTasksSupport.downloadFile(remotePath, env, localFile, err);
            int rc = task.get().intValue();
            assertEquals(0, rc);
        }
        time = System.currentTimeMillis() - time;
        System.err.printf("Downloaded %d files from %s in %d seconds\n", remoteFiles.size(), env, time/1000);
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(DownloadSpeedTestCase.class);
    }

}
