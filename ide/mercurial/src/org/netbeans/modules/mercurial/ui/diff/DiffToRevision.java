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
package org.netbeans.modules.mercurial.ui.diff;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.ui.repository.HeadRevisionPicker;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Vrabec
 */
public class DiffToRevision  implements ActionListener {
    
    private final DiffToRevisionPanel panel;
    private final JButton okButton;
    private final JButton cancelButton;
    private final File repository;
    private final HgRevision baseRevision;
    private final Map<JRadioButton, HgRevision> selectionsFirst = new HashMap<JRadioButton, HgRevision>();
    private final Map<JRadioButton, HgRevision> selectionsSecond = new HashMap<JRadioButton, HgRevision>();
    private JRadioButton selectedOption;
    
    @NbBundle.Messages({
        "CTL_DiffToRevision_okButton.text=&Diff",
        "CTL_DiffToRevision_okButton.ACSD=Diff selected revisions",
        "CTL_DiffToRevision_cancelButton.text=&Cancel",
        "CTL_DiffToRevision_cancelButton.ACSD=Cancel",
        "CTL_DiffToRevision_ACSD=Select revisions to diff"
    })
    public DiffToRevision (File repository, HgRevision base) {
        this.repository = repository;
        this.baseRevision = base;
        panel = new DiffToRevisionPanel();
        okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, Bundle.CTL_DiffToRevision_okButton_text());
        okButton.getAccessibleContext().setAccessibleDescription(Bundle.CTL_DiffToRevision_okButton_ACSD());
        cancelButton = new JButton();
        Mnemonics.setLocalizedText(cancelButton, Bundle.CTL_DiffToRevision_cancelButton_text());
        cancelButton.getAccessibleContext().setAccessibleDescription(Bundle.CTL_DiffToRevision_cancelButton_ACSD());
        initializeSelections();
        attachListeners();
        panel.rbLocalToAny.doClick();
    } 

    public boolean showDialog() {
        DialogDescriptor dialogDescriptor;
        dialogDescriptor = new DialogDescriptor(panel, Bundle.CTL_DiffToRevision_ACSD());

        dialogDescriptor.setOptions(new Object[] { okButton, cancelButton });
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.mercurial.ui.diff.DiffToRevisionPanel")); //NOI18N
        dialogDescriptor.setValid(false);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        dialog.getAccessibleContext().setAccessibleDescription(Bundle.CTL_DiffToRevision_ACSD());
        dialog.setVisible(true);
        dialog.setResizable(false);
        boolean ret = dialogDescriptor.getValue() == okButton;
        return ret;       
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.rbLocalToBase) {
            setEnabled(panel.localToRevisionPanel, false);
            setEnabled(panel.baseToRevisionPanel, false);
            selectedOption = panel.rbLocalToBase;
        } else if (e.getSource() == panel.rbLocalToAny) {
            setEnabled(panel.localToRevisionPanel, true);
            setEnabled(panel.baseToRevisionPanel, false);
            selectedOption = panel.rbLocalToAny;
        } else if (e.getSource() == panel.rbBaseToAny) {
            setEnabled(panel.localToRevisionPanel, false);
            setEnabled(panel.baseToRevisionPanel, true);
            selectedOption = panel.rbBaseToAny;
        } else if (e.getSource() == panel.btnSelectBaseToAny) {
            HeadRevisionPicker picker = new HeadRevisionPicker(repository, null);
            if (picker.showDialog()) {
                HgLogMessage msg = picker.getSelectionRevision();
                selectionsFirst.put(panel.rbBaseToAny, msg.getHgRevision());
                panel.tfSelectedRevisionBaseToAny.setText(msg.toAnnotatedString(baseRevision.getChangesetId()));
            }
        } else if (e.getSource() == panel.btnSelectLocalToAny) {
            HeadRevisionPicker picker = new HeadRevisionPicker(repository, null);
            if (picker.showDialog()) {
                HgLogMessage msg = picker.getSelectionRevision();
                selectionsFirst.put(panel.rbLocalToAny, msg.getHgRevision());
                panel.tfSelectedRevisionLocalToAny.setText(msg.toAnnotatedString(baseRevision.getChangesetId()));
            }
        }
    }
    
    public HgRevision getSelectedTreeFirst () {
        return selectionsFirst.get(selectedOption);
    }
    
    public HgRevision getSelectedTreeSecond () {
        return selectionsSecond.get(selectedOption);
    }

    private void attachListeners () {
        panel.btnSelectBaseToAny.addActionListener(this);
        panel.btnSelectLocalToAny.addActionListener(this);
        panel.rbLocalToBase.addActionListener(this);
        panel.rbLocalToAny.addActionListener(this);
        panel.rbBaseToAny.addActionListener(this);
    }

    private void setEnabled (JPanel panel, boolean enabled) {
        for (int i = 0; i < panel.getComponentCount(); ++i) {
            panel.getComponent(i).setEnabled(enabled);
        }
    }

    private void initializeSelections () {
        selectionsFirst.put(panel.rbLocalToBase, HgRevision.BASE);
        selectionsFirst.put(panel.rbLocalToAny, baseRevision);
        selectionsFirst.put(panel.rbBaseToAny, baseRevision);
        selectionsSecond.put(panel.rbLocalToBase, HgRevision.CURRENT);
        selectionsSecond.put(panel.rbLocalToAny, HgRevision.CURRENT);
        selectionsSecond.put(panel.rbBaseToAny, HgRevision.BASE);
        panel.tfSelectedRevisionLocalToAny.setText(baseRevision.toString());
        panel.tfSelectedRevisionBaseToAny.setText(baseRevision.toString());
    }

}
