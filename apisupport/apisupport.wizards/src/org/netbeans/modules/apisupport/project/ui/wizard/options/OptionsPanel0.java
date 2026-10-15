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

package org.netbeans.modules.apisupport.project.ui.wizard.options;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.common.WizardUtils;
import org.netbeans.modules.apisupport.project.ui.wizard.options.NewOptionsIterator.DataModel;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.AsyncGUIJob;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Radek Matous
 */
final class OptionsPanel0 extends BasicWizardIterator.Panel {
    private NewOptionsIterator.DataModel data;
    private DocumentListener fieldsDL;
    
    public OptionsPanel0(final WizardDescriptor setting, final NewOptionsIterator.DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        initAccessibility();
        putClientProperty("NewFileWizard_Title",// NOI18N
                NbBundle.getMessage(OptionsPanel0.class,"LBL_OptionsWizardTitle")); // NOI18N
        primaryPanelCombo.setModel(UIUtil.createComboWaitModel());
        Utilities.attachInitJob(primaryPanelCombo, new AsyncGUIJob() {
            ComboBoxModel model;
            @Override public void construct() {
                model = new DefaultComboBoxModel(getPrimaryIdsFromLayer());
            }
            @Override public void finished() {
                primaryPanelCombo.setModel(model);
            }
        });
    }
    
    /** Returns array of IDs of primary panels (categories) from project's layer. 
     * Advanced is added as first item.
     * @return array of IDs
     */
    private String[] getPrimaryIdsFromLayer() {
        ArrayList<String> primaryIds = new ArrayList<String>();
        try {
            FileSystem layerFS = data.getProject().getLookup().lookup(NbModuleProvider.class).getEffectiveSystemFilesystem();
            FileObject optionsDialogFO = layerFS.findResource("OptionsDialog"); //NOI18N
            if(optionsDialogFO != null) {
                FileObject[] children = optionsDialogFO.getChildren();
                for (int i = 0; i < children.length; i++) {
                    FileObject child = children[i];
                    if(!child.isFolder()) {
                        primaryIds.add(child.getName());
                    }
                }
                Collections.sort(primaryIds);
            }
        } catch (IOException x) {
            Logger.getLogger(OptionsPanel0.class.getName()).log(Level.INFO, null, x);
        }
        primaryIds.remove("Advanced"); // NOI18N
        primaryIds.add(0, "Advanced"); // NOI18N
        return primaryIds.toArray(new String[0]);
    }
    
    private void addListeners() {
        if (fieldsDL == null) {
            fieldsDL = new UIUtil.DocumentAdapter() {
                public void insertUpdate(DocumentEvent e) { updateData(); }
            };
            
            categoryNameField.getDocument().addDocumentListener(fieldsDL);
            secondaryPanelTitle.getDocument().addDocumentListener(fieldsDL);
            iconField.getDocument().addDocumentListener(fieldsDL);
            primaryKwField.getDocument().addDocumentListener(fieldsDL);
            secondaryKwField.getDocument().addDocumentListener(fieldsDL);
            if(primaryPanelCombo.getEditor().getEditorComponent() instanceof JTextField) {
                ((JTextField)primaryPanelCombo.getEditor().getEditorComponent()).getDocument().addDocumentListener(fieldsDL);
            }
        }
    }
    
    private void removeListeners() {
        if (fieldsDL != null) {        
            categoryNameField.getDocument().removeDocumentListener(fieldsDL);
            secondaryPanelTitle.getDocument().removeDocumentListener(fieldsDL);
            iconField.getDocument().removeDocumentListener(fieldsDL);
            fieldsDL = null;
        }
    }
    
    
    protected void storeToDataModel() {
        removeListeners();
        updateData();
    }
    protected void readFromDataModel() {
        addListeners();
    }

    private void updateData() {
        try {
            //do not allow platforms older then 6.5
            SpecificationVersion apiVersion = data.getModuleInfo().getDependencyVersion("org.netbeans.modules.options.api");
            if (apiVersion == null || apiVersion.compareTo(new SpecificationVersion("1.10")) < 0) { // NOI18N
                setError(NbBundle.getMessage(OptionsPanel0.class, "MSG_INVALID_PLATFORM")); // NOI18N
                return;
            }
        } catch (IOException x) {
            Logger.getLogger(OptionsPanel0.class.getName()).log(Level.INFO, null, x);
        }

        int retCode = 0;
        if (advancedButton.isSelected()) {
            assert !optionsCategoryButton.isSelected();
            retCode = data.setDataForSecondaryPanel(
                    primaryPanelCombo.getEditor().getItem().toString(),
                    secondaryPanelTitle.getText(),
                    secondaryKwField.getText());
        } else {
            assert optionsCategoryButton.isSelected();
            File icon = FileUtil.normalizeFile(new File(iconField.getText()));
            retCode = data.setDataForPrimaryPanel(
                    categoryNameField.getText(),
                    icon,
                    allowSecondaryPanelsCheckBox.isSelected(),
                    primaryKwField.getText());
        }
        
        String msg = data.getMessage(retCode);
        if (DataModel.isSuccessCode(retCode)) {
            markValid();
        } else if (DataModel.isErrorCode(retCode)) {
            setError(msg);
        }  else if (DataModel.isWarningCode(retCode)) {
            setWarning(msg);
        } else if (DataModel.isInfoCode(retCode)) {
            setInfo(msg, false);
        } else {
            assert false : retCode;
        }
    }
    
    protected String getPanelName() {
        return NbBundle.getMessage(OptionsPanel0.class,"LBL_OptionsPanel0_Title"); // NOI18N
    }
    
    
    protected HelpCtx getHelp() {
        return new HelpCtx(OptionsPanel0.class);
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(OptionsPanel0.class, key);
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_OptionsPanel0"));
        advancedButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_Advanced"));
        optionsCategoryButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_OptionsCategory"));
        secondaryPanelTitle.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_DisplayName"));
        categoryNameField.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_CategoryName"));
        iconField.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_IconPath"));
        iconButton.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_IconButton"));
        allowSecondaryPanelsCheckBox.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_AllowSecondaryPanels"));
        primaryPanelCombo.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_PrimaryPanelCombo"));
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        addListeners();
        updateData();
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        removeListeners();
    }
    
    private void enableDisable() {
        boolean advancedEnabled = advancedButton.isSelected();
        assert advancedEnabled != optionsCategoryButton.isSelected();
        
        categoryNameField.setEnabled(!advancedEnabled);
        categoryNameLbl.setEnabled(!advancedEnabled);
        iconButton.setEnabled(!advancedEnabled);
        iconField.setEnabled(!advancedEnabled);
        iconLbl.setEnabled(!advancedEnabled);
        primaryKwField.setEnabled(!advancedEnabled);
        primKeywordsLabel.setEnabled(!advancedEnabled);
        allowSecondaryPanelsCheckBox.setEnabled(!advancedEnabled);
    
        primaryPanelComboLbl.setEnabled(advancedEnabled);
        primaryPanelCombo.setEnabled(advancedEnabled);
        secondaryPanelTitle.setEnabled(advancedEnabled);
        secondaryKwField.setEditable(advancedEnabled);
        keywordsLabel.setEnabled(advancedEnabled);
        displayNameLbl1.setEnabled(advancedEnabled);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        advancedButton = new javax.swing.JRadioButton();
        optionsCategoryButton = new javax.swing.JRadioButton();
        dummyPanel = new javax.swing.JPanel();
        categoryNameLbl = new javax.swing.JLabel();
        categoryNameField = new javax.swing.JTextField();
        displayNameLbl1 = new javax.swing.JLabel();
        secondaryPanelTitle = new javax.swing.JTextField();
        iconLbl = new javax.swing.JLabel();
        iconField = new javax.swing.JTextField();
        iconButton = new javax.swing.JButton();
        allowSecondaryPanelsCheckBox = new javax.swing.JCheckBox();
        primaryPanelComboLbl = new javax.swing.JLabel();
        primaryPanelCombo = new javax.swing.JComboBox();
        keywordsLabel = new javax.swing.JLabel();
        primKeywordsLabel = new javax.swing.JLabel();
        secondaryKwField = new javax.swing.JTextField();
        primaryKwField = new javax.swing.JTextField();

        buttonGroup1.add(advancedButton);
        advancedButton.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/options/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(advancedButton, bundle.getString("LBL_Advanced")); // NOI18N
        advancedButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        advancedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advancedButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(optionsCategoryButton);
        org.openide.awt.Mnemonics.setLocalizedText(optionsCategoryButton, bundle.getString("LBL_OptionsCategory")); // NOI18N
        optionsCategoryButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        optionsCategoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionsCategoryButtonActionPerformed(evt);
            }
        });

        categoryNameLbl.setLabelFor(categoryNameField);
        org.openide.awt.Mnemonics.setLocalizedText(categoryNameLbl, bundle.getString("LBL_CategoryName")); // NOI18N
        categoryNameLbl.setEnabled(false);

        categoryNameField.setEnabled(false);

        displayNameLbl1.setLabelFor(secondaryPanelTitle);
        org.openide.awt.Mnemonics.setLocalizedText(displayNameLbl1, bundle.getString("LBL_DisplaName")); // NOI18N

        iconLbl.setLabelFor(iconField);
        org.openide.awt.Mnemonics.setLocalizedText(iconLbl, org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "LBL_Icon")); // NOI18N
        iconLbl.setEnabled(false);

        iconField.setEditable(false);
        iconField.setText(org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "CTL_None")); // NOI18N
        iconField.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(iconButton, org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "LBL_Icon_Browse")); // NOI18N
        iconButton.setEnabled(false);
        iconButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iconButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(allowSecondaryPanelsCheckBox, org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "LBL_AllowSecondaryPanels")); // NOI18N
        allowSecondaryPanelsCheckBox.setEnabled(false);
        allowSecondaryPanelsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allowSecondaryPanelsCheckBoxActionPerformed(evt);
            }
        });

        primaryPanelComboLbl.setLabelFor(primaryPanelCombo);
        org.openide.awt.Mnemonics.setLocalizedText(primaryPanelComboLbl, org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "LBL_PrimaryPanelCombo")); // NOI18N

        primaryPanelCombo.setEditable(true);
        primaryPanelCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                primaryPanelComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(keywordsLabel, org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "LBL_Keywords")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(primKeywordsLabel, org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "LBL_Keywords")); // NOI18N
        primKeywordsLabel.setEnabled(false);

        secondaryKwField.setText(org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "OptionsPanel0.secondaryKwField.text")); // NOI18N

        primaryKwField.setText(org.openide.util.NbBundle.getMessage(OptionsPanel0.class, "OptionsPanel0.primaryKwField.text")); // NOI18N
        primaryKwField.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(advancedButton, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(98, 98, 98))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(categoryNameLbl)
                    .addComponent(iconLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(iconField, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(iconButton)
                        .addGap(18, 18, 18))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(categoryNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 418, Short.MAX_VALUE)
                        .addContainerGap())))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(displayNameLbl1)
                            .addComponent(primaryPanelComboLbl)
                            .addComponent(keywordsLabel))
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(secondaryKwField, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                            .addComponent(primaryPanelCombo, 0, 408, Short.MAX_VALUE)
                            .addComponent(secondaryPanelTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE))
                        .addGap(10, 10, 10))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(235, 235, 235)
                        .addComponent(dummyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(optionsCategoryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(primKeywordsLabel)
                        .addGap(49, 49, 49)
                        .addComponent(primaryKwField, javax.swing.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(allowSecondaryPanelsCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 338, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(advancedButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(primaryPanelComboLbl)
                    .addComponent(primaryPanelCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(displayNameLbl1)
                    .addComponent(secondaryPanelTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(keywordsLabel)
                    .addComponent(secondaryKwField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(optionsCategoryButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(categoryNameLbl)
                    .addComponent(categoryNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(iconLbl)
                    .addComponent(iconField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iconButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(primKeywordsLabel)
                    .addComponent(primaryKwField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(allowSecondaryPanelsCheckBox)
                .addGap(68, 68, 68)
                .addComponent(dummyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        advancedButton.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.advancedButton.AccessibleContext.accessibleDescription")); // NOI18N
        optionsCategoryButton.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.optionsCategoryButton.AccessibleContext.accessibleDescription")); // NOI18N
        dummyPanel.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel0.dummyPanel.AccessibleContext.accessibleName")); // NOI18N
        dummyPanel.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.dummyPanel.AccessibleContext.accessibleDescription")); // NOI18N
        categoryNameLbl.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.categoryNameLbl.AccessibleContext.accessibleDescription")); // NOI18N
        categoryNameField.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel0.categoryNameField.AccessibleContext.accessibleName")); // NOI18N
        categoryNameField.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.categoryNameField.AccessibleContext.accessibleDescription")); // NOI18N
        displayNameLbl1.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.displayNameLbl1.AccessibleContext.accessibleDescription")); // NOI18N
        secondaryPanelTitle.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel0.displayNameField1.AccessibleContext.accessibleName")); // NOI18N
        secondaryPanelTitle.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.displayNameField1.AccessibleContext.accessibleDescription")); // NOI18N
        iconLbl.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.iconLbl.AccessibleContext.accessibleDescription")); // NOI18N
        iconField.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel0.iconField.AccessibleContext.accessibleName")); // NOI18N
        iconField.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.iconField.AccessibleContext.accessibleDescription")); // NOI18N
        iconButton.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.iconButton.AccessibleContext.accessibleDescription")); // NOI18N
        allowSecondaryPanelsCheckBox.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.allowSecondaryPanelsCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        primaryPanelComboLbl.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.primaryPanelComboLbl.AccessibleContext.accessibleDescription")); // NOI18N
        primaryPanelCombo.getAccessibleContext().setAccessibleName(getMessage("OptionsPanel0.primaryPanelCombo.AccessibleContext.accessibleName")); // NOI18N
        primaryPanelCombo.getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.primaryPanelCombo.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(getMessage("OptionsPanel0.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(getMessage("OptionsPanel0.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void optionsCategoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsCategoryButtonActionPerformed
        enableDisable();
        updateData();
    }//GEN-LAST:event_optionsCategoryButtonActionPerformed
    
    private void advancedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advancedButtonActionPerformed
        enableDisable();
        updateData();
    }//GEN-LAST:event_advancedButtonActionPerformed
    
    private void iconButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iconButtonActionPerformed
        JFileChooser chooser = WizardUtils.getIconFileChooser(iconField.getText());
        int ret = chooser.showDialog(this, getMessage("LBL_Select")); // NOI18N
        if (ret == JFileChooser.APPROVE_OPTION) {
            iconField.setText(chooser.getSelectedFile().getAbsolutePath());
            updateData();
        }
    }//GEN-LAST:event_iconButtonActionPerformed

private void allowSecondaryPanelsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allowSecondaryPanelsCheckBoxActionPerformed
    updateData();
}//GEN-LAST:event_allowSecondaryPanelsCheckBoxActionPerformed

private void primaryPanelComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_primaryPanelComboActionPerformed
    updateData();
}//GEN-LAST:event_primaryPanelComboActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton advancedButton;
    private javax.swing.JCheckBox allowSecondaryPanelsCheckBox;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField categoryNameField;
    private javax.swing.JLabel categoryNameLbl;
    private javax.swing.JLabel displayNameLbl1;
    private javax.swing.JPanel dummyPanel;
    private javax.swing.JButton iconButton;
    private javax.swing.JTextField iconField;
    private javax.swing.JLabel iconLbl;
    private javax.swing.JLabel keywordsLabel;
    private javax.swing.JRadioButton optionsCategoryButton;
    private javax.swing.JLabel primKeywordsLabel;
    private javax.swing.JTextField primaryKwField;
    private javax.swing.JComboBox primaryPanelCombo;
    private javax.swing.JLabel primaryPanelComboLbl;
    private javax.swing.JTextField secondaryKwField;
    private javax.swing.JTextField secondaryPanelTitle;
    // End of variables declaration//GEN-END:variables
    
}
