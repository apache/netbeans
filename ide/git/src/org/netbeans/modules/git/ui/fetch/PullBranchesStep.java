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

package org.netbeans.modules.git.ui.fetch;

import java.awt.EventQueue;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.selectors.ItemSelector;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import static org.netbeans.modules.git.ui.fetch.Bundle.*;

/**
 *
 * @author ondra
 */
public class PullBranchesStep extends AbstractWizardPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor>, ChangeListener {
    private GitRemoteConfig remote;
    private final File repository;
    private final ItemSelector<BranchMapping> branches;
    private String mergingBranch;
    private GitBranch currentBranch;
    private static final String REMOTE_BRANCH_NAME_WITH_REMOTE = "{0}/{1}"; //NOI18N
    private boolean candidatesEmpty;

    public PullBranchesStep (File repository) {
        this.repository = repository;
        this.branches = new ItemSelector<BranchMapping>(NbBundle.getMessage(PullBranchesStep.class, "FetchBranchesPanel.jLabel1.text")); //NOI18N
        this.branches.addChangeListener(this);
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                validateBeforeNext();
            }
        });
        getJComponent().setName(NbBundle.getMessage(PullBranchesStep.class, "LBL_FetchBranches.remoteBranches")); //NOI18N
    }
    
    @Override
    @NbBundle.Messages({
        "# {0} - remote branch name", "# {1} - current branch name",
        "MSG_PullBranchesStep.mergingBranch=Branch <b>{0}</b> will be merged into the current branch <b>{1}</b>.",
        "# {0} - remote branch name",
        "MSG_PullBranchesStep.noCurrentBranch=No current branch. Branch <b>{0}</b> will be checked out.",
        "MSG_PullBranchesPanel.errorNoBranchSelected=No branch selected, please select branches to fetch",
        "MSG_PullBranchesPanel.warningNoBranchToMerge=No branch to merge selected. Doing a fetch instead.<br>Please note that no merge/rebase will be done.",
        "MSG_PullBranchesPanel.warningMultipleCandidatesToMerge=Cannot merge more than one branch. Doing a fetch instead.<br>Please note that no merge/rebase will be done."
    })
    protected final boolean validateBeforeNext () {
        setValid(true, null);
        if (branches.getSelectedBranches().isEmpty()) {
            setValid(false, new Message(MSG_PullBranchesPanel_errorNoBranchSelected(), true)); //NOI18N
        } else {
            StringBuilder sb;
            boolean info;
            if (mergingBranch != null) {
                info = true;
                if (currentBranch == null) {
                    sb = new StringBuilder(MSG_PullBranchesStep_noCurrentBranch(mergingBranch));
                } else {
                    sb = new StringBuilder(MSG_PullBranchesStep_mergingBranch(mergingBranch, currentBranch.getName()));
                }
            } else {
                info = false;
                if (candidatesEmpty) {
                    sb = new StringBuilder(MSG_PullBranchesPanel_warningNoBranchToMerge());
                } else {
                    sb = new StringBuilder(MSG_PullBranchesPanel_warningMultipleCandidatesToMerge());
                }
            }
            String msgDeletedBranches = FetchBranchesStep.getDeletedBranchesMessage(branches.getSelectedBranches());
            if (msgDeletedBranches != null) {
                sb.append("<br>").append(msgDeletedBranches);
            }
            setValid(true, new Message(sb.toString(), info));
        }
        return isValid();
    }

    @Override
    protected final JComponent getJComponent () {
        return branches.getPanel();
    }
    
    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.git.ui.fetch.PullBranchesStep"); //NOI18N
    }

    public void setRemote (GitRemoteConfig remote) {
        this.remote = remote;
        validateBeforeNext();
    }

    public void fillRemoteBranches (final Map<String, GitBranch> branches) {
        fillRemoteBranches(Collections.<String,GitBranch>emptyMap(), Collections.<String,GitBranch>emptyMap());
        new GitProgressSupport.NoOutputLogging() {
            @Override
            protected void perform () {
                final Map<String, GitBranch> localBranches = new HashMap<String, GitBranch>();
                RepositoryInfo info = RepositoryInfo.getInstance(repository);
                info.refresh();
                localBranches.putAll(info.getBranches());
                EventQueue.invokeLater(new Runnable () {
                    @Override
                    public void run () {
                        fillRemoteBranches(branches, localBranches);
                    }
                });
            }
        }.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(PullBranchesStep.class, "MSG_FetchBranchesPanel.loadingLocalBranches")); //NOI18N
    }

    private void fillRemoteBranches (Map<String, GitBranch> branches, Map<String, GitBranch> localBranches) {
        List<BranchMapping> l = new ArrayList<BranchMapping>(branches.size());
        Set<String> displayedBranches = new HashSet<String>(localBranches.size());
        for (GitBranch branch : localBranches.values()) {
            if (branch.isActive()) {
                currentBranch = branch;
            }
        }
        // current branch may still be null - remote branches are fetched but
        // the local repository is freshly initialized - no HEAD yet
        for (GitBranch branch : branches.values()) {
            String branchName = remote.getRemoteName() + "/" + branch.getName();
            displayedBranches.add(branchName);
            GitBranch localBranch = localBranches.get(branchName);
            boolean preselected = localBranch != null && (!localBranch.getId().equals(branch.getId()) // is a modification
                    // or is the tracked branch - should be offered primarily
                    || currentBranch != null 
                    && currentBranch.getTrackedBranch() != null 
                    && currentBranch.getTrackedBranch().getName().equals(localBranch.getName()));
            l.add(new BranchMapping(branch.getName(), branch.getId(), localBranch, remote, preselected));
        }
        for (GitBranch branch : localBranches.values()) {
            if (branch.isRemote() && !displayedBranches.contains(branch.getName())
                    && branch.getName().startsWith(remote.getRemoteName() + "/")) {
                // about to be deleted
                l.add(new BranchMapping(null, null, branch, remote, false));
            }
        }
        this.branches.setBranches(l);
        stateChanged(new ChangeEvent(this));
    }
    
    @Override
    public void stateChanged(ChangeEvent ce) {
        markMergingBranch();
        validateBeforeNext();
    }

    public String getBranchToMerge () {
        return mergingBranch;
    }

    public List<String> getSelectedRefSpecs () {
        List<String> specs = new LinkedList<String>();
        for (BranchMapping b : branches.getSelectedBranches()) {
            specs.add(b.getRefSpec());
        }
        return specs;
    }

    @Override
    public boolean isFinishPanel () {
        return true;
    }

    private void markMergingBranch () {
        mergingBranch = null;
        List<BranchMapping> candidates = new ArrayList<BranchMapping>(branches.getSelectedBranches().size());
        for (BranchMapping mapping : branches.getSelectedBranches()) {
            if (!mapping.isDestructive()) {
                candidates.add(mapping);
            }
        }
        BranchMapping toMerge = null;
        candidatesEmpty = candidates.isEmpty();
        if (candidates.size() == 1) {
            toMerge = candidates.get(0);
        } else {
            for (BranchMapping mapping : candidates) {
                GitBranch tracked = currentBranch == null ? null : currentBranch.getTrackedBranch();
                if (tracked != null && mapping.getLocalBranch() != null && tracked.getName().equals(mapping.getLocalBranch().getName())) {
                    toMerge = mapping;
                    break;
                }
            }
        }
        if (toMerge != null) {
            mergingBranch = MessageFormat.format(REMOTE_BRANCH_NAME_WITH_REMOTE, new Object[] { toMerge.getRemoteName(), toMerge.getRemoteBranchName() });
        }
    }
}
