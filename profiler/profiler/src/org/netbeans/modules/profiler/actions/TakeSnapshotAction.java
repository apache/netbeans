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
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.modules.profiler.ResultsListener;
import org.netbeans.modules.profiler.ResultsManager;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.netbeans.modules.profiler.utilities.Delegate;
import org.netbeans.modules.profiler.utilities.ProfilerUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;


/**
 * Action to take snapshot of results.
 *
 * @author Ian Formanek
 */
@NbBundle.Messages({
    "LBL_TakeSnapshotAction=&Take Snapshot of Collected Results",
    "HINT_TakeSnapshotAction=Take snapshot of collected results"
})
public final class TakeSnapshotAction extends ProfilingAwareAction {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final int[] ENABLED_STATES = new int[] {
                                                    Profiler.PROFILING_RUNNING, Profiler.PROFILING_PAUSED,
                                                    Profiler.PROFILING_STOPPED
                                                };
    
    private Listener listener;

    /*
     * The following code is an externalization of various listeners registered
     * in the global lookup and needing access to an enclosing instance of
     * TakeSnapshotAction.
     * The enclosing instance will use the FQN registration to obtain the shared instance
     * of the listener implementation and inject itself as a delegate into the listener.
     */
    @ServiceProvider(service=ResultsListener.class)
    public static final class Listener extends Delegate<TakeSnapshotAction> implements ResultsListener {

        @Override
        public void resultsAvailable() {
            if (getDelegate() != null) getDelegate().updateAction();
        }

        @Override
        public void resultsReset() {
            if (getDelegate() != null) getDelegate().updateAction();
        }
        
    }
    
    private static final class Singleton {
        private static final TakeSnapshotAction INSTANCE = new TakeSnapshotAction();
    }
    
    @ActionID(id = "org.netbeans.modules.profiler.actions.TakeSnapshotAction", category = "Profile")
    @ActionRegistration(displayName = "#LBL_TakeSnapshotAction")
    @ActionReferences(value = {
        @ActionReference(path = "Shortcuts", name = "A-F2"),
        @ActionReference(path = "Menu/Profile", position = 900)})
    public static TakeSnapshotAction getInstance() {
        return Singleton.INSTANCE;
    }
    
    //~ Constructors -------------------------------------------------------------------------------------------------------------
    public TakeSnapshotAction() {
        listener = Lookup.getDefault().lookup(Listener.class);
        listener.setDelegate(this);
        setIcon(Icons.getIcon(ProfilerIcons.SNAPSHOT_TAKE));
        putValue(Action.SHORT_DESCRIPTION, Bundle.HINT_TakeSnapshotAction());
        putValue("iconBase", Icons.getResource(ProfilerIcons.SNAPSHOT_TAKE)); // NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    @Override
    protected boolean shouldBeEnabled(Profiler profiler) {
        return super.shouldBeEnabled(profiler) && ResultsManager.getDefault().resultsAvailable();
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;

        // If you will provide context help then use:
        // return new HelpCtx(MyAction.class);
    }

    public String getName() {
        return Bundle.LBL_TakeSnapshotAction();
    }

    @Override
    public void performAction() {
        ProfilerUtils.runInProfilerRequestProcessor(new Runnable() {
                public void run() {
                    ResultsManager.getDefault().takeSnapshot();
                }
            });
    }

    @Override
    protected int[] enabledStates() {
        return ENABLED_STATES;
    }

    protected boolean requiresInstrumentation() {
        return true;
    }
}
