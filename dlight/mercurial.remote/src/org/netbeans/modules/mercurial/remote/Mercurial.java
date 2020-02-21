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
package org.netbeans.modules.mercurial.remote;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.remotefs.versioning.api.RootsToFile;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.remote.ui.repository.HgURL;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VCSHyperlinkProvider;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;

/**
 * Main entry point for Mercurial functionality, use getInstance() to get the Mercurial object.
 *
 * 
 */
public class Mercurial {
    public static final int HG_FETCH_20_REVISIONS = 20;
    public static final int HG_FETCH_50_REVISIONS = 50;
    public static final int HG_FETCH_ALL_REVISIONS = -1;
    public static final int HG_NUMBER_FETCH_OPTIONS = 3;
    public static final int HG_NUMBER_TO_FETCH_DEFAULT = 7;
    public static final int HG_MAX_REVISION_COMBO_SIZE = HG_NUMBER_TO_FETCH_DEFAULT + HG_NUMBER_FETCH_OPTIONS;

    public static final String MERCURIAL_OUTPUT_TAB_TITLE = org.openide.util.NbBundle.getMessage(Mercurial.class, "CTL_Mercurial_DisplayName"); // NOI18N
    public static final String CHANGESET_STR = "changeset:"; // NOI18N

    public static final String PROP_ANNOTATIONS_CHANGED = "annotationsChanged"; // NOI18N
    static final String PROP_VERSIONED_FILES_CHANGED = "versionedFilesChanged"; // NOI18N
    public static final String PROP_CHANGESET_CHANGED = "changesetChanged"; // NOI18N
    static final String PROP_HEAD_CHANGED = "headChanged"; //NOI18N

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.mercurial.remote"); // NOI18N
    public static final Logger STATUS_LOG = Logger.getLogger("org.netbeans.modules.mercurial.remote.status"); //NOI18N
    
    private static final int STATUS_DIFFABLE =
            FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_CONFLICT |
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY;

    private static final String MERCURIAL_SUPPORTED_VERSION_093 = "0.9.3"; // NOI18N
    private static final String MERCURIAL_SUPPORTED_VERSION_094 = "0.9.4"; // NOI18N
    private static final String MERCURIAL_SUPPORTED_VERSION_095 = "0.9.5"; // NOI18N
    private static final String MERCURIAL_SUPPORTED_VERSION_100 = "1.0"; // NOI18N
    private static Mercurial instance;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private RootsToFile rootsToFile;
    private static final List<String> allowableFolders;
    static {
        List<String> files = new ArrayList<>();
        try {
            String allowable = System.getProperty("versioning.hg.allowableFolders", "/"); //NOI18N
            files.addAll(Arrays.asList(allowable.split("\\;"))); //NOI18N
            files.remove(""); //NOI18N
        } catch (Exception e) {
            LOG.log(Level.INFO, e.getMessage(), e);
        }
        allowableFolders = files;
    }

    public static synchronized Mercurial getInstance() {
        if (instance == null) {
            instance = new Mercurial();
            instance.init();
        }
        return instance;
    }

    private MercurialAnnotator   mercurialAnnotator;
    private MercurialInterceptor mercurialInterceptor;
    private HgHistoryProvider historyProvider;
    
    private FileStatusCache     fileStatusCache;
    private HashMap<HgURL, RequestProcessor>   processorsToUrl;
    
    private static final class Version {
        /**
         * true if hg is present and it's version is supported
         */
        private boolean goodVersion;
        private String version;
        /**
         * true if hg version command has been invoked
         */
        private boolean gotVersion;
    }
    private final Map<FileSystem, Version> versions = new HashMap<>();
    private final Object versionsLock = new Object();

    private Result<? extends VCSHyperlinkProvider> hpResult;
    private RequestProcessor parallelRP;

    private Mercurial() {
    }

    private void init() {
        fileStatusCache = new FileStatusCache(this);
        mercurialAnnotator = new MercurialAnnotator(fileStatusCache);
        mercurialInterceptor = new MercurialInterceptor(this, fileStatusCache);
        
        int statisticsFrequency;
        String s = System.getProperty("mercurial.root.stat.frequency", "0"); //NOI18N
        try {
            statisticsFrequency = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            statisticsFrequency = 0;
        }
        rootsToFile = new RootsToFile(new RootsToFile.Callback() {
            @Override
            public boolean repositoryExistsFor (VCSFileProxy file) {
                return HgUtils.hgExistsFor(file);
            }

            @Override
            public VCSFileProxy getTopmostManagedAncestor (VCSFileProxy file) {
                return Mercurial.this.getTopmostManagedAncestor(file);
            }
        }, Logger.getLogger("org.netbeans.modules.mercurial.RootsToFile"), statisticsFrequency); //NOI18N
        for(FileSystem fs : VCSFileProxySupport.getConnectedFileSystems()) {
            asyncInit(VCSFileProxy.createFileProxy(fs.getRoot())); // Does the Hg check but postpones querying user until menu is activated
        }
    }

    void register (final MercurialVCS mvcs) {
        fileStatusCache.addPropertyChangeListener(mvcs);
        addPropertyChangeListener(mvcs);
        //TODO: support shelve, see bug #249105
//        getRequestProcessor().post(new Runnable() {
//            @Override
//            public void run () {
//                for(FileSystem fs : VCSFileProxySupport.getFileSystems()) {
//                    if (isAvailable(VCSFileProxy.createFileProxy(fs.getRoot()), false, false)) {
//                        ShelveChangesActionsRegistry.getInstance().registerAction(mvcs, ShelveChangesAction.getProvider());
//                    }
//                }
//            }
//        });
    }

    public void asyncInit(final VCSFileProxy root) {
        getV(root).goodVersion = false;
        RequestProcessor rp = getRequestProcessor();
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "Remote '"+VCSFileProxySupport.getFileSystem(root)+"' Mercurial subsystem initialized", new Exception()); //NOI18N
        }
        Runnable init = new Runnable() {
            @Override
            public void run() {
                synchronized(Mercurial.this) {
                    checkVersionIntern(root);
                }
            }

        };
        rp.post(init);
    }

    private Version getV(final VCSFileProxy root) {
        FileSystem fileSystem = VCSFileProxySupport.getFileSystem(root);
        Version v;
        synchronized(versionsLock) {
            v = versions.get(fileSystem);
            if (v == null) {
                v = new Version();
                versions.put(fileSystem, v);
            }
        }
        return v;
    }

    private void checkVersionIntern(VCSFileProxy root) {
        Version v = getV(root);
        v.version = HgCommand.getHgVersion(root);
        if (v.version != null) {
            v.goodVersion = isSupportedVersion(v.version);
        } else {
            v.goodVersion = false;
        }
        LOG.log(v.goodVersion ? Level.FINE : Level.INFO, "version: {0}", v.version); // NOI18N
        v.gotVersion = true;
    }

    private boolean isSupportedVersion(String version) {
        if(version.startsWith(MERCURIAL_SUPPORTED_VERSION_093) ||
           version.startsWith(MERCURIAL_SUPPORTED_VERSION_094) ||
           version.startsWith(MERCURIAL_SUPPORTED_VERSION_095) ||
           version.startsWith(MERCURIAL_SUPPORTED_VERSION_100))
        {
            return true;
        }
        if(version.startsWith("0.")) { //NOI18N
            // seems to be older then 0.93
            return false;
        }
        return true;
    }

    public boolean isAvailable (VCSFileProxy root) {
        return isAvailable(root, false, false);
    }

    /**
     * Tests if hg is or is not available
     * @param forceCheck if version command has not been invoked yet and forceCheck is true, it will be, otherwise the command will be skipped
     * @param notifyUI if true and hg is not available, a dialog will be shown and a message will be printed into a logger
     * @return
     */
    public boolean isAvailable (VCSFileProxy root, boolean forceCheck, boolean notifyUI) {
        if (!VCSFileProxySupport.isConnectedFileSystem(VCSFileProxySupport.getFileSystem(root))) {
            return false;
        }
        Version v = getV(root);
        synchronized(this) {
            if (!v.gotVersion) {
                // version has not been scanned yet, run the version command
                LOG.log(Level.FINE, "Call to hg version not finished"); // NOI18N
                if(forceCheck) {
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.log(Level.FINEST, "isAvailable performed", new Exception()); //NOI18N
                    }
                    checkVersionIntern(root);
                } else {
                    return true;
                }
            }
        }
        if (v.version != null && !v.goodVersion) {
            // hg is present but it's version is unsupported
            // a warning message is printed into log, always only once per netbeans session
            OutputLogger logger = getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
            logger.outputInRed(NbBundle.getMessage(Mercurial.class, "MSG_USING_UNRECOGNIZED_VERSION_MSG", v.version)); // NOI18N);
            logger.closeLog();
            LOG.log(Level.WARNING, "Using an unsupported hg version: {0}", v.version); //NOI18N
            v.goodVersion = true; // do not show the warning next time
        } else if (v.version == null) {
            // hg is not present at all, show a warning dialog
            if (notifyUI) {
                OutputLogger logger = getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
                logger.outputInRed(NbBundle.getMessage(Mercurial.class, "MSG_VERSION_NONE_OUTPUT_MSG")); // NOI18N);
                HgUtils.warningDialog(Mercurial.class, "MSG_VERSION_NONE_TITLE", "MSG_VERSION_NONE_MSG"); //NOI18N
                logger.closeLog();
                LOG.warning("Hg is not available");     //NOI18N
            }
        }
        return v.goodVersion; // true if hg is present
    }

    public MercurialAnnotator getMercurialAnnotator() {
        return mercurialAnnotator;
    }

    MercurialInterceptor getMercurialInterceptor() {
        return mercurialInterceptor;
    }

    /**
     * Gets the File Status Cache for the mercurial repository
     *
     * @return FileStatusCache for the repository
     */
    public FileStatusCache getFileStatusCache() {
        return fileStatusCache;
    }

    /**
     * Runs a given callable and refreshes cached modification timestamp of the repository's hg folder after
     * @param callable code to run
     * @param repository owner of the hg folder to refresh
     * @param commandName name of the hg command if available
     */
    public <T> T runWithoutExternalEvents (VCSFileProxy repository, String commandName, Callable<T> callable) throws Exception {
        return getMercurialInterceptor().runWithoutExternalEvents(repository, commandName, callable);
    }

    /**
     * Returns a set of known repository roots (those visible or open in IDE)
     * @param repositoryRoot
     * @return
     */
    public Set<VCSFileProxy> getSeenRoots (VCSFileProxy repositoryRoot) {
        return getMercurialInterceptor().getSeenRoots(repositoryRoot);
    }

   /**
     * Tests whether a file or directory should receive the STATUS_NOTVERSIONED_NOTMANAGED status.
     * All files and folders that have a parent with CVS/Repository file are considered versioned.
     *
     * @param file a file or directory
     * @return false if the file should receive the STATUS_NOTVERSIONED_NOTMANAGED status, true otherwise
     */
    public boolean isManaged(VCSFileProxy file) {
        return VersioningSupport.getOwner(file) instanceof MercurialVCS && !HgUtils.isPartOfMercurialMetadata(file);
    }

    public VCSFileProxy getRepositoryRoot(VCSFileProxy file) {
        return rootsToFile.getRepositoryRoot(file);
    }

   /**
     * Uses content analysis to return the mime type for files.
     *
     * @param file file to examine
     * @return String mime type of the file (or best guess)
     */
    public String getMimeType(VCSFileProxy file) {
        FileObject fo = file.toFileObject();
        String foMime;
        if (fo == null) {
            foMime = "content/unknown"; // NOI18N
        } else {
            foMime = fo.getMIMEType();
            if ("content/unknown".equals(foMime)) { // NOI18N
                foMime = "text/plain"; // NOI18N
            }
        }
        if ((fileStatusCache.getStatus(file).getStatus() & FileInformation.STATUS_VERSIONED) == 0) {
            return HgUtils.isFileContentBinary(file) ? "application/octet-stream" : foMime; // NOI18N
        } else {
            return foMime;
        }
    }

    public void versionedFilesChanged() {
        support.firePropertyChange(PROP_VERSIONED_FILES_CHANGED, null, null);
    }

    public void refreshAllAnnotations() {
        support.firePropertyChange(PROP_ANNOTATIONS_CHANGED, null, null);
    }

    public void refreshAnnotations (Set<VCSFileProxy> files) {
        support.firePropertyChange(PROP_ANNOTATIONS_CHANGED, null, files);
    }
    
    public void changesetChanged(VCSFileProxy repository) {
        support.firePropertyChange(PROP_CHANGESET_CHANGED, repository, null);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public MercurialVCS getMercurialVCS() {
        for(PropertyChangeListener listener : support.getPropertyChangeListeners()) {
            if (listener instanceof MercurialVCS) {
                return (org.netbeans.modules.mercurial.remote.MercurialVCS) listener;
            }
        }
        return null;
    }
    
    public void getOriginalFile(VCSFileProxy workingCopy, VCSFileProxy originalFile) {
        FileInformation info = fileStatusCache.getStatus(workingCopy);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "getOriginalFile: {0} {1}", new Object[] {workingCopy, info}); // NOI18N
        }
        // original file only for diffable status
        if ((info.getStatus() & STATUS_DIFFABLE) == 0) {
            if ((info.getStatus() & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) != 0 && info.getStatus(null) != null && info.getStatus(null).getOriginalFile() != null) {
                // for copied files cat the original file
                workingCopy = info.getStatus(null).getOriginalFile();
            } else {
                // for others noop
                return;
            }
        }

        // We can get status returned as UptoDate instead of LocallyNew
        // because refreshing of status after creation has been scheduled
        // but may not have happened yet.

        try {
            VCSFileProxy original = VersionsCache.getInstance().getFileRevision(workingCopy, HgRevision.BASE);
            if (original == null) {
                return;
            }
            org.netbeans.modules.versioning.util.Utils.copyStreamsCloseAll(VCSFileProxySupport.getOutputStream(originalFile), original.getInputStream(false));
            VCSFileProxySupport.delete(original);
        } catch (IOException e) {
            Logger.getLogger(Mercurial.class.getName()).log(Level.INFO, "Unable to get original file", e); // NOI18N
        }
    }

    /**
     * Serializes all Hg requests (moves them out of AWT).
     */
    public RequestProcessor getRequestProcessor() {
        return getRequestProcessor((HgURL) null);
    }

    /**
     * Request processor for parallel tasks
     * @return
     */
    public RequestProcessor getParallelRequestProcessor() {
        if (parallelRP == null) {
            parallelRP = new RequestProcessor("Mercurial.ParallelRP", 5, true); //NOI18N
        }
        return parallelRP;
    }

    /**
     * Serializes all Hg requests (moves them out of AWT).
     */
    public RequestProcessor getRequestProcessor(VCSFileProxy file) {
        return getRequestProcessor(new HgURL(file));
    }

    /**
     * @param  url  URL or {@code null}
     */
    public RequestProcessor getRequestProcessor(HgURL url) {
        if(processorsToUrl == null) {
            processorsToUrl = new HashMap<>();
        }

        RequestProcessor rp = processorsToUrl.get(url);   //'url' can be null
        if (rp == null) {
            String rpName = "MercurialRemote - "                        //NOI18N
                           + (url != null ? url.toString() : "ANY_KEY");//NOI18N
            rp = new RequestProcessor(rpName, 1, true);
            processorsToUrl.put(url, rp);
        }
        return rp;
    }

    public void clearRequestProcessor(HgURL url) {
        if(processorsToUrl != null && url != null) {
             processorsToUrl.remove(url);
        }
    }

    public void notifyFileChanged(VCSFileProxy file) {
        fileStatusCache.notifyFileChanged(file);
    }

    /**
     *
     * @param repositoryRoot String of Mercurial repository so that logger writes to correct output tab. Can be null
     * in which case the logger will not print anything
     * @return OutputLogger logger to write to
     */
    public OutputLogger getLogger(String repositoryRoot) {
        return OutputLogger.getLogger(repositoryRoot);
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
            return Collections.<VCSHyperlinkProvider>emptyList();
        }
        Collection<? extends VCSHyperlinkProvider> providersCol = hpResult.allInstances();
        List<VCSHyperlinkProvider> providersList = new ArrayList<>(providersCol.size());
        providersList.addAll(providersCol);
        return Collections.unmodifiableList(providersList);
    }

    /**
     * Returns scanned version or null if has not been scanned yet
     * @return
     */
    public String getVersion (VCSFileProxy root) {
        return getV(root).version;
    }

    private final Set<VCSFileProxy> knownRoots = Collections.synchronizedSet(new HashSet<VCSFileProxy>());
    private final Set<VCSFileProxy> unversionedParents = Collections.synchronizedSet(new HashSet<VCSFileProxy>(20));
    VCSFileProxy getTopmostManagedAncestor(VCSFileProxy file) {
        if (file.toFile() != null) {
            return null;
        }
        if (!isAllowable(file)) {
            return null;
        }
        if (Mercurial.LOG.isLoggable(Level.FINE)) {
            Mercurial.LOG.log(Level.FINE, "getTopmostManagedParent {0}", new Object[] { file });
        }
        if (!VCSFileProxySupport.isConnectedFileSystem(VCSFileProxySupport.getFileSystem(file))) {
            return null;
        }
        long t = System.currentTimeMillis();
        if(unversionedParents.contains(file)) {
            Mercurial.LOG.fine(" cached as unversioned");
            return null;
        }
        if (Mercurial.LOG.isLoggable(Level.FINE)) {
            Mercurial.LOG.log(Level.FINE, "getTopmostManagedParent {0}", new Object[] { file });
        }
        VCSFileProxy parent = getKnownParent(file);
        if(parent != null) {
            if (Mercurial.LOG.isLoggable(Level.FINE)) {
                Mercurial.LOG.log(Level.FINE, "  getTopmostManagedParent returning known parent " + parent);
            }
            return parent;
        }

        if (HgUtils.isPartOfMercurialMetadata(file)) {
            for (;file != null; file = file.getParentFile()) {
                if (HgUtils.isAdministrative(file)) {
                    file = file.getParentFile();
                    // the parent folder of .hg metadata cannot be unversioned, it's nonsense
                    unversionedParents.remove(file);
                    break;
                }
            }
        }
        Set<VCSFileProxy> done = new HashSet<>();
        VCSFileProxy topmost = null;
        for (;file != null; file = file.getParentFile()) {
            if(unversionedParents.contains(file)) {
                if (Mercurial.LOG.isLoggable(Level.FINE)) {
                    Mercurial.LOG.log(Level.FINE, " already known as unversioned {0}", new Object[] { file });
                }
                break;
            }
            if (VersioningSupport.isExcluded(file)) {
                break;
            }
            // is the folder a special one where metadata should not be looked for?
            boolean forbiddenFolder = Utils.isForbiddenFolder(file);
            if (!forbiddenFolder && HgUtils.hgExistsFor(file)){
                if (Mercurial.LOG.isLoggable(Level.FINE)) {
                    Mercurial.LOG.log(Level.FINE, " found managed parent {0}", new Object[] { file });
                }
                done.clear();   // all folders added before must be removed, they ARE in fact managed by hg
                topmost =  file;
            } else {
                if (Mercurial.LOG.isLoggable(Level.FINE)) {
                    Mercurial.LOG.log(Level.FINE, " found unversioned {0}", new Object[] { file });
                }
                if(file.exists()) { // could be created later ...
                    done.add(file);
                }
            }
        }
        if(done.size() > 0) {
            Mercurial.LOG.log(Level.FINE, " storing unversioned");
            unversionedParents.addAll(done);
        }
        if(Mercurial.LOG.isLoggable(Level.FINE)) {
            Mercurial.LOG.log(Level.FINE, " getTopmostManagedParent returns {0} after {1} millis", new Object[] { topmost, System.currentTimeMillis() - t });
        }
        if(topmost != null) {
            if (knownRoots.add(topmost)) {
                String homeDir = System.getProperty("user.home"); //NOI18N
                if (homeDir != null && homeDir.startsWith(topmost.getPath())) {
                    LOG.log(Level.WARNING, "Home folder {0} lies under a hg versioned root {1}." //NOI18N
                            + "Expecting lots of performance issues.", new Object[] { homeDir, topmost }); //NOI18N
                }
            }
        }

        return topmost;
    }
    
   private VCSFileProxy getKnownParent(VCSFileProxy file) {
        VCSFileProxy[] roots = knownRoots.toArray(new VCSFileProxy[knownRoots.size()]);
        VCSFileProxy knownParent = null;
        for (VCSFileProxy r : roots) {
            if(!VersioningSupport.isExcluded(file) && VCSFileProxySupport.isAncestorOrEqual(r, file) && (knownParent == null || VCSFileProxySupport.isAncestorOrEqual(knownParent, r))) {
                knownParent = r;
            }
        }
        return knownParent;
    }

    public void clearAncestorCaches() {
        unversionedParents.clear();
        knownRoots.clear();
        rootsToFile.clear();
    }

    public HgHistoryProvider getMercurialHistoryProvider() {
        if(historyProvider == null) {
            historyProvider = new HgHistoryProvider();
        }
        return historyProvider;
    }

    public void historyChanged (VCSFileProxy repository) {
        Set<VCSFileProxy> openFiles = HgUtils.getOpenedFiles(repository);
        if (!openFiles.isEmpty()) {
            support.firePropertyChange(PROP_HEAD_CHANGED, null, openFiles);
            if (historyProvider != null) {
                historyProvider.fireHistoryChange(openFiles.toArray(new VCSFileProxy[openFiles.size()]));
            }
        }
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
}
