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
import org.netbeans.api.java.project.JavaProjectConstants;
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

/**
 *
 * @author Anton Chechel <anton.chechel@oracle.com>
 * @author Roman Svitanic
 */
public class ConfigureFXMLCSSPanelVisual extends JPanel implements ActionListener, DocumentListener {
    
    private Panel observer;
    private final boolean isMaven;
    private boolean ignoreRootCombo;
    private RequestProcessor.Task updatePackagesTask;
    private static final ComboBoxModel WAIT_MODEL = SourceGroupSupport.getWaitModel();
    SourceGroupSupport support;
    private String previousCssName;

    ConfigureFXMLCSSPanelVisual(Panel observer, SourceGroupSupport support, boolean isMaven) {
        this.support = support;
        this.observer = observer;
        this.isMaven = isMaven;
        setName(NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class,"TXT_CSSNameAndLoc")); // NOI18N
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
                        NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class,
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
        if(isMaven && support.getType().equals(JavaProjectConstants.SOURCES_TYPE_RESOURCES)) {
            createdPackageComboBox.getEditor().setItem(FXMLTemplateWizardIterator.defaultMavenCSSPackage);
        } else {
            createdPackageComboBox.getEditor().setItem(support.getParent().getCurrentPackageName());
        }
        updatePackages();
        updateText();
        updateResult();
    }
    
    boolean isCSSEnabled() {
        return cssCheckBox.isSelected();
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

    String getNewCSSName() {
        String text = createdNameTextField.getText().trim();
        return text.length() == 0 ? null : text;
    }

    String getExistingCSSName() {
        String text = existingNameTextField.getText().trim();
        return text.length() == 0 ? null : text;
    }

    private void radioButtonsStateChanged() {
        if (!cssCheckBox.isSelected()) {
            return;
        }
        createdNameLabel.setEnabled(createNewRadioButton.isSelected());
        createdNameTextField.setEnabled(createNewRadioButton.isSelected());
        existingNameLabel.setEnabled(!createNewRadioButton.isSelected());
        existingNameTextField.setEnabled(!createNewRadioButton.isSelected());
        chooseButton.setEnabled(!createNewRadioButton.isSelected());

        createdLocationLabel.setEnabled(createNewRadioButton.isSelected());
        createdLocationComboBox.setEnabled(createNewRadioButton.isSelected());
        createdPackageLabel.setEnabled(createNewRadioButton.isSelected());
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
        createNewRadioButton = new javax.swing.JRadioButton();
        cssCheckBox = new javax.swing.JCheckBox();
        createdNameLabel = new javax.swing.JLabel();
        createdNameTextField = new javax.swing.JTextField();
        fileLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        createdLocationLabel = new javax.swing.JLabel();
        createdLocationComboBox = new javax.swing.JComboBox();
        createdPackageLabel = new javax.swing.JLabel();
        createdPackageComboBox = new javax.swing.JComboBox();
        useExistingRadioButton = new javax.swing.JRadioButton();
        existingNameLabel = new javax.swing.JLabel();
        existingNameTextField = new javax.swing.JTextField();
        chooseButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));

        setPreferredSize(new java.awt.Dimension(500, 340));
        setLayout(new java.awt.GridBagLayout());

        buttonGroup1.add(createNewRadioButton);
        createNewRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(createNewRadioButton, org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.createNewRadioButton.text")); // NOI18N
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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 0, 0);
        add(createNewRadioButton, gridBagConstraints);
        createNewRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.createNewRadioButton.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cssCheckBox, org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.cssCheckBox.text")); // NOI18N
        cssCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cssCheckBoxItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        add(cssCheckBox, gridBagConstraints);
        cssCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.cssCheckBox.AccessibleContext.accessibleDescription")); // NOI18N

        createdNameLabel.setLabelFor(createdNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(createdNameLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.createdNameLabel.text")); // NOI18N
        createdNameLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
        add(createdNameLabel, gridBagConstraints);
        createdNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.createdNameLabel.AccessibleContext.accessibleDescription")); // NOI18N

        createdNameTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(createdNameTextField, gridBagConstraints);

        fileLabel.setLabelFor(fileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fileLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.fileLabel.text")); // NOI18N
        fileLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(25, 15, 0, 0);
        add(fileLabel, gridBagConstraints);
        fileLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.fileLabel.AccessibleContext.accessibleDescription")); // NOI18N

        fileTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(fileTextField, gridBagConstraints);

        createdLocationLabel.setLabelFor(createdLocationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(createdLocationLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.createdLocationLabel.text")); // NOI18N
        createdLocationLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 0);
        add(createdLocationLabel, gridBagConstraints);
        createdLocationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.createdLocationLabel.AccessibleContext.accessibleDescription")); // NOI18N

        createdLocationComboBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(createdLocationComboBox, gridBagConstraints);

        createdPackageLabel.setLabelFor(createdPackageComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(createdPackageLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.createdPackageLabel.text")); // NOI18N
        createdPackageLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 40, 0, 0);
        add(createdPackageLabel, gridBagConstraints);
        createdPackageLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.createdPackageLabel.AccessibleContext.accessibleDescription")); // NOI18N

        createdPackageComboBox.setEditable(true);
        createdPackageComboBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        add(createdPackageComboBox, gridBagConstraints);

        buttonGroup1.add(useExistingRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(useExistingRadioButton, org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.useExistingRadioButton.text")); // NOI18N
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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(10, 15, 0, 0);
        add(useExistingRadioButton, gridBagConstraints);
        useExistingRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.useExistingRadioButton.AccessibleContext.accessibleDescription")); // NOI18N

        existingNameLabel.setLabelFor(existingNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(existingNameLabel, org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.existingNameLabel.text")); // NOI18N
        existingNameLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 0, 0);
        add(existingNameLabel, gridBagConstraints);
        existingNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.existingNameLabel.AccessibleContext.accessibleDescription")); // NOI18N

        existingNameTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(existingNameTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(chooseButton, org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.chooseButton.text")); // NOI18N
        chooseButton.setEnabled(false);
        chooseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(chooseButton, gridBagConstraints);
        chooseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "ConfigureFXMLCSSPanelVisual.chooseButton.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 0.5;
        add(filler1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void cssCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cssCheckBoxItemStateChanged
        createNewRadioButton.setEnabled(cssCheckBox.isSelected());
        if (createNewRadioButton.isSelected()) {
            createdNameLabel.setEnabled(cssCheckBox.isSelected());
            createdNameTextField.setEnabled(cssCheckBox.isSelected());
            createdLocationLabel.setEnabled(cssCheckBox.isSelected());
            createdLocationComboBox.setEnabled(cssCheckBox.isSelected());
            createdPackageLabel.setEnabled(cssCheckBox.isSelected());
            createdPackageComboBox.setEnabled(cssCheckBox.isSelected());            
        }
        useExistingRadioButton.setEnabled(cssCheckBox.isSelected());
        if (useExistingRadioButton.isSelected()) {
            existingNameLabel.setEnabled(cssCheckBox.isSelected());
            existingNameTextField.setEnabled(cssCheckBox.isSelected());
            chooseButton.setEnabled(cssCheckBox.isSelected());
        }
        fileLabel.setEnabled(cssCheckBox.isSelected());
        updateResult();
        fireChange();
    }//GEN-LAST:event_cssCheckBoxItemStateChanged

    private void chooseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseButtonActionPerformed
        JFileChooser chooser = new JFileChooser(new FXMLTemplateWizardIterator.SrcFileSystemView(support.getSourceGroupsAsFiles()));
        chooser.setDialogTitle(NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class, "LBL_ConfigureFXMLPanel_FileChooser_Select_CSS")); // NOI18N
        chooser.setFileFilter(FXMLTemplateWizardIterator.FXMLTemplateFileFilter.createCSSFilter());
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
            String cssFile = FileUtil.normalizeFile(chooser.getSelectedFile()).getPath();
            // XXX check other roots ?
            final String srcPath = FileUtil.normalizeFile(FileUtil.toFile(support.getCurrentSourceGroupFolder())).getPath();
            final String relativePath = cssFile.substring(srcPath.length() + 1);
            existingNameTextField.setText(relativePath);
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
    private javax.swing.JRadioButton createNewRadioButton;
    private javax.swing.JComboBox createdLocationComboBox;
    private javax.swing.JLabel createdLocationLabel;
    private javax.swing.JLabel createdNameLabel;
    private javax.swing.JTextField createdNameTextField;
    private javax.swing.JComboBox createdPackageComboBox;
    private javax.swing.JLabel createdPackageLabel;
    private javax.swing.JCheckBox cssCheckBox;
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
        String cssName = getNewCSSName();
        if (cssName == null  || cssName.equals(previousCssName)) {
            cssName = support.getParent().getCurrentFileName().toLowerCase() + FXMLTemplateWizardIterator.CSS_FILE_EXTENSION;
            createdNameTextField.setText(cssName);
            previousCssName = cssName;
        }
    }

    private void updateResult() {
        String cssName = shouldCreateCSS() ? getNewCSSName() : getExistingCSSName();
        if (cssName == null) {
            fileTextField.setText(null);
            return;
        }

        if (shouldCreateCSS()) {
            final Object selectedItem = createdLocationComboBox.getSelectedItem();
            String createdFileName;
            if (selectedItem instanceof SourceGroupSupport.SourceGroupProxy) {
                SourceGroupSupport.SourceGroupProxy g = (SourceGroupSupport.SourceGroupProxy) selectedItem;
                String packageName = getPackageName();
                support.setCurrentSourceGroup(g);
                support.setCurrentPackageName(packageName);
                support.setCurrentFileName(cssName);
                String path = support.getCurrentPackagePath();
                createdFileName = path == null ? "" : path.replace(".", "/") + cssName;
            } else {
                //May be null if nothing selected
                createdFileName = "";   //NOI18N
            }
            fileTextField.setText(createdFileName.replace('/', File.separatorChar)); // NOI18N

        } else {
            fileTextField.setText(getPathForExistingCSS(cssName));
        }
    }

    private String getPathForExistingCSS(String cssName) {
        assert cssName != null;
        if(cssName.toLowerCase().endsWith(FXMLTemplateWizardIterator.CSS_FILE_EXTENSION)) {
            String stripped = cssName.substring(0, cssName.length() - FXMLTemplateWizardIterator.CSS_FILE_EXTENSION.length());
            cssName = stripped.replace('.', File.separatorChar) + FXMLTemplateWizardIterator.CSS_FILE_EXTENSION;
        }
        return FileUtil.normalizeFile(FileUtil.toFile(support.getCurrentSourceGroupFolder())).getPath() + File.separatorChar + cssName;
    }
    
    /**
     * Returns error message or null if no error occurred
     */
    String isCSSValid() {
        if(!isCSSEnabled()) {
            return null;
        }
        if (createNewRadioButton.isSelected()) {
            return FXMLTemplateWizardIterator.canUseFileName(FileUtil.toFile(support.getCurrentChooserFolder()), getNewCSSName());
        }
        
        if (existingNameTextField.getText().isEmpty()) {
            return NbBundle.getMessage(ConfigureFXMLCSSPanelVisual.class,"WARN_ConfigureFXMLPanel_Provide_CSS_Name"); // NOI18N
        }
        
        return FXMLTemplateWizardIterator.fileExist(getPathForExistingCSS(getExistingCSSName()));
    }
    
    boolean shouldCreateCSS() {
        return cssCheckBox.isSelected() && createNewRadioButton.isSelected();
    }

    static class Panel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor> {
        
        private ConfigureFXMLCSSPanelVisual component;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private WizardDescriptor settings;
        SourceGroupSupport support;

        public Panel(SourceGroupSupport support, boolean isMaven) {
            this.support = support;
            component = new ConfigureFXMLCSSPanelVisual(this, support, isMaven);
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
                settings.putProperty(FXMLTemplateWizardIterator.PROP_CSS_ENABLED, component.isCSSEnabled());
                settings.putProperty(FXMLTemplateWizardIterator.PROP_CSS_NAME_PROPERTY, 
                    component.shouldCreateCSS() ? component.getNewCSSName() : null);
                settings.putProperty(FXMLTemplateWizardIterator.PROP_CSS_EXISTING_PROPERTY,
                    component.getExistingCSSName());
            }
            settings.putProperty("NewFileWizard_Title", null); // NOI18N
        }

        @Override
        public boolean isValid() {
            if (component.isCSSEnabled()) {
                if (!FXMLTemplateWizardIterator.isValidPackageName(component.getPackageName())) {
                    FXMLTemplateWizardIterator.setErrorMessage("WARN_ConfigureFXMLPanel_Provide_Package_Name", settings); // NOI18N
                    return false;
                }

                if (!FXMLTemplateWizardIterator.isValidPackage(component.getLocationFolder(), component.getPackageName())) {
                    FXMLTemplateWizardIterator.setErrorMessage("WARN_ConfigureFXMLPanel_Package_Invalid", settings); // NOI18N
                    return false;
                }

                String errorMessage = component.isCSSValid();
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
