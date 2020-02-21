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
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.remote.support.RemoteProjectSupport;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory.class, position=200)
public class FullRemoteSyncFactory extends BaseSyncFactory {

    /** this factory ID -  public for test purposes */
    private static final String ID = "full"; //NOI18N
    private final PathMap pathMapper = new FullRemotePathMap();

    @Override
    public RemoteSyncWorker createNew( ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, 
            FileObject privProjectStorageDir, String workingDir, List<FSPath> files, List<FSPath> buildResults) {
        return new FullRemoteSyncWorker(executionEnvironment, out, err, files);
    }

    @Override
    public RemoteSyncWorker createNew(Lookup.Provider project, PrintWriter out, PrintWriter err) {
        ExecutionEnvironment execEnv = RemoteProjectSupport.getExecutionEnvironment(project);
        RemoteProject rp = project.getLookup().lookup(RemoteProject.class);
        if (rp == null) {
            return null;
        }
        final FileObject projDirFO = rp.getSourceBaseDirFileObject();
        FileSystem fileSystem = RemoteFileUtil.getProjectSourceFileSystem(project);
        return new FullRemoteSyncWorker(execEnv, out, err, Collections.singletonList(FSPath.toFSPath(projDirFO)));
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "FULL_Factory_Name");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(getClass(), "FULL_Factory_Description");
    }


    @Override
    public String getID() {
        return ID;
    }

    @Override
    public boolean isCopying() {
        return false;
    }
    
    @Override
    public boolean isApplicable(ExecutionEnvironment execEnv) {
        // return RemoteProject.FULL_REMOTE && ! RemoteUtil.isForeign(execEnv);
        return false; // never show it in the list
    }

    @Override
    public boolean isPathMappingCustomizable() {
        return false;
    }

    @Override
    public PathMap getPathMap(ExecutionEnvironment executionEnvironment) {
        return pathMapper;
    }

    private final static class FullRemotePathMap extends PathMap {

        @Override
        public boolean checkRemotePaths(File localPaths[], boolean fixMissingPath) {
            return true;
        }

        @Override
        public String getLocalPath(String rpath, boolean useDefault) {
            return rpath;
        }

        @Override
        public String getRemotePath(String lpath, boolean useDefault) {
            return lpath;
        }

        @Override
        public String getTrueLocalPath(String rpath) {
            return null;
        }
    }
}
