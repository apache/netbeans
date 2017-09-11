/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.subversion.ui.update;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.util.logging.Level;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.ui.search.SvnSearch;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 *
 * @author Tomas Stupka
 */
public class RevertModifications implements PropertyChangeListener {

    private RevertModificationsPanel panel;
    private JButton okButton;
    private JButton cancelButton;
    private RevertType[] types;

    public static class RevisionInterval {
        public RevisionInterval(SVNRevision revision) {
            this.startRevision = revision;
            this.endRevision = revision;
        }                
        public RevisionInterval(SVNRevision startRevision, SVNRevision endRevision) {
            this.startRevision = startRevision;
            this.endRevision = endRevision;
        }        
        SVNRevision startRevision;
        SVNRevision endRevision;
    }
    
    /** Creates a new instance of RevertModifications */
    public RevertModifications(RepositoryFile repositoryFile) {
        this (repositoryFile, null);
    }

    /** Creates a new instance of RevertModifications */
    public RevertModifications(RepositoryFile repositoryFile, String defaultRevision) {
        OneCommitRevertType ocrt = new OneCommitRevertType(repositoryFile, getPanel().oneCommitRadioButton);
        LocalRevertType lrt = new LocalRevertType(getPanel().localChangesRadioButton);
        types = new RevertType[] {
            lrt,
            ocrt,
            new MoreCommitsRevertType(repositoryFile, getPanel().moreCommitsRadioButton)
        };
        okButton = new JButton(org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_RevertForm_Action_Revert")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSD_RevertForm_Action_Revert")); // NOI18N
        cancelButton = new JButton(org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_RevertForm_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSD_RevertForm_Action_Cancel")); // NOI18N
        if (defaultRevision != null) {
            panel.oneCommitRadioButton.setSelected(true);
            panel.oneRevisionTextField.setText(defaultRevision);
            ocrt.actionPerformed(null);
        } else {
            panel.localChangesRadioButton.setSelected(true);
            lrt.actionPerformed(null);
        }
        initInputVerifiers();
    }

    private void initInputVerifiers () {
        InputVerifier iv = new InputVerifier() {
            @Override
            public boolean verify (JComponent input) {
                if (input == panel.startRevisionTextField || input == panel.endRevisionTextField || input == panel.oneRevisionTextField) {
                    JTextComponent comp = (JTextComponent) input;
                    if (comp.getText().trim().isEmpty()) {
                        comp.setText(SVNRevision.HEAD.toString());
                    }
                }
                return true;
            }
        };
        panel.startRevisionTextField.setInputVerifier(iv);
        panel.endRevisionTextField.setInputVerifier(iv);
        panel.oneRevisionTextField.setInputVerifier(iv);
    }
    
    private RevertModificationsPanel getPanel() {
        if(panel == null) {
            panel = new RevertModificationsPanel();
        }
        return panel;
    }

    public RevisionInterval getRevisionInterval() {
        for (int i = 0; i < types.length; i++) {
            if(types[i].isSelected()) {
                return types[i].getRevisionInterval();
            }
        }
        return null;
    }      

    public boolean revertNewFiles() {
        for (int i = 0; i < types.length; i++) {
            if(types[i].isSelected()) {
                return types[i].revertNewFiles();
            }
        }
        return false;
    }      

    public boolean revertRecursively () {
        for (RevertType type : types) {
            if(type.isSelected()) {
                return type.revertRecursively();
            }
        }
        return false;
    }   
    
    public boolean showDialog() {
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_RevertDialog")); // NOI18N
        dialogDescriptor.setOptions(new Object[] {okButton, cancelButton});
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSD_RevertDialog")); // NOI18N
        dialog.setVisible(true);
        dialog.setResizable(false);
        boolean ret = dialogDescriptor.getValue() == okButton;
        return ret;       
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if( evt.getPropertyName().equals(RepositoryPaths.PROP_VALID) ) {
            if(okButton != null) {
                boolean valid = ((Boolean)evt.getNewValue()).booleanValue();
                okButton.setEnabled(valid);
            }
        }        
    }

    protected void setMoreCommitsFieldsEnabled(boolean b) {
        getPanel().startRevisionTextField.setEnabled(b);
        getPanel().endRevisionTextField.setEnabled(b);
        getPanel().startSearchButton.setEnabled(b);
        getPanel().endSearchButton.setEnabled(b);
    }

    protected void setOneCommitFieldsEnabled(boolean b) {
        getPanel().oneRevisionSearchButton.setEnabled(b);
        getPanel().oneRevisionTextField.setEnabled(b);
    }

    protected final void setLocalModificationsFieldsEnabled (boolean b) {
        getPanel().cbRecursiveRevert.setEnabled(b);
        getPanel().revertNewFilesCheckBox.setEnabled(b);
    }
        
    private abstract class RevertType implements ActionListener, DocumentListener {
        private JRadioButton button;

        RevertType(JRadioButton button) {
            this.button = button;
            button.addActionListener(this);
        }

        boolean isSelected() {
            return button.isSelected();
        }

        boolean revertNewFiles() {
            return false;
        }
        
        boolean revertRecursively () {
            return true;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            validateUserInput();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            validateUserInput();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            validateUserInput();
        }

        void validateUserInput() {
            // default means nothing to do
        }

        RevisionInterval getRevisionInterval() {
            return null; // default means null
        }
        
        protected SVNRevision getRevision(RepositoryPaths path) {
            try {
                return path.getRepositoryFiles()[0].getRevision();
            } catch (NumberFormatException ex) {
                // should be already checked and
                // not happen at this place anymore
                Subversion.LOG.log(Level.INFO, null, ex);
            } catch (MalformedURLException ex) {
                // should be already checked and
                // not happen at this place anymore
                Subversion.LOG.log(Level.INFO, null, ex);
            }
            return null;
        }

        protected boolean validateRevision(SVNRevision revision) {
            boolean valid = revision == null || revision.equals(SVNRevision.HEAD) || revision.getKind() == SVNRevision.Kind.number;
            RevertModifications.this.okButton.setEnabled(valid);
            return valid;
        }
    }

    private class LocalRevertType extends RevertType {

        LocalRevertType (JRadioButton button) {
            super(button);
        }

        @Override
        RevertModifications.RevisionInterval getRevisionInterval() {
            return null;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setLocalModificationsFieldsEnabled(true);
            setOneCommitFieldsEnabled(false);
            setMoreCommitsFieldsEnabled(false);
        }

        @Override
        boolean revertRecursively () {
            return getPanel().cbRecursiveRevert.isSelected();
        }
        
        @Override
        boolean revertNewFiles() {
            return panel.revertNewFilesCheckBox.isSelected();
        }
    }

    private class OneCommitRevertType extends RevertType {

        private RepositoryPaths oneRevisionPath;

        OneCommitRevertType (RepositoryFile repositoryFile, JRadioButton button) {
            super(button);
            oneRevisionPath =
                new RepositoryPaths(
                    repositoryFile,
                    null,
                    null,
                    getPanel().oneRevisionTextField,
                    getPanel().oneRevisionSearchButton
                );
            oneRevisionPath.addPropertyChangeListener(RevertModifications.this);
            oneRevisionPath.setupBehavior(null, 0, null, SvnSearch.SEACRH_HELP_ID_REVERT);
        }

        @Override
        RevertModifications.RevisionInterval getRevisionInterval() {
            SVNRevision revision = getRevision(oneRevisionPath);
            RevisionInterval ret = new RevisionInterval(revision);
            return ret;
        }

        @Override
        void validateUserInput() {
            validateRevision(getRevision(oneRevisionPath));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setLocalModificationsFieldsEnabled(false);
            setOneCommitFieldsEnabled(true);
            setMoreCommitsFieldsEnabled(false);
            validateUserInput();
        }

    }

    private class MoreCommitsRevertType extends RevertType {

        private RepositoryPaths endPath;
        private RepositoryPaths startPath;

        MoreCommitsRevertType (RepositoryFile repositoryFile, JRadioButton button) {
            super(button);
            startPath =
                new RepositoryPaths(
                    repositoryFile,
                    null,
                    null,
                    getPanel().startRevisionTextField,
                    getPanel().startSearchButton
                );
            startPath.addPropertyChangeListener(RevertModifications.this);
            startPath.setupBehavior(null, 0, null, SvnSearch.SEACRH_HELP_ID_REVERT);

            endPath =
                new RepositoryPaths(
                    repositoryFile,
                    null,
                    null,
                    getPanel().endRevisionTextField,
                    getPanel().endSearchButton
                );
            endPath.addPropertyChangeListener(RevertModifications.this);
            endPath.setupBehavior(null, 0, null, SvnSearch.SEACRH_HELP_ID_REVERT);
        }

        @Override
        RevertModifications.RevisionInterval getRevisionInterval() {                       
            SVNRevision revision1 = getRevision(startPath);
            SVNRevision revision2 = getRevision(endPath);
            if(revision1 == null || revision2 == null) {
                return null;
            }

            return getResortedRevisionInterval(revision1, revision2);            
        }

        @Override
        void validateUserInput() {
            if(!validateRevision(getRevision(startPath))) {
                return;
            }
            if(!validateRevision(getRevision(endPath))) {
                return;
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setLocalModificationsFieldsEnabled(false);
            setMoreCommitsFieldsEnabled(true);
            setOneCommitFieldsEnabled(false);
            validateUserInput();
        }

        private RevisionInterval getResortedRevisionInterval(SVNRevision revision1, SVNRevision revision2) {
            RevisionInterval ret; 
            if(revision1.equals(SVNRevision.HEAD) && revision1.equals(SVNRevision.HEAD)) {
                ret = new RevisionInterval (revision1, revision2);
            } else if (revision1.equals(SVNRevision.HEAD)) {
                ret = new RevisionInterval (revision2, revision1);
            } else if (revision2.equals(SVNRevision.HEAD)) {
                ret = new RevisionInterval (revision1, revision2);                
            } else {
                Long r1 = Long.parseLong(revision1.toString());
                Long r2 = Long.parseLong(revision2.toString());
                if(r1.compareTo(r2) < 0) {
                    ret = new RevisionInterval (revision1, revision2);
                } else {
                    ret = new RevisionInterval (revision2, revision1);
                }
            }
            return ret;
        }
        
    }    

}
