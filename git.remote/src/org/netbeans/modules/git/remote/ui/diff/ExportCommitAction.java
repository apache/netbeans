/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.git.remote.ui.diff;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.GitModuleConfig;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.remotefs.versioning.api.ExportDiffSupport;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.diff.ExportCommitAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_ExportCommitAction_Name")
public class ExportCommitAction extends SingleRepositoryAction {

    @Override
    protected void performAction (VCSFileProxy repository, VCSFileProxy[] roots, VCSContext context) {
        exportCommit(repository, GitUtils.HEAD);
    }
    
    public void exportCommit (final VCSFileProxy repository, final String preselectedRevision) {
        Mutex.EVENT.readAccess(new Runnable () {
            @Override
            public void run () {
                ExportDiffSupport exportDiffSupport = new ExportCommit(repository, preselectedRevision) {
                    @Override
                    public void writeDiffFile (final VCSFileProxy toFile) {
                        final String revStr = getSelectionRevision();
                        saveFolderToPrefs(toFile);
                        GitProgressSupport support = new GitProgressSupport() {
                            @Override
                            public void perform() {
                                boolean success = false;
                                    OutputStream out = null;
                                    try {
                                        GitClient client = getClient();
                                        ensureParentExists(toFile);
                                        out = new BufferedOutputStream(VCSFileProxySupport.getOutputStream(toFile));
                                        client.addNotificationListener(new DefaultFileListener(new VCSFileProxy[0]));
                                        setProgress(NbBundle.getMessage(ExportUncommittedChangesAction.class, "MSG_ExportCommitAction.preparingDiff")); //NOI18N
                                        client.exportCommit(revStr, out, getProgressMonitor());
                                        if (!isCanceled()) {
                                            success = true;
                                        }
                                    } catch (Exception ex) {
                                        GitClientExceptionHandler.notifyException(ex, true);
                                    } finally {
                                        if (out != null) {
                                            try {
                                                out.flush();
                                                out.close();
                                            } catch (IOException ex) { }
                                        }
                                        if (success && VCSFileProxySupport.length(toFile) > 0) {
                                            VCSFileProxySupport.openFile(toFile);
                                        } else {
                                            VCSFileProxySupport.delete(toFile);
                                        }
                                    }
                            }
                        };
                        support.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(ExportCommitAction.class, "LBL_ExportCommitAction_Progress")).waitFinished(); //NOI18N
                    }
                };
                exportDiffSupport.export();
            }
        });
    }
    
    private static void saveFolderToPrefs (final VCSFileProxy file) {
        if (file.getParentFile() != null) {
            GitModuleConfig.getDefault().getPreferences().put("ExportDiff.saveFolder", file.getParentFile().getPath()); //NOI18N
        }
    }
    
    private static void ensureParentExists(VCSFileProxy destination) {
        VCSFileProxy parent = destination.getParentFile();
        if (parent != null) {
            VCSFileProxySupport.mkdirs(parent);
        }
    }
}
