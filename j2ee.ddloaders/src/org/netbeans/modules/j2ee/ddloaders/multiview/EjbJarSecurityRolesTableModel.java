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
