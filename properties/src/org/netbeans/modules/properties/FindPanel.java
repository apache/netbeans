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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.properties;

import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.ContainerOrderFocusTraversalPolicy;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import org.openide.util.NbBundle;


/**
 * Find panel for Resource Bundles table view component. GUI represenation only.
 *
 * @author  Peter Zavadsky
 */
public class FindPanel extends javax.swing.JPanel {

    /** Creates new form FindPanel. */
    public FindPanel() {
        initComponents ();
        initAccessibility ();
        findCombo.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
             public void keyTyped(KeyEvent evt) {
                if (evt.getKeyChar() == '\n') {
                    findButton.doClick(20);
                    evt.consume();
                }
            }
        });
    }

    private String getBundleString(String s){
        return NbBundle.getMessage(FindPanel.class, s);
    }

    // Accessor methods.
    
    /** Accessor to buttons. */
    public JButton[] getButtons() {
        return new JButton[] { findButton, cancelButton};
    }
    
    /** Accessor to combo box. */
    public JComboBox getComboBox() {
        return findCombo;
    }

    /** Accessor to highlight check box. */
    public JCheckBox getHighlightCheck() {
        return highlightCheck;
    }
    
    /** Accessor to match case check box. */
    public JCheckBox getMatchCaseCheck() {
        return matchCaseCheck;
    }
    
    /** Accessor to backward check box. */
    public JCheckBox getBackwardCheck() {
        return backwardCheck;
    }
    
    /** Accessor to wrap check box. */
    public JCheckBox getWrapCheck() {
        return wrapCheck;
    }
    
    /** Accessor to row check box. */
    public JCheckBox getRowCheck() {
        return rowCheck;
    }
    
    private void initAccessibility () {
        this.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_FindPanel"));
        
        findButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_CTL_Find"));
        rowCheck.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_CTL_SearchByRows"));
        wrapCheck.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_CTL_WrapSearch"));
        matchCaseCheck.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_CTL_MatchCaseCheck"));
        cancelButton.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_CTL_Cancel"));
        backwardCheck.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_CTL_BackwardCheck"));
        findCombo.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_CTL_FindCombo"));
        highlightCheck.getAccessibleContext().setAccessibleDescription(getBundleString("ACS_CTL_HighlightCheck"));
        
    }    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        findLabel = new javax.swing.JLabel();
        findCombo = new javax.swing.JComboBox();
        highlightCheck = new javax.swing.JCheckBox();
        matchCaseCheck = new javax.swing.JCheckBox();
        backwardCheck = new javax.swing.JCheckBox();
        wrapCheck = new javax.swing.JCheckBox();
        rowCheck = new javax.swing.JCheckBox();
        findButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setFocusCycleRoot(true);
        setFocusTraversalPolicy(new ContainerOrderFocusTraversalPolicy());
        setLayout(new java.awt.GridBagLayout());

        findLabel.setLabelFor(findCombo);
        org.openide.awt.Mnemonics.setLocalizedText(findLabel, getBundleString("LBL_Find")); // NOI18N
        findLabel.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(findLabel, gridBagConstraints);

        findCombo.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 11, 0, 0);
        add(findCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(highlightCheck, getBundleString("CTL_HighlightCheck")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(highlightCheck, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(matchCaseCheck, getBundleString("CTL_MatchCaseCheck")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(matchCaseCheck, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(backwardCheck, getBundleString("CTL_BackwardCheck")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 0);
        add(backwardCheck, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(wrapCheck, getBundleString("CTL_WrapSearch")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(wrapCheck, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(rowCheck, getBundleString("CTL_SearchByRows")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 0);
        add(rowCheck, gridBagConstraints);

        findButton.setText(getBundleString("CTL_Find")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(12, 11, 0, 11);
        add(findButton, gridBagConstraints);

        cancelButton.setText(getBundleString("CTL_Cancel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 11);
        add(cancelButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    public boolean requestFocusInWindow() {
        return findCombo.requestFocusInWindow();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox backwardCheck;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton findButton;
    private javax.swing.JComboBox findCombo;
    private javax.swing.JLabel findLabel;
    private javax.swing.JCheckBox highlightCheck;
    private javax.swing.JCheckBox matchCaseCheck;
    private javax.swing.JCheckBox rowCheck;
    private javax.swing.JCheckBox wrapCheck;
    // End of variables declaration//GEN-END:variables

}
