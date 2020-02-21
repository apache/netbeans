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
package org.netbeans.modules.git.remote;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import org.netbeans.modules.git.remote.FileInformation.Status;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class MoveLocalInterceptorTest extends AbstractLocalGitTestCase {

    public static final String PROVIDED_EXTENSIONS_REMOTE_LOCATION = "ProvidedExtensions.RemoteLocation";

    public MoveLocalInterceptorTest(String name) {
        super(name);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testMoveFileToIgnoredFolder_DO","testMoveFileToIgnoredFolder_FO").contains(getName());
    }

    @Override
    protected boolean isRunAll() {return false;}

    public void testMoveVersionedFile_DO() throws Exception {
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        add();
        commit();
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // move
        refreshHandler.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        moveDO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());

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
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        add(fromFile);
        commit(fromFile);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        // move
        refreshHandler.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        moveDO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveVersionedFolder2Repos_DO () throws Exception {
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation2, "folderParent");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, toFile.getName());
        VCSFileProxySupport.createNew(fromFile);
        add();
        commit();
        // move
        refreshHandler.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        moveDO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveFileTree2Repos_DO () throws Exception {
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        add(repositoryLocation);
        commit(repositoryLocation);

        // move
        refreshHandler.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        moveDO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
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
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        add(repositoryLocation);
        commit(repositoryLocation);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        // move
        refreshHandler.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        moveFO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveVersionedFolder2Repos_FO() throws Exception {
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation2, "folderParent");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, toFile.getName());
        VCSFileProxySupport.createNew(fromFile);
        add();
        commit();
        // move
        refreshHandler.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        moveFO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveFileTree2Repos_FO() throws Exception {
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        add(repositoryLocation);
        commit(repositoryLocation);

        // move
        refreshHandler.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        moveFO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
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

    public void testMoveUnversionedFile_DO() throws Exception {
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // rename
        refreshHandler.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        moveDO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveUnversionedFolder_DO() throws Exception {
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // move
        refreshHandler.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        moveDO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }
    
    public void testMoveAddedFile2Folder_DO () throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile, toFile)));
        moveDO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveAddedFile2UnversionedFolder_DO () throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile)));
        moveDO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveA2B2A_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(folder);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folder, fileA.getName());

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        moveDO(fileA, fileB);
        moveDO(fileB, fileA);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
    }

    public void testMoveA2CB2A_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        write(fileA, "aaa");
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        write(fileB, "bbb");
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        add();
        commit();

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        moveDO(fileA, fileC);
        moveDO(fileB, fileA);
        assertTrue(refreshHandler.waitForFilesToRefresh());

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
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        write(fileA, "aaa");
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        write(fileB, "bbb");
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        add();
        commit();

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        moveFO(fileA, fileC);
        moveFO(fileB, fileA);
        assertTrue(refreshHandler.waitForFilesToRefresh());

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
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        moveDO(fileA, fileB);
        moveDO(fileB, fileC);
        moveDO(fileC, fileA);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileC).getStatus());

        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        moveDO(fileA, fileB);
        assertTrue(refreshHandler.waitForFilesToRefresh());
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileB, fileC)));
        moveDO(fileB, fileC);
        assertTrue(refreshHandler.waitForFilesToRefresh());
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileC)));
        moveDO(fileC, fileA);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileC).getStatus());
    }

    public void testMoveA2B_CreateA_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        moveDO(fileA, fileB);

        // create from file
        fileA.getParentFile().toFileObject().createData(fileA.getName());
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        FileInformation info = getCache().getStatus(fileB);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
    }

    public void testMoveVersionedFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy file = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(file);
        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, file.getName());

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        moveDO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(file).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertTrue(info.isRenamed());
        assertEquals(file, info.getOldFile());
    }

    public void testMoveFileTree_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation, "to");
        VCSFileProxySupport.mkdirs(toFolderParent);

        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        moveDO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
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

    public void testMoveVersionedFile_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        add();
        commit();
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile, toFile)));
        moveFO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fromFile, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testMoveUnversionedFile_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // rename
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile, toFile)));
        moveFO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveUnversionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        moveFO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveAddedFile2UnversionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile)));
        moveFO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveAddedFile2VersionedFolder_FO() throws Exception {
        // init
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        //commit(repositoryLocation);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // rename
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile, toFile)));
        moveFO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void testMoveA2B2A_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        assertFalse(fileA.exists());
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        assertFalse(folder.exists());
        VCSFileProxySupport.mkdirs(folder);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folder, fileA.getName());
        assertFalse(fileB.exists());

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        moveFO(fileA, fileB);
        moveFO(fileB, fileA);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
    }

    public void testMoveA2B2C_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        moveFO(fileA, fileB);
        moveFO(fileB, fileC);
        assertTrue(refreshHandler.waitForFilesToRefresh());

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

    public void testMoveA2B2C2A_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        moveFO(fileA, fileB);
        moveFO(fileB, fileC);
        moveFO(fileC, fileA);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileC).getStatus());
    }

    public void testMoveA2B_CreateA_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        moveFO(fileA, fileB);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // create from file
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA)));
        fileA.getParentFile().toFileObject().createData(fileA.getName());
        assertTrue(refreshHandler.waitForFilesToRefresh());

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

    public void testMoveVersionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        moveFO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        FileInformation info = getCache().getStatus(toFile);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
        assertEquals(fromFile, info.getOldFile());
        assertTrue(info.isRenamed());
    }

    public void testMoveFileTree_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation, "to");
        VCSFileProxySupport.mkdirs(toFolderParent);

        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        moveFO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
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
        VCSFileProxy ignored = createFolder(repositoryLocation, "ignoredFolder");
        getClient(repositoryLocation).ignore(new VCSFileProxy[] { ignored }, GitUtils.NULL_PROGRESS_MONITOR);
        VCSFileProxy toFolder = createFolder(ignored, "toFolder");
        VCSFileProxy fromFile = createFile(repositoryLocation, "file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
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
        VCSFileProxy ignored = createFolder(repositoryLocation, "ignoredFolder");
        getClient(repositoryLocation).ignore(new VCSFileProxy[] { ignored }, GitUtils.NULL_PROGRESS_MONITOR);
        VCSFileProxy toFolder = createFolder(ignored, "toFolder");
        VCSFileProxy fromFile = createFile(repositoryLocation, "file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
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

}
