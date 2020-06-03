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

package org.netbeans.modules.git.remote.ui.conflicts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.FileInformation.Status;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.actions.GitAction;
import org.netbeans.modules.git.remote.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 */
public class MarkResolvedAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(MarkResolvedAction.class.getName());

    @Override
    protected boolean enable (Node[] activatedNodes) {
        VCSContext context = getCurrentContext(activatedNodes);
        return Git.getInstance().getFileStatusCache().containsFiles(context, EnumSet.of(Status.IN_CONFLICT), false);
    }

    @Override
    protected void performAction (final VCSFileProxy repository, final VCSFileProxy[] roots, VCSContext context) {
        GitProgressSupport supp = new GitProgressSupport () {
            @Override
            protected void perform() {
                VCSFileProxy[] conflicts = Git.getInstance().getFileStatusCache().listFiles(roots, EnumSet.of(Status.IN_CONFLICT));
                if (conflicts.length == 0) {
                    DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor(NbBundle.getMessage(MarkResolvedAction.class, "MSG_NoConflicts"), //NOI18N
                            NbBundle.getMessage(MarkResolvedAction.class, "LBL_NoConflicts"), //NOI18N
                            NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.INFORMATION_MESSAGE, new Object[] { NotifyDescriptor.OK_OPTION }, NotifyDescriptor.OK_OPTION));
                } else {
                    List<VCSFileProxy> toAdd = new LinkedList<>(), toRemove = new LinkedList<>();
                    for (VCSFileProxy f : conflicts) {
                        (f.exists() ? toAdd : toRemove).add(f);
                    }
                    GitClient client;
                    try {
                        client = getClient();
                        client.addNotificationListener(new DefaultFileListener(roots));
                        if (!toAdd.isEmpty()) {
                            client.add(toAdd.toArray(new VCSFileProxy[toAdd.size()]), getProgressMonitor());
                        }
                        if (!toRemove.isEmpty()) {
                            client.remove(toRemove.toArray(new VCSFileProxy[toRemove.size()]), true, getProgressMonitor());
                        }
                    } catch (GitException ex) {
                        LOG.log(Level.WARNING, null, ex);
                    } finally {
                        setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                        Collection<VCSFileProxy> toRefresh = new ArrayList<>(toAdd.size() + toRemove.size());
                        toRefresh.addAll(toAdd);
                        toRefresh.addAll(toRemove);
                        Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.singletonMap(repository, toRefresh));
                    }
                }
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(MarkResolvedAction.class, "LBL_MarkingProgress")); //NOI18N
    }
    
}
