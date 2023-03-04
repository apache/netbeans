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
import org.netbeans.lib.profiler.TargetAppRunner;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.modules.profiler.utilities.Delegate;
import org.netbeans.modules.profiler.ResultsListener;
import org.netbeans.modules.profiler.ResultsManager;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.netbeans.modules.profiler.utilities.ProfilerUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.lookup.ServiceProvider;


/**
 * Reset Collected Results for the profiled application (= Reset Collectors)
 *
 * @author Ian Formanek
 */
@NbBundle.Messages({
    "LBL_ResetResultsAction=R&eset Collected Results",
    "HINT_ResetResultsAction=Reset collected results"
})
public final class ResetResultsAction extends CallableSystemAction {
    
    Listener resultListener;
    
    //~ Constructors -------------------------------------------------------------------------------------------------------------
    
    /* 
     * The following code is an externalization of various listeners registered
     * in the global lookup and needing access to an enclosing instance of
     * ResetResultsAction. 
     * The enclosing instance will use the FQN registration to obtain the shared instance
     * of the listener implementation and inject itself as a delegate into the listener.
     */
    @ServiceProvider(service=ResultsListener.class)
    public static final class Listener extends Delegate<ResetResultsAction> implements ResultsListener {
        @Override
        public void resultsAvailable() {
            if (getDelegate() != null) getDelegate().updateEnabledState();
        }

        @Override
        public void resultsReset() { 
            if (getDelegate() != null) getDelegate().updateEnabledState();
        }
    }
    
    private static final class Singleton {
        private static final ResetResultsAction INSTANCE = new ResetResultsAction();
    }
    
    private ResetResultsAction() {
        putValue(Action.NAME, Bundle.LBL_ResetResultsAction());
        putValue(Action.SHORT_DESCRIPTION, Bundle.HINT_ResetResultsAction());
        putValue(Action.SMALL_ICON, Icons.getIcon(ProfilerIcons.RESET_RESULTS));
        putValue("iconBase", Icons.getResource(ProfilerIcons.RESET_RESULTS)); // NOI18N
        
        resultListener = Lookup.getDefault().lookup(Listener.class);
        resultListener.setDelegate(this);
        updateEnabledState();
    }
    
    @ActionID(category="Profile", id="org.netbeans.modules.profiler.actions.ResetResultsAction")
    @ActionRegistration(displayName="#LBL_ResetResultsAction", lazy=false)
    @ActionReferences({
        @ActionReference(path="Menu/Profile", position=1000, separatorAfter=1100),
        @ActionReference(path = "Shortcuts", name = "AS-F2")
    })
    public static ResetResultsAction getInstance() {
        return Singleton.INSTANCE;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    /**
     * Invoked when an action occurs.
     */
    @Override
    public void performAction() {
        
        ProfilerUtils.runInProfilerRequestProcessor(new Runnable() {
            @Override
            public void run() {
                ResultsManager.getDefault().reset();
        
                try {
                    TargetAppRunner runner = Profiler.getDefault().getTargetAppRunner();

                    if (runner.targetJVMIsAlive()) {
                        runner.resetTimers();
                    } else {
                        runner.getProfilerClient().resetClientData();

                        // TODO 
                        //        CPUCallGraphBuilder.resetCollectors();
                    }
                } catch (ClientUtils.TargetAppOrVMTerminated targetAppOrVMTerminated) {} // ignore
            }
        });
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public String getName() {
        return Bundle.LBL_ResetResultsAction();
    }

    @Override
    protected String iconResource() {
        return Icons.getResource(ProfilerIcons.RESET_RESULTS);
    }
    
    private void updateEnabledState() {
        setEnabled(ResultsManager.getDefault().resultsAvailable());
    }
}
