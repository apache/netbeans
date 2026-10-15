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
package org.netbeans.modules.git;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.junit.MockServices;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.masterfs.VersioningAnnotationProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Utilities;

/**
 *
 * @author ondra
 */
public class FilesystemInterceptorTest extends AbstractGitTestCase {

    public static final String PROVIDED_EXTENSIONS_REMOTE_LOCATION = "ProvidedExtensions.RemoteLocation";
    private StatusRefreshLogHandler h;

    public FilesystemInterceptorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(new Class[] {
            VersioningAnnotationProvider.class,
            GitVCS.class});
        System.setProperty("versioning.git.handleExternalEvents", "false");
        System.setProperty("org.netbeans.modules.masterfs.watcher.disable", "true");
        Git.STATUS_LOG.setLevel(Level.ALL);
        h = new StatusRefreshLogHandler(repositoryLocation);
        Git.STATUS_LOG.addHandler(h);
    }

    @Override
    protected void tearDown() throws Exception {
        Git.STATUS_LOG.removeHandler(h);
        super.tearDown();
    }

    public void testSeenRootsLogin () throws Exception {
        File folderA = new File(repositoryLocation, "folderA");
        File fileA1 = new File(folderA, "file1");
        File fileA2 = new File(folderA, "file2");
        folderA.mkdirs();
        fileA1.createNewFile();
        fileA2.createNewFile();
        File folderB = new File(repositoryLocation, "folderB");
        File fileB1 = new File(folderB, "file1");
        File fileB2 = new File(folderB, "file2");
        folderB.mkdirs();
        fileB1.createNewFile();
        fileB2.createNewFile();
        File folderC = new File(repositoryLocation, "folderC");
        File fileC1 = new File(folderC, "file1");
        File fileC2 = new File(folderC, "file2");
        folderC.mkdirs();
        fileC1.createNewFile();
        fileC2.createNewFile();

        FilesystemInterceptor interceptor = Git.getInstance().getVCSInterceptor();
        Field f = FilesystemInterceptor.class.getDeclaredField("gitFolderEventsHandler");
        f.setAccessible(true);
        Object hgFolderEventsHandler = f.get(interceptor);
        f = hgFolderEventsHandler.getClass().getDeclaredField("seenRoots");
        f.setAccessible(true);
        HashMap<File, Set<File>> map = (HashMap) f.get(hgFolderEventsHandler);

        LogHandler handler = new LogHandler();
        Git.STATUS_LOG.addHandler(handler);
        handler.setFilesToInitializeRoots(folderA);
        interceptor.pingRepositoryRootFor(folderA);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        Set<File> files = map.get(repositoryLocation);
        assertEquals(1, files.size());
        assertTrue(files.contains(folderA));
        handler.setFilesToInitializeRoots(fileA1);
        interceptor.pingRepositoryRootFor(fileA1);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        files = map.get(repositoryLocation);
        assertEquals(1, files.size());
        assertTrue(files.contains(folderA));

        handler.setFilesToInitializeRoots(fileB1);
        interceptor.pingRepositoryRootFor(fileB1);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(2, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));

        handler.setFilesToInitializeRoots(fileB2);
        interceptor.pingRepositoryRootFor(fileB2);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(3, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));
        assertTrue(files.contains(fileB2));

        handler.setFilesToInitializeRoots(folderC);
        interceptor.pingRepositoryRootFor(folderC);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(4, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));
        assertTrue(files.contains(fileB2));
        assertTrue(files.contains(folderC));

        handler.setFilesToInitializeRoots(folderB);
        interceptor.pingRepositoryRootFor(folderB);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(3, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(folderB));
        assertTrue(files.contains(folderC));

        handler.setFilesToInitializeRoots(repositoryLocation);
        interceptor.pingRepositoryRootFor(repositoryLocation);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        Git.STATUS_LOG.removeHandler(handler);
        assertEquals(1, files.size());
        assertTrue(files.contains(repositoryLocation));
    }

    private class LogHandler extends Handler {
        private File fileToInitialize;
        private boolean filesInitialized;
        private final HashSet<File> initializedFiles = new HashSet<>();

        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().contains("GitFolderEventsHandler.initializeFiles: finished")) {
                synchronized (this) {
                    filesInitialized = true;
                    notifyAll();
                }
            } else if (record.getMessage().contains("GitFolderEventsHandler.initializeFiles: ")) {
                if (record.getParameters()[0].equals(fileToInitialize.getAbsolutePath())) {
                    synchronized (this) {
                        initializedFiles.add(fileToInitialize);
                        notifyAll();
                    }
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        private void setFilesToInitializeRoots (File file) {
            fileToInitialize = file;
            initializedFiles.clear();
            filesInitialized = false;
        }

        private boolean waitForFilesToInitializeRoots() throws InterruptedException {
            for (int i = 0; i < 20; ++i) {
                synchronized (this) {
                    if (filesInitialized && initializedFiles.contains(fileToInitialize)) {
                        return true;
                    }
                    wait(500);
                }
            }
            return false;
        }
    }

    public void testGetWrongAttribute () throws Exception {
        File file = new File(repositoryLocation, "attrfile");
        file.createNewFile();
        FileObject fo = FileUtil.toFileObject(file);

        String str = (String) fo.getAttribute("peek-a-boo");
        assertNull(str);
    }

    // TODO implement getRemoteRepositoryURL
//    public void testGetRemoteLocationAttribute () throws Exception {
//        File file = new File(repositoryLocation, "attrfile");
//        file.createNewFile();
//        FileObject fo = FileUtil.toFileObject(file);
//
//        String str = (String) fo.getAttribute(PROVIDED_EXTENSIONS_REMOTE_LOCATION);
//        assertNotNull(str);
//        assertEquals(repositoryLocation.getAbsolutePath().toString(), str);
//    }

    public void testModifyVersionedFile () throws Exception {
        // init
        File file = new File(repositoryLocation, "file");
        h.setFilesToRefresh(Collections.singleton(file));
        file.createNewFile();
        add();
        commit();
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());
        assertTrue(h.waitForFilesToRefresh());

        h.setFilesToRefresh(Collections.singleton(file));
        try (PrintWriter pw = new PrintWriter(fo.getOutputStream())) {
            pw.println("hello new file");
        }
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertEquals(EnumSet.of(Status.MODIFIED_HEAD_WORKING_TREE, Status.MODIFIED_INDEX_WORKING_TREE), getCache().getStatus(file).getStatus());
    }

    public void testDeleteUnversionedFile () throws Exception {
        // init
        File file = new File(repositoryLocation, "file");
        file.createNewFile();
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());

        // delete
        h.setFilesToRefresh(Collections.singleton(file));
        delete(file);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(file.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());
    }

    public void testDeleteVersionedFile() throws Exception {
        // init
        File file = new File(repositoryLocation, "file");
        file.createNewFile();
        add(file);
        commit(repositoryLocation);
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());

        // delete
        h.setFilesToRefresh(Collections.singleton(file));
        delete(file);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(file.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
        commit(repositoryLocation);
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());
    }

    public void testDeleteVersionedFileExternally() throws Exception {
        // init
        File file = new File(repositoryLocation, "file");
        h.setFilesToRefresh(Collections.singleton(file));
        FileUtil.toFileObject(repositoryLocation).createData(file.getName());
        assertTrue(h.waitForFilesToRefresh());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
        add(file);
        commit(repositoryLocation);
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());

        // delete externally
        file.delete();

        // test
        assertFalse(file.exists());

        // notify changes
        h.setFilesToRefresh(Collections.singleton(file));
        FileUtil.refreshFor(file);
        assertTrue(h.waitForFilesToRefresh());
        assertEquals(EnumSet.of(Status.REMOVED_INDEX_WORKING_TREE, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
        delete(true, file);
        commit(repositoryLocation);
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());
    }

    public void testDeleteVersionedFolder() throws Exception {
        // init
        File folder = new File(repositoryLocation, "folder1");
        folder.mkdirs();
        File file = new File(folder, "file");
        file.createNewFile();
        add();
        commit();
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());

        // delete
        h.setFilesToRefresh(Collections.singleton(folder));
        delete(folder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(folder.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
    }
    
    public void testDeleteVersionedFolder_NoMetadata() throws Exception {
        // init
        File folder = new File(repositoryLocation, "folder1");
        folder.mkdirs();
        File file = new File(folder, "file");
        file.createNewFile();
        add();
        commit();
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());

        // delete
        Utils.deleteRecursively(new File(repositoryLocation, ".git"));
        delete(folder);
        
        // test
        assertFalse(folder.exists());
    }
    
    public void testDeleteVersionedFolder_NoMetadata_FO() throws Exception {
        // init
        File folder = new File(repositoryLocation, "folder1");
        folder.mkdirs();
        File file = new File(folder, "file");
        file.createNewFile();
        add();
        commit();
        getCache().refreshAllRoots(Collections.singleton(file));
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());

        // delete
        Utils.deleteRecursively(new File(repositoryLocation, ".git"));
        deleteFO(folder);
        
        // test
        assertFalse(folder.exists());
    }

    public void testDeleteNotVersionedFolder() throws Exception {
        // init
        File folder = new File(repositoryLocation, "folder2");
        folder.mkdirs();
        File file = new File(folder, "file");
        file.createNewFile();

        // delete
        h.setFilesToRefresh(Collections.singleton(folder));
        delete(folder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(folder.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());
    }

    public void testDeleteRepositoryLocationRoot() throws Exception {
        // delete
        File f = new File(repositoryLocation, ".aaa");
        f.createNewFile();
        add(f);
        commit(f);
        f = new File(repositoryLocation, "aaa");
        f.createNewFile();
        add(f);
        commit(f);

        delete(repositoryLocation);

        // test
        assertFalse(repositoryLocation.exists());
    }

    public void testDeleteVersionedFileTree() throws Exception {
        // init
        File folder = new File(repositoryLocation, "folder");
        folder.mkdirs();
        File folder1 = new File(folder, "folder1");
        folder1.mkdirs();
        File folder2 = new File(folder, "folder2");
        folder2.mkdirs();
        File file11 = new File(folder1, "file1");
        file11.createNewFile();
        File file12 = new File(folder1, "file2");
        file12.createNewFile();
        File file21 = new File(folder2, "file1");
        file21.createNewFile();
        File file22 = new File(folder2, "file2");
        file22.createNewFile();

        add();
        commit();

        // delete
        h.setFilesToRefresh(Collections.singleton(folder));
        delete(folder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file11).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file12).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file21).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file22).getStatus());
    }

    public void testDeleteNotVersionedFileTree() throws Exception {
        // init
        File folder = new File(repositoryLocation, "folder");
        folder.mkdirs();
        File folder1 = new File(folder, "folder1");
        folder1.mkdirs();
        File folder2 = new File(folder, "folder2");
        folder2.mkdirs();
        File file11 = new File(folder1, "file1");
        file11.createNewFile();
        File file12 = new File(folder1, "file2");
        file12.createNewFile();
        File file21 = new File(folder2, "file1");
        file21.createNewFile();
        File file22 = new File(folder2, "file2");
        file22.createNewFile();

        // delete
        h.setFilesToRefresh(Collections.singleton(folder));
        delete(folder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertFalse(file11.exists());
        assertFalse(file12.exists());
        assertFalse(file21.exists());
        assertFalse(file22.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file11).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file12).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file21).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file22).getStatus());
    }

    public void testCreateNewFile() throws Exception {
        // init
        File file = new File(repositoryLocation, "file");

        h.setFilesToRefresh(Collections.singleton(file));
        // create
        FileObject fo = FileUtil.toFileObject(repositoryLocation);
        fo.createData(file.getName());
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(file.exists());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
    }

    public void testDeleteA_CreateA() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        fileA.createNewFile();
        add();
        commit();
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());

        // delete
        h.setFilesToRefresh(Collections.singleton(fileA));
        FileObject fo = FileUtil.toFileObject(fileA);
        fo.delete();
        assertTrue(h.waitForFilesToRefresh());

        // test if deleted
        assertFalse(fileA.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());

        // create
        h.setFilesToRefresh(Collections.singleton(fileA));
        fo.getParent().createData(fo.getName());
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
    }

    public void testDeleteA_CreateA_RunAtomic() throws Exception {
        // init
        final File fileA = new File(repositoryLocation, "A");
        h.setFilesToRefresh(Collections.singleton(fileA));
        fileA.createNewFile();
        add();
        commit();
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertTrue(h.waitForFilesToRefresh());

        final FileObject fo = FileUtil.toFileObject(fileA);
        AtomicAction a = () -> {
            fo.delete();
            fo.getParent().createData(fo.getName());
        };
        h.setFilesToRefresh(Collections.singleton(fileA));
        fo.getFileSystem().runAtomicAction(a);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
    }

    public void testRenameVersionedFile_DO() throws Exception {
        File fromFile = new File(repositoryLocation, "fromFile");
        fromFile.createNewFile();
        add();
        commit();
        File toFile = new File(repositoryLocation, "toFile");

        // rename
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        renameDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertTrue(info.isRenamed());
        assertEquals(fromFile, info.getOldFile());
    }

    public void testMoveVersionedFile_DO() throws Exception {
        File fromFile = new File(repositoryLocation, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation, "toFolder");
        toFolder.mkdirs();
        add();
        commit();
        File toFile = new File(toFolder, fromFile.getName());

        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        moveDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertTrue(info.isRenamed());
        assertEquals(fromFile, info.getOldFile());
    }

    public void testMoveVersionedFile2Repos_DO() throws Exception {
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdirs();
        File repositoryLocation2 = initSecondRepository();
        File toFolder = new File(repositoryLocation2, "toFolder");
        toFolder.mkdirs();

        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        add(fromFile);
        commit(fromFile);
        File toFile = new File(toFolder, "file");
        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        moveDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveVersionedFolder2Repos_DO () throws Exception {
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdirs();
        File repositoryLocation2 = initSecondRepository();
        File toFolderParent = new File(repositoryLocation2, "folderParent");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        File toFile = new File(toFolder, "file");
        File fromFile = new File(fromFolder, toFile.getName());
        fromFile.createNewFile();
        add();
        commit();
        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        moveDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveFileTree2Repos_DO () throws Exception {
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File repositoryLocation2 = initSecondRepository();
        File toFolderParent = new File(repositoryLocation2, "toFolder");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        add(repositoryLocation);
        commit(repositoryLocation);

        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        moveDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile22).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());

        assertFalse(fromFolder.exists());
        assertFalse(fromFolder1.exists());
        assertFalse(fromFolder2.exists());
        assertFalse(fromFile11.exists());
        assertFalse(fromFile12.exists());
        assertFalse(fromFile21.exists());
        assertFalse(fromFile22.exists());
    }

    public void testMoveVersionedFile2Repos_FO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdirs();
        File repositoryLocation2 = initSecondRepository();
        File toFolder = new File(repositoryLocation2, "toFolder");
        toFolder.mkdirs();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        add(repositoryLocation);
        commit(repositoryLocation);
        File toFile = new File(toFolder, "file");
        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        moveFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveVersionedFolder2Repos_FO() throws Exception {
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdirs();
        File repositoryLocation2 = initSecondRepository();
        File toFolderParent = new File(repositoryLocation2, "folderParent");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        File toFile = new File(toFolder, "file");
        File fromFile = new File(fromFolder, toFile.getName());
        fromFile.createNewFile();
        add();
        commit();
        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        moveFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveFileTree2Repos_FO() throws Exception {
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File repositoryLocation2 = initSecondRepository();
        File toFolderParent = new File(repositoryLocation2, "toFolder");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        add(repositoryLocation);
        commit(repositoryLocation);

        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        moveFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile22).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());

        assertFalse(fromFolder.exists());
        assertFalse(fromFolder1.exists());
        assertFalse(fromFolder2.exists());
        assertFalse(fromFile11.exists());
        assertFalse(fromFile12.exists());
        assertFalse(fromFile21.exists());
        assertFalse(fromFile22.exists());
    }

    public void testRenameUnversionedFile_DO() throws Exception {
        File fromFile = new File(repositoryLocation, "fromFile");
        fromFile.createNewFile();
        File toFile = new File(repositoryLocation, "toFile");

        // rename
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        renameDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveUnversionedFile_DO() throws Exception {
        File fromFile = new File(repositoryLocation, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation, "toFolder");
        toFolder.mkdirs();

        File toFile = new File(toFolder, fromFile.getName());

        // rename
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        moveDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testRenameUnversionedFolder_DO() throws Exception {
        File fromFolder = new File(repositoryLocation, "fromFolder");
        fromFolder.mkdirs();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation, "toFolder");
        File toFile = new File(toFolder, fromFile.getName());

        // rename
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        renameDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveUnversionedFolder_DO() throws Exception {
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdirs();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        File toParent = new File(repositoryLocation, "toFolder");
        toParent.mkdirs();
        File toFolder = new File(toParent, fromFolder.getName());
        File toFile = new File(toFolder, fromFile.getName());

        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        moveDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }
    
    public void testRenameFileChangeCase_DO () throws Exception {
        // prepare
        File fromFile = createFile(repositoryLocation, "file");
        File toFile = new File(repositoryLocation, "FILE");
        add(fromFile);
        commit(fromFile);
        
        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        renameDO(fromFile, toFile);
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(Arrays.asList(toFile.getParentFile().list()).contains(toFile.getName()));
            assertFalse(Arrays.asList(fromFile.getParentFile().list()).contains(fromFile.getName()));
        } else {
            assertTrue(h.waitForFilesToRefresh());
            assertFalse(fromFile.exists());
            assertTrue(toFile.exists());
            assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
            assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
        }
    }
    
    public void testRenameFileChangeCase_FO () throws Exception {
        // prepare
        File fromFile = createFile(repositoryLocation, "file");
        File toFile = new File(repositoryLocation, "FILE");
        add(fromFile);
        commit(fromFile);
        
        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        renameFO(fromFile, toFile);
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(Arrays.asList(toFile.getParentFile().list()).contains(toFile.getName()));
            assertFalse(Arrays.asList(fromFile.getParentFile().list()).contains(fromFile.getName()));
        } else {
            assertTrue(h.waitForFilesToRefresh());
            assertFalse(fromFile.exists());
            assertTrue(toFile.exists());
            assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
            assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
        }
    }
    
    public void testRenameFolderChangeCase_DO () throws Exception {
        // prepare
        File fromFolder = createFolder(repositoryLocation, "folder");
        File fromFile = createFile(fromFolder, "file");
        File toFolder = new File(repositoryLocation, "FOLDER");
        File toFile = new File(toFolder, fromFile.getName());
        add(fromFolder);
        commit(fromFolder);
        
        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        renameDO(fromFolder, toFolder);
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(Arrays.asList(toFolder.getParentFile().list()).contains(toFolder.getName()));
            assertFalse(Arrays.asList(fromFolder.getParentFile().list()).contains(fromFolder.getName()));
        } else {
            assertTrue(h.waitForFilesToRefresh());
            assertFalse(fromFolder.exists());
            assertTrue(toFolder.exists());
            assertTrue(toFile.exists());
            assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
            assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
        }
    }
    
    public void testRenameFolderChangeCase_FO () throws Exception {
        // prepare
        File fromFolder = createFolder(repositoryLocation, "folder");
        File fromFile = createFile(fromFolder, "file");
        File toFolder = new File(repositoryLocation, "FOLDER");
        File toFile = new File(toFolder, fromFile.getName());
        add(fromFolder);
        commit(fromFolder);
        
        // move
        h.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        renameFO(fromFolder, toFolder);
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(Arrays.asList(toFolder.getParentFile().list()).contains(toFolder.getName()));
            assertFalse(Arrays.asList(fromFolder.getParentFile().list()).contains(fromFolder.getName()));
        } else {
            assertTrue(h.waitForFilesToRefresh());
            assertFalse(fromFolder.exists());
            assertTrue(toFolder.exists());
            assertTrue(toFile.exists());
            assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
            assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
        }
    }

    public void testCopyVersionedFile_DO() throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation, "toFolder");
        toFolder.mkdirs();
        add();
        commit();
        File toFile = new File(toFolder, fromFile.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFile));
        copyDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        // sadly does not work in jgit
        assertFalse(info.isCopied());
    }

    public void testCopyVersionedFile_DO_IgnoreNewFiles() throws Exception {
        try {
            GitModuleConfig.getDefault().setExcludeNewFiles(true);

            // init
            File fromFile = new File(repositoryLocation, "file");
            fromFile.createNewFile();
            File toFolder = new File(repositoryLocation, "toFolder");
            toFolder.mkdirs();
            add();
            commit();
            File toFile = new File(toFolder, fromFile.getName());

            // copy
            h.setFilesToRefresh(Collections.singleton(toFile));
            copyDO(fromFile, toFile);
            assertTrue(h.waitForFilesToRefresh());
            getCache().refreshAllRoots(Collections.singleton(fromFile));

            // test
            assertTrue(fromFile.exists());
            assertTrue(toFile.exists());

            assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
            FileInformation info = getCache().getStatus(toFile);
            assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
            // sadly does not work in jgit
            assertFalse(info.isCopied());
        } finally {
            GitModuleConfig.getDefault().setExcludeNewFiles(false);
        }
    }

    public void testCopyUnversionedFile_DO() throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation, "toFolder");
        toFolder.mkdirs();

        File toFile = new File(toFolder, fromFile.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFile));
        copyDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyUnversionedFolder_DO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdirs();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        File toParent = new File(repositoryLocation, "toFolder");
        toParent.mkdirs();
        File toFolder = new File(toParent, fromFolder.getName());
        File toFile = new File(toFolder, fromFile.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFolder));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyAddedFile2UnversionedFolder_DO() throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation.getParentFile(), "toFolder");
        toFolder.mkdirs();

        File toFile = new File(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // copy
        copyDO(fromFile, toFile);
        getCache().refreshAllRoots(Collections.singleton(fromFile));
        getCache().refreshAllRoots(Collections.singleton(toFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyVersionedFile2UnversionedFolder_DO() throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "file");
        fromFile.createNewFile();
        File unversionedFolder = new File(repositoryLocation.getParentFile(), getName() + "_unversioned");
        unversionedFolder.mkdirs();

        File toFile = new File(unversionedFolder, fromFile.getName());

        // add
        add(fromFile);
        commit();

        // copy
        copyDO(fromFile, toFile);
        getCache().refreshAllRoots(Collections.singleton(fromFile));
        getCache().refreshAllRoots(Collections.singleton(toFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyVersionedFolder2UnversionedFolder_DO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdir();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();

        File unversionedFolder = new File(repositoryLocation.getParentFile(), getName() + "_unversioned");
        unversionedFolder.mkdirs();
        File toFolder = new File(unversionedFolder, fromFolder.getName());
        File toFile = new File(toFolder, fromFile.getName());

        // add
        add(fromFolder);
        commit(fromFolder);

        // copy
        copyDO(fromFolder, toFolder);
        getCache().refreshAllRoots(new HashSet<>(Arrays.asList(fromFile, toFile)));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyAddedFile2VersionedFolder_DO() throws Exception {
        // init
        File toFolder = new File(repositoryLocation, "toFolder");
        toFolder.mkdirs();
        commit(repositoryLocation);
        File fromFile = new File(repositoryLocation, "fromFile");
        fromFile.createNewFile();

        File toFile = new File(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // rename
        h.setFilesToRefresh(Collections.singleton(toFile));
        copyDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyA2B2C_DO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        fileA.createNewFile();
        File folderB = new File(repositoryLocation, "folderB");
        folderB.mkdirs();
        File folderC = new File(repositoryLocation, "folderC");
        folderC.mkdirs();
        add();
        commit();

        File fileB = new File(folderB, fileA.getName());
        File fileC = new File(folderC, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileB, fileC)));
        copyDO(fileA, fileB);
        copyDO(fileB, fileC);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fileA));

        // test
        assertTrue(fileA.exists());
        assertTrue(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
    }

    public void testCopyVersionedFolder_DO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "from");
        fromFolder.mkdirs();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        File toParent = new File(repositoryLocation, "toFolder");
        toParent.mkdirs();
        File toFile = new File(new File(toParent, fromFolder.getName()), fromFile.getName());
        add();
        commit();

        File toFolder = new File(toParent, fromFolder.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyFileTree_DO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "from");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File toFolderParent = new File(repositoryLocation, "to");
        toFolderParent.mkdirs();

        add();
        commit();

        File toFolder = new File(toFolderParent, fromFolder.getName());

        // move
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFolder));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile22).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());
    }

    public void testCopyVersionedFile2Repos_DO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdirs();
        File repositoryLocation2 = initSecondRepository();
        File toFolder = new File(repositoryLocation2, "toFolder");
        toFolder.mkdirs();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        add();
        commit();
        File toFile = new File(toFolder, "file");

        // copy
        h.setFilesToRefresh(Collections.singleton(toFile));
        copyDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyVersionedFolder2Repos_DO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdirs();
        File repositoryLocation2 = initSecondRepository();
        File toFolderParent = new File(repositoryLocation2, "folderParent");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        File toFile = new File(toFolder, "file");
        File fromFile = new File(fromFolder, toFile.getName());
        fromFile.createNewFile();
        add(repositoryLocation);
        commit(repositoryLocation);

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFolder));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyFileTree2Repos_DO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File repositoryLocation2 = initSecondRepository();
        File toFolderParent = new File(repositoryLocation2, "toFolder");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        add(repositoryLocation);
        commit(repositoryLocation);

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFolder));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile22).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());
    }

    public void testCopyVersionedFile_FO() throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation, "toFolder");
        toFolder.mkdirs();
        add();
        commit();
        File toFile = new File(toFolder, fromFile.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFile));
        copyFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyUnversionedFile_FO() throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation, "toFolder");
        toFolder.mkdirs();

        File toFile = new File(toFolder, fromFile.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFile));
        copyFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyUnversionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdirs();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        File toParent = new File(repositoryLocation, "toFolder");
        toParent.mkdirs();
        File toFolder = new File(toParent, fromFolder.getName());
        File toFile = new File(toFolder, fromFile.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyAddedFile2UnversionedFolder_FO() throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation.getParentFile(), "toFolder");
        toFolder.mkdirs();

        File toFile = new File(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // copy
        copyFO(fromFile, toFile);
        getCache().refreshAllRoots(Collections.singleton(fromFile));
        getCache().refreshAllRoots(Collections.singleton(toFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyAddedFile2VersionedFolder_FO() throws Exception {
        // init
        File toFolder = new File(repositoryLocation, "toFolder");
        toFolder.mkdirs();
        add();
        commit();
        File fromFile = new File(repositoryLocation, "fromFile");
        fromFile.createNewFile();

        File toFile = new File(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // copy
        h.setFilesToRefresh(Collections.singleton(toFile));
        copyFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyA2B2C_FO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        fileA.createNewFile();
        File folderB = new File(repositoryLocation, "folderB");
        folderB.mkdirs();
        File folderC = new File(repositoryLocation, "folderC");
        folderC.mkdirs();
        add();
        commit();

        File fileB = new File(folderB, fileA.getName());
        File fileC = new File(folderC, fileA.getName());

        // copy
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileB, fileC)));
        copyFO(fileA, fileB);
        copyFO(fileB, fileC);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fileA));

        // test
        assertTrue(fileA.exists());
        assertTrue(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileC).getStatus());
    }

    public void testCopyVersionedFile2UnversionedFolder_FO() throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation.getParentFile(), getName() + "toFolder");
        toFolder.mkdirs();

        File toFile = new File(toFolder, fromFile.getName());

        // add
        add(fromFile);
        commit(repositoryLocation);

        // copy
        copyFO(fromFile, toFile);
        getCache().refreshAllRoots(new HashSet<>(Arrays.asList(fromFile, toFile)));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyVersionedFolder2UnversionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdir();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation.getParentFile(), getName() + "toFolder");

        File toFile = new File(toFolder, fromFile.getName());

        // add
        add(fromFolder);
        commit();

        // copy
        copyFO(fromFolder, toFolder);
        getCache().refreshAllRoots(new HashSet<>(Arrays.asList(fromFile, toFile)));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyVersionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "from");
        fromFolder.mkdirs();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        File toParent = new File(repositoryLocation, "toFolder");
        toParent.mkdirs();
        add();
        commit();

        File toFolder = new File(toParent, fromFolder.getName());
        File toFile = new File(toFolder, fromFile.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyFileTree_FO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "from");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File toFolderParent = new File(repositoryLocation, "to");
        toFolderParent.mkdirs();

        add();
        commit();

        File toFolder = new File(toFolderParent, fromFolder.getName());

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFolder));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile22).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());
    }

    public void testCopyVersionedFile2Repos_FO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdirs();
        File repositoryLocation2 = initSecondRepository();
        File toFolder = new File(repositoryLocation2, "toFolder");
        toFolder.mkdirs();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        add();
        commit();
        File toFile = new File(toFolder, "file");

        // copy
        h.setFilesToRefresh(Collections.singleton(toFile));
        copyFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyVersionedFolder2Repos_FO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdirs();
        File toFolderParent = new File(initSecondRepository(), "folderParent");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        File toFile = new File(toFolder, "file");

        File fromFile = new File(fromFolder, toFile.getName());
        fromFile.createNewFile();
        add();
        commit();

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFolder));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testCopyFileTree2Repos_FO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File toFolderParent = new File(initSecondRepository(), "toFolder");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        add();
        commit();

        // copy
        h.setFilesToRefresh(Collections.singleton(toFolder));
        copyFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFolder));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile22).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());
    }

    public void testCopyA2CB2A_FO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        fileA.createNewFile();
        File folderB = new File(repositoryLocation, "folderB");
        folderB.mkdirs();
        File fileB = new File(folderB, fileA.getName());
        fileB.createNewFile();
        File folderC = new File(repositoryLocation, "folderC");
        folderC.mkdirs();
        add();
        commit();

        File fileC = new File(folderC, fileA.getName());

        // copy
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileC, fileA)));
        copyFO(fileA, fileC);
        copyFO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fileB));

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertTrue(fileB.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileC).getStatus());
    }

    public void testRenameAddedFile_DO () throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "fromFile");
        fromFile.createNewFile();
        File toFile = new File(repositoryLocation, "toFile");

        // add
        add(fromFile);

        // rename
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile, toFile)));
        renameDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveAddedFile2Folder_DO () throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation, "toFolder");
        toFolder.mkdirs();

        File toFile = new File(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile, toFile)));
        moveDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveAddedFile2UnversionedFolder_DO () throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation.getParentFile(), "toFolder");
        toFolder.mkdirs();

        File toFile = new File(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile)));
        moveDO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testRenameA2B2A_DO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "from");
        fileA.createNewFile();
        add();
        commit();

        File fileB = new File(repositoryLocation, "to");

        // rename
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        renameDO(fileA, fileB);
        renameDO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
    }

    public void testMoveA2B2A_DO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        fileA.createNewFile();
        File folder = new File(repositoryLocation, "folder");
        folder.mkdirs();
        add();
        commit();

        File fileB = new File(folder, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        moveDO(fileA, fileB);
        moveDO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
    }

    public void testRenameA2B2C_DO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        fileA.createNewFile();
        add();
        commit();

        File fileB = new File(repositoryLocation, "B");
        File fileC = new File(repositoryLocation, "C");

        // rename
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        renameDO(fileA, fileB);
        renameDO(fileB, fileC);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fileA.exists());
        assertFalse(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        FileInformation info = getCache().getStatus(fileC);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertTrue(info.isRenamed());
        assertEquals(fileA, info.getOldFile());
    }

    public void testRenameA2B2C2A_DO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        fileA.createNewFile();
        add();
        commit();

        File fileB = new File(repositoryLocation, "B");
        File fileC = new File(repositoryLocation, "C");

        // rename
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        renameDO(fileA, fileB);
        renameDO(fileB, fileC);
        renameDO(fileC, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileC).getStatus());
    }

    public void testMoveA2CB2A_DO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        write(fileA, "aaa");
        File folderB = new File(repositoryLocation, "folderB");
        folderB.mkdirs();
        File fileB = new File(folderB, fileA.getName());
        write(fileB, "bbb");
        File folderC = new File(repositoryLocation, "folderC");
        folderC.mkdirs();
        add();
        commit();

        File fileC = new File(folderC, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        moveDO(fileA, fileC);
        moveDO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.MODIFIED_HEAD_INDEX, Status.MODIFIED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileC).getStatus());
    }

    public void testMoveA2CB2A_FO () throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        write(fileA, "aaa");
        File folderB = new File(repositoryLocation, "folderB");
        folderB.mkdirs();
        File fileB = new File(folderB, fileA.getName());
        write(fileB, "bbb");
        File folderC = new File(repositoryLocation, "folderC");
        folderC.mkdirs();
        add();
        commit();

        File fileC = new File(folderC, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        moveFO(fileA, fileC);
        moveFO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.MODIFIED_HEAD_INDEX, Status.MODIFIED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileC).getStatus());
    }

    public void testRenameA2CB2A_DO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        write(fileA, "aaa");
        File fileB = new File(repositoryLocation, "B");
        write(fileB, "bbb");
        add();
        commit();

        File fileC = new File(repositoryLocation, "C");

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        renameDO(fileA, fileC);
        renameDO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.MODIFIED_HEAD_INDEX, Status.MODIFIED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileC).getStatus());
    }

    public void testRenameA2CB2A_FO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        write(fileA, "aaa");
        File fileB = new File(repositoryLocation, "B");
        write(fileB, "bbb");
        add();
        commit();

        File fileC = new File(repositoryLocation, "C");

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        renameFO(fileA, fileC);
        renameFO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.MODIFIED_HEAD_INDEX, Status.MODIFIED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileC).getStatus());
    }

    public void testMoveA2B2C2A_DO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        fileA.createNewFile();
        File folderB = new File(repositoryLocation, "folderB");
        folderB.mkdirs();
        File folderC = new File(repositoryLocation, "folderC");
        folderC.mkdirs();
        add();
        commit();

        File fileB = new File(folderB, fileA.getName());
        File fileC = new File(folderC, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        moveDO(fileA, fileB);
        moveDO(fileB, fileC);
        moveDO(fileC, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileC).getStatus());

        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        moveDO(fileA, fileB);
        assertTrue(h.waitForFilesToRefresh());
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileB, fileC)));
        moveDO(fileB, fileC);
        assertTrue(h.waitForFilesToRefresh());
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileC)));
        moveDO(fileC, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileC).getStatus());
    }

    public void testRenameA2B_CreateA_DO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        fileA.createNewFile();
        add();
        commit();

        // rename
        File fileB = new File(repositoryLocation, "B");
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        renameDO(fileA, fileB);
        // create from file
        FileUtil.toFileObject(fileA.getParentFile()).createData(fileA.getName());
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        FileInformation info = getCache().getStatus(fileB);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
    }

    public void testMoveA2B_CreateA_DO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "file");
        fileA.createNewFile();
        File folderB = new File(repositoryLocation, "folderB");
        folderB.mkdirs();
        add();
        commit();

        File fileB = new File(folderB, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        moveDO(fileA, fileB);

        // create from file
        FileUtil.toFileObject(fileA.getParentFile()).createData(fileA.getName());
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        FileInformation info = getCache().getStatus(fileB);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
    }

    public void testDeleteA_RenameB2A_DO_129805() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        fileA.createNewFile();
        File fileB = new File(repositoryLocation, "B");
        fileB.createNewFile();
        add();
        commit();

        // delete A
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        delete(fileA);
        // rename B to A
        renameDO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fileB.exists());
        assertTrue(fileA.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
    }

    public void testRenameVersionedFolder_DO () throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "from");
        fromFolder.mkdirs();
        File file = new File(fromFolder, "file");
        file.createNewFile();
        add();
        commit();

        File toFolder = new File(repositoryLocation, "to");
        File toFile = new File(toFolder, file.getName());

        // rename
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        renameDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertTrue(info.isRenamed());
        assertEquals(file, info.getOldFile());
    }

    public void testMoveVersionedFolder_DO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "from");
        fromFolder.mkdirs();
        File toParent = new File(repositoryLocation, "toFolder");
        toParent.mkdirs();
        File file = new File(fromFolder, "file");
        file.createNewFile();
        add();
        commit();

        File toFolder = new File(toParent, fromFolder.getName());
        File toFile = new File(toFolder, file.getName());

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        moveDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertTrue(info.isRenamed());
        assertEquals(file, info.getOldFile());
    }

    public void testRenameFileTree_DO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "from");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();
        add();
        commit();

        // rename
        File toFolder = new File(repositoryLocation, "to");
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        renameDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, "folder1");
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, "folder2");
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        FileInformation info11 = getCache().getStatus(toFile11);
        FileInformation info12 = getCache().getStatus(toFile12);
        FileInformation info21 = getCache().getStatus(toFile21);
        FileInformation info22 = getCache().getStatus(toFile22);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info11.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info12.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info21.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info22.getStatus());
        assertEquals(fromFile11, info11.getOldFile());
        assertEquals(fromFile12, info12.getOldFile());
        assertEquals(fromFile21, info21.getOldFile());
        assertEquals(fromFile22, info22.getOldFile());
        assertTrue(info11.isRenamed());
        assertTrue(info12.isRenamed());
        assertTrue(info21.isRenamed());
        assertTrue(info22.isRenamed());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile22).getStatus());
    }

    public void testMoveFileTree_DO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "from");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File toFolderParent = new File(repositoryLocation, "to");
        toFolderParent.mkdirs();

        add();
        commit();

        File toFolder = new File(toFolderParent, fromFolder.getName());

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        moveDO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        FileInformation info11 = getCache().getStatus(toFile11);
        FileInformation info12 = getCache().getStatus(toFile12);
        FileInformation info21 = getCache().getStatus(toFile21);
        FileInformation info22 = getCache().getStatus(toFile22);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info11.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info12.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info21.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info22.getStatus());
        assertEquals(fromFile11, info11.getOldFile());
        assertEquals(fromFile12, info12.getOldFile());
        assertEquals(fromFile21, info21.getOldFile());
        assertEquals(fromFile22, info22.getOldFile());
        assertTrue(info11.isRenamed());
        assertTrue(info12.isRenamed());
        assertTrue(info21.isRenamed());
        assertTrue(info22.isRenamed());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile22).getStatus());
    }

    public void testRenameVersionedFile_FO() throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "fromFile");
        fromFile.createNewFile();
        add();
        commit();
        File toFile = new File(repositoryLocation, "toFile");

        // rename
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile, toFile)));
        renameFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fromFile, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testMoveVersionedFile_FO() throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation, "toFolder");
        toFolder.mkdirs();
        add();
        commit();
        File toFile = new File(toFolder, fromFile.getName());

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile, toFile)));
        moveFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fromFile, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testRenameUnversionedFile_FO() throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "fromFile");
        fromFile.createNewFile();
        File toFile = new File(repositoryLocation, "toFile");

        // rename
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile, toFile)));
        renameFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveUnversionedFile_FO() throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation, "toFolder");
        toFolder.mkdirs();

        File toFile = new File(toFolder, fromFile.getName());

        // rename
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile, toFile)));
        moveFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testRenameUnversionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "fromFolder");
        fromFolder.mkdirs();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation, "toFolder");
        File toFile = new File(toFolder, fromFile.getName());

        // rename
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        renameFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveUnversionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "folder");
        fromFolder.mkdirs();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        File toParent = new File(repositoryLocation, "toFolder");
        toParent.mkdirs();
        File toFolder = new File(toParent, fromFolder.getName());
        File toFile = new File(toFolder, fromFile.getName());

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        moveFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testRenameAddedFile_FO() throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "fromFile");
        fromFile.createNewFile();
        File toFile = new File(repositoryLocation, "toFile");

        // add
        add(fromFile);

        // rename
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile, toFile)));
        renameFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveAddedFile2UnversionedFolder_FO() throws Exception {
        // init
        File fromFile = new File(repositoryLocation, "file");
        fromFile.createNewFile();
        File toFolder = new File(repositoryLocation.getParentFile(), "toFolder");
        toFolder.mkdirs();

        File toFile = new File(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile)));
        moveFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveAddedFile2VersionedFolder_FO() throws Exception {
        // init
        File toFolder = new File(repositoryLocation, "toFolder");
        toFolder.mkdirs();
        commit(repositoryLocation);
        File fromFile = new File(repositoryLocation, "fromFile");
        fromFile.createNewFile();

        File toFile = new File(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // rename
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile, toFile)));
        moveFO(fromFile, toFile);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testRenameA2B2A_FO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "from");
        fileA.createNewFile();
        commit(repositoryLocation);

        File fileB = new File(repositoryLocation, "to");

        // rename
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        renameFO(fileA, fileB);
        renameFO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
    }

    public void testMoveA2B2A_FO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        assertFalse(fileA.exists());
        fileA.createNewFile();
        File folder = new File(repositoryLocation, "folder");
        assertFalse(folder.exists());
        folder.mkdirs();
        add();
        commit();

        File fileB = new File(folder, fileA.getName());
        assertFalse(fileB.exists());

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        moveFO(fileA, fileB);
        moveFO(fileB, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
    }

    public void testRenameA2B2C_FO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        fileA.createNewFile();
        add();
        commit();

        File fileB = new File(repositoryLocation, "B");
        File fileC = new File(repositoryLocation, "C");

        // rename
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        renameFO(fileA, fileB);
        renameFO(fileB, fileC);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fileA.exists());
        assertFalse(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        FileInformation info = getCache().getStatus(fileC);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fileA, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testMoveA2B2C_FO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        fileA.createNewFile();
        File folderB = new File(repositoryLocation, "folderB");
        folderB.mkdirs();
        File folderC = new File(repositoryLocation, "folderC");
        folderC.mkdirs();
        add();
        commit();

        File fileB = new File(folderB, fileA.getName());
        File fileC = new File(folderC, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        moveFO(fileA, fileB);
        moveFO(fileB, fileC);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fileA.exists());
        assertFalse(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        FileInformation info = getCache().getStatus(fileC);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fileA, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testRenameA2B2C2A_FO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        fileA.createNewFile();
        add();
        commit();

        File fileB = new File(repositoryLocation, "B");
        File fileC = new File(repositoryLocation, "C");

        // rename
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        renameFO(fileA, fileB);
        renameFO(fileB, fileC);
        renameFO(fileC, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileC).getStatus());
    }

    public void testMoveA2B2C2A_FO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        fileA.createNewFile();
        File folderB = new File(repositoryLocation, "folderB");
        folderB.mkdirs();
        File folderC = new File(repositoryLocation, "folderC");
        folderC.mkdirs();
        add();
        commit();

        File fileB = new File(folderB, fileA.getName());
        File fileC = new File(folderC, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        moveFO(fileA, fileB);
        moveFO(fileB, fileC);
        moveFO(fileC, fileA);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileC).getStatus());
    }

    public void testRenameA2B_CreateA_FO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "A");
        fileA.createNewFile();
        add();
        commit();

        // rename
        File fileB = new File(repositoryLocation, "B");
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        renameFO(fileA, fileB);
        assertTrue(h.waitForFilesToRefresh());

        // create from file
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA)));
        FileUtil.toFileObject(fileA.getParentFile()).createData(fileA.getName());
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());

        //should be uptodate
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        FileInformation info = getCache().getStatus(fileB);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fileA, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testMoveA2B_CreateA_FO() throws Exception {
        // init
        File fileA = new File(repositoryLocation, "file");
        fileA.createNewFile();
        File folderB = new File(repositoryLocation, "folderB");
        folderB.mkdirs();
        add();
        commit();

        File fileB = new File(folderB, fileA.getName());

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        moveFO(fileA, fileB);
        assertTrue(h.waitForFilesToRefresh());

        // create from file
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA)));
        FileUtil.toFileObject(fileA.getParentFile()).createData(fileA.getName());
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());

        //should be uptodate
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        FileInformation info = getCache().getStatus(fileB);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fileA, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testRenameVersionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "from");
        fromFolder.mkdirs();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        add();
        commit();

        File toFolder = new File(repositoryLocation, "to");
        File toFile = new File(toFolder, fromFile.getName());

        // rename
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        renameFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fromFile, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testMoveVersionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "from");
        fromFolder.mkdirs();
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        File toParent = new File(repositoryLocation, "toFolder");
        toParent.mkdirs();
        add();
        commit();

        File toFolder = new File(toParent, fromFolder.getName());
        File toFile = new File(toFolder, fromFile.getName());

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        moveFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fromFile, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testRenameFileTree_FO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "from");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();
        add();
        commit();

        // rename
        File toFolder = new File(repositoryLocation, "to");
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        renameFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, "folder1");
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, "folder2");
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        FileInformation info11 = getCache().getStatus(toFile11);
        FileInformation info12 = getCache().getStatus(toFile12);
        FileInformation info21 = getCache().getStatus(toFile21);
        FileInformation info22 = getCache().getStatus(toFile22);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info11.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info12.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info21.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info22.getStatus());
        assertEquals(fromFile11, info11.getOldFile());
        assertEquals(fromFile12, info12.getOldFile());
        assertEquals(fromFile21, info21.getOldFile());
        assertEquals(fromFile22, info22.getOldFile());
        assertTrue(info11.isRenamed());
        assertTrue(info12.isRenamed());
        assertTrue(info21.isRenamed());
        assertTrue(info22.isRenamed());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());
    }

    public void testMoveFileTree_FO() throws Exception {
        // init
        File fromFolder = new File(repositoryLocation, "from");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File toFolderParent = new File(repositoryLocation, "to");
        toFolderParent.mkdirs();

        add();
        commit();

        File toFolder = new File(toFolderParent, fromFolder.getName());

        // move
        h.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        moveFO(fromFolder, toFolder);
        assertTrue(h.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        FileInformation info11 = getCache().getStatus(toFile11);
        FileInformation info12 = getCache().getStatus(toFile12);
        FileInformation info21 = getCache().getStatus(toFile21);
        FileInformation info22 = getCache().getStatus(toFile22);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info11.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info12.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info21.getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info22.getStatus());
        assertEquals(fromFile11, info11.getOldFile());
        assertEquals(fromFile12, info12.getOldFile());
        assertEquals(fromFile21, info21.getOldFile());
        assertEquals(fromFile22, info22.getOldFile());
        assertTrue(info11.isRenamed());
        assertTrue(info12.isRenamed());
        assertTrue(info21.isRenamed());
        assertTrue(info22.isRenamed());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());
    }
    
    public void testMoveFileToIgnoredFolder_DO () throws Exception {
        // prepare
        File ignored = createFolder(repositoryLocation, "ignoredFolder");
        getClient(repositoryLocation).ignore(new File[] { ignored }, GitUtils.NULL_PROGRESS_MONITOR);
        File toFolder = createFolder(ignored, "toFolder");
        File fromFile = createFile(repositoryLocation, "file");
        File toFile = new File(toFolder, fromFile.getName());
        add(fromFile);
        commit(fromFile);
        getCache().refreshAllRoots(ignored);
        
        // move
        moveDO(fromFile, toFile);
        
        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        getCache().refreshAllRoots(fromFile, toFile);
        assertTrue(getCache().getStatus(fromFile).containsStatus(FileInformation.Status.REMOVED_HEAD_INDEX));
        assertTrue(getCache().getStatus(toFile).containsStatus(FileInformation.Status.NOTVERSIONED_EXCLUDED));
    }
    
    public void testMoveFileToIgnoredFolder_FO () throws Exception {
        // prepare
        File ignored = createFolder(repositoryLocation, "ignoredFolder");
        getClient(repositoryLocation).ignore(new File[] { ignored }, GitUtils.NULL_PROGRESS_MONITOR);
        File toFolder = createFolder(ignored, "toFolder");
        File fromFile = createFile(repositoryLocation, "file");
        File toFile = new File(toFolder, fromFile.getName());
        add(fromFile);
        commit(fromFile);
        getCache().refreshAllRoots(ignored);
        
        // move
        moveFO(fromFile, toFile);
        
        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        getCache().refreshAllRoots(fromFile, toFile);
        assertTrue(getCache().getStatus(fromFile).containsStatus(FileInformation.Status.REMOVED_HEAD_INDEX));
        assertTrue(getCache().getStatus(toFile).containsStatus(FileInformation.Status.NOTVERSIONED_EXCLUDED));
    }

    public void testCopyFileToIgnoredFolder_DO () throws Exception {
        // prepare
        File ignored = createFolder(repositoryLocation, "ignoredFolder");
        getClient(repositoryLocation).ignore(new File[] { ignored }, GitUtils.NULL_PROGRESS_MONITOR);
        File toFolder = createFolder(ignored, "toFolder");
        File fromFile = createFile(repositoryLocation, "file");
        File toFile = new File(toFolder, fromFile.getName());
        add(fromFile);
        commit(fromFile);
        getCache().refreshAllRoots(ignored);
        
        // copy
        copyDO(fromFile, toFile);
        
        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());
        getCache().refreshAllRoots(fromFile, toFile);
        assertTrue(getCache().getStatus(fromFile).containsStatus(FileInformation.Status.UPTODATE));
        assertTrue(getCache().getStatus(toFile).containsStatus(FileInformation.Status.NOTVERSIONED_EXCLUDED));
    }

    public void testCopyFileToIgnoredFolder_FO () throws Exception {
        // prepare
        File ignored = createFolder(repositoryLocation, "ignoredFolder");
        getClient(repositoryLocation).ignore(new File[] { ignored }, GitUtils.NULL_PROGRESS_MONITOR);
        File toFolder = createFolder(ignored, "toFolder");
        File fromFile = createFile(repositoryLocation, "file");
        File toFile = new File(toFolder, fromFile.getName());
        add(fromFile);
        commit(fromFile);
        getCache().refreshAllRoots(ignored);
        
        // copy
        copyFO(fromFile, toFile);
        
        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());
        getCache().refreshAllRoots(fromFile, toFile);
        assertTrue(getCache().getStatus(fromFile).containsStatus(FileInformation.Status.UPTODATE));
        assertTrue(getCache().getStatus(toFile).containsStatus(FileInformation.Status.NOTVERSIONED_EXCLUDED));
    }
    
    public void testIsModifiedAttributeFile () throws Exception {
        // file is outside of versioned space, attribute should be unknown
        File file = new File(testBase, "file");
        file.createNewFile();
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        String attributeModified = "ProvidedExtensions.VCSIsModified";
        
        Object attrValue = fo.getAttribute(attributeModified);
        assertNull(attrValue);
        
        // file inside a git repo
        file = new File(repositoryLocation, "file");
        write(file, "init");
        fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        // new file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        add();
        // added file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        commit();
        
        // unmodified file, returns FALSE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.FALSE, attrValue);
        
        write(file, "modification");
        // modified file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        write(file, "init");
        // back to up to date
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.FALSE, attrValue);
    }

    private void renameDO(File from, File to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(FileUtil.toFileObject(from));
        daoFrom.rename(to.getName());
    }

    private void renameFO(File from, File to) throws DataObjectNotFoundException, IOException {
        // ensure parent is known by filesystems
        // otherwise no event will be thrown
        FileUtil.toFileObject(from.getParentFile());
        FileObject foFrom = FileUtil.toFileObject(from);
        FileLock lock = foFrom.lock();
        try {
            foFrom.rename(lock, to.getName(), null);
        } finally {
            lock.releaseLock();
        }
    }

    private void moveDO(File from, File to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(FileUtil.toFileObject(from));
        DataObject daoTarget = DataObject.find(FileUtil.toFileObject(to.getParentFile()));
        daoFrom.move((DataFolder) daoTarget);
    }

    private void copyDO(File from, File to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(FileUtil.toFileObject(from));
        DataObject daoTarget = DataObject.find(FileUtil.toFileObject(to.getParentFile()));
        daoFrom.copy((DataFolder) daoTarget);
    }

    private void moveFO(File from, File to) throws DataObjectNotFoundException, IOException {
        FileObject foFrom = FileUtil.toFileObject(from);
        assertNotNull(foFrom);
        FileObject foTarget = FileUtil.toFileObject(to.getParentFile());
        assertNotNull(foTarget);
        FileLock lock = foFrom.lock();
        try {
            foFrom.move(lock, foTarget, to.getName(), null);
        } finally {
            lock.releaseLock();
        }
    }

    private void copyFO(File from, File to) throws DataObjectNotFoundException, IOException {
        FileObject foFrom = FileUtil.toFileObject(from);
        assertNotNull(foFrom);
        FileObject foTarget = FileUtil.toFileObject(to.getParentFile());
        assertNotNull(foTarget);
        FileLock lock = foFrom.lock();
        try {
            foFrom.copy(foTarget, getName(to), getExt(to));
        } finally {
            lock.releaseLock();
        }
    }

    private void delete(File file) throws IOException {
        DataObject dao = DataObject.find(FileUtil.toFileObject(file));
        dao.delete();
    }
    
    private void deleteFO (File toDelete) throws DataObjectNotFoundException, IOException {
        FileObject fo = FileUtil.toFileObject(toDelete);
        assertNotNull(fo);
        FileLock lock = fo.lock();
        try {
            fo.delete(lock);
        } finally {
            lock.releaseLock();
        }
    }

    private String getName(File f) {
        String ret = f.getName();
        int idx = ret.lastIndexOf(".");
        return idx > -1 ? ret.substring(0, idx) : ret;
    }

    private String getExt(File f) {
        String ret = f.getName();
        int idx = ret.lastIndexOf(".");
        return idx > -1 ? ret.substring(idx) : null;
    }
}
