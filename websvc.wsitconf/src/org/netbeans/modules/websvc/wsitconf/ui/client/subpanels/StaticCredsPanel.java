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

import org.netbeans.modules.websvc.wsitconf.ui.client.PanelEnabler;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandler;
import org.netbeans.modules.xml.wsdl.model.Binding;

/**
 *
 * @author  Martin Grebac
 */

public class StaticCredsPanel extends javax.swing.JPanel implements PanelEnabler {
    
    private boolean inSync = false;

    private Binding binding;
    private boolean enable;
    
    /** Creates new form DynamicCredentials */
    public StaticCredsPanel(Binding b, boolean enable) {
        this.binding = b;
        this.enable = enable;
               
        initComponents();
        sync();
    }
    
    public void sync() {
        inSync = true;

        String defaultUsername = ProprietarySecurityPolicyModelHelper.getDefaultUsername(binding);
        if (defaultUsername != null) {
            setDefaultUsername(defaultUsername);
        }
        String defaultPassword = ProprietarySecurityPolicyModelHelper.getDefaultPassword(binding);
        if (defaultPassword != null) {
            setDefaultPassword(defaultPassword);
        }
        
        enableDisable();
        
        inSync = false;
    }
    
    private String getDefaultPassword() {
        return String.copyValueOf(this.defaultPasswordField.getPassword());
    }

    private void setDefaultPassword(String passwd) {
        this.defaultPasswordField.setText(passwd);
    }

    private String getDefaultUsername() {
        return this.defaultUsernameTextField.getText();
    }

    private void setDefaultUsername(String username) {
        this.defaultUsernameTextField.setText(username);
    }
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (inSync) {
            return;
        }        
        if (source.equals(defaultUsernameTextField)) {
            String u = getDefaultUsername();
            if ((u != null) && (u.length() == 0)) {
                u = null;
            }
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER, null, u, true);
        } else if (source.equals(defaultPasswordField)) {
            String p = getDefaultPassword();
            if ((p != null) && (p.length() == 0)) {
                p = null;
            }
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.PASSWORD_CBHANDLER, null, p, true);
        }
        
        enableDisable();
    }

    private void enableDisable() {        
        defaultPasswordField.setEnabled(isPanelEnabled());
        defaultPasswordLabel.setEnabled(isPanelEnabled());
        defaultUsernameLabel.setEnabled(isPanelEnabled());
        defaultUsernameTextField.setEnabled(isPanelEnabled());
    }
    
    public boolean isPanelEnabled() {
        return enable;
    }
    
    public void enablePanel(boolean doEnable) {
        enable = doEnable;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        defaultPasswordLabel = new javax.swing.JLabel();
        defaultUsernameLabel = new javax.swing.JLabel();
        defaultUsernameTextField = new javax.swing.JTextField();
        defaultPasswordField = new javax.swing.JPasswordField();

        org.openide.awt.Mnemonics.setLocalizedText(defaultPasswordLabel, org.openide.util.NbBundle.getMessage(StaticCredsPanel.class, "LBL_DefaultPasswordLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(defaultUsernameLabel, org.openide.util.NbBundle.getMessage(StaticCredsPanel.class, "LBL_DefaultUsernameLabel")); // NOI18N

        defaultUsernameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                defaultUsernameTextFieldKeyReleased(evt);
            }
        });

        defaultPasswordField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                defaultPasswordFieldKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(defaultUsernameLabel)
                    .addComponent(defaultPasswordLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(defaultUsernameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                    .addComponent(defaultPasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(defaultUsernameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(defaultUsernameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(defaultPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(defaultPasswordLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {defaultPasswordField, defaultUsernameTextField});

    }// </editor-fold>//GEN-END:initComponents

private void defaultPasswordFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_defaultPasswordFieldKeyReleased
    setValue(defaultPasswordField, null);
}//GEN-LAST:event_defaultPasswordFieldKeyReleased

private void defaultUsernameTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_defaultUsernameTextFieldKeyReleased
    setValue(defaultUsernameTextField, null);
}//GEN-LAST:event_defaultUsernameTextFieldKeyReleased
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPasswordField defaultPasswordField;
    private javax.swing.JLabel defaultPasswordLabel;
    private javax.swing.JLabel defaultUsernameLabel;
    private javax.swing.JTextField defaultUsernameTextField;
    // End of variables declaration//GEN-END:variables
    
}
