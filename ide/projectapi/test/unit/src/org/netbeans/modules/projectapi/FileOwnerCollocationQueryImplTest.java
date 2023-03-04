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

package org.netbeans.modules.projectapi;

import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;

/**
 *
 * @author mkleint
 */
public class FileOwnerCollocationQueryImplTest extends NbTestCase {
    
    public FileOwnerCollocationQueryImplTest(String testName) {
        super(testName);
    }            
    private FileObject scratch;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(TestUtil.testProjectFactory());
        scratch = TestUtil.makeScratchDir(this);
    }
    /**
     * Test of findRoot method, of class FileOwnerCollocationQueryImpl.
     */
    public void testFindRoot() throws Exception {
        FileObject root =  scratch.createFolder("root");
        FileObject projdir = root.createFolder("prj1");
        projdir.createFolder("testproject");
        
        //root/prj1/foo
        FileOwnerCollocationQueryImpl instance = new FileOwnerCollocationQueryImpl();
        assertEquals(projdir.toURI(), instance.findRoot(projdir.createData("foo").toURI()));
        
        //root/prj2/foo/prj3/bar
        projdir = root.createFolder("prj2");
        FileObject expected = projdir;
        projdir.createFolder("testproject");
        projdir = projdir.createFolder("foo").createFolder("prj3");
        projdir.createFolder("testproject");
        assertEquals(expected.toURI(), instance.findRoot(projdir.createData("bar").toURI()));
        
        //root
        assertEquals(null, instance.findRoot(root.toURI()));
    }

    /**
     * Test of areCollocated method, of class FileOwnerCollocationQueryImpl.
     */
    public void testAreCollocated() throws Exception {
        FileObject root =  scratch.createFolder("root");
        FileObject projdir = scratch.createFolder("prj1");
        projdir.createFolder("testproject");
        FileObject lib = root.createFolder("libs");

        
        FileObject file1 = lib.createData("pron");
        FileObject file2 = projdir.createData("xxx");
        FileOwnerCollocationQueryImpl instance = new FileOwnerCollocationQueryImpl();
        assertFalse(instance.areCollocated(file1.toURI(), file2.toURI()));
        file1 = projdir.createData("pron");
        assertTrue(instance.areCollocated(file1.toURI(), file2.toURI()));
        
        
        file1 = projdir;
        file2 = lib;
        assertFalse(instance.areCollocated(file1.toURI(), file2.toURI()));
        
        projdir = root.createFolder("noproj").createFolder("proj1");
        projdir.createFolder("testproject");
        FileObject projdir2 = root.createFolder("noproj2").createFolder("proj2");
        projdir2.createFolder("testproject");
        file1 = projdir.createData("foo");
        file2 = projdir2.createData("bar");
//        System.out.println("root1=" + instance.findRoot(file1.getURL().toURI()));
//        System.out.println("root2=" + instance.findRoot(file2.getURL().toURI()));
        assertFalse(instance.areCollocated(file1.toURI(), file2.toURI()));
        
    }

}
