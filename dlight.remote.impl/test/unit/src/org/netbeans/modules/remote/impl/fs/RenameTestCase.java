/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
