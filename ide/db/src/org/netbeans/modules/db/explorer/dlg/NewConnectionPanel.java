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
package org.netbeans.modules.db.explorer.dlg;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.db.util.DatabaseExplorerInternalUIs;
import org.netbeans.modules.db.util.JdbcUrl;
import org.netbeans.modules.db.util.PropertyEditorPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public class NewConnectionPanel extends ConnectionDialog.FocusablePanel {
    private static final Logger LOGGER = Logger.getLogger(NewConnectionPanel.class.getName());
    private static final Pattern numbers = Pattern.compile("(\\d+)");
    private static final String USERINPUT_FIELD = "<USERNAME>";
    
    private AddConnectionWizard wd;
    private DatabaseConnection connection;
    private ProgressHandle progressHandle;
    private Window window;
    private boolean updatingUrl = false;
    private boolean updatingFields = false;
    private final LinkedHashMap<String, UrlField> urlFields = new LinkedHashMap<>();
    
    private final ConnectionPanel wp;
    private Properties connectionProperties = new Properties();

    private void initFieldMap() {
        // These should be in the order of display on the form, so that we correctly
        // put focus on the first visible field.
        urlFields.put(JdbcUrl.TOKEN_HOST, new UrlField(hostField, hostLabel));
        urlFields.put(JdbcUrl.TOKEN_PORT, new UrlField(portField, portLabel));
        urlFields.put(JdbcUrl.TOKEN_DB, new UrlField(databaseField, databaseLabel));
        urlFields.put(JdbcUrl.TOKEN_SID, new UrlField(sidField, sidLabel));
        urlFields.put(JdbcUrl.TOKEN_SERVICENAME, new UrlField(serviceField, serviceLabel));
        urlFields.put(JdbcUrl.TOKEN_TNSNAME, new UrlField(tnsField, tnsLabel));
        urlFields.put(JdbcUrl.TOKEN_DSN, new UrlField(dsnField, dsnLabel));
        urlFields.put(JdbcUrl.TOKEN_FILE, new UrlField(fileField, fileLabel, fileBrowseButton));
        urlFields.put(JdbcUrl.TOKEN_SERVERNAME, new UrlField(serverNameField, serverNameLabel));
        urlFields.put(JdbcUrl.TOKEN_INSTANCE, new UrlField(instanceField, instanceLabel));
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public NewConnectionPanel(AddConnectionWizard wizard, ConnectionPanel panel, String driverClass, DatabaseConnection connection) {
        this.wd = wizard;
        this.connection = connection;
        this.wp = panel;
        initComponents();
        initAccessibility();
        initFieldMap();

        DatabaseExplorerInternalUIs.connect(templateComboBox, JDBCDriverManager.getDefault(), driverClass);

        ConnectionProgressListener progressListener = new ConnectionProgressListener() {

            @Override
            public void connectionStarted() {
                startProgress();
            }

            @Override
            public void connectionStep(String step) {
                setProgressMessage(step);
            }

            @Override
            public void connectionFinished() {
                stopProgress();
            }

            @Override
            public void connectionFailed() {
                stopProgress();
            }
        };
        wd.addConnectionProgressListener(progressListener);
        
        String driver = connection.getDriver();
        String driverName = connection.getDriverName();
        if (driver != null && driverName != null) {
            for (int i = 0; i < templateComboBox.getItemCount(); i++) {
                Object item = templateComboBox.getItemAt(i);
                if (item instanceof JdbcUrl) {
                    JdbcUrl url = ((JdbcUrl) item);
                    assert url.getDriver() != null;
                    if (url.getClassName().equals(driver) && url.getDriver().getName().equals(driverName)) {
                        templateComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        for (Entry<String, UrlField> entry : urlFields.entrySet()) {
            new InputAdapter(entry.getValue().getField());
            new FocusAdapter(entry.getKey(), entry.getValue().getField());
        }

        new InputAdapter(templateComboBox);
        new InputAdapter(userField);
        new InputAdapter(passwordField);
        new FocusAdapter(USERINPUT_FIELD, userField);

        urlField.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                updateFieldsFromUrl();
            }
        });
        urlField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (urlField.hasFocus()) {
                    updateFieldsFromUrl();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (urlField.hasFocus()) {
                    updateFieldsFromUrl();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (urlField.hasFocus()) {
                    updateFieldsFromUrl();
                }
            }
        });

        setUrlField();
        
        JdbcUrl url = getSelectedJdbcUrl();
        
        if(wd.getUser() != null) {
            userField.setText(wd.getUser());
        } else if (url.getSampleUser() != null) {
            userField.setText(url.getSampleUser());
        }
        if (wd.getPassword() != null) {
            passwordField.setText(wd.getPassword());
        } else if (url.getSamplePassword()!= null) {
            passwordField.setText(url.getSamplePassword());
        }
        if (wd.getDatabaseUrl() != null) {
            urlField.setText(wd.getDatabaseUrl());
        } else if (url.getSampleUrl() != null) {
            urlField.setText(url.getSampleUrl());
        }
        
        updateFieldsFromUrl();
        setUpFields();
        connectionProperties = connection.getConnectionProperties();

        DocumentListener docListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent evt) {
                fireChange();
            }

            @Override
            public void removeUpdate(DocumentEvent evt) {
                fireChange();
            }

            @Override
            public void changedUpdate(DocumentEvent evt) {
                fireChange();
            }
        };

        userField.getDocument().addDocumentListener(docListener);
        passwordField.getDocument().addDocumentListener(docListener);
        
    }

    public void setWindow(Window window) {
        this.window = window;
    }

    private void initAccessibility() {
        templateLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDriverNameA11yDesc")); //NOI18N
        templateComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDriverNameComboBoxA11yName")); //NOI18N
        userLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionUserNameA11yDesc")); //NOI18N
        userField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionUserNameTextFieldA11yName")); //NOI18N
        passwordLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionPasswordA11yDesc")); //NOI18N
        passwordField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionPasswordTextFieldA11yName")); //NOI18N
        hostLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionHostA11yDesc")); //NOI18N
        hostField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionHostTextFieldA11yName")); //NOI18N
        portLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionPortA11yDesc")); //NOI18N
        portField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionPortTextFieldA11yName")); //NOI18N
        serverNameField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionServerNameTextFieldA11yName")); //NOI18N
        serverNameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionServerNameA11yDesc")); //NOI18N
        databaseField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDatabaseNameTextFieldA11yName")); //NOI18N
        databaseLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDatabaseNameA11yDesc")); //NOI18N
        urlField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionJDBCURLTextFieldA11yName")); //NOI18N
        sidField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionSIDTextFieldA11yName")); //NOI18N
        sidLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionSIDA11yDesc")); //NOI18N
        serviceField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionServiceNameTextFieldA11yName")); //NOI18N
        serviceLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionServiceNameA11yDesc")); //NOI18N
        tnsField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionTNSNameTextFieldA11yName")); //NOI18N
        tnsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionTNSNameA11yDesc")); //NOI18N
        dsnField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDSNTextFieldA11yName")); //NOI18N
        dsnLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDSNA11yDesc")); //NOI18N
        fileField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionFileTextFieldA11yName")); //NOI18N
        fileLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionFileA11yDesc")); //NOI18N
        instanceField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionInstanceNameTextFieldA11yName")); //NOI18N
        instanceLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionInstanceNameA11yDesc")); //NOI18N
    }

    @Override
    public void initializeFocus() {
        setFocus();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        inputModeButtonGroup = new javax.swing.ButtonGroup();
        templateComboBox = new javax.swing.JComboBox();
        hostField = new javax.swing.JTextField();
        templateLabel = new javax.swing.JLabel();
        hostLabel = new javax.swing.JLabel();
        portLabel = new javax.swing.JLabel();
        portField = new javax.swing.JTextField();
        databaseLabel = new javax.swing.JLabel();
        databaseField = new javax.swing.JTextField();
        sidLabel = new javax.swing.JLabel();
        sidField = new javax.swing.JTextField();
        serviceLabel = new javax.swing.JLabel();
        serviceField = new javax.swing.JTextField();
        tnsLabel = new javax.swing.JLabel();
        tnsField = new javax.swing.JTextField();
        serverNameLabel = new javax.swing.JLabel();
        serverNameField = new javax.swing.JTextField();
        instanceLabel = new javax.swing.JLabel();
        instanceField = new javax.swing.JTextField();
        userLabel = new javax.swing.JLabel();
        userField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        dsnLabel = new javax.swing.JLabel();
        dsnField = new javax.swing.JTextField();
        fileLabel = new javax.swing.JLabel();
        fileField = new javax.swing.JTextField();
        fileBrowseButton = new javax.swing.JButton();
        urlField = new javax.swing.JTextField();
        passwordCheckBox = new javax.swing.JCheckBox();
        directUrlLabel = new javax.swing.JLabel();
        bTestConnection = new javax.swing.JButton();
        bConnectionProperties = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        templateComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDriverClassComboBoxA11yDesc")); // NOI18N
        templateComboBox.addItemListener(formListener);
        templateComboBox.addActionListener(formListener);

        hostField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionHostA11yDesc")); // NOI18N

        templateLabel.setLabelFor(templateComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(templateLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionDriverName")); // NOI18N

        hostLabel.setLabelFor(hostField);
        org.openide.awt.Mnemonics.setLocalizedText(hostLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionHost")); // NOI18N

        portLabel.setLabelFor(portField);
        org.openide.awt.Mnemonics.setLocalizedText(portLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionPort")); // NOI18N

        portField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionPortA11yDesc")); // NOI18N

        databaseLabel.setLabelFor(databaseField);
        org.openide.awt.Mnemonics.setLocalizedText(databaseLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionDatabase")); // NOI18N

        databaseField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDatabaseNameA11yDesc")); // NOI18N

        sidLabel.setLabelFor(sidField);
        org.openide.awt.Mnemonics.setLocalizedText(sidLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionSID")); // NOI18N

        sidField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionSIDA11yDesc")); // NOI18N

        serviceLabel.setLabelFor(serviceField);
        org.openide.awt.Mnemonics.setLocalizedText(serviceLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionServiceName")); // NOI18N

        serviceField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionServiceNameA11yDesc")); // NOI18N

        tnsLabel.setLabelFor(tnsField);
        org.openide.awt.Mnemonics.setLocalizedText(tnsLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionTNSName")); // NOI18N

        tnsField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionTNSNameA11yDesc")); // NOI18N

        serverNameLabel.setLabelFor(serverNameField);
        org.openide.awt.Mnemonics.setLocalizedText(serverNameLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionServerName")); // NOI18N

        serverNameField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionServerNameA11yDesc")); // NOI18N

        instanceLabel.setLabelFor(instanceField);
        org.openide.awt.Mnemonics.setLocalizedText(instanceLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionInstanceName")); // NOI18N

        instanceField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionInstanceNameA11yDesc")); // NOI18N

        userLabel.setLabelFor(userField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionUserName")); // NOI18N

        userField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionUserNameA11yDesc")); // NOI18N

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionPassword")); // NOI18N

        passwordField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionPasswordA11yDesc")); // NOI18N

        dsnLabel.setLabelFor(dsnField);
        org.openide.awt.Mnemonics.setLocalizedText(dsnLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionDSN")); // NOI18N

        dsnField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionDSNA11yDesc")); // NOI18N

        fileLabel.setLabelFor(fileField);
        org.openide.awt.Mnemonics.setLocalizedText(fileLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionFile")); // NOI18N

        fileField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionFileA11yDesc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(fileBrowseButton, "&Browse...");
        fileBrowseButton.addActionListener(formListener);

        urlField.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionJDBCURLA11yDesc")); // NOI18N
        urlField.addActionListener(formListener);
        urlField.addFocusListener(formListener);
        urlField.addKeyListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(passwordCheckBox, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionRememberPassword")); // NOI18N
        passwordCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACS_NewConnectionRememberPasswordA11yDesc")); // NOI18N
        passwordCheckBox.setMargin(new java.awt.Insets(3, 0, 1, 1));

        directUrlLabel.setLabelFor(urlField);
        org.openide.awt.Mnemonics.setLocalizedText(directUrlLabel, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionDirectURL")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bTestConnection, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionPanel.bTestConnection")); // NOI18N
        bTestConnection.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(bConnectionProperties, org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionPanel.bConnectionProperties")); // NOI18N
        bConnectionProperties.addActionListener(formListener);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(directUrlLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hostLabel)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(instanceLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(serverNameLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(dsnLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(fileLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(tnsLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(serviceLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(sidLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(databaseLabel, javax.swing.GroupLayout.Alignment.LEADING))
                            .addComponent(templateLabel)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(passwordLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(userLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(bConnectionProperties)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bTestConnection)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(userField, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(sidField)
                            .addComponent(serviceField)
                            .addComponent(tnsField)
                            .addComponent(dsnField)
                            .addComponent(serverNameField)
                            .addComponent(instanceField)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(hostField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(portLabel)
                                .addGap(2, 2, 2)
                                .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(databaseField, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(passwordField)
                            .addComponent(urlField, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(templateComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(passwordCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(fileField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fileBrowseButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(templateLabel)
                    .addComponent(templateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hostLabel)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(hostField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(portLabel))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(databaseLabel)
                    .addComponent(databaseField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sidLabel)
                    .addComponent(sidField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serviceLabel)
                    .addComponent(serviceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tnsLabel)
                    .addComponent(tnsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dsnLabel)
                    .addComponent(dsnField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileLabel)
                    .addComponent(fileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverNameLabel)
                    .addComponent(serverNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(instanceLabel)
                    .addComponent(instanceField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userLabel)
                    .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(passwordCheckBox)
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bTestConnection)
                    .addComponent(bConnectionProperties))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(urlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(directUrlLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bTestConnection.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionWizard.bTestConnection.ACD")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewConnectionPanel.class, "ACD_NewConnectionPanel")); // NOI18N
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.FocusListener, java.awt.event.ItemListener, java.awt.event.KeyListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == templateComboBox) {
                NewConnectionPanel.this.templateComboBoxActionPerformed(evt);
            }
            else if (evt.getSource() == urlField) {
                NewConnectionPanel.this.urlFieldActionPerformed(evt);
            }
            else if (evt.getSource() == bTestConnection) {
                NewConnectionPanel.this.bTestConnectionActionPerformed(evt);
            }
            else if (evt.getSource() == bConnectionProperties) {
                NewConnectionPanel.this.bConnectionPropertiesActionPerformed(evt);
            }
            else if (evt.getSource() == fileBrowseButton) {
                NewConnectionPanel.this.fileBrowseButtonActionPerformed(evt);
            }
        }

        public void focusGained(java.awt.event.FocusEvent evt) {
        }

        public void focusLost(java.awt.event.FocusEvent evt) {
            if (evt.getSource() == urlField) {
                NewConnectionPanel.this.urlFieldFocusLost(evt);
            }
        }

        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            if (evt.getSource() == templateComboBox) {
                NewConnectionPanel.this.templateComboBoxItemStateChanged(evt);
            }
        }

        public void keyPressed(java.awt.event.KeyEvent evt) {
            if (evt.getSource() == urlField) {
                NewConnectionPanel.this.urlFieldKeyPressed(evt);
            }
        }

        public void keyReleased(java.awt.event.KeyEvent evt) {
        }

        public void keyTyped(java.awt.event.KeyEvent evt) {
        }
    }// </editor-fold>//GEN-END:initComponents

    private void urlFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_urlFieldActionPerformed
    }//GEN-LAST:event_urlFieldActionPerformed

    private void urlFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_urlFieldFocusLost
    }//GEN-LAST:event_urlFieldFocusLost

    private void urlFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_urlFieldKeyPressed
    }//GEN-LAST:event_urlFieldKeyPressed

    private void templateComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_templateComboBoxActionPerformed
    }//GEN-LAST:event_templateComboBoxActionPerformed

    private void templateComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_templateComboBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            Object item = evt.getItem();
            if (item != null && !(item instanceof JdbcUrl)) {
                // This is an item indicating "Create a New Driver", and if
                // we futz with the fields, then the ComboBox wants to make the
                // drop-down invisible and the dialog never gets a chance to
                // get invoked.
                return;
            }

            JdbcUrl jdbcurl = (JdbcUrl) item;
            
            // Field entry mode doesn't make sense if this URL isn't parsed.  change the mode
            // now if appropriate
            if (!jdbcurl.isParseUrl()) {
                updateInputMode(false);
            } else {
                setUpFields();
            }

            if (wd.getDatabaseUrl() == null && jdbcurl.getSampleUrl() != null) {
                // Show the appropriate sample URL if the user switches the JDBC URL type.
                urlField.setText(jdbcurl.getSampleUrl());
                updateFieldsFromUrl();
            } else {
                updateUrlFromFields();
            }
            fireChange();
        }
    }//GEN-LAST:event_templateComboBoxItemStateChanged

    private void bTestConnectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bTestConnectionActionPerformed
        tryConnection();
    }//GEN-LAST:event_bTestConnectionActionPerformed

    private void bConnectionPropertiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bConnectionPropertiesActionPerformed
        PropertyEditorPanel pep = new PropertyEditorPanel(connectionProperties, true);
        DialogDescriptor dd = new DialogDescriptor(
                pep,
                NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionPanel.dlgConnectionProperties"),
                true,
                null);
        Object result = DialogDisplayer.getDefault().notify(dd);
        if(result == NotifyDescriptor.OK_OPTION) {
            connectionProperties = pep.getValue();
        }
    }//GEN-LAST:event_bConnectionPropertiesActionPerformed

  private void fileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileBrowseButtonActionPerformed
        FileChooserBuilder fileChooserBuilder = new FileChooserBuilder(NewConnectionPanel.class);
        fileChooserBuilder.setTitle(NbBundle.getMessage(AddDriverDialog.class, "NewConnectionFile_Chooser_Title")); //NOI18N
        fileChooserBuilder.setFilesOnly(true);
        File existingFile = new File(fileField.getText());
        if (existingFile.exists() && existingFile.isDirectory()) {
            fileChooserBuilder.setDefaultWorkingDirectory(existingFile);
        } else {
            File parentFile = existingFile.getParentFile();
            if (parentFile != null && parentFile.exists()) {
                fileChooserBuilder.setDefaultWorkingDirectory(existingFile.getParentFile());
            }
        }
        File file = fileChooserBuilder.showOpenDialog();
        if (file != null) {
            JdbcUrl url = getSelectedJdbcUrl();
            JdbcUrl.DatabaseFileValidator validator = (url == null) ? null : url.getDatabaseFileValidator();
            String validationErrorMessage = null;
            if (validator != null) {
                try {
                    validationErrorMessage = validator.getValidationErrorMessage(file);
                } catch (IOException e) {
                    LOGGER.log(Level.INFO, "Problem while attempting to validate database file", e);
                }
            }
            if (validationErrorMessage != null) {
                String validationErrorMessageFinal = validationErrorMessage;
                /* Use invokeLater to give the file browser dialog time to disappear before the new
                dialog is shown. Otherwise DialogDisplayer can make it appear behind the
                New Connection dialog. */
                SwingUtilities.invokeLater(() -> {
                    NotifyDescriptor msgDesc =
                            new NotifyDescriptor.Message(validationErrorMessageFinal, JOptionPane.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(msgDesc);
                });
                /* Still allow the file to be selected. Maybe there's a newer JDBC driver that
                supports other formats etc. */
            }
            fileField.setText(file.getAbsolutePath());
        }
  }//GEN-LAST:event_fileBrowseButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bConnectionProperties;
    private javax.swing.JButton bTestConnection;
    private javax.swing.JTextField databaseField;
    private javax.swing.JLabel databaseLabel;
    private javax.swing.JLabel directUrlLabel;
    private javax.swing.JTextField dsnField;
    private javax.swing.JLabel dsnLabel;
    private javax.swing.JButton fileBrowseButton;
    private javax.swing.JTextField fileField;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JTextField hostField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.ButtonGroup inputModeButtonGroup;
    private javax.swing.JTextField instanceField;
    private javax.swing.JLabel instanceLabel;
    private javax.swing.JCheckBox passwordCheckBox;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField portField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField serverNameField;
    private javax.swing.JLabel serverNameLabel;
    private javax.swing.JTextField serviceField;
    private javax.swing.JLabel serviceLabel;
    private javax.swing.JTextField sidField;
    private javax.swing.JLabel sidLabel;
    private javax.swing.JComboBox templateComboBox;
    private javax.swing.JLabel templateLabel;
    private javax.swing.JTextField tnsField;
    private javax.swing.JLabel tnsLabel;
    private javax.swing.JTextField urlField;
    private javax.swing.JTextField userField;
    private javax.swing.JLabel userLabel;
    // End of variables declaration//GEN-END:variables

    public void setConnectionInfo() {
        JdbcUrl url = getSelectedJdbcUrl();
        if (url != null) {
            JDBCDriver driver = url.getDriver();
            assert (driver != null);
            connection.setDriverName(driver.getName());
            connection.setDriver(driver.getClassName());
        }

        connection.setDatabase(urlField.getText());
        connection.setUser(userField.getText());
        connection.setPassword(getPassword());
        connection.setRememberPassword(passwordCheckBox.isSelected());
        connection.setConnectionProperties(connectionProperties);
    }

    private void resize() {
        revalidate();
        if (window != null) {
            window.pack();
        }
    }

    private void updateInputMode(boolean copyUrl) {
        setUpFields();
    }

    /**
     * Set up which fields are enabled based on the URL template for the
     * selected driver
     */
    private void setUpFields() {

        Object item = templateComboBox.getSelectedItem();
        if (item != null && !(item instanceof JdbcUrl)) {
            // This is an item indicating "Create a New Driver", and if
            // we futz with the fields, then the ComboBox wants to make the
            // drop-down invisible and the dialog never gets a chance to
            // get invoked.
            return;
        }

        JdbcUrl jdbcurl = (JdbcUrl) item;

        if (jdbcurl == null) {
            for (UrlField uf : urlFields.values()) {
                for (JComponent c : uf.getComponents()) {
                    c.setVisible(false);
                }
            }

            checkValid();
            resize();
            return;
        }

        boolean showUsernamePassword = jdbcurl.isUsernamePasswordDisplayed();
        userField.setVisible(showUsernamePassword);
        userLabel.setVisible(showUsernamePassword);
        passwordField.setVisible(showUsernamePassword);
        passwordLabel.setVisible(showUsernamePassword);
        passwordCheckBox.setVisible(showUsernamePassword);
        if (!showUsernamePassword) {
            userField.setText("");
            passwordField.setText("");
            passwordCheckBox.setSelected(false);
        }

        directUrlLabel.setVisible(true);

        for (Entry<String, UrlField> entry : urlFields.entrySet()) {
            for (JComponent c : entry.getValue().getComponents()) {
                c.setVisible(jdbcurl.supportsToken(entry.getKey()));
            }
        }

        if (!jdbcurl.isParseUrl()) {
            setUrlField();
        }

        setFocus();
        checkValid();
        resize();
    }

    private void setFocus() {
        if (templateComboBox.getItemCount() <= 1) { // the first item is "Add Driver...""
            templateComboBox.requestFocusInWindow();
            return;
        }

        for (Entry<String, UrlField> entry : urlFields.entrySet()) {
            if (entry.getValue().getField().isVisible()) {
                entry.getValue().getField().requestFocusInWindow();
                return;
            }
        }

        userField.requestFocusInWindow();
    }

    private JdbcUrl getSelectedJdbcUrl() {
        Object item = templateComboBox.getSelectedItem();
        if (!(item instanceof JdbcUrl)) {
            return null;
        }

        return (JdbcUrl) item;
    }

    private void setUrlField() {
        if (!connection.getDatabase().isEmpty()) {
            urlField.setText(connection.getDatabase());
            return;
        }

        JdbcUrl jdbcurl = getSelectedJdbcUrl();
        if (jdbcurl == null) {
            urlField.setText("");
            return;
        }

        if (jdbcurl.isParseUrl()) {
            updateUrlFromFields();
        } else {
            urlField.setText(jdbcurl.getUrlTemplate());
        }

    }

    private String getPassword() {
        String password;
        String tempPassword = new String(passwordField.getPassword());
        if (tempPassword.length() > 0) {
            password = tempPassword;
        } else {
            password = null;
        }

        return password;
    }

    public String getTitle() {
        return NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionDialogTitle"); //NOI18N
    }

    private void startProgress() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(NewConnectionPanel.class, "ConnectionProgress_Connecting"));
                progressHandle.start();
                enableInput(false);
            }
        });
    }

    private void setProgressMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (progressHandle != null) {
                    progressHandle.setDisplayName(message);
                }
            }
        });
    }

    /**
     * Terminates the use of the progress bar.
     */
    public void terminateProgress() {
        stopProgress();
    }

    private void stopProgress() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (progressHandle != null) {
                    progressHandle.finish();
                    enableInput(true);
                }
            }
        });
    }

    private void enableInput(boolean enable) {
        templateComboBox.setEnabled(enable);
        userField.setEnabled(enable);
        passwordField.setEnabled(enable);
        passwordCheckBox.setEnabled(enable);
        urlField.setEnabled(enable);
        bTestConnection.setEnabled(enable);
        bConnectionProperties.setEnabled(enable);

        for (Entry<String, UrlField> entry : urlFields.entrySet()) {
            entry.getValue().getField().setEnabled(enable);
        }
    }

    private void resetProgress() {
        if (progressHandle != null) {
            progressHandle.setDisplayName(""); // NOI18N
        }
    }

    private void fireChange() {

        // the user has changed some parameter, so if there's a connection it's
        // no longer in sync with the field data
        wd.closeConnection();

        firePropertyChange("argumentChanged", null, null);
        resetProgress();
        wp.fireChangeEvent();
    }

    private void updateUrlFromFields() {

        JdbcUrl url = getSelectedJdbcUrl();
        if (url == null || !url.isParseUrl()) {
            return;
        }

        // If the fields are being modified because the user is manually
        // changing the URL, don't circle back and update the URL again.
        if (!updatingUrl) {
            updatingFields = true;

            for (Entry<String, UrlField> entry : urlFields.entrySet()) {
                url.put(entry.getKey(), entry.getValue().getField().getText());
            }

            urlField.setText(url.getUrl());

            updatingFields = false;
        }

        checkValid();
    }

    void checkValid() {
        JdbcUrl url = getSelectedJdbcUrl();

        boolean requiredFieldMissing = false;
        if (url == null) {
            displayMessage(NbBundle.getMessage(NewConnectionPanel.class, "NewConnection.MSG_SelectADriver"), false);
        } else if (url.isParseUrl()) {
            for (Entry<String, UrlField> entry : urlFields.entrySet()) {
                if (url.requiresToken(entry.getKey()) && isEmpty(entry.getValue().getField().getText())) {
                    requiredFieldMissing = true;
                    String fieldName = entry.getValue().getLabel().getText();
                    /* Drop the colon, since this message goes at the bottom of the wizard dialog (e.g. avoid
                    the message looking like "Please specify a value for TNS Name:"). */
                    if (fieldName.endsWith(":")) {
                        fieldName = fieldName.substring(0, fieldName.length() - 1);
                    }
                    displayMessage(NbBundle.getMessage(NewConnectionPanel.class, "NewConnection.ERR_FieldRequired",
                            fieldName), false);
                }
            }

            if (!requiredFieldMissing) {
                clearError();
            }
        } else if (isEmpty(urlField.getText())) {
            displayMessage(NbBundle.getMessage(NewConnectionPanel.class, "NewConnection.MSG_SpecifyURL"), false);
        } else {
            clearError();
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private void updateFieldsFromUrl() {
        JdbcUrl url = getSelectedJdbcUrl();
        if (url == null) {
            return;
        }

        // If this is called because the URL is being changed due to
        // changes in the fields, then don't circle back and update
        // the fields again.
        if (updatingFields) {
            return;
        }

        try {
            url.setUrl(urlField.getText());
            clearError();
        } catch (MalformedURLException e) {
            LOGGER.log(Level.FINE, null, e);
            // just log in but don't report it to users
        }

        if (url.isParseUrl()) {
            // Setting this flag prevents the docment listener for the fields
            // from trying to update the URL, thus causing a circular even loop.
            updatingUrl = true;

            for (Entry<String, UrlField> entry : urlFields.entrySet()) {
                entry.getValue().getField().setText(url.get(entry.getKey()));
            }

            updatingUrl = false;
        }
    }

    private void clearError() {
        wd.getNotificationLineSupport().clearMessages();
        wd.setValid(true);
        wp.fireChangeEvent();
    }

    private void displayMessage(String message, boolean isError) {
        wd.setValid(!isError);
        wp.fireChangeEvent();
        if (isError) {
            wd.getNotificationLineSupport().setErrorMessage(message);
        } else {
            wd.getNotificationLineSupport().setInformationMessage(message);
        }
    }
    private RequestProcessor RP = new RequestProcessor(NewConnectionPanel.class.getName(), 1);

    private void tryConnection() {
        setWaitingState(true);
        RP.post(new Runnable() {

            @Override
            public void run() {
                testConnection();
            }
        });
    }

    public void setWaitingState(boolean wait) {
        Component rootPane = getRootPane();
        enableInput(! wait);
        if (rootPane != null) {
            rootPane.setCursor(wait ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : null);
        }
    }

    private void testConnection() {
        try {
            setConnectionInfo();
            wp.validate();
            displayMessage(NbBundle.getMessage(NewConnectionPanel.class, "NewConnectionPanel.ConnectionPassed"), false); // NOI18N
        } catch (WizardValidationException ex) {
            displayMessage(ex.getLocalizedMessage(), true);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setWaitingState(false);
            }
        });
    }

    boolean valid() {
        setConnectionInfo();
        return wd.getValid();
    }

    private class UrlField {

        private final JTextField field;
        private final JLabel label;
        private final List<JComponent> components;

        public UrlField(JTextField field, JLabel label, JComponent ... otherComponents) {
            this.field = field;
            this.label = label;
            List<JComponent> toComponents = new ArrayList<>();
            toComponents.add(field);
            toComponents.add(label);
            toComponents.addAll(Arrays.asList(otherComponents));
            components = Collections.unmodifiableList(toComponents);
        }

        public JTextField getField() {
            return field;
        }

        public JLabel getLabel() {
            return label;
        }

        public List<JComponent> getComponents() {
            return components;
        }
    }

    /**
     * Class handles focus lost event and trims field contents and ensures port
     * to be numeric.
     */
    private class FocusAdapter implements FocusListener {
        private final String targetToken;

        public FocusAdapter(String targetToken, JTextField textField) {
            this.targetToken = targetToken;
            textField.addFocusListener(this);
        }
        
        @Override
        public void focusGained(FocusEvent e) {}

        @Override
        public void focusLost(FocusEvent e) {
            Object source = e.getSource();
            if (source instanceof JTextField) {
                JTextField textField = (JTextField) source;
                String inputText = textField.getText();
                switch(targetToken) {
                    case JdbcUrl.TOKEN_HOST:
                    case JdbcUrl.TOKEN_DB:
                    case JdbcUrl.TOKEN_SID:
                    case JdbcUrl.TOKEN_SERVICENAME:
                    case JdbcUrl.TOKEN_TNSNAME:
                    case JdbcUrl.TOKEN_DSN:
                    case JdbcUrl.TOKEN_FILE:
                    case JdbcUrl.TOKEN_SERVERNAME:
                    case JdbcUrl.TOKEN_INSTANCE:
                    case USERINPUT_FIELD:
                        textField.setText(inputText.trim());
                        break;
                    case JdbcUrl.TOKEN_PORT:
                        Integer port = null;
                        try {
                            port = Integer.valueOf(inputText.trim());
                        } catch (NumberFormatException ex) {}
                        if(port != null) {
                            textField.setText(Integer.toString(port));
                        } else {
                            Matcher numberMatcher = numbers.matcher(inputText);
                            if(numberMatcher.find()) {
                                textField.setText(numberMatcher.group(1));
                            } else {
                                textField.setText("");
                            }
                        }
                        break;
                    default:
                        // Unhandled fields are left untouched
                        break;
                }
            }
        }
    }
    
    /**
     * This class is used to track user input for an associated input field.
     */
    private class InputAdapter implements DocumentListener, ListDataListener {

        @SuppressWarnings("LeakingThisInConstructor")
        public InputAdapter(JTextField source) {
            source.getDocument().addDocumentListener(this);
        }

        @SuppressWarnings("LeakingThisInConstructor")
        public InputAdapter(JComboBox source) {
            source.getModel().addListDataListener(this);
        }

        @SuppressWarnings("LeakingThisInConstructor")
        public InputAdapter(JTextArea source) {
            source.getDocument().addDocumentListener(this);
        }

        @Override
        public void insertUpdate(DocumentEvent evt) {
            updateUrlFromFields();
            checkValid();
            fireChange();
        }

        @Override
        public void removeUpdate(DocumentEvent evt) {
            updateUrlFromFields();
            checkValid();
            fireChange();
        }

        @Override
        public void changedUpdate(DocumentEvent evt) {
            updateUrlFromFields();
            checkValid();
            fireChange();
        }

        @Override
        public void intervalAdded(ListDataEvent evt) {
            fireChange();
        }

        @Override
        public void intervalRemoved(ListDataEvent evt) {
            fireChange();
        }

        @Override
        public void contentsChanged(ListDataEvent evt) {
            updateUrlFromFields();
            checkValid();
            fireChange();
        }
    }
}
