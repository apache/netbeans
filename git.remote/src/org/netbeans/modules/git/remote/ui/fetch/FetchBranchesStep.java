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

package org.netbeans.modules.git.remote.ui.fetch;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRemoteConfig;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.remote.ui.selectors.ItemSelector;
import org.netbeans.modules.git.remote.ui.wizards.AbstractWizardPanel;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 */
public class FetchBranchesStep extends AbstractWizardPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor>, ChangeListener {
    private String fetchUri;
    private GitRemoteConfig remote;
    private GitProgressSupport supp;
    private GitProgressSupport validatingSupp;
    private final Mode mode;
    private final VCSFileProxy repository;
    private final ItemSelector<BranchMapping> branches;

    public static enum Mode {
        ACCEPT_EMPTY_SELECTION,
        ACCEPT_NON_EMPTY_SELECTION_ONLY
    }

    public FetchBranchesStep (VCSFileProxy repository, Mode mode) {
        this.mode = mode;
        this.repository = repository;
        this.branches = new ItemSelector<>(NbBundle.getMessage(FetchBranchesStep.class,"FetchBranchesPanel.jLabel1.text"));
        this.branches.addChangeListener(this);
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                validateBeforeNext();
            }
        });
        getJComponent().setName(NbBundle.getMessage(FetchBranchesStep.class, "LBL_FetchBranches.remoteBranches")); //NOI18N
    }
    
    @Override
    protected final boolean validateBeforeNext () {
        setValid(true, null);
        boolean acceptEmptySelection = mode == Mode.ACCEPT_EMPTY_SELECTION;
        if (!acceptEmptySelection && branches.getSelectedBranches().isEmpty()) {
            setValid(false, new Message(NbBundle.getMessage(FetchBranchesStep.class, "MSG_FetchBranchesPanel.errorNoBranchSelected"), true)); //NOI18N
        } else if (acceptEmptySelection && branches.isEmpty()) {
            setValid(true, new Message(NbBundle.getMessage(FetchBranchesStep.class, "MSG_FetchBranchesPanel.errorNoBranch"), true)); //NOI18N
        } else {
            String msgDeletedBranches = getDeletedBranchesMessage(branches.getSelectedBranches());
            if (msgDeletedBranches == null) {
                setValid(true, null);
            } else {
                setValid(true, new Message(msgDeletedBranches, true));
            }
        }
        return isValid();
    }

    @Override
    protected final JComponent getJComponent () {
        return branches.getPanel();
    }
    
    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(FetchBranchesStep.class);
    }

    public void setFetchUri (String fetchUri, boolean loadRemoteBranches) {
        if (fetchUri != null && !fetchUri.equals(this.fetchUri) || fetchUri == null && this.fetchUri != null) {
            this.fetchUri = fetchUri;
            if (loadRemoteBranches) {
                refreshRemoteBranches();
            }
        }
    }

    public void setRemote (GitRemoteConfig remote) {
        this.remote = remote;
        validateBeforeNext();
    }

    public void fillRemoteBranches (final Map<String, GitBranch> branches) {
        if (repository == null) {
            fillRemoteBranches(branches, Collections.<String, GitBranch>emptyMap());
        } else {
            fillRemoteBranches(Collections.<String,GitBranch>emptyMap(), Collections.<String,GitBranch>emptyMap());
            new GitProgressSupport.NoOutputLogging() {
                @Override
                protected void perform () {
                    final Map<String, GitBranch> localBranches = new HashMap<>();
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
            }.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(FetchBranchesStep.class, "MSG_FetchBranchesPanel.loadingLocalBranches")); //NOI18N
        }
    }

    private void fillRemoteBranches (Map<String, GitBranch> branches, Map<String, GitBranch> localBranches) {
        List<BranchMapping> l = new ArrayList<>(branches.size());
        Set<String> displayedBranches = new HashSet<>(localBranches.size());
        for (GitBranch branch : branches.values()) {
            String branchName = remote.getRemoteName() + "/" + branch.getName();
            displayedBranches.add(branchName);
            GitBranch localBranch = localBranches.get(branchName);
            boolean preselected = localBranch != null && !localBranch.getId().equals(branch.getId());
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
        validateBeforeNext();
    }
    
    @Override
    public void stateChanged(ChangeEvent ce) {
        validateBeforeNext();
    }

    private void refreshRemoteBranches () {
        assert EventQueue.isDispatchThread();
        cancelBackgroundTasks();
        DefaultListModel model = new DefaultListModel();
        branches.setBranches(new ArrayList<BranchMapping>(0));
        if (fetchUri != null) {
            final String uri = fetchUri;
            model.addElement(NbBundle.getMessage(FetchBranchesStep.class, "MSG_FetchBranchesPanel.loadingBranches")); //NOI18N
            branches.setEnabled(false);
            Utils.post(new Runnable() {
                @Override
                public void run () {
                    try {
                        //TODO: what if repository is null?
                        final VCSFileProxy tempRepository = VCSFileProxy.createFileProxy(VCSFileProxySupport.getFileSystem(repository).getTempFolder());
                        supp = new GitProgressSupport.NoOutputLogging() {
                            @Override
                            protected void perform () {
                                final Map<String, GitBranch> branches = new HashMap<>();
                                final Map<String, GitBranch> localBranches = new HashMap<>();
                                try {
                                    GitClient client = getClient();
                                    client.init(getProgressMonitor());
                                    branches.putAll(client.listRemoteBranches(uri, getProgressMonitor()));
                                    if (repository != null) {
                                        RepositoryInfo info = RepositoryInfo.getInstance(repository);
                                        info.refresh();
                                        localBranches.putAll(info.getBranches());
                                    }
                                } catch (GitException ex) {
                                    GitClientExceptionHandler.notifyException(ex, true);
                                } finally {
                                    VCSFileProxySupport.delete(tempRepository);
                                    final GitProgressSupport supp = this;
                                    EventQueue.invokeLater(new Runnable () {
                                        @Override
                                        public void run () {
                                            if (!supp.isCanceled()) {
                                                fillRemoteBranches(branches, localBranches);
                                            }
                                        }
                                    });
                                }
                            }
                        };
                        supp.start(Git.getInstance().getRequestProcessor(tempRepository), tempRepository, NbBundle.getMessage(FetchBranchesStep.class, "MSG_FetchBranchesPanel.loadingBranches")); //NOI18N
                    } catch (IOException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
            });
        }
    }
    
    @NbBundle.Messages({
        "# {0} - branch name", "MSG_FetchBranchesStep.toBeDeletedBranch=Branch {0} will be permanently removed."
    })
    static String getDeletedBranchesMessage (List<BranchMapping> selectedBranches) {
        StringBuilder sb = new StringBuilder(100);
        for (BranchMapping m : selectedBranches) {
            if (m.isDeletion()) {
                sb.append(Bundle.MSG_FetchBranchesStep_toBeDeletedBranch(m.getLocalBranch().getName())).append("<br>");
            }
        }
        if (sb.length() == 0) {
            return null;
        } else {
            sb.delete(sb.length() - 4, sb.length());
            return sb.toString();
        }
    }

    public void cancelBackgroundTasks () {
        if (supp != null) {
            supp.cancel();
        }
        if (validatingSupp != null) {
            validatingSupp.cancel();
        }
    }

    public List<String> getSelectedRefSpecs () {
        List<String> specs = new LinkedList<>();
        for (BranchMapping b : branches.getSelectedBranches()) {
            specs.add(b.getRefSpec());
        }
        return specs;
    }

    @Override
    public boolean isFinishPanel () {
        return true;
    }
}
