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

import org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.openide.util.NbBundle;

/**
 * SecurityRoleRefTablePanel.java
 *
 * Panel for displaying the securirty role reference table.
 *
 * @author  ptliu
 */
public class SecurityRoleRefTablePanel extends DefaultTablePanel {
    
    private SecurityRoleRefTableModel model;
    private WebApp webApp;
    private DDDataObject dObj;
    private Servlet servlet;
    
    /** Creates new form SecurityRoleRefTablePanel */
    public SecurityRoleRefTablePanel(final DDDataObject dObj,
            final SecurityRoleRefTableModel model) {
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
    
    void setModel(WebApp webApp, Servlet servlet, SecurityRoleRef[] roleRefs) {
        model.setData(servlet, roleRefs);
        model.setWebApp(webApp);
        this.webApp=webApp;
        this.servlet = servlet;
    }
    
    private class TableActionListener implements java.awt.event.ActionListener {
        private boolean add;
        
        TableActionListener(boolean add) {
            this.add=add;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            final int row = (add?-1:getTable().getSelectedRow());
            final SecurityRoleRefPanel dialogPanel = new SecurityRoleRefPanel(
                    webApp.getSecurityRole());
            
            if (!add) {
                SecurityRoleRef roleRef = model.getSecurityRoleRef(row);
                dialogPanel.setRoleRefName(roleRef.getRoleName());
                dialogPanel.setRoleRefLink(roleRef.getRoleLink());
                dialogPanel.setDescription(roleRef.getDefaultDescription());
            }
            
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(EjbRefsTablePanel.class,"TTL_SecurityRoleRef"),add) {
                protected String validate() {
                    String name = dialogPanel.getRoleRefName().trim();
                    SecurityRoleRef roleRef = null;
                    
                    if (row != -1)
                        roleRef = model.getSecurityRoleRef(row);
                    
                    if (name.length()==0) {
                        return NbBundle.getMessage(SecurityRoleRefTablePanel.class,"TXT_EmptySecurityRoleRefName");
                    } else {
                        SecurityRoleRef[] roleRefs = servlet.getSecurityRoleRef();
                        
                        for (int i = 0; i < roleRefs.length; i++) {
                            if (roleRefs[i] != roleRef && name.equals(roleRefs[i].getRoleName())) {
                                return NbBundle.getMessage(SecurityRoleRefTablePanel.class,"TXT_SecurityRoleRefNameExists",name);
                            }
                        }
                    }
                    
                    if (isEmpty(dialogPanel.getRoleRefLink())) {
                        return NbBundle.getMessage(SecurityRoleRefTablePanel.class,"TXT_EmptySecurityRoleRefLink");
                    }
                    
                    return null;
                }
            };
            
            if (add)
                dialog.setValid(false); // disable OK button
            
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getRoleRefNameTF().getDocument().addDocumentListener(docListener);
            dialogPanel.getDescriptionTA().getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            
            dialogPanel.getRoleRefNameTF().getDocument().removeDocumentListener(docListener);
            dialogPanel.getDescriptionTA().getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                
                String roleRefName = dialogPanel.getRoleRefName();
                String roleRefLink = dialogPanel.getRoleRefLink();
                String description = dialogPanel.getDescription();
                
                
                if (add)
                    model.addRow(new String[]{roleRefName, roleRefLink, description});
                else
                    model.editRow(row, new String[]{roleRefName, roleRefLink, description});
                
                dObj.setChangedFromUI(false);
            }
        }
    }
    
    /**
     * @return true if the given <code>str</code> is null, empty or contains
     *  only spaces.
     */
    private boolean isEmpty(String str){
        return null == str || "".equals(str.trim());
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
