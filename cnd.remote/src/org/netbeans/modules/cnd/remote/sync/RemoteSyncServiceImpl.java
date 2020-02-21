/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
