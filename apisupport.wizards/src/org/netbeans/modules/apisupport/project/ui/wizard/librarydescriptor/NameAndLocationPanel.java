/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.librarydescriptor;

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
 * Represents <em>Name and Location</em> panel in J2SE Library Descriptor Wizard.
 *
 * @author Radek Matous
 */
final class NameAndLocationPanel extends BasicWizardIterator.Panel {
    
    private NewLibraryDescriptor.DataModel data;
    
    /** Creates new NameAndLocationPanel */
    public NameAndLocationPanel(final WizardDescriptor setting, final NewLibraryDescriptor.DataModel data) {
        super(setting);
        this.data = data;
        initComponents();
        initAccessibility();
        putClientProperty("NewFileWizard_Title",// NOI18N
                NbBundle.getMessage(NameAndLocationPanel.class,"LBL_LibraryWizardTitle")); // NOI18N
        
        DocumentListener dListener = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                NewLibraryDescriptor.DataModel _data = getTemporaryDataModel();                
                setEnabledForFilesInfo(checkValidity(_data));
                setFilesInfoIntoTextAreas(_data);
            }
        };
        libraryNameVale.getDocument().addDocumentListener(dListener);
        libraryDisplayNameValue.getDocument().addDocumentListener(dListener);        
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(NameAndLocationPanel.class, key);
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_NameIconLocationPanel"));
        createdFilesValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_CreatedFiles"));
        modifiedFilesValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_ModifiedFiles"));
        libraryDisplayNameValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_DisplayName"));
        libraryNameVale.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_Name"));
        projectNameValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_ProjectName"));
    }
    
    protected void storeToDataModel() {
        NewLibraryDescriptor.DataModel _temp = getTemporaryDataModel();        
        data.setLibraryName(_temp.getLibraryName());
        data.setLibraryDisplayName(_temp.getLibraryDisplayName());        
        data.setCreatedModifiedFiles(_temp.getCreatedModifiedFiles());        
    }
    
    private NewLibraryDescriptor.DataModel getTemporaryDataModel() {
        NewLibraryDescriptor.DataModel _temp = data.cloneMe(getSettings());        
        _temp.setLibraryName(libraryNameVale.getText());
        _temp.setLibraryDisplayName(libraryDisplayNameValue.getText());        
        if (_temp.isValidLibraryDisplayName() && _temp.isValidLibraryName()) {
            CreatedModifiedFiles files = CreatedModifiedFilesProvider.createInstance(_temp);
            _temp.setCreatedModifiedFiles(files);
        }                
        return _temp;
    }
    
    private void setEnabledForFilesInfo(boolean enabled) {        
        createdFilesValue.setEnabled(enabled);
        modifiedFilesValue.setEnabled(enabled);
    }

    private void setFilesInfoIntoTextAreas(final NewLibraryDescriptor.DataModel _temp) {
        if (_temp.getCreatedModifiedFiles() != null) {
            createdFilesValue.setText(WizardUtils.generateTextAreaContent(
                    _temp.getCreatedModifiedFiles().getCreatedPaths()));
            modifiedFilesValue.setText(WizardUtils.generateTextAreaContent(
                    _temp.getCreatedModifiedFiles().getModifiedPaths()));
        }
    }
    
    protected void readFromDataModel() {
        libraryNameVale.setText(this.data.getLibrary().getName());
        libraryDisplayNameValue.setText(this.data.getLibrary().getDisplayName());
        checkValidity(getTemporaryDataModel());
    }
    
    protected String getPanelName() {
        return NbBundle.getMessage(NameAndLocationPanel.class,"LBL_NameAndLocation_Title"); // NOI18N
    }

    
    private boolean checkValidity(final NewLibraryDescriptor.DataModel _data) {
        if (!_data.isValidLibraryName()) {
            setError(NbBundle.getMessage(NameAndLocationPanel.class,"ERR_InvalidName")); // NOI18N
            return false;
        } else if (!_data.isValidLibraryDisplayName()) {
            setError(NbBundle.getMessage(NameAndLocationPanel.class,"ERR_EmptyDescName")); // NOI18N
            return false;
        }else if (_data.libraryAlreadyExists()) {
            setError(NbBundle.getMessage(NameAndLocationPanel.class,
                    "ERR_LibraryExists", _data.getLibraryName()));
            return false;
        }
        markValid();
        return true;
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(NameAndLocationPanel.class);
    }
    
    public void addNotify() {
        super.addNotify();
        checkValidity(getTemporaryDataModel());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        libraryName = new javax.swing.JLabel();
        libraryNameVale = new javax.swing.JTextField();
        libraryDisplayName = new javax.swing.JLabel();
        libraryDisplayNameValue = new javax.swing.JTextField();
        projectName = new javax.swing.JLabel();
        projectNameValue = new JTextField(ProjectUtils.getInformation(this.data.getProject()).getDisplayName());
        createdFiles = new javax.swing.JLabel();
        modifiedFiles = new javax.swing.JLabel();
        createdFilesValueS = new javax.swing.JScrollPane();
        createdFilesValue = new javax.swing.JTextArea();
        modifiedFilesValueS = new javax.swing.JScrollPane();
        modifiedFilesValue = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        libraryName.setLabelFor(libraryNameVale);
        org.openide.awt.Mnemonics.setLocalizedText(libraryName, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/librarydescriptor/Bundle").getString("LBL_LibraryName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 6, 12);
        add(libraryName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 6, 0);
        add(libraryNameVale, gridBagConstraints);

        libraryDisplayName.setLabelFor(libraryDisplayNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(libraryDisplayName, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/librarydescriptor/Bundle").getString("LBL_LibraryDisplayName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(libraryDisplayName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(libraryDisplayNameValue, gridBagConstraints);

        projectName.setLabelFor(projectNameValue);
        org.openide.awt.Mnemonics.setLocalizedText(projectName, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/librarydescriptor/Bundle").getString("LBL_ProjectName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 12);
        add(projectName, gridBagConstraints);

        projectNameValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 0);
        add(projectNameValue, gridBagConstraints);

        createdFiles.setLabelFor(createdFilesValue);
        org.openide.awt.Mnemonics.setLocalizedText(createdFiles, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/librarydescriptor/Bundle").getString("LBL_CreatedFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 12);
        add(createdFiles, gridBagConstraints);

        modifiedFiles.setLabelFor(modifiedFilesValue);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedFiles, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/wizard/librarydescriptor/Bundle").getString("LBL_ModifiedFiles"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(modifiedFiles, gridBagConstraints);

        createdFilesValue.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        createdFilesValue.setColumns(20);
        createdFilesValue.setEditable(false);
        createdFilesValue.setRows(5);
        createdFilesValue.setBorder(null);
        createdFilesValueS.setViewportView(createdFilesValue);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 0);
        add(createdFilesValueS, gridBagConstraints);

        modifiedFilesValue.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        modifiedFilesValue.setColumns(20);
        modifiedFilesValue.setEditable(false);
        modifiedFilesValue.setRows(5);
        modifiedFilesValue.setToolTipText("modifiedFilesValue");
        modifiedFilesValue.setBorder(null);
        modifiedFilesValueS.setViewportView(modifiedFilesValue);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(modifiedFilesValueS, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel createdFiles;
    private javax.swing.JTextArea createdFilesValue;
    private javax.swing.JScrollPane createdFilesValueS;
    private javax.swing.JLabel libraryDisplayName;
    private javax.swing.JTextField libraryDisplayNameValue;
    private javax.swing.JLabel libraryName;
    private javax.swing.JTextField libraryNameVale;
    private javax.swing.JLabel modifiedFiles;
    private javax.swing.JTextArea modifiedFilesValue;
    private javax.swing.JScrollPane modifiedFilesValueS;
    private javax.swing.JLabel projectName;
    private javax.swing.JTextField projectNameValue;
    // End of variables declaration//GEN-END:variables
    
}
