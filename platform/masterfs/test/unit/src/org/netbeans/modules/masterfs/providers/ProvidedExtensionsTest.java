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

package org.netbeans.modules.masterfs.providers;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.ProvidedExtensionsAccessor;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenSupport;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.netbeans.modules.masterfs.filebasedfs.utils.Utils;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Item;
import org.openide.util.Lookup.Result;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Radek Matous
 */
public class ProvidedExtensionsTest extends NbTestCase {
    private ProvidedExtensionsImpl iListener;
    private ProvidedExtensionsImpl iListenerNoCanWrite;

    static {
        MockServices.setServices(AnnotationProviderImpl.class, AnnotationProviderImplNoCanWrite.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        BaseAnnotationProvider provider = (BaseAnnotationProvider)Lookups.metaInfServices(
                Thread.currentThread().getContextClassLoader()).lookup(BaseAnnotationProvider.class);
        assertNotNull(provider);
        iListener = lookupImpl(true);
        assertNotNull(iListener);
        iListenerNoCanWrite = lookupImpl(false);
        assertNotNull(iListenerNoCanWrite);
        clearWorkDir();
    }
    
    private ProvidedExtensionsImpl lookupImpl(boolean providesCanWrite) {
        Result<BaseAnnotationProvider> result = Lookup.getDefault().
                       lookup(new Lookup.Template(BaseAnnotationProvider.class));
        for (Item<BaseAnnotationProvider> item : result.allItems()) {
            if (!item.getId().contains(ProvidedExtensionsTest.class.getSimpleName())) {
                continue;
            }
            BaseAnnotationProvider ap = item.getInstance();
            InterceptionListener iil = ap.getInterceptionListener();
            if (iil instanceof ProvidedExtensionsImpl) {
                ProvidedExtensionsImpl extension = (ProvidedExtensionsImpl) iil;
                if (ProvidedExtensionsAccessor.IMPL.providesCanWrite(extension) == providesCanWrite) {
                    return (ProvidedExtensionsImpl) iil;
                }
            }
        }
        return null;
    }
    
    public ProvidedExtensionsTest(String testName) {
        super(testName);
    }
    
    public void testImplsFileLock() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        assertNotNull(iListener);
        iListener.clear();
        FileObject toLock = fo.createData(getName());
        assertNotNull(toLock);
        assertEquals("Check on " + iListener, 0, iListener.implsFileLockCalls);
        FileLock fLock = toLock.lock();
        try {
            assertTrue(fLock.isValid());
            assertEquals(0, iListener.implsFileUnlockCalls);                        
            assertEquals(1, iListener.implsFileLockCalls);            
        } finally {
            fLock.releaseLock();
            assertEquals("Just one unlock " + iListener, 1, iListener.implsFileUnlockCalls);                                    
        }        
    }

    public void testImplsFileLockThrowsIO() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        assertNotNull(iListener);
        iListener.clear();
        FileObject toLock = fo.createData(getName());
        assertNotNull(toLock);
        assertEquals("Check on " + iListener, 0, iListener.implsFileLockCalls);
        
        iListener.throwFromLock = new IOException("Pretend cannot lock");
        try {
            FileLock fLock = toLock.lock();
            fail("Never gets here: " + fLock);
        } catch (IOException ex) {
            assertSame("OK, got the exception", iListener.throwFromLock, ex);
            assertEquals("One call to lock", 1, iListener.implsFileLockCalls);            
            assertEquals("No call to unlock", 0, iListener.implsFileUnlockCalls);                        
        }

        iListener.throwFromLock = null;
        {
            FileLock fLock = toLock.lock();
            assertTrue(fLock.isValid());
            assertEquals("Second call to lock", 2, iListener.implsFileLockCalls);            
            fLock.releaseLock();
            assertEquals("Just one unlock " + iListener, 1, iListener.implsFileUnlockCalls);
        }
    }
    
    public void testImplsBeforeChange() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        assertNotNull(iListener);
        FileObject toChange = fo.createData("aa");
        assertNotNull(toChange);
        OutputStream os = toChange.getOutputStream();
        try {
            assertEquals(1, iListener.implsBeforeChangeCalls);            
        } finally {
            os.close();
        }
        
    }

    public void testImplsCanWrite() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        assertNotNull(iListener);
        assertNotNull(iListenerNoCanWrite);
        int nCalls = iListener.implsCanWriteCalls;
        FileObject toChange = fo.createData("cw");
        assertNotNull(toChange);
        boolean cw = toChange.canWrite();
        assertEquals(nCalls + 1, iListener.implsCanWriteCalls);
        assertEquals(0, iListenerNoCanWrite.implsCanWriteCalls);
    }
    
    public void testImplsMove() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        assertNotNull(iListener);
        FileObject toMove = fo.createData("aa");
        assertNotNull(toMove);
        FileObject whereToMove = fo.createFolder("aafolder");
        assertNotNull(whereToMove);
        
        iListener.clear();
        FileLock lock = toMove.lock();
        iListener.setLock(lock);
        try {
            assertEquals(0,iListener.implsMoveCalls);
            assertEquals(0,iListener.moveImplCalls);
            iListener.setImplsMoveRetVal(true);
            assertNotNull(toMove.move(lock, whereToMove, toMove.getName(), toMove.getExt()));
            assertEquals(1,iListener.implsMoveCalls);
            assertEquals(1,iListener.moveImplCalls);
        } finally {
            if (lock != null) {
                iListener.setLock(null);
                lock.releaseLock();
            }
        }
    }
    
    public void testImplsMove2() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        assertNotNull(iListener);
        FileObject toMove = fo.createData("aa");
        assertNotNull(toMove);
        FileObject whereToMove = fo.createFolder("aafolder");
        assertNotNull(whereToMove);
        
        iListener.clear();
        FileLock lock = toMove.lock();
        iListener.setLock(lock);
        try {
            assertEquals(0,iListener.implsMoveCalls);
            assertEquals(0,iListener.moveImplCalls);
            iListener.setImplsMoveRetVal(false);
            assertNotNull(toMove.move(lock, whereToMove, toMove.getName(), toMove.getExt()));
            assertEquals(1,iListener.implsMoveCalls);
            assertEquals(0,iListener.moveImplCalls);
        } finally {
            if (lock != null) {
                iListener.setLock(null);
                lock.releaseLock();
            }
        }
    }
    
    public void testImplsRename() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        assertNotNull(iListener);
        FileObject toRename = fo.createData("aa");
        assertNotNull(toRename);
        
        iListener.clear();
        FileLock lock = toRename.lock();
        iListener.setLock(lock);
        try {
            assertEquals(0,iListener.implsRenameCalls);
            assertEquals(0,iListener.renameImplCalls);
            iListener.setImplsRenameRetVal(true);
            assertTrue(toRename.isValid());
            assertNull(toRename.getParent().getFileObject(toRename.getExt(), toRename.getName()));
            toRename.rename(lock,toRename.getExt(), toRename.getName());
            assertEquals(1,iListener.implsRenameCalls);
            assertEquals(1,iListener.renameImplCalls);
        } finally {
            if (lock != null) {
                iListener.setLock(null);
                lock.releaseLock();
            }
        }
    }
    
    public void testImplsCaseOnlyRename() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        assertNotNull(iListener);
        FileObject toRename = fo.createData("aa");
        assertNotNull(toRename);
        
        iListener.clear();
        FileLock lock = toRename.lock();
        iListener.setLock(lock);
        try {
            assertEquals(0,iListener.implsRenameCalls);
            assertEquals(0,iListener.renameImplCalls);
            iListener.setImplsRenameRetVal(true);
            assertTrue(toRename.isValid());
            assertNull(toRename.getParent().getFileObject(toRename.getExt(), toRename.getName()));
            toRename.rename(lock,toRename.getName().toUpperCase(), toRename.getExt().toUpperCase());
            assertEquals(1,iListener.implsRenameCalls);
            assertEquals(1,iListener.renameImplCalls);
        } finally {
            if (lock != null) {
                iListener.setLock(null);
                lock.releaseLock();
            }
        }
    }

    public void testDuringAtomicAction() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        iListener.clear();
        fo.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject f = FileUtil.createData(new File(getWorkDir(), "a/b/c/d/e/f/g.txt"));
                assertEquals(7, iListener.implsCreateSuccessCalls);
                f.delete();
                assertEquals(1, iListener.implsDeleteSuccessCalls);
            }            
        });
    }
    
    public void testCreatedExternally() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        FileObject[] children = fo.getChildren(); // scan folder

        FileObject folder = fo.createFolder("folder");
        iListener.clear();
        assertEquals(0, iListener.implsCreatedExternallyCalls);
        File f = new File(FileUtil.toFile(fo), "file");
        f.createNewFile();
        assertEquals(0, iListener.implsCreatedExternallyCalls);
        fo.refresh();
        assertEquals(1, iListener.implsCreatedExternallyCalls);
    }
    
    public void testCreatedDeleteBrokenLinkExternally () throws Exception {
        if (Utilities.isWindows()) {
            log("Skipping test " + getName() + " on Windows");
            return;
        }
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        FileObject[] children = fo.getChildren(); // scan folder

        FileObject folder = fo.createFolder("folder");
        iListener.clear();
        assertEquals(0, iListener.implsCreatedExternallyCalls);
        File f = new File(FileUtil.toFile(fo), "wlock");
        ProcessBuilder pb = new ProcessBuilder().directory(f.getParentFile()).command(new String[] { "ln", "-s", "doesnotexist", f.getName() });
        pb.start().waitFor();
        assertEquals(0, iListener.implsCreatedExternallyCalls);
        fo.refresh();
        assertEquals(1, iListener.implsCreatedExternallyCalls);
        iListener.clear();
        f.delete();
        assertEquals(0, iListener.implsDeletedExternallyCalls);
        fo.refresh();
        assertEquals(1, iListener.implsDeletedExternallyCalls);
        
        // let's try once more, now it starts failing
        pb = new ProcessBuilder().directory(f.getParentFile()).command(new String[] { "ln", "-s", "doesnotexist", f.getName() });
        pb.start().waitFor();
        iListener.clear();
        assertEquals(0, iListener.implsCreatedExternallyCalls);
        fo.refresh();
        assertEquals(1, iListener.implsCreatedExternallyCalls);
        iListener.clear();
        f.delete();
        assertEquals(0, iListener.implsDeletedExternallyCalls);
        fo.refresh();
        assertEquals(1, iListener.implsDeletedExternallyCalls);
    }

    public void testDeletedExternally() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        FileObject file = fo.createData("file");

        iListener.clear();
        FileUtil.toFile(file).delete();
        assertEquals(0, iListener.implsDeletedExternallyCalls);
        fo.refresh();
        assertEquals(1, iListener.implsDeletedExternallyCalls);
    }

    public void testFileChangedExternally() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        FileObject file = fo.createData("file");
        FileUtil.toFile(file).setLastModified(System.currentTimeMillis() - 10000);
        fo.refresh();

        iListener.clear();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(FileUtil.toFile(file));
            fos.write("data".getBytes());
            fos.flush();
        } finally {
            if(fos != null) fos.close();
        }
        assertEquals(0, iListener.implsFileChangedCalls);
        fo.refresh();
        file.refresh();
        assertEquals(1, iListener.implsFileChangedCalls);
    }

    public void testMove_BeforeSuccessFailure() throws IOException {
        FileObject fromFolder = FileUtil.toFileObject(getWorkDir()).createFolder("moveFrom");
        FileObject toFolder = FileUtil.toFileObject(getWorkDir()).createFolder("moveTo");
        assertNotNull(fromFolder);
        assertNotNull(toFolder);
        FileObject toMove = fromFolder.createData("aa");
        assertNotNull(toMove);
        iListener.clear();

        assertNotNull(iListener);
        assertEquals(0,iListener.beforeMoveCalls);
        assertEquals(0,iListener.moveSuccessCalls);
        assertEquals(0,iListener.moveFailureCalls);

        // move
        FileLock lock = toMove.lock();
        toMove.move(lock, toFolder, toMove.getName(), toMove.getExt());
        assertFalse(toMove.isValid());
        assertEquals(1,iListener.beforeMoveCalls);
        assertEquals(1,iListener.moveSuccessCalls);

        iListener.clear();
        try {
            // success
            assertEquals(0,iListener.moveSuccessCalls);
            assertEquals(0,iListener.moveFailureCalls);

            // move to itself => failure
            toMove.move(lock, toFolder, toMove.getName(), toMove.getExt());
            fail();
        } catch (IOException ex) {
            // failure
            assertEquals(0,iListener.moveSuccessCalls);
            assertEquals(1,iListener.moveFailureCalls);
        }
    }

    public void testCopy_BeforeSuccessFailure() throws IOException {
        FileObject fromFolder = FileUtil.toFileObject(getWorkDir()).createFolder("copyFrom");
        FileObject toFolder = FileUtil.toFileObject(getWorkDir()).createFolder("copyTo");
        assertNotNull(fromFolder);
        assertNotNull(toFolder);
        FileObject fromCopy = fromFolder.createData("aa");
        assertNotNull(fromCopy);
        iListener.clear();

        assertNotNull(iListener);
        assertEquals(0,iListener.beforeCopyCalls);
        assertEquals(0,iListener.copySuccessCalls);
        assertEquals(0,iListener.copyFailureCalls);

        // copy
        fromCopy.copy(toFolder, fromCopy.getName(), fromCopy.getExt());
        assertTrue(fromCopy.isValid());
        assertEquals(1,iListener.beforeCopyCalls);
        assertEquals(1,iListener.copySuccessCalls);

        iListener.clear();
        try {
            // success
            assertEquals(0,iListener.copySuccessCalls);
            assertEquals(0,iListener.copyFailureCalls);

            // move to itself => failure
            fromCopy.copy(toFolder, fromCopy.getName(), fromCopy.getExt());
            fail();
        } catch (IOException ex) {
            // failure
            assertEquals(0,iListener.copySuccessCalls);
            assertEquals(1,iListener.copyFailureCalls);
        }
    }

    public void testImplsRename2() throws IOException {
        final List events = new ArrayList();
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        final FileObject toRename = fo.createData(getName());
        assertNotNull(toRename);
        
        iListener.clear();
        FileLock lock = toRename.lock();
        iListener.setLock(lock);
        FileChangeListener fcl = null;
        try {
            final String origNameExt = toRename.getNameExt();
            final String origPath = toRename.getPath();
            final File origFile = FileUtil.toFile(toRename);

            iListener.setImplsRenameRetVal(true);
            fcl =  new FileChangeAdapter() {
                public void fileRenamed(FileRenameEvent fe)  {
                    events.add(fe);
                    assertFalse(fe.getFile().getNameExt().equals(origNameExt));
                    assertNull(toRename.getParent().getFileObject(origNameExt));
                    File f = FileUtil.toFile(toRename);
                    assertNotNull(f);
                    assertNotNull(FileUtil.toFileObject(f));
                    /* sometimes fails:
                        assertSame(toRename, FileUtil.toFileObject(f));
                    */
                    assertTrue(f.exists());
                    FileObject delegate = FileBasedFileSystem.getFileObject(f);
                    assertNotNull(delegate);
                    assertTrue(delegate.isValid());
                    assertNull(FileBasedFileSystem.getFileObject(origFile));
                    assertTrue(toRename.isValid());

                    assertSame(toRename, fe.getFile());
                    assertSame(toRename.getParent(), fe.getFile().getParent());
                    try {
                        assertNotNull(toRename.getFileSystem().findResource(toRename.getPath()));
                        assertNull(toRename.getFileSystem().findResource(origPath));

                    } catch (FileStateInvalidException ex) {
                        fail();
                    }

                    assertEquals("bb",toRename.getName());
                    assertEquals("ext",toRename.getExt());
                    assertFalse(origNameExt.equals(toRename.getNameExt()));

                    assertEquals("bb",fe.getFile().getName());
                    assertEquals("ext",fe.getFile().getExt());
                    assertFalse(origNameExt.equals(fe.getFile().getNameExt()));

                    assertFalse(fe.getName().equals(fe.getFile().getName()));
                    assertFalse(fe.getExt().equals(fe.getFile().getName()));

                    //refreshes shouldn't generate any additional events
                    toRename.refresh();
                    toRename.getParent().refresh();                    
                }

                public void fileChanged(FileEvent fe) {
                    fail();
                }

                public void fileDeleted(FileEvent fe) {
                    fail();
                }

                public void fileDataCreated(FileEvent fe) {
                    fail();
                }

                public void fileFolderCreated(FileEvent fe) {
                    fail();
                }
            };
            toRename.getParent().addFileChangeListener(fcl);
            toRename.addFileChangeListener(fcl);
            toRename.getFileSystem().addFileChangeListener(fcl);
            toRename.rename(lock,"bb", "ext");
            assertNull(toRename.getParent().getFileObject(origNameExt));
            
        } finally {
            toRename.getParent().removeFileChangeListener(fcl);
            toRename.removeFileChangeListener(fcl);
            toRename.getFileSystem().removeFileChangeListener(fcl);

            if (lock != null) {
                iListener.setLock(null);
                lock.releaseLock();
            }
        }
        assertEquals(3,events.size());
    }

    public void testImplsRename3() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        assertNotNull(fo);
        final FileObject toRename = fo.createData("aa");
        assertNotNull(toRename);
        FileChangeListener fcl = null;
        
        iListener.clear();
        FileLock lock = toRename.lock();
        iListener.setLock(lock);
        try {
            final String origNameExt = toRename.getNameExt();
            final String origPath = toRename.getPath();
            final File origFile = FileUtil.toFile(toRename);
            final File newFile = new File(origFile.getParentFile(),FileInfo.composeName("bb", "ext"));


            iListener.setImplsRenameRetVal(true);
            fcl =  new FileChangeAdapter() {
                @Override
                public void fileRenamed(FileRenameEvent fe)  {
                    try {                        
                        File f = FileUtil.toFile(toRename);
                        assertNotNull(f);
                        assertNotNull(FileUtil.toFileObject(f));
                        /* sometimes fails:
                            assertSame(toRename, FileUtil.toFileObject(f));
                        */
                        assertTrue(f.exists());
                        FileObject delegate = FileBasedFileSystem.getFileObject(f);
                        assertNotNull(delegate);
                        assertTrue(delegate.isValid());
                        if (fe.getFile().getNameExt().equals(newFile.getName())) {
                            assertNull(FileBasedFileSystem.getFileObject(origFile));
                            assertNotNull(FileBasedFileSystem.getFileObject(newFile));
                            assertNotNull(FileUtil.toFileObject(newFile));
                            assertNull(FileUtil.toFileObject(origFile));
                        } else {
                            assertNotNull(FileBasedFileSystem.getFileObject(origFile));
                            assertNull(FileBasedFileSystem.getFileObject(newFile));
                            assertNull(FileUtil.toFileObject(newFile));
                            assertNotNull(FileUtil.toFileObject(origFile));
                        }
                        assertTrue(toRename.isValid());

                        assertSame(toRename, fe.getFile());
                        assertSame(toRename.getParent(), fe.getFile().getParent());
                        try {
                            assertNotNull(toRename.getFileSystem().findResource(toRename.getPath()));

                        } catch (FileStateInvalidException ex) {
                            fail();
                        }

                        assertFalse(fe.getName().equals(fe.getFile().getName()));
                        assertFalse(fe.getExt().equals(fe.getFile().getName()));

                        //refreshes shouldn't generate any additional events
                        toRename.refresh();
                        toRename.getParent().refresh();
                    } catch(AssertionFailedError afe) {
                        afe.printStackTrace();
                        throw afe;
                    }
                }

                public void fileChanged(FileEvent fe) {
                    fail();
                }

                public void fileDeleted(FileEvent fe) {
                    fail();
                }

                public void fileDataCreated(FileEvent fe) {
                    fail();
                }

                public void fileFolderCreated(FileEvent fe) {
                    fail();
                }
            };
            toRename.getParent().addFileChangeListener(fcl);
            toRename.addFileChangeListener(fcl);
            toRename.getFileSystem().addFileChangeListener(fcl);
            toRename.rename(lock,"bb", "ext");
            assertNull(toRename.getParent().getFileObject(origNameExt));
            toRename.rename(lock,"aa", "");
            toRename.rename(lock,"bb", "ext");
            toRename.rename(lock,"aa", "");
            toRename.rename(lock,"bb", "ext");
            toRename.rename(lock,"aa", "");
        } finally {
            if (lock != null) {
                iListener.setLock(null);
                lock.releaseLock();
            }
            toRename.getParent().removeFileChangeListener(fcl);
            toRename.getFileSystem().removeFileChangeListener(fcl);
        }
    }

    
    public static class AnnotationProviderImplNoCanWrite extends InterceptionListenerTest.AnnotationProviderImpl  {
        private ProvidedExtensionsImpl impl = new ProvidedExtensionsImpl(this, false);
        public InterceptionListener getInterceptionListener() {
            return impl;
        }
    }
    
    public static class AnnotationProviderImpl extends InterceptionListenerTest.AnnotationProviderImpl  {
        private ProvidedExtensionsImpl impl = new ProvidedExtensionsImpl(this, true);
        public InterceptionListener getInterceptionListener() {
            return impl;
        }
    }
    
    public static class ProvidedExtensionsImpl extends ProvidedExtensions  {
        private static File refreshCallForDir;
        private static long refreshCallRetValue;
        private static List<File> refreshCallToAdd;
        private int copyImplCalls;
        private int implsMoveCalls;
        private int moveImplCalls;
        private int implsRenameCalls;
        private int renameImplCalls;
        private int implsBeforeChangeCalls;
        private int implsCreateSuccessCalls;        
        private int implsDeleteSuccessCalls;
        private int implsCreatedExternallyCalls;
        private int implsDeletedExternallyCalls;
        private int implsFileChangedCalls;
        private int implsFileLockCalls;
        private int implsFileUnlockCalls;
        private int implsCanWriteCalls;
        private int beforeMoveCalls;
        private int moveSuccessCalls;
        private int moveFailureCalls;
        private int beforeCopyCalls;
        private int copySuccessCalls;
        private int copyFailureCalls;
        
        private static  boolean implsMoveRetVal = true;
        private static boolean implsRenameRetVal = true;
        private static boolean implsDeleteRetVal = false;
        private static boolean implsCopyRetVal = false;

        private static int cnt;
        
        public static FileLock lock;
        private final BaseAnnotationProvider provider;
        private IOException throwFromLock;
        private int implsCopyCalls;

        public ProvidedExtensionsImpl() {
            this(null, false);
        }

        public ProvidedExtensionsImpl(BaseAnnotationProvider p, boolean provideCanWrite) {
            super(provideCanWrite);
            this.provider = p;
            cnt++;
        }
        
        public static void assertCreated(String msg, boolean reallyCreated) {
            if (reallyCreated) {
                assertEquals(msg, 1, cnt);
            } else {
                assertEquals(msg, 0, cnt);
            }
        }
        
        public void clear() {
            implsMoveCalls = 0;
            moveImplCalls = 0;
            implsRenameCalls = 0;
            renameImplCalls = 0;
            implsBeforeChangeCalls = 0;
            implsCreateSuccessCalls = 0;
            implsDeleteSuccessCalls = 0;
            implsCreatedExternallyCalls = 0;
            implsDeletedExternallyCalls = 0;
            implsFileChangedCalls = 0;
            beforeMoveCalls = 0;
            moveSuccessCalls = 0;
            moveFailureCalls = 0;
            beforeCopyCalls = 0;
            copySuccessCalls = 0;
            copyFailureCalls = 0;
            implsFileLockCalls = 0;
            implsFileUnlockCalls = 0;
            implsCanWriteCalls = 0;
        }

        public boolean canWrite(File f) {
            implsCanWriteCalls++;
            return super.canWrite(f);
        }

        public void fileLocked(FileObject fo) throws IOException {
            super.fileLocked(fo);
            implsFileLockCalls++;
            if (throwFromLock != null) {
                throw throwFromLock;
            }
        }

        public void fileUnlocked(FileObject fo) {
            super.fileUnlocked(fo);
            implsFileUnlockCalls++;
        }

        private void assertLock() {
            assertFalse("No lock when calling to extensions", ChildrenSupport.isLock());
        }
        
        public void createSuccess(FileObject fo) {
            assertLock();
            super.createSuccess(fo);
            assertNotNull(FileUtil.toFile(fo));
            implsCreateSuccessCalls++;
        }

        public void deleteSuccess(FileObject fo) {
            assertLock();
            implsDeleteSuccessCalls++;
        }

        public void beforeChange(FileObject f) {
            assertLock();
            assertNotNull(FileUtil.toFile(f));
            implsBeforeChangeCalls++;
        }
        
        public void createdExternally(FileObject fo) {
            implsCreatedExternallyCalls++;
        }

        public void deletedExternally(FileObject fo) {
            implsDeletedExternallyCalls++;
        }

        public void fileChanged(FileObject fo) {
            implsFileChangedCalls++;
        }

        public void beforeMove(FileObject fo, File to) {
            to.getClass();
            assertLock();
            beforeMoveCalls++;
        }
        public void beforeCreate(FileObject parent, String name, boolean isFolder) {
            assertLock();
        }    

        public void moveSuccess(FileObject fo, File to) {
            to.getClass();
            assertLock();
            moveSuccessCalls++;
        }

        public void moveFailure(FileObject fo, File to) {
            to.getClass();
            assertLock();
            moveFailureCalls++;
        }

        public void beforeCopy(FileObject fo, File to) {
            to.getClass();
            assertLock();
            beforeCopyCalls++;
        }

        public void copySuccess(FileObject fo, File to) {
            to.getClass();
            assertLock();
            copySuccessCalls++;
        }

        public void copyFailure(FileObject fo, File to) {
            to.getClass();
            assertLock();
            copyFailureCalls++;
        }

        public static void nextRefreshCall(File forDir, long retValue, File... toAdd) {
            refreshCallForDir = forDir;
            refreshCallRetValue = retValue;
            refreshCallToAdd = Arrays.asList(toAdd);
        }

        @Override
        public long refreshRecursively(File dir, long lastTimeStamp, List<? super File> children) {
            if (Utils.equals(dir, refreshCallForDir)) {
                children.addAll(refreshCallToAdd);
                long r = refreshCallRetValue;
                refreshCallForDir = null;
                refreshCallToAdd = null;
                refreshCallRetValue = -1;
                return r;
            }
            return -1;
        }
        
        @Override
        public ProvidedExtensions.DeleteHandler getDeleteHandler(File f) {
            return (!isImplsDeleteRetVal()) ? null : new ProvidedExtensions.DeleteHandler(){
                final Set s = new HashSet();
                @Override
                public boolean delete(File file) {
                    if (file.isDirectory()) {
                        File[] childs = file.listFiles(new FileFilter() {
                            public boolean accept(File pathname) {
                                boolean accepted = pathname.isFile();
                                if (!accepted && pathname.isDirectory()) {
                                    accepted = !s.contains(pathname);
                                    if (!s.contains(pathname)) {
                                        s.add(pathname);
                                    }
                                } 
                                return accepted;
                            }
                        });
                        return childs.length == 0;
                    }
                    return file.delete();
                }                
            };
        }
                
        @Override
        public ProvidedExtensions.IOHandler getRenameHandler(final File from, final String newName) {
            implsRenameCalls++;
            final File f = new File(from.getParentFile(),newName);
            
            return (!isImplsRenameRetVal()) ? null : new ProvidedExtensions.IOHandler(){
                public void handle() throws IOException {
                    renameImplCalls++;
                    assertTrue(from.renameTo(f));
                }
            };
        }
        
        @Override
        public ProvidedExtensions.IOHandler getMoveHandler(final File from, final File to) {
            to.getClass();
            implsMoveCalls++;
            return (!isImplsMoveRetVal() || to == null) ? null : new ProvidedExtensions.IOHandler(){
                public void handle() throws IOException {
                    moveImplCalls++;
                    if (to.exists()) {
                        throw new IOException();
                    }
                    assertTrue(from.exists());
                    assertFalse(to.exists());
                    
                    assertFalse(from.equals(to));
                    if (from.isDirectory()) {
                        from.renameTo(to);
                    } else {
                        InputStream inputStream = new FileInputStream(from);
                        OutputStream outputStream = new FileOutputStream(to);
                        try {
                            FileUtil.copy(inputStream, outputStream);
                        } finally {
                            if (inputStream != null) inputStream.close();
                            if (outputStream != null) outputStream.close();
                        }
                        to.setLastModified(from.lastModified());
                        assertTrue(from.delete());
                    }
                    
                    assertFalse(from.exists());
                    assertTrue(to.exists());
                }
            };
        }
        
        @Override
        public ProvidedExtensions.IOHandler getCopyHandler(final File from, final File to) {
            to.getClass();
            if (from.isDirectory()) {
                return null;
            }
            implsCopyCalls++;
            return (!isImplsCopyRetVal() || to == null) ? null : new ProvidedExtensions.IOHandler(){
                @Override
                public void handle() throws IOException {
                    copyImplCalls++;
                    if (to.exists()) {
                        throw new IOException();
                    }
                    assertTrue(from.exists());
                    assertFalse(to.exists());
                    
                    assertFalse(from.equals(to));
                    InputStream inputStream = new FileInputStream(from);
                    OutputStream outputStream = new FileOutputStream(to);
                    try {
                        FileUtil.copy(inputStream, outputStream);
                    } finally {
                        if (inputStream != null) inputStream.close();
                        if (outputStream != null) outputStream.close();
                    }
                    assertTrue(from.exists());
                    assertTrue(to.exists());
                }
            };
        }
        
        public static FileLock getLock() {
            return lock;
        }
        
        public static void setLock(FileLock lock) {
            ProvidedExtensionsImpl.lock = lock;
        }

        public static boolean isImplsMoveRetVal() {
            return implsMoveRetVal;
        }

        public static void setImplsMoveRetVal(boolean implsMoveRetVal) {
            ProvidedExtensionsImpl.implsMoveRetVal = implsMoveRetVal;
        }

        public static boolean isImplsRenameRetVal() {
            return implsRenameRetVal;
        }

        public static void setImplsRenameRetVal(boolean implsRenameRetVal) {
            ProvidedExtensionsImpl.implsRenameRetVal = implsRenameRetVal;
        }

        public static boolean isImplsDeleteRetVal() {
            return implsDeleteRetVal;
        }

        public static void setImplsDeleteRetVal(boolean implsDeleteRetVal) {
            ProvidedExtensionsImpl.implsDeleteRetVal = implsDeleteRetVal;
        }

        public static boolean isImplsCopyRetVal() {
            return ProvidedExtensionsImpl.implsCopyRetVal;
        }
        public static void setImplsCopyRetVal(boolean v) {
            ProvidedExtensionsImpl.implsCopyRetVal = v;
        }
    }
}
