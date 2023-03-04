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

package org.netbeans.modules.php.project.connections.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public final class NewRemoteConnectionPanel extends JPanel {

    private static final long serialVersionUID = 2806958431387531044L;

    private static final Logger LOGGER = Logger.getLogger(NewRemoteConnectionPanel.class.getName());

    private final ConfigManager configManager;
    private DialogDescriptor descriptor;
    private NotificationLineSupport notificationLineSupport;

    public NewRemoteConnectionPanel(ConfigManager configManager) {
        this.configManager = configManager;
        initComponents();

        connectionTypeComboBox.setModel(new DefaultComboBoxModel<>(new Vector<>(RemoteConnections.get().getRemoteConnectionTypes())));

        registerListeners();
    }

    public boolean open() {
        descriptor = new DialogDescriptor(
                this,
                NbBundle.getMessage(NewRemoteConnectionPanel.class, "LBL_CreateNewConnection"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null);
        descriptor.setValid(false);
        notificationLineSupport = descriptor.createNotificationLineSupport();
        notificationLineSupport.setInformationMessage(NbBundle.getMessage(NewRemoteConnectionPanel.class, "TXT_ProvideConnectionName"));

        return DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.OK_OPTION;
    }

    public String getConnectionName() {
        return connectionNameTextField.getText().trim();
    }

    public String getOldConfigName() {
        // Backwards compatibility with new getConfigName - #190930
        return getConnectionName().replaceAll("[^a-zA-Z0-9_.-]", "_"); // NOI18N
    }

    public String getConfigName() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5"); // NOI18N
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            // fallback
            return getOldConfigName();
        }

        md.update(getConnectionName().getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        BigInteger hash = new BigInteger(1, digest);
        String hashWord = hash.toString(16);
        String postfix = hashWord.substring(hashWord.length() - 6);

        return getOldConfigName() + "-" + postfix; // NOI18N
    }

    public String getConnectionType() {
        return (String) connectionTypeComboBox.getSelectedItem();
    }

    void validateFields() {
        String name = getConnectionName();
        String config = getConfigName();
        String oldConfig = getOldConfigName();
        String type = getConnectionType();

        String err = null;
        if (name.length() == 0) {
            err = NbBundle.getMessage(NewRemoteConnectionPanel.class, "MSG_EmptyConnectionName");
        } else if (name.length() > Preferences.MAX_NAME_LENGTH) {
            err = NbBundle.getMessage(NewRemoteConnectionPanel.class, "MSG_LongConnectionName", Preferences.MAX_NAME_LENGTH);
        } else if (type.length() == 0) {
            err = NbBundle.getMessage(NewRemoteConnectionPanel.class, "MSG_EmptyConnectionType");
        } else if (configManager.exists(config) || configManager.exists(oldConfig)) {
            err = NbBundle.getMessage(NewRemoteConnectionPanel.class, "MSG_ConnectionExists", name);
        }
        setError(err);
    }

    private void registerListeners() {
        connectionNameTextField.getDocument().addDocumentListener(new DocumentListener() {
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
                validateFields();
            }
        });
        connectionTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                validateFields();
            }
        });
    }

    private void setError(String msg) {
        assert descriptor != null;
        assert notificationLineSupport != null;

        if (StringUtils.hasText(msg)) {
            notificationLineSupport.setErrorMessage(msg);
            descriptor.setValid(false);
        } else {
            notificationLineSupport.clearMessages();
            descriptor.setValid(true);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connectionNameLabel = new JLabel();
        connectionNameTextField = new JTextField();
        connectionTypeLabel = new JLabel();
        connectionTypeComboBox = new JComboBox<String>();

        connectionNameLabel.setLabelFor(connectionNameTextField);
        Mnemonics.setLocalizedText(connectionNameLabel, NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionNameLabel.text")); // NOI18N

        connectionNameTextField.setText(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionNameTextField.text")); // NOI18N

        connectionTypeLabel.setLabelFor(connectionTypeComboBox);
        Mnemonics.setLocalizedText(connectionTypeLabel, NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionTypeLabel.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(connectionNameLabel)
                    .addComponent(connectionTypeLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(connectionTypeComboBox, 0, 221, Short.MAX_VALUE)
                    .addComponent(connectionNameTextField, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(connectionNameLabel)
                    .addComponent(connectionNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(connectionTypeLabel)
                    .addComponent(connectionTypeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        connectionNameLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionNameLabel.AccessibleContext.accessibleName")); // NOI18N
        connectionNameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionNameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        connectionNameTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionNameTextField.AccessibleContext.accessibleName")); // NOI18N
        connectionNameTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionNameTextField.AccessibleContext.accessibleDescription")); // NOI18N
        connectionTypeLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionTypeLabel.AccessibleContext.accessibleName")); // NOI18N
        connectionTypeLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionTypeLabel.AccessibleContext.accessibleDescription")); // NOI18N
        connectionTypeComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionTypeComboBox.AccessibleContext.accessibleName")); // NOI18N
        connectionTypeComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.connectionTypeComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewRemoteConnectionPanel.class, "NewRemoteConnectionPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel connectionNameLabel;
    private JTextField connectionNameTextField;
    private JComboBox<String> connectionTypeComboBox;
    private JLabel connectionTypeLabel;
    // End of variables declaration//GEN-END:variables

}
