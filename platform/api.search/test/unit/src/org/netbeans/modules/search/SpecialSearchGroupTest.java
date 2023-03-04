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
package org.netbeans.modules.search;

import java.io.IOException;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author jhavlin
 */
public class SpecialSearchGroupTest extends NbTestCase {
    
    public SpecialSearchGroupTest(String name) {
        super(name);
    }
    
    public void testFilePathAsList() throws IOException {
        
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        
        FileObject a = root.createFolder("a");
        FileObject b = a.createFolder("b");
        FileObject c = b.createData("c");
        
//        List<FileObject> path = SpecialSearchGroup.CommonSearchRoot.filePathAsList(c);
//
//        assertEquals(4, path.size());
//
//        assertEquals(root, path.get(0));
//        assertEquals(a, path.get(1));
//        assertEquals(b, path.get(2));
//        assertEquals(c, path.get(3));
    }
    
    public void testFindCommonPath() throws IOException {
        
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        
        FileObject a = root.createFolder("a");
        FileObject b = a.createFolder("b");
        FileObject c = b.createData("c");
        
        FileObject b1 = a.createFolder("b1");
        FileObject b1c = b1.createData("b1c");
        
//        List<FileObject> path1 = filePathAsList(c);
//        List<FileObject> path2 = filePathAsList(b1c);
//
//        List<FileObject> commonPath = findCommonPath(path1, path2);
//
//        assertEquals(2, commonPath.size());
//
//        assertEquals(root, commonPath.get(0));
//        assertEquals(a, commonPath.get(1));
    }
    
    public void testFindCommonPathNegative() throws IOException {
        
        FileObject root1 = FileUtil.createMemoryFileSystem().getRoot();
        FileObject root2 = FileUtil.createMemoryFileSystem().getRoot();
        
        FileObject a1 = root1.createFolder("a");
        FileObject b1 = a1.createData("b");        
        
        FileObject a2 = root2.createFolder("a");
        FileObject b2 = a2.createData("b");        
        
//        List<FileObject> p1 = filePathAsList(b1);
//        List<FileObject> p2 = filePathAsList(b2);
//
//        List<FileObject> common = findCommonPath(p1, p2);
//
//        assertTrue(common.isEmpty());
    }
    
    public void testFindCommonPathObject() throws IOException {
        
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        
        FileObject a = root.createFolder("a");
        FileObject b = a.createFolder("b");
        FileObject c = b.createData("c");
        
        FileObject b1 = a.createFolder("b1");
        FileObject b1c = b1.createData("b1c");
        
//        SpecialSearchGroup.CommonSearchRoot csr = new SpecialSearchGroup.CommonSearchRoot(1);
//
//        csr.update(c);
//        csr.update(b1c);
//
//        assertEquals(a, csr.getFileObject());
    }
    
    public void testFindCommonPathObjectNegative() throws IOException {
        
        FileObject root1 = FileUtil.createMemoryFileSystem().getRoot();
        FileObject root2 = FileUtil.createMemoryFileSystem().getRoot();
        
        FileObject a1 = root1.createFolder("a");
        FileObject b1 = a1.createData("b");        
        
        FileObject a2 = root2.createFolder("a");
        FileObject b2 = a2.createData("b");        
        
//        SpecialSearchGroup.CommonSearchRoot csr = new SpecialSearchGroup.CommonSearchRoot();
//
//        csr.update(b1);
//        csr.update(b2);
//
//        assertNull(csr.getFileObject());
    }
}
