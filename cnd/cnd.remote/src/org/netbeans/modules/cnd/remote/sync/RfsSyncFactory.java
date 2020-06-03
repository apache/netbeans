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

import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteSyncWorker;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 */
public @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory.class, position=300)
class RfsSyncFactory extends BaseSyncFactory {

    public static final boolean ENABLE_RFS = CndUtils.getBoolean("cnd.remote.fs", true);
    
    /** this factory ID -  public for test purposes */
    public static final String ID = "rfs"; //NOI18N

    @Override
    public RemoteSyncWorker createNew( ExecutionEnvironment executionEnvironment, PrintWriter out, PrintWriter err, 
            FileObject privProjectStorageDir, String workingDir, List<FSPath> files, List<FSPath> buildResults) {
        return new RfsSyncWorker(executionEnvironment, out, err, privProjectStorageDir, files, buildResults);
    }

    @Override
    public String getDisplayName() {
        // That's justa  replacement for ScpSyncFactory/ScpSyncWorker - we don't need no new name
        return NbBundle.getMessage(getClass(), "RFS_Factory_Name");
    }

    @Override
    public String getDescription() {
        // That's justa  replacement for ScpSyncFactory/ScpSyncWorker - we don't need no new name
        return NbBundle.getMessage(getClass(), "RFS_Factory_Description");
    }

    @Override
    public String getID() {
        return ID;
    }
    
    @Override
    public boolean isCopying() {
        return true;
    }
    
    @Override
    public boolean isApplicable(ExecutionEnvironment execEnv) {
        if (ENABLE_RFS && execEnv.isRemote() && ! RemoteUtil.isForeign(execEnv)) {
            Boolean applicable = RfsSetupProvider.isApplicable(execEnv);
            if (applicable == null) {
                RemoteUtil.LOGGER.log(Level.WARNING, "Can not determine whether RFS is applicable for {0}", execEnv.getDisplayName());
                return true;
            } else {
                return applicable.booleanValue();
            }
        } else {
            return false;
        }
    }

    @Override
    public PathMap getPathMap(ExecutionEnvironment executionEnvironment) {
        return RemotePathMap.getPathMap(executionEnvironment, false);
    }
}
