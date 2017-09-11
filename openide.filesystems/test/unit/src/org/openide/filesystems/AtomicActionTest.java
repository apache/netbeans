/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
