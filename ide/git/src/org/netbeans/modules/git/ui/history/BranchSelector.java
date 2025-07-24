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

package org.netbeans.modules.git.ui.history;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JButton;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.ui.repository.RevisionDialogController;
import org.netbeans.modules.git.ui.repository.SelectBranchPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public final class BranchSelector {
    protected final SelectBranchPanel panel;
    private final RevisionDialogController revisionPicker;
    private JButton okButton;
    private DialogDescriptor dd;

    public BranchSelector (File repository) {
        this.revisionPicker = new RevisionDialogController(repository, new File[0],
                Collections.<String, GitBranch>emptyMap(), null);
        panel = new SelectBranchPanel(revisionPicker.getPanel());
    }
    
    public BranchSelector(File repository, HashMap<String, GitBranch> branches) {
        this.revisionPicker = new RevisionDialogController(repository, new File[0], branches, null);
        panel = new SelectBranchPanel(revisionPicker.getPanel());
    }

    public String getSelectedBranch () {
        return revisionPicker.getRevision().getRevision();
    }
    
    @NbBundle.Messages({
        "BranchSelector.okButton.text=&OK",
        "BranchSelector.title=Select Branch"
    })
    public boolean open () {
        okButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(okButton, Bundle.BranchSelector_okButton_text());
        dd = new DialogDescriptor(panel, Bundle.BranchSelector_title(), true,
                new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton,
                DialogDescriptor.DEFAULT_ALIGN, null, null);
        revisionPicker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                if (evt.getPropertyName() == RevisionDialogController.PROP_VALID) {
                    setRevisionValid(Boolean.TRUE.equals(evt.getNewValue()));
                } else if (evt.getPropertyName() == RevisionDialogController.PROP_REVISION_ACCEPTED) {
                    if (dd.isValid()) {
                        okButton.doClick();
                    }
                }
            }
        });
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        okButton.setEnabled(false);
        d.setVisible(true);
        return okButton == dd.getValue();
    }
    
    private void setRevisionValid (boolean flag) {
        okButton.setEnabled(flag);
        dd.setValid(flag);
    }

}
