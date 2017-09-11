/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.branch;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.ui.repository.RevisionDialogController;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import static org.netbeans.modules.git.ui.branch.Bundle.*;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ondra
 */
public class CherryPick {
    private final CherryPickPanel panel;
    private final RevisionDialogController revisionPicker;
    private JButton okButton;
    private DialogDescriptor dd;
    private boolean valid;
    private final File repository;
    private final RequestProcessor.Task mergedIntoTask;
    private String revision;

    CherryPick (File repository, String initialRevision) {
        this.repository = repository;
        revisionPicker = new RevisionDialogController(repository, new File[] { repository }, initialRevision);
        revisionPicker.setMergingInto(GitUtils.HEAD);
        panel = new CherryPickPanel(revisionPicker.getPanel());
        mergedIntoTask = Utils.createTask(new MergedIntoTask());
    }

    String getRevision() {
        return revisionPicker.getRevision().getCommitId();
    }

    @NbBundle.Messages({
        "LBL_CherryPick.OKButton.text=&Apply",
        "# {0} - repository name", "LBL_CherryPick.title=Cherry Pick - {0}",
    })
    boolean showDialog () {
        okButton = new JButton(LBL_CherryPick_OKButton_text());
        org.openide.awt.Mnemonics.setLocalizedText(okButton, okButton.getText());
        dd = new DialogDescriptor(panel, Bundle.LBL_CherryPick_title(repository), true,
                new Object[] { okButton, DialogDescriptor.CANCEL_OPTION }, okButton, DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx("org.netbeans.modules.git.ui.branch.CherryPick"), null); //NOI18N
        enableRevisionPanel();
        revisionPicker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange (PropertyChangeEvent evt) {
                mergedIntoTask.cancel();
                if (evt.getPropertyName() == RevisionDialogController.PROP_VALID) {
                    boolean v = Boolean.TRUE.equals(evt.getNewValue());
                    setValid(v);
                    if (v) {
                        revision = getRevision();
                        mergedIntoTask.schedule(500);
                    }
                }
            }
        });
        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.setVisible(true);
        return okButton == dd.getValue();
    }
    
    private void enableRevisionPanel () {
        setValid(valid);
    }

    private void setValid (boolean flag) {
        this.valid = flag;
        okButton.setEnabled(flag);
        dd.setValid(flag);
        panel.lblError.setVisible(false);
    }

    @NbBundle.Messages({
        "# {0} - branch name", "CherryPickPanel.info.merged=Already part of \"{0}\"!"
    })
    private class MergedIntoTask implements Runnable, Cancellable {

        private ProgressMonitor.DefaultProgressMonitor pm;
        
        @Override
        public void run () {
            pm = new ProgressMonitor.DefaultProgressMonitor();
            final GitBranch activeBranch = RepositoryInfo.getInstance(repository).getActiveBranch();
            boolean mergedInto = false;
            final String rev = revision;
            if (activeBranch.getId().equals(rev)) {
                mergedInto = true;
            } else {
                GitClient client = null;
                try {
                    client = Git.getInstance().getClient(repository);
                    GitRevisionInfo ancestor = client.getCommonAncestor(new String[] { revision, GitUtils.HEAD }, pm);
                    if (ancestor != null && ancestor.getRevision().equals(rev)) {
                        mergedInto = true;
                    }
                } catch (GitException ex) {
                    Logger.getLogger(CherryPick.class.getName()).log(Level.FINE, null, ex);
                } finally {
                    if (client != null) {
                         client.release();
                    }
                }
            }
            final boolean merged = mergedInto;
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run () {
                    if (rev.equals(revision) && merged) {
                        panel.lblError.setText(Bundle.CherryPickPanel_info_merged(activeBranch.getName()));
                        panel.lblError.setVisible(true);
                    }
                }
            });
        }

        @Override
        public boolean cancel () {
            return pm != null && pm.cancel();
        }
        
    }
}
