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
                            client.add(toAdd.toArray(new File[toAdd.size()]), getProgressMonitor());
                        }
                        if (!toRemove.isEmpty()) {
                            client.remove(toRemove.toArray(new File[toRemove.size()]), true, getProgressMonitor());
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
