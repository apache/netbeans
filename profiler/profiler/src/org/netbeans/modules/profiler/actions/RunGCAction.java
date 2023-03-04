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

import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.ProfilerLogger;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.netbeans.modules.profiler.utilities.ProfilerUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * Run Garbage Collection in the target application VM
 *
 * @author Ian Formanek
 */
@NbBundle.Messages({
    "LBL_RunGCAction=Run &GC",
    "HINT_RunGCAction=Request garbage collection in the profiled process"
})
public final class RunGCAction extends ProfilingAwareAction {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final int[] ENABLED_STATES = new int[] { Profiler.PROFILING_RUNNING };
    
    private static final class Singleton {
        private static final RunGCAction INSTANCE = new RunGCAction();
    }
    
    @ActionID(id = "org.netbeans.modules.profiler.actions.RunGCAction", category = "Profile")
    @ActionRegistration(displayName = "#LBL_RunGCAction", lazy=false)
    @ActionReference(path = "Menu/Profile", position = 700, separatorAfter=800)    
    public static RunGCAction getInstance() {
        return Singleton.INSTANCE;
    }

    
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    protected RunGCAction() {
        super();
        setIcon(Icons.getIcon(ProfilerIcons.RUN_GC));
        putValue("iconBase", Icons.getResource(ProfilerIcons.RUN_GC)); // NOI18N
        putProperty(Action.SHORT_DESCRIPTION, Bundle.HINT_RunGCAction());
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;

        // If you will provide context help then use:
        // return new HelpCtx(MyAction.class);
    }

    public String getName() {
        return Bundle.LBL_RunGCAction();
    }

    public void performAction() {
        ProfilerUtils.runInProfilerRequestProcessor(new Runnable() {
            public void run() {
                try {
                    Profiler.getDefault().getTargetAppRunner().runGC();
                } catch (final ClientUtils.TargetAppOrVMTerminated e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() { ProfilerDialogs.displayWarning(e.getMessage()); }
                    });
                    ProfilerLogger.log(e.getMessage());
                }
            }
        });
    }

    protected int[] enabledStates() {
        return ENABLED_STATES;
    }
}
