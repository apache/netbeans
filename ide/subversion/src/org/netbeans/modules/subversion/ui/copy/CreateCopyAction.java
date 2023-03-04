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

package org.netbeans.modules.subversion.ui.copy;

import java.awt.EventQueue;
import java.io.File;
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
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class CreateCopyAction extends ContextAction {
    
    /** Creates a new instance of CreateCopyAction */
    public CreateCopyAction() {
        
    }

    @Override
    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Copy";    // NOI18N
    }

    @Override
    protected int getFileEnabledStatus() {
        return    FileInformation.STATUS_MANAGED
               & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY
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
        if(roots == null || roots.length == 0) return;
        File[] files = Subversion.getInstance().getStatusCache().listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);       
        
        File interestingFile;
        if(roots.length == 1) {
            interestingFile = roots[0];
        } else {
            interestingFile = SvnUtils.getPrimaryFile(roots[0]);
        }

        final SVNUrl repositoryUrl; 
        final SVNUrl fileUrl;        
        try {            
            repositoryUrl = SvnUtils.getRepositoryRootUrl(interestingFile); // XXX
            fileUrl = SvnUtils.getRepositoryUrl(interestingFile);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }                   
        final RepositoryFile repositoryFile = new RepositoryFile(repositoryUrl, fileUrl, SVNRevision.HEAD);        

        final RequestProcessor rp = createRequestProcessor(ctx);
        final boolean hasChanges = files.length > 0;
        final CreateCopy createCopy = new CreateCopy(repositoryFile, interestingFile, hasChanges);

        performCopy(createCopy, rp, nodes, roots);
    }

    private void performCopy(final CreateCopy createCopy, final RequestProcessor rp, final Node[] nodes, final File[] roots) {
        if (!createCopy.showDialog()) {
            return;
        }
        rp.post(new Runnable() {
            @Override
            public void run() {
                String errorText = validateTargetPath(createCopy);
                if (errorText == null) {
                    ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(CreateCopyAction.this, nodes) {
                        @Override
                        public void perform() {
                            performCopy(createCopy, this, roots);
                        }
                    };
                    support.start(rp);
                } else {
                    SvnClientExceptionHandler.annotate(errorText);
                    createCopy.setErrorText(errorText);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            performCopy(createCopy, rp, nodes, roots);
                        }
                    });
                }
            }
        });
    }

    private String validateTargetPath(CreateCopy createCopy) {
        String errorText = null;
        try {
            RepositoryFile toRepositoryFile = createCopy.getToRepositoryFile();
            SvnClient client = Subversion.getInstance().getClient(toRepositoryFile.getRepositoryUrl());
            ISVNInfo info = null;
            try {
                info = client.getInfo(toRepositoryFile.getFileUrl());
            } catch (SVNClientException e) {
                if (!SvnClientExceptionHandler.isWrongUrl(e.getMessage())) {
                    throw e;
                }
            }
            if (info != null) {
                errorText = NbBundle.getMessage(CreateCopyAction.class, "MSG_CreateCopy_Target_Exists");     // NOI18N
            }
        } catch (SVNClientException ex) {
            errorText = null;
        }
        return errorText;
    }

    /**
     * Performs a copy given by the CreateCopy controller. If a local file has to copied
     * and there is more then one file in roots then a copy is created for each one of them.
     *
     * @param createCopy
     * @param support
     * @param roots
     */
    private void performCopy(final CreateCopy createCopy, final SvnProgressSupport support, final File[] roots) {
        if (roots == null) {
            return;
        }
        final RepositoryFile toRepositoryFile = createCopy.getToRepositoryFile();                

        try {                
            SvnClient client;
            try {
                client = Subversion.getInstance().getClient(toRepositoryFile.getRepositoryUrl());
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, true, true);
                return;
            }

            if(support.isCanceled()) {
                return;
            }

            if(createCopy.isLocal()) {
                if(roots.length == 1) {
                    client.copy(new File[] {createCopy.getLocalFile()}, toRepositoryFile.getFileUrl(), createCopy.getMessage(), true, true);
                } else {
                    // more roots => copying a multifile dataobject - see getActionRoots(ctx)
                    for (File root : roots) {
                        SVNUrl toUrl = getToRepositoryFile(toRepositoryFile, root).getFileUrl();
                        client.copy(new File[] {root}, toUrl, createCopy.getMessage(), true, true);
                    }
                }
            } else {
                if(roots.length == 1) {
                    RepositoryFile fromRepositoryFile = createCopy.getFromRepositoryFile();
                    client.copy(fromRepositoryFile.getFileUrl(),
                            toRepositoryFile.getFileUrl(),
                            createCopy.getMessage(),
                            fromRepositoryFile.getRevision(), true);
                } else {
                    // more roots => copying a multifile dataobject - see getActionRoots(ctx)
                    for (File root : roots) {
                        SVNUrl fromUrl = SvnUtils.getRepositoryRootUrl(root).appendPath(SvnUtils.getRepositoryPath(root));
                        SVNUrl toUrl = getToRepositoryFile(toRepositoryFile, root).getFileUrl();
                        client.copy(fromUrl, toUrl, createCopy.getMessage(), SVNRevision.HEAD, true);
                    }
                }
            }                            
            
            if(support.isCanceled()) {
                return;
            }

            if(createCopy.switchTo()) {
                final boolean rootsPresent = roots.length > 1;
                File[] indexingRoots = rootsPresent ? roots : new File[] { createCopy.getLocalFile() };
                SvnUtils.runWithoutIndexing(new Callable<Void>() {
                    @Override
                    public Void call () throws Exception {
                        if (rootsPresent) {
                            // more roots menas we copyied a multifile dataobject - see getActionRoots(ctx)
                            // lets also switch all of them
                            for (File file : roots) {
                                SwitchToAction.performSwitch(getToRepositoryFile(toRepositoryFile, file), file, support);
                            }
                        } else {
                            SwitchToAction.performSwitch(toRepositoryFile, createCopy.getLocalFile(), support);
                        }
                        return null;
                    }
                }, indexingRoots);
            }            
        } catch (SVNClientException ex) {
            support.annotate(ex);
        }
    }

    private RepositoryFile getToRepositoryFile(RepositoryFile toRepositoryFile, File file) {
        return toRepositoryFile.replaceLastSegment(file.getName(), 0);
    }
}
