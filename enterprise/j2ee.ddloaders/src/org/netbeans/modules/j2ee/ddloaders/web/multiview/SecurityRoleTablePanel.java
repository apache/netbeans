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


package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.openide.util.NbBundle;

/**
 * SecurityRoleTablePanel.java
 *
 * Panel for displaying the security role table.
 *
 * @author ptliu
 */
public class SecurityRoleTablePanel extends DefaultTablePanel {
    private SecurityRoleTableModel model;
    private WebApp webApp;
    private DDDataObject dObj;
    
    /** Creates new form ContextParamPanel */
    public SecurityRoleTablePanel(final DDDataObject dObj, final SecurityRoleTableModel model) {
    	super(model);
    	this.model=model;
        this.dObj=dObj;
        
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                int row = getTable().getSelectedRow();
                model.removeRow(row);
                dObj.setChangedFromUI(false);
            }
        });
        editButton.addActionListener(new TableActionListener(false));
        addButton.addActionListener(new TableActionListener(true));
    }

    void setModel(WebApp webApp, SecurityRole[] roles) {
        model.setData(webApp, roles);
        this.webApp=webApp;
    }
    
    private class TableActionListener implements java.awt.event.ActionListener {
        private boolean add;
        
        TableActionListener(boolean add) {
            this.add=add;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            final int row = (add?-1:getTable().getSelectedRow());
            final SecurityRolePanel dialogPanel = new SecurityRolePanel();
            final String currentRoleName = null;
            SecurityRole role = null;
            
            if (!add) {
                role = model.getSecurityRole(row);
                dialogPanel.setRoleName(role.getRoleName());
                dialogPanel.setDescription(role.getDefaultDescription());
            }
            
            final SecurityRole currentRole = role;
            
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(EjbRefsTablePanel.class,"TTL_SecurityRole"),add) {
                protected String validate() {
                    String name = dialogPanel.getRoleName().trim();
            
                    if (name.length()==0) {
                        return NbBundle.getMessage(SecurityRoleTablePanel.class,"TXT_EmptySecurityRoleName");
                    } else {
                        SecurityRole[] roles = webApp.getSecurityRole();
                        boolean exists=false;
                        
                        for (int i = 0; i < roles.length; i++) {
                            if (name.equals(roles[i].getRoleName()) &&
                                    roles[i] != currentRole) {
                                return NbBundle.getMessage(SecurityRoleTablePanel.class,"TXT_SecurityRoleNameExists",name);
                            }
                        }
                    }
                    
                    return null;
                }
            };
       
            if (add) 
                dialog.setValid(false); // disable OK button
  
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getRoleNameTF().getDocument().addDocumentListener(docListener);
            dialogPanel.getDescriptionTA().getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            
            dialogPanel.getRoleNameTF().getDocument().removeDocumentListener(docListener);
            dialogPanel.getDescriptionTA().getDocument().removeDocumentListener(docListener);
        
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                
                String roleName = dialogPanel.getRoleName();
                String description = dialogPanel.getDescription();
         
                if (add) 
                    model.addRow(new String[]{roleName, description});
                else 
                    model.editRow(row, new String[]{roleName, description});
                
                dObj.setChangedFromUI(false);
            }
        }   
    }
 
}
