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
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class ScheduleRefreshParityTestCase extends RemoteFileTestBase {

    public ScheduleRefreshParityTestCase(String testName) {
        super(testName);
    }
    
    public ScheduleRefreshParityTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }
    
    private void doTestScheduleRefresh2(final FileObject baseDirFO, File log, boolean recursive) throws Throwable {
        PrintStream out = new PrintStream(log);
        final String[] path = new String[] { "build", "debug", "solaris" };
        final String objdir = path[0] + '/' + path[1] + '/' + path[2];
        final boolean local = FileSystemProvider.getExecutionEnvironment(baseDirFO).isLocal();
        try {
            class Worker {
                File baseDirFile;
                public Worker() {
                    if (local) {
                        baseDirFile = FileUtil.toFile(baseDirFO);
                    }
                }
                public void create()  throws Throwable {
                    if (local) {
                        File current = baseDirFile;
                        for (int i = 0; i < path.length; i++) {
                            File child = new File(current, path[i]);
                            current = child;
                        }
                        assertTrue(current.mkdirs());
                        for (int i = 0; i < 5; i++) {
                            File file = new File(current, "file_" + i + ".o");
                            assertTrue(file.createNewFile());
                        }
                    } else {
                        StringBuilder script = new StringBuilder();
                        script.append("cd ").append(baseDirFO.getPath()).append("; ");
                        script.append("mkdir -p ");
                        for (int i = 0; i < path.length; i++) {
                            script.append(path[i]).append("/");
                        }
                        script.append("; ");
                        script.append("cd ").append(objdir).append("; ");
                        for (int i = 0; i < 5; i++) {
                            script.append("touch file_").append(i).append(".o").append("; ");
                        }
                        runScript(script.toString());
                    }
                }
                public void delete()  throws Throwable {
                    if (local) {
                        removeDirectoryContent(new File(baseDirFile, path[0]));
                    } else {
                        removeRemoteDirIfNotNull(baseDirFO.getPath() + '/' + path[0] + '/' + path[1]);
                    }
                }
            }
            
            Worker worker = new Worker();
            worker.create();
            baseDirFO.refresh();
            Set<FileObject> bag = new HashSet<>();
            recurse(baseDirFO, bag);
            
            String prefix = baseDirFO.getPath();
            DumpingFileChangeListener fcl = new DumpingFileChangeListener("baseDir", prefix, out, false);
            if (recursive) {
                FileSystemProvider.addRecursiveListener(fcl, baseDirFO.getFileSystem(), baseDirFO.getPath());
            } else {
                baseDirFO.addFileChangeListener(fcl);
            }
            
            worker.delete();
            
            long time = System.currentTimeMillis();
            FileSystemProvider.scheduleRefresh(baseDirFO);            
            if (baseDirFO instanceof RemoteFileObject) {
                ((RemoteFileObject) baseDirFO).getFileSystem().getRefreshManager().testWaitLastRefreshFinished(time);
            } else {
                Class<?> localProvider = Class.forName("org.netbeans.modules.remote.support.LocalFileSystemProvider");
                if (localProvider != null) {
                    Method method = localProvider.getDeclaredMethod("testWaitLastRefreshFinished");
                    method.invoke(null);
                }
            }            
        } finally {
            out.close();
        }
    }
    
    private void recurse(FileObject fo, Set<FileObject> bag) {
        bag.add(fo);
        for (FileObject child : fo.getChildren()) {
            recurse(child, bag);
        }
    }
            
    @ForAllEnvironments
    public void testScheduleRefresh() throws Throwable {
        doTestScheduleRefresh(true);
    }
    
    public void doTestScheduleRefresh(boolean recursive) throws Throwable {
        String remoteBaseDir = mkTempAndRefreshParent(true);
        File localBaseDir = createTempFile(getClass().getSimpleName(), ".tmp", true);
        try {            
            FileObject remoteBaseDirFO = getFileObject(remoteBaseDir);
            FileObject localBaseDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(localBaseDir));            
            File workDir = getWorkDir();
            File remoteLog = new File(workDir, "remote.dat");
            File localLog = new File(workDir, "local.dat");
            
            doTestScheduleRefresh2(remoteBaseDirFO, remoteLog, recursive);
            doTestScheduleRefresh2(localBaseDirFO, localLog, recursive);

            if (RemoteApiTest.TRACE_LISTENERS) {
                printFile(localLog, "LOCAL ", System.out);
                printFile(remoteLog, "REMOTE", System.out);
            }
            sortFile(localLog);
            sortFile(remoteLog);
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
            if (localBaseDir != null && localBaseDir.exists()) {
                removeDirectory(localBaseDir);
            }
        }    
    }
           
    public static Test suite() {
        return RemoteApiTest.createSuite(ScheduleRefreshParityTestCase.class);
    }
}
