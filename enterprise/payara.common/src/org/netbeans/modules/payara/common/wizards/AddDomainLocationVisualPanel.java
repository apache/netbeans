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

package org.netbeans.modules.payara.common.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileFilter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.payara.tooling.utils.NetUtils;
import org.netbeans.modules.payara.common.PayaraInstance;
import org.netbeans.modules.payara.common.ServerDetails;
import org.netbeans.modules.payara.common.ui.IpComboBox;
import org.netbeans.modules.payara.spi.Utils;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  vbk
 */
public class AddDomainLocationVisualPanel extends javax.swing.JPanel {

    private transient final List<ChangeListener> listeners; 

    /** IP addresses selection content. */
    Set<? extends InetAddress> ips;

    /** Payara server is local or remote. */
    private boolean isLocal;

    /** DAS port value to be set as default. */
    private int defaultDasPort;

    /** HTTP port value to be set as default. */
    private int defaultHttpPort;

    /** Creates new form AddDomainLocationVisualPanel */
    public AddDomainLocationVisualPanel() {
        listeners = new CopyOnWriteArrayList<>();
        ips = NetUtils.getHostIP4s();
        initComponents();
        dasPortField.setEnabled(false);
        httpPortField.setEnabled(false);
        defaultDasPort = PayaraInstance.DEFAULT_ADMIN_PORT;
        defaultHttpPort = PayaraInstance.DEFAULT_HTTP_PORT;
        setName(NbBundle.getMessage(AddDomainLocationVisualPanel.class, "TITLE_DomainLocation")); // NOI18N
    }
    
    void initModels(final String gfRoot, final boolean isLocal) {
        if (isLocal) {
            domainLocalLabel.setVisible(true);
            domainLocalField.setVisible(true);
            hostLocalLabel.setVisible(true);
            hostLocalField.setVisible(true);
            localIpCB.setVisible(true);
            domainRemoteLabel.setVisible(false);
            domainRemoteField.setVisible(false);
            hostRemoteLabel.setVisible(false);
            hostRemoteField.setVisible(false);
            remoteLink.setVisible(false);
        } else {
            domainLocalLabel.setVisible(false);
            domainLocalField.setVisible(false);
            hostLocalLabel.setVisible(false);
            hostLocalField.setVisible(false);
            localIpCB.setVisible(false);
            domainRemoteLabel.setVisible(true);
            domainRemoteField.setVisible(true);
            hostRemoteLabel.setVisible(true);
            hostRemoteField.setVisible(true);
            remoteLink.setVisible(true);
        }
        KeyListener kl = new MyKeyListener();
        if (isLocal) {
            // Put the choices into the combo box...
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            File domainsDir = new File(
                    gfRoot, PayaraInstance.DEFAULT_DOMAINS_FOLDER);
            File candidates[] = domainsDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File dir) {
                    File logsDir = new File(dir, "logs"); // NOI18N
                    return Utils.canWrite(logsDir);
                }
            });
            if (null != candidates) {
                for (File f : candidates) {
                    model.addElement(f.getName());
                }
            }
            if (model.getSize() == 0) {
                FileObject userHome = FileUtil.toFileObject(
                        FileUtil.normalizeFile(
                        new File(System.getProperty("user.home"))));
                String defaultItem = FileUtil.findFreeFolderName(
                        userHome, "personal_domain");
                model.addElement(System.getProperty("user.home")
                        + File.separator + defaultItem);
            }
            domainLocalField.setModel(model);
            domainLocalField.getEditor()
                    .getEditorComponent().addKeyListener(kl);
            domainLocalField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    domainLocalField.getEditor().setItem(
                            domainLocalField.getSelectedItem());
                    fireChangeEvent();
                }
            });
            localIpCB.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateLocalIpsCombobox();
                    fireChangeEvent();
                }
            });
            updateLocalIpsCombobox();
        } else {
            domainRemoteField.addKeyListener(kl);
            hostRemoteField.addKeyListener(kl);
        }
        useDefaultPortsCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePortsFields();
                fireChangeEvent();
            }
        });
        dasPortField.addKeyListener(kl);
        httpPortField.addKeyListener(kl);
        initPortsFields();
        // make sure the target field is ok...
        if (ServerDetails.getVersionFromInstallDirectory(new File(gfRoot)) < 
                ServerDetails.PAYARA_SERVER_4_1_144.getVersion()) {
            targetValueField.setText(""); // NOI18N
            targetValueField.setEnabled(false);
        } else {
            targetValueField.setText(""); // NOI18N
            targetValueField.setEnabled(true);
        }
        targetValueField.addKeyListener(kl);
        userNameField.addKeyListener(kl);
        passwordField.addKeyListener(kl);
    }
    
    /**
     * Retrieve DAS port value stored in form.
     * <p/>
     * @return DAS port value stored in form.
     */
    String getAdminPortValue() {
        return dasPortField.getText().trim();
    }
    
    /**
     * Set DAS port value of corresponding form field.
     * <p/>
     * @param port DAS port value to be set.
     */
    void setAdminPortValue(String port) {
        dasPortField.setText(port);
    }

    /**
     * Retrieve HTTP port value stored in form.
     * <p/>
     * @return HTTP port value stored in form.
     */
    String getHttpPortValue() {
        return httpPortField.getText().trim();
    }

    /**
     * Set HTTP port value of corresponding form field.
     * <p/>
     * @param port HTTP port value to be set.
     */
    void setHttpPortValue(String port) {
        httpPortField.setText(port);
    }

    String getTargetValue() {
        return targetValueField.getText().trim();
    }

    /**
     * Return administrator's user name value from text field.
     * <p/>
     * @return Administrator's user name value from text field.
     */
    String getUserNameValue() {
        return userNameField.getText().trim();
    }

    /**
     * Return administrator's password value from text field.
     * <p/>
     * @return Administrator's password value from text field.
     */
    String getPasswordValue() {
        return new String(passwordField.getPassword());
    }

    boolean getUseDefaultPorts() {
        return useDefaultPortsCB.isEnabled() && useDefaultPortsCB.isSelected();
    }

    /**
     * Get local host from form in local domain mode.
     * <p/>
     * @return Local host field value.
     */
    Object getLocalHost() {
        return hostLocalField.getEditor().getItem();
    }

    /**
     * Get remote host from form in remote domain mode.
     * <p/>
     * @return Remote host field value.
     */
    String getRemoteHost() {
        return hostRemoteField.getText().trim();
    }

    /**
     * Get local domain name from form in remote domain mode.
     * <p/>
     * @return Local domain name field value.
     */
    String getLocalDomain() {
        return (String)domainLocalField.getEditor().getItem();
    }

    /**
     * Get remote domain name from form in remote domain mode.
     * <p/>
     * @return Remote domain name field value.
     */
    String getRemoteDomain() {
        return domainRemoteField.getText().trim();
    }

    /**
     * Initialize server port with default value.
     * <p/>
     * @return Default administrator's user name value.
     */
    private String initAdminPortValue() {
        return Integer.toString(defaultDasPort);
    }

    /**
     * Initialize server port with default value.
     * <p/>
     * @return Default administrator's user name value.
     */
    private String initHttpPortValue() {
        return Integer.toString(defaultHttpPort);
    }

    /**
     * Initialize administrator's user name with default value.
     * <p/>
     * @return Default administrator's user name value.
     */
    private String initUserNameValue() {
        //return PayaraInstance.DEFAULT_ADMIN_NAME;
        return "";
    }

    /**
     * Initialize administrator's password with default value.
     * <p/>
     * @return Default administrator's password value.
     */
    private String initPasswordValue() {
        //return PayaraInstance.DEFAULT_ADMIN_PASSWORD;
        return "";
    }

    /**
     * 
     * @param l 
     */
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    /**
     * 
     * @param l 
     */
    public void removeChangeListener(ChangeListener l ) {
        listeners.remove(l);
    }

    private void fireChangeEvent() {
        ChangeEvent ev = new ChangeEvent(this);
        for(ChangeListener listener: listeners) {
            listener.stateChanged(ev);
        }
    }

    /**
     * Update content of host IPs combo box depending
     * on <code>Looopback</code> check box status.
     * <p/>
     * @param e A semantic event which indicates that a component-defined
     *          action occurred.
     */
    private void updateLocalIpsCombobox() {
        ((IpComboBox)hostLocalField).updateModel(ips, localIpCB.isSelected());
    }

    /**
     * Initialize content of port fields values depending
     * on <code>Default</code> check box status.
     */
    private void initPortsFields() {
        dasPortField.setText(initAdminPortValue());
        httpPortField.setText(initHttpPortValue());
        if (useDefaultPortsCB.isSelected()) {
            dasPortField.setEnabled(false);
            httpPortField.setEnabled(false);
        } else {
            dasPortField.setEnabled(true);
            httpPortField.setEnabled(true);
        }
    }

    /**
     * Update content of port fields values depending
     * on <code>Default</code> check box status.
     */
    private void updatePortsFields() {
        if (useDefaultPortsCB.isSelected()) {
            dasPortField.setText(initAdminPortValue());
            httpPortField.setText(initHttpPortValue());
            dasPortField.setEnabled(false);
            httpPortField.setEnabled(false);
        } else {
            dasPortField.setEnabled(true);
            httpPortField.setEnabled(true);
        }
    }

    /**
     * Update ports fields to contain valid values and handle default ports
     * check box depending on local domain availability.
     * <p/>
     * Default port values are updated to supplied values
     * of <code>dasPort</code> and <code>httpPort</code>.
     * <p/>
     * @param dasPort             DAS port to be set in form.
     * @param httpPort            HTTP port to be set in form.
     * @param localExistindDomain Is local domain available?
     */
    void setPortsFields(final int dasPort, final int httpPort,
            final boolean localExistindDomain) {
        if (localExistindDomain) {
            useDefaultPortsCB.setSelected(true);
            useDefaultPortsCB.setEnabled(false);
            if (dasPort >= 0) {
                defaultDasPort = dasPort;
            }
            if (httpPort >= 0) {
                defaultHttpPort = httpPort;
            }
        } else {
            useDefaultPortsCB.setEnabled(true);
            defaultDasPort = PayaraInstance.DEFAULT_ADMIN_PORT;
            defaultHttpPort = PayaraInstance.DEFAULT_HTTP_PORT;            
        }
        updatePortsFields();
    }

    /**
     * Update ports fields to contain valid values and handle default ports
     * check box depending on local domain availability.
     * <p/>
     * Default port values are not updated.
     * <p/>
     * @param localExistindDomain Is local domain available?
     */
    void setPortsFields(final boolean localExistindDomain) {
        setPortsFields(-1, -1, localExistindDomain);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked") // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        targetValueLabel = new javax.swing.JLabel();
        targetValueField = new javax.swing.JTextField();
        userNameLabel = new javax.swing.JLabel();
        userNameField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        dasPortFieldLabel = new javax.swing.JLabel();
        dasPortField = new javax.swing.JTextField();
        httpPortFieldLabel = new javax.swing.JLabel();
        httpPortField = new javax.swing.JTextField();
        useDefaultPortsCB = new javax.swing.JCheckBox();
        remotePanel = new javax.swing.JPanel();
        domainLocalLabel = new javax.swing.JLabel();
        domainLocalField = new javax.swing.JComboBox();
        hostLocalLabel = new javax.swing.JLabel();
        localIpCB = new javax.swing.JCheckBox();
        hostLocalField = new IpComboBox(ips, localIpCB.isSelected());
        hostRemoteField = new javax.swing.JTextField();
        hostRemoteLabel = new javax.swing.JLabel();
        domainRemoteLabel = new javax.swing.JLabel();
        domainRemoteField = new javax.swing.JTextField();
        remoteLink = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(438, 353));

        targetValueLabel.setLabelFor(dasPortField);
        org.openide.awt.Mnemonics.setLocalizedText(targetValueLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.targetValueLabel.text")); // NOI18N

        targetValueField.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.targetValueField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(userNameLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.userNameLabel.text")); // NOI18N

        userNameField.setText(initUserNameValue());

        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.passwordLabel.text")); // NOI18N

        passwordField.setText(initPasswordValue());

        dasPortFieldLabel.setLabelFor(dasPortField);
        org.openide.awt.Mnemonics.setLocalizedText(dasPortFieldLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.dasPortFieldLabel.text")); // NOI18N

        dasPortField.setColumns(5);
        dasPortField.setText(initAdminPortValue());

        httpPortFieldLabel.setLabelFor(httpPortField);
        httpPortFieldLabel.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.httpPortFieldLabel.text")); // NOI18N

        httpPortField.setColumns(5);
        httpPortField.setText(initHttpPortValue());

        org.openide.awt.Mnemonics.setLocalizedText(useDefaultPortsCB, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.useDefaultPortsCB.text")); // NOI18N
        useDefaultPortsCB.setToolTipText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.useDefaultPortsCB.toolTipText")); // NOI18N

        javax.swing.GroupLayout remotePanelLayout = new javax.swing.GroupLayout(remotePanel);
        remotePanel.setLayout(remotePanelLayout);
        remotePanelLayout.setHorizontalGroup(
            remotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        remotePanelLayout.setVerticalGroup(
            remotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 37, Short.MAX_VALUE)
        );

        domainLocalLabel.setLabelFor(domainLocalField);
        org.openide.awt.Mnemonics.setLocalizedText(domainLocalLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.domainLocalLabel.text")); // NOI18N

        domainLocalField.setEditable(true);

        hostLocalLabel.setLabelFor(hostLocalField);
        hostLocalLabel.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.hostLocalLabel.text")); // NOI18N
        hostLocalLabel.setMaximumSize(new java.awt.Dimension(62, 16));
        hostLocalLabel.setMinimumSize(new java.awt.Dimension(62, 16));
        hostLocalLabel.setPreferredSize(new java.awt.Dimension(62, 16));

        localIpCB.setSelected(true);
        localIpCB.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.localIpCB.text")); // NOI18N

        hostLocalField.setEditable(true);

        hostRemoteField.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.hostRemoteField.text")); // NOI18N
        hostRemoteField.setMaximumSize(new java.awt.Dimension(32767, 32767));

        hostRemoteLabel.setLabelFor(hostRemoteField);
        org.openide.awt.Mnemonics.setLocalizedText(hostRemoteLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.hostRemoteLabel.text")); // NOI18N
        hostRemoteLabel.setMaximumSize(new java.awt.Dimension(62, 16));
        hostRemoteLabel.setMinimumSize(new java.awt.Dimension(62, 16));
        hostRemoteLabel.setPreferredSize(new java.awt.Dimension(62, 16));

        domainRemoteLabel.setLabelFor(domainLocalField);
        org.openide.awt.Mnemonics.setLocalizedText(domainRemoteLabel, org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.domainRemoteLabel.text")); // NOI18N

        domainRemoteField.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.domainRemoteField.text")); // NOI18N
        domainRemoteField.setMaximumSize(new java.awt.Dimension(32767, 32767));

        remoteLink.setText(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.remoteLink.text")); // NOI18N
        remoteLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                remoteLinkMouseClicked(evt);
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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(domainLocalLabel)
                            .addComponent(domainRemoteLabel)
                            .addComponent(hostLocalLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(hostRemoteLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dasPortFieldLabel)
                            .addComponent(targetValueLabel)
                            .addComponent(userNameLabel)
                            .addComponent(passwordLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(hostLocalField, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(localIpCB))
                            .addComponent(domainRemoteField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(domainLocalField, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(hostRemoteField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(passwordField)
                                    .addComponent(userNameField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(targetValueField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(dasPortField, javax.swing.GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(httpPortFieldLabel)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(httpPortField, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(useDefaultPortsCB))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(112, 112, 112)
                                        .addComponent(remotePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
                    .addComponent(remoteLink)))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {dasPortFieldLabel, domainLocalLabel, domainRemoteLabel, hostLocalLabel, hostRemoteLabel, httpPortFieldLabel, passwordLabel, targetValueLabel, userNameLabel});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(domainLocalLabel)
                    .addComponent(domainLocalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(domainRemoteLabel)
                    .addComponent(domainRemoteField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(hostLocalLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hostLocalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(localIpCB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostRemoteLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hostRemoteField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dasPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(useDefaultPortsCB)
                    .addComponent(dasPortFieldLabel)
                    .addComponent(httpPortFieldLabel)
                    .addComponent(httpPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(targetValueLabel)
                    .addComponent(targetValueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userNameLabel)
                    .addComponent(userNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(passwordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(remoteLink, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
                .addComponent(remotePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        dasPortField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.dasPortField.AccessibleContext.accessibleDescription")); // NOI18N
        useDefaultPortsCB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.useDefaultPortsCB.AccessibleContext.accessibleDescription")); // NOI18N
        domainLocalField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.domainLocalField.AccessibleContext.accessibleDescription")); // NOI18N
        hostRemoteField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddDomainLocationVisualPanel.class, "AddDomainLocationVisualPanel.hostRemoteField.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void remoteLinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_remoteLinkMouseClicked
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(
                    new URL("http://wiki.netbeans.org/RemoteGlassFish4")); // NOI18N
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_remoteLinkMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField dasPortField;
    private javax.swing.JLabel dasPortFieldLabel;
    private javax.swing.JComboBox domainLocalField;
    private javax.swing.JLabel domainLocalLabel;
    private javax.swing.JTextField domainRemoteField;
    private javax.swing.JLabel domainRemoteLabel;
    private javax.swing.JComboBox hostLocalField;
    private javax.swing.JLabel hostLocalLabel;
    private javax.swing.JTextField hostRemoteField;
    private javax.swing.JLabel hostRemoteLabel;
    private javax.swing.JTextField httpPortField;
    private javax.swing.JLabel httpPortFieldLabel;
    private javax.swing.JCheckBox localIpCB;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JLabel remoteLink;
    private javax.swing.JPanel remotePanel;
    private javax.swing.JTextField targetValueField;
    private javax.swing.JLabel targetValueLabel;
    private javax.swing.JCheckBox useDefaultPortsCB;
    private javax.swing.JTextField userNameField;
    private javax.swing.JLabel userNameLabel;
    // End of variables declaration//GEN-END:variables

    class MyKeyListener implements KeyListener {
        @Override
            public void keyTyped(KeyEvent arg0) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

        @Override
            public void keyPressed(KeyEvent arg0) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

        @Override
            public void keyReleased(KeyEvent arg0) {
                //throw new UnsupportedOperationException("Not supported yet.");
                fireChangeEvent();
            }

    }
}
