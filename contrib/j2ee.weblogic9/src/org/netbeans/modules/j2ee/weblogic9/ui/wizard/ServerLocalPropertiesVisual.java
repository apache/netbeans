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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.weblogic9.VersionBridge;
import org.netbeans.modules.j2ee.weblogic9.WLDeploymentFactory;
import org.netbeans.modules.j2ee.weblogic9.WLPluginProperties;
import org.netbeans.modules.j2ee.weblogic9.deploy.WLJpa2SwitchSupport;
import org.netbeans.modules.weblogic.common.api.DomainConfiguration;
import org.netbeans.modules.weblogic.common.api.WebLogicLayout;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * The second panel of the custom wizard used for registering an instance of
 * the server. Here user should choose among the the existing local instances,
 * or enter the host/port/username/password conbination for a remote one
 *
 * @author Petr Hejl
 */
public class ServerLocalPropertiesVisual extends javax.swing.JPanel {

    private transient WLInstantiatingIterator instantiatingIterator;

    private final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();

    private final List<Instance> instances = new ArrayList<Instance>();
    
    private transient WLJpa2SwitchSupport support;
    
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
    public ServerLocalPropertiesVisual(WLInstantiatingIterator instantiatingIterator) {
        // save the instantiating iterator
        this.instantiatingIterator = instantiatingIterator;

        // set the panel's name
        setName(NbBundle.getMessage(
                ServerLocalPropertiesVisual.class, "SERVER_PROPERTIES_STEP") );  // NOI18N

        initComponents();
        
        localInstancesCombo.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                fireChangeEvent();
            }
        });
        localInstancesCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
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

        // perhaps we could use just strings in combo
        // not sure about the domain with same name - use directory ?
        Object item = localInstancesCombo.getEditor().getItem();
        Instance instance;
        if (item instanceof Instance) {
            instance = (Instance) item;
        } else {
            instance = getServerInstance(item.toString().trim());
        }
        if (instance == null) {
            String value = item.toString().trim();
            for (Instance inst : instances) {
                if (value.equals(inst.getDomainName())) {
                    instance = inst;
                    break;
                }
            }
        }

        // check the profile root directory for validity
        if (instance == null || !isValidDomainRoot(instance.getDomainPath())) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerLocalPropertiesVisual.class, "ERR_INVALID_DOMAIN_ROOT"))); // NOI18N
            return false;
        }

        if (InstanceProperties.getInstanceProperties(getUrl(instance)) != null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerLocalPropertiesVisual.class, "ERR_ALREADY_REGISTERED"))); // NOI18N
            return false;
        }

        // we do expand version string here because one may be 12.1.4.0 while the other may be 12.1.4.0.0
        if (instance.getDomainVersion() != null
                && instantiatingIterator.getServerVersion() != null
                && !instantiatingIterator.getServerVersion().expand("0").equals(instance.getDomainVersion().expand("0"))) { // NOI18N
            wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(
                            ServerLocalPropertiesVisual.class, "ERR_INVALID_DOMAIN_VERSION"))); // NOI18N
            return false;
        }

        if (instance.isProductionModeEnabled()){
            wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(
                            ServerLocalPropertiesVisual.class, "WARN_PRODUCTION_MODE"))); // NOI18N
        }

        // show a hint for sample domain
        if (instance.getName().startsWith("examples") && passwordField.getPassword().length <= 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerLocalPropertiesVisual.class, "ERR_EMPTY_SAMPLE_PASSWORD"))); // NOI18N
        } else if (passwordField.getPassword().length <= 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(ServerLocalPropertiesVisual.class, "ERR_EMPTY_PASSWORD"))); // NOI18N
        } else {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                    WLInstantiatingIterator.decorateMessage(NbBundle.getMessage(this.getClass(), "MSG_RegisterExisting", instance.getDomainName()))); // NOI18N
        }

        // save the data to the parent instantiating iterator
        instantiatingIterator.setUrl(getUrl(instance));
        instantiatingIterator.setDomainRoot(instance.getDomainPath());
        instantiatingIterator.setUsername(usernameField.getText());
        instantiatingIterator.setPassword(new String(passwordField.getPassword()));
        instantiatingIterator.setPort(Integer.toString(instance.getPort()));
        instantiatingIterator.setDomainName(instance.getDomainName());
        instantiatingIterator.setHost(instance.getHost());
        instantiatingIterator.setRemote(false);
        instantiatingIterator.setSsl(false);
        return true;
    }

    private String getUrl(Instance instance) {
        return WLDeploymentFactory.getUrl(instance.getHost(), instance.getPort(), instantiatingIterator.getServerRoot(), instance.getDomainPath());
    }

    /**
     * Checks whether the specified path is the valid domain root directory.
     *
     * @return true if the path is the valid domain root, false otherwise
     */
    private boolean isValidDomainRoot(String path) {
        // set the child directories/files that should be present and validate
        // the directory as the domain root

        String[] children = {
                    "servers", // NOI18N
                    "config", // NOI18N
                    "config/config.xml", // NOI18N
        };
        return hasChildren(path, children);
    }

    /**
     * Checks whether the supplied directory has the required children
     *
     * @return true if the directory contains all the children, false otherwise
     */
    private boolean hasChildren(String parent, String[] children) {
        // if parent is null, it cannot contain any children
        if (parent == null) {
            return false;
        }

        // if the children array is null, then the condition is fullfilled
        if (children == null) {
            return true;
        }

        // for each child check whether it is contained and if it is not,
        // return false
        for (int i = 0; i < children.length; i++) {
            if (!(new File(parent + File.separator + children[i]).exists())) {
                return false;
            }
        }

        // all is good
        return true;
    }

    /**
     * Gets the list of local server instances.
     *
     * @return a vector with the local instances
     */
    private List<Instance> getServerInstances() {
        // initialize the resulting vector
        List<Instance> result = new ArrayList<Instance>();

        // get the list of registered profiles
        String[] domains = WLPluginProperties
                .getRegisteredDomainPaths(instantiatingIterator.getServerRoot());

        // for each domain get the list of instances
        for (int i = 0; i < domains.length; i++) {
            Instance localInstance = getServerInstance(domains[i]);
            if (localInstance != null) {
                result.add(localInstance);
            }
        }

        // convert the vector to an array and return
        return result;
    }
    
    private Instance getServerInstance(String domainPath) {
        DomainConfiguration config = WebLogicLayout.getDomainConfiguration(domainPath);
        if (config == null) {
            return null;
        }
        if (config.getAdminServer() == null) {
            return null;
        }

        return new Instance(config.getAdminServer(), config.getHost(), config.getPort(),
                domainPath, config.getName(), VersionBridge.getVersion(config.getVersion()),
                config.isProduction());
    }


    /**
     * Updates the local instances combobox model with the fresh local
     * instances list
     */
    public void updateInstancesList() {
        instances.clear();
        instances.addAll(getServerInstances());
        localInstancesCombo.setModel(new DefaultComboBoxModel(instances.toArray()));
    }
    
    public void updateJpa2Button() {
        File root = new File(instantiatingIterator.getServerRoot());
        support = new WLJpa2SwitchSupport(root);
        boolean statusVisible = support.isSwitchSupported();
        boolean buttonVisible = statusVisible
                && !support.isEnabledViaSmartUpdate();

        jpa2SwitchLabel.setVisible(statusVisible);
        jpa2Status.setVisible(statusVisible);
        jpa2SwitchButton.setVisible(buttonVisible);
        updateJpa2Status();
    }
    
    private void updateJpa2Status() {
        if (support.isEnabled() || support.isEnabledViaSmartUpdate()) {
            jpa2Status.setText(NbBundle.getMessage(ServerLocalPropertiesVisual.class, "ServerLocalPropertiesVisual.jpa2Status.enabledText"));
            Mnemonics.setLocalizedText(jpa2SwitchButton, NbBundle.getMessage(ServerLocalPropertiesVisual.class, "ServerLocalPropertiesVisual.jpa2SwitchButton.disableText"));
        } else {
            jpa2Status.setText(NbBundle.getMessage(ServerLocalPropertiesVisual.class, "ServerLocalPropertiesVisual.jpa2Status.disabledText"));
            Mnemonics.setLocalizedText(jpa2SwitchButton, NbBundle.getMessage(ServerLocalPropertiesVisual.class, "ServerLocalPropertiesVisual.jpa2SwitchButton.enableText"));
        }         
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

        localInstancesLabel = new javax.swing.JLabel();
        localInstancesCombo = new javax.swing.JComboBox(new javax.swing.DefaultComboBoxModel(getServerInstances().toArray()));
        usernameLabel = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        browseButton = new javax.swing.JButton();
        explanationLabel = new javax.swing.JLabel();
        jpa2SwitchLabel = new javax.swing.JLabel();
        jpa2Status = new javax.swing.JLabel();
        jpa2SwitchButton = new javax.swing.JButton();

        localInstancesLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        localInstancesLabel.setLabelFor(localInstancesCombo);
        org.openide.awt.Mnemonics.setLocalizedText(localInstancesLabel, org.openide.util.NbBundle.getMessage(ServerLocalPropertiesVisual.class, "LBL_LOCAL_INSTANCE")); // NOI18N

        localInstancesCombo.setEditable(true);
        localInstancesCombo.addItemListener(new LocalInstancesItemListener());

        usernameLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        usernameLabel.setLabelFor(usernameField);
        org.openide.awt.Mnemonics.setLocalizedText(usernameLabel, org.openide.util.NbBundle.getMessage(ServerLocalPropertiesVisual.class, "LBL_USERNAME")); // NOI18N

        usernameField.setColumns(15);
        usernameField.setText("weblogic"); // NOI18N

        passwordLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(ServerLocalPropertiesVisual.class, "LBL_PASSWORD")); // NOI18N

        passwordField.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(ServerLocalPropertiesVisual.class, "ServerLocalPropertiesVisual.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        explanationLabel.setText(org.openide.util.NbBundle.getMessage(ServerLocalPropertiesVisual.class, "ServerLocalPropertiesVisual.explanationLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jpa2SwitchLabel, org.openide.util.NbBundle.getMessage(ServerLocalPropertiesVisual.class, "ServerLocalPropertiesVisual.jpa2SwitchLabel.text")); // NOI18N

        jpa2Status.setText(org.openide.util.NbBundle.getMessage(ServerLocalPropertiesVisual.class, "ServerLocalPropertiesVisual.jpa2Status.disabledText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jpa2SwitchButton, org.openide.util.NbBundle.getMessage(ServerLocalPropertiesVisual.class, "ServerLocalPropertiesVisual.jpa2SwitchButton.enableText")); // NOI18N
        jpa2SwitchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jpa2SwitchButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(localInstancesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(localInstancesCombo, 0, 1, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseButton))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(passwordLabel)
                    .addComponent(usernameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 204, Short.MAX_VALUE))
            .addComponent(explanationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jpa2SwitchLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jpa2Status)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jpa2SwitchButton))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(localInstancesLabel)
                    .addComponent(browseButton)
                    .addComponent(localInstancesCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(explanationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameLabel)
                    .addComponent(usernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jpa2SwitchLabel)
                    .addComponent(jpa2Status)
                    .addComponent(jpa2SwitchButton)))
        );

        localInstancesCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerLocalPropertiesVisual.class, "ACSD_ServerLocalPropertiesPanel_localInstancesCombo")); // NOI18N
        usernameField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerLocalPropertiesVisual.class, "ACSD_ServerPropertiesPanel_usernameField")); // NOI18N
        passwordField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ServerLocalPropertiesVisual.class, "ACSD_ServerPropertiesPanel_passwordField")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        Object item = localInstancesCombo.getEditor().getItem();
        if (item != null && !item.toString().trim().isEmpty()) {
            chooser.setSelectedFile(new File(item.toString()));
        } else {
            chooser.setSelectedFile(new File(instantiatingIterator.getServerRoot()));
        }
        if (chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            localInstancesCombo.getEditor().setItem(chooser.getSelectedFile().getAbsolutePath());
            fireChangeEvent();
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void jpa2SwitchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jpa2SwitchButtonActionPerformed
        if (!support.isEnabled()) {
            support.enable();
        } else {
            support.disable();
        }
        updateJpa2Status();
    }//GEN-LAST:event_jpa2SwitchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel explanationLabel;
    private javax.swing.JLabel jpa2Status;
    private javax.swing.JButton jpa2SwitchButton;
    private javax.swing.JLabel jpa2SwitchLabel;
    private javax.swing.JComboBox localInstancesCombo;
    private javax.swing.JLabel localInstancesLabel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JTextField usernameField;
    private javax.swing.JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables
    

    /**
     * Simple listener for instance combo box changes.
     */
    private class LocalInstancesItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            fireChangeEvent();
        }

    }

    /**
     * A model for the server instance. It contains all the critical properties
     * for the plugin: name, host, port, profile path, domain name.
     *
     * @author Kirill Sorokin
     */
    private static class Instance implements Serializable {

        /**
         * Instance's name, it is used a the parameter to the startup/shutdown
         * scripts
         */
        private String name;

        /**
         * Instance's host
         */
        private String host;

        /**
         * Instance's port
         */
        private int port;

        /**
         * Instance's profile directory
         */
        private String domainPath;
        
        /**
         * Instance's domain name
         */
        private String domainName;
        
        /**
         * Production mode is enabled for domain.
         */
        private boolean isProductionModeEnabled;

        private Version domainVersion;

        /**
         * Creates a new instance of Instance
         *
         * @param name the instance's name
         * @param host the instance's host
         * @param port the instance's port
         * @param domainPath the instance's profile path
         */
        public Instance(String name, String host, int port, String domainPath,
                String domainName, Version domainVersion, boolean isProductionModeEnabled) {
            // save the properties
            this.name = name;
            this.host = host;
            this.port = port;
            this.domainPath = domainPath;
            this.domainName = domainName;
            this.domainVersion = domainVersion;
            this.isProductionModeEnabled = isProductionModeEnabled;
        }

        /**
         * Getter for the instance's name
         *
         * @return the instance's name
         */
        public String getName() {
            return this.name;
        }
        
        /**
         * Getter for the domain name
         *
         * @return the domain name
         */
        public String getDomainName() {
            return this.domainName;
        }

        /**
         * Getter for the instance's host
         *
         * @return the instance's host
         */
        public String getHost() {
            return this.host;
        }

        /**
         * Getter for the instance's port
         *
         * @return the instance's port
         */
        public int getPort() {
            return this.port;
        }

        /**
         * Getter for the instance's profile path
         *
         * @return the instance's profile path
         */
        public String getDomainPath() {
            return this.domainPath;
        }
        
        /**
         * 
         * Getter for domain production mode property. 
         * 
         * @return true if production mode is enabled for domain
         */
        public boolean isProductionModeEnabled(){
            return isProductionModeEnabled;
        }

        public Version getDomainVersion() {
            return domainVersion;
        }

        /**
         * An overriden version of the Object's toString() so that the
         * instance is displayed properly in the combobox
         */
        @Override
        public String toString() {
            return domainPath;//domainName;
        }
    }
}
