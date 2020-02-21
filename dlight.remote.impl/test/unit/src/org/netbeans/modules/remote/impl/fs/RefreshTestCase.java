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

import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;
/**
 *
 */
public class RefreshTestCase extends RemoteFileTestBase {

    public RefreshTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }


    @ForAllEnvironments
    public void testDirectoryExplicitRefresh() throws Exception {
        String dir = null;
        try {
            dir = mkTempAndRefreshParent(true);
            String file1 = "file1.dat";
            runScript("echo xxx > " + dir + '/' + file1);
            FileObject dirFO = getFileObject(dir);
            getFileObject(dirFO, file1);

            int prevSyncCount = fs.getDirSyncCount();

            String file2 = "file2.dat";
            runScript("echo xxx > " + dir + '/' + file2);
            FileObject fo2 = dirFO.getFileObject(file2);
            assertNull("should be null now?!", fo2);

            StopWatch sw;
            
            sw = new StopWatch("Refreshing " + dirFO, true);
            dirFO.refresh();
            sw.stop(true);

            fo2 = dirFO.getFileObject(file2);
            assertNotNull("Should not be null after refresh", fo2);
            assertEquals("Dir. sync count differs", prevSyncCount+1, fs.getDirSyncCount());

            // the same, just check that the directory is not synchronized once more
            fo2 = dirFO.getFileObject(file2);
            assertNotNull("Should not be null after refresh", fo2);
            assertEquals("Dir. sync count differs", prevSyncCount+1, fs.getDirSyncCount());

            String file3 = "file3.dat";
            runScript("echo xxx > " + dir + '/' + file3);
            FileObject fo3 = dirFO.getFileObject(file3);
            assertNull("should be null now?!", fo3);
            int totalFileObjectsCount = rootFO.getFileSystem().getCachedFileObjectsCount();
            sw = new StopWatch("Refreshing " + rootFO + " (" + totalFileObjectsCount + " file objects in FS)", true);
            rootFO.refresh();
            sw.stop(true);
            //sleep(2000);
            fo3 = dirFO.getFileObject(file3);
            assertNotNull("Should not be null after root refresh", fo3);
//            assertEquals("Dir. sync count differs", prevSyncCount+2, fs.getDirSyncCount());
        } finally {
            removeRemoteDirIfNotNull(dir);
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(RefreshTestCase.class);
    }
    
    /**
     * NB: thread unsafe!
     */
    private static class StopWatch {

        private long time;
        private long lastStart;
        private final String text;

        public StopWatch(String text) {
            this(text, false);
        }

        public StopWatch(String text, boolean start) {
            time = 0;
            this.text = text;
            if (start) {
                start();
            }
        }

        public final void start() {
            lastStart = System.currentTimeMillis();
        }

        public long stop() {
            return stop(false);
        }

        public long stop(boolean report) {
            time += System.currentTimeMillis() - lastStart;
            if (report) {
                report();
            }
            return time;
        }

        public long report() {
            System.err.println(' ' + text + ' ' + time + " ms");
            return time;
        }

        public long getTime() {
            return time;
        }
    }    
}
