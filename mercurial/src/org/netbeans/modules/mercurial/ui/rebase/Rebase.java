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
package org.netbeans.modules.mercurial.ui.rebase;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
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
        "CTL_RebasePanel_okButton.ACSD=Rebase selected changesets",
        "CTL_RebasePanel_cancelButton.text=&Cancel",
        "CTL_RebasePanel_cancelButton.ACSD=Cancel rebase",
        "CTL_RebasePanel_ACSD=Select changesets to rebase"
    })
    public Rebase (File repository, HgLogMessage workingCopyParent, Collection<HgLogMessage> branchHeads) {
        kinds = new RebaseKind[] {
            new RebaseKind.BasicKind(branchHeads, workingCopyParent),
            new RebaseKind.SelectDestinationKind(repository, branchHeads, workingCopyParent),
            new RebaseKind.SelectBaseKind(repository, branchHeads, workingCopyParent),
            new RebaseKind.SelectSourceKind(repository, branchHeads, workingCopyParent)
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
        dialogDescriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.mercurial.ui.rebase.RebasePanel")); //NOI18N
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
        return getSelectedKind().getBase();
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
