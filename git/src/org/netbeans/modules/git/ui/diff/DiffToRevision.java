/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
