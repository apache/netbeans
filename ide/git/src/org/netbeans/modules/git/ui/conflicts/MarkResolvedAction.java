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

package org.netbeans.modules.git.ui.conflicts;

import org.netbeans.modules.git.ui.actions.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class MarkResolvedAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(MarkResolvedAction.class.getName());

    @Override
    protected boolean enableFull (Node[] activatedNodes) {
        VCSContext context = getCurrentContext(activatedNodes);
        return Git.getInstance().getFileStatusCache().containsFiles(context, EnumSet.of(Status.IN_CONFLICT), false);
    }

    @Override
    protected void performAction (final File repository, final File[] roots, VCSContext context) {
        GitProgressSupport supp = new GitProgressSupport () {
            @Override
            protected void perform() {
                File[] conflicts = Git.getInstance().getFileStatusCache().listFiles(roots, EnumSet.of(Status.IN_CONFLICT));
                if (conflicts.length == 0) {
                    DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor(NbBundle.getMessage(MarkResolvedAction.class, "MSG_NoConflicts"), //NOI18N
                            NbBundle.getMessage(MarkResolvedAction.class, "LBL_NoConflicts"), //NOI18N
                            NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.INFORMATION_MESSAGE, new Object[] { NotifyDescriptor.OK_OPTION }, NotifyDescriptor.OK_OPTION));
                } else {
                    List<File> toAdd = new LinkedList<File>(), toRemove = new LinkedList<File>();
                    for (File f : conflicts) {
                        (f.exists() ? toAdd : toRemove).add(f);
                    }
                    GitClient client;
                    try {
                        client = getClient();
                        client.addNotificationListener(new DefaultFileListener(roots));
                        if (!toAdd.isEmpty()) {
                            client.add(toAdd.toArray(new File[0]), getProgressMonitor());
                        }
                        if (!toRemove.isEmpty()) {
                            client.remove(toRemove.toArray(new File[0]), true, getProgressMonitor());
                        }
                    } catch (GitException ex) {
                        LOG.log(Level.WARNING, null, ex);
                    } finally {
                        setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                        Collection<File> toRefresh = new ArrayList<File>(toAdd.size() + toRemove.size());
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
