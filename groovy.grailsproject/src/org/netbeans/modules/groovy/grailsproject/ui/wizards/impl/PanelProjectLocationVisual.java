/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.groovy.grailsproject.ui.wizards.impl;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import javax.swing.JFileChooser;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import java.io.File;
import java.text.MessageFormat;
import javax.swing.SwingUtilities;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;


/**
 *
 * @author  schmidtm
 */
public class PanelProjectLocationVisual extends SettingsPanel implements DocumentListener {

    private PanelConfigureProject panel;

    boolean valid(WizardDescriptor settings) {
        if (projectNameTextField.getText().trim().length() == 0) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                NbBundle.getMessage(PanelProjectLocationVisual.class,
                "GetProjectLocationPanel.EmptyProjectName"));
            return false;
        }

        if(!new File(projectLocationTextField.getText().trim()).isDirectory()) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                NbBundle.getMessage(PanelProjectLocationVisual.class,
                "GetProjectLocationPanel.LocationNotDirectory"));
            return false;
        }

        if (new File(projectFolderTextField.getText().trim()).exists()) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                NbBundle.getMessage(PanelProjectLocationVisual.class,
                "GetProjectLocationPanel.FileAlreadyExists"));
            return false;
        }

        return true;
    }
    
    void read (WizardDescriptor d) {
        File projectLocation = (File) d.getProperty ("projectFolder");  //NOI18N
        if (projectLocation == null || projectLocation.getParentFile() == null || !projectLocation.getParentFile().isDirectory ()) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        
        Integer count = (Integer) d.getProperty("WizardPanel_GrailsProjectCounter");
        String formater = NbBundle.getMessage(PanelProjectLocationVisual.class, "TXT_GrailsApplication");
        
        int baseCount = count.intValue();
        
        String newPrjName = (String) d.getProperty ("name"); //NOI18N
        if (newPrjName == null) {        
            while ((newPrjName = validFreeProjectName(projectLocation, formater, baseCount)) == null) {
                baseCount++;
            }
        }
        
        projectLocationTextField.setText(projectLocation.getAbsolutePath());
        projectFolderTextField.setText( projectLocation.getAbsolutePath() + File.separatorChar + projectNameTextField.getText().trim() );
        projectNameTextField.setText(newPrjName);
    }
    
    void validate (WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }

    void store( WizardDescriptor d ) {
        d.putProperty( "projectFolder", new File(projectFolderTextField.getText().trim()) ); // NOI18N
        d.putProperty( "projectName", projectNameTextField.getText().trim() ); // NOI18N
    }
    
    
    
    /** Creates new form NewGrailsProjectPanel */
    public PanelProjectLocationVisual(PanelConfigureProject parentStep) {
        this.panel = parentStep;
        initComponents();
        
        // register event listeners to auto-update some fields.
        projectLocationTextField.getDocument().addDocumentListener( this );
        projectNameTextField.getDocument().addDocumentListener( this );        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        projectNameLabel = new javax.swing.JLabel();
        projectLocationLabel = new javax.swing.JLabel();
        projectFolderLabel = new javax.swing.JLabel();
        projectNameTextField = new javax.swing.JTextField();
        projectLocationTextField = new javax.swing.JTextField();
        projectFolderTextField = new javax.swing.JTextField();
        browseLocationJButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        projectNameLabel.setDisplayedMnemonic('N');
        projectNameLabel.setLabelFor(projectNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectNameLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "PanelProjectLocationVisual.projectNameLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        add(projectNameLabel, gridBagConstraints);
        projectNameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "GetProjectLocationPanel.projectNameLabel.AccessibleContext.accessibleDescription")); // NOI18N

        projectLocationLabel.setDisplayedMnemonic('L');
        projectLocationLabel.setLabelFor(projectLocationTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectLocationLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "PanelProjectLocationVisual.projectLocationLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(projectLocationLabel, gridBagConstraints);
        projectLocationLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "GetProjectLocationPanel.projectLocationLabel.AccessibleContext.accessibleDescription")); // NOI18N

        projectFolderLabel.setDisplayedMnemonic('F');
        projectFolderLabel.setLabelFor(projectFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(projectFolderLabel, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "PanelProjectLocationVisual.projectFolderLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(projectFolderLabel, gridBagConstraints);
        projectFolderLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "GetProjectLocationPanel.projectFolderLabel.AccessibleContext.accessibleDescription")); // NOI18N

        projectNameTextField.setText(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "PanelProjectLocationVisual.projectNameTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 12, 0);
        add(projectNameTextField, gridBagConstraints);
        projectNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "GetProjectLocationPanel.projectNameTextField.AccessibleContext.accessibleName")); // NOI18N
        projectNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "GetProjectLocationPanel.projectNameTextField.AccessibleContext.accessibleDescription")); // NOI18N

        projectLocationTextField.setText(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "PanelProjectLocationVisual.projectLocationTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 5, 0);
        add(projectLocationTextField, gridBagConstraints);
        projectLocationTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "GetProjectLocationPanel.projectLocationTextField.AccessibleContext.accessibleName")); // NOI18N
        projectLocationTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "GetProjectLocationPanel.projectLocationTextField.AccessibleContext.accessibleDescription")); // NOI18N

        projectFolderTextField.setEditable(false);
        projectFolderTextField.setText(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "PanelProjectLocationVisual.projectFolderTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(projectFolderTextField, gridBagConstraints);
        projectFolderTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "GetProjectLocationPanel.projectFolderTextField.AccessibleContext.accessibleName")); // NOI18N
        projectFolderTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "GetProjectLocationPanel.projectFolderTextField.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseLocationJButton, org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "PanelProjectLocationVisual.browseLocationJButton.text")); // NOI18N
        browseLocationJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseLocationJButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 5, 0);
        add(browseLocationJButton, gridBagConstraints);
        browseLocationJButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "GetProjectLocationPanel.browseLocationJButton.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "GetProjectLocationPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelProjectLocationVisual.class, "GetProjectLocationPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseLocationJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseLocationJButtonActionPerformed
            JFileChooser chooser = new JFileChooser ();
            FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
            chooser.setDialogTitle(NbBundle.getMessage(PanelProjectLocationVisual.class,"GetProjectLocationPanel.FileChooserTitle"));
            chooser.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY);
            String path = projectLocationTextField.getText().trim();
            if (path.length() > 0) {
                File f = new File (path);
                if (f.exists ()) {
                    chooser.setSelectedFile(f);
                }
            }
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this))) { //NOI18N
                File projectDir = chooser.getSelectedFile();
                projectLocationTextField.setText( projectDir.getAbsolutePath() );
            }   
}//GEN-LAST:event_browseLocationJButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseLocationJButton;
    private javax.swing.JLabel projectFolderLabel;
    private javax.swing.JTextField projectFolderTextField;
    private javax.swing.JLabel projectLocationLabel;
    private javax.swing.JTextField projectLocationTextField;
    private javax.swing.JLabel projectNameLabel;
    private javax.swing.JTextField projectNameTextField;
    // End of variables declaration//GEN-END:variables

    
    public void insertUpdate(DocumentEvent e) {
        updateTexts( e ) ;
    }

    public void removeUpdate(DocumentEvent e) {
        updateTexts( e ) ;
    }

    public void changedUpdate(DocumentEvent e) {
        updateTexts( e ) ;
    }
    
    /** Handles changes in the Project name and project directory
     */
    private void updateTexts( DocumentEvent e ) {
        
        Document doc = e.getDocument();
                
        if ( doc == projectNameTextField.getDocument() || doc == projectLocationTextField.getDocument() ) {
            // Change in the project name
        
            String projectName = projectNameTextField.getText().trim();
            String projectFolder = projectLocationTextField.getText().trim();
             
            getProjectFolderTextField().setText( new File(projectFolder, projectName).getAbsolutePath() );
            
            panel.fireChangeEvent();
            
        }                
  
    }

    public javax.swing.JTextField getProjectFolderTextField() {
        return projectFolderTextField;
    }
    
    private String validFreeProjectName (final File parentFolder, final String formater, final int index) {
        String name = MessageFormat.format(formater, index);
        File file = new File (parentFolder, name);
        return file.exists() ? null : name;
    }
    
}
