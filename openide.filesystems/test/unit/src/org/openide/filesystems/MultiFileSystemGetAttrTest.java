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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Enumerations;

public class MultiFileSystemGetAttrTest extends NbTestCase {
    private MyLFS fs1;
    private MyLFS fs2;
    private MyLFS fs3;
    private MultiFileSystem mfs;

    public MultiFileSystemGetAttrTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        fs1 = new MyLFS();
        final File f1 = new File(getWorkDir(), "fs1");
        f1.mkdirs();
        fs1.setRootDirectory(f1);
        fs1.getRoot().createFolder("1").createData("d");

        fs2 = new MyLFS();
        final File f2 = new File(getWorkDir(), "fs2");
        f2.mkdirs();
        fs2.setRootDirectory(f2);
        fs2.getRoot().createFolder("2").createData("d");
        fs2.setReadOnly(true);

        fs3 = new MyLFS();
        final File f3 = new File(getWorkDir(), "fs3");
        f3.mkdirs();
        fs3.setRootDirectory(f3);
        fs3.getRoot().createFolder("3").createData("d");
        fs3.setReadOnly(true);


        mfs = new MultiFileSystem(fs1, fs2, fs3);
    }


    public void testNumberOfQueriesOnReadOnlyFSIsZero() {

        FileObject f1 = mfs.findResource("1/d");
        FileObject f2 = mfs.findResource("2/d");
        FileObject f3 = mfs.findResource("3/d");

        assertNull(f1.getAttribute("unknown"));
        assertNull(f2.getAttribute("unknown"));
        assertNull(f3.getAttribute("unknown"));

        assertTrue("No queries on read only fs2: " + fs2.rootQueries, fs2.rootQueries.isEmpty());
        assertTrue("No queries on read only fs3: " + fs3.rootQueries, fs3.rootQueries.isEmpty());
        assertFalse("Some queries on main fs1: " + fs1.rootQueries, fs1.rootQueries.isEmpty());

    }

    public void testBackslashAttrs() throws Exception { // #199043
        FileSystem layers = FileUtil.createMemoryFileSystem();
        FileObject physFolder = FileUtil.createFolder(layers.getRoot(), "sub/dir");
        FileSystem writable = FileUtil.createMemoryFileSystem();
        writable.getRoot().createFolder("sub");
        FileSystem sfs = new MultiFileSystem(writable, layers);
        FileObject virtFolder = sfs.findResource("sub/dir");
        virtFolder.setAttribute("a", true);
        assertNull(physFolder.getAttribute("a"));
        assertEquals(Collections.emptyList(), Collections.list(physFolder.getAttributes()));
        assertEquals(true, writable.getRoot().getAttribute("sub\\dir\\a"));
        assertEquals(Collections.singletonList("sub\\dir\\a"), Collections.list(writable.getRoot().getAttributes()));
        assertEquals(true, virtFolder.getAttribute("a"));
        assertEquals(Collections.singletonList("a"), Collections.list(virtFolder.getAttributes()));
        assertNull(sfs.getRoot().getAttribute("sub\\dir\\a"));
        assertEquals(Collections.emptyList(), Collections.list(sfs.getRoot().getAttributes()));
    }

    public void testCanSetAttributeOnSFS() throws Exception { // #202316
        FileSystem layers = FileUtil.createMemoryFileSystem();
        FileObject physFolder = FileUtil.createFolder(layers.getRoot(), "sub/dir");
        FileSystem writable = FileUtil.createMemoryFileSystem();
        writable.getRoot().createFolder("sub");
        FileSystem sfs = new MultiFileSystem(new MultiFileSystem(writable), layers);
        FileObject virtFolder = sfs.findResource("sub/dir");
        virtFolder.setAttribute("a", true);
        assertEquals(true, virtFolder.getAttribute("a"));
    }

    public static class MyLFS extends LocalFileSystem implements AbstractFileSystem.Attr {
        ArrayList<String> rootQueries = new ArrayList<String>();

        @SuppressWarnings("LeakingThisInConstructor")
        public MyLFS() {
            this.attr = this;
        }

        @Override
        public Object readAttribute(String name, String attrName) {
            if (name.equals("")) {
                rootQueries.add(name);
                rootQueries.add(attrName);
            }
            return null;
        }

        @Override
        public void writeAttribute(String name, String attrName, Object value) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Enumeration<String> attributes(String name) {
            return Enumerations.empty();
        }

        @Override
        public void renameAttributes(String oldName, String newName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void deleteAttributes(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }


    }
}
