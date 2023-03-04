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
package org.netbeans.modules.git.ui.rebase;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import org.netbeans.libs.git.GitBranch;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Vrabec
 */
public class Rebase implements ActionListener, PropertyChangeListener {
    
    private final RebasePanel panel;
    private final JButton okButton;
    private final JButton cancelButton;
    private final RebaseKind[] kinds;
    static final String PROP_VALID = "rebase.propValid"; //NOI18N
    
    @NbBundle.Messages({
        "CTL_RebasePanel_okButton.text=&Rebase",
        "CTL_RebasePanel_okButton.ACSD=Rebase selected commits",
        "CTL_RebasePanel_cancelButton.text=&Cancel",
        "CTL_RebasePanel_cancelButton.ACSD=Cancel rebase",
        "CTL_RebasePanel_ACSD=Select commits to rebase"
    })
    public Rebase (File repository, Map<String, GitBranch> branches, GitBranch activeBranch) {
        kinds = new RebaseKind[] {
            new RebaseKind.BasicKind(activeBranch),
            new RebaseKind.SelectDestinationKind(repository, activeBranch)
        };
        panel = new RebasePanel();
        okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, Bundle.CTL_RebasePanel_okButton_text());
        okButton.getAccessibleContext().setAccessibleDescription(Bundle.CTL_RebasePanel_okButton_ACSD());
        cancelButton = new JButton();
        Mnemonics.setLocalizedText(cancelButton, Bundle.CTL_RebasePanel_cancelButton_text());
        cancelButton.getAccessibleContext().setAccessibleDescription(Bundle.CTL_RebasePanel_cancelButton_ACSD());
        okButton.setEnabled(false);
        attachListeners();
        initializeCombo();
    } 

    public boolean showDialog() {
        DialogDescriptor dialogDescriptor;
        dialogDescriptor = new DialogDescriptor(panel, Bundle.CTL_RebasePanel_ACSD());

        dialogDescriptor.setOptions(new Object[] { okButton, cancelButton });
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.git.ui.rebase.RebasePanel")); //NOI18N
        dialogDescriptor.setValid(false);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        dialog.getAccessibleContext().setAccessibleDescription(Bundle.CTL_RebasePanel_ACSD());
        dialog.setVisible(true);
        dialog.setResizable(false);
        boolean ret = dialogDescriptor.getValue() == okButton;
        return ret;       
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.cmbRebaseType) {
            RebaseKind rebaseKind = (RebaseKind) panel.cmbRebaseType.getSelectedItem();
            panel.lblDescription.setText(rebaseKind.getDescription());
            ((CardLayout) panel.panelKind.getLayout()).show(panel.panelKind, rebaseKind.getId());
            okButton.setEnabled(rebaseKind.isValid());
        }
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (okButton != null && evt.getSource() == panel.cmbRebaseType.getSelectedItem()
                && PROP_VALID.equals(evt.getPropertyName())) {
            boolean valid = (Boolean) evt.getNewValue();
            okButton.setEnabled(valid);
        }       
    }
    
    String getRevisionBase () {
        return getSelectedKind().getUpstream();
    }
    
    String getRevisionSource () {
        return getSelectedKind().getSource();
    }
    
    String getRevisionDest () {
        return getSelectedKind().getDest();
    }

    private void attachListeners () {
        panel.cmbRebaseType.addActionListener(this);
    }

    private void initializeCombo () {
        DefaultComboBoxModel model = new DefaultComboBoxModel(kinds);
        for (RebaseKind kind : kinds) {
            panel.panelKind.add(kind.getPanel(), kind.getId());
            kind.addPropertyChangeListener(this);
        }
        panel.cmbRebaseType.setModel(model);
        panel.cmbRebaseType.setRenderer(new RebaseKindRenderer());
        panel.cmbRebaseType.setSelectedIndex(0);
    }

    private RebaseKind getSelectedKind () {
        return (RebaseKind) panel.cmbRebaseType.getSelectedItem();
    }

    private static class RebaseKindRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof RebaseKind) {
                value = ((RebaseKind) value).getDisplayName();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

    }
}
