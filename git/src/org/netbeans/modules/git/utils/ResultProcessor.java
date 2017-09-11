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
            GitProgressSupport supp = new ResolveConflictsExecutor(conflicts.toArray(new File[conflicts.size()]));
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
        return files.toArray(new File[files.size()]);
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
