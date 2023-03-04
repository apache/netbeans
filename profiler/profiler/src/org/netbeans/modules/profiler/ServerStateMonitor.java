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
package org.netbeans.modules.profiler;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.event.SimpleProfilingStateAdapter;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.openide.util.NbBundle;

/**
 * Monitor of the profiler state which displays status of the server in the main window status bar.
 *
 * @author Jan Taus
 */
@NbBundle.Messages({
    "ServerStateMonitor_ProfilerBusy=Profiler Busy",
    "ServerStateMonitor_ServerInitializing=Initializing...",
    "ServerStateMonitor_ServerPreparing=Preparing data...",
    "ServerStateMonitor_ServerInstrumenting=Instrumenting..."
})
class ServerStateMonitor {    
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final Profiler profiler;
    private ProgressHandle progressHandle = null;
    private int activeServerState = -1;
    private int activeServerProgress = -1;
    private int activeServerProgressValue = -1;

    //~ Constructors ---------------------------------------------------------------------------------------------------------

    ServerStateMonitor(Profiler profiler) {
        this.profiler = profiler;
        updateProgress();
        profiler.addProfilingStateListener(new SimpleProfilingStateAdapter() {
            @Override
            protected void update() {
                updateProgress();
            }
        });
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    private void updateProgress() {
        boolean display = profiler.getProfilingState() != Profiler.PROFILING_INACTIVE &&
                          profiler.getServerState() != CommonConstants.SERVER_RUNNING;
        if (display) {
            int serverProgress = profiler.getServerProgress();
            int serverState = profiler.getServerState();
            if (progressHandle == null) {
                progressHandle = ProgressHandle.createHandle(Bundle.ServerStateMonitor_ProfilerBusy());
                if (serverProgress == CommonConstants.SERVER_PROGRESS_INDETERMINATE) {
                    progressHandle.start();
                } else {
                    progressHandle.start(CommonConstants.SERVER_PROGRESS_WORKUNITS);
                }
                activeServerState = -1;
                activeServerProgress = serverProgress;
            }
            if (serverProgress != activeServerProgress) {
                if (activeServerProgress == CommonConstants.SERVER_PROGRESS_INDETERMINATE) {
                    progressHandle.switchToDeterminate(CommonConstants.SERVER_PROGRESS_WORKUNITS);
                    progressHandle.progress(serverProgress);
                    activeServerProgressValue = serverProgress;
                } else if (serverProgress == CommonConstants.SERVER_PROGRESS_INDETERMINATE) {
                    progressHandle.switchToIndeterminate();
                } else {
                    if (serverProgress > activeServerProgressValue) {
                        progressHandle.progress(serverProgress);
                        activeServerProgressValue = serverProgress;
                    }
                }
                activeServerProgress = serverProgress;
            }

            if (serverState != activeServerState) {
                activeServerState = serverState;
                switch (activeServerState) {
                    case CommonConstants.SERVER_INITIALIZING:
                        progressHandle.progress(Bundle.ServerStateMonitor_ServerInitializing());
                        break;
                    case CommonConstants.SERVER_INSTRUMENTING:
                        progressHandle.progress(Bundle.ServerStateMonitor_ServerInstrumenting());
                        break;
                    case CommonConstants.SERVER_PREPARING:
                        progressHandle.progress(Bundle.ServerStateMonitor_ServerPreparing());
                        break;
                    default:
                        progressHandle.progress(""); // NOI18N
                        break;
                }
            }
        } else {
            closeProgress();
        }
    }

    private void closeProgress() {
        if (progressHandle != null) {
            progressHandle.finish();
            progressHandle = null;
        }
    }
}
