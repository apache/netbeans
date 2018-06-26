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

package org.netbeans.modules.php.editor.codegen.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek, Andrei Badea
 */
public class ProgressPanel extends JPanel {

    // XXX copied from j2ee.core.utilities. No better way since the module
    // is in the Java cluster.

    private Dialog dialog;

    public ProgressPanel() {
        initComponents();
    }

    public void open(JComponent progressComponent) {
        holder.add(progressComponent, BorderLayout.CENTER);

        DialogDescriptor dd = new DialogDescriptor(
                this,
                NbBundle.getMessage(ProgressPanel.class, "MSG_PleaseWait"),
                true,
                new Object[0],
                DialogDescriptor.NO_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null,
                true);
        dialog = DialogDisplayer.getDefault().createDialog(dd);
        if (dialog instanceof JDialog) {
            JDialog jDialog = ((JDialog) dialog);
            jDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            JRootPane rootPane = jDialog.getRootPane();
            rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel"); // NOI18N
            rootPane.getActionMap().put("cancel", new AbstractAction() { // NOI18N
                @Override
                public void actionPerformed(ActionEvent event) {
                    if (cancelButton.isEnabled()) {
                        cancelButton.doClick();
                    }
                }
            });
        }
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    public void close() {
        if (dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
        }
    }

    public boolean isOpen() {
        return dialog != null && dialog.isVisible();
    }

    public void setText(String text) {
        info.setText(text);
    }

    public String getText() {
        return info.getText();
    }

    public void setCancelVisible(boolean cancelVisible) {
        cancelButton.setVisible(cancelVisible);
    }

    public boolean isCancelVisible() {
        return cancelButton.isVisible();
    }

    public void setCancelEnabled(boolean cancelEnabled) {
        cancelButton.setEnabled(cancelEnabled);
    }

    public boolean isCancelEnabled() {
        return cancelButton.isEnabled();
    }

    public void addCancelActionListener(ActionListener listener) {
        cancelButton.addActionListener(listener);
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        info = new javax.swing.JLabel();
        holder = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        info.setText(" ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 11);
        add(info, gridBagConstraints);

        holder.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 11);
        add(holder, gridBagConstraints);

        cancelButton.setText(org.openide.util.NbBundle.getMessage(ProgressPanel.class, "LBL_Cancel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 11, 11);
        add(cancelButton, gridBagConstraints);

    }// </editor-fold>


    // Variables declaration - do not modify
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel holder;
    private javax.swing.JLabel info;
    // End of variables declaration

    @Override
    public Dimension getPreferredSize() {
        Dimension orig = super.getPreferredSize();
        return new Dimension(500, orig.height);
    }

}
