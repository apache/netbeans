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

package org.netbeans.modules.subversion.remote.ui.diff;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.subversion.remote.FileInformation;
import org.netbeans.modules.subversion.remote.RepositoryFile;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.subversion.remote.ui.actions.ActionUtils;
import org.netbeans.modules.subversion.remote.ui.actions.ContextAction;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Diff action between revisions
 *
 * 
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
        final Context ctx = getContext(nodes);
        SvnProgressSupport supp = new SvnProgressSupport(ctx.getFileSystem()) {
            @Override
            protected void perform() {
                if (isCanceled()) {
                    return;
                }
                if (!Subversion.getInstance().checkClientAvailable(ctx)) {
                    Logger.getLogger(DiffToAction.class.getName()).log(Level.FINE, "Client is unavailable, cannot perform Diff To"); //NOI18N
                    return;
                }
                final String contextName = getContextDisplayName(nodes);
                SVNUrl repositoryUrl = null, fileUrl = null;
                VCSFileProxy[] roots = SvnUtils.getActionRoots(ctx, false);
                VCSFileProxy interestingFile = null;
                if (roots != null) {
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
                }
                if (repositoryUrl == null || fileUrl == null) {
                    Logger.getLogger(DiffToAction.class.getName()).log(Level.FINE,
                            "No repository URL: {0} or file URL: {1} for roots: {2}", new Object[] { //NOI18N
                                repositoryUrl, fileUrl, Arrays.asList(roots) });
                }
                final SelectDiffTree panel = new SelectDiffTree(new RepositoryFile(ctx.getFileSystem(), repositoryUrl, fileUrl, SVNRevision.HEAD), interestingFile);
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
                                new RepositoryFile(ctx.getFileSystem(), fRepositoryUrl, fFileUrl, SVNRevision.WORKING));
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
