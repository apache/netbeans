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

package org.netbeans.modules.j2ee.weblogic9.ui.wizard;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * The second panel of the custom wizard used for registering an instance of
 * the server. Here user should choose among the the existing local instances,
 * or enter the host/port/username/password conbination for a remote one
 *
 * @author Petr Hejl
 */
public class ServerRemotePropertiesVisual extends javax.swing.JPanel {

    private transient WLInstantiatingIterator instantiatingIterator;

    private final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();

    /**
     * Creates a new instance of the ServerPropertiesVisual. It initializes all
     * the GUI components that appear on the panel.
     *
     * @param steps the names of the steps in the wizard
     * @param index index of this panel in the wizard
     * @param listener a listener that will propagate the chage event higher in
     *      the hierarchy
     * @param instantiatingIterator the parent instantiating iterator
     */
    public ServerRemotePropertiesVisual(WLInstantiatingIterator instantiatingIterator) {
        // save the instantiating iterator
        this.instantiatingIterator = instantiatingIterator;

        // set the panel's name
        setName(NbBundle.getMessage(
                ServerRemotePropertiesVisual.class, "SERVER_PROPERTIES_STEP") );  // NOI18N

        initComponents();
        
        hostNameTextField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                fireChangeEvent();
            }
        });
        adminPortTextField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                fireChangeEvent();
            }
        });
        debugModeCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                fireChangeEvent();
            }
        });
        debugPortTextField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                fireChangeEvent();
            }
        });
        usernameField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                fireChangeEvent();
            }
        });
        passwordField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                fireChangeEvent();
            }
        });
    }
    
    public boolean valid(WizardDescriptor wizardDescriptor) {
        // clear the error message
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);

        String host = hostNameTextField.getText();
        if (host != null) {
            host = host.trim();
        }

        if (host == null || host.isEmpty()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerRemotePropertiesVisual.class, "ERR_EMPTY_HOST"))); // NOI18N
            return false;
        }

        String strPort = adminPortTextField.getText();
        int port;
        try {
            port = Integer.parseInt(strPort);
        } catch (NumberFormatException ex) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerRemotePropertiesVisual.class, "ERR_NON_NUMERIC_PORT"))); // NOI18N
            return false;
        }

        String debugPort = debugPortTextField.getText();
        if (debugModeCheckBox.isSelected()) {
            try {
                Integer.parseInt(debugPort);
            } catch (NumberFormatException ex) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerRemotePropertiesVisual.class, "ERR_NON_NUMERIC_PORT"))); // NOI18N
                return false;
            }
        }

        if (InstanceProperties.getInstanceProperties(getUrl(host, port)) != null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerRemotePropertiesVisual.class, "ERR_ALREADY_REGISTERED"))); // NOI18N
            return false;
        } else if (passwordField.getPassword().length <= 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerRemotePropertiesVisual.class, "ERR_EMPTY_PASSWORD"))); // NOI18N
        } else {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerRemotePropertiesVisual.class, "MSG_RegisterRemote"))); // NOI18N
        }

        // save the data to the parent instantiating iterator
        instantiatingIterator.setUrl(getUrl(host, port));
        instantiatingIterator.setDomainRoot(null);
        instantiatingIterator.setUsername(usernameField.getText());
        instantiatingIterator.setPassword(new String(passwordField.getPassword()));
        instantiatingIterator.setPort(Integer.toString(port));
        instantiatingIterator.setDebugPort(debugPort);
        instantiatingIterator.setDomainName(null);
        instantiatingIterator.setHost(host);
        instantiatingIterator.setRemote(true);
        instantiatingIterator.setRemoteDebug(debugModeCheckBox.isSelected());
        instantiatingIterator.setSsl(sslCheckBox.isSelected());
        return true; 
    }
    
    private String getUrl(String host, int port) {
        return WLDeploymentFactory.getUrl(host, port, instantiatingIterator.getServerRoot(), null);
    }

    /**
     * Adds a listener
     *
     * @param listener the listener to be added
     */
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a registered listener
     *
     * @param listener the listener to be removed
     */
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    private void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        usernameLabel = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        hostNameLabel = new javax.swing.JLabel();
        hostNameTextField = new javax.swing.JTextField();
        adminPortLabel = new javax.swing.JLabel();
        adminPortTextField = new javax.swing.JTextField();
        debugPortLabel = new javax.swing.JLabel();
        debugPortTextField = new javax.swing.JTextField();
        debugModeCheckBox = new javax.swing.JCheckBox();
        sslCheckBox = new javax.swing.JCheckBox();

        usernameLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        usernameLabel.setLabelFor(usernameField);
        org.openide.awt.Mnemonics.setLocalizedText(usernameLabel, org.openide.util.NbBundle.getMessage(ServerRemotePropertiesVisual.class, "LBL_USERNAME")); // NOI18N

        usernameField.setColumns(15);
        usernameField.setText("weblogic"); // NOI18N

        passwordLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(ServerRemotePropertiesVisual.class, "LBL_PASSWORD")); // NOI18N

        passwordField.setColumns(15);

        hostNameLabel.setLabelFor(hostNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(hostNameLabel, org.openide.util.NbBundle.getMessage(ServerRemotePropertiesVisual.class, "ServerRemotePropertiesVisual.hostNameLabel.text")); // NOI18N

        adminPortLabel.setLabelFor(adminPortTextField);
        org.openide.awt.Mnemonics.setLocalizedText(adminPortLabel, org.openide.util.NbBundle.getMessage(ServerRemotePropertiesVisual.class, "ServerRemotePropertiesVisual.adminPortLabel.text")); // NOI18N

        adminPortTextField.setColumns(5);
        adminPortTextField.setText("7001");

        debugPortLabel.setLabelFor(debugPortTextField);
        org.openide.awt.Mnemonics.setLocalizedText(debugPortLabel, org.openide.util.NbBundle.getMessage(ServerRemotePropertiesVisual.class, "ServerRemotePropertiesVisual.debugPortLabel.text")); // NOI18N

        debugPortTextField.setColumns(5);
        debugPortTextField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(debugModeCheckBox, org.openide.util.NbBundle.getMessage(ServerRemotePropertiesVisual.class, "ServerRemotePropertiesVisual.debugModeCheckBox.text")); // NOI18N
        debugModeCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                debugModeCheckBoxItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(sslCheckBox, org.openide.util.NbBundle.getMessage(ServerRemotePropertiesVisual.class, "ServerRemotePropertiesVisual.sslCheckBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hostNameLabel)
                    .addComponent(adminPortLabel))
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(adminPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sslCheckBox)
                        .addContainerGap())
                    .addComponent(hostNameTextField)))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(passwordLabel)
                            .addComponent(usernameLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(debugModeCheckBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(debugPortLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(debugPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(204, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostNameLabel)
                    .addComponent(hostNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(adminPortLabel)
                    .addComponent(adminPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sslCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(debugModeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(debugPortLabel)
                    .addComponent(debugPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameLabel)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        usernameField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerRemotePropertiesVisual.class, "ACSD_ServerPropertiesPanel_usernameField")); // NOI18N
        passwordField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerRemotePropertiesVisual.class, "ACSD_ServerPropertiesPanel_passwordField")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void debugModeCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_debugModeCheckBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            debugPortTextField.setEnabled(true);
        } else if (evt.getStateChange() == ItemEvent.DESELECTED) {
            debugPortTextField.setEnabled(false);
        }
    }//GEN-LAST:event_debugModeCheckBoxItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel adminPortLabel;
    private javax.swing.JTextField adminPortTextField;
    private javax.swing.JCheckBox debugModeCheckBox;
    private javax.swing.JLabel debugPortLabel;
    private javax.swing.JTextField debugPortTextField;
    private javax.swing.JLabel hostNameLabel;
    private javax.swing.JTextField hostNameTextField;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JCheckBox sslCheckBox;
    private javax.swing.JTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
}
