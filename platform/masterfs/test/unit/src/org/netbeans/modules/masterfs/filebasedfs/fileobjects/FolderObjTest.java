/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.Assume;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.Statistics;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.netbeans.modules.masterfs.providers.ProvidedExtensionsTest;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * FolderObjTest.java
 * @author Radek Matous
 */
public class FolderObjTest extends NbTestCase {
    File testFile;
    Logger LOG;
    
    public FolderObjTest(String testName) {
        super(testName);
    }
            
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        LOG = Logger.getLogger("test." + getName());
        testFile = getWorkDir();        
    }

    @Override
    protected Level logLevel() {
        String[] testsWithEnabledLogger = new String[] {
            "testCreateFolder72617",
            "testCreateData72617",
            ".testBug127256"
        };
        return (Arrays.asList(testsWithEnabledLogger).contains(getName())) ? 
            Level.FINEST : Level.OFF;
    }

    abstract static class IntrusiveLogHandler extends Handler {
        private String message;
        IntrusiveLogHandler(final String message) {
            this.message = message;
        }
        public final void publish(LogRecord record) {
            String recordMsg = record.getMessage();
            if (recordMsg != null && recordMsg.indexOf(this.message) != -1) {
                processLogRecord(record);
            }
        }

        protected abstract void processLogRecord(final LogRecord record);
        public void flush() {}
        public void close() { flush(); }
    }
    
    public void testRenameWithAttributes() throws Exception {
        final FileObject workDirFo = FileBasedFileSystem.getFileObject(getWorkDir());
        FileObject folder = workDirFo.createFolder("a");
        folder.createData("non.empty");
        folder.setAttribute("name", "jmeno");
        assertEquals("jmeno", folder.getAttribute("name"));
        FileLock lock = folder.lock();
        folder.rename(lock, "b", null);
        lock.releaseLock();
        assertEquals("Name is not b", "b", folder.getNameExt());
        WeakReference<?> ref = new WeakReference<FileObject>(folder);
        folder = null;
        assertGC("Folder can disappear", ref);
        folder = workDirFo.getFileObject("b");
        assertNotNull("Folder b found", folder);
        assertEquals("The attribute remains even after rename", "jmeno", folder.getAttribute("name"));
        assertEquals("One children", 1, folder.getChildren().length);
    }

    public void testMoveWithAttributes() throws Exception {
        final FileObject workDirFo = FileBasedFileSystem.getFileObject(getWorkDir());
        FileObject target = workDirFo.createFolder("target");
        FileObject folder = workDirFo.createFolder("a");
        folder.createData("non.empty");
        folder.setAttribute("name", "jmeno");
        assertEquals("jmeno", folder.getAttribute("name"));
        FileLock lock = folder.lock();
        FileObject newF = folder.move(lock, target, "b", null);
        assertFalse("Invalidated", folder.isValid());
        lock.releaseLock();
        assertEquals("Name is not b", "b", newF.getNameExt());
        WeakReference<?> ref = new WeakReference<FileObject>(newF);
        newF = null;
        assertGC("Folder can disappear", ref);
        folder = target.getFileObject("b");
        assertNotNull("Folder b found", folder);
        assertEquals("The attribute remains even after rename", "jmeno", folder.getAttribute("name"));
        assertEquals("One children", 1, folder.getChildren().length);
    }

    public void testBug127256() throws Exception {
        final FileObject workDirFo = FileBasedFileSystem.getFileObject(getWorkDir());
        FileObject folder = workDirFo.createFolder("a");
        assertTrue("Is folder", folder.isFolder());
        folder.delete();
        FileObject data = workDirFo.createData("a");
        assertNotNull(data);
        assertTrue("Is data", data.isData());
        assertTrue("Data is valid", data.isValid());
        assertFalse("Folder is invalid", folder.isValid());
    }
    
    public void testAsyncCall() throws Exception {
        final FileObject workDirFo = FileBasedFileSystem.getFileObject(getWorkDir());
        File f = new File(getWorkDir(), "a");
        assertNull(workDirFo.getFileObject("a"));
        assertTrue(f.createNewFile());
        final Thread t = Thread.currentThread();
        class FileChange extends FileChangeAdapter {
            private boolean called = false;
            @Override
            public void fileDataCreated(FileEvent fe) {
                assertNotSame(t, Thread.currentThread());
                called = true;
                synchronized (workDirFo) {
                    workDirFo.notifyAll();
                }
            }
            public boolean isCalled() {
                return called;
            }            
        } 
        FileChange fcl = new FileChange();
        workDirFo.addFileChangeListener(fcl);
        try {
            assertNotNull(FileUtil.toFileObject(f));
            synchronized(workDirFo) {
                workDirFo.wait();
            }
            assertNotNull(workDirFo.getFileObject("a"));                
            assertTrue(fcl.isCalled());
        } finally {
            workDirFo.removeFileChangeListener(fcl);
        }
    }
    
    
    public void testBug128234() throws Exception {
        final FileObject workDirFo = FileBasedFileSystem.getFileObject(getWorkDir());
        FileObject fo = workDirFo.createData("a");
        assertNotNull(fo);
        File f = FileUtil.toFile(fo);
        assertNotNull(f);
        FileUtil.toFileObject(new File(f,f.getName()));
    }
    
    
    
    public void testMove85336() throws Exception {
        final FileObject workDirFo = FileBasedFileSystem.getFileObject(getWorkDir());
        FolderObj to =  (FolderObj)FileUtil.createFolder(workDirFo, "a/b/c");
        FolderObj from =  (FolderObj)FileUtil.createFolder(workDirFo, "aa/b/c");        
        assertNotNull(to);
        assertNotNull(from);
        BaseFileObj what =  (BaseFileObj)FileUtil.createData(from, "hello.txt");        
        assertNotNull(what);
        FileLock lck = what.lock();
        ProvidedExtensions.IOHandler io = new ProvidedExtensionsTest.ProvidedExtensionsImpl().
                getMoveHandler(what.getFileName().getFile(), new File(to.getFileName().getFile(),what.getNameExt())); 
        to.getChildren();
        try {
            what.move(lck, to, what.getName(), what.getExt(), io);
        } finally {
            lck.releaseLock();
        }        
    }
    
    public void testFolderToFile() throws Exception {
        File f = new File(getWorkDir(), "classes");
        f.createNewFile();
        assertTrue("File created", f.isFile());
        
        FileObject fo = FileUtil.toFileObject(f);
        assertTrue("Is data", fo.isData());
        
        f.delete();
        f.mkdirs();
        assertTrue("Is dir", f.isDirectory());
        
        fo.refresh();
        
        assertFalse("No longer valid", fo.isValid());
        FileObject folder = FileUtil.toFileObject(f);
        assertNotNull("Folder found", folder);
        if (fo == folder) {
            fail("Should not be the same: " + folder);
        }
        assertTrue("Is folder", folder.isFolder());
    }
    
    public void testFolderToFileWithParent() throws Exception {
        File f = new File(getWorkDir(), "classes");
        f.createNewFile();
        assertTrue("File created", f.isFile());
        
        FileObject fo = FileUtil.toFileObject(f);
        assertTrue("Is data", fo.isData());
        FileObject parent = fo.getParent();
        List<FileObject> arr = Arrays.asList(parent.getChildren());
        
        assertTrue("Contains " + fo + ": " +arr, arr.contains(fo));
        
        f.delete();
        f.mkdirs();
        assertTrue("Is dir", f.isDirectory());
        
        
        fo.refresh();
        
        assertFalse("No longer valid", fo.isValid());
        FileObject folder = FileUtil.toFileObject(f);
       
        if (fo == folder) {
            fail("Should not be the same: " + folder);
        }
        assertTrue("Is folder", folder.isFolder());
    }

    public void testFolderToFileWithParentRefresh() throws Exception {
        File f = new File(getWorkDir(), "classes");
        f.createNewFile();
        assertTrue("File created", f.isFile());
        
        FileObject fo = FileUtil.toFileObject(f);
        assertTrue("Is data", fo.isData());
        FileObject parent = fo.getParent();
        List<FileObject> arr = Arrays.asList(parent.getChildren());
        
        assertTrue("Contains " + fo + ": " +arr, arr.contains(fo));
        
        f.delete();
        f.mkdirs();
        assertTrue("Is dir", f.isDirectory());
        
        
        parent.refresh();
        
        assertFalse("No longer valid", fo.isValid());
        FileObject folder = FileUtil.toFileObject(f);
       
        if (fo == folder) {
            fail("Should not be the same: " + folder);
        }
        assertTrue("Is folder", folder.isFolder());
    }
            
    public void testRefresh109490() throws Exception {
        final File wDir = getWorkDir();
        final FileObject wDirFo = FileBasedFileSystem.getFileObject(wDir);
        final List<FileEvent> fileEvents = new ArrayList<FileEvent>();
        FileSystem fs = wDirFo.getFileSystem();
        FileChangeListener fListener = new FileChangeAdapter() {

            @Override
            public void fileDataCreated(FileEvent fe) {
                super.fileDataCreated(fe);
                fileEvents.add(fe);
            }
        };
        try {
            fs.addFileChangeListener(fListener);

            File file = new File(wDir, "testao.f");
            File file2 = new File(wDir, "testc1.f");
            assertEquals(file.hashCode(), file2.hashCode());
            wDirFo.getChildren();
            assertTrue(file.createNewFile());
            assertTrue(file2.createNewFile());
            assertEquals(0, fileEvents.size());
            fs.refresh(true);
            assertEquals(2, fileEvents.size());
            assertEquals(Arrays.asList(wDirFo.getChildren()).toString(), 2,wDirFo.getChildren().length);
            assertTrue(Arrays.asList(wDirFo.getChildren()).toString().indexOf(file.getName()) != -1);            
            assertTrue(Arrays.asList(wDirFo.getChildren()).toString().indexOf(file2.getName()) != -1);                        
        } finally {
            fs.removeFileChangeListener(fListener);
        }
    }
    
    public void testCreateFolder72617() throws IOException {
        Handler handler = new IntrusiveLogHandler("FolderCreated:") {//NOI18N
            protected void processLogRecord(final LogRecord record) {
                Object[] params = record.getParameters();
                if (params != null && params.length > 0 && params[0] instanceof File) {
                    File f = (File)params[0];
                    assertTrue(f.exists());
                    assertTrue(f.delete());
                }                
            }            
        };
        Logger.getLogger("org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj").addHandler(handler);
        try {
            File f = testFile;
            FileObject fo = FileBasedFileSystem.getFileObject(f);
            assertNotNull(fo);
            File f2 = new File (testFile, "newfoldercreated");
            try {
                fo.createFolder (f2.getName());
                fail();
            } catch(IOException iex) {}        
        } finally {
          Logger.getLogger("org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj").removeHandler(handler);
        }
    }
    
    public void testCreateData72617() throws IOException {
        Handler handler = new IntrusiveLogHandler("DataCreated:") {//NOI18N
            protected void processLogRecord(final LogRecord record) {
                Object[] params = record.getParameters();
                if (params != null && params.length > 0 && params[0] instanceof File) {
                    File f = (File)params[0];
                    assertTrue(f.exists());
                    assertTrue(f.delete());
                }                
            }            
        };
        Logger.getLogger("org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj").addHandler(handler);
        try {
            File f = testFile;
            FileObject fo = FileBasedFileSystem.getFileObject(f);
            assertNotNull(fo);
            File f2 = new File (testFile, "newdatacreated");
            try {
                fo.createData (f2.getName());
                fail();
            } catch(IOException iex) {}        
        } finally {
          Logger.getLogger("org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj").removeHandler(handler);
        }
    }
    
    public void testGetRoot() throws IOException {
        FileSystem fs = FileBasedFileSystem.getInstance();
        FileObject workDirFo = FileBasedFileSystem.getFileObject(getWorkDir());
        while (workDirFo != null && !workDirFo.isRoot()) {
            assertFalse(workDirFo.isRoot());
            workDirFo = workDirFo.getParent();
        }
        assertNotNull(workDirFo);
        assertTrue(workDirFo.isRoot());
        assertSame(workDirFo, fs.getRoot());
    }
    

    public void testChildren() throws Exception {
        final FolderObj testRoot = (FolderObj)FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(testRoot);
        for (int i = 0; i < 5; i++) {
            assertTrue(new File (getWorkDir(), "file"+String.valueOf(i)).createNewFile());
        }                
        assertEquals(5,testRoot.getChildren().length);
    }
    
    public void testSize() throws Exception {
        final FolderObj testRoot = (FolderObj)FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(testRoot);
        FileObj bfo = (FileObj)testRoot.createData(getName());

        int expectedSize = 264;
        /* assertSize(FileObj) JDK1.6.0:
         * 
         * java.lang.ref.ReferenceQueue$Null: 1, 32B
         * $Proxy0: 1, 16B
         * java.lang.ref.ReferenceQueue$Lock: 2, 16B
         * org.netbeans.modules.masterfs.filebasedfs.fileobjects.BaseFileObj$FileChangeListenerForVersioning: 1, 16B
         * java.lang.ref.WeakReference: 1, 24B
         * org.openide.util.Utilities$ActiveQueue: 1, 40B
         * org.openide.util.WeakListenerImpl$ProxyListener: 1, 24B
         * [Ljava.lang.Object;: 1, 24B
         * org.openide.util.WeakListenerImpl$ListenerReference: 1, 32B
         * javax.swing.event.EventListenerList: 1, 16B
         * org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObj: 1, 40B
         */
        assertSize("", Collections.singleton(bfo), expectedSize, new Object[] {bfo.getFileName()});        
        /* assertSize(FolderObj) JDK1.6.0:
         * 
         * java.lang.ref.ReferenceQueue$Null: 1, 32B
         * $Proxy0: 1, 16B
         * org.openide.util.WeakListenerImpl$ListenerReference: 1, 32B
         * org.netbeans.modules.masterfs.filebasedfs.fileobjects.BaseFileObj$FileChangeListenerForVersioning: 1, 16B
         * org.openide.util.WeakListenerImpl$ProxyListener: 1, 24B
         * javax.swing.event.EventListenerList: 1, 16B
         * org.openide.util.Utilities$ActiveQueue: 1, 40B
         * [Ljava.lang.Object;: 1, 24B
         * java.lang.ref.WeakReference: 1, 24B
         * org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj: 1, 40B
         * java.lang.ref.ReferenceQueue$Lock: 2, 16B
         */
        assertSize("", Collections.singleton(testRoot), expectedSize, new Object[] {testRoot.getFileName(), 
        testRoot.getChildrenCache()});                        
    }
    
    public void testCreateNbAttrs() throws IOException  {
        final FileObject testRoot = FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(testRoot);        
        try {            
            testRoot.createData(".nbattrs");
            fail();
        } catch (IOException ex) {}        
    }
    
    public void testCreateWriteLock() throws IOException  {
        final FileObject testRoot = FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(testRoot);        
        File file2lock = new File(getWorkDir(),"aa.txt");
        File associatedLock = WriteLockUtils.getAssociatedLockFile(file2lock);//NOI18N
        
        try {            
            testRoot.createData(associatedLock.getName());
            fail();
        } catch (IOException ex) {}        
    }
    
    
    public void testCaseInsensitivity() throws Exception {
        if (!Utilities.isWindows()) return;
        final FileObject testRoot = FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(testRoot);
        
        File testa = new File(getWorkDir(), "a");
        File testA = new File(getWorkDir(), "A");
        
        if (testA.exists()) {
            assertTrue(testA.delete());
        }
        if (!testa.exists()) {
            assertTrue(testa.createNewFile());
        }

        //FileBasedFileSystem's case sensitivity depends on platform. This is different behaviour
        // than originally provided by AbstractFileSystem.
        FileObject A = testRoot.getFileObject("A");
        assertNotNull(A);
        assertNotNull(testRoot.getFileObject("a"));
        assertSame(testRoot.getFileObject("A"), testRoot.getFileObject("a"));
        assertSame(URLMapper.findFileObject(Utilities.toURI(testa).toURL()),
                URLMapper.findFileObject(Utilities.toURI(testA).toURL()));
        
        //but 
        testRoot.getChildren();
        assertEquals("A",testRoot.getFileObject("A").getName());
        assertEquals("A",testRoot.getFileObject("a").getName());        
        BaseFileObj bobj = (BaseFileObj)testRoot.getFileObject("a");
        NamingFactory.checkCaseSensitivity(bobj.getFileName(),testa);
        assertEquals("a",testRoot.getFileObject("a").getName());                
        assertEquals("a",testRoot.getFileObject("A").getName());                        
    }
    
    private class TestListener extends FileChangeAdapter {
        private List<FileObject> fileObjects;
        TestListener(List<FileObject> fileObjects) {
            this.fileObjects = fileObjects;
        }
        @Override
        public void fileFolderCreated(FileEvent fe) {
            assertTrue(fileObjects.remove(fe.getFile())); 
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            assertTrue(fileObjects.remove(fe.getFile())); 
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            assertTrue(fileObjects.remove(fe.getFile())); 
        }        
    }
    
    public void testSimulatesRefactoringRename() throws Exception {
        if (!Utilities.isWindows()) return;
        FileBasedFileSystem fs = FileBasedFileSystem.getInstance();
        assertNotNull(fs);
        final FileObject root = FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(root);
        FileObject main = root.createData("Main.java");
        FileUtil.createData(root,"subpackage/newclass.java");
        final List<FileObject> fileObjects = new ArrayList<FileObject>() {
            @Override
            public boolean add(FileObject o) {
                assertNotNull(o);
                return super.add(o);
            }
            
        };
        final TestListener tl = new TestListener(fileObjects);
        fs.addFileChangeListener(tl);
        try {
            fs.runAtomicAction(new FileSystem.AtomicAction(){
                public void run() throws IOException {
                    FileObject subpackage = root.getFileObject("subpackage");
                    FileObject newclass = subpackage.getFileObject("newclass.java");
                    FileObject subpackage1 = root.createFolder("subpackage1");
                    fileObjects.add(subpackage1);
                    FileObject newclass1 = subpackage1.createData("newclass.java");
                    fileObjects.add(newclass1);
                    subpackage.delete();
                    fileObjects.add(subpackage);
                    fileObjects.add(newclass);
                }
            });
        } finally {
            fs.removeFileChangeListener(tl);
        }
        assertTrue(fileObjects.toString(),fileObjects.isEmpty());
        assertNotNull(root.getFileObject("Main.java"));
        assertNotNull(root.getFileObject("subpackage1"));
        assertNotNull(root.getFileObject("subpackage1/newclass.java"));
        assertNull(root.getFileObject("subpackage"));
        fs.addFileChangeListener(tl);
        try {
            fs.runAtomicAction(new FileSystem.AtomicAction(){
                public void run() throws IOException {
                    FileObject subpackage1 = root.getFileObject("subpackage1");
                    FileObject newclass = root.getFileObject("subpackage1/newclass.java");
                    FileObject Subpackage = root.createFolder("Subpackage");
                    fileObjects.add(Subpackage);
                    FileObject newclass1 = Subpackage.createData("newclass.java");
                    fileObjects.add(newclass1);
                    subpackage1.delete();
                    fileObjects.add(subpackage1);
                    fileObjects.add(newclass);
                }
            });
        } finally {
            fs.removeFileChangeListener(tl);
        }
        assertTrue(fileObjects.toString(), fileObjects.isEmpty());
        assertNotNull(root.getFileObject("Main.java"));
        assertNotNull(root.getFileObject("Subpackage/newclass.java"));
        assertNull(root.getFileObject("subpackage1"));
    }
    
    public void testRename() throws Exception {
        File f = testFile;

        final List<FileEvent> l = new ArrayList<FileEvent>();
        FileChangeListener fcl = new FileChangeAdapter () {
            @Override
             public void fileRenamed(FileRenameEvent fe) {
                 FileObject fold = fe.getFile();
                 final FileObject[] ch = fold.getChildren();
                 assertTrue("There should be some children in " + fold, ch.length > 0);
                 l.add(fe);
             }
        };
        
        FileObject fo = FileBasedFileSystem.getFileObject(f);
        assertNotNull(fo);
        FileObject folder =fo.createFolder("testRename");
        assertNotNull(folder);
        
        FileObject file =folder.createData("test.txt");
        assertNotNull(file);
        folder.addFileChangeListener(fcl);
        assertTrue(folder.getChildren().length > 0);
        FileLock lock = folder.lock();
        try {
            folder.rename(lock,"renfolder","");            
            assertTrue(folder.getChildren().length > 0);
            assertTrue(!l.isEmpty());            
            
            l.clear();
            
            folder.rename(lock,"testRename","");            
            assertTrue(folder.getChildren().length > 0);
            assertTrue(!l.isEmpty());            
            
        } finally {
            lock.releaseLock();
        }
        
        
     }

     public void testRename2 () throws Exception {
        File test = new File (getWorkDir(), "testrename.txt");
        if (!test.exists()) {
            assertTrue(test.createNewFile());
        }
        
        FileObject testFo = FileBasedFileSystem.getFileObject(test);
        assertNotNull (testFo);

        FileLock lock = testFo.lock();
        assertNotNull (lock);
        try {
         testFo.rename(lock, "TESTRENAME", "TXT");           
        } finally {
            lock.releaseLock();
        }
    }

    /** Tests slashes not allowed for rename (#128818). */
    public void testRename128818() throws Exception {
        File test = new File(getWorkDir(), "testrename128818.txt");
        if (!test.exists()) {
            assertTrue(test.createNewFile());
        }
        FileObject testFo = FileBasedFileSystem.getFileObject(test);
        assertNotNull(testFo);

        FileLock lock = testFo.lock();
        assertNotNull(lock);
        try {
            try {
                testFo.rename(lock, "a/b", "abc");
                fail("Rename should not allow slash in the name.");
            } catch (IOException ioe) {
                // OK
            }
            try {
                testFo.rename(lock, "a\\b", "abc");
                fail("Rename should not allow backslash in the name.");
            } catch (IOException ioe) {
                // OK
            }
        } finally {
            lock.releaseLock();
        }
    }

    /**
     * Test of getChildren method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testGetChildren() throws Exception {
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance();
        assertNotNull(fs);
        assertNotNull(f.getAbsolutePath(),FileBasedFileSystem.getInstance());
        
        while (f != null) {
            FileObject fo0 = FileBasedFileSystem.getFileObject(f);
            assertNotNull(f.getAbsolutePath(),fo0);
            
            FileObject fo = FileUtil.toFileObject(f);//fs.getRoot().getFileObject(f.getAbsolutePath().replace('\\','/'));
            assertNotNull(f.getAbsolutePath(),fo);
            if (fo0 != fo) {
                fs.getRoot().getFileObject(f.getAbsolutePath().replace('\\','/'));
            }
            assertSame(fo.toString(), fo0, fo);

            if(!f.getParentFile().equals(FileUtil.toFile(FileObjectFactory.getInstance(f).getRoot()))) {
                FileObject parent = fo.getParent();
                assertNotNull(parent);
                String nameExt = fo.getNameExt();
                FileObject fo2 = parent.getFileObject(nameExt);
                assertNotNull(fo2);
                assertSame(fo,  fo2);
                FileObject[] fos = parent.getChildren();
                List<FileObject> list = Arrays.asList(fos);
                assertTrue((fo.toString()+ "  " + System.identityHashCode(fo)),list.contains(fo));

               
                WeakReference<FileObject> ref = new WeakReference<FileObject>(fo);
                String msg = fo.toString();
                fo = null; fo0 = null; fo2 = null; parent = null;fos = null; list = null;
                assertGC(msg, ref);                
            } else {
                //disk roots are kept by hard reference
                WeakReference<FileObject> ref = new WeakReference<FileObject>(fo);
                String msg = fo.toString();
                fo = null; fo0 = null; 
                assertNotNull(msg, ref.get());                                
                break;
            }
            
            f = f.getParentFile();
        }        
    }
    
    public void testGetChildrenStackOwerflow() throws Exception {
        File f = testFile;
        assertTrue(f.exists());
        assertTrue(f.isDirectory());        
        final FileObject fo = FileBasedFileSystem.getFileObject(f); 
        assertNotNull(f.getAbsolutePath(),fo);        
        assertTrue(fo.isFolder());
        assertEquals(0,fo.getChildren().length);
        assertTrue(new File(f,"child1").createNewFile());
        assertTrue(new File(f,"child2").createNewFile());
        final File child3 = new File(f,"child3");
        assertTrue(child3.createNewFile());
        final List<FileObject> keepThem = new ArrayList<FileObject>();
        fo.addFileChangeListener(new FileChangeAdapter(){
            @Override
            public void fileDeleted(FileEvent fe) {
                for (FileObject fodel : keepThem) {
                    FileObject[] all =  fo.getChildren();
                    for (int i = 0; i < all.length; i++) {
                        all[i].refresh();
                    }
                    
                }
            }
            
            @Override
            public void fileDataCreated(FileEvent fe) {
                FileObject ffoo = fe.getFile(); 
                keepThem.add(ffoo);
                ((BaseFileObj)ffoo).getFileName().getFile().delete();
                ffoo.refresh();
            }            
        } );

        fo.refresh();
        assertEquals(0,fo.getChildren().length);
    }
    
    /**
     * Test of isData method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testIsData() {
        File f = testFile;
        
        while (f != null && f.exists()) {            
            FileObject fo = FileBasedFileSystem.getFileObject(f);
            assertNotNull(f.getAbsolutePath(),fo);
            assertEquals(f.isFile(), fo.isData());
            if (fo.isData ()) {
                assertTrue(fo instanceof FileObj);
            }
            
            f = f.getParentFile();
        }
    }
    
    /**
     * Test of isFolder method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testIsFolder() {
        File f = testFile;
        
        while (f != null && f.exists()) {            
            FileObject fo = FileBasedFileSystem.getFileObject(f);
            assertNotNull(f.getAbsolutePath(),fo);
            if (fo.isFolder() && !fo.isRoot()) {
                assertTrue(fo instanceof FolderObj);
            }
           
            f = f.getParentFile();            
        }
    }
    
    /**
     * Test of isRoot method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testIsRoot() {
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance();
        assertNotNull(fs);
        FileObject fo = null;
        while (f != null && f.exists()) {
            fo = FileBasedFileSystem.getFileObject(f);
            assertNotNull(f.getAbsolutePath(),fo);
            f = f.getParentFile();            
        }
        assertNotNull(fo.toString(), fo);        
        FileObject root = fo;// fo.getParent ();
        
        assertNotNull(root.toString(), root);                        
        if (Utilities.isWindows()) {
            assertNotNull(root.getParent());
            root = root.getParent();
        } 
        assertTrue(root.isRoot());
        assertTrue(root instanceof RootObj<?>);
        assertSame(root, fs.getRoot());        
    }
    
    /**
     * Test of getFileObject method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testGetFileObject() {
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance();
        assertNotNull(fs);
        
        while (f != null && f.exists()) {
            FileObject fo = FileBasedFileSystem.getFileObject(f);
            assertNotNull(f.getAbsolutePath(),fo);
            FileObject parent = fo.getParent();
            while (parent != null && parent != fo) {                
                assertNotNull(parent);
                assertNotNull(fo);
                String relativePath = FileUtil.getRelativePath(parent, fo);
                assertNotNull(relativePath);
                FileObject fo2 = parent.getFileObject(relativePath);
                assertNotNull((relativePath + " not found in " + parent.toString()), fo2);
                assertSame (fo, fo2);
                parent = parent.getParent();
            }

            assertNotNull(fs.getRoot().getFileObject(fo.getPath()));           
            f = f.getParentFile();            
        }        
    }
    
    
    /**
     * Test of createFolder method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testCreateFolder() throws Exception {
        File f = testFile;

        FileObject fo = FileBasedFileSystem.getFileObject(f);
        
        File f2 = new File (testFile, "newfoldercreated");
        FileObject nfo = fo.createFolder (f2.getName());
        assertNotNull(nfo);
        assertTrue(nfo.isFolder());
        File nfile = ((BaseFileObj)nfo).getFileName().getFile ();
        assertSame(nfo, FileBasedFileSystem.getFileObject(nfile));
        assertSame(fo, nfo.getParent());
        
        try {
            FileObject nfo2 = fo.createFolder (f2.getName());    
            fail ();
        } catch (IOException iox) {
            
        }
        
    }

    /**
     * Test of createData method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testCreateData() throws Exception {
        File f = testFile;

        final FileObject fo = FileBasedFileSystem.getFileObject(f);
        
        File f2 = new File (testFile, "newdatacreated.txt");
        final FileObject nfo = fo.createData (f2.getName());
        assertNotNull(nfo);
        assertTrue(nfo.isData());
        File nfile = ((BaseFileObj)nfo).getFileName().getFile ();
        assertEquals(nfo.getClass(), FileBasedFileSystem.getFileObject(nfile).getClass());
        assertSame(nfo, FileBasedFileSystem.getFileObject(nfile));
        /*if (nfo.getParent() != fo) {
            nfo.getParent();
        }*/
        System.gc();System.gc();System.gc();System.gc();System.gc();System.gc();
        FileObject pp = nfo.getParent();
        assertEquals(((BaseFileObj)pp).getFileName().getFile(), ((BaseFileObj)fo).getFileName().getFile());
        assertEquals(((BaseFileObj)pp).getFileName().getId(), ((BaseFileObj)fo).getFileName().getId());
        
        assertSame(((BaseFileObj)fo).getFileName().getId() + " | " + ((BaseFileObj)nfo.getParent()).getFileName().getId(), fo, pp);        
        
        try {
            FileObject nfo2 = fo.createData (f2.getName());    
            fail ();
        } catch (IOException iox) {
            
        }
    }

    
    /**
     * Test of delete method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testDelete() throws IOException {
        File f = testFile;
        
        FileObject testFo = FileBasedFileSystem.getFileObject(testFile);
        assertNotNull(testFo);
        
        final List<FileEvent> l = new ArrayList<FileEvent>();
        FileChangeListener fcl = new FileChangeAdapter () {
            @Override
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
            }            
        };
        FileObject fo = FileUtil.createData(testFo, "delete/the/whole/structure/in/depth/todelete.txt");
        fo.addFileChangeListener(fcl);
        
        FileObject toDelete = testFo.getFileObject("delete");        
        assertNotNull(toDelete);
        toDelete.addFileChangeListener(fcl);

        FileObject toGC = testFo.getFileObject("delete/the/whole/structure");
        assertNotNull(toGC);
        Reference<FileObject> toGCRef = new WeakReference<FileObject>(toGC);
        toGC.addFileChangeListener(fcl);
        toGC = null;
        
        assertGC("", toGCRef);
        toDelete.delete();
        assertEquals(0,testFo.getChildren().length);
        toDelete = testFo.getFileObject("delete");        
        assertNull(toDelete);        
        assertEquals(2, l.size());
    }

    public void testDelete2() throws IOException {
        File f = testFile;
        
        FileObject testFo = FileBasedFileSystem.getFileObject(testFile);
        assertNotNull(testFo);
        assertEquals(0,testFo.getChildren().length);
        
        FileObject newFolder = testFo.createFolder("testDelete2");
        assertNotNull(newFolder);
        assertEquals(1,testFo.getChildren().length);
        
        newFolder.delete();
        assertFalse(newFolder.isValid());
        assertEquals(0,testFo.getChildren().length);
        assertNull(testFo.getFileObject(newFolder.getNameExt()));
    }
    
    public void testExternalDelete2() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        
        FileObject testFo = FileBasedFileSystem.getFileObject(f);
        assertNotNull(testFo);
        assertTrue(testFo.isFolder());

        final List<FileEvent> l = new ArrayList<FileEvent>();
        FileChangeListener fcl = new FileChangeAdapter () {
            @Override
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }            
            @Override
            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFo.addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();
        testFo.refresh();
        assertEquals(1, l.size());        
    }

    public void testExternalDelete2_1() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        
        FileObject fo = FileUtil.toFileObject(f.getParentFile());
        
        final FileObject testFo = FileBasedFileSystem.getFileObject(f);
        assertNotNull(testFo);
        assertTrue(testFo.isFolder());

        final List<FileEvent> l = new ArrayList<FileEvent>();
        FileChangeListener fcl = new FileChangeAdapter () {
            @Override
            public void fileDeleted(FileEvent fe) {
                if (fe.getFile().equals(testFo)) {
                    l.add(fe);
                }
                fe.getFile().refresh();
            }
            @Override
            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFo.getFileSystem().addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();
        testFo.getFileSystem().refresh(true);
        assertEquals(1, l.size());        
        testFo.getFileSystem().removeFileChangeListener(fcl);
    }

    public void testExternalDelete2_2() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        
        FileObject testFo = FileBasedFileSystem.getFileObject(f);
        assertNotNull(testFo);
        assertTrue(testFo.isFolder());

        final List<FileEvent> l = new ArrayList<FileEvent>();
        FileChangeListener fcl = new FileChangeAdapter () {
            @Override
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }    
            @Override
            public void fileChanged(FileEvent fe) {
                fail();
            }            
        };

        testFo.addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();
        testFo.getFileSystem().refresh(true);
        assertEquals(1, l.size());        
    }
    
    public void testExternalDelete3() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh3/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        f = new File(f, "f.txt");
        assert !f.exists() : f.getAbsolutePath();
        assert f.createNewFile() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        
        
        FileObject testFo = FileBasedFileSystem.getFileObject(f);
        assertNotNull(testFo);
        assertTrue(testFo.isData());

        final List<FileEvent> l = new ArrayList<FileEvent>();
        FileChangeListener fcl = new FileChangeAdapter () {
            @Override
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }            

            @Override
            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFo.addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();

        //testFo.lastModified();
        //testFo.refresh();
        testFo.refresh();
        
        assertEquals(1, l.size());        
    }

    public void testExternalDelete3_1() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh3/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        f = new File(f, "f.txt");
        assert !f.exists() : f.getAbsolutePath();
        assert f.createNewFile() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        
        
        FileObject fo = FileUtil.toFileObject(f.getParentFile());        
        
        FileObject testFo = FileBasedFileSystem.getFileObject(f);
        assertNotNull(testFo);
        assertTrue(testFo.isData());

        final List<FileEvent> l = new ArrayList<FileEvent>();
        FileChangeListener fcl = new FileChangeAdapter () {
            @Override
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }
            @Override
            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFo.getFileSystem().addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();
        testFo.getFileSystem().refresh(true);
        assertEquals(1, l.size());        
        testFo.getFileSystem().removeFileChangeListener(fcl);

    }
    
    public void testExternalDelete3_2() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh3/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        f = new File(f, "f.txt");
        assert !f.exists() : f.getAbsolutePath();
        assert f.createNewFile() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        
        
        FileObject testFo = FileBasedFileSystem.getFileObject(f);
        assertNotNull(testFo);
        assertTrue(testFo.isData());

        final List<FileEvent> l = new ArrayList<FileEvent>();
        FileChangeListener fcl = new FileChangeAdapter () {
            @Override
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }            
            @Override
            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFo.addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();
        testFo.getFileSystem().refresh(true);
        assertEquals(1, l.size());        
    }
    
    
    public void testExternalDelete4() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh3/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();

        FileObject testFolder = FileBasedFileSystem.getFileObject(f);
        assertNotNull(testFolder);
        assertTrue(testFolder.isFolder());
        
        f = new File(f, "f.txt");
        assert !f.exists() : f.getAbsolutePath();
        assert f.createNewFile() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();

        FileObject testFile1 = testFolder.getFileObject(f.getName());
        assertNotNull(testFile1);
        assertTrue(testFile1.isData());
                        

        final List<FileEvent> l = new ArrayList<FileEvent>();
        FileChangeListener fcl = new FileChangeAdapter () {
            @Override
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }            
            @Override
            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFolder.addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        assertTrue(f.delete());
        testFolder.refresh();
        assertEquals("Events: " + l, 1, l.size());        
    }

    public void testExternalDelete4_1() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh3/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();

        FileObject testFolder = FileBasedFileSystem.getFileObject(f);
        assertNotNull(testFolder);
        assertTrue(testFolder.isFolder());
        
        f = new File(f, "f.txt");
        assert !f.exists() : f.getAbsolutePath();
        assert f.createNewFile() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();

        FileObject testFile1 = testFolder.getFileObject(f.getName());
        assertNotNull(testFile1);
        assertTrue(testFile1.isData());
                        

        final List<FileEvent> l = new ArrayList<FileEvent>();
        FileChangeListener fcl = new FileChangeAdapter () {
            @Override
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }            
            @Override
            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFolder.getFileSystem().addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();
        testFolder.getFileSystem().refresh(true);
       testFolder.getFileSystem().removeFileChangeListener(fcl);        
        assertEquals(1, l.size());        

    }

    public void testExternalDelete4_1_1() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh3/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();

        FileObject testFolder = FileBasedFileSystem.getFileObject(f);
        assertNotNull(testFolder);
        assertTrue(testFolder.isFolder());
        
        f = new File(f, "f.txt");
        assert !f.exists() : f.getAbsolutePath();
        assert f.createNewFile() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();

        FileObject testFile1 = FileBasedFileSystem.getFileObject(f);//!!!!!!
        assertNotNull(testFile1);
        assertTrue(testFile1.isData());
                        

        final List<FileEvent> l = new ArrayList<FileEvent>();
        FileChangeListener fcl = new FileChangeAdapter () {
            @Override
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }            
            @Override
            public void fileChanged(FileEvent fe) {
                fail();
            }
            
        };

        testFolder.getFileSystem().addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();
        testFolder.getFileSystem().refresh(true);
       testFolder.getFileSystem().removeFileChangeListener(fcl);        
        assertEquals(1, l.size());        

    }
    
    public void testExternalDelete4_2() throws IOException {
        File f = new File(testFile, "testDelete2/testForExternalRefresh3/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();

        FileObject testFolder = FileBasedFileSystem.getFileObject(f);
        assertNotNull(testFolder);
        assertTrue(testFolder.isFolder());
        
        f = new File(f, "f.txt");
        assert !f.exists() : f.getAbsolutePath();
        assert f.createNewFile() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();

        FileObject testFile1 = testFolder.getFileObject(f.getName());
        assertNotNull(testFile1);
        assertTrue(testFile1.isData());
                        

        final List<FileEvent> l = new ArrayList<FileEvent>();
        FileChangeListener fcl = new FileChangeAdapter () {
            @Override
            public void fileDeleted(FileEvent fe) {
                l.add(fe);
                fe.getFile().refresh();
            }            
            @Override
            public void fileChanged(FileEvent fe) {
                fail("Unexpected event " + fe);
            }
            
        };

        testFolder.getFileSystem().addFileChangeListener(fcl);
        assertEquals(0, l.size());
        
        f.delete();
        testFolder.refresh(true);
        assertEquals("Events: " + l, 1, l.size());        
        testFolder.getFileSystem().removeFileChangeListener(fcl);

    }
    
    
    /**
     * Test of getInputStream method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testGetInputStream() {
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance();
        
        FileObject root = fs.getRoot();
        assertNotNull(root);
        
        Enumeration<? extends FileObject> en = root.getFolders(true);
        for (int i = 0; i < 10 && en.hasMoreElements(); i++) {
            FileObject fo = (FileObject) en.nextElement();
            assertTrue(fo.isFolder());
            assertFalse(fo.isData());
            try {
                fo.getInputStream();
                fail ();
            } catch (FileNotFoundException e) {
                
            }

        }
    }

    //see issue: #43231 and #56285
    public void testRefresh43231() throws Exception {
        File thisTest = new File(getWorkDir(),getName());
        thisTest.createNewFile();
        FileObject testf = FileBasedFileSystem.getFileObject(thisTest);
        final List<FileEvent> l = new ArrayList<FileEvent>();
        testf.addFileChangeListener(new FileChangeAdapter(){
            @Override
            public void fileChanged(FileEvent fe) {
                if (l.isEmpty()) {                    
                    fail();
                }
                l.clear();
            }            
        });
        //first refresh after initialization compares
        //lastModified with oldLastModified this way:
        //if (lastModified > oldLastModified) the fileChange
        assertTrue(thisTest.setLastModified(thisTest.lastModified() - 1000000));        
        testf.refresh();
        assertTrue(l.isEmpty());        

        //every next refresh compares
        //lastModified with oldLastModified this way:
        //if (lastModified != oldLastModified) the fileChange                
        l.add(new FileEvent(testf)); // make not empty
        assertTrue(thisTest.setLastModified(thisTest.lastModified()-10000));
        testf.refresh();
        assertTrue(l.isEmpty());
    }
    
    public void testRefresh69744() throws Exception {
        File thisTest = new File(getWorkDir(),"thisTest");
        thisTest.createNewFile();
        FileObject testf = FileBasedFileSystem.getFileObject(thisTest);
        assertNotNull(testf);
        assertGC("",new WeakReference<FileObject>(testf.getParent()));
        modifyFileObject(testf, "abc");
        FileSystem fs = testf.getFileSystem();
        final List<FileEvent> l = new ArrayList<FileEvent>();
        FileChangeListener fcl = new FileChangeAdapter() {
            @Override
            public void fileChanged(FileEvent fe) {
                l.add(fe);
            }
        };
        Thread.sleep(1500);
        fs.addFileChangeListener(fcl);
        try {
            modifyFileObject(testf, "def");
            assertFalse(l.isEmpty());
        } finally {
            fs.removeFileChangeListener(fcl);
        }
    }

    private void modifyFileObject(final FileObject testf, String content) throws IOException {
        FileLock lock = null;
        OutputStream os = null;
        try {
            lock = testf.lock();
            os = testf.getOutputStream(lock);
            os.write(content.getBytes());
        } finally {
            if (os != null) os.close();
            if (lock != null) lock.releaseLock();            
        }
    }
    
    public void testFileTypeChanged() throws Exception {
        File f = new File(testFile, "testFileTypeNotRemembered/");
        assert !f.exists() : f.getAbsolutePath();
        assert f.mkdirs() : f.getAbsolutePath();
        assert f.exists() : f.getAbsolutePath();
        
        FileObject parent = FileBasedFileSystem.getFileObject(testFile);
        
        assertNotNull(parent);
        assertTrue(parent.isFolder());
        FileObject fo = parent.getFileObject("testFileTypeNotRemembered");
        assertTrue(fo.isFolder());
        
        fo.delete();
        FileObject fo2 = parent.createFolder("testFileTypeNotRemembered");
        assertNotNull(fo);
        assertTrue(fo != fo2);
        
        fo2 = FileBasedFileSystem.getFileObject(f);
        assertNotNull(fo);
        assertTrue(fo != fo2);
    }
    
    public void testRefresh2 () throws Exception {
        String childName = "refreshtest.txt";
        FileSystem fs = FileBasedFileSystem.getInstance();
        final File file = new File (testFile, childName);
        FileObject parent = FileBasedFileSystem.getFileObject(testFile);
        assertNotNull(parent);

        file.createNewFile();
        parent.getFileObject(childName);        
        parent.getChildren();
        fs.refresh(true);

        final ArrayList<FileObject> deleted = new ArrayList<FileObject>();
        final ArrayList<FileObject> created = new ArrayList<FileObject>();
        
        FileChangeListener fcl = new FileChangeAdapter () {
            @Override
            public void fileDeleted(FileEvent fe) {
                BaseFileObj fo = (BaseFileObj)fe.getFile();
                if (file.equals(fo.getFileName().getFile())) {
                    String p = fo.toString();
                    deleted.add(fo);
                }
            }

            @Override
            public void fileDataCreated(FileEvent fe) {
                BaseFileObj fo = (BaseFileObj)fe.getFile();
                if (file.equals(fo.getFileName().getFile())) {
                    String p = fo.toString();
                    created.add(fo);
                }
            }
            
        };
        fs.addFileChangeListener(fcl);
        int stepsCount = 10;
        for (int i = 0; i < stepsCount; i++) {
            assertTrue(file.delete());
            fs.refresh(true);
            
            assertTrue(file.createNewFile());
            fs.refresh(true);            
        }
        
        fs.removeFileChangeListener(fcl);
        assertEquals(stepsCount,deleted.size());
        assertEquals(stepsCount,created.size());
        
    }

    public void testRefresh3 () throws Exception {
        String childName = "refreshtest2.txt";
        FileBasedFileSystem fs = FileBasedFileSystem.getInstance();
        final File file = new File (testFile, childName);
        FileObject parent = FileBasedFileSystem.getFileObject(testFile);
        assertNotNull(parent);


        final ArrayList<String> events = new ArrayList<String>();

        final ArrayList<String> deletedIncrement = new ArrayList<String>();
        final ArrayList<String> createdIncrement = new ArrayList<String>();
        
        final ArrayList<FileObject> hardRef = new ArrayList<FileObject>();
        final FileChangeListener fcl = new FileChangeAdapter () {
            @Override
            public void fileDeleted(FileEvent fe) {
                BaseFileObj fo = (BaseFileObj)fe.getFile();
                if (file.equals(fo.getFileName().getFile())) 
                {
                    String p = fo.toString();
                    assertEquals(0, events.size());
                    assertTrue(!fo.isValid());                    
                    events.add("fo");
                    deletedIncrement.add("fo");
                    fo.removeFileChangeListener(this);
                    fo.getParent().addFileChangeListener(this);
                    hardRef.clear();                    
                    hardRef.add(fo.getParent());                                        
                    fo.getParent().getChildren ();
                }
            }


            @Override
            public void fileDataCreated(FileEvent fe) {
                BaseFileObj fo = (BaseFileObj)fe.getFile();
                if (file.equals(fo.getFileName().getFile())) 
                {
                    String p = fo.toString();
                    assertEquals(1,events.size());
                    assertTrue(fo.isValid());
                    assertTrue(events.remove("fo"));
                    createdIncrement.add("fo");
                    fo.getParent().removeFileChangeListener(this);                    
                    fo.addFileChangeListener(this);
                    hardRef.clear();                    
                    hardRef.add(fo);
                    
                }
            }

            
        };
        fs.refresh(true);
        file.createNewFile();
        hardRef.add(parent.getFileObject(childName));        
        parent.getFileObject(childName).addFileChangeListener(fcl);        
        parent = null;
        int stepsCount = 10;
        Reference<FileObject> ref2 = new WeakReference<FileObject>(FileBasedFileSystem.getFileObject(file.getParentFile()));
        assertGC("", ref2);                                
        
        for (int i = 0; i < stepsCount; i++) {
            assertTrue(file.delete());
            fs.refresh(true);
            Reference<FileObject> ref = new WeakReference<FileObject>(FileBasedFileSystem.getFileObject(file));
            assertGC("", ref);                    
            
            
            assertTrue(file.createNewFile());
            fs.refresh(true);            
                        
            ref = new WeakReference<FileObject>(FileBasedFileSystem.getFileObject(file.getParentFile()));
            assertGC(file.getParentFile().getAbsolutePath(), ref);                                                
        }
        
        fs.removeFileChangeListener(fcl);
        assertEquals(0,events.size());
        assertEquals(stepsCount,createdIncrement.size());
        assertEquals(stepsCount,deletedIncrement.size());
        
     }
    
    public void testRefreshDoesNotMultiplyFileObjects_89059 () throws Exception {
        FileObject fo = FileBasedFileSystem.getFileObject(testFile);
        fo.getChildren();
        FileSystem fs = fo.getFileSystem();
        FileChangeListener fcl = new FileChangeAdapter();
        OutputStream os = null;
        fs.addFileChangeListener(fcl);
        fo.addFileChangeListener(fcl);
        try {
            //no change
            int foInstancesInCache = Statistics.fileObjects();
            fs.refresh(false);
            assertTrue(foInstancesInCache >= Statistics.fileObjects());

            //internal change
            File ff = new File(testFile,"a/b/c/d/aa.txt");//NOI18N
            FileUtil.createData(ff);
            foInstancesInCache = Statistics.fileObjects();
            fs.refresh(false);
            assertTrue(foInstancesInCache >= Statistics.fileObjects());

            //external change
            FileObject ffObject = FileBasedFileSystem.getFileObject(ff);
            foInstancesInCache = Statistics.fileObjects();
            os = new java.io.FileOutputStream(ff);
            os.write("dsdopsdsd".getBytes());//NOI18N
            os.close();
            fs.refresh(false);
            assertTrue(foInstancesInCache >= Statistics.fileObjects());

            assertTrue(new File(testFile,"nfile").createNewFile());//NOI18N
            fs.refresh(false);
            fo.refresh(false);
            assertTrue(foInstancesInCache+1 >= Statistics.fileObjects());

            foInstancesInCache = Statistics.fileObjects();
            assertTrue(new File(testFile,"aa/bb/cc").mkdirs());//NOI18N
            fs.refresh(false);
            fo.refresh(false);
            assertTrue(foInstancesInCache+3 >= Statistics.fileObjects());
        } finally {
            if (os != null) {
                os.close();
            }
            fs.removeFileChangeListener(fcl);
            fo.removeFileChangeListener(fcl);
        }
    }
    
    /**
     * Test of getOutputStream method, of class org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj.
     */
    public void testGetOutputStream() {
        File f = testFile;
        FileSystem fs = FileBasedFileSystem.getInstance();
        
        FileObject root = fs.getRoot();
        assertNotNull(root);
        
        Enumeration<? extends FileObject> en = root.getFolders(true);
        for (int i = 0; i < 10 && en.hasMoreElements(); i++) {
            FileObject fo = (FileObject) en.nextElement();
            assertTrue(fo.isFolder());
            assertFalse(fo.isData());
            try {
                fo.getOutputStream(fo.lock());
                fail ();
            } catch (IOException e) {
                
            } finally {
                
            }

        }
    }
    
    public void testReadWrite ( ) throws Exception{
        String content = "content of data file";
        File f = testFile;
        
        BaseFileObj fo = (BaseFileObj)FileBasedFileSystem.getFileObject(f);
        assertNotNull(fo);
        File dFile = new File (fo.getFileName().getFile(),"newreadwrite.txt");
        BaseFileObj data = (BaseFileObj)fo.createData(dFile.getName());
                
        FileLock lock = data.lock();
        try {
            OutputStream os = data.getOutputStream(lock);
            os.write(content.getBytes());            
            os.close();
        } finally {
            lock.releaseLock();
        }
        
        InputStream is = data.getInputStream();
        byte[] b = new byte [content.length()];
        assertEquals(content.length(), is.read(b));
        assertEquals(new String (b),new String (b), content);                        
    }

    

    public void testDeleteNoParent() throws IOException {
        FileObject parent = FileBasedFileSystem.getFileObject(testFile).getParent();
        FileObject fobj = FileBasedFileSystem.getFileObject(testFile);
        assertNotNull(fobj);
        //parent not exists + testFile not exists
        EventsEvaluator ev = new EventsEvaluator(fobj.getFileSystem());
        Reference<FileObject> ref = new WeakReference<FileObject>(parent);
        parent = null;
        assertGC("", ref);                                
        fobj.delete();
        ev.assertDeleted(1);
    }
    
    /**
     * Test for bug 239302 - Deadlock encountered during project rename.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void testDeadlockBug239302() throws IOException, InterruptedException {
        final FileObject wd = FileBasedFileSystem.getFileObject(getWorkDir());
        final FolderObj folder = (FolderObj) wd.createFolder("folder");
        final File folderFile = FileUtil.toFile(folder);
        final FileObjectFactory factory = FileObjectFactory.getInstance(
                folderFile);

        folder.createData("a.txt");
        folder.createData("b.txt");
        folder.createData("c.txt");
        folder.createData("d.txt");
        folder.createData("E.txt");

        final Runnable rename = new Runnable() {

            @Override
            public void run() {
                FileLock lock = null;
                try {
                    lock = folder.lock();
                    folder.rename(lock, "folder" + Math.random(), null);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        };

        final Runnable invalidate = new Runnable() {

            @Override
            public void run() {
                factory.invalidateSubtree(folder, true, true);
            }
        };

        for (int i = 0; i < 100; i++) {
            Thread t1 = new Thread(rename, "Rename-test");
            Thread t2 = new Thread(invalidate, "Invalidate-test");
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        }
    }

    public void testVirtualFOs() throws IOException {
        Assume.assumeFalse(Utilities.isWindows()); // TODO fails on win
        final FileObject wd = FileBasedFileSystem.getFileObject(getWorkDir());
        FileObject nonExisting = wd.getFileObject("non-existing-folder/non-existing-folder/non-existing-child.xyz", false);
        assertFalse(nonExisting.isValid());
        assertFalse(nonExisting.getParent().isValid());
        assertFalse(nonExisting.getParent().getParent().isValid());
    }

    private class EventsEvaluator extends FileChangeAdapter {
        private int folderCreatedCount;
        private int dataCreatedCount;
        private int deletedCount;        
        private FileSystem fs;
        EventsEvaluator(FileSystem fs) throws FileStateInvalidException {
            this.fs = fs;
            fs.refresh(true);
            fs.addFileChangeListener(this);
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            super.fileFolderCreated(fe);
            folderCreatedCount++;
        }

        
        @Override
        public void fileDataCreated(FileEvent fe) {
            super.fileDataCreated(fe);
            dataCreatedCount++;
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            super.fileDeleted(fe);
            deletedCount++;
        }

        public void assertFolderCreated(int count) {
            assertEquals(this.folderCreatedCount, count);
        }
        
        public void assertDataCreated(int count) {
            assertEquals(this.dataCreatedCount, count);
        }

        public void assertDeleted(int count) {
            assertEquals(this.deletedCount, count);
        }
        
        public void resetFolderCreated() {
            folderCreatedCount = 0;
        }
        
        public void resetDataCreated() {
            dataCreatedCount = 0;
        }


        public void resetDeleted() {
            deletedCount = 0;
        }
        
        public void cleanUp() throws FileStateInvalidException {
            fs.removeFileChangeListener(this);
        }
    }
}
