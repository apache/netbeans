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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.ui.components.ProfilerToolbar;
import org.netbeans.lib.profiler.ui.swing.PopupButton;
import org.netbeans.lib.profiler.ui.swing.SmallButton;
import org.netbeans.lib.profiler.utils.Wildcards;
import org.netbeans.modules.profiler.ResultsListener;
import org.netbeans.modules.profiler.ResultsManager;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.netbeans.modules.profiler.v2.ProfilerFeature;
import org.netbeans.modules.profiler.v2.ProfilerSession;
import org.netbeans.modules.profiler.v2.impl.WeakProcessor;
import org.netbeans.modules.profiler.v2.ui.SettingsPanel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "SQLFeature_name=SQL Queries",
    "SQLFeature_description=Display executed SQL queries, their duration and invocation paths",
    "SQLFeature_profileMethod=Profile Method",
    "SQLFeature_profileClass=Profile Class"
})
final class SQLFeature extends ProfilerFeature.Basic {
    
    private final WeakProcessor processor;
    
    private FeatureMode currentMode;
    private FeatureMode appliedMode;
    
    private SQLFeatureModes.AllQueriesMode allQueriesMode;
    private SQLFeatureModes.FilteredQueriesMode filteredQueriesMode;
    
    
    private SQLFeature(ProfilerSession session) {
        super(Icons.getIcon(ProfilerIcons.WINDOW_SQL), Bundle.SQLFeature_name(),
              Bundle.SQLFeature_description(), 20, session);
        
        Lookup.Provider project = session.getProject();
        String projectName = project == null ? "External Process" : // NOI18N
                             ProjectUtilities.getDisplayName(project);
        processor = new WeakProcessor("SQLFeature Processor for " + projectName); // NOI18N
        
        initModes();
    }
    
    
    // --- Mode ----------------------------------------------------------------
    
    private static final String MODE_FLAG = "MODE_FLAG"; // NOI18N
    
    private void initModes() {
        allQueriesMode = new SQLFeatureModes.AllQueriesMode() {
            String readFlag(String flag, String defaultValue) {
                return SQLFeature.this.readFlag(getID() + "_" + flag, defaultValue); // NOI18N
            }
            void storeFlag(String flag, String value) {
                SQLFeature.this.storeFlag(getID() + "_" + flag, value); // NOI18N
            }
            void settingsChanged() {
                SQLFeature.this.settingsChanged();
            }
        };
        
        filteredQueriesMode = new SQLFeatureModes.FilteredQueriesMode() {
            String readFlag(String flag, String defaultValue) {
                return SQLFeature.this.readFlag(getID() + "_" + flag, defaultValue); // NOI18N
            }
            void storeFlag(String flag, String value) {
                SQLFeature.this.storeFlag(getID() + "_" + flag, value); // NOI18N
            }
            void settingsChanged() {
                SQLFeature.this.settingsChanged();
            }
        };
        
        String _currentMode = readFlag(MODE_FLAG, allQueriesMode.getID());
        if (_currentMode.equals(filteredQueriesMode.getID())) currentMode = filteredQueriesMode;
        else currentMode = allQueriesMode;
        
        appliedMode = currentMode;
    }
    
    private void saveMode() {
        storeFlag(MODE_FLAG, currentMode.getID());
    }
    
    private void setMode(FeatureMode newMode) {
        if (currentMode == newMode) return;
        currentMode = newMode;
        modeChanged();
    }
    
    private void confirmMode() {
        appliedMode = currentMode;
    }
    
    private void modeChanged() {
        updateModeName();
        updateModeUI();
        configurationChanged();
        saveMode();
    }
    
    
    // --- Settings ------------------------------------------------------------
    
    public boolean supportsSettings(ProfilingSettings psettings) {
        return !ProfilingSettings.isCPUSettings(psettings) &&
               !ProfilingSettings.isMemorySettings(psettings);
    }

    public void configureSettings(ProfilingSettings psettings) {
        currentMode.configureSettings(psettings);
    }
    
    public boolean currentSettingsValid() {
        return currentMode.currentSettingsValid();
    }
    
    private void submitChanges() {
        confirmMode();
        confirmSettings();
        fireChange();
    }
    
    // Changes to current settings are pending
    private boolean pendingChanges() {
        if (appliedMode != currentMode) return true;
        return currentMode.pendingChanges();
    }
    
    // Profiling settings defined by this feature have changed
    private void configurationChanged() {
        assert isActivated();
        
        ProfilerSession session = getSession();
        
        if (!session.inProgress()) submitChanges();
        else updateApplyButton(session.getState());
    }
    
    private void confirmSettings() {
        currentMode.confirmSettings();
    }
    
    private void confirmAllSettings() {
        if (allQueriesMode != null) allQueriesMode.confirmSettings();
        if (filteredQueriesMode != null) filteredQueriesMode.confirmSettings();
    }
    
    private void settingsChanged() {
        configurationChanged();
    }
    
    private void selectionChanged() {
        configurationChanged();
    }
    
    
    // --- Settings UI ---------------------------------------------------------
    
    private static final String SETTINGS_FLAG = "SETTINGS_FLAG"; // NOI18N
    
    private JPanel settingsUI;
    private JButton modeButton;
    private JPanel settingsContainer;
    private JButton applyButton;
    
    public JPanel getSettingsUI() {
        if (settingsUI == null) {
            settingsUI = new JPanel(new GridBagLayout()) {
                public void setVisible(boolean visible) {
                    if (visible && getComponentCount() == 0) populateSettingsUI();
                    super.setVisible(visible);
                    storeFlag(SETTINGS_FLAG, visible ? Boolean.TRUE.toString() : null);
                }
                public Dimension getPreferredSize() {
                    if (getComponentCount() == 0) return new Dimension();
                    else return super.getPreferredSize();
                }
            };
            
            String _vis = readFlag(SETTINGS_FLAG, null);
            boolean vis = _vis == null ? false : Boolean.parseBoolean(_vis);
            settingsUI.setVisible(vis || currentMode != allQueriesMode);
        }
        return settingsUI;
    }
    
    private void populateSettingsUI() {
        settingsUI.setOpaque(false);
        settingsUI.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints c;
        
        JPanel profilePanel = new SettingsPanel();
        profilePanel.add(new JLabel(Bundle.ObjectsFeature_profileMode()));
        profilePanel.add(Box.createHorizontalStrut(5));
        
        // Mode button
        modeButton = new PopupButton(currentMode.getName()) {
            protected void populatePopup(JPopupMenu popup) {
                popup.add(new JRadioButtonMenuItem(allQueriesMode.getName(), currentMode == allQueriesMode) {
                    protected void fireActionPerformed(ActionEvent e) { setMode(allQueriesMode); }
                });
                popup.add(new JRadioButtonMenuItem(filteredQueriesMode.getName(), currentMode == filteredQueriesMode) {
                    protected void fireActionPerformed(ActionEvent e) { setMode(filteredQueriesMode); }
                });
            }
        };
        profilePanel.add(modeButton);
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.NORTHWEST;
        settingsUI.add(profilePanel, c);
        
        // Settings container
        settingsContainer = new JPanel(new BorderLayout());
        settingsContainer.setOpaque(false);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new Insets(0, 10, 0, 0);
        c.anchor = GridBagConstraints.NORTHWEST;
        settingsUI.add(settingsContainer, c);
        
        JPanel buttonsPanel = new SettingsPanel();
        
        final Component space = Box.createHorizontalStrut(10);
        buttonsPanel.add(space);
        
        // Apply button
        applyButton = new SmallButton(Bundle.ObjectsFeature_applyButton()) {
            protected void fireActionPerformed(ActionEvent e) {
                stopResults();
                resetResults();
                submitChanges();
                unpauseResults();
            }
            public void setVisible(boolean visible) {
                super.setVisible(visible);
                space.setVisible(visible);
            }
        };
        buttonsPanel.add(applyButton);
        
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.NORTHEAST;
        settingsUI.add(buttonsPanel, c);
        
        updateModeUI();
        updateApplyButton(getSession().getState());
    }
    
    private void updateModeName() {
        if (modeButton != null) modeButton.setText(currentMode.getName());
    }
    
    private void updateModeUI() {
        if (settingsContainer != null) {
            settingsContainer.removeAll();

            JComponent modeUI = currentMode.getUI();
            if (modeUI != null) settingsContainer.add(modeUI);
            settingsContainer.doLayout();
            settingsContainer.repaint();
        }
    }
    
    private void updateApplyButton(int state) {
        if (applyButton != null) {
            boolean visible = state != Profiler.PROFILING_INACTIVE;
            applyButton.setVisible(visible);
            if (visible) applyButton.setEnabled(currentSettingsValid() && pendingChanges());
        }
    }
    
    
    // --- Toolbar & Results UI ------------------------------------------------
    
    private SQLFeatureUI ui;
    
    public JPanel getResultsUI() {
        return getUI().getResultsUI();
    }
    
    public ProfilerToolbar getToolbar() {
        return getUI().getToolbar();
    }
    
    private SQLFeatureUI getUI() {
        if (ui == null) ui = new SQLFeatureUI() {
            void selectForProfiling(final ClientUtils.SourceCodeSelection value) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        String name = Wildcards.ALLWILDCARD.equals(value.getMethodName()) ?
                                      Bundle.SQLFeature_profileClass() :
                                      Bundle.SQLFeature_profileMethod();
                        ProfilerSession.findAndConfigure(Lookups.fixed(value), getProject(), name);
                    }
                });
            }
            Lookup.Provider getProject() {
                return SQLFeature.this.getSession().getProject();
            }
            ProfilerClient getProfilerClient() {
                Profiler profiler = SQLFeature.this.getSession().getProfiler();
                return profiler.getTargetAppRunner().getProfilerClient();
            }
            int getSessionState() {
                return SQLFeature.this.getSessionState();
            }
            void refreshResults() {
                SQLFeature.this.refreshResults();
            }
        };
        return ui;
    }
    
    
    // --- Live results --------------------------------------------------------
    
    private Runnable refresher;
    private volatile boolean running;
    
    
    private void startResults() {
        if (running) return;
        running = true;
        
        refresher = new Runnable() {
            public void run() {
                if (running) {
                    refreshView();
                    refreshResults(1500);
                }
            }
        };
        
        refreshResults(1000);
    }

    private void refreshView() {
        if (ui != null && ResultsManager.getDefault().resultsAvailable()) {
            try {
                ui.refreshData();
            } catch (ClientUtils.TargetAppOrVMTerminated ex) {
                stopResults();
            }
        }
    }
    
    private void refreshResults() {
        if (running) processor.post(new Runnable() {
            public void run() {
                if (ui != null) ui.setForceRefresh();
                refreshView();
            }
        });
    }
    
    private void refreshResults(int delay) {
        if (running && refresher != null) processor.post(refresher, delay);
    }
    
    private void resetResults() {
        if (ui != null) ui.resetData();
    }
    
    private void stopResults() {
        if (refresher != null) {
            running = false;
            refresher = null;
        }
    }
    
    private void unpauseResults() {
        if (ui != null) ui.resetPause();
    }
    
    
    // --- Session lifecycle ---------------------------------------------------
    
    private SQLResetter resetter;
    
    public void notifyActivated() {
        resetResults();
        
        resetter = Lookup.getDefault().lookup(SQLResetter.class);
        resetter.controller = this;
    }
    
    public void notifyDeactivated() {
        resetResults();
        
        if (resetter != null) {
            resetter.controller = null;
            resetter = null;
        }
        
        if (ui != null) {
            ui.cleanup();
            ui = null;
        }
    }
    
    
    protected void profilingStateChanged(int oldState, int newState) {
        if (newState == Profiler.PROFILING_INACTIVE || newState == Profiler.PROFILING_IN_TRANSITION) {
            stopResults();
            confirmAllSettings();
        } else if (isActivated() && newState == Profiler.PROFILING_RUNNING) {
            startResults();
        } else if (newState == Profiler.PROFILING_STARTED) {
            resetResults();
            unpauseResults();
        }
        
        if (ui != null) ui.sessionStateChanged(getSessionState());
        
        updateApplyButton(newState);
    }
    
    
    @ServiceProvider(service=ResultsListener.class)
    public static final class SQLResetter implements ResultsListener {
        private SQLFeature controller;
        public void resultsAvailable() { /*if (controller != null) controller.refreshView();*/ }
        public void resultsReset() { if (controller != null && controller.ui != null) controller.ui.resetData(); }
    }
    
    
    // --- Provider ------------------------------------------------------------
    
    @ServiceProvider(service=ProfilerFeature.Provider.class)
    public static final class Provider extends ProfilerFeature.Provider {
        public ProfilerFeature getFeature(ProfilerSession session) {
            //return Boolean.getBoolean("org.netbeans.modules.profiler.features.enableSQL") ? new SQLFeature(session) : null; // NOI18N
            return new SQLFeature(session);
        }
    }
    
}
