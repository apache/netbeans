/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.git.ui.commit;

import java.io.File;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
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
    protected boolean enableFull (Node[] nodes) {
        VCSContext context = GitUtils.getCurrentContext(nodes);
        return Git.getInstance().getFileStatusCache().containsFiles(context.getRootFiles(), EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE), true);
    }
    
    @Override
    protected void performContextAction (final Node[] nodes) {
        final VCSContext context = GitUtils.getCurrentContext(nodes);
        FileStatusCache cache = Git.getInstance().getFileStatusCache();
        final Set<File> files = context.getRootFiles();
        for (File f : files) {
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
                final Map<File, Set<File>> sortedFiles = GitUtils.sortByRepository(files);
                try {
                    GitUtils.runWithoutIndexing(new Callable<Void>() {
                        @Override
                        public Void call () throws Exception {
                            FileListener list = new FileListener() {
                                @Override
                                public void notifyFile (File file, String relativePathToRoot) {
                                    setProgress(file.getName());
                                }
                            };
                            for (Map.Entry<File, Set<File>> e : sortedFiles.entrySet()) {
                                if (isCanceled()) {
                                    return null;
                                }
                                File root = e.getKey();
                                GitClient client = null;
                                try {
                                    client = Git.getInstance().getClient(root);
                                    client.addNotificationListener(list);
                                    File[] roots = e.getValue().toArray(new File[e.getValue().size()]);
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
                    }, files.toArray(new File[files.size()]));
                } catch (GitException ex) {
                    LOG.log(Level.INFO, null, ex);
                }
            }
        };
        support.start(Git.getInstance().getRequestProcessor(), null, NbBundle.getMessage(DeleteLocalAction.class, "LBL_DeleteLocalAction.progress")); //NOI18N
    }
    
}
