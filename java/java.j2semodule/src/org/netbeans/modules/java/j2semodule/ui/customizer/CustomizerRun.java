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

package org.netbeans.modules.java.j2semodule.ui.customizer;

import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.customizer.MainClassChooser;
import org.netbeans.modules.java.j2semodule.J2SEModularProject;
//import org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider;
//import org.netbeans.modules.java.j2seproject.api.J2SERunConfigProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.Collator;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.api.common.project.ui.ProjectUISupport;
import org.openide.util.Parameters;

public class CustomizerRun extends JPanel implements HelpCtx.Provider {
    public static final Logger log = Logger.getLogger(CustomizerRun.class.getName());
    
    private J2SEModularProject project;
    
    private DataSource[] data;
    private Map<String/*|null*/,Map<String,String/*|null*/>/*|null*/> configs;
    J2SEModularProjectProperties uiProperties;
    
//    private java.util.List<J2SECategoryExtensionProvider> compProviders = new LinkedList<J2SECategoryExtensionProvider>();
//    private J2SERunConfigProvider compProviderDeprecated;
    private int nextExtensionYPos;
    
    public CustomizerRun( J2SEModularProjectProperties uiProperties ) {
        this.uiProperties = uiProperties;
        initComponents();
        
        // NOT APPLICABLE FOR J2SEMODULARPROJECT
        lblPlatform.setVisible(false);
        platform.setVisible(false);
        jButtonManagePlatforms.setVisible(false);
        
        this.project = uiProperties.getProject();
        
        nextExtensionYPos = 0;
        // BEGIN Deprecated
//        compProviderDeprecated = Lookup.getDefault().lookup(J2SERunConfigProvider.class);
//        initExtPanel(project);
        // END Deprecated
        
//        for (J2SECategoryExtensionProvider compProvider : project.getLookup().lookupAll(J2SECategoryExtensionProvider.class)) {
//            if( compProvider.getCategory() == J2SECategoryExtensionProvider.ExtensibleCategory.RUN ) {
//                if( addExtPanel(project,compProvider,nextExtensionYPos) ) {
//                    compProviders.add(compProvider);
//                    nextExtensionYPos++;
//                }
//            }
//        }
        addPanelFiller(nextExtensionYPos);
        
        configs = uiProperties.RUN_CONFIGS;
        
//        updatePlatformsList();

        data = new DataSource[]{
//            new ComboDataSource(J2SEModularProjectProperties.PLATFORM_RUNTIME, lblPlatform, platform, configCombo, configs),
            new TextDataSource(ProjectProperties.MAIN_CLASS, jLabelMainClass, jTextFieldMainClass, configCombo, configs),
            new TextDataSource(ProjectProperties.APPLICATION_ARGS, jLabelArgs, jTextFieldArgs, configCombo, configs),
            new TextDataSource(ProjectProperties.RUN_JVM_ARGS, jLabelVMOptions, jTextVMOptions, configCombo, configs),
            new TextDataSource(ProjectProperties.RUN_WORK_DIR, jLabelWorkingDirectory, jTextWorkingDirectory, configCombo, configs),
        };        
        
        configChanged(uiProperties.activeConfig);
        
        configCombo.setRenderer(new ConfigListCellRenderer());                
        jButtonMainClass.addActionListener( new MainClassListener( project.getSourceRoots(), jTextFieldMainClass ) );
//        final ListDataListener currentSourceLevelListener = new ListDataListener() {
//            @Override
//            public void intervalAdded(ListDataEvent e) {
//            }
//
//            @Override
//            public void intervalRemoved(ListDataEvent e) {
//            }
//
//            @Override
//            public void contentsChanged(ListDataEvent e) {
//                PlatformKey currentPlatform = (PlatformKey) platform.getSelectedItem();
//                platform.setSelectedIndex(0);
//                final Collection<? extends PlatformKey> updatedPlatforms = updatePlatformsList();
//                if (updatedPlatforms.contains(currentPlatform)) {
//                    platform.setSelectedItem(currentPlatform);
//                }
//            }
//        };
//        uiProperties.JAVAC_SOURCE_MODEL.addListDataListener(currentSourceLevelListener);
//        uiProperties.JAVAC_PROFILE_MODEL.addListDataListener(currentSourceLevelListener);
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx( CustomizerRun.class );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        configSep = new javax.swing.JSeparator();
        configPanel = new javax.swing.JPanel();
        configLabel = new javax.swing.JLabel();
        configCombo = new javax.swing.JComboBox();
        configNew = new javax.swing.JButton();
        configDel = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        jLabelMainClass = new javax.swing.JLabel();
        jTextFieldMainClass = new javax.swing.JTextField();
        jButtonMainClass = new javax.swing.JButton();
        jLabelArgs = new javax.swing.JLabel();
        jTextFieldArgs = new javax.swing.JTextField();
        jLabelWorkingDirectory = new javax.swing.JLabel();
        jTextWorkingDirectory = new javax.swing.JTextField();
        jButtonWorkingDirectoryBrowse = new javax.swing.JButton();
        jLabelVMOptions = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextVMOptions = new javax.swing.JTextArea();
        jLabelVMOptionsExample = new javax.swing.JLabel();
        customizeOptionsButton = new javax.swing.JButton();
        lblPlatform = new javax.swing.JLabel();
        platform = new javax.swing.JComboBox();
        jButtonManagePlatforms = new javax.swing.JButton();
        extPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        add(configSep, gridBagConstraints);

        configPanel.setLayout(new java.awt.GridBagLayout());

        configLabel.setLabelFor(configCombo);
        org.openide.awt.Mnemonics.setLocalizedText(configLabel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        configPanel.add(configLabel, gridBagConstraints);
        configLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_CustomizerRun_Cfg")); // NOI18N

        configCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<default>" }));
        configCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 0);
        configPanel.add(configCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(configNew, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configNew")); // NOI18N
        configNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configNewActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 0);
        configPanel.add(configNew, gridBagConstraints);
        configNew.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_CustomizerRun_NewCfg")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(configDel, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.configDelete")); // NOI18N
        configDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configDelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 0);
        configPanel.add(configDel, gridBagConstraints);
        configDel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_CustomizerRun_DeleteCfg")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        add(configPanel, gridBagConstraints);

        mainPanel.setLayout(new java.awt.GridBagLayout());

        jLabelMainClass.setLabelFor(jTextFieldMainClass);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        mainPanel.add(jLabelMainClass, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        mainPanel.add(jTextFieldMainClass, gridBagConstraints);
        jTextFieldMainClass.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("AD_jTextFieldMainClass")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonMainClass, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_MainClass_JButton")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        mainPanel.add(jButtonMainClass, gridBagConstraints);
        jButtonMainClass.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("AD_jButtonMainClass")); // NOI18N

        jLabelArgs.setLabelFor(jTextFieldArgs);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelArgs, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Args_JLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        mainPanel.add(jLabelArgs, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        mainPanel.add(jTextFieldArgs, gridBagConstraints);
        jTextFieldArgs.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("AD_jTextFieldArgs")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabelWorkingDirectory, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Working_Directory")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        mainPanel.add(jLabelWorkingDirectory, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        mainPanel.add(jTextWorkingDirectory, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2semodule/ui/customizer/Bundle"); // NOI18N
        jTextWorkingDirectory.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizeRun_Run_Working_Directory ")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonWorkingDirectoryBrowse, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Working_Directory_Browse")); // NOI18N
        jButtonWorkingDirectoryBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonWorkingDirectoryBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        mainPanel.add(jButtonWorkingDirectoryBrowse, gridBagConstraints);
        jButtonWorkingDirectoryBrowse.getAccessibleContext().setAccessibleDescription(bundle.getString("AD_CustomizeRun_Run_Working_Directory_Browse")); // NOI18N

        jLabelVMOptions.setLabelFor(jTextVMOptions);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptions, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        mainPanel.add(jLabelVMOptions, gridBagConstraints);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jTextVMOptions.setColumns(20);
        jTextVMOptions.setLineWrap(true);
        jTextVMOptions.setRows(5);
        jTextVMOptions.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextVMOptionsKeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTextVMOptions);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        mainPanel.add(jScrollPane1, gridBagConstraints);

        jLabelVMOptionsExample.setLabelFor(jTextFieldMainClass);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelVMOptionsExample, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options_Example")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        mainPanel.add(jLabelVMOptionsExample, gridBagConstraints);
        jLabelVMOptionsExample.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_CustomizeRun_Run_VM_Options_Example")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(customizeOptionsButton, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_VM_Options_JButton")); // NOI18N
        customizeOptionsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customizeVMOptionsByDialog(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        mainPanel.add(customizeOptionsButton, gridBagConstraints);
        customizeOptionsButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AN_CustomizeRun_Run_VM_Options_JButton")); // NOI18N
        customizeOptionsButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerRun.class, "AD_CustomizeRun_Run_VM_Options_Customize")); // NOI18N

        lblPlatform.setLabelFor(platform);
        org.openide.awt.Mnemonics.setLocalizedText(lblPlatform, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_RuntimePlatform")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        mainPanel.add(lblPlatform, gridBagConstraints);

        platform.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        platform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                platformActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        mainPanel.add(platform, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButtonManagePlatforms, org.openide.util.NbBundle.getMessage(CustomizerRun.class, "LBL_ManagePlatforms")); // NOI18N
        jButtonManagePlatforms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonManagePlatformsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        mainPanel.add(jButtonManagePlatforms, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.8;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        add(mainPanel, gridBagConstraints);

        extPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(extPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
//    private java.util.List<PlatformKey> updatePlatformsList() {
//        final java.util.List<PlatformKey> platformList = new ArrayList<>();
//        final SpecificationVersion targetLevel = getProjectTargetLevel();
//        final SourceLevelQuery.Profile targetProfile = getProjectProfile();
//        if (targetLevel != null && targetProfile != null) {
//            for (J2SERuntimePlatformProvider rpt : project.getLookup().lookupAll(J2SERuntimePlatformProvider.class)) {
//                for (JavaPlatform jp : rpt.getPlatformType(targetLevel, targetProfile)) {
//                    platformList.add(PlatformKey.create(jp));
//                }
//            }
//            Collections.sort(platformList);
//        }
//        platformList.add(0, PlatformKey.createDefault());
//        final DefaultComboBoxModel<PlatformKey> model = new DefaultComboBoxModel<>(platformList.toArray(new PlatformKey[0]));
//        platform.setModel(model);
//        return platformList;
//    }

//    @CheckForNull
//    private SpecificationVersion getProjectTargetLevel() {
//        final Object key = uiProperties.JAVAC_SOURCE_MODEL.getSelectedItem();
//        return key == null ?
//            null :
//            PlatformUiSupport.getSourceLevel(key);
//    }
//
//    @CheckForNull
//    private SourceLevelQuery.Profile getProjectProfile() {
//        final Object key = uiProperties.JAVAC_PROFILE_MODEL.getSelectedItem();
//        return key == null ?
//            null :
//            PlatformUiSupport.getProfile(key);
//    }

//    @Deprecated
//    private void initExtPanel(Project p) {
//        if (compProviderDeprecated != null) {
//            J2SERunConfigProvider.ConfigChangeListener ccl = new J2SERunConfigProvider.ConfigChangeListener() {
//                public void propertiesChanged(Map<String, String> updates) {
//                    // update active configuration
//                    Map<String,String> m = configs.get(uiProperties.activeConfig);
//                    m.putAll(updates);
//                }
//            };
//            JComponent comp = compProviderDeprecated.createComponent(p, ccl);
//            if (comp != null) {
//                java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
//                constraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//                constraints.gridx = 0;
//                constraints.gridy = nextExtensionYPos++;
//                constraints.weightx = 1.0;
//                extPanel.add(comp, constraints);
//            }
//        }
//    }
    
//    private boolean addExtPanel(Project p, J2SECategoryExtensionProvider compProvider, int gridY) {
//        if (compProvider != null) {
//            J2SECategoryExtensionProvider.ConfigChangeListener ccl = new J2SECategoryExtensionProvider.ConfigChangeListener() {
//                public void propertiesChanged(Map<String, String> updates) {
//                    // update active configuration
//                    Map<String,String> m = configs.get(uiProperties.activeConfig);
//                    m.putAll(updates);
//                }
//            };
//            JComponent comp = compProvider.createComponent(p, ccl);
//            if (comp != null) {
//                java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
//                constraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
//                constraints.gridx = 0;
//                constraints.gridy = gridY;
//                constraints.weightx = 1.0;
//                extPanel.add(comp, constraints);
//                return true;
//            }
//        }
//        return false;
//    }

    private void addPanelFiller(int gridY) {
        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        constraints.gridx = 0;
        constraints.gridy = gridY;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        extPanel.add( new Box.Filler(
                new Dimension(), 
                new Dimension(),
                new Dimension(10000,10000) ),
                constraints);
    }
    
    private boolean createNewConfiguration(boolean platformChanged) {
        DialogDescriptor d = new DialogDescriptor(new CreateConfigurationPanel(platformChanged), NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.input.title"));        
        
        if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
            return false;
        }
        String name = ((CreateConfigurationPanel) d.getMessage()).getConfigName();
        String config = name.replaceAll("[^a-zA-Z0-9_.-]", "_"); // NOI18N
        if (config.trim().length() == 0) {
            //#143764
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.input.empty", config), // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE));
            return false;

        }
        if (configs.get(config) != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.input.duplicate", config), // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE));
            return false;
        }
        Map<String,String> m = new HashMap<String,String>();
        if (!name.equals(config)) {
            m.put("$label", name); // NOI18N
        }
        configs.put(config, m);
        configChanged(config);
        uiProperties.activeConfig = config;
        return true;
    }  
    
    private void configDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configDelActionPerformed
        String config = (String) configCombo.getSelectedItem();
        assert config != null;
        configs.put(config, null);
        configChanged(null);
        uiProperties.activeConfig = null;
    }//GEN-LAST:event_configDelActionPerformed

    private void configNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configNewActionPerformed
        createNewConfiguration(false);
    }//GEN-LAST:event_configNewActionPerformed

    private void configComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configComboActionPerformed
        String config = (String) configCombo.getSelectedItem();
        if (config.length() == 0) {
            config = null;
            platform.setSelectedIndex(0);
        }
        configChanged(config);
        uiProperties.activeConfig = config;
    }//GEN-LAST:event_configComboActionPerformed

    private void jButtonWorkingDirectoryBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonWorkingDirectoryBrowseActionPerformed
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        
        String workDir = jTextWorkingDirectory.getText();
        if (workDir.equals("")) {
            workDir = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
        }
        chooser.setSelectedFile(new File(workDir));
        chooser.setDialogTitle(NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_Working_Directory_Browse_Title")); // NOI18N
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            jTextWorkingDirectory.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonWorkingDirectoryBrowseActionPerformed

    private void customizeVMOptionsByDialog(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customizeVMOptionsByDialog
        String origin = jTextVMOptions.getText();
        try {
            String result = ProjectUISupport.showVMOptionCustomizer(SwingUtilities.getWindowAncestor(this), origin);
            jTextVMOptions.setText(result);
        } catch (Exception e) {
            log.log(Level.WARNING, "Cannot parse vm options.", e); // NOI18N
        }
    }//GEN-LAST:event_customizeVMOptionsByDialog

    private void jButtonManagePlatformsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonManagePlatformsActionPerformed
//        PlatformKey currentPlatform = (PlatformKey) platform.getSelectedItem();
//        platform.setSelectedIndex(0);
//
//        JavaPlatform jp = ((PlatformKey) this.platform.getSelectedItem()).getPlatform();
//        PlatformsCustomizer.showCustomizer(jp);
//
//        java.util.List<PlatformKey> updatedPlatforms = updatePlatformsList();
//        if (updatedPlatforms.contains(currentPlatform)) {
//            platform.setSelectedItem(currentPlatform);
//        }
    }//GEN-LAST:event_jButtonManagePlatformsActionPerformed

    private void platformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_platformActionPerformed
//        String config = (String) configCombo.getSelectedItem();
//        PlatformKey currentPlatform = ((PlatformKey) platform.getSelectedItem());
//        String currentPlatformName = ((PlatformKey) platform.getSelectedItem()).displayName;
//        if (config.isEmpty() && !currentPlatformName.equals(NbBundle.getMessage(CustomizerRun.class, "TXT_ActivePlatform"))) { //NOI18N
//            platform.setSelectedIndex(0);
//            if (createNewConfiguration(true)) {
//                platform.setSelectedItem(currentPlatform);
//            }
//        }
    }//GEN-LAST:event_platformActionPerformed

    private void jTextVMOptionsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextVMOptionsKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume();
        }
        if (evt.getKeyCode() == KeyEvent.VK_TAB) {
            if (evt.getModifiers() > 0) {
                jTextVMOptions.transferFocusBackward();
            } else {
                jTextVMOptions.transferFocus();
            }
            evt.consume();
        }
    }//GEN-LAST:event_jTextVMOptionsKeyPressed

    private void configChanged(String activeConfig) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("");
        SortedSet<String> alphaConfigs = new TreeSet<String>(new Comparator<String>() {
            Collator coll = Collator.getInstance();
            public int compare(String s1, String s2) {
                return coll.compare(label(s1), label(s2));
            }
            private String label(String c) {
                Map<String,String> m = configs.get(c);
                String label = m.get("$label"); // NOI18N
                return label != null ? label : c;
            }
        });
        for (Map.Entry<String,Map<String,String>> entry : configs.entrySet()) {
            String config = entry.getKey();
            if (config != null && entry.getValue() != null) {
                alphaConfigs.add(config);
            }
        }
        for (String c : alphaConfigs) {
            model.addElement(c);
        }
        configCombo.setModel(model);
        configCombo.setSelectedItem(activeConfig != null ? activeConfig : "");
        Map<String,String> m = configs.get(activeConfig);
        if (m != null) {
//            // BEGIN Deprecated
//            if (compProviderDeprecated != null) {
//                compProviderDeprecated.configUpdated(m);
//            }
//            // END Deprecated
//            for(J2SECategoryExtensionProvider compProvider : compProviders) {
//                compProvider.configUpdated(m);
//            }
            for (DataSource ds : data) {
                ds.update(activeConfig);
            }
        } // else ??
        configDel.setEnabled(activeConfig != null);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox configCombo;
    private javax.swing.JButton configDel;
    private javax.swing.JLabel configLabel;
    private javax.swing.JButton configNew;
    private javax.swing.JPanel configPanel;
    private javax.swing.JSeparator configSep;
    private javax.swing.JButton customizeOptionsButton;
    private javax.swing.JPanel extPanel;
    private javax.swing.JButton jButtonMainClass;
    private javax.swing.JButton jButtonManagePlatforms;
    private javax.swing.JButton jButtonWorkingDirectoryBrowse;
    private javax.swing.JLabel jLabelArgs;
    private javax.swing.JLabel jLabelMainClass;
    private javax.swing.JLabel jLabelVMOptions;
    private javax.swing.JLabel jLabelVMOptionsExample;
    private javax.swing.JLabel jLabelWorkingDirectory;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFieldArgs;
    private javax.swing.JTextField jTextFieldMainClass;
    private javax.swing.JTextArea jTextVMOptions;
    private javax.swing.JTextField jTextWorkingDirectory;
    private javax.swing.JLabel lblPlatform;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JComboBox platform;
    // End of variables declaration//GEN-END:variables
    
    
    // Innercasses -------------------------------------------------------------
    
    private final class MainClassListener implements ActionListener /*, DocumentListener */ {
        
        private final JButton okButton;
        private SourceRoots sourceRoots;
        private JTextField mainClassTextField;
        
        MainClassListener( SourceRoots sourceRoots, JTextField mainClassTextField ) {            
            this.sourceRoots = sourceRoots;
            this.mainClassTextField = mainClassTextField;
            this.okButton  = new JButton (NbBundle.getMessage (CustomizerRun.class, "LBL_ChooseMainClass_OK")); // NOI18N
            this.okButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage (CustomizerRun.class, "AD_ChooseMainClass_OK")); // NOI18N
        }
        
        // Implementation of ActionListener ------------------------------------
        
        /** Handles button events
         */        
        public void actionPerformed( ActionEvent e ) {
            
            // only chooseMainClassButton can be performed
            
            final MainClassChooser panel = new MainClassChooser (sourceRoots.getRoots(), null, mainClassTextField.getText());
            Object[] options = new Object[] {
                okButton,
                DialogDescriptor.CANCEL_OPTION
            };
            panel.addChangeListener (new ChangeListener () {
               public void stateChanged(ChangeEvent e) {
                   if (e.getSource () instanceof MouseEvent && MouseUtils.isDoubleClick (((MouseEvent)e.getSource ()))) {
                       // click button and finish the dialog with selected class
                       okButton.doClick ();
                   } else {
                       okButton.setEnabled (panel.getSelectedMainClass () != null);
                   }
               }
            });
            okButton.setEnabled (false);
            DialogDescriptor desc = new DialogDescriptor (
                panel,
                NbBundle.getMessage (CustomizerRun.class, "LBL_ChooseMainClass_Title" ), // NOI18N
                true, 
                options, 
                options[0], 
                DialogDescriptor.BOTTOM_ALIGN, 
                null, 
                null);
            //desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
            Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
            dlg.setVisible (true);
            if (desc.getValue() == options[0]) {
               mainClassTextField.setText (panel.getSelectedMainClass ());
            } 
            dlg.dispose();
        }
        
    }
    
    private final class ConfigListCellRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public ConfigListCellRenderer () {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            String config = (String) value;
            String label;
            if (config == null) {
                // uninitialized?
                label = null;
            } else if (config.length() > 0) {
                Map<String,String> m = configs.get(config);
                label = m != null ? m.get("$label") : /* temporary? */ null; // NOI18N
                if (label == null) {
                    label = config;
                }
            } else {
                label = NbBundle.getBundle("org.netbeans.modules.java.j2semodule.Bundle").getString("J2SEModularConfigurationProvider.default.label"); // NOI18N
            }
            setText(label);
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
        
        // #93658: GTK needs name to render cell renderer "natively"
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
        
    }

//    private static final class PlatformKey implements Comparable<PlatformKey> {
//
//        private final JavaPlatform platform;
//        private final String displayName;
//
//        private PlatformKey() {
//            this.displayName = NbBundle.getMessage(
//                CustomizerRun.class,
//                "TXT_ActivePlatform");
//            this.platform = null;
//        }
//
//        private PlatformKey(@NonNull final JavaPlatform platform) {
//            this.displayName = platform.getDisplayName();
//            this.platform = platform;
//        }
//
//        @Override
//        public String toString() {
//            return displayName;
//        }
//
//        @Override
//        public int hashCode() {
//            return platform == null ? 17 : platform.hashCode();
//        }
//
//        @Override
//        public boolean equals(@NullAllowed final Object obj) {
//            if (obj == this) {
//                return true;
//            }
//            if (!(obj instanceof PlatformKey)) {
//                return false;
//            }
//            final PlatformKey pk = (PlatformKey) obj;
//            return platform == null ? pk.platform == null : platform.equals(pk.platform);
//        }
//
//        @NonNull
//        String getPlatformAntName() {
//            String antName = platform == null ?
//                "" :    //NOI18N
//                platform.getProperties().get(J2SEModularProjectProperties.PROP_PLATFORM_ANT_NAME);
//            assert antName != null;
//            return antName;
//        }
//
//        @CheckForNull
//        JavaPlatform getPlatform() {
//            return platform;
//        }
//
//        static PlatformKey create(@NonNull final JavaPlatform platform) {
//            return new PlatformKey(platform);
//        }
//
//        static PlatformKey createDefault() {
//            return new PlatformKey();
//        }
//
//        @Override
//        public int compareTo(PlatformKey o) {
//            return this.displayName.toLowerCase().compareTo(o.displayName.toLowerCase());
//        }
//    }
    
    private abstract static class DataSource {

        private final String propName;
        private final JLabel label;
        private final JComboBox<?> configCombo;
        private final Map<String,Map<String,String>> configs;
        private final Font basefont;
        private final Font boldfont;


        DataSource(
            @NonNull final String propName,
            @NonNull final JLabel label,
            @NonNull final JComboBox<?> configCombo,
            @NonNull final Map<String,Map<String,String>> configs) {
            Parameters.notNull("propName", propName);   //NOI18N
            Parameters.notNull("label", label);         //NOI18N
            Parameters.notNull("configCombo", configCombo); //NOI18N
            Parameters.notNull("configs", configs); //NOI18N
            this.propName = propName;
            this.label = label;
            this.configCombo = configCombo;
            this.configs = configs;
            basefont = label.getFont();
            boldfont = basefont.deriveFont(Font.BOLD);
        }

        final String getPropertyName() {
            return propName;
        }

        final JLabel getLabel() {
            return label;
        }

        final void changed(@NullAllowed String value) {
            String config = (String) configCombo.getSelectedItem();
            if (config.length() == 0) {
                config = null;
            }
            if (value != null && config != null && value.equals(configs.get(null).get(propName))) {
                // default value, do not store as such
                value = null;
            }
            configs.get(config).put(propName, value);
            updateFont(value);
        }

        final void updateFont(@NullAllowed String value) {
            String config = (String) configCombo.getSelectedItem();
            if (config.length() == 0) {
                config = null;
            }
            String def = configs.get(null).get(propName);
            label.setFont(config != null && !Utilities.compareObjects(
                value != null ? value : "", def != null ? def : "") ? boldfont : basefont);
        }

        @CheckForNull
        final String getPropertyValue(
            @NullAllowed String config,
            @NonNull String key) {
            final Map<String,String> m = configs.get(config);
            String v = m.get(key);
            if (v == null) {
                // display default value
                final Map<String,String> def = configs.get(null);
                v = def.get(getPropertyName());
            }
            return v;
        }

        abstract String getPropertyValue();

        abstract void update(@NullAllowed String activeConfig);
    }

    private static class TextDataSource extends DataSource {

        private final JTextComponent textComp;

        TextDataSource(
            @NonNull final String propName,
            @NonNull final JLabel label,
            @NonNull final JTextComponent textComp,
            @NonNull final JComboBox<?> configCombo,
            @NonNull final Map<String,Map<String,String>> configs) {
            super(propName, label, configCombo, configs);
            Parameters.notNull("textComp", textComp);   //NOI18N
            this.textComp = textComp;
            this.textComp.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    changed(textComp.getText());
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    changed(textComp.getText());
                }
                @Override
                public void changedUpdate(DocumentEvent e) {}

            });
            updateFont(textComp.getText());
        }

        @Override
        String getPropertyValue() {
            return textComp.getText();
        }

        @Override
        void update(@NullAllowed final String activeConfig) {
            textComp.setText(getPropertyValue(activeConfig, getPropertyName()));
        }
    }

//    private static class ComboDataSource extends DataSource {
//
//        private final JComboBox<PlatformKey> combo;
//
//        ComboDataSource(
//            @NonNull final String propName,
//            @NonNull final JLabel label,
//            @NonNull final JComboBox<PlatformKey> combo,
//            @NonNull final JComboBox<?> configCombo,
//            @NonNull final Map<String,Map<String,String>> configs) {
//            super(propName, label, configCombo, configs);
//            Parameters.notNull("combo", combo); //NOI18N
//            this.combo = combo;
//            this.combo.addItemListener(new ItemListener() {
//                @Override
//                public void itemStateChanged(ItemEvent e) {
//                    changed(getPropertyValue());
//                }
//            });
//            updateFont(getPropertyValue());
//        }
//
//        @Override
//        final String getPropertyValue() {
//            return ((PlatformKey)combo.getSelectedItem()).getPlatformAntName();
//        }
//
//        @Override
//        void update(String activeConfig) {
//            String antName = getPropertyValue(activeConfig, getPropertyName());
//            if (antName == null) {
//                antName = "";   //NOI18N
//            }
//            final ComboBoxModel<PlatformKey> model = combo.getModel();
//            PlatformKey active = null, project = null;
//
//            for (int i=0; i < model.getSize(); i++) {
//                final PlatformKey pk = model.getElementAt(i);
//                final String pkn = pk.getPlatformAntName();
//                if (antName.equals(pkn)) {
//                    active = pk;
//                    break;
//                }
//                if (pkn.isEmpty()) {
//                    project = pk;
//                }
//            }
//            if (active == null) {
//                active = project;
//            }
//            combo.setSelectedItem(active);
//        }
//    }

    private static class CreateConfigurationPanel extends JPanel {

        private JLabel defaultConfigPlatformMsg = new JLabel();
        private JLabel configNameLabel = new JLabel();
        private JTextField configName = new JTextField();

        public CreateConfigurationPanel(boolean showDefaultConfigMsg) {
            org.openide.awt.Mnemonics.setLocalizedText(defaultConfigPlatformMsg, NbBundle.getMessage(CustomizerRun.class, "TXT_DefaultConfigPlatformChange")); // NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(configNameLabel, NbBundle.getMessage(CustomizerRun.class, "CustomizerRun.input.prompt")); // NOI18N
            configNameLabel.setLabelFor(configName);
            defaultConfigPlatformMsg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/java/j2semodule/ui/resources/info.png"))); // NOI18N

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addComponent(configNameLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(configName, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                    .addComponent(defaultConfigPlatformMsg)
                    .addGap(0, 0, Short.MAX_VALUE)))
                    .addContainerGap()));
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(defaultConfigPlatformMsg)
                    .addGap(18, 18, 18)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(configNameLabel))));
            defaultConfigPlatformMsg.setVisible(showDefaultConfigMsg);
        }

        public String getConfigName() {
            return configName.getText();
        }
    }
}
