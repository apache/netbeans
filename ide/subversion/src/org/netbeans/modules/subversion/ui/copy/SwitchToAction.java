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

package org.netbeans.modules.subversion.ui.copy;

import java.awt.EventQueue;
import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Petr Kuzel
 */
public class SwitchToAction extends ContextAction {
    
    /**
     * Creates a new instance of SwitchToAction
     */
    public SwitchToAction() {        
    }

    @Override
    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Switch";    // NOI18N        
    }

    @Override
    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }

    @Override
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        return super.enable(nodes) && nodes.length == 1;
    }

    @Override
    protected void performContextAction(final Node[] nodes) {
        
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
        
        Context ctx = getContext(nodes);        

        File[] roots = SvnUtils.getActionRoots(ctx);
        if(roots == null || roots.length == 0) return;

        File interestingFile;
        if(roots.length == 1) {
            interestingFile = roots[0];
        } else {
            interestingFile = SvnUtils.getPrimaryFile(roots[0]);
        }

        SVNUrl rootUrl = null, fileUrl = null;
        try {            
            rootUrl = SvnUtils.getRepositoryRootUrl(interestingFile);
            fileUrl = SvnUtils.getRepositoryUrl(interestingFile);
        } catch (SVNClientException ex) {
            if (rootUrl == null) {
                SvnClientExceptionHandler.notifyException(ex, true, true);
                return;
            }
        }
        final RepositoryFile repositoryFile = new RepositoryFile(rootUrl, fileUrl == null ? rootUrl : fileUrl, SVNRevision.HEAD);
        boolean hasChanges = Subversion.getInstance().getStatusCache().containsFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE, true);

        final RequestProcessor rp = createRequestProcessor(ctx);

        final SwitchTo switchTo = new SwitchTo(repositoryFile, interestingFile, hasChanges);
                
        performSwitch(switchTo, rp, nodes, roots);
    }

    /**
     * Switches all files from the roots array to their respective urls given by the SwitchTo controller
     * @param switchTo
     * @param rp
     * @param nodes
     * @param roots
     */
    private void performSwitch(final SwitchTo switchTo, final RequestProcessor rp, final Node[] nodes, final File[] roots) {
        if(!switchTo.showDialog()) {
           return;
        }
        rp.post(new Runnable() {
            @Override
            public void run() {
                if(!validateInput(roots[0], switchTo.getRepositoryFile())) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            performSwitch(switchTo, rp, nodes, roots);
                        }
                    });
                } else {
                    ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(SwitchToAction.this, nodes) {
                        @Override
                        public void perform() {
                            final ContextAction.ProgressSupport supp = this;
                            try {
                                SvnUtils.runWithoutIndexing(new Callable<Void>() {
                                    @Override
                                    public Void call () throws Exception {
                                        for (File root : roots) {
                                            RepositoryFile toRepositoryFile = switchTo.getRepositoryFile();
                                            if (root.isFile() && roots.length > 1) {
                                                // change the filename ONLY for multi-file data objects, not for folders
                                                toRepositoryFile = toRepositoryFile.replaceLastSegment(root.getName(), 0);
                                            }
                                            performSwitch(toRepositoryFile, root, supp);
                                        }
                                        return null;
                                    }
                                }, roots);
                            } catch (SVNClientException ex) {
                                SvnClientExceptionHandler.notifyException(ex, true, false);
                            }
                        }
                    };
                    support.start(rp);                            
                }
            }
        });
    }
    
    private boolean validateInput(File root, RepositoryFile toRepositoryFile) {
        boolean ret = false;
        SvnClient client;
        try {                   
            client = Subversion.getInstance().getClient(toRepositoryFile.getRepositoryUrl());
            ISVNInfo info = client.getInfo(toRepositoryFile.getFileUrl());
            if(info.getNodeKind() == SVNNodeKind.DIR && root.isFile()) {
                SvnClientExceptionHandler.annotate(NbBundle.getMessage(SwitchToAction.class, "LBL_SwitchFileToFolderError"));
                ret = false;
            } else if(info.getNodeKind() == SVNNodeKind.FILE && root.isDirectory()) {
                SvnClientExceptionHandler.annotate(NbBundle.getMessage(SwitchToAction.class, "LBL_SwitchFolderToFileError"));
                ret = false;
            } else {
                ret = true;
            }
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return ret;
        }                            
        return ret;
    }        

    static void performSwitch(final RepositoryFile toRepositoryFile, final File root, final SvnProgressSupport support) {
        File[][] split = Utils.splitFlatOthers(new File[] {root} );
        boolean recursive;
        // there can be only 1 root file
        if(split[0].length > 0) {
            recursive = false;
        } else {
            recursive = true;
        }

        try {
            SvnClient client;
            try {
                client = Subversion.getInstance().getClient(toRepositoryFile.getRepositoryUrl());
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, true, true);
                return;
            }
            // ... and switch
            client.switchToUrl(root, toRepositoryFile.getFileUrl(), toRepositoryFile.getRevision(), recursive);
            // the client doesn't notify as there is no output rom the cli. Lets emulate onNotify as from the client
            List<File> switchedFiles = SvnUtils.listManagedRecursively(root);
            File[] fileArray = switchedFiles.toArray(new File[0]);
            // the cache fires status change events to trigger the annotation refresh
            // unfortunatelly - we have to call the refresh explicitly for each file also
            // from this place as the branch label was changed evern if the files status didn't
            Subversion.getInstance().getStatusCache().getLabelsCache().flushFileLabels(fileArray);
            Subversion.getInstance().refreshAnnotations(fileArray);
            // refresh the inline diff
            Subversion.getInstance().versionedFilesChanged();
        } catch (SVNClientException ex) {
            support.annotate(ex);
        }
    }
}
