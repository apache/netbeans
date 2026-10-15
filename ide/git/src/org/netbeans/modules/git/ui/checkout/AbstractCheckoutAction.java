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
package org.netbeans.modules.git.ui.checkout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.client.GitProgressSupport.DefaultFileListener;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author ondra
 */
public abstract class AbstractCheckoutAction extends SingleRepositoryAction {
    
    public static final String PREF_KEY_RECENT_BRANCHES = "recentlySwitchedBranches"; //NOI18N
    
    protected AbstractCheckoutAction () {
        this(null);
    }

    protected AbstractCheckoutAction (String iconResource) {
        super(iconResource);
    }
    
    private static final Logger LOG = Logger.getLogger(CheckoutRevisionAction.class.getName());
    
    protected final void checkoutRevision (final File repository, AbstractCheckoutRevision checkout, String progressLabelKey, HelpCtx helpCtx) {
        if (checkout.show(helpCtx)) {
            checkoutRevision(repository, checkout.getRevision(), checkout.isCreateBranchSelected() ? checkout.getBranchName() : null,
                    NbBundle.getMessage(CheckoutRevisionAction.class, progressLabelKey));
        }
    }
    
    public final void checkoutRevision (final File repository, final String revisionToCheckout, final String newBranchName, String progressLabel) {
        GitProgressSupport supp = new GitProgressSupport() {

            private String revision;
            private final Collection<File> notifiedFiles = new HashSet<File>();

            @Override
            protected void perform () {
                RepositoryInfo info = RepositoryInfo.getInstance(repository);
                if (!canCheckout(info)) {
                    return;
                }
                
                Collection<File> seenRoots = Git.getInstance().getSeenRoots(repository);
                final Set<String> seenPaths = new HashSet<String>(GitUtils.getRelativePaths(repository, seenRoots.toArray(new File[0])));
                try {
                    final GitClient client = getClient();
                    revision = revisionToCheckout;
                    if (newBranchName != null) {
                        revision = newBranchName;
                        LOG.log(Level.FINE, "Creating branch: {0}:{1}", new Object[] { revision, revisionToCheckout }); //NOI18N
                        GitBranch branch = client.createBranch(revision, revisionToCheckout, getProgressMonitor());
                        logBranchCreation(revisionToCheckout, branch);
                    }
                    client.addNotificationListener(new FileListener() {
                        @Override
                        public void notifyFile (File file, String relativePathToRoot) {
                            if (isUnderRoots(relativePathToRoot)) {
                                notifiedFiles.add(file);
                            }
                        }

                        private boolean isUnderRoots (String relativePathToRoot) {
                            boolean underRoot = seenPaths.isEmpty() || seenPaths.contains(relativePathToRoot);
                            if (!underRoot) {
                                for (String path : seenPaths) {
                                    if (relativePathToRoot.startsWith(path + "/")) {
                                        underRoot = true;
                                        break;
                                    }
                                }
                            }
                            return underRoot;
                        }
                    });
                    client.addNotificationListener(new DefaultFileListener(new File[] { repository }));
                    GitUtils.runWithoutIndexing(new Callable<Void>() {

                        @Override
                        public Void call () throws Exception {
                            LOG.log(Level.FINE, "Checking out commit: {0}", revision); //NOI18N
                            boolean failOnConflict = true;
                            boolean cont = true;
                            // do we have conflicts, even before the checkout?
                            boolean hadConflicts = !client.getConflicts(new File[0], getProgressMonitor()).isEmpty();
                            while (cont) {
                                cont = false;
                                try {
                                    client.checkoutRevision(revision, failOnConflict, getProgressMonitor());
                                    if (!isCanceled() && isBranch(revision, client.getBranches(true, GitUtils.NULL_PROGRESS_MONITOR))) {
                                        Utils.insert(NbPreferences.forModule(AbstractCheckoutAction.class), PREF_KEY_RECENT_BRANCHES + repository.getAbsolutePath(), revision, 5);
                                    }
                                } catch (GitException.CheckoutConflictException ex) {
                                    if (LOG.isLoggable(Level.FINE)) {
                                        LOG.log(Level.FINE, "Conflicts during checkout: {0} - {1}", new Object[] { repository, Arrays.asList(ex.getConflicts()) }); //NOI18N
                                    }
                                    File[] conflicts = getFilesInConflict(ex.getConflicts());
                                    if (resolveConflicts(conflicts, failOnConflict && !hadConflicts)) { // do not allow to merge with local unresolved conflicts
                                        cont = true;
                                        failOnConflict = false;
                                    }
                                }
                            }
                            return null;
                        }
                    }, repository);
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                } finally {
                    if (!notifiedFiles.isEmpty()) {
                        setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                        Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.singletonMap(repository, notifiedFiles));
                        GitUtils.headChanged(repository);
                    }
                }
            }

            private boolean isBranch (String revision, Map<String, GitBranch> branches) {
                GitBranch b = branches.get(revision);
                return b != null && b.getName() != GitBranch.NO_BRANCH;
            }

            private void logBranchCreation (String revision, GitBranch branch) {
                OutputLogger logger = getLogger();
                if (branch != null) {
                    logger.outputLine(NbBundle.getMessage(CheckoutRevisionAction.class, "MSG_CheckoutRevisionAction.branchCreated", new Object[] { branch.getName(), revision, branch.getId() })); //NOI18N
                } else {
                    logger.outputLine(NbBundle.getMessage(CheckoutRevisionAction.class, "MSG_CheckoutRevisionAction.noBranchCreated", new Object[] { revision })); //NOI18N
                }
            }

            private boolean resolveConflicts (File[] conflicts, boolean mergeAllowed) throws GitException {
                JButton merge = new JButton();
                Mnemonics.setLocalizedText(merge, NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction.mergeButton.text")); //NOI18N
                merge.setToolTipText(NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction.mergeButton.TTtext")); //NOI18N
                JButton revert = new JButton();
                Mnemonics.setLocalizedText(revert, NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction.revertButton.text")); //NOI18N
                revert.setToolTipText(NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction.revertButton.TTtext")); //NOI18N
                JButton review = new JButton();
                Mnemonics.setLocalizedText(review, NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction.reviewButton.text")); //NOI18N
                review.setToolTipText(NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction.reviewButton.TTtext")); //NOI18N
                Object initialValue;
                Object[] buttons;
                if (mergeAllowed) {
                    initialValue = merge;
                    buttons = new Object[] { merge, revert, review, NotifyDescriptor.CANCEL_OPTION };                    
                } else {
                    initialValue = review;
                    buttons = new Object[] { revert, review, NotifyDescriptor.CANCEL_OPTION };
                }
                Object o = DialogDisplayer.getDefault().notify(new NotifyDescriptor(NbBundle.getMessage(CheckoutRevisionAction.class, "MSG_CheckoutRevisionAction.checkoutConflicts"), //NOI18N
                        NbBundle.getMessage(CheckoutRevisionAction.class, "LBL_CheckoutRevisionAction.checkoutConflicts"), //NOI18N
                        NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE, buttons, initialValue));
                if (o == merge) {
                    return true;
                } else if (o == revert) {
                    GitClient client = getClient();
                    LOG.log(Level.FINE, "Checking out paths from HEAD"); //NOI18N
                    client.checkout(conflicts, GitUtils.HEAD, true, getProgressMonitor());
                    LOG.log(Level.FINE, "Cleanup new files"); //NOI18N
                    client.clean(conflicts, getProgressMonitor());
                    LOG.log(Level.FINE, "Checking out branch: {0}, second shot", revision); //NOI18N
                    client.checkoutRevision(revision, true, getProgressMonitor());
                    notifiedFiles.addAll(Arrays.asList(conflicts));
                } else if (o == review) {
                    setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                    GitUtils.openInVersioningView(Arrays.asList(conflicts), repository, getProgressMonitor());
                }
                return false;
            }

            private File[] getFilesInConflict (String[] conflicts) {
                List<File> files = new ArrayList<File>(conflicts.length);
                for (String path : conflicts) {
                    files.add(new File(repository, path));
                }
                return files.toArray(new File[0]);
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, progressLabel);
    }

    @NbBundle.Messages(value = {"# {0} - repository state", "MSG_CheckoutRevisionAction.cannotCheckout.invalidState=Cannot checkout in the current repository state: {0}"})
    protected final boolean canCheckout (RepositoryInfo info) {
        boolean canCheckout = true;
        if (!info.getRepositoryState().canCheckout()) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.MSG_CheckoutRevisionAction_cannotCheckout_invalidState(info.getRepositoryState()), NotifyDescriptor.INFORMATION_MESSAGE));
            canCheckout = false;
        }
        return canCheckout;
    }
}
