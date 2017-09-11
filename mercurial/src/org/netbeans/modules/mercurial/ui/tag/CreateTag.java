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
package org.netbeans.modules.mercurial.ui.tag;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.repository.ChangesetPickerPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
class CreateTag implements ActionListener {
    private final CreateTagPanel panel;
    private static final HgLogMessage PARENT_REVISION = new HgLogMessage(null, Collections.<String>emptyList(), null, 
            null, null, null, Long.toString(new Date().getTime()), NbBundle.getMessage(CreateTag.class, "MSG_Revision_Parent"), //NOI18N
            null, null, null, null, null, "", ""); //NOI18N
    private HgLogMessage selectedRevision;
    private final File repository;
    private ChangesetPickerSimplePanel changesetPickerPanel;

    public CreateTag (File repository) {
        this.panel = new CreateTagPanel();
        this.repository = repository;
        setSelectedRevision(null);
        attachListeners();
    }

    boolean showDialog () {
        final JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, NbBundle.getMessage(CreateTag.class, "CTL_CreateTag.ok.text")); //NOI18N
        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(CreateTag.class, "LBL_CreateTagPanel.title"), //NOI18N
                true, new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx("org.netbeans.modules.mercurial.ui.tag.CreateTagPanel"), null); //NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        okButton.setEnabled(false);
        panel.txtTagName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate (DocumentEvent e) {
                tagNameChanged();
            }

            @Override
            public void removeUpdate (DocumentEvent e) {
                tagNameChanged();
            }

            @Override
            public void changedUpdate (DocumentEvent e) {
            }

            private void tagNameChanged () {
                okButton.setEnabled(!panel.txtTagName.getText().trim().isEmpty());
            }
        });
        dialog.setVisible(true);
        return dd.getValue() == okButton;
    }

    String getTagName () {
        return panel.txtTagName.getText().trim();
    }

    String getMessage () {
        return panel.txtMessage.getText().trim();
    }

    String getRevision () {
        return selectedRevision.getRevisionNumber();
    }

    boolean isLocalTag () {
        return panel.cbLocal.isSelected();
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.cbLocal) {
            panel.txtMessage.setEnabled(!panel.cbLocal.isSelected());
        } else if (e.getSource() == panel.btnSelectRevision) {
            if (changesetPickerPanel == null) {
                changesetPickerPanel = new ChangesetPickerSimplePanel(repository);
            }
            
            if (changesetPickerPanel.selectRevision()) {
                HgLogMessage revisionWithChangeset = changesetPickerPanel.getSelectedRevision();
                setSelectedRevision(revisionWithChangeset);
            }
        }
    }

    private void setSelectedRevision (HgLogMessage message) {
        selectedRevision = message == null ? PARENT_REVISION : message;
        message = selectedRevision;
        String value;
        if (message == PARENT_REVISION) {
            value = message.getCSetShortID();
        } else {
            StringBuilder sb = new StringBuilder().append(message.getRevisionNumber());
            StringBuilder labels = new StringBuilder();
            for (String branch : message.getBranches()) {
                labels.append(branch).append(' ');
            }
            for (String tag : message.getTags()) {
                labels.append(tag).append(' ');
                break; // just one tag
            }
            sb.append(" (").append(labels).append(labels.length() == 0 ? "" : "- ").append(message.getCSetShortID().substring(0, 7)).append(")"); //NOI18N
            value = sb.toString();
        }
        panel.txtRevision.setText(value);
        panel.txtRevision.setCaretPosition(0);
    }

    private void attachListeners () {
        panel.cbLocal.addActionListener(this);
        panel.btnSelectRevision.addActionListener(this);
    }

    private static class ChangesetPickerSimplePanel extends ChangesetPickerPanel implements PropertyChangeListener {

        private boolean initialized;
        private JButton selectButton;

        public ChangesetPickerSimplePanel(File repository) {
            super(repository, null);
            initComponents();
        }

        @Override
        protected String getRefreshLabel() {
            return NbBundle.getMessage(CreateTag.class, "MSG_Fetching_Revisions"); //NOI18N
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (selectButton != null && ChangesetPickerSimplePanel.PROP_VALID.equals(evt.getPropertyName())) {
                boolean valid = (Boolean) evt.getNewValue();
                selectButton.setEnabled(valid);
            }
        }

        private void initRevisions() {
            if (!initialized) {
                initialized = true;
                loadRevisions();
            }
        }

        private void initComponents() {
            org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CreateTag.class, "CreateTag.ChangesetPicker.jLabel1.text")); // NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CreateTag.class, "CreateTag.ChangesetPicker.jLabel2.text")); // NOI18N
        }

        private boolean selectRevision () {
            if (selectButton == null) {
                selectButton = new JButton();
            }
            org.openide.awt.Mnemonics.setLocalizedText(selectButton, org.openide.util.NbBundle.getMessage(CreateTag.class, "CTL_CreateTag.ChangesetPicker_SelectButton")); //NOI18N
            DialogDescriptor dd = new DialogDescriptor(this,
                    org.openide.util.NbBundle.getMessage(CreateTag.class, "CTL_CreateTag.ChangesetPicker_Title"), // NOI18N
                    true,
                    new Object[] { selectButton, DialogDescriptor.CANCEL_OPTION},
                    selectButton,
                    DialogDescriptor.DEFAULT_ALIGN,
                    new HelpCtx(this.getClass()),
                    null);
            selectButton.setEnabled(getSelectedRevision() != null);
            addPropertyChangeListener(this);
            initRevisions();
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialog.setVisible(true);
            removePropertyChangeListener(this);
            return dd.getValue() == selectButton;
        }
    }
}
