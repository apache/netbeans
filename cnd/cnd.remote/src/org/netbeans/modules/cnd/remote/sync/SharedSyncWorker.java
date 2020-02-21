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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.spi.remote.setup.support.RemoteSyncNotifier;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileSystem;

/**
 *
 */
/*package-local*/ class SharedSyncWorker implements RemoteSyncWorker {

    private final File[] files;
    private final FSPath[] fsPaths;
    private final FileSystem fileSystem;
    private final String workingDir;
    private final ExecutionEnvironment executionEnvironment;

    public SharedSyncWorker(ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, 
            String workingDir, List<FSPath> paths, List<FSPath> buildResults) {
        this.fileSystem = SyncUtils.getSingleFileSystem(paths);
        this.fsPaths = paths.toArray(new FSPath[paths.size()]);
        this.files = SyncUtils.toFiles(this.fsPaths);
        this.executionEnvironment = executionEnvironment;
        this.workingDir = workingDir;
    }
    
    @Override
    public boolean startup(Map<String, String> env2add) {

        if (SyncUtils.isDoubleRemote(executionEnvironment, fileSystem)) {
            RemoteSyncNotifier.getInstance().warnDoubleRemote(executionEnvironment, fileSystem);
            return false;
        }

        PathMap mapper = HostInfoProvider.getMapper(executionEnvironment);
        if (files != null && files.length > 0) {
            File[] filesToCheck;
            if (workingDir == null || (!new File(workingDir).exists())) {
                filesToCheck = files;
            } else {
                filesToCheck = new File[files.length + 1];
                System.arraycopy(files, 0, filesToCheck, 0, files.length);
                filesToCheck[files.length] = new File(workingDir);
            }
            // or is filtering inexistent paths a responsibiity of path mapper?
            // it seems it's rather not since it's too common
            return mapper.checkRemotePaths(filterInexistent(filesToCheck), true);
        }
        return true;
    }

    private static File[] filterInexistent(File[] files) {
        boolean inexistentFound = false;
        for (File file : files) {
            if (!file.exists()) {
                inexistentFound = true;
                break;
            }
        }
        if (inexistentFound) {
            List<File> l = new ArrayList<>();
            for (File file : files) {
                if (file.exists()) {
                    l.add(file);
                }
            }
            return l.toArray(new File[l.size()]);
        }
        return files;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean cancel() {
        return false;
    }
}
