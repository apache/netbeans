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
import java.io.File;
import java.util.concurrent.Future;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;

/**
 */
public class DownloadTestCase extends RemoteTestBase {

    public DownloadTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    @org.netbeans.api.annotations.common.SuppressWarnings("RV")
    public void testCopyFrom() throws Exception {
        File localFile = File.createTempFile("cnd", ".cnd");
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        String remoteFile = "/usr/include/stdio.h";
        Future<Integer> task = CommonTasksSupport.downloadFile(remoteFile, execEnv, localFile.getAbsolutePath(), null);
        int rc = task.get().intValue();
        assertEquals("Copying finished with rc != 0: ", 0, rc);
        String content = readFile(localFile);
        String text2search = "printf";
        assertTrue("The copied file (" + localFile + ") does not contain \"" + text2search + "\"",
                content.indexOf(text2search) >= 0);
        localFile.delete();
    }
    
    public static Test suite() {
        return new RemoteDevelopmentTest(DownloadTestCase.class);
    }
}
