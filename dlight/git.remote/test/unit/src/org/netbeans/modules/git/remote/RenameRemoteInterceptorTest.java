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
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.git.remote.FileInformation.Status;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
@ClassForAllEnvironments(section = "remote.svn")
public class RenameRemoteInterceptorTest extends AbstractRemoteGitTestCase {

    public static final String PROVIDED_EXTENSIONS_REMOTE_LOCATION = "ProvidedExtensions.RemoteLocation";

    public RenameRemoteInterceptorTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, RenameRemoteInterceptorTest.class, "renameVersionedFile_DO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameUnversionedFile_DO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameUnversionedFolder_DO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameFileChangeCase_DO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameFileChangeCase_FO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameFolderChangeCase_DO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameFolderChangeCase_FO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameAddedFile_DO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameA2B2A_DO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameA2B2C_DO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameA2B2C2A_DO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameA2CB2A_DO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameA2CB2A_FO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameA2B_CreateA_DO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameVersionedFolder_DO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameFileTree_DO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameVersionedFile_FO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameUnversionedFile_FO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameUnversionedFolder_FO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameAddedFile_FO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameA2B2A_FO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameA2B2C_FO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameA2B2C2A_FO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameA2B_CreateA_FO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameVersionedFolder_FO");
        addTest(suite, RenameRemoteInterceptorTest.class, "renameFileTree_FO");
        return(suite);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList().contains(testName);
    }

    @Override
    protected boolean isRunAll() {return false;}

    public void renameVersionedFile_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        add();
        commit();
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(repositoryLocation, "toFile");

        // rename
        refreshHandler.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        renameDO(fromFile, toFile);
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

    public void renameUnversionedFile_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(repositoryLocation, "toFile");

        // rename
        refreshHandler.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        renameDO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void renameUnversionedFolder_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "fromFolder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // rename
        refreshHandler.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        renameDO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void renameFileChangeCase_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy fromFile = createFile(repositoryLocation, "file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(repositoryLocation, "FILE");
        add(fromFile);
        commit(fromFile);
        
        // move
        refreshHandler.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        renameDO(fromFile, toFile);
        
        // test
        if (VCSFileProxySupport.isMac(repositoryLocation)) {
            assertTrue(Arrays.asList(toFile.getParentFile().listFiles()).contains(toFile.getName()));
            assertFalse(Arrays.asList(fromFile.getParentFile().listFiles()).contains(fromFile.getName()));
        } else {
            assertTrue(refreshHandler.waitForFilesToRefresh());
            assertFalse(fromFile.exists());
            assertTrue(toFile.exists());
            assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
            assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
        }
    }
    
    public void renameFileChangeCase_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy fromFile = createFile(repositoryLocation, "file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(repositoryLocation, "FILE");
        add(fromFile);
        commit(fromFile);
        
        // move
        refreshHandler.setFilesToRefresh(new HashSet(Arrays.asList(fromFile, toFile)));
        renameFO(fromFile, toFile);
        
        // test
        if (VCSFileProxySupport.isMac(repositoryLocation)) {
            assertTrue(Arrays.asList(toFile.getParentFile().listFiles()).contains(toFile.getName()));
            assertFalse(Arrays.asList(fromFile.getParentFile().listFiles()).contains(fromFile.getName()));
        } else {
            assertTrue(refreshHandler.waitForFilesToRefresh());
            assertFalse(fromFile.exists());
            assertTrue(toFile.exists());
            assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
            assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
        }
    }
    
    public void renameFolderChangeCase_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy fromFolder = createFolder(repositoryLocation, "folder");
        VCSFileProxy fromFile = createFile(fromFolder, "file");
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "FOLDER");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        add(fromFolder);
        commit(fromFolder);
        
        // move
        refreshHandler.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        renameDO(fromFolder, toFolder);
        
        // test
        if (VCSFileProxySupport.isMac(repositoryLocation)) {
            assertTrue(Arrays.asList(toFolder.getParentFile().listFiles()).contains(toFolder.getName()));
            assertFalse(Arrays.asList(fromFolder.getParentFile().listFiles()).contains(fromFolder.getName()));
        } else {
            assertTrue(refreshHandler.waitForFilesToRefresh());
            assertFalse(fromFolder.exists());
            assertTrue(toFolder.exists());
            assertTrue(toFile.exists());
            assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
            assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
        }
    }
    
    public void renameFolderChangeCase_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy fromFolder = createFolder(repositoryLocation, "folder");
        VCSFileProxy fromFile = createFile(fromFolder, "file");
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "FOLDER");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        add(fromFolder);
        commit(fromFolder);
        
        // move
        refreshHandler.setFilesToRefresh(new HashSet(Arrays.asList(fromFolder, toFolder)));
        renameFO(fromFolder, toFolder);
        
        // test
        if (VCSFileProxySupport.isMac(repositoryLocation)) {
            assertTrue(Arrays.asList(toFolder.getParentFile().listFiles()).contains(toFolder.getName()));
            assertFalse(Arrays.asList(fromFolder.getParentFile().listFiles()).contains(fromFolder.getName()));
        } else {
            assertTrue(refreshHandler.waitForFilesToRefresh());
            assertFalse(fromFolder.exists());
            assertTrue(toFolder.exists());
            assertTrue(toFile.exists());
            assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fromFile).getStatus());
            assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
        }
    }

    public void renameAddedFile_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(repositoryLocation, "toFile");

        // add
        add(fromFile);

        // rename
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile, toFile)));
        renameDO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void renameA2B2A_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "to");

        // rename
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        renameDO(fileA, fileB);
        renameDO(fileB, fileA);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
    }

    public void renameA2B2C_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(repositoryLocation, "C");

        // rename
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        renameDO(fileA, fileB);
        renameDO(fileB, fileC);
        assertTrue(refreshHandler.waitForFilesToRefresh());

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

    public void renameA2B2C2A_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(repositoryLocation, "C");

        // rename
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        renameDO(fileA, fileB);
        renameDO(fileB, fileC);
        renameDO(fileC, fileA);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileC).getStatus());
    }

    public void renameA2CB2A_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        write(fileA, "aaa");
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        write(fileB, "bbb");
        add();
        commit();

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(repositoryLocation, "C");

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        renameDO(fileA, fileC);
        renameDO(fileB, fileA);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.MODIFIED_HEAD_INDEX, Status.MODIFIED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileC).getStatus());
    }

    public void renameA2CB2A_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        write(fileA, "aaa");
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        write(fileB, "bbb");
        add();
        commit();

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(repositoryLocation, "C");

        // move
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        renameFO(fileA, fileC);
        renameFO(fileB, fileA);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.MODIFIED_HEAD_INDEX, Status.MODIFIED_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.REMOVED_HEAD_INDEX, Status.REMOVED_HEAD_WORKING_TREE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileC).getStatus());
    }

    public void renameA2B_CreateA_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();

        // rename
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        renameDO(fileA, fileB);
        // create from file
        fileA.getParentFile().toFileObject().createData(fileA.getName());
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());
if(false);
else    getCache().refreshAllRoots(fileA);

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        FileInformation info = getCache().getStatus(fileB);
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), info.getStatus());
    }

    public void renameVersionedFolder_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy file = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(file);
        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "to");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, file.getName());

        // rename
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        renameDO(fromFolder, toFolder);
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

    public void renameFileTree_DO() throws Exception {
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
        add();
        commit();

        // rename
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "to");
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        renameDO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, "folder1");
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, "folder2");
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

    public void renameVersionedFile_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        add();
        commit();
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(repositoryLocation, "toFile");

        // rename
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile, toFile)));
        renameFO(fromFile, toFile);
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

    public void renameUnversionedFile_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(repositoryLocation, "toFile");

        // rename
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile, toFile)));
        renameFO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void renameUnversionedFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "fromFolder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "toFolder");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // rename
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        renameFO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void renameAddedFile_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(repositoryLocation, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(repositoryLocation, "toFile");

        // add
        add(fromFile);

        // rename
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFile, toFile)));
        renameFO(fromFile, toFile);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fromFile).getStatus());
        assertEquals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(toFile).getStatus());
    }

    public void renameA2B2A_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.createNew(fileA);
        commit(repositoryLocation);

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "to");

        // rename
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        renameFO(fileA, fileB);
        renameFO(fileB, fileA);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());

        assertEquals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
    }

    public void renameA2B2C_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(repositoryLocation, "C");

        // rename
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        renameFO(fileA, fileB);
        renameFO(fileB, fileC);
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

    public void renameA2B2C2A_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(repositoryLocation, "C");

        // rename
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB, fileC)));
        renameFO(fileA, fileB);
        renameFO(fileB, fileC);
        renameFO(fileC, fileA);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());

        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileA).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileB).getStatus());
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(fileC).getStatus());
    }

    public void renameA2B_CreateA_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(repositoryLocation, "A");
        VCSFileProxySupport.createNew(fileA);
        add();
        commit();

        // rename
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(repositoryLocation, "B");
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fileA, fileB)));
        renameFO(fileA, fileB);
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

    public void renameVersionedFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(repositoryLocation, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        add();
        commit();

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "to");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // rename
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        renameFO(fromFolder, toFolder);
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

    public void renameFileTree_FO() throws Exception {
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
        add();
        commit();

        // rename
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(repositoryLocation, "to");
        refreshHandler.setFilesToRefresh(new HashSet<>(Arrays.asList(fromFolder, toFolder)));
        renameFO(fromFolder, toFolder);
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, "folder1");
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, "folder2");
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

}
