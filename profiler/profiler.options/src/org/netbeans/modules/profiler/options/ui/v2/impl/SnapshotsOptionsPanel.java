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

package org.netbeans.modules.profiler.options.ui.v2.impl;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.ui.UIUtils;
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
    "SnapshotsOptionsPanel_Name=Snapshots",
    "SnapshotsOptionsPanel_CatSnapshots=Snapshots",
    "SnapshotsOptionsPanel_OnSnapshotLabel=&When taking snapshot:",
    "SnapshotsOptionsPanel_ItemOpenSnapshot=Open snapshot",
    "SnapshotsOptionsPanel_ItemSaveSnapshot=Save snapshot",
    "SnapshotsOptionsPanel_ItemSaveOpenSnapshot=Save and open snapshot",
    "SnapshotsOptionsPanel_OnHeapdumpLabel=Wh&en taking heap dump:",
    "SnapshotsOptionsPanel_ItemSaveConfirmOpen=Save and confirm open",
    "SnapshotsOptionsPanel_OnOOMEHeapdumpLabel=&On OOME heap dump:",
    "SnapshotsOptionsPanel_ItemDoNothing=Do nothing",
    "SnapshotsOptionsPanel_ItemSaveToProject=Save to project",
    "SnapshotsOptionsPanel_ItemSaveToTemp=Save to temporary directory",
    "SnapshotsOptionsPanel_ItemSaveToCustom=Save to custom directory:",
    "SnapshotsOptionsPanel_ChooseCustomDir=...",
    "SnapshotsOptionsPanel_RestoreSnapshots=&Restore open snapshots on new IDE session",
    "SnapshotsOptionsPanel_RestoreHeapDumps=Re&store open heap dumps on new IDE session",
    "SnapshotsOptionsPanel_CatSnapshotsWindow=Snapshots Window",
    "SnapshotsOptionsPanel_OpenAutomaticallyLabel=O&pen automatically:",
    "SnapshotsOptionsPanel_ItemNever=Never",
    "SnapshotsOptionsPanel_ItemNewSession=On new profiling session",
    "SnapshotsOptionsPanel_ItemShowWindow=On show profiler window",
    "SnapshotsOptionsPanel_ItemFirstSnapshot=On first saved snapshot",
    "SnapshotsOptionsPanel_ItemEachSnapshot=On each saved snapshot",
    "SnapshotsOptionsPanel_CloseAutomaticallyLabel=C&lose automatically:",
    "SnapshotsOptionsPanel_ItemCloseSession=On close profiling session",
    "SnapshotsOptionsPanel_ItemHideWindow=On hide profiler window",
    "SnapshotsOptionsPanel_ChooseDirCaption=Choose Heap Dump Directory"
})
@ServiceProvider( service = ProfilerOptionsPanel.class, position = 20 )
public final class SnapshotsOptionsPanel extends ProfilerOptionsPanel {
    
    private JComboBox onSnapshotCombo;
    private JComboBox onHeapDumpCombo;
    private JComboBox onOOMEHeapDumpCombo;
    private JTextField customOOMEField;
    private JButton customOOMEButton;
    private JCheckBox restoreSnapshotChoice;
    private JCheckBox restoreHeapDumpsChoice;
    private JComboBox openSnapshotsWindowCombo;
    private JComboBox closeSnapshotsWindowCombo;
    
    
    public SnapshotsOptionsPanel() {
        initUI();
    }
    
    
    public String getDisplayName() {
        return Bundle.SnapshotsOptionsPanel_Name();
    }

    public void storeTo(ProfilerIDESettings settings) {
        String customDir = customOOMEField.getText().trim();
        if (onOOMEHeapDumpCombo.getSelectedIndex() == 3 && !new File(customDir).isDirectory())
            onOOMEHeapDumpCombo.setSelectedIndex(0);
        
        int onSnapshot = onSnapshotCombo.getSelectedIndex();
        settings.setAutoOpenSnapshot(onSnapshot == 0 || onSnapshot == 2);
        settings.setAutoSaveSnapshot(onSnapshot == 1 || onSnapshot == 2);
        
        String onHeapDump;
        switch (onHeapDumpCombo.getSelectedIndex()) {
            case 0: onHeapDump = "NO_OPTION"; break; // NOI18N
            case 1: onHeapDump = "YES_OPTION"; break; // NOI18N
            default: onHeapDump = null; break;
        }
        settings.setDoNotShowAgain("HeapDumpAction.heapdumpSaved", onHeapDump); // NOI18N
        
        settings.setOOMDetectionMode(onOOMEHeapDumpCombo.getSelectedIndex());
        settings.setCustomHeapdumpPath(customDir);
        settings.setReopenSnapshots(restoreSnapshotChoice.isSelected());
        settings.setReopenHeapDumps(restoreHeapDumpsChoice.isSelected());
        
        settings.setSnapshotWindowOpenPolicy(openSnapshotsWindowCombo.getSelectedIndex());
        settings.setSnapshotWindowClosePolicy(closeSnapshotsWindowCombo.getSelectedIndex());
    }

    public void loadFrom(ProfilerIDESettings settings) {
        int onSnapshot = settings.getAutoSaveSnapshot() ? 1 : 0;
        if (settings.getAutoOpenSnapshot()) onSnapshot *= 2;
        onSnapshotCombo.setSelectedIndex(onSnapshot);
        
        String onHeapDump = settings.getDoNotShowAgain("HeapDumpAction.heapdumpSaved"); // NOI18N
        if ("NO_OPTION".equals(onHeapDump)) onHeapDumpCombo.setSelectedIndex(0); // NOI18N
        else if ("YES_OPTION".equals(onHeapDump)) onHeapDumpCombo.setSelectedIndex(1); // NOI18N
        else onHeapDumpCombo.setSelectedIndex(2);
        
        int oomeMode = settings.getOOMDetectionMode();
        String customDir = settings.getCustomHeapdumpPath();
        if (oomeMode == 3 && !new File(customDir).isDirectory()) {
            oomeMode = 0;
            settings.setOOMDetectionMode(oomeMode);
        }
        onOOMEHeapDumpCombo.setSelectedIndex(oomeMode);
        customOOMEField.setText(customDir);
        restoreSnapshotChoice.setSelected(settings.getReopenSnapshots());
        restoreHeapDumpsChoice.setSelected(settings.getReopenHeapDumps());
        
        openSnapshotsWindowCombo.setSelectedIndex(settings.getSnapshotWindowOpenPolicy());
        closeSnapshotsWindowCombo.setSelectedIndex(settings.getSnapshotWindowClosePolicy());
    }

    public boolean equalsTo(ProfilerIDESettings settings) {
        int onSnapshot = settings.getAutoSaveSnapshot() ? 1 : 0;
        if (settings.getAutoOpenSnapshot()) onSnapshot *= 2;
        if (onSnapshot != onSnapshotCombo.getSelectedIndex()) return false;
        
        String onHeapDump = settings.getDoNotShowAgain("HeapDumpAction.heapdumpSaved"); // NOI18N
        if (onHeapDump == null && onHeapDumpCombo.getSelectedIndex() != 2) return false;
        else if ("NO_OPTION".equals(onHeapDump) && onHeapDumpCombo.getSelectedIndex() != 0) return false; // NOI18N
        else if ("YES_OPTION".equals(onHeapDump) && onHeapDumpCombo.getSelectedIndex() != 1) return false; // NOI18N
        
        if (settings.getOOMDetectionMode() != onOOMEHeapDumpCombo.getSelectedIndex()) return false;
        if (!Objects.equals(settings.getCustomHeapdumpPath(), customOOMEField.getText().trim())) return false;
        if (settings.getReopenSnapshots() != restoreSnapshotChoice.isSelected()) return false;
        if (settings.getReopenHeapDumps() != restoreHeapDumpsChoice.isSelected()) return false;
        
        if (settings.getSnapshotWindowOpenPolicy() != openSnapshotsWindowCombo.getSelectedIndex()) return false;
        if (settings.getSnapshotWindowClosePolicy() != closeSnapshotsWindowCombo.getSelectedIndex()) return false;
        
        return true;
    }
    
    
    private void initUI() {
        setLayout(new GridBagLayout());
        
        GridBagConstraints c;
        int y = 0;
        int htab = 8;
        int hgap = 10;
        int vgap = 5;
        
        Separator snapshotsSeparator = new Separator(Bundle.SnapshotsOptionsPanel_CatSnapshots());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, vgap * 2, 0);
        add(snapshotsSeparator, c);
        
        JLabel onSnapshotLabel = new JLabel();
        Mnemonics.setLocalizedText(onSnapshotLabel, Bundle.SnapshotsOptionsPanel_OnSnapshotLabel());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab, vgap, 0);
        add(onSnapshotLabel, c);
        
        onSnapshotCombo = new JComboBox(new String[] {
            Bundle.SnapshotsOptionsPanel_ItemOpenSnapshot(),
            Bundle.SnapshotsOptionsPanel_ItemSaveSnapshot(),
            Bundle.SnapshotsOptionsPanel_ItemSaveOpenSnapshot() });
        onSnapshotLabel.setLabelFor(onSnapshotCombo);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(onSnapshotCombo, c);
        
        JPanel filler1 = new JPanel(null);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(filler1, c);
        
//        JLabel onThreadDumpLabel = new JLabel("When taking thread dump:");
//        c = new GridBagConstraints();
//        c.gridx = 0;
//        c.gridy = y;
//        c.anchor = GridBagConstraints.WEST;
//        c.insets = new Insets(0, htab, vgap, 0);
//        add(onThreadDumpLabel, c);
//        
//        JComboBox onThreadDumpCombo = new JComboBox(new String[] { "Open snapshot", "Save snapshot", "Save and open snapshot" });
//        onThreadDumpLabel.setLabelFor(onThreadDumpCombo);
//        c = new GridBagConstraints();
//        c.gridx = 1;
//        c.gridy = y;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.insets = new Insets(0, hgap, vgap, 0);
//        add(onThreadDumpCombo, c);
//        
//        JPanel filler2 = new JPanel(null);
//        c = new GridBagConstraints();
//        c.gridx = 2;
//        c.gridy = y++;
//        c.weightx = 1;
//        c.gridwidth = GridBagConstraints.REMAINDER;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        add(filler2, c);
        
        JLabel onHeapDumpLabel = new JLabel();
        Mnemonics.setLocalizedText(onHeapDumpLabel, Bundle.SnapshotsOptionsPanel_OnHeapdumpLabel());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab, vgap, 0);
        add(onHeapDumpLabel, c);
        
        onHeapDumpCombo = new JComboBox(new String[] {
            Bundle.SnapshotsOptionsPanel_ItemSaveSnapshot(),
            Bundle.SnapshotsOptionsPanel_ItemSaveOpenSnapshot(),
            Bundle.SnapshotsOptionsPanel_ItemSaveConfirmOpen() });
        onHeapDumpLabel.setLabelFor(onHeapDumpCombo);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(onHeapDumpCombo, c);
        
        JPanel filler3 = new JPanel(null);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(filler3, c);
        
        JLabel onOOMEHeapDumpLabel = new JLabel();
        Mnemonics.setLocalizedText(onOOMEHeapDumpLabel, Bundle.SnapshotsOptionsPanel_OnOOMEHeapdumpLabel());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(vgap * 3, htab, vgap, 0);
        add(onOOMEHeapDumpLabel, c);
        
        onOOMEHeapDumpCombo = new JComboBox(new String[] {
            Bundle.SnapshotsOptionsPanel_ItemDoNothing(),
            Bundle.SnapshotsOptionsPanel_ItemSaveToProject(),
            Bundle.SnapshotsOptionsPanel_ItemSaveToTemp(),
            Bundle.SnapshotsOptionsPanel_ItemSaveToCustom() }) {
            public void setSelectedIndex(int index) {
                super.setSelectedIndex(index);
                boolean custom = index == 3;
                customOOMEField.setVisible(custom);
                customOOMEButton.setVisible(custom);
            }
        };
        onOOMEHeapDumpLabel.setLabelFor(onOOMEHeapDumpCombo);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(vgap * 3, hgap, vgap, 0);
        add(onOOMEHeapDumpCombo, c);
        
        JPanel filler4 = new JPanel(null);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(filler4, c);
        
        customOOMEField = new JTextField();
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(customOOMEField, c);
        
        customOOMEButton = new JButton() {
            protected void fireActionPerformed(ActionEvent e) {
                super.fireActionPerformed(e);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        String newDir = selectCustomDir(customOOMEField.getText().trim());
                        if (newDir != null) customOOMEField.setText(newDir);
                    }
                });
            }
        };
        Mnemonics.setLocalizedText(customOOMEButton, Bundle.SnapshotsOptionsPanel_ChooseCustomDir());
        customOOMEButton.setMargin(new Insets(0, hgap, 0, hgap));
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y;
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new Insets(0, vgap, vgap, 0);
        add(customOOMEButton, c);
        
        JPanel filler5 = new JPanel(null);
        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = y++;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(filler5, c);
        
        restoreSnapshotChoice = new JCheckBox();
        Mnemonics.setLocalizedText(restoreSnapshotChoice, Bundle.SnapshotsOptionsPanel_RestoreSnapshots());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(vgap * 3, htab, vgap, 0);
        add(restoreSnapshotChoice, c);
        
        restoreHeapDumpsChoice = new JCheckBox();
        Mnemonics.setLocalizedText(restoreHeapDumpsChoice, Bundle.SnapshotsOptionsPanel_RestoreHeapDumps());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab, vgap, 0);
        add(restoreHeapDumpsChoice, c);
        
        Separator snapshotsWindowSeparator = new Separator(Bundle.SnapshotsOptionsPanel_CatSnapshotsWindow());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(vgap * 4, 0, vgap * 2, 0);
        add(snapshotsWindowSeparator, c);
        
        JLabel openSnapshotsWindowLabel = new JLabel();
        Mnemonics.setLocalizedText(openSnapshotsWindowLabel, Bundle.SnapshotsOptionsPanel_OpenAutomaticallyLabel());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab, vgap, 0);
        add(openSnapshotsWindowLabel, c);
        
        openSnapshotsWindowCombo = new JComboBox(new String[] {
            Bundle.SnapshotsOptionsPanel_ItemNever(),
            Bundle.SnapshotsOptionsPanel_ItemNewSession(),
            Bundle.SnapshotsOptionsPanel_ItemShowWindow(),
            Bundle.SnapshotsOptionsPanel_ItemFirstSnapshot(),
            Bundle.SnapshotsOptionsPanel_ItemEachSnapshot() });
        openSnapshotsWindowLabel.setLabelFor(openSnapshotsWindowCombo);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(openSnapshotsWindowCombo, c);
        
        JPanel filler6 = new JPanel(null);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(filler6, c);
        
        JLabel closeSnapshotsWindowLabel = new JLabel();
        Mnemonics.setLocalizedText(closeSnapshotsWindowLabel, Bundle.SnapshotsOptionsPanel_CloseAutomaticallyLabel());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, htab, vgap, 0);
        add(closeSnapshotsWindowLabel, c);
        
        closeSnapshotsWindowCombo = new JComboBox(new String[] {
            Bundle.SnapshotsOptionsPanel_ItemNever(),
            Bundle.SnapshotsOptionsPanel_ItemCloseSession(),
            Bundle.SnapshotsOptionsPanel_ItemHideWindow() });
        closeSnapshotsWindowLabel.setLabelFor(closeSnapshotsWindowCombo);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, hgap, vgap, 0);
        add(closeSnapshotsWindowCombo, c);
        
        JPanel filler7 = new JPanel(null);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = y++;
        c.weightx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(filler7, c);
        
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
    
    private String selectCustomDir(String currentDir) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(currentDir));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setDialogTitle(Bundle.SnapshotsOptionsPanel_ChooseDirCaption());
        return chooser.showOpenDialog(SwingUtilities.getRoot(this)) == JFileChooser.APPROVE_OPTION ?
               chooser.getSelectedFile().getAbsolutePath() : null;
    }
    
}
