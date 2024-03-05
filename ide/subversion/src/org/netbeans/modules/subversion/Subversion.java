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

package org.netbeans.modules.subversion;

import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.client.*;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.tigris.subversion.svnclientadapter.*;
import org.openide.util.RequestProcessor;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import org.netbeans.modules.subversion.ui.ignore.IgnoreAction;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.subversion.config.PasswordFile;
import org.netbeans.modules.subversion.ui.repository.RepositoryConnection;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.util.DelayScanRegistry;
import org.netbeans.modules.versioning.util.VCSHyperlinkProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * A singleton Subversion manager class, center of Subversion module. Use {@link #getInstance()} to get access
 * to Subversion module functionality.
 *
 * @author Maros Sandor
 */
public class Subversion {

    /**
     * Fired when textual annotations and badges have changed. The NEW value is Set<File> of files that changed or NULL
     * if all annotaions changed.
     */
    public static final String PROP_ANNOTATIONS_CHANGED = "annotationsChanged";

    static final String PROP_VERSIONED_FILES_CHANGED = "versionedFilesChanged";

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

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.subversion");

    private Result<? extends VCSHyperlinkProvider> hpResult;

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
        fileStatusProvider = new FileStatusProvider();
        filesystemHandler  = new FilesystemHandler(this);
        refreshHandler = new SvnClientRefreshHandler();
        prepareCache();
    }

    public void attachListeners(SubversionVCS svcs) {
        fileStatusCache.addVersioningListener(svcs);
        addPropertyChangeListener(svcs);
    }

    RequestProcessor.Task cleanupTask;
    
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
                    LOG.fine("Cleaning up cache"); // NOI18N
                    fileStatusCache.cleanUp(); // do not call before computeIndex()
                } finally {
                    Subversion.LOG.fine("END Cleaning up cache"); // NOI18N
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

    public boolean checkClientAvailable() {
        if(SvnClientFactory.wasJavahlCrash()) {
            throw new RuntimeException("It appears that subversion javahl initialization caused trouble in a previous Netbeans session. Please report.");
        }
        try {
            SvnClientFactory.checkClientAvailable();
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return false;
        }
        return true;
    }

    public SvnClient getClient(SVNUrl repositoryUrl,
                               String username,
                               char[] password)
    throws SVNClientException
    {
        return getClient(repositoryUrl, username, password, SvnClientExceptionHandler.EX_DEFAULT_HANDLED_EXCEPTIONS);
    }

    public SvnClient getClient(SVNUrl repositoryUrl,
                               String username,
                               char[] password,
                               int handledExceptions) throws SVNClientException {
        SvnClient client = SvnClientFactory.getInstance().createSvnClient(repositoryUrl, null, username, password, handledExceptions);
        attachListeners(client);
        return client;
    }

    public SvnClient getClient(SVNUrl repositoryUrl, SvnProgressSupport progressSupport) throws SVNClientException {
        Parameters.notNull("repositoryUrl", repositoryUrl); //NOI18N
        String username = ""; // NOI18N
        char[] password = null;
        RepositoryConnection rc = SvnModuleConfig.getDefault().getRepositoryConnection(repositoryUrl.toString());
        if(rc != null) {
            username = rc.getUsername();
            password = rc.getPassword();
        } else if(!Utilities.isWindows()) {
            PasswordFile pf = PasswordFile.findFileForUrl(repositoryUrl);
            if(pf != null) {
                username = pf.getUsername();
                String psswdString = pf.getPassword();
                password = psswdString != null ? psswdString.toCharArray() : null;
            }
        }
        return getClient(repositoryUrl, username, password, progressSupport);
    }

    public SvnClient getClient(SVNUrl repositoryUrl,
                               String username,
                               char[] password,
                               SvnProgressSupport support) throws SVNClientException {
        SvnClient client = SvnClientFactory.getInstance().createSvnClient(repositoryUrl, support, username, password, SvnClientExceptionHandler.EX_DEFAULT_HANDLED_EXCEPTIONS);
        attachListeners(client);
        return client;
    }

    public SvnClient getClient(File file) throws SVNClientException {
        return getClient(file, null);
    }

    public SvnClient getClient(File file, SvnProgressSupport support) throws SVNClientException {
        SVNUrl repositoryUrl = SvnUtils.getRepositoryRootUrl(file);
        assert repositoryUrl != null : "Unable to get repository: " + file.getAbsolutePath() + " is probably unmanaged."; // NOI18N

        return repositoryUrl == null ? null : getClient(repositoryUrl, support);
    }

    public SvnClient getClient(Context ctx, SvnProgressSupport support) throws SVNClientException {
        File[] roots = ctx.getRootFiles();
        SVNUrl repositoryUrl = null;
        for (File root : roots) {
            // XXX #168094 logging
            if (!SvnUtils.isManaged(root)) {
                Subversion.LOG.log(Level.WARNING, "getClient: unmanaged file in context: {0}", root.getAbsoluteFile()); //NOI18N
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
            for (File root : roots) {
                sb.append("\n").append(root.getAbsolutePath());         //NOI18N
            }
            throw new SVNClientException(sb.toString());
        }
        return getClient(repositoryUrl, support);
    }

    public SvnClient getClient(SVNUrl repositoryUrl) throws SVNClientException {
        return getClient(repositoryUrl, null);
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
    public SvnClient getClient(boolean attachListeners) throws SVNClientException {
        cleanupFilesystem();
        SvnClient client = SvnClientFactory.getInstance().createSvnClient();
        if(attachListeners) {
            attachListeners(client);
        }
        return client;
    }

    public void versionedFilesChanged() {
        unversionedParents.clear();
        support.firePropertyChange(PROP_VERSIONED_FILES_CHANGED, null, null);
    }

    /**
     * Backdoor for SvnClientFactory
     */
    public void cleanupFilesystem() {
        filesystemHandler.removeInvalidMetadata();
    }

    private void attachListeners(SvnClient client) {
        client.addNotifyListener(getLogger(client.getSvnUrl()));
        client.addNotifyListener(refreshHandler);

        List<ISVNNotifyListener> l = getSVNNotifyListeners();

        ISVNNotifyListener[] listeners = null;
        synchronized(l) {
            listeners = l.toArray(new ISVNNotifyListener[0]);
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
    public OutputLogger getLogger(SVNUrl repositoryRoot) {
        return OutputLogger.getLogger(repositoryRoot);
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
    boolean isIgnored(File file) {
        String name = file.getName();
        file = FileUtil.normalizeFile(file);

        // ask SVN

        final File parent = file.getParentFile();
        if (parent != null) {
            int pstatus = fileStatusCache.getStatus(parent).getStatus();
            if ((pstatus & FileInformation.STATUS_VERSIONED) != 0) {
                try {
                    SvnClient client = getClient(false);

                    List<String> gignores = SvnConfigFiles.getInstance().getGlobalIgnores();
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
                        SvnClientExceptionHandler.notifyException(ex, false, false);
                    }
                }
            }
        }

        if (SharabilityQuery.getSharability(file) == SharabilityQuery.NOT_SHARABLE) {
            try {
                // BEWARE: In NetBeans VISIBILTY == SHARABILITY ... and we hide Locally Removed folders => we must not Ignore them by mistake
                FileInformation info = fileStatusCache.getCachedStatus(file); // getStatus may cause stack overflow
                if (SubversionVisibilityQuery.isHiddenFolder(info, file)) {
                    return false;
                }
                // if IDE-ignore-root then propagate IDE opinion to Subversion svn:ignore
                if (SharabilityQuery.getSharability(parent) !=  SharabilityQuery.NOT_SHARABLE) {
                    if ((fileStatusCache.getStatus(parent).getStatus() & FileInformation.STATUS_VERSIONED) != 0) {
                        IgnoreAction.ignore(file);
                    }
                }
            } catch (SVNClientException ex) {
                if(!WorkingCopyAttributesCache.getInstance().isSuppressed(ex)) {
                    SvnClientExceptionHandler.notifyException(ex, false, false);
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
            processorsToUrl = new HashMap<String, RequestProcessor>();
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

    private final Set<File> unversionedParents = Collections.synchronizedSet(new HashSet<File>(20));
    /**
     * Delegates to SubversionVCS.getTopmostManagedAncestor
     * @param file a file for which the topmost managed ancestor shall be looked up.
     * @return topmost managed ancestor for the given file
     */
    public File getTopmostManagedAncestor (File file) {
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "looking for managed parent for {0}", new Object[] { file });
        }
        if(unversionedParents.contains(file)) {
            Subversion.LOG.fine(" cached as unversioned");
            return null;
    }
        File metadataRoot = null;
        if (SvnUtils.isPartOfSubversionMetadata(file)) {
            Subversion.LOG.fine(" part of metaddata");
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
        File topmost = null;
        Set<File> done = new HashSet<File>();
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
            boolean forbiddenFolder = org.netbeans.modules.versioning.util.Utils.isForbiddenFolder(file);
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
            Subversion.LOG.log(Level.FINE, " storing unversioned");
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
        return topmost;
    }

    FileStatusProvider getVCSAnnotator() {
        return fileStatusProvider;
    }

    VCSInterceptor getVCSInterceptor() {
        return filesystemHandler;
    }

    private List<ISVNNotifyListener> getSVNNotifyListeners() {
        if(svnNotifyListeners == null) {
            svnNotifyListeners = new ArrayList<ISVNNotifyListener>();
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
    public void refreshAnnotations(File... files) {
        Set<File> s = new HashSet<File>(Arrays.asList(files));
        support.firePropertyChange(PROP_ANNOTATIONS_CHANGED, null, s);
    }

    /**
     * Refreshes all textual annotations, badges and sidebars for the given files.
     *
     * @param files files to chage the annotations and sidebars for
     */
    public void refreshAnnotationsAndSidebars (File... files) {
        Set<File> s = files == null ? null : new HashSet<>(Arrays.asList(files));
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

    public void getOriginalFile(File workingCopy, File originalFile) {
        FileInformation info = fileStatusCache.getStatus(workingCopy);
        if ((info.getStatus() & STATUS_DIFFABLE) == 0) {
            return;
        }

        File original = null;
        try {
            SvnClientFactory.checkClientAvailable();
            original = VersionsCache.getInstance().getBaseRevisionFile(workingCopy);
            if (original == null) {
                throw new IOException("Unable to get BASE revision of " + workingCopy);
            }
            org.netbeans.modules.versioning.util.Utils.copyStreamsCloseAll(new FileOutputStream(originalFile), new FileInputStream(original));
        } catch (IOException e) {
            LOG.log(Level.INFO, "Unable to get original file", e);
        } catch (SVNClientException ex) {
            Subversion.LOG.log(Level.INFO, "Subversion.getOriginalFile: file is managed but svn client is unavailable (file {0})", workingCopy.getAbsolutePath()); //NOI18N
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
        List<VCSHyperlinkProvider> providersList = new ArrayList<VCSHyperlinkProvider>(providersCol.size());
        providersList.addAll(providersCol);
        return Collections.unmodifiableList(providersList);
    }
}
