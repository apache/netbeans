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

package org.netbeans.modules.git.ui.checkout;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JButton;
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
public class CheckoutPaths implements ActionListener {
    private CheckoutPathsPanel panel;
    private RevisionDialogController revisionPicker;
    private JButton okButton;
    private DialogDescriptor dd;
    private boolean valid = true;

    CheckoutPaths (File repository, File[] roots) {
        revisionPicker = new RevisionDialogController(repository, roots, GitUtils.HEAD);
        panel = new CheckoutPathsPanel(revisionPicker.getPanel());
    }

    String getRevision() {
        String revision = null;
        if (panel.cbUpdateIndex.isSelected()) {
            revision = revisionPicker.getRevision().getRevision();
        }
        return revision;
    }

    boolean show() {
        panel.cbUpdateIndex.addActionListener(this);
        okButton = new JButton(NbBundle.getMessage(CheckoutPaths.class, "LBL_CheckoutPaths.OKButton.text")); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(okButton, okButton.getText());
        dd = new DialogDescriptor(panel, NbBundle.getMessage(CheckoutPaths.class, "LBL_CheckoutPaths.title"), true,  //NOI18N
                new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(CheckoutPaths.class), null);
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

    @Override
    public void actionPerformed (ActionEvent e) {
        if (panel.cbUpdateIndex == e.getSource()) {
            enableRevisionPanel();
        }
    }

    private void enableRevisionPanel () {
        revisionPicker.setEnabled(panel.cbUpdateIndex.isSelected());
        setValid(valid);
    }

    private void setValid (boolean flag) {
        this.valid = flag;
        flag |= !panel.cbUpdateIndex.isSelected();
        okButton.setEnabled(flag);
        dd.setValid(flag);
    }
}
