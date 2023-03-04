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

package org.netbeans.modules.git.ui.checkout;

import org.netbeans.modules.git.client.GitClientExceptionHandler;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
// XXX create tests for all possible revert settings
@ActionID(id = "org.netbeans.modules.git.ui.checkout.RevertChangesAction", category = "Git")
@ActionRegistration(displayName = "#LBL_RevertChangesAction_Name")
@NbBundle.Messages("LBL_RevertChangesAction_Name=&Revert Modifications...")
public class RevertChangesAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(RevertChangesAction.class.getName());
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/get_clean.png"; //NOI18N
    
    public RevertChangesAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }

    @Override
    protected void performAction (final File repository, final File[] roots, VCSContext context) {
        final RevertChanges revert = new RevertChanges(roots);
        if (revert.show()) {
            GitProgressSupport supp = new GitProgressSupport() {
                @Override
                protected void perform () {
                    final Collection<File> notifiedFiles = new HashSet<File>();
                    final File[] actionRoots = GitUtils.listFiles(roots, FileInformation.STATUS_LOCAL_CHANGES);
                    if (actionRoots.length == 0) {
                        return;
                    }
                    try {
                        GitUtils.runWithoutIndexing(new Callable<Void>() {

                            @Override
                            public Void call () throws Exception {
                                // init client
                                GitClient client = getClient();
                                client.addNotificationListener(new FileListener() {
                                    @Override
                                    public void notifyFile (File file, String relativePathToRoot) {
                                        notifiedFiles.add(file);
                                    }
                                });
                                client.addNotificationListener(new DefaultFileListener(actionRoots));

                                // revert
                                if(revert.isRevertAll()) {
                                    logRevert("revert all", actionRoots, repository);
                                    client.checkout(actionRoots, "HEAD", true, getProgressMonitor()); // XXX no constant for HEAD???
                                } else if (revert.isRevertIndex()) {
                                    logRevert("revert index", actionRoots, repository);
                                    client.reset(actionRoots, "HEAD", true, getProgressMonitor());
                                } else if (revert.isRevertWT()) {
                                    logRevert("revert wt", actionRoots, repository);
                                    client.checkout(actionRoots, null, true, getProgressMonitor());                             
                                }

                                if(revert.isRemove()) {
                                    logRevert("clean ", actionRoots, repository);
                                    client.clean(actionRoots, getProgressMonitor());
                                }
                                return null;
                            }
                        }, actionRoots);
                        
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    } finally {
                        // persist settings
                        revert.storeSettings();                        
                        
                        // refresh
                        setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                        Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.singletonMap(getRepositoryRoot(), notifiedFiles));                        
                    }
                }

                private void logRevert(String msg, File[] roots, File repository) {
                    if(LOG.isLoggable(Level.FINE)) {
                        String  repopath = repository.getAbsolutePath();
                        if(!repopath.endsWith("/")) {
                            repopath = repopath + "/";
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append(msg);
                        sb.append(" [");
                        for (int i = 0; i < roots.length; i++) {
                            String path = roots[i].getAbsolutePath();
                            if(path.startsWith(repopath)) {
                                path = path.substring(repopath.length());
                            }
                            sb.append(path);
                            if(i < roots.length - 1) {
                                sb.append(",");
                            }
                        }
                        sb.append("]");
                        LOG.fine(sb.toString());
                    }
                }
            };
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(RevertChangesAction.class, "LBL_CheckoutPaths.progressName"));        
        }
    }

    public void revertFiles (File repository, final File[] files, final String revision, String progressName) {
        if (files.length == 0) {
            return;
        }
        new GitProgressSupport() {
            @Override
            public void perform() {
                final Collection<File> notifiedFiles = new HashSet<File>();
                try {
                    GitUtils.runWithoutIndexing(new Callable<Void>() {

                        @Override
                        public Void call () throws Exception {
                            // init client
                            GitClient client = getClient();
                            client.addNotificationListener(new FileListener() {
                                @Override
                                public void notifyFile (File file, String relativePathToRoot) {
                                    notifiedFiles.add(file);
                                }
                            });
                            client.addNotificationListener(new GitProgressSupport.DefaultFileListener(files));

                            // revert
                            client.checkout(files, revision, false, getProgressMonitor());                             
                            return null;
                        }
                        
                    }, files);
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                } finally {
                    // refresh
                    setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                    Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.singletonMap(getRepositoryRoot(), notifiedFiles));
                }
            }
        }.start(Git.getInstance().getRequestProcessor(repository), repository, progressName);
    }
    
}
