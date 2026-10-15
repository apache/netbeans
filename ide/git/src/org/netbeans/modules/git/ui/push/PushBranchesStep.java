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

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitTag;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.selectors.ItemSelector;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class PushBranchesStep extends AbstractWizardPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor>, ChangeListener {
    private final File repository;
    private final ItemSelector<PushMapping> localObjects;
    private boolean lastPanel;

    public PushBranchesStep (File repository) {
        this.repository = repository;
        this.localObjects = new ItemSelector<PushMapping>(NbBundle.getMessage(PushBranchesStep.class, "PushBranchesPanel.jLabel1.text")); //NOI18N
        this.localObjects.addChangeListener(this);
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                validateBeforeNext();
            }
        });
        getJComponent().setName(NbBundle.getMessage(PushBranchesStep.class, "LBL_PushBranches.localBranches")); //NOI18N
    }
    
    @Override
    protected final boolean validateBeforeNext () {
        setValid(true, null);
        if (localObjects.getSelectedBranches().isEmpty()) {
            setValid(false, new Message(NbBundle.getMessage(PushBranchesStep.class, "MSG_PushBranchesPanel.errorNoBranchSelected"), false)); //NOI18N
        } else if (isDeleteUpdateConflict(localObjects.getSelectedBranches())) {
            setValid(false, new Message(NbBundle.getMessage(PushBranchesStep.class, "MSG_PushBranchesPanel.errorMixedSeletion"), false)); //NOI18N
        } else {
            String msgDeletedBranches = getDestructiveActionMessage(localObjects.getSelectedBranches());
            if (msgDeletedBranches != null) {
                setValid(true, new Message(msgDeletedBranches, true));
            }
        }
        return isValid();
    }

    @Override
    protected final JComponent getJComponent () {
        return localObjects.getPanel();
    }
    
    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(PushBranchesStep.class);
    }

    /**
     *
     * @param cfg configuration of the remote repository including URLs of remote
     * @param branches list of all branches in the remote repo
     * @param tags list of all tags in the remote repo
     */
    public void fillRemoteBranches (final GitRemoteConfig cfg, final Map<String, GitBranch> branches,
            final Map<String, String> tags) {
        fillLocalObjects(Collections.<PushMapping>emptyList());
        new GitProgressSupport.NoOutputLogging() {
            @Override
            protected void perform () {
                final Map<String, GitBranch> localBranches = new HashMap<String, GitBranch>();
                final Map<String, GitTag> localTags = new HashMap<String, GitTag>();
                RepositoryInfo info = RepositoryInfo.getInstance(repository);
                info.refresh();
                localBranches.putAll(info.getBranches());
                localTags.putAll(info.getTags());
                
                final List<PushMapping> l = new ArrayList<PushMapping>(branches.size());
                GitClient client;
                try {
                    client = getClient();
                } catch (GitException ex) {
                    client = null;
                }
                for (GitBranch branch : localBranches.values()) {
                    if (branch.getName() == GitBranch.NO_BRANCH) {
                        // unnamed branch cannot be pushed
                        continue;
                    }
                    if (!branch.isRemote()) {
                        //get the remote branch that corresponds to local branch
                        GitBranch remoteBranch = branches.get(branch.getName());
                        boolean conflicted = false;
                        boolean updateNeeded = remoteBranch != null && !remoteBranch.getId().equals(branch.getId());
                        if (client != null && remoteBranch != null) {
                            String idLocal = branch.getId();
                            String idRemote = remoteBranch.getId();
                            if (!idLocal.equals(idRemote)) {
                                try {
                                    GitRevisionInfo rev = client.getCommonAncestor(new String[] { idLocal, idRemote } , getProgressMonitor());
                                    // conflict if
                                    // A) rev == null : completely unrelated commits
                                    // B) ancestor is neither remote branch (opposite means EQUAL or PUSH needed but not CONFLICT)
                                    //    nor local head (opposite means EQUAL or pull needed but not CONFLICT)
                                    conflicted = rev == null || (!idRemote.equals(rev.getRevision()) && !idLocal.equals(rev.getRevision()));
                                    if (!conflicted && idLocal.equals(rev.getRevision())) {
                                        // clear updateneeded flag because there are just unfetched/unmerged upstream commits
                                        updateNeeded = false;
                                    }
                                } catch (GitException.MissingObjectException ex) {
                                    if (idRemote.equals(ex.getObjectName())) {
                                        conflicted = true;
                                    } else {
                                        Logger.getLogger(PushBranchesStep.class.getName()).log(Level.INFO, idLocal + ", " + idRemote, ex); //NOI18N
                                    }
                                } catch (GitException ex) {
                                    Logger.getLogger(PushBranchesStep.class.getName()).log(Level.INFO, idLocal + ", " + idRemote, ex); //NOI18N
                                }
                            }
                        }
                        boolean preselected = !conflicted && updateNeeded;

                        //add current branch in the list for update or for adding
                        l.add(new PushMapping.PushBranchMapping(remoteBranch == null ? null : remoteBranch.getName(),
                                remoteBranch == null ? null : remoteBranch.getId(),
                                branch, conflicted, preselected, updateNeeded));
                    }
                }
                if (cfg != null) {
                    // deletions
                    for (GitBranch branch : branches.values()) {
                        String branchName = cfg.getRemoteName() + "/" + branch.getName();
                        GitBranch local = localBranches.get(branchName);
                        if (local == null || !local.isRemote()) {
                            // mirror deleted or simply not present locally => offer to delete in the remote repo
                            l.add(new PushMapping.PushBranchMapping(branch.getName(), branch.getId(), false));
                        }
                    }
                }

                //adding a new tag
                for (GitTag tag : localTags.values()) {
                    String repoTagId = tags.get(tag.getTagName());
                    if (!tag.getTagId().equals(repoTagId)) {
                        //in the remote there is no such tag, need to add it.
                        l.add(new PushMapping.PushTagMapping(tag, repoTagId == null ? null : tag.getTagName()));
                    }
                }

                //deletion of a tag
                for (String tag : tags.keySet()) {
                    //get the name of a corresponding local tag.
                    GitTag localTag = localTags.get(tag);
                    if (localTag == null) {
                        //in the local repo no such tag. Probably we need to delete in the remote?
                        l.add(new PushMapping.PushTagMapping(tag));
                    }
                }

                EventQueue.invokeLater(new Runnable () {
                    @Override
                    public void run () {
                        fillLocalObjects(l);
                    }
                });
            }
        }.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(PushBranchesStep.class, "MSG_PushBranchesPanel.loadingLocalBranches")); //NOI18N
    }

    private void fillLocalObjects (List<PushMapping> mappings) {
        this.localObjects.setBranches(mappings);
        validateBeforeNext();
    }
    
    @Override
    public void stateChanged(ChangeEvent ce) {
        validateBeforeNext();
    }

    @Override
    public boolean isFinishPanel () {
        return lastPanel;
    }

    void setAsLastPanel (boolean isLastPanel) {
        this.lastPanel = isLastPanel;
    }

    Collection<PushMapping> getSelectedMappings () {
        return localObjects.getSelectedBranches();
    }
    
    private static String getDestructiveActionMessage (List<PushMapping> selectedObjects) {
        StringBuilder sb = new StringBuilder(100);
        for (PushMapping m : selectedObjects) {
            if (m.isDestructive()) {
                sb.append(m.getInfoMessage()).append("<br>");
            }
        }
        if (sb.length() == 0) {
            return null;
        } else {
            sb.delete(sb.length() - 4, sb.length());
            return sb.toString();
        }
    }

    private boolean isDeleteUpdateConflict (List<PushMapping> selectedObjects) {
        Set<String> toDelete = new HashSet<String>(selectedObjects.size());
        Set<String> toUpdate = new HashSet<String>(selectedObjects.size());
        for (PushMapping m : selectedObjects) {
            if (m.isDestructive() && m.getLocalName() != null) {
                toDelete.add(m.getRemoteName());
            } else {
                toUpdate.add(m.getRemoteName());
            }
        }
        // is there an intersection between the sets?
        toDelete.retainAll(toUpdate);
        return !toDelete.isEmpty();
    }
}
