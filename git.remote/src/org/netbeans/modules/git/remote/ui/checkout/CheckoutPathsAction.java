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

package org.netbeans.modules.git.remote.ui.checkout;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.progress.FileListener;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.actions.GitAction;
import org.netbeans.modules.git.remote.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.checkout.CheckoutPathsAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_CheckoutPathsAction_Name")
public class CheckoutPathsAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(CheckoutPathsAction.class.getName());

    @Override
    protected void performAction (VCSFileProxy repository, VCSFileProxy[] roots, VCSContext context) {
        final CheckoutPaths checkout = new CheckoutPaths(repository, roots);
        if (checkout.show()) {
            checkoutFiles(repository, roots, checkout.getRevision());
        }
    }
    
    public void checkoutFiles (final VCSFileProxy repository, final VCSFileProxy[] roots, final String revision) {
        GitProgressSupport supp = new GitProgressSupport() {

            @Override
            protected void perform () {
                final Collection<VCSFileProxy> notifiedFiles = new HashSet<>();
                try {
                    final GitClient client = getClient();
                    client.addNotificationListener(new FileListener() {
                        @Override
                        public void notifyFile (VCSFileProxy file, String relativePathToRoot) {
                            notifiedFiles.add(file);
                        }
                    });
                    client.addNotificationListener(new DefaultFileListener(roots));
                    final VCSFileProxy[][] split = VCSFileProxySupport.splitFlatOthers(roots);
                    GitUtils.runWithoutIndexing(new Callable<Void>() {

                        @Override
                        public Void call () throws Exception {
                            for (int c = 0; c < split.length; c++) {
                                VCSFileProxy[] splitRoots = split[c];
                                if (splitRoots.length == 0) {
                                    continue;
                                }
                                if (c == 1) {
                                    // recursive
                                    LOG.log(Level.FINE, "Checking out paths recursively, revision: {0}", revision); //NOI18N
                                    client.checkout(splitRoots, revision, true, getProgressMonitor());
                                } else {
                                    // not recursive, list only direct descendants
                                    LOG.log(Level.FINE, "Checking out paths non-recursively, revision: {0}", revision); //NOI18N
                                    client.checkout(splitRoots, revision, false, getProgressMonitor());
                                }
                            }
                            return null;
                        }
                    }, roots);
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                } finally {
                    if (!notifiedFiles.isEmpty()) {
                        setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                        Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.singletonMap(getRepositoryRoot(), notifiedFiles));
                    }
                }
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(CheckoutPathsAction.class, "LBL_CheckoutPaths.progressName"));
    }

}
