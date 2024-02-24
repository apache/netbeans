/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.git.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.conflicts.ResolveConflictsAction;
import org.netbeans.modules.git.ui.conflicts.ResolveConflictsExecutor;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class ResultProcessor {

    private static final Logger LOG = Logger.getLogger(ResultProcessor.class.getName());
    protected final GitClient client;
    protected final File repository;
    private final String revision;
    protected final ProgressMonitor pm;

    public ResultProcessor (GitClient client, File repository, String revision, ProgressMonitor pm) {
        this.client = client;
        this.repository = repository;
        this.revision = revision;
        this.pm = pm;
    }

    protected final void printConflicts (OutputLogger logger, StringBuilder sb, Collection<File> conflicts) {
        if (sb.length() > 0) {
            if (sb.charAt(sb.length() - 1) == '\n') {
                sb.delete(sb.length() - 1, sb.length());
            }
            logger.outputLine(sb.toString());
            sb.delete(0, sb.length());
        }
        for (File f : conflicts) {
            logger.outputFile(f.getAbsolutePath(), f, 0);
        }
    }

    protected final void resolveConflicts (Collection<File> conflicts) {
        JButton resolve = new JButton();
        Mnemonics.setLocalizedText(resolve, NbBundle.getMessage(ResultProcessor.class, "LBL_ResultProcessor.resolveButton.text")); //NOI18N
        resolve.setToolTipText(NbBundle.getMessage(ResultProcessor.class, "LBL_ResultProcessor.resolveButton.TTtext")); //NOI18N
        JButton review = new JButton();
        Mnemonics.setLocalizedText(review, NbBundle.getMessage(ResultProcessor.class, "LBL_ResultProcessor.reviewButton.text")); //NOI18N
        review.setToolTipText(NbBundle.getMessage(ResultProcessor.class, "LBL_ResultProcessor.reviewButton.TTtext")); //NOI18N
        Object o = DialogDisplayer.getDefault().notify(new NotifyDescriptor(NbBundle.getMessage(ResultProcessor.class, "MSG_ResultProcessor.resolveConflicts"), //NOI18N
                NbBundle.getMessage(ResultProcessor.class, "LBL_ResultProcessor.resolveConflicts"), //NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE, new Object[] { resolve, review, NotifyDescriptor.CANCEL_OPTION }, resolve));
        if (o == review) {
            openInVersioningView(conflicts);
        } else if (o == resolve) {
            GitProgressSupport supp = new ResolveConflictsExecutor(conflicts.toArray(new File[0]));
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(ResolveConflictsAction.class, "MSG_PreparingMerge")); //NOI18N
        }
    }

    public final boolean resolveLocalChanges (String[] conflicts) throws GitException {
        return resolveLocalChanges(getFilesInConflict(conflicts));
    }
        
    public final boolean resolveLocalChanges (File[] localChanges) throws GitException {
        JButton revert = new JButton();
        Mnemonics.setLocalizedText(revert, NbBundle.getMessage(ResultProcessor.class, "LBL_ResultProcessor.revertButton.text")); //NOI18N
        revert.setToolTipText(NbBundle.getMessage(ResultProcessor.class, "LBL_ResultProcessor.revertButton.TTtext")); //NOI18N
        JButton review = new JButton();
        Mnemonics.setLocalizedText(review, NbBundle.getMessage(ResultProcessor.class, "LBL_ResultProcessor.reviewButton.text")); //NOI18N
        review.setToolTipText(NbBundle.getMessage(ResultProcessor.class, "LBL_ResultProcessor.reviewButton.TTtext")); //NOI18N
        Object o = DialogDisplayer.getDefault().notify(new NotifyDescriptor(NbBundle.getMessage(ResultProcessor.class, "MSG_ResultProcessor.localModifications"), //NOI18N
                NbBundle.getMessage(ResultProcessor.class, "LBL_ResultProcessor.localModifications"), //NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE, new Object[] { revert, review, NotifyDescriptor.CANCEL_OPTION }, revert));
        if (o == revert) {
            LOG.log(Level.FINE, "Checking out paths from HEAD"); //NOI18N
            client.checkout(localChanges, GitUtils.HEAD, true, pm);
            LOG.log(Level.FINE, "Cleanup new files"); //NOI18N
            client.clean(localChanges, pm);
            LOG.log(Level.FINE, "Checking out branch: {0}, second shot", revision); //NOI18N
            return true;
        } else if (o == review) {
            openInVersioningView(Arrays.asList(localChanges));
            return false;
        } else {
            return false;
        }
    }

    private File[] getFilesInConflict (String[] conflicts) {
        List<File> files = new ArrayList<File>(conflicts.length);
        for (String path : conflicts) {
            files.add(new File(repository, path));
        }
        return files.toArray(new File[0]);
    }

    protected final void openInVersioningView (final Collection<File> files) {
        new GitProgressSupport() {
            @Override
            protected void perform () {
                GitUtils.openInVersioningView(files, repository, getProgressMonitor());
            }
        }.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
    }
}
