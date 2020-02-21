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
package org.netbeans.modules.subversion.remote;

import java.util.Collections;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import static org.netbeans.modules.subversion.remote.RemoteVersioningTestBase.addTest;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
@ClassForAllEnvironments(section = "remote.svn")
public class InterceptorCopyFOTest extends RemoteVersioningTestBase {

    public InterceptorCopyFOTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
    
    // XXX add tests for move/copy of ignored files
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, InterceptorCopyFOTest.class, "copyVersionedFile_FO");
        addTest(suite, InterceptorCopyFOTest.class, "copyUnversionedFile_FO");
        addTest(suite, InterceptorCopyFOTest.class, "copyUnversionedFolder_FO");
        addTest(suite, InterceptorCopyFOTest.class, "copyAddedFile2UnversionedFolder_FO");
        addTest(suite, InterceptorCopyFOTest.class, "copyVersionedFolder2UnversionedFolder_FO");
        addTest(suite, InterceptorCopyFOTest.class, "copyVersionedFile2UnversionedFolder_FO");
        addTest(suite, InterceptorCopyFOTest.class, "copyVersionedFile2IgnoredFolder_FO");
        addTest(suite, InterceptorCopyFOTest.class, "copyAddedFile2VersionedFolder_FO");
        addTest(suite, InterceptorCopyFOTest.class, "copyA2B2C_FO");
        addTest(suite, InterceptorCopyFOTest.class, "copyVersionedFolder_FO");
        addTest(suite, InterceptorCopyFOTest.class, "copyFileTree_FO"); //failed
        addTest(suite, InterceptorCopyFOTest.class, "copyVersionedFile2Repos_FO");
        addTest(suite, InterceptorCopyFOTest.class, "copyVersionedFolder2Repos_FO");
        addTest(suite, InterceptorCopyFOTest.class, "copyFileTree2Repos_FO");
        addTest(suite, InterceptorCopyFOTest.class, "deleteA_copyUnversioned2A_FO");

        return(suite);
    }
    
    public void copyVersionedFile_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(wc);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertTrue(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyUnversionedFile_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fromFile));

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyUnversionedFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());

        // copy
        copyFO(fromFolder, toFolder);

        // test 
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fromFolder));

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyAddedFile2UnversionedFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fromFile).getTextStatus());

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyAddedFile2VersionedFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFodler");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(wc);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "fromFile");
        VCSFileProxySupport.createNew(fromFile);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyA2B2C_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(wc, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        commit(wc);

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // copy
        copyFO(fileA, fileB);
        copyFO(fileB, fileC);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertCachedStatus(fileB, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(fileC, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertTrue(getSVNStatus(fileB).isCopied());
        assertTrue(getSVNStatus(fileC).isCopied());

//        commit(wc);
    }

    public void copyVersionedFile2UnversionedFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(dataRootDir, testName + "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);
        commit(wc);

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NOTMANAGED);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyVersionedFile2IgnoredFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // commit
        commit(fromFile);
        //ignore
        getClient().setIgnoredPatterns(wc, Collections.singletonList(toFolder.getName()));

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        if (version.compareTo(new Version(1,7,0)) >= 0)
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        if (version.compareTo(new Version(1,7,0)) >= 0)
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getStatus(toFile));
        if (version.compareTo(new Version(1,7,0)) >= 0)
        assertFalse(getSVNStatus(toFile).isCopied());
    }

    public void copyVersionedFolder2UnversionedFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdir(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(dataRootDir, testName + "toFolder");

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFolder);
        commit(wc);

        // copy
        copyFO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NOTMANAGED);

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyVersionedFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        commit(wc);

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());

        // copy
        copyFO(fromFolder, toFolder);
        
        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertTrue(getSVNStatus(toFolder).isCopied());
        
//        commit(wc);
        
    }

    public void copyFileTree_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "from");
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

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(wc, "to");
        VCSFileProxySupport.mkdirs(toFolderParent);

        commit(wc);

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());

        // copy
        copyFO(fromFolder, toFolder);

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

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder2).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile11).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile12).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile21).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile22).getTextStatus());

        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder2).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile11).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile12).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile21).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile22).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder1));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder2));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile11));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile12));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile21));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile22));

        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_UPTODATE);
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_UPTODATE);
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile12));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile21));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile22));

        assertTrue(getSVNStatus(toFolder).isCopied());
        assertTrue(getSVNStatus(toFolder1).isCopied());
        assertTrue(getSVNStatus(toFolder2).isCopied());
        assertTrue(getSVNStatus(toFile11).isCopied());
        assertTrue(getSVNStatus(toFile12).isCopied());
        assertTrue(getSVNStatus(toFile21).isCopied());
        assertTrue(getSVNStatus(toFile22).isCopied());

//        commit(wc);
        
    }

    public void copyVersionedFile2Repos_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(wc2);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        commit(wc);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
//        commit(wc2);
    }

    public void copyVersionedFolder2Repos_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(wc2, "folderParent");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        commit(wc2);

        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, toFile.getName());
        VCSFileProxySupport.createNew(fromFile);
        commit(wc);

        // copy
        copyFO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());
        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
//        commit(wc2);
    }

    public void copyFileTree2Repos_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
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

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(wc2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        commit(wc);
        commit(wc2);

        // copy
        copyFO(fromFolder, toFolder);

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

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder2).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile11).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile12).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile21).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile22).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder1).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder2).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile11).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile12).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile21).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile22).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder1));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder2));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile11));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile12));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile21));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile22));
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile11, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile12, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile21, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile22, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(fromFolder).isCopied());
        assertFalse(getSVNStatus(fromFolder1).isCopied());
        assertFalse(getSVNStatus(fromFolder2).isCopied());
        assertFalse(getSVNStatus(fromFile11).isCopied());
        assertFalse(getSVNStatus(fromFile12).isCopied());
        assertFalse(getSVNStatus(fromFile21).isCopied());
        assertFalse(getSVNStatus(fromFile22).isCopied());

//        commit(wc);
//        commit(wc2);

    }

    public void copyA2CB2A_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxySupport.createNew(fileB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(wc, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        commit(wc);

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // copy
        copyFO(fileA, fileC);
        Thread.sleep(500);
        copyFO(fileB, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertTrue(fileB.exists());

        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileC));

        commit(wc);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertTrue(fileB.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileC));

        assertTrue(getSVNStatus(fileB).isCopied());
        assertTrue(getSVNStatus(fileA).isCopied());
    }

    
    public void deleteA_copyUnversioned2A_FO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        final VCSFileProxy folderA = VCSFileProxy.createFileProxy(wc, "folderA");
        VCSFileProxySupport.mkdir(folderA);

        VCSFileProxy fileA = VCSFileProxy.createFileProxy(folderA, "f");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);
        
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdir(folderB);
        VCSFileProxy fileUnversioned = VCSFileProxy.createFileProxy(folderB, "f");
        VCSFileProxySupport.createNew(fileUnversioned);

        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        copyFO(fileUnversioned, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileUnversioned.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
    }
    
}
