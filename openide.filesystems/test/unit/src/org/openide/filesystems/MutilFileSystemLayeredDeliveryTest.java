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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
