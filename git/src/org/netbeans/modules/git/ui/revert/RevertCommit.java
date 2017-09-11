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
package org.netbeans.modules.git.ui.revert;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.git.ui.repository.RevisionDialogController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class RevertCommit implements ActionListener, DocumentListener {
    private RevertCommitPanel panel;
    private RevisionDialogController revisionPicker;
    private JButton okButton;
    private DialogDescriptor dd;
    private boolean valid = true;

    RevertCommit (File repository, File[] roots, String initialRevision) {
        revisionPicker = new RevisionDialogController(repository, roots, initialRevision);
        panel = new RevertCommitPanel(revisionPicker.getPanel());
        attachListeners();
    }

    String getRevision () {
        return revisionPicker.getRevision().getRevision();
    }

    String getMessage () {
        return panel.txtCommitMessage.getText().trim();
    }

    boolean isCommitEnabled () {
        return panel.cbCommit.isSelected();
    }

    boolean show() {
        okButton = new JButton(NbBundle.getMessage(RevertCommit.class, "LBL_RevertCommit.OKButton.text")); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(okButton, okButton.getText());
        dd = new DialogDescriptor(panel, NbBundle.getMessage(RevertCommit.class, "LBL_RevertCommit.title"), true,  //NOI18N
                new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(RevertCommit.class), null);
        enableRevisionPanel();
        revisionPicker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                if (evt.getPropertyName() == RevisionDialogController.PROP_VALID) {
                    setValid(Boolean.TRUE.equals(evt.getNewValue()));
                }
            }
        });
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
        return okButton == dd.getValue();
    }

    private void enableRevisionPanel () {
        setValid(valid);
    }

    private void setValid (boolean flag) {
        this.valid = flag;
        okButton.setEnabled(flag);
        dd.setValid(flag);
    }

    private void attachListeners () {
        panel.cbCommit.addActionListener(this);
        panel.txtCommitMessage.getDocument().addDocumentListener(this);
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.cbCommit) {
            panel.commitMessagePanel.setVisible(panel.cbCommit.isSelected());
        }
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        messageChanged();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        messageChanged();
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
        messageChanged();
    }

    private void messageChanged () {
        panel.lblMessageWarning.setVisible(getMessage().isEmpty());
    }
}
