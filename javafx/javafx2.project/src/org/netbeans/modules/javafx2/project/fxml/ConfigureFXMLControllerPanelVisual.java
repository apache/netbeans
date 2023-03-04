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

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.netbeans.modules.javafx2.project.JFXProjectUtils;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author Anton Chechel
 * @author Petr Somol
 * @author Roman Svitanic
 */
public class ConfigureFXMLControllerPanelVisual extends JPanel implements ActionListener, DocumentListener {
    
    private static final String SPACE_CHAR = " "; //NOI18N
    private Panel observer;
    private boolean ignoreRootCombo;
    private RequestProcessor.Task updatePackagesTask;
    private static final ComboBoxModel WAIT_MODEL = SourceGroupSupport.getWaitModel();
    private final boolean isMaven;
    SourceGroupSupport support;
    private String previousControllerName;

    private ConfigureFXMLControllerPanelVisual(Panel observer, SourceGroupSupport support, boolean isMaven) {
        this.support = support;
        this.observer = observer;
        this.isMaven = isMaven;
        setName(NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class,"TXT_ControllerNameAndLoc")); // NOI18N
        initComponents(); // Matisse
        initComponents2(); // My own
    }

    private void fireChange() {
        this.observer.fireChangeEvent();
    }

    private void initComponents2() {
        createdNameTextField.getDocument().addDocumentListener(this);
        existingNameTextField.getDocument().addDocumentListener(this);
        createdPackageComboBox.getEditor().addActionListener(this);
        Component packageEditor = createdPackageComboBox.getEditor().getEditorComponent();
        if (packageEditor instanceof JTextField) {
            ((JTextField) packageEditor).getDocument().addDocumentListener(this);
        }

        createdLocationComboBox.setRenderer(new SourceGroupSupport.GroupListCellRenderer());
        createdPackageComboBox.setRenderer(PackageView.listRenderer());
        createdLocationComboBox.addActionListener(this);
    }

    public void initValues(FileObject template, FileObject preselectedFolder) {
        if (template == null) {
                throw new IllegalArgumentException(
                        NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class,
                            "MSG_ConfigureFXMLPanel_Template_Error")); // NOI18N
        }

        String displayName;
        try {
            DataObject templateDo = DataObject.find(template);
            displayName = templateDo.getNodeDelegate().getDisplayName();
        } catch (DataObjectNotFoundException ex) {
            displayName = template.getName();
        }
        putClientProperty("NewFileWizard_Title", displayName); // NOI18N

        createdLocationComboBox.setModel(new DefaultComboBoxModel(support.getSourceGroups().toArray()));
        SourceGroupSupport.SourceGroupProxy preselectedGroup = isMaven ? 
                SourceGroupSupport.getContainingSourceGroup(support, preselectedFolder) : support.getParent().getCurrentSourceGroup();
        ignoreRootCombo = true;
        createdLocationComboBox.setSelectedItem(preselectedGroup);
        ignoreRootCombo = false;
        if(isMaven) {
            Object preselectedPackage = FXMLTemplateWizardIterator.getPreselectedPackage(preselectedGroup, preselectedFolder);
            if (preselectedPackage != null) {
                createdPackageComboBox.getEditor().setItem(preselectedPackage);
            }
        } else {
            createdPackageComboBox.getEditor().setItem(support.getParent().getCurrentPackageName());
        }
        updatePackages();
        updateText();
        updateResult();
    }
    
    boolean isControllerEnabled() {
        return controllerCheckBox.isSelected();
    }

    public FileObject getLocationFolder() {
        final Object selectedItem  = createdLocationComboBox.getSelectedItem();
        return (selectedItem instanceof SourceGroupSupport.SourceGroupProxy) ? ((SourceGroupSupport.SourceGroupProxy)selectedItem).getRootFolder() : null;
    }

    public String getPackageFileName() {
        String packageName = createdPackageComboBox.getEditor().getItem().toString();
        return packageName.replace('.', '/'); // NOI18N
    }

    /**
     * Name of selected package, or "" for default package.
     */
    String getPackageName() {
        return createdPackageComboBox.getEditor().getItem().toString();
    }

    String getNewControllerName() {
        String text = createdNameTextField.getText().trim();
        return text.length() == 0 ? null : text;
    }

    String getExistingControllerName() {
        String text = existingNameTextField.getText().trim();
        return text.length() == 0 ? null : text;
    }
    
    private void radioButtonsStateChanged() {
        if (!controllerCheckBox.isSelected()) {
            return;
        }
        createdNameLabel.setEnabled(createNewRadioButton.isSelected());
        createdNameTextField.setEnabled(createNewRadioButton.isSelected());
        existingNameLabel.setEnabled(!createNewRadioButton.isSelected());
        existingNameTextField.setEnabled(!createNewRadioButton.isSelected());
        chooseButton.setEnabled(!createNewRadioButton.isSelected());

        createdLocation.setEnabled(createNewRadioButton.isSelected());
        createdLocationComboBox.setEnabled(createNewRadioButton.isSelected());
        createdPackage.setEnabled(createNewRadioButton.isSelected());
        createdPackageComboBox.setEnabled(createNewRadioButton.isSelected());            

        updateResult();
        fireChange();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        controllerCheckBox = new javax.swing.JCheckBox();
        createdNameLabel = new javax.swing.JLabel();
        createdNameTextField = new javax.swing.JTextField();
        fileLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        createNewRadioButton = new javax.swing.JRadioButton();
        useExistingRadioButton = new javax.swing.JRadioButton();
        existingNameLabel = new javax.swing.JLabel();
        existingNameTextField = new javax.swing.JTextField();
        chooseButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        createdLocation = new javax.swing.JLabel();
        createdLocationComboBox = new javax.swing.JComboBox();
        createdPackage = new javax.swing.JLabel();
        createdPackageComboBox = new javax.swing.JComboBox();

        setPreferredSize(new java.awt.Dimension(500, 340));
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(controllerCheckBox, org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.controllerCheckBox.text")); // NOI18N
        controllerCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                controllerCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(controllerCheckBox, gridBagConstraints);
        controllerCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.controllerCheckBox.AccessibleContext.accessibleDescription")); // NOI18N

        createdNameLabel.setLabelFor(createdNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdNameLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.createdNameLabel.text")); // NOI18N
        createdNameLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
        add(createdNameLabel, gridBagConstraints);
        createdNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.createdNameLabel.AccessibleContext.accessibleDescription")); // NOI18N

        createdNameTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(createdNameTextField, gridBagConstraints);

        fileLabel.setLabelFor(fileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fileLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.resultLabel.text")); // NOI18N
        fileLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(25, 15, 0, 0);
        add(fileLabel, gridBagConstraints);
        fileLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.fileLabel.AccessibleContext.accessibleDescription")); // NOI18N

        fileTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(fileTextField, gridBagConstraints);

        buttonGroup1.add(createNewRadioButton);
        createNewRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createNewRadioButton, org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.createNewRadioButton.text")); // NOI18N
        createNewRadioButton.setEnabled(false);
        createNewRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                createNewRadioButtonItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 0, 0);
        add(createNewRadioButton, gridBagConstraints);
        createNewRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.createNewRadioButton.AccessibleContext.accessibleDescription")); // NOI18N

        buttonGroup1.add(useExistingRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(useExistingRadioButton, org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.useExistingRadioButton.text")); // NOI18N
        useExistingRadioButton.setEnabled(false);
        useExistingRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                useExistingRadioButtonItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 15, 0, 0);
        add(useExistingRadioButton, gridBagConstraints);
        useExistingRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.useExistingRadioButton.AccessibleContext.accessibleDescription")); // NOI18N

        existingNameLabel.setLabelFor(existingNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(existingNameLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.existingNameLabel.text")); // NOI18N
        existingNameLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
        add(existingNameLabel, gridBagConstraints);
        existingNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.existingNameLabel.AccessibleContext.accessibleDescription")); // NOI18N

        existingNameTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(existingNameTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(chooseButton, org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.chooseButton.text")); // NOI18N
        chooseButton.setEnabled(false);
        chooseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(chooseButton, gridBagConstraints);
        chooseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.chooseButton.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(filler1, gridBagConstraints);

        createdLocation.setLabelFor(createdLocationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(createdLocation, org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.createdLocation.text")); // NOI18N
        createdLocation.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 0);
        add(createdLocation, gridBagConstraints);
        createdLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.locationLabel.AccessibleContext.accessibleDescription")); // NOI18N

        createdLocationComboBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(createdLocationComboBox, gridBagConstraints);

        createdPackage.setLabelFor(createdPackageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(createdPackage, org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.createdPackage.text")); // NOI18N
        createdPackage.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 0);
        add(createdPackage, gridBagConstraints);
        createdPackage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "ConfigureFXMLControllerPanelVisual.packageLabel.AccessibleContext.accessibleDescription")); // NOI18N

        createdPackageComboBox.setEditable(true);
        createdPackageComboBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(createdPackageComboBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void controllerCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_controllerCheckBoxItemStateChanged
        createNewRadioButton.setEnabled(controllerCheckBox.isSelected());
        if (createNewRadioButton.isSelected()) {
            createdNameLabel.setEnabled(controllerCheckBox.isSelected());
            createdNameTextField.setEnabled(controllerCheckBox.isSelected());
            createdLocation.setEnabled(controllerCheckBox.isSelected());
            createdLocationComboBox.setEnabled(controllerCheckBox.isSelected());
            createdPackage.setEnabled(controllerCheckBox.isSelected());
            createdPackageComboBox.setEnabled(controllerCheckBox.isSelected());            
        }
        useExistingRadioButton.setEnabled(controllerCheckBox.isSelected());
        if (useExistingRadioButton.isSelected()) {
            existingNameLabel.setEnabled(controllerCheckBox.isSelected());
            existingNameTextField.setEnabled(controllerCheckBox.isSelected());
            chooseButton.setEnabled(controllerCheckBox.isSelected());
        }
        fileLabel.setEnabled(controllerCheckBox.isSelected());
        updateResult();
        fireChange();
    }//GEN-LAST:event_controllerCheckBoxItemStateChanged

    private void chooseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseButtonActionPerformed
        JFileChooser chooser = new JFileChooser(new FXMLTemplateWizardIterator.SrcFileSystemView(support.getSourceGroupsAsFiles()));
        chooser.setDialogTitle(NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "LBL_ConfigureFXMLPanel_FileChooser_Select_Controller")); // NOI18N
        chooser.setFileFilter(FXMLTemplateWizardIterator.FXMLTemplateFileFilter.createJavaFilter());
        String existingPath = existingNameTextField.getText();
        if (existingPath.length() > 0) {
            File f = new File(support.getCurrentChooserFolder().getPath() + File.pathSeparator + existingPath);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            } else {
                chooser.setCurrentDirectory(FileUtil.toFile(support.getCurrentChooserFolder()));
            }
        } else {
            chooser.setCurrentDirectory(FileUtil.toFile(support.getCurrentChooserFolder()));
        }
        
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            String controllerClass = FileUtil.normalizeFile(chooser.getSelectedFile()).getPath();
            // XXX check other roots ?
            final String srcPath = FileUtil.normalizeFile(FileUtil.toFile(support.getCurrentSourceGroupFolder())).getPath();
            final String relativePath = controllerClass.substring(srcPath.length() + 1);
            final String relativePathWithoutExt = relativePath.substring(0, relativePath.indexOf(FXMLTemplateWizardIterator.JAVA_FILE_EXTENSION));
            existingNameTextField.setText(relativePathWithoutExt.replace(File.separatorChar, '.')); // NOI18N
        }
    }//GEN-LAST:event_chooseButtonActionPerformed

    private void createNewRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_createNewRadioButtonItemStateChanged
        radioButtonsStateChanged();
    }//GEN-LAST:event_createNewRadioButtonItemStateChanged

    private void useExistingRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_useExistingRadioButtonItemStateChanged
        radioButtonsStateChanged();
    }//GEN-LAST:event_useExistingRadioButtonItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton chooseButton;
    private javax.swing.JCheckBox controllerCheckBox;
    private javax.swing.JRadioButton createNewRadioButton;
    private javax.swing.JLabel createdLocation;
    private javax.swing.JComboBox createdLocationComboBox;
    private javax.swing.JLabel createdNameLabel;
    private javax.swing.JTextField createdNameTextField;
    private javax.swing.JLabel createdPackage;
    private javax.swing.JComboBox createdPackageComboBox;
    private javax.swing.JLabel existingNameLabel;
    private javax.swing.JTextField existingNameTextField;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JRadioButton useExistingRadioButton;
    // End of variables declaration//GEN-END:variables

    // ActionListener implementation -------------------------------------------
    @Override
    public void actionPerformed(ActionEvent e) {
        if (createdLocationComboBox == e.getSource()) {
            if (!ignoreRootCombo) {
                updatePackages();
            }
            updateText();
            updateResult();
            fireChange();
        } else if (createdPackageComboBox == e.getSource()) {
            updateText();
            updateResult();
            fireChange();
        } else if (createdPackageComboBox.getEditor() == e.getSource()) {
            updateText();
            updateResult();
            fireChange();
        }
    }

    // DocumentListener implementation -----------------------------------------
    @Override
    public void changedUpdate(DocumentEvent e) {
        updateResult();
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
        final Object item = createdLocationComboBox.getSelectedItem();
        if (!(item instanceof SourceGroupSupport.SourceGroupProxy)) {
            return;
        }
        WAIT_MODEL.setSelectedItem(createdPackageComboBox.getEditor().getItem());
        createdPackageComboBox.setModel(WAIT_MODEL);

        if (updatePackagesTask != null) {
            updatePackagesTask.cancel();
        }

        updatePackagesTask = new RequestProcessor("ComboUpdatePackages").post(new Runnable() { // NOI18N
            @Override
            public void run() {
                final ComboBoxModel model = ((SourceGroupSupport.SourceGroupProxy) item).getPackagesComboBoxModel();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        model.setSelectedItem(createdPackageComboBox.getEditor().getItem());
                        createdPackageComboBox.setModel(model);
                    }
                });
            }
        });
    }
    
    private void updateText() {
        String controllerName = getNewControllerName();
        if (controllerName == null  || controllerName.equals(previousControllerName)) {
            controllerName = support.getParent().getCurrentFileName();
            if (controllerName.contains(SPACE_CHAR)) {
                String[] splittedName = controllerName.trim().split(SPACE_CHAR);
                StringBuilder sb = new StringBuilder();
                for (String part : splittedName) {
                    sb.append(String.valueOf(part.charAt(0)).toUpperCase());
                    sb.append(part.substring(1));
                }
                controllerName = sb.toString() + NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "TXT_FileNameControllerPostfix"); // NOI18N;
            } else {
                String firstChar = String.valueOf(controllerName.charAt(0)).toUpperCase();
                String otherChars = controllerName.substring(1);
                controllerName = firstChar + otherChars + NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "TXT_FileNameControllerPostfix"); // NOI18N
            }
            createdNameTextField.setText(controllerName);
            previousControllerName = controllerName;
        }
    }
    
    private void updateResult() {
        String controllerName = shouldCreateController() ? getNewControllerName() : getExistingControllerName();
        if (controllerName == null) {
            fileTextField.setText(null);
            return;
        }

        if (shouldCreateController()) {
            final Object selectedItem = createdLocationComboBox.getSelectedItem();
            String createdFileName;
            if (selectedItem instanceof SourceGroupSupport.SourceGroupProxy) {
                SourceGroupSupport.SourceGroupProxy g = (SourceGroupSupport.SourceGroupProxy) selectedItem;
                String packageName = getPackageName();
                support.setCurrentSourceGroup(g);
                support.setCurrentPackageName(packageName);
                support.setCurrentFileName(controllerName);
                if (controllerName != null && controllerName.length() > 0) {
                    controllerName = controllerName + FXMLTemplateWizardIterator.JAVA_FILE_EXTENSION;
                }
                String path = support.getCurrentPackagePath();
                createdFileName = path == null ? "" : path.replace(".", "/") + controllerName;
            } else {
                //May be null if nothing selected
                createdFileName = "";   //NOI18N
            }
            fileTextField.setText(createdFileName.replace('/', File.separatorChar)); // NOI18N

        } else {
            fileTextField.setText(getPathForExistingController(controllerName));
        }
    }

    private String getPathForExistingController(String controllerName) {
        assert controllerName != null;
        return FileUtil.normalizeFile(FileUtil.toFile(support.getCurrentSourceGroupFolder())).getPath() + File.separatorChar
                + controllerName.replace('.', File.separatorChar) + FXMLTemplateWizardIterator.JAVA_FILE_EXTENSION;
    }
    
    /**
     * Returns error message or null if no error occurred
     */
    String isControllerValid() {
        if(!isControllerEnabled()) {
            return null;
        }
        if (createNewRadioButton.isSelected()) {
            if (!Utilities.isJavaIdentifier(getNewControllerName())) {
                return NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "WARN_ConfigureFXMLPanel_Provide_Java_Name"); // NOI18N
            }
            return FXMLTemplateWizardIterator.canUseFileName(FileUtil.toFile(support.getCurrentChooserFolder()), getNewControllerName());
        }
        
        if (existingNameTextField.getText().isEmpty()) {
            return NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, "WARN_ConfigureFXMLPanel_Provide_Java_Name"); // NOI18N
        }
        
        return FXMLTemplateWizardIterator.fileExist(getPathForExistingController(getExistingControllerName()));
    }

    boolean shouldCreateController() {
        return controllerCheckBox.isSelected() && createNewRadioButton.isSelected();
    }

    static class Panel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor> {
        
        private ConfigureFXMLControllerPanelVisual component;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private WizardDescriptor settings;
        SourceGroupSupport support;

        public Panel(SourceGroupSupport support, boolean isMaven) {
            this.support = support;
            component = new ConfigureFXMLControllerPanelVisual(this, support, isMaven);
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
            FileObject preselectedFolder = Templates.getTargetFolder(settings);
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
            if (isValid()) {
                settings.putProperty(FXMLTemplateWizardIterator.PROP_JAVA_CONTROLLER_ENABLED, component.isControllerEnabled());
                settings.putProperty(FXMLTemplateWizardIterator.PROP_JAVA_CONTROLLER_NAME_PROPERTY, 
                    component.shouldCreateController() ? component.getNewControllerName() : null);
                settings.putProperty(FXMLTemplateWizardIterator.PROP_JAVA_CONTROLLER_EXISTING_PROPERTY, 
                    component.getExistingControllerName());
            }
            settings.putProperty("NewFileWizard_Title", null); // NOI18N
        }

        @Override
        public boolean isValid() {
            if (component.isControllerEnabled()) {
                if (!FXMLTemplateWizardIterator.isValidPackageName(component.getPackageName())) {
                    FXMLTemplateWizardIterator.setErrorMessage("WARN_ConfigureFXMLPanel_Provide_Package_Name", settings); // NOI18N
                    return false;
                }

                if (JFXProjectUtils.hasModuleInfo(support) && component.getPackageName().isEmpty()) {
                    FXMLTemplateWizardIterator.setErrorMessage("WARN_ConfigureFXMLPanel_Default_Package_Invalid", settings); // NOI18N
                    return false;
                }

                if (!FXMLTemplateWizardIterator.isValidPackage(component.getLocationFolder(), component.getPackageName())) {
                    FXMLTemplateWizardIterator.setErrorMessage("WARN_ConfigureFXMLPanel_Package_Invalid", settings); // NOI18N
                    return false;
                }

                String errorMessage = component.isControllerValid();
                settings.getNotificationLineSupport().setErrorMessage(errorMessage);
                return errorMessage == null;
            } else {
                settings.getNotificationLineSupport().setErrorMessage(null);
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
