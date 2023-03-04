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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.ui.components.ProfilerToolbar;
import org.netbeans.lib.profiler.ui.swing.ActionPopupButton;
import org.netbeans.lib.profiler.ui.swing.GrayLabel;
import org.netbeans.lib.profiler.ui.threads.ThreadsPanel;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ThreadsFeatureUI_show=Show:",
    "ThreadsFeatureUI_filterAll=All threads",
    "ThreadsFeatureUI_filterLive=Live threads",
    "ThreadsFeatureUI_filterFinished=Finished threads",
    "ThreadsFeatureUI_filterSelected=Selected threads",
    "ThreadsFeatureUI_timeline=Timeline:",
    "ThreadsFeatureUI_threadsFilter=Threads filter",
    "# HTML formatted:",
    "ThreadsFeatureUI_noThreadsMsg=<html><b>No threads are currently selected.</b><br><br>Use the Selected column or invoke Select thread action to select threads.</html>"
})
abstract class ThreadsFeatureUI extends FeatureUI {
    
    private ProfilerToolbar toolbar;
    private ThreadsPanel threadsView;
    
    
    // --- External implementation ---------------------------------------------
    
    abstract Profiler getProfiler();
    
    
    // --- API implementation --------------------------------------------------
    
    ProfilerToolbar getToolbar() {
        if (toolbar == null) initUI();
        return toolbar;
    }

    JPanel getResultsUI() {
        if (threadsView == null) initUI();
        return threadsView;
    }
    
    
    void cleanup() {
        if (threadsView != null) threadsView.cleanup();
    }
    
    
    void sessionStateChanged(int sessionState) {
        refreshToolbar(sessionState);
        
        if (sessionState == Profiler.PROFILING_INACTIVE || sessionState == Profiler.PROFILING_IN_TRANSITION) {
            if (threadsView != null) threadsView.profilingSessionFinished();
        } else if (sessionState == Profiler.PROFILING_RUNNING) {
            if (threadsView != null) threadsView.profilingSessionStarted();
        }
    }
    
    
    // --- UI ------------------------------------------------------------------
    
    private JLabel shLabel;
    private ActionPopupButton shFilter;
    
    private JLabel tlLabel;
    private JComponent tlZoomInButton;
    private JComponent tlZoomOutButton;
    private JComponent tlFitWidthButton;
    
    
    private void initUI() {
        
        assert SwingUtilities.isEventDispatchThread();
        
        // --- Results ---------------------------------------------------------
        
        threadsView = new ThreadsPanel(getProfiler().getThreadsManager(), null) {
            protected void filterSelected(ThreadsPanel.Filter filter) {
                super.filterSelected(filter);
                shFilter.selectAction(filter.ordinal());
            }
        };
        threadsView.threadsMonitoringEnabled();
        
        threadsView.putClientProperty("HelpCtx.Key", "ProfileThreads.HelpCtx"); // NOI18N
        
        
        // --- Toolbar ---------------------------------------------------------
        
        shLabel = new GrayLabel(Bundle.ThreadsFeatureUI_show());

        Action aAll = new AbstractAction() {
            { putValue(NAME, Bundle.ThreadsFeatureUI_filterAll()); }
            public void actionPerformed(ActionEvent e) { setFilter(ThreadsPanel.Filter.ALL); }
            
        };
        Action aLive = new AbstractAction() {
            { putValue(NAME, Bundle.ThreadsFeatureUI_filterLive()); }
            public void actionPerformed(ActionEvent e) { setFilter(ThreadsPanel.Filter.LIVE); }
            
        };
        Action aFinished = new AbstractAction() {
            { putValue(NAME, Bundle.ThreadsFeatureUI_filterFinished()); }
            public void actionPerformed(ActionEvent e) { setFilter(ThreadsPanel.Filter.FINISHED); }
            
        };
        Action aSelected = new AbstractAction() {
            { putValue(NAME, Bundle.ThreadsFeatureUI_filterSelected()); }
            public void actionPerformed(ActionEvent e) { setSelectedFilter(); }
            
        };
        shFilter = new ActionPopupButton(aAll, aLive, aFinished, aSelected);
        shFilter.setToolTipText(Bundle.ThreadsFeatureUI_threadsFilter());

        tlLabel = new GrayLabel(Bundle.ThreadsFeatureUI_timeline());


        tlZoomInButton = (JComponent)threadsView.getZoomIn();
        tlZoomInButton.putClientProperty("JButton.buttonType", "segmented"); // NOI18N
        tlZoomInButton.putClientProperty("JButton.segmentPosition", "first"); // NOI18N
        tlZoomOutButton = (JComponent)threadsView.getZoomOut();
        tlZoomOutButton.putClientProperty("JButton.buttonType", "segmented"); // NOI18N
        tlZoomOutButton.putClientProperty("JButton.segmentPosition", "middle"); // NOI18N
        tlFitWidthButton = (JComponent)threadsView.getFitWidth();
        tlFitWidthButton.putClientProperty("JButton.buttonType", "segmented"); // NOI18N
        tlFitWidthButton.putClientProperty("JButton.segmentPosition", "last"); // NOI18N

        toolbar = ProfilerToolbar.create(true);

        toolbar.addSpace(2);
        toolbar.addSeparator();
        toolbar.addSpace(5);

        toolbar.add(shLabel);
        toolbar.addSpace(2);
        toolbar.add(shFilter);

        toolbar.addSpace(2);
        toolbar.addSeparator();
        toolbar.addSpace(5);

        toolbar.add(tlLabel);
        toolbar.addSpace(2);
        toolbar.add(tlZoomInButton);
        toolbar.add(tlZoomOutButton);
        toolbar.add(tlFitWidthButton);
        
        
        // --- Sync UI ---------------------------------------------------------

        setFilter(ThreadsPanel.Filter.LIVE);
        sessionStateChanged(getSessionState());
        
    }
    
    private void refreshToolbar(final int state) {
//        if (toolbar != null) SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//            }
//        });
    }
    
    private void setSelectedFilter() {
        if (threadsView.hasSelectedThreads()) {
            setFilter(ThreadsPanel.Filter.SELECTED);
        } else {
            threadsView.showSelectedColumn();
            shFilter.selectAction(threadsView.getFilter().ordinal());
            ProfilerDialogs.displayWarning(Bundle.ThreadsFeatureUI_noThreadsMsg());
        }
    }

    private void setFilter(ThreadsPanel.Filter filter) {
        threadsView.setFilter(filter);
    }
    
}
