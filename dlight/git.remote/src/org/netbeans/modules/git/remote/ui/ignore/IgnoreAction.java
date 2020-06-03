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

package org.netbeans.modules.git.remote.ui.ignore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitStatus;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.FileInformation;
import org.netbeans.modules.git.remote.FileInformation.Status;
import org.netbeans.modules.git.remote.FileStatusCache;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.actions.GitAction;
import org.netbeans.modules.git.remote.ui.actions.MultipleRepositoryAction;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.actions.SystemAction;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.ignore.IgnoreAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_IgnoreAction_Name")
@NbBundle.Messages("LBL_IgnoreAction_Name=&Ignore")
public class IgnoreAction extends MultipleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(IgnoreAction.class.getName());
    
    @Override
    protected Task performAction (VCSFileProxy repository, VCSFileProxy[] roots, VCSContext context) {
        return ignore(repository, roots);
    }

    @Override
    protected boolean enable (Node[] activatedNodes) {
        boolean enabled = super.enable(activatedNodes);
        if (enabled) {
            VCSContext ctx = getCurrentContext(activatedNodes);
            FileStatusCache cache = Git.getInstance().getFileStatusCache();
            enabled = cache.containsFiles(ctx, EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), true);
            if (!enabled) {
                for (VCSFileProxy root : ctx.getRootFiles()) {
                    FileInformation status = cache.getStatus(root);
                    if (status.isDirectory() && !status.containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
                        enabled = true;
                        break;
                    }
                }
            }
        }
        return enabled;
    }

    /**
     * Ignores those files from the given roots that are not yet ignored. So those already ignored are skipped.
     * @param repository
     * @param roots 
     */
    public void ignoreFolders (VCSFileProxy repository, VCSFileProxy[] roots) {
        final VCSFileProxy[] toIgnore = filterFolders(repository, roots);
        ignore(repository, toIgnore);
    }
    
    private Task ignore (VCSFileProxy repository, VCSFileProxy[] roots) {
        final VCSFileProxy[] toIgnore = filterRoots(roots);
        if (toIgnore.length == 0) {
            return null;
        } else {
            GitProgressSupport supp = new GitProgressSupport() {
                private final Set<VCSFileProxy> notifiedFiles = new HashSet<>();
                private VCSFileProxy[] modifiedIgnores = new VCSFileProxy[0];
                @Override
                protected void perform () {
                    try {
                        GitClient client = getClient();
                        VCSFileProxy[] toRemoveFromIndex = getForRemovalFromIndex(toIgnore);
                        if (toRemoveFromIndex.length > 0) {
                            client.remove(toIgnore, true, getProgressMonitor());
                        }
                        client.addNotificationListener(new DefaultFileListener(toIgnore));
                        client.addNotificationListener(new FileListener() {
                            @Override
                            public void notifyFile (VCSFileProxy file, String relativePathToRoot) {
                                notifiedFiles.add(file);
                            }
                        });
                        modifiedIgnores = client.ignore(toIgnore, getProgressMonitor());
                        SystemAction.get(IgnoreAction.class).setEnabled(false);
                        SystemAction.get(UnignoreAction.class).setEnabled(false);
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    } finally {
                        if (modifiedIgnores.length > 0) {
                            VersioningSupport.refreshFor(modifiedIgnores);
                            notifiedFiles.addAll(Arrays.asList(modifiedIgnores));
                        }
                        if (!notifiedFiles.isEmpty()) {
                            setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                            Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.<VCSFileProxy, Collection<VCSFileProxy>>singletonMap(getRepositoryRoot(), notifiedFiles));
                        }
                    }
                }
            };
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(IgnoreAction.class, "LBL_IgnoreAction.progressName")); //NOI18N
            return supp.getTask();
        }
    }

    private static VCSFileProxy[] filterRoots (VCSFileProxy[] roots) {
        List<VCSFileProxy> toIgnore = new LinkedList<>();
        FileStatusCache cache = Git.getInstance().getFileStatusCache();
        for (VCSFileProxy root : roots) {
            FileInformation info = cache.getStatus(root);
            if (info.isDirectory() || info.containsStatus(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE))) {
                toIgnore.add(root);
            }
        }
        return toIgnore.toArray(new VCSFileProxy[toIgnore.size()]);
    }

    private static VCSFileProxy[] getForRemovalFromIndex (VCSFileProxy[] roots) {
        List<VCSFileProxy> ret = new ArrayList<>(roots.length);
        FileStatusCache cache = Git.getInstance().getFileStatusCache();
        for (VCSFileProxy root : roots) {
            FileInformation info = cache.getStatus(root);
            if (info.containsStatus(Status.NEW_HEAD_INDEX)) {
                // file is new and scheduled for the initial commit
                // it has to be removed first from the index before being ignored
                ret.add(root);
            }
        }
        return ret.toArray(new VCSFileProxy[ret.size()]);
    }

    private VCSFileProxy[] filterFolders (VCSFileProxy repository, VCSFileProxy[] roots) {
        List<VCSFileProxy> unignoredFolders = new LinkedList<>();
        Map<VCSFileProxy, GitStatus> statuses;
        GitClient client = null;
        try {
            client = Git.getInstance().getClient(repository);
            statuses = client.getStatus(roots, GitUtils.NULL_PROGRESS_MONITOR);
        } catch (GitException ex) {
            LOG.log(Level.INFO, null, ex);
            statuses = Collections.<VCSFileProxy, GitStatus>emptyMap();
        } finally {
            if (client != null) {
                client.release();
            }
        }
        for (VCSFileProxy f : roots) {
            GitStatus st = statuses.get(f);
            if (st == null || st.getStatusIndexWC() != GitStatus.Status.STATUS_IGNORED) {
                unignoredFolders.add(f);
            } else {
                LOG.log(Level.FINE, "File {0} already ignored", f);
            }
        }
        return unignoredFolders.toArray(new VCSFileProxy[unignoredFolders.size()]);
    }
}
