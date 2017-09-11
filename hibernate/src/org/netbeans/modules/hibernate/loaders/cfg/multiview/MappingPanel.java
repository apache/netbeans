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

package org.netbeans.modules.hibernate.loaders.cfg.multiview;

import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;

/**
 *
 * @author  Dongmei Cao
 */
public class MappingPanel extends javax.swing.JPanel {
    
    /** Creates new form MappingPanel */
    public MappingPanel(HibernateCfgDataObject dObj) {
        initComponents();
        
        String[] mappingFiles = Util.getMappingFilesFromProject(dObj.getPrimaryFile());
        this.resourceComboBox.setModel( new DefaultComboBoxModel(mappingFiles) );
        
        // TODO: enable them later
        this.jarButton.setEnabled(false);
        this.fileButton.setEnabled(false);
    }
    
    public void initValues( String resourceName, String fileName, String jarName, String packageName, String className ) {
        this.resourceComboBox.setSelectedItem(resourceName);
        this.fileTextField.setText( fileName );
        this.jarTextField.setText( jarName );
        this.pacakgeTextField.setText( packageName );
        this.classTextField.setText( className );
    }
    
    public boolean isDataValid() {
        // At least one field should be filled to make it valid
        if( getResourceName().length() != 0 
                || getJarName().length() != 0 
                || getFileName().length() != 0 
                || getPackageName().length() != 0 
                || getClassName().length() != 0 ) {
            return true;
        } else
            return false;
            
    }
    
    public void addClassButtonActionListener( ActionListener listener ) {
        this.classButton.addActionListener(listener);
    }
    
    public void addFileButtonActionListener( ActionListener listener ) {
        this.fileButton.addActionListener(listener);
    }
    
    public void addJarButtonActionListener( ActionListener listener ) {
        this.jarButton.addActionListener(listener);
    }
    
    public void addPackageButtonListener( ActionListener listener ) {
        this.packageButton.addActionListener(listener);
    }
    
    public JTextField getResourceTextField() {
        return (JTextField)resourceComboBox.getEditor().getEditorComponent();
    }
    
    public JTextField getFileTextField() {
        return this.fileTextField;
    }
    
    public JTextField getClassTextField() {
        return classTextField;
    }

    public JTextField getJarTextField() {
        return jarTextField;
    }

    public JTextField getPacakgeTextField() {
        return pacakgeTextField;
    }
    
    public String getResourceName() {
        return getResourceTextField().getText().trim();
    }
    
    public String getFileName() {
        return this.fileTextField.getText().trim();
    }
    
    public String getJarName() {
        return this.jarTextField.getText().trim();
    }
    
    public String getPackageName() {
        return this.pacakgeTextField.getText().trim();
    }
    
    public String getClassName() {
        return this.classTextField.getText().trim();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        resourceLabel = new javax.swing.JLabel();
        fileLabel = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        jarLabel = new javax.swing.JLabel();
        jarTextField = new javax.swing.JTextField();
        packageLabel = new javax.swing.JLabel();
        pacakgeTextField = new javax.swing.JTextField();
        classLabel = new javax.swing.JLabel();
        classTextField = new javax.swing.JTextField();
        jarButton = new javax.swing.JButton();
        fileButton = new javax.swing.JButton();
        packageButton = new javax.swing.JButton();
        classButton = new javax.swing.JButton();
        resourceComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        resourceLabel.setLabelFor(resourceComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(resourceLabel, org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.resourceLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(resourceLabel, gridBagConstraints);
        resourceLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.resourceLabel.text")); // NOI18N
        resourceLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.resourceLabel.text")); // NOI18N

        fileLabel.setLabelFor(fileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fileLabel, org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.fileLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(fileLabel, gridBagConstraints);
        fileLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.fileLabel.text")); // NOI18N
        fileLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.fileLabel.text")); // NOI18N

        fileTextField.setText(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.fileTextField.text")); // NOI18N
        fileTextField.setPreferredSize(new java.awt.Dimension(200, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(fileTextField, gridBagConstraints);

        jarLabel.setLabelFor(jarTextField);
        org.openide.awt.Mnemonics.setLocalizedText(jarLabel, org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.jarLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(jarLabel, gridBagConstraints);
        jarLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.jarLabel.text")); // NOI18N
        jarLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.jarLabel.text")); // NOI18N

        jarTextField.setText(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.jarTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(jarTextField, gridBagConstraints);

        packageLabel.setLabelFor(pacakgeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(packageLabel, org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.packageLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(packageLabel, gridBagConstraints);
        packageLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.packageLabel.text")); // NOI18N
        packageLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.packageLabel.text")); // NOI18N

        pacakgeTextField.setText(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.pacakgeTextField.text")); // NOI18N
        pacakgeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pacakgeTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(pacakgeTextField, gridBagConstraints);

        classLabel.setLabelFor(classTextField);
        org.openide.awt.Mnemonics.setLocalizedText(classLabel, org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.classLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(classLabel, gridBagConstraints);
        classLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.classLabel.text")); // NOI18N
        classLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.classLabel.text")); // NOI18N

        classTextField.setText(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.classTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(classTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jarButton, org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.jarButton.text")); // NOI18N
        jarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jarButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(jarButton, gridBagConstraints);
        jarButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.jarButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(fileButton, org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.fileButton.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(fileButton, gridBagConstraints);
        fileButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.fileButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(packageButton, org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.packageButton.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(packageButton, gridBagConstraints);
        packageButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.packageButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(classButton, org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.classButton.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 12);
        add(classButton, gridBagConstraints);
        classButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MappingPanel.class, "MappingPanel.classButton.text")); // NOI18N

        resourceComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(resourceComboBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void pacakgeTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pacakgeTextFieldActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_pacakgeTextFieldActionPerformed

    private void jarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jarButtonActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_jarButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton classButton;
    private javax.swing.JLabel classLabel;
    private javax.swing.JTextField classTextField;
    private javax.swing.JButton fileButton;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JButton jarButton;
    private javax.swing.JLabel jarLabel;
    private javax.swing.JTextField jarTextField;
    private javax.swing.JTextField pacakgeTextField;
    private javax.swing.JButton packageButton;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JComboBox resourceComboBox;
    private javax.swing.JLabel resourceLabel;
    // End of variables declaration//GEN-END:variables

    
}
