/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

/*
 * FormCustomEditorAdvanced.java
 *
 * Created on February 26, 2001, 1:06 PM
 */

package org.netbeans.modules.form;

/**
 *
 * @author  Ian Formanek, Vladimir Zboril
 */
public class FormCustomEditorAdvanced extends javax.swing.JPanel {

    static final int DEFAULT_WIDTH = 350;
    static final int DEFAULT_HEIGHT = 300;

    static final long serialVersionUID =-885210213146017493L;

    /** Creates new form FormCustomEditorAdvanced */
    public FormCustomEditorAdvanced(String preCode, String postCode) {
        initComponents();
        preCheckBox.setText(
            FormUtils.getBundleString("CTL_GeneratePreInitializationCode")); // NOI18N
        postCheckBox.setText(
            FormUtils.getBundleString("CTL_GeneratePostInitializationCode")); // NOI18N
        preCheckBox.setMnemonic(
            FormUtils.getBundleString("CTL_GeneratePreInitializationCode_Mnemonic") // NOI18N
                .charAt(0));
        postCheckBox.setMnemonic(
            FormUtils.getBundleString("CTL_GeneratePostInitializationCode_Mnemonic") // NOI18N
                .charAt(0));
        preEditorPane.setContentType("text/x-java");   // allow syntax coloring // NOI18N
        postEditorPane.setContentType("text/x-java");  // allow syntax coloring // NOI18N

        // restore state according to parameters
        preCheckBox.setSelected(preCode != null);
        postCheckBox.setSelected(postCode != null);

        // restore current pre/post code, if set
        if (preCode != null) preEditorPane.setText(preCode);
        if (postCode != null) postEditorPane.setText(postCode);
        
        org.openide.util.HelpCtx.setHelpIDString(this, "gui.source.modifying.property"); // NOI18N
        
        preEditorPane.setEnabled(preCode != null);
        postEditorPane.setEnabled(postCode != null);
        
        // cursors (carets) must be hidden explicitely in some situations
        preEditorPane.getCaret().setVisible(preCheckBox.isSelected() && preEditorPane.hasFocus());
        postEditorPane.getCaret().setVisible(postCheckBox.isSelected() && postEditorPane.hasFocus());

        preEditorPane.getAccessibleContext().setAccessibleName(preCheckBox.getText());
        postEditorPane.getAccessibleContext().setAccessibleName(postCheckBox.getText());

        preCheckBox.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_GeneratePreInitializationCode")); // NOI18N
        postCheckBox.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_GeneratePostInitializationCode")); // NOI18N
        preEditorPane.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_GeneratePreInitializationCodeArea")); // NOI18N
        postEditorPane.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_CTL_GeneratePostInitializationCodeArea")); // NOI18N
        getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_FormCustomEditorAdvanced")); // NOI18N
    }

    String getPreCode() {
        if (!preCheckBox.isSelected()) return null;
        if ("".equals(preEditorPane.getText())) return null; // NOI18N
        return preEditorPane.getText();
    }

    String getPostCode() {
        if (!postCheckBox.isSelected()) return null;
        if ("".equals(postEditorPane.getText())) return null; // NOI18N
        return postEditorPane.getText();
    }

    public java.awt.Dimension getPreferredSize() {
        java.awt.Dimension inh = super.getPreferredSize();
        return new java.awt.Dimension(Math.max(inh.width, DEFAULT_WIDTH), Math.max(inh.height, DEFAULT_HEIGHT));
    }
       
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        prePanel = new javax.swing.JPanel();
        preCheckBox = new javax.swing.JCheckBox();
        preScrollPane = new javax.swing.JScrollPane();
        preEditorPane = new javax.swing.JEditorPane();
        postPanel = new javax.swing.JPanel();
        postCheckBox = new javax.swing.JCheckBox();
        postScrollPane = new javax.swing.JScrollPane();
        postEditorPane = new javax.swing.JEditorPane();
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        prePanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints2;
        
        preCheckBox.setText("jCheckBox1");
        preCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                preCheckBoxItemStateChanged(evt);
            }
        });
        
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
        prePanel.add(preCheckBox, gridBagConstraints2);
        
        preScrollPane.setViewportView(preEditorPane);
        
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints2.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints2.insets = new java.awt.Insets(5, 0, 0, 0);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        prePanel.add(preScrollPane, gridBagConstraints2);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(prePanel, gridBagConstraints1);
        
        postPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints3;
        
        postCheckBox.setText("jCheckBox2");
        postCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                postCheckBoxItemStateChanged(evt);
            }
        });
        
        gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
        postPanel.add(postCheckBox, gridBagConstraints3);
        
        postScrollPane.setViewportView(postEditorPane);
        
        gridBagConstraints3 = new java.awt.GridBagConstraints();
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 1;
        gridBagConstraints3.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints3.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints3.insets = new java.awt.Insets(5, 0, 0, 0);
        gridBagConstraints3.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints3.weightx = 1.0;
        gridBagConstraints3.weighty = 1.0;
        postPanel.add(postScrollPane, gridBagConstraints3);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(postPanel, gridBagConstraints1);
        
    }//GEN-END:initComponents

    private void postCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_postCheckBoxItemStateChanged
        // Add your handling code here:
        postEditorPane.setEnabled(postCheckBox.isSelected());
        if (postCheckBox.isSelected()) {
            postEditorPane.requestFocus();
        }
    }//GEN-LAST:event_postCheckBoxItemStateChanged

    private void preCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_preCheckBoxItemStateChanged
        // Add your handling code here:
        preEditorPane.setEnabled(preCheckBox.isSelected());
        if (preCheckBox.isSelected()) {
            preEditorPane.requestFocus();
        }
    }//GEN-LAST:event_preCheckBoxItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel prePanel;
    private javax.swing.JCheckBox preCheckBox;
    private javax.swing.JScrollPane preScrollPane;
    private javax.swing.JEditorPane preEditorPane;
    private javax.swing.JPanel postPanel;
    private javax.swing.JCheckBox postCheckBox;
    private javax.swing.JScrollPane postScrollPane;
    private javax.swing.JEditorPane postEditorPane;
    // End of variables declaration//GEN-END:variables

}
