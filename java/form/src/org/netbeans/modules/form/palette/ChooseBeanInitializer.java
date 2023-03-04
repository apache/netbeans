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
package org.netbeans.modules.form.palette;

import java.awt.Dialog;
import javax.lang.model.SourceVersion;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.modules.form.RADComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Initializer for the "Choose Bean" palette item, letting the user enter the
 * component class to use.
 *
 * @author Tomas Pavek
 */
class ChooseBeanInitializer implements PaletteItem.ComponentInitializer {

    @Override
    public boolean prepare(PaletteItem item, FileObject classPathRep) {
        ChooseBeanPanel panel = new ChooseBeanPanel();
        DialogDescriptor dd = new DialogDescriptor(panel,
                NbBundle.getMessage(ChooseBeanInitializer.class, "TITLE_Choose_Bean")); // NOI18N
        dd.setOptionType(DialogDescriptor.OK_CANCEL_OPTION);
        HelpCtx.setHelpIDString(panel, "f1_gui_choose_bean_html"); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        String className = null;
        boolean invalidInput;
        do {
            invalidInput = false;
            dialog.setVisible(true);
            if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                className = panel.getEnteredName();
                String checkName;
                if (className != null && className.endsWith(">") && className.indexOf("<") > 0) { // NOI18N
                    checkName = className.substring(0, className.indexOf("<")); // NOI18N
                } else {
                    checkName = className;
                }
                if (!SourceVersion.isName(checkName)) {
                    invalidInput = true;
                    DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(ChooseBeanInitializer.class, "MSG_InvalidClassName"), // NOI18N
                                                     NotifyDescriptor.WARNING_MESSAGE));
                } else if (!PaletteItem.checkDefaultPackage(checkName, classPathRep)) {
                    invalidInput = true;
                }
            } else {
                return false;
            }
        } while (invalidInput);
        item.setClassFromCurrentProject(className, classPathRep);
        return true;
    }

    @Override
    public void initializeComponent(RADComponent metacomp) {
    }

    private static class ChooseBeanPanel extends JPanel {
        private JTextField nameField;

        ChooseBeanPanel() {
            JLabel nameLabel = new JLabel(NbBundle.getMessage(ChooseBeanInitializer.class, "MSG_Choose_Bean")); // NOI18N
            nameField = new JTextField(25);
            GroupLayout layout = new GroupLayout(this);
            layout.setAutoCreateContainerGaps(true);
            layout.setAutoCreateGaps(true);
            setLayout(layout);
            layout.setHorizontalGroup(layout.createSequentialGroup()
                    .addComponent(nameLabel).addComponent(nameField));
            layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel).addComponent(nameField));
        }

        String getEnteredName() {
            return nameField.getText();
        }
    }
}
