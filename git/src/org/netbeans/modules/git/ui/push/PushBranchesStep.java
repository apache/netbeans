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
            String msgDeletedBranches = getDeletedBranchesMessage(localObjects.getSelectedBranches());
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
                
                for (GitTag tag : localTags.values()) {
                    String repoTagId = tags.get(tag.getTagName());
                    if (!tag.getTagId().equals(repoTagId)) {
                        l.add(new PushMapping.PushTagMapping(tag, repoTagId == null ? null : tag.getTagName()));
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
    
    public static String getDeletedBranchesMessage (List<PushMapping> selectedObjects) {
        StringBuilder sb = new StringBuilder(100);
        for (PushMapping m : selectedObjects) {
            if (m.isDeletion()) {
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
            if (m.isDeletion()) {
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
