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

package org.netbeans.modules.git.ui.ignore;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.actions.MultipleRepositoryAction;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.ignore.UnignoreAction", category = "Git")
@ActionRegistration(displayName = "#LBL_UnignoreAction_Name")
@NbBundle.Messages("LBL_UnignoreAction_Name=Un&ignore")
public class UnignoreAction extends MultipleRepositoryAction {

    @Override
    protected Task performAction (File repository, File[] roots, VCSContext context) {
        return unignore(repository, roots);
    }

    @Override
    protected boolean enableFull (Node[] activatedNodes) {
        boolean enabled = super.enableFull(activatedNodes);
        if (enabled) {
            enabled = false;
            FileStatusCache cache = Git.getInstance().getFileStatusCache();
            for (File root : getCurrentContext(activatedNodes).getRootFiles()) {
                if (cache.getStatus(root).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
                    File parent = root.getParentFile();
                    enabled = parent == null || !cache.getStatus(parent).containsStatus(Status.NOTVERSIONED_EXCLUDED);
                    break;
                }
            }
        }
        return enabled;
    }

    public Task unignore (File repository, File[] roots) {
        final File[] toUnignore = filterRoots(roots);
        if (toUnignore.length == 0) {
            return null;
        } else {
            GitProgressSupport supp = new GitProgressSupport() {
                private final Set<File> notifiedFiles = new HashSet<File>();
                private File[] modifiedIgnores = new File[0];
                @Override
                protected void perform () {
                    try {
                        GitClient client = getClient();
                        client.addNotificationListener(new DefaultFileListener(toUnignore));
                        client.addNotificationListener(new FileListener() {
                            @Override
                            public void notifyFile (File file, String relativePathToRoot) {
                                notifiedFiles.add(file);
                            }
                        });
                        modifiedIgnores = client.unignore(toUnignore, getProgressMonitor());
                        SystemAction.get(IgnoreAction.class).setEnabled(false);
                        SystemAction.get(UnignoreAction.class).setEnabled(false);
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    } finally {
                        if (modifiedIgnores.length > 0) {
                            FileUtil.refreshFor(modifiedIgnores);
                            notifiedFiles.addAll(Arrays.asList(modifiedIgnores));
                        }
                        if (!notifiedFiles.isEmpty()) {
                            setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                            Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(getRepositoryRoot(), notifiedFiles));
                        }
                    }
                }
            };
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(UnignoreAction.class, "LBL_UnignoreAction.progressName")); //NOI18N
            return supp.getTask();
        }
    }
    
    private static File[] filterRoots (File[] roots) {
        List<File> toUnignore = new LinkedList<File>();
        FileStatusCache cache = Git.getInstance().getFileStatusCache();
        for (File root : roots) {
            FileInformation info = cache.getStatus(root);
            if (info.containsStatus(EnumSet.of(Status.NOTVERSIONED_EXCLUDED))) {
                toUnignore.add(root);
            }
        }
        return toUnignore.toArray(new File[0]);
    }
}
