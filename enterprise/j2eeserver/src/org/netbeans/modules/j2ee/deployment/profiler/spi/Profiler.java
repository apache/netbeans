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

package org.netbeans.modules.j2ee.deployment.profiler.spi;

import java.util.Map;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.j2ee.deployment.profiler.api.ProfilerServerSettings;

/**
 * Profiler has to implement this interface and register it in the default Lookup.
 *
 * @author sherold
 */
public interface Profiler {

    /**
     * Inform the profiler that some server is starting in the profile mode. It
     * allows the Profiler to correctly detect STATE_STARTING.
     */
    void notifyStarting();
    
    /**
     * This method is used from the <code>nbstartprofiledserver</code>
     * task to connect the Profiler to a server ready for profiling.
     *
     * @param projectProperties properties of project the <code>nbstartprofiledserver</code>
     *                          ant task was started from.
     *
     * @return <code>true</code> if the Profiler successfully attached to the server.
     */
    boolean attachProfiler(Map projectProperties);
    
    /**
     * This method is used from the Runtime tab to obtain settings for starting 
     * the server. It displays dialog and let the user choose required mode 
     * (direct/dynamic attach) and other settings for the server startup.
     *
     * @param   serverInstanceID ID of the server instance that is going to be started
     *
     * @return  required settings or <code>null</code> if user cancelled starting 
     *          the server.
     * 
     * @deprecated 
     */
    @Deprecated
    ProfilerServerSettings getSettings(String serverInstanceID);

    /**
     * This method is used from the Runtime tab to obtain settings for starting
     * the server. It displays dialog and let the user choose required mode
     * (direct/dynamic attach) and other settings for the server startup.
     *
     * @param   serverInstanceID ID of the server instance that is going to be started
     * @param   verbose Whether to show the informational dialog
     *
     * @return  required settings or <code>null</code> if user cancelled starting
     *          the server.
     * 
     * @deprecated
     */
    @Deprecated
    ProfilerServerSettings getSettings(String serverInstanceID, boolean verbose);
    
    /**
     * Returns state of Profiler agent instance started from the IDE. It detects 
     * possible response from an unknown (not started from the IDE) Profiler
     * agent, in this case it returns STATE_INACTIVE.
     *
     * @return state of Profiler agent instance.
     */
    int getState();
    
    /**
     * Stops execution of the application (its JVM) currently being profiled.
     * Shutdown is performed by the Profiler agent when in STATE_BLOCKED, STATE_RUNNING
     * or STATE_PROFILING state.
     *
     * @return object used to monitor progress of shutdown.
     */
    ProgressObject shutdown();
}
