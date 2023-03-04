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

package org.netbeans.modules.masterfs.filebasedfs.children;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex.Privileged;
import org.openide.util.Utilities;

/**
 *
 * @author Radek Matous
 */
public class ChildrenSupportTest extends NbTestCase {
    ChildrenSupport folderItem;
    File testFile;

    /*Just for testRefresh*/
    private File fbase;
    private File removed1;
    private File removed2;
    private File added1;
    private File added2;
    private FileNaming folderName;

    public ChildrenSupportTest(String testName) {
        super(testName);
    }

    public static void assertNoLock() {
        assertFalse("No read and write access", ChildrenSupport.isLock());
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        super.setUp();
        clearWorkDir();
        testFile = getWorkDir().getParentFile();
        folderName = NamingFactory.fromFile(testFile);
        folderItem = new ChildrenSupport ();

        if (getName().startsWith("testRefresh")) {
            fbase = new File (testFile, "testrefreshDir");
            removed1 = new File (fbase, "removed1/");
            removed2 = new File (fbase, "removed2/");
            added1 = new File (fbase, "added1/");
            added2 = new File (fbase, "added2/");

            assertTrue (testFile.exists());
            assertTrue(testFile.isDirectory());
            if (!fbase.exists()) assertTrue (fbase.getAbsoluteFile().mkdirs());
            if (!removed1.exists())assertTrue (removed1.mkdir());
            if (!removed2.exists())assertTrue (removed2.mkdir());
            if (added1.exists()) assertTrue (added1.delete());
            if (added2.exists()) assertTrue (added2.delete());
        }
    }

    public void testGetChild() throws Exception {
        File wDir =  getWorkDir();
        File file = new File(wDir, getName());
        FolderObj fo = (FolderObj)FileBasedFileSystem.getFileObject(wDir);
        assertNotNull(fo);
        assertEquals(fo.getFileName(),NamingFactory.fromFile(wDir));
        ChildrenCache chCache = fo.getChildrenCache();
        assertNotNull(chCache);
        ChildrenSupport childrenSupport = (ChildrenSupport)chCache;

        assertFalse(file.exists());
        assertTrue(childrenSupport.isStatus(ChildrenSupport.NO_CHILDREN_CACHED));
        assertNull(chCache.getChild(file.getName(), false));
        assertTrue(childrenSupport.isStatus(ChildrenSupport.SOME_CHILDREN_CACHED));

        assertTrue(file.createNewFile());
        assertNull(chCache.getChild(file.getName(), false));
        assertNotNull(chCache.getChild(file.getName(),true));
        assertTrue(childrenSupport.isStatus(ChildrenSupport.SOME_CHILDREN_CACHED));

        assertTrue(file.delete());
        assertNotNull(chCache.getChild(file.getName(),false));
        assertNull(chCache.getChild(file.getName(),true));
        assertTrue(childrenSupport.isStatus(ChildrenSupport.SOME_CHILDREN_CACHED));

        assertTrue(file.createNewFile());
        assertNull(chCache.getChild(file.getName(),false));
        assertNotNull(chCache.getChild(file.getName(),true));
        assertTrue(childrenSupport.isStatus(ChildrenSupport.SOME_CHILDREN_CACHED));
    }

    public void testRefreshChild() throws Exception {
        File wDir =  getWorkDir();
        File file = new File(wDir, getName());
        FolderObj fo = (FolderObj)FileBasedFileSystem.getFileObject(wDir);
        assertNotNull(fo);
        assertEquals(fo.getFileName(),NamingFactory.fromFile(wDir));
        ChildrenCache orig = fo.getChildrenCache();
        assertNotNull(orig);
        ChildrenSupport childrenSupport = (ChildrenSupport)orig;
        MyCache chCache = new MyCache(orig);

        assertFalse(file.exists());
        assertTrue(childrenSupport.isStatus(ChildrenSupport.NO_CHILDREN_CACHED));
        assertNull(chCache.getChild(file.getName(),false));
        assertTrue(childrenSupport.isStatus(ChildrenSupport.SOME_CHILDREN_CACHED));

        assertTrue(file.createNewFile());
        assertNull(chCache.getChild(file.getName(),false));
        Map m = chCache.refresh();
        assertEquals(1, m.keySet().size());
        assertEquals(m.keySet().toArray()[0],NamingFactory.fromFile(file));
        assertEquals(m.values().toArray()[0],ChildrenCache.ADDED_CHILD);
        assertNotNull(chCache.getChild(file.getName(),false));
        assertTrue(childrenSupport.isStatus(ChildrenSupport.SOME_CHILDREN_CACHED));

        assertTrue(file.delete());
        assertNotNull(chCache.getChild(file.getName(),false));
        m = chCache.refresh();
        assertEquals(1, m.keySet().size());
        assertEquals(m.keySet().toArray()[0],NamingFactory.fromFile(file));
        assertEquals(m.values().toArray()[0],ChildrenCache.REMOVED_CHILD);
        assertNull(chCache.getChild(file.getName(),false));
        assertTrue(childrenSupport.isStatus(ChildrenSupport.SOME_CHILDREN_CACHED));

        assertTrue(file.createNewFile());
        assertNull(chCache.getChild(file.getName(),false));
        m = chCache.refresh();
        assertEquals(1, m.keySet().size());
        assertEquals(m.keySet().toArray()[0],NamingFactory.fromFile(file));
        assertEquals(m.values().toArray()[0],ChildrenCache.ADDED_CHILD);
        assertNotNull(chCache.getChild(file.getName(),false));
        assertTrue(childrenSupport.isStatus(ChildrenSupport.SOME_CHILDREN_CACHED));
    }

    public void testRefresh109490() throws Exception {
        File wDir = getWorkDir();
        File file = new File(wDir, "testao.f");
        File file2 = new File(wDir, "testc1.f");
        assertEquals(file.hashCode(), file2.hashCode());
        FolderObj fo = (FolderObj)FileBasedFileSystem.getFileObject(wDir);
        assertNotNull(fo);
        assertEquals(fo.getFileName(),NamingFactory.fromFile(wDir));
        ChildrenCache orig = fo.getChildrenCache();
        assertNotNull(orig);
        MyCache chCache = new MyCache(orig);
        assertEquals(0,chCache.getChildren(true).size());
        ChildrenSupport childrenSupport = (ChildrenSupport)orig;
        assertEquals(0,childrenSupport.getCachedChildren().size());
        assertTrue(file.createNewFile());
        assertTrue(file2.createNewFile());

        assertEquals(2,chCache.refresh().size());
        
        Iterator<FileNaming> res = childrenSupport.getCachedChildren().iterator();
        assertNotNull("Has one element", res.next());
        
        assertTrue("Third file created", new File(wDir, "testciao.f").createNewFile());
        assertEquals("One additional file created", 1, chCache.refresh().size());
        
        assertNotNull("Can iterate to next", res.next());
        assertFalse("And that was the last one", res.hasNext());
    }


    public void testGetChildren() throws Exception {
        File wDir =  getWorkDir();
        File file = new File(wDir, getName());
        FolderObj fo = (FolderObj)FileBasedFileSystem.getFileObject(wDir);
        assertNotNull(fo);
        assertEquals(fo.getFileName(),NamingFactory.fromFile(wDir));
        ChildrenCache orig = fo.getChildrenCache();
        assertNotNull(orig);
        ChildrenSupport childrenSupport = (ChildrenSupport)orig;
        MyCache chCache = new MyCache(orig);

        assertFalse(file.exists());
        assertTrue(childrenSupport.isStatus(ChildrenSupport.NO_CHILDREN_CACHED));
        assertTrue(chCache.getChildren(false).isEmpty());
        assertTrue(childrenSupport.isStatus(ChildrenSupport.ALL_CHILDREN_CACHED));

        assertTrue(file.createNewFile());
        assertNull(chCache.getChild(file.getName(),false));
        assertTrue(chCache.getChildren(false).isEmpty());
        assertFalse(chCache.getChildren(true).isEmpty());
        assertFalse(chCache.getChildren(false).isEmpty());
        assertNotNull(chCache.getChild(file.getName(),false));
        assertEquals(chCache.getChild(file.getName(),false),
                chCache.getChildren(false).toArray()[0]);
        assertTrue(childrenSupport.isStatus(ChildrenSupport.ALL_CHILDREN_CACHED));

        assertTrue(file.delete());
        assertNotNull(chCache.getChild(file.getName(),false));
        assertFalse(chCache.getChildren(false).isEmpty());
        assertTrue(chCache.getChildren(true).isEmpty());
        assertTrue(chCache.getChildren(false).isEmpty());
        assertNull(chCache.getChild(file.getName(), false));
        assertTrue(chCache.getChildren(false).isEmpty());
        assertTrue(childrenSupport.isStatus(ChildrenSupport.ALL_CHILDREN_CACHED));

        assertTrue(file.createNewFile());
        assertNull(chCache.getChild(file.getName(), false));
        assertTrue(chCache.getChildren(false).isEmpty());
        assertFalse(chCache.getChildren(true).isEmpty());
        assertFalse(chCache.getChildren(false).isEmpty());
        assertNotNull(chCache.getChild(file.getName(), false));
        assertEquals(chCache.getChild(file.getName(), false),
                chCache.getChildren(false).toArray()[0]);
        assertTrue(childrenSupport.isStatus(ChildrenSupport.ALL_CHILDREN_CACHED));
    }

    public void testRefreshChildren() throws Exception {
        File wDir =  getWorkDir();
        File file = new File(wDir, getName());
        FolderObj fo = (FolderObj)FileBasedFileSystem.getFileObject(wDir);
        assertNotNull(fo);
        assertEquals(fo.getFileName(),NamingFactory.fromFile(wDir));
        ChildrenCache orig = fo.getChildrenCache();
        assertNotNull(orig);
        ChildrenSupport childrenSupport = (ChildrenSupport)orig;
        MyCache chCache = new MyCache(orig);

        assertFalse(file.exists());
        assertTrue(childrenSupport.isStatus(ChildrenSupport.NO_CHILDREN_CACHED));
        assertTrue(chCache.getChildren(false).isEmpty());
        assertTrue(childrenSupport.isStatus(ChildrenSupport.ALL_CHILDREN_CACHED));

        assertTrue(file.createNewFile());
        assertNull(chCache.getChild(file.getName(),false));
        assertTrue(chCache.getChildren(false).isEmpty());
        Map m = chCache.refresh();
        assertEquals(1, m.keySet().size());
        assertEquals(m.keySet().toArray()[0],NamingFactory.fromFile(file));
        assertEquals(m.values().toArray()[0],ChildrenCache.ADDED_CHILD);
        assertFalse(chCache.getChildren(false).isEmpty());
        assertNotNull(chCache.getChild(file.getName(),false));
        assertEquals(chCache.getChild(file.getName(),false),
                chCache.getChildren(false).toArray()[0]);
        assertTrue(childrenSupport.isStatus(ChildrenSupport.ALL_CHILDREN_CACHED));

        assertTrue(file.delete());
        assertNotNull(chCache.getChild(file.getName(),false));
        assertFalse(chCache.getChildren(false).isEmpty());
        m = chCache.refresh();
        assertEquals(1, m.keySet().size());
        assertEquals(m.keySet().toArray()[0],NamingFactory.fromFile(file));
        assertEquals(m.values().toArray()[0],ChildrenCache.REMOVED_CHILD);
        assertTrue(chCache.getChildren(false).isEmpty());
        assertNull(chCache.getChild(file.getName(),false));
        assertTrue(chCache.getChildren(false).isEmpty());
        assertTrue(childrenSupport.isStatus(ChildrenSupport.ALL_CHILDREN_CACHED));

        assertTrue(file.createNewFile());
        assertNull(chCache.getChild(file.getName(),false));
        assertTrue(chCache.getChildren(false).isEmpty());
        m = refresh(childrenSupport, NamingFactory.fromFile(wDir));
        assertEquals(1, m.keySet().size());
        assertEquals(m.keySet().toArray()[0],NamingFactory.fromFile(file));
        assertEquals(m.values().toArray()[0],ChildrenCache.ADDED_CHILD);
        assertFalse(chCache.getChildren(false).isEmpty());
        assertNotNull(chCache.getChild(file.getName(),false));
        assertEquals(chCache.getChild(file.getName(),false),
                chCache.getChildren(false).toArray()[0]);
        assertTrue(childrenSupport.isStatus(ChildrenSupport.ALL_CHILDREN_CACHED));
    }

    private Map refresh(ChildrenSupport childrenSupport, FileNaming fromFile) {
        Runnable[] task = new Runnable[1];
        Map<FileNaming, Integer> res = null;
        while (res == null) {
            if (task[0] != null) {
                task[0].run();
            }
            res = childrenSupport.refresh(fromFile, task);
        }
        return res;
        
    }
    private Set<FileNaming> getChildren(ChildrenSupport childrenSupport, FileNaming fromFile, boolean b) {
        Runnable[] task = new Runnable[1];
        Set<FileNaming> res = null;
        while (res == null) {
            if (task[0] != null) {
                task[0].run();
            }
            res = childrenSupport.getChildren(fromFile, b, task);
        }
        return res;
        
    }
    
    private static class MyCache implements ChildrenCache {
        ChildrenCache delegate;

        public MyCache(ChildrenCache delegate) {
            this.delegate = delegate;
        }

        @Override
        public void removeChild(FileNaming childName) {
            delegate.removeChild(childName);
        }

        @Override
        public Map<FileNaming, Integer> refresh(Runnable[] task) {
            return delegate.refresh(task);
        }
        public Map<FileNaming, Integer> refresh() {
            Runnable[] task = new Runnable[1];
            Map<FileNaming, Integer> res = null;
            while (res == null) {
                if (task[0] != null) {
                    task[0].run();
                }
                res = delegate.refresh(task);
            }
            return res;
        }

        @Override
        public boolean isCacheInitialized() {
            return delegate.isCacheInitialized();
        }

        @Override
        public Privileged getMutexPrivileged() {
            return delegate.getMutexPrivileged();
        }

        @Override
        public Set<FileNaming> getChildren(boolean rescan, Runnable[] task) {
            return delegate.getChildren(rescan, task);
        }
        public Set<FileNaming> getChildren(boolean rescan) {
            Runnable[] task = new Runnable[1];
            Set<FileNaming> res = null;
            while (res == null) {
                if (task[0] != null) {
                    task[0].run();
                }
                res = delegate.getChildren(rescan, task);
            }
            return res;
        }

        @Override
        public FileNaming getChild(String childName, boolean rescan) {
            return delegate.getChild(childName, rescan);
        }

        @Override
        public FileNaming getChild(String childName, boolean rescan,
                Runnable[] task) {
            return getChild(childName, rescan);
        }

        @Override
        public Set<FileNaming> getCachedChildren() {
            return delegate.getCachedChildren();
        }
        
        
    }



    /**
     * Test of getFolderName method, of class org.netbeans.modules.masterfs.children.FolderPathItems.
     */
    public void testGetFolderItem() throws Exception {
        assertEquals(testFile, folderName.getFile());
    }


    /**
     * Test of getChildren method, of class org.netbeans.modules.masterfs.children.FolderPathItems.
     */
    public void testGetChildrenPathItems() throws Exception{
        Set childItems = getChildren(folderItem, folderName, true);
        List lst = Arrays.asList(testFile.listFiles());
        Iterator it = childItems.iterator();
        while (it.hasNext()) {
            FileNaming pi = (FileNaming)it.next();
            File f = pi.getFile();
            assertTrue (lst.contains(f));
        }
    }

    /**
     * Test of getFileName method, of class org.netbeans.modules.masterfs.children.FolderPathItems.
     */
    public void testGetChildItem() throws Exception{
        getChildren(folderItem, folderName, true);
        List lst = Arrays.asList(testFile.listFiles());
        Iterator it = lst.iterator();
        while (it.hasNext()) {
            File f = (File)it.next();
            FileNaming item = folderItem.getChild(f.getName(), folderName,false);
            assertNotNull(item);
            assertTrue (item.getFile().equals(f));
        }
    }

    public void testRefresh () throws Exception {
        FileNaming fpiName = NamingFactory.fromFile(fbase);
        ChildrenSupport fpi = new ChildrenSupport();
        getChildren(fpi, fpiName, true);
        assertTrue (removed1.delete());
        assertTrue (removed2.delete());
        assertTrue (added1.mkdirs());
        assertTrue (added2.mkdirs());

        List added = Arrays.asList(new String[] {"added1", "added2"});
        List removed = Arrays.asList(new String[] {"removed1", "removed2"});

        Map changes = refresh(fpi, fpiName);
        Iterator it = changes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            FileNaming pItem = (FileNaming)entry.getKey();
            Integer type = (Integer)entry.getValue();
            if (type == ChildrenCache.ADDED_CHILD) {
                assertTrue (added.contains(pItem.getName()));
            }
            if (type == ChildrenCache.REMOVED_CHILD) {
                assertTrue (removed.contains(pItem.getName()));
            }
        }
        assertTrue (changes.size() == 4);
    }

    public void testRefresh2 () throws Exception {
        FileNaming fpiName = NamingFactory.fromFile(fbase);
        ChildrenSupport fpi = new ChildrenSupport();
        fpi.getChild("removed1", fpiName, false);
        assertTrue (removed1.delete());
        assertTrue (removed2.delete());
        assertTrue (added1.mkdirs());
        assertTrue (added2.mkdirs());

        Map changes = refresh(fpi, fpiName);
        Iterator it = changes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            FileNaming pItem = (FileNaming)entry.getKey();
            Integer type = (Integer)entry.getValue();
            assertEquals("removed1", pItem.getName());
        }
        assertTrue (changes.size() == 1);
    }

    /** Simulate listFiles() returns null in case of I/O failure like 'too many files open'. */
    private static class File150009 extends File {

        public File150009(File file) {
            super(file.getAbsolutePath());
        }

        @Override
        public File[] listFiles() {
            return null;
        }
    }

    /** Tests that files are not removed if folder.listFiles() returns null.
     * It can happen in case of I/O failure like 'too many files open' (see #150009)
     */
    public void testRefresh150009() {
        FileNaming fpiName = NamingFactory.fromFile(fbase);
        // remove and plug our File implementation
        fpiName = NamingFactory.fromFile(new File150009(fbase));
        ChildrenSupport fpi = new ChildrenSupport();
        assertNotNull(fpi.getChild("removed1", fpiName, false));
        assertNotNull(fpi.getChild("removed2", fpiName, false));
        assertFalse("Children must not be deleted when File.listFiles() returns null.", getChildren(fpi, fpiName, true).isEmpty());
    }

    /**
     * Test for bug 240156.
     *
     * @throws java.io.IOException
     */
    public void testRefreshAfterCaseChange() throws IOException {

        if (!Utilities.isWindows()) { // TODO may be also applicable on Mac
            return;
        }

        File dir = new File(getWorkDir(), "dir");
        dir.mkdir();
        FileObject dirFO = FileUtil.toFileObject(dir);

        // Try to retrieve a not-existing file, it's name will be cached
        // in ChildrenSupport.notExistingChildren.
        File notExisting = new File(dir, "missing");
        FileUtil.toFileObject(notExisting);

        // Create the file, but with different letter-case.
        FileObject existingFO = FileUtil.createFolder(dirFO, "MisSING");

        // Refresh the directory. ChildrenSupport will check items in
        // notExistingChildren - "missing" now exists with different letter
        // case, but it is not detected, so the cached FileName (or FolderName)
        // in NamingFactory is updated (method updateCase).
        // NOTE: The refresh can be invoked from native filesystem watcher,
        //       so the bug may appear quite randomly.
        dirFO.refresh();

        assertEquals("MisSING", existingFO.getNameExt());
    }

    /**
     * Check that fix for bug 240156 {@link #testRefreshAfterCaseChange()}
     * doens't break anything.
     *
     * @throws IOException
     */
    public void testChangeFileToDir() throws IOException {

        File dir = new File(getWorkDir(), "dir");
        dir.mkdir();
        FileObject dirFO = FileUtil.toFileObject(dir);

        File fileOrDir = new File(dir, "fileOrDir");
        fileOrDir.createNewFile();
        FileObject fileOrDirFO = FileUtil.toFileObject(fileOrDir);
        assertTrue(fileOrDirFO.isData());
        dirFO.refresh();

        fileOrDir.delete();
        dirFO.refresh();

        fileOrDir.mkdir();
        dirFO.refresh();

        fileOrDirFO = FileUtil.toFileObject(fileOrDir);
        assertTrue(fileOrDirFO.isFolder());
    }
}
