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

package org.netbeans.core.execution.beaninfo.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;

import org.openide.execution.NbProcessDescriptor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Custom property editor for NbProcessDescriptor class.
*
* @author  Ian Formanek
*/
public class NbProcessDescriptorCustomEditor extends javax.swing.JPanel
implements PropertyChangeListener {
    private NbProcessDescriptorEditor editor;

    private static int DEFAULT_WIDTH = 400;
    private static int DEFAULT_HEIGHT = 250;

    static final long serialVersionUID =-2766277953540349247L;
    /** Creates new NbProcessDescriptorCustomEditor
     * @param editor the NbProcessDescriptorEditor
     */
    public NbProcessDescriptorCustomEditor (NbProcessDescriptorEditor editor, PropertyEnv env) {
        this.editor = editor;
        initComponents ();
        
        if ( editor.pd != null ) {
            processField.setText (editor.pd.getProcessName ());
            argumentsArea.setText (editor.pd.getArguments ());
            hintArea.setText (editor.pd.getInfo ());
        }
        

        processField.getAccessibleContext().setAccessibleDescription(getString("ACSD_NbProcessDescriptorCustomEditor.processLabel"));
        argumentsArea.getAccessibleContext().setAccessibleDescription(getString("ACSD_NbProcessDescriptorCustomEditor.argumentsLabel"));
        hintArea.getAccessibleContext().setAccessibleDescription(getString("ACSD_NbProcessDescriptorCustomEditor.argumentKeyLabel"));
        jButton1.getAccessibleContext().setAccessibleDescription(getString("ACSD_NbProcessDescriptorCustomEditor.jButton1"));
        
        getAccessibleContext().setAccessibleDescription(getString("ACSD_CustomNbProcessDescriptorEditor"));

        HelpCtx.setHelpIDString (this, NbProcessDescriptorCustomEditor.class.getName ());

        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);
    }

    public java.awt.Dimension getPreferredSize() {
        java.awt.Dimension inh = super.getPreferredSize ();
        return new java.awt.Dimension (DEFAULT_WIDTH, Math.max (inh.height, DEFAULT_HEIGHT));
    }

    /** Get the customized property value.
    * @return the property value
    * @exception InvalidStateException when the custom property editor does not contain a valid property value
    *            (and thus it should not be set)
    */
    private Object getPropertyValue () throws IllegalStateException {
        if ( editor.pd == null )
            return new NbProcessDescriptor (processField.getText (), argumentsArea.getText () );
        return new NbProcessDescriptor (processField.getText (), argumentsArea.getText (), editor.pd.getInfo ());
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        processLabel = new javax.swing.JLabel();
        processField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        argumentsLabel = new javax.swing.JLabel();
        argumentsScrollPane = new javax.swing.JScrollPane();
        argumentsArea = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        argumentKeyLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        hintArea = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        processLabel.setLabelFor(processField);
        org.openide.awt.Mnemonics.setLocalizedText(processLabel, getString("CTL_NbProcessDescriptorCustomEditor.processLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 12);
        add(processLabel, gridBagConstraints);
        processLabel.getAccessibleContext().setAccessibleDescription("Process");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 3.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 5);
        add(processField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, getString("CTL_NbProcessDescriptorCustomEditor.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jButton1, gridBagConstraints);
        jButton1.getAccessibleContext().setAccessibleDescription("Browse");

        argumentsLabel.setLabelFor(argumentsArea);
        org.openide.awt.Mnemonics.setLocalizedText(argumentsLabel, org.openide.util.NbBundle.getMessage(NbProcessDescriptorCustomEditor.class, "CTL_NbProcessDescriptorCustomEditor.argumentsLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 12);
        add(argumentsLabel, gridBagConstraints);
        argumentsLabel.getAccessibleContext().setAccessibleDescription("Process arguments");

        argumentsScrollPane.setMinimumSize(new java.awt.Dimension(22, 35));
        argumentsScrollPane.setViewportView(argumentsArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(argumentsScrollPane, gridBagConstraints);

        jPanel1.setLayout(new java.awt.BorderLayout(0, 2));

        argumentKeyLabel.setLabelFor(hintArea);
        org.openide.awt.Mnemonics.setLocalizedText(argumentKeyLabel, getString("CTL_NbProcessDescriptorCustomEditor.argumentKeyLabel.text")); // NOI18N
        jPanel1.add(argumentKeyLabel, java.awt.BorderLayout.NORTH);
        argumentKeyLabel.getAccessibleContext().setAccessibleDescription("Arguments hint");

        hintArea.setBackground((java.awt.Color) javax.swing.UIManager.getDefaults ().get ("Label.background"));
        hintArea.setEditable(false);
        hintArea.setLineWrap(true);
        jScrollPane1.setViewportView(hintArea);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 7.0;
        gridBagConstraints.weighty = 7.0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // Add your handling code here:
        JFileChooser chooser = org.netbeans.beaninfo.editors.FileEditor.createHackedFileChooser();
        chooser.setMultiSelectionEnabled (false);
        File init = new File(processField.getText()); // #13372
        if (init.isFile()) {
            chooser.setCurrentDirectory(init.getParentFile());
            chooser.setSelectedFile(init);
        }
        int retVal = chooser.showOpenDialog (this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            String absolute_name = chooser.getSelectedFile ().getAbsolutePath ();
            //System.out.println("file:" + absolute_name); // NOI18N
            processField.setText (absolute_name);
        }
    }//GEN-LAST:event_jButton1ActionPerformed




    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel argumentKeyLabel;
    private javax.swing.JTextArea argumentsArea;
    private javax.swing.JLabel argumentsLabel;
    private javax.swing.JScrollPane argumentsScrollPane;
    private javax.swing.JTextArea hintArea;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField processField;
    private javax.swing.JLabel processLabel;
    // End of variables declaration//GEN-END:variables

    private static final String getString(String s) {
        return NbBundle.getMessage(NbProcessDescriptorCustomEditor.class, s);
    }

}
