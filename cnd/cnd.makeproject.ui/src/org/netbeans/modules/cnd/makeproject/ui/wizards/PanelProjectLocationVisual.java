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

import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.CndLanguageStandards;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

public class PanelProjectLocationVisual extends SettingsPanel implements HelpCtx.Provider {

    public static final String PROP_PROJECT_NAME = "projectName"; // NOI18N
    public static final String PROP_MAIN_NAME = "mainName"; // NOI18N
    //changed from EDT thread only
    private volatile WizardValidationWorkerCheckState currentState = new WizardValidationWorkerCheckState(Boolean.TRUE,
            new ValidationResult(Boolean.FALSE, NbBundle.getMessage(PanelProjectLocationVisual.class, "PanelProjectLocationVisual.Validating_Wizard")));//NOI18N
    private static final RequestProcessor RP = new RequestProcessor("Inot Hosts", 1); // NOI18N
    private static final RequestProcessor validationRP = new RequestProcessor("Wizard Validation", 1); // NOI18N
    static final String[] CPP = new String[]{"C++", // NOI18N
                                CndLanguageStandards.CndLanguageStandard.CPP98.toString(),
                                CndLanguageStandards.CndLanguageStandard.CPP11.toString(),
                                CndLanguageStandards.CndLanguageStandard.CPP14.toString(),
                                CndLanguageStandards.CndLanguageStandard.CPP17.toString(),
                                CndLanguageStandards.CndLanguageStandard.CPP20.toString(),
                                CndLanguageStandards.CndLanguageStandard.CPP23.toString(),
                                };
    static final String[] C = new String[]{"C", // NOI18N
                                CndLanguageStandards.CndLanguageStandard.C89.toString(),
                                CndLanguageStandards.CndLanguageStandard.C99.toString(),
                                CndLanguageStandards.CndLanguageStandard.C11.toString(),
                                CndLanguageStandards.CndLanguageStandard.C17.toString(),
                                CndLanguageStandards.CndLanguageStandard.C23.toString(),
                                };
    static final String[] FORTRAN = new String[]{"Fortran90 Fixed", // NOI18N
                                                 "Fortran90 Free", // NOI18N
                                                 "Fortran95", // NOI18N
                                                 "Fortran2003", // NOI18N
                                                 "Fortran2008" // NOI18N
                                };
    private final PanelConfigureProject controller;
    private final String templateName;
    private String name;
    private final int type;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final Object FAKE_ITEM = new Object();
    private ExecutionEnvironment env;
    private FileSystem fileSystem;
    private char fsFileSeparator;
//    private AtomicBoolean isValid = new AtomicBoolean(false);
    private final WizardValidationWorker validationWorker = new WizardValidationWorker();
    static final int VALIDATION_DELAY = 300;

    /**
     * Creates new form PanelProjectLocationVisual
     */
    public PanelProjectLocationVisual(PanelConfigureProject panel, String name, boolean showMakefileTextField, int type) {
        initComponents();
        this.controller = panel;
        this.controller.addChangeListener(validationWorker);
        this.name = name;
        this.templateName = name;
        this.type = type;
        // Register listener on the textFields to make the automatic updates
        projectNameTextField.getDocument().addDocumentListener(validationWorker);
        projectLocationTextField.getDocument().addDocumentListener(validationWorker);
        if (showMakefileTextField) {
            makefileTextField.getDocument().addDocumentListener(validationWorker);
        } else {
            makefileTextField.setVisible(false);
            makefileLabel.setVisible(false);
        }

        // Accessibility
        makefileTextField.getAccessibleContext().setAccessibleDescription(getString("AD_MAKEFILE"));

        createMainTextField.setText("main"); // NOI18N
        createMainTextField.getDocument().addDocumentListener(validationWorker);

        if (type == NewMakeProjectWizardIterator.TYPE_APPLICATION) {
            createMainCheckBox.setVisible(true);
            createMainTextField.setVisible(true);
            createMainComboBox.setVisible(true);
            fillComboBox(MIMENames.C_MIME_TYPE, MIMENames.CPLUSPLUS_MIME_TYPE, MIMENames.FORTRAN_MIME_TYPE);
            String prefLanguage = MakeProjectOptions.getPrefApplicationLanguage();
            createMainComboBox.setSelectedItem(prefLanguage);
        } else if (type == NewMakeProjectWizardIterator.TYPE_DB_APPLICATION) {
            createMainCheckBox.setVisible(true);
            createMainTextField.setVisible(true);
            createMainComboBox.setVisible(true);
            fillComboBox(MIMENames.C_MIME_TYPE, MIMENames.CPLUSPLUS_MIME_TYPE);
            createMainComboBox.setSelectedIndex(0);
        } else if (type == NewMakeProjectWizardIterator.TYPE_QT_APPLICATION) {
            createMainCheckBox.setVisible(true);
            createMainTextField.setVisible(true);
            createMainComboBox.setVisible(true);
            fillComboBox(MIMENames.CPLUSPLUS_MIME_TYPE);
            createMainComboBox.setSelectedIndex(0);
        } else {
            createMainCheckBox.setVisible(false);
            createMainCheckBox.setSelected(false);
            createMainTextField.setVisible(false);
            createMainComboBox.setVisible(false);
        }
        disableHostsInfo(this.hostComboBox, this.toolchainComboBox);
    }

    private void fillComboBox(String ... mimeTypes){
        for(String mime : mimeTypes) {
            if (mime.equals(MIMENames.C_MIME_TYPE)) {
                for(String st : C) {
                    createMainComboBox.addItem(st);
                }
            } else if (mime.equals(MIMENames.CPLUSPLUS_MIME_TYPE)) {
                for(String st : CPP) {
                    createMainComboBox.addItem(st);
                }
            } else if (mime.equals(MIMENames.FORTRAN_MIME_TYPE)) {
                for(String st : FORTRAN) {
                    createMainComboBox.addItem(st);
                }
            }
        }
    }

    static Pair<String,Integer> getLanguageStandard(String value) {
        if (value == null) {
            return null;
        }
        for(int i = 0; i < C.length; i++) {
            if (value.equals(C[i])) {
                return Pair.of(C[0], i);
            }
        }
        for(int i = 0; i < CPP.length; i++) {
            if (value.equals(CPP[i])) {
                return Pair.of(CPP[0], i);
            }
        }
        for(int i = 0; i < FORTRAN.length; i++) {
            if (value.equals(FORTRAN[i])) {
                return Pair.of(FORTRAN[0], i);
            }
        }
        return null;
    }

    /*package*/
    static void disableHostsInfo(JComboBox hostComboBox, JComboBox toolchainComboBox) {
        // load hosts && toolchains
        hostComboBox.setEnabled(false);
        toolchainComboBox.setEnabled(false);
        hostComboBox.addItem(FAKE_ITEM);
        toolchainComboBox.addItem(FAKE_ITEM);
        hostComboBox.setRenderer(new MyDevHostListCellRenderer(FAKE_ITEM));
        toolchainComboBox.setRenderer(new MyToolchainListCellRenderer(FAKE_ITEM));
    }

    /*package*/
    static void updateToolchainsComponents(JComboBox hostComboBox, JComboBox toolchainComboBox,
            Collection<ServerRecord> records, ServerRecord srToSelect, CompilerSet csToSelect, boolean isDefaultCompilerSet, boolean enableHost, boolean enableToolchain) {

        hostComboBox.removeAllItems();
        toolchainComboBox.removeAllItems();
        if (records != null) {
            records.forEach((serverRecord) -> {
                hostComboBox.addItem(serverRecord);
            });
            hostComboBox.setSelectedItem(srToSelect);
            updateToolchains(toolchainComboBox, srToSelect);
            for (int i = 0; i < toolchainComboBox.getModel().getSize(); i++) {
                Object elementAt = toolchainComboBox.getModel().getElementAt(i);
                if (elementAt instanceof ToolCollectionItem) {
                    ToolCollectionItem item = (ToolCollectionItem) elementAt;
                    if (isDefaultCompilerSet && item.isDefaultCompilerSet()) {
                        toolchainComboBox.setSelectedIndex(i);
                        break;
                    } else {
                        if (item.getCompilerSet().equals(csToSelect)) {
                            toolchainComboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
            hostComboBox.setEnabled(enableHost);
            toolchainComboBox.setEnabled(enableToolchain);
        }
    }

    private static Collection<ServerRecord> initServerRecords(ToolsCacheManager toolsCacheManager, ExecutionEnvironment ee) {
        Collection<ServerRecord> out = new ArrayList<>();

        Collection<ServerRecord> records = new ArrayList<>();
        if (toolsCacheManager != null && toolsCacheManager.getServerUpdateCache() != null) {
            records.addAll(toolsCacheManager.getServerUpdateCache().getHosts());
        } else {
            records.addAll(ServerList.getRecords());
        }
        if (ee != null) {
            ServerRecord r = ServerList.get(ee);
            if (r.isSetUp()) {
                records.add(r);
            }
        }

        for (ServerRecord serverRecord : records) {
            if (serverRecord.isSetUp() && !serverRecord.isDeleted()) {
                CompilerSetManager csm;
                if (toolsCacheManager != null && ee != null) {
                    csm = toolsCacheManager.getCompilerSetManagerCopy(ee, false);
                } else {
                    csm = CompilerSetManager.get(serverRecord.getExecutionEnvironment());
                }
                if (csm != null) {
                    csm.finishInitialization();
                    if (!csm.isEmpty() && !csm.isUninitialized()) {
                        out.add(serverRecord);
                    }
                }
            }
        }
        return out;
    }

    public String getProjectName() {
        return this.projectNameTextField.getText();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("NewAppWizard"); // NOI18N
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        projectNameLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationLabel = new javax.swing.JLabel();
        projectLocationTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        createdFolderLabel = new javax.swing.JLabel();
        createdFolderTextField = new javax.swing.JTextField();
        makefileLabel = new javax.swing.JLabel();
        makefileTextField = new javax.swing.JTextField();
        createMainCheckBox = new javax.swing.JCheckBox();
        createMainTextField = new javax.swing.JTextField();
        createMainComboBox = new javax.swing.JComboBox();
        hostLabel = new javax.swing.JLabel();
        toolchainLabel = new javax.swing.JLabel();
        hostComboBox = new javax.swing.JComboBox();
        toolchainComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_ProjectName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(projectNameLabel, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle"); // NOI18N
        projectNameLabel.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_projectNameLabel")); // NOI18N
        projectNameLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_projectNameLabel")); // NOI18N

        projectNameTextField.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        add(projectNameTextField, gridBagConstraints);

        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_ProjectLocation_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(projectLocationLabel, gridBagConstraints);
        projectLocationLabel.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_projectLocationLabel")); // NOI18N
        projectLocationLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_projectLocationLabel")); // NOI18N

        projectLocationTextField.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        add(projectLocationTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_BrowseLocation_Button")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLocationAction(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_browseButton")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_browseButton")); // NOI18N

        createdFolderLabel.setLabelFor(createdFolderTextField);
        createdFolderLabel.setText(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_CreatedProjectFolder_Lablel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(createdFolderLabel, gridBagConstraints);
        createdFolderLabel.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_createdFolderLabel")); // NOI18N
        createdFolderLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_createdFolderLabel")); // NOI18N

        createdFolderTextField.setColumns(20);
        createdFolderTextField.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 5, 0);
        add(createdFolderTextField, gridBagConstraints);

        makefileLabel.setLabelFor(makefileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(makefileLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_MAKEFILE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(makefileLabel, gridBagConstraints);

        makefileTextField.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 0);
        add(makefileTextField, gridBagConstraints);

        createMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createMainCheckBox, bundle.getString("LBL_createMainfile")); // NOI18N
        createMainCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createMainCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(createMainCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        add(createMainTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        add(createMainComboBox, gridBagConstraints);

        hostLabel.setLabelFor(hostComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_HOST")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(hostLabel, gridBagConstraints);

        toolchainLabel.setLabelFor(toolchainComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(toolchainLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_TOOLCHAIN")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(toolchainLabel, gridBagConstraints);

        hostComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                hostComboBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        add(hostComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
        add(toolchainComboBox, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACSN_PanelProjectLocationVisual")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "ACSD_PanelProjectLocationVisual")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseLocationAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLocationAction
        String path = this.projectLocationTextField.getText();
        JFileChooser chooser = RemoteFileChooserUtil.createFileChooser(FileSystemProvider.getFileSystem(env),
                NbBundle.getMessage(PanelProjectLocationVisual.class, "LBL_NWP1_SelectProjectLocation"),
                null, JFileChooser.DIRECTORIES_ONLY, null, path, true);
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) { //NOI18N
            File projectDir = chooser.getSelectedFile();
            projectLocationTextField.setText(projectDir.getAbsolutePath());
        }
        controller.fireChangeEvent();
    }//GEN-LAST:event_browseLocationAction

    private void createMainCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createMainCheckBoxActionPerformed
        // TODO add your handling code here:
        createMainTextField.setEnabled(createMainCheckBox.isSelected());
        createMainComboBox.setEnabled(createMainCheckBox.isSelected());
}//GEN-LAST:event_createMainCheckBoxActionPerformed

    private void hostComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_hostComboBoxItemStateChanged
        if (!initialized.get()) {
            return;
        }
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            ServerRecord newItem = (ServerRecord) evt.getItem();
            updateToolchains(toolchainComboBox, newItem);
            controller.fireChangeEvent(); // Notify that the panel changed
        }
    }

    /*package*/ static void updateToolchains(JComboBox toolchainComboBox, ServerRecord newItem) {
        // change toolchains
        CompilerSetManager csm = CompilerSetManager.get(newItem.getExecutionEnvironment());
        toolchainComboBox.removeAllItems();
        CompilerSet defaultCompilerSet = csm.getDefaultCompilerSet();
        if (defaultCompilerSet != null) {
            toolchainComboBox.addItem(new ToolCollectionItem(defaultCompilerSet, true));
        }
        csm.getCompilerSets().forEach((compilerSet) -> {
            toolchainComboBox.addItem(new ToolCollectionItem(compilerSet, false));
        });
        if (toolchainComboBox.getModel().getSize() > 0) {
            toolchainComboBox.setSelectedIndex(0);
        }
    }//GEN-LAST:event_hostComboBoxItemStateChanged

    @Override
    public void addNotify() {
        super.addNotify();
        //same problem as in 31086, initial focus on Cancel button
        projectNameTextField.requestFocus();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        validationWorker.shutdown();
    }

    private boolean isValidMakeFile(String text) {
        if (text.length() == 0) {
            return false;
        }
        if (text.contains("\\") || // NOI18N
                text.contains("/") || // NOI18N
                text.contains("..") || // NOI18N
                hasIllegalChar(text)) {
            return false;
        }
        return true;
    }

    private boolean isValidMainFile(String text) {
        // unix allows a lot of strange names, but let's prohibit this for project
        // using symbols invalid on Windows
        if (text.length() == 0) {
            return true;
        }
        if (text.startsWith(" ") || // NOI18N
                text.startsWith("\\") || // NOI18N
                text.startsWith("/") || // NOI18N
                text.contains("..") || // NOI18N
                hasIllegalChar(text)) {
            return false;
        }
        return true;
    }

    public static boolean isValidProjectName(String text) {
        // unix allows a lot of strange names, but let's prohibit this for project
        // using symbols invalid on Windows
        if (text.length() == 0 || text.startsWith(" ") || // NOI18N
                text.contains("\\") || // NOI18N
                text.contains("/") || // NOI18N
                hasIllegalChar(text)) {
            return false;
        }
        // check ability to create file with specified name on target OS
        boolean ok = false;
        try {
            File file = File.createTempFile(text + "dummy", "");// NOI18N
            ok = true;
            file.delete();
        } catch (Exception ex) {
            // failed to create
        }
        return ok;
    }

    private static boolean hasIllegalChar(String text) {
        return text.contains(":") || // NOI18N
                text.contains("*") || // NOI18N
                text.contains("?") || // NOI18N
                text.contains("\"") || // NOI18N
                text.contains("<") || // NOI18N
                text.contains(">") || // NOI18N
                text.contains("|") ||  // NOI18N
                text.contains("$") ||  // NOI18N
                text.contains("{") ||  // NOI18N
                text.contains("}") ||  // NOI18N
                text.contains("(") ||  // NOI18N
                text.contains(")");  // NOI18N

    }

    @Override
    boolean valid(WizardDescriptor wizardDescriptor) {
        if (!initialized.get() || currentState == null) {
            return false;
        }
        ValidationResult result = currentState.validationResult;
        boolean valid = result.isValid;
        wizardDescriptor.putProperty(result.isValid ? WizardDescriptor.PROP_WARNING_MESSAGE : WizardDescriptor.PROP_ERROR_MESSAGE, result.msgError);
        return valid;

    }


    void setError() {
        controller.fireChangeEvent(new ChangeEvent(validationWorker)); // Notify that the panel changed
    }

    private FileObject getExistingParent(String path) {
        path = PathUtilities.getDirName(path);
        FileObject fo = fileSystem.findResource(path);
        while (fo == null) {
            path = PathUtilities.getDirName(path);
            if (path == null || path.length() == 0) {
                return null;
            } else {
                fo = fileSystem.findResource(path);
            }
        }
        return fo;
    }

    @Override
    void store(WizardDescriptor d) {

        String projectName = projectNameTextField.getText().trim();
        String folder = createdFolderTextField.getText().trim();
        Boolean valid = currentState.validationResult.isValid;
        //will check only if valid already, otherwise just write as it is
        if (valid) {
            if (CndPathUtilities.isPathAbsolute(folder)) {
                String normalizeAbsolutePath = RemoteFileUtil.normalizeAbsolutePath(folder, env);
                FSPath path = new FSPath(fileSystem, normalizeAbsolutePath);
                WizardConstants.PROPERTY_PROJECT_FOLDER.put(d, path);
            }
        } else {
            WizardConstants.PROPERTY_PROJECT_FOLDER_STRING_VALUE.put(d, projectLocationTextField.getText().trim());
        }
        WizardConstants.PROPERTY_NAME.put(d, projectName);
        WizardConstants.PROPERTY_GENERATED_MAKEFILE_NAME.put(d, makefileTextField.getText());
        if (valid) {
            if (CndPathUtilities.isPathAbsolute(projectLocationTextField.getText())) {
                if (env.isLocal()) {
                    File projectsDir = CndFileUtils.createLocalFile(projectLocationTextField.getText());
                    if (projectsDir.isDirectory()) {
                        ProjectChooser.setProjectsFolder(projectsDir);
                    }
                } else {
                    RemoteFileUtil.setProjectsFolder(projectLocationTextField.getText(), env);
                }
            }
        }

        WizardConstants.MAIN_CLASS.put(d, null);

        MIMEExtensions cExtensions = MIMEExtensions.get(MIMENames.C_MIME_TYPE);
        MIMEExtensions ccExtensions = MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE);
        MIMEExtensions fortranExtensions = MIMEExtensions.get(MIMENames.FORTRAN_MIME_TYPE);

        WizardConstants.PROPERTY_CREATE_MAIN_FILE.put(d, createMainCheckBox.isSelected());
        if (createMainCheckBox.isSelected() && createMainTextField.getText().length() > 0) {
            if (type == NewMakeProjectWizardIterator.TYPE_APPLICATION) {
                Pair<String, Integer> languageStandard = getLanguageStandard((String) createMainComboBox.getSelectedItem());
                if (languageStandard != null) {
                    WizardConstants.PROPERTY_LANGUAGE_STANDARD.put(d, (String) createMainComboBox.getSelectedItem());
                    if (languageStandard.first().equals(C[0])) {
                        WizardConstants.PROPERTY_MAIN_FILE_NAME.put(d, createMainTextField.getText() + "." + cExtensions.getDefaultExtension()); // NOI18N
                        WizardConstants.PROPERTY_MAIN_TEMPLATE_NAME.put(d, "Templates/cFiles/main.c"); // NOI18N
                    } else if (languageStandard.first().equals(CPP[0])) {
                        WizardConstants.PROPERTY_MAIN_FILE_NAME.put(d, createMainTextField.getText() + "." + ccExtensions.getDefaultExtension()); // NOI18N
                        WizardConstants.PROPERTY_MAIN_TEMPLATE_NAME.put(d, "Templates/cppFiles/main.cc"); // NOI18N
                    } else if (languageStandard.first().equals(FORTRAN[0])) {
                        switch(languageStandard.second()) {
                            case 0:
                                WizardConstants.PROPERTY_MAIN_FILE_NAME.put(d, createMainTextField.getText() + ".f"); // NOI18N
                                WizardConstants.PROPERTY_MAIN_TEMPLATE_NAME.put(d, "Templates/fortranFiles/fortranFixedFormatFile.f"); // NOI18N
                                break;
                            case 1:
                                WizardConstants.PROPERTY_MAIN_FILE_NAME.put(d, createMainTextField.getText() + ".f90"); // NOI18N
                                WizardConstants.PROPERTY_MAIN_TEMPLATE_NAME.put(d, "Templates/fortranFiles/fortranFreeFormatFile.f90"); // NOI18N
                                break;
                            case 2:
                                WizardConstants.PROPERTY_MAIN_FILE_NAME.put(d, createMainTextField.getText() + ".f95"); // NOI18N
                                WizardConstants.PROPERTY_MAIN_TEMPLATE_NAME.put(d, "Templates/fortranFiles/fortranFreeFormatFile.f90"); // NOI18N
                                break;
                            case 3:
                                WizardConstants.PROPERTY_MAIN_FILE_NAME.put(d, createMainTextField.getText() + ".f03"); // NOI18N
                                WizardConstants.PROPERTY_MAIN_TEMPLATE_NAME.put(d, "Templates/fortranFiles/fortranFreeFormatFile.f90"); // NOI18N
                                break;
                            case 4:
                                WizardConstants.PROPERTY_MAIN_FILE_NAME.put(d, createMainTextField.getText() + ".f08"); // NOI18N
                                WizardConstants.PROPERTY_MAIN_TEMPLATE_NAME.put(d, "Templates/fortranFiles/fortranFreeFormatFile.f90"); // NOI18N
                                break;
                        }
                    }
                }
                MakeProjectOptions.setPrefApplicationLanguage((String) createMainComboBox.getSelectedItem());
            } else if (type == NewMakeProjectWizardIterator.TYPE_DB_APPLICATION) {
                Pair<String, Integer> languageStandard = getLanguageStandard((String) createMainComboBox.getSelectedItem());
                if (languageStandard != null) {
                    WizardConstants.PROPERTY_LANGUAGE_STANDARD.put(d, (String) createMainComboBox.getSelectedItem());
                    if (languageStandard.first().equals(C[0])) {
                        WizardConstants.PROPERTY_MAIN_FILE_NAME.put(d, createMainTextField.getText() + ".pc"); // NOI18N
                        WizardConstants.PROPERTY_MAIN_TEMPLATE_NAME.put(d, "Templates/cFiles/main.pc"); // NOI18N
                    } else if (languageStandard.first().equals(CPP[0])) {
                        WizardConstants.PROPERTY_MAIN_FILE_NAME.put(d, createMainTextField.getText() + ".pc"); // NOI18N
                        WizardConstants.PROPERTY_MAIN_TEMPLATE_NAME.put(d, "Templates/ccFiles/main.pc"); // NOI18N
                    }
                }
            } else if (type == NewMakeProjectWizardIterator.TYPE_QT_APPLICATION) {
                Pair<String, Integer> languageStandard = getLanguageStandard((String) createMainComboBox.getSelectedItem());
                if (languageStandard != null) {
                    WizardConstants.PROPERTY_LANGUAGE_STANDARD.put(d, (String) createMainComboBox.getSelectedItem());
                    if (languageStandard.first().equals(CPP[0])) {
                        WizardConstants.PROPERTY_MAIN_FILE_NAME.put(d, createMainTextField.getText() + "." + ccExtensions.getDefaultExtension()); // NOI18N
                        WizardConstants.PROPERTY_MAIN_TEMPLATE_NAME.put(d, "Templates/qtFiles/main.cc"); // NOI18N
                    }
                }
            }
        }
        Object obj = hostComboBox.getSelectedItem();
        if (obj != null && obj instanceof ServerRecord) {
            ServerRecord sr = (ServerRecord) obj;
            WizardConstants.PROPERTY_HOST_UID.put(d, ExecutionEnvironmentFactory.toUniqueID(sr.getExecutionEnvironment()));
        }
        Object selectedItem = toolchainComboBox.getSelectedItem();
        if (selectedItem instanceof ToolCollectionItem) {
            ToolCollectionItem item = (ToolCollectionItem) selectedItem;
            WizardConstants.PROPERTY_TOOLCHAIN.put(d, item.getCompilerSet());
            WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT.put(d, item.isDefaultCompilerSet());
        }
    }

    @Override
    void read(final WizardDescriptor settings) {
        initialized.set(false);
        env = WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV.get(settings);
        boolean enabledHost;
        if (env != null) {
            WizardConstants.PROPERTY_HOST_UID.put(settings, ExecutionEnvironmentFactory.toUniqueID(env));
            enabledHost = false;
        } else {
            env = ExecutionEnvironmentFactory.getLocal();
            enabledHost = true;
        }

        fileSystem = FileSystemProvider.getFileSystem(env);
        fsFileSeparator = FileSystemProvider.getFileSeparatorChar(fileSystem);

        FSPath projectLocationFSPath = WizardConstants.PROPERTY_PROJECT_FOLDER.get(settings); // File - SIC! for projects always local
        String projectName = null;
        String projectLocation;
        if (projectLocationFSPath == null) {
            String projectLocationStringValue = WizardConstants.PROPERTY_PROJECT_FOLDER_STRING_VALUE.get(settings);
            if (projectLocationStringValue != null && !projectLocationStringValue.trim().isEmpty()) {
                projectLocation = projectLocationStringValue;
            } else {
                projectLocation = RemoteFileUtil.getProjectsFolder(env);
                if (projectLocation == null) {
                    projectLocation = getDefaultProjectDir(env);
                }
            }
        } else {
            projectLocation = projectLocationFSPath.getPath();
            int i = projectLocation.lastIndexOf(fsFileSeparator);
            if (i > 0) {
                projectName = projectLocation.substring(i + 1);
                projectLocation = projectLocation.substring(0, i);
            }
        }
        final String projectNameText = projectName;
        final String projectLocationText = projectLocation;
        projectLocationTextField.setText(projectLocationText);
        projectLocationTextField.setText(projectLocationText);
        if (projectNameText != null) {
            projectNameTextField.setText(projectNameText);
            projectNameTextField.selectAll();
        }
        String hostUID = WizardConstants.PROPERTY_HOST_UID.get(settings);
        CompilerSet cs = WizardConstants.PROPERTY_TOOLCHAIN.get(settings);
        boolean isDefaultCompilerSet = Boolean.TRUE.equals(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT.get(settings));
        Boolean readOnlyToolchain = WizardConstants.PROPERTY_READ_ONLY_TOOLCHAIN.get(settings);
        if (Boolean.TRUE.equals(readOnlyToolchain)) {
            enabledHost = false;
        }
        final boolean enabledHostFinal = enabledHost;
        RP.post(new DevHostsInitializer(hostUID, cs, isDefaultCompilerSet,
                readOnlyToolchain, WizardConstants.PROPERTY_TOOLS_CACHE_MANAGER.get(settings)) {
            @Override
            public void updateComponents(Collection<ServerRecord> records, ServerRecord srToSelect, CompilerSet csToSelect, boolean isDefaultCompilerSet, boolean enabled) {
                updateToolchainsComponents(PanelProjectLocationVisual.this.hostComboBox, PanelProjectLocationVisual.this.toolchainComboBox, records, srToSelect, csToSelect, isDefaultCompilerSet, enabledHostFinal, enabled);
                initialized.set(true);
                controller.fireChangeEvent(); // Notify that the panel changed
            }
        });
        String prefferedName = WizardConstants.PROPERTY_PREFERED_PROJECT_NAME.get(settings); //NOI18N
        if (prefferedName != null && prefferedName.length() > 0) {
            name = prefferedName;
        }
        String project = projectNameText;
        if (project == null) {
            if (name == null) {
                String workingDir = WizardConstants.PROPERTY_WORKING_DIR.get(settings);
                if (workingDir != null && workingDir.length() > 0
                        && (templateName.equals(NewMakeProjectWizardIterator.MAKEFILEPROJECT_PROJECT_NAME)
                        || templateName.equals(NewMakeProjectWizardIterator.FULL_REMOTE_PROJECT_NAME))) {
                    name = CndPathUtilities.getBaseName(workingDir);
                } else {
                    String sourcesPath = WizardConstants.PROPERTY_SOURCE_FOLDER_PATH.get(settings);
                    if (sourcesPath != null && sourcesPath.length() > 0) {
                        name = CndPathUtilities.getBaseName(sourcesPath);
                    }
                }
            }
            int baseCount = 1;
            final String formater = name + "_{0}"; // NOI18N
            //put whatever it is and then re-calculate in separate thread
            final String firstName = MessageFormat.format(formater, new Object[]{baseCount});
            projectNameTextField.setText(firstName);
            projectNameTextField.selectAll();
            validationRP.post(() -> {
                int baseCount1 = 1;
                String project1 = firstName;
                while ((project1 = validFreeProjectName(projectLocationText, fsFileSeparator, formater, baseCount1)) == null) {
                    baseCount1++;
                }
                settings.putProperty(NewMakeProjectWizardIterator.PROP_NAME_INDEX, baseCount1);
                //update
                if (!project1.equals(firstName)) {
                    final String projectNameRecalculated = project1;
                    SwingUtilities.invokeLater(() -> {
                        projectNameTextField.setText(projectNameRecalculated);
                        projectNameTextField.selectAll();
                    });
                }
            });

        }




    }

    private String getDefaultProjectDir(ExecutionEnvironment env) {
        String res = null;
        try {
            if (env.isLocal()) {
                res = ProjectChooser.getProjectsFolder().getPath();
            } else if (HostInfoUtils.isHostInfoAvailable(env)) {
                res = HostInfoUtils.getHostInfo(env).getUserDir() + fsFileSeparator + ProjectChooser.getProjectsFolder().getName();
            }
        } catch (IOException ex) {
        } catch (ConnectionManager.CancellationException ex) {
        }
        return res == null ? fileSystem.getRoot().getPath() : res;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JCheckBox createMainCheckBox;
    private javax.swing.JComboBox createMainComboBox;
    private javax.swing.JTextField createMainTextField;
    private javax.swing.JLabel createdFolderLabel;
    private javax.swing.JTextField createdFolderTextField;
    private javax.swing.JComboBox hostComboBox;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JLabel makefileLabel;
    private javax.swing.JTextField makefileTextField;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    private javax.swing.JComboBox toolchainComboBox;
    private javax.swing.JLabel toolchainLabel;
    // End of variables declaration//GEN-END:variables

    private String validFreeProjectName(String parentFolder, final char fs, final String formater, final int index) {
        String projectName = MessageFormat.format(formater, new Object[]{index});
        if (RemoteFileUtil.fileExists(parentFolder + fs + projectName, env)) { //NOI18N
            return null;
        }
        return projectName;
    }

    // Implementation of DocumentListener --------------------------------------
    private static ValidationResult isValidLocalProjectNameAndLocation(String projectNameTextField, String projectLocationTextField, String createdFolderTextField) {
        if (!isValidProjectName(projectNameTextField)) {
            return new ValidationResult(Boolean.FALSE, NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectName")); // Display name not specified
        }
        if (!CndPathUtilities.isPathAbsolute(projectLocationTextField)) { // empty field imcluded
            String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectLocation"); // NOI18N
            return new ValidationResult(Boolean.FALSE, message);
        }
        File f = CndFileUtils.createLocalFile(projectLocationTextField).getAbsoluteFile();
        if (getCanonicalFile(f) == null) {
            String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectLocation"); // NOI18N
            return new ValidationResult(Boolean.FALSE, message);
        }
        final File destFolder = getCanonicalFile(CndFileUtils.createLocalFile(createdFolderTextField).getAbsoluteFile()); // project folder always local
        if (destFolder == null) {
            String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectName"); // NOI18N
            return new ValidationResult(Boolean.FALSE, message);

        }
        return new ValidationResult(Boolean.TRUE, null);
    }

    private static class ValidationResult {

        private Boolean isValid;
        private String msgError;

        ValidationResult(Boolean isValid, String msgError) {
            this.isValid = isValid;
            this.msgError = msgError;
        }
    }


    private String contructProjectMakefileName(String projectName, int count) {
        String makefileName = projectName + "-" + MakeConfigurationDescriptor.DEFAULT_PROJECT_MAKFILE_NAME; // NOI18N
        if (count > 0) {
            makefileName += "" + count + ".mk"; // NOI18N
        } else {
            makefileName += ".mk"; // NOI18N
        }
        return makefileName.replace(' ', '_'); // NOI18N
    }

    public static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
    /**
     * Look up i18n strings here
     */
    private static ResourceBundle bundle;

    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(PanelProjectLocationVisual.class);
        }
        return bundle.getString(s);
    }

    /*package*/ static final class MyDevHostListCellRenderer extends DefaultListCellRenderer {

        private final Object loadingMarker;

        public MyDevHostListCellRenderer(Object loadingItem) {
            loadingMarker = loadingItem;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (loadingMarker.equals(value)) {
                label.setText(NbBundle.getMessage(PanelProjectLocationVisual.class, "Loading_Host_Text")); // NOI18N
            } else {
                ServerRecord rec = (ServerRecord) value;
                if (rec != null) {
                    label.setText(rec.getDisplayName());
                }
            }
            return label;
        }
    }

    /*package*/ static final class MyToolchainListCellRenderer extends DefaultListCellRenderer {

        private final Object loadingMarker;

        public MyToolchainListCellRenderer(Object loadingItem) {
            loadingMarker = loadingItem;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (loadingMarker.equals(value)) {
                label.setText(NbBundle.getMessage(PanelProjectLocationVisual.class, "Loading_Toolchain_Text")); // NOI18N
            } else {
                label.setText(value.toString());
            }
            return label;
        }
    }

    /*package*/ abstract static class DevHostsInitializer implements Runnable {

        private final String hostUID;
        private final CompilerSet cs;
        private final boolean isDefaultCompilerSet;
        private final boolean readOnlyUI;
        private final ToolsCacheManager toolsCacheManager;
        // fields to be inited in worker thread and used in EDT
        private Collection<ServerRecord> records;
        private ServerRecord srToSelect;
        private CompilerSet csToSelect;

        public DevHostsInitializer(String hostUID, CompilerSet cs, boolean isDefaultCompilerSet, Boolean readOnlyToolchain, ToolsCacheManager toolsCacheManager) {
            this.hostUID = hostUID;
            this.cs = cs;
            this.isDefaultCompilerSet = isDefaultCompilerSet;
            this.readOnlyUI = readOnlyToolchain == null ? false : readOnlyToolchain;
            this.toolsCacheManager = toolsCacheManager;
        }

        @Override
        public void run() {
            if (!SwingUtilities.isEventDispatchThread()) {
                try {
                    ExecutionEnvironment ee = (hostUID == null) ? null : ExecutionEnvironmentFactory.fromUniqueID(hostUID);
                    records = initServerRecords(toolsCacheManager, ee);
                    srToSelect = null;
                    if (ee != null) {
                        srToSelect = ServerList.get(ee);
                    }
                    if (!records.contains(srToSelect)) {
                        srToSelect = null;
                    }
                    if (srToSelect == null || srToSelect.isDeleted()) {
                        srToSelect = ServerList.getDefaultRecord();
                        if (!records.contains(srToSelect) && !records.isEmpty()) {
                            srToSelect = records.iterator().next();
                        }
                    }
                    if (cs == null) {
                        CompilerSetManager csm;
                        if (toolsCacheManager == null) {
                            csm = CompilerSetManager.get(srToSelect.getExecutionEnvironment());
                        } else {
                            csm = toolsCacheManager.getCompilerSetManagerCopy(srToSelect.getExecutionEnvironment(), false);
                        }
                        csToSelect = csm.getDefaultCompilerSet();
                    } else {
                        csToSelect = cs;
                    }
                } finally {
                    SwingUtilities.invokeLater(this);
                }
            } else {
                updateComponents(records, srToSelect, csToSelect, isDefaultCompilerSet, !readOnlyUI);
            }
        }

        public abstract void updateComponents(Collection<ServerRecord> records, ServerRecord srToSelect,
                CompilerSet csToSelect, boolean isDefaultCompilerSet, boolean enabled);
    }

    public final static class ToolCollectionItem {

        private final boolean defaultCompilerSet;
        private final CompilerSet compilerSet;

        private ToolCollectionItem(CompilerSet compilerSet, boolean defaultCompilerSet) {
            this.defaultCompilerSet = defaultCompilerSet;
            this.compilerSet = compilerSet;
        }

        @Override
        public String toString() {
            String name = NbBundle.getMessage(PanelProjectLocationVisual.class, "Toolchain_Name_Text", compilerSet.getName(), compilerSet.getDisplayName());
            if (isDefaultCompilerSet()) {
                return getString("DefaultToolCollection") + " (" + name + ")";
            } else {
                return name;
            }
        }

        public boolean isDefaultCompilerSet() {
            return defaultCompilerSet;
        }

        public CompilerSet getCompilerSet() {
            return compilerSet;
        }
    }

    private static final class WizardValidationWorkerCheckState {
        // null - all is fine
        // TRUE - check in progress
        // FALSE - check failed

        private final Boolean checking;
        private final ValidationResult validationResult;

        private WizardValidationWorkerCheckState(Boolean checking, ValidationResult validationResult) {
            this.checking = checking;
            this.validationResult = validationResult;
        }
    }

    private static final class ProjectValidationParams {

        private final String projectName;
        private final String projectLocation;
        private final String createdProjectFolder;
        private String makefileTextField;
        private long eventID;
        private String createdMain;
        private boolean isMainCreated;

        ProjectValidationParams (String projectName, String projectLocation, String createdProjectFolder) {
            this.projectName = projectName;
            this.projectLocation = projectLocation;
            this.createdProjectFolder = createdProjectFolder;
        }

        void setRequestID (long eventID) {
            this.eventID = eventID;
        }

        void setMakefile (String makefileTextField) {
            this.makefileTextField = makefileTextField;
        }
        void setMainAttributes (boolean isMainCreated, String createdMain) {
            this.isMainCreated = isMainCreated;
            this.createdMain = createdMain;
        }

    }

    private class WizardValidationWorker implements Runnable, DocumentListener, ChangeListener {
        private final Object wizardValidationExecutorLock = new Object();
        private final ScheduledExecutorService wizardValidationExecutor;
        private ScheduledFuture<?>  wizardValidationTask;
        private long lastEventID = 0;
        private WizardValidationWorkerCheckState lastCheck = null;
        private ProjectValidationParams projectParams;
        private final AtomicBoolean makefileNameChangedManually = new AtomicBoolean(false);


        WizardValidationWorker() {
            wizardValidationExecutor = Executors.newScheduledThreadPool(1);
        }

        @Override
        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                WizardValidationWorkerCheckState curStatus = lastCheck;
                currentState = curStatus;
                ValidationResult validationResult = null;
                if (curStatus != null) {
                    validationResult = curStatus.validationResult;
                }
                if (curStatus == null || curStatus.checking == null) {
                    if (validationResult != null) {
                        validationResult = new ValidationResult(Boolean.TRUE, validationResult.msgError);
                    }
                    currentState = new WizardValidationWorkerCheckState(null, validationResult);
                }
                setError();
            } else {
                recalculateProjectParams();
                //check if we are not cancelled already
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    //log.log(Level.FINEST, "Interrupted (1) check for {0}", path);
                }
                ValidationResult result = validate();
                if (Thread.interrupted()) {
                    return;
                }
                lastCheck = new WizardValidationWorkerCheckState(result.isValid ? null : Boolean.FALSE, result);
                SwingUtilities.invokeLater(this);

            }

        }

        void recalculateProjectParams() {
            if (makefileNameChangedManually.get()) {
                return;
            }
            String createdFolderTextFieldValue = projectParams.createdProjectFolder.trim();
            String projectName = projectParams.projectName;

            // re-evaluate name of master project file.
            String makefileName;
            if (!templateName.equals(NewMakeProjectWizardIterator.MAKEFILEPROJECT_PROJECT_NAME)) {
                makefileName = MakeConfigurationDescriptor.DEFAULT_PROJECT_MAKFILE_NAME;
            } else {
                makefileName = contructProjectMakefileName(projectName, 0);
            }

            //need to construct MakefileName only in case the folder exists
            if (CndFileUtils.isExistingDirectory(fileSystem, createdFolderTextFieldValue)) {
                for (int count = 0;;) {
                    if (Thread.interrupted()) {
                        return;
                    }
                    String proposedMakefile = createdFolderTextFieldValue + fsFileSeparator + makefileName;
                    if (!CndFileUtils.isExistingFile(fileSystem, proposedMakefile)
                            && !CndFileUtils.isExistingFile(fileSystem, proposedMakefile.toLowerCase(Locale.getDefault()))
                            && !CndFileUtils.isExistingFile(fileSystem, proposedMakefile.toUpperCase(Locale.getDefault()))) {
                        break;
                    }
                    makefileName = contructProjectMakefileName(projectName, count++);
                }
            }
            final String makefileNameText = makefileName;
            final long currentEventID = projectParams.eventID;
            SwingUtilities.invokeLater(() -> {
                //do not set text field if we are already processing next event
                if (currentEventID < lastEventID) {
                    return;
                }
                makefileTextField.getDocument().removeDocumentListener(WizardValidationWorker.this);
                makefileTextField.setText(makefileNameText);
                projectParams.setMakefile(makefileNameText);
                makefileTextField.getDocument().addDocumentListener(WizardValidationWorker.this);
            });

        }

        public ValidationResult validate() {
            if (projectParams.eventID < lastEventID) {
                return new ValidationResult(Boolean.FALSE, null);
            }
            String projectFolder = projectParams.createdProjectFolder.trim();
            String projectLocation = projectParams.projectLocation.trim();
            if (projectFolder.isEmpty() || projectLocation.isEmpty()) {
                String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalProjectLocation"); // NOI18N
                return new ValidationResult(Boolean.FALSE, message);
            }
            ValidationResult result = isValidLocalProjectNameAndLocation(projectNameTextField.getText(), projectLocation, projectFolder);
            if (!result.isValid) {
                return result;
            }
            String makefileName = projectParams.makefileTextField;
            if (makefileName.contains(" ")) {//NOI18N
                String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_SpacesInMakefile");// NOI18N
                return new ValidationResult(Boolean.FALSE, message);
            }
            if (!isValidMakeFile(makefileName)) {
                String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalMakefileName");//NOI18N
                return new ValidationResult(Boolean.FALSE, message);
            }
            if (projectParams.isMainCreated && !isValidMainFile(projectParams.createdMain)) {
                String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_IllegalMainFileName");//NOI18N
                return new ValidationResult(Boolean.FALSE, message);
            }
            if (Thread.interrupted()) {
                return new ValidationResult(Boolean.FALSE, null);
            }
            FileObject projectDirFO = fileSystem.findResource(projectFolder); // can be null
            if (projectDirFO != null && projectDirFO.isValid()) {
                if (projectDirFO.isData()) {
                    String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectfolderNotEmpty", makefileName);//NOI18N
                    return new ValidationResult(Boolean.FALSE, message);
                }
                if (Thread.interrupted()) {
                    return new ValidationResult(Boolean.FALSE, null);
                }
                FileObject nbProjFO = projectDirFO.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
                if (nbProjFO != null && nbProjFO.isValid()) {
                    String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectfolderNotEmpty", MakeConfiguration.NBPROJECT_FOLDER);//NOI18N
                    return new ValidationResult(Boolean.FALSE, message);
                }
                if (Thread.interrupted()) {
                    return new ValidationResult(Boolean.FALSE, null);
                }
                FileObject makeFO = fileSystem.findResource(projectDirFO.getPath() + fsFileSeparator + makefileName);
                if (makeFO != null && makeFO.isValid()) {
                    String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectfolderNotEmpty", makefileName);//NOI18N
                    // Folder exists and is not empty
                    return new ValidationResult(Boolean.FALSE, message);
                }
                if (Thread.interrupted()) {
                    return new ValidationResult(Boolean.FALSE, null);
                }
                FileObject nbFO = fileSystem.findResource(projectDirFO.getPath() + fsFileSeparator + MakeConfiguration.NBPROJECT_FOLDER);
                if (nbFO != null && nbFO.isValid()) {
                    String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectfolderNotEmpty", MakeConfiguration.NBPROJECT_FOLDER);//NOI18N
                    // Folder exists and is not empty
                    return new ValidationResult(Boolean.FALSE, message);
                }
                if (Thread.interrupted()) {
                    return new ValidationResult(Boolean.FALSE, null);
                }
                if (type != NewMakeProjectWizardIterator.TYPE_MAKEFILE && type != NewMakeProjectWizardIterator.TYPE_BINARY) {
                    FileObject destFO = fileSystem.findResource(projectDirFO.getPath() + fsFileSeparator + MakeConfiguration.DIST_FOLDER);
                    if (destFO != null && destFO.isValid()) {
                        String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectFolderExists");//NOI18N
                        // Folder exists and is not empty
                        return new ValidationResult(Boolean.FALSE, message);
                    }
                    FileObject buildFO = fileSystem.findResource(projectDirFO.getPath() + fsFileSeparator + MakeConfiguration.BUILD_FOLDER);
                    if (buildFO != null && buildFO.isValid()) {
                        String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectFolderExists");//NOI18N
                        // Folder exists and is not empty
                        return new ValidationResult(Boolean.FALSE, message);
                    }
                }
            } else {
                if (Thread.interrupted()) {
                    return new ValidationResult(Boolean.FALSE, null);
                }
                FileObject existingParent = getExistingParent(projectFolder);
                if (existingParent == null) {
                    String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectFolderReadOnly");//NOI18N
                    return new ValidationResult(Boolean.FALSE, message);
                }
                if (!existingParent.canWrite()) {
                    String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_ProjectFolderReadOnly");//NOI18N
                    return new ValidationResult(Boolean.FALSE, message);
                }
            }
            if (Thread.interrupted()) {
                return new ValidationResult(Boolean.FALSE, null);
            }
            Object sr = hostComboBox.getSelectedItem();
            if (!(sr instanceof ServerRecord) || !((ServerRecord)sr).isOnline()) {
                String message = NbBundle.getMessage(PanelProjectLocationVisual.class, "MSG_OfflineHost");
                return new ValidationResult(Boolean.TRUE, message);
            }
            return new ValidationResult(Boolean.TRUE, null);
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            updateDocument(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateDocument(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateDocument(e);
        }

        private void updateDocument(DocumentEvent e) {
            final String projectName = projectNameTextField.getText().trim();
            String projectFolder = projectLocationTextField.getText().trim();
            if (e.getDocument() == projectNameTextField.getDocument() || e.getDocument() == projectLocationTextField.getDocument()) {
                while (projectFolder.endsWith("/") || projectFolder.endsWith("\\")) { // NOI18N
                    projectFolder = projectFolder.substring(0, projectFolder.length() - 1);
                }
                final String projectFolderText = projectFolder;
                final String createdFolderTextFieldValue = projectFolderText + fsFileSeparator + projectName;
                createdFolderTextField.setText(createdFolderTextFieldValue);
                // re-evaluate name of master project file.
                if (makefileTextField.getText().trim().isEmpty()) {
                    String makefileName =
                            !templateName.equals(NewMakeProjectWizardIterator.MAKEFILEPROJECT_PROJECT_NAME)
                            ? MakeConfigurationDescriptor.DEFAULT_PROJECT_MAKFILE_NAME : contructProjectMakefileName(projectName, 0);
                    makefileTextField.getDocument().removeDocumentListener(this);
                    makefileTextField.setText(makefileName);
                    makefileTextField.getDocument().addDocumentListener(this);
                }
            }
            if (e.getDocument() == makefileTextField.getDocument()) {
                makefileNameChangedManually.set(true);
            }
            String createdFolderTextFieldValue = createdFolderTextField.getText().trim();
            //form here project params which will be used in validation work
            projectParams = new ProjectValidationParams(projectName, projectFolder, createdFolderTextFieldValue);
            projectParams.setMainAttributes(createMainCheckBox.isSelected(), createMainTextField.getText().trim());
            projectParams.setMakefile(makefileTextField.getText().trim());

            handleProjectParamsChanges();
            //run pre-validation and to not schedule task
            if (projectNameTextField.getDocument() == e.getDocument()) {
                firePropertyChange(PROP_PROJECT_NAME, null, projectNameTextField.getText());
            }
            if (createMainTextField.getDocument() == e.getDocument()) {
                firePropertyChange(PROP_MAIN_NAME, null, createMainTextField.getText());
            }
        }

        private void handleProjectParamsChanges() {
            synchronized (wizardValidationExecutorLock) {
                if (wizardValidationExecutor.isShutdown()) {
                    return;
                }
            }
            //will handle next event
            if (projectParams != null) {
                projectParams.setRequestID(++lastEventID);
            }
            ValidationResult validationResult = new ValidationResult(Boolean.FALSE, NbBundle.getMessage(PanelProjectLocationVisual.class, "PanelProjectLocationVisual.Validating_Wizard"));
            currentState = new WizardValidationWorkerCheckState(Boolean.TRUE, validationResult);//NOI18N
            setError();
            synchronized (wizardValidationExecutorLock) {
                if (wizardValidationTask != null) {
                    wizardValidationTask.cancel(true);
                }
                wizardValidationTask = wizardValidationExecutor.schedule(this,
                        VALIDATION_DELAY, TimeUnit.MILLISECONDS);
            }
        }

        void cancel() {
            synchronized (wizardValidationExecutorLock) {
                if (wizardValidationTask != null) {
                    wizardValidationTask.cancel(true);
                }
            }
        }

        void shutdown() {
            synchronized (wizardValidationExecutorLock) {
                if (wizardValidationTask != null) {
                    wizardValidationTask.cancel(true);
                }
                wizardValidationExecutor.shutdown();
            }
        }


        @Override
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == this) {
                //ignore own ones
                return;
            }
            handleProjectParamsChanges();
        }
    }

}
