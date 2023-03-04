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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
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
final class DatasourceComboBoxCustomizer extends javax.swing.JPanel {
    
    private Dialog dialog = null;
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport statusLine;
    private boolean dialogOK = false;
    private final HashMap<String, Datasource> datasources;
    private String jndiName;
    private String url;
    private String username;
    private String password;
    private String driverClassName;

    public DatasourceComboBoxCustomizer(Set<Datasource> datasources) {
        this.datasources = new HashMap<String, Datasource>();
        if (datasources != null) { // transform Set to Map for faster searching
            for (Iterator it = datasources.iterator(); it.hasNext();) {
                Datasource datasource = (Datasource) it.next();
                if (datasource.getJndiName() != null)
                    this.datasources.put(datasource.getJndiName(), datasource);
            }
        }
        initComponents();
        
        DatabaseExplorerUIs.connect(connCombo, ConnectionManager.getDefault());

        connCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                verify();
            }
        });
        jndiNameField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent documentEvent) {
                verify();
            }
            public void insertUpdate(DocumentEvent documentEvent) {
                verify();
            }
            public void removeUpdate(DocumentEvent documentEvent) {
                verify();
            }
        });
        addAncestorListener(new AncestorListener() {
            public void ancestorAdded(AncestorEvent event) {
                verify();
            }
            public void ancestorRemoved(AncestorEvent event) {
                verify();
            }
            public void ancestorMoved(AncestorEvent event) {
                verify();
            }
        });
    }
    
    public boolean showDialog() {
        descriptor = new DialogDescriptor
            (this, NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "LBL_DatasourceCustomizer"), true, // NOI18N
             DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION,
             DialogDescriptor.DEFAULT_ALIGN,
             new HelpCtx("DatasourceUIHelper_DatasourceCustomizer"), // NOI18N
             new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
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
             });
        statusLine = descriptor.createNotificationLineSupport();
        descriptor.setClosingOptions(new Object[] { DialogDescriptor.CANCEL_OPTION });
        
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
            statusLine.setErrorMessage(NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "ERR_NoPassword")); // NOI18N
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
        
        String jndiName = jndiNameField.getText().trim();
        if (jndiName.length() == 0) {
            statusLine.setInformationMessage(NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "ERR_JNDI_NAME_EMPTY")); // NOI18N
            valid = false;
        }
        else
        if (datasourceAlreadyExists(jndiName)) {
            statusLine.setErrorMessage(NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "ERR_DS_EXISTS")); // NOI18N
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
            statusLine.setInformationMessage(NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "ERR_NO_CONN_SELECTED")); // NOI18N
            valid = false;
        }
        else {
            statusLine.clearMessages();
        }

        descriptor.setValid(valid);
        return valid;
    }
    
    // TODO this is incorrect - it is needed to normalize jndiName (e.g. "DefaultDs" vs. "java:DefaultDs" vs. "java:/DefaultDs")
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
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "LBL_DSC_JndiName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DatasourceComboBoxCustomizer.class, "LBL_DSC_DbConn")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jndiNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                    .addComponent(connCombo, 0, 337, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jndiNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(connCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox connCombo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField jndiNameField;
    // End of variables declaration//GEN-END:variables
    
}
