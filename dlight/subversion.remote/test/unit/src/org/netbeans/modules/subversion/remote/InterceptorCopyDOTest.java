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
public class InterceptorCopyDOTest extends RemoteVersioningTestBase {

    public InterceptorCopyDOTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, InterceptorCopyDOTest.class, "copyVersionedFile_DO");
        addTest(suite, InterceptorCopyDOTest.class, "copyUnversionedFile_DO");
        addTest(suite, InterceptorCopyDOTest.class, "copyUnversionedFolder_DO");
        addTest(suite, InterceptorCopyDOTest.class, "copyAddedFile2UnversionedFolder_DO");
        addTest(suite, InterceptorCopyDOTest.class, "copyAddedFile2VersionedFolder_DO");

        addTest(suite, InterceptorCopyDOTest.class, "copyVersionedFile2UnversionedFolder_DO");
        addTest(suite, InterceptorCopyDOTest.class, "copyVersionedFile2IgnoredFolder_DO");
        addTest(suite, InterceptorCopyDOTest.class, "copyVersionedFolder2UnversionedFolder_DO");

        addTest(suite, InterceptorCopyDOTest.class, "copyA2B2C_DO");
        addTest(suite, InterceptorCopyDOTest.class, "copyVersionedFolder_DO");
        addTest(suite, InterceptorCopyDOTest.class, "copyFileTree_DO");
        addTest(suite, InterceptorCopyDOTest.class, "copyVersionedFile2Repos_DO");
        addTest(suite, InterceptorCopyDOTest.class, "copyVersionedFolder2Repos_DO");
        addTest(suite, InterceptorCopyDOTest.class, "copyFileTree2Repos_DO");
        addTest(suite, InterceptorCopyDOTest.class, "deleteA_copyUnversioned2A_DO");

        return(suite);
    }

    public void copyVersionedFile_DO() throws Exception {
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
        copyDO(fromFile, toFile);

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

    public void copyUnversionedFile_DO() throws Exception {
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
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyUnversionedFolder_DO() throws Exception {
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
        copyDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fromFolder));
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyAddedFile2UnversionedFolder_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFodler");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);

        // copy
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyVersionedFile2UnversionedFolder_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy unversionedFolder = VCSFileProxy.createFileProxy(dataRootDir, testName + "_unversioned");
        VCSFileProxySupport.mkdirs(unversionedFolder);
        
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(unversionedFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);
        commit(wc);

        // copy
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, getStatus(toFile));

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyVersionedFile2IgnoredFolder_DO() throws Exception {
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
        copyDO(fromFile, toFile);

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

    public void copyVersionedFolder2UnversionedFolder_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdir(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);

        VCSFileProxy unversionedFolder = VCSFileProxy.createFileProxy(dataRootDir, testName + "_unversioned");
        VCSFileProxySupport.mkdirs(unversionedFolder);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(unversionedFolder, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFolder);
        commit(wc);

        // copy
        copyDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        if (version.compareTo(new Version(1,7,0)) >= 0)
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        if (version.compareTo(new Version(1,7,0)) >= 0)
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, getStatus(toFolder));

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyAddedFile2VersionedFolder_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(wc);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "fromFile");
        VCSFileProxySupport.createNew(fromFile);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);

        // rename
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyA2B2C_DO() throws Exception {
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

        // move
        copyDO(fileA, fileB);
        copyDO(fileB, fileC);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileB));
        assertCachedStatus(fileC, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertTrue(getSVNStatus(fileB).isCopied());
        assertTrue(getSVNStatus(fileC).isCopied());
        
//        commit(wc);
    }

    public void copyVersionedFolder_DO() throws Exception {
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
        copyDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        if (version.compareTo(new Version(1,7,0)) >= 0)
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        // XXX will fail after fixing in fileentry.copy() !!!
        assertFalse(getSVNStatus(toFolder).isCopied());
//        commit(wc);
    }

    public void copyFileTree_DO() throws Exception {
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

        // move
        copyDO(fromFolder, toFolder);

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
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile11).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile12).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile21).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile22).getTextStatus());

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
        assertCachedStatus(toFile11, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile12, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile21, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile22, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        // XXX should be fixed first in fileentry.copy
        //     afterwards assertTrue(...)
        assertFalse(getSVNStatus(toFolder).isCopied());
        assertFalse(getSVNStatus(toFolder1).isCopied());
        assertFalse(getSVNStatus(toFolder2).isCopied());

        assertTrue(getSVNStatus(toFile11).isCopied());
        assertTrue(getSVNStatus(toFile12).isCopied());
        assertTrue(getSVNStatus(toFile21).isCopied());
        assertTrue(getSVNStatus(toFile22).isCopied());

//        commit(wc);
    }

    public void copyVersionedFile2Repos_DO() throws Exception {
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
        copyDO(fromFile, toFile);

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

    public void copyVersionedFolder2Repos_DO() throws Exception {
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
        copyDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists()); // TODO later delete from folder
        assertTrue(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());
        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
//        commit(wc2);
    }

    public void copyFileTree2Repos_DO() throws Exception {
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
        copyDO(fromFolder, toFolder);

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

        commit(wc);
        commit(wc2);

        assertTrue(fromFolder.exists());
        assertTrue(fromFolder1.exists());
        assertTrue(fromFolder2.exists());
        assertTrue(fromFile11.exists());
        assertTrue(fromFile12.exists());
        assertTrue(fromFile21.exists());
        assertTrue(fromFile22.exists());

        assertFalse(getSVNStatus(fromFolder).isCopied());
        assertFalse(getSVNStatus(fromFolder1).isCopied());
        assertFalse(getSVNStatus(fromFolder2).isCopied());
        assertFalse(getSVNStatus(fromFile11).isCopied());
        assertFalse(getSVNStatus(fromFile12).isCopied());
        assertFalse(getSVNStatus(fromFile21).isCopied());
        assertFalse(getSVNStatus(fromFile22).isCopied());

    }

    public void deleteA_copyUnversioned2A_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy folderA = VCSFileProxy.createFileProxy(wc, "folderA");
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
        copyDO(fileUnversioned, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileUnversioned.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
    }
}
