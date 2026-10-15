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

package org.netbeans.modules.subversion.ui.update;

import java.util.Iterator;
import java.util.regex.Matcher;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ActionUtils;
import org.netbeans.modules.subversion.util.ClientCheckSupport;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VersioningOutputManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Update action
 *
 * @author Petr Kuzel
 */ 
public class UpdateAction extends ContextAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/subversion/resources/icons/update.png"; //NOI18N
    private static final int STATUS_RECURSIVELY_TRAVERSIBLE = FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;

    public UpdateAction () {
        this(ICON_RESOURCE);
    }

    protected UpdateAction (String iconResource) {
        super(iconResource);
    }
    
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
        ClientCheckSupport.getInstance().runInAWTIfAvailable(ActionUtils.cutAmpersand(getRunningName(nodes)), new Runnable() {
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

    private void update(Context ctx, SvnProgressSupport progress, String contextDisplayName, SVNRevision revision) {
               
        File[] roots = ctx.getRootFiles();
        
        Map<File, List<File>> rootsPerCheckout = new HashMap<>();
        for (File root : roots) {
            File topManaged = Subversion.getInstance().getTopmostManagedAncestor(root);
            if (topManaged != null) {
                List<File> files = rootsPerCheckout.get(topManaged);
                if (files == null) {
                    files = new ArrayList<>();
                    rootsPerCheckout.put(topManaged, files);
                }
                files.add(root);
            }
        }
        if (rootsPerCheckout.isEmpty()) {
            return;
        }
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        cache.refreshCached(ctx);
        for (Map.Entry<File, List<File>> e : rootsPerCheckout.entrySet()) {
            List<File> files = e.getValue();
            try {
                if (rootsPerCheckout.size() > 1) {
                    contextDisplayName = getContextDisplayName(files);
                }
                File root = files.get(0);
                SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
                if(repositoryUrl == null) {
                    Subversion.LOG.log(Level.WARNING, "Could not retrieve repository root for context file {0}", new Object[]{root});
                    continue;
                }
                update(e.getKey(), files.toArray(new File[0]), progress, contextDisplayName, repositoryUrl, revision);
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, true, true);
            }
        }
    }

    private static void update (File checkoutRoot, File[] roots, final SvnProgressSupport progress, String contextDisplayName, SVNUrl repositoryUrl, final SVNRevision revision) {
        File[][] split = Utils.splitFlatOthers(roots);
        final List<File> recursiveFiles = new ArrayList<File>();
        final List<File> flatFiles = new ArrayList<File>();
        
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
        UpdateOutputListener listener = new UpdateOutputListener();
        try {
            client = Subversion.getInstance().getClient(repositoryUrl); 
            // this isn't clean - the client notifies only files which realy were updated. 
            // The problem here is that the revision in the metadata is set to HEAD even if the file didn't change =>
            // we have to explicitly force the refresh for the relevant context - see bellow in updateRoots
            client.removeNotifyListener(Subversion.getInstance().getRefreshHandler());
            client.addNotifyListener(listener);
            client.addNotifyListener(progress);
            progress.setCancellableDelegate(client);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }

        try {                    
            UpdateNotifyListener l = new UpdateNotifyListener();            
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
                HashSet<File> filesToRefresh = new HashSet<File>(l.existedFiles);
                filesToRefresh.addAll(l.conflictedFiles);
                Subversion.getInstance().getStatusCache().refreshAsync(filesToRefresh.toArray(new File[0]));
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
            openResults(listener.getResults(), repositoryUrl, contextDisplayName,
                    checkoutRoot == null ? "" : checkoutRoot.getAbsolutePath());                
        }
    }

    private static void openResults(final List<FileUpdateInfo> resultsList, final SVNUrl url, final String contextDisplayName, final String checkoutRoot) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                UpdateResults results = new UpdateResults(resultsList, url, contextDisplayName);
                VersioningOutputManager vom = VersioningOutputManager.getInstance();
                vom.addComponent(SvnUtils.decodeToString(url) + "-UpdateExecutor-" + checkoutRoot, results); // NOI18N
            }
        });
    }
    
    private static void updateRoots(List<File> roots, SvnProgressSupport support, SvnClient client, boolean recursive, SVNRevision revision) throws SVNClientException {
        for (Iterator<File> it = roots.iterator(); it.hasNext();) {
            File root = it.next();
            if(support.isCanceled()) {
                break;
            }
            long rev = client.update(root, revision == null ? SVNRevision.HEAD : revision, recursive);
            revisionUpdateWorkaround(recursive, FileUtil.normalizeFile(root), client, rev);
        }
        return;
    }

    private static void revisionUpdateWorkaround(final boolean recursive, final File root, final SvnClient client, final long revision) throws SVNClientException {
        Utils.post(new Runnable() {
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
                        SvnClientExceptionHandler.notifyException(ex, true, true);
                    }
                } else {
                    svnRevision = new SVNRevision.Number(revision);
                }

                // this isn't clean - the client notifies only files which realy were updated.
                // The problem here is that the revision in the metadata is set to HEAD even if the file didn't change         
                List<File> filesToRefresh;
                File[] fileArray;
                if (recursive) {
                    Subversion.getInstance().getStatusCache().patchRevision(new File[] { root }, svnRevision);
                    int maxItems = 5;
                    filesToRefresh = patchFilesRecursively(root, svnRevision, maxItems);
                    // if >= 10000 rather refresh everything than just too large set of files
                    fileArray = filesToRefresh.size() >= maxItems ? null : filesToRefresh.toArray(new File[0]);
                } else {
                    filesToRefresh = new ArrayList<>();
                    filesToRefresh.add(root);
                    File[] files = root.listFiles();
                    if (files != null) {
                        filesToRefresh.addAll(Arrays.asList(files));
                    }
                    fileArray = filesToRefresh.toArray(new File[0]);
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
        if(!Subversion.getInstance().checkClientAvailable()) {
            return;
        }
        if (context == null || context.getRoots().size() == 0) {
            return;
        }

        SVNUrl repository;
        try {
            repository = getSvnUrl(context);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }

        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
        SvnProgressSupport support = new SvnProgressSupport() {
            public void perform() {
                SystemAction.get(UpdateAction.class).update(context, this, contextDisplayName, null);
            }
        };
        support.start(rp, repository, org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Update_Progress")); // NOI18N
    }

    /**
     * Run update on a single file
     * @param file
     */
    public static void performUpdate(final File file) {
        if(!Subversion.getInstance().checkClientAvailable()) {
            return;
        }
        if (file == null) {
            return;
        }

        SVNUrl repository;
        try {
            repository = SvnUtils.getRepositoryRootUrl(file);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
        final SVNUrl repositoryUrl = repository;

        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
        SvnProgressSupport support = new SvnProgressSupport() {
            public void perform() {
//                FileStatusCache cache = Subversion.getInstance().getStatusCache();
//                cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                update(Subversion.getInstance().getTopmostManagedAncestor(file),
                        new File[] {file}, this, file.getAbsolutePath(), repositoryUrl, null);
            }
        };
        support.start(rp, repositoryUrl, org.openide.util.NbBundle.getMessage(UpdateAction.class, "MSG_Update_Progress")); // NOI18N
    }
    
    private static List<File> patchFilesRecursively (File root, SVNRevision.Number revision, int maxReturnFiles) {
        List<File> ret = new ArrayList<>();
        if (root == null) {
            return ret;
        }
        if (maxReturnFiles > 0) {
            // at this point it's useless to refresh a specific set of files in the IDE
            // it's better to refresh everything to save memory and it might be faster anyway
            ret.add(root);
        }
        File[] files = root.listFiles();
        if (files != null) {
            FileStatusCache cache = Subversion.getInstance().getStatusCache();
            cache.patchRevision(files, revision);
            for (File file : files) {
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
    
    private String getContextDisplayName (List<File> files) {
        Node[] nodes = new Node[files.size()];
        for (int i = 0; i < files.size(); ++i) {
            final File file = files.get(i);
            nodes[i] = new AbstractNode(Children.LEAF, Lookups.fixed(file)) {

                @Override
                public String getName () {
                    return file.getName();
                }
                
            };
        }
        return getContextDisplayName(nodes);
    }
    
    private static class UpdateOutputListener implements ISVNNotifyListener {

        private List<FileUpdateInfo> results;        
        
        public void setCommand(int command) {
        }

        public void logCommandLine(String str) {
        }

        public void logMessage(String logMsg) {
            catchMessage(logMsg);
        }

        public void logError(String str) {
            if (str == null) return;
            catchMessage(str);
        }

        public void logRevision(long rev, String str) {
        }

        public void logCompleted(String str) {
        }

        public void onNotify(File file, SVNNodeKind kind) {   
        }
        
        List<FileUpdateInfo> getResults() {
            if(results == null) {
                results = new ArrayList<FileUpdateInfo>();
            }
            return results;
        }

        private void catchMessage(String logMsg) {
            FileUpdateInfo[] fuis = FileUpdateInfo.createFromLogMsg(logMsg);
            if(fuis != null) {
                for(FileUpdateInfo fui : fuis) {
                    if(fui != null) getResults().add(fui);
                }
            }
        }
        
    };

    private static class UpdateNotifyListener implements ISVNNotifyListener {
        private static Pattern conflictFilePattern = Pattern.compile("(C...|.C..|..C.|...C) ?(.+)"); //NOI18N
        private static Pattern existedFilePattern = Pattern.compile("E    ?(.+)"); //NOI18N
        HashSet<File> conflictedFiles = new HashSet<File>();
        HashSet<File> existedFiles = new HashSet<File>();
        public void logMessage(String msg) {
            catchMessage(msg);
        }
        public void logError(String msg) {
            if (msg == null) return;
            catchMessage(msg);
        }
        public void setCommand(int arg0)                    { /* boring */  }
        public void logCommandLine(String arg0)             { /* boring */  }
        public void logRevision(long arg0, String arg1)     { /* boring */  }
        public void logCompleted(String arg0)               { /* boring */  }
        public void onNotify(File arg0, SVNNodeKind arg1)   { /* boring */  }

        private void catchMessage (String message) {
            Matcher m = conflictFilePattern.matcher(message);
            if (m.matches() && m.groupCount() > 1) {
                String filePath = m.group(2);
                conflictedFiles.add(FileUtil.normalizeFile(new File(filePath)));
            } else {
                m = existedFilePattern.matcher(message);
                if (m.matches() && m.groupCount() > 0) {
                    String filePath = m.group(1);
                    existedFiles.add(FileUtil.normalizeFile(new File(filePath)));
                }
            }
        }
    }
    
}
