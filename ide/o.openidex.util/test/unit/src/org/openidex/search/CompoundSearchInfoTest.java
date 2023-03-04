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

package org.openidex.search;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;

/**
 *
 * @author  Marian Petras
 */
public class CompoundSearchInfoTest extends NbTestCase {
    
    public CompoundSearchInfoTest(String testName) {
        super(testName);
    }
    
    public void setUp() {
        MockServices.setServices(new Class[] {DummyDataLoader.class});
    }

    public void testNullArgument() {
        try {
            new CompoundSearchInfo(null);
            fail("constructor of CompoundSearchInfo should throw" +
                    " an IllegalArgumentException when null is passed");
        } catch (IllegalArgumentException ex) {
            //correct
        } catch (Exception ex) {
            fail("constructor of CompoundSearchInfo should throw" +
                    " an IllegalArgumentException when null is passed" +
                    " - different type of exception was thrown: "
                    + ex.getClass().getName());
        }
    }
    
    public void testEmptyList() {
        SearchInfo.Files searchInfo = new CompoundSearchInfo(new SearchInfo[0]);
        assertFalse(searchInfo.canSearch());
        assertFalse(searchInfo.objectsToSearch().hasNext());
        assertFalse(searchInfo.filesToSearch().hasNext());
    }
    
    public void testOneItemList() throws IOException {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject fsRoot = fs.getRoot();
        
        FileObject dir = fsRoot.createFolder("dir");
        dir.createData("a", DummyDataLoader.dummyExt);
        dir.createData("b", DummyDataLoader.dummyExt);
        dir.createData("c", DummyDataLoader.dummyExt);
        DataFolder folder = DataFolder.findFolder(dir);
        
        
        SearchInfo refSearchInfo;
        SearchInfo testSearchInfo;
        Iterator refIt;
        Iterator testIt;

        Set testSet = new HashSet();
        
        refSearchInfo = new SimpleSearchInfo(folder, false, null);
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo});
        assertTrue(testSearchInfo.canSearch());
        
        for(testIt = testSearchInfo.objectsToSearch(); testIt.hasNext();){
            testSet.add(testIt.next());
        }
        refIt = refSearchInfo.objectsToSearch();
        while (refIt.hasNext()) {
            assertTrue(testSet.remove(refIt.next()));
        }
        assertTrue(testSet.isEmpty());

        refSearchInfo = new SimpleSearchInfo(folder, false, null) {
            public boolean canSearch() {
                return false;
            }
        };
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo});
        assertEquals(refSearchInfo.canSearch(), testSearchInfo.canSearch());
    }

    public void testOneItemFilesList() throws IOException {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject fsRoot = fs.getRoot();

        FileObject dir = fsRoot.createFolder("dir");
        dir.createData("a", DummyDataLoader.dummyExt);
        dir.createData("b", DummyDataLoader.dummyExt);
        dir.createData("c", DummyDataLoader.dummyExt);
        DataFolder folder = DataFolder.findFolder(dir);

        SearchInfo.Files refSearchInfo;
        SearchInfo.Files testSearchInfo;
        Iterator refIt;
        Iterator testIt;
        Set testSet = new HashSet();

        refSearchInfo = new SimpleSearchInfo(folder, false, null);
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo});
        assertTrue(testSearchInfo.canSearch());

        for(testIt = testSearchInfo.filesToSearch(); testIt.hasNext();){
            testSet.add(testIt.next());
        }
        refIt = refSearchInfo.filesToSearch();
        while (refIt.hasNext()) {
            assertTrue(testSet.remove(refIt.next()));
        }
        assertTrue(testSet.isEmpty());

        refSearchInfo = new SimpleSearchInfo(folder, false, null) {
            public boolean canSearch() {
                return false;
            }
        };
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo});
        assertEquals(refSearchInfo.canSearch(), testSearchInfo.canSearch());
    }
    
    public void testMultipleItemsList() throws IOException {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject fsRoot = fs.getRoot();
        
        FileObject dir1 = fsRoot.createFolder("dir1");
        dir1.createData("1a", DummyDataLoader.dummyExt);
        dir1.createData("1b", DummyDataLoader.dummyExt);
        dir1.createData("1c", DummyDataLoader.dummyExt);
        DataFolder folder1 = DataFolder.findFolder(dir1);
        
        FileObject dir2 = fsRoot.createFolder("dir2");
        dir2.createData("2a", DummyDataLoader.dummyExt);
        dir2.createData("2b", DummyDataLoader.dummyExt);
        DataFolder folder2 = DataFolder.findFolder(dir2);
        
        
        SearchInfo refSearchInfo1, refSearchInfo2;
        SearchInfo testSearchInfo;
        Iterator refIt;
        Iterator testIt;
        
        refSearchInfo1 = new SimpleSearchInfo(folder1, false, null);
        refSearchInfo2 = new SimpleSearchInfo(folder2, false, null);
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo1,
                                                                  refSearchInfo2});
        assertTrue(testSearchInfo.canSearch());
        

        Set testSet = new HashSet();
        for(testIt = testSearchInfo.objectsToSearch(); testIt.hasNext();){
            testSet.add(testIt.next());
        }
        refIt = refSearchInfo1.objectsToSearch();
        while (refIt.hasNext()) {
            assertTrue(testSet.remove(refIt.next()));
        }
        refIt = refSearchInfo2.objectsToSearch();
        while (refIt.hasNext()) {
            assertTrue(testSet.remove(refIt.next()));
        }
        
        assertTrue(testSet.isEmpty());
        
        refSearchInfo1 = new SimpleSearchInfo(folder1, false, null);
        refSearchInfo2 = new SimpleSearchInfo(folder2, false, null) {
            public boolean canSearch() {
                return false;
            }
        };
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo1,
                                                                  refSearchInfo2});
        assertTrue(testSearchInfo.canSearch());
        
        testSet.clear();
        for(testIt = testSearchInfo.objectsToSearch(); testIt.hasNext();){
            testSet.add(testIt.next());
        }
        refIt = refSearchInfo1.objectsToSearch();
        while (refIt.hasNext()) {
            assertTrue(testSet.remove(refIt.next()));
        }
        assertTrue(testSet.isEmpty());
        
        
        refSearchInfo1 = new SimpleSearchInfo(folder1, false, null) {
            public boolean canSearch() {
                return false;
            }
        };
        refSearchInfo2 = new SimpleSearchInfo(folder2, false, null);
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo1,
                                                                  refSearchInfo2});
        assertTrue(testSearchInfo.canSearch());
        
        testSet.clear();
        for(testIt = testSearchInfo.objectsToSearch(); testIt.hasNext();){
            testSet.add(testIt.next());
        }
        refIt = refSearchInfo2.objectsToSearch();
        while (refIt.hasNext()) {
            assertTrue(testSet.remove(refIt.next()));
        }
        assertTrue(testSet.isEmpty());
        
        
        refSearchInfo1 = new SimpleSearchInfo(folder1, false, null) {
            public boolean canSearch() {
                return false;
            }
        };
        refSearchInfo2 = new SimpleSearchInfo(folder2, false, null) {
            public boolean canSearch() {
                return false;
            }
        };
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo1,
                                                                  refSearchInfo2});
        assertFalse(testSearchInfo.canSearch());
    }

    public void testMultipleItemsFilesList() throws IOException {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject fsRoot = fs.getRoot();

        FileObject dir1 = fsRoot.createFolder("dir1");
        dir1.createData("1a", DummyDataLoader.dummyExt);
        dir1.createData("1b", DummyDataLoader.dummyExt);
        dir1.createData("1c", DummyDataLoader.dummyExt);
        DataFolder folder1 = DataFolder.findFolder(dir1);

        FileObject dir2 = fsRoot.createFolder("dir2");
        dir2.createData("2a", DummyDataLoader.dummyExt);
        dir2.createData("2b", DummyDataLoader.dummyExt);
        DataFolder folder2 = DataFolder.findFolder(dir2);


        SearchInfo.Files refSearchInfo1, refSearchInfo2;
        SearchInfo.Files testSearchInfo;
        Iterator refIt;
        Iterator testIt;

        refSearchInfo1 = new SimpleSearchInfo(folder1, false, null);
        refSearchInfo2 = new SimpleSearchInfo(folder2, false, null);
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo1,
                                                                  refSearchInfo2});
        assertTrue(testSearchInfo.canSearch());

        Set testSet = new HashSet();
        for(testIt = testSearchInfo.filesToSearch(); testIt.hasNext();){
            testSet.add(testIt.next());
        }

        refIt = refSearchInfo1.filesToSearch();
        while (refIt.hasNext()) {
            assertTrue(testSet.remove(refIt.next()));
        }
        refIt = refSearchInfo2.filesToSearch();
        while (refIt.hasNext()) {
            assertTrue(testSet.remove(refIt.next()));
        }
        assertTrue(testSet.isEmpty());


        refSearchInfo1 = new SimpleSearchInfo(folder1, false, null);
        refSearchInfo2 = new SimpleSearchInfo(folder2, false, null) {
            public boolean canSearch() {
                return false;
            }
        };
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo1,
                                                                  refSearchInfo2});
        assertTrue(testSearchInfo.canSearch());

        testSet.clear();
        for(testIt = testSearchInfo.filesToSearch(); testIt.hasNext();){
            testSet.add(testIt.next());
        }

        refIt = refSearchInfo1.filesToSearch();
        while (refIt.hasNext()) {
            assertTrue(testSet.remove(refIt.next()));
        }
        assertTrue(testSet.isEmpty());


        refSearchInfo1 = new SimpleSearchInfo(folder1, false, null) {
            public boolean canSearch() {
                return false;
            }
        };
        refSearchInfo2 = new SimpleSearchInfo(folder2, false, null);
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo1,
                                                                  refSearchInfo2});
        assertTrue(testSearchInfo.canSearch());

        testSet.clear();
        for(testIt = testSearchInfo.filesToSearch(); testIt.hasNext();){
            testSet.add(testIt.next());
        }
        refIt = refSearchInfo2.filesToSearch();
        while (refIt.hasNext()) {
            assertTrue(testSet.remove(refIt.next()));
        }
        assertTrue(testSet.isEmpty());


        refSearchInfo1 = new SimpleSearchInfo(folder1, false, null) {
            public boolean canSearch() {
                return false;
            }
        };
        refSearchInfo2 = new SimpleSearchInfo(folder2, false, null) {
            public boolean canSearch() {
                return false;
            }
        };
        testSearchInfo = new CompoundSearchInfo(new SearchInfo[] {refSearchInfo1,
                                                                  refSearchInfo2});
        assertFalse(testSearchInfo.canSearch());
   }
}
