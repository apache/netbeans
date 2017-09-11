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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.netbeans.lib.ddl.DDLException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.*;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

public class AddIndexDialog {
    boolean result = false;
    Dialog dialog = null;
    DialogDescriptor descriptor;
    JTextField namefld;
    CheckBoxListener cbxlistener;
    JCheckBox cbx_uq;
    private static final Logger LOGGER = Logger.getLogger(AddIndexDialog.class.getName());
    
    public AddIndexDialog(Collection columns, final Specification spec, final String tablename, final String schemaName) {
        try {
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con = new GridBagConstraints ();
            pane.setLayout (layout);

            // Index name

            JLabel label = new JLabel();
            Mnemonics.setLocalizedText(label, NbBundle.getMessage (AddIndexDialog.class, "AddIndexName")); //NOI18N
            label.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (AddIndexDialog.class, "ACS_AddIndexNameA11yDesc"));
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 0;
            layout.setConstraints(label, con);
            pane.add(label);

            // Index name field

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 0;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            namefld = new JTextField(35);
            namefld.setToolTipText(NbBundle.getMessage (AddIndexDialog.class, "ACS_AddIndexNameTextFieldA11yDesc"));
            namefld.getAccessibleContext().setAccessibleName(NbBundle.getMessage (AddIndexDialog.class, "ACS_AddIndexNameTextFieldA11yName"));
            label.setLabelFor(namefld);
            layout.setConstraints(namefld, con);
            pane.add(namefld);

            // Unique/Non-unique

            JLabel label_uq = new JLabel(NbBundle.getMessage (AddIndexDialog.class, "AddUniqueIndex")); //NOI18N
            label.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (AddIndexDialog.class, "ACS_AddUniqueIndexA11yDesc"));
            con.weightx = 0.0;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 1;
            layout.setConstraints(label_uq, con);
            pane.add(label_uq);

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 1;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            cbx_uq = new JCheckBox();
            Mnemonics.setLocalizedText(cbx_uq, NbBundle.getMessage (AddIndexDialog.class, "AddIndexUnique"));
            cbx_uq.setToolTipText(NbBundle.getMessage (AddIndexDialog.class, "ACS_UniqueA11yDesc"));
            label_uq.setLabelFor(cbx_uq);
            layout.setConstraints(cbx_uq, con);
            pane.add(cbx_uq);

            // Items list title

            label = new JLabel(NbBundle.getMessage (AddIndexDialog.class, "AddIndexLabel")); //NOI18N
            label.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (AddIndexDialog.class, "ACS_AddIndexLabelA11yDesc"));
            con.weightx = 0.0;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 2;
            con.gridwidth = 2;
            layout.setConstraints(label, con);
            pane.add(label);

            // Items list

            JPanel subpane = new JPanel();
            label.setLabelFor(subpane);
            int colcount = columns.size();
            colcount = (colcount%2==0?colcount/2:colcount/2+1);
            GridLayout sublayout = new GridLayout(colcount,2);
            subpane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            subpane.setLayout(sublayout);

            cbxlistener = new CheckBoxListener(columns);
            Iterator iter = columns.iterator();
            while(iter.hasNext()) {
                String colname = (String)iter.next();
                JCheckBox cbx = new JCheckBox(colname);
                cbx.setName(colname);
                cbx.setToolTipText(colname);
                cbx.addActionListener(cbxlistener);
                subpane.add(cbx);
            }

            con.weightx = 1.0;
            con.weighty = 1.0; 
            con.gridwidth = 2;
            con.fill = GridBagConstraints.BOTH;
            con.insets = new java.awt.Insets (0, 0, 0, 0);
            con.gridx = 0;
            con.gridy = 3;
            JScrollPane spane = new JScrollPane(subpane);
            layout.setConstraints(spane, con);
            pane.add(spane);
            pane.getAccessibleContext().setAccessibleName(NbBundle.getMessage (AddIndexDialog.class, "ACS_AddIndexDialogA11yName"));  // NOI18N
            pane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (AddIndexDialog.class, "ACS_AddIndexDialogA11yDesc"));  // NOI18N
            
            ActionListener listener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    if (event.getSource() == DialogDescriptor.OK_OPTION) {
                        try {
                            result = false;
                            boolean wasException = DbUtilities.doWithProgress(null, new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    AddIndexDDL ddl = new AddIndexDDL(spec, schemaName, tablename);

                                    return ddl.execute(getIndexName(), cbx_uq.isSelected(), getSelectedColumns());
                                }
                            });

                            if (!wasException) {
                                dialog.setVisible(false);
                                dialog.dispose();
                            }
                            result = true;
                        } catch (InvocationTargetException e) {
                            Throwable cause = e.getCause();
                            if (cause instanceof DDLException) {
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                            } else {
                                LOGGER.log(Level.INFO, cause.getLocalizedMessage(), cause);
                                DbUtilities.reportError(NbBundle.getMessage (AddIndexDialog.class, "ERR_UnableToAddIndex"), e.getMessage());
                            }
                        }
                    }
                }
            };

            descriptor = new DialogDescriptor(pane, NbBundle.getMessage (AddIndexDialog.class, "AddIndexTitle"), true, listener); //NOI18N
             // Initally no column is checked => an index can't be created
            descriptor.setValid(false);
            // inbuilt close of the dialog is only after CANCEL button click
            // after OK button is dialog closed by hand
            Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
            descriptor.setClosingOptions(closingOptions);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setResizable(true);
        } catch (MissingResourceException e) {
            LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
        }
    }

    public boolean run()
    {
        if (dialog != null) dialog.setVisible(true);
        return result;
    }

    public Set getSelectedColumns()
    {
        return cbxlistener.getSelectedColumns();
    }

    public void setIndexName(String name)
    {
        namefld.setText(name);
    }

    public String getIndexName()
    {
        return namefld.getText();
    }

    class CheckBoxListener implements ActionListener
    {
        private HashSet<String> set;

        CheckBoxListener(Collection columns)
        {
            set = new HashSet<String> ();
        }

        @Override
        public void actionPerformed(ActionEvent event)
        {
            JCheckBox cbx = (JCheckBox)event.getSource();
            String name = cbx.getName();
            if (cbx.isSelected()) set.add(name);
            else set.remove(name);
            // Only allow creation of index if at least one column is selected
            if(set.size() > 0) {
                descriptor.setValid(true);
            } else {
                descriptor.setValid(false);
            }
        }

        public Set getSelectedColumns()
        {
            return set;
        }
    }
}
