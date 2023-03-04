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

package org.netbeans.modules.profiler.v2.features;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.ui.components.ProfilerToolbar;
import org.netbeans.lib.profiler.ui.locks.LockContentionPanel;
import org.netbeans.lib.profiler.ui.swing.ActionPopupButton;
import org.netbeans.lib.profiler.ui.swing.GrayLabel;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "LocksFeatureUI_show=View by:",
    "LocksFeatureUI_aggregationByThreads=Threads",
    "LocksFeatureUI_aggregationByMonitors=Monitors",
    "LocksFeatureUI_aggregationHint=Results aggregation"
})
abstract class LocksFeatureUI extends FeatureUI {
    
    private ProfilerToolbar toolbar;
    private LockContentionPanel locksView;
    
    // --- External implementation ---------------------------------------------
        
    abstract ProfilerClient getProfilerClient();
    
    abstract void refreshResults();
    
   
    // --- API implementation --------------------------------------------------
    
    ProfilerToolbar getToolbar() {
        if (toolbar == null) initUI();
        return toolbar;
    }

    JPanel getResultsUI() {
        if (locksView == null) initUI();
        return locksView;
    }
    
    
    void sessionStateChanged(int sessionState) {
        refreshToolbar(sessionState);
        
        if (sessionState == Profiler.PROFILING_INACTIVE || sessionState == Profiler.PROFILING_IN_TRANSITION) {
            if (locksView != null) locksView.profilingSessionFinished();
        } else if (sessionState == Profiler.PROFILING_RUNNING) {
            if (locksView != null) locksView.profilingSessionStarted();
        }
    }

    void resetPause() {
//        if (lrPauseButton != null) lrPauseButton.setSelected(false);
    }
    
    void setForceRefresh() {
        if (locksView != null) locksView.setForceRefresh(true);
    }
    
    void refreshData() throws ClientUtils.TargetAppOrVMTerminated {
        if (locksView != null) locksView.refreshData();
    }
        
    void resetData() {
        if (locksView != null) locksView.resetData();
    }
    
    
    // --- UI ------------------------------------------------------------------
    
    private JLabel shLabel;
    private ActionPopupButton shAggregation;
    
    
    private void initUI() {
        
        assert SwingUtilities.isEventDispatchThread();
        
        // --- Results ---------------------------------------------------------

        locksView = new LockContentionPanel() {
            protected ProfilerClient getProfilerClient() {
                return LocksFeatureUI.this.getProfilerClient();
            }
        };
        locksView.lockContentionEnabled();
        
        locksView.putClientProperty("HelpCtx.Key", "ProfileLocks.HelpCtx"); // NOI18N
        
        
        // --- Toolbar ---------------------------------------------------------
        
        shLabel = new GrayLabel(Bundle.LocksFeatureUI_show());
        
        Action aThreads = new AbstractAction() {
            { putValue(NAME, Bundle.LocksFeatureUI_aggregationByThreads()); }
            public void actionPerformed(ActionEvent e) { setAggregation(LockContentionPanel.Aggregation.BY_THREADS); }
            
        };
        Action aMonitors = new AbstractAction() {
            { putValue(NAME, Bundle.LocksFeatureUI_aggregationByMonitors()); }
            public void actionPerformed(ActionEvent e) { setAggregation(LockContentionPanel.Aggregation.BY_MONITORS); }
            
        };
        shAggregation = new ActionPopupButton(aThreads, aMonitors);
        shAggregation.setToolTipText(Bundle.LocksFeatureUI_aggregationHint());

        toolbar = ProfilerToolbar.create(true);

        toolbar.addSpace(2);
        toolbar.addSeparator();
        toolbar.addSpace(5);

        toolbar.add(shLabel);
        toolbar.addSpace(2);
        toolbar.add(shAggregation);


        // --- Sync UI ---------------------------------------------------------
        
        setAggregation(LockContentionPanel.Aggregation.BY_THREADS);
        sessionStateChanged(getSessionState());

    }
    
    private void refreshToolbar(final int state) {
//        if (toolbar != null) SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//            }
//        });
    }
    
    private void setAggregation(LockContentionPanel.Aggregation aggregation) {
        locksView.setAggregation(aggregation);
        shAggregation.selectAction(aggregation.ordinal());
    }
    
}
