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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import junit.framework.Test;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class RemoteFileSystemParallelReadTestCase extends RemoteFileSystemParallelTestBase {

    public RemoteFileSystemParallelReadTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @RandomlyFails
    @ForAllEnvironments
    public void testParallelRead() throws Exception {

        if (Utilities.isMac()) { /// for somoe reason @RandomlyFails does NOT work on Mac
            return;
        }

        final String absPath = "/usr/include/stdio.h";

        final AtomicLong size = new AtomicLong(-1);
        final AtomicReference<FileObject> fileObjectRef = new AtomicReference<>();

        class Worker extends ParallelTestWorker {

            public Worker(String name, int threadCount) {
                super(name, threadCount);
            }

            @Override
            protected void work() throws Exception {
                FileObject fo = getFileObject(absPath);
                assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isValid());
                if (!fileObjectRef.compareAndSet(null, fo)) {
                    FileObject prevInstance = fileObjectRef.get();
                    if (fo != prevInstance) {
                        assertTrue("Different file object instances for " + absPath + ": " + prevInstance + " and " + fo, false);
                    }
                }
                String content = readFile(fo);
                int currSize = content.length();
                size.compareAndSet(-1, currSize);
                String text2search = "printf";
                assertTrue("Can not find \"" + text2search + "\" in " + getFileName(execEnv, absPath),
                        content.indexOf(text2search) >= 0);
                // size reported by file system and size of text in characters differ
                // TODO:rfs think out how to check the size
                //assertEquals("File size for " + absPath + " differ", size.get(), currSize);
            }
        }

        Worker worker = new Worker(absPath, 20);
        fs.resetStatistic();
        doTest(worker);
        assertEquals("Dir. sync count differs", 3, fs.getDirSyncCount());
        assertEquals("File transfer count differs", 1, fs.getFileCopyCount());
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(RemoteFileSystemParallelReadTestCase.class);
    }
}
