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

package org.netbeans.modules.j2ee.common;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.support.DatabaseExplorerUIs;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Libor Kotouc
 * @author  Petr Slechta
 */
class DatasourceCustomizer extends javax.swing.JPanel {

    private static final ResourceBundle bundle = NbBundle.getBundle(DatasourceCustomizer.class);

    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport statusLine;
    private boolean dialogOK = false;
    private HashMap<String, Datasource> datasources;
    private String jndiName;
    private String url;
    private String username;
    private String password;
    private String driverClassName;

    public DatasourceCustomizer(List<Datasource> datasources) {
        if (datasources != null) { // transform Set to Map for faster searching
            this.datasources = new HashMap<String, Datasource>();
            for (Iterator it = datasources.iterator(); it.hasNext();) {
                Datasource ds = (Datasource) it.next();
                if (ds.getJndiName() != null)
                    this.datasources.put(ds.getJndiName(), ds);
            }
        }
        initComponents();

        DatabaseExplorerUIs.connect(connCombo, ConnectionManager.getDefault());
        
        connCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                verify();
            }
        });
        jndiNameField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                verify();
            }
            public void insertUpdate(DocumentEvent e) {
                verify();
            }
            public void removeUpdate(DocumentEvent e) {
                verify();
            }
        });
    }
    
    public boolean showDialog() {
        descriptor = new DialogDescriptor
                    (this, NbBundle.getMessage(DatasourceCustomizer.class, "LBL_DatasourceCustomizer"), true,  //NOI18N
                    DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
                    DialogDescriptor.DEFAULT_ALIGN,
                    new HelpCtx(DatasourceCustomizer.class),
                    new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            boolean close = true;
                            if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
                                boolean valid = handleConfirmation();
                                close = valid;
                                dialogOK = valid;
                            }
                        
                            if (close) {
                                dialog.dispose();
                            }
		     }
		 } 
                );
        descriptor.setClosingOptions(new Object[] { DialogDescriptor.CANCEL_OPTION });
        statusLine = descriptor.createNotificationLineSupport();
        
        verify();

        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        repaint();
        
        return dialogOK;
    }
    
    private boolean handleConfirmation() {
        jndiName = jndiNameField.getText().trim();
        DatabaseConnection conn = (DatabaseConnection)connCombo.getSelectedItem();
        if (conn.getPassword() == null) {
            ConnectionManager.getDefault().showConnectionDialog(conn);
        }
        if (conn.getPassword() == null) {
            //user did not provide the password
            statusLine.setErrorMessage(bundle.getString("ERR_NoPassword"));  //NOI18N
            return false;
        }
        url = conn.getDatabaseURL();
        username = conn.getUser();
        password = conn.getPassword();
        driverClassName = conn.getDriverClass();
        return true;
    }
    
    private boolean verify() {
        boolean isValid = verifyJndiName();
        if (isValid)
            isValid = verifyConnection();
        return isValid;
    }
    
    private boolean verifyJndiName() {
        boolean valid = true;
        
        String jndiNameFromField = jndiNameField.getText().trim();
        if (jndiNameFromField.length() == 0) {
            statusLine.setInformationMessage(bundle.getString("ERR_JNDI_NAME_EMPTY"));  // NOI18N
            valid = false;
        }
        else if (datasourceAlreadyExists(jndiNameFromField)) {
            statusLine.setErrorMessage(bundle.getString("ERR_DS_EXISTS")); // NOI18N
            valid = false;
        }
        else {
            statusLine.clearMessages();
        }
        descriptor.setValid(valid);
        return valid;
    }
    
    private boolean verifyConnection() {
        boolean valid = true;
        
        if (!(connCombo.getSelectedItem() instanceof DatabaseConnection)) {
            statusLine.setInformationMessage(bundle.getString("ERR_NO_CONN_SELECTED"));  // NOI18N
            valid = false;
        }
        else {
            statusLine.clearMessages();
        }
        descriptor.setValid(valid);
        return valid;
    }
    
    private boolean datasourceAlreadyExists(String jndiName) {
        return datasources.containsKey(jndiName);
    }
    
    String getJndiName() {
        return jndiName;
    }

    String getUrl() {
        return url;
    }

    String getUsername() {
        return username;
    }

    String getPassword() {
        return password;
    }

    String getDriverClassName() {
        return driverClassName;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jndiNameField = new javax.swing.JTextField();
        connCombo = new javax.swing.JComboBox();

        setForeground(new java.awt.Color(255, 0, 0));

        jLabel1.setLabelFor(jndiNameField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DatasourceCustomizer.class, "LBL_DSC_JndiName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DatasourceCustomizer.class, "LBL_DSC_DbConn")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jndiNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                    .addComponent(connCombo, 0, 327, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jndiNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(connCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox connCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jndiNameField;
    // End of variables declaration//GEN-END:variables

}
