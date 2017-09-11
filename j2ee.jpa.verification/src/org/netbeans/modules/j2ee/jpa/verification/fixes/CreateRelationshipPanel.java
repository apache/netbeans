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
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.j2ee.persistence.dd.JavaPersistenceQLKeywords;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Tomasz Slota
 */
class CreateRelationshipPanel extends javax.swing.JPanel {
    public enum NameStatus {VALID, ILLEGAL_JAVA_ID, ILLEGAL_SQL_KEYWORD,  DUPLICATE};
    public enum AvailableSelection {INVERSE_ONLY, OWNING_ONLY, BOTH};
    private Collection<String> availableFields;
    private DefaultComboBoxModel mdlAvailableFields = new DefaultComboBoxModel();
    private FieldNameValidator nameValidator = null;
    private Border brdrBlack = BorderFactory.createLineBorder(Color.BLACK);
    private DialogDescriptor dlgDescriptor = null;
    
    /** Creates new form ProvideIDAnnotationPanel */
    public CreateRelationshipPanel() {
        initComponents();
        
        radioPickUpExistingField.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                boolean pickUpExisting = radioPickUpExistingField.isSelected();
                lstExistingFields.setEnabled(pickUpExisting);
                txtNewFieldName.setEnabled(!radioPickUpExistingField.isSelected());
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
        buttonGroup2 = new javax.swing.ButtonGroup();
        radioPickUpExistingField = new javax.swing.JRadioButton();
        lstExistingFields = new javax.swing.JComboBox();
        radioCreateNewField = new javax.swing.JRadioButton();
        lblName = new javax.swing.JLabel();
        txtNewFieldName = new javax.swing.JTextField();
        pnlErrorMsg = new javax.swing.JPanel();
        lblErrorMsg = new javax.swing.JLabel();
        lblError = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        radioOwningSide = new javax.swing.JRadioButton();
        radioInversedSide = new javax.swing.JRadioButton();

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

        txtNewFieldName.setText(org.openide.util.NbBundle.getMessage(CreateRelationshipPanel.class, "PickOrCreateFieldPanel.txtNewFieldName.text")); // NOI18N
        txtNewFieldName.setEnabled(false);

        pnlErrorMsg.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        pnlErrorMsg.setFocusable(false);

        org.openide.awt.Mnemonics.setLocalizedText(lblErrorMsg, bundle.getString("MSG_IllegalJavaID")); // NOI18N

        lblError.setForeground(new java.awt.Color(255, 0, 51));
        org.openide.awt.Mnemonics.setLocalizedText(lblError, bundle.getString("LBL_Error")); // NOI18N

        javax.swing.GroupLayout pnlErrorMsgLayout = new javax.swing.GroupLayout(pnlErrorMsg);
        pnlErrorMsg.setLayout(pnlErrorMsgLayout);
        pnlErrorMsgLayout.setHorizontalGroup(
            pnlErrorMsgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlErrorMsgLayout.createSequentialGroup()
                .addComponent(lblError)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblErrorMsg, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlErrorMsgLayout.setVerticalGroup(
            pnlErrorMsgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlErrorMsgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(lblError)
                .addComponent(lblErrorMsg))
        );

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CreateRelationshipPanel.class, "CreateRelationshipPanel.jLabel1.text")); // NOI18N

        buttonGroup2.add(radioOwningSide);
        radioOwningSide.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(radioOwningSide, org.openide.util.NbBundle.getMessage(CreateRelationshipPanel.class, "CreateRelationshipPanel.radioOwningSide.text")); // NOI18N
        radioOwningSide.setMargin(new java.awt.Insets(0, 0, 0, 0));

        buttonGroup2.add(radioInversedSide);
        org.openide.awt.Mnemonics.setLocalizedText(radioInversedSide, org.openide.util.NbBundle.getMessage(CreateRelationshipPanel.class, "CreateRelationshipPanel.radioInversedSide.text")); // NOI18N
        radioInversedSide.setMargin(new java.awt.Insets(0, 0, 0, 0));

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
                        .addComponent(lstExistingFields, 0, 398, Short.MAX_VALUE))
                    .addComponent(radioCreateNewField)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addComponent(lblName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlErrorMsg, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtNewFieldName, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE)))
                    .addComponent(radioOwningSide)
                    .addComponent(radioInversedSide)
                    .addComponent(jLabel1))
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
                .addComponent(pnlErrorMsg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioOwningSide)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addComponent(radioInversedSide)
                .addGap(8, 8, 8))
        );
    }// </editor-fold>//GEN-END:initComponents
        
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblErrorMsg;
    private javax.swing.JLabel lblName;
    private javax.swing.JComboBox lstExistingFields;
    private javax.swing.JPanel pnlErrorMsg;
    private javax.swing.JRadioButton radioCreateNewField;
    private javax.swing.JRadioButton radioInversedSide;
    private javax.swing.JRadioButton radioOwningSide;
    private javax.swing.JRadioButton radioPickUpExistingField;
    private javax.swing.JTextField txtNewFieldName;
    // End of variables declaration//GEN-END:variables
    
    public void setAvailableFields(Collection<String> availableFields){
        this.availableFields = availableFields;
        mdlAvailableFields.removeAllElements();
        
        if (availableFields.size() == 0){
            setChoosingExistingFieldEnabled(false);
        } else{
            setChoosingExistingFieldEnabled(true);
            
            for (String fieldName : availableFields) {
                mdlAvailableFields.addElement(fieldName);
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
    
    public void setNameValidator(FieldNameValidator nameValidator){
        this.nameValidator = nameValidator;
    }
    
    public void setExistingFieldNames(Collection<String> existingFieldNames){
        nameValidator.setExistingFieldNames(existingFieldNames);
    }
    
    void setDefaultFieldName(String defaultFieldName) {
        txtNewFieldName.setText(defaultFieldName);
    }
    
    private void setErrorPanelVisible(boolean visible){
        lblErrorMsg.setVisible(visible);
        lblError.setVisible(visible);
        pnlErrorMsg.setBorder(visible ? brdrBlack : null);
    }
    
    public void setFieldNameStatus(NameStatus nameStatus){
        boolean validName = nameStatus == nameStatus.VALID;
        setErrorPanelVisible(!validName);
        
        if (dlgDescriptor != null){
            dlgDescriptor.setValid(validName);
        }
        
        String errorMsgBundleId;
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
            default:
                errorMsgBundleId = null;
        }
        
        lblErrorMsg.setText(errorMsgBundleId);
    }
    
    public void setEntityClassNames(String entity1Name, String entity2Name){
        org.openide.awt.Mnemonics.setLocalizedText(radioOwningSide,
                NbBundle.getMessage(CreateRelationshipPanel.class,
                "CreateRelationshipPanel.radioOwningSide.text", entity1Name)); // NOI18N
        
        org.openide.awt.Mnemonics.setLocalizedText(radioInversedSide,
                NbBundle.getMessage(CreateRelationshipPanel.class,
                "CreateRelationshipPanel.radioInversedSide.text", entity2Name)); // NOI18N
    }
    
    public void setAvailableSelection(AvailableSelection sel){
        switch (sel){
            case INVERSE_ONLY:
                radioInversedSide.setSelected(true);
                radioOwningSide.setEnabled(false);
                break;
            case OWNING_ONLY:
                radioInversedSide.setEnabled(false);
                break;
        }
    }
    
    public boolean owningSide(){
        return radioOwningSide.isSelected();
    }
    
    public static interface FieldNameValidator{
        public NameStatus checkName(String name);
        public void setExistingFieldNames(Collection<String> existingFieldNames);
    }
    
    public static class DefaultFieldNameValidator implements FieldNameValidator{
        private Collection<String> existingFieldNames;
        
        public void setExistingFieldNames(Collection<String> existingFieldNames){
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
    }
}
