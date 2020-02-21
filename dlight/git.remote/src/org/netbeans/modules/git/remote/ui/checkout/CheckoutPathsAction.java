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

package org.netbeans.modules.git.remote.ui.checkout;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.actions.GitAction;
import org.netbeans.modules.git.remote.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.checkout.CheckoutPathsAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_CheckoutPathsAction_Name")
public class CheckoutPathsAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(CheckoutPathsAction.class.getName());

    @Override
    protected void performAction (VCSFileProxy repository, VCSFileProxy[] roots, VCSContext context) {
        final CheckoutPaths checkout = new CheckoutPaths(repository, roots);
        if (checkout.show()) {
            checkoutFiles(repository, roots, checkout.getRevision());
        }
    }
    
    public void checkoutFiles (final VCSFileProxy repository, final VCSFileProxy[] roots, final String revision) {
        GitProgressSupport supp = new GitProgressSupport() {

            @Override
            protected void perform () {
                final Collection<VCSFileProxy> notifiedFiles = new HashSet<>();
                try {
                    final GitClient client = getClient();
                    client.addNotificationListener(new FileListener() {
                        @Override
                        public void notifyFile (VCSFileProxy file, String relativePathToRoot) {
                            notifiedFiles.add(file);
                        }
                    });
                    client.addNotificationListener(new DefaultFileListener(roots));
                    final VCSFileProxy[][] split = VCSFileProxySupport.splitFlatOthers(roots);
                    GitUtils.runWithoutIndexing(new Callable<Void>() {

                        @Override
                        public Void call () throws Exception {
                            for (int c = 0; c < split.length; c++) {
                                VCSFileProxy[] splitRoots = split[c];
                                if (splitRoots.length == 0) {
                                    continue;
                                }
                                if (c == 1) {
                                    // recursive
                                    LOG.log(Level.FINE, "Checking out paths recursively, revision: {0}", revision); //NOI18N
                                    client.checkout(splitRoots, revision, true, getProgressMonitor());
                                } else {
                                    // not recursive, list only direct descendants
                                    LOG.log(Level.FINE, "Checking out paths non-recursively, revision: {0}", revision); //NOI18N
                                    client.checkout(splitRoots, revision, false, getProgressMonitor());
                                }
                            }
                            return null;
                        }
                    }, roots);
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                } finally {
                    if (!notifiedFiles.isEmpty()) {
                        setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                        Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.singletonMap(getRepositoryRoot(), notifiedFiles));
                    }
                }
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(CheckoutPathsAction.class, "LBL_CheckoutPaths.progressName"));
    }

}
