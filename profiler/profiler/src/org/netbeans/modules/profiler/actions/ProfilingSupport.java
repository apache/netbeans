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

package org.netbeans.modules.profiler.actions;

import org.openide.util.NbBundle;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.utilities.ProfilerUtils;


/**
 * A supporting class for the IDE profiling actions.
 * It centralizes all the code that has to do with figuring out context
 * from the IDE and interface it to the actual profiling.
 *
 * @author Ian Formanek
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "CAPTION_Question=Question",
    "ProfilingSupport_StopStartProfileSessionMessage=Profiling session is currently in progress.\nDo you want to stop the current session and start a new one?",
    "ProfilingSupport_StopStartAttachSessionMessage=Profiling session is currently in progress\nDo you want to detach from the target application and start a new profiling session?",
    "ProfilingSupport_FailedLoadSettingsMsg=Failed to load attach settings: {0}"
})
public final class ProfilingSupport {

    public static boolean checkProfilingInProgress() {
        final Profiler profiler = Profiler.getDefault();
        final int state = profiler.getProfilingState();
        final int mode = profiler.getProfilingMode();

        if ((state == Profiler.PROFILING_PAUSED) || (state == Profiler.PROFILING_RUNNING)) {
            if (mode == Profiler.MODE_PROFILE) {
                if (!ProfilerDialogs.displayConfirmation(
                    Bundle.ProfilingSupport_StopStartProfileSessionMessage(), 
                    Bundle.CAPTION_Question())) {
                    return true;
                }
                // TODO remove the condition when the method is only called in awt or only in RP
                if (SwingUtilities.isEventDispatchThread()) {
                    StopAction.getInstance().setEnabled(false);
                    ProfilerUtils.runInProfilerRequestProcessor(new Runnable() {
                        @Override
                        public void run() {
                            profiler.stopApp();
                        }
                    });
                } else {
                    profiler.stopApp();
                }
                
            } else {
                if (!ProfilerDialogs.displayConfirmation(
                    Bundle.ProfilingSupport_StopStartAttachSessionMessage(), 
                    Bundle.CAPTION_Question())) {
                    return true;
                }

                profiler.detachFromApp();
            }
        }

        return false;
    }
    
}
