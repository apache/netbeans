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

package org.netbeans.modules.web.jsf.wizards;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.core.api.support.java.JavaIdentifiers;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.SourceGroupUISupport;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.JsfPreferences;
import org.netbeans.modules.web.jsf.JsfTemplateUtils;
import org.netbeans.modules.web.jsf.JsfTemplateUtils.OpenTemplateAction;
import org.netbeans.modules.web.jsf.JsfTemplateUtils.TemplateType;
import org.netbeans.modules.web.jsf.dialogs.BrowseFolders;
import org.netbeans.modules.web.jsf.palette.items.CancellableDialog;
import org.netbeans.modules.web.jsf.wizards.JSFConfigurationPanel.PreferredLanguage;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author  Pavel Buzek
 */
public class PersistenceClientSetupPanelVisual extends javax.swing.JPanel implements DocumentListener, CancellableDialog {

    private WizardDescriptor wizard;
    private Project project;
    private JTextComponent jpaPackageComboBoxEditor, jsfPackageComboBoxEditor;
    private ChangeSupport changeSupport = new ChangeSupport(this);
    private boolean cancelled = false;
    
    /** Creates new form CrudSetupPanel */
    public PersistenceClientSetupPanelVisual(WizardDescriptor wizard) {
        this.wizard = wizard;
        this.project = Templates.getProject(wizard);
        initComponents();
        initTemplateComboBox();

        // listeners
        jpaPackageComboBoxEditor = (JTextComponent)jpaPackageComboBox.getEditor().getEditorComponent();
        jpaPackageComboBoxEditor.getDocument().addDocumentListener(this);
        jsfPackageComboBoxEditor = (JTextComponent)jsfPackageComboBox.getEditor().getEditorComponent();
        jsfPackageComboBoxEditor.getDocument().addDocumentListener(this);
        jsfFolder.getDocument().addDocumentListener(this);
        localizationBundleTextField.getDocument().addDocumentListener(this);
    }

    private void initTemplateComboBox() {
        for (JsfTemplateUtils.Template template : JsfTemplateUtils.getTemplates(TemplateType.PAGES)) {
            templatesComboBox.addItem(template);
        }

        // in case of preferred JSP remove other Facelet templates
        JsfPreferences jsfPreferences = JsfPreferences.forProject(project);
        if (!jsfPreferences.isJsfPresent() && jsfPreferences.getPreferredLanguage() != PreferredLanguage.Facelets) {
            for (int i = templatesComboBox.getItemCount() - 1; i >= 0; i--) {
                JsfTemplateUtils.Template template = (JsfTemplateUtils.Template) templatesComboBox.getItemAt(i);
                if (!JsfTemplateUtils.STANDARD_TPL.equals(template.getName())) {
                    templatesComboBox.removeItemAt(i);
                }
            }
        }

        templatesComboBox.setRenderer(new JsfTemplateUtils.TemplateCellRenderer());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jsfFolder = new javax.swing.JTextField();
        browseFolderButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        projectLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationLabel = new javax.swing.JLabel();
        locationComboBox = new javax.swing.JComboBox();
        jsfPackageLabel = new javax.swing.JLabel();
        jsfPackageComboBox = new javax.swing.JComboBox();
        ajaxifyCheckbox = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jpaPackageLabel = new javax.swing.JLabel();
        jpaPackageComboBox = new javax.swing.JComboBox();
        overrideExistingCheckBox = new javax.swing.JCheckBox();
        customizeTemplatesLabel = new javax.swing.JLabel();
        localizationBundleLabel = new javax.swing.JLabel();
        localizationBundleTextField = new javax.swing.JTextField();
        templateChooserLabel = new javax.swing.JLabel();
        templatesComboBox = new javax.swing.JComboBox();

        setName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_JSFPagesAndClasses")); // NOI18N

        jLabel2.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_JSF_Pages").charAt(0));
        jLabel2.setLabelFor(jsfFolder);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle"); // NOI18N
        jLabel2.setText(bundle.getString("LBL_JSF_pages_folder")); // NOI18N

        browseFolderButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_Browse").charAt(0));
        browseFolderButton.setText(bundle.getString("LBL_Browse")); // NOI18N
        browseFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseFolderButtonActionPerformed(evt);
            }
        });

        jLabel4.setText(bundle.getString("MSG_Jsf_Pages_Location")); // NOI18N

        projectLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_Project").charAt(0));
        projectLabel.setLabelFor(projectTextField);
        projectLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Project")); // NOI18N

        projectTextField.setEditable(false);

        locationLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_Location").charAt(0));
        locationLabel.setLabelFor(locationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_SrcLocation")); // NOI18N

        locationComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationComboBoxActionPerformed(evt);
            }
        });

        jsfPackageLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_Package").charAt(0));
        jsfPackageLabel.setLabelFor(jsfPackageComboBox);
        jsfPackageLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Package")); // NOI18N

        jsfPackageComboBox.setEditable(true);

        ajaxifyCheckbox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("LBL_AJAXIFY_APP.Mnemonic").charAt(0));
        ajaxifyCheckbox.setText(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_AJAXIFY_APP")); // NOI18N
        ajaxifyCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ajaxifyCheckboxActionPerformed(evt);
            }
        });

        jLabel6.setText(bundle.getString("MSG_Jpa_Jsf_Packages")); // NOI18N

        jpaPackageLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("LBL_Jpa_Controller_Package_Mnemonic").charAt(0));
        jpaPackageLabel.setLabelFor(jpaPackageComboBox);
        jpaPackageLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Jpa_Controller_Package")); // NOI18N

        jpaPackageComboBox.setEditable(true);

        overrideExistingCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("PersistenceClientSetupPanelVisual.overrideExistingFiles.mnemonic").charAt(0));
        overrideExistingCheckBox.setText(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "PersistenceClientSetupPanelVisual.overrideExistingFiles")); // NOI18N
        overrideExistingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overrideExistingCheckBoxActionPerformed(evt);
            }
        });

        customizeTemplatesLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "PersistenceClientSetupPanelVisual.customizeTemplate")); // NOI18N
        customizeTemplatesLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                customizeTemplatesLabelMouseClicked(evt);
            }
        });

        localizationBundleLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("PersistenceClientSetupPanelVisual.localizationBundle.mnemonic").charAt(0));
        localizationBundleLabel.setLabelFor(localizationBundleTextField);
        localizationBundleLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "PersistenceClientSetupPanelVisual.localizationBundle")); // NOI18N

        templateChooserLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("PersistenceClientSetupPanelVisual.chooseTemplate.mnemonic").charAt(0));
        templateChooserLabel.setLabelFor(templatesComboBox);
        templateChooserLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "PersistenceClientSetupPanelVisual.chooseTemplate")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ajaxifyCheckbox)
                    .addComponent(jLabel4)
                    .addComponent(jLabel6)
                    .addComponent(overrideExistingCheckBox)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(templateChooserLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(templatesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 342, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(projectLabel)
                                    .addComponent(locationLabel)
                                    .addComponent(jpaPackageLabel)
                                    .addComponent(jsfPackageLabel)
                                    .addComponent(jLabel2)
                                    .addComponent(localizationBundleLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(projectTextField)
                                    .addComponent(locationComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jpaPackageComboBox, 0, 343, Short.MAX_VALUE)
                                    .addComponent(jsfPackageComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jsfFolder)
                                    .addComponent(localizationBundleTextField))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(customizeTemplatesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(browseFolderButton))))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLabel)
                    .addComponent(projectTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationLabel)
                    .addComponent(locationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jpaPackageLabel)
                    .addComponent(jpaPackageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jsfPackageLabel)
                    .addComponent(jsfPackageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addComponent(jLabel4)
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jsfFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseFolderButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(localizationBundleLabel)
                    .addComponent(localizationBundleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ajaxifyCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(overrideExistingCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(templateChooserLabel)
                    .addComponent(templatesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(customizeTemplatesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_JSF_pages_folder")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_JSF_pages_folder")); // NOI18N
        jsfFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "ACSD_JSF_Pages")); // NOI18N
        browseFolderButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "ACSD_Browser")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "MSG_Jsf_Pages_Location")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "MSG_Jsf_Pages_Location")); // NOI18N
        projectLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Project")); // NOI18N
        projectLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Project")); // NOI18N
        projectTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "ACSD_Project")); // NOI18N
        locationLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_SrcLocation")); // NOI18N
        locationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_SrcLocation")); // NOI18N
        locationComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "ACSD_Location")); // NOI18N
        jsfPackageLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Package")); // NOI18N
        jsfPackageLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Package")); // NOI18N
        jsfPackageComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "ACSD_Package")); // NOI18N
        ajaxifyCheckbox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_AJAXIFY_APP")); // NOI18N
        ajaxifyCheckbox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_AJAXIFY_APP")); // NOI18N
        jLabel6.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "MSG_Jpa_Jsf_Packages")); // NOI18N
        jLabel6.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "MSG_Jpa_Jsf_Packages")); // NOI18N
        jpaPackageLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Jpa_Controller_Package")); // NOI18N
        jpaPackageLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Jpa_Controller_Package")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void locationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationComboBoxActionPerformed
        locationChanged();
    }//GEN-LAST:event_locationComboBoxActionPerformed
        
    private void browseFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseFolderButtonActionPerformed
        Sources s = (Sources) Templates.getProject(wizard).getLookup().lookup(Sources.class);
        org.netbeans.api.project.SourceGroup[] groups = s.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
        if (fo!=null) {
            String res = "/"+JSFConfigUtilities.getResourcePath(groups,fo,'/',true);
            jsfFolder.setText(res);
        }
    }//GEN-LAST:event_browseFolderButtonActionPerformed

    private void ajaxifyCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ajaxifyCheckboxActionPerformed
        // TODO add your handling code here:
        changeSupport.fireChange();
}//GEN-LAST:event_ajaxifyCheckboxActionPerformed

    private void overrideExistingCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overrideExistingCheckBoxActionPerformed
        changeSupport.fireChange();
    }//GEN-LAST:event_overrideExistingCheckBoxActionPerformed

    private void customizeTemplatesLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_customizeTemplatesLabelMouseClicked
        String viewTemplatePath = JsfTemplateUtils.getTemplatePath(TemplateType.PAGES, getTemplatesStyle(), WizardProperties.VIEW_TEMPLATE);
        String editTemplatePath = JsfTemplateUtils.getTemplatePath(TemplateType.PAGES, getTemplatesStyle(), WizardProperties.EDIT_TEMPLATE);
        String createTemplatePath = JsfTemplateUtils.getTemplatePath(TemplateType.PAGES, getTemplatesStyle(), WizardProperties.CREATE_TEMPLATE);
        String listTemplatePath = JsfTemplateUtils.getTemplatePath(TemplateType.PAGES, getTemplatesStyle(), WizardProperties.LIST_TEMPLATE);
        String baseTemplatePath = JsfTemplateUtils.getTemplatePath(TemplateType.PAGES, getTemplatesStyle(), WizardProperties.BASE_TEMPLATE);
        String controllerTemplatePath = JsfTemplateUtils.getTemplatePath(TemplateType.PAGES, getTemplatesStyle(), WizardProperties.CONTROLLER_TEMPLATE);
        String paginationTemplatePath = JsfTemplateUtils.getTemplatePath(TemplateType.PAGES, getTemplatesStyle(), WizardProperties.PAGINATION_TEMPLATE);
        String utilTemplatePath = JsfTemplateUtils.getTemplatePath(TemplateType.PAGES, getTemplatesStyle(), WizardProperties.UTIL_TEMPLATE);
        String bundleTemplatePath = JsfTemplateUtils.getTemplatePath(TemplateType.PAGES, getTemplatesStyle(), WizardProperties.BUNDLE_TEMPLATE);

        JPopupMenu menu = new JPopupMenu();
        List<String> paths = new ArrayList<>();
        processValidPath(menu, paths, viewTemplatePath, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "PersistenceClientSetupPanelVisual.viewTemplate"));
        processValidPath(menu, paths, editTemplatePath, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "PersistenceClientSetupPanelVisual.editTemplate"));
        processValidPath(menu, paths, createTemplatePath, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "PersistenceClientSetupPanelVisual.createTemplate"));
        processValidPath(menu, paths, listTemplatePath, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "PersistenceClientSetupPanelVisual.listTemplate"));
        processValidPath(menu, paths, baseTemplatePath, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "PersistenceClientSetupPanelVisual.baseTemplate"));
        processValidPath(menu, paths, controllerTemplatePath, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "PersistenceClientSetupPanelVisual.controllerTemplate"));
        processValidPath(menu, paths, paginationTemplatePath, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "PersistenceClientSetupPanelVisual.paginationTemplate"));
        processValidPath(menu, paths, utilTemplatePath, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "PersistenceClientSetupPanelVisual.utilTemplate"));
        processValidPath(menu, paths, bundleTemplatePath, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "PersistenceClientSetupPanelVisual.bundleTemplate"));
        menu.insert(new OpenTemplateAction(this, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "PersistenceClientSetupPanelVisual.allTemplates"), paths.toArray(new String[0])), 0);
        menu.show(customizeTemplatesLabel, evt.getX(), evt.getY());
    }//GEN-LAST:event_customizeTemplatesLabelMouseClicked

    private void processValidPath(JPopupMenu menu, List<String> paths, String path, String message) {
        if (FileUtil.getConfigRoot().getFileObject(path) != null) {
            paths.add(path);
            menu.add(new OpenTemplateAction(this, message, path));
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox ajaxifyCheckbox;
    private javax.swing.JButton browseFolderButton;
    private javax.swing.JLabel customizeTemplatesLabel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JComboBox jpaPackageComboBox;
    private javax.swing.JLabel jpaPackageLabel;
    private javax.swing.JTextField jsfFolder;
    private javax.swing.JComboBox jsfPackageComboBox;
    private javax.swing.JLabel jsfPackageLabel;
    private javax.swing.JLabel localizationBundleLabel;
    private javax.swing.JTextField localizationBundleTextField;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JCheckBox overrideExistingCheckBox;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JLabel templateChooserLabel;
    private javax.swing.JComboBox templatesComboBox;
    // End of variables declaration//GEN-END:variables
    
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    
    boolean valid(WizardDescriptor wizard) {
//        List<Entity> entities = (List<Entity>) wizard.getProperty(WizardProperties.ENTITY_CLASS);
//        String controllerPkg = getJsfPackage();
//        
//        boolean filesAlreadyExist = false;
//        String troubleMaker = "";
//        for (Entity entity : entities) {
//            String entityClass = entity.getClass2();
//            String simpleClassName = JSFClientGenerator.simpleClassName(entityClass);
//            String firstLower = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);
//            String folder = jsfFolder.getText().endsWith("/") ? jsfFolder.getText() : jsfFolder.getText() + "/";
//            folder = folder + firstLower;
//            String controller = controllerPkg + "." + simpleClassName + "Controller";
//            String fqn = getJsfPackage().length() > 0 ? getJsfPackage().replace('.', '/') + "/" + simpleClassName : simpleClassName;
//            if (getLocationValue().getRootFolder().getFileObject(fqn + "Controller.java") != null) {
//                filesAlreadyExist = true;
//                troubleMaker = controllerPkg + "." + simpleClassName + "Controller.java";
//                break;
//            }
//            if (getLocationValue().getRootFolder().getFileObject(fqn + "Converter.java") != null) {
//                filesAlreadyExist = true;
//                troubleMaker = controllerPkg + "." + simpleClassName + "Converter.java";
//                break;
//            }
//        }
//        if (filesAlreadyExist) {
//            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,                                  // NOI18N
//                NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "MSG_FilesAlreadyExist", troubleMaker));
//            return false;
//        }
//        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null); // NOI18N
        
            if (Util.isContainerManaged(project)) {
                ClassPath cp = ClassPath.getClassPath(getLocationValue().getRootFolder(), ClassPath.COMPILE);
                ClassLoader cl = cp.getClassLoader(true);
                try {
                    Class.forName("javax.transaction.UserTransaction", false, cl);
                }
                catch (ClassNotFoundException cnfe) {
                    try {
                        Class.forName("jakarta.transaction.UserTransaction", false, cl);
                    } catch (ClassNotFoundException cnfe2) {
                        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "ERR_UserTransactionUnavailable"));
                        return false;
                    }
                }
                catch (UnsupportedClassVersionError ucve) {
                    Logger.getLogger(PersistenceClientSetupPanelVisual.class.getName()).log(Level.WARNING, ucve.toString());
                }
            }
        
            Sources srcs = (Sources) project.getLookup().lookup(Sources.class);
            SourceGroup sgWeb[] = srcs.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
            FileObject pagesRootFolder = sgWeb[0].getRootFolder();
            File pagesRootFolderAsFile = FileUtil.toFile(pagesRootFolder);
            String jsfFolderText = jsfFolder.getText();
            try {
                String canonPath = new File(pagesRootFolderAsFile, jsfFolderText).getCanonicalPath();
            }
            catch (IOException ioe) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "ERR_JsfTargetChooser_InvalidJsfFolder"));
                return false;
            }
        
            String[] packageNames = {getJpaPackage(), getJsfPackage()};
            for (int i = 0; i < packageNames.length; i++) {
                if (packageNames[i].trim().equals("")) { // NOI18N
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "ERR_JavaTargetChooser_CantUseDefaultPackage"));
                    return false;
                }

                if (!JavaIdentifiers.isValidPackageName(packageNames[i])) {
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class,"ERR_JavaTargetChooser_InvalidPackage")); //NOI18N
                    return false;
                }

                if (!SourceGroups.isFolderWritable(getLocationValue(), packageNames[i])) {
                    wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "ERR_JavaTargetChooser_UnwritablePackage")); //NOI18N
                    return false;
                }
            }
            if (overrideExistingCheckBox.isVisible()) {
                boolean conflict = PersistenceClientIterator.doesSomeFileExistAlready(
                        getLocationValue().getRootFolder(), pagesRootFolder, getJpaPackage(),
                        getJsfPackage(), jsfFolder.getText(), (List<String>)wizard.getProperty(WizardProperties.ENTITY_CLASS),
                        localizationBundleTextField.getText());
                if (conflict) {
                    if (overrideExistingCheckBox.isSelected()) {
                        wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class,"ERR_JavaTargetChooser_FileAlreadyExistWarning")); //NOI18N
                    } else {
                        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class,"ERR_JavaTargetChooser_FileAlreadyExist")); //NOI18N
                        return false;
                    }
                }
            }
            if (localizationBundleTextField.isVisible() && localizationBundleTextField.getText().length() == 0) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PersistenceClientSetupPanelVisual.class,"ERR_JavaTargetChooser_MissingBundleName")); //NOI18N
                return false;
            }

            if (ajaxifyCheckbox.isSelected()) {
                if (LibraryManager.getDefault().getLibrary("jsf-extensions") == null) { //NOI18N
                    wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                        NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "MSG_JsfExtensionsLibraryRequired"));
                    return true;
                }
            }
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null); // NOI18N
            return true;
    }
    
    public SourceGroup getLocationValue() {
        return (SourceGroup)locationComboBox.getSelectedItem();
    }

    public String getJsfPackage() {
        return jsfPackageComboBoxEditor.getText();
    }
    
    public String getJpaPackage() {
        return jpaPackageComboBoxEditor.getText();
    }

    public String getTemplatesStyle() {
        return ((JsfTemplateUtils.Template) templatesComboBox.getSelectedItem()).getName();
    }

    private void locationChanged() {
        updateSourceGroupPackages();
//        changeSupport.fireChange();
    }
    
    void read(WizardDescriptor settings) {
        jsfFolder.setText((String) settings.getProperty(WizardProperties.JSF_FOLDER));
        
        project = Templates.getProject(settings);
        
        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());

         SourceGroup[] sourceGroups = SourceGroups.getJavaSourceGroups(project);      
         SourceGroupUISupport.connect(locationComboBox, sourceGroups);

        jsfPackageComboBox.setRenderer(PackageView.listRenderer());

        updateSourceGroupPackages();

        if(J2eeProjectCapabilities.forProject(project).isEjb31LiteSupported())
        {
            //change label if we will generate session beans
            jpaPackageLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "LBL_Jpa_SessionBean_Package")); // NOI18N
        }
        if (localizationBundleTextField.getText().length() == 0) {
            localizationBundleTextField.setText(NbBundle.getMessage(PersistenceClientSetupPanelVisual.class, "PersistenceClientSetupPanelVisual.defaultBundleName")); // NOI18N
        }

        boolean jsf2Generator = "true".equals(settings.getProperty(PersistenceClientIterator.JSF2_GENERATOR_PROPERTY));
        ajaxifyCheckbox.setVisible(!jsf2Generator);
        overrideExistingCheckBox.setVisible(jsf2Generator);
        customizeTemplatesLabel.setVisible(jsf2Generator);
        localizationBundleLabel.setVisible(jsf2Generator);
        localizationBundleTextField.setVisible(jsf2Generator);

    }
    
    void store(WizardDescriptor settings) {
        settings.putProperty(WizardProperties.JSF_FOLDER, jsfFolder.getText());
        String jpaPkg = getJpaPackage();
        String jsfPkg = getJsfPackage();
        settings.putProperty(WizardProperties.JPA_CLASSES_PACKAGE, jpaPkg);
        settings.putProperty(WizardProperties.JSF_CLASSES_PACKAGE, jsfPkg);
        settings.putProperty(WizardProperties.AJAXIFY_JSF_CRUD, ajaxifyCheckbox.isSelected());
        settings.putProperty(WizardProperties.JAVA_PACKAGE_ROOT_FILE_OBJECT, getLocationValue().getRootFolder());
        settings.putProperty(WizardProperties.LOCALIZATION_BUNDLE_NAME, localizationBundleTextField.getText());
        settings.putProperty(WizardProperties.TEMPLATE_STYLE, getTemplatesStyle());
    }

    private void updateSourceGroupPackages() {
        SourceGroup sourceGroup = (SourceGroup)locationComboBox.getSelectedItem();
        JComboBox[] combos = {jpaPackageComboBox, jsfPackageComboBox};
        for (JComboBox combo : combos) {
            ComboBoxModel model = PackageView.createListView(sourceGroup);
            if (model.getSelectedItem()!= null && model.getSelectedItem().toString().startsWith("META-INF")
                    && model.getSize() > 1) { // NOI18N
                model.setSelectedItem(model.getElementAt(1));
            }
            combo.setModel(model);
        }
    }
    
    public void insertUpdate(DocumentEvent e) {
        changeSupport.fireChange();
    }

    public void removeUpdate(DocumentEvent e) {
        changeSupport.fireChange();
    }

    public void changedUpdate(DocumentEvent e) {
        changeSupport.fireChange();
    }

    public void cancel() {
        cancelled = true;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }

}
