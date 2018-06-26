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

package org.netbeans.modules.web.jsf.dialogs;

import java.util.Hashtable;
import java.util.Iterator;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author  radko
 */
public class AddNavigationRuleDialog extends javax.swing.JPanel implements ValidatingPanel{
    private JSFConfigDataObject config;
    public final String NO_FROM_VIEW_DEFINED = "nofromviewdefined";
    private Hashtable existingRules = null;
    /** Creates new form AddNavigationRuleDialog */
    public AddNavigationRuleDialog(JSFConfigDataObject config) {
        initComponents();
        this.config = config;
    }

    public javax.swing.text.JTextComponent[] getDocumentChangeComponents() {
        return new javax.swing.text.JTextComponent[]{jTextFieldFromView};
    }

    public javax.swing.AbstractButton[] getStateChangeComponents() {
        return new javax.swing.AbstractButton[]{  };
    }

    public String validatePanel() {
        String message = null;
        if(existingRules == null){
            existingRules = new Hashtable();
            NavigationRule rule;
            FacesConfig facesConfig = ConfigurationUtils.getConfigModel(config.getPrimaryFile(), true).getRootComponent();
            
            Iterator iter = facesConfig.getNavigationRules().iterator();
            while (iter.hasNext()){
                rule = (NavigationRule) iter.next();
                if (rule.getFromViewId() != null){
                    existingRules.put(rule.getFromViewId(), "");
                } 
                else { // if there is a rule withouth from view, put symbolic constant. 
                    existingRules.put(NO_FROM_VIEW_DEFINED, "");
                }
            }
        }
        String fromView = getFromView();
        if (fromView == null || fromView.length() == 0){
            fromView = NO_FROM_VIEW_DEFINED;
        }
        if (existingRules.get(fromView)!=null){
            if (fromView.equals(NO_FROM_VIEW_DEFINED)){
                message = NbBundle.getMessage(AddManagedBeanDialog.class,"MSG_AddNavigationRule_RuleExistWithNoFromView");
            }
            else {
                message = NbBundle.getMessage(AddManagedBeanDialog.class,"MSG_AddNavigationRule_RuleExist");
            }
        }
        return message;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelFromView = new javax.swing.JLabel();
        jTextFieldFromView = new javax.swing.JTextField();
        jButtonBrowse = new javax.swing.JButton();
        jLabelDesc = new javax.swing.JLabel();
        jScrollPaneDesc = new javax.swing.JScrollPane();
        jTextAreaDesc = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/dialogs/Bundle").getString("ACSD_AddNavigationRuleDialog"));
        jLabelFromView.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(AddNavigationRuleDialog.class, "MNE_FromView").charAt(0));
        jLabelFromView.setLabelFor(jTextFieldFromView);
        jLabelFromView.setText(org.openide.util.NbBundle.getMessage(AddNavigationRuleDialog.class, "LBL_FromView"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 5, 12);
        add(jLabelFromView, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 5, 0);
        add(jTextFieldFromView, gridBagConstraints);
        jTextFieldFromView.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/dialogs/Bundle").getString("ACSD_RuleFromFiew"));

        jButtonBrowse.setMnemonic(org.openide.util.NbBundle.getMessage(AddNavigationRuleDialog.class, "MNE_Browse").charAt(0));
        jButtonBrowse.setText(org.openide.util.NbBundle.getMessage(AddNavigationRuleDialog.class, "LBL_Browse"));
        jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 5, 11);
        add(jButtonBrowse, gridBagConstraints);
        jButtonBrowse.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/dialogs/Bundle").getString("ACSD_RuleBrowse"));

        jLabelDesc.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(AddNavigationRuleDialog.class, "MNE_BeanDescription").charAt(0));
        jLabelDesc.setLabelFor(jTextAreaDesc);
        jLabelDesc.setText(org.openide.util.NbBundle.getMessage(AddNavigationRuleDialog.class, "LBL_RuleDescription"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(jLabelDesc, gridBagConstraints);

        jTextAreaDesc.setColumns(20);
        jTextAreaDesc.setRows(5);
        jScrollPaneDesc.setViewportView(jTextAreaDesc);
        jTextAreaDesc.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/dialogs/Bundle").getString("ACSD_RuleDescription"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jScrollPaneDesc, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
        try{
            org.netbeans.api.project.SourceGroup[] groups = JSFConfigUtilities.getDocBaseGroups(config.getPrimaryFile());
            org.openide.filesystems.FileObject fo = BrowseFolders.showDialog(groups);
            if (fo!=null) {
                String res = "/"+JSFConfigUtilities.getResourcePath(groups,fo,'/',true);
                jTextFieldFromView.setText(res);
            }
        } catch (java.io.IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_jButtonBrowseActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JLabel jLabelDesc;
    private javax.swing.JLabel jLabelFromView;
    private javax.swing.JScrollPane jScrollPaneDesc;
    private javax.swing.JTextArea jTextAreaDesc;
    private javax.swing.JTextField jTextFieldFromView;
    // End of variables declaration//GEN-END:variables
    
    public String getFromView(){
        return jTextFieldFromView.getText().trim();
    }
    
    public String getDescription(){
        return jTextAreaDesc.getText();
    }
}
