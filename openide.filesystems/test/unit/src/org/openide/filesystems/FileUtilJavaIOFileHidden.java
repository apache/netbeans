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
