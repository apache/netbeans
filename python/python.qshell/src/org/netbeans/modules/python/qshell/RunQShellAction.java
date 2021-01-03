/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.python.qshell;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import org.netbeans.modules.python.qshell.richexecution.Program;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public final class RunQShellAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {

        JButton runButton = new JButton();
        runButton.setToolTipText(NbBundle.getMessage(RunQShellAction.class, "TT_RunButtonAction"));
        QShellConfigPanel panel = new QShellConfigPanel();

        panel.command.setText(QShellConfig.getQShellCommand());
        panel.path.setText(QShellConfig.getQShellPath());

        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(RunQShellAction.class, "CTL_runDialog_Title")); // NOI18N
        dd.setModal(true);
        dd.setMessageType(DialogDescriptor.QUESTION_MESSAGE);
        Mnemonics.setLocalizedText(runButton, NbBundle.getMessage(RunQShellAction.class, "CTL_RunDialog_Run")); //NOI18N

        dd.setOptions(new Object[] {runButton, DialogDescriptor.CANCEL_OPTION}); // NOI18N
        dd.setHelpCtx(new HelpCtx("org.netbeans.modules.clearcase.ui.checkout.Uncheckout"));

        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RunQShellAction.class, "ACSD_RunDialog")); // NOI18N
        dialog.pack();
        dialog.setVisible(true);

        Object value = dd.getValue();
        if (value != runButton) return;

        QShellConfig.setQShellCommand(panel.command.getText().trim());
        QShellConfig.setQShellPath(panel.path.getText().trim());

        QShellTopComponent qtc = QShellTopComponent.findInstance();
        qtc.open();
        qtc.requestActive();

        // TODO: support multiple platforms, add to options
        Program program = new Program(panel.command.getText().trim());
        program.directory(new File(panel.path.getText().trim()));

        qtc.addTerminal(program);
    }
}
