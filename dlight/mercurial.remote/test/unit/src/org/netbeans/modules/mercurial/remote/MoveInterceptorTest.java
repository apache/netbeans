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
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.Utilities;

/**
 *
 * 
 */
@ClassForAllEnvironments(section = "remote.svn")
public class MoveInterceptorTest extends  RemoteVersioningTestBase {

    public MoveInterceptorTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, MoveInterceptorTest.class, "moveFileToIgnoredFolder_DO");
        addTest(suite, MoveInterceptorTest.class, "moveFileToIgnoredFolder_FO");
        addTest(suite, MoveInterceptorTest.class, "renameFileChangeCase_DO");
        addTest(suite, MoveInterceptorTest.class, "renameFileChangeCase_FO");
        addTest(suite, MoveInterceptorTest.class, "renameFolderChangeCase_DO");
        addTest(suite, MoveInterceptorTest.class, "renameFolderChangeCase_FO");
        return(suite);
    }
    
    public void moveFileToIgnoredFolder_DO () throws Exception {
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
        moveDO(fromFile, toFile);
        
        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getCache().refresh(toFile).getStatus());
    }
    
    public void moveFileToIgnoredFolder_FO () throws Exception {
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
        moveFO(fromFile, toFile);
        
        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getCache().refresh(toFile).getStatus());
    }
    
    public void renameFileChangeCase_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy fromFile = createFile("file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(getWorkTreeDir(), "FILE");
        commit(fromFile);
        
        // move
        renameDO(fromFile, toFile.getName());
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(isParentHasChild(toFile));
            assertFalse(isParentHasChild(fromFile));
        } else {
            assertFalse(fromFile.exists());
            assertTrue(toFile.exists());
            assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
            assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCache().refresh(toFile).getStatus());
        }
    }
    
    public void renameFileChangeCase_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy fromFile = createFile("file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(getWorkTreeDir(), "FILE");
        commit(fromFile);
        
        // move
        renameFO(fromFile, toFile.getName());
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(isParentHasChild(toFile));
            assertFalse(isParentHasChild(fromFile));
        } else {
            assertFalse(fromFile.exists());
            assertTrue(toFile.exists());
            assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
            assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCache().refresh(toFile).getStatus());
        }
    }
    
    public void renameFolderChangeCase_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy fromFolder = createFolder("folder");
        VCSFileProxy fromFile = createFile(fromFolder, "file");
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(getWorkTreeDir(), "FOLDER");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        commit(fromFolder);
        
        // move
        renameDO(fromFolder, toFolder.getName());
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(isParentHasChild(toFolder));
            assertFalse(isParentHasChild(fromFolder));
        } else {
            assertFalse(fromFolder.exists());
            assertTrue(toFolder.exists());
            assertTrue(toFile.exists());
            assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
            assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCache().refresh(toFile).getStatus());
        }
    }
    
    public void renameFolderChangeCase_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy fromFolder = createFolder("folder");
        VCSFileProxy fromFile = createFile(fromFolder, "file");
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(getWorkTreeDir(), "FOLDER");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        commit(fromFolder);
        
        // move
        renameFO(fromFolder, toFolder.getName());
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(isParentHasChild(toFolder));
            assertFalse(isParentHasChild(fromFolder));
        } else {
            assertFalse(fromFolder.exists());
            assertTrue(toFolder.exists());
            assertTrue(toFile.exists());
            assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
            assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCache().refresh(toFile).getStatus());
        }
    }
}
