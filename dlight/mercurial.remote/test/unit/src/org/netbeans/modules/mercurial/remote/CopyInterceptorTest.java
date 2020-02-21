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

package org.netbeans.modules.mercurial.remote;

import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
@ClassForAllEnvironments(section = "remote.svn")
public class CopyInterceptorTest extends  RemoteVersioningTestBase {

    public CopyInterceptorTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, CopyInterceptorTest.class, "copyFile_SingleRepo_FO");
        addTest(suite, CopyInterceptorTest.class, "copyFile_SingleRepo_DO");
        addTest(suite, CopyInterceptorTest.class, "copyUnversionedFile_SingleRepo_FO");
        addTest(suite, CopyInterceptorTest.class, "copyUnversionedFile_SingleRepo_DO");
        addTest(suite, CopyInterceptorTest.class, "copyFolder_SingleRepo_FO");
        addTest(suite, CopyInterceptorTest.class, "copyFolder_SingleRepo_DO");
        addTest(suite, CopyInterceptorTest.class, "copyUnversionedFolder_SingleRepo_FO");
        addTest(suite, CopyInterceptorTest.class, "copyUnversionedFolder_SingleRepo_DO");
        addTest(suite, CopyInterceptorTest.class, "copyTree_SingleRepo_FO");
        addTest(suite, CopyInterceptorTest.class, "copyTree_SingleRepo_DO");
        addTest(suite, CopyInterceptorTest.class, "copyUnversionedTree_SingleRepo_FO");
        addTest(suite, CopyInterceptorTest.class, "copyUnversionedTree_SingleRepo_DO");
        addTest(suite, CopyInterceptorTest.class, "copyTree_TwoRepos_FO");
        addTest(suite, CopyInterceptorTest.class, "copyTree_TwoRepos_DO");
        addTest(suite, CopyInterceptorTest.class, "copyTree_UnversionedTarget_FO");
        addTest(suite, CopyInterceptorTest.class, "copyTree_UnversionedTarget_DO");
        addTest(suite, CopyInterceptorTest.class, "copyFileToIgnoredFolder_DO");
        addTest(suite, CopyInterceptorTest.class, "copyFileToIgnoredFolder_FO");
        return(suite);
    }
    
    public void copyFile_SingleRepo_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(folder, "copy");

        commit(folder);
        copyFO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file).getStatus());
        FileInformation st = getCachedStatus(copy, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());

        VCSFileProxy target = createFolder("target");
        copy =VCSFileProxy.createFileProxy(target, file.getName());
        copyFO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file).getStatus());
        st = getCachedStatus(copy, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());
    }

    public void copyFile_SingleRepo_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file");
        VCSFileProxy targetFolder = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(targetFolder, file.getName());

        commit(folder);
        copyDO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(file).getStatus());
        FileInformation st = getCachedStatus(copy, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(st.getStatus(null).isCopied());
    }

    public void copyUnversionedFile_SingleRepo_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy file = createFile(folder, "file");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(folder, "copy");

        copyFO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file).getStatus());
        FileInformation st = getCachedStatus(copy, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));

        VCSFileProxy target = createFolder("target");
        copy = VCSFileProxy.createFileProxy(target, file.getName());
        copyFO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file).getStatus());
        st = getCachedStatus(copy, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));
    }

    public void copyUnversionedFile_SingleRepo_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy file = createFile(folder, "file");
        VCSFileProxy targetFolder = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(targetFolder, file.getName());

        copyDO(file, copy);
        assertTrue(file.exists());
        assertTrue(copy.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCache().refresh(file).getStatus());
        FileInformation st = getCachedStatus(copy, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertNull(st.getStatus(null));
    }

    public void copyFolder_SingleRepo_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file1 = createFile(folder, "file1");
        VCSFileProxy file2 = createFile(folder, "file2");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(folder.getParentFile(), "copy");
        VCSFileProxy copiedFile1 = VCSFileProxy.createFileProxy(copy, file1.getName());
        VCSFileProxy copiedFile2 = VCSFileProxy.createFileProxy(copy, file2.getName());

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


        VCSFileProxy target = createFolder("target");
        copy = VCSFileProxy.createFileProxy(target, folder.getName());
        copiedFile1 = VCSFileProxy.createFileProxy(copy, file1.getName());
        copiedFile2 = VCSFileProxy.createFileProxy(copy, file2.getName());
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

    public void copyFolder_SingleRepo_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file1 = createFile(folder, "file1");
        VCSFileProxy file2 = createFile(folder, "file2");
        VCSFileProxy target = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
        VCSFileProxy copiedFile1 = VCSFileProxy.createFileProxy(copy, file1.getName());
        VCSFileProxy copiedFile2 = VCSFileProxy.createFileProxy(copy, file2.getName());

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

    public void copyUnversionedFolder_SingleRepo_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy file1 = createFile(folder, "file1");
        VCSFileProxy file2 = createFile(folder, "file2");
        VCSFileProxy target = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
        VCSFileProxy copiedFile1 = VCSFileProxy.createFileProxy(copy, file1.getName());
        VCSFileProxy copiedFile2 = VCSFileProxy.createFileProxy(copy, file2.getName());

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

    public void copyUnversionedFolder_SingleRepo_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy file1 = createFile(folder, "file1");
        VCSFileProxy file2 = createFile(folder, "file2");
        VCSFileProxy target = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
        VCSFileProxy copiedFile1 = VCSFileProxy.createFileProxy(copy, file1.getName());
        VCSFileProxy copiedFile2 = VCSFileProxy.createFileProxy(copy, file2.getName());

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

    public void copyTree_SingleRepo_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy target = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, "copy");
        VCSFileProxy[] copies = prepareTree(folder, copy);

        commit(folder);

        copyFO(folder, copy);
        for (VCSFileProxy f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
            assertTrue(st.getStatus(null).isCopied());
        }
    }

    public void copyTree_SingleRepo_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy target = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
        VCSFileProxy[] copies = prepareTree(folder, copy);

        commit(folder);

        copyDO(folder, copy);
        for (VCSFileProxy f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
            assertTrue(st.getStatus(null).isCopied());
        }
    }

    public void copyUnversionedTree_SingleRepo_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy target = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
        VCSFileProxy[] copies = prepareTree(folder, copy);

        copyFO(folder, copy);
        for (VCSFileProxy f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
            assertNull(st.getStatus(null));
        }
    }

    public void copyUnversionedTree_SingleRepo_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy target = createFolder("target");
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
        VCSFileProxy[] copies = prepareTree(folder, copy);

        copyDO(folder, copy);
        for (VCSFileProxy f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
            assertNull(st.getStatus(null));
        }
    }

    public void copyTree_TwoRepos_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy target = createFolder("target");
        HgCommand.doCreate(target, NULL_LOGGER);
        Mercurial.getInstance().versionedFilesChanged();
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
        VCSFileProxy[] copies = prepareTree(folder, copy);

        copyFO(folder, copy);
        for (VCSFileProxy f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
            assertNull(st.getStatus(null));
        }
    }

    public void copyTree_TwoRepos_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy target = createFolder("target");
        HgCommand.doCreate(target, NULL_LOGGER);
        Mercurial.getInstance().versionedFilesChanged();
        VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
        VCSFileProxy[] copies = prepareTree(folder, copy);

        copyDO(folder, copy);
        for (VCSFileProxy f : copies) {
            assertTrue(f.exists());
            FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
            assertNull(st.getStatus(null));
        }
    }

    public void copyTree_UnversionedTarget_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy target = VCSFileProxy.createFileProxy(getWorkTreeDir().getParentFile(), "mercurialtest_target_" + testName + "_" + System.currentTimeMillis());
        VCSFileProxySupport.mkdirs(target);
        try {
            VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
            VCSFileProxy[] copies = prepareTree(folder, copy);

            copyFO(folder, copy);
            Mercurial.getInstance().versionedFilesChanged();
            for (VCSFileProxy f : copies) {
                assertTrue(f.exists());
                FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NOTMANAGED);
                assertNull(st.getStatus(null));
                assertNull(Mercurial.getInstance().getRepositoryRoot(f));
            }
        } finally {
            // cleanup, temp folder is outside workdir
            VCSFileProxySupport.delete(target);
        }
    }

    public void copyTree_UnversionedTarget_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        commit(folder);
        VCSFileProxy target = VCSFileProxy.createFileProxy(getWorkTreeDir().getParentFile(), "mercurialtest_target_" + testName + "_" + System.currentTimeMillis());
        VCSFileProxySupport.mkdirs(target);
        try {
            VCSFileProxy copy = VCSFileProxy.createFileProxy(target, folder.getName());
            VCSFileProxy[] copies = prepareTree(folder, copy);

            copyDO(folder, copy);
            Mercurial.getInstance().versionedFilesChanged();
            for (VCSFileProxy f : copies) {
                assertTrue(f.exists());
                FileInformation st = getCachedStatus(f, FileInformation.STATUS_NOTVERSIONED_NOTMANAGED);
                assertNull(st.getStatus(null));
                assertNull(Mercurial.getInstance().getRepositoryRoot(f));
            }
        } finally {
            // cleanup, temp folder is outside workdir
            VCSFileProxySupport.delete(target);
        }
    }
    
    public void copyFileToIgnoredFolder_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy folder = createFolder("ignoredFolder");
        HgUtils.addIgnored(folder.getParentFile(), new VCSFileProxy[] { folder });
        VCSFileProxy toFolder = createFolder(folder, "toFolder");
        VCSFileProxy fromFile = createFile("file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        commit(fromFile);
        
        // move
        copyDO(fromFile, toFile);
        
        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(fromFile).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getCache().refresh(toFile).getStatus());
    }
    
    public void copyFileToIgnoredFolder_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy folder = createFolder("ignoredFolder");
        HgUtils.addIgnored(folder.getParentFile(), new VCSFileProxy[] { folder });
        VCSFileProxy toFolder = createFolder(folder, "toFolder");
        VCSFileProxy fromFile = createFile("file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        commit(fromFile);
        
        // move
        copyFO(fromFile, toFile);
        
        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCache().refresh(fromFile).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getCache().refresh(toFile).getStatus());
    }
    
}
