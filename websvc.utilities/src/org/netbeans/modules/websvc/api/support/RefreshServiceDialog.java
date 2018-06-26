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

package org.netbeans.modules.websvc.api.support;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Milan Kuchtiak
 */
public class RefreshServiceDialog extends javax.swing.JPanel {
    
    private String implClass,url;
    private boolean downloadWsdl;
    
    private RefreshServiceDialog(boolean downloadWsdl, String implClass, String url) {
        this.downloadWsdl=downloadWsdl;
        this.implClass = implClass;
        this.url=url;
        initComponents();
        // display the delete_wsdl checkbox only if wsdl exists
        if (!downloadWsdl) {
            downloadWsdlCheckBox.setVisible(false);
            jLabel1.setVisible(false);
            jTextField1.setVisible(false);
        } else {
            jTextField1.setText(url);
            downloadWsdlCheckBox.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent e) {
                    if (((javax.swing.JCheckBox)e.getSource()).isSelected())
                        jTextField1.setEditable(true);
                    else 
                        jTextField1.setEditable(false);
                }

            });            
        }
    }
 
    public static RefreshServiceDialog.Result open(boolean downloadWsdl, String implClass, String url) {
        String title = NbBundle.getMessage(RefreshServiceDialog.class, "MSG_ConfirmServiceRefresh");
        RefreshServiceDialog refreshDialog = new RefreshServiceDialog(downloadWsdl, implClass, url);
        NotifyDescriptor desc = new NotifyDescriptor.Confirmation(refreshDialog, title, NotifyDescriptor.YES_NO_OPTION);
        Object result = DialogDisplayer.getDefault().notify(desc);
        if (result.equals(NotifyDescriptor.CLOSED_OPTION)) {
            return Result.CLOSE;
        } else if (result.equals(NotifyDescriptor.NO_OPTION)) {
            return Result.CLOSE;
        } else if (refreshDialog.downloadWsdl() && refreshDialog.regenerate()) {
            Result res = Result.DO_ALL;
            res.setWsdlUrl(refreshDialog.getWsdlUrl());
            return res;
        } else if (refreshDialog.downloadWsdl()) {
            Result res = Result.DOWNLOAD_WSDL;
            res.setWsdlUrl(refreshDialog.getWsdlUrl());
            return res;
        } else if (refreshDialog.regenerate()) {
            return Result.REGENERATE_IMPL_CLASS;
        } else return Result.CLOSE;
        
    }
    
    /**
     * This prompts the user if he/she wants to regenerate the impl bean
     * but does not give a choice as to refreshing the nodes (i.e. the nodes are
     * always refreshed.
     */
    public static RefreshServiceDialog.Result openWithOKButtonOnly(boolean downloadWsdl, String implClass, String url) {
        RefreshServiceDialog delDialog = new RefreshServiceDialog(downloadWsdl, implClass, url);
        NotifyDescriptor desc = new NotifyDescriptor.Message(delDialog, NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(desc);
        if(delDialog.regenerate()) {
            return Result.REGENERATE_IMPL_CLASS;
        }
        return Result.CLOSE;
    }
    
    
    private boolean downloadWsdl() {
        if (!downloadWsdl) return false;
        return downloadWsdlCheckBox.isSelected();
    }
    
    private boolean regenerate() {
        return regenerateCheckBox.isSelected();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        downloadWsdlCheckBox = new javax.swing.JCheckBox();
        regenerateCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(downloadWsdlCheckBox, org.openide.util.NbBundle.getMessage(RefreshServiceDialog.class, "MSG_DownloadWsdl", new Object[] {url})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(downloadWsdlCheckBox, gridBagConstraints);

        regenerateCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(regenerateCheckBox, org.openide.util.NbBundle.getMessage(RefreshServiceDialog.class, "MSG_RegenerateImplClass")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        add(regenerateCheckBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RefreshServiceDialog.class, "HINT_DownloadServiceWsdl")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 22, 0, 0);
        add(jLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RefreshServiceDialog.class, "HINT_RegenerateImplClass", new Object[] {implClass+".java.old"})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 22, 0, 0);
        add(jLabel2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(RefreshServiceDialog.class, "HINT_RefreshService")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 20, 0);
        add(jLabel3, gridBagConstraints);

        jTextField1.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 22, 0, 22);
        add(jTextField1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox downloadWsdlCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JCheckBox regenerateCheckBox;
    // End of variables declaration//GEN-END:variables
    
    private String getWsdlUrl() {
        return jTextField1.getText().trim();
    }
    
    public static enum Result {
        CLOSE,
        DOWNLOAD_WSDL,
        REGENERATE_IMPL_CLASS,
        DO_ALL;
        
        private String wsdlUrl;
        
        public void setWsdlUrl(String wsdlUrl) {
            this.wsdlUrl = wsdlUrl;
        }
        
        public String getWsdlUrl() {
            return wsdlUrl;
        }
    }
}
