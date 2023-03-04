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
            prompt.setText("<html>" + promptText.replace("&", "&amp;")
                                                .replace("<", "&lt;")
                                                .replaceAll("\r?\n", "<br>"));
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
