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
            if (!mapping.isDeletion()) {
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
