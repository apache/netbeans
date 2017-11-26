/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.hibernate.loaders.cfg.multiview;

import org.netbeans.modules.hibernate.loaders.cfg.*;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.openide.util.NbBundle;

/**
 * Panel containing the security data table and add/edit/remove buttons
 * 
 * @author Dongmei Cao
 */
public class SecurityTablePanel extends DefaultTablePanel {

    private SecurityTableModel model;
    private HibernateCfgDataObject configDataObject;

    public SecurityTablePanel(final HibernateCfgDataObject dObj, final SecurityTableModel model) {
        super(model);
        this.model = model;
        this.configDataObject = dObj;
        removeButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
            configDataObject.modelUpdatedFromUI();
            int row = getTable().getSelectedRow();
            model.removeRow(row);
            }
        });
        editButton.addActionListener(new TableActionListener(false));
        addButton.addActionListener(new TableActionListener(true));
    }

    private class TableActionListener implements java.awt.event.ActionListener {

        private boolean add;

        TableActionListener(boolean add) {
            this.add = add;
        }

        public void actionPerformed(java.awt.event.ActionEvent evt) {
            final int row = (add ? -1 : getTable().getSelectedRow());
            final GrantPanel dialogPanel = new GrantPanel();

            if (!add) {
                String roleName = (String) model.getValueAt(row, 0);
                String entityName = (String) model.getValueAt(row, 1);
                String actions = (String) model.getValueAt(row, 2);
                dialogPanel.initValues(roleName, entityName, actions);
            }

            EditDialog dialog = new EditDialog(dialogPanel, NbBundle.getMessage(SecurityTablePanel.class, "LBL_Security"), add) {

                protected String validate() {
                    // TODO: more validation code later
                    String role = dialogPanel.getRole();
                    String entityName = dialogPanel.getEntityName();
                    String actions = dialogPanel.getActions();
                    if (role.length() == 0) {
                        return NbBundle.getMessage(SecurityTablePanel.class, "TXT_Role_Empty");
                    } 
                    if (entityName.length() == 0) {
                        return NbBundle.getMessage(SecurityTablePanel.class, "TXT_Entity_Name_Empty");
                    }
                    if(actions.length() == 0 ) {
                        return NbBundle.getMessage(SecurityTablePanel.class, "TXT_Actions_Empty");
                    }
                    return null;
                }
            };

            if (add) {
                dialog.setValid(false);
            } // disable OK button
            
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getRoleTextField().getDocument().addDocumentListener(docListener);
            dialogPanel.getEntityNameTextField().getDocument().addDocumentListener(docListener);
            dialogPanel.getActionsTextField().getDocument().addDocumentListener(docListener);

            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            dialogPanel.getRoleTextField().getDocument().removeDocumentListener(docListener);
            dialogPanel.getEntityNameTextField().getDocument().removeDocumentListener(docListener);
            dialogPanel.getActionsTextField().getDocument().removeDocumentListener(docListener);

            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                configDataObject.modelUpdatedFromUI();
                String role = dialogPanel.getRole();
                String entityName = dialogPanel.getEntityName();
                String actions = dialogPanel.getActions();
                if (add) {
                    model.addRow(new String[]{role, entityName, actions});
                } else {
                    model.editRow(row, new String[]{role, entityName, actions});
                }
            }
        }
    }
}
