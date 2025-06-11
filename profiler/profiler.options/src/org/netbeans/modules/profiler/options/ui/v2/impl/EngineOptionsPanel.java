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

package org.netbeans.modules.profiler.options.ui.v2.impl;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Objects;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.JExtendedSpinner;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.netbeans.modules.profiler.options.ui.v2.ProfilerOptionsPanel;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "EngineOptionsPanel_Name=Engine",
    "EngineOptionsPanel_MethodsProfiling=Methods Profiling",
    "EngineOptionsPanel_MethodsGeneral=General (Sampled):",
    "EngineOptionsPanel_MethodsFocused=Focused (Instrumented):",
    "EngineOptionsPanel_ObjectsProfiling=Objects Profiling",
    "EngineOptionsPanel_ThreadsProfiling=Threads Profiling",
    "StpSamplingFrequencyLabel=Sampling &frequency:",
    "StpSamplingFrequencyTooltip=Customize sampling frequency of the profiler.",
    "StpSamplingFrequencyMs=ms",
    "CPUSettingsAdvancedPanel_SchemeComboBoxItemLazy=Lazy",
    "CPUSettingsAdvancedPanel_SchemeComboBoxItemEager=Eager",
    "CPUSettingsAdvancedPanel_SchemeComboBoxItemTotal=Total",
    "CPUSettingsAdvancedPanel_MethodsTrackingLabelText=Methods tracking:",
    "CPUSettingsAdvancedPanel_InstrRadioText=&Exact call tree and timing",
    "CPUSettingsAdvancedPanel_SamplingRadioText=E&xact call tree, sampled timing:",
    "CPUSettingsAdvancedPanel_ExcludeTimeCheckboxText=Excl&ude time spent in Thread.sleep() and Object.wait()",
    "CPUSettingsAdvancedPanel_ProfileThreadsCheckboxText=&Profile new Threads/Runnables",
    "CPUSettingsAdvancedPanel_LimitThreadsCheckboxText=&Limit number of profiled threads:",
    "CPUSettingsAdvancedPanel_InstrSchemeLabelText=I&nstrumentation scheme:",
    "CPUSettingsAdvancedPanel_InstrumentLabelText=Instrument:",
    "CPUSettingsAdvancedPanel_MethodInvokeCheckboxText=&Method.invoke()",
    "CPUSettingsAdvancedPanel_GetterSetterCheckboxText=&Getter/setter methods",
    "CPUSettingsAdvancedPanel_EmptyMethodsCheckboxText=Empt&y methods",
    "MemorySettingsBasicPanel_TrackEveryLabelText=Limit &allocations tracking to every:",
    "MemorySettingsBasicPanel_AllocLabelText=th object",
    "MemorySettingsAdvancedPanel_RunGcCheckboxText=&Run garbage collection when getting memory results",
    "EngineOptionsPanel_SampleThreads=&Sample threads states",
})
@ServiceProvider( service = ProfilerOptionsPanel.class, position = 30 )
public final class EngineOptionsPanel extends ProfilerOptionsPanel {
    
    private JCheckBox excludeTimeCheckbox;
    private JCheckBox instrumentEmptyMethodsCheckbox;
    private JCheckBox instrumentGettersSettersCheckbox;
    private JCheckBox instrumentMethodInvokeCheckbox;
    private JCheckBox limitThreadsCheckbox;
    private JCheckBox profileSpawnedThreadsCheckbox;
    private JComboBox instrumentationSchemeCombo;
    private JRadioButton exactTimingRadio;
    private JRadioButton sampledTimingRadio;
    private JSpinner limitThreadsSpinner;
    private JSpinner sampledTimingSpinner;
    private JSpinner samplingFrequencySpinner;
    
//    private JSpinner trackEverySpinner;
    private JCheckBox runGCCheckbox;
    
    private JCheckBox sampledThreadsChoice;
    
    
    public EngineOptionsPanel() {
        initUI();
    }
    
    
    public String getDisplayName() {
        return Bundle.EngineOptionsPanel_Name();
    }

    public void storeTo(ProfilerIDESettings settings) {
        ProfilingSettings pSettings = settings.getDefaultProfilingSettings();
        
        pSettings.setSamplingFrequency((Integer)samplingFrequencySpinner.getValue());
        int samplingInterval = (Integer)sampledTimingSpinner.getValue();
        if (exactTimingRadio.isSelected()) samplingInterval = -samplingInterval;
        pSettings.setSamplingInterval(samplingInterval);
        pSettings.setExcludeWaitTime(excludeTimeCheckbox.isSelected());
        pSettings.setInstrumentSpawnedThreads(profileSpawnedThreadsCheckbox.isSelected());
        int limitThreads = (Integer)limitThreadsSpinner.getValue();
        if (!limitThreadsCheckbox.isSelected()) limitThreads = -limitThreads;
        pSettings.setNProfiledThreadsLimit(limitThreads);
        int instrScheme = instrumentationSchemeCombo.getSelectedIndex();
        if (instrScheme == 0) pSettings.setInstrScheme(CommonConstants.INSTRSCHEME_LAZY);
        else if (instrScheme == 1) pSettings.setInstrScheme(CommonConstants.INSTRSCHEME_EAGER);
        else pSettings.setInstrScheme(CommonConstants.INSTRSCHEME_TOTAL);
        pSettings.setInstrumentMethodInvoke(instrumentMethodInvokeCheckbox.isSelected());
        pSettings.setInstrumentGetterSetterMethods(instrumentGettersSettersCheckbox.isSelected());
        pSettings.setInstrumentEmptyMethods(instrumentEmptyMethodsCheckbox.isSelected());
        
//        pSettings.setAllocTrackEvery((Integer)trackEverySpinner.getValue());
        pSettings.setRunGCOnGetResultsInMemoryProfiling(runGCCheckbox.isSelected());
        
        pSettings.setThreadsSamplingEnabled(sampledThreadsChoice.isSelected());
        
        settings.saveDefaultProfilingSettings();
    }

    public void loadFrom(ProfilerIDESettings settings) {
        ProfilingSettings pSettings = settings.getDefaultProfilingSettings();
        
        samplingFrequencySpinner.setValue(pSettings.getSamplingFrequency());
        int samplingInterval = pSettings.getSamplingInterval();
        exactTimingRadio.setSelected(samplingInterval <= 0);
        sampledTimingRadio.setSelected(samplingInterval > 0);
        sampledTimingSpinner.setValue(Math.abs(samplingInterval));
        excludeTimeCheckbox.setSelected(pSettings.getExcludeWaitTime());
        profileSpawnedThreadsCheckbox.setSelected(pSettings.getInstrumentSpawnedThreads());
        int limitThreads = pSettings.getNProfiledThreadsLimit();
        limitThreadsCheckbox.setSelected(limitThreads > 0);
        limitThreadsSpinner.setValue(Math.abs(limitThreads));
        int instrScheme = pSettings.getInstrScheme();
        if (instrScheme == CommonConstants.INSTRSCHEME_LAZY) instrumentationSchemeCombo.setSelectedIndex(0);
        else if (instrScheme == CommonConstants.INSTRSCHEME_EAGER) instrumentationSchemeCombo.setSelectedIndex(1);
        else instrumentationSchemeCombo.setSelectedIndex(2);
        instrumentMethodInvokeCheckbox.setSelected(pSettings.getInstrumentMethodInvoke());
        instrumentGettersSettersCheckbox.setSelected(pSettings.getInstrumentGetterSetterMethods());
        instrumentEmptyMethodsCheckbox.setSelected(pSettings.getInstrumentEmptyMethods());
        
//        trackEverySpinner.setValue(pSettings.getAllocTrackEvery());
        runGCCheckbox.setSelected(pSettings.getRunGCOnGetResultsInMemoryProfiling());
        
        sampledThreadsChoice.setSelected(pSettings.getThreadsSamplingEnabled());
    }

    public boolean equalsTo(ProfilerIDESettings settings) {
        ProfilingSettings pSettings = settings.getDefaultProfilingSettings();
        
        if (!Objects.equals(samplingFrequencySpinner.getValue(), pSettings.getSamplingFrequency())) return false;
        int samplingInterval = pSettings.getSamplingInterval();
        if (samplingInterval > 0) { if (!sampledTimingRadio.isSelected()) return false; }
        else { if (!exactTimingRadio.isSelected()) return false; }
        if (!Objects.equals(sampledTimingSpinner.getValue(), Math.abs(samplingInterval))) return false;
        if (excludeTimeCheckbox.isSelected() != pSettings.getExcludeWaitTime()) return false;
        if (profileSpawnedThreadsCheckbox.isSelected() != pSettings.getInstrumentSpawnedThreads()) return false;
        int limitThreads = pSettings.getNProfiledThreadsLimit();
        if (limitThreads > 0 && !limitThreadsCheckbox.isSelected()) return false;
        if (!Objects.equals(limitThreadsSpinner.getValue(), Math.abs(limitThreads))) return false;
        int instrScheme = instrumentationSchemeCombo.getSelectedIndex();
        if (instrScheme == 0 && pSettings.getInstrScheme() != CommonConstants.INSTRSCHEME_LAZY) return false;
        if (instrScheme == 1 && pSettings.getInstrScheme() != CommonConstants.INSTRSCHEME_EAGER) return false;
        if (instrScheme == 2 && pSettings.getInstrScheme() != CommonConstants.INSTRSCHEME_TOTAL) return false;
        if (instrumentMethodInvokeCheckbox.isSelected() != pSettings.getInstrumentMethodInvoke()) return false;
        if (instrumentGettersSettersCheckbox.isSelected() != pSettings.getInstrumentGetterSetterMethods()) return false;
        if (instrumentEmptyMethodsCheckbox.isSelected() != pSettings.getInstrumentEmptyMethods()) return false;
        
//        if (!Objects.equals(trackEverySpinner.getValue(), pSettings.getAllocTrackEvery())) return false;
        if (runGCCheckbox.isSelected() != pSettings.getRunGCOnGetResultsInMemoryProfiling()) return false;
        
        if (sampledThreadsChoice.isSelected() != pSettings.getThreadsSamplingEnabled()) return false;
        
        return true;
    }
    
    
    private void initUI() {
        setLayout(new GridBagLayout());
        
        GridBagConstraints c;
        int y = 0;
        int htab = 8;
        int hgap = 10;
        int vgap = 5;
        
        Separator cpuSettingsSeparator = new Separator(Bundle.EngineOptionsPanel_MethodsProfiling());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, vgap * 2, 0);
        add(cpuSettingsSeparator, c);
        
        JLabel methodsSamplingLabel = new JLabel();
        Mnemonics.setLocalizedText(methodsSamplingLabel, Bundle.EngineOptionsPanel_MethodsGeneral());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab, vgap, 0);
        add(methodsSamplingLabel, c);
        
        JLabel samplingFrequencyLabel = new JLabel();
        Mnemonics.setLocalizedText(samplingFrequencyLabel, Bundle.StpSamplingFrequencyLabel());
//        samplingFrequencyLabel.setToolTipText(Bundle.StpSamplingFrequencyTooltip());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab * 2, vgap, 0);
        add(samplingFrequencyLabel, c);
        
        samplingFrequencySpinner = new JExtendedSpinner(new SpinnerNumberModel(10, 1, 65535, 1));
//        samplingFrequencySpinner.setToolTipText(Bundle.StpSamplingFrequencyTooltip());
        samplingFrequencyLabel.setLabelFor(samplingFrequencySpinner);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(samplingFrequencySpinner, c);
        
        JLabel samplingFrequencyUnitsLabel = new JLabel();
        Mnemonics.setLocalizedText(samplingFrequencyUnitsLabel, Bundle.StpSamplingFrequencyMs());
//        samplingFrequencyUnitsLabel.setToolTipText(Bundle.StpSamplingFrequencyTooltip());
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, hgap / 2, vgap, 0);
        add(samplingFrequencyUnitsLabel, c);
        
        JLabel methodsInstrumentingLabel = new JLabel();
        Mnemonics.setLocalizedText(methodsInstrumentingLabel, Bundle.EngineOptionsPanel_MethodsFocused());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(vgap * 2, htab, vgap, 3);
        add(methodsInstrumentingLabel, c);

        JLabel methodsTrackingLabel = new JLabel();
        Mnemonics.setLocalizedText(methodsTrackingLabel, Bundle.CPUSettingsAdvancedPanel_MethodsTrackingLabelText());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab * 2, vgap, 0);
        add(methodsTrackingLabel, c);
        
        ButtonGroup methodsTrackingRadiosGroup = new ButtonGroup();

        exactTimingRadio = new JRadioButton();
        Mnemonics.setLocalizedText(exactTimingRadio, Bundle.CPUSettingsAdvancedPanel_InstrRadioText());
//        exactTimingRadio.setToolTipText(Bundle.StpExactTimingTooltip());
        methodsTrackingRadiosGroup.add(exactTimingRadio);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab * 2 + hgap, 0, 0);
        add(exactTimingRadio, c);

        sampledTimingRadio = new JRadioButton();
        Mnemonics.setLocalizedText(sampledTimingRadio, Bundle.CPUSettingsAdvancedPanel_SamplingRadioText());
//        sampledTimingRadio.setToolTipText(Bundle.StpSampledTimingTooltip());
        methodsTrackingRadiosGroup.add(sampledTimingRadio);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab * 2 + hgap, vgap, 0);
        add(sampledTimingRadio, c);

        sampledTimingSpinner = new JExtendedSpinner(new SpinnerNumberModel(10, 1, 65535, 1));
//        sampledTimingSpinner.setToolTipText(Bundle.StpSampledTimingTooltip());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(sampledTimingSpinner, c);

        JLabel sampledTimingUnitsLabel = new JLabel();
        Mnemonics.setLocalizedText(sampledTimingUnitsLabel, Bundle.StpSamplingFrequencyMs());
//        sampledTimingLabel.setToolTipText(Bundle.StpSampledTimingTooltip());
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, hgap / 2, vgap, 0);
        add(sampledTimingUnitsLabel, c);

        excludeTimeCheckbox = new JCheckBox();
        Mnemonics.setLocalizedText(excludeTimeCheckbox, Bundle.CPUSettingsAdvancedPanel_ExcludeTimeCheckboxText());
//        excludeTimeCheckbox.setToolTipText(Bundle.StpSleepWaitTooltip());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab * 2, vgap, 0);
        add(excludeTimeCheckbox, c);

        profileSpawnedThreadsCheckbox = new JCheckBox();
        Mnemonics.setLocalizedText(profileSpawnedThreadsCheckbox, Bundle.CPUSettingsAdvancedPanel_ProfileThreadsCheckboxText());
//        profileSpawnedThreadsCheckbox.setToolTipText(Bundle.StpSpawnedTooltip());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab * 2, vgap, 0);
        add(profileSpawnedThreadsCheckbox, c);

        limitThreadsCheckbox = new JCheckBox();
        Mnemonics.setLocalizedText(limitThreadsCheckbox, Bundle.CPUSettingsAdvancedPanel_LimitThreadsCheckboxText());
//        limitThreadsCheckbox.setToolTipText(Bundle.StpLimitThreadsTooltip());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab * 2, vgap, 0);
        add(limitThreadsCheckbox, c);

        limitThreadsSpinner = new JExtendedSpinner(new SpinnerNumberModel(50, 1, 65535, 1));
//        limitThreadsSpinner.setToolTipText(Bundle.StpLimitThreadsTooltip());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y++;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(limitThreadsSpinner, c);

        JLabel instrumentationSchemeLabel = new JLabel();
        Mnemonics.setLocalizedText(instrumentationSchemeLabel, Bundle.CPUSettingsAdvancedPanel_InstrSchemeLabelText());
//        instrumentationSchemeLabel.setToolTipText(Bundle.StpInstrSchemeTooltip());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab * 2, vgap, 0);
        add(instrumentationSchemeLabel, c);

        instrumentationSchemeCombo = new JComboBox(new String[] {
                                                       Bundle.CPUSettingsAdvancedPanel_SchemeComboBoxItemLazy(), 
                                                       Bundle.CPUSettingsAdvancedPanel_SchemeComboBoxItemEager(),
                                                       Bundle.CPUSettingsAdvancedPanel_SchemeComboBoxItemTotal()
                                                   });
        instrumentationSchemeLabel.setLabelFor(instrumentationSchemeCombo);
//        instrumentationSchemeCombo.setToolTipText(Bundle.StpInstrSchemeTooltip());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y++;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(instrumentationSchemeCombo, c);

        JLabel instrumentLabel = new JLabel();
        Mnemonics.setLocalizedText(instrumentLabel, Bundle.CPUSettingsAdvancedPanel_InstrumentLabelText());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab * 2, vgap, 0);
        add(instrumentLabel, c);

        instrumentMethodInvokeCheckbox = new JCheckBox();
        Mnemonics.setLocalizedText(instrumentMethodInvokeCheckbox, Bundle.CPUSettingsAdvancedPanel_MethodInvokeCheckboxText());
//        instrumentMethodInvokeCheckbox.setToolTipText(Bundle.StpMethodInvokeTooltip());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab * 2 + hgap, 0, 0);
        add(instrumentMethodInvokeCheckbox, c);

        instrumentGettersSettersCheckbox = new JCheckBox();
        Mnemonics.setLocalizedText(instrumentGettersSettersCheckbox, Bundle.CPUSettingsAdvancedPanel_GetterSetterCheckboxText());
//        instrumentGettersSettersCheckbox.setToolTipText(Bundle.StpGetterSetterTooltip());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab * 2 + hgap, 0, 0);
        add(instrumentGettersSettersCheckbox, c);

        instrumentEmptyMethodsCheckbox = new JCheckBox();
        Mnemonics.setLocalizedText(instrumentEmptyMethodsCheckbox, Bundle.CPUSettingsAdvancedPanel_EmptyMethodsCheckboxText());
//        instrumentEmptyMethodsCheckbox.setToolTipText(Bundle.StpEmptyMethodsTooltip());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab * 2 + hgap, vgap, 0);
        add(instrumentEmptyMethodsCheckbox, c);
        
        Separator memorySettingsSeparator = new Separator(Bundle.EngineOptionsPanel_ObjectsProfiling());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(vgap * 4, 0, vgap * 2, 0);
        add(memorySettingsSeparator, c);

//        JLabel trackEveryLabel1 = new JLabel();
//        Mnemonics.setLocalizedText(trackEveryLabel1, Bundle.MemorySettingsBasicPanel_TrackEveryLabelText());
// //        trackEveryLabel1.setToolTipText(Bundle.StpTrackEveryTooltip());
//        c = new GridBagConstraints();
//        c.gridx = 0;
//        c.gridy = y;
//        c.gridwidth = 1;
//        c.fill = GridBagConstraints.NONE;
//        c.anchor = GridBagConstraints.WEST;
//        c.insets = new Insets(0, htab, vgap, 0);
//        add(trackEveryLabel1, c);
//
//        trackEverySpinner = new JExtendedSpinner(new SpinnerNumberModel(10, 1, 65535, 1));
//        trackEveryLabel1.setLabelFor(trackEverySpinner);
// //        trackEverySpinner.setToolTipText(Bundle.StpTrackEveryTooltip());
//        c = new GridBagConstraints();
//        c.gridx = 1;
//        c.gridy = y;
//        c.gridwidth = 1;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.anchor = GridBagConstraints.WEST;
//        c.insets = new Insets(0, hgap, vgap, 0);
//        add(trackEverySpinner, c);
//
//        JLabel trackEveryLabel2 = new JLabel(Bundle.MemorySettingsBasicPanel_AllocLabelText());
// //        trackEveryLabel2.setToolTipText(Bundle.StpTrackEveryTooltip());
//        c = new GridBagConstraints();
//        c.gridx = 2;
//        c.gridy = y++;
//        c.gridwidth = 1;
//        c.fill = GridBagConstraints.NONE;
//        c.anchor = GridBagConstraints.WEST;
//        c.insets = new Insets(0, hgap / 2, vgap, 0);
//        add(trackEveryLabel2, c);

        runGCCheckbox = new JCheckBox();
        Mnemonics.setLocalizedText(runGCCheckbox, Bundle.MemorySettingsAdvancedPanel_RunGcCheckboxText());
//        runGCCheckbox.setToolTipText(Bundle.StpRunGcTooltip());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab, vgap, 0);
        add(runGCCheckbox, c);
        
        Separator dataTransferSeparator = new Separator(Bundle.EngineOptionsPanel_ThreadsProfiling());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(vgap * 4, 0, vgap * 2, 0);
        add(dataTransferSeparator, c);
        
        sampledThreadsChoice = new JCheckBox();
        Mnemonics.setLocalizedText(sampledThreadsChoice, Bundle.EngineOptionsPanel_SampleThreads());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, htab, vgap, 0);
        add(sampledThreadsChoice, c);
        
        JPanel filler = UIUtils.createFillerPanel();
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        add(filler, c);
        
    }
    
}
