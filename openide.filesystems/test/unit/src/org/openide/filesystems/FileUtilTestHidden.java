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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import org.openide.util.BaseUtilities;

public class FileUtilTestHidden extends TestBaseHid {

    private FileObject root = null;

    protected String[] getResources(String testName) {
        return new String[]{
                    "fileutildir/tofile.txt",
                    "fileutildir/tofileobject.txt",
                    "fileutildir/isParentOf.txt",
                    "fileutildir/fileutildir2/fileutildir3",
                    "fileutildir/fileutildir2/folder/file"
                };
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void setUp() throws Exception {
        super.setUp();
        Repository.getDefault().addFileSystem(testedFS);
        root = testedFS.findResource(getResourcePrefix());
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void tearDown() throws Exception {
        Repository.getDefault().removeFileSystem(testedFS);
        super.tearDown();
    }

    /** Creates new FileObjectTestHidden */
    public FileUtilTestHidden(String name) {
        super(name);
    }

    private static class TestListener extends FileChangeAdapter {

        boolean wasCalled = false;

        @Override
        public void fileFolderCreated(FileEvent fe) {
            wasCalled = true;
        }
    }

    public void testRunAtomicAction() throws Exception {
        final LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());

        final FileSystem defSystem = FileUtil.getConfigRoot().getFileSystem();
        final TestListener l = new TestListener();
        try {
            defSystem.addFileChangeListener(l);
            lfs.addFileChangeListener(l);
            assertFalse(l.wasCalled);
            FileUtil.runAtomicAction(new FileSystem.AtomicAction() {

                public void run() throws IOException {
                    assertFalse(l.wasCalled);
                    assertNull(defSystem.getRoot().getFileObject(getName()));
                    assertNotNull(FileUtil.createFolder(defSystem.getRoot(), getName()));
                    assertNull(lfs.getRoot().getFileObject(getName()));
                    assertNotNull(FileUtil.createFolder(lfs.getRoot(), getName()));
                    assertFalse(l.wasCalled);
                }
            });
            assertTrue(l.wasCalled);
        } finally {
            defSystem.removeFileChangeListener(l);
            lfs.removeFileChangeListener(l);
        }
    }

    public void testCreateFolder() throws Exception {
        if (this.testedFS instanceof JarFileSystem) {
            return;
        }
        assertNotNull(root);
        FileObject folder = root.getFileObject("fileutildir");
        FileObject result = FileUtil.createFolder(folder, "fileutildir2/folder/fileutildir4");
        assertNotNull(result);
        assertSame(result, root.getFileObject("fileutildir/fileutildir2/folder/fileutildir4"));
    }

    public void testNormalizeFile() throws Exception {
        File file = getWorkDir();
        file = FileUtil.normalizeFile(file);

        File file2 = FileUtil.normalizeFile(file);
        assertSame(file, file2);

        file2 = new File(file, "test/..");
        file2 = FileUtil.normalizeFile(file);
        assertEquals(file2, file);

        if (BaseUtilities.isUnix()) {
            assertEquals(new File("/"), FileUtil.normalizeFile(new File("/..")));
            assertEquals(new File("/"), FileUtil.normalizeFile(new File("/../.")));
            assertEquals(new File("/tmp"), FileUtil.normalizeFile(new File("/../../tmp")));
        }
    }

    public void testNormalizeFile2() throws Exception {
        if (!BaseUtilities.isWindows()) {
            return;
        }
        File rootFile = FileUtil.toFile(root);
        if (rootFile == null) {
            return;
        }
        assertTrue(rootFile.exists());
        rootFile = FileUtil.normalizeFile(rootFile);

        File testFile = new File(rootFile, "abc.txt");
        assertTrue(testFile.createNewFile());
        assertTrue(testFile.exists());

        File testFile2 = new File(rootFile, "ABC.TXT");
        assertTrue(testFile2.exists());

        assertSame(testFile, FileUtil.normalizeFile(testFile));
        assertNotSame(testFile2, FileUtil.normalizeFile(testFile2));
    }

    public void testFindFreeFolderName() throws Exception {
        if (this.testedFS.isReadOnly()) {
            return;
        }
        String name = FileUtil.findFreeFolderName(root, "fileutildir".toUpperCase());
        root.createFolder(name);
    }

    public void testIsArchiveFile() throws Exception {
        final String base = BaseUtilities.toURI(getWorkDir()).toURL().toExternalForm();
        URL url = new URL(base + "test.jar");    //NOI18N
        assertTrue("test.jar has to be an archive", FileUtil.isArchiveFile(url));  //NOI18N
        url = new URL(base + ".hidden.jar");   //NOI18N
        assertTrue(FileUtil.isArchiveFile(url));  //NOI18N
        url = new URL(base + "folder");    //NOI18N
        assertFalse("folder cannot to be an archive", FileUtil.isArchiveFile(url));   //NOI18N
        url = new URL(base + "folder/");    //NOI18N
        assertFalse("folder cannot to be an archive", FileUtil.isArchiveFile(url));   //NOI18N
        url = new URL(base + ".hidden");   //NOI18N
        assertFalse(".hidden cannot to be an archive", FileUtil.isArchiveFile(url));  //NOI18N
        url = new URL(base + ".hidden/");   //NOI18N
        assertFalse(".hidden cannot to be an archive", FileUtil.isArchiveFile(url));  //NOI18N
    }

    public void testIsParentOf() throws Exception {
        if (this.testedFS instanceof JarFileSystem) {
            return;
        }
        final List<FileEvent> events = new ArrayList<FileEvent>();
        assertNotNull(root);
        final FileObject parent = root.getFileObject("fileutildir");
        assertNotNull(parent);

        final FileObject child = root.getFileObject("fileutildir/isParentOf.txt");
        assertNotNull(child);

        assertTrue(FileUtil.isParentOf(parent, child));

        child.addFileChangeListener(new FileChangeAdapter() {

            @Override
            public void fileDeleted(FileEvent fe) {
                FileObject file = fe.getFile();
                assertTrue(FileUtil.isParentOf(parent, file));
                assertEquals(parent, file.getParent());
                events.add(fe);
            }
        });
        child.delete();
        assertTrue(events.size() == 1);
        assertNull(root.getFileObject("fileutildir/isParentOf.txt"));

    }

    public void testIsParentOf3() throws Exception {
        if (this.testedFS instanceof JarFileSystem) {
            return;
        }
        final List<FileEvent> events = new ArrayList<FileEvent>();
        assertNotNull(root);
        final FileObject[] fileObjects = new FileObject[]{
            root.getFileObject("fileutildir"),
            root.getFileObject("fileutildir/fileutildir2"),
            root.getFileObject("fileutildir/fileutildir2/fileutildir3")
        };

        for (int i = 0; i < fileObjects.length; i++) {
            FileObject fo = fileObjects[i];
            assertNotNull(fo);
        }

        assertTrue(FileUtil.isParentOf(root, fileObjects[0]));
        assertTrue(FileUtil.isParentOf(fileObjects[0], fileObjects[1]));
        assertTrue(FileUtil.isParentOf(fileObjects[1], fileObjects[2]));
        final FileChangeListener fcl = new FileChangeAdapter() {

            @Override
            public void fileDeleted(FileEvent fe) {
                FileObject file = fe.getFile();
                assertNotNull(file.getPath(), file.getParent());
                assertTrue(file.getPath(), FileUtil.isParentOf(root, file));
                events.add(fe);
            }
        };
        try {
            testedFS.addFileChangeListener(fcl);
            fileObjects[1].delete();
            assertTrue(events.size() > 0);
        } finally {
            testedFS.removeFileChangeListener(fcl);
        }
    }

    public void testGetFileDisplayName() throws Exception {
        final FileObject[] fileObjects = new FileObject[]{
            root,
            root.getFileObject("fileutildir"),
            root.getFileObject("fileutildir/fileutildir2")
        };

        for (int i = 0; i < fileObjects.length; i++) {
            FileObject fo = fileObjects[i];
            assertNotNull(fo);
            String displayName = FileUtil.getFileDisplayName(fo);
            File f = FileUtil.toFile(fo);
            if (f != null) {
                assertEquals(f.getAbsolutePath(), displayName);
            } else {
                FileObject archivFo = FileUtil.getArchiveFile(fo);
                File archiv = (archivFo != null) ? FileUtil.toFile(archivFo) : null;
                if (archiv != null) {
                    if (fo.isRoot()) {
                        assertEquals(displayName, archiv.getAbsolutePath());
                    } else {
                        assertTrue(displayName.indexOf(archiv.getAbsolutePath()) != -1);
                    }
                } else {
                    if (fo.isRoot()) {
                        assertEquals(displayName, fo.getFileSystem().getDisplayName());
                    } else {
                        assertTrue(displayName.indexOf(fo.getFileSystem().getDisplayName()) != -1);
                    }
                }
            }
        }
    }
}
