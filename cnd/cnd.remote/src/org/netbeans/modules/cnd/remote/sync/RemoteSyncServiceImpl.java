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

package org.netbeans.modules.cnd.remote.sync;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.remote.support.RemoteProjectSupport;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=RemoteSyncService.class)
public class RemoteSyncServiceImpl implements RemoteSyncService {

    private static class Uploader implements RemoteSyncSupport.Worker {

        private final ExecutionEnvironment execEnv;
        private final PathMap pathMap;
        private final FileData fileData;

        private final Set<String> checkedDirs = new HashSet<>();
        private UploadStatus uploadStatus;

        public Uploader(Lookup.Provider project, ExecutionEnvironment execEnv) throws IOException {
            this.execEnv = execEnv;
            pathMap = HostInfoProvider.getMapper(this.execEnv);
            if (project == null) {
                fileData = null;
            } else {
                FileObject privProjectStorageDir = RemoteProjectSupport.getPrivateStorage(project);
                fileData = FileData.get(privProjectStorageDir, execEnv);
            }
        }


        @Override
        public void process(File file, Writer err) throws RemoteSyncSupport.PathMapperException, InterruptedException, ExecutionException, IOException {
            String remotePath = pathMap.getRemotePath(file.getAbsolutePath(), false);
            if (remotePath == null) {
                throw new RemoteSyncSupport.PathMapperException(file);
            }
            checkDir(remotePath);
            Future<UploadStatus> task = CommonTasksSupport.uploadFile(file.getAbsolutePath(), execEnv, remotePath, 0700);
            uploadStatus = task.get();
            if (uploadStatus.isOK()) {
                if (fileData != null) {
                    fileData.setState(file, FileState.COPIED);
                }
            } else {
                if (fileData != null) {
                    fileData.setState(file, FileState.ERROR);
                }
                if (err != null) {
                    err.append(uploadStatus.getError());
                }
                throw new IOException("RC=" + uploadStatus.getExitCode()); //NOI18N
            }
        }

        @Override
        public void close() {
            if (fileData != null) {
                fileData.store();
            }
        }

        private void checkDir(String remoteFilePath) throws InterruptedException, ExecutionException {
            int slashPos = remoteFilePath.lastIndexOf('/'); //NOI18N
            if (slashPos >= 0) {
                String remoteDir = remoteFilePath.substring(0, slashPos);
                if (!checkedDirs.contains(remoteDir)) {
                    checkedDirs.add(remoteDir);
                    Future<Integer> task = CommonTasksSupport.mkDir(execEnv, remoteDir, null);
                    task.get();
                }
            }
        }

    }

    @Override
    public RemoteSyncSupport.Worker getUploader(Lookup.Provider project, ExecutionEnvironment execEnv) throws IOException {
        return new Uploader(project, execEnv);
    }

}
