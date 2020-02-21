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
package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public class RenameTestCase extends RemoteFileTestBase  {

    public RenameTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    public RenameTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
        
    public void testLocalRename() throws Exception {
        File tmpDir = createTempFile("testLocalRename", "dat", true);
        try {
            FileObject tmpDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(tmpDir));
            assertNotNull(tmpDirFO);
            FileObject oldFO = tmpDirFO.createData("file_1");
            String newName = "file_1_renamed";
            FileLock lock = oldFO.lock();
            oldFO.rename(lock, newName, null);
            lock.releaseLock();
            FileObject newFO = tmpDirFO.getFileObject(newName);
            assertNotNull(newFO);
            assertTrue(newFO == oldFO);
        } finally {
            removeDirectory(tmpDir);
        }
    }

    @ForAllEnvironments
    public void testRemoteRename() throws Exception {
        String tmpDir = null;
        try {
            tmpDir = mkTempAndRefreshParent(true);
            FileObject tmpDirFO = getFileObject(tmpDir);
            FileObject oldFO = tmpDirFO.createData("file_1");
            String newName = "file_1_renamed";
            FileLock lock = oldFO.lock();
            oldFO.rename(lock, newName, null);
            lock.releaseLock();
            FileObject newFO = tmpDirFO.getFileObject(newName);
            assertNotNull(newFO);
            assertTrue(newFO == oldFO);
            assertTrue(newFO.getPath().endsWith(newName));
        } finally {
            removeRemoteDirIfNotNull(tmpDir);
        }
    }

    @ForAllEnvironments
    public void testRemoteRenameWithPlus() throws Exception {
        String tmpDir = null;
        try {
            tmpDir = mkTempAndRefreshParent(true);
            FileObject tmpDirFO = getFileObject(tmpDir);
            FileObject oldFO = tmpDirFO.createData("file+1");
            String newName = "file+1_renamed";
            FileLock lock = oldFO.lock();
            oldFO.rename(lock, newName, null);
            lock.releaseLock();
            FileObject newFO = tmpDirFO.getFileObject(newName);
            assertNotNull(newFO);
            assertTrue(newFO == oldFO);
            assertTrue(newFO.getPath().endsWith(newName));
        } finally {
            removeRemoteDirIfNotNull(tmpDir);
        }
    }

    @ForAllEnvironments
    public void testRemoteRenameDir() throws Exception {
        String tmpDir = null;
        try {
            tmpDir = mkTempAndRefreshParent(true);
            FileObject tmpDirFO = getFileObject(tmpDir);
            FileObject oldFO = tmpDirFO.createFolder("dir_1");
            FileObject child = oldFO.createData("child");
            FileObject subdir = oldFO.createFolder("sub_dir_1");
            FileObject grandchild = subdir.createData("grand_child");
            String newName = "dir_1_renamed";
            FileLock lock = oldFO.lock();
            oldFO.rename(lock, newName, null);
            lock.releaseLock();
            FileObject newFO = tmpDirFO.getFileObject(newName);
            assertNotNull(newFO);
            assertTrue(newFO == oldFO);
            assertTrue(newFO.getPath().endsWith(newName));
            assertTrue(child.getPath().contains("renamed"));
            assertTrue(subdir.getPath().contains("renamed"));
            assertTrue(grandchild.getPath().contains("renamed"));
        } finally {
            removeRemoteDirIfNotNull(tmpDir);
        }
    }

    @ForAllEnvironments
    public void testRemoteRenameDirWithPlus() throws Exception {
        String tmpDir = null;
        try {
            tmpDir = mkTempAndRefreshParent(true);
            FileObject tmpDirFO = getFileObject(tmpDir);
            FileObject oldFO = tmpDirFO.createFolder("dir+1");
            FileObject child = oldFO.createData("child");
            FileObject subdir = oldFO.createFolder("sub_dir_1");
            FileObject grandchild = subdir.createData("grand_child");
            String newName = "dir+1_renamed";
            FileLock lock = oldFO.lock();
            oldFO.rename(lock, newName, null);
            lock.releaseLock();
            FileObject newFO = tmpDirFO.getFileObject(newName);
            assertNotNull(newFO);
            assertTrue(newFO == oldFO);
            assertTrue(newFO.getPath().endsWith(newName));
            assertTrue(child.getPath().contains("renamed"));
            assertTrue(subdir.getPath().contains("renamed"));
            assertTrue(grandchild.getPath().contains("renamed"));
        } finally {
            removeRemoteDirIfNotNull(tmpDir);
        }
    }

    @ForAllEnvironments
    public void testRenameLinkChild() throws Exception {
        String tmpDir = null;
        try {
            tmpDir = mkTempAndRefreshParent(true);
            runScript(
                    "cd " + tmpDir + "; " +
                    "mkdir real_dir; " + 
                    "ln -s real_dir lnk_dir; " +
                    "cd real_dir; " +
                    "touch file_1; " +
                    "touch file_2; " +
                    "");
            FileObject tmpDirFO = getFileObject(tmpDir);
            tmpDirFO.refresh();
            FileObject fo1 = tmpDirFO.getFileObject("lnk_dir/file_1");
            assertNotNull(fo1);
            FileObject fo2 = tmpDirFO.getFileObject("lnk_dir/file_2");
            assertNotNull(fo2);
            final FileLock lock = fo1.lock();
            fo1.move(lock, fo2.getParent(), "file1-renamed", "new-ext");
            lock.releaseLock();
        } finally {
            removeRemoteDirIfNotNull(tmpDir);
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(RenameTestCase.class);
    }
    
}
