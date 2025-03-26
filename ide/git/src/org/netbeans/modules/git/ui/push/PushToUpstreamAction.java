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

package org.netbeans.modules.git.ui.push;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.MultipleRepositoryAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import static org.netbeans.modules.git.ui.push.Bundle.*;

import org.netbeans.modules.git.ui.repository.RepositoryInfo.PushMode;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.push.PushToUpstreamAction", category = "Git")
@ActionRegistration(displayName = "#LBL_PushToUpstreamAction_Name")
@Messages("LBL_PushToUpstreamAction_Name=Pu&sh to Upstream")
public class PushToUpstreamAction extends MultipleRepositoryAction {
    
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/push.png"; //NOI18N
    
    public PushToUpstreamAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected RequestProcessor.Task performAction (File repository, File[] roots, VCSContext context) {
        return push(repository, GitUtils.getRepositoryRoots(context));
    }
    
    @NbBundle.Messages({"LBL_Push.pushToUpstreamFailed=Push to Upstream Failed",
        "LBL_PushToUpstreamAction.preparing=Preparing Push...",
        "MSG_Err.noBranchState=You are not on a branch, push cannot continue.",
        "# {0} - local branch", "MSG_Err.unknownRemoteBranchName=Cannot guess remote branch name for {0}"
    })
    Task push (final File repository, final Set<File> repositories) {
        final Task[] t = new Task[1];
        GitProgressSupport supp = new GitProgressSupport.NoOutputLogging() {
            @Override
            protected void perform () {
                RepositoryInfo info = RepositoryInfo.getInstance(repository);
                info.refresh();
                GitBranch activeBranch = info.getActiveBranch();
                if (activeBranch == null) {
                    return;
                }
                String errorLabel = LBL_Push_pushToUpstreamFailed();
                if (GitBranch.NO_BRANCH.equals(activeBranch.getName())) {
                    GitUtils.notifyError(errorLabel, MSG_Err_noBranchState());
                    return;
                }
                RepositoryInfo.PushMode pushMode = info.getPushMode();
                GitBranch trackedBranch = getTrackedBranch(activeBranch, pushMode, errorLabel);
                GitRemoteConfig cfg = getRemoteConfigForActiveBranch(trackedBranch, info, errorLabel);                        
                if (cfg == null) {
                    return;
                }
                String uri = cfg.getPushUris().isEmpty() ? cfg.getUris().get(0) : cfg.getPushUris().get(0);
                List<PushMapping> pushMappings = new LinkedList<>();
                List<String> fetchSpecs = cfg.getFetchRefSpecs();
                String remoteBranchName;
                String trackedBranchId = null;
                boolean conflicted = false;
                if (trackedBranch == null) {
                    if (shallCreateNewBranch(activeBranch)) {
                        remoteBranchName = activeBranch.getName();
                    } else {
                        return;
                    }
                } else {
                    trackedBranchId = trackedBranch.getId();
                    remoteBranchName = guessRemoteBranchName(fetchSpecs, trackedBranch.getName(), cfg.getRemoteName());
                    if (remoteBranchName == null) {
                        GitUtils.notifyError(errorLabel, MSG_Err_unknownRemoteBranchName(trackedBranch.getName()));
                        return;
                    }

                    try {
                        GitBranch remoteBranch = getClient()
                                .listRemoteBranches(uri, getProgressMonitor())
                                .get(remoteBranchName);
                        GitRevisionInfo rev = getClient().getCommonAncestor(new String[]{activeBranch.getId(), remoteBranch.getId()}, getProgressMonitor());
                        // conflict if
                        // A) rev == null : completely unrelated commits
                        // B) ancestor is neither remote branch (opposite means EQUAL or PUSH needed but not CONFLICT)
                        //    nor local head (opposite means EQUAL or pull needed but not CONFLICT)
                        conflicted = rev == null || (!remoteBranch.getId().equals(rev.getRevision()) && !activeBranch.getId().equals(rev.getRevision()));
                    } catch (GitException ex) {
                        Logger.getLogger(PushBranchesStep.class.getName()).log(Level.INFO, activeBranch.getId() + ", " + remoteBranchName, ex); //NOI18N
                    }

                    if(conflicted) {
                        if(!shallForcePush(remoteBranchName)) {
                            return;
                        }
                    }
                }
                pushMappings.add(new PushMapping.PushBranchMapping(remoteBranchName, trackedBranchId, activeBranch, conflicted, false));
                Utils.logVCSExternalRepository("GIT", uri); //NOI18N
                if (!isCanceled()) {
                    t[0] = SystemAction.get(PushAction.class).push(repository, uri, pushMappings,
                            cfg.getFetchRefSpecs(), null, repositories, true);
                }
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, LBL_PushToUpstreamAction_preparing()).waitFinished();
        return t[0];
    }
        
    private static String parseRemote (String branchName) {
        int pos = branchName.indexOf('/');
        String remoteName = null;
        if (pos > 0) {
            remoteName = branchName.substring(0, pos);
        }
        return remoteName;
    }

    protected GitBranch getTrackedBranch (GitBranch activeBranch, PushMode pushMode, String errorLabel) {
        GitBranch trackedBranch = activeBranch.getTrackedBranch();
        if (trackedBranch != null && !trackedBranch.isRemote()) {
            trackedBranch = null;
        }
        if (trackedBranch != null && pushMode == PushMode.ASK) {
            if (!("" + parseRemote(trackedBranch.getName()) + "/" + activeBranch.getName()).equals(trackedBranch.getName())) {
                trackedBranch = null;
            }
        }
        return trackedBranch;
    }

    @Messages({"# {0} - branch name", "MSG_Err.noRemoteBranch=No remote found for branch {0}",
        "MSG_Err.noRemote=No remote defined in repository configuration",
        "# {0} - remote count", "MSG_Err.moreRemotes=Cannot choose from {0} remotes",
        "# {0} - branch name", "MSG_Err.noUri=No URI specified for remote {0}",
        "# {0} - branch name", "MSG_Err.noSpecs=No fetch ref specs specified for remote {0}"})
    protected static GitRemoteConfig getRemoteConfigForActiveBranch (GitBranch trackedBranch, RepositoryInfo info, String errorLabel) {
        Map<String, GitRemoteConfig> remotes = info.getRemotes();
        GitRemoteConfig cfg;
        if (trackedBranch == null) {
            if (remotes.size() == 1) {
                cfg = remotes.values().iterator().next();
            } else if (remotes.isEmpty()) {
                GitUtils.notifyError(errorLabel, MSG_Err_noRemote());
                return null;
            } else {
                GitUtils.notifyError(errorLabel, MSG_Err_moreRemotes(remotes.size()));
                return null;
            }
        } else {
            String remoteName = parseRemote(trackedBranch.getName());
            cfg = remoteName == null ? null : remotes.get(remoteName);
            if (cfg == null) {
                GitUtils.notifyError(errorLabel, MSG_Err_noRemoteBranch(trackedBranch.getName()));
                return null;
            }
        }
        if (cfg.getPushUris().isEmpty() && cfg.getUris().isEmpty()) {
            GitUtils.notifyError(errorLabel, MSG_Err_noUri(cfg.getRemoteName()));
            return null;
        }
        if (cfg.getFetchRefSpecs().isEmpty()) {
            GitUtils.notifyError(errorLabel, MSG_Err_noSpecs(cfg.getRemoteName()));
            return null;
        }
        return cfg;
    }

    public static String guessRemoteBranchName (List<String> fetchSpecs, String branchName, String remoteName) {
        String remoteBranchName = null;
        String branchShortName = branchName.startsWith(remoteName) 
                ? branchName.substring(remoteName.length() + 1)
                : branchName.substring(branchName.indexOf('/') + 1);
        for (String spec : fetchSpecs) {
            if (spec.startsWith("+")) { //NOI18N
                spec = spec.substring(1);
            }
            int pos = spec.lastIndexOf(':');
            if (pos > 0) {
                String left = spec.substring(0, pos);
                String right = spec.substring(pos + 1);
                if (right.endsWith(GitUtils.PREFIX_R_REMOTES + branchName)
                        || right.endsWith(GitUtils.PREFIX_R_REMOTES + remoteName + "/*")) { //NOI18N
                    if (left.endsWith("/*")) { //NOI18N
                        remoteBranchName = branchShortName;
                        break;
                    } else if (left.startsWith(GitUtils.PREFIX_R_HEADS)) {
                        remoteBranchName = left.substring(GitUtils.PREFIX_R_HEADS.length());
                        break;
                    }
                }
            }
        }
        return remoteBranchName;
    }

    @NbBundle.Messages({
        "LBL_Push.createNewBranch=Create New Branch?",
        "# {0} - branch name", "MSG_Push.createNewBranch=Push is about to create a new branch \"{0}\" in the remote repository.\n"
                + "Do you want to continue and create the branch?"
    })
    private static boolean shallCreateNewBranch (GitBranch branch) {
        return NotifyDescriptor.YES_OPTION == DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                Bundle.MSG_Push_createNewBranch(branch.getName()),
                Bundle.LBL_Push_createNewBranch(),
                NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE));
    }

    @NbBundle.Messages({
        "LBL_Push.forcePush=Conflicting change",
        "# {0} - branch name",
        "MSG_Push.forcePush=There are conflicting changes in the target branch \"{0}\".\n"
                + "Do you want to abort or force push?",
        "BTN_Push.forcePush=Force push"
    })
    private static boolean shallForcePush (String branchName) {
        String push = Bundle.BTN_Push_forcePush();
        return push == DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                Bundle.MSG_Push_forcePush(branchName),
                Bundle.LBL_Push_forcePush(),
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                new Object[] {NotifyDescriptor.CANCEL_OPTION, push},
                NotifyDescriptor.CANCEL_OPTION
        ));
    }
    
}
