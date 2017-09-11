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

package org.apache.tools.ant.module.bridge.impl;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.input.MultipleChoiceInputRequest;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author David Konecny, Dusan Balek, Jesse Glick
 */
final class NbInputHandler implements InputHandler {

    private JComboBox combo = null;
    private JTextField input = null;
    private final Runnable interestingOutputCallback;
    boolean secure;

    public NbInputHandler(Runnable interestingOutputCallback) {
        this.interestingOutputCallback = interestingOutputCallback;
    }

    public void handleInput(InputRequest request) throws BuildException {
        interestingOutputCallback.run();

        // #30196 - for one Ant script containing several <input> tasks there will be created
        // just one instance of the NbInputHandler. So it is necessary to cleanup the instance
        // used by the previous <input> task first.
        combo = null;
        input = null;

        JPanel panel = createPanel(request);
        DialogDescriptor dlg = new DialogDescriptor(panel,
        NbBundle.getMessage(NbInputHandler.class, "TITLE_input_handler")); //NOI18N
        do {
            DialogDisplayer.getDefault().createDialog(dlg).setVisible(true);
            if (dlg.getValue() != NotifyDescriptor.OK_OPTION) {
                throw new BuildException(NbBundle.getMessage(NbInputHandler.class, "MSG_input_aborted")); //NOI18N
            }
            String value;
            if (combo != null) {
                value = (String) combo.getSelectedItem();
            } else {
                value = input.getText();
            }
            request.setInput(value);
        } while (!request.isInputValid());
    }

    private JPanel createPanel(InputRequest request) {

        JPanel pane = new JPanel();
        pane.setLayout(new GridBagLayout());
        String promptText = request.getPrompt().trim();
        JLabel prompt = new JLabel();
        if (promptText.contains("\n")) { // #35712
            prompt.setText("<html>" + promptText.
                    replaceAll("&", "&amp;").
                    replaceAll("<", "&lt;").
                    replaceAll("\r?\n", "<br>"));
        } else {
            prompt.setText(promptText);
        }
        if (promptText.length() > 0) {
            prompt.setDisplayedMnemonic(promptText.charAt(0));
        }
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(12, 12, 11, 6);
        pane.add(prompt, gridBagConstraints);

        JComponent comp = null;
        if (request instanceof MultipleChoiceInputRequest) {
            combo = new JComboBox(((MultipleChoiceInputRequest)request).getChoices());
            if (defaultValue != null && defaultValue.length() > 0) {
                combo.setSelectedItem(getDefaultValue(request));
            }
            comp = combo;
        } else {
            if (secure) {
                input = new JPasswordField(getDefaultValue(request), 25);
            } else {
                input = new JTextField(getDefaultValue(request), 25);
            }
            comp = input;
        }

        prompt.setLabelFor(comp);

        comp.getAccessibleContext().setAccessibleDescription(
        NbBundle.getMessage(NbInputHandler.class, "ACSD_input_handler")); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(12, 6, 11, 6);
        pane.add(comp, gridBagConstraints);

        pane.getAccessibleContext().setAccessibleName(
        NbBundle.getMessage(NbInputHandler.class, "TITLE_input_handler")); // NOI18N
        pane.getAccessibleContext().setAccessibleDescription(
        NbBundle.getMessage(NbInputHandler.class, "ACSD_input_handler")); // NOI18N

        HelpCtx.setHelpIDString(pane, "org.apache.tools.ant.module.run.NBInputHandler"); // NOI18N

        return pane;
    }

    private static String defaultValue;
    static void setDefaultValue(String d) {
        defaultValue = d;
    }
    private static String getDefaultValue(InputRequest req) {
        try {
            // Ant 1.7.0+
            return (String) InputRequest.class.getMethod("getDefaultValue").invoke(req); // NOI18N
        } catch (NoSuchMethodException e) {
            // OK, Ant 1.6.5 or earlier
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return defaultValue;
    }

}
