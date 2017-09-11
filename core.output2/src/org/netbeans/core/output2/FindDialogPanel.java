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

package org.netbeans.core.output2;

import java.awt.event.ActionEvent;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Vector;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbPreferences;

class FindDialogPanel extends javax.swing.JPanel {

    static final long serialVersionUID =5048678953767663114L;
    private static final String KEY_REGEXP = "regExp"; // NOI18N
    private static final String KEY_MATCHCASE = "matchCase"; // NOI18N

    private static Reference<FindDialogPanel> panel = null;
    private JButton acceptButton;
    private static Vector<Object> history = new Vector<Object>();
    
    /** Initializes the Form */
    FindDialogPanel() {
        regExp = NbPreferences.forModule(Controller.class).getBoolean(KEY_REGEXP, false);
        matchCase = NbPreferences.forModule(Controller.class).getBoolean(KEY_MATCHCASE, false);
        initComponents();
        acceptButton = new JButton();
        Mnemonics.setLocalizedText(chbRegExp, NbBundle.getMessage(FindDialogPanel.class, "LBL_Use_RegExp"));
        Mnemonics.setLocalizedText(chbMatchCase, NbBundle.getMessage(FindDialogPanel.class, "LBL_Match_Case"));

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(FindDialogPanel.class, "ACSN_Find"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FindDialogPanel.class, "ACSD_Find"));
        findWhat.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FindDialogPanel.class, "ACSD_Find_What"));
        acceptButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FindDialogPanel.class, "ACSD_FindBTN"));

        findWhat.setModel(new DefaultComboBoxModel(history));
        findWhat.getEditor().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                acceptButton.doClick();
            }
        });
        findWhatLabel.setFocusable(false);
    }

    public static FindDialogPanel getPanel() {
        FindDialogPanel result = null;
        if (panel != null) {
            result = panel.get();
        }
        if (result == null) {
            result = new FindDialogPanel();
            panel = new SoftReference<FindDialogPanel> (result);
        }
        return result;
    }
    
    void setFindText(String text) {
        int end = text.indexOf("\n");
        String txt = text;
        if (end  > -1) {
            txt = text.substring(0, end);
        }
        if (!txt.equals(findWhat.getSelectedItem())) {
            findWhat.insertItemAt(txt, 0);
            findWhat.setSelectedIndex(0);
        }
        selectText();
    }
    
    private void selectText() {
        Component comp = findWhat.getEditor().getEditorComponent();
        if (comp instanceof JTextField) {
            JTextField fld = (JTextField)comp;
            fld.setSelectionStart(0);
            fld.setSelectionEnd(fld.getText().length());
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

        findWhatLabel = new javax.swing.JLabel();
        findWhat = new javax.swing.JComboBox();
        chbRegExp = new javax.swing.JCheckBox();
        chbMatchCase = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        findWhatLabel.setLabelFor(findWhat);
        findWhatLabel.setText(org.openide.util.NbBundle.getMessage(FindDialogPanel.class, "LBL_Find_What")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 5, 5);
        add(findWhatLabel, gridBagConstraints);

        findWhat.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 5, 5);
        add(findWhat, gridBagConstraints);

        chbRegExp.setSelected(regExp());
        chbRegExp.setText(org.openide.util.NbBundle.getMessage(FindDialogPanel.class, "LBL_Use_RegExp")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 5, 5);
        add(chbRegExp, gridBagConstraints);

        chbMatchCase.setSelected(matchCase());
        chbMatchCase.setText(org.openide.util.NbBundle.getMessage(FindDialogPanel.class, "LBL_Match_Case")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 5, 5);
        add(chbMatchCase, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chbMatchCase;
    private javax.swing.JCheckBox chbRegExp;
    protected javax.swing.JComboBox findWhat;
    protected javax.swing.JLabel findWhatLabel;
    // End of variables declaration//GEN-END:variables


    private String getPattern() {
        return (String) findWhat.getSelectedItem();
    }

    private void updateHistory() {
        Object pattern = findWhat.getEditor().getItem();

        history.add( 0, pattern );
        for ( int i = history.size() - 1; i > 0; i-- ) {
            if ( history.get( i ).equals( pattern ) ) {
                history.remove( i );
                break;
            }
        }
    }

    private static String result;
    private static boolean regExp;
    private static boolean matchCase;

    static String getResult(String selection, String dlgTitle, String comboLabel, String buttonText) {
        final FindDialogPanel findPanel = getPanel();
        Mnemonics.setLocalizedText(findPanel.acceptButton, NbBundle.getMessage(FindDialogPanel.class, buttonText));
        Mnemonics.setLocalizedText(findPanel.findWhatLabel, NbBundle.getMessage(FindDialogPanel.class, comboLabel));
        if (selection != null) {
            findPanel.setFindText(selection);
        }
        findPanel.selectText();

        DialogDescriptor dd = new DialogDescriptor(findPanel, NbBundle.getMessage(FindDialogPanel.class, dlgTitle),
                true, new Object[] {findPanel.acceptButton, DialogDescriptor.CANCEL_OPTION}, findPanel.acceptButton,
                DialogDescriptor.RIGHT_ALIGN, null, null);
        Object res = DialogDisplayer.getDefault().notify(dd);
        if (res.equals(findPanel.acceptButton)) {
            findPanel.updateHistory();
            regExp = findPanel.chbRegExp.getModel().isSelected();
            matchCase  = findPanel.chbMatchCase.getModel().isSelected();
            NbPreferences.forModule(FindDialogPanel.class).putBoolean(KEY_REGEXP, regExp);
            NbPreferences.forModule(FindDialogPanel.class).putBoolean(KEY_MATCHCASE, matchCase);
            result = findPanel.getPattern();
            return result;
        } else {
            result = null;
            return null;
        }
    }

    static String result() {
        return result;
    }

    static boolean regExp() {
        return regExp;
    }

    static boolean matchCase() {
        return matchCase;
    }
}
