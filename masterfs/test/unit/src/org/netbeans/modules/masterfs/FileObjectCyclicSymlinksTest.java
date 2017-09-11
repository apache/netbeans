/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
