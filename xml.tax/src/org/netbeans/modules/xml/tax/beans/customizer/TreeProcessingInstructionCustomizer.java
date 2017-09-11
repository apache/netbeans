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
package org.netbeans.modules.xml.tax.beans.customizer;

import java.beans.PropertyChangeEvent;

import org.netbeans.tax.TreeProcessingInstruction;
import org.netbeans.tax.TreeException;

import org.netbeans.modules.xml.tax.util.TAXUtil;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeProcessingInstructionCustomizer extends AbstractTreeCustomizer {

    /** Serial Version UID */
    private static final long serialVersionUID = 1151841201717291218L;


    //
    // init
    //

    /** */
    public TreeProcessingInstructionCustomizer () {
        super ();
        
        initComponents ();
        targetLabel.setDisplayedMnemonic (Util.THIS.getChar ("MNE_xmlTarget")); // NOI18N
        
        initAccessibility ();
    }
    
    
    //
    // itself
    //
    
    /**
     */
    protected final TreeProcessingInstruction getProcessingInstruction () {
        return (TreeProcessingInstruction)getTreeObject ();
    }
    
    /**
     */
    protected final void safePropertyChange (PropertyChangeEvent pche) {
        super.safePropertyChange (pche);
        
        if (pche.getPropertyName ().equals (TreeProcessingInstruction.PROP_TARGET)) {
            updateTargetComponent ();
        } else if (pche.getPropertyName ().equals (TreeProcessingInstruction.PROP_DATA)) {
            updateDataComponent ();
        }
    }
    
    /**
     */
    protected final void updateProcessingInstructionTarget () {
        try {
            getProcessingInstruction ().setTarget (targetField.getText ());
        } catch (TreeException exc) {
            updateTargetComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateTargetComponent () {
        targetField.setText (getProcessingInstruction ().getTarget ());
    }
    
    /**
     */
    protected final void updateProcessingInstructionData () {
        try {
            getProcessingInstruction ().setData (dataTextArea.getText ());
        } catch (TreeException exc) {
            updateDataComponent ();
            TAXUtil.notifyTreeException (exc);
        }
    }
    
    /**
     */
    protected final void updateDataComponent () {
        dataTextArea.setText (getProcessingInstruction ().getData ());
    }
    
    /**
     */
    protected final void initComponentValues () {
        updateTargetComponent ();
        updateDataComponent ();
    }
    
    
    /**
     */
    protected void updateReadOnlyStatus (boolean editable) {
        targetField.setEditable (editable);
        dataTextArea.setEditable (editable);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        targetLabel = new javax.swing.JLabel();
        targetField = new javax.swing.JTextField();
        dataPanel = new javax.swing.JPanel();
        dataScroll = new javax.swing.JScrollPane();
        dataTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(350, 230));
        targetLabel.setText(Util.THIS.getString ("PROP_xmlTarget"));
        targetLabel.setLabelFor(targetField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(targetLabel, gridBagConstraints);

        targetField.setColumns(20);
        targetField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                targetFieldActionPerformed(evt);
            }
        });

        targetField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                targetFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                targetFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(targetField, gridBagConstraints);

        dataPanel.setLayout(new java.awt.BorderLayout());

        dataPanel.setPreferredSize(new java.awt.Dimension(350, 230));
        dataTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                dataTextAreaFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                dataTextAreaFocusLost(evt);
            }
        });

        dataTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                dataTextAreaKeyPressed(evt);
            }
        });

        dataScroll.setViewportView(dataTextArea);

        dataPanel.add(dataScroll, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(dataPanel, gridBagConstraints);

    }//GEN-END:initComponents

    private void dataTextAreaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dataTextAreaFocusGained
        if ("new".equals(getClientProperty("xml-edit-mode"))) {  // NOI18N
            dataTextArea.selectAll();
        }
    }//GEN-LAST:event_dataTextAreaFocusGained

    private void targetFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_targetFieldFocusGained
        if ("new".equals(getClientProperty("xml-edit-mode"))) {  // NOI18N
            targetField.selectAll();
        }

    }//GEN-LAST:event_targetFieldFocusGained

    private void dataTextAreaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dataTextAreaKeyPressed
        // Add your handling code here:
        if ( applyKeyPressed (evt) ) {
            updateProcessingInstructionData ();
        }
    }//GEN-LAST:event_dataTextAreaKeyPressed

    private void dataTextAreaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dataTextAreaFocusLost
        // Add your handling code here:
        updateProcessingInstructionData ();
    }//GEN-LAST:event_dataTextAreaFocusLost
        
    /**
     */
    private void targetFieldFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_targetFieldFocusLost
        // Add your handling code here:
        updateProcessingInstructionTarget ();
    }//GEN-LAST:event_targetFieldFocusLost
    
    
    /**
     */
    private void targetFieldActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetFieldActionPerformed
        // Add your handling code here:
        updateProcessingInstructionTarget ();
    }//GEN-LAST:event_targetFieldActionPerformed
    
        
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea dataTextArea;
    private javax.swing.JLabel targetLabel;
    private javax.swing.JPanel dataPanel;
    private javax.swing.JTextField targetField;
    private javax.swing.JScrollPane dataScroll;
    // End of variables declaration//GEN-END:variables
    
    /** Initialize accesibility
     */
    public void initAccessibility (){        
        this.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_TreeProcessingInstructionCustomizer"));
        targetField.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_targetField"));
        dataTextArea.getAccessibleContext ().setAccessibleDescription (Util.THIS.getString ("ACSD_dataPanel1"));
        dataTextArea.getAccessibleContext ().setAccessibleName (Util.THIS.getString ("ACSN_dataPanel1"));
    }
}
