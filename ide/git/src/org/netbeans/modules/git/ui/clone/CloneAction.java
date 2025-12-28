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

package org.netbeans.modules.git.ui.clone;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitSubmoduleStatus;
import org.netbeans.libs.git.GitTransportUpdate;
import org.netbeans.libs.git.GitTransportUpdate.Type;
import org.netbeans.libs.git.GitURI;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.CredentialsCallback;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.ContextHolder;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.ProjectUtilities;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Stupka
 */
@ActionID(id = "org.netbeans.modules.git.ui.clone.CloneAction", category = "Git")
@ActionRegistration(displayName = "#LBL_CloneAction_Name")
@ActionReferences({
   @ActionReference(path="Versioning/Git/Actions/Global", position=310)
})
@NbBundle.Messages("LBL_CloneAction_Name=&Clone...")
public class CloneAction implements ActionListener, HelpCtx.Provider {
    private final VCSContext ctx;
    private static final Logger LOG = Logger.getLogger(CloneAction.class.getName());

    public CloneAction (ContextHolder ctx) {
        this.ctx = ctx.getContext();
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.git.ui.clone.CloneAction");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        Utils.logVCSActionEvent("Git"); //NOI18N
        String cloneFromPath = null;
        if(ctx != null) {
            Set<File> roots = ctx.getRootFiles();
            if(roots.size() == 1) {
                Lookup l = ctx.getElements();
                Project project = null;
                if(l != null) {
                    Collection<? extends Node> nodes = l.lookupAll(Node.class);
                    if(nodes != null && !nodes.isEmpty()) {
                        project = nodes.iterator().next().getLookup().lookup(Project.class);
                    }
                }
                if(project == null) {
                    FileObject fo = FileUtil.toFileObject(roots.iterator().next());
                    if(fo != null && fo.isFolder()) {
                        try {
                            project = ProjectManager.getDefault().findProject(fo);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IllegalArgumentException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                if(project != null) {
                    FileObject fo = project.getProjectDirectory();
                    File file = FileUtil.toFile(fo);
                    if(file != null) {
                        if(Git.getInstance().isManaged(file) ) {
                            cloneFromPath = Git.getInstance().getRepositoryRoot(file).getAbsolutePath();
                        }
                    }
                }
            }
        }
        performClone(cloneFromPath, null);
    }

    private static void performClone(String url, PasswordAuthentication pa) throws MissingResourceException {
        performClone(url, pa, false);
    }
    
    @NbBundle.Messages({
        "LBL_Clone.confirmSubmoduleInit.title=Initialize Submodules",
        "MSG_Clone.confirmSubmoduleInit.text=Uninitialized submodules found in the cloned repository.\n\n"
                + "Do you want to automatically initialize and clone them?",
        "MSG_Clone.progress.initializingRepository=Initializing repository",
        "MSG_Clone.progress.fetchingCommits=Fetching commits",
        "# {0} - remote name", "MSG_Clone.progress.settingRemote=Setting up \"{0}\" remote",
        "# {0} - branch name", "MSG_Clone.progress.creatingBranch=Creating \"{0}\" branch",
        "# {0} - branch name", "MSG_Clone.progress.checkingoutBranch=Checking-out \"{0}\" branch",
        "MSG_Clone.progress.refreshingFiles=Refreshing files",
        "MSG_Clone.progress.scanningForProjects=Scanning for NetBeans projects",
        "MSG_Clone.progress.checkingForSubmodules=Checking for submodules",
        "MSG_Clone.progress.initializingSubmodules=Initializing submodules",
        "MSG_Clone.progress.updatingSubmodules=Updating submodules",
        "# {0} - submodule folder", "MSG_Clone.progress.updatingSubmodule=Submodule {0}"
    })
    public static File performClone(String url, PasswordAuthentication pa, boolean waitFinished) throws MissingResourceException {
        final CloneWizard wiz = new CloneWizard(pa, url);
        Boolean ok = Mutex.EVENT.readAccess(new Mutex.Action<Boolean>() {
            @Override
            public Boolean run () {
                return wiz.show();
            }
        });
        if (Boolean.TRUE.equals(ok)) {            
            final GitURI remoteUri = wiz.getRemoteURI();
            final File destination = wiz.getDestination();
            final String remoteName = wiz.getRemoteName();
            List<String> branches = wiz.getBranchNames();
            final List<String> refSpecs;
            if (branches == CloneWizard.ALL_BRANCHES) {
                // all branches to fetch
                refSpecs = Collections.<String>singletonList(GitUtils.getGlobalRefSpec(remoteName));
            } else {
                refSpecs = new ArrayList<String>(branches.size());
                for (String branchName : branches) {
                    refSpecs.add(GitUtils.getRefSpec(branchName, remoteName));
                }
            }
            final GitBranch branch = wiz.getBranch();
            final boolean scan = wiz.scanForProjects();
            
            GitProgressSupport supp = new GitProgressSupport(10) {
                @Override
                protected void perform () {
                    try {
                        GitUtils.runWithoutIndexing(new Callable<Void>() {
                            @Override
                            public Void call () throws Exception {
                                GitClient client = getClient();
                                setDisplayName(Bundle.MSG_Clone_progress_initializingRepository());
                                client.init(getProgressMonitor());
                                Git.getInstance().versionedFilesChanged();
                                setDisplayName(Bundle.MSG_Clone_progress_fetchingCommits(), 1);
                                Map<String, GitTransportUpdate> updates = client.fetch(remoteUri.toPrivateString(), refSpecs, getProgressMonitor());
                                log(updates);

                                if(isCanceled()) {
                                    return null;
                                }
                                
                                List<String> refs = Arrays.asList(GitUtils.getGlobalRefSpec(remoteName));
                                setDisplayName(Bundle.MSG_Clone_progress_settingRemote(remoteName), 2);
                                String username = new CredentialsCallback().getUsername(remoteUri.toString(), "");
                                GitURI uriToSave = remoteUri;
                                if (username != null && !username.isEmpty()) {
                                    uriToSave = uriToSave.setUser(username);
                                }
                                client.setRemote(new CloneRemoteConfig(remoteName, uriToSave, refs).toGitRemote(), getProgressMonitor());
                                org.netbeans.modules.versioning.util.Utils.logVCSExternalRepository("GIT", remoteUri.getHost()); //NOI18N
                                if (branch == null) {
                                    setDisplayName(Bundle.MSG_Clone_progress_creatingBranch(GitUtils.MASTER), 1);
                                    client.createBranch(GitUtils.MASTER, GitUtils.PREFIX_R_REMOTES + remoteName + "/" + GitUtils.MASTER, getProgressMonitor());
                                } else {
                                    setDisplayName(Bundle.MSG_Clone_progress_checkingoutBranch(branch.getName()), 1);
                                    client.createBranch(branch.getName(), remoteName + "/" + branch.getName(), getProgressMonitor());
                                    client.checkoutRevision(branch.getName(), true, getProgressMonitor());
                                    client.reset(branch.getName(), org.netbeans.libs.git.GitClient.ResetType.HARD, getProgressMonitor());
                                }

                                setDisplayName(Bundle.MSG_Clone_progress_refreshingFiles(), 2);
                                Git.getInstance().getFileStatusCache().refreshAllRoots(destination);
                                
                                if (!isCanceled()) {
                                    initSubmodules();
                                    Git.getInstance().versionedFilesChanged();
                                }

                                if(scan && !isCanceled()) {
                                    setDisplayName(Bundle.MSG_Clone_progress_scanningForProjects(), 1);
                                    scanForProjects(destination);
                                }
                                return null;
                            }

                            private void initSubmodules () {
                                try {
                                    GitClient client = getClient();
                                    setDisplayName(Bundle.MSG_Clone_progress_checkingForSubmodules(), 1);
                                    Map<File, GitSubmoduleStatus> statuses = client.getSubmoduleStatus(new File[0], getProgressMonitor());
                                    List<File> toInit = new ArrayList<>(statuses.size());
                                    for (Map.Entry<File, GitSubmoduleStatus> e : statuses.entrySet()) {
                                        if (e.getValue().getStatus() == GitSubmoduleStatus.StatusType.UNINITIALIZED) {
                                            toInit.add(e.getKey());
                                        }
                                    }
                                    if (!isCanceled() && !toInit.isEmpty() && confirmSubmoduleInit(toInit)) {
                                        setDisplayName(Bundle.MSG_Clone_progress_initializingSubmodules(), 1);
                                        client.initializeSubmodules(toInit.toArray(new File[0]), getProgressMonitor());
                                        setDisplayName(Bundle.MSG_Clone_progress_updatingSubmodules(), 1);
                                        for (File submoduleRoot : toInit) {
                                            if (isCanceled()) {
                                                return;
                                            }
                                            try {
                                                setProgress(Bundle.MSG_Clone_progress_updatingSubmodule(submoduleRoot.getName()));
                                                client.updateSubmodules(new File[] { submoduleRoot }, getProgressMonitor());
                                            } catch (GitException ex) {
                                                LOG.log(Level.INFO, null, ex);
                                            }
                                        }
                                    } else {
                                        updateProgress(2);
                                    }
                                } catch (GitException ex) {
                                    LOG.log(Level.INFO, null, ex);
                                }
                            }

                            private boolean confirmSubmoduleInit (List<File> subrepos) {
                                return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(Utilities.findDialogParent(),
                                        Bundle.MSG_Clone_confirmSubmoduleInit_text(),
                                        Bundle.LBL_Clone_confirmSubmoduleInit_title(),
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE);
                            }
                        }, destination);
                        
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    }
                }

                private void log (Map<String, GitTransportUpdate> updates) {
                    OutputLogger logger = getLogger();
                    if (updates.isEmpty()) {
                        logger.outputLine(NbBundle.getMessage(CloneAction.class, "MSG_CloneAction.updates.noChange")); //NOI18N
                    } else {
                        for (Map.Entry<String, GitTransportUpdate> e : updates.entrySet()) {
                            GitTransportUpdate update = e.getValue();
                            if (update.getType() == Type.BRANCH) {
                                logger.outputLine(NbBundle.getMessage(CloneAction.class, "MSG_CloneAction.updates.updateBranch", new Object[] { //NOI18N
                                    update.getLocalName(), 
                                    update.getOldObjectId(),
                                    update.getNewObjectId(),
                                    update.getResult(),
                                }));
                            } else {
                                logger.outputLine(NbBundle.getMessage(CloneAction.class, "MSG_CloneAction.updates.updateTag", new Object[] { //NOI18N
                                    update.getLocalName(), 
                                    update.getResult(),
                                }));
                            }
                        }
                    }
                }

                public void scanForProjects (File workingFolder) {
                    Map<Project, Set<Project>> checkedOutProjects = new HashMap<Project, Set<Project>>();
                    checkedOutProjects.put(null, new HashSet<Project>()); // initialize root project container
                    File normalizedWorkingFolder = FileUtil.normalizeFile(workingFolder);
                    FileObject fo = FileUtil.toFileObject(normalizedWorkingFolder);
                    if (fo == null || !fo.isFolder()) {
                        return;
                    } else {
                        ProjectUtilities.scanForProjects(fo, checkedOutProjects);
                    }
                    if (isCanceled()) {
                        return;
                    }
                    // open project selection
                    ProjectUtilities.openClonedOutProjects(checkedOutProjects, workingFolder);
                }
            };
            Task task = supp.start(Git.getInstance().getRequestProcessor(destination), destination, NbBundle.getMessage(CloneAction.class, "LBL_CloneAction.progressName")); //NOI18N
            if(waitFinished) {
                task.waitFinished();
            }
            return destination;
        }
        return null;
    }
    
    private static class CloneRemoteConfig {
        private String remoteName;
        private GitURI remoteUri;
        private List<String> refSpecs;
        public CloneRemoteConfig(String remoteName, GitURI remoteUri, List<String> refSpecs) {
            this.remoteName = remoteName;
            this.remoteUri = remoteUri;
            this.refSpecs = refSpecs;
        }
        public String getRemoteName() {
            return remoteName;
        }
        public List<String> getUris() {
            return Arrays.asList(remoteUri.toPrivateString());
        }
        public List<String> getPushUris() {
            return Collections.emptyList();
        }
        public List<String> getFetchRefSpecs() {
            return refSpecs;
        }
        public List<String> getPushRefSpecs() {
            return Collections.emptyList();
        }

        private GitRemoteConfig toGitRemote () {
            return new GitRemoteConfig(remoteName, getUris(), getPushUris(), getFetchRefSpecs(), getPushRefSpecs());
        }
    }    
}
