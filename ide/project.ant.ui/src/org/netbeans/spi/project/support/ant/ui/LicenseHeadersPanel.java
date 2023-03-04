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
package org.netbeans.spi.project.support.ant.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import static org.netbeans.spi.project.support.ant.ui.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author mkleint
 */
class LicenseHeadersPanel extends javax.swing.JPanel {

    private static final String defaultProjectLicense = 
"<#if licenseFirst??>\n" +
"${licenseFirst}\n" +
"</#if>\n" +
"${licensePrefix}Here comes the text of your license\n" +
"${licensePrefix}Each line should be prefixed with ${licensePrefix}\n" +
"<#if licenseLast??>\n" +
"${licenseLast}\n" +
"</#if>";
    private final CustomizerUtilities.LicensePanelContentHandler handler;
    private final ProjectCustomizer.Category category;
    private final FileChangeAdapter fslistener;
    private final DocumentListener editorListener;
    /**
     * Creates new form LicenseHeadersPanel
     */
    LicenseHeadersPanel(ProjectCustomizer.Category category, CustomizerUtilities.LicensePanelContentHandler handler) {
        this.handler = handler;
        this.category = category;
        
        initComponents();
        
        btnProject.setVisible(false); //how to implement browse??
        
        
        editorListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                LicenseHeadersPanel.this.handler.setProjectLicenseContent(epLicense.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                LicenseHeadersPanel.this.handler.setProjectLicenseContent(epLicense.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                LicenseHeadersPanel.this.handler.setProjectLicenseContent(epLicense.getText());
            }
        };
        
        loadGlobalLicenses();
        initValues();
        txtProject.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                LicenseHeadersPanel.this.handler.setProjectLicenseLocation(txtProject.getText().trim());
                setTextToProjectLicense();
            }
        });
        
        rbGlobal.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                txtProject.setEnabled(false);
                epLicense.setEditable(false);
                btnProject.setEnabled(false);
                comGlobal.setEnabled(true);
                LicenseHeadersPanel.this.category.setErrorMessage(null);
                epLicense.getDocument().removeDocumentListener(editorListener);
                LicenseHeadersPanel.this.handler.setProjectLicenseContent(null);
                setTextToGlobalLicense();
                LicenseHeadersPanel.this.handler.setProjectLicenseLocation(null);
            }
        });
        rbProject.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                txtProject.setEnabled(true);
                epLicense.setEditable(true);
                btnProject.setEnabled(true);
                comGlobal.setEnabled(false);
                epLicense.getDocument().addDocumentListener(editorListener);
                
                LicenseHeadersPanel.this.handler.setProjectLicenseLocation(txtProject.getText().trim());
                setTextToProjectLicense();
            }

        });
        comGlobal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                setTextToGlobalLicense();
                GlobalItem item = (GlobalItem) comGlobal.getSelectedItem();
                LicenseHeadersPanel.this.handler.setGlobalLicenseName(item.getName());
            }

        });
        FileObject root = FileUtil.getConfigFile("Templates/Licenses");
        fslistener = new FileChangeAdapter() {
            @Override
            public void fileDataCreated(FileEvent fe) {
                reloadGlobalTemplatesCombo();
            }

            @Override
            public void fileDeleted(FileEvent fe) {
                reloadGlobalTemplatesCombo();
            }

            @Override
            public void fileRenamed(FileRenameEvent fe) {
                reloadGlobalTemplatesCombo();
            }

            @Override
            public void fileAttributeChanged(FileAttributeEvent fe) {
                if ("displayName".equals(fe.getName())) {
                    reloadGlobalTemplatesCombo();
                }
            }
            
            @Override
            public void fileChanged(FileEvent fe) {
                if (rbGlobal.isSelected()) {
                    setTextToGlobalLicense();
                }
            }
        };
        root.addRecursiveListener(FileUtil.weakFileChangeListener(fslistener, root));
        
    }
    
    
    @Messages({
        "# {0} - name of license",
        "ERR_missing_license=The project's license with name \"{0}\" was not found in IDE's license headers."})
    private void reloadGlobalTemplatesCombo() {
        category.setErrorMessage(null);
        GlobalItem item = (GlobalItem) comGlobal.getSelectedItem();
        String selection = item != null ? item.getName() : null;
        loadGlobalLicenses();
        if (selection != null) {
            boolean found = selectComboBoxItem(selection, rbGlobal.isSelected());
            if (!found) {
                category.setErrorMessage(ERR_missing_license(selection));
            }
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        rbGlobal = new javax.swing.JRadioButton();
        rbProject = new javax.swing.JRadioButton();
        comGlobal = new javax.swing.JComboBox();
        btnGlobal = new javax.swing.JButton();
        txtProject = new javax.swing.JTextField();
        btnProject = new javax.swing.JButton();
        lblLicense = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        epLicense = new javax.swing.JEditorPane();

        buttonGroup1.add(rbGlobal);
        rbGlobal.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbGlobal, org.openide.util.NbBundle.getMessage(LicenseHeadersPanel.class, "LicenseHeadersPanel.rbGlobal.text")); // NOI18N

        buttonGroup1.add(rbProject);
        org.openide.awt.Mnemonics.setLocalizedText(rbProject, org.openide.util.NbBundle.getMessage(LicenseHeadersPanel.class, "LicenseHeadersPanel.rbProject.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnGlobal, org.openide.util.NbBundle.getMessage(LicenseHeadersPanel.class, "LicenseHeadersPanel.btnGlobal.text")); // NOI18N
        btnGlobal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGlobalActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btnProject, org.openide.util.NbBundle.getMessage(LicenseHeadersPanel.class, "LicenseHeadersPanel.btnProject.text")); // NOI18N
        btnProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProjectActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblLicense, org.openide.util.NbBundle.getMessage(LicenseHeadersPanel.class, "LicenseHeadersPanel.lblLicense.text")); // NOI18N

        epLicense.setEditable(false); //default is Use global license value
        epLicense.setContentType("text/x-freemarker"); // NOI18N
        jScrollPane2.setViewportView(epLicense);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rbGlobal)
                    .addComponent(rbProject))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(comGlobal, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtProject))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnGlobal)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnProject)
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblLicense)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnGlobal, btnProject});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbGlobal)
                    .addComponent(comGlobal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGlobal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbProject)
                    .addComponent(txtProject, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnProject))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblLicense)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnGlobalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGlobalActionPerformed
        // TODO add your handling code here:
        Action action = FileUtil.getConfigObject("Actions/System/org-netbeans-modules-templates-actions-TemplatesAction.instance", Action.class);
        if (action != null) {
            System.setProperty("org.netbeans.modules.templates.actions.TemplatesAction.preselect", "Licenses");
            action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "perform"));
        } else {
            Exceptions.printStackTrace(new Exception("Actions/System/org-netbeans-modules-templates-actions-TemplatesAction.instance not found"));
        }
    }//GEN-LAST:event_btnGlobalActionPerformed

    private void btnProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProjectActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnProjectActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGlobal;
    private javax.swing.JButton btnProject;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox comGlobal;
    private javax.swing.JEditorPane epLicense;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblLicense;
    private javax.swing.JRadioButton rbGlobal;
    private javax.swing.JRadioButton rbProject;
    private javax.swing.JTextField txtProject;
    // End of variables declaration//GEN-END:variables

    
    private void loadGlobalLicenses() {
        FileObject root = FileUtil.getConfigFile("Templates/Licenses");
        DefaultComboBoxModel<GlobalItem> model = new DefaultComboBoxModel<>();
        for (FileObject fo : root.getChildren()) {
            if (fo.getAttribute("template") == null) {
                continue;
            }
            String displayName = (String) fo.getAttribute("displayName");
            if (displayName == null) {
                displayName = fo.getName();
                if (displayName.startsWith("license-")) {
                   displayName = displayName.substring("license-".length());
                }
            }
            model.addElement(new GlobalItem(displayName, fo));
        }
        comGlobal.setModel(model);
    }
    
    @Messages({
        "# {0} - name of license",
        "ERR_missing_license_template=<License header template not found for name \"{0}\">"})    
    private void setTextToGlobalLicense() {
        GlobalItem item = (GlobalItem) comGlobal.getSelectedItem();
        if (item == null) {
            epLicense.setText("");
        } else {
            try {
                if (item.fileObject != null) {
                    epLicense.setText(item.fileObject.asText());
                } else {
                   epLicense.setText(ERR_missing_license_template(item.getName())); 
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    @Messages({
        "# {0} - path of license",
        "ERR_missing_license_path=File at path \"{0}\" doesn't exist."}) 
    private void setTextToProjectLicense() {
        category.setErrorMessage(null);
        String path = txtProject.getText();
        FileObject fo = handler.resolveProjectLocation(path);
        if (fo != null && fo.isData()) {
            try {
                epLicense.setText(fo.asText());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            category.setErrorMessage(ERR_missing_license_path(path));
            epLicense.setText(defaultProjectLicense);
        }
        
    }
    
    

    private void initValues() {
        String name = handler.getGlobalLicenseName();
        String path = handler.getProjectLicenseLocation();
        if (name == null) {
            name = "default";
        }
        boolean found = selectComboBoxItem(name, true);
        if (path == null) {
            path = handler.getDefaultProjectLicenseLocation();
            txtProject.setText(path);
            rbGlobal.setSelected(true); //has to come last
            if (!found) {
                category.setErrorMessage(ERR_missing_license(name));
            }
        } else {
            txtProject.setText(path);
            rbProject.setSelected(true); //has to come last
            setTextToProjectLicense();
            epLicense.getDocument().addDocumentListener(editorListener);
        }
    }

    private boolean selectComboBoxItem(String name, boolean setText) {
        boolean found = false;
        DefaultComboBoxModel model = (DefaultComboBoxModel) comGlobal.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            GlobalItem gi = (GlobalItem) model.getElementAt(i);
            if (gi.getName().equals(name)) {
                comGlobal.setSelectedItem(gi);
                found = true;
                if (setText) {
                    setTextToGlobalLicense();
                }
                break;
            }
        }
        if (!found) {
            GlobalItem itm = new GlobalItem(name, null);
            model.insertElementAt(itm, 0);
            comGlobal.setSelectedItem(itm);
            if (setText) {
                setTextToGlobalLicense();
            }
        }
        return found;
    }

    
    private class GlobalItem {
        
        final String displayName;
        final FileObject fileObject;

        public GlobalItem(String name, FileObject fileObject) {
            this.displayName = name;
            this.fileObject = fileObject;
        }
        
        public String getName() {
            if (fileObject == null) {
                return displayName;
            }
            String name = fileObject.getName();
            if (name.startsWith("license-")) {
                name = name.substring("license-".length());
            }
            return name;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
    
    
    
}
