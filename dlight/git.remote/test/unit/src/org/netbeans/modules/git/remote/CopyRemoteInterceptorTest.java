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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.git.remote.FileInformation.Status;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
@ClassForAllEnvironments(section = "remote.svn")
public class CopyRemoteInterceptorTest extends AbstractRemoteGitTestCase {

    public static final String PROVIDED_EXTENSIONS_REMOTE_LOCATION = "ProvidedExtensions.RemoteLocation";

    public CopyRemoteInterceptorTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, CopyRemoteInterceptorTest.class, "copyVersionedFile_DO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyUnversionedFile_DO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyUnversionedFolder_DO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyAddedFile2UnversionedFolder_DO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyVersionedFile2UnversionedFolder_DO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyVersionedFolder2UnversionedFolder_DO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyAddedFile2VersionedFolder_DO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyA2B2C_DO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyVersionedFolder_DO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyFileTree_DO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyVersionedFile2Repos_DO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyVersionedFolder2Repos_DO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyFileTree2Repos_DO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyVersionedFile_FO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyUnversionedFile_FO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyUnversionedFolder_FO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyAddedFile2UnversionedFolder_FO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyAddedFile2VersionedFolder_FO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyA2B2C_FO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyVersionedFile2UnversionedFolder_FO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyVersionedFolder2UnversionedFolder_FO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyVersionedFolder_FO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyFileTree_FO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyVersionedFile2Repos_FO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyVersionedFolder2Repos_FO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyFileTree2Repos_FO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyA2CB2A_FO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyFileToIgnoredFolder_DO");
        addTest(suite, CopyRemoteInterceptorTest.class, "copyFileToIgnoredFolder_FO");
        return(suite);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList().contains(testName);
    }

    @Override
    protected boolean isRunAll() {return false;}

    public void copyVersionedFile_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        add();
        commit();
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        refreshHandler.setFilesToRefresh(Collections.singleton(toFile));
        copyDO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());
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

    public void copyUnversionedFile_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        refreshHandler.setFilesToRefresh(Collections.singleton(toFile));
        copyDO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void copyUnversionedFolder_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        refreshHandler.setFilesToRefresh(Collections.singleton(toFolder));
        copyDO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());
if(false)getCache().refreshAllRoots(Collections.singleton(fromFolder));
else    getCache().refreshAllRoots(new HashSet<>(Arrays.asList(fromFolder,toFile)));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void copyAddedFile2UnversionedFolder_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

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

    public void copyVersionedFile2UnversionedFolder_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy unversionedFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), testName + "_unversioned");
        VCSFileProxySupport.mkdirs(unversionedFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(unversionedFolder, fromFile.getName());

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

    public void copyVersionedFolder2UnversionedFolder_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);

        VCSFileProxy unversionedFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), testName + "_unversioned");
        VCSFileProxySupport.mkdirs(unversionedFolder);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(unversionedFolder, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

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

    public void copyAddedFile2VersionedFolder_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(repositoryLocation);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // rename
        refreshHandler.setFilesToRefresh(Collections.singleton(toFile));
        copyDO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void copyA2B2C_DO() throws Exception {
        if (skipTest()) {
            return;
        }
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
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileB, fileC)));
        copyDO(fileA, fileB);
        copyDO(fileB, fileC);
        assertTrue(refreshHandler.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fileA));

        // test
        assertTrue(fileA.exists());
        assertTrue(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
    }

    public void copyVersionedFolder_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(toParent, fromFolder.getName()), fromFile.getName());
        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());

        // copy
        refreshHandler.setFilesToRefresh(Collections.singleton(toFolder));
        copyDO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());
if(false)getCache().refreshAllRoots(Collections.singleton(fromFile));
else    getCache().refreshAllRoots(new HashSet<>(Arrays.asList(fromFile,toFile)));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void copyFileTree_DO() throws Exception {
        if (skipTest()) {
            return;
        }
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
        refreshHandler.setFilesToRefresh(Collections.singleton(toFolder));
        copyDO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFolder));

        // test
        assertTrue(fromFolder.exists());
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

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile22).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());
    }

    public void copyVersionedFile2Repos_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        add();
        commit();
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");

        // copy
        refreshHandler.setFilesToRefresh(Collections.singleton(toFile));
        copyDO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void copyVersionedFolder2Repos_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(repositoryLocation2, "folderParent");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, toFile.getName());
        VCSFileProxySupport.createNew(fromFile);
        add(repositoryLocation);
        commit(repositoryLocation);

        // copy
        refreshHandler.setFilesToRefresh(Collections.singleton(toFolder));
        copyDO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());
if(false)getCache().refreshAllRoots(Collections.singleton(fromFolder));
else     getCache().refreshAllRoots(new HashSet<>(Arrays.asList(fromFolder,toFile)));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void copyFileTree2Repos_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
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

        // copy
        refreshHandler.setFilesToRefresh(Collections.singleton(toFolder));
        copyDO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());
if(false)getCache().refreshAllRoots(Collections.singleton(fromFolder));
else    getCache().refreshAllRoots(new HashSet<>(Arrays.asList(fromFolder,toFolder)));

        // test
        assertTrue(fromFolder.exists());
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

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile22).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());
    }

    public void copyVersionedFile_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        add();
        commit();
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        refreshHandler.setFilesToRefresh(Collections.singleton(toFile));
        copyFO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void copyUnversionedFile_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        refreshHandler.setFilesToRefresh(Collections.singleton(toFile));
        copyFO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void copyUnversionedFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        refreshHandler.setFilesToRefresh(Collections.singleton(toFolder));
        copyFO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());
if(false)getCache().refreshAllRoots(Collections.singleton(fromFile));
else    getCache().refreshAllRoots(new HashSet<>(Arrays.asList(fromFile,toFile)));
        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void copyAddedFile2UnversionedFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

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

    public void copyAddedFile2VersionedFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        add();
        commit();
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        add(fromFile);

        // copy
        refreshHandler.setFilesToRefresh(Collections.singleton(toFile));
        copyFO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void copyA2B2C_FO() throws Exception {
        if (skipTest()) {
            return;
        }
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

        // copy
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileB, fileC)));
        copyFO(fileA, fileB);
        copyFO(fileB, fileC);
        assertTrue(refreshHandler.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fileA));

        // test
        assertTrue(fileA.exists());
        assertTrue(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileC).getStatus());
    }

    public void copyVersionedFile2UnversionedFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), testName + "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

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

    public void copyVersionedFolder2UnversionedFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation.getParentFile(), testName + "toFolder");

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

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

    public void copyVersionedFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
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

        // copy
        refreshHandler.setFilesToRefresh(Collections.singleton(toFolder));
        copyFO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());
if(false)getCache().refreshAllRoots(Collections.singleton(fromFile));
else    getCache().refreshAllRoots(new HashSet<>(Arrays.asList(fromFile,toFile)));
        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void copyFileTree_FO() throws Exception {
        if (skipTest()) {
            return;
        }
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

        // copy
        refreshHandler.setFilesToRefresh(Collections.singleton(toFolder));
        copyFO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());
if(false)getCache().refreshAllRoots(Collections.singleton(fromFolder));
else    getCache().refreshAllRoots(new HashSet<>(Arrays.asList(fromFolder,toFolder)));
        // test
        assertTrue(fromFolder.exists());
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

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile22).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());
    }

    public void copyVersionedFile2Repos_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy repositoryLocation2 = initSecondRepository();
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        add();
        commit();
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");

        // copy
        refreshHandler.setFilesToRefresh(Collections.singleton(toFile));
        copyFO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fromFile));

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void copyVersionedFolder2Repos_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(initSecondRepository(), "folderParent");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");

        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, toFile.getName());
        VCSFileProxySupport.createNew(fromFile);
        add();
        commit();

        // copy
        refreshHandler.setFilesToRefresh(Collections.singleton(toFolder));
        copyFO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());
if(false)getCache().refreshAllRoots(Collections.singleton(fromFolder));
else    getCache().refreshAllRoots(new HashSet<>(Arrays.asList(fromFolder,toFile)));

        // test
        assertTrue(fromFolder.exists());
        assertTrue(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void copyFileTree2Repos_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
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

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(initSecondRepository(), "toFolder");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        add();
        commit();

        // copy
        refreshHandler.setFilesToRefresh(Collections.singleton(toFolder));
        copyFO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());
if(false)getCache().refreshAllRoots(Collections.singleton(fromFolder));
else    getCache().refreshAllRoots(new HashSet<>(Arrays.asList(fromFolder,toFolder)));

        // test
        assertTrue(fromFolder.exists());
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

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile11).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile12).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile21).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile22).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile11).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile12).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile21).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile22).getStatus());
    }

    public void copyA2CB2A_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxySupport.createNew(fileB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        add();
        commit();

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // copy
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileC, fileA)));
        copyFO(fileA, fileC);
        copyFO(fileB, fileA);
        assertTrue(refreshHandler.waitForFilesToRefresh());
        getCache().refreshAllRoots(Collections.singleton(fileB));

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertTrue(fileB.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileC).getStatus());
    }

    public void copyFileToIgnoredFolder_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy ignored = createFolder(repositoryLocation, "ignoredFolder");
        getClient(repositoryLocation).ignore(new VCSFileProxy[] { ignored }, GitUtils.NULL_PROGRESS_MONITOR);
        VCSFileProxy toFolder = createFolder(ignored, "toFolder");
        VCSFileProxy fromFile = createFile(repositoryLocation, "file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
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

    public void copyFileToIgnoredFolder_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy ignored = createFolder(repositoryLocation, "ignoredFolder");
        getClient(repositoryLocation).ignore(new VCSFileProxy[] { ignored }, GitUtils.NULL_PROGRESS_MONITOR);
        VCSFileProxy toFolder = createFolder(ignored, "toFolder");
        VCSFileProxy fromFile = createFile(repositoryLocation, "file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
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
}
