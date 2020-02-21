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

import org.netbeans.modules.subversion.remote.api.ISVNStatus;
import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.DateFormat;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.ISVNLock;
import org.netbeans.modules.subversion.remote.api.ISVNProperty;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNConflictDescriptor;
import org.netbeans.modules.subversion.remote.api.SVNNodeKind;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.turbo.CustomProviders;
import org.netbeans.modules.turbo.Turbo;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.util.DelayScanRegistry;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 * Central part of Subversion status management, deduces and caches statuses of files under version control.
 * 
 * 
 */
public class FileStatusCache {

    /**
     * Indicates that status of a file changed and listeners SHOULD check new status 
     * values if they are interested in this file.
     * First parameter: File whose status changes
     * Second parameter: old FileInformation object, may be null
     * Third parameter: new FileInformation object
     */
    public static final Object EVENT_FILE_STATUS_CHANGED = new Object();

    /**
     * Property indicating status of cache readiness
     */
    public static final String PROP_CACHE_READY = "subversion.cache.ready"; //NOI18N

    /**
     * A special map saying that no file inside the folder is managed.
     */ 
    private static final Map<VCSFileProxy, FileInformation> NOT_MANAGED_MAP = new NotManagedMap();
       
    public static final RepositoryStatus REPOSITORY_STATUS_UNKNOWN  = null;

    // Constant FileInformation objects that can be safely reused
    // Files that have a revision number cannot share FileInformation objects 
    private static final FileInformation FILE_INFORMATION_EXCLUDED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, false);
    private static final FileInformation FILE_INFORMATION_EXCLUDED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, true);
    private static final FileInformation FILE_INFORMATION_UPTODATE_DIRECTORY = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, true);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, false);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, true);
    private static final FileInformation FILE_INFORMATION_UNKNOWN = new FileInformation(FileInformation.STATUS_UNKNOWN, false);

    private static final int CACHE_SIZE_WARNING_THRESHOLD = 100000; // log when cache gets too big and steps over this threshold

    /**
     * Auxiliary conflict file siblings
     * After update: *.r#, *.mine
     * After merge: *.working, *.merge-right.r#, *.metge-left.r#
     */
    private static final Pattern auxConflictPattern = Pattern.compile("(.*?)\\.((r\\d+)|(mine)|" + // NOI18N
        "(working)|(merge-right\\.r\\d+)|((merge-left.r\\d+)))$"); // NOI18N

    /*
     * Holds three kinds of information: what folders we have scanned, what files we have found
     * and what statuses of these files are.
     * If a directory is not found as a key in the map, we have not scanned it yet.
     * If it has been scanned, it maps to a Set of files that were found somehow out of sync with the
     * repository (have any other status then up-to-date). In case all files are up-to-date, it maps
     * to Collections.EMPTY_MAP. Entries in this map are created as directories are scanne, are never removed and
     * are updated by the refresh method.
     */

    private final Turbo     turbo;
    
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.subversion.remote.FileStatusCache"); //NOI18N
    /**
     * Indicates if the cache index is ready 
     */
    private boolean ready = false;

    /**
     * Identifies attribute that holds information about all non STATUS_VERSIONED_UPTODATE files.
     *
     * <p>Key type: File identifying a folder
     * <p>Value type: Map&lt;File, FileInformation>
     */
    private final String FILE_STATUS_MAP = DiskMapTurboProvider.ATTR_STATUS_MAP;

    private DiskMapTurboProvider        cacheProvider;    

    private RequestProcessor rp = new RequestProcessor("Subversion - file status refresh", 1); // NOI18N    
    private final LinkedHashSet<VCSFileProxy> filesToRefresh = new LinkedHashSet<>();
    private RequestProcessor.Task refreshTask;
    private final FileLabelCache labelsCache;
    private Annotator annotator;

    private long refreshedFilesCount;
    private static final boolean EXCLUDE_SYMLINKS = "true".equals(System.getProperty("versioning.subversion.doNotFollowSymlinks", "false")); //NOI18N
    private static final Logger LABELS_CACHE_LOG = Logger.getLogger(FileStatusCache.class.getName());
    private static final FileLabelInfo FAKE_LABEL_INFO = new FileLabelInfo("", "", "", "", "", ""); //NOI18N
    private static final boolean VERSIONING_ASYNC_ANNOTATOR = !"false".equals(System.getProperty("versioning.asyncAnnotator", "true")); //NOI18N

    FileStatusCache() {
        cacheProvider = new DiskMapTurboProvider();
        
        turbo = Turbo.createCustom(new CustomProviders() {
            private final Set providers = Collections.singleton(cacheProvider);
            @Override
            public Iterator providers() {
                return providers.iterator();
            }
        }, 200, 5000);
    
        refreshTask = rp.create( new Runnable() {
            @Override
            public void run() {
                if (DelayScanRegistry.getInstance().isDelayed(refreshTask, LOG, "FileStatusCache.refreshTask")) { //NOI18N
                    return;
                }
                long startTime = 0;
                long files = 0;
                boolean logEnabled = LOG.isLoggable(Level.FINE);
                if (logEnabled) {
                    // logging
                    startTime = System.currentTimeMillis();
                }
                VCSFileProxy fileToRefresh;
                do {
                    fileToRefresh = null;
                    synchronized(filesToRefresh) {
                        Iterator<VCSFileProxy> it = filesToRefresh.iterator();
                        if (it.hasNext()) {
                            fileToRefresh = it.next();
                            it.remove();
                        }
                    }
                    if (fileToRefresh != null) {
                        refresh(fileToRefresh, REPOSITORY_STATUS_UNKNOWN);
                        if (logEnabled) {
                            // logging
                            ++files;
                            ++refreshedFilesCount;
                        }
                    }
                } while (fileToRefresh != null);
                if (logEnabled) {
                    LOG.log(Level.FINE, "refreshTask lasted {0} ms for {1} files, {2} files refreshed so far", new Object[] { //NOI18N
                        System.currentTimeMillis() - startTime, files, refreshedFilesCount});
                }
            }
        });
        labelsCache = new FileLabelCache(this);
    }

    void setAnnotator(Annotator annotator) {
        this.annotator = annotator;
    }


    // --- Public interface -------------------------------------------------

    /**
     * Evaluates if there are any files with the given status under the given roots
     *
     * @param context context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return true if there are any files with the given status otherwise false
     */
    public boolean containsFiles(Context context, int includeStatus, boolean addExcluded) {
        long ts = System.currentTimeMillis();
        try {
            VCSFileProxy[] roots = context.getRootFiles();
            // check all files underneath the roots
            return containsFiles(roots, includeStatus, addExcluded);
        } finally {
            if(LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, " containsFiles(Context, int) took {0}", (System.currentTimeMillis() - ts));
            }
        }
    }

    /**
     * Evaluates if there are any files with the given status under the given roots
     *
     * @param rootFiles context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return true if there are any files with the given status otherwise false
     */
    public boolean containsFiles(Set<VCSFileProxy> rootFiles, int includeStatus, boolean addExcluded) {
        long ts = System.currentTimeMillis();
        try {
            VCSFileProxy[] roots = rootFiles.toArray(new VCSFileProxy[rootFiles.size()]);
            return containsFiles(roots, includeStatus, addExcluded);
        } finally {
            if(LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, " containsFiles(Set<File>, int) took {0}", (System.currentTimeMillis() - ts));
            }
        }
    }

    private boolean containsFiles(VCSFileProxy[] roots, int includeStatus, boolean addExcluded) {
        // get as deep as possible, so Turbo.readEntry() - which accesses io - gets called the least times
        // in such case we may end up with just access to io - getting the status of indeed modified file
        // the other way around it would check status for all directories along the path
        for (VCSFileProxy root : roots) {
            if(containsFilesIntern(cacheProvider.getIndexValues(root, includeStatus), includeStatus, !VersioningSupport.isFlat(root), addExcluded)) {
                return true;
            }
        }

        // check to roots if they apply to the given status
        if (containsFilesIntern(roots, includeStatus, false, addExcluded)) {
            return true;
        }
        return false;
    }

    private boolean containsFilesIntern(VCSFileProxy[] indexRoots, int includeStatus, boolean recursively, boolean addExcluded) {
        if(indexRoots == null || indexRoots.length == 0) {
            return false;
        }
        // get as deep as possible, so Turbo.readEntry() - which accesses io - gets called the least times
        // in such case we may end up with just access to io - getting the status of indeed modified file
        // the other way around it would check status for all directories along the path
        for (VCSFileProxy root : indexRoots) {
            VCSFileProxy[] indexValues = cacheProvider.getIndexValues(root, includeStatus);
            if(recursively && containsFilesIntern(indexValues, includeStatus, recursively, addExcluded)) {
                return true;
            }
        }
        for (VCSFileProxy root : indexRoots) {
            FileInformation fi = getCachedStatus(root);
            if( (fi != null && (fi.getStatus() & includeStatus) != 0) &&
                (addExcluded || !SvnModuleConfig.getDefault(VCSFileProxySupport.getFileSystem(root)).isExcludedFromCommit(root.getPath())))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Lists <b>modified files</b> and all folders that are known to be inside
     * this folder. There are locally modified files present
     * plus any files that exist in the folder in the remote repository. It
     * returns all folders, including CVS folders.
     *    
     * @param dir folder to list
     * @return
     */
    public VCSFileProxy [] listFiles(VCSFileProxy dir) {
        Set<VCSFileProxy> files = getScannedFiles(dir).keySet();
        return files.toArray(new VCSFileProxy[files.size()]);
    }

    /**
     * Lists <b>interesting files</b> that are known to be inside given folders.
     * These are locally and remotely modified and ignored files.
     *
     * <p>Comapring to CVS this method returns both folders and files.
     *
     * @param roots context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return File [] array of interesting files
     */
    public VCSFileProxy [] listFiles(VCSFileProxy[] roots, int includeStatus) { 
        long ts = System.currentTimeMillis();
        try {
            Set<VCSFileProxy> set = new HashSet<>();

            // get all files with given status underneath the roots files;
            // do it recusively if root isn't a flat folder
            for (VCSFileProxy root : roots) {
                Set<VCSFileProxy> files =
                        listFilesIntern(
                            cacheProvider.getIndexValues(root, includeStatus),
                            includeStatus,
                            !VersioningSupport.isFlat(root));
                set.addAll(files);
            }

            // check also the root files for status and add them eventualy
            set.addAll(listFilesIntern(roots, includeStatus, false));

            return set.toArray(new VCSFileProxy[set.size()]);
        } finally {
            if(LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, " listFiles(File[], int, boolean) took {0}", (System.currentTimeMillis() - ts));
            }
        }
    }

    /**
     * Lists <b>interesting files</b> that are known to be inside given folders.
     * These are locally and remotely modified and ignored files.
     *
     * <p>Comapring to CVS this method returns both folders and files.
     *
     * @param context context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return File [] array of interesting files
     */
    public VCSFileProxy [] listFiles(Context context, int includeStatus) {
        long ts = System.currentTimeMillis();
        try {
            Set<VCSFileProxy> set = new HashSet<>();
            VCSFileProxy [] roots = context.getRootFiles();

            // list all files applying to the status with
            // root being their ancestor or equal
            set.addAll(Arrays.asList(listFiles(roots, includeStatus)));

            // filter exclusions
            if (context.getExclusions().size() > 0) {
                for (VCSFileProxy excluded : context.getExclusions()) {
                    for (Iterator<VCSFileProxy> i = set.iterator(); i.hasNext();) {
                        VCSFileProxy file = i.next();
                        if (SvnUtils.isParentOrEqual(excluded, file)) {
                            i.remove();
                        }
                    }
                }
            }
            return set.toArray(new VCSFileProxy[set.size()]);
        } finally {
            if(LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, " listFiles(Context, int) took {0}", (System.currentTimeMillis() - ts));
            }
        }
    }

    private Set<VCSFileProxy> listFilesIntern(VCSFileProxy[] roots, int includeStatus, boolean recursively) {
        if(roots == null || roots.length == 0) {
            return Collections.emptySet();
        }
        Set<VCSFileProxy> ret = new HashSet<>();
        for (VCSFileProxy root : roots) {
            if(recursively) {
                ret.addAll(listFilesIntern(cacheProvider.getIndexValues(root, includeStatus), includeStatus, recursively));
            }
            FileInformation fi = getCachedStatus(root);
            if(fi == null || (fi.getStatus() & includeStatus) == 0) {
                continue;
            }
            ret.add(root);
        }
        return ret;
    }

    /**
     * Determines the versioning status of a file. This method accesses disk and may block for a long period of time.
     * 
     * @param file file to get status for
     * @return FileInformation structure containing the file status
     * @see FileInformation
     */ 
    public FileInformation getStatus(VCSFileProxy file) {
        if (SvnUtils.isAdministrative(file)) {
            return FILE_INFORMATION_NOTMANAGED_DIRECTORY;
        }
        VCSFileProxy dir = file.getParentFile();
        if (dir == null) {
            return FILE_INFORMATION_NOTMANAGED; //default for filesystem roots 
        }
        Map<VCSFileProxy, FileInformation> files = getScannedFiles(dir);
        if (files == NOT_MANAGED_MAP) {
            return FILE_INFORMATION_NOTMANAGED;
        }
        FileInformation fi = files.get(file);
        if (fi != null) {
            return fi;            
        }
        if (!exists(file)) {
            return FILE_INFORMATION_UNKNOWN;
        }
        if (file.isDirectory()) {
            return refresh(file, REPOSITORY_STATUS_UNKNOWN);
        } else {
            return new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
        }
    }

    /**
     * Looks up cached file status.
     * 
     * @param file file to check
     * @return give file's status or null if the file's status is not in cache
     */ 
    public FileInformation getCachedStatus(VCSFileProxy file) {
        VCSFileProxy parent = file.getParentFile();
        if (parent == null) {
            return FILE_INFORMATION_NOTMANAGED_DIRECTORY;
        }
        Map<VCSFileProxy, FileInformation> files = (Map<VCSFileProxy, FileInformation>) turbo.readEntry(parent, FILE_STATUS_MAP);
        return files != null ? files.get(file) : null;
    }

    /**
     * 
     * Refreshes the given files asynchrously
     * 
     * @param files files to be refreshed
     */
    public void refreshAsync(List<VCSFileProxy> files) {
        refreshAsync(false, files.toArray(new VCSFileProxy[files.size()]));
    }
    
    /**
     * 
     * Refreshes the given files asynchrously
     * 
     * @param files files to be refreshed
     */
    public void refreshAsync(VCSFileProxy... files) {
        refreshAsync(false, files);
    }
    
    /**
     * 
     * Refreshes the given files asynchrously
     * 
     * @param files files to be refreshed
     * @param recursively if true all children are also refreshed
     */
    public void refreshAsync(final boolean recursively, final VCSFileProxy... files) {
        if (files == null || files.length == 0) {
            return;
        }
        Subversion.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                synchronized (filesToRefresh) {
                    for (VCSFileProxy file : files) {
                        if (recursively) {
                            filesToRefresh.addAll(SvnUtils.listManagedRecursively(file));
                        } else {
                            filesToRefresh.add(file);
                        }
                    }
                }
                refreshTask.schedule(200);
            }
        });
    }

    /**
     * Refreshes the status of the file given the repository status. Repository status is filled
     * in when this method is called while processing server output. 
     *
     * @param file
     * @param repositoryStatus
     */ 
    public FileInformation refresh(VCSFileProxy file, RepositoryStatus repositoryStatus) {
        return refresh(file, repositoryStatus, false);
    }
    
    /**
     * Refreshes status of all files inside given context. Files that have some remote status, eg. REMOTELY_ADDED
     * are brought back to UPTODATE.
     * DOES NOT refresh ignored files.
     * 
     * @param ctx context to refresh
     */ 
    public void refreshCached(Context ctx) {
        VCSFileProxy [] files = listFiles(ctx, ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED);
        for (int i = 0; i < files.length; i++) {
            VCSFileProxy file = files[i];
            refresh(file, REPOSITORY_STATUS_UNKNOWN);
        }
    }
    
    /**
     * Refreshes the status for the given file and all its children
     * 
     * @param root
     */
    public void refreshRecursively (VCSFileProxy root) {
        // refresh the root itself
        FileInformation info = refresh(root, REPOSITORY_STATUS_UNKNOWN);
        // for unignored files, refresh recursively its direct children too
        if (info == null || (info.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0) {
            List<VCSFileProxy> files = SvnUtils.listChildren(root);
            for (VCSFileProxy file : files) {
                refreshRecursively(file);
            }
        }
    }    
    
    private FileInformation refresh (VCSFileProxy file, RepositoryStatus repositoryStatus, boolean forceChangeEvent) {
        
        boolean refreshDone = false;
        FileInformation current = null;
        FileInformation fi = null;
        VCSFileProxy [] content = null; 
                
        synchronized (this) {
            VCSFileProxy dir = file.getParentFile();
            if (dir == null) {
                return FILE_INFORMATION_NOTMANAGED; //default for filesystem roots 
            }
            Map<VCSFileProxy, FileInformation> files = getScannedFiles(dir);
            if (files == NOT_MANAGED_MAP && repositoryStatus == REPOSITORY_STATUS_UNKNOWN) {
                return FILE_INFORMATION_NOTMANAGED;
            }
            current = files.get(file);
            for (Map.Entry<VCSFileProxy, FileInformation> e : files.entrySet()) {
                VCSFileProxy fKey = e.getKey();
                if (fKey.getPath().equals(file.getPath())) {
                    current = e.getValue();
                    break;
                }
            }

            ISVNStatus status = null;
            boolean symlink = false;
            try {
                VCSFileProxy topmost = Subversion.getInstance().getTopmostManagedAncestor(file);
                symlink = topmost != null && isSymlink(file);
                if (!(symlink || SvnUtils.isPartOfSubversionMetadata(file))) {
                    if (isParentIgnored(file)) {
                        // increase performace and do not query files under ignored parent
                        status = null;
                    } else if (Subversion.getInstance().isConnected(file)) {
                        SvnClient client = Subversion.getInstance().getClient(false, new Context(file));
                        status = SvnUtils.getSingleStatus(client, file);
                        if (status != null && SVNStatusKind.UNVERSIONED.equals(status.getTextStatus())) {
                            status = null;
                        }
                    }                    
                }
            } catch (SVNClientException e) {
                // svnClientAdapter does not return SVNStatusKind.UNVERSIONED!!!
                // unversioned resource is expected getSingleStatus()
                // does not return SVNStatusKind.UNVERSIONED but throws exception instead            
                // instead of throwing exception
                if (!SvnClientExceptionHandler.isUnversionedResource(e.getMessage())
                        && !WorkingCopyAttributesCache.getInstance().isSuppressed(e)) {
                    // missing or damaged entries
                    // or ignored file
                    SvnClientExceptionHandler.notifyException(new Context(file), e, false, false);
                }
            }

            if (symlink) {
                fi = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
            } else {
                fi = createFileInformation(file, status, repositoryStatus);
            }
            if (equivalent(fi, current)) {
                refreshDone = true;
            }
            // do not include uptodate files into cache, missing directories must be included
            if (!refreshDone && current == null && !fi.isDirectory() && fi.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE) {
                refreshDone = true;
            }

            if(!refreshDone) {               
                if (fi.getStatus() == FileInformation.STATUS_UNKNOWN && 
                      current != null && current.isDirectory() && ( current.getStatus() == FileInformation.STATUS_VERSIONED_DELETEDLOCALLY || 
                                                                    current.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY )) 
                {
                    // - if the file was deleted then all it's children have to be refreshed.
                    // - we have to list the children before the turbo.writeEntry() call 
                    //   as that unfortunatelly tends to purge them from the cache 
                    content = listFiles(new VCSFileProxy[] {file}, ~0);
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "refresh: will need recursive refresh for deleted folder {0}", file.getPath()); //NOI18N
                    }
                } 

                dir = dir.normalizeFile();
                file = VCSFileProxy.createFileProxy(dir, file.getName());
                Map<VCSFileProxy, FileInformation> newFiles = new HashMap<>(files);
                if (fi.getStatus() == FileInformation.STATUS_UNKNOWN) {
                    newFiles.remove(file);
                    turbo.writeEntry(file, FILE_STATUS_MAP, null);  // remove mapping in case of directories
                }
                else if (fi.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE && file.isFile()) {
                    newFiles.remove(file);
                } else {
                    newFiles.remove(file);
                    newFiles.put(file, fi);
                }
                assert newFiles.containsKey(dir) == false : "Dir " + dir + "contains " + files.toString(); //NOI18N
                turbo.writeEntry(dir, FILE_STATUS_MAP, newFiles.isEmpty() ? null : newFiles);
            }
        }

        if(!refreshDone) {
            if(content == null && file.isDirectory() && needRecursiveRefresh(fi, current)) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "refresh: need recursive refresh for {0}", file.getPath()); //NOI18N
                }
                content = listFiles(file);
            }

            if ( content != null ) {
                for (int i = 0; i < content.length; i++) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "refresh: recursive refresh for {0}, child of {1}", //NOI18N
                                new Object[] { content[i].getPath(), file.getPath() });
                    }
                    refresh(content[i], REPOSITORY_STATUS_UNKNOWN);
                }
            }
            fireFileStatusChanged(file, current, fi);    
        } else {
            // scan also children if there's no information about them yet (not yet explored folder)
            if (fi.isDirectory() && "true".equals(System.getProperty("org.netbeans.modules.subversion.FileStatusCache.recursiveScan", "false")) //NOI18N
                    && (fi.getStatus() & (FileInformation.STATUS_NOTVERSIONED_NOTMANAGED | FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) == 0 // do not scan notmanaged or ignored folders
                    && turbo.readEntry(file, FILE_STATUS_MAP) == null) {    // scan only those which have not yet been scanned, no information is available for them
                refreshAsync(file.listFiles());
            }
            if(forceChangeEvent) {
                fireFileStatusChanged(file, current, fi);
            }
        }                       
        return fi;
    }    

    private boolean isParentIgnored (VCSFileProxy file) {
        VCSFileProxy parent = file.getParentFile();
        if (parent != null) {
            FileInformation parentInfo = getCachedStatus(parent);
            return parentInfo != null && parentInfo.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
        } else {
            return false;
        }
    }

    public void patchRevision(VCSFileProxy[] fileArray, SVNRevision.Number revision) {        
        for (VCSFileProxy file : fileArray) {            
            synchronized(this) {        
                FileInformation status = getCachedStatus(file);
                ISVNStatus entry = status != null ? status.getEntry(file) : null;
                if(entry != null) {
                    SVNRevision.Number rev = entry.getRevision();
                    if(rev == null) {
                        continue;
                    }
                    if(rev.getNumber() != revision.getNumber()) {
                        FileInformation info = createFileInformation(file, new FakeRevisionStatus(entry, revision), REPOSITORY_STATUS_UNKNOWN);
                        VCSFileProxy dir = file.getParentFile();
                        Map<VCSFileProxy, FileInformation> files = getScannedFiles(dir);
                        Map<VCSFileProxy, FileInformation> newFiles = new HashMap<>(files);
                        newFiles.put(file, info);
                        turbo.writeEntry(dir, FILE_STATUS_MAP, newFiles.isEmpty() ? null : newFiles);
                    }
                }
            }
        }
    }
    
    /**
     * Two FileInformation objects are equivalent if their status contants are equal AND they both reperesent a file (or
     * both represent a directory) AND Entries they cache, if they can be compared, are equal. 
     *  
     * @param other object to compare to
     * @return true if status constants of both object are equal, false otherwise
     */ 
    private static boolean equivalent(FileInformation main, FileInformation other) {
        if (other == null || main.getStatus() != other.getStatus() || main.isDirectory() != other.isDirectory()) {
            return false;
        }
        
        ISVNStatus e1 = main.getEntry(null);
        ISVNStatus e2 = other.getEntry(null);
        return e1 == e2 || e1 == null || e2 == null || equal(e1, e2);
    }

    /**
     * Replacement for missing Entry.equals(). It is implemented as a separate method to maintain compatibility.
     * 
     * @param e1 first entry to compare
     * @param e2 second Entry to compare
     * @return true if supplied entries contain equivalent information
     */ 
    private static boolean equal(ISVNStatus e1, ISVNStatus e2) {
        if (!SVNStatusKind.IGNORED.equals(e1.getTextStatus()) && !SVNStatusKind.UNVERSIONED.equals(e1.getTextStatus())
                 && !SVNStatusKind.ADDED.equals(e1.getTextStatus())) {
            // check revisions just when it's meaningful, unversioned or ignored files have no revision and thus should be considered equal
            // added/copied files make no sense either, they have no revision yet
            long r1 = -1;
            if (e1 != null) {
                SVNRevision r = e1.getRevision();
                r1 = r != null ? e1.getRevision().getNumber() : r1;
            }

            long r2 = -2;
            if (e2 != null) {
                SVNRevision r = e2.getRevision();
                r2 = r != null ? e2.getRevision().getNumber() : r2;
            }

            if (r1 != r2) {
                return false;
            }
        }
        if (e1.isCopied() != e2.isCopied()) {
            return false;
        }
        return e1.getUrl() == e2.getUrl() || 
                e1.getUrl() != null && e1.getUrl().equals(e2.getUrl());
    }
    
    private boolean needRecursiveRefresh(FileInformation fi, FileInformation current) {
        //     looks like the same thing is done at diferent places in a different way but the same result.
        if (fi.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED || 
                current != null && current.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
            return true;
        }
        if (fi.getStatus() == FileInformation.STATUS_NOTVERSIONED_NOTMANAGED ||
                current != null && current.getStatus() == FileInformation.STATUS_NOTVERSIONED_NOTMANAGED) {
            return true;
        }
        if (fi.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY ||
                current != null && current.getStatus() == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) {
            return true;
        }        
        return false;
    }

    PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }
    
    public boolean ready() {
        return ready;
    }
    
    // --- Package private contract ------------------------------------------

    /**
     * compute the cache index
     */
    void computeIndex() {
        try {
            cacheProvider.computeIndex();
            Subversion.getInstance().refreshAllAnnotations();
        } finally {
            ready = true;
            propertySupport.firePropertyChange(PROP_CACHE_READY, false, true);
        }
    }

    /**
     * Cleans up the cache by removing or correcting entries that are no longer valid or correct.
     * WARNING: index has to be computed first
     */
    void cleanUp() {
        VCSFileProxy[] modifiedFiles = cacheProvider.getAllIndexValues();
        if (modifiedFiles.length > CACHE_SIZE_WARNING_THRESHOLD) {
            LOG.log(Level.WARNING, "Cache contains too many entries: {0}", (Integer) modifiedFiles.length); //NOI18N
        }
        for (VCSFileProxy file : modifiedFiles) {
            if (!VCSFileProxySupport.isConnectedFileSystem(VCSFileProxySupport.getFileSystem(file))) {
                continue;
            }
            FileInformation info = getCachedStatus(file);
            if (info != null && (info.getStatus() & FileInformation.STATUS_LOCAL_CHANGE) != 0) {
                refresh(file, REPOSITORY_STATUS_UNKNOWN);
            } else if (info == null ||info.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                // remove entries that were excluded but no longer exist
                // cannot simply call refresh on excluded files because of 'excluded on server' status
                if (!exists(file)) {
                    refresh(file, REPOSITORY_STATUS_UNKNOWN);
                }
            }
        }
    }

    /**
     * Refreshes given directory and all subdirectories.
     *
     * @param dir directory to refresh
     */
    void directoryContentChanged(VCSFileProxy dir) {
        Map<VCSFileProxy, FileInformation> originalFiles = (Map<VCSFileProxy, FileInformation>) turbo.readEntry(dir, FILE_STATUS_MAP);
        if (originalFiles != null) {
            for (Iterator<VCSFileProxy> i = originalFiles.keySet().iterator(); i.hasNext();) {
                VCSFileProxy file = i.next();
                refresh(file, REPOSITORY_STATUS_UNKNOWN);
            }
        }
    }
    
    // --- Private methods ---------------------------------------------------
    private Map<VCSFileProxy, FileInformation> getScannedFiles(VCSFileProxy dir) {
        Map<VCSFileProxy, FileInformation> files;

        // there are 2nd level nested admin dirs (.svn/tmp, .svn/prop-base, ...)

        if (SvnUtils.isAdministrative(dir)) {
            return NOT_MANAGED_MAP;
        }
        VCSFileProxy parent = dir.getParentFile();
        if (parent != null && SvnUtils.isAdministrative(parent)) {
            return NOT_MANAGED_MAP;
        }

        files = (Map<VCSFileProxy, FileInformation>) turbo.readEntry(dir, FILE_STATUS_MAP);
        if (files != null) {
            if (files.containsKey(dir)) {
                LOG.log(Level.WARNING, "Corrupted cached entry for folder {0}, it contains {1}", new Object[] {dir, files}); //NOI18N
                files.remove(dir); // remove the corrupted entry
                turbo.writeEntry(dir, FILE_STATUS_MAP, files); // and save the corrected map
            }
            return files;
        }
        if (isNotManagedByDefault(dir)) {
            return NOT_MANAGED_MAP; 
        }

        // scan and populate cache with results
        dir = dir.normalizeFile();
        files = scanFolder(dir);    // must not execute while holding the lock, it may take long to execute
        assert files.containsKey(dir) == false : "Dir " + dir + "contains " + files.toString(); //NOI18N
        turbo.writeEntry(dir, FILE_STATUS_MAP, files);
        for (Iterator<VCSFileProxy> i = files.keySet().iterator(); i.hasNext();) {
            VCSFileProxy file = i.next();
            FileInformation info = files.get(file);
            if ((info.getStatus() & (FileInformation.STATUS_LOCAL_CHANGE | FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) != 0) {
                fireFileStatusChanged(file, null, info);
            }
        }
        return files;
    }

    private boolean isNotManagedByDefault(VCSFileProxy dir) {
        return !(dir.exists() || SvnUtils.isManaged(dir)); // cannot just test dir for existence, deleted folders now no longer exist on disk
    }

    /**
     * Scans all files in the given folder, computes and stores their CVS status. 
     * 
     * @param dir directory to scan
     * @return Map map to be included in the status cache (File => FileInformation)
     */ 
    private Map<VCSFileProxy, FileInformation> scanFolder(VCSFileProxy dir) {
        VCSFileProxy [] files = dir.listFiles();
        if (files == null) {
            files = new VCSFileProxy[0];
        }
        Map<VCSFileProxy, FileInformation> folderFiles = new HashMap<>(files.length);

        ISVNStatus [] entries = null;
        final Context context = new Context(dir);
        try {
            if (SvnUtils.isManaged(dir) && !isParentIgnored(dir)) {                
                SvnClient client = Subversion.getInstance().getClient(true, context);
                entries = client.getStatus(dir, false, true); 
            }
        } catch (SVNClientException e) {
            // no or damaged entries
            //LOG.getDefault().annotate(e, "Can not status " + dir.getPath() + ", guessing it...");  // NOI18N
            if (!SvnClientExceptionHandler.isUnversionedResource(e.getMessage())
                    && !WorkingCopyAttributesCache.getInstance().isSuppressed(e)) {
                // missing or damaged entries
                // or ignored file
                SvnClientExceptionHandler.notifyException(context, e, false, false);
            }
        }

        if (entries == null) {
            for (int i = 0; i < files.length; i++) {
                VCSFileProxy file = files[i];
                if (SvnUtils.isAdministrative(file)) {
                    continue;
                }
                FileInformation fi = createFileInformation(file, null, REPOSITORY_STATUS_UNKNOWN);
                if (fi.isDirectory() || fi.getStatus() != FileInformation.STATUS_VERSIONED_UPTODATE) {
                    folderFiles.put(file, fi);
                }
            }
        } else {
            Set<VCSFileProxy> localFiles = new HashSet<>(Arrays.asList(files));
            for (int i = 0; i < entries.length; i++) {
                ISVNStatus entry = entries[i];
                VCSFileProxy file = entry.getFile();
                if (file.equals(dir)) {
                    continue;
                }
                localFiles.remove(file);
                if (SvnUtils.isAdministrative(file)) {
                    continue;
                }
                FileInformation fi = createFileInformation(file, entry, REPOSITORY_STATUS_UNKNOWN);
                if (fi.isDirectory() || fi.getStatus() != FileInformation.STATUS_VERSIONED_UPTODATE) {
                    folderFiles.put(file, fi);
                }
            }

            Iterator<VCSFileProxy> it = localFiles.iterator();
            while (it.hasNext()) {
                VCSFileProxy localFile = it.next();
                FileInformation fi = createFileInformation(localFile, null, REPOSITORY_STATUS_UNKNOWN);
                VCSFileProxy topmost = Subversion.getInstance().getTopmostManagedAncestor(localFile);
                if (fi.isDirectory() || topmost == null || fi.getStatus() != FileInformation.STATUS_VERSIONED_UPTODATE
                        && !isSymlink(localFile)) {
                    folderFiles.put(localFile, fi);
                }
            }
        }

        return folderFiles;
    }

    /**
     * Examines a file or folder and computes its status. 
     * 
     * @param status entry for this file or null if the file is unknown to subversion
     * @return FileInformation file/folder status bean
     */ 
    private FileInformation createFileInformation(VCSFileProxy file, ISVNStatus status, RepositoryStatus repositoryStatus) {
        if (status == null || status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {
            if (!SvnUtils.isManaged(file)) {
                return file.isDirectory() ? FILE_INFORMATION_NOTMANAGED_DIRECTORY : FILE_INFORMATION_NOTMANAGED;
            }
            return createMissingEntryFileInformation(file, repositoryStatus);
        } else {
            return createVersionedFileInformation(file, status, repositoryStatus);
        }
    }

    /**
     * Examines a file or folder that has an associated CVS entry. 
     * 
     * @param file file/folder to examine
     * @param status status of the file/folder as reported by the CVS server 
     * @return FileInformation file/folder status bean
     */ 
    private FileInformation createVersionedFileInformation(VCSFileProxy file, ISVNStatus status, RepositoryStatus repositoryStatus) {

        SVNStatusKind kind = status.getTextStatus();
        SVNStatusKind pkind = status.getPropStatus();

        int remoteStatus = 0;
        if (repositoryStatus != REPOSITORY_STATUS_UNKNOWN) {
            if (repositoryStatus.getStatus().getRepositoryTextStatus() == SVNStatusKind.MODIFIED
            || repositoryStatus.getStatus().getRepositoryPropStatus() == SVNStatusKind.MODIFIED) {
                remoteStatus = FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY;
            } else if (repositoryStatus.getStatus().getRepositoryTextStatus() == SVNStatusKind.DELETED
            /*|| repositoryStatus.getRepositoryPropStatus() == SVNStatusKind.DELETED*/) {
                remoteStatus = FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY;
            } else if (repositoryStatus.getStatus().getRepositoryTextStatus() == SVNStatusKind.ADDED
                        || repositoryStatus.getStatus().getRepositoryTextStatus() == SVNStatusKind.REPLACED) {
                // solved in createMissingfileInformation
            } else if ( (repositoryStatus.getStatus().getRepositoryTextStatus() == null &&
                         repositoryStatus.getStatus().getRepositoryPropStatus() == null)
                        ||
                        (repositoryStatus.getStatus().getRepositoryTextStatus() == SVNStatusKind.NONE &&
                         repositoryStatus.getStatus().getRepositoryPropStatus() == SVNStatusKind.NONE))
            {
                // no remote change at all
            } else {
                // so far above were observed....
                Subversion.LOG.log(Level.WARNING,"SVN.FSC: unhandled repository status: {0}" + "\n" +   // NOI18N
                                       "\ttext: " + "{1}" + "\n" +             // NOI18N
                                       "\tprop: " + "{2}", new Object[] { file.getPath(), repositoryStatus.getStatus().getRepositoryTextStatus(), //NOI18N
                                           repositoryStatus.getStatus().getRepositoryPropStatus() });
            }
            if (repositoryStatus.getLock() != null) {
                remoteStatus |= FileInformation.STATUS_LOCKED_REMOTELY;
            }
        }
        
        if (status. getLockOwner() != null) {
            remoteStatus = FileInformation.STATUS_LOCKED | remoteStatus;
        }
        
        int propertyStatus = 0;
        if (SVNStatusKind.NONE.equals(pkind)) {
            // no influence
        } else if (SVNStatusKind.NORMAL.equals(pkind)) {
            // no influence
        } else if (SVNStatusKind.MODIFIED.equals(pkind)) {
            propertyStatus = FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY_PROPERTY;
        } else if (SVNStatusKind.CONFLICTED.equals(pkind)) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT_CONTENT | remoteStatus, status);
        } else {
            throw new IllegalArgumentException("Unknown prop status: " + status.getPropStatus()); // NOI18N
        }

        int additionalStatus = remoteStatus | propertyStatus;
        if (status.hasTreeConflict()) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT_TREE | additionalStatus, status);
        } else if (SVNStatusKind.NONE.equals(kind)) {
            return FILE_INFORMATION_UNKNOWN;
        } else if (SVNStatusKind.NORMAL.equals(kind)) {
            int finalStatus = FileInformation.STATUS_VERSIONED_UPTODATE | remoteStatus;
            if (propertyStatus != 0) {
                finalStatus = additionalStatus;
            }
            return new FileInformation(finalStatus, status);
        } else if (SVNStatusKind.MODIFIED.equals(kind)) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY_CONTENT | additionalStatus, status);
        } else if (SVNStatusKind.ADDED.equals(kind)) {
            return new FileInformation(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY | additionalStatus, status);
        } else if (SVNStatusKind.DELETED.equals(kind)) {                    
            return new FileInformation(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY | additionalStatus, status);
        } else if (SVNStatusKind.UNVERSIONED.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | additionalStatus, status);
        } else if (SVNStatusKind.MISSING.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_VERSIONED_DELETEDLOCALLY | additionalStatus, status);
        } else if (SVNStatusKind.REPLACED.equals(kind)) {                      
            // this status or better to use this simplyfication?
            return new FileInformation(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY | additionalStatus, status);
        } else if (SVNStatusKind.MERGED.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_VERSIONED_MERGE | additionalStatus, status);
        } else if (SVNStatusKind.CONFLICTED.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT_CONTENT | additionalStatus, status);
        } else if (SVNStatusKind.OBSTRUCTED.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT_CONTENT | additionalStatus, status);
        } else if (SVNStatusKind.IGNORED.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED | remoteStatus, status);
        } else if (SVNStatusKind.INCOMPLETE.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT_CONTENT | additionalStatus, status);
        } else if (SVNStatusKind.EXTERNAL.equals(kind)) {            
            return new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE | remoteStatus, status);
        } else {        
            throw new IllegalArgumentException("Unknown text status: " + status.getTextStatus()); // NOI18N
        }
    }

    static String statusText(ISVNStatus status) {
        return "file: " + status.getTextStatus().toString() + " copied: " + status.isCopied() + " prop: " + status.getPropStatus().toString(); // NOI18N
    }

    /**
     * Examines a file or folder that does NOT have an associated Subversion status. 
     * 
     * @param file file/folder to examine
     * @return FileInformation file/folder status bean
     */ 
    private FileInformation createMissingEntryFileInformation(final VCSFileProxy file, RepositoryStatus repositoryStatus) {
        
        // ignored status applies to whole subtrees
        boolean exists = file.exists();
        VCSFileProxy parent = file.getParentFile();
        if (parent == null) {
            LOG.log(Level.WARNING, "createMissingEntryFileInformation for root folder: {0}, isManaged={1}", //NOI18N
                    new Object[] { file, SvnUtils.isManaged(file) });
        }
        if(exists && VCSFileProxySupport.isMac(file) && parent != null) {
            // handle case on mac, "fileA".exists() is the same as "filea".exists but svn client understands the difference
            exists = false;
            final VCSFileProxy[] listFiles = parent.listFiles();
            if (listFiles != null) {
                for(VCSFileProxy child : listFiles) {
                    if (child.getName().equals(file.getName())) {
                        exists = true;
                        break;
                    }
                }
            }
        } 
        boolean isDirectory = exists && file.isDirectory();
        int parentStatus = parent == null || isNotManagedByDefault(parent)
                ? FileInformation.STATUS_NOTVERSIONED_NOTMANAGED
                : getStatus(parent).getStatus();
        if (parentStatus == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
            return isDirectory ? 
                FILE_INFORMATION_EXCLUDED_DIRECTORY : FILE_INFORMATION_EXCLUDED;
        }
        /**FILE_INFORMATION_NOTMANAGED should be set only for existing files
         * Deleted files, which were originally ignored (i.e. build/classes), 
         * used to acquire status F_I_NOTMANAGED, which was stuck to them indefinitely
         */
        if (exists && parentStatus == FileInformation.STATUS_NOTVERSIONED_NOTMANAGED) {
            if (isDirectory) {
                // Working directory roots (aka managed roots). We already know that isManaged(file) is true
                return SvnUtils.isPartOfSubversionMetadata(file) ? 
                    FILE_INFORMATION_NOTMANAGED_DIRECTORY : FILE_INFORMATION_UPTODATE_DIRECTORY;
            } else {
                return FILE_INFORMATION_NOTMANAGED;
            }
        }

        // mark auxiliary after-update conflict files as ignored
        // C source.java
        // I source.java.mine
        // I source.java.r45
        // I source.java.r57
        //
        // after-merge conflicts (even svn st does not recognize as ignored)
        // C source.java
        // ? source.java.working
        // ? source.jave.merge-right.r20
        // ? source.java.merge-left.r0
        //
        String name = file.getName();
        Matcher m = auxConflictPattern.matcher(name);
        if (exists && m.matches()) {
            if (parent != null) {
                String masterName = m.group(1);
                VCSFileProxy master = VCSFileProxy.createFileProxy(parent, masterName);
                if (master.isFile()) {
                    return FILE_INFORMATION_EXCLUDED;
                }
            }
        }
        
        if (exists) {
            if (Subversion.getInstance().isIgnored(file) 
                    || repositoryStatus != null && repositoryStatus.getStatus().getTextStatus() == SVNStatusKind.EXTERNAL) {
                return new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, file.isDirectory());
            } else {
                return new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, file.isDirectory());
            }
        } else {
            if (repositoryStatus != REPOSITORY_STATUS_UNKNOWN) {
                if (repositoryStatus.getStatus().getRepositoryTextStatus() == SVNStatusKind.ADDED
                        || repositoryStatus.getStatus().getRepositoryTextStatus() == SVNStatusKind.REPLACED) {
                    boolean folder = repositoryStatus.getStatus().getNodeKind() == SVNNodeKind.DIR;
                    return new FileInformation(FileInformation.STATUS_VERSIONED_NEWINREPOSITORY, folder);
                }
            }
            return FILE_INFORMATION_UNKNOWN;
        }
    }

    
    private boolean exists(VCSFileProxy file) {
        if (!file.exists()) {
            return false;
        }
        return file.getPath().equals(file.normalizeFile().getPath());
    }

    ListenersSupport listenerSupport = new ListenersSupport(this);
    public void addVersioningListener(VersioningListener listener) {
        listenerSupport.addListener(listener);
    }

    public void removeVersioningListener(VersioningListener listener) {
        listenerSupport.removeListener(listener);
    }
    
    private void fireFileStatusChanged(VCSFileProxy file, FileInformation oldInfo, FileInformation newInfo) {
        getLabelsCache().remove(file); // remove info from label cache, it could change
        listenerSupport.fireVersioningEvent(EVENT_FILE_STATUS_CHANGED, new Object [] { file, oldInfo, newInfo });
    }

    private boolean isSymlink (VCSFileProxy file) {
        boolean symlink = false;
        if (EXCLUDE_SYMLINKS) {
            symlink = VCSFileProxySupport.isSymlink(file);
        }
        return symlink;
    }

    private static final class NotManagedMap extends AbstractMap<VCSFileProxy, FileInformation> {
        @Override
        public Set<Map.Entry<VCSFileProxy, FileInformation>> entrySet() {
            return Collections.emptySet();
        }
    }

    private static class FakeRevisionStatus implements ISVNStatus {
        private final ISVNStatus value;
        private final SVNRevision.Number revision;
        public FakeRevisionStatus(ISVNStatus value, SVNRevision.Number revision) {
            this.value = value;
            this.revision = revision;           
        }
        @Override
        public boolean isWcLocked() {
            return value.isWcLocked();
        }
        @Override
        public boolean isSwitched() {
            return value.isSwitched();
        }
        @Override
        public boolean isCopied() {
            return value.isCopied();
        }
        @Override
        public String getUrlString() {
            return value.getUrlString();
        }
        @Override
        public SVNUrl getUrl() {
            return value.getUrl();
        }
        @Override
        public SVNStatusKind getTextStatus() {
            return value.getTextStatus();
        }
        @Override
        public SVNRevision.Number getRevision() {                        
            return revision;
        }
        @Override
        public SVNStatusKind getRepositoryTextStatus() {
            return value.getRepositoryTextStatus();
        }
        @Override
        public SVNStatusKind getRepositoryPropStatus() {
            return value.getRepositoryPropStatus();
        }
        @Override
        public SVNStatusKind getPropStatus() {
            return value.getPropStatus();
        }
        @Override
        public String getPath() {
            return value.getPath();
        }
        @Override
        public SVNNodeKind getNodeKind() {
            return value.getNodeKind();
        }
        @Override
        public String getLockOwner() {
            return value.getLockOwner();
        }
        @Override
        public Date getLockCreationDate() {
            return value.getLockCreationDate();
        }
        @Override
        public String getLockComment() {
            return value.getLockComment();
        }
        @Override
        public String getLastCommitAuthor() {
            return value.getLastCommitAuthor();
        }
        @Override
        public SVNRevision.Number getLastChangedRevision() {
            return value.getLastChangedRevision();
        }
        @Override
        public Date getLastChangedDate() {
            return value.getLastChangedDate();
        }
        @Override
        public VCSFileProxy getFile() {
            return value.getFile();
        }
        @Override
        public VCSFileProxy getConflictWorking() {
            return value.getConflictWorking();
        }
        @Override
        public VCSFileProxy getConflictOld() {
            return value.getConflictOld();
        }
        @Override
        public VCSFileProxy getConflictNew() {
            return value.getConflictNew();
        }
        @Override
        public boolean hasTreeConflict() {
            return value.hasTreeConflict();
        }
        @Override
        public SVNConflictDescriptor getConflictDescriptor() {
            return value.getConflictDescriptor();
        }
        @Override
        public boolean isFileExternal() {
            return value.isFileExternal();
        }

        @Override
        public String getMovedFromAbspath () {
            return value.getMovedFromAbspath();
        }

        @Override
        public String getMovedToAbspath () {
            return value.getMovedToAbspath();
        }
    }

    public FileLabelCache getLabelsCache () {
        return labelsCache;
    }

    /**
     * Cache of information needed for name annotations, caching such information prevents from running status commands in AWT
     */
    public class FileLabelCache {
        private final LinkedHashMap<VCSFileProxy, FileLabelInfo> fileLabels;
        private final Set<VCSFileProxy> filesForLabelRefresh = new HashSet<>();
        private final RequestProcessor.Task labelInfoRefreshTask;
        private final FileStatusCache master;

        private FileLabelCache(FileStatusCache master) {
            this.master = master;
            labelInfoRefreshTask = master.rp.create(new LabelInfoRefreshTask());
            fileLabels = new LinkedHashMap<>(100);
        }

        public void flushFileLabels(VCSFileProxy... files) {
            synchronized (fileLabels) {
                if (files == null) {
                    fileLabels.clear();
                    return;
                }
                for (VCSFileProxy f : files) {
                    if (LABELS_CACHE_LOG.isLoggable(Level.FINE)) {
                        LABELS_CACHE_LOG.log(Level.FINE, "Removing from cache: {0}", f.getPath()); //NOI18N
                    }
                    fileLabels.remove(f);
                }
            }
        }

        /**
         * Returns a not null cache item.
         * @param file
         * @param mimeTypeFlag mime label is needed?
         * @return a cache item or a fake one if the original is null or invalid
         */
        @org.netbeans.api.annotations.common.SuppressWarnings("RCN") // assert in release mode does not guarantee that "labelInfo != null"
        public FileLabelInfo getLabelInfo(VCSFileProxy file, boolean mimeTypeFlag) {
            FileLabelInfo labelInfo;
            boolean refreshInfo = false;
            synchronized (fileLabels) {
                labelInfo = fileLabels.get(file);
                if (labelInfo == null || !labelInfo.isValid(mimeTypeFlag, true)) {
                    if (LABELS_CACHE_LOG.isLoggable(Level.FINE)) {
                        if (labelInfo == null && LABELS_CACHE_LOG.isLoggable(Level.FINER)) {
                            LABELS_CACHE_LOG.log(Level.FINER, "No item in cache for : {0}", file.getPath()); //NOI18N
                        } else if (labelInfo != null) {
                            LABELS_CACHE_LOG.log(Level.FINE, "Too old item in cache for : {0}", file.getPath()); //NOI18N
                        }
                    }
                    if (labelInfo == null) {
                        labelInfo = FAKE_LABEL_INFO;
                    }
                    refreshInfo = true;
                }
            }
            if (refreshInfo) {
                refreshLabelFor(file);
            }
            if (VERSIONING_ASYNC_ANNOTATOR) {
                labelInfo = fileLabels.get(file);
                assert labelInfo != null : "null label info for " + file.getPath();
                if (labelInfo == null) {
                    labelInfo = FAKE_LABEL_INFO;
                }
            }
            return labelInfo;
        }

        /**
         * schedules file's label info refresh or run the refresh immediately depending on the async status of the versioning annotator
         * @param file
         */
        private void refreshLabelFor(VCSFileProxy file) {
            synchronized (filesForLabelRefresh) {
                filesForLabelRefresh.add(file);
            }
            if (!EventQueue.isDispatchThread() || VERSIONING_ASYNC_ANNOTATOR) { // refresh does not block the AWT
                labelInfoRefreshTask.run();
            } else {
                labelInfoRefreshTask.schedule(200);
            }
        }

        private void remove(VCSFileProxy file) {
            synchronized (fileLabels) {
                fileLabels.remove(file);
            }
        }

        private class LabelInfoRefreshTask extends Task {

            @Override
            public void run() {
                Set<VCSFileProxy> filesToRefresh;
                synchronized (filesForLabelRefresh) {
                    // pick up files for refresh
                    filesToRefresh = new HashSet<>(filesForLabelRefresh);
                    filesForLabelRefresh.clear();
                }
                if (!filesToRefresh.isEmpty()) {
                    // labels are accummulated in a temporary map so their timestamp can be later set to a more accurate value
                    // initialization for many files can be time-consuming and labels initialized in first cycles can grow old even before
                    // their annotations are refreshed through refreshAnnotations()
                    HashMap<VCSFileProxy, FileLabelInfo> labels = new HashMap<>(filesToRefresh.size());
                    for (VCSFileProxy file : filesToRefresh) {
                        try {
                            FileInformation fi = master.getCachedStatus(file);
                            if (fi != null && (fi.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0
                                    || master.isParentIgnored(file)) {
                                // increase performace and do not query ignored files
                                labels.put(file, FAKE_LABEL_INFO);
                                continue;
                            }
                            final Context context = new Context(file);
                            SvnClient client = Subversion.getInstance().getClient(false, context);
                            // get status for all files
                            ISVNInfo info = SvnUtils.getInfoFromWorkingCopy(client, file);
                            SVNRevision rev = info.getRevision();
                            String revisionString, stickyString, binaryString = null;
                            String lastRevisionString, lastDateString = null;
                            revisionString = rev != null && !"-1".equals(rev.toString()) ? rev.toString() : ""; //NOI18N
                            rev = info.getLastChangedRevision();
                            lastRevisionString = rev != null && !"-1".equals(rev.toString()) ? rev.toString() : ""; //NOI18N
                            if (info.getLastChangedDate() != null) {
                                lastDateString = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(info.getLastChangedDate());
                            }
                            Annotator.AnnotationFormat af = annotator.getAnnotationFormat(context.getFileSystem());
                            if (af.mimeTypeFlag) {
                                // call svn prop command only when really needed
                                if (fi == null || (fi.getStatus() & (FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) == 0) {
                                    binaryString = getMimeType(client, file);
                                } else {
                                    binaryString = "";                  //NOI18N
                                }
                            }
                            if (fi == null || (fi.getStatus() & (FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) == 0) {
                                // copy name
                                if (info.getUrl() != null) {
                                    stickyString = SvnUtils.getCopy(context, info.getUrl());
                                } else {
                                    // slower
                                    stickyString = SvnUtils.getCopy(file);
                                }
                            } else {
                                stickyString = ""; //NOI18N
                            }
                            labels.put(file, new FileLabelInfo(revisionString, binaryString, stickyString, info.getLastCommitAuthor(), lastDateString, lastRevisionString));
                        } catch (SVNClientException ex) {
                            if (WorkingCopyAttributesCache.getInstance().isSuppressed(ex)) {
                                try {
                                    WorkingCopyAttributesCache.getInstance().logSuppressed(ex, file);
                                } catch (SVNClientException ex1) {
                                    // do not log again
                                }
                            } else {
                                if (!SvnClientExceptionHandler.isUnversionedResource(ex.getMessage())) {
                                    LABELS_CACHE_LOG.log(Level.WARNING, "LabelInfoRefreshTask: failed getting info and info for {0}", file.getPath());
                                    LABELS_CACHE_LOG.log(Level.INFO, null, ex);
                                }
                            }
                            labels.put(file, FAKE_LABEL_INFO);
                        }
                    }
                    synchronized (fileLabels) {
                        for (Map.Entry<VCSFileProxy, FileLabelInfo> e : labels.entrySet()) {
                            e.getValue().updateTimestamp(); // after a possible slow initialization for many files update all timestamps, so they remain in cache longer
                            FileLabelInfo oldInfo = fileLabels.remove(e.getKey()); // fileLabels is a LinkedHashSet, so in order to move the item to the back in the chain, it must be removed before inserting
                            fileLabels.put(e.getKey(), e.getValue());
                            if (e.getValue().equals(oldInfo)) {
                                filesToRefresh.remove(e.getKey());
                            }
                        }
                    }
                    if (!VERSIONING_ASYNC_ANNOTATOR) {
                        Subversion.getInstance().refreshAnnotations(filesToRefresh.toArray(new VCSFileProxy[filesToRefresh.size()]));
                    }
                    synchronized (fileLabels) {
                        if (fileLabels.size() > 50) {
                            if (LABELS_CACHE_LOG.isLoggable(Level.FINE)) {
                                LABELS_CACHE_LOG.log(Level.FINE, "Cache contains : {0} entries before a cleanup", fileLabels.size()); //NOI18N
                            }
                            for (Iterator<VCSFileProxy> it = fileLabels.keySet().iterator(); it.hasNext();) {
                                VCSFileProxy f = it.next();
                                Annotator.AnnotationFormat af = annotator.getAnnotationFormat(VCSFileProxySupport.getFileSystem(f));
                                if (!fileLabels.get(f).isValid(af.mimeTypeFlag, false)) {
                                    it.remove();
                                } else {
                                    break;
                                }
                            }
                            if (LABELS_CACHE_LOG.isLoggable(Level.FINE)) {
                                LABELS_CACHE_LOG.log(Level.FINE, "Cache contains : {0} entries after a cleanup", fileLabels.size()); //NOI18N
                            }
                        }
                    }
                }
            }

            private String getMimeType(SvnClient client, VCSFileProxy file) {
                try {
                    ISVNProperty prop = client.propertyGet(file, ISVNProperty.MIME_TYPE);
                    if (prop != null) {
                        String mime = prop.getValue();
                        return mime != null ? mime : "";                //NOI18N
                    }
                } catch (SVNClientException ex) {
                    if (LABELS_CACHE_LOG.isLoggable(Level.FINE)) {
                        LABELS_CACHE_LOG.log(Level.FINE, null, ex);
                    }
                    return "";                                          //NOI18N
                }
                return "";                                              //NOI18N
            }

        }

    }
    
    /**
     * File label cache item
     */
    public static class FileLabelInfo {

        private static final long VALID_LABEL_PERIOD = 20000; // 20 seconds
        private final String revisionString;
        private final String binaryString;
        private final String stickyString;
        private final String lastRevisionString;
        private final String lastAuthorString;
        private final String lastDateString;
        private boolean pickedUp;
        private long timestamp;

        private FileLabelInfo (String revisionString, String binaryString, String stickyString, String lastAuthorString, String lastDateString, String lastRevisionString) {
            this.revisionString = revisionString;
            this.binaryString = binaryString;
            this.stickyString = stickyString;
            this.lastAuthorString = lastAuthorString;
            this.lastDateString = lastDateString;
            this.lastRevisionString = lastRevisionString;
            updateTimestamp();
        }

        private void updateTimestamp() {
            this.timestamp = System.currentTimeMillis();
        }

        /**
         *
         * @param mimeFlag if set to true, binaryString will be checked for being set
         * @param checkFirstAccess first access to this info is always valid,
         * so the oldness will be checked only when this is false or the info has already been accessed for the first time
         * @return
         */
        private boolean isValid(boolean mimeFlag, boolean checkFirstAccess) {
            long diff = System.currentTimeMillis() - timestamp;
            boolean valid = (checkFirstAccess && !pickedUp && (pickedUp = true)) || (diff <= VALID_LABEL_PERIOD);
            return valid && (!mimeFlag || binaryString != null);

        }

        /**
         * Returns a not null String with revision number, empty for unknown revisions
         * @return
         */
        String getRevisionString() {
            return revisionString != null ? revisionString : "";        //NOI18N
        }

        /*
         * Returns a not null String, empty for not binary files
         */
        public String getBinaryString() {
            return binaryString != null ? binaryString : "";            //NOI18N
        }

        /**
         * returns a not null String denoting a copy name
         * @return
         */
        public String getStickyString() {
            return stickyString != null ? stickyString : "";            //NOI18N
        }

        public String getLastRevisionString () {
            return lastRevisionString == null ? "" : lastRevisionString; //NOI18N
        }

        public String getLastDateString () {
            return lastDateString == null ? "" : lastDateString; //NOI18N
        }

        public String getLastAuthorString () {
            return lastAuthorString == null ? "" : lastAuthorString; //NOI18N
        }

        @Override
        public boolean equals (Object obj) {
            if (obj instanceof FileLabelInfo) {
                FileLabelInfo other = (FileLabelInfo) obj;
                return getRevisionString().equals(other.getRevisionString())
                        && getBinaryString().equals(other.getBinaryString())
                        && getStickyString().equals(other.getStickyString())
                        && getLastAuthorString().equals(other.getLastAuthorString())
                        && getLastDateString().equals(other.getLastDateString())
                        && getLastRevisionString().equals(other.getLastRevisionString());
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode () {
            int hash = 7;
            hash = 71 * hash + (this.revisionString != null ? this.revisionString.hashCode() : 0);
            hash = 71 * hash + (this.binaryString != null ? this.binaryString.hashCode() : 0);
            hash = 71 * hash + (this.stickyString != null ? this.stickyString.hashCode() : 0);
            hash = 71 * hash + (this.lastRevisionString != null ? this.lastRevisionString.hashCode() : 0);
            hash = 71 * hash + (this.lastAuthorString != null ? this.lastAuthorString.hashCode() : 0);
            hash = 71 * hash + (this.lastDateString != null ? this.lastDateString.hashCode() : 0);
            return hash;
        }
    }

    public static class RepositoryStatus {
        private final ISVNStatus status;
        private final ISVNLock lock;

        public RepositoryStatus (ISVNStatus status, ISVNLock lock) {
            this.status = status;
            this.lock = lock;
        }

        public ISVNStatus getStatus () {
            return status;
        }

        public ISVNLock getLock () {
            return lock;
        }
    }
}
