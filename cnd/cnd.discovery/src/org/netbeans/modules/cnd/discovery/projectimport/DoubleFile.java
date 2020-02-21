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
package org.netbeans.modules.cnd.discovery.projectimport;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public final class DoubleFile {
    private final File localFile;
    private final String remotePath;
    private final ExecutionEnvironment remoteEE;
    private boolean validLocal = true;
    private boolean validRemote = true;

    private DoubleFile(File localPath, String remotePath, ExecutionEnvironment remoteEE) {
        this.localFile = localPath;
        this.remotePath = remotePath;
        this.remoteEE = remoteEE;
    }

    public static DoubleFile createFile(String prefix, FSPath path) {
        ExecutionEnvironment ee = FileSystemProvider.getExecutionEnvironment(path.getFileSystem());
        if (ee.isLocal()) {
            return new DoubleFile(new File(path.getPath()), null, ee);
        } else {
            File local;
            try {
                local = File.createTempFile(prefix, ".log"); // NOI18N
                local.deleteOnExit();
            } catch (IOException ex) {
                return null;
            }
            return new DoubleFile(local, path.getPath(), ee);
        }
    }

    public static DoubleFile createFile(File path, ExecutionEnvironment ee) {
        File local = path;
        local.deleteOnExit();
        String remotePath = null;
        if (ee.isRemote()) {
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(ee);
                if (ee.isRemote()) {
                    remotePath = hostInfo.getTempDir() + "/" + local.getName(); // NOI18N
                }
            } catch (IOException | ConnectionManager.CancellationException ex) {
            }
        }
        return new DoubleFile(local, remotePath, ee);
    }

    public static DoubleFile createTmpFile(String prefix, ExecutionEnvironment ee) {
        File local;
        try {
            local = File.createTempFile(prefix, ".log"); // NOI18N
            local.deleteOnExit();
        } catch (IOException ex) {
            return null;
        }
        local.deleteOnExit();
        String remotePath = null;
        if (ee.isRemote()) {
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(ee);
                if (ee.isRemote()) {
                    remotePath = hostInfo.getTempDir() + "/" + local.getName(); // NOI18N
                }
            } catch (IOException | ConnectionManager.CancellationException ex) {
            }
        }
        return new DoubleFile(local, remotePath, ee);
    }

    public boolean isValidRemote() {
        return validRemote;
    }

    public FileObject getLocalFileObject() {
        return FileUtil.toFileObject(localFile);
    }

    public File getLocalFile() {
        return localFile;
    }

    public boolean existLocalFile() {
        return localFile.exists() && validLocal;
    }

    public String getLocalPath() {
        return localFile.getAbsolutePath();
    }

    public String getRemotePath() {
        return remotePath;
    }

    void upload() {
        try {
            Future<CommonTasksSupport.UploadStatus> task = CommonTasksSupport.uploadFile(localFile, remoteEE, remotePath, 0555);
            if (ImportProject.TRACE) {
                ImportProject.logger.log(Level.INFO, "#upload file {0}->{1}", new Object[]{localFile.getAbsolutePath(), remotePath}); // NOI18N
            }
            /*int rc =*/
            task.get();
        } catch (Throwable ex) {
            ImportProject.logger.log(Level.INFO, "Cannot upload file {0}->{1}. Exception {2}", new Object[]{localFile.getAbsolutePath(), remotePath, ex.getMessage()}); // NOI18N
            validRemote = false;
        }
    }

    void download() {
        try {
            if (HostInfoUtils.fileExists(remoteEE, remotePath)) {
                Future<Integer> task = CommonTasksSupport.downloadFile(remotePath, remoteEE, localFile.getAbsolutePath(), null);
                if (ImportProject.TRACE) {
                    ImportProject.logger.log(Level.INFO, "#download file {0}->{1}", new Object[]{remotePath, localFile.getAbsolutePath()}); // NOI18N
                }
                /*int rc =*/
                task.get();
            }
        } catch (Throwable ex) {
            ImportProject.logger.log(Level.INFO, "Cannot download file {0}->{1}. Exception {2}", new Object[]{remotePath, localFile.getAbsolutePath(), ex.getMessage()}); // NOI18N
            validLocal = false;
        }
    }
}
