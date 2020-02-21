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
import java.util.logging.Logger;

/**
 * Interface for a path mapping utility which will be implemented in another module.
 * 
 */
public abstract class PathMap {

    @Deprecated
    public final boolean checkRemotePath(String path, boolean fixMissingPath) {
        Logger.getLogger("cnd.remote.logger").warning("Use of deprecated PathMap.checkRemotePath");
        return checkRemotePaths(new File[] { new File(path) }, fixMissingPath);
    }

    public abstract boolean checkRemotePaths(File[] localPaths, boolean fixMissingPath);

    public abstract String getTrueLocalPath(String rpath);

    public String getLocalPath(String rpath) {
        return getLocalPath(rpath, false);
    }

    @Deprecated
    public abstract String getLocalPath(String rpath, boolean useDefault);

    public String getRemotePath(String lpath) {
        return getRemotePath(lpath, false);
    }

    //TODO: deprecate and remote
    public abstract String getRemotePath(String lpath, boolean useDefault);
}
