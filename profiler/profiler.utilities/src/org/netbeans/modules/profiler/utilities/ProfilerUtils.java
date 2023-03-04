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
package org.netbeans.modules.profiler.utilities;

import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;

/**
 * Miscellaneous utilities
 *
 * @author Jiri Sedlacek
 */
public final class ProfilerUtils {
    
    private static RequestProcessor profilerRequestProcessor;
    private static ErrorManager profilerErrorManager;
    
    
    public static synchronized RequestProcessor getProfilerRequestProcessor() {
        if (profilerRequestProcessor == null)
            profilerRequestProcessor = new RequestProcessor("Profiler Request Processor", 1); // NOI18N
        return profilerRequestProcessor;
    }
    
    public static synchronized ErrorManager getProfilerErrorManager() {
        if (profilerErrorManager == null)
            profilerErrorManager = ErrorManager.getDefault().getInstance("org.netbeans.modules.profiler"); // NOI18N
        return profilerErrorManager;
    }
    
    public static void runInProfilerRequestProcessor(Runnable r) {
        getProfilerRequestProcessor().post(r);
    }
    
    public static void runInProfilerRequestProcessor(Runnable r, int delay) {
        getProfilerRequestProcessor().post(r, delay);
    }
}
