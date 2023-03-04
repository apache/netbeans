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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.EjbRefsTablePanel;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.SecurityRolePanel;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.SecurityRoleTablePanel;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.openide.util.NbBundle;

/**
 * Table model for the security roles table.
 *
 * @author ptliu
 */
public class EjbJarSecurityRolesTableModel extends InnerTableModel {
    private static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_RoleName"),
    Utils.getBundleMessage("LBL_Description")};
    
    private static final int[] COLUMN_WIDTHS = new int[]{170, 250};
    
    private AssemblyDescriptor assemblyDesc;
    
    private EjbJar ejbJar;
    
    public EjbJarSecurityRolesTableModel(XmlMultiViewDataSynchronizer synchronizer,
            EjbJar ejbJar) {
        super(synchronizer, COLUMN_NAMES, COLUMN_WIDTHS);
        
        this.ejbJar = ejbJar;
        this.assemblyDesc = ejbJar.getSingleAssemblyDescriptor();
    }
    
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        SecurityRole role = assemblyDesc.getSecurityRole(rowIndex);
        
        switch (columnIndex) {
            case 0:
                role.setRoleName((String) value);
                break;
            case 1:
                role.setDescription((String) value);
                break;
        }
        
        modelUpdatedFromUI();
        fireTableCellUpdated(rowIndex, columnIndex);
    }
    
    public int getRowCount() {
        if (assemblyDesc == null) return 0;
        
        return assemblyDesc.getSecurityRole().length;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        SecurityRole role = assemblyDesc.getSecurityRole(rowIndex);
        
        switch (columnIndex) {
            case 0:
                return role.getRoleName();
            case 1:
                return role.getDefaultDescription();
        }
        
        return null;
    }
    
    public int addRow() {
        
        if (assemblyDesc == null) {
            assemblyDesc = getAssemblyDesc();
        }
        
        final SecurityRolePanel dialogPanel = new SecurityRolePanel();
        final String currentRoleName = null;
        
        EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(EjbRefsTablePanel.class,"TTL_SecurityRole"), true) {
            protected String validate() {
                String name = dialogPanel.getRoleName().trim();
                
                if (name.length()==0) {
                    return NbBundle.getMessage(SecurityRoleTablePanel.class,"TXT_EmptySecurityRoleName");
                } else {
                    SecurityRole[] roles = assemblyDesc.getSecurityRole();
                    boolean exists=false;
                    
                    for (int i = 0; i < roles.length; i++) {
                        if (name.equals(roles[i].getRoleName())){
                            return NbBundle.getMessage(SecurityRoleTablePanel.class,"TXT_SecurityRoleNameExists",name);
                        }
                    }
                }
                
                return null;
            }
        };
        dialog.setValid(false);
        javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
        dialogPanel.getRoleNameTF().getDocument().addDocumentListener(docListener);
        dialogPanel.getDescriptionTA().getDocument().addDocumentListener(docListener);
        
        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
        d.setVisible(true);
        
        dialogPanel.getRoleNameTF().getDocument().removeDocumentListener(docListener);
        dialogPanel.getDescriptionTA().getDocument().removeDocumentListener(docListener);
        
        if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
            SecurityRole role = assemblyDesc.newSecurityRole();
            role.setRoleName(dialogPanel.getRoleName());
            role.setDescription(dialogPanel.getDescription());
            assemblyDesc.addSecurityRole(role);
            modelUpdatedFromUI();
        }
        
        return getRowCount() - 1;
    }
    
    
    
    
    public void removeRow(final int row) {
        SecurityRole role = assemblyDesc.getSecurityRole(row);
        assemblyDesc.removeSecurityRole(role);
        
        modelUpdatedFromUI();
    }
    
    private AssemblyDescriptor getAssemblyDesc() {
        AssemblyDescriptor assemblyDesc = ejbJar.getSingleAssemblyDescriptor();
        
        if (assemblyDesc == null) {
            assemblyDesc = ejbJar.newAssemblyDescriptor();
            ejbJar.setAssemblyDescriptor(assemblyDesc);
        }
        
        return assemblyDesc;
    }
}
