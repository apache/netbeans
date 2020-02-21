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

package org.netbeans.modules.subversion.remote.ui.update;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.netbeans.modules.subversion.remote.FileInformation;
import org.netbeans.modules.subversion.remote.FileStatusCache;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.ISVNNotifyListener;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNNodeKind;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.subversion.remote.ui.actions.ActionUtils;
import org.netbeans.modules.subversion.remote.ui.actions.ContextAction;
import org.netbeans.modules.subversion.remote.util.ClientCheckSupport;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VersioningOutputManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.awt.StatusDisplayer;
import org.openide.util.RequestProcessor;

/**
 * Update action
 *
 * 
 */ 
public class UpdateAction extends ContextAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/subversion/remote/resources/icons/update.png"; //NOI18N
    private static final int STATUS_RECURSIVELY_TRAVERSIBLE = FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;

    public UpdateAction () {
        this(ICON_RESOURCE);
    }

    protected UpdateAction (String iconResource) {
        super(iconResource);
    }
    
    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Update";    // NOI18N
    }

    @Override
    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_VERSIONED | FileInformation.STATUS_IN_REPOSITORY
                | FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY; // updating locally new file is permitted, it either does nothing or exchanges the local file with the one in repository
    }

    @Override
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED 
             & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
             & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected void performContextAction(final Node[] nodes) {
        ClientCheckSupport.getInstance().runInAWTIfAvailable(nodes, ActionUtils.cutAmpersand(getRunningName(nodes)), new Runnable() {
            @Override
            public void run() {
                performUpdate(nodes);
            }
        });
    }

    void performUpdate(final Node[] nodes) {
        // FIXME add shalow logic allowing to ignore nested projects
        // look into CVS, it's very tricky:
        // project1/
        //   nbbuild/  (project1)
        //   project2/
        //   src/ (project1)
        //   test/ (project1 but imagine it's in repository, to be updated )
        // Is there a way how to update project1 without updating project2?
        final Context ctx = getContext(nodes);
        if (ctx.getRootFiles().length == 0) {
            Subversion.LOG.info("UpdateAction.performUpdate: context is empty, some files may be unversioned."); //NOI18N
            return;
        }
        final SVNRevision revision = getRevision(ctx);
        if (revision == null) {
            return;
        }
        final ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this, nodes, ctx) {
            @Override
            public void perform() {
                update(ctx, this, getContextDisplayName(nodes), revision);
            }
        };                    
        Utils.post(new Runnable() {
            @Override
            public void run () {
                support.start(createRequestProcessor(ctx));
            }
        });
    }

    protected SVNRevision getRevision (Context ctx) {
        return SVNRevision.HEAD;
    }

    private static void update(Context ctx, SvnProgressSupport progress, String contextDisplayName, SVNRevision revision) {
               
        VCSFileProxy[] roots = ctx.getRootFiles();
        
        SVNUrl repositoryUrl = null;
        try {
            for (VCSFileProxy root : roots) {
                repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
                if(repositoryUrl != null) {
                    break;
                } else {
                    Subversion.LOG.log(Level.WARNING, "Could not retrieve repository root for context file {0}", new Object[]{root});
                }
            }
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ctx, ex, true, true);
            return;
        }        
        if (repositoryUrl == null) {
            return;
        }

        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        cache.refreshCached(ctx);
        update(roots, progress, contextDisplayName, repositoryUrl, revision);
    }

    private static void update(VCSFileProxy[] roots, final SvnProgressSupport progress, String contextDisplayName, SVNUrl repositoryUrl, final SVNRevision revision) {
        VCSFileProxy[][] split = VCSFileProxySupport.splitFlatOthers(roots);
        final List<VCSFileProxy> recursiveFiles = new ArrayList<>();
        final List<VCSFileProxy> flatFiles = new ArrayList<>();
        
        // recursive files
        for (int i = 0; i<split[1].length; i++) {
            recursiveFiles.add(split[1][i]);
        }        
        // flat files
        //File[] flatRoots = SvnUtils.flatten(split[0], getDirectoryEnabledStatus());
        for (int i= 0; i<split[0].length; i++) {            
            flatFiles.add(split[0][i]);
        }
                        
        
        final SvnClient client;
        final Context context = new Context(roots); 
        UpdateOutputListener listener = new UpdateOutputListener(context);
        try {
            client = Subversion.getInstance().getClient(context, repositoryUrl); 
            // this isn't clean - the client notifies only files which realy were updated. 
            // The problem here is that the revision in the metadata is set to HEAD even if the file didn't change =>
            // we have to explicitly force the refresh for the relevant context - see bellow in updateRoots
            client.removeNotifyListener(Subversion.getInstance().getRefreshHandler());
            client.addNotifyListener(listener);
            client.addNotifyListener(progress);
            progress.setCancellableDelegate(client);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(context, ex, true, true);
            return;
        }

        try {                    
            UpdateNotifyListener l = new UpdateNotifyListener(context);            
            client.addNotifyListener(l);            
            try {
                SvnUtils.runWithoutIndexing(new Callable<Void>() {
                    @Override
                    public Void call () throws Exception {
                        updateRoots(recursiveFiles, progress, client, true, revision);
                        if(progress.isCanceled()) {
                            return null;
                        }
                        updateRoots(flatFiles, progress, client, false, revision);
                        return null;
                    }
                }, roots);
                if(progress.isCanceled()) {
                    return;
                }
            } finally {
                client.removeNotifyListener(l);
                client.removeNotifyListener(progress);
            }
            if (!l.existedFiles.isEmpty() || !l.conflictedFiles.isEmpty()) {
                // status of replaced files should be refreshed
                // because locally added files can be replaced with those in repository and their status would be still the same in the cache
                HashSet<VCSFileProxy> filesToRefresh = new HashSet<>(l.existedFiles);
                filesToRefresh.addAll(l.conflictedFiles);
                Subversion.getInstance().getStatusCache().refreshAsync(filesToRefresh.toArray(new VCSFileProxy[filesToRefresh.size()]));
            }
            if (!l.conflictedFiles.isEmpty()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(
                                org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_UpdateCausedConflicts_Prompt"), //NOI18N
                                NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                });
            } else {
                StatusDisplayer.getDefault().setStatusText(org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Update_Completed")); // NOI18N
            }
        } catch (SVNClientException e1) {
            progress.annotate(e1);
        } finally {            
            openResults(listener.getResults(), repositoryUrl, contextDisplayName);                
        }
    }

    private static void openResults(final List<FileUpdateInfo> resultsList, final SVNUrl url, final String contextDisplayName) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                UpdateResults results = new UpdateResults(resultsList, url, contextDisplayName);
                VersioningOutputManager vom = VersioningOutputManager.getInstance();
                vom.addComponent(SvnUtils.decodeToString(url) + "-UpdateExecutor", results); // NOI18N
            }
        });
    }
    
    private static void updateRoots(List<VCSFileProxy> roots, SvnProgressSupport support, SvnClient client, boolean recursive, SVNRevision revision) throws SVNClientException {
        for (Iterator<VCSFileProxy> it = roots.iterator(); it.hasNext();) {
            VCSFileProxy root = it.next();
            if(support.isCanceled()) {
                break;
            }
            long rev = client.update(root, revision == null ? SVNRevision.HEAD : revision, recursive);
            revisionUpdateWorkaround(recursive, root.normalizeFile(), client, rev);
        }
    }

    private static void revisionUpdateWorkaround(final boolean recursive, final VCSFileProxy root, final SvnClient client, final long revision) throws SVNClientException {
        Utils.post(new Runnable() {
            @Override
            public void run() {
                SVNRevision.Number svnRevision = null;
                if(revision < -1) {
                    ISVNInfo info = null;
                    try {
                        info = SvnUtils.getInfoFromWorkingCopy(client, root); // try to retrieve from local WC first
                        svnRevision = info.getRevision();
                        if(svnRevision == null) {
                            info = client.getInfo(root); // contacts the server
                            svnRevision = info.getRevision();
                        }
                    } catch (SVNClientException ex) {
                        SvnClientExceptionHandler.notifyException(new Context(root), ex, true, true);
                    }
                } else {
                    svnRevision = new SVNRevision.Number(revision);
                }

                // this isn't clean - the client notifies only files which realy were updated.
                // The problem here is that the revision in the metadata is set to HEAD even if the file didn't change         
                List<VCSFileProxy> filesToRefresh;
                VCSFileProxy[] fileArray;
                if (recursive) {
                    Subversion.getInstance().getStatusCache().patchRevision(new VCSFileProxy[] { root }, svnRevision);
                    int maxItems = 5;
                    filesToRefresh = patchFilesRecursively(root, svnRevision, maxItems);
                    // if >= 10000 rather refresh everything than just too large set of files
                    fileArray = filesToRefresh.size() >= maxItems ? null : filesToRefresh.toArray(new VCSFileProxy[filesToRefresh.size()]);
                } else {
                    filesToRefresh = new ArrayList<>();
                    filesToRefresh.add(root);
                    VCSFileProxy[] files = root.listFiles();
                    if (files != null) {
                        filesToRefresh.addAll(Arrays.asList(files));
                    }
                    fileArray = filesToRefresh.toArray(new VCSFileProxy[filesToRefresh.size()]);
                    Subversion.getInstance().getStatusCache().patchRevision(fileArray, svnRevision);
                }

                // the cache fires status change events to trigger the annotation refresh.
                // unfortunatelly, we have to call the refresh explicitly for each file from this place
                // as the revision label was changed even if the files status wasn't
                Subversion.getInstance().getStatusCache().getLabelsCache().flushFileLabels(fileArray);
                Subversion.getInstance().refreshAnnotationsAndSidebars(fileArray);
            }
        });
    }
    
    public static void performUpdate(final Context context, final String contextDisplayName) {
        if (context == null || context.getRoots().size() == 0) {
            return;
        }
        if(!Subversion.getInstance().checkClientAvailable(context)) {
            return;
        }
        SVNUrl repository;
        try {
            repository = ContextAction.getSvnUrl(context);
            // NB: repository can be null here
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(context, ex, true, true);
            return;
        }

        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
        SvnProgressSupport support = new SvnProgressSupport(context.getFileSystem()) {
            @Override
            public void perform() {
                update(context, this, contextDisplayName, null);
            }
        };
        support.start(rp, repository, org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Update_Progress")); // NOI18N
    }

    /**
     * Run update on a single file
     * @param file
     */
    public static void performUpdate(final VCSFileProxy file) {
        if (file == null) {
            return;
        }
        final Context context = new Context(file);
        if(!Subversion.getInstance().checkClientAvailable(context)) {
            return;
        }

        final SVNUrl repository;
        try {
            repository = ContextAction.getSvnUrl(context);
            if (repository == null) {
                SvnClientExceptionHandler.notifyNullUrl(context);
                return; // otherwise exceptions in update() below
            }
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(context, ex, true, true);
            return;
        }

        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
        SvnProgressSupport support = new SvnProgressSupport(context.getFileSystem()) {
            @Override
            public void perform() {
//                FileStatusCache cache = Subversion.getInstance().getStatusCache();
//                cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                update(new VCSFileProxy[] {file}, this, file.getPath(), repository, null);
            }
        };
        support.start(rp, repository, org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Update_Progress")); // NOI18N
    }
    
    private static List<VCSFileProxy> patchFilesRecursively (VCSFileProxy root, SVNRevision.Number revision, int maxReturnFiles) {
        List<VCSFileProxy> ret = new ArrayList<>();
        if (root == null) {
            return ret;
        }
        if (maxReturnFiles > 0) {
            // at this point it's useless to refresh a specific set of files in the IDE
            // it's better to refresh everything to save memory and it might be faster anyway
            ret.add(root);
        }
        VCSFileProxy[] files = root.listFiles();
        if (files != null) {
            FileStatusCache cache = Subversion.getInstance().getStatusCache();
            cache.patchRevision(files, revision);
            for (VCSFileProxy file : files) {
                FileInformation info = cache.getCachedStatus(file);
                if (!(SvnUtils.isPartOfSubversionMetadata(file) || SvnUtils.isAdministrative(file)
                        || info != null && (info.getStatus() & STATUS_RECURSIVELY_TRAVERSIBLE) == 0)) {
                    if (file.isDirectory()) {
                        ret.addAll(patchFilesRecursively(file, revision, maxReturnFiles - ret.size()));
                    } else if (maxReturnFiles - ret.size() > 0) {
                        ret.add(file);
                    }
                }
            }
        }
        return ret;
    }
    
    private static class UpdateOutputListener implements ISVNNotifyListener {

        private List<FileUpdateInfo> results;
        private final VCSFileProxy root;
        private UpdateOutputListener(Context context) {
            this.root = context.getRootFiles()[0];
        }
        
        @Override
        public void setCommand(ISVNNotifyListener.Command command) {
        }

        @Override
        public void logCommandLine(String str) {
        }

        @Override
        public void logMessage(String logMsg) {
            catchMessage(logMsg);
        }

        @Override
        public void logError(String str) {
            if (str == null) {
                return;
            }
            catchMessage(str);
        }

        @Override
        public void logRevision(long rev, String str) {
        }

        @Override
        public void logCompleted(String str) {
        }

        @Override
        public void onNotify(VCSFileProxy file, SVNNodeKind kind) {   
        }
        
        List<FileUpdateInfo> getResults() {
            if(results == null) {
                results = new ArrayList<>();
            }
            return results;
        }

        private void catchMessage(String logMsg) {
            FileUpdateInfo[] fuis = FileUpdateInfo.createFromLogMsg(root, logMsg);
            if(fuis != null) {
                for(FileUpdateInfo fui : fuis) {
                    if(fui != null) {
                        getResults().add(fui);
                    }
                }
            }
        }
        
    };

    private static class UpdateNotifyListener implements ISVNNotifyListener {
        private static final Pattern conflictFilePattern = Pattern.compile("(C...|.C..|..C.|...C) ?(.+)"); //NOI18N
        private static final Pattern existedFilePattern = Pattern.compile("E    ?(.+)"); //NOI18N
        HashSet<VCSFileProxy> conflictedFiles = new HashSet<>();
        HashSet<VCSFileProxy> existedFiles = new HashSet<>();
        private final VCSFileProxy root;
        
        private UpdateNotifyListener(Context context) {
            root = context.getRootFiles()[0];
        }
        
        @Override
        public void logMessage(String msg) {
            catchMessage(msg);
        }
        @Override
        public void logError(String msg) {
            if (msg == null) {
                return;
            }
            catchMessage(msg);
        }
        @Override
        public void setCommand(ISVNNotifyListener.Command arg0)                    { /* boring */  }
        @Override
        public void logCommandLine(String arg0)             { /* boring */  }
        @Override
        public void logRevision(long arg0, String arg1)     { /* boring */  }
        @Override
        public void logCompleted(String arg0)               { /* boring */  }
        @Override
        public void onNotify(VCSFileProxy arg0, SVNNodeKind arg1)   { /* boring */  }

        private void catchMessage (String message) {
            Matcher m = conflictFilePattern.matcher(message);
            if (m.matches() && m.groupCount() > 1) {
                String filePath = m.group(2);
                conflictedFiles.add(VCSFileProxySupport.getResource(root, filePath).normalizeFile());
            } else {
                m = existedFilePattern.matcher(message);
                if (m.matches() && m.groupCount() > 0) {
                    String filePath = m.group(1);
                    existedFiles.add(VCSFileProxySupport.getResource(root, filePath).normalizeFile());
                }
            }
        }
    }
    
}
