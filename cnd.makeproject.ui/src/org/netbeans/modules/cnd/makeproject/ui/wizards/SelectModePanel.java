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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import org.netbeans.modules.cnd.makeproject.ui.utils.ExpandableEditableComboBox;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JFileChooser;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectUtils;
import org.netbeans.modules.cnd.makeproject.api.wizards.BuildSupport;
import org.netbeans.modules.cnd.makeproject.api.wizards.BuildSupport.BuildFile;
import org.netbeans.modules.cnd.makeproject.api.wizards.BuildSupport.BuildFileProvider;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport.PreBuildArtifact;
import org.netbeans.modules.cnd.makeproject.api.wizards.PreBuildSupport.PreBuildArtifactProvider;
import org.netbeans.modules.cnd.makeproject.ui.wizards.PanelProjectLocationVisual.DevHostsInitializer;
import org.netbeans.modules.cnd.makeproject.ui.wizards.PanelProjectLocationVisual.ToolCollectionItem;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class SelectModePanel extends javax.swing.JPanel {
    private static final int VERIFY_DELAY = 300;

    private final SelectModeDescriptorPanel controller;
    private volatile boolean initialized = false;
    private volatile boolean firstTime = true;
    private static final String SOURCES_FILE_KEY = "sourcesField"; // NOI18N
    private ExecutionEnvironment env;
    private FileSystem fileSystem;
    private static final RequestProcessor RP = new RequestProcessor("SelectModePanel", 1); // NOI18N
    private static final RequestProcessor RP2 = new RequestProcessor("SelectRoot", 1); // NOI18N
    private final RequestProcessor.Task refreshSourceFolderTask;
    private final RefreshRunnable refreshRunnable;
    private final AtomicBoolean updateHost = new AtomicBoolean(false);

    /** Creates new form SelectModePanel */
    public SelectModePanel(SelectModeDescriptorPanel controller) {
        this.controller = controller;
        initComponents();
        instructions.setEditorKit(new HTMLEditorKit());
        instructions.setBackground(instructionPanel.getBackground());
        instructions.setForeground(instructionPanel.getForeground());
        instructions.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        disableHostSensitiveComponents();
        refreshRunnable = new RefreshRunnable();
        refreshSourceFolderTask = RP2.create(refreshRunnable);
        addListeners();
    }
    
    private void addListeners(){
        ((ExpandableEditableComboBox)sourceFolder).addChangeListener((ActionEvent e) -> {
            refreshInstruction(true);
        });
        simpleMode.addActionListener((ActionEvent e) -> {
            refreshInstruction(false);
        });
        advancedMode.addActionListener((ActionEvent e) -> {
            refreshInstruction(false);
        });
    }
    
    private void refreshInstruction(boolean refreshRoot) {
        controller.invalidate();
        refreshSourceFolderTask.cancel();
        controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(SelectModePanel.class, "SelectModeError0")); // NOI18N
        refreshRunnable.setRefreshRoot(refreshRoot);
        refreshSourceFolderTask.schedule(VERIFY_DELAY);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        instructionPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        instructions = new javax.swing.JTextPane();
        simpleMode = new javax.swing.JRadioButton();
        advancedMode = new javax.swing.JRadioButton();
        modeLabel = new javax.swing.JLabel();
        toolchainComboBox = new javax.swing.JComboBox();
        toolchainLabel = new javax.swing.JLabel();
        hostComboBox = new javax.swing.JComboBox();
        hostLabel = new javax.swing.JLabel();
        sourceFolderLabel = new javax.swing.JLabel();
        sourceBrowseButton = new javax.swing.JButton();
        sourceFolder = new ExpandableEditableComboBox();
        useBuildAnalyzerCheckBox = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(450, 350));
        setLayout(new java.awt.GridBagLayout());

        instructionPanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(200, 200));

        instructions.setEditable(false);
        instructions.setBorder(null);
        instructions.setFocusable(false);
        instructions.setOpaque(false);
        jScrollPane1.setViewportView(instructions);

        instructionPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(instructionPanel, gridBagConstraints);

        buttonGroup1.add(simpleMode);
        simpleMode.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(simpleMode, bundle.getString("SimpleModeButtonText")); // NOI18N
        simpleMode.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
        add(simpleMode, gridBagConstraints);

        buttonGroup1.add(advancedMode);
        org.openide.awt.Mnemonics.setLocalizedText(advancedMode, bundle.getString("AdvancedModeButtonText")); // NOI18N
        advancedMode.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
        add(advancedMode, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(modeLabel, bundle.getString("SelectModeLabelText")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(modeLabel, gridBagConstraints);

        toolchainComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                toolchainComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(toolchainComboBox, gridBagConstraints);

        toolchainLabel.setLabelFor(toolchainComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(toolchainLabel, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "LBL_TOOLCHAIN")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 8, 0);
        add(toolchainLabel, gridBagConstraints);

        hostComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                hostComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(hostComboBox, gridBagConstraints);

        hostLabel.setLabelFor(hostComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "LBL_HOST")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 8, 0);
        add(hostLabel, gridBagConstraints);

        sourceFolderLabel.setLabelFor(sourceFolder);
        org.openide.awt.Mnemonics.setLocalizedText(sourceFolderLabel, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "SELECT_MODE_SOURCES_FOLDER")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(sourceFolderLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(sourceBrowseButton, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "SELECT_MODE_BROWSE_PROJECT_FOLDER")); // NOI18N
        sourceBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceBrowseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        add(sourceBrowseButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        add(sourceFolder, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(useBuildAnalyzerCheckBox, org.openide.util.NbBundle.getMessage(SelectModePanel.class, "UseBuildAnalyzer")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 8, 0);
        add(useBuildAnalyzerCheckBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void toolchainComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_toolchainComboBoxItemStateChanged
        if (!initialized) {
            return;
        }
        if (updateHost.get()) {
            return;
        }
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            controller.getWizardStorage().setCompilerSet(getSelectedCompilerSet());
            refreshInstruction(false);
        }
    }//GEN-LAST:event_toolchainComboBoxItemStateChanged

    private void hostComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_hostComboBoxItemStateChanged
        if (!initialized) {
            return;
        }
        try {
            updateHost.set(true);
            if (evt.getStateChange() == ItemEvent.SELECTED) {
                ServerRecord newItem = (ServerRecord) evt.getItem();
                PanelProjectLocationVisual.updateToolchains(toolchainComboBox, newItem);
                controller.getWizardStorage().setExecutionEnvironment(getSelectedExecutionEnvironment());
                controller.getWizardStorage().setCompilerSet(getSelectedCompilerSet());
                refreshInstruction(false);
            }
        } finally {
            updateHost.set(false);
        }
    }//GEN-LAST:event_hostComboBoxItemStateChanged

    private void sourceBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceBrowseButtonActionPerformed
        String path = ((ExpandableEditableComboBox)sourceFolder).getText();
        if (path.isEmpty()) { 
            path = SelectModePanel.getDefaultDirectory(env);
        }
        String approveButtonText = NbBundle.getMessage(SelectModePanel.class, "SOURCES_DIR_BUTTON_TXT"); // NOI18N
        String title = NbBundle.getMessage(SelectModePanel.class, "SOURCES_DIR_CHOOSER_TITLE_TXT"); //NOI18N
        JFileChooser fileChooser = NewProjectWizardUtils.createFileChooser(
                controller.getWizardDescriptor(),
                title,
                approveButtonText,
                JFileChooser.DIRECTORIES_ONLY,
                null,
                path,
                false);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        File selectedFile = fileChooser.getSelectedFile();
        if (selectedFile != null) { // seems paranoidal, but once I've seen NPE otherwise 8-()
            path = selectedFile.getPath();
            ((ExpandableEditableComboBox)sourceFolder).setText(path);
        }
    }//GEN-LAST:event_sourceBrowseButtonActionPerformed

    public static String getDefaultDirectory(ExecutionEnvironment env) {
        String home;
        if (env.isLocal()) {
            home = System.getProperty("user.home"); // NOI18N
        } else if (!(HostInfoUtils.isHostInfoAvailable(env) && ConnectionManager.getInstance().isConnectedTo(env))) {
            home = null;
        } else {
            try {
                home = HostInfoUtils.getHostInfo(env).getUserDir();
            } catch (IOException | ConnectionManager.CancellationException ex) {
                home = null;
            }
        }
        return home == null ? "" : home; // NOI18N
    }
   
    void read(WizardDescriptor wizardDescriptor) {
        initialized = false;
        env = WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV.get(wizardDescriptor);
        if (env != null) {
            WizardConstants.PROPERTY_HOST_UID.put(wizardDescriptor, ExecutionEnvironmentFactory.toUniqueID(env));
        } else {
            env = ExecutionEnvironmentFactory.getLocal();
        }
        fileSystem = FileSystemProvider.getFileSystem(env);
        ((ExpandableEditableComboBox)sourceFolder).setStorage(SOURCES_FILE_KEY, NbPreferences.forModule(SelectModePanel.class));
        if (firstTime) {
            ((ExpandableEditableComboBox)sourceFolder).read("");
            RP.post(new Runnable() {
                @Override
                public void run() {
                    if (!SwingUtilities.isEventDispatchThread()) {
                        // init host info if it has not been inited yet.
                        try {
                            HostInfoUtils.getHostInfo(env);
                        } catch (IOException | ConnectionManager.CancellationException ex) {
                            // do nothing
                        }
                        SwingUtilities.invokeLater(this);
                    } else {
                        ((ExpandableEditableComboBox)sourceFolder).setEnv(env);
                    }
                }
            });
        }
        refreshInstruction(false);
        firstTime = false;
        
        String hostUID = WizardConstants.PROPERTY_HOST_UID.get(wizardDescriptor);
        CompilerSet cs = WizardConstants.PROPERTY_TOOLCHAIN.get(wizardDescriptor);
        boolean isDefaultCompilerSet = Boolean.TRUE.equals(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT.get(wizardDescriptor));
        RP.post(new DevHostsInitializer(hostUID, cs, isDefaultCompilerSet, false,
                WizardConstants.PROPERTY_TOOLS_CACHE_MANAGER.get(wizardDescriptor)) {
            @Override
            public void updateComponents(Collection<ServerRecord> records, ServerRecord srToSelect, CompilerSet csToSelect, boolean isDefaultCompilerSet, boolean enabled) {
                boolean enableHost = enabled;
                if (controller.isFullRemote()) {
                    enableHost = false;
                }
                enableHostSensitiveComponents(records, srToSelect, csToSelect, isDefaultCompilerSet, enableHost, enabled);
            }
        });
        useBuildAnalyzerCheckBox.setSelected(true);
    }

    private ExecutionEnvironment getSelectedExecutionEnvironment() {
        Object obj = hostComboBox.getSelectedItem();
        if (obj != null && obj instanceof ServerRecord) {
            ServerRecord sr = (ServerRecord) obj;
            return sr.getExecutionEnvironment();
        }
        return ServerList.getDefaultRecord().getExecutionEnvironment();
    }
    
    private CompilerSet getSelectedCompilerSet() {
        Object tc = toolchainComboBox.getSelectedItem();
        if (tc instanceof ToolCollectionItem) {
            return ((ToolCollectionItem) tc).getCompilerSet();
        }
        return null;
    }

    void store(WizardDescriptor wizardDescriptor) {
        WizardConstants.PROPERTY_SIMPLE_MODE.put(wizardDescriptor, simpleMode.isSelected());
        controller.getWizardStorage().setFullRemoteEnv(WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV.get(wizardDescriptor));
        WizardConstants.PROPERTY_SIMPLE_MODE_FOLDER.put(wizardDescriptor, ((ExpandableEditableComboBox)sourceFolder).getText().trim());
        ((ExpandableEditableComboBox)sourceFolder).setStorage(SOURCES_FILE_KEY, NbPreferences.forModule(SelectModePanel.class));
        ((ExpandableEditableComboBox)sourceFolder).store();
        String folderPath = ((ExpandableEditableComboBox)sourceFolder).getText().trim();
        if (WizardDescriptor.CLOSED_OPTION.equals(wizardDescriptor.getValue()) || WizardDescriptor.CANCEL_OPTION.equals(wizardDescriptor.getValue()) ) {
            return;
        }
        if (CndPathUtilities.isPathAbsolute(folderPath)) {
            String normalizeAbsolutePath = RemoteFileUtil.normalizeAbsolutePath(folderPath, env);
            FSPath path = new FSPath(fileSystem, normalizeAbsolutePath);
            WizardConstants.PROPERTY_PROJECT_FOLDER.put(wizardDescriptor, path);
        }
        WizardConstants.PROPERTY_READ_ONLY_TOOLCHAIN.put(wizardDescriptor, Boolean.TRUE);

        ExecutionEnvironment ee = getSelectedExecutionEnvironment();
        WizardConstants.PROPERTY_HOST_UID.put(wizardDescriptor, ExecutionEnvironmentFactory.toUniqueID(ee));
        controller.getWizardStorage().setExecutionEnvironment(ee);

        Object tc = toolchainComboBox.getSelectedItem();
        if (tc != null && tc instanceof ToolCollectionItem) {
            ToolCollectionItem item = (ToolCollectionItem) tc;
            WizardConstants.PROPERTY_TOOLCHAIN.put(wizardDescriptor, item.getCompilerSet());
            WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT.put(wizardDescriptor, item.isDefaultCompilerSet());
            controller.getWizardStorage().setCompilerSet(item.getCompilerSet());
            controller.getWizardStorage().setDefaultCompilerSet(item.isDefaultCompilerSet());
        }
        FileObject fo = controller.getWizardStorage().getSourcesFileObject();
        WizardConstants.PROPERTY_NATIVE_PROJ_FO.put(wizardDescriptor, fo);
        WizardConstants.PROPERTY_NATIVE_PROJ_DIR.put(wizardDescriptor, (fo == null) ? null : fo.getPath()); 
        WizardConstants.PROPERTY_USE_BUILD_ANALYZER.put(wizardDescriptor, useBuildAnalyzerCheckBox.isSelected());
        initialized = false;
    }

    private static final byte noMessage = 0;
    private static final byte notFolder = 1;
    private static final byte cannotReadFolder = 2;
    private static final byte cannotWriteFolder = 3;
    private static final byte alreadyNbPoject = 4;
    private static final byte notFoundMakeAndConfigure = 5;
    private static final byte notRoot = 6;
    private static final byte notExists = 7;
    private static final byte notAbsolute = 8;
    private byte messageKind = noMessage;

    boolean valid() {
        messageKind = noMessage;
        String path = ((ExpandableEditableComboBox)sourceFolder).getText().trim();
        try {
            if (path.length() == 0) {
                controller.getWizardStorage().setSourcesFileObject(null);
                return false;
            }
            if (!CndPathUtilities.isPathAbsolute(path)) {
                controller.getWizardStorage().setSourcesFileObject(null);
                messageKind = notAbsolute;
                return false;
            }
            FileObject projectDirFO = fileSystem.findResource(path); // can be null
            if (projectDirFO == null || !projectDirFO.isValid()) {
                controller.getWizardStorage().setSourcesFileObject(null);
                messageKind = notExists;
                return false;
            }
            if (!projectDirFO.isFolder()) {
                controller.getWizardStorage().setSourcesFileObject(null);
                messageKind = notFolder;
                return false;
            }
            if (!projectDirFO.canRead()) {
                controller.getWizardStorage().setSourcesFileObject(null);
                messageKind = cannotReadFolder;
                return false;
            }
            
            boolean simple = simpleMode.isSelected();
            SelectModePanel.this.controller.getWizardStorage().setMode(simple);            
            if (simple) {
                if (!projectDirFO.canWrite()) {
                    controller.getWizardStorage().setSourcesFileObject(null);
                    messageKind = cannotWriteFolder;
                    return false;
                }
                FileObject nbProjFO = projectDirFO.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
                if (nbProjFO != null && nbProjFO.isValid()) {
                    controller.getWizardStorage().setSourcesFileObject(null);
                    messageKind = alreadyNbPoject;
                    return false;
                }
                try {
                    Project prj = ProjectManager.getDefault().findProject(projectDirFO);
                    if (prj != null) {
                        MakeProjectUtils.forgetDeadProjectIfNeed(projectDirFO);
                        prj = ProjectManager.getDefault().findProject(projectDirFO);
                    }                        
                    if (prj != null) {
                        controller.getWizardStorage().setSourcesFileObject(null);
                        messageKind = alreadyNbPoject;
                        return false;
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            controller.getWizardStorage().setSourcesFileObject(projectDirFO);
            CompilerSet cs = null;
            Object tc = toolchainComboBox.getSelectedItem();
            if (tc != null && tc instanceof ToolCollectionItem) {
                ToolCollectionItem item = (ToolCollectionItem) tc;
                cs = item.getCompilerSet();
            }
            ExecutionEnvironment ee = getSelectedExecutionEnvironment();
            FileObject sourcesFileObject = controller.getWizardStorage().getSourcesFileObject();
            PreBuildArtifact findConfigureScripts = PreBuildSupport.findArtifactInFolder(sourcesFileObject, ee, cs);
            if (findConfigureScripts != null) {
                return true;
            }
            BuildFile makeFile = BuildSupport.findBuildFileInFolder(projectDirFO, ee, cs);
            if (makeFile != null) {
                FileObject makeFO = new FSPath(projectDirFO.getFileSystem(), makeFile.getFile()).getFileObject();
                if (makeFO != null && makeFO.isValid()) {
                    controller.getWizardStorage().setMake(makeFO);
                    return true;
                }
            }
            if (simpleMode.isSelected()) {
                messageKind = notFoundMakeAndConfigure;
                return false;
            }
            return true;
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
            messageKind = cannotReadFolder;
            return false;
        } finally {
            if (messageKind > 0) {
                controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
                        NbBundle.getMessage(SelectModePanel.class, "SelectModeError"+messageKind,path)); // NOI18N
            } else {
                controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
                if (simpleMode.isSelected()) {
                    controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                            NbBundle.getMessage(SelectModePanel.class, "CleanInfoMessageSimpleMode")); // NOI18N
                } else {
                    controller.getWizardDescriptor().putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);
                }
            }
        }
    }
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton advancedMode;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox hostComboBox;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextPane instructions;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel modeLabel;
    private javax.swing.JRadioButton simpleMode;
    private javax.swing.JButton sourceBrowseButton;
    private javax.swing.JComboBox sourceFolder;
    private javax.swing.JLabel sourceFolderLabel;
    private javax.swing.JComboBox toolchainComboBox;
    private javax.swing.JLabel toolchainLabel;
    private javax.swing.JCheckBox useBuildAnalyzerCheckBox;
    // End of variables declaration//GEN-END:variables

    private void disableHostSensitiveComponents() {
        PanelProjectLocationVisual.disableHostsInfo(this.hostComboBox, this.toolchainComboBox);
        this.advancedMode.setEnabled(false);
        this.simpleMode.setEnabled(false);
    }

    private void enableHostSensitiveComponents(Collection<ServerRecord> records, 
            ServerRecord srToSelect, CompilerSet csToSelect, boolean isDefaultCompilerSet, boolean enableHost, boolean enableToolchain) {
        PanelProjectLocationVisual.updateToolchainsComponents(SelectModePanel.this.hostComboBox, SelectModePanel.this.toolchainComboBox, 
                records, srToSelect, csToSelect, isDefaultCompilerSet, enableHost, enableToolchain);
        this.advancedMode.setEnabled(true);
        this.simpleMode.setEnabled(true);
        refreshInstruction(false);
        initialized = true;
        controller.getWizardStorage().setCompilerSet(csToSelect);
        controller.getWizardStorage().setExecutionEnvironment(env);
        refreshInstruction(false);
    }

    private class RefreshRunnable implements Runnable {
        private boolean refreshRoot = false;
        private int generation = 0;
        private final Object lock = new Object();

        public RefreshRunnable() {
        }
        
        private void setRefreshRoot(boolean refreshRoot) {
            synchronized(lock) {
                if (refreshRoot && ! this.refreshRoot) {
                    this.refreshRoot = true;
                    generation++;
                }
            }
        }

        @Override
        public void run() {
            int startCount;
            synchronized(lock) {
                refreshRoot = false;
                startCount = generation;
            }
            synchronized(lock) {
                if (startCount < generation) {
                    return;
                }
            }
            boolean validate = SelectModePanel.this.controller.validate();
            if (!validate) {
                return;
            }
            boolean simple = simpleMode.isSelected();
            if (simple) {
                String tool = ""; // NOI18N
                String toolsInfo = ""; // NOI18N
                if (SelectModePanel.this.controller.getWizardStorage() != null) {
                    String configure = SelectModePanel.this.controller.getWizardStorage().getConfigure();
                    if (configure != null) {
                        String normalizedPath = CndFileUtils.normalizeAbsolutePath(fileSystem, configure);
                        FileObject fo = CndFileUtils.toFileObject(fileSystem, normalizedPath);
                        if (fo != null && fo.isValid()) {
                            PreBuildArtifact scriptArtifact = PreBuildSupport.scriptToArtifact(fo);
                            if (scriptArtifact != null) {
                                PreBuildArtifactProvider preBuildProvider = PreBuildSupport.getPreBuildProvider(scriptArtifact);
                                toolsInfo = preBuildProvider.getHint();
                                tool = preBuildProvider.getToolName();
                            }
                        }
                    } else {
                        String makefile = SelectModePanel.this.controller.getWizardStorage().getMake();
                        if (makefile != null) {
                            BuildFile scriptFile = BuildSupport.scriptToBuildFile(makefile);
                            if (scriptFile != null) {
                                SelectModePanel.this.controller.getWizardStorage().getProjectPath();
                                tool = CndPathUtilities.getRelativePath(((ExpandableEditableComboBox)sourceFolder).getText().trim(), makefile);
                                BuildFileProvider buildFileProvider = BuildSupport.getBuildFileProvider(scriptFile);
                                toolsInfo = buildFileProvider.getHint();
                            }
                        }
                    }
                }
                synchronized(lock) {
                    if (startCount <generation) {
                        return;
                    }
                }
                if (tool == null || tool.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        org.openide.awt.Mnemonics.setLocalizedText(simpleMode, NbBundle.getMessage(SelectModePanel.class, "SimpleModeButtonText")); // NOI18N
                        instructions.setText(NbBundle.getMessage(SelectModePanel.class, "SelectModeSimpleInstructionText")); // NOI18N
                    });
                } else {
                    final String message1 = NbBundle.getMessage(SelectModePanel.class, "SimpleModeButtonSpecifiedText", tool); // NOI18N
                    final String message2 = NbBundle.getMessage(SelectModePanel.class, "SelectModeSimpleInstructionSpecifiedText", toolsInfo);
                    SwingUtilities.invokeLater(() -> {
                        org.openide.awt.Mnemonics.setLocalizedText(simpleMode, message1);
                        instructions.setText(message2);
                    });
                }
            } else {
                SwingUtilities.invokeLater(() -> {
                    org.openide.awt.Mnemonics.setLocalizedText(simpleMode, NbBundle.getMessage(SelectModePanel.class, "SimpleModeButtonText")); // NOI18N
                    instructions.setText(NbBundle.getMessage(SelectModePanel.class, "SelectModeAdvancedInstructionText")); // NOI18N
                });
            }
        }
    }

}
