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

package org.netbeans.modules.profiler.ppoints;

import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.openide.util.Lookup;


/**
 * Abstract superclass for all Profiling Points defined globally for profiling session
 *
 * @author Jiri Sedlacek
 */
public abstract class GlobalProfilingPoint extends ProfilingPoint {
    //~ Constructors -------------------------------------------------------------------------------------------------------------
    GlobalProfilingPoint(String name, Lookup.Provider project, ProfilingPointFactory factory) {
        super(name, project, factory);
    }
    
    
    public boolean supportsProfilingSettings(ProfilingSettings profilingSettings) {
        return super.supportsProfilingSettings(profilingSettings) ||
        // CPU profiling
        (profilingSettings.getProfilingType() == ProfilingSettings.PROFILE_CPU_SAMPLING && getFactory().supportsCPU())
               || 
        // Memory profiling
        (profilingSettings.getProfilingType() == ProfilingSettings.PROFILE_MEMORY_SAMPLING && getFactory().supportsMemory());
    }
    

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    abstract void hit(long hitValue);
}
