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
public class FastRefreshTestCase extends RemoteFileTestBase {

    public FastRefreshTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testFastRefresh() throws Exception {
        getFileObject("/usr").getChildren();
        getFileObject("/usr/include").getChildren();
//        getFileObject("/usr/bin").getChildren();
//        getFileObject("/tmp").getChildren();
        doTestFastRefresh(false);
        doTestFastRefresh(true);
   }

    private long doTestFastRefresh(boolean fast) throws Exception {
        //RemoteLogger.getInstance().setLevel(Level.ALL);
        String dir = null;
        try {
            dir = mkTempAndRefreshParent(true);
            String file1 = "file1.dat";
            runScript("echo xxx > " + dir + '/' + file1);
            RemoteDirectory dirFO = (RemoteDirectory) getFileObject(dir).getImplementor();            
            getFileObject(dirFO.getOwnerFileObject(), file1);
            int prevSyncCount = fs.getDirSyncCount();
            String file2 = "file2.dat";
            runScript("echo xxx > " + dir + '/' + file2);
            FileObject fo2 = dirFO.getOwnerFileObject().getFileObject(file2);
            assertNull("should be null now?!", fo2);
            StopWatch sw1 = new StopWatch(dirFO.getOwnerFileObject(), fast).start();
            if (fast) {
                RemoteFileSystemTransport.refreshFast(dirFO, false);
            } else {
                dirFO.refresh();
            }
            sw1.stop(true);
            fo2 = dirFO.getOwnerFileObject().getFileObject(file2);
            assertNotNull("Should not be null b refresh", fo2);
            assertEquals("Dir. sync count differs", prevSyncCount+1, fs.getDirSyncCount());
            // the same, just check that the directory is not synchronized once more
            fo2 = dirFO.getOwnerFileObject().getFileObject(file2);
            assertNotNull("Should not be null after refresh", fo2);
            assertEquals("Dir. sync count differs", prevSyncCount+1, fs.getDirSyncCount());
            String file3 = "file3.dat";
            runScript("echo xxx > " + dir + '/' + file3);
            FileObject fo3 = dirFO.getOwnerFileObject().getFileObject(file3);
            assertNull("should be null now?!", fo3);
            StopWatch sw2 = new StopWatch(rootFO, fast).start();
            if (fast) {
                RemoteFileSystemTransport.refreshFast((RemoteDirectory) rootFO.getImplementor(), false);
            } else {
                rootFO.refresh();
            }
            sw2.stop(true);
            fo3 = dirFO.getOwnerFileObject().getFileObject(file3);
            assertNotNull("Should not be null after root refresh", fo3);
            final long res = sw2.getTime();
            
            for (int i = 0; i < 3; i++) {
                StopWatch sw3 = new StopWatch(rootFO, fast).start();
                if (fast) {
                    RemoteFileSystemTransport.refreshFast((RemoteDirectory) rootFO.getImplementor(), false);
                } else {
                    rootFO.refresh();
                }
                sw3.stop(true);
            }
            
            return res;
        } finally {
            removeRemoteDirIfNotNull(dir);
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(FastRefreshTestCase.class);
    }
    
    /**
     * NB: thread unsafe!
     */
    private class StopWatch {

        private long time;
        private int dirSyncCount;
        private int foCount;
        
        private long lastStart;
        private int lastDirSyncCount;
        private int lastFoCount;

        private boolean started;
        
        private final RemoteFileObject fo;
        private final boolean fast;

        public StopWatch(RemoteFileObject fo, boolean fast) {
            time = 0;
            this.fo = fo;
            this.fast = fast;
            this.started = false;
        }

        public final StopWatch start() {
            lastStart = System.currentTimeMillis();            
            lastDirSyncCount = fs.getDirSyncCount();
            lastFoCount = rootFO.getFileSystem().getCachedFileObjectsCount();
            started = true;
            return this;
        }

        public long stop() {
            if (started) {
                started = false;
            } else {
                throw new IllegalStateException("Trying to stop a stopwatch that was not started");
            }
            return stop(false);
        }

        public long stop(boolean report) {
            time += System.currentTimeMillis() - lastStart;
            dirSyncCount += fs.getDirSyncCount() - lastDirSyncCount;
            foCount += rootFO.getFileSystem().getCachedFileObjectsCount() - lastFoCount;
            if (report) {
                report();
            }
            return time;
        }

        public long report() {
            String text = (fast ? "[Fast] " : "[Slow] ") + "refresh for " + 
                    fo.getExecutionEnvironment().getDisplayName() + ':' +  
                    (fo.getPath().isEmpty() ? "/" : fo.getPath()) + 
                    " [FOs: " + rootFO.getFileSystem().getCachedFileObjectsCount() +
                    " SYNCs: " + dirSyncCount + 
                    " New FOs: " + foCount + "]";
            System.err.println(' ' + text + ' ' + time + " ms");
            return time;
        }

        public long getTime() {
            return time;
        }
    }        
}
