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

/**
 * Subset of FileUtilTestHidden tests that are applicable only for local FS 
 * @author Alexander Simon
 */
public class FileUtilJavaIOFileHidden extends TestBaseHid {

    private FileObject root = null;

    @Override
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
    public FileUtilJavaIOFileHidden(String name) {
        super(name);
    }

    public void testToFile() throws Exception {
        if (this.testedFS instanceof JarFileSystem) {
            return;
        }
        assertNotNull(root);
        FileObject testFo = root.getFileObject("fileutildir/tofile.txt");
        assertNotNull(testFo);

        File testFile = FileUtil.toFile(testFo);
        assertNotNull(testFile);
        assertTrue(testFile.exists());
    }

    public void testToFileObject() throws Exception {
        if (this.testedFS instanceof JarFileSystem) {
            return;
        }
        assertNotNull(root);
        FileObject testFo = root.getFileObject("fileutildir/tofileobject.txt");
        assertNotNull(testFo);

        File rootFile = FileUtil.toFile(root);
        assertNotNull(rootFile);
        assertTrue(rootFile.exists());

        File testFile = new File(rootFile, "fileutildir/tofileobject.txt");
        assertNotNull(testFile);
        assertTrue(testFile.exists());

        FileObject testFo2 = FileUtil.toFileObject(testFile);
        assertNotNull(testFo2);
        assertEquals(testFo2, testFo);
    }
}
