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
package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.AbstractListModel;

import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.Dialog;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx ;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;

/**
 * A JPanel that presents a list of tables for selection
 *
 * @author  jhoff
 */
public class AddTableDlg extends javax.swing.JPanel {

    /** A return status code - returned if Cancel button has been pressed */
    public static final int     RET_CANCEL = 0;

    /** A return status code - returned if OK button has been pressed */
    public static final int     RET_OK = 1;

    // The model for the JList component - a String array
    private String[]            _tableList;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel              _mainPanel;
    // The Swing component whose model is the _tableList
    private JList               _tableJList;
    private JScrollPane         _tableScrollPane;
    // End of variables declaration//GEN-END:variables

    private JLabel              tableListLabel;

    private int                 returnStatus = RET_CANCEL;
    private Dialog              dialog;

    // Default Constructor

    public AddTableDlg() {
        this(null, true);
    }

    /** Creates new form AddTableDlg */

    public AddTableDlg(String[] tableList,
                       boolean modal)
    {
        _tableList = tableList;
        initComponents();

        ActionListener listener = new ActionListener () {
                public void actionPerformed (ActionEvent evt) {
                    Object o = evt.getSource();
                    if (o == NotifyDescriptor.CANCEL_OPTION) {
                        returnStatus = RET_CANCEL;
                    } else if (o == NotifyDescriptor.OK_OPTION) {
                        // do something useful
                        returnStatus = RET_OK;
                    }
                }
            };

        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    returnStatus = RET_OK;
                    dialog.setVisible(false);
                }
            }
        };
        _tableJList.addMouseListener(mouseListener);


        DialogDescriptor dlg =
            new DialogDescriptor(this,
                                 NbBundle.getMessage(AddTableDlg.class, "Add_Table_Title"),     // NOI18N
                                 modal,
                                 listener);
        dlg.setHelpCtx (
            new HelpCtx( "projrave_ui_elements_editors_about_query_editor" ) );        // NOI18N

        dialog = DialogDisplayer.getDefault().createDialog(dlg);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddTableDlg.class, "TABLE_LIST_a11yDescription"));
        dialog.setVisible(true);
    }

    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }

    public Object[] getSelectedValues() {
        return _tableJList.getSelectedValues();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        java.awt.GridBagConstraints gridBagConstraints;

        _mainPanel = new JPanel();
        _tableScrollPane = new JScrollPane();
        _tableJList = new JList();

        setLayout(new java.awt.GridBagLayout());

        _mainPanel.setLayout(new java.awt.GridBagLayout());

        // Set the model to be the array that was passed to it
        _tableJList.setModel(new AbstractListModel() {
            public int getSize() { return _tableList.length; }
            public Object getElementAt(int i) { return _tableList[i]; }
        });
        _tableJList.getAccessibleContext().
            setAccessibleName(NbBundle.getMessage(AddTableDlg.class, "TABLE_LIST_a11yName"));
        _tableJList.getAccessibleContext().
            setAccessibleDescription(NbBundle.getMessage(AddTableDlg.class, "TABLE_LIST_a11yDescription"));
        tableListLabel = new JLabel();
        tableListLabel.setText(NbBundle.getMessage(AddTableDlg.class, "TABLE_LIST_label"));
        tableListLabel.setLabelFor(_tableJList);
        _tableScrollPane.setViewportView(_tableJList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        _mainPanel.add(_tableScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        add(_mainPanel, gridBagConstraints);


    }//GEN-END:initComponents
}
