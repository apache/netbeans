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

package org.netbeans.modules.web.debug.breakpoints;

import org.openide.util.NbBundle;

/**
 * @author Martin Grebac
 */
public class ActionsPanel extends javax.swing.JPanel {

    private JspLineBreakpoint  breakpoint;

    /** Creates new form LineBreakpointPanel */
    public ActionsPanel (JspLineBreakpoint b) {
        breakpoint = b;
        initComponents ();

        cbSuspend.addItem(NbBundle.getMessage(ActionsPanel.class, "LBL_CB_Actions_Panel_Suspend_None"));
        cbSuspend.addItem(NbBundle.getMessage(ActionsPanel.class, "LBL_CB_Actions_Panel_Suspend_Current"));
        cbSuspend.addItem(NbBundle.getMessage(ActionsPanel.class, "LBL_CB_Actions_Panel_Suspend_All"));
        switch (b.getSuspend ()) {
            case JspLineBreakpoint.SUSPEND_NONE:
                cbSuspend.setSelectedIndex (0);
                break;
            case JspLineBreakpoint.SUSPEND_EVENT_THREAD:
                cbSuspend.setSelectedIndex (1);
                break;
            case JspLineBreakpoint.SUSPEND_ALL:
                cbSuspend.setSelectedIndex (2);
                break;
        }
        if (b.getPrintText () != null)
            tfPrintText.setText (b.getPrintText ());
    }
    
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        tfPrintText = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        cbSuspend = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.TitledBorder(NbBundle.getMessage(ActionsPanel.class, "LBL_Actions")));
        tfPrintText.setToolTipText(NbBundle.getMessage(ActionsPanel.class, "TTT_TF_Actions_Panel_Print_Text"));
        tfPrintText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //TODO
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        gridBagConstraints.weightx = 1.0;
        add(tfPrintText, gridBagConstraints);
        tfPrintText.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ActionsPanel.class, "ACSD_TF_Actions_Panel_Print_Text"));

        jLabel1.setDisplayedMnemonic(NbBundle.getMessage(ActionsPanel.class, "MN_L_Actions_Panel_Suspend").charAt(0));
        jLabel1.setLabelFor(cbSuspend);
        jLabel1.setText(NbBundle.getMessage(ActionsPanel.class, "L_Actions_Panel_Suspend"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ActionsPanel.class, "ASCD_L_Actions_Panel_Suspend"));

        cbSuspend.setToolTipText(NbBundle.getMessage(ActionsPanel.class, "TTT_CB_Actions_Panel_Suspend"));
        cbSuspend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //TODO
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(cbSuspend, gridBagConstraints);
        cbSuspend.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ActionsPanel.class, "ASCD_CB_Actions_Panel_Suspend"));

        jLabel2.setDisplayedMnemonic(NbBundle.getMessage(ActionsPanel.class, "MN_L_Actions_Panel_Print_Text").charAt(0));
        jLabel2.setLabelFor(tfPrintText);
        jLabel2.setText(NbBundle.getMessage(ActionsPanel.class, "L_Actions_Panel_Print_Text"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(jLabel2, gridBagConstraints);

    }
    
    /**
     * Called when "Ok" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public void ok () {
        String printText = tfPrintText.getText ();
        if (printText.trim ().length () > 0) {
            breakpoint.setPrintText (printText.trim ());
        } else {
            breakpoint.setPrintText (null);
        }
        
        switch (cbSuspend.getSelectedIndex ()) {
            case 0:
                breakpoint.setSuspend (JspLineBreakpoint.SUSPEND_NONE);
                break;
            case 1:
                breakpoint.setSuspend (JspLineBreakpoint.SUSPEND_EVENT_THREAD);
                break;
            case 2:
                breakpoint.setSuspend (JspLineBreakpoint.SUSPEND_ALL);
                break;
        }
    }
    
    // Variables declaration - do not modify
    private javax.swing.JComboBox cbSuspend;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField tfPrintText;
    // End of variables declaration
    
}
