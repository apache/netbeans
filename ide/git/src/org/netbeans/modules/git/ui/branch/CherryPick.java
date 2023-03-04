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
