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
package org.netbeans.modules.masterfs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author jhavlin
 */
public class FileObjectCyclicSymlinksTest extends NbTestCase {

    private static final Logger LOG
            = Logger.getLogger(FileObjectCyclicSymlinksTest.class.getName());

    public FileObjectCyclicSymlinksTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        deleteChildren(getWorkDir());
        clearWorkDir();
    }

    @Override
    protected void tearDown() throws Exception {
        deleteChildren(getWorkDir());
        clearWorkDir();
    }

    /**
     * Test that symbolic links are handled correctly, without infinite
     * recursion.
     *
     * Let's use the following data structure:
     * <pre>
     * f1
     *   f2
     *     f3
     *       f4
     *         f5 (symbolic link with target = f1)
     * </pre>
     *
     * @throws java.io.IOException
     */
    public void test218795() throws IOException {

        File rootF = getWorkDir();
        createDirTreeWithChangingNames(rootF, 5);

        FileObject rootFO = FileUtil.toFileObject(rootF);
        assertNotNull(rootFO);
        assertEquals(rootF, FileUtil.toFile(rootFO));
        rootFO.refresh();
        assertEquals(4, countRecursiveChildren(rootFO));
    }

    /**
     * Count number of children returned by FileObject.getChildren(true). Limit
     * the number to a reasonable constant.
     */
    private int countRecursiveChildren(FileObject root) {
        Enumeration<? extends FileObject> children = root.getChildren(true);
        int count = 0;
        LOG.log(Level.FINER, "Enumerating recursive children under {0}", root);
        while (children.hasMoreElements()) {
            FileObject child = children.nextElement();
            assertNotNull(child);
            File f = FileUtil.toFile(child);
            LOG.log(Level.FINER, "Found child: {0}", f.getPath());
            assertNotNull(f);
            count++;
            if (count > 100) {
                break;
            }
        }
        return count;
    }

    /**
     * Print info about skipped test.
     */
    private void printSkipped() {
        LOG.log(Level.INFO, "Skipping {0}", getName());
    }

    /**
     * Create a directory "tree" where folder have increasing names, e.g.
     * f1/f2/f3/f4. The last folder is a symlink to the first folder.
     *
     * @param root Root directory
     * @param depth Depth of the tree.
     * @return The symbolic link on success, null otherwise.
     */
    private File createDirTreeWithChangingNames(File root, int depth) {
        return createDirTree(root, depth, "f", true);
    }

    /**
     * Create directory tree with one cyclic symbolic link.
     */
    private File createDirTree(File root, int depth, String name,
            boolean appendLevelToName) {

        assert depth > 1;
        File parent = root;
        File first = null;

        for (int i = 1; i <= depth; i++) {
            File file = new File(parent, name + (appendLevelToName ? i : ""));
            if (i == depth) {
                Path newLink = Paths.get(Utilities.toURI(file));
                Path target = Paths.get(Utilities.toURI(first));
                try {
                    Files.createSymbolicLink(newLink, target);
                    return file;
                } catch (IOException ex) {
                    return null;
                }
            } else {
                file.mkdir();
                parent = file;
                if (i == 1) {
                    first = file;
                }
            }
        }
        return null;
    }

    /**
     * Test directory tree with a indirect cyclic link.
     * <pre>
     *  a
     *    b
     *      c ( -> a)
     *    d
     *      e ( -> c)
     * </pre>
     *
     * @throws java.io.IOException
     */
    public void testIndirectSymbolicLink() throws IOException {
        File a = new File(getWorkDir(), "a");
        File b = new File(a, "b");
        File c = new File(b, "c");
        File d = new File(a, "d");
        File e = new File(d, "e");
        assertTrue(b.mkdirs());
        assertTrue(d.mkdirs());
        try {
            Files.createSymbolicLink(c.toPath(), a.toPath());
            Files.createSymbolicLink(e.toPath(), c.toPath());
            FileObject fo = FileUtil.toFileObject(getWorkDir());
            assertEquals(3, countRecursiveChildren(fo));
        } catch (IOException ex) {
            printSkipped();
        }
    }

    /**
     * Test allowed (non-cyclic) symlink.
     * <pre>
     * a
     *   b
     *     c ( -> d)
     *   d
     *     e
     *       f
     * </pre>
     *
     * @throws java.io.IOException
     */
    public void testAllowedSymbolicLink() throws IOException {
        File a = new File(getWorkDir(), "a");
        File b = new File(a, "b");
        File c = new File(b, "c");
        File d = new File(a, "d");
        File e = new File(d, "e");
        File f = new File(e, "f");
        assertTrue(b.mkdirs());
        assertTrue(f.mkdirs());
        try {
            Files.createSymbolicLink(c.toPath(), d.toPath());
            assertEquals(3, countRecursiveChildren(FileUtil.toFileObject(b)));
        } catch (IOException ex) {
            printSkipped();
        }
    }

    /**
     * Delete all child files and directories in a directory.
     */
    private void deleteChildren(File root) throws IOException {
        for (File f : root.listFiles()) {
            deleteFile(f);
        }
    }

    /**
     * Recursively delete directory, do not throw an exception if a file cannot
     * be deleted, do not traverse symbolic links.
     */
    private static void deleteFile(File file) throws IOException {
        if (file.isDirectory() && file.equals(file.getCanonicalFile())
                && !Files.isSymbolicLink(file.toPath())) {
            // file is a directory - delete sub files first
            File files[] = file.listFiles();
            for (File file1 : files) {
                deleteFile(file1);
            }
        }
        // file is a File :-)
        file.delete();
    }
}
