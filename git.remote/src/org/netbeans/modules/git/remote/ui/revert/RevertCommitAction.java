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
package org.netbeans.modules.git.remote.ui.revert;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRevertResult;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.actions.GitAction;
import org.netbeans.modules.git.remote.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.remote.ui.output.OutputLogger;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.git.remote.utils.ResultProcessor;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.revert.RevertCommitAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_RevertCommitAction_Name")
@NbBundle.Messages({
    "LBL_RevertCommitAction_Name=Re&vert Commit..."
})
public class RevertCommitAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(RevertCommitAction.class.getName());

    @Override
    protected void performAction (VCSFileProxy repository, VCSFileProxy[] roots, VCSContext context) {
        revert(repository, roots, GitUtils.HEAD);
    }

    public void revert (final VCSFileProxy repository, VCSFileProxy[] roots, String preselectedRevision) {
        final RevertCommit revert = new RevertCommit(repository, roots, preselectedRevision);
        if (revert.show()) {
            new GitProgressSupport() {
                @Override
                protected void perform () {
                    try {
                        GitUtils.runWithoutIndexing(new Callable<Void>() {
                            @Override
                            public Void call () throws Exception {
                                GitClient client = getClient();
                                client.addNotificationListener(new DefaultFileListener(new VCSFileProxy[] { repository }));
                                String revision = revert.getRevision();
                                LOG.log(Level.FINE, "Reverting revision {0}", revision); //NOI18N
                                boolean cont;
                                RevertResultProcessor mrp = new RevertResultProcessor(client, repository, revision, getLogger(), getProgressMonitor());
                                do {
                                    cont = false;
                                    try {
                                        GitRevertResult result = client.revert(revision, revert.getMessage(), revert.isCommitEnabled(), getProgressMonitor());
                                        mrp.processResult(result);
                                        GitUtils.headChanged(repository);
                                    } catch (GitException.CheckoutConflictException ex) {
                                        if (LOG.isLoggable(Level.FINE)) {
                                            LOG.log(Level.FINE, "Local modifications in WT during revert: {0} - {1}", new Object[] { repository, Arrays.asList(ex.getConflicts()) }); //NOI18N
                                        }
                                        cont = mrp.resolveLocalChanges(ex.getConflicts());
                                    }
                                } while (cont);
                                return null;
                            }
                        }, repository);
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    } finally {
                        setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                        Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.<VCSFileProxy, Collection<VCSFileProxy>>singletonMap(repository, Git.getInstance().getSeenRoots(repository)));
                    }
                }
            }.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(RevertCommitAction.class, "LBL_RevertCommitAction.progress")); //NOI18N
        }
    }
    
    public static class RevertResultProcessor extends ResultProcessor {

        private final OutputLogger logger;
        private final String revision;
        
        public RevertResultProcessor (GitClient client, VCSFileProxy repository, String revision, OutputLogger logger, ProgressMonitor pm) {
            super(client, repository, revision, pm);
            this.revision = revision;
            this.logger = logger;
        }

        public void processResult (GitRevertResult result) {
            StringBuilder sb = new StringBuilder(NbBundle.getMessage(RevertCommit.class, "MSG_RevertCommitAction.result", result.getStatus().toString())); //NOI18N
            GitRevisionInfo info = result.getNewHead();
            switch (result.getStatus()) {
                case NO_CHANGE:
                    sb.append(NbBundle.getMessage(RevertCommit.class, "MSG_RevertCommitAction.result.noChange", revision)); //NOI18N
                    break;
                case REVERTED_IN_INDEX:
                    sb.append(NbBundle.getMessage(RevertCommit.class, "MSG_RevertCommitAction.result.revertedIndex", revision)); //NOI18N
                    break;
                case REVERTED:
                    sb.append(NbBundle.getMessage(RevertCommit.class, "MSG_RevertCommitAction.result.reverted", revision)); //NOI18N
                    GitUtils.printInfo(sb, info);
                    break;
                case CONFLICTING:
                    sb.append(NbBundle.getMessage(RevertCommit.class, "MSG_RevertCommitAction.result.conflict", revision)); //NOI18N
                    printConflicts(logger, sb, result.getConflicts());
                    resolveConflicts(result.getConflicts());
                    break;
                case FAILED:
                    final Action openAction = logger.getOpenOutputAction();
                    if (openAction != null) {
                        try {
                            EventQueue.invokeAndWait(new Runnable() {
                                @Override
                                public void run () {
                                    openAction.actionPerformed(new ActionEvent(RevertResultProcessor.this, ActionEvent.ACTION_PERFORMED, null));
                                }
                            });
                        } catch (InterruptedException ex) {
                        } catch (InvocationTargetException ex) {
                        }
                    }
                    sb.append(NbBundle.getMessage(RevertCommit.class, "MSG_RevertCommitAction.result.failedFiles", revision)); //NOI18N
                    printConflicts(logger, sb, result.getFailures());
                    DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(
                            NbBundle.getMessage(RevertCommit.class, "MSG_RevertCommitAction.result.failed", revision), NotifyDescriptor.ERROR_MESSAGE)); //NOI18N
                    break;
            }
            if (sb.length() > 0) {
                logger.outputLine(sb.toString());
            }
        }
    }
}
