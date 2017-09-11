/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mercurial.ui.branch;

import java.awt.Dialog;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
class CreateBranch {
    private final CreateBranchPanel panel;

    public CreateBranch () {
        this.panel = new CreateBranchPanel();
    }

    public String getBranchName () {
        return panel.txtBranchName.getText().trim();
    }

    public boolean showDialog () {
        final JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, NbBundle.getMessage(CreateBranch.class, "CTL_CreateBranch.ok.text")); //NOI18N
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(CreateBranch.class, "LBL_CreateBranchPanel.title"), //NOI18N
                true, new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx("org.netbeans.modules.mercurial.ui.branch.CreateBranchPanel"), null); //NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        okButton.setEnabled(false);
        panel.txtBranchName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate (DocumentEvent e) {
                branchNameChanged();
            }

            @Override
            public void removeUpdate (DocumentEvent e) {
                branchNameChanged();
            }

            @Override
            public void changedUpdate (DocumentEvent e) {
            }

            private void branchNameChanged () {
                okButton.setEnabled(!panel.txtBranchName.getText().trim().isEmpty());
            }
        });
        dialog.setVisible(true);
        return dd.getValue() == okButton;
    }
    
}
