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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.reset;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import org.netbeans.libs.git.GitClient.ResetType;
import org.netbeans.modules.git.ui.repository.RevisionDialogController;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class Reset implements ActionListener {
    private ResetPanel panel;
    private RevisionDialogController revisionPicker;
    private JButton okButton;
    private DialogDescriptor dd;
    private boolean valid = true;

    Reset (File repository, File[] roots) {
        revisionPicker = new RevisionDialogController(repository, roots, GitUtils.HEAD);
        panel = new ResetPanel(revisionPicker.getPanel());
    }

    String getRevision () {
        return revisionPicker.getRevision().getRevision();
    }

    boolean show () {
        panel.rbSoft.addActionListener(this);
        panel.rbMixed.addActionListener(this);
        panel.rbHard.addActionListener(this);
        
        okButton = new JButton(NbBundle.getMessage(Reset.class, "LBL_Reset.OKButton.text")); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(okButton, okButton.getText());
        dd = new DialogDescriptor(panel, NbBundle.getMessage(Reset.class, "LBL_Reset.title"), true,  //NOI18N
                new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(Reset.class), null);
        revisionPicker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                if (evt.getPropertyName() == RevisionDialogController.PROP_VALID) {
                    setRevisionValid(Boolean.TRUE.equals(evt.getNewValue()));
                }
            }
        });
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        validate();
        d.setVisible(true);
        return okButton == dd.getValue();
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        validate();
    }

    private void setRevisionValid (boolean flag) {
        this.valid = flag;
        validate();
    }

    ResetType getType () {
        String cmd = null;
        for (JRadioButton btn : new JRadioButton[] { panel.rbHard, panel.rbMixed, panel.rbSoft }) {
            if (btn.isSelected()) {
                cmd = btn.getActionCommand();
                break;
            }
        }
        return ResetType.valueOf(cmd);
    }

    private void validate () {
        boolean flag = valid && (panel.rbHard.isSelected() || panel.rbMixed.isSelected() || panel.rbSoft.isSelected());
        okButton.setEnabled(flag);
        dd.setValid(flag);
    }
}
