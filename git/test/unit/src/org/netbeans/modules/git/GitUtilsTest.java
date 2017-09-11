/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
