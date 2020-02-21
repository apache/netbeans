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

package org.netbeans.modules.cnd.api.remote;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.Lookup;

/**
 * Utility functions related with remote synchronization
 */
public final class RemoteSyncSupport {

    private RemoteSyncSupport() {
    }

    public static RemoteSyncWorker createSyncWorker(Lookup.Provider project, PrintWriter out, PrintWriter err) {
        if (project != null) {
            RemoteProject remoteProject = project.getLookup().lookup(RemoteProject.class);
            if (remoteProject != null) {
                RemoteSyncFactory syncFactory = remoteProject.getSyncFactory();
                if (syncFactory != null) {
                    return syncFactory.createNew(project, out, err);
                }
            }
        }
        return null;
    }

    /** 
     * For the case we know environment for sure, but project can be null -
     * instead of repeating in client construct like
     * pathMap = (project == null) ? HostInfoProvider.getMapper(execEnv) : RemoteSyncSupport.getPathMap(project);
     */
    public static PathMap getPathMap(ExecutionEnvironment env, Lookup.Provider project) {
        PathMap pathMap = null;
        if (project != null) {
            pathMap = getPathMap(project);
        }
        if (pathMap == null) {
            pathMap = HostInfoProvider.getMapper(env);
        }
        return pathMap;
    }

    public static PathMap getPathMap(Lookup.Provider project) {
        if (project == null) {
            return null;
        }
        RemoteProject remoteProject = project.getLookup().lookup(RemoteProject.class);
        if (remoteProject == null) {
            return null;
        } else {
            PathMap pathMap = null;
            ExecutionEnvironment execEnv = remoteProject.getDevelopmentHost();
            if (execEnv == null) {
                return null;
            }
            RemoteSyncFactory syncFactory = remoteProject.getSyncFactory();
            if (syncFactory != null) {
                pathMap = syncFactory.getPathMap(execEnv);
            }
            if (pathMap == null) {
                pathMap = HostInfoProvider.getMapper(execEnv);
            }
            return pathMap;
        }
    }

    public static ExecutionEnvironment getRemoteFileSystemHost(Lookup.Provider project) {
        RemoteProject remoteProject = project.getLookup().lookup(RemoteProject.class);
        return (remoteProject == null) ? ExecutionEnvironmentFactory.getLocal() : remoteProject.getSourceFileSystemHost();
    }

//    /**
//     * Creates an instance of RemoteSyncWorker.
//     *
//     * @param localDir local directory that should be synchronized
//     *
//     * @param executionEnvironment
//     *
//     * @param out output stream:
//     * in the case implementation uses an external program (rsync? scp?),
//     * it can redirect its stdout here
//     *
//     * @param err error stream:
//     * in the case implementation uses an external program (rsync? scp?),
//     * it can redirect its stderr here
//     *
//     * @param privProjectStorageDir a directory to store misc. cache-ing information;
//     * it is caller's responsibility top guarantee that different local dirs
//     * has different privProjectStorage associated
//     * (usually it is "nbprohect/private" :-))
//     *
//     * @return new instance of the RemoteSyncWorker
//     */
//    public static RemoteSyncWorker createSyncWorker(ExecutionEnvironment executionEnvironment,
//        PrintWriter out, PrintWriter err, File privProjectStorageDir, File... localDirs) {
//
//        ServerRecord serverRecord = ServerList.get(executionEnvironment);
//        if (serverRecord != null) {
//            RemoteSyncFactory syncFactory = serverRecord.getSyncFactory();
//            if (syncFactory != null) {
//                return syncFactory.createNew(executionEnvironment, out, err, privProjectStorageDir, localDirs);
//            }
//        }
//        return null;
//    }

    public static class PathMapperException extends Exception {

        private final File file;
        
        public PathMapperException(File file) {
            super("Could not find remote path for " + file.getAbsolutePath()); //NOI18N
            this.file = file;
        }

        public File getFile() {
            return file;
        }
    }

    public interface Worker {
        void process(File file, Writer err) throws PathMapperException, InterruptedException, ExecutionException, IOException;
        void close();
    }

    public static Worker createUploader(Lookup.Provider project, ExecutionEnvironment execEnv) throws IOException {
        RemoteSyncService rss = Lookup.getDefault().lookup(RemoteSyncService.class);
        return (rss == null) ? null : rss.getUploader(project, execEnv);
    }
}
