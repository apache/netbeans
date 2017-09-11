/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
