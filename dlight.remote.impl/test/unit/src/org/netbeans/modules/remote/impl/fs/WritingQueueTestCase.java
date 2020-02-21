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
package org.netbeans.modules.remote.impl.fs;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class WritingQueueTestCase extends RemoteFileTestBase {

    public WritingQueueTestCase(String testName) {
        super(testName);
    }
    
    public WritingQueueTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testMultipleWrite() throws Exception {
        String tempFile = null;
        try {
            tempFile = mkTempAndRefreshParent();
            FileObject fo = getFileObject(tempFile);
            
            StringBuilder ref = new StringBuilder();
            for (int i = 0; i < 200000; i++) {
                ref.append(' ');                
            }            

            final int triesCount = 8;
            
            for (int i = 0; i < triesCount; i++) {
                ref.replace(0, ref.length()-1, "" + i);
                writeFile(fo, ref);
            }
            String readContent = ProcessUtils.execute(execEnv, "cat", tempFile).getOutputString();
            if (!readContent.contentEquals(ref)) {
                assertTrue("File content differ: expected " + ref.substring(0, 32) + 
                        "... but was " + readContent.substring(0, 32) + "...", false);
            }
            
        } finally {
            if (tempFile != null) {
                removeRemoteDirIfNotNull(tempFile);
            }
        }
    }
    
    // see #198200 - Deadlock after closing full remote project 
    @ForAllEnvironments
    public void testNonBlockingWrite() throws Exception {
        String tempFile = null;
        try {
            tempFile = mkTempAndRefreshParent();
            FileObject fo = getFileObject(tempFile);
            
            final String data = new String(new byte[200000]);

            int failuresCount = 0;
            final int triesCount = 4;
            final int ratio = 10;
            
            for (int i = 0; i < triesCount; i++) {
                
                long time1 = System.currentTimeMillis();
                writeFile(fo, data);
                time1 = System.currentTimeMillis() - time1;

                long time2 = System.currentTimeMillis();
                writeFile(fo, data);
                time2 = System.currentTimeMillis() - time2;

                System.err.printf("First: %d curr: %d\n", time1, time2);
                if (time2 > time1 * ratio) {
                    failuresCount++;
                }

            }
            
            if (failuresCount == triesCount) {
                assertTrue("2-nd write is bo", false);
            }
            
        } finally {
            if (tempFile != null) {
                removeRemoteDirIfNotNull(tempFile);
            }
        }
    }

    
    public static Test suite() {
        return RemoteApiTest.createSuite(WritingQueueTestCase.class);
    }
}
