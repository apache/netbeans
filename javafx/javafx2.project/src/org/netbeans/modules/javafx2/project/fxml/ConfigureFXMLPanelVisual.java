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
package org.netbeans.modules.javafx2.project.fxml;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.javafx2.project.JFXProjectProperties;
import org.netbeans.modules.javafx2.project.fxml.SourceGroupSupport.SourceGroupProxy;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.*;

/**
 *
 * @author Anton Chechel <anton.chechel@oracle.com>
 * @author Petr Somol
 */
public class ConfigureFXMLPanelVisual extends JPanel implements ActionListener, DocumentListener {
    
    private Panel observer;
    private Project project;
    private SourceGroupSupport support;

    private boolean ignoreRootCombo;
    private RequestProcessor.Task updatePackagesTask;
    private static final ComboBoxModel WAIT_MODEL = SourceGroupSupport.getWaitModel();
    private final boolean isMaven;
    
    private ConfigureFXMLPanelVisual(Panel observer, Project project, SourceGroupSupport support, boolean isMaven) {
        this.observer = observer;
        this.project = project;
        this.support = support;
        this.isMaven = isMaven;
    
        setName(NbBundle.getMessage(ConfigureFXMLPanelVisual.class,"TXT_FXMLNameAndLoc")); // NOI18N
        initComponents(); // Matisse
        initComponents2(); // My own
    }

    private void fireChange() {
        this.observer.fireChangeEvent();
    }

    private void initComponents2() {
        fxmlNameTextField.getDocument().addDocumentListener(this);
        
        packageComboBox.getEditor().addActionListener(this);
        Component packageEditor = packageComboBox.getEditor().getEditorComponent();
        if (packageEditor instanceof JTextField) {
            ((JTextField) packageEditor).getDocument().addDocumentListener(this);
        }

        locationComboBox.setRenderer(new SourceGroupSupport.GroupListCellRenderer());
        packageComboBox.setRenderer(PackageView.listRenderer());
        locationComboBox.addActionListener(this);
    }

    public void initValues(FileObject template, FileObject preselectedFolder) {
        if (template == null) {
                throw new IllegalArgumentException(
                        NbBundle.getMessage(ConfigureFXMLPanelVisual.class,
                            "MSG_ConfigureFXMLPanel_Template_Error")); // NOI18N
        }

        // Show name of the project
        projectTextField.setText(ProjectUtils.getInformation(project).getDisplayName());
        
        String displayName;
        try {
            DataObject templateDo = DataObject.find(template);
            displayName = templateDo.getNodeDelegate().getDisplayName();
        } catch (DataObjectNotFoundException ex) {
            displayName = template.getName();
        }
        putClientProperty("NewFileWizard_Title", displayName); // NOI18N        

        // Setup comboboxes 
        locationComboBox.setModel(new DefaultComboBoxModel(support.getSourceGroups().toArray()));
        SourceGroupProxy preselectedGroup = SourceGroupSupport.getContainingSourceGroup(support, preselectedFolder);
        ignoreRootCombo = true;
        locationComboBox.setSelectedItem(preselectedGroup);
        ignoreRootCombo = false;
        FileObject targetFolder = preselectedFolder;
        if(isMaven && support.getType().equals(JavaProjectConstants.SOURCES_TYPE_RESOURCES)) {
            packageComboBox.getEditor().setItem(FXMLTemplateWizardIterator.defaultMavenFXMLPackage);
            targetFolder = null;
            if(preselectedGroup.isReal()) {
                File f = new File(preselectedGroup.getRootFolder().getPath() + File.separator + FXMLTemplateWizardIterator.defaultMavenFXMLPackage);
                if(f.exists()) {
                    targetFolder = FileUtil.toFileObject(f);
                }
            }
        } else {
            Object preselectedPackage = FXMLTemplateWizardIterator.getPreselectedPackage(preselectedGroup, preselectedFolder);
            if (preselectedPackage != null) {
                packageComboBox.getEditor().setItem(preselectedPackage);
            }
        }
        if (template != null) {
            if (fxmlNameTextField.getText().trim().length() == 0) { // To preserve the fxml name on back in the wiazard
                final String baseName = template.getName();
                String activeName = baseName;
                if (targetFolder != null) {
                    int index = 0;
                    while (true) {
                        FileObject fo = targetFolder.getFileObject(activeName, JFXProjectProperties.FXML_EXTENSION);
                        if (fo == null) {
                            break;
                        }
                        activeName = baseName + ++index;
                    }
                }
                fxmlNameTextField.setText(activeName);
                fxmlNameTextField.selectAll();
            }
        }
        
        updatePackages();
        updateText();
    }
    
    public FileObject getLocationFolder() {
        final Object selectedItem  = locationComboBox.getSelectedItem();
        return (selectedItem instanceof SourceGroupProxy) ? ((SourceGroupProxy)selectedItem).getRootFolder() : null;
    }

    public String getPackageFileName() {
        String packageName = packageComboBox.getEditor().getItem().toString();
        return packageName.replace('.', '/'); // NOI18N
    }

    /**
     * Name of selected package, or "" for default package.
     */
    String getPackageName() {
        return packageComboBox.getEditor().getItem().toString();
    }

    public String getFXMLName() {
        String text = fxmlNameTextField.getText().trim();
        return text.length() == 0 ? null : text;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fxmlNameLabel = new javax.swing.JLabel();
        fxmlNameTextField = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        locationLabel = new javax.swing.JLabel();
        packageLabel = new javax.swing.JLabel();
        resultLabel = new javax.swing.JLabel();
        projectTextField = new javax.swing.JTextField();
        locationComboBox = new javax.swing.JComboBox();
        resultTextField = new javax.swing.JTextField();
        packageComboBox = new javax.swing.JComboBox();

        setPreferredSize(new java.awt.Dimension(500, 340));

        fxmlNameLabel.setLabelFor(fxmlNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fxmlNameLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.fxmlNameLabel.text")); // NOI18N

        projectLabel.setLabelFor(projectTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.projectLabel.text")); // NOI18N

        locationLabel.setLabelFor(locationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.locationLabel.text")); // NOI18N

        packageLabel.setLabelFor(packageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(packageLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.packageLabel.text")); // NOI18N

        resultLabel.setLabelFor(resultTextField);
        org.openide.awt.Mnemonics.setLocalizedText(resultLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.resultLabel.text")); // NOI18N

        projectTextField.setEditable(false);
        projectTextField.setEnabled(false);

        resultTextField.setEditable(false);
        resultTextField.setEnabled(false);

        packageComboBox.setEditable(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(projectLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(locationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(packageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(resultLabel))
                    .addComponent(fxmlNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fxmlNameTextField)
                    .addComponent(projectTextField)
                    .addComponent(locationComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(resultTextField)
                    .addComponent(packageComboBox, 0, 409, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fxmlNameLabel)
                    .addComponent(fxmlNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLabel)
                    .addComponent(projectTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(packageLabel)
                    .addComponent(packageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resultLabel)
                    .addComponent(resultTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(204, Short.MAX_VALUE))
        );

        fxmlNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.fxmlNameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        projectLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.projectLabel.AccessibleContext.accessibleDescription")); // NOI18N
        locationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.locationLabel.AccessibleContext.accessibleDescription")); // NOI18N
        packageLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.packageLabel.AccessibleContext.accessibleDescription")); // NOI18N
        resultLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLPanelVisual.class, "ConfigureFXMLPanelVisual.resultLabel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel fxmlNameLabel;
    private javax.swing.JTextField fxmlNameTextField;
    private javax.swing.JComboBox locationComboBox;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JComboBox packageComboBox;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTextField;
    private javax.swing.JLabel resultLabel;
    private javax.swing.JTextField resultTextField;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
    @Override
    public void actionPerformed(ActionEvent e) {
        if (locationComboBox == e.getSource()) {
            if (!ignoreRootCombo) {
                updatePackages();
            }
            updateText();
            fireChange();
        } else if (packageComboBox == e.getSource()) {
            updateText();
            fireChange();
        } else if (packageComboBox.getEditor() == e.getSource()) {
            updateText();
            fireChange();
        }
    }

    // DocumentListener implementation -----------------------------------------
    @Override
    public void changedUpdate(DocumentEvent e) {
        updateText();
        fireChange();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    // Private methods ---------------------------------------------------------
    private void updatePackages() {
        final Object item = locationComboBox.getSelectedItem();
        if (!(item instanceof SourceGroupProxy)) {
            return;
        }
        WAIT_MODEL.setSelectedItem(packageComboBox.getEditor().getItem());
        packageComboBox.setModel(WAIT_MODEL);

        if (updatePackagesTask != null) {
            updatePackagesTask.cancel();
        }

        updatePackagesTask = new RequestProcessor("ComboUpdatePackages").post(new Runnable() { // NOI18N
            @Override
            public void run() {
                final ComboBoxModel model = ((SourceGroupProxy) item).getPackagesComboBoxModel();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        model.setSelectedItem(packageComboBox.getEditor().getItem());
                        packageComboBox.setModel(model);
                    }
                });
            }
        });
    }
    
    private void updateText() {
        final Object selectedItem = locationComboBox.getSelectedItem();
        String createdFileName;
        if (selectedItem instanceof SourceGroupProxy) {
            SourceGroupProxy g = (SourceGroupProxy) selectedItem;
            String packageName = getPackageName();
            String fxmlName = getFXMLName();
            support.setCurrentSourceGroup(g);
            support.setCurrentPackageName(packageName);
            support.setCurrentFileName(fxmlName);
            if (fxmlName != null && fxmlName.length() > 0) {
                fxmlName = fxmlName + FXMLTemplateWizardIterator.FXML_FILE_EXTENSION;
            }
            String path = support.getCurrentPackagePath();
            createdFileName = path == null ? "" : path.replace(".", "/") + fxmlName;
        } else {
            //May be null if nothing selected
            createdFileName = "";   //NOI18N
        }
        resultTextField.setText(createdFileName.replace('/', File.separatorChar)); // NOI18N
    }


    // Private innerclasses ----------------------------------------------------
    
    static class Panel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor> {
        
        private ConfigureFXMLPanelVisual component;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private WizardDescriptor settings;
        SourceGroupSupport support;

        public Panel(Project project, SourceGroupSupport support, boolean isMaven) {
            this.support = support;
            component = new ConfigureFXMLPanelVisual(this, project, support, isMaven);
        }

        @Override
        public Component getComponent() {
            return component;
        }

        @Override
        public HelpCtx getHelp() {
            return null;
        }

        @Override
        public void readSettings(WizardDescriptor settings) {
            this.settings = settings;
            // Try to preselect a folder
            FileObject preselectedFolder = Templates.getTargetFolder(settings);
            // Init values
            component.initValues(Templates.getTemplate(settings), preselectedFolder);

            // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
            // this name is used in NewFileWizard to modify the title
            Object substitute = component.getClientProperty("NewFileWizard_Title"); // NOI18N
            if (substitute != null) {
                settings.putProperty("NewFileWizard_Title", substitute); // NOI18N
            }
        }

        @Override
        public void storeSettings(WizardDescriptor settings) {
            Object value = settings.getValue();
            if (WizardDescriptor.PREVIOUS_OPTION.equals(value)
                    || WizardDescriptor.CANCEL_OPTION.equals(value)
                    || WizardDescriptor.CLOSED_OPTION.equals(value)) {
                return;
            }
            settings.putProperty("NewFileWizard_Title", null); // NOI18N
        }

        @Override
        public boolean isValid() {
            if (component.getFXMLName() == null) {
                FXMLTemplateWizardIterator.setInfoMessage("WARN_ConfigureFXMLPanel_Provide_FXML_Name", settings); // NOI18N
                return false;
            }
            
            if (!FXMLTemplateWizardIterator.isValidPackageName(component.getPackageName())) {
                FXMLTemplateWizardIterator.setErrorMessage("WARN_ConfigureFXMLPanel_Provide_Package_Name", settings); // NOI18N
                return false;
            }

            if (!FXMLTemplateWizardIterator.isValidPackage(component.getLocationFolder(), component.getPackageName())) {
                FXMLTemplateWizardIterator.setErrorMessage("WARN_ConfigureFXMLPanel_Package_Invalid", settings); // NOI18N
                return false;
            }

            // test for illegal characters in file name
            if (!FXMLTemplateWizardIterator.validFileName(component.getFXMLName())) {
                FXMLTemplateWizardIterator.setErrorMessage("MSG_invalid_file_name", settings); // NOI18N
                return false;
            }
            
            FileObject rootFolder = component.getLocationFolder();
            String errorMessage = FXMLTemplateWizardIterator.canUseFileName(rootFolder, 
                    component.getPackageFileName(), component.getFXMLName(), JFXProjectProperties.FXML_EXTENSION);
            settings.getNotificationLineSupport().setErrorMessage(errorMessage);
            if (errorMessage != null) {
                return false;
            }

            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        private void fireChangeEvent() {
            changeSupport.fireChange();
        }

        @Override
        public boolean isFinishPanel() {
            return true;
        }
    }
}
