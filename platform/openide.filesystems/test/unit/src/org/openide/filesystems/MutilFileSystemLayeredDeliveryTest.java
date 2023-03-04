/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.filesystems;

import java.io.IOException;
import java.net.URL;
import org.netbeans.junit.*;


public class MutilFileSystemLayeredDeliveryTest extends NbTestCase {
    MultiFileSystem down;
    MultiFileSystem top;
    LocalFileSystem lfs;

    public MutilFileSystemLayeredDeliveryTest(String test) {
        super(test);
    }

    @Override
    protected void setUp() throws Exception {
        down = new MultiFileSystem();
        FileSystem mfs = new MultiFileSystem(down);
        for (int i = 0; i < 5; i++) {
            mfs = new MultiFileSystem(mfs);
        }
        lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        top = new MultiFileSystem(lfs, mfs);
    }

    public void testHowManyChanges() throws Exception {
        L l = new L();
        L l2 = new L();

        FileObject fo = FileUtil.createFolder(top.getRoot(), "testMethodValue");
        FileObject mv = FileUtil.createFolder(top.getRoot(), "testMV");
        fo.addFileChangeListener(l);
        mv.addFileChangeListener(l);
        top.getRoot().addFileChangeListener(l2);
        assertEquals("No children yet", 0, fo.getChildren().length);
        assertNull("No value for ", fo.getAttribute("value1"));
        assertEquals("No children yet", 0, mv.getChildren().length);
        assertNull("No value for ", mv.getAttribute("value1"));


        final FileSystem fs = FileUtil.createMemoryFileSystem();
        down.setDelegates(fs);
        FileUtil.runAtomicAction(new Runnable() {
            public void run() {
                try {
                    final FileObject tmv = fs.getRoot().createFolder("testMethodValue");
                    final FileObject tmv2 = fs.getRoot().createFolder("testMV");
                    for (int i = 0; i < 10; i++) {
                        tmv.setAttribute("value" + i, i);
                        tmv2.setAttribute("value" + i, i);
                    }
                } catch (IOException ex) {
                    fail(ex.getMessage());
                }
            }
        });

        assertNotNull("Some value for ", fo.getAttribute("value1"));

        assertEquals("Ten changes", 20, l.change);
        assertEquals("One Run", 1, l.run);
        assertEquals("Twenty changes", 20, l2.change);
        assertEquals("One Run", 1, l2.run);

        l.change = 0;
        l.run = 0;
        l2.change = 0;
        l2.run = 0;

        final FileSystem fs2 = FileUtil.createMemoryFileSystem();
        final FileObject tmv = fs2.getRoot().createFolder("testMethodValue");
        final FileObject tmv2 = fs2.getRoot().createFolder("testMV");
        down.setDelegates(fs2);

        assertEquals("No attributes", false, fo.getAttributes().hasMoreElements());
        assertEquals("No attributes", false, mv.getAttributes().hasMoreElements());

        assertEquals("Two changes", 2, l.change);
        assertEquals("One Run", 1, l.run);
        assertEquals("One 2Run", 1, l2.run);
    }

    private static class L implements FileChangeListener, Runnable {
        int change;
        int run;

        private void event(FileEvent ev) {
            change++;
            int prev = run;
            ev.runWhenDeliveryOver(this);
            if (prev != run) {
                assertEquals("Delivery is not immediate", prev, run);
            }
        }

        public void run() {
            run++;
        }


        public void fileFolderCreated(FileEvent fe) {
            event(fe);
        }

        public void fileDataCreated(FileEvent fe) {
            event(fe);
        }

        public void fileChanged(FileEvent fe) {
            event(fe);
        }

        public void fileDeleted(FileEvent fe) {
            event(fe);
        }

        public void fileRenamed(FileRenameEvent fe) {
            event(fe);
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            event(fe);
        }
    } // end of L
}
