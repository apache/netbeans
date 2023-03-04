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

package org.netbeans.modules.git;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.git.utils.GitUtils;

/**
 *
 * @author ondra
 */
public class GitUtilsTest extends NbTestCase {

    public GitUtilsTest (String name) {
        super(name);
    }

    public void testPrepareRootFiles () throws IOException {
        Set<File> roots = new HashSet<File>();
        Set<File> expectedRoots = new HashSet<File>();
        File repository = getWorkDir();

        // single file
        File file = new File(repository, "folder/folder/file");
        expectedRoots.add(file);
        assertFalse(GitUtils.prepareRootFiles(repository, roots, file));
        assertEquals(expectedRoots, roots);

        // sibling
        File file2 = new File(repository, "folder/folder/file2");
        expectedRoots.add(file2);
        assertFalse(GitUtils.prepareRootFiles(repository, roots, file2));
        assertEquals(expectedRoots, roots);
        assertTrue(GitUtils.prepareRootFiles(repository, roots, file2));
        assertEquals(expectedRoots, roots);

        // another subtree
        File file3 = new File(repository, "folder/folder2/file");
        expectedRoots.add(file3);
        assertFalse(GitUtils.prepareRootFiles(repository, roots, file3));
        assertEquals(expectedRoots, roots);
        File file4 = new File(repository, "folder/folder2/file2");
        expectedRoots.add(file4);
        assertFalse(GitUtils.prepareRootFiles(repository, roots, file4));
        assertEquals(expectedRoots, roots);

        // parent folder
        File folder1 = new File(repository, "folder/folder");
        expectedRoots.add(folder1);
        expectedRoots.remove(file);
        expectedRoots.remove(file2);
        assertFalse(GitUtils.prepareRootFiles(repository, roots, folder1));
        assertEquals(expectedRoots, roots);

        // child file
        assertTrue(GitUtils.prepareRootFiles(repository, roots, file));
        assertTrue(GitUtils.prepareRootFiles(repository, roots, file2));
        assertEquals(expectedRoots, roots);

        // parent folder
        File folder2 = new File(repository, "folder/folder2");
        expectedRoots.add(folder2);
        expectedRoots.remove(file3);
        expectedRoots.remove(file4);
        assertFalse(GitUtils.prepareRootFiles(repository, roots, folder2));
        assertEquals(expectedRoots, roots);

        // child file
        assertTrue(GitUtils.prepareRootFiles(repository, roots, file3));
        assertTrue(GitUtils.prepareRootFiles(repository, roots, file4));
        assertEquals(expectedRoots, roots);

        expectedRoots.clear();
        expectedRoots.add(repository);
        assertFalse(GitUtils.prepareRootFiles(repository, roots, repository));
        assertEquals(expectedRoots, roots);
    }

}
