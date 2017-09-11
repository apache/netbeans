/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.core.windows.documentgroup;

import java.awt.Dialog;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
class NewGroupPanel extends javax.swing.JPanel {

    private static final String DEFAULT_NAME = NbBundle.getMessage(NewGroupPanel.class, "Txt_NEW_GROUP_NAME");
    
    /**
     * Creates new form NewGroupPanel
     */
    public NewGroupPanel() {
        initComponents();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        txtName.selectAll();
        txtName.requestFocusInWindow();
    }
    
    
    
    void showDialog() {
        final DialogDescriptor dd = new DialogDescriptor(this, NbBundle.getMessage(NewGroupPanel.class, "Dlg_NEW_GROUP"), 
                true, DialogDescriptor.OK_CANCEL_OPTION, null, null);
        dd.setHelpCtx( new HelpCtx("org.netbeans.core.windows.documentgroup.NewGroupAction") ); //NOI18N
        boolean hasOpenedDocuments = WindowManagerImpl.getInstance().getEditorTopComponents().length > 0;
        if( !hasOpenedDocuments ) {
            cbKeepOpenedDocuments.setSelected( false );
            cbKeepOpenedDocuments.setEnabled( false );
        }
        txtName.getDocument().addDocumentListener( new DocumentListener() {

            @Override
            public void insertUpdate( DocumentEvent e ) {
                validate( dd );
            }

            @Override
            public void removeUpdate( DocumentEvent e ) {
                validate( dd );
            }

            @Override
            public void changedUpdate( DocumentEvent e ) {
                validate( dd );
            }
        });
        txtName.setText(DEFAULT_NAME);
        
        
        Dialog dlg = DialogDisplayer.getDefault().createDialog( dd );
        dlg.setVisible( true );
        if( DialogDescriptor.OK_OPTION == dd.getValue() ) {
            if( !cbKeepOpenedDocuments.isSelected() ) {
                if( !GroupsManager.closeAllDocuments() ) {
                    return;
                }
            }
            String name = txtName.getText().trim();
            GroupsManager.getDefault().addGroup( name );
            GroupsMenuAction.refreshMenu();
        }
    }
    
    private void validate( DialogDescriptor dd ) {
        String name = txtName.getText();
        if( null == name )
            name = ""; //NOI18N
        name = name.trim();
        dd.setValid( !name.isEmpty() && !DEFAULT_NAME.equals(name) );
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        cbKeepOpenedDocuments = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        org.openide.awt.Mnemonics.setLocalizedText(lblName, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.lblName.text")); // NOI18N

        cbKeepOpenedDocuments.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cbKeepOpenedDocuments, org.openide.util.NbBundle.getMessage(NewGroupPanel.class, "NewGroupPanel.cbKeepOpenedDocuments.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtName))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbKeepOpenedDocuments)
                        .addGap(0, 102, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbKeepOpenedDocuments)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbKeepOpenedDocuments;
    private javax.swing.JLabel lblName;
    private javax.swing.JTextField txtName;
    // End of variables declaration//GEN-END:variables
}
