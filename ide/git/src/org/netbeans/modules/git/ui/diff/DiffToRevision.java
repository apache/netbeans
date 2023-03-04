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
package org.netbeans.modules.git.ui.diff;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.ui.repository.Revision;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Vrabec
 */
class DiffToRevision  implements ActionListener, PropertyChangeListener {
    
    private final DiffToRevisionPanel panel;
    private final JButton okButton;
    private final JButton cancelButton;
    private final File repository;
    private final DiffToRevisionKind[] kinds;
    static final String PROP_VALID = "diffto.propValid"; //NOI18N
    
    @NbBundle.Messages({
        "CTL_DiffToRevision_okButton.text=&Diff",
        "CTL_DiffToRevision_okButton.ACSD=Diff selected revisions",
        "CTL_DiffToRevision_cancelButton.text=&Cancel",
        "CTL_DiffToRevision_cancelButton.ACSD=Cancel",
        "CTL_DiffToRevision_ACSD=Select revisions to diff"
    })
    public DiffToRevision (File repository, GitBranch currentBranch) {
        this.repository = repository;
        Revision baseRevision = new Revision.BranchReference(currentBranch);
        kinds = new DiffToRevisionKind[] {
            new DiffToRevisionKind.LocalToBaseKind(),
            new DiffToRevisionKind.LocalToRevisionKind(repository, baseRevision),
            new DiffToRevisionKind.BaseToRevisionKind(repository, baseRevision)
        };
        panel = new DiffToRevisionPanel();
        okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, Bundle.CTL_DiffToRevision_okButton_text());
        okButton.getAccessibleContext().setAccessibleDescription(Bundle.CTL_DiffToRevision_okButton_ACSD());
        cancelButton = new JButton();
        Mnemonics.setLocalizedText(cancelButton, Bundle.CTL_DiffToRevision_cancelButton_text());
        cancelButton.getAccessibleContext().setAccessibleDescription(Bundle.CTL_DiffToRevision_cancelButton_ACSD());
        okButton.setEnabled(false);
        attachListeners();
        initializeCombo();
    } 

    public boolean showDialog() {
        DialogDescriptor dialogDescriptor;
        dialogDescriptor = new DialogDescriptor(panel, Bundle.CTL_DiffToRevision_ACSD());

        dialogDescriptor.setOptions(new Object[] { okButton, cancelButton });
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.git.ui.diff.DiffToRevisionPanel")); //NOI18N
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
        if (e.getSource() == panel.cmbDiffKind) {
            DiffToRevisionKind rebaseKind = (DiffToRevisionKind) panel.cmbDiffKind.getSelectedItem();
            panel.lblDescription.setText(rebaseKind.getDescription());
            ((CardLayout) panel.panelDiffKind.getLayout()).show(panel.panelDiffKind, rebaseKind.getId());
            okButton.setEnabled(rebaseKind.isValid());
        }
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (okButton != null && evt.getSource() == panel.cmbDiffKind.getSelectedItem()
                && PROP_VALID.equals(evt.getPropertyName())) {
            boolean valid = (Boolean) evt.getNewValue();
            okButton.setEnabled(valid);
        }       
    }
    
    public Revision getSelectedTreeFirst () {
        return getSelectedKind().getTreeFirst();
    }
    
    public Revision getSelectedTreeSecond () {
        return getSelectedKind().getTreeSecond();
    }

    private void attachListeners () {
        panel.cmbDiffKind.addActionListener(this);
    }

    private void initializeCombo () {
        DefaultComboBoxModel model = new DefaultComboBoxModel(kinds);
        for (DiffToRevisionKind kind : kinds) {
            panel.panelDiffKind.add(kind.getPanel(), kind.getId());
            kind.addPropertyChangeListener(this);
        }
        panel.cmbDiffKind.setModel(model);
        panel.cmbDiffKind.setRenderer(new DiffKindRenderer());
        panel.cmbDiffKind.setSelectedIndex(1);
    }

    private DiffToRevisionKind getSelectedKind () {
        return (DiffToRevisionKind) panel.cmbDiffKind.getSelectedItem();
    }

    private static class DiffKindRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof DiffToRevisionKind) {
                value = ((DiffToRevisionKind) value).getDisplayName();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

    }

}
