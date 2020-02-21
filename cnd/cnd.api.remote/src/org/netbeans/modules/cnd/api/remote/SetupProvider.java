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
import java.util.Collection;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * An interface to allow cnd modules to add binary and script files to the cnd.remote setup and initialization.
 *
 */
public interface SetupProvider {
    /**
     * Gets a map of files that need to be delivered to the remote host
     * @param env host files should be delivered to
     * @return a map, where
     * key - the relative remote path (relative against HostInfoProvider.getLibDir(env))
     * value - the local file
     *
     * Local path can be absolute or relative;
     * in the latter case, InstalledFileLocator will be used to find the file
     */
    public Map<String, File> getBinaryFiles(ExecutionEnvironment env);
    
    public void failed(Collection<File> files, StringBuilder describeProblem);
}
