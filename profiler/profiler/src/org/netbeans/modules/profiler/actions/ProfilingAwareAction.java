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

import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.event.SimpleProfilingStateAdapter;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.openide.util.actions.CallableSystemAction;


/**
 * @author Ian Formanek
 */
public abstract class ProfilingAwareAction extends CallableSystemAction {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    boolean enabledSet = false;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    protected ProfilingAwareAction() {
        Profiler.getDefault().addProfilingStateListener(new SimpleProfilingStateAdapter() {

            @Override
            protected void update() {
                updateAction();
            }
        });
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    @Override
    public final boolean isEnabled() {
        if(enabledSet) {
            return super.isEnabled();
        } else {
            return shouldBeEnabled(Profiler.getDefault());
        }
    }

    @Override
    public final void setEnabled(boolean value) {
        enabledSet = true;
        super.setEnabled(value);
    }

    /** Called whenever state of the Profiler has changed.
     *  By default this method use {@link #shouldBeEnabled(org.netbeans.lib.profiler.common.Profiler)} to update the
     *  enabled property of the action.
     */
    protected void updateAction()
    {
        setEnabled(shouldBeEnabled(Profiler.getDefault()));
    }

    /** Compute if the action is enabled based on the state of the Profiler.
     *  Default implementation uses array returned by the {@link #enabledStates() } to determine the state.
     */
    protected boolean shouldBeEnabled(Profiler profiler) {
        boolean shouldBeEnabled = false;
        int lastProfilingState = profiler.getProfilingState();
        int lastInstrumentation = lastProfilingState != Profiler.PROFILING_INACTIVE ?
                                profiler.getTargetAppRunner().getProfilerClient().getCurrentInstrType() :
                                 CommonConstants.INSTR_NONE;

        final int[] enabledStates = enabledStates();

        for (int i = 0; i < enabledStates.length; i++) {
            if (lastProfilingState == enabledStates[i]) {
                shouldBeEnabled = true;

                break;
            }
        }

        if (shouldBeEnabled && requiresInstrumentation()) {
            shouldBeEnabled = (lastInstrumentation != CommonConstants.INSTR_NONE);
        }

        return shouldBeEnabled;
    }    

    /** Used by the default implementation of the {@link #shouldBeEnabled(Profiler) } to determine the enabled
     *  state of the action. */
    protected abstract int[] enabledStates();

    @Override
    protected final boolean asynchronous() {
        return false;
    }

    protected boolean requiresInstrumentation() {
        return false;
    }
    
}
