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
 * Software is Sun Microsystems, Inc. Portions Copyright 2007 Sun
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

package org.netbeans.modules.websvc.wsitconf.ui.client.subpanels;

import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.ui.ClassDialog;
import org.netbeans.modules.websvc.wsitconf.ui.client.PanelEnabler;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandler;
import org.netbeans.modules.xml.wsdl.model.Binding;

/**
 *
 * @author  Martin Grebac
 */

public class DynamicCredsPanel extends javax.swing.JPanel implements PanelEnabler {
    
    private boolean inSync = false;

    private Binding binding;
    private boolean enable;
    
    private Project project;

    /** Creates new form DynamicCredentials */
    public DynamicCredsPanel(Binding b, Project project, boolean enable) {
        this.binding = b;
        this.enable = enable;
        this.project = project;
               
        initComponents();
        
        sync();
    }
    
    public void sync() {
        inSync = true;

        String usernameCallback = ProprietarySecurityPolicyModelHelper.getCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER);
        if (usernameCallback != null) {
            setCallbackHandler(usernameCallback, CallbackHandler.USERNAME_CBHANDLER);
        }
        String passwdCallback = ProprietarySecurityPolicyModelHelper.getCallbackHandler(binding, CallbackHandler.PASSWORD_CBHANDLER);
        if (passwdCallback != null) {
            setCallbackHandler(passwdCallback, CallbackHandler.PASSWORD_CBHANDLER);
        }
        
        enableDisable();
        
        inSync = false;
    }
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (inSync) return;
        ConfigVersion cfgVersion = PolicyModelHelper.getConfigVersion(binding);
        if (source.equals(usernameHandlerField)) {
            String classname = getCallbackHandler(CallbackHandler.USERNAME_CBHANDLER);
            if ((classname != null) && (classname.length() == 0)) {
                classname = null;
            }
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER, classname, null, true);
            return;
        }

        if (source.equals(passwdHandlerField)) {
            String classname = getCallbackHandler(CallbackHandler.PASSWORD_CBHANDLER);
            if ((classname != null) && (classname.length() == 0)) {
                classname = null;
            }
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.PASSWORD_CBHANDLER, classname, null, true);
            return;
        }
        
        enableDisable();
    }

    private void enableDisable() {        
        passwdBrowseButton.setEnabled(isPanelEnabled());
        passwdHandlerField.setEnabled(isPanelEnabled());
        passwdHandlerLabel.setEnabled(isPanelEnabled());
        usernameBrowseButton.setEnabled(isPanelEnabled());
        usernameHandlerField.setEnabled(isPanelEnabled());
        usernameHandlerLabel.setEnabled(isPanelEnabled());
    }

    public boolean isPanelEnabled() {
        return enable;
    }
    
    public void enablePanel(boolean doEnable) {
        enable = doEnable;
    }
    
    private String getCallbackHandler(String type) {
        if (CallbackHandler.USERNAME_CBHANDLER.equals(type)) return usernameHandlerField.getText();
        if (CallbackHandler.PASSWORD_CBHANDLER.equals(type)) return passwdHandlerField.getText();
        return null;
    }

    private void setCallbackHandler(String classname, String type) {
        if (CallbackHandler.USERNAME_CBHANDLER.equals(type)) this.usernameHandlerField.setText(classname);
        if (CallbackHandler.PASSWORD_CBHANDLER.equals(type)) this.passwdHandlerField.setText(classname);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        usernameHandlerLabel = new javax.swing.JLabel();
        usernameHandlerField = new javax.swing.JTextField();
        passwdHandlerLabel = new javax.swing.JLabel();
        passwdHandlerField = new javax.swing.JTextField();
        usernameBrowseButton = new javax.swing.JButton();
        passwdBrowseButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(usernameHandlerLabel, org.openide.util.NbBundle.getMessage(DynamicCredsPanel.class, "LBL_UsernameCBHLabel")); // NOI18N

        usernameHandlerField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                usernameHandlerFieldKeyReleased(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(passwdHandlerLabel, org.openide.util.NbBundle.getMessage(DynamicCredsPanel.class, "LBL_PasswordCBHLabel")); // NOI18N

        passwdHandlerField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                passwdHandlerFieldKeyReleased(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(usernameBrowseButton, org.openide.util.NbBundle.getMessage(DynamicCredsPanel.class, "LBL_Username_Browse")); // NOI18N
        usernameBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernameBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(passwdBrowseButton, org.openide.util.NbBundle.getMessage(DynamicCredsPanel.class, "LBL_Password_Browse")); // NOI18N
        passwdBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwdBrowseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(passwdHandlerLabel)
                        .addGap(13, 13, 13))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(usernameHandlerLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(usernameHandlerField, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
                    .addComponent(passwdHandlerField, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(usernameBrowseButton)
                    .addComponent(passwdBrowseButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernameBrowseButton)
                    .addComponent(usernameHandlerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameHandlerLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(passwdBrowseButton)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(passwdHandlerLabel)
                        .addComponent(passwdHandlerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void passwdHandlerFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwdHandlerFieldKeyReleased
    setValue(passwdHandlerField, null);
}//GEN-LAST:event_passwdHandlerFieldKeyReleased

private void usernameHandlerFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_usernameHandlerFieldKeyReleased
    setValue(usernameHandlerField, null);
}//GEN-LAST:event_usernameHandlerFieldKeyReleased

    private void passwdBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwdBrowseButtonActionPerformed
        if (project != null) {
            ClassDialog classDialog = new ClassDialog(project, "javax.security.auth.callback.CallbackHandler"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setCallbackHandler(selectedClass, CallbackHandler.PASSWORD_CBHANDLER);
//                    ConfigVersion cfgVersion = PolicyModelHelper.getConfigVersion(binding);
                    ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.PASSWORD_CBHANDLER, selectedClass, null, true);
                    break;
                }
            }
        }
    }//GEN-LAST:event_passwdBrowseButtonActionPerformed

    private void usernameBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameBrowseButtonActionPerformed
        if (project != null) {
            ClassDialog classDialog = new ClassDialog(project, "javax.security.auth.callback.CallbackHandler"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setCallbackHandler(selectedClass, CallbackHandler.USERNAME_CBHANDLER);
//                    ConfigVersion cfgVersion = PolicyModelHelper.getConfigVersion(binding);
                    ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER, selectedClass, null, true);
                    break;
                }
            }
        }
    }//GEN-LAST:event_usernameBrowseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton passwdBrowseButton;
    private javax.swing.JTextField passwdHandlerField;
    private javax.swing.JLabel passwdHandlerLabel;
    private javax.swing.JButton usernameBrowseButton;
    private javax.swing.JTextField usernameHandlerField;
    private javax.swing.JLabel usernameHandlerLabel;
    // End of variables declaration//GEN-END:variables
    
}
