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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.swing.Action;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRefUpdateResult;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.client.ProgressDelegate;
import org.netbeans.modules.git.ui.fetch.PullAction;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.git.utils.LogUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@NbBundle.Messages({
    "LBL_SyncBranchAction.progressName=Synchronizing with tracked branch",
    "# {0} - branch name", "# {1} - tracked branch name", "MSG_SyncBranchAction.result=Synchronizing branch {0} with {1}\n",
    "# {0} - tracked branch name", "MSG_SyncBranchAction.result.upToDate=Already in sync with {0}",
    "# {0} - branch name", "MSG_SyncBranchAction.result.notAttempted=Sync interrupted, likely because \"{0}\" is an unsupported reference.",
    "# {0} - branch name", "MSG_SyncBranchAction.result.notAttempted.pushNeeded=Sync not started, branch \"{0}\" requires a push.",
    "MSG_SyncBranchAction.result.rejected=Rejected - requires full merge.",
    "# {0} - branch name", "MSG_SyncBranchAction.result.ff=Branch \"{0}\" fast-forwarded to:\n"
})
public final class BranchSynchronizer {

    public void syncBranches (final File repository, final String[] branchNames, final boolean interactive) {
        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            protected void perform () {
                try {
                    new Executor(getClient(), getLogger(), repository, branchNames,
                            getProgress(), interactive).execute();
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, interactive);
                }
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.LBL_SyncBranchAction_progressName());
    }

    public void syncBranches (File repository, String[] branchNames, ProgressDelegate progress, OutputLogger logger) throws GitException {
        GitClient client = null;
        try {
            client = Git.getInstance().getClient(repository);
            new Executor(client, logger, repository, branchNames, progress, false).execute();
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }

    private static Map<String, GitBranch> getBranches (File repository) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        info.refresh();
        return info.getBranches();
    }
    
    private static class Executor {
        
        private final OutputLogger logger;
        private final GitClient client;
        private final boolean interactive;
        private final File repository;
        private final String[] branchNames;
        private final ProgressDelegate progress;

        public Executor (GitClient client, OutputLogger logger, File repository,
                String[] branchNames, ProgressDelegate progress, boolean interactive) {
            this.client = client;
            this.logger = logger;
            this.repository = repository;
            this.branchNames = branchNames;
            this.interactive = interactive;
            this.progress = progress;
        }
        
        private void execute () {
            for (String branchName : branchNames) {
                try {
                    Map<String, GitBranch> branches = getBranches(repository);
                    GitBranch branch = branches.get(branchName);
                    if (branch == null) {
                        return;
                    }
                    final GitBranch tracked = branch.getTrackedBranch();
                    if (tracked == null) {
                        return;
                    }
                    logger.outputLine(Bundle.MSG_SyncBranchAction_result(branch.getName(), tracked.getName()));
                    GitRevisionInfo ancestor = client.getCommonAncestor(new String[] { branch.getName(), tracked.getName() }, progress.getProgressMonitor());
                    if (progress.isCanceled()) {
                        return;
                    }
                    if (equal(ancestor, tracked)) {
                        if (!tracked.isRemote() || equal(ancestor, branch)) {
                            processResult(GitRefUpdateResult.NO_CHANGE, branch, tracked);
                        } else {
                            processPushNeeded(branch);
                        }
                    } else if (branch.isActive()) {
                        // active branch, need a merge or rebase
                        try {
                            GitUtils.runWithoutIndexing(new Callable<Void>() {
                                @Override
                                public Void call () throws Exception {
                                    new PullAction.BranchSynchronizer(tracked.getName(), repository, new PullAction.BranchSynchronizer.GitProgressSupportDelegate() {

                                        @Override
                                        public GitClient getClient () throws GitException {
                                            return client;
                                        }

                                        @Override
                                        public OutputLogger getLogger () {
                                            return logger;
                                        }

                                        @Override
                                        public ProgressDelegate getProgress () {
                                            return progress;
                                        }

                                    }).execute();
                                    return null;
                                }
                            });
                        } finally {
                            Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repository, Git.getInstance().getSeenRoots(repository)));
                            GitUtils.headChanged(repository);
                        }
                    } else {
                        GitRefUpdateResult res = client.updateReference(branchName, tracked.getName(), progress.getProgressMonitor());
                        processResult(res, branch, tracked);
                    }
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, interactive);
                }
            }
        }

        private void processResult (GitRefUpdateResult result, GitBranch current, GitBranch tracked) {
            GitRevisionInfo info = null;
            StringBuilder sb = new StringBuilder();
            switch (result) {
                case REJECTED:
                    sb.append(Bundle.MSG_SyncBranchAction_result_rejected());
                    final Action openAction = logger.getOpenOutputAction();
                    if (openAction != null && interactive) {
                        try {
                            EventQueue.invokeAndWait(new Runnable() {
                                @Override
                                public void run () {
                                    openAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
                                }
                            });
                        } catch (InterruptedException | InvocationTargetException ex) {
                        }
                    }
                    if (interactive) {
                        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(
                            Bundle.MSG_SyncBranchAction_result_rejected(), NotifyDescriptor.ERROR_MESSAGE));
                    }
                    break;
                case FAST_FORWARD:
                    try {
                        info = client.log(current.getName(), GitUtils.NULL_PROGRESS_MONITOR);
                        sb.append(Bundle.MSG_SyncBranchAction_result_ff(current.getName()));
                        GitUtils.printInfo(sb, info, false);
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, false);
                    }
                case NOT_ATTEMPTED:
                    sb.append(Bundle.MSG_SyncBranchAction_result_notAttempted(current.getName()));
                    break;
                case NO_CHANGE:
                    sb.append(Bundle.MSG_SyncBranchAction_result_upToDate(tracked.getName()));
                    break;
                default:
                    sb.append(result.toString());
                    break;
            }
            if (sb.length() > 0) {
                logger.outputLine(sb.toString());
            }
            if (info != null) {
                LogUtils.logBranchUpdateReview(repository, current.getName(),
                        current.getId(), info.getRevision(), logger);
            }
        }

        private boolean equal (GitRevisionInfo ancestor, GitBranch branch) {
            return ancestor != null && ancestor.getRevision().equals(branch.getId());
        }

        private void processPushNeeded (GitBranch branch) {
            logger.outputLine(Bundle.MSG_SyncBranchAction_result_notAttempted_pushNeeded(branch.getName()));
        }
    }

}
