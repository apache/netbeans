/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.hudson.spi;

import org.netbeans.modules.hudson.api.HudsonInstance;

/**
 * Agent that is informed about addition and removal of Hudson instances, and
 * that helps the HudsonManager automatically add and remove Hudson instances,
 * depending on the state of the rest of the IDE. For example, some projects can
 * have associated Hudson builder. When such project is opened, its Hudson
 * instance should be added to the manager.
 *
 * @author jhavlin
 */
public abstract class HudsonManagerAgent {

    /**
     * Called when an instance is added to the manager (including the ones added
     * by the manager agent, too).
     *
     * @param instance Hudson instanca that has been just added.
     */
    public abstract void instanceAdded(HudsonInstance instance);

    /**
     * Called when an instance is removed from the manager (including the ones
     * removed by the manager agent, too).
     *
     * @param instance Hudson instance that has been just removed.
     */
    public abstract void instanceRemoved(HudsonInstance instance);

    /**
     * Start the agent. Allocate all resources, e.g. listeners.
     */
    public abstract void start();

    /**
     * Terminate the agent. Release all resources allocated in {@link #start()}.
     */
    public abstract void terminate();
}
