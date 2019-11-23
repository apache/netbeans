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
package org.netbeans.modules.docker.ui.wizard;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.docker.ui.UiUtils;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Petr Hejl
 */
public class ConfigurationLinuxPanel extends javax.swing.JPanel implements Configuration {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    /**
     * Creates new form DockerConnectionLinux
     */
    public ConfigurationLinuxPanel() {
        initComponents();

        DefaultDocumentListener listener = new DefaultDocumentListener();
        nameTextField.getDocument().addDocumentListener(listener);
        socketTextField.getDocument().addDocumentListener(listener);
        urlTextField.getDocument().addDocumentListener(listener);
        certTextField.getDocument().addDocumentListener(listener);
    }

    @Override
    public void setInputEnabled(boolean enabled) {
        nameTextField.setEnabled(enabled);
        socketRadioButton.setEnabled(enabled);
        urlRadioButton.setEnabled(enabled);

        if (!enabled) {
            socketTextField.setEnabled(enabled);
            socketBrowseButton.setEnabled(enabled);
            urlTextField.setEnabled(enabled);
            certTextField.setEnabled(enabled);
            certBrowseButton.setEnabled(enabled);
        } else {
            boolean socketSelected = socketRadioButton.isSelected();
            socketTextField.setEnabled(socketSelected);
            socketBrowseButton.setEnabled(socketSelected);
            urlTextField.setEnabled(!socketSelected);
            certTextField.setEnabled(!socketSelected);
            certBrowseButton.setEnabled(!socketSelected);
        }
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public String getDisplayName() {
        return UiUtils.getValue(nameTextField);
    }

    @Override
    public void setDisplayName(String displayName) {
        nameTextField.setText(displayName);
    }

    @Override
    public boolean isSocketSelected() {
        return socketRadioButton.isSelected();
    }

    @Override
    public void setSocketSelected(boolean socketSelected) {
        socketRadioButton.setSelected(socketSelected);
        refresh();
    }

    @Override
    public File getSocket() {
        String value = UiUtils.getValue(socketTextField);
        if (value != null) {
            return new File(value);
        }
        return null;
    }

    @Override
    public void setSocket(File socket) {
        if (socket != null) {
            socketTextField.setText(socket.getAbsolutePath());
        }
    }

    @Override
    public String getUrl() {
        return UiUtils.getValue(urlTextField);
    }

    @Override
    public void setUrl(String url) {
        urlTextField.setText(url);
    }

    @Override
    public String getCertPath() {
        return UiUtils.getValue(certTextField);
    }

    @Override
    public void setCertPath(String path) {
        certTextField.setText(path);
    }

    private void refresh() {
        boolean socketSelected = socketRadioButton.isSelected();
        socketTextField.setEnabled(socketSelected);
        socketBrowseButton.setEnabled(socketSelected);
        urlTextField.setEnabled(!socketSelected);
        certTextField.setEnabled(!socketSelected);
        certBrowseButton.setEnabled(!socketSelected);

        changeSupport.fireChange();
    }

    private class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            changeSupport.fireChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changeSupport.fireChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            changeSupport.fireChange();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        switchButtonGroup = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        urlLabel = new javax.swing.JLabel();
        urlTextField = new javax.swing.JTextField();
        certDirectoryLabel = new javax.swing.JLabel();
        certTextField = new javax.swing.JTextField();
        certBrowseButton = new javax.swing.JButton();
        socketRadioButton = new javax.swing.JRadioButton();
        socketLabel = new javax.swing.JLabel();
        socketBrowseButton = new javax.swing.JButton();
        socketTextField = new javax.swing.JTextField();
        urlRadioButton = new javax.swing.JRadioButton();

        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(ConfigurationLinuxPanel.class, "ConfigurationLinuxPanel.nameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, org.openide.util.NbBundle.getMessage(ConfigurationLinuxPanel.class, "ConfigurationLinuxPanel.urlLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(certDirectoryLabel, org.openide.util.NbBundle.getMessage(ConfigurationLinuxPanel.class, "ConfigurationLinuxPanel.certDirectoryLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(certBrowseButton, org.openide.util.NbBundle.getMessage(ConfigurationLinuxPanel.class, "ConfigurationLinuxPanel.certBrowseButton.text")); // NOI18N
        certBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                certBrowseButtonActionPerformed(evt);
            }
        });

        switchButtonGroup.add(socketRadioButton);
        socketRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(socketRadioButton, org.openide.util.NbBundle.getMessage(ConfigurationLinuxPanel.class, "ConfigurationLinuxPanel.socketRadioButton.text")); // NOI18N
        socketRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                socketRadioButtonActionPerformed(evt);
            }
        });

        socketLabel.setLabelFor(socketTextField);
        org.openide.awt.Mnemonics.setLocalizedText(socketLabel, org.openide.util.NbBundle.getMessage(ConfigurationLinuxPanel.class, "ConfigurationLinuxPanel.socketLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(socketBrowseButton, org.openide.util.NbBundle.getMessage(ConfigurationLinuxPanel.class, "ConfigurationLinuxPanel.socketBrowseButton.text")); // NOI18N
        socketBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                socketBrowseButtonActionPerformed(evt);
            }
        });

        switchButtonGroup.add(urlRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(urlRadioButton, org.openide.util.NbBundle.getMessage(ConfigurationLinuxPanel.class, "ConfigurationLinuxPanel.urlRadioButton.text")); // NOI18N
        urlRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                urlRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(nameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameTextField))
            .addGroup(layout.createSequentialGroup()
                .addComponent(socketRadioButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(urlRadioButton)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(socketLabel)
                    .addComponent(urlLabel)
                    .addComponent(certDirectoryLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(socketTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(socketBrowseButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(certTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(certBrowseButton))
                    .addComponent(urlTextField)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(socketRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(socketLabel)
                    .addComponent(socketTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(socketBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(urlRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(urlLabel)
                    .addComponent(urlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(certDirectoryLabel)
                    .addComponent(certTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(certBrowseButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void certBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_certBrowseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileHidingEnabled(false);
        String text = UiUtils.getValue(certTextField);
        if (text != null) {
            chooser.setSelectedFile(new File(text));
        }
        if (chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            certTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_certBrowseButtonActionPerformed

    private void socketBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_socketBrowseButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileHidingEnabled(false);
        String text = UiUtils.getValue(socketTextField);
        if (text != null) {
            chooser.setSelectedFile(new File(text));
        }
        if (chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this)) == JFileChooser.APPROVE_OPTION) {
            socketTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_socketBrowseButtonActionPerformed

    private void socketRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_socketRadioButtonActionPerformed
        refresh();
    }//GEN-LAST:event_socketRadioButtonActionPerformed

    private void urlRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_urlRadioButtonActionPerformed
        refresh();
    }//GEN-LAST:event_urlRadioButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton certBrowseButton;
    private javax.swing.JLabel certDirectoryLabel;
    private javax.swing.JTextField certTextField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton socketBrowseButton;
    private javax.swing.JLabel socketLabel;
    private javax.swing.JRadioButton socketRadioButton;
    private javax.swing.JTextField socketTextField;
    private javax.swing.ButtonGroup switchButtonGroup;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JRadioButton urlRadioButton;
    private javax.swing.JTextField urlTextField;
    // End of variables declaration//GEN-END:variables
}
