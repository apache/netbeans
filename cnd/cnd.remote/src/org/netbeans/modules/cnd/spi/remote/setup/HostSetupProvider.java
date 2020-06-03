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

import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 * Allows to plug in a different way of setting up a remote host.
 *
 * It's a factory that creates instances of HostSetupWorker
 * (which carries up the work of setting up a host)
 *
 */
public interface HostSetupProvider {

    // TODO: should support the notion of protocol

    /**
     * Gets a string that identifies this provider;
     * it can be used for storing, say, last selected provider in properties
     * @return this provider ID
     */
    String getID();

    /**
     * Gets this provider name to be displayed in UI
     * (most likely, in combo box or radio button group)
     * @return this provider name to be displayed in UI
     */
    String getDisplayName();

    /**
     * Creates an instance of HostSetupWorker,
     * which provide UI and performs actual work
     * @return an instance of HostSetupWorker
     */
    HostSetupWorker createHostSetupWorker(ToolsCacheManager toolsCacheManager);

    /**
     * Determines whether this provider is applicable.
     * This allows switching providers ON and OFF programmatically
     * e.g. via -J-D... ;-)
     * @return true if this provider is applicable, otherwise false
     */
    boolean isApplicable();

    public boolean canCheckSetup(ExecutionEnvironment execEnv);

    public boolean isSetUp(ExecutionEnvironment execEnv);

    public boolean setUp(ExecutionEnvironment execEnv);
}
