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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.javaee.specs.support.api.util.JndiNamespacesDefinition;
import org.openide.NotificationLineSupport;
import org.openide.util.NbBundle;

/**
 * Panel for adding message destination.
 * @author Tomas Mysik
 */
public class MessageDestinationPanel extends javax.swing.JPanel {
    
    public static final String IS_VALID = MessageDestinationPanel.class.getName() + ".IS_VALID";
    
    // map because of faster searching
    private final Map<String, MessageDestination.Type> destinationMap;
    private final boolean generated;
    private final String prefix;
    private NotificationLineSupport statusLine;
    
    // private because correct initialization is needed
    private MessageDestinationPanel(Map<String, MessageDestination.Type> destinationMap,
            boolean generated, String prefix) {
        initComponents();
        this.destinationMap = destinationMap;
        this.generated = generated;
        this.prefix = prefix;
    }
    
    /**
     * Factory method for creating new instance.
     * @param destinationMap the names and the types of project message destinations.
     * @return MessageDestinationPanel instance.
     */
    public static MessageDestinationPanel newInstance(final Map<String, MessageDestination.Type> destinationMap,
            boolean generated, String prefix) {
        MessageDestinationPanel mdp = new MessageDestinationPanel(destinationMap, generated, prefix);
        mdp.initialize();
        return mdp;
    }

    public void setNotificationLine(NotificationLineSupport statusLine) {
        this.statusLine = statusLine;
    }

    /**
     * Get the name of the message destination.
     * @return message destination name.
     */
    public String getDestinationName() {
        String name = destinationNameText.getText().trim();
        if (generated) {
            return JndiNamespacesDefinition.normalize(name, prefix);
        }
        return name;
    }
    
    /**
     * Get the type of the message destination.
     * @return message destination type.
     * @see MessageDestination.Type
     */
    public MessageDestination.Type getDestinationType() {
        if (queueTypeRadio.isSelected()) {
            return MessageDestination.Type.QUEUE;
        }
        return MessageDestination.Type.TOPIC;
    }
    
    private void initialize() {
        registerListeners();
        verifyAndFire();
    }
    
    private void registerListeners() {
        // text field
        destinationNameText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent event) {
                verifyAndFire();
            }
            public void removeUpdate(DocumentEvent event) {
                verifyAndFire();
            }
            public void changedUpdate(DocumentEvent event) {
                verifyAndFire();
            }
        });
        
        // radio buttons
        queueTypeRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                verifyAndFire();
            }
        });
        topicTypeRadio.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                verifyAndFire();
            }
        });

        addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                verifyAndFire();
            }
            public void ancestorRemoved(AncestorEvent event) {
                verifyAndFire();
            }
            public void ancestorMoved(AncestorEvent event) {
                verifyAndFire();
            }
        });
    }
    
    private void setError(String key) {
        if (statusLine != null) {
            statusLine.setErrorMessage(NbBundle.getMessage(MessageDestinationPanel.class, key));
        }
    }
    
    private void setInfo(String key) {
        if (statusLine != null) {
            statusLine.setInformationMessage(NbBundle.getMessage(MessageDestinationPanel.class, key));
        }
    }

    private void verifyAndFire() {
        boolean isValid = verifyComponents();
        firePropertyChange(IS_VALID, !isValid, isValid);
    }
    
    private boolean verifyComponents() {
        // destination name - form & duplicity
        String destinationName = destinationNameText.getText();
        if (destinationName == null || destinationName.trim().length() == 0) {
            setInfo("ERR_NoDestinationName"); // NOI18N
            return false;
        } else {
            destinationName = destinationName.trim();
            if (generated) {
                destinationName = JndiNamespacesDefinition.normalize(destinationName, prefix);
            }
            MessageDestination.Type type = destinationMap.get(destinationName);
            if (type != null && type.equals(getDestinationType())) {
                setError("ERR_DuplicateDestination"); // NOI18N
                return false;
            }
        }
        
        // destination type (radio)
        if (destinationTypeGroup.getSelection() == null) {
            setInfo("ERR_NoDestinationType"); // NOI18N
            return false;
        }
        
        // no errors
        statusLine.clearMessages();
        return true;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        destinationTypeGroup = new javax.swing.ButtonGroup();
        destinationNameLabel = new javax.swing.JLabel();
        destinationNameText = new javax.swing.JTextField();
        destinationTypeLabel = new javax.swing.JLabel();
        queueTypeRadio = new javax.swing.JRadioButton();
        topicTypeRadio = new javax.swing.JRadioButton();

        destinationNameLabel.setLabelFor(destinationNameText);
        org.openide.awt.Mnemonics.setLocalizedText(destinationNameLabel, org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "LBL_DestinationName")); // NOI18N

        destinationTypeLabel.setLabelFor(queueTypeRadio);
        org.openide.awt.Mnemonics.setLocalizedText(destinationTypeLabel, org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "LBL_DestinationType")); // NOI18N

        destinationTypeGroup.add(queueTypeRadio);
        queueTypeRadio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(queueTypeRadio, org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "LBL_Queue")); // NOI18N
        queueTypeRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        queueTypeRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        destinationTypeGroup.add(topicTypeRadio);
        org.openide.awt.Mnemonics.setLocalizedText(topicTypeRadio, org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "LBL_Topic")); // NOI18N
        topicTypeRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        topicTypeRadio.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(destinationTypeLabel)
                    .addComponent(destinationNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(topicTypeRadio)
                    .addComponent(queueTypeRadio)
                    .addComponent(destinationNameText, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(destinationNameLabel)
                    .addComponent(destinationNameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(destinationTypeLabel)
                    .addComponent(queueTypeRadio))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(topicTypeRadio)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        destinationNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "MessageDestinationPanel.destinationNameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        queueTypeRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "MessageDestinationPanel.queueTypeRadio.AccessibleContext.accessibleDescription")); // NOI18N
        topicTypeRadio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "MessageDestinationPanel.topicTypeRadio.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "ACSD_AddMessageDestination")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MessageDestinationPanel.class, "ACSD_AddMessageDestination")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel destinationNameLabel;
    private javax.swing.JTextField destinationNameText;
    private javax.swing.ButtonGroup destinationTypeGroup;
    private javax.swing.JLabel destinationTypeLabel;
    private javax.swing.JRadioButton queueTypeRadio;
    private javax.swing.JRadioButton topicTypeRadio;
    // End of variables declaration//GEN-END:variables
    
}
