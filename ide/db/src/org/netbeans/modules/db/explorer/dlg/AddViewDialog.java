/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.*;
import org.netbeans.modules.db.explorer.*;
import org.openide.NotificationLineSupport;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;

public class AddViewDialog {

    private static final Logger LOGGER = Logger.getLogger(AddIndexDialog.class.getName());

    Dialog dialog = null;
    JTextField namefld;
    JTextArea tarea;
    private DialogDescriptor descriptor = null;
    private NotificationLineSupport statusLine;

    public AddViewDialog(final Specification spec, final String schemaName) {
        try {
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con = new GridBagConstraints ();
            pane.setLayout (layout);

            // Index name

            JLabel label = new JLabel();
            Mnemonics.setLocalizedText(label, NbBundle.getMessage (AddViewDialog.class, "AddViewName"));
            label.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (AddViewDialog.class, "ACS_AddViewNameA11yDesc"));
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
            namefld.setToolTipText(NbBundle.getMessage (AddViewDialog.class, "ACS_AddViewNameTextFieldA11yDesc"));
            namefld.getAccessibleContext().setAccessibleName(NbBundle.getMessage (AddViewDialog.class, "ACS_AddViewNameTextFieldA11yName"));
            label.setLabelFor(namefld);
            layout.setConstraints(namefld, con);
            pane.add(namefld);
            DocumentListener docListener = new DocumentListener() {

                @Override
                public void insertUpdate(DocumentEvent e) {
                    validate();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    validate();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    validate();
                }
            };
            namefld.getDocument().addDocumentListener(docListener);

            // Items list title

            label = new JLabel();
            Mnemonics.setLocalizedText(label, NbBundle.getMessage (AddViewDialog.class, "AddViewLabel"));
            label.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (AddViewDialog.class, "ACS_AddViewLabelA11yDesc"));
            con.weightx = 0.0;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 1;
            con.gridwidth = 2;
            layout.setConstraints(label, con);
            pane.add(label);

            // Editor list

            tarea = new JTextArea(5,50);
            tarea.setToolTipText(NbBundle.getMessage (AddViewDialog.class, "ACS_AddViewTextAreaA11yDesc"));
            tarea.getAccessibleContext().setAccessibleName(NbBundle.getMessage (AddViewDialog.class, "ACS_AddViewTextAreaA11yName"));
            label.setLabelFor(tarea);
            tarea.getDocument().addDocumentListener(docListener);

            con.weightx = 1.0;
            con.weighty = 1.0;
            con.gridwidth = 2;
            con.fill = GridBagConstraints.BOTH;
            con.insets = new java.awt.Insets (0, 0, 0, 0);
            con.gridx = 0;
            con.gridy = 2;
            JScrollPane spane = new JScrollPane(tarea);
            layout.setConstraints(spane, con);
            pane.add(spane);

            ActionListener listener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    
                    if (event.getSource() == DialogDescriptor.OK_OPTION) {
                        
                        try {
                            boolean wasException = DbUtilities.doWithProgress(null, new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    return AddViewDDL.addView(spec, 
                                            schemaName,
                                            getViewName(), getViewCode());
                                }
                            });

                            if (!wasException) {
                                dialog.setVisible(false);
                                dialog.dispose();
                            }
                        } catch (InvocationTargetException e) {
                            Throwable cause = e.getCause();
                            if (cause instanceof DDLException) {
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                            } else {
                                LOGGER.log(Level.INFO, cause.getLocalizedMessage(), cause);
                                DbUtilities.reportError(NbBundle.getMessage (AddViewDialog.class, "ERR_UnableToCreateView"), e.getMessage());
                            }
                        }
                    }
                }
            };

            pane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (AddViewDialog.class, "ACS_AddViewDialogA11yDesc")); //NOI18N

            descriptor = new DialogDescriptor(pane, NbBundle.getMessage (AddViewDialog.class, "AddViewTitle"), true, listener); //NOI18N
            descriptor.setHelpCtx(new HelpCtx("createviews")); // NOI18N
            statusLine = descriptor.createNotificationLineSupport();
            // inbuilt close of the dialog is only after CANCEL button click
            // after OK button is dialog closed by hand
            Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
            descriptor.setClosingOptions(closingOptions);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setResizable(true);
            validate();
        } catch (MissingResourceException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
    }

    public String getViewName()
    {
        return namefld.getText();
    }

    public String getViewCode()
    {
        return tarea.getText();
    }

    /** Validate and update state of UI. */
    private void validate() {
        assert statusLine != null : "Notification status line not available";  //NOI18N

        String message = null;

        String viewName = getViewName();
        if (viewName == null || viewName.length() == 0) {
            message = NbBundle.getMessage(AddViewDialog.class, "AddViewMissingViewName");
        } else if (getViewCode() == null || getViewCode().length() == 0) {
            message = NbBundle.getMessage(CreateTableDialog.class, "AddViewMissingViewCode");
        }

        if (message == null) {
            statusLine.clearMessages();
            descriptor.setValid(true);
        } else {
            statusLine.setInformationMessage(message);
            descriptor.setValid(false);
        }
    }

    /**
     *  Shows Create View dialog and creates a new view in specified schema.
     * @param spec DB specification
     * @param schema DB schema to create table in
     * @return true if new view successfully created, false if cancelled
     */
    public static boolean showDialogAndCreate(final Specification spec, final String schema) {
        final AddViewDialog panel = new AddViewDialog(spec, schema);
        panel.dialog.setVisible(true);
        if (panel.descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            return true;
        }
        return false;
    }
}
