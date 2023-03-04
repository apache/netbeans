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
