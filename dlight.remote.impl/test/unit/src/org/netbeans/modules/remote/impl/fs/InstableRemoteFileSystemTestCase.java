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
public class InstableRemoteFileSystemTestCase extends RemoteFileTestBase {

    private static boolean stop = false;
    private static int iteration = 0;
    
    public InstableRemoteFileSystemTestCase(String testName) {
        super(testName);
    }
    
    public InstableRemoteFileSystemTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    private void doTest2() throws Throwable {
        String tempFile = null;
        try {
            FileObject fo;
            String stdio_h = "/usr/include/stdio.h";
            fo = getFileObject(stdio_h);
            assertFalse("FileObject should NOT be writable: " + fo.getPath(), fo.canWrite());
            tempFile = mkTempAndRefreshParent();
            System.err.printf("TEMP DIR: %s\n", tempFile);
            fo = getFileObject(tempFile);
            assertTrue("FileObject should be writable: " + fo.getPath(), fo.canWrite());
            String content = "a quick brown fox...";
            writeFile(fo, content);
            CharSequence readContent = readFile(fo);
            assertEquals("File content differ", content.toString(), readContent.toString());
            readContent = ProcessUtils.execute(execEnv, "cat", tempFile).getOutputString();
            assertEquals("File content differ", content.toString(), readContent.toString());
        } finally {
            if (tempFile != null) {
                removeRemoteDirIfNotNull(tempFile);
            }
        }
    }

    private void doTest1() throws Throwable {
        if (stop) {
            return;
        }
        iteration++;
        System.err.printf("\n----------\nITERATION %d\n", iteration);
        try {
            doTest2();
        } catch (Throwable thr) {
            stop = true;
            System.err.printf("TEST FAILED ON %d ITERATION\n", iteration);
            throw thr;
        }
    }
    
    @ForAllEnvironments
    public void testWrite() throws Throwable {
        doTest1();
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(InstableRemoteFileSystemTestCase.class, 128);
    }
}
