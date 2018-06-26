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

package org.netbeans.modules.php.project;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class ProjectPropertiesSupportTest extends NbTestCase {

    public ProjectPropertiesSupportTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testFindClosestDir0() throws Exception {
        List<FileObject> roots = Arrays.asList(
                getDir("test"),
                getDir("test2"),
                getDir("test3")
        );
        assertSame(roots.get(0), ProjectPropertiesSupport.findClosestDir(roots, null));
    }

    public void testFindClosestDir1() throws Exception {
        FileObject fo = getDir("src/mydir");
        List<FileObject> roots = Arrays.asList(
                getDir("test"),
                getDir("test2"),
                getDir("test3")
        );
        assertSame(roots.get(0), ProjectPropertiesSupport.findClosestDir(roots, fo));
    }

    public void testFindClosestDir2() throws Exception {
        FileObject fo = getDir("mybundle/src/mydir");
        List<FileObject> roots = Arrays.asList(
                getDir("test"),
                getDir("mybundle2/test"),
                getDir("mybundle/test/")
        );
        assertSame(roots.get(2), ProjectPropertiesSupport.findClosestDir(roots, fo));
    }

    public void testFindClosestDir3() throws Exception {
        FileObject fo = getDir("mybundle/src/mydir");
        List<FileObject> roots = Arrays.asList(
                getDir("test"),
                getDir("mybundle/src/mydir/test"),
                getDir("mybundle/src/mydir/hello/test"),
                getDir("mybundle/test/")
        );
        assertSame(roots.get(1), ProjectPropertiesSupport.findClosestDir(roots, fo));
    }

    public void testFindClosestDir4() throws Exception {
        FileObject fo = getDir("mybundle/src/mydir");
        List<FileObject> roots = Arrays.asList(
                getDir("test"),
                getDir("mybundle/src/mydir/hello/test"),
                getDir("mybundle/src/mydir/test"),
                getDir("mybundle/test/")
        );
        assertSame(roots.get(2), ProjectPropertiesSupport.findClosestDir(roots, fo));
    }

    public void testFindClosestDir5() throws Exception {
        FileObject fo = getDir("src/mydir");
        List<FileObject> roots = Arrays.asList(
                getDir("test2"),
                getDir("src/mydir"),
                getDir("test3")
        );
        assertSame(roots.get(1), ProjectPropertiesSupport.findClosestDir(roots, fo));
    }

    private FileObject getDir(String relPath) throws IOException {
        FileObject folder = FileUtil.createFolder(new File(getWorkDir(), relPath));
        assertTrue(folder.getPath(), folder.isFolder());
        return folder;
    }

}
