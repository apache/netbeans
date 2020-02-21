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

package org.netbeans.modules.git.remote.ui.reset;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.actions.GitAction;
import org.netbeans.modules.git.remote.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.reset.ResetAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_ResetAction_Name")
@NbBundle.Messages("LBL_ResetAction_Name=Re&set...")
public class ResetAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(ResetAction.class.getName());

    @Override
    protected void performAction (final VCSFileProxy repository, final VCSFileProxy[] roots, VCSContext context) {
        final Reset reset = new Reset(repository, roots);
        if (reset.show() && (reset.getType() != GitClient.ResetType.SOFT || !reset.getRevision().equals(GitUtils.HEAD))) {
            GitProgressSupport supp = new GitProgressSupport() {

                @Override
                protected void perform () {
                    try {
                        final org.netbeans.modules.git.remote.client.GitClient client = getClient();
                        if (reset.getType() == GitClient.ResetType.HARD) {
                            client.addNotificationListener(new DefaultFileListener(new VCSFileProxy[] { repository }));
                        }
                        client.addNotificationListener(new DefaultFileListener(new VCSFileProxy[] { repository }));
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "Reset head, revision: {0}", reset.getRevision()); //NOI18N
                        }
                        if (reset.getType() == GitClient.ResetType.HARD) {
                            GitUtils.runWithoutIndexing(new Callable<Void>() {
                                @Override
                                public Void call () throws Exception {
                                    client.reset(reset.getRevision(), reset.getType(), getProgressMonitor());
                                    return null;
                                }
                            }, repository);
                        } else {
                            client.reset(reset.getRevision(), reset.getType(), getProgressMonitor());
                        }
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    } finally {
                        setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                        Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.<VCSFileProxy, Collection<VCSFileProxy>>singletonMap(getRepositoryRoot(), Git.getInstance().getSeenRoots(repository)));
                        GitUtils.headChanged(repository);
                    }
                }
            };
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(ResetAction.class, "LBL_Reset.progressName"));
        }
    }

}
