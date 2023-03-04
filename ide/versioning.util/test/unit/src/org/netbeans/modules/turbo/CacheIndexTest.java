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

package org.netbeans.modules.turbo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.util.Utils;

/**
 *
 * @author Tomas Stupka
 */
public class CacheIndexTest extends NbTestCase {
    private File workDir;
    private File wc;

    public CacheIndexTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {          
        super.setUp();
        workDir = getWorkDir();
        wc = new File(workDir, getName() + "_wc");
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    public void testAddFile() throws MalformedURLException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        CacheIndex ci = new CIndex();
        Map<File, Set<File>> index = getIndex(ci);
        index.clear();

        File root = new File(wc, "root");
        
        File folder1 = new File(root, "folder1");
        File folder11 = new File(folder1, "folder11");
        File folder111 = new File(folder11, "folder111");
        File folder112 = new File(folder11, "folder112");
        folder111.mkdirs();
        folder112.mkdirs();
        File file111_1 = new File(folder111, "file111_1");
        File file111_2 = new File(folder111, "file111_2");
        File file111_3 = new File(folder111, "file111_3");
        File file112_1 = new File(folder112, "file112_1");
        file111_1.createNewFile();
        file111_2.createNewFile();
        file111_3.createNewFile();
        file112_1.createNewFile();

        // add folder11 -> all versioned parents will be added
        ci.add(folder11);
        checkParents(index, folder11);
        
        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});

        // add file111_1 -> all versioned parents will be added
        ci.add(file111_1);
        checkParents(index, file111_1);
        
        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1});

        // add file111_1 one more time -> the structure won't change
        ci.add(file111_1);
        checkParents(index, file111_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1});

        // add file111_2 -> the parent structure won't change as they are already there
        ci.add(file111_2);
        checkParents(index, file111_1, file111_2);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1, file111_2});

        // add file112_1 -> the parent structure won't change as they are already there
        ci.add(file112_1);
        checkParents(index, file111_1, file111_2, file112_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111, folder112});
        assertValueSet(index.get(folder111), new File[] {file111_1, file111_2});
        assertValueSet(index.get(folder112), new File[] {file112_1});

    }

    public void testAddFiles() throws MalformedURLException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        CacheIndex ci = new CIndex();
        Map<File, Set<File>> index = getIndex(ci);
        index.clear();
        
        File root = new File(wc, "root");

        File folder1 = new File(root, "folder1");
        File folder11 = new File(folder1, "folder11");
        File folder111 = new File(folder11, "folder111");
        File folder112 = new File(folder11, "folder112");
        folder111.mkdirs();
        folder112.mkdirs();
        File file111_1 = new File(folder111, "file111_1");
        File file111_2 = new File(folder111, "file111_2");
        File file111_3 = new File(folder111, "file111_3");
        File file112_1 = new File(folder112, "file112_1");
        file111_1.createNewFile();
        file111_2.createNewFile();
        file111_3.createNewFile();
        file112_1.createNewFile();

        // add file111_1, file111_2, file111_3 -> all versioned parents will be added
        Set<File> s = new HashSet<File>();
        s.add(file111_1);
        s.add(file111_2);
        s.add(file111_3);
        ci.add(folder111, s);
        checkParents(index, file111_1, file111_2, file111_3);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1, file111_2, file111_3});

        // add file111_1, file111_3 -> all versioned parents will be added
        s = new HashSet<File>();
        s.add(file111_1);
        s.add(file111_3);
        ci.add(folder111, s);
        checkParents(index, file111_1, file111_3);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1, file111_3});

        // add file112_1
        s = new HashSet<File>();
        s.add(file112_1);
        ci.add(folder112, s);

        checkParents(index, file111_1, file111_3, file112_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111, folder112});
        assertValueSet(index.get(folder111), new File[] {file111_1, file111_3});
        assertValueSet(index.get(folder112), new File[] {file112_1});        
    }

    public void testAddRemoveLastNode() throws MalformedURLException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        CacheIndex ci = new CIndex();
        Map<File, Set<File>> index = getIndex(ci);
        index.clear();

        File root = new File(wc, "root");

        File folder1 = new File(root, "folder1");
        File folder11 = new File(folder1, "folder11");
        File folder111 = new File(folder11, "folder111");
        folder111.mkdirs();
        File file111_1 = new File(folder111, "file111_1");
        file111_1.createNewFile();

        // add file111_1
        Set<File> s = new HashSet<File>();
        s.add(file111_1);
        ci.add(folder111, s);
        checkParents(index, file111_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1});

        // add empty set 
        s = new HashSet<File>();
        ci.add(folder111, s);

        assertEquals(0, index.keySet().size());
    }

    public void testAddRemovePartFromLastNodes() throws MalformedURLException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        CacheIndex ci = new CIndex();
        Map<File, Set<File>> index = getIndex(ci);
        index.clear();

        File root = new File(wc, "root");

        File folder1 = new File(root, "folder1");
        File folder11 = new File(folder1, "folder11");
        File folder111 = new File(folder11, "folder111");
        folder111.mkdirs();
        File file111_1 = new File(folder111, "file111_1");
        File file111_2 = new File(folder111, "file111_2");
        file111_1.createNewFile();

        // add file111_1
        Set<File> s = new HashSet<File>();
        s.add(file111_1);
        s.add(file111_2);
        ci.add(folder111, s);

        checkParents(index, file111_1, file111_2);
        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1, file111_2});

        // add empty set
        s = new HashSet<File>();
        s.add(file111_1); // file111_2 will be removed
        ci.add(folder111, s);

        checkParents(index, file111_1);
        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1}); // only file111_1, file111_2 was removed
    }

    public void testAddRemovePartFromNode() throws MalformedURLException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        CacheIndex ci = new CIndex();
        Map<File, Set<File>> index = getIndex(ci);
        index.clear();

        File root = new File(wc, "root");

        File folder1 =   new File(root,      "folder1");
        File folder11 =  new File(folder1,     "folder11");
        File folder111 = new File(folder11,     "folder111");
        File file111_1 = new File(folder111,      "file111_1");
        File folder112 = new File(folder11,     "folder112");
        File file112_1 = new File(folder112,      "file112_1");
        folder111.mkdirs();
        folder112.mkdirs();
        file111_1.createNewFile();
        file112_1.createNewFile();

        // add file111_1, file112_1 -> all versioned parents will be added
        Set<File> s = new HashSet<File>();
        s.add(file111_1);
        ci.add(folder111, s);
        s.clear();
        s.add(file112_1);
        ci.add(folder112, s);
        checkParents(index, file111_1, file112_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111, folder112});
        assertValueSet(index.get(folder111), new File[] {file111_1});
        assertValueSet(index.get(folder112), new File[] {file112_1});

        // remove folder111 -> nothing will be removed - file111_1 still there
        s = new HashSet<File>();
        s.add(folder112);
        ci.add(folder11, s);
        checkParents(index, file111_1, file112_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111, folder112});
        assertValueSet(index.get(folder111), new File[] {file111_1});
        assertValueSet(index.get(folder112), new File[] {file112_1});

        // remove file111_1 -> parent folder111 will be also removed
        s = new HashSet<File>();        
        ci.add(folder111, s);
        checkParents(index, file112_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder112});
        assertValueSet(index.get(folder112), new File[] {file112_1});
    }

    public void testAddRemoveParentContainsFiles() throws MalformedURLException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        CacheIndex ci = new CIndex();
        Map<File, Set<File>> index = getIndex(ci);
        index.clear();

        File root = new File(wc, "root");

        File folder1 =   new File(root,      "folder1");
        File folder11 =  new File(folder1,     "folder11");
        File file11_1 =  new File(folder11,     "file11_1");
        File folder111 = new File(folder11,     "folder111");
        File file111_1 = new File(folder111,      "file111_1");
        folder111.mkdirs();
        file11_1.createNewFile();
        file111_1.createNewFile();

        // add file111_1, file112_1 -> all versioned parents will be added
        Set<File> s = new HashSet<File>();
        s.add(file11_1);
        ci.add(folder11, s);
        s.clear();
        s.add(file111_1);
        ci.add(folder111, s);
        checkParents(index, file111_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111, file11_1});
        assertValueSet(index.get(folder111), new File[] {file111_1});

        // remove file111_1 -> folder111 will be removed, folder11 remains
        s = new HashSet<File>();        
        ci.add(folder11, s);
        checkParents(index, file111_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder111), new File[] {file111_1});        
    }

    public void testAddRemoveNotExistingParentContainsFiles() throws MalformedURLException, IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        CacheIndex ci = new CIndex();
        Map<File, Set<File>> index = getIndex(ci);
        index.clear();

        File root = new File(wc, "root");

        File folder1 =   new File(root,      "folder1");
        File folder11 =  new File(folder1,     "folder11");
        File folder111 = new File(folder11,     "folder111");
        File file111_1 = new File(folder111,      "file111_1");
        File folder112 = new File(folder11,     "folder112");
        File folder12 =  new File(folder1,     "folder12");
        File file12_1 =  new File(folder12,     "file12_1");
        folder111.mkdirs();
        folder112.mkdirs();
        folder12.mkdirs();
        file111_1.createNewFile();
        file12_1.createNewFile();

        // add file111_1 -> all versioned parents will be added
        Set<File> s = new HashSet<File>();
        s.add(file111_1);
        ci.add(folder111, s);
        s.clear();
        s.add(file12_1);
        ci.add(folder12, s);
        checkParents(index, file111_1, file12_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11, folder12});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder12), new File[] {file12_1});
        assertValueSet(index.get(folder111), new File[] {file111_1});

        // remove file112_1 which isn't indexed -> folder111 remains
        s = new HashSet<File>();
        ci.add(folder112, s);
        checkParents(index, file111_1, file12_1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder1});
        assertValueSet(index.get(folder1), new File[] {folder11, folder12});
        assertValueSet(index.get(folder11), new File[] {folder111});
        assertValueSet(index.get(folder12), new File[] {file12_1});
        assertValueSet(index.get(folder111), new File[] {file111_1});
    }

    /**
     * Adding removed files (not folders) may result in corrupted index. Adding such file will remove parent entries even if there's
     * a modified sibling of the deleted file. See the corresponding fix in CacheIndex.
     */
    public void testRemoveFileTwice () throws Exception {
        CacheIndex ci = new CIndex();
        Map<File, Set<File>> index = getIndex(ci);
        index.clear();

        File root = new File(wc, "root");

        File folder =   new File(root,      "folder1");
        File file1 = new File(folder,      "file1");
        File file2 =  new File(folder,     "file2");
        folder.mkdirs();
        file1.createNewFile();
        file2.createNewFile();

        // add file111_1 -> all versioned parents will be added
        Set<File> s = new HashSet<File>();
        s.add(file1);
        ci.add(folder, s);
        checkParents(index, file1);
        s.clear();
        s.add(file1);
        s.add(file2);
        ci.add(folder, s);
        checkParents(index, file1);

        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder});
        assertValueSet(index.get(folder), new File[] {file1, file2});

        //remove file1 from index
        file2.delete();
        s.clear();
        s.add(file1);
        ci.add(folder, s);

        checkParents(index, file1);
        assertValueSet(index.get(wc), new File[] {root});
        assertValueSet(index.get(root), new File[] {folder});
        assertValueSet(index.get(folder), new File[] {file1});

        if (!file2.isFile()) {
            ci.add(file2, null);
            checkParents(index, file1);
            assertValueSet(index.get(wc), new File[] {root});
            assertValueSet(index.get(root), new File[] {folder});
            assertValueSet(index.get(folder), new File[] {file1});
        } else {
            fail("The file should be deleted");
        }

        //remove file1 from index
        s.clear();
        ci.add(folder, s);
        assertEquals(0, index.keySet().size());
    }

    private void assertValueSet(Set<File> s, File... expectedValues) {
        assertEquals(expectedValues.length, s.size());
        for (File ev : expectedValues) {
            if(!s.contains(ev)) {
                fail("expected value " + ev + " not found in value set");
            }
        }
    }

    private void checkParents(Map<File, Set<File>> index, File... files) {
        Set<File> parents = new HashSet<File>();

        for (File file : files) {
            File parent = file;
            while((parent = parent.getParentFile()) != null) {
                parents.add(parent);
                if(parent.equals(wc)) {
                    break;
                }
            }
            assert parent != null;
        }

        assertEquals(parents.size(), index.keySet().size());
        for (File p : parents) {
            assertTrue(index.containsKey(p));
        }
    }

    private Map<File, Set<File>> getIndex(CacheIndex ci) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = CacheIndex.class.getDeclaredField("index");
        f.setAccessible(true);
        return (Map<File, Set<File>>) f.get(ci);
    }

    private class CIndex extends CacheIndex {
        @Override
        protected boolean isManaged(File file) {
            boolean ancestorOrEqual = Utils.isAncestorOrEqual(wc, file);
            return ancestorOrEqual;
        }
    }

}
