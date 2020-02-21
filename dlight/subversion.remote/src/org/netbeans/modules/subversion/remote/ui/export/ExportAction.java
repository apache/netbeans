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

package org.netbeans.modules.subversion.remote.ui.export;

import org.netbeans.modules.subversion.remote.FileInformation;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.ui.actions.ContextAction;
import org.netbeans.modules.subversion.remote.util.CheckoutCompleted;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 * 
 */
public class ExportAction extends ContextAction {
    
    public ExportAction() { }

    @Override
    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Export";    // NOI18N
    }

    @Override
    protected int getFileEnabledStatus() {
        return    FileInformation.STATUS_MANAGED
               & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    @Override
    protected int getDirectoryEnabledStatus() {
        return    FileInformation.STATUS_MANAGED 
               & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
               & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }

    @Override
    protected boolean enable(Node[] nodes) {
        return nodes != null && nodes.length == 1 && isCacheReady() && getCachedContext(nodes).getRoots().size() > 0;
    }   

    @Override    
    protected void performContextAction(final Node[] nodes) {
        Context ctx = getContext(nodes);
        if(!Subversion.getInstance().checkClientAvailable(ctx)) {            
            return;
        }
        final VCSFileProxy[] roots = SvnUtils.getActionRoots(ctx);
        if(roots == null || roots.length != 1) {
            return;
        }
        VCSFileProxy[] files = Subversion.getInstance().getStatusCache().listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);       
        
        VCSFileProxy fromFile = roots[0];

        final RequestProcessor rp = createRequestProcessor(ctx);
        final boolean hasChanges = files.length > 0;
        final Export export = new Export(fromFile, hasChanges);
        if(export.showDialog()) {
            performExport(export, rp, nodes, roots);
        }
    }

    private void performExport(final Export export, final RequestProcessor rp, final Node[] nodes, final VCSFileProxy[] roots) {
        rp.post(new Runnable() {
            @Override
            public void run() {
                ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(ExportAction.this, nodes, getCachedContext(nodes)) {
                    @Override
                    public void perform() {
                        VCSFileProxy fromFile = export.getFromFile();
                        VCSFileProxy toFile = export.getToFile();
                        VCSFileProxySupport.mkdir(toFile);
                        if(isCanceled()) {
                            return;
                        }

                        SvnClient client;
                        try {
                            client = Subversion.getInstance().getClient(fromFile);
                            client.doExport (fromFile, toFile, true);
                        } catch (SVNClientException ex) {
                            SvnClientExceptionHandler.notifyException(new Context(fromFile), ex, true, true); // should not happen
                            return;
                        }
                        if(export.getScanAfterExport()) {
                            CheckoutCompleted cc = new CheckoutCompleted(toFile, new String[] {"."}); //NOI18N
                            if (isCanceled()) {
                                return;
                            }
                            cc.scanForProjects(this, CheckoutCompleted.Type.EXPORT);
                        }
                    }
                };
                support.start(rp);
            }
        });
    }

}
