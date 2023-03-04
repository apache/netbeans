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
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.checkout.CheckoutPathsAction", category = "Git")
@ActionRegistration(displayName = "#LBL_CheckoutPathsAction_Name")
public class CheckoutPathsAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(CheckoutPathsAction.class.getName());

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        final CheckoutPaths checkout = new CheckoutPaths(repository, roots);
        if (checkout.show()) {
            checkoutFiles(repository, roots, checkout.getRevision());
        }
    }
    
    public void checkoutFiles (final File repository, final File[] roots, final String revision) {
        GitProgressSupport supp = new GitProgressSupport() {

            @Override
            protected void perform () {
                final Collection<File> notifiedFiles = new HashSet<File>();
                try {
                    final GitClient client = getClient();
                    client.addNotificationListener(new FileListener() {
                        @Override
                        public void notifyFile (File file, String relativePathToRoot) {
                            notifiedFiles.add(file);
                        }
                    });
                    client.addNotificationListener(new DefaultFileListener(roots));
                    final File[][] split = Utils.splitFlatOthers(roots);
                    GitUtils.runWithoutIndexing(new Callable<Void>() {

                        @Override
                        public Void call () throws Exception {
                            for (int c = 0; c < split.length; c++) {
                                File[] splitRoots = split[c];
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
