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
package org.netbeans.modules.subversion.remote;

import java.beans.PropertyChangeListener;
import org.netbeans.modules.subversion.remote.client.SvnClientFactory;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.subversion.remote.api.ISVNNotifyListener;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.SvnClientRefreshHandler;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.subversion.remote.config.SvnConfigFiles;
import org.netbeans.modules.subversion.remote.ui.ignore.IgnoreAction;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.subversion.remote.versioning.util.VCSHyperlinkProvider;
import org.netbeans.modules.versioning.core.util.Utils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.netbeans.modules.versioning.util.DelayScanRegistry;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**
 * A singleton Subversion manager class, center of Subversion module. Use {@link #getInstance()} to get access
 * to Subversion module functionality.
 *
 * 
 */
public class Subversion {
    
    /**
     * Fired when textual annotations and badges have changed. The NEW value is Set<File> of files that changed or NULL
     * if all annotaions changed.
     */
    public static final String PROP_ANNOTATIONS_CHANGED = "annotationsChanged"; //NOI18N

    static final String PROP_VERSIONED_FILES_CHANGED = "versionedFilesChanged"; //NOI18N

    /**
     * Results in refresh of annotations and diff sidebars
     */
    static final String PROP_BASE_FILE_CHANGED = "baseFileChanged";     //NOI18N

    static final String INVALID_METADATA_MARKER = "invalid-metadata"; // NOI18N

    private static final int STATUS_DIFFABLE =
            FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_CONFLICT |
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY;

    private static Subversion instance;

    private FileStatusCache                     fileStatusCache;
    private FilesystemHandler                   filesystemHandler;
    private FileStatusProvider                  fileStatusProvider;
    private SvnClientRefreshHandler             refreshHandler;
    private Annotator                           annotator;
    private HashMap<String, RequestProcessor>   processorsToUrl;

    private List<ISVNNotifyListener> svnNotifyListeners;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.subversion.remote"); //NOI18N

    private Result<? extends VCSHyperlinkProvider> hpResult;
    
    private static final List<String> allowableFolders;
    static {
        List<String> files = new ArrayList<String>();
        try {
            String allowable = System.getProperty("versioning.svn.allowableFolders", "/"); //NOI18N
            files.addAll(Arrays.asList(allowable.split("\\;"))); //NOI18N
            files.remove(""); //NOI18N
        } catch (Exception e) {
            LOG.log(Level.INFO, e.getMessage(), e);
        }
        allowableFolders = files;
    }

    public static synchronized Subversion getInstance() {
        if (instance == null) {
            instance = new Subversion();
            instance.init();
        }
        return instance;
    }
    private RequestProcessor parallelRP;
    private HistoryProvider historyProvider;

    private Subversion() {
    }
    
    private void init() {
        fileStatusCache = new FileStatusCache();
        annotator = new Annotator(this);
        fileStatusCache.setAnnotator(annotator);
        fileStatusProvider = new FileStatusProvider();
        filesystemHandler  = new FilesystemHandler(this);
        refreshHandler = new SvnClientRefreshHandler();
        prepareCache();
    }
    
    public void attachListeners(SubversionVCS svcs) {
        fileStatusCache.addVersioningListener(svcs);
        addPropertyChangeListener(svcs);
    }

    private RequestProcessor.Task cleanupTask;
    
    private void prepareCache() {
        cleanupTask = getRequestProcessor().create(new Runnable() {
            @Override
            public void run() {
                
                if(!fileStatusCache.ready()) {
                    fileStatusCache.computeIndex();                    
                }
                
                if (DelayScanRegistry.getInstance().isDelayed(cleanupTask, LOG, "Subversion.cleanupTask")) { //NOI18N
                    return;
                }
                
                try {
                    if (Subversion.LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Cleaning up cache"); // NOI18N
                    }
                    fileStatusCache.cleanUp(); // do not call before computeIndex()
                } finally {
                    if (Subversion.LOG.isLoggable(Level.FINE)) {
                        Subversion.LOG.fine("END Cleaning up cache"); // NOI18N
                    }
                    cleanupTask = null;
                }
            }
        });
        cleanupTask.schedule(500);
    }

    public void shutdown() {
        fileStatusProvider.shutdown();
    }

    public FileStatusCache getStatusCache() {
        return fileStatusCache;
    }

    public Annotator getAnnotator() {
        return annotator;
    }
    
    public HistoryProvider getHistoryProvider() {
        if(historyProvider == null) {
            historyProvider = new HistoryProvider();
        }
        return historyProvider;
    }    

    public SvnClientRefreshHandler getRefreshHandler() {
        return refreshHandler;
    }

    public boolean checkClientAvailable(Context context) {
        final FileSystem fileSystem = context.getFileSystem();
        if (fileSystem == null || !VCSFileProxySupport.isConnectedFileSystem(fileSystem)) {
            return false;
        }
        try {
            SvnClientFactory.checkClientAvailable(context);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(context, ex, true, true);
            return false;
        }
        return true;
    }

    public SvnClient getClient(Context context, SVNUrl repositoryUrl, String username, char[] password, int handledExceptions) throws SVNClientException {
        SvnClient client = SvnClientFactory.getInstance(context).createSvnClient(context, repositoryUrl, null, username, password, handledExceptions);
        attachListeners(context.getFileSystem(), client);
        return client;
    }

    public SvnClient getClient(Context context, SVNUrl repositoryUrl, SvnProgressSupport progressSupport) throws SVNClientException {
        Parameters.notNull("repositoryUrl", repositoryUrl); //NOI18N
        String username = ""; // NOI18N
        char[] password = null;

        return getClient(context, repositoryUrl, username, password, progressSupport);
    }

    public SvnClient getClient(Context context, SVNUrl repositoryUrl, String username, char[] password, SvnProgressSupport support) throws SVNClientException {
        SvnClient client = SvnClientFactory.getInstance(context).createSvnClient(context, repositoryUrl, support, username, password, SvnClientExceptionHandler.EX_DEFAULT_HANDLED_EXCEPTIONS);
        attachListeners(context.getFileSystem(), client);
        return client;
    }

    public SvnClient getClient(VCSFileProxy file) throws SVNClientException {
        return getClient(file, null);
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("RCN") // assert in release mode does not guarantee that "repositoryUrl != null"
    public SvnClient getClient(VCSFileProxy file, SvnProgressSupport support) throws SVNClientException {
        SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(file);
        assert repositoryUrl != null : "Unable to get repository: " + file.getPath() + " is probably unmanaged."; // NOI18N

        return repositoryUrl == null ? null : getClient(new Context(file), repositoryUrl, support);
    }

    public SvnClient getClient(Context ctx, SvnProgressSupport support) throws SVNClientException {
        VCSFileProxy[] roots = ctx.getRootFiles();
        SVNUrl repositoryUrl = null;
        for (VCSFileProxy root : roots) {
            // XXX #168094 logging
            if (!SvnUtils.isManaged(root)) {
                Subversion.LOG.log(Level.WARNING, "getClient: unmanaged file in context: {0}", root.getPath()); //NOI18N
            }
            repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
            if (repositoryUrl != null) {
                break;
            } else {
                Subversion.LOG.log(Level.WARNING, "Could not retrieve repository root for context file {0}", new Object[]{root});
            }
        }

//        assert repositoryUrl != null : "Unable to get repository, context contains only unmanaged files!"; // NOI18N
        if (repositoryUrl == null) {
            // XXX #168094 logging
            // preventing NPE in getClient(repositoryUrl, support)
            StringBuilder sb = new StringBuilder("Cannot determine repositoryRootUrl for selected context:"); //NOI18N
            for (VCSFileProxy root : roots) {
                sb.append("\n").append(root.getPath());         //NOI18N
            }
            throw new SVNClientException(sb.toString());
        }
        return getClient(ctx, repositoryUrl, support);
    }

    public SvnClient getClient(Context context, SVNUrl repositoryUrl) throws SVNClientException {
        return getClient(context, repositoryUrl, null);
    }

    /**
     * <b>Creates</b> ClientAtapter implementation that already handles:
     * <ul>
     *    <li>prompts user for password if necessary,
     *    <li>let user specify proxy setting on network errors or
     *    <li>let user cancel operation
     *    <li>logs command execuion into output tab
     *    <li>posts notification events in status cache
     * </ul>
     *
     * <p>It hanldes cancellability
     */
    public SvnClient getClient(boolean attachListeners, Context context) throws SVNClientException {
        cleanupFilesystem();
        SvnClient client = SvnClientFactory.getInstance(context).createSvnClient(context);
        if(attachListeners) {
            attachListeners(context.getFileSystem(), client);
        }
        return client;
    }

    public void versionedFilesChanged() {
        topmostInfo.clear();
        unversionedParents.clear();
        support.firePropertyChange(PROP_VERSIONED_FILES_CHANGED, null, null);
    }

    /**
     * Backdoor for SvnClientFactory
     */
    public void cleanupFilesystem() {
        filesystemHandler.removeInvalidMetadata();
    }

    private void attachListeners(FileSystem fileSystem, SvnClient client) {
        client.addNotifyListener(getLogger(fileSystem, client.getSvnUrl()));
        client.addNotifyListener(refreshHandler);

        List<ISVNNotifyListener> l = getSVNNotifyListeners();

        ISVNNotifyListener[] listeners = null;
        synchronized(l) {
            listeners = l.toArray(new ISVNNotifyListener[l.size()]);
        }
        for(ISVNNotifyListener listener : listeners) {
            client.addNotifyListener(listener);
        }
    }

    /**
     *
     * @param repositoryRoot URL of Subversion repository so that logger writes to correct output tab. Can be null
     * in which case the logger will not print anything
     * @return OutputLogger logger to write to
     */
    public OutputLogger getLogger(FileSystem fileSystem, SVNUrl repositoryRoot) {
        return OutputLogger.getLogger(fileSystem, repositoryRoot);
    }

    /**
     * Non-recursive ignore check.
     *
     * <p>Side effect: if under SVN version control
     * it sets svn:ignore property
     *
     * @return true if file is listed in parent's ignore list
     * or IDE thinks it should be.
     */
    boolean isIgnored(VCSFileProxy file) {
        String name = file.getName();
        file = file.normalizeFile();

        // ask SVN

        final VCSFileProxy parent = file.getParentFile();
        if (parent != null) {
            int pstatus = fileStatusCache.getStatus(parent).getStatus();
            final Context context = new Context(parent);
            if ((pstatus & FileInformation.STATUS_VERSIONED) != 0) {
                try {
                    SvnClient client = getClient(false, context);

                    List<String> gignores = SvnConfigFiles.getInstance(context.getFileSystem()).getGlobalIgnores();
                    if(gignores != null && SvnUtils.getMatchinIgnoreParterns(gignores, name, true).size() > 0) {
                        // no need to read the ignored property -> its already set in ignore patterns
                        return true;
                    }
                    List<String> patterns = client.getIgnoredPatterns(parent);
                    if(patterns != null && SvnUtils.getMatchinIgnoreParterns(patterns, name, true).size() > 0) {
                        return true;
                    }

                } catch (SVNClientException ex)  {
                    if(!SvnClientExceptionHandler.isUnversionedResource(ex.getMessage()) && 
                       !SvnClientExceptionHandler.isCancelledAction(ex.getMessage()) &&
                       !WorkingCopyAttributesCache.getInstance().isSuppressed(ex))
                    {
                        SvnClientExceptionHandler.notifyException(context, ex, false, false);
                    }
                }
            }
        }

        if (SharabilityQuery.getSharability(VCSFileProxySupport.toURI(file)) == SharabilityQuery.Sharability.NOT_SHARABLE) {
            try {
                // BEWARE: In NetBeans VISIBILTY == SHARABILITY ... and we hide Locally Removed folders => we must not Ignore them by mistake
                FileInformation info = fileStatusCache.getCachedStatus(file); // getStatus may cause stack overflow
                if (SubversionVisibilityQuery.isHiddenFolder(info, file)) {
                    return false;
                }
                // if IDE-ignore-root then propagate IDE opinion to Subversion svn:ignore
                if (SharabilityQuery.getSharability(VCSFileProxySupport.toURI(parent)) !=  SharabilityQuery.Sharability.NOT_SHARABLE) {
                    if ((fileStatusCache.getStatus(parent).getStatus() & FileInformation.STATUS_VERSIONED) != 0) {
                        IgnoreAction.ignore(file);
                    }
                }
            } catch (SVNClientException ex) {
                if(!WorkingCopyAttributesCache.getInstance().isSuppressed(ex)) {
                    SvnClientExceptionHandler.notifyException(new Context(file), ex, false, false);
                }
            }
            return true;
        } else {
            // backward compatability #68124
            if (".nbintdb".equals(name)) {  // NOI18N
                return true;
            }

            return false;
        }
    }

    /**
     * Serializes all SVN requests (moves them out of AWT).
     */
    public RequestProcessor getRequestProcessor() {
        return getRequestProcessor(null);
    }

    public RequestProcessor getParallelRequestProcessor () {
        if (parallelRP == null) {
            parallelRP = new RequestProcessor("Subversion.ParallelTasks", 5, true); //NOI18N
        }
        return parallelRP;
    }

    /**
     * Serializes all SVN requests (moves them out of AWT).
     */
    public RequestProcessor getRequestProcessor(SVNUrl url) {
        if(processorsToUrl == null) {
            processorsToUrl = new HashMap<>();
        }

        String key;
        if(url != null) {
            key = url.toString();
        } else {
            key = "ANY_URL"; // NOI18N
        }

        RequestProcessor rp = processorsToUrl.get(key);
        if(rp == null) {
            rp = new RequestProcessor("Subversion - " + key, 1, true); // NOI18N
            processorsToUrl.put(key, rp);
        }
        return rp;
    }

    private final Set<VCSFileProxy> unversionedParents = Collections.synchronizedSet(new HashSet<VCSFileProxy>(20));

    /**
     * Return true if file belongs to connected remote file system and is not forbidden
     *
     * @param file
     * @return
     */
    public boolean isConnected(VCSFileProxy file) {
        if (file.toFile() != null) {
            return false;
        }
        if (!isAllowable(file)) {
            return false;
        }
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "looking for managed parent for {0}", new Object[] { file });
        }
        if (!VCSFileProxySupport.isConnectedFileSystem(VCSFileProxySupport.getFileSystem(file))) {
            return false;
        }
        return true;
    }

    /**
     * Delegates to SubversionVCS.getTopmostManagedAncestor
     * @param file a file for which the topmost managed ancestor shall be looked up.
     * @return topmost managed ancestor for the given file
     */
    public VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {
        if (!isConnected(file)) {
            return null;
        }
        if(unversionedParents.contains(file)) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.fine(" cached as unversioned");
            }
            return null;
       }
        VCSFileProxy metadataRoot = null;
        if (SvnUtils.isPartOfSubversionMetadata(file)) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.fine(" part of metaddata");
            }
            for (;file != null; file = file.getParentFile()) {
                if (SvnUtils.isAdministrative(file)) {
                    if (Subversion.LOG.isLoggable(Level.FINE)) {
                        Subversion.LOG.log(Level.FINE, " will use parent {0}", new Object[] { file });
                    }
                    metadataRoot = file;
                    file = file.getParentFile();
                    break;
                }
            }
        }
        VCSFileProxy topmost = null;
        Set<VCSFileProxy> done = new HashSet<>();
        for (; file != null; file = file.getParentFile()) {
            if(unversionedParents.contains(file)) {
                if (Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.log(Level.FINE, " already known as unversioned {0}", new Object[] { file });
                }
                break;
            }
            if (VersioningSupport.isExcluded(file)) {
                break;
            }
            // is the folder a special one where metadata should not be looked for?
            boolean forbiddenFolder = Utils.isForbiddenFolder(file);
            if (!forbiddenFolder && SvnUtils.hasMetadata(file)) {
                if (Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.log(Level.FINE, " found managed parent {0}", new Object[] { file });
                }
                topmost = file;
                done.clear();
            } else {
                if (Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.log(Level.FINE, " found unversioned {0}", new Object[] { file });
                }
                if(file.exists()) { // could be created later ...
                    done.add(file);
                }
            }
        }
        if(done.size() > 0) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, " storing unversioned");
            }
            unversionedParents.addAll(done);
        }
        if (topmost == null && metadataRoot != null) {
            // .svn is considered managed, too, see #159453
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, "setting metadata root as managed parent {0}", new Object[] { metadataRoot });
            }
            topmost = metadataRoot;
        }
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "returning managed parent {0}", new Object[] { topmost });
        }
        cacheIfNeededAllForTopmost(topmost);
        return topmost;
    }

    FileStatusProvider getVCSAnnotator() {
        return fileStatusProvider;
    }

    VCSInterceptor getInterceptor() {
        return filesystemHandler;
    }

    private List<ISVNNotifyListener> getSVNNotifyListeners() {
        if(svnNotifyListeners == null) {
            svnNotifyListeners = new ArrayList<>();
        }
        return svnNotifyListeners;
    }

    /**
     * Refreshes all textual annotations and badges.
     */
    public void refreshAllAnnotations() {
        support.firePropertyChange(PROP_ANNOTATIONS_CHANGED, null, null);
    }

    /**
     * Refreshes all textual annotations and badges for the given files.
     *
     * @param files files to chage the annotations for
     */
    public void refreshAnnotations(VCSFileProxy ... files) {
        Set<VCSFileProxy> s = new HashSet<>(Arrays.asList(files));
        support.firePropertyChange(PROP_ANNOTATIONS_CHANGED, null, s);
    }

    /**
     * Refreshes all textual annotations, badges and sidebars for the given files.
     *
     * @param files files to chage the annotations and sidebars for
     */
    public void refreshAnnotationsAndSidebars (VCSFileProxy... files) {
        Set<VCSFileProxy> s = files == null ? null : new HashSet<>(Arrays.asList(files));
        support.firePropertyChange(PROP_BASE_FILE_CHANGED, null, s);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void addSVNNotifyListener(ISVNNotifyListener listener) {
        List<ISVNNotifyListener> listeners = getSVNNotifyListeners();
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    public void removeSVNNotifyListener(ISVNNotifyListener listener) {
        List<ISVNNotifyListener> listeners = getSVNNotifyListeners();
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    void getOriginalFile(VCSFileProxy workingCopy, VCSFileProxy originalFile) {
        FileInformation info = fileStatusCache.getStatus(workingCopy);
        if ((info.getStatus() & STATUS_DIFFABLE) == 0) {
            return;
        }

        VCSFileProxy original = null;
        try {
            final Context context = new Context(workingCopy);
            SvnClientFactory.checkClientAvailable(context);
            original = VersionsCache.getInstance(context.getFileSystem()).getBaseRevisionFile(workingCopy);
            if (original == null) {
                throw new IOException("Unable to get BASE revision of " + workingCopy); //NOI18N
            }
            org.netbeans.modules.versioning.util.Utils.copyStreamsCloseAll(VCSFileProxySupport.getOutputStream(originalFile), original.getInputStream(false));
        } catch (IOException e) {
            LOG.log(Level.INFO, "Unable to get original file", e); //NOI18N
        } catch (SVNClientException ex) {
            Subversion.LOG.log(Level.INFO, "Subversion.getOriginalFile: file is managed but svn client is unavailable (file {0})", workingCopy.getPath()); //NOI18N
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, null, ex);
            }
        }
    }

        /**
     *
     * @return registered hyperlink providers
     */
    public List<VCSHyperlinkProvider> getHyperlinkProviders() {
        if (hpResult == null) {
            hpResult = (Result<? extends VCSHyperlinkProvider>) Lookup.getDefault().lookupResult(VCSHyperlinkProvider.class);
        }
        if (hpResult == null) {
            return Collections.emptyList();
        }
        Collection<? extends VCSHyperlinkProvider> providersCol = hpResult.allInstances();
        List<VCSHyperlinkProvider> providersList = new ArrayList<>(providersCol.size());
        providersList.addAll(providersCol);
        return Collections.unmodifiableList(providersList);
    }

    private boolean isAllowable(VCSFileProxy file) {
        String path = file.getPath()+"/"; //NOI18N
        for(String s : allowableFolders) {
            if (s.endsWith("/")) { //NOI18N
                if (path.startsWith(s)) {
                    return true;
                }
            } else {
                if (path.startsWith(s+"/")) { //NOI18N
                    return true;
                }
            }
        }
        return false;
    }

    public SVNUrl getTopmostRepositoryUrl(VCSFileProxy file) throws SVNClientException {
        VCSFileProxy topmost = getTopmostManagedAncestor(file);
        if (topmost == null) {
            return null;
        }
        GlobalInfo out = topmostInfo.get(topmost);
        assert out != null;
        return out.getTopmostRepositoryUrl();
    }
    
    public void refreshTopmostRepositoryUrl(VCSFileProxy file) {
        VCSFileProxy topmost = getTopmostManagedAncestor(file);
        if (topmost == null) {
            return;
        }
        GlobalInfo out = topmostInfo.get(topmost);
        out.refresh();
    }
    
    private final ConcurrentMap<VCSFileProxy, GlobalInfo> topmostInfo = new ConcurrentHashMap<>();
    private void cacheIfNeededAllForTopmost(VCSFileProxy topmost) {
        if (topmost != null) {
            try {
                GlobalInfo info = topmostInfo.get(topmost);
                if (info == null) {
                    topmostInfo.putIfAbsent(topmost, new GlobalInfo(topmost));
                    info = topmostInfo.get(topmost);
                    info.getTopmostRepositoryUrl();
                }
            } catch (SVNClientException ex) {
                LOG.log(Level.INFO, "Cannot get repository URL for "+topmost.getPath(), ex); //NOI18N
            }
        }
    }

    private final class GlobalInfo implements Runnable {
        private boolean inited = false;
        private SVNUrl result;
        private final VCSFileProxy topmost;
        private SVNClientException ex;
        RequestProcessor.Task runner;
        
        private GlobalInfo(VCSFileProxy topmost) {
            this.topmost = topmost;
        }

        private SVNUrl getTopmostRepositoryUrl() throws SVNClientException {
            synchronized (this) {
                if (!inited) {
                    if (runner == null) {
                        runner = getParallelRequestProcessor().create(this);
                        runner.run();
                    }
                    runner.waitFinished();
                }
                if (ex != null) {
                    throw ex;
                }
                return result;
            }
        }

        @Override
        public void run() {
            try {
                result = SvnUtils.getRepositoryRootUrl(topmost, true);
            } catch (SVNClientException ex) {
                this.ex = ex;
            }
            inited = true;
        }

        private void refresh() {
            synchronized(this) {
                inited = false;
                ex = null;
                runner = null;
                result = null;
            }
            try {
                getTopmostRepositoryUrl();
            } catch (SVNClientException ex) {
                // will be thrown when getting url next time
            }
        }
    }
}

