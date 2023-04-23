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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.*;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.common.ServerUtil;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore.api.codegeneration.JmsDestinationDefinition;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Panel for specifying message destination for Send JMS Message action.
 * @author Tomas Mysik
 */
public class SendJmsMessagePanel extends javax.swing.JPanel implements ChangeListener {

    /** Name of default JMS connection factory which must be provided by every JavaEE7 complied server. */
    private static final String DEFAULT_JMS_CONNECTION_FACTORY = "java:comp/DefaultJMSConnectionFactory"; //NOI18N

    public static final String IS_VALID = SendJmsMessagePanel.class.getName() + ".IS_VALID";

    private final Project project;
    private final EjbJar ejbJar;
    private final J2eeModuleProvider provider;
    private final Set<MessageDestination> moduleDestinations;
    private final Set<MessageDestination> serverDestinations;
    private final boolean isDestinationCreationSupportedByServerPlugin;
    private final ServiceLocatorStrategyPanel slPanel;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private List<SendJMSMessageUiSupport.MdbHolder> mdbs;
    private String errorMsg;
    private String warningMsg;
    
    // private because correct initialization is needed
    private SendJmsMessagePanel(Project project, J2eeModuleProvider provider, Set<MessageDestination>
            moduleDestinations, Set<MessageDestination> serverDestinations, String lastLocator, ClasspathInfo cpInfo) {
        initComponents();

        this.project = project;
        this.provider = provider;
        this.moduleDestinations = moduleDestinations;
        this.serverDestinations = serverDestinations;
        this.ejbJar = EjbJar.getEjbJar(project.getProjectDirectory());
        // get MDBs with listening on model updates
        this.mdbs = SendJMSMessageUiSupport.getMdbs(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // get new MDBs
                mdbs = SendJMSMessageUiSupport.getMdbs(null);
                populate();
                verifyAndFire();
            }
        });
        changeSupport.addChangeListener(this);
        scanningLabel.setVisible(SourceUtils.isScanInProgress());

        isDestinationCreationSupportedByServerPlugin = provider.getConfigSupport().supportsCreateMessageDestination();
        slPanel = new ServiceLocatorStrategyPanel(lastLocator, cpInfo);
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                verifyAndFire();
            }
            @Override
            public void ancestorRemoved(AncestorEvent event) {
                verifyAndFire();
            }
            @Override
            public void ancestorMoved(AncestorEvent event) {
                verifyAndFire();
            }
        });
    }
    
    /**
     * Factory method for creating new instance.
     * @param provider Java EE module provider.
     * @param moduleDestinations project message destinations.
     * @param serverDestinations server message destinations.
     * @param lastLocator name of the service locator.
     * @return SendJmsMessagePanel instance.
     */
    public static SendJmsMessagePanel newInstance(final Project project, final J2eeModuleProvider provider,
            final Set<MessageDestination> moduleDestinations, final Set<MessageDestination> serverDestinations,
            final String lastLocator, ClasspathInfo cpInfo) {
        SendJmsMessagePanel sjmp = new SendJmsMessagePanel(
                project,
                provider,
                moduleDestinations,
                serverDestinations,
                lastLocator,
                cpInfo);
        sjmp.initialize();
        sjmp.verifyAndFire();
        sjmp.handleConnectionFactory();
        return sjmp;
    }

    /**
     * Get the message destination.
     * @return selected destination or <code>null</code> if no destination type is selected.
     */
    public MessageDestination getDestination() {
        if (projectDestinationsRadio.isSelected()) {
            if (projectDestinationsCombo.getSelectedItem() == null
                    || ((String) projectDestinationsCombo.getSelectedItem()).isEmpty()) {
                return null;
            } else {
                final String selectedDestination = (String) projectDestinationsCombo.getSelectedItem();
                for (MessageDestination messageDestination : moduleDestinations) {
                    if (messageDestination.getName().equals(selectedDestination)) {
                        // predefined project's message destinations
                        return messageDestination;
                    }
                }
                // message destination is unknown
                return new JmsDestinationDefinition(selectedDestination, Type.QUEUE, false);
            }
        } else if (serverDestinationsRadio.isSelected()) {
            return (MessageDestination) serverDestinationsCombo.getSelectedItem();
        }
        SendJMSMessageUiSupport.MdbHolder mdbHolder = (SendJMSMessageUiSupport.MdbHolder) mdbCombo.getSelectedItem();
        if (mdbHolder != null) {
            return mdbHolder.getMessageDestination();
        }
        return null;
    }

    public String getConnectionFactory() {
        return connectionFactoryTextField.getText();
    }
    
    /**
     * Get the service locator strategy.
     * @return the service locator strategy.
     */
    public String getServiceLocator() {
        return slPanel.classSelected();
    }

    /**
     * Return project holding MDB if MDB from some project is used, null otherwise.
     */
    public Project getMdbHolderProject() {
        if (mdbRadio.isSelected()) {
            SendJMSMessageUiSupport.MdbHolder mdbHolder = (SendJMSMessageUiSupport.MdbHolder) mdbCombo.getSelectedItem();
            return mdbHolder.getProject();
        }
        return null;
    }
    
    private void initialize() {
        registerListeners();
        setupProjectDestinationsOption();
        setupMessageDrivenOption();
        setupServiceLocatorPanel();
        handleComboBoxes();
        
        populate();
    }
    
    private void registerListeners() {
        // radio buttons
        projectDestinationsRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                verifyAndFire();
                handleComboBoxes();
            }
        });
        serverDestinationsRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                verifyAndFire();
                handleComboBoxes();
            }
        });
        mdbRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                verifyAndFire();
                handleComboBoxes();
            }
        });
        
        // combo boxes
        projectDestinationsCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                verifyAndFire();
            }
        });
        serverDestinationsCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                verifyAndFire();
            }
        });
        mdbCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                verifyAndFire();
                handleConnectionFactory();
            }
        });
        connectionFactoryTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                verifyAndFire();
            }
            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                verifyAndFire();
            }
            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                verifyAndFire();
            }
        });
    }
    
    private void setupProjectDestinationsOption() {
        if (isEjbOrEjbCapable()) {
            projectDestinationsRadio.setEnabled(true);
            setupAddButton();
            projectDestinationsRadio.setSelected(true);
        } else {
            projectDestinationsRadio.setEnabled(false);
            addButton.setEnabled(false);
            serverDestinationsRadio.setSelected(true);
        }
    }
    
    private void setupMessageDrivenOption() {
        mdbRadio.setEnabled(isEjbOrEjbCapable() || Utils.isPartOfJ2eeApp(provider));
    }

    private boolean isEjbOrEjbCapable() {
        return J2eeModule.Type.EJB.equals(provider.getJ2eeModule().getType())
                || (ejbJar != null
                    && ejbJar.getJ2eeProfile() != null
                    && ejbJar.getJ2eeProfile().isAtLeast(Profile.JAVA_EE_6_WEB));
    }
    
    private void setupAddButton() {
        if (!isDestinationCreationSupportedByServerPlugin) {
            // missing server?
            addButton.setEnabled(false);
        }
    }
   
    private void handleComboBoxes() {
        projectDestinationsCombo.setEnabled(projectDestinationsRadio.isSelected());
        serverDestinationsCombo.setEnabled(serverDestinationsRadio.isSelected());
        mdbCombo.setEnabled(mdbRadio.isSelected());
        destinationText.setEnabled(mdbRadio.isSelected());
        handleConnectionFactory();
    }
    
    private void handleConnectionFactory() {
        MessageDestination messageDestination = getDestination();
        if (ejbJar != null
                && ejbJar.getJ2eeProfile() != null
                && ejbJar.getJ2eeProfile().isAtLeast(Profile.JAVA_EE_7_WEB)) {
            // JavaEE7 specification - section EE.5.21.1
            // set the factory by default - message destination can be custom
            connectionFactoryTextField.setText(DEFAULT_JMS_CONNECTION_FACTORY);
        } else {
            if (messageDestination != null) {
                connectionFactoryTextField.setText(messageDestination.getName() + "Factory"); // NOI18N
            } else {
                connectionFactoryTextField.setText(null);
            }
        }
    }
    
    private void setupServiceLocatorPanel() {
        slPanel.addPropertyChangeListener(ServiceLocatorStrategyPanel.IS_VALID,
                new PropertyChangeListener() {
            @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        Object newvalue = evt.getNewValue();
                        if (newvalue instanceof Boolean) {
                            boolean isServiceLocatorOk = ((Boolean) newvalue);
                            if (isServiceLocatorOk) {
                                verifyAndFire();
                            } else {
                                firePropertyChange(IS_VALID, true, false);
                            }
                        }
                    }
                });
        serviceLocatorPanel.add(slPanel, BorderLayout.CENTER);
    }
    
    private void populate() {
        SendJMSMessageUiSupport.populateDestinations(moduleDestinations, projectDestinationsCombo, null);
        SendJMSMessageUiSupport.populateDestinations(serverDestinations, serverDestinationsCombo, null);
        SendJMSMessageUiSupport.populateMessageDrivenBeans(mdbs, mdbCombo, destinationText);
    }

    private boolean valid() {
        return verifyComponents() && slPanel.verifyComponents();
    }
    
    void verifyAndFire() {
        changeSupport.fireChange();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        // update scanning label visibility
        scanningLabel.setVisible(SourceUtils.isScanInProgress());

        boolean isValid = valid();
        firePropertyChange(IS_VALID, !isValid, isValid);
    }

    private void isValidServer() {
        // show warning if the project is missing any app server
        boolean isValid = ServerUtil.isValidServerInstance(provider);
        if (isValid) {
            warningMsg = null;
        } else {
            warningMsg = NbBundle.getMessage(SendJmsMessagePanel.class, "ERR_MissingServer"); //NOI18N
        }
    }
    
    private boolean verifyComponents() {
        // destination
        if (destinationGroup.getSelection() == null || getDestination() == null) {
            errorMsg = NbBundle.getMessage(SendJmsMessagePanel.class, "ERR_NoDestinationSelected"); //NOI18N
            return false;
        }
        
        if (getConnectionFactory().trim().length() < 1) {
            errorMsg = NbBundle.getMessage(SendJmsMessagePanel.class, "ERR_NoConnectionFactorySelected"); //NOI18N
            return false;
        }

        isValidServer();
        
        // no errors
        errorMsg = null;
        return true;
    }

    public String getWarningMessage() {
        return warningMsg;
    }

    public String getErrorMessage() {
        if (!verifyComponents()) {
            return errorMsg;
        } else if (!slPanel.verifyComponents()) {
            return slPanel.getErrorMessage();
        } else {
            return null;
        }
    }

    private static ComboBoxModel getProjectDestinationComboModel() {
        return new ProjectDestinationComboModel();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        destinationGroup = new javax.swing.ButtonGroup();
        projectDestinationsRadio = new javax.swing.JRadioButton();
        serverDestinationsRadio = new javax.swing.JRadioButton();
        projectDestinationsCombo = new javax.swing.JComboBox();
        serverDestinationsCombo = new javax.swing.JComboBox();
        addButton = new javax.swing.JButton();
        mdbRadio = new javax.swing.JRadioButton();
        mdbCombo = new javax.swing.JComboBox();
        serviceLocatorPanel = new javax.swing.JPanel();
        destinationLabel = new javax.swing.JLabel();
        destinationText = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        connectionFactoryTextField = new javax.swing.JTextField();
        scanningLabel = new javax.swing.JLabel();

        destinationGroup.add(projectDestinationsRadio);
        projectDestinationsRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(projectDestinationsRadio, org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "LBL_ProjectDestinations")); // NOI18N

        destinationGroup.add(serverDestinationsRadio);
        org.openide.awt.Mnemonics.setLocalizedText(serverDestinationsRadio, org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "LBL_ServerDestinations")); // NOI18N

        projectDestinationsCombo.setEditable(true);
        projectDestinationsCombo.setModel(getProjectDestinationComboModel());

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "LBL_Add")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        destinationGroup.add(mdbRadio);
        org.openide.awt.Mnemonics.setLocalizedText(mdbRadio, org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "LBL_MessageDrivenBean")); // NOI18N

        serviceLocatorPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(destinationLabel, org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "LBL_Destination")); // NOI18N

        destinationText.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "LBL_ConnectionFactory")); // NOI18N

        scanningLabel.setFont(new java.awt.Font("Dialog", 2, 12)); // NOI18N
        scanningLabel.setForeground(new java.awt.Color(0, 0, 0));
        org.openide.awt.Mnemonics.setLocalizedText(scanningLabel, org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "LBL_ScanningInProgress")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(serviceLocatorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 631, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(projectDestinationsRadio)
                                    .addComponent(serverDestinationsRadio)
                                    .addComponent(mdbRadio)
                                    .addComponent(destinationLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(destinationText, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                                    .addComponent(mdbCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, 392, Short.MAX_VALUE)
                                    .addComponent(serverDestinationsCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, 392, Short.MAX_VALUE)
                                    .addComponent(projectDestinationsCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, 392, Short.MAX_VALUE)
                                    .addComponent(connectionFactoryTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(scanningLabel)
                                .addGap(0, 334, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectDestinationsRadio)
                    .addComponent(addButton)
                    .addComponent(projectDestinationsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverDestinationsRadio)
                    .addComponent(serverDestinationsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mdbRadio)
                    .addComponent(mdbCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(destinationLabel)
                    .addComponent(destinationText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(connectionFactoryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(serviceLocatorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addComponent(scanningLabel))
        );

        projectDestinationsRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "ACSD_JMSProjectDestination")); // NOI18N
        serverDestinationsRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "ACSD_JMSServerDestination")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "ACSD_AddMessageDestination")); // NOI18N
        mdbRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "ACSD_JMSMessageDestination")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "ACSD_SendJMSMessage")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SendJmsMessagePanel.class, "ACSD_SendJMSMessage")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        MessageDestination destination =
                SendJMSMessageUiSupport.prepareMessageDestination(project, provider, moduleDestinations, serverDestinations);
        if (destination != null) {
            moduleDestinations.add(destination);
            SendJMSMessageUiSupport.populateDestinations(moduleDestinations, projectDestinationsCombo, destination);
        }
    }//GEN-LAST:event_addButtonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JTextField connectionFactoryTextField;
    private javax.swing.ButtonGroup destinationGroup;
    private javax.swing.JLabel destinationLabel;
    private javax.swing.JTextField destinationText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JComboBox mdbCombo;
    private javax.swing.JRadioButton mdbRadio;
    private javax.swing.JComboBox projectDestinationsCombo;
    private javax.swing.JRadioButton projectDestinationsRadio;
    private javax.swing.JLabel scanningLabel;
    private javax.swing.JComboBox serverDestinationsCombo;
    private javax.swing.JRadioButton serverDestinationsRadio;
    private javax.swing.JPanel serviceLocatorPanel;
    // End of variables declaration//GEN-END:variables

    @SuppressWarnings("serial") // not used to be serialized
    public static class ProjectDestinationComboModel extends DefaultComboBoxModel {

        @Override
        public Object getSelectedItem() {
            Object selectedItem = super.getSelectedItem();
            if (selectedItem instanceof MessageDestination) {
                return ((MessageDestination) selectedItem).getName();
            } else {
                return selectedItem;
            }
        }

    }
}
