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
import org.openide.util.NbBundle;
import javax.swing.*;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.utilities.ProfilerUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;


/**
 * Stop/Finish the currently profiled target application
 *
 * @author Ian Formanek
 */
@NbBundle.Messages({
    "StopAction_DoYouWantToTerminateCap=Detach Profiler",
    "StopAction_DoYouWantToTerminateMsg=Do you want to terminate the profiled application upon detach?",
    "LBL_StopAction=&Stop Profiling Session",
    "HINT_StopAction=Stop (Terminate) the Profiled Application",
    "LBL_DetachAction=Detach...",
    "HINT_DetachAction=Detach from the Profiled Application"
})
public final class StopAction extends ProfilingAwareAction {
    private static final int[] enabledStates = new int[]{Profiler.PROFILING_PAUSED, Profiler.PROFILING_RUNNING, Profiler.PROFILING_STARTED};
    
    private static final class Singleton {
        private static final StopAction INSTANCE = new StopAction();
    }
    
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private boolean taskPosted = false;
    private int mode = -1; // not determined yet

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    
    private StopAction() {
        updateAction();
    }

//    @ActionID(category="Profile", id="org.netbeans.modules.profiler.actions.StopAction")
//    @ActionRegistration(displayName="#LBL_StopAction")
//    @ActionReferences({
//        @ActionReference(path="Menu/Profile", position=300, separatorAfter=400),
//        @ActionReference(path="Shortcuts", name="S-F2")
//    })
    public static StopAction getInstance() {
        return Singleton.INSTANCE;
    }
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Invoked when an action occurs.
     */
    public void performAction() {
        if (taskPosted) { // TODO this doesn't prevent from multiple stop tasks being posted!!!

            return; // already performing
        }

        Runnable task = null;

        if (mode == Profiler.MODE_ATTACH) {
            Boolean ret = ProfilerDialogs.displayCancellableConfirmationDNSA(
                Bundle.StopAction_DoYouWantToTerminateMsg(), Bundle.StopAction_DoYouWantToTerminateCap(),
                null, StopAction.class.getName(), false);

            if (Boolean.TRUE.equals(ret)) {
                task = new Runnable() {
                        public void run() {
                            Profiler.getDefault().stopApp();
                            taskPosted = false;
                        }
                    };
            } else if (Boolean.FALSE.equals(ret)) {
                task = new Runnable() {
                        public void run() {
                            Profiler.getDefault().detachFromApp();
                            taskPosted = false;
                        }
                    };
            }
        } else {
            task = new Runnable() {
                    public void run() {
                        Profiler.getDefault().stopApp();
                        taskPosted = false;
                    }
                };
        }

        if (task != null) {
            taskPosted = true;
            updateAction();
            ProfilerUtils.runInProfilerRequestProcessor(task);
        }
    }

    @Override
    protected int[] enabledStates() {
        return enabledStates;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(StopAction.class);
    }

    @Override
    public String getName() {
        return (mode == Profiler.MODE_PROFILE) ? Bundle.LBL_StopAction() : Bundle.LBL_DetachAction();
    }

    @Override
    protected void updateAction() {
        super.updateAction();
        mode = Profiler.getDefault().getProfilingMode();

        if (mode == Profiler.MODE_PROFILE) {
            setToStop(); 
        } else if (mode == Profiler.MODE_ATTACH) {
            setToDetach(); 
        }
        
        firePropertyChange(SMALL_ICON, null, null);
    }

    @Override
    protected boolean shouldBeEnabled(Profiler profiler) {
        return super.shouldBeEnabled(profiler) && (profiler.getProfilingState() == Profiler.PROFILING_INACTIVE
                || profiler.getServerState() == CommonConstants.SERVER_RUNNING);
    }
    
    private void setToDetach() {
        putValue(Action.NAME, Bundle.LBL_DetachAction());
        putValue(Action.SHORT_DESCRIPTION, Bundle.HINT_DetachAction());
        putValue(Action.SMALL_ICON, Icons.getIcon(GeneralIcons.DETACH));
        putValue("iconBase", Icons.getResource(GeneralIcons.DETACH)); // NOI18N
        setIcon(Icons.getIcon(GeneralIcons.DETACH));
    }

    private void setToStop() {
        putValue(Action.NAME, Bundle.LBL_StopAction());
        putValue(Action.SHORT_DESCRIPTION, Bundle.HINT_StopAction());
        putValue(Action.SMALL_ICON, Icons.getIcon(GeneralIcons.STOP));
        putValue("iconBase", Icons.getResource(GeneralIcons.STOP)); // NOI18N
        setIcon(Icons.getIcon(GeneralIcons.STOP));
    }
}
