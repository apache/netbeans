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


package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import java.awt.GridBagLayout;
import java.util.StringTokenizer;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.web.AuthConstraint;
import org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint;
import org.netbeans.modules.j2ee.dd.api.web.UserDataConstraint;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.openide.util.NbBundle;

/**
 * SecurityConstraintPanel.java
 *
 * Panel for adding and editing the security-constraint element of the web
 * deployment descriptor.
 *
 * @author  ptliu
 */
public class SecurityConstraintPanel extends SectionInnerPanel {
    
    private SectionView view;
    private DDDataObject dObj;
    private WebApp webApp;
    private SecurityConstraint constraint;
    
    /** Creates new form SecurityConstraintPanel */
    public SecurityConstraintPanel(SectionView view, DDDataObject dObj,
            SecurityConstraint constraint) {
        super(view);
        initComponents();
        
        this.view = view;
        this.dObj = dObj;
        this.webApp = dObj.getWebApp();
        this.constraint = constraint;
        
        initPanel();
    }
    
    private void initPanel() {
        displayNameTF.setText(constraint.getDefaultDisplayName());
        addValidatee(displayNameTF);
        
        AuthConstraint authConstraint = constraint.getAuthConstraint();
        if (authConstraint != null) {
            authConstraintCB.setSelected(true);
            updateVisualState();
            String nameString = getRoleNamesString(authConstraint);
            roleNamesTF.setText(nameString);
            authConstraintDescTF.setText(authConstraint.getDefaultDescription());
        }
        
        addModifier(authConstraintCB);
        //addValidatee(roleNamesTF);
        addModifier(authConstraintDescTF);
        
        UserDataConstraint userDataConstraint = constraint.getUserDataConstraint();
        if (userDataConstraint != null) {
            userDataConstraintCB.setSelected(true);
            updateVisualState();
            transportGuaranteeCB.setSelectedItem((String) userDataConstraint.getTransportGuarantee());
            userDataConstraintDescTF.setText(userDataConstraint.getDefaultDescription());
        }
        
        addModifier(userDataConstraintCB);
        addModifier(userDataConstraintDescTF);
        addModifier(transportGuaranteeCB);
        
        WebResourceCollectionTableModel model = new WebResourceCollectionTableModel();
        WebResourceCollectionTablePanel panel = new WebResourceCollectionTablePanel(dObj, model);
        panel.setModel(dObj.getWebApp(), constraint, constraint.getWebResourceCollection());
        
        webResourceCollectionPanel2.setLayout(new GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        //gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        gridBagConstraints.weightx = 1.0;
        //gridBagConstraints.weighty = 5.0;
        webResourceCollectionPanel2.add(panel, gridBagConstraints);
        
    }
    
    private void updateVisualState() {
        if (authConstraintCB.isSelected()) {
            authConstraintDescLabel.setEnabled(true);
            authConstraintDescTF.setEnabled(true);
            roleNamesLabel.setEnabled(true);
            roleNamesTF.setEnabled(true);
            editButton.setEnabled(true);
        } else {
            authConstraintDescLabel.setEnabled(false);
            authConstraintDescTF.setEnabled(false);
            roleNamesLabel.setEnabled(false);
            roleNamesTF.setEnabled(false);
            editButton.setEnabled(false);
        }
        
        if (userDataConstraintCB.isSelected()) {
            userDataConstraintDescLabel.setEnabled(true);
            userDataConstraintDescTF.setEnabled(true);
            transportGuaranteeLabel.setEnabled(true);
            transportGuaranteeCB.setEnabled(true);
        } else {
            userDataConstraintDescLabel.setEnabled(false);
            userDataConstraintDescTF.setEnabled(false);
            transportGuaranteeLabel.setEnabled(false);
            transportGuaranteeCB.setEnabled(false);
        }
    }
    
    public void linkButtonPressed(Object obj, String id) {
    }
    
    
    public javax.swing.JComponent getErrorComponent(String name) {
        return null;
    }
    
    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==displayNameTF) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Display Name", displayNameTF));
                
                return;
            }
            
            SecurityConstraint[] constraints = webApp.getSecurityConstraint();
            for (int i=0; i < constraints.length;i++) {
                if (constraints[i] != constraint &&
                        val.equals(constraints[i].getDefaultDisplayName())) {
                    getSectionView().getErrorPanel().setError(new Error(Error.TYPE_FATAL, Error.DUPLICATE_VALUE_MESSAGE, val, displayNameTF));
                    return;
                }
            }
            getSectionView().getErrorPanel().clearError();
            
        }
    }
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (source == displayNameTF) {
            String text = (String)value;
            constraint.setDisplayName(text);
            SectionPanel enclosingPanel = getSectionView().findSectionPanel(constraint);
            enclosingPanel.setTitle(text);
            enclosingPanel.getNode().setDisplayName(text);
        } else if (source == authConstraintCB) {
            if (authConstraintCB.isSelected()) {
                refillAuthConstraint();
            } else {
                setAuthConstraint(null);
            }
        } else if (source == roleNamesTF) {
            refillAuthConstraint();
        } else if (source == authConstraintDescTF) {
            refillAuthConstraint();
        } else if (source == userDataConstraintCB) {
            if (userDataConstraintCB.isSelected()) {
                refillUserDataConstraint();
            } else {
                setUserDataConstraint(null);
            }
        } else if (source == transportGuaranteeCB) {
            refillUserDataConstraint();
        } else if (source == userDataConstraintDescTF) {
            refillUserDataConstraint();
        }
    }
    
    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (source == displayNameTF) {
            displayNameTF.setText(constraint.getDefaultDisplayName());
        }
    }
    
    /** This will be called before model is changed from this panel
     */
    @Override
    protected void startUIChange() {
        dObj.setChangedFromUI(true);
    }
    
    /** This will be called after model is changed from this panel
     */
    @Override
    protected void endUIChange() {
        dObj.modelUpdatedFromUI();
        dObj.setChangedFromUI(false);
    }
    
    private void setUserDataConstraint(UserDataConstraint userDataConstraint) {
        constraint.setUserDataConstraint(userDataConstraint);
    }
    
    private UserDataConstraint getUserDataConstraint() {
        UserDataConstraint userDataConstraint = constraint.getUserDataConstraint();
        if (userDataConstraint == null) {
            try {
                userDataConstraint = (UserDataConstraint) webApp.createBean("UserDataConstraint");  //NOI18N
                constraint.setUserDataConstraint(userDataConstraint);
            } catch (ClassNotFoundException ex) {
            }
        }
        
        return userDataConstraint;
    }
    
    private void refillUserDataConstraint() {
        setUserDataConstraint(null);
        UserDataConstraint userDataConstraint = getUserDataConstraint();
        userDataConstraint.setDescription(userDataConstraintDescTF.getText());
        userDataConstraint.setTransportGuarantee((String) transportGuaranteeCB.getSelectedItem());
    }
    
    private void setAuthConstraint(AuthConstraint authConstraint) {
        constraint.setAuthConstraint(authConstraint);
    }
    
    private AuthConstraint getAuthConstraint() {
        AuthConstraint authConstraint = constraint.getAuthConstraint();
        if (authConstraint == null) {
            try {
                authConstraint = (AuthConstraint) webApp.createBean("AuthConstraint"); //NOI18N
                constraint.setAuthConstraint(authConstraint);
            } catch (ClassNotFoundException ex) {
            }
        }
        
        return authConstraint;
    }
    
    private void refillAuthConstraint() {
        // Null out the previous authConstraint.
        setAuthConstraint(null);
        
        AuthConstraint authConstraint = getAuthConstraint();
        authConstraint.setDescription(authConstraintDescTF.getText());
        
        String roleNamesString = roleNamesTF.getText();
        StringTokenizer tokenizer = new StringTokenizer(roleNamesString, ","); //NOI18N
        
        while (tokenizer.hasMoreTokens()) {
            String roleName = tokenizer.nextToken().trim();
            
            if (roleName.length() > 0)
                authConstraint.addRoleName(roleName);
        }
    }
    
    private String getRoleNamesString(AuthConstraint authConstraint) {
        String names[] = authConstraint.getRoleName();
        String nameString = "";     //NOI18N
        
        for (int i = 0; i < names.length; i++) {
            if (i > 0)
                nameString += ", ";     //NOI18N
            
            nameString += names[i];
        }
        
        return nameString;
    }
    
    private String[] getSelectedRoleNames() {
        return constraint.getAuthConstraint().getRoleName();
    }
    
    private String[] getAllRoleNames() {
        SecurityRole[] roles = webApp.getSecurityRole();
        String[] roleNames = new String[roles.length];
        
        for (int i = 0; i < roles.length; i++) {
            roleNames[i] = roles[i].getRoleName();
        }
        
        return roleNames;
    }
    
    private void setSelectedRoleNames(String[] roleNames) {
        AuthConstraint authConstraint = constraint.getAuthConstraint();
        
        authConstraint.setRoleName(roleNames);
        roleNamesTF.setText(getRoleNamesString(authConstraint));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        displayNameLabel = new javax.swing.JLabel();
        displayNameTF = new javax.swing.JTextField();
        roleNamesLabel = new javax.swing.JLabel();
        roleNamesTF = new javax.swing.JTextField();
        authConstraintDescLabel = new javax.swing.JLabel();
        authConstraintDescTF = new javax.swing.JTextField();
        transportGuaranteeLabel = new javax.swing.JLabel();
        transportGuaranteeCB = new javax.swing.JComboBox();
        webResourceCollectionLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        webResourceCollectionPanel = new javax.swing.JPanel();
        authConstraintCB = new javax.swing.JCheckBox();
        userDataConstraintCB = new javax.swing.JCheckBox();
        webResourceCollectionPanel2 = new javax.swing.JPanel();
        userDataConstraintDescLabel = new javax.swing.JLabel();
        userDataConstraintDescTF = new javax.swing.JTextField();
        editButton = new javax.swing.JButton();

        displayNameLabel.setLabelFor(displayNameTF);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(displayNameLabel, bundle.getString("LBL_displayName")); // NOI18N

        roleNamesLabel.setLabelFor(roleNamesTF);
        org.openide.awt.Mnemonics.setLocalizedText(roleNamesLabel, bundle.getString("LBL_SecurityRoleNames")); // NOI18N
        roleNamesLabel.setEnabled(false);

        roleNamesTF.setEditable(false);
        roleNamesTF.setEnabled(false);

        authConstraintDescLabel.setLabelFor(authConstraintDescTF);
        org.openide.awt.Mnemonics.setLocalizedText(authConstraintDescLabel, bundle.getString("LBL_SecurityRoleDescription")); // NOI18N
        authConstraintDescLabel.setEnabled(false);

        authConstraintDescTF.setEnabled(false);

        transportGuaranteeLabel.setLabelFor(transportGuaranteeCB);
        org.openide.awt.Mnemonics.setLocalizedText(transportGuaranteeLabel, bundle.getString("LBL_TransportGuarantee")); // NOI18N
        transportGuaranteeLabel.setEnabled(false);

        transportGuaranteeCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NONE", "INTEGRAL", "CONFIDENTIAL" }));
        transportGuaranteeCB.setEnabled(false);

        webResourceCollectionLabel.setLabelFor(webResourceCollectionPanel);
        org.openide.awt.Mnemonics.setLocalizedText(webResourceCollectionLabel, bundle.getString("LBL_WebResourceCollection")); // NOI18N

        jPanel1.setLayout(new java.awt.GridBagLayout());

        webResourceCollectionPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(authConstraintCB, bundle.getString("LBL_AuthConstraint")); // NOI18N
        authConstraintCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        authConstraintCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        authConstraintCB.setOpaque(false);
        authConstraintCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                authConstraintCBActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(userDataConstraintCB, bundle.getString("LBL_UserDataConstraint")); // NOI18N
        userDataConstraintCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        userDataConstraintCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        userDataConstraintCB.setOpaque(false);
        userDataConstraintCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userDataConstraintCBActionPerformed(evt);
            }
        });

        webResourceCollectionPanel2.setOpaque(false);

        javax.swing.GroupLayout webResourceCollectionPanel2Layout = new javax.swing.GroupLayout(webResourceCollectionPanel2);
        webResourceCollectionPanel2.setLayout(webResourceCollectionPanel2Layout);
        webResourceCollectionPanel2Layout.setHorizontalGroup(
            webResourceCollectionPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 584, Short.MAX_VALUE)
        );
        webResourceCollectionPanel2Layout.setVerticalGroup(
            webResourceCollectionPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 38, Short.MAX_VALUE)
        );

        userDataConstraintDescLabel.setLabelFor(userDataConstraintDescTF);
        org.openide.awt.Mnemonics.setLocalizedText(userDataConstraintDescLabel, bundle.getString("LBL_UserDataConstraintDescription")); // NOI18N
        userDataConstraintDescLabel.setEnabled(false);

        userDataConstraintDescTF.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(editButton, bundle.getString("LBL_EditRoleNames")); // NOI18N
        editButton.setEnabled(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
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
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(transportGuaranteeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(transportGuaranteeCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(userDataConstraintDescLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(userDataConstraintDescTF, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(displayNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(displayNameTF, javax.swing.GroupLayout.DEFAULT_SIZE, 472, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(userDataConstraintCB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(webResourceCollectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(webResourceCollectionLabel)
                    .addComponent(webResourceCollectionPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(roleNamesLabel)
                            .addComponent(authConstraintDescLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(roleNamesTF, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                            .addComponent(authConstraintDescTF, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)))
                    .addComponent(authConstraintCB))
                .addGap(6, 6, 6)
                .addComponent(editButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(displayNameTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(displayNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webResourceCollectionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webResourceCollectionPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(authConstraintCB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(authConstraintDescLabel)
                    .addComponent(authConstraintDescTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(39, 39, 39)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(webResourceCollectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(roleNamesLabel)
                            .addComponent(roleNamesTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(editButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(userDataConstraintCB)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userDataConstraintDescTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userDataConstraintDescLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(transportGuaranteeCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(transportGuaranteeLabel))
                .addContainerGap(36, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        SecurityRolesEditorPanel dialogPanel = new SecurityRolesEditorPanel(
                getAllRoleNames(), getSelectedRoleNames());
        EditDialog dialog = new EditDialog(dialogPanel,
                NbBundle.getMessage(SecurityConstraintPanel.class,"TTL_RoleNames"),
                false) {
            protected String validate() {
                return null;
            }
        };
              
        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
        d.setVisible(true);
        
        if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
            dObj.modelUpdatedFromUI();
            dObj.setChangedFromUI(true);
            
            String[] selectedRoles = dialogPanel.getSelectedRoles();
            setSelectedRoleNames(selectedRoles);
            dObj.setChangedFromUI(false);
        }
    }//GEN-LAST:event_editButtonActionPerformed
    
    private void userDataConstraintCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userDataConstraintCBActionPerformed
        updateVisualState();
    }//GEN-LAST:event_userDataConstraintCBActionPerformed
    
    private void authConstraintCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_authConstraintCBActionPerformed
        updateVisualState();
    }//GEN-LAST:event_authConstraintCBActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox authConstraintCB;
    private javax.swing.JLabel authConstraintDescLabel;
    private javax.swing.JTextField authConstraintDescTF;
    private javax.swing.JLabel displayNameLabel;
    private javax.swing.JTextField displayNameTF;
    private javax.swing.JButton editButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel roleNamesLabel;
    private javax.swing.JTextField roleNamesTF;
    private javax.swing.JComboBox transportGuaranteeCB;
    private javax.swing.JLabel transportGuaranteeLabel;
    private javax.swing.JCheckBox userDataConstraintCB;
    private javax.swing.JLabel userDataConstraintDescLabel;
    private javax.swing.JTextField userDataConstraintDescTF;
    private javax.swing.JLabel webResourceCollectionLabel;
    private javax.swing.JPanel webResourceCollectionPanel;
    private javax.swing.JPanel webResourceCollectionPanel2;
    // End of variables declaration//GEN-END:variables
    
}
