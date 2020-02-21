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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
@ClassForAllEnvironments(section = "remote.svn")
public class DeleteInterceptorTest extends  RemoteVersioningTestBase {

    public DeleteInterceptorTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, DeleteInterceptorTest.class, "fullScanLimitedOnVisibleRoots");
        addTest(suite, DeleteInterceptorTest.class, "deleteFile_FO");
        addTest(suite, DeleteInterceptorTest.class, "deleteFileDO");
        addTest(suite, DeleteInterceptorTest.class, "deleteFolder_FO");
        addTest(suite, DeleteInterceptorTest.class, "deleteFolder_DO");
        return(suite);
    }
    
    public void fullScanLimitedOnVisibleRoots () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy repo = VCSFileProxy.createFileProxy(getWorkTreeDir().getParentFile(), String.valueOf(System.currentTimeMillis()));
        VCSFileProxySupport.mkdir(repo);
        HgCommand.doCreate(repo, NULL_LOGGER);

        VCSFileProxy folderA = VCSFileProxy.createFileProxy(repo, "folderA");
        VCSFileProxy fileA1 = VCSFileProxy.createFileProxy(folderA, "file1");
        VCSFileProxy fileA2 = VCSFileProxy.createFileProxy(folderA, "file2");
        VCSFileProxySupport.mkdirs(folderA);
        VCSFileProxySupport.createNew(fileA1);
        VCSFileProxySupport.createNew(fileA2);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repo, "folderB");
        VCSFileProxy fileB1 = VCSFileProxy.createFileProxy(folderB, "file1");
        VCSFileProxy fileB2 = VCSFileProxy.createFileProxy(folderB, "file2");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxySupport.createNew(fileB1);
        VCSFileProxySupport.createNew(fileB2);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repo, "folderC");
        VCSFileProxy fileC1 = VCSFileProxy.createFileProxy(folderC, "file1");
        VCSFileProxy fileC2 = VCSFileProxy.createFileProxy(folderC, "file2");
        VCSFileProxySupport.mkdirs(folderC);
        VCSFileProxySupport.createNew(fileC1);
        VCSFileProxySupport.createNew(fileC2);

        MercurialInterceptor interceptor = Mercurial.getInstance().getMercurialInterceptor();
        Field f = MercurialInterceptor.class.getDeclaredField("hgFolderEventsHandler");
        f.setAccessible(true);
        Object hgFolderEventsHandler = f.get(interceptor);
        f = hgFolderEventsHandler.getClass().getDeclaredField("seenRoots");
        f.setAccessible(true);
        HashMap<VCSFileProxy, Set<VCSFileProxy>> map = (HashMap) f.get(hgFolderEventsHandler);

        getCache().markAsSeenInUI(folderA);
        // some time for bg threads
        Thread.sleep(3000);
        Set<VCSFileProxy> files = map.get(repo);
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

        VCSFileProxySupport.delete(repo);
    }

    public void deleteFile_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file1");
        commit(folder);
        createFile(folder, "file2");        
        deleteFO(file);
        assertFalse(file.exists());
        assertTrue(folder.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file).getStatus());
    }
    
    public void deleteFileDO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file1");
        commit(folder);
        createFile(folder, "file2");        
        deleteDO(file);
        assertFalse(file.exists());
        assertTrue(folder.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file).getStatus());
    }
    
    public void deleteFolder_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file1 = createFile(folder, "file1");
        VCSFileProxy file2 = createFile(folder, "file2");
        commit(folder);
        
        deleteFO(folder);
        assertFalse(file1.exists());
        assertFalse(file2.exists());
        assertFalse(folder.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file2).getStatus());
    }
    
    public void deleteFolder_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file1 = createFile(folder, "file1");
        VCSFileProxy file2 = createFile(folder, "file2");
        commit(folder);
        
        deleteDO(folder);
        assertFalse(file1.exists());
        assertFalse(file2.exists());
        assertFalse(folder.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file2).getStatus());
    }
}
