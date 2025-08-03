/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.awt.event.ItemEvent;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.ui.components.ProfilerToolbar;
import org.netbeans.lib.profiler.ui.cpu.LiveCPUView;
import org.netbeans.lib.profiler.ui.cpu.LiveCPUViewUpdater;
import org.netbeans.lib.profiler.ui.swing.GrayLabel;
import org.netbeans.lib.profiler.ui.swing.MultiButtonGroup;
import org.netbeans.modules.profiler.actions.ResetResultsAction;
import org.netbeans.modules.profiler.actions.TakeSnapshotAction;
import org.netbeans.modules.profiler.api.GoToSource;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
//    "MethodsFeatureUI_viewHotSpots=Hot spots",
//    "MethodsFeatureUI_viewCallTree=Call tree",
//    "MethodsFeatureUI_viewCombined=Combined",
    "MethodsFeatureUI_selectedMethods=Selected methods",
    "MethodsFeatureUI_liveResults=Results:",
    "MethodsFeatureUI_pauseResults=Pause live results",
    "MethodsFeatureUI_updateResults=Update live results",
    "MethodsFeatureUI_view=View:",
    "MethodsFeatureUI_viewForward=Forward calls",
    "MethodsFeatureUI_viewHotSpots=Hot spots",
    "MethodsFeatureUI_viewReverse=Reverse calls",
    "MethodsFeatureUI_resultsMode=Results mode",
    "MethodsFeatureUI_profilingData=Collected data:",
    "MethodsFeatureUI_snapshot=Snapshot",
    "MethodsFeatureUI_showAbsolute=Show absolute values",
    "MethodsFeatureUI_showDeltas=Show delta values"
})
abstract class MethodsFeatureUI extends FeatureUI {
    
    private ProfilerToolbar toolbar;
    private LiveCPUView cpuView;
    private LiveCPUViewUpdater updater;

    
    // --- External implementation ---------------------------------------------
    
    abstract Set<ClientUtils.SourceCodeSelection> getClassesSelection();
    
    abstract Set<ClientUtils.SourceCodeSelection> getMethodsSelection();
    
    abstract void selectForProfiling(ClientUtils.SourceCodeSelection value);
    
    abstract Lookup.Provider getProject();
    
    abstract ProfilerClient getProfilerClient();
    
    abstract void refreshResults();
    
    
    // --- API implementation --------------------------------------------------
    
    ProfilerToolbar getToolbar() {
        if (toolbar == null) initUI();
        return toolbar;
    }

    JPanel getResultsUI() {
        if (cpuView == null) initUI();
        return cpuView;
    }
    
    boolean hasResultsUI() {
        return cpuView != null;
    }
    
    void sessionStateChanged(int sessionState) {
        refreshToolbar(sessionState);
    }
    
    
    void resetPause() {
        if (lrPauseButton != null) lrPauseButton.setSelected(false);
    }
    
    void setForceRefresh() {
        if (updater != null) updater.setForceRefresh(true);
    }
    
    void refreshData() throws ClientUtils.TargetAppOrVMTerminated {
        if (updater != null) updater.update();
    }
    
    void resetData() {
        if (lrDeltasButton != null) {
            lrDeltasButton.setSelected(false);
            lrDeltasButton.setToolTipText(Bundle.MethodsFeatureUI_showDeltas());
        }
        if (cpuView != null) {
            cpuView.resetData();
            cpuView.setDiffView(false);
        }
    }
    
    
    void cleanup() {
        if (updater != null) updater.cleanup();
    }
    
    
    // --- UI ------------------------------------------------------------------
    
//    private static enum View { CALL_TREE, HOT_SPOTS, COMBINED }
    
    private JLabel lrLabel;
    private JToggleButton lrPauseButton;
    private JButton lrRefreshButton;
    private JToggleButton lrDeltasButton;
//    private ActionPopupButton lrView;
    
    private JLabel pdLabel;
    private JButton pdSnapshotButton;
    private JButton pdResetResultsButton;
    
    private boolean popupPause;
    private JToggleButton[] toggles;
    
    
    private void initUI() {
        
        assert SwingUtilities.isEventDispatchThread();
        
        // --- Results ---------------------------------------------------------
        
        cpuView = new LiveCPUView(getMethodsSelection()) {
//            protected ProfilerClient getProfilerClient() {
//                return MethodsFeatureUI.this.getProfilerClient();
//            }
// //            protected boolean isSampling() {
// //                return MethodsFeatureUI.this.getProfilerClient().getCurrentInstrType() == ProfilerClient.INSTR_NONE_SAMPLING;
// //            }
// //            protected void requestResults() throws ClientUtils.TargetAppOrVMTerminated {
// //                MethodsFeatureUI.this.getProfilerClient().forceObtainedResultsDump(true);
// //            }
// //            protected CPUResultsSnapshot getResults() throws ClientUtils.TargetAppOrVMTerminated, CPUResultsSnapshot.NoDataAvailableException {
// //                ProfilerClient client = MethodsFeatureUI.this.getProfilerClient();
// //                return client.getStatus().getInstrMethodClasses() == null ?
// //                       null : client.getCPUProfilingResultsSnapshot(false);
// //            }
            protected boolean showSourceSupported() {
                return GoToSource.isAvailable();
            }
            protected void showSource(ClientUtils.SourceCodeSelection value) {
                Lookup.Provider project = getProject();
                String className = value.getClassName();
                String methodName = value.getMethodName();
                String methodSig = value.getMethodSignature();
                GoToSource.openSource(project, className, methodName, methodSig);
            }
            protected void selectForProfiling(ClientUtils.SourceCodeSelection value) {
                MethodsFeatureUI.this.selectForProfiling(value);
            }
            protected void popupShowing() {
                if (lrPauseButton.isEnabled() && !lrRefreshButton.isEnabled()) {
                    popupPause = true;
                    lrPauseButton.setSelected(true);
                }
            }
            protected void popupHidden() {
                if (lrPauseButton.isEnabled() && popupPause) {
                    popupPause = false;
                    lrPauseButton.setSelected(false);
                }
            }
            protected void foundInForwardCalls() {
                super.foundInForwardCalls();
                toggles[0].setSelected(true);
            }
            protected void foundInHotSpots() {
                super.foundInHotSpots();
                toggles[1].setSelected(true);
            }
            protected void foundInReverseCalls() {
                super.foundInReverseCalls();
                toggles[2].setSelected(true);
            }
        };
        
        cpuView.putClientProperty("HelpCtx.Key", "ProfileMethods.HelpCtx"); // NOI18N
        
        updater = new LiveCPUViewUpdater(cpuView, getProfilerClient());
        
        
        // --- Toolbar ---------------------------------------------------------
        
        lrLabel = new GrayLabel(Bundle.MethodsFeatureUI_liveResults());
            
        lrPauseButton = new JToggleButton(Icons.getIcon(GeneralIcons.PAUSE)) {
            protected void fireItemStateChanged(ItemEvent event) {
                boolean paused = isSelected();
                updater.setPaused(paused);
                if (!paused) refreshResults();
                refreshToolbar(getSessionState());
            }
        };
        lrPauseButton.setToolTipText(Bundle.MethodsFeatureUI_pauseResults());
        lrPauseButton.setEnabled(false);

        lrRefreshButton = new JButton(Icons.getIcon(GeneralIcons.UPDATE_NOW)) {
            protected void fireActionPerformed(ActionEvent e) {
                refreshResults();
            }
        };
        lrRefreshButton.setToolTipText(Bundle.MethodsFeatureUI_updateResults());
        
        Icon icon = Icons.getIcon(ProfilerIcons.DELTA_RESULTS);
        lrDeltasButton = new JToggleButton(icon) {
            protected void fireActionPerformed(ActionEvent e) {
                if (!cpuView.setDiffView(isSelected())) setSelected(false);
                setToolTipText(isSelected() ? Bundle.MethodsFeatureUI_showAbsolute() :
                                              Bundle.MethodsFeatureUI_showDeltas());
            }
        };
        lrDeltasButton.setToolTipText(Bundle.MethodsFeatureUI_showDeltas());
        
        MultiButtonGroup group = new MultiButtonGroup();
        toggles = new JToggleButton[3];
        
        JToggleButton forwardCalls = new JToggleButton(Icons.getIcon(ProfilerIcons.NODE_FORWARD)) {
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                cpuView.setView(isSelected(), toggles[1].isSelected(), toggles[2].isSelected());
                refreshResults();
            }
        };
        forwardCalls.putClientProperty("JButton.buttonType", "segmented"); // NOI18N
        forwardCalls.putClientProperty("JButton.segmentPosition", "first"); // NOI18N
        forwardCalls.setToolTipText(Bundle.MethodsFeatureUI_viewForward());
        group.add(forwardCalls);
        toggles[0] = forwardCalls;
//        toolbar.add(forwardCalls);
        forwardCalls.setSelected(true);
        
        JToggleButton hotSpots = new JToggleButton(Icons.getIcon(ProfilerIcons.TAB_HOTSPOTS)) {
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                cpuView.setView(toggles[0].isSelected(), isSelected(), toggles[2].isSelected());
                refreshResults();
            }
        };
        hotSpots.putClientProperty("JButton.buttonType", "segmented"); // NOI18N
        hotSpots.putClientProperty("JButton.segmentPosition", "middle"); // NOI18N
        hotSpots.setToolTipText(Bundle.MethodsFeatureUI_viewHotSpots());
        group.add(hotSpots);
        toggles[1] = hotSpots;
//        toolbar.add(hotSpots);
        hotSpots.setSelected(false);
        
        JToggleButton reverseCalls = new JToggleButton(Icons.getIcon(ProfilerIcons.NODE_REVERSE)) {
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                cpuView.setView(toggles[0].isSelected(), toggles[1].isSelected(), isSelected());
                refreshResults();
            }
        };
        reverseCalls.putClientProperty("JButton.buttonType", "segmented"); // NOI18N
        reverseCalls.putClientProperty("JButton.segmentPosition", "last"); // NOI18N
        reverseCalls.setToolTipText(Bundle.MethodsFeatureUI_viewReverse());
        group.add(reverseCalls);
        toggles[2] = reverseCalls;
//        toolbar.add(reverseCalls);
        reverseCalls.setSelected(false);
        
//        Action aCallTree = new AbstractAction() {
//            { putValue(NAME, Bundle.MethodsFeatureUI_viewCallTree()); }
//            public void actionPerformed(ActionEvent e) { setView(View.CALL_TREE); }
//            
//        };
//        Action aHotSpots = new AbstractAction() {
//            { putValue(NAME, Bundle.MethodsFeatureUI_viewHotSpots()); }
//            public void actionPerformed(ActionEvent e) { setView(View.HOT_SPOTS); }
//            
//        };
//        Action aCombined = new AbstractAction() {
//            { putValue(NAME, Bundle.MethodsFeatureUI_viewCombined()); }
//            public void actionPerformed(ActionEvent e) { setView(View.COMBINED); }
//            
//        };
//        lrView = new ActionPopupButton(aCallTree, aHotSpots, aCombined);
//        lrView.setToolTipText(Bundle.MethodsFeatureUI_resultsMode());

        pdLabel = new GrayLabel(Bundle.MethodsFeatureUI_profilingData());

        pdSnapshotButton = new JButton(TakeSnapshotAction.getInstance());
        pdSnapshotButton.setHideActionText(true);
//        pdSnapshotButton.setText(Bundle.MethodsFeatureUI_snapshot());

        pdResetResultsButton = new JButton(ResetResultsAction.getInstance());
        pdResetResultsButton.setHideActionText(true);

        toolbar = ProfilerToolbar.create(true);

        toolbar.addSpace(2);
        toolbar.addSeparator();
        toolbar.addSpace(5);

        toolbar.add(lrLabel);
        toolbar.addSpace(2);
        toolbar.add(lrPauseButton);
        toolbar.add(lrRefreshButton);
        
        toolbar.addSpace(5);
        toolbar.add(lrDeltasButton);
        
        toolbar.addSpace(2);
//        toolbar.addSeparator();
        toolbar.addSpace(5);
        
        toolbar.add(new GrayLabel(Bundle.MethodsFeatureUI_view()));
        toolbar.addSpace(2);
        toolbar.add(forwardCalls);
        toolbar.add(hotSpots);
        toolbar.add(reverseCalls);
        
        toolbar.addSpace(5);
        toolbar.add(cpuView.createThreadSelector());

        toolbar.addSpace(2);
        toolbar.addSeparator();
        toolbar.addSpace(5);

        toolbar.add(pdLabel);
        toolbar.addSpace(2);
        toolbar.add(pdSnapshotButton);
        toolbar.addSpace(3);
        toolbar.add(pdResetResultsButton);
        
        
        // --- Sync UI ---------------------------------------------------------
        
//        setView(View.HOT_SPOTS);
        cpuView.setView(true, false, false);
        sessionStateChanged(getSessionState());
        
    }
    
    private void refreshToolbar(final int state) {
        if (toolbar != null) SwingUtilities.invokeLater(new Runnable() {
            public void run() {
//                boolean running = isRunning(state);
                boolean running = state == Profiler.PROFILING_RUNNING;
                lrPauseButton.setEnabled(running);
                lrRefreshButton.setEnabled(!popupPause && running && lrPauseButton.isSelected());
                lrDeltasButton.setEnabled(running);
            }
        });
    }

//    private void setView(View view) {
//        lrView.selectAction(view.ordinal());
//        
//        switch (view) {
//            case HOT_SPOTS:
//                cpuView.setView(false, true);
//                break;
//            case CALL_TREE:
//                cpuView.setView(true, false);
//                break;
//            case COMBINED:
//                cpuView.setView(true, true);
//                break;
//        }
//        
//        refreshResults();
//    }
    
}
