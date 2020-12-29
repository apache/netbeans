/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
