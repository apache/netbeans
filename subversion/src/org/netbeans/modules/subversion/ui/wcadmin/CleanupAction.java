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

package org.netbeans.modules.subversion.ui.wcadmin;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Ondra Vrabec
 */
@ActionID(id = "org.netbeans.modules.subversion.ui.wcadmin.CleanupAction", category = "Subversion")
@ActionRegistration(displayName = "CTL_Cleanup_Title")
public class CleanupAction extends ContextAction {

    @Override
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }

    @Override
    protected int getFileEnabledStatus() {
        return 0;
    }

    @Override
    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_Cleanup_Title"; //NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        final Context ctx = getContext(nodes);
        final File[] roots = ctx.getRootFiles();
        if (roots == null || roots.length == 0) {
            Subversion.LOG.log(Level.FINE, "No versioned folder in the selected context for {0}", nodes); //NOI18N
            return;
        }

        File root = roots[0];

        final SVNUrl repositoryUrl;
        SVNUrl repository = null;
        try {
            repository = SvnUtils.getRepositoryRootUrl(root);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, false, false);
        }
        repositoryUrl = repository;
        if(repositoryUrl == null) {
            Subversion.LOG.log(Level.WARNING, "Could not retrieve repository root for context file {0}", new Object[]{ root }); //NOI18N
        }
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
        SvnProgressSupport support = new SvnProgressSupport() {
            @Override
            protected void perform() {
                for (File root : roots) {
                    try {
                        SvnClient client = repositoryUrl == null ? Subversion.getInstance().getClient(false) : Subversion.getInstance().getClient(root);
                        setCancellableDelegate(client);
                        client.cleanup(root);
                    } catch (SVNClientException ex) {
                        annotate(ex);
                    } finally {
                        if (repositoryUrl != null) {
                            getLogger().getOpenOutputAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "")); //NOI18N
                        }
                    }
                }
            }
        };
        support.start(rp, repositoryUrl, NbBundle.getMessage(CleanupAction.class, "LBL_Cleanup_Progress")); //NOI18N
    }
}
