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

package org.netbeans.modules.git.ui.reset;

import org.netbeans.modules.git.client.GitClientExceptionHandler;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
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
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.reset.ResetAction", category = "Git")
@ActionRegistration(displayName = "#LBL_ResetAction_Name")
@NbBundle.Messages("LBL_ResetAction_Name=Re&set...")
public class ResetAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(ResetAction.class.getName());

    @Override
    protected void performAction (final File repository, final File[] roots, VCSContext context) {
        final Reset reset = new Reset(repository, roots);
        if (reset.show() && (reset.getType() != GitClient.ResetType.SOFT || !reset.getRevision().equals(GitUtils.HEAD))) {
            GitProgressSupport supp = new GitProgressSupport() {

                @Override
                protected void perform () {
                    try {
                        final org.netbeans.modules.git.client.GitClient client = getClient();
                        if (reset.getType() == GitClient.ResetType.HARD) {
                            client.addNotificationListener(new DefaultFileListener(new File[] { repository }));
                        }
                        client.addNotificationListener(new DefaultFileListener(new File[] { repository }));
                        LOG.log(Level.FINE, "Reset head, revision: {0}", reset.getRevision()); //NOI18N
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
                        Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(getRepositoryRoot(), Git.getInstance().getSeenRoots(repository)));
                        GitUtils.headChanged(repository);
                    }
                }
            };
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(ResetAction.class, "LBL_Reset.progressName"));
        }
    }

}
