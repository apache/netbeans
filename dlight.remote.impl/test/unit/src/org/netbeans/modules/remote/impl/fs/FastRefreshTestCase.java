/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
