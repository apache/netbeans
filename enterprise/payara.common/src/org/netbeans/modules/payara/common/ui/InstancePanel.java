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
package org.netbeans.modules.payara.common.ui;

import java.net.InetAddress;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import org.netbeans.modules.payara.tooling.utils.NetUtils;
import org.netbeans.modules.payara.common.PayaraLogger;
import org.netbeans.modules.payara.common.PayaraSettings;
import org.netbeans.modules.payara.common.PayaraInstance;
import org.openide.util.NbBundle;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.payara.tooling.data.PayaraPlatformVersionAPI;

/**
 * Common instance properties editor.
 * <p/>
 * @author Tomas Kraus
 */
public abstract class InstancePanel extends javax.swing.JPanel {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Properties for check box fields.
     */
    protected static class CheckBoxProperties {

        /** Comet support property. */
        final String cometSupportProperty;

        /** HTTP monitor property. */
        final String httpMonitorProperty;

        /** JDBC driver deployment property. */
        final String jdbcDriverDeploymentProperty;

        /** Preserve sessions property. */
        final String preserveSessionsProperty;

        /** hot deploy property. */
        final String hotDeployProperty;

        /** Loopback property. */
        final String loopbackProperty;

        /**
         * Creates an instance of check box fields properties by retrieving them
         * from Payara instance object.
         * <p/>
         * @param instance Payara instance object containing check box
         *        fields properties.
         */
        protected CheckBoxProperties(final PayaraInstance instance) {
            String cometSupportPropertyTmp
                    = instance.getProperty(PayaraModule.COMET_FLAG);
            cometSupportProperty = cometSupportPropertyTmp != null
                    ? cometSupportPropertyTmp
                    : System.getProperty(PayaraModule.COMET_FLAG);
            httpMonitorProperty
                    = instance.getProperty(PayaraModule.HTTP_MONITOR_FLAG);
            jdbcDriverDeploymentProperty
                    = instance.getProperty(PayaraModule.DRIVER_DEPLOY_FLAG);
            hotDeployProperty
                    = instance.getProperty(PayaraModule.HOT_DEPLOY);
            preserveSessionsProperty
                    = instance.getProperty(PayaraModule.SESSION_PRESERVATION_FLAG);
            loopbackProperty
                    = instance.getProperty(PayaraModule.LOOPBACK_FLAG);
        }

        /**
         * Store given <code>boolean</code> property into Payara instance
         * object properties.
         * <p/>
         * @param key      Payara instance object property key.
         * @param value    Payara instance object property value.
         * @param instance Payara instance object to store properties.
         */
        protected void storeBooleanProperty(final String key, final boolean value,
                final PayaraInstance instance) {
            // Store true value as String property.
            if (value) {
                instance.putProperty(key, Boolean.toString(value));
            // Store false value by removal of property.
            } else {
                instance.removeProperty(key);
            }
        }

        /**
         * Store check box fields flags by setting them as Payara instance
         * object properties.
         * <p/>
         * @param cometSupportFlag         Comet support flag.
         * @param httpMonitorFlag          HTTP monitor flag.
         * @param jdbcDriverDeploymentFlag JDBC driver deployment flag.
         * @param hotDeployFlag
         * @param preserveSessionsFlag     Preserve sessions flag.
         * @param loopbackFlag
         * @param instance                 Payara instance object to store
         *                                 check box fields properties.
         */
        protected void store(final boolean cometSupportFlag,
                final boolean httpMonitorFlag,
                final boolean jdbcDriverDeploymentFlag,
                final boolean hotDeployFlag,
                final boolean preserveSessionsFlag,
                final boolean loopbackFlag,
                final PayaraInstance instance) {
            // Update properties only when stored value differs.
            if (cometSupportFlag != getCommetSupportProperty()) {
                // Comet support is always stored into instance when differs.
                instance.putProperty(PayaraModule.COMET_FLAG,
                        Boolean.toString(cometSupportFlag));
            }
            if (httpMonitorFlag != getHttpMonitorProperty()) {
                storeBooleanProperty(PayaraModule.HTTP_MONITOR_FLAG,
                        httpMonitorFlag, instance);
            }
            if (jdbcDriverDeploymentFlag != getJdbcDriverDeploymentProperty()) {
                storeBooleanProperty(PayaraModule.DRIVER_DEPLOY_FLAG,
                        jdbcDriverDeploymentFlag, instance);
            }
            if (hotDeployFlag != getHotDeployProperty()) {
                storeBooleanProperty(PayaraModule.HOT_DEPLOY,
                        hotDeployFlag, instance);
            }
            if (preserveSessionsFlag != getPreserveSessionsProperty()) {
                storeBooleanProperty(PayaraModule.SESSION_PRESERVATION_FLAG,
                        preserveSessionsFlag, instance);
            }
            if (loopbackFlag != getLoopbackProperty()) {
                storeBooleanProperty(PayaraModule.LOOPBACK_FLAG,
                        loopbackFlag, instance);
            }
        }

        /**
         * Get Comet support property
         * <p/>
         * @return Comet support property.
         */
        protected boolean getCommetSupportProperty() {
            return Boolean.parseBoolean(cometSupportProperty);
        }

        /**
         * Get HTTP monitor property.
         * <p/>
         * @return HTTP monitor property.
         */
        protected boolean getHttpMonitorProperty() {
            return Boolean.parseBoolean(httpMonitorProperty);
        }

        /**
         * Get JDBC driver deployment property.
         * <p/>
         * @return JDBC driver deployment property.
         */
        protected boolean getJdbcDriverDeploymentProperty() {
            return Boolean.parseBoolean(jdbcDriverDeploymentProperty);
        }

        /**
         * Get hot deploy property.
         * <p/>
         * @return hot deploy property.
         */
        protected boolean getHotDeployProperty() {
            return Boolean.parseBoolean(hotDeployProperty);
        }

        /**
         * Get preserve sessions property.
         * <p/>
         * @return Preserve sessions property.
         */
        protected boolean getPreserveSessionsProperty() {
            return Boolean.parseBoolean(preserveSessionsProperty);
        }

        protected boolean getLoopbackProperty() {
            return Boolean.parseBoolean(loopbackProperty);
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = PayaraLogger.get(InstancePanel.class);

    /** Maximum port number value. */
    private static final int MAX_PORT_VALUE = 0x10000 - 0x01;

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Payara server instance to be modified. */
    protected final PayaraInstance instance;
    
    /** IP addresses selection content. */
    protected Set<? extends InetAddress> ips;

    /** Comet support flag. */
    protected boolean cometSupportFlag;

    /** HTTP monitor flag. */
    protected boolean httpMonitorFlag;

    /** JDBC driver deployment flag. */
    protected boolean jdbcDriverDeploymentFlag;

    /** Show password text in this form flag. */
    protected boolean showPasswordFlag;

    /** Preserve sessions flag. */
    protected boolean preserverSessionsFlag;

     /** Hot Deploy flag. */
    protected boolean hotDeployFlag;

    protected boolean loopbackFlag;

    /** Configuration file <code>domain.xml</code> was parsed successfully. */
    protected boolean configFileParsed;

    ////////////////////////////////////////////////////////////////////////////
    // Abstract methods                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Host field initialization.
     * <p/>
     * Initialize proper host fields in child class.
     */
    protected abstract void initHost();

    /**
     * Port fields initialization.
     * <p/>
     * Initialize proper port fields in child class.
     */
    protected abstract void initPorts();

    /**
     * Get host field value to be stored into local Payara server instance
     * object properties.
     * <p/>
     * @return Host field value converted to {@link String}.
     */
    protected abstract String getHost();

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of common Payara server properties editor.
     * <p/>
     * @param instance Payara server instance to be modified.
     */
    protected InstancePanel(final PayaraInstance instance) {
        this.instance = instance;
        ips = NetUtils.getHostIP4s();
        initComponents();
        ((AbstractDocument)dasPortField.getDocument())
                .setDocumentFilter(new Filter.PortNumber());
        ((AbstractDocument)httpPortField.getDocument())
                .setDocumentFilter(new Filter.PortNumber());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Installation and domain directories fields initialization.
     * <p/>
     * Initialize installation root and domains directory fields with values
     * stored in Payara instance object. This code can be part of common
     * initialization because for remote server domains folder is initialized
     * as empty <code>String</code> and child class makes it just invisible.
     */
    protected void initDirectoriesFields() {
        String installationRoot = instance.getPayaraRoot();
        String domainsFolder = instance.getDomainsFolder();
        installationLocationField.setText(
                installationRoot != null ? installationRoot : "");
        domainsFolderField.setText(domainsFolder != null ? domainsFolder : "");
    }

    /**
     * Domain name and target fields initialization.
     * <p/>
     * Initialize domain name and target fields with values stored in Payara
     * instance object.
     */
    protected void initDomainAndTarget() {
        String target = instance.getTarget();
        String domainName = instance.getDomainName();
        domainField.setText(domainName != null ? domainName : "");
        targetField.setText(target != null ? target : "");
    }

    /**
     * Credential fields initialization.
     * <p/>
     * Initialize user name and password fields with values stored in Payara
     * instance object.
     */
    protected void initCredentials() {
        userNameField.setText(instance.getUserName());
        passwordField.setText(instance.getPassword());
    }
    
    /**
     * Docker fields initialization.
     * <p/>
     * Initialize host path and container path with values stored in Payara
     * instance object.
     */
    protected void initInstanceType() {
        if (instance.isDocker()) {
            instanceTypeComboBox.setSelectedItem(DOCKER_INSTANCE);
        } else if(instance.isWSL()) {
            instanceTypeComboBox.setSelectedItem(WSL_INSTANCE);
        } else {
            instanceTypeComboBox.setSelectedItem(DEFAULT_INSTANCE);
        }
        hostPathField.setText(instance.getHostPath());
        containerPathField.setText(instance.getContainerPath());
    }

    /**
     * Initialize internal properties storage from Payara instance object
     * properties.
     * <p/>
     * @param properties Payara instance object properties for check boxes.
     */
    protected void initFlagsFromProperties(final CheckBoxProperties properties) {
        cometSupportFlag = properties.getCommetSupportProperty();
        httpMonitorFlag = properties.getHttpMonitorProperty();
        jdbcDriverDeploymentFlag = properties.getJdbcDriverDeploymentProperty();
        hotDeployFlag = properties.getHotDeployProperty();
        preserverSessionsFlag = properties.getPreserveSessionsProperty();
        loopbackFlag = properties.getLoopbackProperty();
    }

    /**
     * Check box fields initialization.
     * <p/>
     * Initialize check box fields to allow Payara instance flags
     * modification and allowing user to display password text in password
     * field.
     */
    protected void initCheckBoxes() {
        // Retrieve properties from Payara instance object.
        initFlagsFromProperties(new CheckBoxProperties(instance));
        // Initialize internal properties storage.
        showPasswordFlag
                = PayaraSettings.getGfShowPasswordInPropertiesForm();
        // Set form fields values.
        commetSupport.setSelected(cometSupportFlag);
        httpMonitor.setSelected(httpMonitorFlag);
        jdbcDriverDeployment.setSelected(jdbcDriverDeploymentFlag);
        showPassword.setSelected(showPasswordFlag);
        hotDeploy.setSelected(instance.isHotDeployFeatureAvailable()? hotDeployFlag : false);
        preserveSessions.setSelected(preserverSessionsFlag);
        localIpCB.setSelected(loopbackFlag);
    }

    /**
     * Host name field storage.
     * <p/>
     * Store host field content when form fields value differs from Payara
     * instance property.
     */
    protected void storeHost() {
        String host = getHost();
        if (!host.equals(instance.getHost())) {
            instance.setHost(host);
        }
    }

    /**
     * Check box fields storage.
     * <p/>
     * Store check box fields after Payara instance flags modification
     * and store current status of allowing user to display password text
     * in password field.
     */
    protected void storeCheckBoxes() {
        CheckBoxProperties properties = new CheckBoxProperties(instance);
        properties.store(cometSupportFlag, httpMonitorFlag,
                jdbcDriverDeploymentFlag, hotDeployFlag,
                preserverSessionsFlag, loopbackFlag, instance);
        PayaraSettings.setGfShowPasswordInPropertiesForm(showPasswordFlag);
    }

    /**
     * DAS and HTTP ports fields storage.
     * <p/>
     * Validate and store DAS and HTTP ports fields when form fields values
     * differs from Payara instance properties.
     */
    protected void storePorts() {
        final String dasPortStr = dasPortField.getText().trim();
        final String httpPortStr = httpPortField.getText().trim();
        try {
            int dasPort = Integer.parseInt(dasPortStr);
            if (0 <= dasPort && dasPort < MAX_PORT_VALUE) {
                // Update value only when values differs.
                if (instance.getAdminPort() != dasPort) {
                    instance.setAdminPort(dasPort);
                }
            } else {
                LOGGER.log(Level.INFO,
                        NbBundle.getMessage(InstancePanel.class,
                        "InstanceLocalPanel.storePorts.dasPortRange",
                        dasPortStr));
            }
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.INFO, NbBundle.getMessage(InstancePanel.class,
                    "InstanceLocalPanel.storePorts.dasPortInvalid",
                    dasPortStr));
        }
        try {
            int httpPort = Integer.parseInt(httpPortStr);
            if (0 <= httpPort && httpPort < MAX_PORT_VALUE) {
                if (instance.getPort() != httpPort) {
                    instance.setHttpPort(httpPort);
                }
            } else {
                LOGGER.log(Level.INFO,
                        NbBundle.getMessage(InstancePanel.class,
                        "InstanceLocalPanel.storePorts.httpPortRange",
                        dasPortStr));
            }
        } catch (NumberFormatException nfe) {
            LOGGER.log(Level.INFO, NbBundle.getMessage(InstancePanel.class,
                    "InstanceLocalPanel.storePorts.httpPortInvalid",
                    httpPortStr));
        }
    }

    /**
     * Target field storage.
     * <p/>
     * Store target when form field value differs from Payara instance
     * property.
     */
    protected void storeTarget() {
        String target = targetField.getText().trim();
        if (!target.equals(instance.getTarget())) {
            instance.setTarget(target);
        }
    }

    /**
     * Administrator user credentials storage.
     * <p/>
     * Store administrator user name and password when form fields values
     * differs from Payara instance properties.
     */
    protected void storeCredentials() {
        final String userName = userNameField.getText().trim();
        final String password = new String(passwordField.getPassword());
        if (!userName.equals(instance.getAdminUser())) {
            instance.setAdminUser(userName);
        }
        if (!password.equals(instance.getAdminPassword())) {
            instance.setAdminPassword(password);
        }
    }

    /**
     * Stores the instance type and related paths into the Payara instance
     * object.
     * <p/>
     * This method checks the selected instance type from the combo box and
     * updates the Payara instance accordingly. It also stores the host path and
     * container path if they differ from the values stored in the instance
     * object.
     * <p/>
     * The instance type can be one of the following: Docker, WSL, or Remote. If
     * Docker is selected, both host path and container path fields are enabled.
     * Otherwise, they are disabled.
     */
    protected void storeInstanceType() {
        if (instanceTypeComboBox.getSelectedItem().equals(DOCKER_INSTANCE)) {
            instance.setDocker(true);
            instance.setWSL(false);
        } else if (instanceTypeComboBox.getSelectedItem().equals(WSL_INSTANCE)) {
            instance.setWSL(true);
            instance.setDocker(false);
        } else {
            instance.setWSL(false);
            instance.setDocker(false);
        }
        final String hostPath = hostPathField.getText().trim();
        if (!hostPath.equals(instance.getHostPath())) {
            instance.setHostPath(hostPath);
        }
        final String containerPath = containerPathField.getText().trim();
        if (!containerPath.equals(instance.getContainerPath())) {
            instance.setContainerPath(containerPath);
        }
    }
    
    /**
     * Enable form fields that can be modified by user.
     * <p/>
     * Set those form fields that can be modified by user as enabled. This
     * is usually done after form has been initialized when all form fields
     * are currently disabled.
     */
    protected void enableFields() {
        if (!configFileParsed) {
            dasPortField.setEnabled(true);
            httpPortField.setEnabled(true);
        }
        targetField.setEnabled(true);
        userNameField.setEnabled(true);
        passwordField.setEnabled(true);
        commetSupport.setEnabled(true);
        httpMonitor.setEnabled(true);
        jdbcDriverDeployment.setEnabled(true);
        hotDeploy.setEnabled(instance.isHotDeployFeatureAvailable());
        showPassword.setEnabled(true);
        preserveSessions.setEnabled(true);
        instanceTypeComboBox.setEnabled(true);
        if (instanceTypeComboBox.getSelectedItem().equals(DOCKER_INSTANCE)) {
            hostPathField.setEnabled(true);
            containerPathField.setEnabled(true);
        } else {
            hostPathField.setEnabled(false);
            containerPathField.setEnabled(false);
        }
    }

    /**
     * Disable all form fields.
     * <p/>
     * Set all form fields as disabled. This is usually done when form is being
     * initialized or stored.
     */
    protected void disableAllFields() {
        installationLocationField.setEnabled(false);
        domainsFolderField.setEnabled(false);
        hostLocalField.setEnabled(false);
        localIpCB.setEnabled(false);
        hostRemoteField.setEnabled(false);
        dasPortField.setEnabled(false);
        httpPortField.setEnabled(false);
        domainField.setEnabled(false);
        targetField.setEnabled(false);
        userNameField.setEnabled(false);
        passwordField.setEnabled(false);
        commetSupport.setEnabled(false);
        httpMonitor.setEnabled(false);
        jdbcDriverDeployment.setEnabled(false);
        hotDeploy.setEnabled(false);
        showPassword.setEnabled(false);
        preserveSessions.setEnabled(false);
        instanceTypeComboBox.setEnabled(false);
        hostPathField.setEnabled(false);
        containerPathField.setEnabled(false);
    }

    /**
     * Initialize form field values from Payara server entity object.
     * <p/>
     * This is top level initialization method used when entering form.
     */
    protected void initFormFields() {
        initDirectoriesFields();
        initPorts();
        initHost();
        initDomainAndTarget();
        initCredentials();
        initInstanceType();
        initCheckBoxes();
        updatePasswordVisibility();
        // do the magic according to loopback checkbox
        localIpCBActionPerformed(null);
    }

    /**
     * Store form field values into Payara server entity object.
     * <p/>
     * This is top level storage method used when leaving form.
     */
    protected void storeFormFields() {
        storeHost();
        storePorts();
        storeTarget();
        storeCredentials();
        storeInstanceType();
        storeCheckBoxes();
    }

    /**
     * Called when entering this panel.
     * <p/>
     * Initialize all panel form fields.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        disableAllFields();
        initFormFields();
        enableFields();
    }

    /**
     * Called when leaving this panel.
     * <p/>
     * Store all form fields from panel.
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        disableAllFields();
        storeFormFields();
    }

    /**
     * Show and hide password text depending on related check box.
     */
    protected void updatePasswordVisibility() {
        showPasswordFlag = showPassword.isSelected();
        passwordField.setEchoChar(showPasswordFlag ? '\0' : '*');        
    }

    ////////////////////////////////////////////////////////////////////////////
    // Generated GUI code                                                     //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        hostLocalLabel = new javax.swing.JLabel();
        localIpCB = new javax.swing.JCheckBox();
        hostLocalField = new IpComboBox(ips, localIpCB.isSelected());
        dasPortLabel = new javax.swing.JLabel();
        dasPortField = new javax.swing.JTextField();
        httpPortLabel = new javax.swing.JLabel();
        httpPortField = new javax.swing.JTextField();
        domainLabel = new javax.swing.JLabel();
        domainField = new javax.swing.JTextField();
        targetLabel = new javax.swing.JLabel();
        targetField = new javax.swing.JTextField();
        installationLocationLabel = new javax.swing.JLabel();
        installationLocationField = new javax.swing.JTextField();
        domainsFolderLabel = new javax.swing.JLabel();
        domainsFolderField = new javax.swing.JTextField();
        userNameLabel = new javax.swing.JLabel();
        userNameField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        commetSupport = new javax.swing.JCheckBox();
        preserveSessions = new javax.swing.JCheckBox();
        httpMonitor = new javax.swing.JCheckBox();
        jdbcDriverDeployment = new javax.swing.JCheckBox();
        passwordField = new javax.swing.JPasswordField();
        hostRemoteLabel = new javax.swing.JLabel();
        hostRemoteField = new javax.swing.JTextField();
        hotDeploy = new javax.swing.JCheckBox();
        showPassword = new javax.swing.JToggleButton();
        instanceTypeLabel = new javax.swing.JLabel();
        containerPathField = new javax.swing.JTextField();
        containerPathLabel = new javax.swing.JLabel();
        hostPathField = new javax.swing.JTextField();
        hostPathLabel = new javax.swing.JLabel();
        instanceTypeComboBox = new javax.swing.JComboBox<>();

        setName(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.displayName")); // NOI18N
        setPreferredSize(new java.awt.Dimension(602, 304));

        hostLocalLabel.setLabelFor(hostLocalField);
        org.openide.awt.Mnemonics.setLocalizedText(hostLocalLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.hostLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(localIpCB, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.localIpCB")); // NOI18N
        localIpCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localIpCBActionPerformed(evt);
            }
        });

        hostLocalField.setEditable(true);

        dasPortLabel.setLabelFor(dasPortField);
        org.openide.awt.Mnemonics.setLocalizedText(dasPortLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.dasPortLabel")); // NOI18N

        dasPortField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.dasPortField.text")); // NOI18N

        httpPortLabel.setLabelFor(httpPortField);
        org.openide.awt.Mnemonics.setLocalizedText(httpPortLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.httpPortLabel")); // NOI18N

        httpPortField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.httpPortField.text")); // NOI18N

        domainLabel.setLabelFor(domainField);
        org.openide.awt.Mnemonics.setLocalizedText(domainLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.domainLabel")); // NOI18N

        domainField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.domainField.text")); // NOI18N

        targetLabel.setLabelFor(targetField);
        org.openide.awt.Mnemonics.setLocalizedText(targetLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.targetLabel")); // NOI18N

        targetField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.targetField.text")); // NOI18N

        installationLocationLabel.setLabelFor(installationLocationField);
        org.openide.awt.Mnemonics.setLocalizedText(installationLocationLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.InstallationlocationLabel")); // NOI18N

        installationLocationField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.installationLocationField.text")); // NOI18N

        domainsFolderLabel.setLabelFor(domainsFolderField);
        org.openide.awt.Mnemonics.setLocalizedText(domainsFolderLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.domainsFolderLabel")); // NOI18N

        domainsFolderField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.domainsFolderField.text")); // NOI18N

        userNameLabel.setLabelFor(userNameField);
        org.openide.awt.Mnemonics.setLocalizedText(userNameLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.userNameLabel")); // NOI18N

        userNameField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.userNameField.text")); // NOI18N

        passwordLabel.setLabelFor(passwordField);
        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.passwordLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(commetSupport, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.commetSupport")); // NOI18N
        commetSupport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commetSupportActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(preserveSessions, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.preserveSessions")); // NOI18N
        preserveSessions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preserveSessionsActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(httpMonitor, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.httpMonitor")); // NOI18N
        httpMonitor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                httpMonitorActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jdbcDriverDeployment, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstanceLocalPanel.jdbcDriverDeployment")); // NOI18N
        jdbcDriverDeployment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jdbcDriverDeploymentActionPerformed(evt);
            }
        });

        passwordField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.passwordField.text")); // NOI18N

        hostRemoteLabel.setLabelFor(domainsFolderField);
        org.openide.awt.Mnemonics.setLocalizedText(hostRemoteLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.hostRemoteLabel.text")); // NOI18N

        hostRemoteField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.hostRemoteField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(hotDeploy, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.hotDeploy.text")); // NOI18N
        hotDeploy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hotDeployActionPerformed(evt);
            }
        });

        showPassword.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/payara/common/resources/show-password.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(showPassword, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.showPassword.text")); // NOI18N
        showPassword.setAlignmentY(0.0F);
        showPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPasswordActionPerformed(evt);
            }
        });

        instanceTypeLabel.setLabelFor(dasPortField);
        org.openide.awt.Mnemonics.setLocalizedText(instanceTypeLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.instanceTypeLabel.text")); // NOI18N

        containerPathField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.containerPathField.text")); // NOI18N

        containerPathLabel.setLabelFor(httpPortField);
        org.openide.awt.Mnemonics.setLocalizedText(containerPathLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.containerPathLabel.text")); // NOI18N

        hostPathField.setText(org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.hostPathField.text")); // NOI18N

        hostPathLabel.setLabelFor(dasPortField);
        org.openide.awt.Mnemonics.setLocalizedText(hostPathLabel, org.openide.util.NbBundle.getMessage(InstancePanel.class, "InstancePanel.hostPathLabel.text")); // NOI18N

        instanceTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(getInstanceTypes()));
        instanceTypeComboBoxActionPerformed(null);
        instanceTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                instanceTypeComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(installationLocationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(domainsFolderLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(hostRemoteLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(domainsFolderField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(installationLocationField)
                            .addComponent(hostRemoteField)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jdbcDriverDeployment, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(httpMonitor, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(commetSupport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(preserveSessions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(hotDeploy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(hostPathLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(instanceTypeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(hostLocalLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(domainLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(dasPortLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(userNameLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(hostLocalField, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(localIpCB))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(domainField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                    .addComponent(dasPortField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                    .addComponent(userNameField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                                    .addComponent(hostPathField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                    .addComponent(instanceTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(httpPortLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(targetLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(passwordLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(containerPathLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(httpPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                    .addComponent(targetField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(passwordField, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(showPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(2, 2, 2))
                                    .addComponent(containerPathField, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {dasPortLabel, domainLabel, hostLocalLabel, httpPortLabel, passwordLabel, targetLabel, userNameLabel});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {domainsFolderLabel, installationLocationLabel});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(installationLocationLabel)
                    .addComponent(installationLocationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(domainsFolderLabel)
                    .addComponent(domainsFolderField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostRemoteLabel)
                    .addComponent(hostRemoteField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostLocalLabel)
                    .addComponent(hostLocalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(localIpCB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dasPortLabel)
                    .addComponent(dasPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(httpPortLabel)
                    .addComponent(httpPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(domainLabel)
                    .addComponent(domainField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(targetLabel)
                    .addComponent(targetField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(passwordField)
                    .addComponent(showPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(userNameField)
                        .addComponent(passwordLabel)
                        .addComponent(userNameLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(instanceTypeLabel)
                    .addComponent(instanceTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostPathLabel)
                    .addComponent(hostPathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(containerPathLabel)
                    .addComponent(containerPathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(commetSupport)
                    .addComponent(hotDeploy))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(httpMonitor)
                    .addComponent(preserveSessions))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jdbcDriverDeployment)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private final static String DEFAULT_INSTANCE = "Remote";
    private final static String DOCKER_INSTANCE = "Docker";
    private final static String WSL_INSTANCE = "WSL";
    
    private String[] getInstanceTypes() {
        return new String[] {
            DEFAULT_INSTANCE, DOCKER_INSTANCE, WSL_INSTANCE 
        };
    }

    private void commetSupportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commetSupportActionPerformed
        cometSupportFlag = commetSupport.isSelected();
    }//GEN-LAST:event_commetSupportActionPerformed

    private void preserveSessionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preserveSessionsActionPerformed
        preserverSessionsFlag = preserveSessions.isSelected();
    }//GEN-LAST:event_preserveSessionsActionPerformed

    private void httpMonitorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_httpMonitorActionPerformed
        httpMonitorFlag = httpMonitor.isSelected();
    }//GEN-LAST:event_httpMonitorActionPerformed

    private void jdbcDriverDeploymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jdbcDriverDeploymentActionPerformed
        jdbcDriverDeploymentFlag = jdbcDriverDeployment.isSelected();
    }//GEN-LAST:event_jdbcDriverDeploymentActionPerformed

    private void localIpCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localIpCBActionPerformed
        loopbackFlag = localIpCB.isSelected();
        Object hostValue = hostLocalField.getEditor().getItem();
        hostLocalField.setEnabled(false);
        ((IpComboBox)hostLocalField).updateModel(ips, localIpCB.isSelected());
        if (hostValue instanceof IpComboBox.InetAddr) {
            ((IpComboBox)hostLocalField).setSelectedIp(
                    ((IpComboBox.InetAddr)hostValue).getIp());
        } else if (hostValue instanceof String) {
            ((IpComboBox)hostLocalField).getEditor().setItem((String)hostValue);
        } else {
            ((IpComboBox)hostLocalField).setSelectedItem(null);
        }
        hostLocalField.setEnabled(true);
    }//GEN-LAST:event_localIpCBActionPerformed

    private void showPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPasswordActionPerformed
        updatePasswordVisibility();
    }//GEN-LAST:event_showPasswordActionPerformed

    private void hotDeployActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hotDeployActionPerformed
         hotDeployFlag = hotDeploy.isSelected();
    }//GEN-LAST:event_hotDeployActionPerformed

    private void instanceTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_instanceTypeComboBoxActionPerformed
        boolean isDockerInstance = instanceTypeComboBox.getSelectedItem().equals(DOCKER_INSTANCE);
        hostPathField.setEnabled(isDockerInstance);
        containerPathField.setEnabled(isDockerInstance);
    }//GEN-LAST:event_instanceTypeComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JCheckBox commetSupport;
    protected javax.swing.JTextField containerPathField;
    protected javax.swing.JLabel containerPathLabel;
    protected javax.swing.JTextField dasPortField;
    protected javax.swing.JLabel dasPortLabel;
    protected javax.swing.JTextField domainField;
    protected javax.swing.JLabel domainLabel;
    protected javax.swing.JTextField domainsFolderField;
    protected javax.swing.JLabel domainsFolderLabel;
    protected javax.swing.JComboBox hostLocalField;
    protected javax.swing.JLabel hostLocalLabel;
    protected javax.swing.JTextField hostPathField;
    protected javax.swing.JLabel hostPathLabel;
    protected javax.swing.JTextField hostRemoteField;
    protected javax.swing.JLabel hostRemoteLabel;
    protected javax.swing.JCheckBox hotDeploy;
    protected javax.swing.JCheckBox httpMonitor;
    protected javax.swing.JTextField httpPortField;
    protected javax.swing.JLabel httpPortLabel;
    protected javax.swing.JTextField installationLocationField;
    protected javax.swing.JLabel installationLocationLabel;
    protected javax.swing.JComboBox<String> instanceTypeComboBox;
    protected javax.swing.JLabel instanceTypeLabel;
    protected javax.swing.JCheckBox jdbcDriverDeployment;
    protected javax.swing.JCheckBox localIpCB;
    protected javax.swing.JPasswordField passwordField;
    protected javax.swing.JLabel passwordLabel;
    protected javax.swing.JCheckBox preserveSessions;
    protected javax.swing.JToggleButton showPassword;
    protected javax.swing.JTextField targetField;
    protected javax.swing.JLabel targetLabel;
    protected javax.swing.JTextField userNameField;
    protected javax.swing.JLabel userNameLabel;
    // End of variables declaration//GEN-END:variables
}
