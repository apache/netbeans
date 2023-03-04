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
package org.netbeans.modules.php.project.ui.customizer;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.connections.common.RemoteUtils;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.runconfigs.RunConfigRemote;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigRemoteValidator;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.UploadFiles;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * @author Tomas Mysik
 */
public final class RunAsRemoteWeb extends RunAsPanel.InsidePanel {

    private static final long serialVersionUID = 1654646512354658L;

    private static final UploadFiles DEFAULT_UPLOAD_FILES = UploadFiles.ON_RUN;

    final PhpProjectProperties properties;
    final PhpProject project;
    final Category category;

    private final JLabel[] labels;
    private final JTextField[] textFields;
    private final String[] propertyNames;


    public RunAsRemoteWeb(final PhpProjectProperties properties, ConfigManager manager, Category category) {
        super(manager);
        this.properties = properties;
        this.category = category;
        project = properties.getProject();

        initComponents();

        labels = new JLabel[] {
            urlLabel,
            indexFileLabel,
            argsLabel,
            uploadDirectoryLabel,
        };
        textFields = new JTextField[] {
            urlTextField,
            indexFileTextField,
            argsTextField,
            uploadDirectoryTextField,
        };
        propertyNames = new String[] {
            PhpProjectProperties.URL,
            PhpProjectProperties.INDEX_FILE,
            PhpProjectProperties.ARGS,
            PhpProjectProperties.REMOTE_DIRECTORY,
        };
        assert labels.length == textFields.length && labels.length == propertyNames.length;

        populateRemoteConnectionComboBox();
        remoteConnectionComboBox.setRenderer(new RemoteConnectionRenderer());
        remoteConnectionComboBox.setKeySelectionManager(RemoteUtils.createRemoteConfigurationKeySelectionManager());
        for (UploadFiles uploadFiles : UploadFiles.values()) {
            uploadFilesComboBox.addItem(uploadFiles);
        }
        uploadFilesComboBox.setRenderer(new RemoteUploadRenderer());

        registerListeners();
    }

    private void registerListeners() {
        for (int i = 0; i < textFields.length; i++) {
            DocumentListener dl = new FieldUpdater(propertyNames[i], labels[i], textFields[i]);
            textFields[i].getDocument().addDocumentListener(dl);
        }
        // remote connection
        ComboBoxSelectedItemConvertor remoteConfigurationConvertor = new ComboBoxSelectedItemConvertor() {
            @Override
            public String convert(JComboBox comboBox) {
                RemoteConfiguration remoteConfiguration = (RemoteConfiguration) comboBox.getSelectedItem();
                assert remoteConfiguration != null;
                return remoteConfiguration.getName();
            }
        };
        remoteConnectionComboBox.addActionListener(new ComboBoxUpdater(PhpProjectProperties.REMOTE_CONNECTION, remoteConnectionLabel,
                remoteConnectionComboBox, remoteConfigurationConvertor));
        // remote upload
        ComboBoxSelectedItemConvertor remoteUploadConvertor = new ComboBoxSelectedItemConvertor() {
            @Override
            public String convert(JComboBox comboBox) {
                UploadFiles uploadFiles = (UploadFiles) comboBox.getSelectedItem();
                assert uploadFiles != null;
                uploadFilesHintLabel.setText(uploadFiles.getDescription());
                return uploadFiles.name();
            }
        };
        uploadFilesComboBox.addActionListener(new ComboBoxUpdater(PhpProjectProperties.REMOTE_UPLOAD, uploadFilesLabel, uploadFilesComboBox,
                remoteUploadConvertor));

        // upload directory hint
        remoteConnectionComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateRemoteConnectionHint();
                }
            }
        });
        uploadDirectoryTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processUpdate();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                processUpdate();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                processUpdate();
            }
            private void processUpdate() {
                updateRemoteConnectionHint();
            }
        });
        updateRemoteConnectionHint();

        preservePermissionsCheckBox.addActionListener(new CheckBoxUpdater(
                PhpProjectProperties.REMOTE_PERMISSIONS, preservePermissionsCheckBox));
        uploadDirectlyCheckBox.addActionListener(new CheckBoxUpdater(
                PhpProjectProperties.REMOTE_UPLOAD_DIRECTLY, uploadDirectlyCheckBox));
    }

    @Override
    protected RunAsType getRunAsType() {
        return RunConfigRemote.getRunAsType();
    }

    @Override
    protected String getDisplayName() {
        return RunConfigRemote.getDisplayName();
    }

    @Override
    protected JComboBox<String> getRunAsCombo() {
        return runAsComboBox;
    }

    @Override
    protected JLabel getRunAsLabel() {
        return runAsLabel;
    }

    @Override
    protected void loadFields() {
        for (int i = 0; i < textFields.length; i++) {
            textFields[i].setText(getValue(propertyNames[i]));
        }
        // remote connection
        selectRemoteConnection();
        // remote upload
        UploadFiles uploadFiles = null;
        String remoteUpload = getValue(PhpProjectProperties.REMOTE_UPLOAD);
        if (remoteUpload == null) {
            uploadFiles = DEFAULT_UPLOAD_FILES;
        } else {
            try {
                uploadFiles = UploadFiles.valueOf(remoteUpload);
            } catch (IllegalArgumentException iae) {
                uploadFiles = DEFAULT_UPLOAD_FILES;
            }
        }
        uploadFilesComboBox.setSelectedItem(uploadFiles);

        boolean preservePermissions = Boolean.parseBoolean(getValue(PhpProjectProperties.REMOTE_PERMISSIONS));
        preservePermissionsCheckBox.setSelected(preservePermissions);

        boolean uploadDirectly = Boolean.parseBoolean(getValue(PhpProjectProperties.REMOTE_UPLOAD_DIRECTLY));
        uploadDirectlyCheckBox.setSelected(uploadDirectly);
    }

    @Override
    protected void validateFields() {
        category.setErrorMessage(RunConfigRemoteValidator.validateCustomizer(createRunConfig()));
        // #148957 always allow to save customizer
        category.setValid(true);
    }

    RunConfigRemote createRunConfig() {
        return RunConfigRemote.create()
                .setUrl(urlTextField.getText())
                .setIndexParentDir(getWebRoot())
                .setIndexRelativePath(indexFileTextField.getText())
                .setArguments(argsTextField.getText())
                .setRemoteConfiguration((RemoteConfiguration) remoteConnectionComboBox.getSelectedItem())
                .setUploadDirectory(uploadDirectoryTextField.getText())
                .setUploadFilesType((UploadFiles) uploadFilesComboBox.getSelectedItem())
                .setPermissionsPreserved(preservePermissionsCheckBox.isSelected())
                .setUploadDirectly(uploadDirectlyCheckBox.isSelected());
    }

    private File getWebRoot() {
        return ProjectPropertiesSupport.getSourceSubdirectory(project, properties.getWebRoot());
    }

    private void populateRemoteConnectionComboBox() {
        List<RemoteConfiguration> connections = RemoteConnections.get().getRemoteConfigurations();
        if (connections.isEmpty()) {
            // no connections defined
            connections = Arrays.asList(RunConfigRemote.NO_REMOTE_CONFIGURATION);
        }
        DefaultComboBoxModel<RemoteConfiguration> model = new DefaultComboBoxModel<>(new Vector<>(connections));
        remoteConnectionComboBox.setModel(model);
    }

    private void selectRemoteConnection() {
        selectRemoteConnection(null);
    }

    private void selectRemoteConnection(String remoteConnection) {
        if (remoteConnection == null) {
            remoteConnection = getValue(PhpProjectProperties.REMOTE_CONNECTION);
        }
        // #141849 - can be null if one adds remote config for the first time for a project but already has some remote connection
        DefaultComboBoxModel<RemoteConfiguration> model = (DefaultComboBoxModel<RemoteConfiguration>) remoteConnectionComboBox.getModel();
        if (remoteConnection == null
                || RunConfigRemote.NO_CONFIG_NAME.equals(remoteConnection)) {
            if (model.getIndexOf(RunConfigRemote.NO_REMOTE_CONFIGURATION) < 0) {
                model.insertElementAt(RunConfigRemote.NO_REMOTE_CONFIGURATION, 0);
            }
            remoteConnectionComboBox.setSelectedItem(RunConfigRemote.NO_REMOTE_CONFIGURATION);
            return;
        }

        int size = remoteConnectionComboBox.getModel().getSize();
        for (int i = 0; i < size; ++i) {
            RemoteConfiguration rc = remoteConnectionComboBox.getItemAt(i);
            if (remoteConnection.equals(rc.getName())) {
                remoteConnectionComboBox.setSelectedItem(rc);
                return;
            }
        }

        // remote connection is missing (probably removed?)
        remoteConnectionComboBox.addItem(RunConfigRemote.MISSING_REMOTE_CONFIGURATION);
        remoteConnectionComboBox.setSelectedItem(RunConfigRemote.MISSING_REMOTE_CONFIGURATION);
        // # 162230
        model.removeElement(RunConfigRemote.NO_REMOTE_CONFIGURATION);
    }

    void updateRemoteConnectionHint() {
        String hint = createRunConfig().getRemoteConnectionHint();
        remoteConnectionHintLabel.setText(hint != null ? "<html><body>" + hint : " "); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        runAsLabel = new JLabel();
        runAsComboBox = new JComboBox<String>();
        urlLabel = new JLabel();
        urlTextField = new JTextField();
        indexFileLabel = new JLabel();
        indexFileTextField = new JTextField();
        indexFileBrowseButton = new JButton();
        argsLabel = new JLabel();
        argsTextField = new JTextField();
        urlHintLabel = new JTextPane();
        remoteConnectionLabel = new JLabel();
        remoteConnectionComboBox = new JComboBox<RemoteConfiguration>();
        manageRemoteConnectionButton = new JButton();
        uploadDirectoryLabel = new JLabel();
        uploadDirectoryTextField = new JTextField();
        remoteConnectionHintLabel = new JLabel();
        uploadFilesLabel = new JLabel();
        uploadFilesComboBox = new JComboBox<UploadFiles>();
        uploadFilesHintLabel = new JLabel();
        preservePermissionsCheckBox = new JCheckBox();
        preservePermissionsLabel = new JLabel();
        uploadDirectlyCheckBox = new JCheckBox();
        uploadDirectlyLabel = new JLabel();
        advancedButton = new JButton();

        runAsLabel.setLabelFor(runAsComboBox);
        Mnemonics.setLocalizedText(runAsLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_RunAs")); // NOI18N

        urlLabel.setLabelFor(urlTextField);
        Mnemonics.setLocalizedText(urlLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_ProjectUrl")); // NOI18N

        urlTextField.setColumns(20);

        indexFileLabel.setLabelFor(indexFileTextField);
        Mnemonics.setLocalizedText(indexFileLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_IndexFile")); // NOI18N

        Mnemonics.setLocalizedText(indexFileBrowseButton, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_Browse")); // NOI18N
        indexFileBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                indexFileBrowseButtonActionPerformed(evt);
            }
        });

        argsLabel.setLabelFor(argsTextField);
        Mnemonics.setLocalizedText(argsLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_Arguments")); // NOI18N

        argsTextField.setColumns(20);

        urlHintLabel.setEditable(false);
        urlHintLabel.setBackground(UIManager.getDefaults().getColor("Label.background"));
        urlHintLabel.setBorder(null);
        urlHintLabel.setFocusable(false);

        remoteConnectionLabel.setLabelFor(remoteConnectionComboBox);
        Mnemonics.setLocalizedText(remoteConnectionLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_RemoteConnection")); // NOI18N

        Mnemonics.setLocalizedText(manageRemoteConnectionButton, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_Manage")); // NOI18N
        manageRemoteConnectionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                manageRemoteConnectionButtonActionPerformed(evt);
            }
        });

        uploadDirectoryLabel.setLabelFor(uploadDirectoryTextField);
        Mnemonics.setLocalizedText(uploadDirectoryLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_UploadDirectory")); // NOI18N

        uploadDirectoryTextField.setColumns(20);

        remoteConnectionHintLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(remoteConnectionHintLabel, "dummy"); // NOI18N

        uploadFilesLabel.setLabelFor(uploadFilesComboBox);
        Mnemonics.setLocalizedText(uploadFilesLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_UploadFiles")); // NOI18N

        uploadFilesHintLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(uploadFilesHintLabel, "dummy"); // NOI18N

        Mnemonics.setLocalizedText(preservePermissionsCheckBox, NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.preservePermissionsCheckBox.text")); // NOI18N

        preservePermissionsLabel.setLabelFor(preservePermissionsCheckBox);
        Mnemonics.setLocalizedText(preservePermissionsLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.preservePermissionsLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(uploadDirectlyCheckBox, NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectlyCheckBox.text")); // NOI18N

        uploadDirectlyLabel.setLabelFor(uploadDirectlyCheckBox);
        Mnemonics.setLocalizedText(uploadDirectlyLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectlyLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(advancedButton, NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.advancedButton.text")); // NOI18N
        advancedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                advancedButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(remoteConnectionLabel)
                    .addComponent(uploadDirectoryLabel)
                    .addComponent(uploadFilesLabel)
                    .addComponent(urlLabel)
                    .addComponent(runAsLabel)
                    .addComponent(indexFileLabel)
                    .addComponent(argsLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(urlHintLabel)
                    .addComponent(urlTextField, Alignment.TRAILING)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(indexFileTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(indexFileBrowseButton))
                    .addComponent(argsTextField, Alignment.TRAILING)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(remoteConnectionComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(manageRemoteConnectionButton))
                    .addComponent(uploadDirectoryTextField)
                    .addComponent(uploadFilesComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(runAsComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(advancedButton, Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(remoteConnectionHintLabel)
                            .addComponent(uploadFilesHintLabel))
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(preservePermissionsCheckBox)
                    .addComponent(uploadDirectlyCheckBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(preservePermissionsLabel)
                            .addComponent(uploadDirectlyLabel))))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {indexFileBrowseButton, manageRemoteConnectionButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(runAsLabel)
                    .addComponent(runAsComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(urlLabel)
                    .addComponent(urlTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(indexFileLabel)
                    .addComponent(indexFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(indexFileBrowseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(argsLabel)
                    .addComponent(argsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(urlHintLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(remoteConnectionLabel)
                    .addComponent(manageRemoteConnectionButton)
                    .addComponent(remoteConnectionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(uploadDirectoryLabel)
                    .addComponent(uploadDirectoryTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(remoteConnectionHintLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(uploadFilesLabel)
                    .addComponent(uploadFilesComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(uploadFilesHintLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(preservePermissionsCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(preservePermissionsLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(uploadDirectlyCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(uploadDirectlyLabel)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(advancedButton))
        );

        runAsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.runAsLabel.AccessibleContext.accessibleName")); // NOI18N
        runAsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.runAsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        runAsComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.runAsComboBox.AccessibleContext.accessibleName")); // NOI18N
        runAsComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.runAsComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        urlLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.urlLabel.AccessibleContext.accessibleName")); // NOI18N
        urlLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.urlLabel.AccessibleContext.accessibleDescription")); // NOI18N
        urlTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.urlTextField.AccessibleContext.accessibleName")); // NOI18N
        urlTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.urlTextField.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileLabel.AccessibleContext.accessibleName")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileLabel.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileTextField.AccessibleContext.accessibleName")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileTextField.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        argsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.argsLabel.AccessibleContext.accessibleName")); // NOI18N
        argsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.argsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        argsTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.argsTextField.AccessibleContext.accessibleName")); // NOI18N
        argsTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.argsTextField.AccessibleContext.accessibleDescription")); // NOI18N
        urlHintLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.jTextPane1.AccessibleContext.accessibleName")); // NOI18N
        urlHintLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.jTextPane1.AccessibleContext.accessibleDescription")); // NOI18N
        remoteConnectionLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.remoteConnectionLabel.AccessibleContext.accessibleName")); // NOI18N
        remoteConnectionLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.remoteConnectionLabel.AccessibleContext.accessibleDescription")); // NOI18N
        remoteConnectionComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.remoteConnectionComboBox.AccessibleContext.accessibleName")); // NOI18N
        remoteConnectionComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.remoteConnectionComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        manageRemoteConnectionButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.manageRemoteConnectionButton.AccessibleContext.accessibleName")); // NOI18N
        manageRemoteConnectionButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.manageRemoteConnectionButton.AccessibleContext.accessibleDescription")); // NOI18N
        uploadDirectoryLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectoryLabel.AccessibleContext.accessibleName")); // NOI18N
        uploadDirectoryLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectoryLabel.AccessibleContext.accessibleDescription")); // NOI18N
        uploadDirectoryTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectoryTextField.AccessibleContext.accessibleName")); // NOI18N
        uploadDirectoryTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectoryTextField.AccessibleContext.accessibleDescription")); // NOI18N
        remoteConnectionHintLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.remoteConnectionHintLabel.AccessibleContext.accessibleName")); // NOI18N
        remoteConnectionHintLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.remoteConnectionHintLabel.AccessibleContext.accessibleDescription")); // NOI18N
        uploadFilesLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadFilesLabel.AccessibleContext.accessibleName")); // NOI18N
        uploadFilesLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadFilesLabel.AccessibleContext.accessibleDescription")); // NOI18N
        uploadFilesComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadFilesComboBox.AccessibleContext.accessibleName")); // NOI18N
        uploadFilesComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadFilesComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        uploadFilesHintLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadFilesHintLabel.AccessibleContext.accessibleName")); // NOI18N
        uploadFilesHintLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadFilesHintLabel.AccessibleContext.accessibleDescription")); // NOI18N
        preservePermissionsCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.preservePermissionsCheckBox.AccessibleContext.accessibleName")); // NOI18N
        preservePermissionsCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.preservePermissionsCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        preservePermissionsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.preservePermissionsLabel.AccessibleContext.accessibleName")); // NOI18N
        preservePermissionsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.preservePermissionsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        uploadDirectlyCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectlyCheckBox.AccessibleContext.accessibleName")); // NOI18N
        uploadDirectlyCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectlyCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        uploadDirectlyLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectlyLabel.AccessibleContext.accessibleName")); // NOI18N
        uploadDirectlyLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.uploadDirectlyLabel.AccessibleContext.accessibleDescription")); // NOI18N
        advancedButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.advancedButton.AccessibleContext.accessibleName")); // NOI18N
        advancedButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.advancedButton.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void manageRemoteConnectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageRemoteConnectionButtonActionPerformed
        if (RemoteConnections.get().openManager((RemoteConfiguration) remoteConnectionComboBox.getSelectedItem())) {
            populateRemoteConnectionComboBox();
            // # 162233
            String selected = null;
            ComboBoxModel model = remoteConnectionComboBox.getModel();
            if (model.getSize() == 1) {
                selected = ((RemoteConfiguration) model.getElementAt(0)).getName();
            }
            selectRemoteConnection(selected);
            updateRemoteConnectionHint();
        }
    }//GEN-LAST:event_manageRemoteConnectionButtonActionPerformed

    @NbBundle.Messages("RunAsRemoteWeb.webRoot.notFound=Web Root directory does not exist (see Sources).")
    private void indexFileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexFileBrowseButtonActionPerformed
        try {
            Utils.browseFolderFile(PhpVisibilityQuery.forProject(project), getWebRoot(), indexFileTextField);
        } catch (FileNotFoundException ex) {
            category.setErrorMessage(Bundle.RunAsRemoteWeb_webRoot_notFound());
            category.setValid(true);
        }
    }//GEN-LAST:event_indexFileBrowseButtonActionPerformed

    private void advancedButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_advancedButtonActionPerformed
        RunAsWebAdvanced.Properties props = new RunAsWebAdvanced.Properties(
                getValue(PhpProjectProperties.DEBUG_URL),
                urlHintLabel.getText(),
                getValue(PhpProjectProperties.DEBUG_PATH_MAPPING_REMOTE),
                getValue(PhpProjectProperties.DEBUG_PATH_MAPPING_LOCAL),
                getValue(PhpProjectProperties.DEBUG_PROXY_HOST),
                getValue(PhpProjectProperties.DEBUG_PROXY_PORT));
        RunAsWebAdvanced advanced = new RunAsWebAdvanced(project, props);
        if (advanced.open()) {
            Pair<String, String> pathMapping = advanced.getPathMapping();
            Pair<String, String> debugProxy = advanced.getDebugProxy();
            RunAsRemoteWeb.this.putValue(PhpProjectProperties.DEBUG_URL, advanced.getDebugUrl().name());
            RunAsRemoteWeb.this.putValue(PhpProjectProperties.DEBUG_PATH_MAPPING_REMOTE, pathMapping.first());
            RunAsRemoteWeb.this.putValue(PhpProjectProperties.DEBUG_PATH_MAPPING_LOCAL, pathMapping.second());
            RunAsRemoteWeb.this.putValue(PhpProjectProperties.DEBUG_PROXY_HOST, debugProxy.first());
            RunAsRemoteWeb.this.putValue(PhpProjectProperties.DEBUG_PROXY_PORT, debugProxy.second());
        }
    }//GEN-LAST:event_advancedButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton advancedButton;
    private JLabel argsLabel;
    private JTextField argsTextField;
    private JButton indexFileBrowseButton;
    private JLabel indexFileLabel;
    private JTextField indexFileTextField;
    private JButton manageRemoteConnectionButton;
    private JCheckBox preservePermissionsCheckBox;
    private JLabel preservePermissionsLabel;
    private JComboBox<RemoteConfiguration> remoteConnectionComboBox;
    private JLabel remoteConnectionHintLabel;
    private JLabel remoteConnectionLabel;
    private JComboBox<String> runAsComboBox;
    private JLabel runAsLabel;
    private JCheckBox uploadDirectlyCheckBox;
    private JLabel uploadDirectlyLabel;
    private JLabel uploadDirectoryLabel;
    private JTextField uploadDirectoryTextField;
    private JComboBox<UploadFiles> uploadFilesComboBox;
    private JLabel uploadFilesHintLabel;
    private JLabel uploadFilesLabel;
    private JTextPane urlHintLabel;
    private JLabel urlLabel;
    private JTextField urlTextField;
    // End of variables declaration//GEN-END:variables

    private class FieldUpdater extends TextFieldUpdater {

        public FieldUpdater(String propName, JLabel label, JTextField field) {
            super(propName, label, field);
        }

        @Override
        protected String getPropValue() {
            String value = super.getPropValue();
            if (getPropName().equals(PhpProjectProperties.REMOTE_DIRECTORY)) {
                value = RemoteUtils.sanitizeUploadDirectory(value, true);
            }
            return value;
        }

        @Override
        protected final String getDefaultValue() {
            return RunAsRemoteWeb.this.getDefaultValue(getPropName());
        }

        @Override
        protected void processUpdate() {
            super.processUpdate();
            urlHintLabel.setText(createRunConfig().getUrlHint());
        }
    }

    interface ComboBoxSelectedItemConvertor {
        String convert(final JComboBox comboBox);
    }

    private class ComboBoxUpdater implements ActionListener {
        private final JLabel label;
        private final JComboBox field;
        private final String propName;
        private final ComboBoxSelectedItemConvertor comboBoxConvertor;

        public ComboBoxUpdater(String propName, JLabel label, JComboBox field, ComboBoxSelectedItemConvertor comboBoxConvertor) {
            this.propName = propName;
            this.label = label;
            this.field = field;
            this.comboBoxConvertor = comboBoxConvertor;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String value = comboBoxConvertor.convert(field);
            RunAsRemoteWeb.this.putValue(propName, value);
            RunAsRemoteWeb.this.markAsModified(label, propName, value);
            validateFields();
        }
    }

    private class CheckBoxUpdater implements ActionListener {
        private final JCheckBox field;
        private final String propName;

        public CheckBoxUpdater(String propName, JCheckBox field) {
            this.field = field;
            this.propName = propName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String value = Boolean.toString(field.isSelected());
            RunAsRemoteWeb.this.putValue(propName, value);
            RunAsRemoteWeb.this.markAsModified(field, propName, value);
            validateFields();
        }
    }

    public static class RemoteConnectionRenderer extends JLabel implements ListCellRenderer<RemoteConfiguration>, UIResource {

        private static final long serialVersionUID = 14547687982567897L;


        public RemoteConnectionRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends RemoteConfiguration> list, RemoteConfiguration value, int index, boolean isSelected, boolean cellHasFocus) {
            setName("ComboBox.listRenderer"); // NOI18N
            // #171722
            String text = null;
            Color foreground = null;
            if (value != null) {
                RemoteConfiguration remoteConfig = (RemoteConfiguration) value;
                text = remoteConfig.getDisplayName();
                foreground = getForeground(remoteConfig, list, isSelected);
            }
            setText(text);
            setIcon(null);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
            } else {
                setBackground(list.getBackground());
            }
            if (foreground != null) {
                setForeground(foreground);
            }
            return this;
        }

        private Color getForeground(RemoteConfiguration remoteConfig, JList list, boolean isSelected) {
            if (remoteConfig == RunConfigRemote.MISSING_REMOTE_CONFIGURATION
                    || remoteConfig == RunConfigRemote.NO_REMOTE_CONFIGURATION) {
                return UIManager.getColor("nb.errorForeground"); // NOI18N
            }
            return isSelected ? list.getSelectionForeground() : list.getForeground();
        }

        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name; // NOI18N
        }
    }

    public static class RemoteUploadRenderer extends JLabel implements ListCellRenderer<UploadFiles>, UIResource {

        private static final long serialVersionUID = 5867432135478679120L;


        public RemoteUploadRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends UploadFiles> list, UploadFiles value, int index, boolean isSelected, boolean cellHasFocus) {
            setName("ComboBox.listRenderer"); // NOI18N
            // #175236
            if (value != null) {
                setText(value.getLabel());
            }
            setIcon(null);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }

        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name; // NOI18N
        }
    }
}
