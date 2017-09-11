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
