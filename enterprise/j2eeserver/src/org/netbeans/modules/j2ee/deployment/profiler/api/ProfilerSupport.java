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

package org.netbeans.modules.j2ee.deployment.profiler.api;

import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.profiler.spi.Profiler;

/**
 * Allows to determine current state of a Profiler registered in the default Lookup.
 *
 * @author sherold
 */
public final class ProfilerSupport {

    /**
     * The Profiler agent isn't running.
     */
    public static final int STATE_INACTIVE  = 0;

    /**
     * The Profiler agent is starting to STATE_BLOCKING or STATE_RUNNING state,
     * target JVM isn't running.
     */
    public static final int STATE_STARTING  = 1;
    
    /**
     * The Profiler agent is running and ready for the Profiler to connect, target
     * JVM is blocked.
     */
    public static final int STATE_BLOCKING  = 2;
    
    /**
     * The Profiler agent is running and ready for the Profiler to connect, target
     * JVM is running.
     */
    public static final int STATE_RUNNING   = 3;
    
    /**
     * The Profiler agent is running and connected to Profiler, target JVM is running.
     */
    public static final int STATE_PROFILING = 4;
    
    /**
     * Returns the current state of a Profiler registered into Lookup.
     *
     * @return the current profiler state or <code>STATE_INACTIVE</code> if no 
     *         Profiler is registered in the default Lookup.
     */
    public static int getState() {
        Profiler profiler = ServerRegistry.getProfiler();
        return profiler == null ? STATE_INACTIVE 
                                : profiler.getState();
    }
}
