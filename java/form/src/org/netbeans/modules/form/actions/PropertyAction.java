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
package org.netbeans.modules.form.actions;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.ResourceBundle;
import javax.swing.*;

import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.*;

/**
 * Action that invokes custom property editor for the given property.
 *
 * @author Jan Stola
 */
public class PropertyAction extends AbstractAction {
    private static final String OK_COMMAND = "OK"; // NOI18N
    private static final String CANCEL_COMMAND = "Cancel"; // NOI18N
    private static final String RESTORE_COMMAND = "Restore"; // NOI18N
    private RADProperty property;
    private Dialog dialog;

    public static PropertyAction createIfEditable(RADProperty property) {
        PropertyEditor propEd = property.getCurrentEditor();
        return propEd != null && propEd.supportsCustomEditor()
                ? new PropertyAction(property) : null;
    }

    public PropertyAction(RADProperty property) {
        this.property = property;
        String name = (String)property.getValue("actionName"); // NOI18N
        if (name == null) {
            StringBuilder sb = new StringBuilder(property.getName());
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
            name = sb.toString();
        }
        putValue(Action.NAME, name);
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        try {
            PropertyEditor propEd = property.getPropertyEditor();
            propEd.setValue(property.getValue());
            final Component custEditor = propEd.getCustomEditor();
            Object[] options = buttons();
            DialogDescriptor descriptor = new DialogDescriptor(
                custEditor,
                (String)getValue(Action.NAME),
                true,
                options,
                DialogDescriptor.CANCEL_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                new ActionListener() {
                @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            String action = e.getActionCommand();
                            if (OK_COMMAND.equals(action)) {
                                Object value;
                                if (custEditor instanceof FormCustomEditor) {
                                    value = ((FormCustomEditor)custEditor).commitChanges();
                                } else {
                                    value = property.getPropertyEditor().getValue();
                                }
                                property.setValue(value);
                            } else if (RESTORE_COMMAND.equals(action)) {
                                property.restoreDefaultValue();
                            }
                            dialog.dispose();
                        } catch (PropertyVetoException pve) {
                            NotifyDescriptor descriptor = new NotifyDescriptor.Message(pve.getLocalizedMessage());
                            DialogDisplayer.getDefault().notify(descriptor);
                        } catch (Exception ex) {
                            NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                                NbBundle.getBundle(PropertyAction.class).getString("MSG_InvalidValue")); // NOI18N
                            DialogDisplayer.getDefault().notify(descriptor);
                        }
                    }
                });
            descriptor.setClosingOptions(new Object[0]);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setVisible(true);
            dialog = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private Object[] buttons() {
        ResourceBundle bundle = NbBundle.getBundle(PropertyAction.class);
        JButton okButton = new JButton(); 
        Mnemonics.setLocalizedText(okButton, bundle.getString("CTL_OK")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_OK")); // NOI18N
        okButton.setActionCommand(OK_COMMAND);
        JButton cancelButton = new JButton();
        Mnemonics.setLocalizedText(cancelButton, bundle.getString("CTL_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_Cancel")); // NOI18N
        cancelButton.setActionCommand(CANCEL_COMMAND);
        if (property.isDefaultValue()) {
            if ("Aqua".equals(UIManager.getLookAndFeel().getID())) {
                return new Object[] { cancelButton, okButton };
            } else {
                return new Object[] {okButton, cancelButton};
            }
        } else {
            JButton restoreButton = new JButton();
            Mnemonics.setLocalizedText(restoreButton, bundle.getString("CTL_RestoreDefault")); // NOI18N
            restoreButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CTL_RestoreDefault")); // NOI18N
            restoreButton.setActionCommand(RESTORE_COMMAND);
            if ("Aqua".equals(UIManager.getLookAndFeel().getID())) {
                return new Object[] { restoreButton, cancelButton, okButton };
            } else {
                return new Object[] {okButton, restoreButton, cancelButton};
            }
        }
    }

}
