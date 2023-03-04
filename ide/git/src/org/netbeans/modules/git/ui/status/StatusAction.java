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

package org.netbeans.modules.git.ui.status;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.status.StatusAction", category = "Git")
@ActionRegistration(displayName = "#LBL_StatusAction_Name")
@NbBundle.Messages({
    "LBL_StatusAction_Name=Sho&w Changes"
})
public class StatusAction extends GitAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/show_changes.png"; //NOI18N
    
    public StatusAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected final void performContextAction (Node[] nodes) {
        VCSContext context = getCurrentContext(nodes);
        performContextAction(context);
    }

    public void performContextAction (VCSContext context) {
        GitVersioningTopComponent stc = GitVersioningTopComponent.findInstance();
        stc.setContentTitle(Utils.getContextDisplayName(context));
        stc.setContext(context);
        stc.open();
        stc.requestActive();
    }

    /**
     * Starts the status scan but does not wait for it to finish.
     * @param context
     * @return running task
     */
    public final GitProgressSupport scanStatus (final VCSContext context) {
        Set<File> repositories = GitUtils.getRepositoryRoots(context);
        if (repositories.isEmpty()) {
            return null;
        } else {
            final Map<File, Collection<File>> toRefresh = new HashMap<File, Collection<File>>(repositories.size());
            for (File repository : repositories) {
                GitUtils.logRemoteRepositoryAccess(repository);
                toRefresh.put(repository, Arrays.asList(GitUtils.filterForRepository(context, repository)));
            }
            GitProgressSupport supp = new GitProgressSupport() {
                @Override
                protected void perform () {
                    long t = 0;
                    if (Git.STATUS_LOG.isLoggable(Level.FINE)) {
                        t = System.currentTimeMillis();
                        Git.STATUS_LOG.log(Level.FINE, "StatusAction.scanStatus(): started for {0}", toRefresh.keySet()); //NOI18N
                    }
                    Git.getInstance().getFileStatusCache().refreshAllRoots(toRefresh, getProgressMonitor());
                    if (Git.STATUS_LOG.isLoggable(Level.FINE)) {
                        Git.STATUS_LOG.log(Level.FINE, "StatusAction.scanStatus(): lasted {0}", System.currentTimeMillis() - t); //NOI18N
                    }
                }
            };
            supp.start(Git.getInstance().getRequestProcessor(), null, NbBundle.getMessage(StatusAction.class, "LBL_ScanningStatuses")); //NOI18N
            return supp;
        }
    }

}
