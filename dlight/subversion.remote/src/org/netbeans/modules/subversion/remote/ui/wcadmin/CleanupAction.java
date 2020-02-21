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

package org.netbeans.modules.subversion.remote.ui.wcadmin;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import org.netbeans.modules.subversion.remote.FileInformation;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.subversion.remote.ui.actions.ContextAction;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * 
 */
@ActionID(id = "org.netbeans.modules.subversion.remote.ui.wcadmin.CleanupAction", category = "SubversionRemote")
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
        final VCSFileProxy[] roots = ctx.getRootFiles();
        if (roots == null || roots.length == 0) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, "No versioned folder in the selected context for {0}", nodes); //NOI18N
            }
            return;
        }

        VCSFileProxy root = roots[0];

        final SVNUrl repositoryUrl;
        SVNUrl repository = null;
        try {
            repository = ContextAction.getSvnUrl(ctx);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ctx, ex, false, false);
        }
        repositoryUrl = repository;
        if(repositoryUrl == null) {
            Subversion.LOG.log(Level.WARNING, "Could not retrieve repository root for context file {0}", new Object[]{ root }); //NOI18N
        }
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
        SvnProgressSupport support = new SvnProgressSupport(VCSFileProxySupport.getFileSystem(root)) {
            @Override
            protected void perform() {
                for (VCSFileProxy root : roots) {
                    try {
                        SvnClient client = repositoryUrl == null ? Subversion.getInstance().getClient(false, new Context(root)) : Subversion.getInstance().getClient(root);
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
