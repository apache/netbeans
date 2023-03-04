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

package org.netbeans.modules.mercurial;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgRepositoryContextCache;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.masterfs.VersioningAnnotationProvider;
import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Utilities;

/**
 *
 * @author tomas
 */
public class InterceptorTest extends AbstractHgTestCase {

    public InterceptorTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", new File(getWorkDir().getParentFile(), "userdir").getAbsolutePath());
        super.setUp();
        MockServices.setServices(new Class[] {
            VersioningAnnotationProvider.class,
            MercurialVCS.class});
        // create
        FileObject fo = FileUtil.toFileObject(getWorkTreeDir());
    }

    public void testGetAttributeRefreh() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        FileObject fo = FileUtil.toFileObject(file);
        Runnable attr = (Runnable) fo.getAttribute("ProvidedExtensions.Refresh");
        assertNotNull(attr);

        attr.run();
        // XXX check status
    }

    public void testGetAttributeWrong() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        FileObject fo = FileUtil.toFileObject(file);
        String attr = (String) fo.getAttribute("peek-a-boo");
        assertNull(attr);
    }

    public void testGetAttributeNotCloned() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        FileObject fo = FileUtil.toFileObject(file);
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNull(attr);
    }

    public void testGetAttributeClonedRoot() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        File cloned = clone(getWorkTreeDir());

        FileObject fo = FileUtil.toFileObject(cloned);
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(getWorkTreeDir().getAbsolutePath(), attr);
    }

    public void testGetAttributeCloned() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        File cloned = clone(getWorkTreeDir());

        FileObject fo = FileUtil.toFileObject(new File(new File(cloned, folder.getName()), file.getName()));
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(getWorkTreeDir().getAbsolutePath(), attr);
    }

    public void testGetAttributeClonedOnlyPush() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        File cloned = clone(getWorkTreeDir());

        String defaultPush = "http://a.repository.far.far/away";
        new HgConfigFiles(cloned).removeProperty(HgConfigFiles.HG_PATHS_SECTION, HgConfigFiles.HG_DEFAULT_PULL);
        new HgConfigFiles(cloned).removeProperty(HgConfigFiles.HG_PATHS_SECTION, HgConfigFiles.HG_DEFAULT_PULL_VALUE);
        new HgConfigFiles(cloned).setProperty(HgConfigFiles.HG_DEFAULT_PUSH, defaultPush);
        HgRepositoryContextCache.getInstance().reset();

        FileObject fo = FileUtil.toFileObject(new File(new File(cloned, folder.getName()), file.getName()));
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(defaultPush, attr);
    }

    public void testGetAttributeClonedPull() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        File cloned = clone(getWorkTreeDir());

        String defaultPull = "http://a.repository.far.far/away";
        new HgConfigFiles(cloned).setProperty(HgConfigFiles.HG_DEFAULT_PULL, defaultPull);
        HgRepositoryContextCache.getInstance().reset();

        FileObject fo = FileUtil.toFileObject(new File(new File(cloned, folder.getName()), file.getName()));
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(defaultPull, attr);
    }

    public void testGetAttributeClonedPullWithCredentials() throws HgException, IOException {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");

        commit(folder);
        File cloned = clone(getWorkTreeDir());

        String defaultPull = "http://so:secure@a.repository.far.far/away";
        String defaultPullReturned = "http://a.repository.far.far/away";

        new HgConfigFiles(cloned).setProperty(HgConfigFiles.HG_DEFAULT_PULL, defaultPull);
        HgRepositoryContextCache.getInstance().reset();

        FileObject fo = FileUtil.toFileObject(new File(new File(cloned, folder.getName()), file.getName()));
        String attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertEquals(defaultPullReturned, attr);
    }

    public void testFullScanLimitedOnVisibleRoots () throws Exception {
        File repo = new File(getWorkDir(), String.valueOf(System.currentTimeMillis()));
        repo.mkdir();
        File folderA = new File(repo, "folderA");
        File fileA1 = new File(folderA, "file1");
        File fileA2 = new File(folderA, "file2");
        folderA.mkdirs();
        fileA1.createNewFile();
        fileA2.createNewFile();
        File folderB = new File(repo, "folderB");
        File fileB1 = new File(folderB, "file1");
        File fileB2 = new File(folderB, "file2");
        folderB.mkdirs();
        fileB1.createNewFile();
        fileB2.createNewFile();
        File folderC = new File(repo, "folderC");
        File fileC1 = new File(folderC, "file1");
        File fileC2 = new File(folderC, "file2");
        folderC.mkdirs();
        fileC1.createNewFile();
        fileC2.createNewFile();

        HgCommand.doCreate(repo, NULL_LOGGER);

        MercurialInterceptor interceptor = Mercurial.getInstance().getMercurialInterceptor();
        Field f = MercurialInterceptor.class.getDeclaredField("hgFolderEventsHandler");
        f.setAccessible(true);
        Object hgFolderEventsHandler = f.get(interceptor);
        f = hgFolderEventsHandler.getClass().getDeclaredField("seenRoots");
        f.setAccessible(true);
        HashMap<File, Set<File>> map = (HashMap) f.get(hgFolderEventsHandler);

        getCache().markAsSeenInUI(folderA);
        // some time for bg threads
        Thread.sleep(3000);
        Set<File> files = map.get(repo);
        assertTrue(files.contains(folderA));

        getCache().markAsSeenInUI(fileB1);
        // some time for bg threads
        Thread.sleep(3000);
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));

        getCache().markAsSeenInUI(fileB2);
        // some time for bg threads
        Thread.sleep(3000);
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(folderB));

        getCache().markAsSeenInUI(folderC);
        // some time for bg threads
        Thread.sleep(3000);
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(folderB));
        assertTrue(files.contains(folderC));

        getCache().markAsSeenInUI(repo);
        // some time for bg threads
        Thread.sleep(3000);
        assertTrue(files.contains(repo));

        Utils.deleteRecursively(repo);
    }

    public void testCopyFile_SingleRepo_FO () throws Exception {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");
        File copy = new File(folder, "copy");

        commit(folder);
        copyFO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file).getStatus());
        FileInformation st = getCachedStatus(copy, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());

        File target = createFolder("target");
        copy = new File(target, file.getName());
        copyFO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file).getStatus());
        st = getCachedStatus(copy, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());
    }

    public void testCopyFile_SingleRepo_DO () throws Exception {
        File folder = createFolder("folder");
        File file = createFile(folder, "file");
        File targetFolder = createFolder("target");
        File copy = new File(targetFolder, file.getName());

        commit(folder);
        copyDO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file).getStatus());
        FileInformation st = getCachedStatus(copy, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());
    }

    public void testCopyUnversionedFile_SingleRepo_FO () throws Exception {
        File folder = createFolder("folder");
        commit(folder);
        File file = createFile(folder, "file");
        File copy = new File(folder, "copy");

        copyFO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file).getStatus());
        FileInformation st = getCachedStatus(copy, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));

        File target = createFolder("target");
        copy = new File(target, file.getName());
        copyFO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file).getStatus());
        st = getCachedStatus(copy, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));
    }

    public void testCopyUnversionedFile_SingleRepo_DO () throws Exception {
        File folder = createFolder("folder");
        commit(folder);
        File file = createFile(folder, "file");
        File targetFolder = createFolder("target");
        File copy = new File(targetFolder, file.getName());

        copyDO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file).getStatus());
        FileInformation st = getCachedStatus(copy, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));
    }

    public void testCopyFolder_SingleRepo_FO () throws Exception {
        File folder = createFolder("folder");
        File file1 = createFile(folder, "file1");
        File file2 = createFile(folder, "file2");
        File copy = new File(folder.getParentFile(), "copy");
        File copiedFile1 = new File(copy, file1.getName());
        File copiedFile2 = new File(copy, file2.getName());

        commit(folder);
        copyFO(folder,copy);
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(copiedFile1.exists());
        assertTrue(copiedFile2.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file2).getStatus());
        FileInformation st = getCachedStatus(copiedFile1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());
        st = getCachedStatus(copiedFile2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());


        File target = createFolder("target");
        copy = new File(target, folder.getName());
        copiedFile1 = new File(copy, file1.getName());
        copiedFile2 = new File(copy, file2.getName());
        copyFO(folder, copy);
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(copiedFile1.exists());
        assertTrue(copiedFile2.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file2).getStatus());
        st = getCachedStatus(copiedFile1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());
        st = getCachedStatus(copiedFile2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());
    }

    public void testCopyFolder_SingleRepo_DO () throws Exception {
        File folder = createFolder("folder");
        File file1 = createFile(folder, "file1");
        File file2 = createFile(folder, "file2");
        File target = createFolder("target");
        File copy = new File(target, folder.getName());
        File copiedFile1 = new File(copy, file1.getName());
        File copiedFile2 = new File(copy, file2.getName());

        commit(folder);
        copyDO(folder,copy);
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(copiedFile1.exists());
        assertTrue(copiedFile2.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file2).getStatus());
        FileInformation st = getCachedStatus(copiedFile1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());
        st = getCachedStatus(copiedFile2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());
    }

    public void testCopyUnversionedFolder_SingleRepo_FO () throws Exception {
        File folder = createFolder("folder");
        commit(folder);
        File file1 = createFile(folder, "file1");
        File file2 = createFile(folder, "file2");
        File target = createFolder("target");
        File copy = new File(target, folder.getName());
        File copiedFile1 = new File(copy, file1.getName());
        File copiedFile2 = new File(copy, file2.getName());

        copyFO(folder,copy);
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(copiedFile1.exists());
        assertTrue(copiedFile2.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file2).getStatus());
        FileInformation st = getCachedStatus(copiedFile1, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));
        st = getCachedStatus(copiedFile2, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));
    }

    public void testCopyUnversionedFolder_SingleRepo_DO() throws Exception {
        File folder = createFolder("folder");
        commit(folder);
        File file1 = createFile(folder, "file1");
        File file2 = createFile(folder, "file2");
        File target = createFolder("target");
        File copy = new File(target, folder.getName());
        File copiedFile1 = new File(copy, file1.getName());
        File copiedFile2 = new File(copy, file2.getName());

        copyDO(folder,copy);
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(copiedFile1.exists());
        assertTrue(copiedFile2.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file2).getStatus());
        FileInformation st = getCachedStatus(copiedFile1, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));
        st = getCachedStatus(copiedFile2, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));
    }

    public void testCopyTree_SingleRepo_FO () throws Exception {
        File folder = createFolder("folder");
        File target = createFolder("target");
        File copy = new File(target, "copy");
        File[] copies = prepareTree(folder, copy);

        commit(folder);

        copyFO(folder, copy);
        for (File f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
            assertTrue(st.getStatus(null).isCopied());
        }
    }

    public void testCopyTree_SingleRepo_DO () throws Exception {
        File folder = createFolder("folder");
        File target = createFolder("target");
        File copy = new File(target, folder.getName());
        File[] copies = prepareTree(folder, copy);

        commit(folder);

        copyDO(folder, copy);
        for (File f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
            assertTrue(st.getStatus(null).isCopied());
        }
    }

    public void testCopyUnversionedTree_SingleRepo_FO () throws Exception {
        File folder = createFolder("folder");
        commit(folder);
        File target = createFolder("target");
        File copy = new File(target, folder.getName());
        File[] copies = prepareTree(folder, copy);

        copyFO(folder, copy);
        for (File f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
            assertNull(st.getStatus(null));
        }
    }

    public void testCopyUnversionedTree_SingleRepo_DO() throws Exception {
        File folder = createFolder("folder");
        commit(folder);
        File target = createFolder("target");
        File copy = new File(target, folder.getName());
        File[] copies = prepareTree(folder, copy);

        copyDO(folder, copy);
        for (File f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
            assertNull(st.getStatus(null));
        }
    }

    public void testCopyTree_TwoRepos_FO () throws Exception {
        File folder = createFolder("folder");
        commit(folder);
        File target = createFolder("target");
        HgCommand.doCreate(target, NULL_LOGGER);
        Mercurial.getInstance().versionedFilesChanged();
        File copy = new File(target, folder.getName());
        File[] copies = prepareTree(folder, copy);

        copyFO(folder, copy);
        for (File f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
            assertNull(st.getStatus(null));
        }
    }

    public void testCopyTree_TwoRepos_DO() throws Exception {
        File folder = createFolder("folder");
        commit(folder);
        File target = createFolder("target");
        HgCommand.doCreate(target, NULL_LOGGER);
        Mercurial.getInstance().versionedFilesChanged();
        File copy = new File(target, folder.getName());
        File[] copies = prepareTree(folder, copy);

        copyDO(folder, copy);
        for (File f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
            assertNull(st.getStatus(null));
        }
    }

    public void testCopyTree_UnversionedTarget_FO () throws Exception {
        File folder = createFolder("folder");
        commit(folder);
        File target = new File("/tmp/mercurialtest_target_" + getName() + "_" + System.currentTimeMillis());
        target.mkdirs();
        try {
            File copy = new File(target, folder.getName());
            File[] copies = prepareTree(folder, copy);

            copyFO(folder, copy);
            Mercurial.getInstance().versionedFilesChanged();
            for (File f : copies) {
                assertTrue(f.exists());
                FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NOTMANAGED);
                assertNull(st.getStatus(null));
                assertNull(Mercurial.getInstance().getRepositoryRoot(f));
            }
        } finally {
            // cleanup, temp folder is outside workdir
            FileUtils.deleteRecursively(target);
        }
    }

    public void testCopyTree_UnversionedTarget_DO() throws Exception {
        File folder = createFolder("folder");
        commit(folder);
        File target = new File("/tmp/mercurialtest_target_" + getName() + "_" + System.currentTimeMillis());
        target.mkdirs();
        try {
            File copy = new File(target, folder.getName());
            File[] copies = prepareTree(folder, copy);

            copyDO(folder, copy);
            Mercurial.getInstance().versionedFilesChanged();
            for (File f : copies) {
                assertTrue(f.exists());
                FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NOTMANAGED);
                assertNull(st.getStatus(null));
                assertNull(Mercurial.getInstance().getRepositoryRoot(f));
            }
        } finally {
            // cleanup, temp folder is outside workdir
            FileUtils.deleteRecursively(target);
        }
    }
    
    public void testMoveFileToIgnoredFolder_DO () throws Exception {
        // prepare
        File folder = createFolder("ignoredFolder");
        HgUtils.addIgnored(folder.getParentFile(), new File[] { folder });
        File toFolder = createFolder(folder, "toFolder");
        File fromFile = createFile("file");
        File toFile = new File(toFolder, fromFile.getName());
        commit(fromFile);
        
        // move
        moveDO(fromFile, toFile);
        
        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getCache().refresh(toFile).getStatus());
    }
    
    public void testMoveFileToIgnoredFolder_FO () throws Exception {
        // prepare
        File folder = createFolder("ignoredFolder");
        HgUtils.addIgnored(folder.getParentFile(), new File[] { folder });
        File toFolder = createFolder(folder, "toFolder");
        File fromFile = createFile("file");
        File toFile = new File(toFolder, fromFile.getName());
        commit(fromFile);
        
        // move
        moveFO(fromFile, toFile);
        
        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getCache().refresh(toFile).getStatus());
    }
    
    public void testRenameFileChangeCase_DO () throws Exception {
        // prepare
        File fromFile = createFile("file");
        File toFile = new File(getWorkTreeDir(), "FILE");
        commit(fromFile);
        
        // move
        renameDO(fromFile, toFile.getName());
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(Arrays.asList(toFile.getParentFile().list()).contains(toFile.getName()));
            assertFalse(Arrays.asList(fromFile.getParentFile().list()).contains(fromFile.getName()));
        } else {
            assertFalse(fromFile.exists());
            assertTrue(toFile.exists());
            assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
            assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCache().refresh(toFile).getStatus());
        }
    }
    
    public void testRenameFileChangeCase_FO () throws Exception {
        // prepare
        File fromFile = createFile("file");
        File toFile = new File(getWorkTreeDir(), "FILE");
        commit(fromFile);
        
        // move
        renameFO(fromFile, toFile.getName());
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(Arrays.asList(toFile.getParentFile().list()).contains(toFile.getName()));
            assertFalse(Arrays.asList(fromFile.getParentFile().list()).contains(fromFile.getName()));
        } else {
            assertFalse(fromFile.exists());
            assertTrue(toFile.exists());
            assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
            assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCache().refresh(toFile).getStatus());
        }
    }
    
    public void testRenameFolderChangeCase_DO () throws Exception {
        // prepare
        File fromFolder = createFolder("folder");
        File fromFile = createFile(fromFolder, "file");
        File toFolder = new File(getWorkTreeDir(), "FOLDER");
        File toFile = new File(toFolder, fromFile.getName());
        commit(fromFolder);
        
        // move
        renameDO(fromFolder, toFolder.getName());
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(Arrays.asList(toFolder.getParentFile().list()).contains(toFolder.getName()));
            assertFalse(Arrays.asList(fromFolder.getParentFile().list()).contains(fromFolder.getName()));
        } else {
            assertFalse(fromFolder.exists());
            assertTrue(toFolder.exists());
            assertTrue(toFile.exists());
            assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
            assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCache().refresh(toFile).getStatus());
        }
    }
    
    public void testRenameFolderChangeCase_FO () throws Exception {
        // prepare
        File fromFolder = createFolder("folder");
        File fromFile = createFile(fromFolder, "file");
        File toFolder = new File(getWorkTreeDir(), "FOLDER");
        File toFile = new File(toFolder, fromFile.getName());
        commit(fromFolder);
        
        // move
        renameFO(fromFolder, toFolder.getName());
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(Arrays.asList(toFolder.getParentFile().list()).contains(toFolder.getName()));
            assertFalse(Arrays.asList(fromFolder.getParentFile().list()).contains(fromFolder.getName()));
        } else {
            assertFalse(fromFolder.exists());
            assertTrue(toFolder.exists());
            assertTrue(toFile.exists());
            assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
            assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCache().refresh(toFile).getStatus());
        }
    }
    
    public void testCopyFileToIgnoredFolder_DO () throws Exception {
        // prepare
        File folder = createFolder("ignoredFolder");
        HgUtils.addIgnored(folder.getParentFile(), new File[] { folder });
        File toFolder = createFolder(folder, "toFolder");
        File fromFile = createFile("file");
        File toFile = new File(toFolder, fromFile.getName());
        commit(fromFile);
        
        // move
        copyDO(fromFile, toFile);
        
        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(fromFile).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getCache().refresh(toFile).getStatus());
    }
    
    public void testCopyFileToIgnoredFolder_FO () throws Exception {
        // prepare
        File folder = createFolder("ignoredFolder");
        HgUtils.addIgnored(folder.getParentFile(), new File[] { folder });
        File toFolder = createFolder(folder, "toFolder");
        File fromFile = createFile("file");
        File toFile = new File(toFolder, fromFile.getName());
        commit(fromFile);
        
        // move
        copyFO(fromFile, toFile);
        
        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(fromFile).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getCache().refresh(toFile).getStatus());
    }
    
    public void testDeleteFile_FO () throws Exception {
        File folder = createFolder("folder");
        File file = createFile(folder, "file1");
        commit(folder);
        
        createFile(folder, "file2");
        deleteFO(file);
        assertFalse(file.exists());
        assertTrue(folder.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file).getStatus());
    }
    
    public void testDeleteFileDO () throws Exception {
        File folder = createFolder("folder");
        File file = createFile(folder, "file1");
        commit(folder);
        
        createFile(folder, "file2");
        deleteDO(file);
        assertFalse(file.exists());
        assertTrue(folder.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file).getStatus());
    }
    
    public void testDeleteFolder_FO () throws Exception {
        File folder = createFolder("folder");
        File file1 = createFile(folder, "file1");
        File file2 = createFile(folder, "file2");
        commit(folder);
        
        deleteFO(folder);
        assertFalse(file1.exists());
        assertFalse(file2.exists());
        assertFalse(folder.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file2).getStatus());
    }
    
    public void testDeleteFolder_DO () throws Exception {
        File folder = createFolder("folder");
        File file1 = createFile(folder, "file1");
        File file2 = createFile(folder, "file2");
        commit(folder);
        
        deleteDO(folder);
        assertFalse(file1.exists());
        assertFalse(file2.exists());
        assertFalse(folder.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file2).getStatus());
    }
    
    public void testIsModifiedAttributeFile () throws Exception {
        // file is outside of versioned space, attribute should be unknown
        File file = File.createTempFile("testIsModifiedAttributeFile", "txt");
        file.deleteOnExit();
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        String attributeModified = "ProvidedExtensions.VCSIsModified";
        
        Object attrValue = fo.getAttribute(attributeModified);
        assertNull(attrValue);
        
        // file inside a git repo
        file = new File(getWorkTreeDir(), "file");
        write(file, "init");
        fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        // new file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        HgCommand.doAdd(getWorkTreeDir(), file, NULL_LOGGER);
        // added file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        commit(file);
        
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

    private void moveDO (File from, File to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(FileUtil.toFileObject(from));
        DataObject daoTarget = DataObject.find(FileUtil.toFileObject(to.getParentFile()));
        daoFrom.move((DataFolder) daoTarget);
    }

    private void moveFO (File from, File to) throws DataObjectNotFoundException, IOException {
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

    private void deleteDO (File toDelete) throws DataObjectNotFoundException, IOException {
        DataObject dao = DataObject.find(FileUtil.toFileObject(toDelete));
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
    
    private void renameDO (File from, String newName) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(FileUtil.toFileObject(from));
        daoFrom.rename(newName);
    }

    private void renameFO (File from, String newName) throws DataObjectNotFoundException, IOException {
        // need to let FS know about it
        FileObject parent = FileUtil.toFileObject(from.getParentFile());
        FileObject foFrom = FileUtil.toFileObject(from);
        assertNotNull(foFrom);
        FileLock lock = foFrom.lock();
        try {
            foFrom.rename(lock, newName, null);
        } finally {
            lock.releaseLock();
        }
    }

    private void copyDO (File from, File to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(FileUtil.toFileObject(from));
        DataObject daoTarget = DataObject.find(FileUtil.toFileObject(to.getParentFile()));
        daoFrom.copy((DataFolder) daoTarget);
    }

    private void copyFO (File from, File to) throws DataObjectNotFoundException, IOException {
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

    private FileInformation getCachedStatus (File file, int expectedStatus) throws InterruptedException {
        for (int i = 0; i < 20; ++i) {
            FileInformation info = getCache().getCachedStatus(file);
            if ((info.getStatus() & expectedStatus) != 0) {
                return info;
            }
            Thread.sleep(1000);
        }
        fail("Status " + expectedStatus + " expected for " + file);
        return null;
    }

    private File[] prepareTree (File folder, File copy) throws IOException {
        createFile(folder, "file1");
        createFile(folder, "file2");
        File subfolder1 = createFolder(folder, "subfolder1");
        createFile(subfolder1, "file1");
        createFile(subfolder1, "file2");
        File subfolder1_1 = createFolder(subfolder1, "subfolder1_1");
        createFile(subfolder1_1, "file1");
        createFile(subfolder1_1, "file2");
        File subfolder1_2 = createFolder(subfolder1, "subfolder1_2");
        createFile(subfolder1_2, "file1");
        createFile(subfolder1_2, "file2");
        File subfolder2 = createFolder(folder, "subfolder2");
        createFile(subfolder2, "file1");
        createFile(subfolder2, "file2");
        File subfolder2_1 = createFolder(subfolder2, "subfolder2_1");
        createFile(subfolder2_1, "file1");
        createFile(subfolder2_1, "file2");
        File subfolder2_2 = createFolder(subfolder2, "subfolder2_2");
        createFile(subfolder2_2, "file1");
        createFile(subfolder2_2, "file2");

        return new File[] { new File(copy, "file1"), new File(copy, "file2"), new File(new File(copy, "subfolder1"), "file1"), new File(new File(copy, "subfolder1"), "file2"),
            new File(new File(copy, "subfolder2"), "file1"), new File(new File(copy, "subfolder2"), "file2"),
            new File(new File(new File(copy, "subfolder1"), "subfolder1_1"), "file1"), new File(new File(new File(copy, "subfolder1"), "subfolder1_1"), "file2"),
            new File(new File(new File(copy, "subfolder1"), "subfolder1_2"), "file1"), new File(new File(new File(copy, "subfolder1"), "subfolder1_2"), "file2"),
            new File(new File(new File(copy, "subfolder2"), "subfolder2_1"), "file1"), new File(new File(new File(copy, "subfolder2"), "subfolder2_1"), "file2"),
            new File(new File(new File(copy, "subfolder2"), "subfolder2_2"), "file1"), new File(new File(new File(copy, "subfolder2"), "subfolder2_2"), "file2")};

    }
}
