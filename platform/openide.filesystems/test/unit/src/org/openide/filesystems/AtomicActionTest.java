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

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;

/**
 * Test proving embeding of AtomicActions
 * @author Radek Matous
 */
public class AtomicActionTest extends NbTestCase {
    
    public AtomicActionTest(String name) {
        super(name);
    }
    
    /**
     * Setups variables.
     */
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }
    
    public void testBasic() throws Exception {
        File f = getWorkDir();
        final LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(f);                
        //------------------------
        FileObject root = lfs.getRoot();
        assertNotNull(FileUtil.createData(root, "data"));        
        final FileObject data = root.getFileObject("data");
        assertNotNull(data);
        
        final TestChangeListener tcl = new TestChangeListener();
        assertFalse(tcl.deleteNotification);
        root.addFileChangeListener(tcl);
        assertFalse(tcl.deleteNotification);
        lfs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                lfs.runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        lfs.runAtomicAction(new FileSystem.AtomicAction() {
                            public void run() throws IOException {
                                data.delete();
                                assertFalse(tcl.deleteNotification);
                            }
                        });                        
                        assertFalse(tcl.deleteNotification);
                    }
                });
                assertFalse(tcl.deleteNotification);
            }
        });
        assertTrue(tcl.deleteNotification);
        assertTrue("Notified about end of delivery", tcl.over);
        tcl.reset();
        assertNotNull(FileUtil.createData(root, "data"));        

        final JarFileSystem jfs = new JarFileSystem();
        
        FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                jfs.runAtomicAction(new FileSystem.AtomicAction() {
                    public void run() throws IOException {
                        lfs.runAtomicAction(new FileSystem.AtomicAction() {
                            public void run() throws IOException {
                                data.delete();
                                assertFalse(tcl.deleteNotification);
                            }
                        });                        
                        assertFalse(tcl.deleteNotification);
                    }
                });
                assertFalse(tcl.deleteNotification);
            }
        });
        assertTrue(tcl.deleteNotification);
        assertTrue("Notified about end of delivery", tcl.over);
    }
    
    private static class TestChangeListener extends FileChangeAdapter
    implements Runnable {
        private boolean deleteNotification;
        private boolean over;
        @Override
        public void fileDeleted(FileEvent fe) {
            assertFalse("Delivery of events is not over yet", over);
            deleteNotification = true;
            fe.runWhenDeliveryOver(this);
        }
        public void reset() {
            deleteNotification = false;
            over = false;
        }

        public void run() {
            assertFalse("Over not set yet", over);
            over = true;
        }
    }    
}
