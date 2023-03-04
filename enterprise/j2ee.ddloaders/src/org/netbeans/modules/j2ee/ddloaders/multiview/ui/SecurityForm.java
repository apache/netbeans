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

package org.netbeans.modules.j2ee.ddloaders.multiview.ui;


import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.netbeans.modules.xml.multiview.Refreshable;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

/**
 * SecurityForm.java
 *
 * Form for adding and editing the run-as and method-permission elemens of
 * the ejb deployment descriptor.
 *
 * @author  ptliu
 */
public class SecurityForm extends SectionNodeInnerPanel {
    public static final String USE_CALLER_ID = "useCallerID";  //NOI18N
    public static final String RUN_AS = "runAs";              //NOI18N
    public static final String NO_SECURITY_ID = "noSecurityID";   //NOI18N
    public static final String ALL_METHOD_PERMISSION = "allMethodPermission"; //NOI18N
    public static final String SET_ROLE_METHOD_PERMISSION = "setRoleMethodPermission";    //NOI18N
    public static final String NO_METHOD_PERMISSION = "noMethodPermission";   //NOI18N
    /**
     * Creates new form SecurityForm
     */
    public SecurityForm(SectionNodeView sectionNodeView) {
        super(sectionNodeView);
        initComponents();
        
        noSecurityIDRB.putClientProperty(Refreshable.PROPERTY_FIXED_VALUE, NO_SECURITY_ID);
        useCallerIDRB.putClientProperty(Refreshable.PROPERTY_FIXED_VALUE, USE_CALLER_ID);
        runAsRB.putClientProperty(Refreshable.PROPERTY_FIXED_VALUE, RUN_AS);
        
        allMethodPermissionRB.putClientProperty(Refreshable.PROPERTY_FIXED_VALUE, ALL_METHOD_PERMISSION);
        setRoleMethodPermissionRB.putClientProperty(Refreshable.PROPERTY_FIXED_VALUE, SET_ROLE_METHOD_PERMISSION);
        noPermissionsRB.putClientProperty(Refreshable.PROPERTY_FIXED_VALUE, NO_METHOD_PERMISSION);
    }
    
    public JComponent getErrorComponent(String errorId) {
        return null;
    }
    
    public void setValue(JComponent source, Object value) {
        
    }
    
    public void linkButtonPressed(Object ddBean, String ddProperty) {
        
    }
    
    public JRadioButton getNoSecurityIDRB() {
        return noSecurityIDRB;
    }
    
    public ButtonGroup getSecurityIDButtonGroup() {
        return buttonGroup1;
    }
    
    public JRadioButton getUseCallerIDRB() {
        return useCallerIDRB;
    }
    
    public JRadioButton getRunAsRB() {
        return runAsRB;
    }
    
    public JTextField getRunAsRoleNameTF() {
        return runAsRoleNameTF;
    }
    
    public JTextField getRunAsDescriptionTF() {
        return runAsDescriptionTF;
    }
    
    public JRadioButton getNoPermissionRB() {
        return noPermissionsRB;
    }
    
    public ButtonGroup getGlobalMethodPermissionButtonGroup() {
        return buttonGroup2;
    }
    
    public JTextField getSetRoleRoleNamesTF() {
        return setRoleRoleNamesTF;
    }
    
    public JRadioButton getAllMethodPermissionRB() {
        return allMethodPermissionRB;
    }
    
    public JRadioButton getSetRolePermissionRB() {
        return setRoleMethodPermissionRB;
    }
    
    protected void updateVisualState() {
        
        if (runAsRB.isSelected()) {
            runAsRoleNameLabel.setEnabled(true);
            runAsRoleNameTF.setEnabled(true);
            runAsDescriptionLabel.setEnabled(true);
            runAsDescriptionTF.setEnabled(true);
        } else {
            runAsRoleNameLabel.setEnabled(false);
            runAsRoleNameTF.setEnabled(false);
            runAsDescriptionLabel.setEnabled(false);
            runAsDescriptionTF.setEnabled(false);
        }
               
        if (setRoleMethodPermissionRB.isSelected()) {
            setRoleRoleNamesLabel.setEnabled(true);
            setRoleRoleNamesTF.setEnabled(true);
            setRoleRoleNamesHintLabel.setEnabled(true);
        } else {
            setRoleRoleNamesLabel.setEnabled(false);
            setRoleRoleNamesTF.setEnabled(false);
            setRoleRoleNamesHintLabel.setEnabled(false);
        }
        
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
        buttonGroup3 = new javax.swing.ButtonGroup();
        runAsRoleNameLabel = new javax.swing.JLabel();
        runAsRoleNameTF = new javax.swing.JTextField();
        runAsDescriptionLabel = new javax.swing.JLabel();
        runAsDescriptionTF = new javax.swing.JTextField();
        setRoleRoleNamesLabel = new javax.swing.JLabel();
        setRoleRoleNamesTF = new javax.swing.JTextField();
        setRoleRoleNamesHintLabel = new javax.swing.JLabel();
        allMethodPermissionRB = new javax.swing.JRadioButton();
        setRoleMethodPermissionRB = new javax.swing.JRadioButton();
        useCallerIDRB = new javax.swing.JRadioButton();
        runAsRB = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        noSecurityIDRB = new javax.swing.JRadioButton();
        noPermissionsRB = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();

        runAsRoleNameLabel.setLabelFor(runAsRoleNameTF);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/multiview/ui/Bundle"); // NOI18N
        runAsRoleNameLabel.setText(bundle.getString("LBL_SecurityRoleName")); // NOI18N
        runAsRoleNameLabel.setEnabled(false);

        runAsRoleNameTF.setEnabled(false);

        runAsDescriptionLabel.setLabelFor(runAsDescriptionTF);
        runAsDescriptionLabel.setText(bundle.getString("LBL_Description")); // NOI18N
        runAsDescriptionLabel.setEnabled(false);

        runAsDescriptionTF.setEnabled(false);

        setRoleRoleNamesLabel.setLabelFor(setRoleRoleNamesTF);
        setRoleRoleNamesLabel.setText(bundle.getString("LBL_RoleNames")); // NOI18N
        setRoleRoleNamesLabel.setEnabled(false);

        setRoleRoleNamesTF.setEnabled(false);

        setRoleRoleNamesHintLabel.setText(bundle.getString("LBL_RoleNamesHint")); // NOI18N
        setRoleRoleNamesHintLabel.setEnabled(false);

        buttonGroup2.add(allMethodPermissionRB);
        allMethodPermissionRB.setText(bundle.getString("LBL_AllUsesAllMethodsPermission")); // NOI18N
        allMethodPermissionRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        allMethodPermissionRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        allMethodPermissionRB.setOpaque(false);

        buttonGroup2.add(setRoleMethodPermissionRB);
        setRoleMethodPermissionRB.setText(bundle.getString("LBL_SetRolesAllMethodsPermission")); // NOI18N
        setRoleMethodPermissionRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setRoleMethodPermissionRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        setRoleMethodPermissionRB.setOpaque(false);

        buttonGroup1.add(useCallerIDRB);
        useCallerIDRB.setText(bundle.getString("LBL_CallerIdentity")); // NOI18N
        useCallerIDRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useCallerIDRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        useCallerIDRB.setOpaque(false);

        buttonGroup1.add(runAsRB);
        runAsRB.setText(bundle.getString("LBL_RunAs")); // NOI18N
        runAsRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        runAsRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        runAsRB.setOpaque(false);

        jLabel1.setText(bundle.getString("LBL_SecurityIdentity")); // NOI18N

        buttonGroup1.add(noSecurityIDRB);
        noSecurityIDRB.setSelected(true);
        noSecurityIDRB.setText(bundle.getString("LBL_NoSecurityIdentity")); // NOI18N
        noSecurityIDRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noSecurityIDRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        noSecurityIDRB.setOpaque(false);

        buttonGroup2.add(noPermissionsRB);
        noPermissionsRB.setSelected(true);
        noPermissionsRB.setText(bundle.getString("LBL_NoGlobalMethodPermissions")); // NOI18N
        noPermissionsRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noPermissionsRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        noPermissionsRB.setOpaque(false);

        jLabel2.setText(bundle.getString("LBL_GlobalMethodPermission")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(noSecurityIDRB)
                            .addComponent(runAsRB)
                            .addComponent(useCallerIDRB)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(runAsDescriptionLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(runAsDescriptionTF, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(runAsRoleNameLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(runAsRoleNameTF, javax.swing.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE))))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(allMethodPermissionRB)
                            .addComponent(setRoleMethodPermissionRB)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(17, 17, 17)
                                .addComponent(setRoleRoleNamesLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(setRoleRoleNamesTF, javax.swing.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                                    .addComponent(setRoleRoleNamesHintLabel)))
                            .addComponent(noPermissionsRB)))
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noSecurityIDRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useCallerIDRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(runAsRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runAsRoleNameLabel)
                    .addComponent(runAsRoleNameTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(runAsDescriptionLabel)
                    .addComponent(runAsDescriptionTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noPermissionsRB)
                .addGap(7, 7, 7)
                .addComponent(allMethodPermissionRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(setRoleMethodPermissionRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(setRoleRoleNamesLabel)
                    .addComponent(setRoleRoleNamesTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(setRoleRoleNamesHintLabel)
                .addGap(31, 31, 31))
        );

        runAsRoleNameTF.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SecurityForm.class, "ACSD_Role_Name")); // NOI18N
        runAsDescriptionTF.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SecurityForm.class, "ACSD_Role_Desc")); // NOI18N
        setRoleRoleNamesTF.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SecurityForm.class, "ACSD_Role_Names")); // NOI18N
        allMethodPermissionRB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SecurityForm.class, "ACSD_NA")); // NOI18N
        setRoleMethodPermissionRB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SecurityForm.class, "ACSD_NA")); // NOI18N
        useCallerIDRB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SecurityForm.class, "ACSD_NA")); // NOI18N
        runAsRB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SecurityForm.class, "ACSD_NA")); // NOI18N
        noSecurityIDRB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SecurityForm.class, "ACSD_NA")); // NOI18N
        noPermissionsRB.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SecurityForm.class, "ACSD_NA")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton allMethodPermissionRB;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JRadioButton noPermissionsRB;
    private javax.swing.JRadioButton noSecurityIDRB;
    private javax.swing.JLabel runAsDescriptionLabel;
    private javax.swing.JTextField runAsDescriptionTF;
    private javax.swing.JRadioButton runAsRB;
    private javax.swing.JLabel runAsRoleNameLabel;
    private javax.swing.JTextField runAsRoleNameTF;
    private javax.swing.JRadioButton setRoleMethodPermissionRB;
    private javax.swing.JLabel setRoleRoleNamesHintLabel;
    private javax.swing.JLabel setRoleRoleNamesLabel;
    private javax.swing.JTextField setRoleRoleNamesTF;
    private javax.swing.JRadioButton useCallerIDRB;
    // End of variables declaration//GEN-END:variables
    
}
