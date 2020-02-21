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

import java.util.Map;
import org.openide.util.Cancellable;

/**
 * RemoteSyncWorker is responsible for the synchronization between local and remomte host.
 * It is created by RemoteSyncFactory each time synchronization is needed
 * (for example, each time the project is built).
 */
public interface RemoteSyncWorker extends Cancellable {

    /**
     * Starts synchronization
     * (it's up to implementation to decide whether all files should be in sync
     * just before return, or the synchronization should be done on the fly)
     *
     * There are no parameters, because a separate instance is created
     * for each synchronization work;
     * so these are factory method parameters, which define what to copy,
     * where to copy, etc.
     *
     * @param env2add (output parameter) - map for worker to store necessary environment settings
     * process on remote host should be run with
     *
     * @return true in the case synchronization started up sucessfully, otehrwise false
     */
    boolean startup(Map<String, String> env2add);

    /**
     * Makes a cleanup.
     * Client MUST call correspondent shutdown for each startup call
     * @return
     */
    void shutdown();

}
