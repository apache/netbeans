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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
