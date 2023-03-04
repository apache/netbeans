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

package org.netbeans.modules.javawebstart.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Signing configuration panel.
 *
 * @author Maros Sandor
 */
public class SigningPanel extends javax.swing.JPanel implements ActionListener, DocumentListener {
    
    private final JWSProjectProperties props;
    private DialogDescriptor desc;

    /** Creates new form SigningPanel */
    public SigningPanel(JWSProjectProperties props) {
        this.props = props;
        initComponents();

        if (JWSProjectProperties.SIGNING_KEY.equals(props.signing)) {
            keySign.setSelected(true);
        } else if (JWSProjectProperties.SIGNING_GENERATED.equals(props.signing)) {
            selfSign.setSelected(true);
        } else {
            noSign.setSelected(true);
        }
        path.setText(props.signingKeyStore);
        key.setText(props.signingKeyAlias);
        if (props.signingKeyStorePassword != null) password.setText(new String(props.signingKeyStorePassword));
        if (props.signingKeyPassword != null) keyPass.setText(new String(props.signingKeyPassword));

        mixedCodeCombo.setModel(props.mixedCodeModel);

        refreshComponents();
    }

    void setDialogDescriptor(DialogDescriptor desc) {
        this.desc = desc;
        updateDialogButtonsAndMessage();
    }
    
    void registerListeners() {
        path.getDocument().addDocumentListener(this);
        password.getDocument().addDocumentListener(this);
        key.getDocument().addDocumentListener(this);
    }
    
    void unregisterListeners() {
        path.getDocument().removeDocumentListener(this);
        password.getDocument().removeDocumentListener(this);
        key.getDocument().removeDocumentListener(this);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateDialogButtonsAndMessage();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateDialogButtonsAndMessage();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        updateDialogButtonsAndMessage();
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        keySign.addActionListener(this);
        selfSign.addActionListener(this);
        noSign.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        refreshComponents();
        updateDialogButtonsAndMessage();
    }

    private void refreshComponents() {
        path.setEnabled(keySign.isSelected());
        browse.setEnabled(keySign.isSelected());
        password.setEnabled(keySign.isSelected());
        key.setEnabled(keySign.isSelected());
        keyPass.setEnabled(keySign.isSelected());
        jLabel1.setEnabled(keySign.isSelected());
        jLabel2.setEnabled(keySign.isSelected());
        jLabel3.setEnabled(keySign.isSelected());
        jLabel4.setEnabled(keySign.isSelected());
        mixedCodeCombo.setEnabled(!noSign.isSelected());
        jLabel5.setEnabled(!noSign.isSelected());
        if(keySign.isSelected()) {
            labelWarning.setText(null);
        } else {
            labelWarning.setText(NbBundle.getMessage(JWSCustomizerPanel.class, "SigningPanel.WarnDeprecated")); //NOI18N
        }
    }

    void store() {
        if (noSign.isSelected()) {
            props.signing = "";
        } else if (selfSign.isSelected()) {
            props.signing = JWSProjectProperties.SIGNING_GENERATED;
        } else {
            props.signing = JWSProjectProperties.SIGNING_KEY;
            props.signingKeyStorePassword = password.getPassword();
            props.signingKeyPassword = keyPass.getPassword();
            props.signingKeyStore = path.getText().trim();
            props.signingKeyAlias = key.getText().trim();
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

        bg = new javax.swing.ButtonGroup();
        selfSign = new javax.swing.JRadioButton();
        keySign = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        path = new javax.swing.JTextField();
        browse = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        password = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        key = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        keyPass = new javax.swing.JPasswordField();
        noSign = new javax.swing.JRadioButton();
        mixedCodeCombo = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        labelWarning = new javax.swing.JLabel();

        bg.add(selfSign);
        org.openide.awt.Mnemonics.setLocalizedText(selfSign, org.openide.util.NbBundle.getMessage(SigningPanel.class, "SigningPanel.selfSign.text")); // NOI18N

        bg.add(keySign);
        org.openide.awt.Mnemonics.setLocalizedText(keySign, org.openide.util.NbBundle.getMessage(SigningPanel.class, "SigningPanel.keySign.text")); // NOI18N

        jLabel1.setLabelFor(path);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SigningPanel.class, "SigningPanel.jLabel1.text")); // NOI18N

        path.setText(org.openide.util.NbBundle.getMessage(SigningPanel.class, "SigningPanel.path.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browse, org.openide.util.NbBundle.getMessage(SigningPanel.class, "SigningPanel.browse.text")); // NOI18N
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });

        jLabel2.setLabelFor(password);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SigningPanel.class, "SigningPanel.jLabel2.text")); // NOI18N

        password.setText(org.openide.util.NbBundle.getMessage(SigningPanel.class, "SigningPanel.password.text")); // NOI18N

        jLabel3.setLabelFor(key);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SigningPanel.class, "SigningPanel.jLabel3.text")); // NOI18N

        key.setText(org.openide.util.NbBundle.getMessage(SigningPanel.class, "SigningPanel.key.text")); // NOI18N

        jLabel4.setLabelFor(keyPass);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SigningPanel.class, "SigningPanel.jLabel4.text")); // NOI18N

        keyPass.setText(org.openide.util.NbBundle.getMessage(SigningPanel.class, "SigningPanel.keyPass.text")); // NOI18N

        bg.add(noSign);
        org.openide.awt.Mnemonics.setLocalizedText(noSign, org.openide.util.NbBundle.getMessage(SigningPanel.class, "SigningPanel.noSign.text")); // NOI18N

        jLabel5.setLabelFor(mixedCodeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(SigningPanel.class, "SigningPanel.jLabel5.text")); // NOI18N
        jLabel5.setToolTipText(org.openide.util.NbBundle.getMessage(SigningPanel.class, "SigningPanel.jLabel5.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelWarning, org.openide.util.NbBundle.getMessage(SigningPanel.class, "SigningPanel.labelWarning.text")); // NOI18N
        labelWarning.setPreferredSize(new java.awt.Dimension(300, 60));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(mixedCodeCombo, 0, 391, Short.MAX_VALUE)
                                    .addComponent(keyPass, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                                    .addComponent(key, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                                    .addComponent(password, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                                    .addComponent(path, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(browse))
                            .addComponent(labelWarning, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(noSign)
                            .addComponent(selfSign)
                            .addComponent(keySign))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(noSign)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selfSign)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(keySign)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(path, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(key, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(keyPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(mixedCodeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addComponent(labelWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3))
        );

        selfSign.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SigningPanel.class, "AD_SigningPanel.selfSign.text")); // NOI18N
        keySign.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SigningPanel.class, "AD_SigningPanel.keySign.text")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SigningPanel.class, "AD_SigningPanel.jLabel1.text")); // NOI18N
        browse.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SigningPanel.class, "AD_SigningPanel.browse.text")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SigningPanel.class, "AD_SigningPanel.jLabel2.text")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SigningPanel.class, "AD_SigningPanel.jLabel3.text")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SigningPanel.class, "AD_SigningPanel.jLabel4.text")); // NOI18N
        noSign.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SigningPanel.class, "AD_SigningPanel.noSign.text")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SigningPanel.class, "AD_SigningPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle(NbBundle.getMessage(JWSCustomizerPanel.class, "TITLE_KeystoreBrowser")); //NOI18N
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File file = FileUtil.normalizeFile(chooser.getSelectedFile());
            path.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_browseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.ButtonGroup bg;
    javax.swing.JButton browse;
    javax.swing.JLabel jLabel1;
    javax.swing.JLabel jLabel2;
    javax.swing.JLabel jLabel3;
    javax.swing.JLabel jLabel4;
    javax.swing.JLabel jLabel5;
    javax.swing.JTextField key;
    javax.swing.JPasswordField keyPass;
    javax.swing.JRadioButton keySign;
    javax.swing.JLabel labelWarning;
    javax.swing.JComboBox mixedCodeCombo;
    javax.swing.JRadioButton noSign;
    javax.swing.JPasswordField password;
    javax.swing.JTextField path;
    javax.swing.JRadioButton selfSign;
    // End of variables declaration//GEN-END:variables

    private void updateDialogButtonsAndMessage() {
        if(!keySign.isSelected() || (password.getDocument().getLength()>0 && key.getDocument().getLength()>0)) {
           desc.setValid(true);
           if(keySign.isSelected()) {
               if(path.getDocument().getLength()>0) {
                   labelWarning.setText(null);
               } else {
                   labelWarning.setText(NbBundle.getMessage(JWSCustomizerPanel.class, "SigningPanel.InfoDefaultPath")); //NOI18N
               }
           }
        } else {
           desc.setValid(false);
           labelWarning.setText(NbBundle.getMessage(JWSCustomizerPanel.class, "SigningPanel.WarnMissingInfo")); //NOI18N
        }
    }

}
