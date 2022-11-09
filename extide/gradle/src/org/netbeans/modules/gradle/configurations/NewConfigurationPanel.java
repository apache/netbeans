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
package org.netbeans.modules.gradle.configurations;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.EditorKit;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.modules.gradle.customizer.BuildActionsCustomizer;
import org.openide.NotificationLineSupport;
import org.openide.filesystems.FileUtil;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public class NewConfigurationPanel extends javax.swing.JPanel implements DocumentListener {

    private final Supplier<Collection<? extends GradleExecConfiguration>> configProvider;
    private final Project project;
    
    private NotificationLineSupport notifications;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public NewConfigurationPanel(Project project, Supplier<Collection<? extends GradleExecConfiguration>> confProvider, boolean isNew) {
        this.project = project;
        this.configProvider = confProvider;

        initComponents();
        EditorKit kit = CloneableEditorSupport.getEditorKit("text/x-gradle-cli"); //NOI18N
        txParameters.setEditorKit(kit);
        txParameters.getDocument().putProperty(BuildActionsCustomizer.GRADLE_PROJECT_PROPERTY, project);
        if (isNew) {
            txId.getDocument().addDocumentListener(this);
            txId.getDocument().addDocumentListener(this);
        } else {
            txId.setEditable(false);
            txId.setEnabled(false);
        }
    }

    public void setDisplayName(String dn) {
        if (dn == null) {
            txLabel.setText("");
        } else {
            txLabel.setText(dn);
        }
    }

    public void setNotifications(NotificationLineSupport notifications) {
        this.notifications = notifications;
        check();
    }
    
    public String getConfigurationId() {
        return txId.getText().trim();
    }
    
    public String getLabel() {
        String l = txLabel.getText().trim();
        return l.isEmpty() ? null : l;
    }

    public void setConfigurationId(String configurationId) {
        txId.setText(configurationId);
    }

    public boolean isShared() {
        return !cbPrivate.isSelected();
    }

    public void setShared(boolean shared) {
        cbPrivate.setSelected(!shared);
    }
    
    public void setParameters(String parameters) {
        txParameters.setText(parameters);
    }

    public String getParameters() {
        String s = txParameters.getText().trim();
        return s.isEmpty() ? null : s;
    }
    
    public void setProfiles(List<String> profiles) {
        String val = ""; //NOI18N
        if (profiles != null) {
            for (String prf : profiles) {
                val = val + prf + " ";
            }
        }
    }

    public void setProperties(Map<String, String> initProps) {
        String content;
        if (initProps == null || initProps.isEmpty()) {
            content = "";
        } else {
            content = String.join("\n", 
                initProps.entrySet().stream().map(e -> 
                        e.getKey() + "=" + (e.getValue() == null ? "" : e.getValue())
                ).collect(Collectors.toList()));
        }
        txProperties.setText(content);
    }
    
    public Map<String, String> getProperties() {
        Map<String, String> propList = new LinkedHashMap<>();
        String[] lines = txProperties.getText().trim().split("\n");
        for (String l : lines) {
            int eq = l.indexOf('=');
            String k;
            String v;
            if (eq == -1) {
                k = l.trim();
                v = "";
            } else {
                k = l.substring(0, eq).trim();
                v = l.substring(eq + 1);
            }
            if (!k.isEmpty()) {
                propList.put(k, v);
            }
        }
        return propList.isEmpty() ? null : propList;
    }
    
    private boolean valid;

    public boolean isPanelValid() {
        return valid;
    }
    
    private void setPanelValid(boolean valid) {
        if (this.valid == valid) {
            return;
        }
        this.valid = valid;
        firePropertyChange("panelValid", !valid, valid);
    }
    
    @NbBundle.Messages({
        "ERR_ConfigurationIdEmpty=Configuration ID cannot be empty.",
        "ERR_ConfigurationIInvalid=Configuration ID contains invalid character(s)",
        "# {0} - configuration ID string",
        "ERR_ConfigurationIExists=The ID {0} is already taken."
    })
    private void check() {
        if (notifications == null) {
            return;
        }
        if (txId.isEditable()) {
            String id = txId.getText().trim();
            // configuration may be used as a part of filename.
            if (id.isEmpty()) {
                notifications.setErrorMessage(Bundle.ERR_ConfigurationIdEmpty());
                setPanelValid(false);
                return;
            }
            if (!FileUtil.isValidFileName(id)) {
                notifications.setErrorMessage(Bundle.ERR_ConfigurationIInvalid());
                setPanelValid(false);
                return;
            }
            Optional<? extends GradleExecConfiguration> present = configProvider.get().stream().filter(c -> id.equalsIgnoreCase(c.getId())).findAny();
            if (present.isPresent()) {
                notifications.setErrorMessage(Bundle.ERR_ConfigurationIExists(present.get().getId()));
                setPanelValid(false);
                return;
            }
        }
        notifications.clearMessages();
        setPanelValid(true);
    }
    
    @Override
    public void insertUpdate(DocumentEvent e) {
        check();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        check();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        check();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txId = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txLabel = new javax.swing.JTextField();
        cbPrivate = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        txProperties = new javax.swing.JEditorPane();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txParameters = new javax.swing.JEditorPane();

        txId.setText(org.openide.util.NbBundle.getMessage(NewConfigurationPanel.class, "NewConfigurationPanel.txId.text")); // NOI18N

        jLabel1.setLabelFor(txId);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(NewConfigurationPanel.class, "NewConfigurationPanel.jLabel1.text")); // NOI18N

        jLabel2.setLabelFor(txLabel);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(NewConfigurationPanel.class, "NewConfigurationPanel.jLabel2.text")); // NOI18N

        txLabel.setText(org.openide.util.NbBundle.getMessage(NewConfigurationPanel.class, "NewConfigurationPanel.txLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbPrivate, org.openide.util.NbBundle.getMessage(NewConfigurationPanel.class, "NewConfigurationPanel.cbPrivate.text")); // NOI18N

        txProperties.setContentType("text/x-properties"); // NOI18N
        jScrollPane1.setViewportView(txProperties);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(NewConfigurationPanel.class, "NewConfigurationPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(NewConfigurationPanel.class, "NewConfigurationPanel.jLabel4.text")); // NOI18N

        jScrollPane3.setViewportView(txParameters);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(txId)
                    .addComponent(txLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbPrivate)
                        .addGap(0, 103, Short.MAX_VALUE))
                    .addComponent(jScrollPane3))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbPrivate)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(44, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbPrivate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField txId;
    private javax.swing.JTextField txLabel;
    private javax.swing.JEditorPane txParameters;
    private javax.swing.JEditorPane txProperties;
    // End of variables declaration//GEN-END:variables
}
