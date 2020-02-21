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

package org.netbeans.modules.cnd.makeproject.ui.configurations;

import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.netbeans.modules.cnd.makeproject.api.PackagerDescriptor;
import org.netbeans.modules.cnd.makeproject.api.PackagerManager;

/**
 *
 */
public class PackagingFilesOuterPanel extends javax.swing.JPanel {
    private final PackagingConfiguration packagingConfiguration;

    /** Creates new form PackagingInfo2Panel */
    public PackagingFilesOuterPanel(PackagingFilesPanel innerPanel, PackagingConfiguration packagingConfiguration) {
        this.packagingConfiguration = packagingConfiguration;
        java.awt.GridBagConstraints gridBagConstraints;
        
        initComponents();
        
        topDirectoryTextField.setText(packagingConfiguration.getTopDirValue());
        
        remove(tmpPanel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        
        // Set default values
        String val = MakeProjectOptions.getDefExePerm();
        if (val.length() != 3) {
            val = "755"; // NOI18N
        }
        exePermTextField.setText(val); // NOI18N
        val = MakeProjectOptions.getDefFilePerm();
        if (val.length() != 3) {
            val = "644"; // NOI18N
        }
        filePermTextField.setText(val); // NOI18N
        groupTextField.setText(MakeProjectOptions.getDefGroup()); // NOI18N
        ownerTextField.setText(MakeProjectOptions.getDefOwner()); // NOI18N
        
        // Hide some fields:
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(packagingConfiguration.getType().getValue());
        if (!packager.supportsGroupAndOwner()) {
            groupLabel.setEnabled(false);
            groupTextField.setEnabled(false);
            ownerLabel.setEnabled(false);
            ownerTextField.setEnabled(false);
        }
        
        innerPanel.setOuterPanel(this);
        add(innerPanel, gridBagConstraints);
        
        setPermissionCheckBoxes();
        
        RegFilePermissionActionListener regFilePermissionActionListener = new RegFilePermissionActionListener();
        rWECheckBoxRegR1.addActionListener(regFilePermissionActionListener);
        rWECheckBoxRegW1.addActionListener(regFilePermissionActionListener);
        rWECheckBoxRegE1.addActionListener(regFilePermissionActionListener);
        rWECheckBoxRegR2.addActionListener(regFilePermissionActionListener);
        rWECheckBoxRegW2.addActionListener(regFilePermissionActionListener);
        rWECheckBoxRegE2.addActionListener(regFilePermissionActionListener);
        rWECheckBoxRegR3.addActionListener(regFilePermissionActionListener);
        rWECheckBoxRegW3.addActionListener(regFilePermissionActionListener);
        rWECheckBoxRegE3.addActionListener(regFilePermissionActionListener);
        
        ExeFilePermissionActionListener exeFilePermissionActionListener = new ExeFilePermissionActionListener();
        rWECheckBoxExeR1.addActionListener(exeFilePermissionActionListener);
        rWECheckBoxExeW1.addActionListener(exeFilePermissionActionListener);
        rWECheckBoxExeE1.addActionListener(exeFilePermissionActionListener);
        rWECheckBoxExeR2.addActionListener(exeFilePermissionActionListener);
        rWECheckBoxExeW2.addActionListener(exeFilePermissionActionListener);
        rWECheckBoxExeE2.addActionListener(exeFilePermissionActionListener);
        rWECheckBoxExeR3.addActionListener(exeFilePermissionActionListener);
        rWECheckBoxExeW3.addActionListener(exeFilePermissionActionListener);
        rWECheckBoxExeE3.addActionListener(exeFilePermissionActionListener);
    }
    
    public PackagingConfiguration getPackagingConfiguration() {
        return packagingConfiguration;
    }
    
    private class RegFilePermissionActionListener implements ActionListener {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            int regValInt1 = rWECheckBoxRegR1.getVal() + rWECheckBoxRegW1.getVal() + rWECheckBoxRegE1.getVal();
            int regValInt2 = rWECheckBoxRegR2.getVal() + rWECheckBoxRegW2.getVal() + rWECheckBoxRegE2.getVal();
            int regValInt3 = rWECheckBoxRegR3.getVal() + rWECheckBoxRegW3.getVal() + rWECheckBoxRegE3.getVal();
            String regVal = "" + regValInt1 + regValInt2 + regValInt3; // NOI18N
            filePermTextField.setText(regVal);
            MakeProjectOptions.setDefFilePerm(regVal);
        }
    }
    
    private class ExeFilePermissionActionListener implements ActionListener {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            int exeValInt1 = rWECheckBoxExeR1.getVal() + rWECheckBoxExeW1.getVal() + rWECheckBoxExeE1.getVal();
            int exeValInt2 = rWECheckBoxExeR2.getVal() + rWECheckBoxExeW2.getVal() + rWECheckBoxExeE2.getVal();
            int exeValInt3 = rWECheckBoxExeR3.getVal() + rWECheckBoxExeW3.getVal() + rWECheckBoxExeE3.getVal();
            String exeVal = "" + exeValInt1 + exeValInt2 + exeValInt3; // NOI18N
            exePermTextField.setText(exeVal);
            MakeProjectOptions.setDefExePerm(exeVal);
        }
    }
    
    private void setPermissionCheckBoxes() {
        String val = filePermTextField.getText();
        setPermissionCheckBoxes(val.substring(0, 1), rWECheckBoxRegR1, rWECheckBoxRegW1, rWECheckBoxRegE1);
        setPermissionCheckBoxes(val.substring(1, 2), rWECheckBoxRegR2, rWECheckBoxRegW2, rWECheckBoxRegE2);
        setPermissionCheckBoxes(val.substring(2, 3), rWECheckBoxRegR3, rWECheckBoxRegW3, rWECheckBoxRegE3);
        
        val = exePermTextField.getText();
        setPermissionCheckBoxes(val.substring(0, 1), rWECheckBoxExeR1, rWECheckBoxExeW1, rWECheckBoxExeE1);
        setPermissionCheckBoxes(val.substring(1, 2), rWECheckBoxExeR2, rWECheckBoxExeW2, rWECheckBoxExeE2);
        setPermissionCheckBoxes(val.substring(2, 3), rWECheckBoxExeR3, rWECheckBoxExeW3, rWECheckBoxExeE3);
    }
    
    private void setPermissionCheckBoxes(String ch, JCheckBox cb1, JCheckBox cb2, JCheckBox cb3) {
        int val = Integer.parseInt(ch);
        cb1.setSelected((val & 4) == 4);
        cb2.setSelected((val & 2) == 2);
        cb3.setSelected((val & 1) == 1);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        topFolderPanel = new javax.swing.JPanel();
        topDirectoryLabel = new javax.swing.JLabel();
        topDirectoryTextField = new javax.swing.JTextField();
        tmpPanel = new javax.swing.JPanel();
        defaultsPanel = new javax.swing.JPanel();
        ownerLabel = new javax.swing.JLabel();
        ownerTextField = new javax.swing.JTextField();
        groupLabel = new javax.swing.JLabel();
        groupTextField = new javax.swing.JTextField();
        defaultValues = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        filePermLabel = new javax.swing.JLabel();
        exePermLabel = new javax.swing.JLabel();
        exePermTextField = new javax.swing.JTextField();
        filePermTextField = new javax.swing.JTextField();
        rLabel1 = new javax.swing.JLabel();
        wLabel1 = new javax.swing.JLabel();
        eLabel1 = new javax.swing.JLabel();
        rWECheckBoxRegR1 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rWECheckBoxRegW1 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rWECheckBoxRegE1 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rWECheckBoxExeR1 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rWECheckBoxExeW1 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rWECheckBoxExeE1 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rLabel2 = new javax.swing.JLabel();
        wLabel2 = new javax.swing.JLabel();
        eLabel2 = new javax.swing.JLabel();
        rWECheckBoxRegR2 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rWECheckBoxRegW2 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rWECheckBoxRegE2 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rWECheckBoxExeR2 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rWECheckBoxExeW2 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rWECheckBoxExeE2 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rLabel3 = new javax.swing.JLabel();
        wLabel3 = new javax.swing.JLabel();
        eLabel3 = new javax.swing.JLabel();
        rWECheckBoxRegR3 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rWECheckBoxRegW3 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rWECheckBoxRegE3 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rWECheckBoxExeR3 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rWECheckBoxExeW3 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();
        rWECheckBoxExeE3 = new org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox();

        setLayout(new java.awt.GridBagLayout());

        topFolderPanel.setLayout(new java.awt.GridBagLayout());

        topDirectoryLabel.setLabelFor(topDirectoryTextField);
        org.openide.awt.Mnemonics.setLocalizedText(topDirectoryLabel, org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.topDirectoryLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        topFolderPanel.add(topDirectoryLabel, gridBagConstraints);

        topDirectoryTextField.setColumns(12);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        topFolderPanel.add(topDirectoryTextField, gridBagConstraints);
        topDirectoryTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.topDirectoryTextField.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(topFolderPanel, gridBagConstraints);

        javax.swing.GroupLayout tmpPanelLayout = new javax.swing.GroupLayout(tmpPanel);
        tmpPanel.setLayout(tmpPanelLayout);
        tmpPanelLayout.setHorizontalGroup(
            tmpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 558, Short.MAX_VALUE)
        );
        tmpPanelLayout.setVerticalGroup(
            tmpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 197, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        add(tmpPanel, gridBagConstraints);

        defaultsPanel.setLayout(new java.awt.GridBagLayout());

        ownerLabel.setLabelFor(ownerTextField);
        org.openide.awt.Mnemonics.setLocalizedText(ownerLabel, org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.ownerLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        defaultsPanel.add(ownerLabel, gridBagConstraints);

        ownerTextField.setColumns(5);
        ownerTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ownerTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        defaultsPanel.add(ownerTextField, gridBagConstraints);
        ownerTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.ownerTextField.AccessibleContext.accessibleDescription")); // NOI18N

        groupLabel.setLabelFor(groupTextField);
        org.openide.awt.Mnemonics.setLocalizedText(groupLabel, org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.groupLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        defaultsPanel.add(groupLabel, gridBagConstraints);

        groupTextField.setColumns(5);
        groupTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groupTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        defaultsPanel.add(groupTextField, gridBagConstraints);
        groupTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.groupTextField.AccessibleContext.accessibleDescription")); // NOI18N

        defaultValues.setText(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.defaultValues.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        defaultsPanel.add(defaultValues, gridBagConstraints);

        jLabel4.setText(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.jLabel4.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 16, 0, 0);
        defaultsPanel.add(jLabel4, gridBagConstraints);

        filePermLabel.setLabelFor(rWECheckBoxRegR1);
        org.openide.awt.Mnemonics.setLocalizedText(filePermLabel, org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.filePermLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        defaultsPanel.add(filePermLabel, gridBagConstraints);

        exePermLabel.setLabelFor(rWECheckBoxExeR1);
        org.openide.awt.Mnemonics.setLocalizedText(exePermLabel, org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.exePermLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        defaultsPanel.add(exePermLabel, gridBagConstraints);

        exePermTextField.setEditable(false);
        exePermTextField.setColumns(3);
        exePermTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exePermTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        defaultsPanel.add(exePermTextField, gridBagConstraints);
        exePermTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.exePermTextField.AccessibleContext.accessibleName")); // NOI18N
        exePermTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.exePermTextField.AccessibleContext.accessibleDescription")); // NOI18N

        filePermTextField.setEditable(false);
        filePermTextField.setColumns(3);
        filePermTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filePermTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        defaultsPanel.add(filePermTextField, gridBagConstraints);
        filePermTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.filePermTextField.AccessibleContext.accessibleName")); // NOI18N
        filePermTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.filePermTextField.AccessibleContext.accessibleDescription")); // NOI18N

        rLabel1.setText(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.rLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 0, 0);
        defaultsPanel.add(rLabel1, gridBagConstraints);

        wLabel1.setText(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.wLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        defaultsPanel.add(wLabel1, gridBagConstraints);

        eLabel1.setText(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.eLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        defaultsPanel.add(eLabel1, gridBagConstraints);

        rWECheckBoxRegR1.setVal(4);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        defaultsPanel.add(rWECheckBoxRegR1, gridBagConstraints);

        rWECheckBoxRegW1.setVal(2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        defaultsPanel.add(rWECheckBoxRegW1, gridBagConstraints);

        rWECheckBoxRegE1.setVal(1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        defaultsPanel.add(rWECheckBoxRegE1, gridBagConstraints);

        rWECheckBoxExeR1.setVal(4);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        defaultsPanel.add(rWECheckBoxExeR1, gridBagConstraints);

        rWECheckBoxExeW1.setVal(2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        defaultsPanel.add(rWECheckBoxExeW1, gridBagConstraints);

        rWECheckBoxExeE1.setVal(1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        defaultsPanel.add(rWECheckBoxExeE1, gridBagConstraints);

        rLabel2.setText(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.rLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 6, 0, 0);
        defaultsPanel.add(rLabel2, gridBagConstraints);

        wLabel2.setText(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.wLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        defaultsPanel.add(wLabel2, gridBagConstraints);

        eLabel2.setText(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.eLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        defaultsPanel.add(eLabel2, gridBagConstraints);

        rWECheckBoxRegR2.setVal(4);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        defaultsPanel.add(rWECheckBoxRegR2, gridBagConstraints);

        rWECheckBoxRegW2.setVal(2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 2;
        defaultsPanel.add(rWECheckBoxRegW2, gridBagConstraints);

        rWECheckBoxRegE2.setVal(1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 2;
        defaultsPanel.add(rWECheckBoxRegE2, gridBagConstraints);

        rWECheckBoxExeR2.setVal(4);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        defaultsPanel.add(rWECheckBoxExeR2, gridBagConstraints);

        rWECheckBoxExeW2.setVal(2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 3;
        defaultsPanel.add(rWECheckBoxExeW2, gridBagConstraints);

        rWECheckBoxExeE2.setVal(1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 3;
        defaultsPanel.add(rWECheckBoxExeE2, gridBagConstraints);

        rLabel3.setText(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.rLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 6, 0, 0);
        defaultsPanel.add(rLabel3, gridBagConstraints);

        wLabel3.setText(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.wLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        defaultsPanel.add(wLabel3, gridBagConstraints);

        eLabel3.setText(org.openide.util.NbBundle.getMessage(PackagingFilesOuterPanel.class, "PackagingFilesOuterPanel.eLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        defaultsPanel.add(eLabel3, gridBagConstraints);

        rWECheckBoxRegR3.setVal(4);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        defaultsPanel.add(rWECheckBoxRegR3, gridBagConstraints);

        rWECheckBoxRegW3.setVal(2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 2;
        defaultsPanel.add(rWECheckBoxRegW3, gridBagConstraints);

        rWECheckBoxRegE3.setVal(1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 2;
        defaultsPanel.add(rWECheckBoxRegE3, gridBagConstraints);

        rWECheckBoxExeR3.setVal(4);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        defaultsPanel.add(rWECheckBoxExeR3, gridBagConstraints);

        rWECheckBoxExeW3.setVal(2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 3;
        defaultsPanel.add(rWECheckBoxExeW3, gridBagConstraints);

        rWECheckBoxExeE3.setVal(1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 3;
        defaultsPanel.add(rWECheckBoxExeE3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 4, 4, 4);
        add(defaultsPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void exePermTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exePermTextFieldActionPerformed
    MakeProjectOptions.setDefExePerm(exePermTextField.getText());
}//GEN-LAST:event_exePermTextFieldActionPerformed

private void filePermTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filePermTextFieldActionPerformed
    MakeProjectOptions.setDefFilePerm(filePermTextField.getText());
}//GEN-LAST:event_filePermTextFieldActionPerformed

private void groupTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groupTextFieldActionPerformed
    MakeProjectOptions.setDefGroup(groupTextField.getText());
}//GEN-LAST:event_groupTextFieldActionPerformed

private void ownerTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ownerTextFieldActionPerformed
    MakeProjectOptions.setDefOwner(ownerTextField.getText());
}//GEN-LAST:event_ownerTextFieldActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel defaultValues;
    private javax.swing.JPanel defaultsPanel;
    private javax.swing.JLabel eLabel1;
    private javax.swing.JLabel eLabel2;
    private javax.swing.JLabel eLabel3;
    private javax.swing.JLabel exePermLabel;
    private javax.swing.JTextField exePermTextField;
    private javax.swing.JLabel filePermLabel;
    private javax.swing.JTextField filePermTextField;
    private javax.swing.JLabel groupLabel;
    private javax.swing.JTextField groupTextField;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel ownerLabel;
    private javax.swing.JTextField ownerTextField;
    private javax.swing.JLabel rLabel1;
    private javax.swing.JLabel rLabel2;
    private javax.swing.JLabel rLabel3;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxExeE1;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxExeE2;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxExeE3;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxExeR1;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxExeR2;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxExeR3;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxExeW1;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxExeW2;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxExeW3;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxRegE1;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxRegE2;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxRegE3;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxRegR1;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxRegR2;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxRegR3;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxRegW1;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxRegW2;
    private org.netbeans.modules.cnd.makeproject.ui.configurations.RWECheckBox rWECheckBoxRegW3;
    private javax.swing.JPanel tmpPanel;
    private javax.swing.JLabel topDirectoryLabel;
    private javax.swing.JTextField topDirectoryTextField;
    private javax.swing.JPanel topFolderPanel;
    private javax.swing.JLabel wLabel1;
    private javax.swing.JLabel wLabel2;
    private javax.swing.JLabel wLabel3;
    // End of variables declaration//GEN-END:variables

    public javax.swing.JTextField getDirPermTextField() {
        return exePermTextField;
    }

    public javax.swing.JTextField getFilePermTextField() {
        return filePermTextField;
    }

    public javax.swing.JTextField getGroupTextField() {
        return groupTextField;
    }

    public javax.swing.JTextField getOwnerTextField() {
        return ownerTextField;
    }
    
    public javax.swing.JTextField getTopDirectoryTextField() {
        return topDirectoryTextField;
    }
}
