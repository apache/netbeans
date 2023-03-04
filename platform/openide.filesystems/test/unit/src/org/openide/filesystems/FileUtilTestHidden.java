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
