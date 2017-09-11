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
        return toUnignore.toArray(new File[toUnignore.size()]);
    }
}
