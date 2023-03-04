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
