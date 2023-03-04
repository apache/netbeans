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
package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.connections.common.RemoteUtils;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.runconfigs.RunConfigRemote;
import org.netbeans.modules.php.project.ui.SourcesFolderProvider;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.UploadFiles;
import org.netbeans.modules.php.project.ui.customizer.RunAsPanel;
import org.netbeans.modules.php.project.ui.customizer.RunAsRemoteWeb.RemoteConnectionRenderer;
import org.netbeans.modules.php.project.ui.customizer.RunAsRemoteWeb.RemoteUploadRenderer;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public final class RunAsRemoteWeb extends RunAsPanel.InsidePanel {

    private static final long serialVersionUID = 857876755153456465L;

    private static final UploadFiles DEFAULT_UPLOAD_FILES = UploadFiles.ON_RUN;

    final ChangeSupport changeSupport = new ChangeSupport(this);
    private final JLabel[] labels;
    private final JTextField[] textFields;
    private final String[] propertyNames;
    private final SourcesFolderProvider sourcesFolderProvider;


    public RunAsRemoteWeb(ConfigManager manager, SourcesFolderProvider sourcesFolderProvider) {
        super(manager);
        this.sourcesFolderProvider = sourcesFolderProvider;

        initComponents();

        labels = new JLabel[] {
            urlLabel,
            uploadDirectoryLabel,
            indexFileLabel,
        };
        textFields = new JTextField[] {
            urlTextField,
            uploadDirectoryTextField,
            indexFileTextField,
        };
        propertyNames = new String[] {
            RunConfigurationPanel.URL,
            RunConfigurationPanel.REMOTE_DIRECTORY,
            RunConfigurationPanel.INDEX_FILE,
        };
        assert labels.length == textFields.length && labels.length == propertyNames.length;

        populateRemoteConnectionComboBox();
        remoteConnectionComboBox.setRenderer(new RemoteConnectionRenderer());
        remoteConnectionComboBox.setKeySelectionManager(RemoteUtils.createRemoteConfigurationKeySelectionManager());
        for (UploadFiles uploadFiles : UploadFiles.values()) {
            uploadFilesComboBox.addItem(uploadFiles);
        }
        uploadFilesComboBox.setRenderer(new RemoteUploadRenderer());

        // listeners
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
        remoteConnectionComboBox.addActionListener(new ComboBoxUpdater(RunConfigurationPanel.REMOTE_CONNECTION, remoteConnectionLabel,
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
        uploadFilesComboBox.addActionListener(new ComboBoxUpdater(RunConfigurationPanel.REMOTE_UPLOAD, uploadFilesLabel, uploadFilesComboBox,
                remoteUploadConvertor));
        runAsComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeSupport.fireChange();
            }
        });

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
        String remoteUpload = getValue(RunConfigurationPanel.REMOTE_UPLOAD);
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
    }

    @Override
    protected void validateFields() {
        changeSupport.fireChange();
    }

    public void addRunAsRemoteWebListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeRunAsRemoteWebListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
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
        String remoteConnection = getValue(RunConfigurationPanel.REMOTE_CONNECTION);
        // #141849 - can be null if one adds remote config for the first time for a project but already has some remote connection
        DefaultComboBoxModel<RemoteConfiguration> model = (DefaultComboBoxModel<RemoteConfiguration>) remoteConnectionComboBox.getModel();
        if (remoteConnection == null
                && model.getIndexOf(RunConfigRemote.NO_REMOTE_CONFIGURATION) != -1) {
            remoteConnectionComboBox.setSelectedItem(RunConfigRemote.NO_REMOTE_CONFIGURATION);
            return;
        }
        int size = remoteConnectionComboBox.getModel().getSize();
        for (int i = 0; i < size; ++i) {
            RemoteConfiguration rc = remoteConnectionComboBox.getItemAt(i);
            if (remoteConnection == null
                    || RunConfigRemote.NO_CONFIG_NAME.equals(remoteConnection)
                    || remoteConnection.equals(rc.getName())) {
                // select existing or
                // if no configuration formerly existed and now some were created => so select the first one
                remoteConnectionComboBox.setSelectedItem(rc);
                return;
            }
        }
        // #165549
        if (model.getIndexOf(RunConfigRemote.NO_REMOTE_CONFIGURATION) == -1) {
            model.addElement(RunConfigRemote.NO_REMOTE_CONFIGURATION);
        }
        remoteConnectionComboBox.setSelectedItem(RunConfigRemote.NO_REMOTE_CONFIGURATION);
    }

    public RunConfigRemote createRunConfig() {
        return RunConfigRemote.create()
                .setUrl(urlTextField.getText().trim())
                .setIndexParentDir(sourcesFolderProvider.getSourcesFolder())
                .setIndexRelativePath(indexFileTextField.getText())
                .setRemoteConfiguration((RemoteConfiguration) remoteConnectionComboBox.getSelectedItem())
                .setUploadDirectory(uploadDirectoryTextField.getText().trim())
                .setUploadFilesType((UploadFiles) uploadFilesComboBox.getSelectedItem());
    }

    public void setUrl(String url) {
        urlTextField.setText(url);
    }

    public void setRemoteConfiguration(RemoteConfiguration remoteConfiguration) {
        remoteConnectionComboBox.setSelectedItem(remoteConfiguration);
    }

    public void setUploadDirectory(String uploadDirectory) {
        uploadDirectoryTextField.setText(uploadDirectory);
    }

    public void setUploadFiles(UploadFiles uploadFiles) {
        uploadFilesComboBox.setSelectedItem(uploadFiles);
    }

    public void setIndexFile(String indexFile) {
        indexFileTextField.setText(indexFile);
    }

    public void hideIndexFile() {
        indexFileLabel.setVisible(false);
        indexFileTextField.setVisible(false);
        indexFileBrowseButton.setVisible(false);
    }

    public void hideRunAs() {
        runAsLabel.setVisible(false);
        runAsComboBox.setVisible(false);
    }

    public void hideUploadFiles() {
        uploadFilesLabel.setVisible(false);
        uploadFilesComboBox.setVisible(false);
        uploadFilesHintLabel.setVisible(false);
    }

    void updateRemoteConnectionHint() {
        remoteConnectionHintLabel.setText(createRunConfig().getRemoteConnectionHint());
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
        remoteConnectionLabel = new JLabel();
        remoteConnectionComboBox = new JComboBox<RemoteConfiguration>();
        manageRemoteConnectionButton = new JButton();
        uploadDirectoryLabel = new JLabel();
        uploadDirectoryTextField = new JTextField();
        remoteConnectionHintLabel = new JLabel();
        uploadFilesLabel = new JLabel();
        uploadFilesComboBox = new JComboBox<UploadFiles>();
        uploadFilesHintLabel = new JLabel();
        indexFileLabel = new JLabel();
        indexFileTextField = new JTextField();
        indexFileBrowseButton = new JButton();

        runAsLabel.setLabelFor(runAsComboBox);
        Mnemonics.setLocalizedText(runAsLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_RunAs")); // NOI18N

        urlLabel.setLabelFor(urlTextField);
        Mnemonics.setLocalizedText(urlLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_ProjectUrl")); // NOI18N

        urlTextField.setColumns(20);

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

        Mnemonics.setLocalizedText(remoteConnectionHintLabel, "dummy"); // NOI18N

        uploadFilesLabel.setLabelFor(uploadFilesComboBox);
        Mnemonics.setLocalizedText(uploadFilesLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_UploadFiles")); // NOI18N

        Mnemonics.setLocalizedText(uploadFilesHintLabel, "dummy"); // NOI18N

        indexFileLabel.setLabelFor(indexFileTextField);
        Mnemonics.setLocalizedText(indexFileLabel, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_IndexFile")); // NOI18N

        indexFileTextField.setColumns(20);

        Mnemonics.setLocalizedText(indexFileBrowseButton, NbBundle.getMessage(RunAsRemoteWeb.class, "LBL_BrowseIndex")); // NOI18N
        indexFileBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                indexFileBrowseButtonActionPerformed(evt);
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
                    .addComponent(indexFileLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(remoteConnectionHintLabel)
                        .addContainerGap())
                    .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(indexFileTextField)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(indexFileBrowseButton))
                        .addComponent(urlTextField, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                        .addComponent(uploadFilesHintLabel)
                        .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(remoteConnectionComboBox, 0, 1, Short.MAX_VALUE)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(manageRemoteConnectionButton))
                        .addComponent(uploadDirectoryTextField)
                        .addComponent(uploadFilesComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(runAsComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
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
                .addGap(18, 18, 18)
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
                .addComponent(uploadFilesHintLabel))
        );

        runAsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.runAsLabel.AccessibleContext.accessibleName")); // NOI18N
        runAsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.runAsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        runAsComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.runAsComboBox.AccessibleContext.accessibleName")); // NOI18N
        runAsComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.runAsComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        urlLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.urlLabel.AccessibleContext.accessibleName")); // NOI18N
        urlLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsLocalWeb.urlLabel.AccessibleContext.accessibleDescription")); // NOI18N
        urlTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.urlTextField.AccessibleContext.accessibleName")); // NOI18N
        urlTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.urlTextField.AccessibleContext.accessibleDescription")); // NOI18N
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
        indexFileLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileLabel.AccessibleContext.accessibleName")); // NOI18N
        indexFileLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileLabel.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileTextField.AccessibleContext.accessibleName")); // NOI18N
        indexFileTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileTextField.AccessibleContext.accessibleDescription")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        indexFileBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.indexFileBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunAsRemoteWeb.class, "RunAsRemoteWeb.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    private void manageRemoteConnectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageRemoteConnectionButtonActionPerformed
        if (RemoteConnections.get().openManager((RemoteConfiguration) remoteConnectionComboBox.getSelectedItem())) {
            populateRemoteConnectionComboBox();
            selectRemoteConnection();
            updateRemoteConnectionHint();
        }
    }//GEN-LAST:event_manageRemoteConnectionButtonActionPerformed

    private void indexFileBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_indexFileBrowseButtonActionPerformed
        try {
            Utils.browseFolderFile(PhpVisibilityQuery.getDefault(), sourcesFolderProvider.getSourcesFolder(), indexFileTextField);
        } catch (FileNotFoundException ex) {
            // cannot happen for sources
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_indexFileBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton indexFileBrowseButton;
    private JLabel indexFileLabel;
    private JTextField indexFileTextField;
    private JButton manageRemoteConnectionButton;
    private JComboBox<RemoteConfiguration> remoteConnectionComboBox;
    private JLabel remoteConnectionHintLabel;
    private JLabel remoteConnectionLabel;
    private JComboBox<String> runAsComboBox;
    private JLabel runAsLabel;
    private JLabel uploadDirectoryLabel;
    private JTextField uploadDirectoryTextField;
    private JComboBox<UploadFiles> uploadFilesComboBox;
    private JLabel uploadFilesHintLabel;
    private JLabel uploadFilesLabel;
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
            if (getPropName().equals(RunConfigurationPanel.REMOTE_DIRECTORY)) {
                value = RemoteUtils.sanitizeUploadDirectory(value, true);
            }
            return value;
        }

        @Override
        protected final String getDefaultValue() {
            return RunAsRemoteWeb.this.getDefaultValue(getPropName());
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
}
