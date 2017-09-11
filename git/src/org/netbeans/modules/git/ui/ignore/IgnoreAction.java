/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.ignore;

import java.io.File;
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
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.actions.MultipleRepositoryAction;
import org.netbeans.modules.git.utils.GitUtils;
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
@ActionID(id = "org.netbeans.modules.git.ui.ignore.IgnoreAction", category = "Git")
@ActionRegistration(displayName = "#LBL_IgnoreAction_Name")
@NbBundle.Messages("LBL_IgnoreAction_Name=&Ignore")
public class IgnoreAction extends MultipleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(IgnoreAction.class.getName());
    
    @Override
    protected Task performAction (File repository, File[] roots, VCSContext context) {
        return ignore(repository, roots);
    }

    @Override
    protected boolean enableFull (Node[] activatedNodes) {
        boolean enabled = super.enableFull(activatedNodes);
        if (enabled) {
            VCSContext ctx = getCurrentContext(activatedNodes);
            FileStatusCache cache = Git.getInstance().getFileStatusCache();
            enabled = cache.containsFiles(ctx, EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), true);
            if (!enabled) {
                for (File root : ctx.getRootFiles()) {
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
    public void ignoreFolders (File repository, File[] roots) {
        final File[] toIgnore = filterFolders(repository, roots);
        ignore(repository, toIgnore);
    }
    
    private Task ignore (File repository, File[] roots) {
        final File[] toIgnore = filterRoots(roots);
        if (toIgnore.length == 0) {
            return null;
        } else {
            GitProgressSupport supp = new GitProgressSupport() {
                private final Set<File> notifiedFiles = new HashSet<File>();
                private File[] modifiedIgnores = new File[0];
                @Override
                protected void perform () {
                    try {
                        GitClient client = getClient();
                        File[] toRemoveFromIndex = getForRemovalFromIndex(toIgnore);
                        if (toRemoveFromIndex.length > 0) {
                            client.remove(toIgnore, true, getProgressMonitor());
                        }
                        client.addNotificationListener(new DefaultFileListener(toIgnore));
                        client.addNotificationListener(new FileListener() {
                            @Override
                            public void notifyFile (File file, String relativePathToRoot) {
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
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(IgnoreAction.class, "LBL_IgnoreAction.progressName")); //NOI18N
            return supp.getTask();
        }
    }

    private static File[] filterRoots (File[] roots) {
        List<File> toIgnore = new LinkedList<File>();
        FileStatusCache cache = Git.getInstance().getFileStatusCache();
        for (File root : roots) {
            FileInformation info = cache.getStatus(root);
            if (info.isDirectory() || info.containsStatus(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE))) {
                toIgnore.add(root);
            }
        }
        return toIgnore.toArray(new File[toIgnore.size()]);
    }

    private static File[] getForRemovalFromIndex (File[] roots) {
        List<File> ret = new ArrayList<File>(roots.length);
        FileStatusCache cache = Git.getInstance().getFileStatusCache();
        for (File root : roots) {
            FileInformation info = cache.getStatus(root);
            if (info.containsStatus(Status.NEW_HEAD_INDEX)) {
                // file is new and scheduled for the initial commit
                // it has to be removed first from the index before being ignored
                ret.add(root);
            }
        }
        return ret.toArray(new File[ret.size()]);
    }

    private File[] filterFolders (File repository, File[] roots) {
        List<File> unignoredFolders = new LinkedList<File>();
        Map<File, GitStatus> statuses;
        GitClient client = null;
        try {
            client = Git.getInstance().getClient(repository);
            statuses = client.getStatus(roots, GitUtils.NULL_PROGRESS_MONITOR);
        } catch (GitException ex) {
            LOG.log(Level.INFO, null, ex);
            statuses = Collections.<File, GitStatus>emptyMap();
        } finally {
            if (client != null) {
                client.release();
            }
        }
        for (File f : roots) {
            GitStatus st = statuses.get(f);
            if (st == null || st.getStatusIndexWC() != GitStatus.Status.STATUS_IGNORED) {
                unignoredFolders.add(f);
            } else {
                LOG.log(Level.FINE, "File {0} already ignored", f);
            }
        }
        return unignoredFolders.toArray(new File[unignoredFolders.size()]);
    }
}
