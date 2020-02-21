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

import org.netbeans.modules.cnd.spi.remote.*;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * Sets up a host.
 * Each time user wants to set up a new remote host,
 * an instance of HostSetupWorker is created (via HostSetupProvider)
 * 
 */
public interface HostSetupWorker {

    /**
     * Describes a result of setting up a host
     */
    interface Result {
        /** Gets newly added host display name */
        public String getDisplayName();

        /** Gets newly added host execution environment */
        public ExecutionEnvironment getExecutionEnvironment();

        /** Gets a way of synchronization for the newly added host */
        public RemoteSyncFactory getSyncFactory();

        /** Gets a runnable that should be run in background after "Finish| button is pressed */
        public Runnable getRunOnFinish();
    }


    /**
     * Gets result
     * @return result or null in the case set up failed or was cancelled
     */
    Result getResult();
}
