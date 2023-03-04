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

package org.netbeans.modules.profiler.options.ui;

import org.netbeans.lib.profiler.ui.components.JExtendedSpinner;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.openide.util.NbBundle;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.*;
import org.netbeans.modules.options.java.api.JavaOptions;
import org.netbeans.modules.profiler.api.JavaPlatform;
import org.netbeans.spi.options.OptionsPanelController;


/**
 * A panel used to edit the global settings of the profiler.
 *
 * @author Ian Formanek
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
//    "ProfilerOptionsPanel_UseProjectJvmText=<Use Java Platform defined in Project>",
    "ProfilerOptionsPanel_KeyOpenAlways=Always",
    "ProfilerOptionsPanel_KeyOpenMonitoring=For Monitoring Only",
    "ProfilerOptionsPanel_KeyOpenNever=Never",
    "ProfilerOptionsPanel_EngineSettingsBorderText=General",
//    "ProfilerOptionsPanel_JavaPlatformLabelText=Profiler &Java Platform:",
//    "ProfilerOptionsPanel_ManagePlatformsButtonName=&Manage Platforms...",
    "ProfilerOptionsPanel_CommPortLabelText=Communication &Port:",
    "ProfilerOptionsPanel_WindowsSettingsBorderText=When Profiling Session Starts",
    "ProfilerOptionsPanel_TelemetryOverviewLabelText=Open Telemetry &Overview:",
    "ProfilerOptionsPanel_ThreadsViewLabelText=Open Thre&ads View:",
    "ProfilerOptionsPanel_ThreadsViewHintText=Threads View is opened only when Threads Monitoring is enabled",
    "ProfilerOptionsPanel_LocksViewLabelText=Open &Lock Contention View:",
    "ProfilerOptionsPanel_LocksViewHintText=Lock Contention View is opened only when Lock Contention monitoring is enabled",
    "ProfilerOptionsPanel_LiveResultsLabelText=Open Live Results For:",
    "ProfilerOptionsPanel_CpuChckBoxText=&CPU",
    "ProfilerOptionsPanel_MemoryChckBoxText=M&emory",
    "ProfilerOptionsPanel_SnapshotsSettingsBorderText=Miscellaneous",
    "ProfilerOptionsPanel_OpenSnapshotRadioText=Open New Snapshot",
    "ProfilerOptionsPanel_SaveSnapshotRadioText=Save New Snapshot",
    "ProfilerOptionsPanel_OpenSaveSnapshotRadioText=Open and Save New Snapshot",
    "ProfilerOptionsPanel_ResetHintText=Click the Reset button to reset state of Do Not Show Again confirmations in all Profiler dialogs:",
    "ProfilerOptionsPanel_ResetButtonName=&Reset",
    "ProfilerOptionsPanel_PortNoSpinnerAccessDescr=Defines port used for communication with Profiler agent.",
    "ProfilerOptionsPanel_CpuLiveResultsCheckboxAccessDescr=CPU live results window will be opened automatically.",
    "ProfilerOptionsPanel_MemoryLiveResultsCheckboxAccessDescr=Memory live results window will be opened automatically.",
    "ProfilerOptionsPanel_TelemetryOverviewComboAccessDescr=Policy for opening Telemetry window when profiling session starts.",
    "ProfilerOptionsPanel_ThreadsViewComboAccessDescr=Policy for opening Threads view window when profiling session starts.",
    "ProfilerOptionsPanel_LocksViewComboAccessDescr=Policy for opening Lock Contention view window when profiling session starts.",
    "ProfilerOptionsPanel_OomeBorderText=On O&utOfMemoryError:",
    "ProfilerOptionsPanel_OomeNothingText=Do nothing",
    "ProfilerOptionsPanel_OomeProjectText=Save heap dump to profiled project",
    "ProfilerOptionsPanel_OomeTempText=Save heap dump to temporary directory",
    "ProfilerOptionsPanel_OomeCustomText=Save heap dump to:",
    "ProfilerOptionsPanel_OomeCustomAccessDescr=Save heap dump to custom directory",
    "ProfilerOptionsPanel_OomeCustomTextfieldAccessDescr=Directory where heap dumps will be saved",
    "ProfilerOptionsPanel_OomeCustomButtonAccessName=Choose directory",
    "ProfilerOptionsPanel_ChooseDumpDirCaption=Choose Heap Dump Directory",
    "ProfilerOptionsPanel_EnableAnalysisCheckbox=E&nable Rule-Based Heap Analysis",
    "ProfilerOptionsPanel_TakingSnapshotLabelText=When taking &snapshot:",
    "ProfilerOptionsPanel_TakingSnapshotComboAccessDescr=Specifies what to do when taking the snapshot",
    "ProfilerOptionsPanel_OomeComboAccessDescr=Specifies action on OutOfMemoryError",
    "ProfilerOptionsPanel_HeapWalkerLabelText=HeapWalker:",
    "ProfilerOptionsPanel_JavaPlatformComboAccessDescr=Java platform used for running the profiled application",
    "ProfilerOptionsPanel_IfThreadsMonitoringEnabledHint=Vaild if threads monitoring is enabled",
    "ProfilerOptionsPanel_IfLockContentionMonitoringEnabledHint=Valid if lock contention monitoring is enabled",
    "ProfilerOptionsPanel_KW_profiler=profiler",
    "ProfilerOptionsPanel_KW_profile=profile",
    "ProfilerOptionsPanel_KW_profiling=profiling",
    "ProfilerOptionsPanel_KW_cpu=cpu",
    "ProfilerOptionsPanel_KW_memory=memory",
    "ProfilerOptionsPanel_KW_threads=threads",
    "ProfilerOptionsPanel_KW_telemetry=telemetry"
})
@OptionsPanelController.Keywords(keywords={"#ProfilerOptionsPanel_KW_profiler",
    "#ProfilerOptionsPanel_KW_profile", "#ProfilerOptionsPanel_KW_profiling",
    "#ProfilerOptionsPanel_KW_cpu", "#ProfilerOptionsPanel_KW_memory",
    "#ProfilerOptionsPanel_KW_threads", "#ProfilerOptionsPanel_KW_telemetry"},
        location=JavaOptions.JAVA, tabTitle="org.netbeans.modules.profiler.options.Bundle#ProfilerOptionsCategory_Title")
public final class ProfilerOptionsPanel extends JPanel implements ActionListener {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class CategorySeparator extends JPanel {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private JLabel captionLabel;
        private JSeparator captionSeparator;
        private boolean addSpaceBefore;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public CategorySeparator(String caption, boolean addSpaceBefore) {
            this.addSpaceBefore = addSpaceBefore;
            initComponents();
            captionLabel.setText(caption);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        private void initComponents() {
            setLayout(new GridBagLayout());

            GridBagConstraints constraints;
            if (addSpaceBefore) {
                JLabel emptyLabel = new JLabel(" ");
                constraints = new GridBagConstraints();
                constraints.gridx = 0;
                constraints.gridy = 0;
                constraints.anchor = GridBagConstraints.WEST;
                constraints.fill = GridBagConstraints.BOTH;
                constraints.insets = new Insets(0, 0, 0, 0);
                add(emptyLabel, constraints);
            }

            // captionLabel
            captionLabel = new JLabel();
            constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = addSpaceBefore ? 1 : 0;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.fill = GridBagConstraints.NONE;
            constraints.insets = new Insets(0, 0, 0, 0);
            add(captionLabel, constraints);

            // captionSeparator
            captionSeparator = new JSeparator();
            constraints = new GridBagConstraints();
            constraints.gridx = 1;
            constraints.gridy = addSpaceBefore ? 1 : 0;
            constraints.weightx = 1;
            constraints.weighty = 1;
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.insets = new Insets(0, 4, 0, 0);
            add(captionSeparator, constraints);
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

//    private ArrayList supportedJavaPlatforms = new ArrayList();
//    private ArrayList supportedJavaPlatformsNames = new ArrayList();
//    private JButton managePlatformsButton;
    private JButton oomeDetectionChooseDirButton;
    private JButton resetConfirmationsButton;
    private JCheckBox cpuLiveResultsCheckbox;
    private JCheckBox enableHeapWalkerAnalysisCheckbox;
    private JCheckBox memoryLiveResultsCheckbox;
//    private JComboBox javaPlatformCombo;
    private JComboBox oomeCombo;
    private JComboBox openThreadsViewCombo;
    private JLabel onlyThreadsEnabledLabel;
    private JComboBox openLocksViewCombo;
    private JLabel onlyContentionEnabledLabel;
    private JComboBox takingSnapshotCombo;
    private JComboBox telemetryOverviewCombo;
    private JExtendedSpinner portNoSpinner;
    private JTextField oomeDetectionDirTextField;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ProfilerOptionsPanel() {
        initComponents();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public JavaPlatform getSelectedJavaPlatform() {
        return null;
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == resetConfirmationsButton) {
            ProfilerIDESettings.getInstance().clearDoNotShowAgainMap();
            resetConfirmationsButton.setEnabled(false);
//        } else if (e.getSource() == managePlatformsButton) {
//            JavaPlatform platform = getSelectedJavaPlatform();
//            JavaPlatform.showCustomizer(/* FIXX platform */ );
//            updateJavaPlatformComboItems();
        } else if (e.getSource() == oomeDetectionChooseDirButton) {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File(oomeDetectionDirTextField.getText()));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setMultiSelectionEnabled(false);
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setDialogTitle(Bundle.ProfilerOptionsPanel_ChooseDumpDirCaption());

            if (chooser.showOpenDialog(SwingUtilities.getRoot(this)) == JFileChooser.APPROVE_OPTION) {
                oomeDetectionDirTextField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

    public void applySettings(ProfilerIDESettings pis) {
        // GlobalProfilingSettings
        pis.setPortNo(((Number) portNoSpinner.getValue()).intValue());

//        JavaPlatform sel = getSelectedJavaPlatform();
//
//        pis.setJavaPlatformForProfiling((sel == null) ? null : sel.getDisplayName());

        // ProfilerIDESettings
        pis.setDisplayLiveResultsCPU(cpuLiveResultsCheckbox.isSelected());
        pis.setDisplayLiveResultsMemory(memoryLiveResultsCheckbox.isSelected());

        Object takingSnapshotSelected = takingSnapshotCombo.getSelectedItem();
        pis.setAutoOpenSnapshot((Bundle.ProfilerOptionsPanel_OpenSnapshotRadioText().equals(takingSnapshotSelected))
                                || (Bundle.ProfilerOptionsPanel_OpenSaveSnapshotRadioText().equals(takingSnapshotSelected)));
        pis.setAutoSaveSnapshot((Bundle.ProfilerOptionsPanel_SaveSnapshotRadioText().equals(takingSnapshotSelected))
                                || (Bundle.ProfilerOptionsPanel_OpenSaveSnapshotRadioText().equals(takingSnapshotSelected)));

        Object oomeSelected = oomeCombo.getSelectedItem();

        if (Bundle.ProfilerOptionsPanel_OomeNothingText().equals(oomeSelected)) {
            pis.setOOMDetectionMode(ProfilerIDESettings.OOME_DETECTION_NONE);
        } else if (Bundle.ProfilerOptionsPanel_OomeProjectText().equals(oomeSelected)) {
            pis.setOOMDetectionMode(ProfilerIDESettings.OOME_DETECTION_PROJECTDIR);
        } else if (Bundle.ProfilerOptionsPanel_OomeTempText().equals(oomeSelected)) {
            pis.setOOMDetectionMode(ProfilerIDESettings.OOME_DETECTION_TEMPDIR);
        } else if (Bundle.ProfilerOptionsPanel_OomeCustomText().equals(oomeSelected)) {
            pis.setOOMDetectionMode(ProfilerIDESettings.OOME_DETECTION_CUSTOMDIR);
        }

        pis.setCustomHeapdumpPath(oomeDetectionDirTextField.getText());

        pis.setHeapWalkerAnalysisEnabled(enableHeapWalkerAnalysisCheckbox.isSelected());

        if (Bundle.ProfilerOptionsPanel_KeyOpenAlways().equals(telemetryOverviewCombo.getSelectedItem())) {
            pis.setTelemetryOverviewBehavior(ProfilerIDESettings.OPEN_ALWAYS);
        } else if (Bundle.ProfilerOptionsPanel_KeyOpenMonitoring().equals(telemetryOverviewCombo.getSelectedItem())) {
            pis.setTelemetryOverviewBehavior(ProfilerIDESettings.OPEN_MONITORING);
        } else {
            pis.setTelemetryOverviewBehavior(ProfilerIDESettings.OPEN_NEVER);
        }

        if (Bundle.ProfilerOptionsPanel_KeyOpenAlways().equals(openThreadsViewCombo.getSelectedItem())) {
            pis.setThreadsViewBehavior(ProfilerIDESettings.OPEN_ALWAYS);
        } else if (Bundle.ProfilerOptionsPanel_KeyOpenMonitoring().equals(openThreadsViewCombo.getSelectedItem())) {
            pis.setThreadsViewBehavior(ProfilerIDESettings.OPEN_MONITORING);
        } else {
            pis.setThreadsViewBehavior(ProfilerIDESettings.OPEN_NEVER);
        }
        
        if (Bundle.ProfilerOptionsPanel_KeyOpenAlways().equals(openLocksViewCombo.getSelectedItem())) {
            pis.setLockContentionViewBehavior(ProfilerIDESettings.OPEN_ALWAYS);
        } else if (Bundle.ProfilerOptionsPanel_KeyOpenMonitoring().equals(openLocksViewCombo.getSelectedItem())) {
            pis.setLockContentionViewBehavior(ProfilerIDESettings.OPEN_MONITORING);
        } else {
            pis.setLockContentionViewBehavior(ProfilerIDESettings.OPEN_NEVER);
        }
    }

    public boolean currentSettingsEquals(ProfilerIDESettings settings) {
        if (((Number) portNoSpinner.getValue()).intValue() != settings.getPortNo()) {
            return false;
        }

        if (cpuLiveResultsCheckbox.isSelected() != settings.getDisplayLiveResultsCPU()) {
            return false;
        }

        if (memoryLiveResultsCheckbox.isSelected() != settings.getDisplayLiveResultsMemory()) {
            return false;
        }

        if (settings.getAutoOpenSnapshot() && settings.getAutoSaveSnapshot()
                && (!Bundle.ProfilerOptionsPanel_OpenSaveSnapshotRadioText().equals(takingSnapshotCombo.getSelectedItem()))) {
            return false;
        }

        if (settings.getAutoOpenSnapshot() && (!Bundle.ProfilerOptionsPanel_OpenSnapshotRadioText().equals(takingSnapshotCombo.getSelectedItem()))) {
            return false;
        }

        if (settings.getAutoSaveSnapshot() && (!Bundle.ProfilerOptionsPanel_SaveSnapshotRadioText().equals(takingSnapshotCombo.getSelectedItem()))) {
            return false;
        }

        if ((settings.getOOMDetectionMode() == ProfilerIDESettings.OOME_DETECTION_NONE)
                && (!Bundle.ProfilerOptionsPanel_OomeNothingText().equals(oomeCombo.getSelectedItem()))) {
            return false;
        }

        if ((settings.getOOMDetectionMode() == ProfilerIDESettings.OOME_DETECTION_PROJECTDIR)
                && (!Bundle.ProfilerOptionsPanel_OomeProjectText().equals(oomeCombo.getSelectedItem()))) {
            return false;
        }

        if ((settings.getOOMDetectionMode() == ProfilerIDESettings.OOME_DETECTION_TEMPDIR)
                && (!Bundle.ProfilerOptionsPanel_OomeTempText().equals(oomeCombo.getSelectedItem()))) {
            return false;
        }

        if ((settings.getOOMDetectionMode() == ProfilerIDESettings.OOME_DETECTION_CUSTOMDIR)
                && (!Bundle.ProfilerOptionsPanel_OomeCustomText().equals(oomeCombo.getSelectedItem()))) {
            return false;
        }

        if (!oomeDetectionDirTextField.getText().equals(settings.getCustomHeapdumpPath())) {
            return false;
        }

        if (Bundle.ProfilerOptionsPanel_KeyOpenAlways().equals(telemetryOverviewCombo.getSelectedItem())) {
            if (settings.getTelemetryOverviewBehavior() != ProfilerIDESettings.OPEN_ALWAYS) {
                return false;
            }
        } else if (Bundle.ProfilerOptionsPanel_KeyOpenMonitoring().equals(telemetryOverviewCombo.getSelectedItem())) {
            if (settings.getTelemetryOverviewBehavior() != ProfilerIDESettings.OPEN_MONITORING) {
                return false;
            }
        } else if (Bundle.ProfilerOptionsPanel_KeyOpenNever().equals(telemetryOverviewCombo.getSelectedItem())) {
            if (settings.getTelemetryOverviewBehavior() != ProfilerIDESettings.OPEN_NEVER) {
                return false;
            }
        }

        if (Bundle.ProfilerOptionsPanel_KeyOpenAlways().equals(openThreadsViewCombo.getSelectedItem())) {
            if (settings.getThreadsViewBehavior() != ProfilerIDESettings.OPEN_ALWAYS) {
                return false;
            }
        } else if (Bundle.ProfilerOptionsPanel_KeyOpenMonitoring().equals(openThreadsViewCombo.getSelectedItem())) {
            if (settings.getThreadsViewBehavior() != ProfilerIDESettings.OPEN_MONITORING) {
                return false;
            }
        } else if (Bundle.ProfilerOptionsPanel_KeyOpenNever().equals(openThreadsViewCombo.getSelectedItem())) {
            if (settings.getThreadsViewBehavior() != ProfilerIDESettings.OPEN_NEVER) {
                return false;
            }
        }
        
        if (Bundle.ProfilerOptionsPanel_KeyOpenAlways().equals(openLocksViewCombo.getSelectedItem())) {
            if (settings.getLockContentionViewBehavior() != ProfilerIDESettings.OPEN_ALWAYS) {
                return false;
            }
        } else if (Bundle.ProfilerOptionsPanel_KeyOpenMonitoring().equals(openLocksViewCombo.getSelectedItem())) {
            if (settings.getLockContentionViewBehavior() != ProfilerIDESettings.OPEN_MONITORING) {
                return false;
            }
        } else if (Bundle.ProfilerOptionsPanel_KeyOpenNever().equals(openLocksViewCombo.getSelectedItem())) {
            if (settings.getLockContentionViewBehavior() != ProfilerIDESettings.OPEN_NEVER) {
                return false;
            }
        }

//        JavaPlatform sel = getSelectedJavaPlatform();
//
//        if (sel == null) {
//            if (settings.getJavaPlatformForProfiling() != null) {
//                return false;
//            }
//        } else {
//            if (!sel.getDisplayName().equals(settings.getJavaPlatformForProfiling())) {
//                return false;
//            }
//        }

        if (settings.getHeapWalkerAnalysisEnabled() != enableHeapWalkerAnalysisCheckbox.isSelected()) {
            return false;
        }

        return true;
    }

    public void init(ProfilerIDESettings pis) {
        resetConfirmationsButton.setEnabled(true);
//        updateJavaPlatformComboItems();

        // GlobalProfilingSettings
        portNoSpinner.setValue(Integer.valueOf(pis.getPortNo()));

//        if (pis.getJavaPlatformForProfiling() != null) {
//            javaPlatformCombo.setSelectedItem(pis.getJavaPlatformForProfiling());
//        } else {
//            javaPlatformCombo.setSelectedIndex(0);
//        }

        // ProfilerIDESettings
        cpuLiveResultsCheckbox.setSelected(pis.getDisplayLiveResultsCPU());
        memoryLiveResultsCheckbox.setSelected(pis.getDisplayLiveResultsMemory());

        if (pis.getAutoOpenSnapshot() && pis.getAutoSaveSnapshot()) {
            takingSnapshotCombo.setSelectedItem(Bundle.ProfilerOptionsPanel_OpenSaveSnapshotRadioText());
        } else if (pis.getAutoOpenSnapshot()) {
            takingSnapshotCombo.setSelectedItem(Bundle.ProfilerOptionsPanel_OpenSnapshotRadioText());
        } else if (pis.getAutoSaveSnapshot()) {
            takingSnapshotCombo.setSelectedItem(Bundle.ProfilerOptionsPanel_SaveSnapshotRadioText());
        }

        if (pis.getOOMDetectionMode() == ProfilerIDESettings.OOME_DETECTION_NONE) {
            oomeCombo.setSelectedItem(Bundle.ProfilerOptionsPanel_OomeNothingText());
        } else if (pis.getOOMDetectionMode() == ProfilerIDESettings.OOME_DETECTION_PROJECTDIR) {
            oomeCombo.setSelectedItem(Bundle.ProfilerOptionsPanel_OomeProjectText());
        } else if (pis.getOOMDetectionMode() == ProfilerIDESettings.OOME_DETECTION_TEMPDIR) {
            oomeCombo.setSelectedItem(Bundle.ProfilerOptionsPanel_OomeTempText());
        } else if (pis.getOOMDetectionMode() == ProfilerIDESettings.OOME_DETECTION_CUSTOMDIR) {
            oomeCombo.setSelectedItem(Bundle.ProfilerOptionsPanel_OomeCustomText());
        }

        oomeDetectionDirTextField.setText(pis.getCustomHeapdumpPath());

        enableHeapWalkerAnalysisCheckbox.setSelected(pis.getHeapWalkerAnalysisEnabled());

        switch (pis.getTelemetryOverviewBehavior()) {
            case ProfilerIDESettings.OPEN_ALWAYS:
                telemetryOverviewCombo.setSelectedItem(Bundle.ProfilerOptionsPanel_KeyOpenAlways());

                break;
            case ProfilerIDESettings.OPEN_MONITORING:
                telemetryOverviewCombo.setSelectedItem(Bundle.ProfilerOptionsPanel_KeyOpenMonitoring());

                break;
            default:
                telemetryOverviewCombo.setSelectedItem(Bundle.ProfilerOptionsPanel_KeyOpenNever());

                break;
        }

        switch (pis.getThreadsViewBehavior()) {
            case ProfilerIDESettings.OPEN_ALWAYS:
                openThreadsViewCombo.setSelectedItem(Bundle.ProfilerOptionsPanel_KeyOpenAlways());

                break;
            case ProfilerIDESettings.OPEN_MONITORING:
                openThreadsViewCombo.setSelectedItem(Bundle.ProfilerOptionsPanel_KeyOpenMonitoring());

                break;
            default:
                openThreadsViewCombo.setSelectedItem(Bundle.ProfilerOptionsPanel_KeyOpenNever());

                break;
        }
        
        switch (pis.getLockContentionViewBehavior()) {
            case ProfilerIDESettings.OPEN_ALWAYS:
                openLocksViewCombo.setSelectedItem(Bundle.ProfilerOptionsPanel_KeyOpenAlways());

                break;
            case ProfilerIDESettings.OPEN_MONITORING:
                openLocksViewCombo.setSelectedItem(Bundle.ProfilerOptionsPanel_KeyOpenMonitoring());

                break;
            default:
                openLocksViewCombo.setSelectedItem(Bundle.ProfilerOptionsPanel_KeyOpenNever());

                break;
        }

        updateEnabling();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints;
        ButtonGroup oomeRadiosGroup = new ButtonGroup();

        // --- General -------------------------------------------------------------

        // General caption
        CategorySeparator generalSeparator = new CategorySeparator(Bundle.ProfilerOptionsPanel_EngineSettingsBorderText(), false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(2, 0, 0, 6);
        add(generalSeparator, gridBagConstraints);

//        // javaPlatformLabel
//        JLabel javaPlatformLabel = new JLabel();
//        org.openide.awt.Mnemonics.setLocalizedText(javaPlatformLabel, Bundle.ProfilerOptionsPanel_JavaPlatformLabelText());
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.insets = new Insets(5, 10, 0, 5);
//        gridBagConstraints.anchor = GridBagConstraints.WEST;
//        add(javaPlatformLabel, gridBagConstraints);
//
//        // javaPlatformCombo
//        javaPlatformCombo = new JComboBox() {
//                public Dimension getMinimumSize() {
//                    return getPreferredSize();
//                }
//            };
//        javaPlatformCombo.getAccessibleContext().setAccessibleDescription(Bundle.ProfilerOptionsPanel_JavaPlatformComboAccessDescr());
//        javaPlatformLabel.setLabelFor(javaPlatformCombo);
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.gridwidth = 2;
//        gridBagConstraints.insets = new Insets(5, 10, 0, 5);
//        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.anchor = GridBagConstraints.WEST;
//        add(javaPlatformCombo, gridBagConstraints);
//
//        // managePlatformsButton
//        managePlatformsButton = new JButton();
//        org.openide.awt.Mnemonics.setLocalizedText(managePlatformsButton, Bundle.ProfilerOptionsPanel_ManagePlatformsButtonName());
//        managePlatformsButton.getAccessibleContext().setAccessibleDescription(Bundle.ProfilerOptionsPanel_ManagePlatformsButtonName());
//        managePlatformsButton.addActionListener(this);
//        gridBagConstraints = new java.awt.GridBagConstraints();
//        gridBagConstraints.gridx = 3;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.insets = new java.awt.Insets(5, 3, 0, 6);
//        gridBagConstraints.fill = GridBagConstraints.NONE;
//        gridBagConstraints.anchor = GridBagConstraints.WEST;
//        add(managePlatformsButton, gridBagConstraints);

        // portNoLabel
        JLabel portNoLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(portNoLabel, Bundle.ProfilerOptionsPanel_CommPortLabelText());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(5, 10, 0, 5);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(portNoLabel, gridBagConstraints);

        // portNoSpinner
        portNoSpinner = new JExtendedSpinner() {
                public Dimension getPreferredSize() {
                    return new Dimension(super.getPreferredSize().width,
                                         getDefaultSpinnerHeight());
                }

                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        portNoLabel.setLabelFor(portNoSpinner);

        if (portNoSpinner.getAccessibleContext() != null) {
            portNoSpinner.getAccessibleContext().setAccessibleDescription(Bundle.ProfilerOptionsPanel_PortNoSpinnerAccessDescr());
        }

        portNoSpinner.fixAccessibility();
        portNoSpinner.setModel(new SpinnerNumberModel(5140, 1, 65535, 1));
        portNoSpinner.setPreferredSize(new Dimension(portNoSpinner.getPreferredSize().width,
                                                     new JComboBox().getPreferredSize().height));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(5, 10, 0, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(portNoSpinner, gridBagConstraints);

        // --- When Profiling Starts -----------------------------------------------

        // Profiling Start caption
        CategorySeparator profilingStartSeparator = new CategorySeparator(Bundle.ProfilerOptionsPanel_WindowsSettingsBorderText(), true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(7, 0, 0, 6);
        add(profilingStartSeparator, gridBagConstraints);

        // telemetryOverviewLabel
        JLabel telemetryOverviewLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(telemetryOverviewLabel, Bundle.ProfilerOptionsPanel_TelemetryOverviewLabelText());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new Insets(5, 10, 0, 5);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(telemetryOverviewLabel, gridBagConstraints);

        // telemetryOverviewCombo
        telemetryOverviewCombo = new JComboBox() {
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        telemetryOverviewLabel.setLabelFor(telemetryOverviewCombo);
        telemetryOverviewCombo.getAccessibleContext().setAccessibleDescription(Bundle.ProfilerOptionsPanel_TelemetryOverviewComboAccessDescr());
        telemetryOverviewCombo.setModel(new DefaultComboBoxModel(new String[] { 
            Bundle.ProfilerOptionsPanel_KeyOpenAlways(), 
            Bundle.ProfilerOptionsPanel_KeyOpenMonitoring(), 
            Bundle.ProfilerOptionsPanel_KeyOpenNever() 
        }));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(5, 10, 0, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(telemetryOverviewCombo, gridBagConstraints);

        // openThreadsViewLabel
        JLabel openThreadsViewLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(openThreadsViewLabel, Bundle.ProfilerOptionsPanel_ThreadsViewLabelText());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new Insets(5, 10, 0, 5);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(openThreadsViewLabel, gridBagConstraints);

        // openThreadsViewCombo
        openThreadsViewCombo = new JComboBox() {
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
                protected void fireActionEvent() {
                    onlyThreadsEnabledLabel.setVisible(
                            !Bundle.ProfilerOptionsPanel_KeyOpenNever().equals(getSelectedItem()));
                }
            };
        openThreadsViewLabel.setLabelFor(openThreadsViewCombo);
        openThreadsViewCombo.getAccessibleContext()
                            .setAccessibleDescription(Bundle.ProfilerOptionsPanel_ThreadsViewComboAccessDescr() + Bundle.ProfilerOptionsPanel_ThreadsViewHintText());
        openThreadsViewCombo.setModel(new DefaultComboBoxModel(new String[] { 
            Bundle.ProfilerOptionsPanel_KeyOpenAlways(), 
            Bundle.ProfilerOptionsPanel_KeyOpenMonitoring(), 
            Bundle.ProfilerOptionsPanel_KeyOpenNever()
        }));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(5, 10, 0, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(openThreadsViewCombo, gridBagConstraints);

        onlyThreadsEnabledLabel = new JLabel(Bundle.ProfilerOptionsPanel_IfThreadsMonitoringEnabledHint());
        onlyThreadsEnabledLabel.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.insets = new Insets(5, 0, 0, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(onlyThreadsEnabledLabel, gridBagConstraints);
        
        // openLocksViewLabel
        JLabel openLocksViewLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(openLocksViewLabel, Bundle.ProfilerOptionsPanel_LocksViewLabelText());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new Insets(5, 10, 0, 5);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(openLocksViewLabel, gridBagConstraints);
        
        // openLocksViewCombo
        openLocksViewCombo = new JComboBox() {
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
                protected void fireActionEvent() {
                    onlyContentionEnabledLabel.setVisible(
                            !Bundle.ProfilerOptionsPanel_KeyOpenNever().equals(getSelectedItem()));
                }
            };
        openLocksViewLabel.setLabelFor(openLocksViewCombo);
        openLocksViewCombo.getAccessibleContext()
                            .setAccessibleDescription(Bundle.ProfilerOptionsPanel_LocksViewComboAccessDescr() + Bundle.ProfilerOptionsPanel_LocksViewHintText());
        openLocksViewCombo.setModel(new DefaultComboBoxModel(new String[] { 
            Bundle.ProfilerOptionsPanel_KeyOpenAlways(), 
            Bundle.ProfilerOptionsPanel_KeyOpenMonitoring(), 
            Bundle.ProfilerOptionsPanel_KeyOpenNever()
        }));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(5, 10, 0, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(openLocksViewCombo, gridBagConstraints);

        onlyContentionEnabledLabel = new JLabel(Bundle.ProfilerOptionsPanel_IfLockContentionMonitoringEnabledHint());
        onlyContentionEnabledLabel.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.insets = new Insets(5, 0, 0, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(onlyContentionEnabledLabel, gridBagConstraints);
        
        int maxWidth = Math.max(telemetryOverviewCombo.getPreferredSize().width, openThreadsViewCombo.getPreferredSize().width);
        maxWidth = Math.max(maxWidth, openLocksViewCombo.getPreferredSize().width) + 15;
        int maxHeight = Math.max(telemetryOverviewCombo.getPreferredSize().height, openThreadsViewCombo.getPreferredSize().height);
        maxHeight = Math.max(maxHeight, openLocksViewCombo.getPreferredSize().height);
        telemetryOverviewCombo.setPreferredSize(new Dimension(maxWidth, maxHeight));
        openThreadsViewCombo.setPreferredSize(new Dimension(maxWidth, maxHeight));
        openLocksViewCombo.setPreferredSize(new Dimension(maxWidth, maxHeight));

        // liveResultsLabel
        JLabel liveResultsLabel = new JLabel(Bundle.ProfilerOptionsPanel_LiveResultsLabelText());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.insets = new Insets(5, 10, 0, 5);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(liveResultsLabel, gridBagConstraints);

        // liveResultsPanel
        JPanel liveResultsPanel = new JPanel();
        liveResultsPanel.setLayout(new GridLayout(1, 2, 0, 0));

        // cpuLiveResultsCheckbox
        cpuLiveResultsCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(cpuLiveResultsCheckbox, Bundle.ProfilerOptionsPanel_CpuChckBoxText());
        cpuLiveResultsCheckbox.getAccessibleContext().setAccessibleDescription(Bundle.ProfilerOptionsPanel_CpuLiveResultsCheckboxAccessDescr());
        liveResultsPanel.add(cpuLiveResultsCheckbox);

        // memoryLiveResultsCheckbox
        memoryLiveResultsCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(memoryLiveResultsCheckbox, Bundle.ProfilerOptionsPanel_MemoryChckBoxText());
        memoryLiveResultsCheckbox.getAccessibleContext().setAccessibleDescription(Bundle.ProfilerOptionsPanel_MemoryLiveResultsCheckboxAccessDescr());
        liveResultsPanel.add(memoryLiveResultsCheckbox);

        // liveResultsLabel placing
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new Insets(5, 10, 0, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(liveResultsPanel, gridBagConstraints);

        // --- Miscellaneous -------------------------------------------------------

        // Miscellaneous caption
        CategorySeparator miscellaneousSeparator = new CategorySeparator(Bundle.ProfilerOptionsPanel_SnapshotsSettingsBorderText(), true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(5, 0, 0, 6);
        add(miscellaneousSeparator, gridBagConstraints);

        // takingSnapshotLabel
        JLabel takingSnapshotLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(takingSnapshotLabel, Bundle.ProfilerOptionsPanel_TakingSnapshotLabelText());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.insets = new Insets(5, 10, 0, 5);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(takingSnapshotLabel, gridBagConstraints);

        // takingSnapshotCombo
        takingSnapshotCombo = new JComboBox() {
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        takingSnapshotLabel.setLabelFor(takingSnapshotCombo);
        takingSnapshotCombo.getAccessibleContext().setAccessibleDescription(Bundle.ProfilerOptionsPanel_TakingSnapshotComboAccessDescr());
        takingSnapshotCombo.setModel(new DefaultComboBoxModel(new String[] {
                                                                  Bundle.ProfilerOptionsPanel_OpenSnapshotRadioText(), 
                                                                  Bundle.ProfilerOptionsPanel_SaveSnapshotRadioText(),
                                                                  Bundle.ProfilerOptionsPanel_OpenSaveSnapshotRadioText()
                                                              }));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new Insets(5, 10, 0, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(takingSnapshotCombo, gridBagConstraints);

        // oomeDetectionLabel
        JLabel oomeDetectionLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(oomeDetectionLabel, Bundle.ProfilerOptionsPanel_OomeBorderText());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.insets = new Insets(5, 10, 0, 5);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(oomeDetectionLabel, gridBagConstraints);

        // oomeCombo
        oomeCombo = new JComboBox() {
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        oomeDetectionLabel.setLabelFor(oomeCombo);
        oomeCombo.getAccessibleContext().setAccessibleDescription(Bundle.ProfilerOptionsPanel_OomeComboAccessDescr());
        oomeCombo.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    updateEnabling();
                }
            });
        oomeCombo.setModel(new DefaultComboBoxModel(new String[] {
                                                        Bundle.ProfilerOptionsPanel_OomeNothingText(), 
                                                        Bundle.ProfilerOptionsPanel_OomeProjectText(), 
                                                        Bundle.ProfilerOptionsPanel_OomeTempText(), 
                                                        Bundle.ProfilerOptionsPanel_OomeCustomText()
                                                    }));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new Insets(5, 10, 0, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(oomeCombo, gridBagConstraints);

        int maxWidth1 = Math.max(takingSnapshotCombo.getPreferredSize().width, oomeCombo.getPreferredSize().width);
        int maxHeight1 = Math.max(takingSnapshotCombo.getPreferredSize().height, oomeCombo.getPreferredSize().height);
        takingSnapshotCombo.setPreferredSize(new Dimension(maxWidth1, maxHeight1));
        oomeCombo.setPreferredSize(new Dimension(maxWidth1, maxHeight1));

        // oomeDetectionPanel
        JPanel oomeDetectionPanel = new JPanel(new GridBagLayout());

        // oomeDetectionDirTextField
        oomeDetectionDirTextField = new JTextField() {
                public Dimension getPreferredSize() {
                    return new Dimension(super.getPreferredSize().width, oomeDetectionChooseDirButton.getPreferredSize().height);
                }

                public Dimension getMinimumSize() {
                    return new Dimension(super.getMinimumSize().width, getPreferredSize().height);
                }
            };
        oomeDetectionDirTextField.getAccessibleContext().setAccessibleName(Bundle.ProfilerOptionsPanel_OomeCustomAccessDescr());
        oomeDetectionDirTextField.getAccessibleContext().setAccessibleDescription(Bundle.ProfilerOptionsPanel_OomeCustomTextfieldAccessDescr());
        oomeDetectionDirTextField.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.insets = new Insets(0, 0, 0, 5);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        oomeDetectionPanel.add(oomeDetectionDirTextField, gridBagConstraints);

        // oomeDetectionChooseDirButton
        oomeDetectionChooseDirButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(oomeDetectionChooseDirButton, "&..."); // NOI18N
        oomeDetectionChooseDirButton.getAccessibleContext().setAccessibleName(Bundle.ProfilerOptionsPanel_OomeCustomButtonAccessName());
        oomeDetectionChooseDirButton.addActionListener(this);
        oomeDetectionChooseDirButton.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 3, 0, 0);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        oomeDetectionPanel.add(oomeDetectionChooseDirButton, gridBagConstraints);

        // oomeDetectionPanel
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new Insets(5, 0, 0, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(oomeDetectionPanel, gridBagConstraints);

        // heapWalkerLabel
        JLabel heapWalkerLabel = new JLabel(Bundle.ProfilerOptionsPanel_HeapWalkerLabelText());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.insets = new Insets(5, 10, 0, 5);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        add(heapWalkerLabel, gridBagConstraints);

        // enableHeapWalkerAnalysisCheckbox
        enableHeapWalkerAnalysisCheckbox = new JCheckBox();
        org.openide.awt.Mnemonics.setLocalizedText(enableHeapWalkerAnalysisCheckbox, Bundle.ProfilerOptionsPanel_EnableAnalysisCheckbox());
        enableHeapWalkerAnalysisCheckbox.getAccessibleContext().setAccessibleDescription(Bundle.ProfilerOptionsPanel_EnableAnalysisCheckbox());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new Insets(5, 10, 0, 6);
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        add(enableHeapWalkerAnalysisCheckbox, gridBagConstraints);

        // resetConfirmationsPanel
        JPanel resetConfirmationsPanel = new JPanel(new GridBagLayout());

        // resetConfirmationsArea
        JTextArea resetConfirmationsArea = new JTextArea(Bundle.ProfilerOptionsPanel_ResetHintText()) {
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                size.width = 1;
                return size;
            }
        };
        resetConfirmationsArea.setOpaque(false);
        resetConfirmationsArea.setWrapStyleWord(true);
        resetConfirmationsArea.setLineWrap(true);
        resetConfirmationsArea.setEnabled(false);
        resetConfirmationsArea.setFont(UIManager.getFont("Label.font")); //NOI18N
        resetConfirmationsArea.setDisabledTextColor(UIManager.getColor("Label.foreground")); //NOI18N
        resetConfirmationsArea.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.insets = new Insets(0, 0, 0, 5);
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        resetConfirmationsPanel.add(resetConfirmationsArea, gridBagConstraints);

        // resetConfirmationsButton
        resetConfirmationsButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(resetConfirmationsButton, Bundle.ProfilerOptionsPanel_ResetButtonName());
        resetConfirmationsButton.getAccessibleContext().setAccessibleDescription(Bundle.ProfilerOptionsPanel_ResetHintText());
        resetConfirmationsButton.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 0, 0);
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        resetConfirmationsPanel.add(resetConfirmationsButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.insets = new Insets(5, 10, 0, 6);
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(resetConfirmationsPanel, gridBagConstraints);
    }

    private void updateEnabling() {
        boolean customOOMEdirSelected = Bundle.ProfilerOptionsPanel_OomeCustomText().equals(oomeCombo.getSelectedItem());
        oomeDetectionDirTextField.setEnabled(customOOMEdirSelected);
        oomeDetectionChooseDirButton.setEnabled(customOOMEdirSelected);
    }

//    private void updateJavaPlatformComboItems() {
//        updateJavaPlatforms();
//
//        Object selectedJavaPlatform = javaPlatformCombo.getSelectedItem();
//
//        javaPlatformCombo.removeAllItems();
//
//        DefaultComboBoxModel javaPlatformComboModel = new DefaultComboBoxModel(supportedJavaPlatformsNames.toArray());
//        javaPlatformComboModel.insertElementAt(Bundle.ProfilerOptionsPanel_UseProjectJvmText(), 0);
//
//        javaPlatformCombo.setModel(javaPlatformComboModel);
//
//        if (selectedJavaPlatform != null) {
//            javaPlatformCombo.setSelectedItem(selectedJavaPlatform);
//        }
//    }
//
//    private void updateJavaPlatforms() {
//        supportedJavaPlatforms.clear();
//        supportedJavaPlatformsNames.clear();
//
//        Iterator supportedPlatforms = JavaPlatform.getPlatforms().iterator();
//
//        JavaPlatform supportedJavaPlatform;
//        String supportedJavaPlatformName;
//
//        while (supportedPlatforms.hasNext()) {
//            supportedJavaPlatform = (JavaPlatform) supportedPlatforms.next();
//            supportedJavaPlatformName = supportedJavaPlatform.getDisplayName();
//
//            if (!supportedJavaPlatformsNames.contains(supportedJavaPlatformName)) {
//                supportedJavaPlatforms.add(supportedJavaPlatform);
//                supportedJavaPlatformsNames.add(supportedJavaPlatformName);
//            }
//        }
//
//        supportedJavaPlatforms.addAll(JavaPlatform.getPlatforms());
//    }
}
