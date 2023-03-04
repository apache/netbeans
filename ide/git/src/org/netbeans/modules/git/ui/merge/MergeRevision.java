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

package org.netbeans.modules.git.ui.merge;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JButton;
import org.netbeans.libs.git.GitRepository.FastForwardOption;
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
public class MergeRevision {
    private final MergeRevisionPanel panel;
    private final RevisionDialogController revisionPicker;
    private JButton okButton;
    private DialogDescriptor dd;
    private boolean valid = true;
    private final FastForwardOption ffOption;

    MergeRevision (File repository, File[] roots, String initialRevision, FastForwardOption defaultFFOption) {
        ffOption = defaultFFOption;
        revisionPicker = new RevisionDialogController(repository, roots, initialRevision);
        revisionPicker.setMergingInto(GitUtils.HEAD);
        panel = new MergeRevisionPanel(revisionPicker.getPanel());
        initFFOptions();
    }

    String getRevision() {
        return revisionPicker.getRevision().getRevision();
    }

    boolean show() {
        okButton = new JButton(NbBundle.getMessage(MergeRevision.class, "LBL_MergeRevision.OKButton.text")); //NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(okButton, okButton.getText());
        dd = new DialogDescriptor(panel, NbBundle.getMessage(MergeRevision.class, "LBL_MergeRevision.title"), true,  //NOI18N
                new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx("org.netbeans.modules.git.ui.merge.MergeRevision"), null); //NOI18N
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

    FastForwardOption getFFOption () {
        if (panel.rbFFOptionOnly.isSelected()) {
            return FastForwardOption.FAST_FORWARD_ONLY;
        } else if (panel.rbFFOptionNever.isSelected()) {
            return FastForwardOption.NO_FAST_FORWARD;
        } else {
            return FastForwardOption.FAST_FORWARD;
        }
    }
    
    private void enableRevisionPanel () {
        setValid(valid);
    }

    private void setValid (boolean flag) {
        this.valid = flag;
        okButton.setEnabled(flag);
        dd.setValid(flag);
    }

    private void initFFOptions () {
        switch (ffOption) {
            case FAST_FORWARD:
                panel.rbFFOption.setSelected(true);
                break;
            case FAST_FORWARD_ONLY:
                panel.rbFFOptionOnly.setSelected(true);
                break;
            case NO_FAST_FORWARD:
                panel.rbFFOptionNever.setSelected(true);
                break;
        }
    }
}
