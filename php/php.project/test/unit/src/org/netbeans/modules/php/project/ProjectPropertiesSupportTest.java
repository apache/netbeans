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
