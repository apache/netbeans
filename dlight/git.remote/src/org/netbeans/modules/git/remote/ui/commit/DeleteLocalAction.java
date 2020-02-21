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

package org.netbeans.modules.git.remote.ui.commit;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.FileInformation;
import org.netbeans.modules.git.remote.FileInformation.Status;
import org.netbeans.modules.git.remote.FileStatusCache;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.actions.GitAction;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Delete action enabled for new local files and added but not yet committed files.
 */
public final class DeleteLocalAction extends GitAction {

    private static final Logger LOG = Logger.getLogger(DeleteLocalAction.class.getName());
    
    @Override
    protected boolean enable (Node[] nodes) {
        VCSContext context = GitUtils.getCurrentContext(nodes);
        return Git.getInstance().getFileStatusCache().containsFiles(context.getRootFiles(), EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), true);
    }
    
    @Override
    protected void performContextAction (final Node[] nodes) {
        final VCSContext context = GitUtils.getCurrentContext(nodes);
        FileStatusCache cache = Git.getInstance().getFileStatusCache();
        final Set<VCSFileProxy> files = context.getRootFiles();
        for (VCSFileProxy f : files) {
            FileInformation info = cache.getStatus(f);
            if (!info.containsStatus(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE))) {
                return;
            }
        }

        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(DeleteLocalAction.class, "CTL_DeleteLocal_Prompt")); // NOI18N
        descriptor.setTitle(NbBundle.getMessage(DeleteLocalAction.class, "CTL_DeleteLocal_Title")); // NOI18N
        descriptor.setMessageType(JOptionPane.WARNING_MESSAGE);
        descriptor.setOptionType(NotifyDescriptor.YES_NO_OPTION);

        Object res = DialogDisplayer.getDefault().notify(descriptor);
        if (res != NotifyDescriptor.YES_OPTION) {
            return;
        }
        
        GitProgressSupport support = new GitProgressSupport.NoOutputLogging() {
            @Override
            public void perform() {
                final Map<VCSFileProxy, Set<VCSFileProxy>> sortedFiles = GitUtils.sortByRepository(files);
                try {
                    GitUtils.runWithoutIndexing(new Callable<Void>() {
                        @Override
                        public Void call () throws Exception {
                            FileListener list = new FileListener() {
                                @Override
                                public void notifyFile (VCSFileProxy file, String relativePathToRoot) {
                                    setProgress(file.getName());
                                }
                            };
                            for (Map.Entry<VCSFileProxy, Set<VCSFileProxy>> e : sortedFiles.entrySet()) {
                                if (isCanceled()) {
                                    return null;
                                }
                                VCSFileProxy root = e.getKey();
                                GitClient client = null;
                                try {
                                    client = Git.getInstance().getClient(root);
                                    client.addNotificationListener(list);
                                    VCSFileProxy[] roots = e.getValue().toArray(new VCSFileProxy[e.getValue().size()]);
                                    client.reset(roots, GitUtils.HEAD, false, getProgressMonitor());
                                    client.clean(roots, getProgressMonitor());
                                } catch (GitException ex) {
                                    LOG.log(Level.INFO, null, ex);
                                } finally {
                                    if (client != null) {
                                        client.release();
                                    }
                                }
                            }
                            return null;
                        }
                    }, files.toArray(new VCSFileProxy[files.size()]));
                } catch (GitException ex) {
                    LOG.log(Level.INFO, null, ex);
                }
            }
        };
        support.start(Git.getInstance().getRequestProcessor(), null, NbBundle.getMessage(DeleteLocalAction.class, "LBL_DeleteLocalAction.progress")); //NOI18N
    }
    
}
