/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.subversion.ui.export;

import java.io.File;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.CheckoutCompleted;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 *
 * @author Tomas Stupka
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
        
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
        
        Context ctx = getContext(nodes);

        final File[] roots = SvnUtils.getActionRoots(ctx);
        if(roots == null || roots.length != 1) return;
        File[] files = Subversion.getInstance().getStatusCache().listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);       
        
        File fromFile = roots[0];

        final RequestProcessor rp = createRequestProcessor(ctx);
        final boolean hasChanges = files.length > 0;
        final Export export = new Export(fromFile, hasChanges);
        if(export.showDialog()) {
            performExport(export, rp, nodes, roots);
        }
    }

    private void performExport(final Export export, final RequestProcessor rp, final Node[] nodes, final File[] roots) {
        rp.post(new Runnable() {
            @Override
            public void run() {
                ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(ExportAction.this, nodes) {
                    @Override
                    public void perform() {
                        File fromFile = export.getFromFile();
                        File toFile = export.getToFile();
                        toFile.mkdir();
                        if(isCanceled()) {
                            return;
                        }

                        SvnClient client;
                        try {
                            client = Subversion.getInstance().getClient(fromFile);
                            client.doExport (fromFile, toFile, true);
                        } catch (SVNClientException ex) {
                            SvnClientExceptionHandler.notifyException(ex, true, true); // should not happen
                            return;
                        }
                        if(export.getScanAfterExport()) {
                            CheckoutCompleted cc = new CheckoutCompleted(toFile, new String[] {"."});
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
