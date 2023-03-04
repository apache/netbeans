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

package org.netbeans.modules.apisupport.project.ui.wizard.updatecenter;

import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.common.WizardUtils;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * The panel in the <em>New Update Center Wizard</em>.
 *
 * @author Jiri Rechtacek
 */
final class UpdateCenterRegistrationPanel extends BasicWizardIterator.Panel {
    
    private DataModel data;
    private DocumentListener updateListener;
    
    /**
     * Creates new UpdateCenterRegistrationPanel
     */
    public UpdateCenterRegistrationPanel(final WizardDescriptor setting, final DataModel data) {
        super(setting);
        this.data = data;
        initComponents();

        putClientProperty("NewFileWizard_Title", getMessage("LBL_NewUpdateCenterWizardTitle")); //NOI18N
        
        updateListener = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                updateData();
            }
        };
    }
    
    private void addListeners() {
        ucUrl.getDocument ().addDocumentListener (updateListener);
        displayName.getDocument ().addDocumentListener (updateListener);
    }
    
    private void removeListeners() {
        ucUrl.getDocument ().removeDocumentListener (updateListener);
        displayName.getDocument ().removeDocumentListener (updateListener);
    }
    
    protected String getPanelName() {
        return getMessage("LBL_UpdateCenterRegistrationPanel_Title"); //NOI18N
    }
    
    protected void storeToDataModel() {
        removeListeners();
        storeBaseData();
    }
    
    protected void readFromDataModel() {
        updateData();
        addListeners();
    }
    
    void updateData() {
        storeBaseData ();
        if (checkValidity ()) {
            CreatedModifiedFiles files = data.refreshCreatedModifiedFiles();
            createdFiles.setText(WizardUtils.generateTextAreaContent(files.getCreatedPaths()));
            modifiedFiles.setText(WizardUtils.generateTextAreaContent(files.getModifiedPaths()));
        }
    }
    
    /** Data needed to compute CMF */
    private void storeBaseData() {
        data.setUpdateCenterURL (ucUrl.getText ().trim ());
        data.setUpdateCenterDisplayName (displayName.getText ().trim ());
    }
    
    private boolean checkValidity() {
        boolean result = false;
        if (data.getUpdateCenterURL ().length () == 0) {
            setError(getMessage ("ERR_Url_Is_Empty")); //NOI18N
        } else if (data.getUpdateCenterDisplayName ().length () == 0) {
            setError(getMessage ("ERR_Empty_Display_Name")); //NOI18N
        } else {
            result = true;
            if (! data.getUpdateCenterURL ().endsWith (".xml")) {
                setWarning(getMessage("WRN_Url_dont_xml_file")); // NOI18N
            } else {
                
                try {
                    // try transform to URL
                    new URL (data.getUpdateCenterURL ());

                    // all is ok
                    markValid();
                } catch (MalformedURLException ex) {
                    setWarning(NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "WRN_Url_cannot_be_created", ex.getLocalizedMessage()));
                }
                
            }
        }

        return result;
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(UpdateCenterRegistrationPanel.class);
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(UpdateCenterRegistrationPanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        ucUrlLabel = new javax.swing.JLabel();
        ucUrl = new javax.swing.JTextField();
        displayNameLabel = new javax.swing.JLabel();
        displayName = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        project = new JTextField(ProjectUtils.getInformation(data.getProject()).getDisplayName());
        createdFilesLabel = new javax.swing.JLabel();
        createdFiles = new javax.swing.JTextArea();
        modifiedFilesLabel = new javax.swing.JLabel();
        modifiedFiles = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "ACN_UpdateCenterRegistrationPanel", new Object[] {}));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "ACD_UpdateCenterRegistrationPanel", new Object[] {}));
        ucUrlLabel.setLabelFor(ucUrl);
        org.openide.awt.Mnemonics.setLocalizedText(ucUrlLabel, org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "LBL_UpdateCenterURL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        add(ucUrlLabel, gridBagConstraints);
        ucUrlLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "ACD_UpdateCenterRegistrationPanel_ucUrlLabel", new Object[] {}));

        ucUrl.setText(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "CTL_SampleURL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(ucUrl, gridBagConstraints);

        displayNameLabel.setLabelFor(displayName);
        org.openide.awt.Mnemonics.setLocalizedText(displayNameLabel, org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "LBL_DisplayName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 12);
        add(displayNameLabel, gridBagConstraints);
        displayNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "ACD_UpdateCenterRegistrationPanel_displayNameLabel", new Object[] {}));

        displayName.setText(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "CTL_SampleName", new Object[] {project.getText ()}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(displayName, gridBagConstraints);

        projectLabel.setLabelFor(project);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "LBL_ProjectName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 12);
        add(projectLabel, gridBagConstraints);
        projectLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "ACD_UpdateCenterRegistrationPanel_projectLabel", new Object[] {}));

        project.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 0);
        add(project, gridBagConstraints);

        createdFilesLabel.setLabelFor(createdFiles);
        org.openide.awt.Mnemonics.setLocalizedText(createdFilesLabel, org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "LBL_CreatedFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 12);
        add(createdFilesLabel, gridBagConstraints);
        createdFilesLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "ACD_UpdateCenterRegistrationPanel_createdFilesLabel", new Object[] {}));

        createdFiles.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        createdFiles.setColumns(20);
        createdFiles.setEditable(false);
        createdFiles.setRows(5);
        createdFiles.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 0);
        add(createdFiles, gridBagConstraints);

        modifiedFilesLabel.setLabelFor(modifiedFiles);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedFilesLabel, org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "LBL_ModifiedFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(modifiedFilesLabel, gridBagConstraints);
        modifiedFilesLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(UpdateCenterRegistrationPanel.class, "ACD_UpdateCenterRegistrationPanel_modifiedFilesLabel", new Object[] {}));

        modifiedFiles.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        modifiedFiles.setColumns(20);
        modifiedFiles.setEditable(false);
        modifiedFiles.setRows(5);
        modifiedFiles.setToolTipText("modifiedFilesValue");
        modifiedFiles.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        add(modifiedFiles, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
        
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea createdFiles;
    private javax.swing.JLabel createdFilesLabel;
    private javax.swing.JTextField displayName;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JTextArea modifiedFiles;
    private javax.swing.JLabel modifiedFilesLabel;
    private javax.swing.JTextField project;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField ucUrl;
    private javax.swing.JLabel ucUrlLabel;
    // End of variables declaration//GEN-END:variables
    
}
