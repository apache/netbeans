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

package org.netbeans.modules.cnd.spi.remote.setup;

import java.io.IOException;
import java.net.ConnectException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;

/**
 * Allows to redefine places where local->remote and remote->local mirrors are located
 */
public interface MirrorPathProvider {

    /**
     * Gets remote mirror absolute path.
     * Remote mirror is a place to which project files are copied
     * in the case non-sharing synchronization.
     * @param executionEnvironment execution environment
     * @return remote mirror absolute path
     * or null in the case this provider can not provide it for the given environment
     */
    String getRemoteMirror(ExecutionEnvironment executionEnvironment) throws ConnectException, IOException, ConnectionManager.CancellationException;

    /**
     * Gets local mirror absolute path.
     * Local mirror is used for copying remote files cache on the local machine
     * (for now, remote headers are processed this way)
     * @param executionEnvironment execution environment
     * @return local mirror absolute path
     * or null in the case this provider can not provide it for the given environment
     *
     * The method is not used in 6.8; it is reserved for post 6.8 use
     */
    String getLocalMirror(ExecutionEnvironment executionEnvironment);
}
