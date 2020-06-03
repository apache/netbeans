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

package org.netbeans.modules.subversion.remote.ui.copy;

import java.util.concurrent.Callable;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.subversion.remote.FileInformation;
import org.netbeans.modules.subversion.remote.RepositoryFile;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.ISVNLogMessage;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.subversion.remote.ui.actions.ContextAction;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.nodes.Node;

/**
 *
 * 
 */
public class MergeAction extends ContextAction {

    public MergeAction() {        
    }

    @Override
    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Merge";    // NOI18N        
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
        return nodes != null && nodes.length == 1 && isCacheReady() && getCachedContext(nodes).getRoots().size() > 0;
    }   
    
    @Override
    protected void performContextAction(final Node[] nodes) {
        Context ctx = getContext(nodes);        
        if(!Subversion.getInstance().checkClientAvailable(ctx)) {            
            return;
        }
        final VCSFileProxy[] roots = SvnUtils.getActionRoots(ctx);
        if(roots == null || roots.length == 0) {
            return;
        }

        VCSFileProxy interestingFile;
        if(roots.length == 1) {
            interestingFile = roots[0];
        } else {
            interestingFile = SvnUtils.getPrimaryFile(roots[0]);
        }

        SVNUrl rootUrl;
        SVNUrl url;
        try {            
            rootUrl = ContextAction.getSvnUrl(ctx);
            if (rootUrl == null) {
                SvnClientExceptionHandler.notifyNullUrl(ctx);
                return; // otherwise NPE, see #267975
            }
            url = SvnUtils.getRepositoryUrl(interestingFile);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ctx, ex, true, true);
            return;
        }           
        final RepositoryFile repositoryRoot = new RepositoryFile(ctx.getFileSystem(), rootUrl, url, SVNRevision.HEAD);

        final Merge merge = new Merge(repositoryRoot, interestingFile);
        if(merge.showDialog()) {
            ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, nodes, ctx) {
                @Override
                public void perform() {
                    for (VCSFileProxy root : roots) {
                        performMerge(merge, repositoryRoot, root, this, roots.length > 1);
                    }
                }
            };
            support.start(createRequestProcessor(ctx));
        }        
    }

    /**
     * Merges changes from the remote url(s) and revisions given bet the Merge controller with
     * the given file
     * @param merge
     * @param repositoryRoot
     * @param file
     * @param support
     * @param partOfMultiFile 
     */
    private void performMerge(final Merge merge, RepositoryFile repositoryRoot, final VCSFileProxy file, SvnProgressSupport support, boolean partOfMultiFile) {
        VCSFileProxy[][] split = VCSFileProxySupport.splitFlatOthers(new VCSFileProxy[] {file} );
        final boolean recursive;
        // there can be only 1 root file
        if(split[0].length > 0) {
            recursive = false;
        } else {
            recursive = true;
        }                

        try {
            final SvnClient client;
            final Context context = new Context(file);
            try {
                client = Subversion.getInstance().getClient(context, repositoryRoot.getRepositoryUrl());
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(context, ex, true, true);
                return;
            }

            if(support.isCanceled()) {
                return;
            }
            
            SVNUrl endUrl = merge.getMergeEndRepositoryFile().getFileUrl();
            final SVNRevision endRevision = merge.getMergeEndRevision();

            final RepositoryFile mergeStartRepositoryFile = merge.getMergeStartRepositoryFile();
            SVNUrl startUrl = mergeStartRepositoryFile != null ? mergeStartRepositoryFile.getFileUrl() : null;
            if (file.isFile() && partOfMultiFile) {
                // change the filename ONLY for multi-file data objects, not for folders
                endUrl = merge.getMergeEndRepositoryFile().replaceLastSegment(file.getName(), 0).getFileUrl();
                startUrl = mergeStartRepositoryFile != null ? mergeStartRepositoryFile.replaceLastSegment(file.getName(), 0).getFileUrl() : null;
            }
            
            SVNRevision startRevision;
            if(startUrl != null) {                
                startRevision = merge.getMergeStartRevision();
                if (merge.isStartRevisionIncluded()) {
                    if (startRevision.getKind() == SVNRevision.Kind.number) {
                        startRevision = new SVNRevision.Number(((SVNRevision.Number) startRevision).getNumber() - 1);
                    } else {
                        ISVNInfo info = client.getInfo(context, startUrl, startRevision, startRevision);
                        if (info != null) {
                            startRevision = new SVNRevision.Number(info.getRevision().getNumber() - 1);
                        }
                    }
                }
            } else {
                // XXX is this the only way we can do it?
                startUrl = endUrl;
                ISVNLogMessage[] log = client.getLogMessages(startUrl, null, new SVNRevision.Number(0), SVNRevision.HEAD, true, false, 0L);
                startRevision = log[0].getRevision();
            }                        
            if(support.isCanceled()) {
                return;
            }
            final SVNUrl fStartUrl = startUrl;
            final SVNUrl fEndUrl = endUrl;
            final SVNRevision fStartRevision = startRevision;
            SvnUtils.runWithoutIndexing(new Callable<Void>() {
                @Override
                public Void call () throws Exception {
                    if (endRevision == null) {
                        client.mergeReintegrate(fEndUrl, SVNRevision.HEAD, file, false, false);
                    } else {
                        client.merge(fStartUrl,
                                 fStartRevision,
                                 fEndUrl,
                                 endRevision,
                                 file,
                                 false,
                                 recursive,
                                 false,
                                 merge.isIgnoreAncestry());
                    }
                    return null;
                }
            }, file);

        } catch (SVNClientException ex) {
            support.annotate(ex);
        }
    }
}
