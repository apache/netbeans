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
package org.netbeans.modules.profiler.nbimpl;

import java.io.File;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.modules.profiler.ProfilerModule;
import org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher;
import org.netbeans.modules.profiler.utilities.ProfilerUtils;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Hurka
 */
@ServiceProvider(service=Profiler.class)
public class NetBeansProfiler extends org.netbeans.modules.profiler.NetBeansProfiler {
    
    // Emits PROFILING_INACTIVE event to all listeners in case the profiling session
    // is not started/running after [millis], even though this is not a state change.
    // Used to detect & notify that non-observable starting of profiling session failed.
    public void checkAliveAfter(int millis) {
        if (getProfilingState() == PROFILING_INACTIVE) ProfilerUtils.runInProfilerRequestProcessor(new Runnable() {
            public void run() {
                if (getProfilingState() == PROFILING_INACTIVE)
                    fireProfilingStateChange(PROFILING_IN_TRANSITION, PROFILING_INACTIVE);
            }
        }, millis);
    }

    @Override
    public String getLibsDir() {
        final File dir = InstalledFileLocator.getDefault()
                                             .locate(ProfilerModule.LIBS_DIR + "/jfluid-server.jar", //NOI18N
                                                     "org.netbeans.lib.profiler", false); //NOI18N

        if (dir == null) {
            return null;
        } else {
            return dir.getParentFile().getPath();
        }
    }    


    @Override
    public boolean rerunAvailable() {
        int state = getProfilingState();
        return (state == Profiler.PROFILING_INACTIVE || state == Profiler.PROFILING_STOPPED) ? ProfilerLauncher.canRelaunch() : false;
    }

    @Override
    public boolean modifyAvailable() {
        return getProfilingState() == Profiler.PROFILING_RUNNING;
    }

    @Override
    public void rerunLastProfiling() {
        ProfilerLauncher.Session s = ProfilerLauncher.getLastSession();
        if (s != null) {
            s.run();
        }
    }
    
}
