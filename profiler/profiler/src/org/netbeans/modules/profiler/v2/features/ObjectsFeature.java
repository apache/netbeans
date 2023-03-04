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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
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
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.netbeans.modules.profiler.api.ProjectUtilities;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.ProfilerIcons;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.v2.ProfilerFeature;
import org.netbeans.modules.profiler.v2.ProfilerSession;
import org.netbeans.modules.profiler.v2.impl.WeakProcessor;
import org.netbeans.modules.profiler.v2.ui.SettingsPanel;
import org.netbeans.modules.profiler.v2.ui.TitledMenuSeparator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ObjectsFeature_name=Objects",
    "ObjectsFeature_description=Profile size and count of allocated objects, including allocation paths",
    "ObjectsFeature_profileMode=Profile:",
    "ObjectsFeature_samplingModes=General (sampled)",
    "ObjectsFeature_instrModes=Focused (instrumented)",
    "ObjectsFeature_applyButton=Apply",
    "ObjectsFeature_arrayWarningCaption=Selected Array Warning",
    "#HTML-formatted, line breaks using <br> to make the displaying dialog not too wide",
    "ObjectsFeature_arrayWarningMsg=<html><b>Array object selected for profiling.</b><br><br>Configuring the target application for profiling arrays can<br>take a long time when attaching to a running process or<br>changing the settings during profiling.<br><br></html>",
    "ObjectsFeature_modeReset=<html><b>Current mode ''{0}'' is not configured properly.</b><br><br>Default ''{1}'' mode has been selected and applied instead.<br><br></html>"
})
final class ObjectsFeature extends ProfilerFeature.Basic {
    
    private final WeakProcessor processor;
    
    private FeatureMode currentMode;
    private FeatureMode appliedMode;
    
    private ObjectsFeatureModes.AllClassesMode allClassesMode;
    private ObjectsFeatureModes.ProjectClassesMode projectClassesMode;
    private ObjectsFeatureModes.SelectedClassesMode selectedClassesMode;
    private ObjectsFeatureModes.CustomClassesMode definedClassesMode;
    
    
    private ObjectsFeature(ProfilerSession session) {
        super(Icons.getIcon(ProfilerIcons.MEMORY), Bundle.ObjectsFeature_name(),
              Bundle.ObjectsFeature_description(), 13, session);
        
        assert !SwingUtilities.isEventDispatchThread();
        
        Lookup.Provider project = session.getProject();
        String projectName = project == null ? "External Process" : // NOI18N
                             ProjectUtilities.getDisplayName(project);
        processor = new WeakProcessor("ObjectsFeature Processor for " + projectName); // NOI18N

        initModes();
    }
    
    
    // --- Configuration -------------------------------------------------------
    
    public boolean supportsConfiguration(Lookup configuration) {
        if (configuration.lookup(SourceClassInfo.class) != null) return true;
        
        ClientUtils.SourceCodeSelection sel = configuration.lookup(ClientUtils.SourceCodeSelection.class);
        return sel != null && Wildcards.ALLWILDCARD.equals(sel.getMethodName());
    }
    
    public void configure(Lookup configuration) {
        // Handle Profile Class action from editor
        SourceClassInfo classInfo = configuration.lookup(SourceClassInfo.class);
        if (classInfo != null) selectClassForProfiling(classInfo);
        
        // Handle Profile Class action from snapshot
        ClientUtils.SourceCodeSelection sel = configuration.lookup(ClientUtils.SourceCodeSelection.class);
        if (sel != null && Wildcards.ALLWILDCARD.equals(sel.getMethodName())) selectForProfiling(sel);
    }
    
    
    private void selectClassForProfiling(SourceClassInfo classInfo) {
        selectForProfiling(new ClientUtils.SourceCodeSelection(classInfo.getQualifiedName(),
                                                               Wildcards.ALLWILDCARD, null));
    }
    
    private void selectForProfiling(ClientUtils.SourceCodeSelection sel) {
        selectedClassesMode.getSelection().add(sel);
    }
    
    
    // --- Mode ----------------------------------------------------------------
    
    private static final String MODE_FLAG = "MODE_FLAG"; // NOI18N
    
    private void initModes() {
        allClassesMode = new ObjectsFeatureModes.AllClassesMode() {
            String readFlag(String flag, String defaultValue) {
                return ObjectsFeature.this.readFlag(getID() + "_" + flag, defaultValue); // NOI18N
            }
            void storeFlag(String flag, String value) {
                ObjectsFeature.this.storeFlag(getID() + "_" + flag, value); // NOI18N
            }
            void settingsChanged() {
                ObjectsFeature.this.settingsChanged();
            }
        };
        
        if (getSession().getProject() != null) projectClassesMode = new ObjectsFeatureModes.ProjectClassesMode() {
            String readFlag(String flag, String defaultValue) {
                return ObjectsFeature.this.readFlag(getID() + "_" + flag, defaultValue); // NOI18N
            }
            void storeFlag(String flag, String value) {
                ObjectsFeature.this.storeFlag(getID() + "_" + flag, value); // NOI18N
            }
            void settingsChanged() {
                ObjectsFeature.this.settingsChanged();
            }
            Lookup.Provider getProject() {
                return ObjectsFeature.this.getSession().getProject();
            }
        };
        
        selectedClassesMode = new ObjectsFeatureModes.SelectedClassesMode() {
            String readFlag(String flag, String defaultValue) {
                return ObjectsFeature.this.readFlag(getID() + "_" + flag, defaultValue); // NOI18N
            }
            void storeFlag(String flag, String value) {
                ObjectsFeature.this.storeFlag(getID() + "_" + flag, value); // NOI18N
            }
            ProfilerSession getSession() {
                return ObjectsFeature.this.getSession();
            }
            void selectForProfiling(Collection<SourceClassInfo> classInfos) {
                for (SourceClassInfo classInfo : classInfos)
                    ObjectsFeature.this.selectClassForProfiling(classInfo);
            }
            void settingsChanged() {
                ObjectsFeature.this.settingsChanged();
            }
            void selectionChanging() {
                ObjectsFeature.this.setMode(this);
                ObjectsFeature.this.getSettingsUI().setVisible(true);
            }
            void selectionChanged() {
                ObjectsFeature.this.selectionChanged();
                if (ObjectsFeature.this.ui != null && ObjectsFeature.this.ui.hasResultsUI())
                    ObjectsFeature.this.ui.getResultsUI().repaint();
            }
        };
        
        if (ProfilerIDESettings.getInstance().getEnableExpertSettings()) {
            definedClassesMode = new ObjectsFeatureModes.CustomClassesMode() {
                String readFlag(String flag, String defaultValue) {
                    return ObjectsFeature.this.readFlag(getID() + "_" + flag, defaultValue); // NOI18N
                }
                void storeFlag(String flag, String value) {
                    ObjectsFeature.this.storeFlag(getID() + "_" + flag, value); // NOI18N
                }
                void settingsChanged() {
                    ObjectsFeature.this.settingsChanged();
                }
            };
        }
        
//        currentMode = allClassesMode;
        String _currentMode = readFlag(MODE_FLAG, allClassesMode.getID());
        if (projectClassesMode != null && _currentMode.equals(projectClassesMode.getID())) currentMode = projectClassesMode;
        else if (_currentMode.equals(selectedClassesMode.getID())) currentMode = selectedClassesMode;
        else if (definedClassesMode != null && _currentMode.equals(definedClassesMode.getID())) currentMode = definedClassesMode;
        else currentMode = allClassesMode;
        
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
               !ProfilingSettings.isJDBCSettings(psettings);
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
        if (allClassesMode != null) allClassesMode.confirmSettings();
        if (projectClassesMode != null) projectClassesMode.confirmSettings();
        if (selectedClassesMode != null) selectedClassesMode.confirmSettings();
        if (definedClassesMode != null) definedClassesMode.confirmSettings();
    }
    
    private void settingsChanged() {
        configurationChanged();
    }
    
    private void selectionChanged() {
        configurationChanged();
        
        if (getSession().inProgress() || getSession().isAttach()) checkArrays();
    }
    
    private void checkArrays() {
        HashSet<ClientUtils.SourceCodeSelection> sel = selectedClassesMode.getSelection();
        for (ClientUtils.SourceCodeSelection s : sel)
            if (s.getClassName().endsWith("[]")) { // NOI18N
                ProfilerDialogs.displayWarningDNSA(Bundle.ObjectsFeature_arrayWarningMsg(),
                                                   Bundle.ObjectsFeature_arrayWarningCaption(),
                                                   null, "ObjectsFeature.arraysDNSA", true); // NOI18N
                break;
            }
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
            settingsUI.setVisible(vis || currentMode != allClassesMode);
//            settingsUI.setVisible(false);
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
                popup.add(new TitledMenuSeparator(Bundle.ObjectsFeature_samplingModes()));
                popup.add(new JRadioButtonMenuItem(allClassesMode.getName(), currentMode == allClassesMode) {
                    protected void fireActionPerformed(ActionEvent e) { setMode(allClassesMode); }
                });
                if (projectClassesMode != null) popup.add(new JRadioButtonMenuItem(projectClassesMode.getName(), currentMode == projectClassesMode) {
                    protected void fireActionPerformed(ActionEvent e) { setMode(projectClassesMode); }
                });

                popup.add(new TitledMenuSeparator(Bundle.ObjectsFeature_instrModes()));
                popup.add(new JRadioButtonMenuItem(selectedClassesMode.getName(), currentMode == selectedClassesMode) {
                    protected void fireActionPerformed(ActionEvent e) { setMode(selectedClassesMode); }
                });
                
                if (definedClassesMode != null) popup.add(new JRadioButtonMenuItem(definedClassesMode.getName(), currentMode == definedClassesMode) {
                    protected void fireActionPerformed(ActionEvent e) { setMode(definedClassesMode); }
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
    
    private ObjectsFeatureUI ui;
    
    public JPanel getResultsUI() {
        return getUI().getResultsUI();
    }
    
    public ProfilerToolbar getToolbar() {
        return getUI().getToolbar();
    }
    
    private ObjectsFeatureUI getUI() {
        if (ui == null) ui = new ObjectsFeatureUI() {
            Set<ClientUtils.SourceCodeSelection> getSelection() {
                return selectedClassesMode.getSelection();
            }
            void selectForProfiling(ClientUtils.SourceCodeSelection value) {
                ObjectsFeature.this.selectForProfiling(value);
            }
            Lookup.Provider getProject() {
                return ObjectsFeature.this.getSession().getProject();
            }
            ProfilerClient getProfilerClient() {
                Profiler profiler = ObjectsFeature.this.getSession().getProfiler();
                return profiler.getTargetAppRunner().getProfilerClient();
            }
            int getSessionState() {
                return ObjectsFeature.this.getSessionState();
            }
            void refreshResults() {
                ObjectsFeature.this.refreshResults();
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
        if (ui != null && ResultsManager.getDefault().resultsAvailable()) try {
            // NOTE: might check ProfilerClient.getCurrentInstrType() here if #247827 still occurs
            ui.refreshData();
        } catch (ClientUtils.TargetAppOrVMTerminated ex) {
            stopResults();
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
    
    private ObjectsResetter resetter;
    
    public void notifyActivated() {
        resetResults();
        
        resetter = Lookup.getDefault().lookup(ObjectsResetter.class);
        resetter.controller = this;
        
        if (getSession().inProgress() && !currentMode.currentSettingsValid()) {
            final String oldMode = currentMode.getName();
            final String newMode = allClassesMode.getName();
            setMode(allClassesMode);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ProfilerDialogs.displayInfo(Bundle.MethodsFeature_modeReset(
                                                oldMode, newMode));
                }
            });
        }
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
        
        settingsUI = null;
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
    public static final class ObjectsResetter implements ResultsListener {
        private ObjectsFeature controller;
        public void resultsAvailable() { /*if (controller != null) controller.refreshView();*/ }
        public void resultsReset() { if (controller != null && controller.ui != null) controller.ui.resetData(); }
    }
    
    
    // --- Provider ------------------------------------------------------------
    
    @ServiceProvider(service=ProfilerFeature.Provider.class)
    public static final class Provider extends ProfilerFeature.Provider {
        public ProfilerFeature getFeature(ProfilerSession session) {
            return new ObjectsFeature(session);
        }
    }
    
}
