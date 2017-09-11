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

package org.netbeans.modules.subversion.ui.diff;

import java.awt.EventQueue;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ActionUtils;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Diff action between revisions
 *
 * @author Petr Kuzel
 */
public class DiffToAction extends ContextAction {

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_DiffTo";    // NOI18N
    }

    @Override
    protected boolean enable (Node[] nodes) {
        return super.enable(nodes) && SvnUtils.getActionRoots(getCachedContext(nodes), false) != null;
    }
    
    @Override
    protected int getFileEnabledStatus() {
        return getDirectoryEnabledStatus();
    }

    @Override
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED; 
    }
    
    @Override
    protected void performContextAction(final Node[] nodes) {
        SvnProgressSupport supp = new SvnProgressSupport() {
            @Override
            protected void perform() {
                if (!Subversion.getInstance().checkClientAvailable()) {
                    Logger.getLogger(DiffToAction.class.getName()).log(Level.FINE, "Client is unavailable, cannot perform Diff To"); //NOI18N
                    return;
                }
                if (isCanceled()) {
                    return;
                }
                final Context ctx = getContext(nodes);
                final String contextName = getContextDisplayName(nodes);
                SVNUrl repositoryUrl = null, fileUrl = null;
                File[] roots = SvnUtils.getActionRoots(ctx, false);
                File interestingFile = null;
                if (roots != null && roots.length > 0) {
                    try {
                        if(roots.length == 1) {
                            interestingFile = roots[0];
                        } else {
                            interestingFile = SvnUtils.getPrimaryFile(roots[0]);
                        }
                        repositoryUrl = SvnUtils.getRepositoryRootUrl(interestingFile);
                        fileUrl = SvnUtils.getRepositoryUrl(interestingFile);
                    } catch (SVNClientException ex) {
                        Logger.getLogger(DiffToAction.class.getName()).log(Level.INFO, null, ex);
                    }
                } else if (roots != null && roots.length == 0) {
                    Logger.getLogger(DiffToAction.class.getName()).log(Level.WARNING, "No cation roots for context: {0}", Arrays.asList(ctx.getRootFiles()));
                }
                if (repositoryUrl == null || fileUrl == null) {
                    Logger.getLogger(DiffToAction.class.getName()).log(Level.FINE,
                            "No repository URL: {0} or file URL: {1} for roots: {2}", new Object[] { //NOI18N
                                repositoryUrl, fileUrl, Arrays.asList(roots) });
                }
                final SelectDiffTree panel = new SelectDiffTree(new RepositoryFile(repositoryUrl, fileUrl, SVNRevision.HEAD), interestingFile);
                final SVNUrl fRepositoryUrl = repositoryUrl;
                final SVNUrl fFileUrl = fileUrl;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        if (!panel.showDialog()) {
                            return;
                        }
                        RepositoryFile left = panel.getRepositoryFile();
                        MultiDiffPanel panel = new MultiDiffPanel(ctx, -1, contextName, false,
                                fRepositoryUrl, fFileUrl, left,
                                new RepositoryFile(fRepositoryUrl, fFileUrl, SVNRevision.WORKING));
                        DiffTopComponent tc = new DiffTopComponent(panel);
                        tc.setName(NbBundle.getMessage(DiffAction.class, "CTL_DiffPanel_Title", contextName)); //NOI18N
                        tc.open();
                        tc.requestActive();
                    }
                });
            }
        };
        supp.start(Subversion.getInstance().getRequestProcessor(), null, ActionUtils.cutAmpersand(getRunningName(nodes)));
    }
}
