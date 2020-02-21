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
import java.io.IOException;
import java.io.PrintStream;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class ListenersParityTestCase extends RemoteFileTestBase {

    public ListenersParityTestCase(String testName) {
        super(testName);
    }
    
    public ListenersParityTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    private void doTestListenersRename2(FileObject baseDirFO, File log, boolean recursive) throws Exception {
        PrintStream out = new PrintStream(log);
        try {
            String prefix = baseDirFO.getPath();
            DumpingFileChangeListener fcl = new DumpingFileChangeListener("baseDir", prefix, out, true);
            if (recursive) {
                FileSystemProvider.addRecursiveListener(fcl, baseDirFO.getFileSystem(), baseDirFO.getPath());
            } else {
                baseDirFO.addFileChangeListener(fcl);
            }
            FileObject childFO = baseDirFO.createData("child_file_1");
            FileObject subdirFO = baseDirFO.createFolder("child_folder");
            if (!recursive) {
                subdirFO.addFileChangeListener(new DumpingFileChangeListener(subdirFO.getNameExt(), prefix, out, true));
            }
            FileObject grandChildFO = subdirFO.createData("grand_child_file");
            FileObject grandChildDirFO = subdirFO.createFolder("grand_child_dir");
            FileObject grandGrandChildFO = grandChildDirFO.createData("grand_grand_child_file");
            baseDirFO.refresh();
            FileLock lock = grandGrandChildFO.lock();
            try {
                grandGrandChildFO.rename(lock, "grand_grand_child_file_renamed", "txt");
            } finally {
                lock.releaseLock();
            }
            lock = subdirFO.lock();
            try {
                subdirFO.rename(lock, "child_folder_renamed", "dir");
            } finally {
                lock.releaseLock();
            }
            
            // baseDirFO.refresh() will break the test. TODO: investigate.
            baseDirFO.refresh();

        } finally {
            out.close();
        }
    }

    private void doTestListenersDelete2(FileObject baseDirFO, File log, boolean recursive) throws Exception {
        PrintStream out = new PrintStream(log);
        try {
            String prefix = baseDirFO.getPath();
            DumpingFileChangeListener fcl = new DumpingFileChangeListener("baseDir", prefix, out, true);
            if (recursive) {
                FileSystemProvider.addRecursiveListener(fcl, baseDirFO.getFileSystem(), baseDirFO.getPath());
            } else {
                baseDirFO.addFileChangeListener(fcl);
            }
            FileObject childFO = baseDirFO.createData("child_file_1");
            FileObject subdirFO = baseDirFO.createFolder("child_folder");
            if (!recursive) {
                subdirFO.addFileChangeListener(new DumpingFileChangeListener(subdirFO.getNameExt(), prefix, out, true));
            }
            FileObject grandChildFO = subdirFO.createData("grand_child_file");
            FileObject grandChildDirFO = subdirFO.createFolder("grand_child_dir");
            FileObject grandGrandChildFO = grandChildDirFO.createData("grand_grand_child_file");
            baseDirFO.refresh();
            grandGrandChildFO.delete();
            grandChildDirFO.delete();
        } finally {
            out.close();
        }
    }


    private void doTestListenersRename1(boolean recursive) throws Throwable {
        File localTmpDir = createTempFile(getClass().getSimpleName(), ".tmp", true);
        String remoteBaseDir = null;
        try {            
            remoteBaseDir = mkTempAndRefreshParent(true);
            FileObject remoteBaseDirFO = getFileObject(remoteBaseDir);
            FileObject localBaseDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(localTmpDir));            
            File workDir = getWorkDir();
            File remoteLog = new File(workDir, "remote.dat");
            File localLog = new File(workDir, "local.dat");
            doTestListenersRename2(remoteBaseDirFO, remoteLog, recursive);
            doTestListenersRename2(localBaseDirFO, localLog, recursive);
            if (RemoteApiTest.TRACE_LISTENERS) {
                printFile(localLog, "LOCAL ", System.out);
                printFile(remoteLog, "REMOTE", System.out);
            }
            File diff = new File(workDir, "diff.diff");
            try {
                assertFile("Remote and local events differ, see diff " + remoteLog.getAbsolutePath() + " " + localLog.getAbsolutePath(), remoteLog, localLog, diff);
            } catch (Throwable ex) {
                if (diff.exists()) {
                    printFile(diff, null, System.err);
                }
                throw ex;
            }
        } finally {
            removeRemoteDirIfNotNull(remoteBaseDir);
            if (localTmpDir != null && localTmpDir.exists()) {
                removeDirectory(localTmpDir);
            }
        }    
    }

    private void doTestListenersDelete1(boolean recursive) throws Throwable {
        File localTmpDir = createTempFile(getClass().getSimpleName(), ".tmp", true);
        String remoteBaseDir = null;
        try {            
            remoteBaseDir = mkTempAndRefreshParent(true);
            FileObject remoteBaseDirFO = getFileObject(remoteBaseDir);
            FileObject localBaseDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(localTmpDir));            
            File workDir = getWorkDir();
            File remoteLog = new File(workDir, "remote.dat");
            File localLog = new File(workDir, "local.dat");
            doTestListenersDelete2(remoteBaseDirFO, remoteLog, recursive);
            doTestListenersDelete2(localBaseDirFO, localLog, recursive);
            if (RemoteApiTest.TRACE_LISTENERS) {
                printFile(localLog, "LOCAL ", System.out);
                printFile(remoteLog, "REMOTE", System.out);
            }
            File diff = new File(workDir, "diff.diff");
            try {
                assertFile("Remote and local events differ, see diff " + remoteLog.getAbsolutePath() + " " + localLog.getAbsolutePath(), remoteLog, localLog, diff);
            } catch (Throwable ex) {
                if (diff.exists()) {
                    printFile(diff, null, System.err);
                }
                throw ex;
            }
        } finally {
            removeRemoteDirIfNotNull(remoteBaseDir);
            if (localTmpDir != null && localTmpDir.exists()) {
                removeDirectory(localTmpDir);
            }
        }    
    }

    private void doTestListenersChange1(boolean externalChange) throws Throwable {
        File localTmpDir = createTempFile(getClass().getSimpleName(), ".tmp", true);
        String remoteBaseDir = null;
        try {
            remoteBaseDir = mkTempAndRefreshParent(true);
            FileObject remoteBaseDirFO = getFileObject(remoteBaseDir);
            FileObject localBaseDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(localTmpDir));
            File workDir = getWorkDir();
            File remoteLog = new File(workDir, "remote.dat");
            File localLog = new File(workDir, "local.dat");
            doTestListenersChange2(remoteBaseDirFO, remoteLog, externalChange);
            // for an external change I wasn't able to make masterfs to fire file change event;
            // but if external change to remote fs behaves the same way internal change for local fs does - then we are fine.
            doTestListenersChange2(localBaseDirFO, localLog, false);
            if (RemoteApiTest.TRACE_LISTENERS) {
                printFile(localLog, "LOCAL ", System.out);
                printFile(remoteLog, "REMOTE", System.out);
            }
            File diff = new File(workDir, "diff.diff");
            try {
                assertFile("Remote and local events differ, see diff " + remoteLog.getAbsolutePath() + " " + localLog.getAbsolutePath(), remoteLog, localLog, diff);
            } catch (Throwable ex) {
                if (diff.exists()) {
                    printFile(diff, null, System.err);
                }
                throw ex;
            }
        } finally {
            removeRemoteDirIfNotNull(remoteBaseDir);
            if (localTmpDir != null && localTmpDir.exists()) {
                removeDirectory(localTmpDir);
            }
        }
    }

    private void doTestListenersChange2(FileObject baseDirFO, File log, boolean externalChange) throws Exception {
        PrintStream out = new PrintStream(log);
        try {
            String prefix = baseDirFO.getPath();
            FileObject subdirFO = baseDirFO.createFolder("child_folder");
            FileObject childFO = subdirFO.createData("child_file_1");
            subdirFO.addFileChangeListener(new DumpingFileChangeListener("Dir  listener", prefix, out, true));
            childFO.addFileChangeListener(new DumpingFileChangeListener("File listener", prefix, out, true));
            if (externalChange) {
                ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(childFO);
                ProcessUtils.ExitStatus rc = ProcessUtils.execute(env, "/bin/sh", "-c", "echo new_content > " + childFO.getPath());
                assertTrue("external modification command failed", rc.exitCode == 0);
                if (env.isLocal()) {
                    //FileUtil.refreshAll();
                    File[] files = new File[] {FileUtil.toFile(subdirFO), FileUtil.toFile(childFO) };
                    FileUtil.refreshFor(files);
                    sleep(5000);
                }
                subdirFO.refresh();
            } else {
                writeFile(childFO, "new file content\n");
            }
        } finally {
            out.close();
        }
    }


    @ForAllEnvironments
    public void testListenersInternalChange() throws Throwable {
        doTestListenersChange1(false);
    }
 
    @ForAllEnvironments
    public void testListenersExternalChange() throws Throwable {
        doTestListenersChange1(true);
    }

    @ForAllEnvironments
    public void testListenersRename() throws Throwable {                
        doTestListenersRename1(false);
    }

    @ForAllEnvironments
    public void testRecursiveListenersRename() throws Throwable {                
        doTestListenersRename1(true);
    }

    @ForAllEnvironments
    public void testListenersDelete() throws Throwable {                
        doTestListenersDelete1(false);
    }

    @ForAllEnvironments
    public void testRecursiveListenersDelete() throws Throwable {                
        doTestListenersDelete1(true);
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(ListenersParityTestCase.class);
    }
}
