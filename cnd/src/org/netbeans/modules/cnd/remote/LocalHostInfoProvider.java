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

package org.netbeans.modules.cnd.remote;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.util.Utilities;

/**
 * HostInfoProvider implementation for local host
 */
/*package-local*/ class LocalHostInfoProvider extends HostInfoProvider {

    private final ExecutionEnvironment execEnv;

    LocalHostInfoProvider(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
    }

    @Override
    public boolean fileExists(String path) {
        if (new File(path).exists()) {
            return true;
        }
        if (Utilities.isWindows() && !path.endsWith(".lnk")) { //NOI18N
            return new File(path+".lnk").exists(); //NOI18N
        }
        return false;
    }

    @Override
    public Map<String, String> getEnv() {
        Map<String, String> result = null;

        if (HostInfoUtils.isHostInfoAvailable(execEnv)) {
            try {
                result = HostInfoUtils.getHostInfo(execEnv).getEnvironment();
            } catch (IOException ex) {
            } catch (CancellationException ex) {
            }
        }

        if (result == null) {
            result = System.getenv();
        }

        return result;
    }

    @Override
    public String getLibDir() {
        return null;
    }

    @Override
    public PathMap getMapper() {
        return new LocalPathMap();
    }

    private static class LocalPathMap extends PathMap {

        @Override
        public boolean checkRemotePaths(File[] localPaths, boolean fixMissingPath) {
            return false;
        }

        @Override
        public String getLocalPath(String rpath,boolean useDefault) {
            return rpath;
        }

        @Override
        public String getRemotePath(String lpath,boolean useDefault) {
            return lpath;
        }

        @Override
        public String getTrueLocalPath(String rpath) {
            return rpath;
        }
    }
}
