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
