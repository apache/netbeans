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

package org.netbeans.modules.j2ee.jpa.verification.fixes;

import java.awt.Color;
import java.util.Set;
import javax.lang.model.element.TypeElement;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.TypeElementFinder;
import org.netbeans.modules.j2ee.persistence.dd.JavaPersistenceQLKeywords;
import org.openide.DialogDescriptor;
import org.openide.NotificationLineSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Tomasz Slota
 */
class PickOrCreateFieldPanel extends javax.swing.JPanel {
    public enum NameStatus {VALID, ILLEGAL_JAVA_ID, ILLEGAL_SQL_KEYWORD,  DUPLICATE};
    private Object availableFields[];
    private DefaultComboBoxModel mdlAvailableFields = new DefaultComboBoxModel();
    private FieldNameValidator nameValidator = null;
    private Border brdrBlack = BorderFactory.createLineBorder(Color.BLACK);
    private DialogDescriptor dlgDescriptor = null;
    private FileObject fileObject;
    private NotificationLineSupport notSupport;
    
    /** Creates new form ProvideIDAnnotationPanel */
    public PickOrCreateFieldPanel() {
        initComponents();
        
        radioPickUpExistingField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                boolean pickUpExisting = radioPickUpExistingField.isSelected();
                lstExistingFields.setEnabled(pickUpExisting);
                btnFindType.setEnabled(!radioPickUpExistingField.isSelected());
                txtNewFieldName.setEnabled(!radioPickUpExistingField.isSelected());
                txtType.setEnabled(!radioPickUpExistingField.isSelected());
            }
        });
        
        lstExistingFields.setModel(mdlAvailableFields);
        txtNewFieldName.setSelectionEnd(txtNewFieldName.getText().length() - 1);
        
        txtNewFieldName.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent documentEvent) {
                update();
            }
            public void insertUpdate(DocumentEvent documentEvent) {
                update();
            }
            public void removeUpdate(DocumentEvent documentEvent) {
                update();
            }
            
            private void update(){
                NameStatus nameStatus = NameStatus.VALID;
                if (nameValidator != null){
                    nameStatus = nameValidator.checkName(getNewIdName());
                }
                setFieldNameStatus(nameStatus);
            }
        });
        
        setNameValidator(new DefaultFieldNameValidator());
        setFieldNameStatus(NameStatus.VALID);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        radioPickUpExistingField = new javax.swing.JRadioButton();
        lstExistingFields = new javax.swing.JComboBox();
        radioCreateNewField = new javax.swing.JRadioButton();
        lblName = new javax.swing.JLabel();
        txtNewFieldName = new javax.swing.JTextField();
        lblType = new javax.swing.JLabel();
        txtType = new javax.swing.JTextField();
        btnFindType = new javax.swing.JButton();

        buttonGroup1.add(radioPickUpExistingField);
        radioPickUpExistingField.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/jpa/verification/fixes/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(radioPickUpExistingField, bundle.getString("LBL_PickExistingField")); // NOI18N
        radioPickUpExistingField.setMargin(new java.awt.Insets(0, 0, 0, 0));

        lstExistingFields.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        buttonGroup1.add(radioCreateNewField);
        org.openide.awt.Mnemonics.setLocalizedText(radioCreateNewField, bundle.getString("LBL_CreateNewField")); // NOI18N
        radioCreateNewField.setMargin(new java.awt.Insets(0, 0, 0, 0));

        lblName.setLabelFor(txtNewFieldName);
        org.openide.awt.Mnemonics.setLocalizedText(lblName, bundle.getString("LBL_FieldName")); // NOI18N

        txtNewFieldName.setText(org.openide.util.NbBundle.getMessage(PickOrCreateFieldPanel.class, "PickOrCreateFieldPanel.txtNewFieldName.text")); // NOI18N
        txtNewFieldName.setEnabled(false);

        lblType.setLabelFor(txtType);
        org.openide.awt.Mnemonics.setLocalizedText(lblType, bundle.getString("LBL_FieldType")); // NOI18N

        txtType.setText(org.openide.util.NbBundle.getMessage(PickOrCreateFieldPanel.class, "PickOrCreateFieldPanel.txtType.text")); // NOI18N
        txtType.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(btnFindType, bundle.getString("LBL_FindType")); // NOI18N
        btnFindType.setEnabled(false);
        btnFindType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFindTypeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(radioPickUpExistingField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lstExistingFields, 0, 388, Short.MAX_VALUE))
                    .addComponent(radioCreateNewField)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblName)
                            .addComponent(lblType))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtType, javax.swing.GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnFindType))
                            .addComponent(txtNewFieldName, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioPickUpExistingField)
                    .addComponent(lstExistingFields, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioCreateNewField)
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(txtNewFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblType)
                    .addComponent(btnFindType)
                    .addComponent(txtType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        lblType.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PickOrCreateFieldPanel.class, "PickOrCreateFieldPanel.lblType.AccessibleContext.accessibleName")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void btnFindTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFindTypeActionPerformed
        ElementHandle<TypeElement> type = TypeElementFinder.find(ClasspathInfo.create(fileObject), null);
        if (type != null) {
            String fqn = type.getQualifiedName().toString();
            txtType.setText(fqn);
        }
    }//GEN-LAST:event_btnFindTypeActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFindType;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblType;
    private javax.swing.JComboBox lstExistingFields;
    private javax.swing.JRadioButton radioCreateNewField;
    private javax.swing.JRadioButton radioPickUpExistingField;
    private javax.swing.JTextField txtNewFieldName;
    private javax.swing.JTextField txtType;
    // End of variables declaration//GEN-END:variables

    public void setFileObject(FileObject fo) {
        this.fileObject = fo;
    }

    public void setAvailableFields(Object availableFields[]){
        this.availableFields = availableFields;
        mdlAvailableFields.removeAllElements();
        
        if (availableFields == null || availableFields.length == 0){
            setChoosingExistingFieldEnabled(false);
        } else{
            setChoosingExistingFieldEnabled(true);
            
            for (int i = 0; i < availableFields.length; i++) {
                mdlAvailableFields.addElement(availableFields[i]);
            }
        }
    }
    
    public void setChoosingExistingFieldEnabled(boolean enabled){
        if (!enabled){
            radioCreateNewField.setSelected(true);
        }
        
        radioPickUpExistingField.setEnabled(enabled);
    }
    
    public boolean wasCreateNewFieldSelected(){
        return radioCreateNewField.isSelected();
    }
    
    public String getNewIdName(){
        return txtNewFieldName.getText();
    }
    
    public Object getSelectedField(){
        return lstExistingFields.getSelectedItem();
    }
    
    public String getSelectedIdType(){
        return txtType.getText();
    }
    
    public void setType(String typeName){
        txtType.setText(typeName);
    }
    
    public void setNameValidator(FieldNameValidator nameValidator){
        this.nameValidator = nameValidator;
    }
    
    public void setExistingFieldNames(Set<String> existingFieldNames){
        nameValidator.setExistingFieldNames(existingFieldNames);
    }
    
    public void setSelelectableType(boolean selectableType){
        txtType.setVisible(selectableType);
        btnFindType.setVisible(selectableType);
        lblType.setVisible(selectableType);
    }
    
    void setDefaultFieldName(String defaultFieldName) {
        txtNewFieldName.setText(defaultFieldName);
    }
    
    public void setFieldNameStatus(NameStatus nameStatus) {
        if (dlgDescriptor == null)
            return;

        dlgDescriptor.setValid(nameStatus == nameStatus.VALID);
       
        String errorMsgBundleId = null;
        String fieldName = txtNewFieldName.getText();
        
        switch (nameStatus){
            case ILLEGAL_JAVA_ID:
                errorMsgBundleId = NbBundle.getMessage(PickOrCreateFieldPanel.class,
                        "MSG_IllegalJavaID", fieldName);
                break;
            case ILLEGAL_SQL_KEYWORD:
                errorMsgBundleId = NbBundle.getMessage(PickOrCreateFieldPanel.class,
                        "MSG_IllegalSQLKeyWord", fieldName);
                break;
            case DUPLICATE:
                errorMsgBundleId = NbBundle.getMessage(PickOrCreateFieldPanel.class,
                        "MSG_DuplicateVariableName", fieldName);
                break;
        }
        notSupport.setErrorMessage(errorMsgBundleId);
    }
    
    public static interface FieldNameValidator{
        public NameStatus checkName(String name);
        public void setExistingFieldNames(Set<String> existingFieldNames);
    }
    
    public static class DefaultFieldNameValidator implements FieldNameValidator{
        private Set<String> existingFieldNames;
        
        public void setExistingFieldNames(Set<String> existingFieldNames){
            this.existingFieldNames = existingFieldNames;
        }
        
        public NameStatus checkName(String name){
            if (!Utilities.isJavaIdentifier(name)){
                return NameStatus.ILLEGAL_JAVA_ID;
            }
            
            if (JavaPersistenceQLKeywords.isKeyword(name)){
                return NameStatus.ILLEGAL_SQL_KEYWORD;
            }
            
            if (existingFieldNames != null && existingFieldNames.contains(name)){
                return NameStatus.DUPLICATE;
            }
            
            return NameStatus.VALID;
        }
    }

    public void setDlgDescriptor(DialogDescriptor dlgDescriptor) {
        this.dlgDescriptor = dlgDescriptor;
        this.notSupport = dlgDescriptor.getNotificationLineSupport();
    }
}
