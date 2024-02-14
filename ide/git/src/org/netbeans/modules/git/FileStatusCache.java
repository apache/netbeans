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

package org.netbeans.modules.git;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.turbo.CacheIndex;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 *
 * @author ondra
 */
public class FileStatusCache {

    public static final String PROP_FILE_STATUS_CHANGED = "status.changed"; // NOI18N

    private final CacheIndex conflictedFiles, modifiedFiles, ignoredFiles;
    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.git.status.cache"); //NOI18N
    private int MAX_COUNT_UPTODATE_FILES = 1024;
    private static final int CACHE_SIZE_WARNING_THRESHOLD = 50000; // log when cache gets too big and steps over this threshold
    private boolean hugeCacheWarningLogged;
    int upToDateAccess = 0;
    private static final int UTD_NOTIFY_NUMBER = 100;
    /**
     * Keeps cached statuses for managed files
     */
    private final Map<File, FileInformation> cachedFiles;
    private final LinkedHashSet<File> upToDateFiles = new LinkedHashSet<>(MAX_COUNT_UPTODATE_FILES);
    private final RequestProcessor rp = new RequestProcessor("Git.cache", 1, true, false);
    private final HashSet<File> nestedRepositories = new HashSet<>(2); // mainly for logging
    private final PropertyChangeSupport listenerSupport = new PropertyChangeSupport(this);

    private static final FileInformation FILE_INFORMATION_UPTODATE = new FileInformation(EnumSet.of(Status.UPTODATE), false);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED = new FileInformation(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), false);
    private static final FileInformation FILE_INFORMATION_EXCLUDED = new FileInformation(EnumSet.of(Status.NOTVERSIONED_EXCLUDED), false);
    private static final FileInformation FILE_INFORMATION_NEWLOCALLY = new FileInformation(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.NEW_HEAD_WORKING_TREE), false);
    private static final FileInformation FILE_INFORMATION_UNKNOWN = new FileInformation(EnumSet.of(Status.UNKNOWN), false);

    private static final Map<File, File> SYNC_REPOSITORIES = new WeakHashMap<>(5);
    private final IgnoredFilesHandler ignoredFilesHandler;
    private final RequestProcessor.Task ignoredFilesHandlerTask;
    private static final boolean USE_IGNORE_INDEX = !Boolean.getBoolean("versioning.git.noignoreindex"); //NOI18N
    private final Git git = Git.getInstance();

    public FileStatusCache() {
        cachedFiles = new HashMap<>();
        conflictedFiles = createCacheIndex();
        modifiedFiles = createCacheIndex();
        ignoredFiles = createCacheIndex();
        ignoredFilesHandler = new IgnoredFilesHandler();
        ignoredFilesHandlerTask = rp.create(ignoredFilesHandler);
    }

    /**
     * Fast version of {@link #getStatus(java.io.File)}.
     * @param file
     * @return always returns a not null value
     */
    public FileInformation getStatus (final File file) {
        return getStatus(file, true);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listenerSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listenerSupport.removePropertyChangeListener(listener);
    }

    /**
     * Prepares refresh candidates, sorts them under their repository roots and eventually calls the cache refresh
     * @param files roots to refresh
     */
    public void refreshAllRoots(File... roots) {
        refreshAllRoots(Arrays.asList(roots), GitUtils.NULL_PROGRESS_MONITOR);
    }
    
    /**
     * Prepares refresh candidates, sorts them under their repository roots and eventually calls the cache refresh
     * @param files roots to refresh
     */
    public void refreshAllRoots (final Collection<File> files) {
        refreshAllRoots(files, GitUtils.NULL_PROGRESS_MONITOR);
    }
    
    /**
     * Prepares refresh candidates, sorts them under their repository roots and eventually calls the cache refresh
     * @param files roots to refresh
     * @param pm progress monitor able to cancel the running status scan
     */
    public void refreshAllRoots (final Collection<File> files, ProgressMonitor pm) {
        long startTime = 0;
        if (LOG.isLoggable(Level.FINE)) {
            startTime = System.currentTimeMillis();
            LOG.log(Level.FINE, "refreshAll: starting for {0} files.", files.size()); //NOI18N
        }
        if (files.isEmpty()) {
            return;
        }
        HashMap<File, Collection<File>> rootFiles = new HashMap<>(5);

        for (File file : files) {
            if (pm.isCanceled()) {
                return;
            }
            // go through all files and sort them under repository roots
            file = FileUtil.normalizeFile(file);
            File repository = git.getRepositoryRoot(file);
            File parentFile;
            File parentRepository;
            if (repository == null) {
                // we have an unversioned root, maybe the whole subtree should be removed from cache (VCS owners might have changed)
                continue;
            } else if (repository.equals(file) && (parentFile = file.getParentFile()) != null
                    && (parentRepository = git.getRepositoryRoot(parentFile)) != null) {
                addUnderRoot(rootFiles, parentRepository, file);
            }
            addUnderRoot(rootFiles, repository, file);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "refreshAll: starting status scan for {0} after {1}", new Object[]{rootFiles.values(), System.currentTimeMillis() - startTime}); //NOI18N
            startTime = System.currentTimeMillis();
        }
        if (!rootFiles.isEmpty()) {
            refreshAllRoots(rootFiles, pm);
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "refreshAll: finishes status scan after {0}", (System.currentTimeMillis() - startTime)); //NOI18N
        }
    }

    /**
     * Refreshes all files under given roots in the cache.
     * @param rootFiles root files to scan sorted under their repository roots
     */
    public void refreshAllRoots (Map<File, Collection<File>> rootFiles) {
        refreshAllRoots(rootFiles, GitUtils.NULL_PROGRESS_MONITOR);
    }

    /**
     * Refreshes all files under given roots in the cache.
     * @param rootFiles root files to scan sorted under their repository roots
     * @param pm progress monitor capable of interrupting the status scan
     */
    public void refreshAllRoots (Map<File, Collection<File>> rootFiles, ProgressMonitor pm) {
        for (Map.Entry<File, Collection<File>> refreshEntry : rootFiles.entrySet()) {
            if (pm.isCanceled()) {
                return;
            }
            File repository = refreshEntry.getKey();
            if (repository == null) {
                continue;
            }
            File syncRepo = getSyncRepository(repository);
            // Synchronize on the repository, refresh should not run concurrently
            synchronized (syncRepo) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "refreshAllRoots() roots: {0}, repositoryRoot: {1} ", new Object[] {refreshEntry.getValue(), repository.getAbsolutePath()}); // NOI18N
                }
                Map<File, GitStatus> interestingFiles;
                GitClient client = null;
                try {
                    // find all files with not up-to-date or ignored status
                    client = git.getClient(repository);
                    interestingFiles = client.getStatus(refreshEntry.getValue().toArray(new File[refreshEntry.getValue().size()]), pm);
                    if (pm.isCanceled()) {
                        return;
                    }
                    for (File root : refreshEntry.getValue()) {
                        // clean all files originally in the cache but now being up-to-date or obsolete (as ignored && deleted)
                        for (File file : listFiles(Collections.singleton(root), EnumSet.complementOf(EnumSet.of(Status.UPTODATE)))) {
                            FileInformation fi = getInfo(file);
                            if (fi == null || fi.containsStatus(Status.UPTODATE)) {
                                LOG.log(Level.WARNING, "refreshAllRoots(): possibly concurrent refresh: {0}:{1}", new Object[] { file, fi });
                                fi = new FileInformation(EnumSet.of(Status.UPTODATE), file.isDirectory());
                                boolean ea = false;
                                assert ea = true;
                                if (ea) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException ex) { }
                                    if (new HashSet<>(Arrays.asList(listFiles(Collections.singleton(file.getParentFile()), EnumSet.complementOf(EnumSet.of(Status.UPTODATE))))).contains(file)) {
                                        LOG.log(Level.WARNING, "refreshAllRoots(): now we have a problem, index seems to be broken", new Object[] { file });
                                    }
                                }
                                continue;
                            }
                            boolean exists = file.exists();
                            File filesOwner = null;
                            boolean correctRepository = true;
                            if (!interestingFiles.containsKey(file) // file no longer has an interesting status
                                    && (fi.containsStatus(Status.NOTVERSIONED_EXCLUDED) && (!exists || // file was ignored and is now deleted
                                    fi.isDirectory() && !GitUtils.isIgnored(file, true)) ||  // folder is now up-to-date (and NOT ignored by Sharability)
                                    !fi.isDirectory() && !fi.containsStatus(Status.NOTVERSIONED_EXCLUDED)) // file is now up-to-date or also ignored by .gitignore
                                    && (correctRepository = !repository.equals(file) && repository.equals(filesOwner = git.getRepositoryRoot(file)))) { // do not remove info for gitlinks or nested repositories
                                LOG.log(Level.FINE, "refreshAllRoots() uninteresting file: {0} {1}", new Object[]{file, fi}); // NOI18N
                                refreshFileStatus(file, FILE_INFORMATION_UNKNOWN); // remove the file from cache
                            }
                            if (!correctRepository) {
                                if (nestedRepositories.add(filesOwner)) {
                                    LOG.log(Level.INFO, "refreshAllRoots: nested repository found: {0} contains {1}", new File[] {repository, filesOwner}); //NOI18N
                                }
                            }
                        }
                    }
                    refreshStatusesBatch(interestingFiles);
                } catch (GitException ex) {
                    LOG.log(Level.INFO, "refreshAllRoots() file: {0} {1} {2} ", new Object[] {repository.getAbsolutePath(), refreshEntry.getValue(), ex.toString()}); //NOI18N
                } finally {
                    if (client != null) {
                        client.release();
                    }
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "refreshAllRoots() roots: finished repositoryRoot: {0} ", new Object[] { repository.getAbsolutePath() } ); // NOI18N
                    }
                }
            }
        }
    }

    /**
     * Evaluates if there are any files with the given status under the given roots
     *
     * @param context context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return true if there are any files with the given status otherwise false
     */
    public boolean containsFiles (VCSContext context, Set<Status> includeStatus, boolean addExcluded) {
        Set<File> roots = context.getRootFiles();
        // check all files underneath the roots
        return containsFiles(roots, includeStatus, addExcluded);
    }

    /**
     * Evaluates if there are any files with the given status under the given roots
     *
     * @param rootFiles context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return true if there are any files with the given status otherwise false
     */
    public boolean containsFiles (Set<File> roots, Set<Status> includeStatus, boolean addExcluded) {
        Set<File> repositories = GitUtils.getRepositoryRoots(roots);
        // get as deep as possible, so Turbo.readEntry() - which accesses io - gets called the least times
        // in such case we may end up with just access to io - getting the status of indeed modified file
        // the other way around it would check status for all directories along the path
        for (File root : roots) {
            if(containsFilesIntern(getIndexValues(root, includeStatus, repositories), includeStatus, !VersioningSupport.isFlat(root), addExcluded, 1, repositories)) {
                return true;
            }
        }

        // check to roots if they apply to the given status
        if (containsFilesIntern(roots, includeStatus, false, addExcluded, 0, repositories)) {
            return true;
        }
        return false;
    }
        
    /**
     * Lists <b>interesting files</b> that are known to be inside given folders.
     * These are locally and remotely modified and ignored files.
     *
     * @param roots context to examine
     * @return File [] array of interesting files
     */
    public File [] listFiles (File... roots) {
        return listFiles(Arrays.asList(roots), FileInformation.STATUS_ALL);
    }
    
    /**
     * Lists <b>interesting files</b> that are known to be inside given folders.
     * These are locally and remotely modified and ignored files.
     *
     * @param roots context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return File [] array of interesting files
     */
    public File [] listFiles (File[] roots, EnumSet<Status> includeStatus) {
        return listFiles(Arrays.asList(roots), includeStatus);
    }
    
    /**
     * Lists <b>interesting files</b> that are known to be inside given folders.
     * These are locally and remotely modified and ignored files.
     *
     * @param roots context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return File [] array of interesting files
     */
    public File [] listFiles (Collection<File> roots, EnumSet<Status> includeStatus) {
        Set<File> set = new HashSet<>();
        Set<File> repositories = GitUtils.getRepositoryRoots(roots);

        // get all files with given status underneath the roots files;
        // do it recusively if root isn't a flat folder
        for (File root : roots) {
            set.addAll(listFilesIntern(getIndexValues(root, includeStatus, repositories), includeStatus, !VersioningSupport.isFlat(root), repositories));
        }
        // check also the root files for status and add them eventually
        set.addAll(listFilesIntern(roots, includeStatus, false, repositories));
        return set.toArray(new File[0]);
    }
    
    /**
     * Returns the cached file information or null if it does not exist in the cache.
     * @param file
     * @return
     */
    private FileInformation getInfo (File file) {
        FileInformation info;
        synchronized (cachedFiles) {
            info = cachedFiles.get(file);
            synchronized (upToDateFiles) {
                if (info == null && upToDateFiles.contains(file)) {
                    addUpToDate(file);
                    info = FILE_INFORMATION_UPTODATE;
                }
            }
        }
        return info;
    }

    /**
     * Sets FI for the given files
     * @param file
     * @param info
     */
    private void setInfo (File file, FileInformation info) {
        synchronized (cachedFiles) {
            cachedFiles.put(file, info);
            if (!hugeCacheWarningLogged && cachedFiles.size() > CACHE_SIZE_WARNING_THRESHOLD) {
                LOG.log(Level.WARNING, "Cache contains too many entries: {0}", (Integer) cachedFiles.size()); //NOI18N
                hugeCacheWarningLogged = true;
            }
            removeUpToDate(file);
        }
    }

    /**
     * Removes the cached value for the given file. Call e.g. if the file becomes up-to-date
     * or uninteresting (no longer existing ignored file).
     * @param file
     */
    private void removeInfo (File file) {
        synchronized (cachedFiles) {
            cachedFiles.remove(file);
            removeUpToDate(file);
        }
    }

    /**
     * Adds an up-to-date file to the cache of UTD files.
     * The cache should have a limited size, so if a threshold is reached, the oldest file is automatically removed.
     * @param file file to add
     */
    private void addUpToDate (File file) {
        synchronized (upToDateFiles) {
            upToDateFiles.remove(file);
            upToDateFiles.add(file); // add the file to the end of the linked collection
            if (upToDateFiles.size() >= MAX_COUNT_UPTODATE_FILES) {
                if (LOG.isLoggable(Level.FINE)) {
                    // trying to find a reasonable limit for uptodate files in cache
                    LOG.log(Level.WARNING, "Cache of uptodate files grows too quickly: {0}", upToDateFiles.size()); //NOI18N
                    MAX_COUNT_UPTODATE_FILES <<= 1;
                    assert false;
                } else {
                    // removing 1/8 eldest entries
                    Iterator<File> it = upToDateFiles.iterator();
                    int toDelete = MAX_COUNT_UPTODATE_FILES >> 3;
                    for (int i = 0; i < toDelete && it.hasNext(); ++i) {
                        it.next();
                        it.remove();
                    }
                }
            }
        }
    }

    private boolean removeUpToDate (File file) {
        synchronized (upToDateFiles) {
            return upToDateFiles.remove(file);
        }
    }

    /**
     * TODO: go through the logic once more, it seems very very complex
     * Fast version of {@link #getStatus(java.io.File)}.
     * @param file
     * @param seenInUI false value means the file/folder is not visible in UI and thus cannot trigger initial git status scan
     * @return always returns a not null value
     */
    private FileInformation getStatus (final File file, boolean seenInUI) {
        FileInformation info = getInfo(file); // cached value
        LOG.log(Level.FINER, "getCachedStatus for file {0}: {1}", new Object[] {file, info}); //NOI18N
        boolean triggerGitScan = false;
        boolean addAsExcluded = false;
        if (info == null) {
            if (git.isManaged(file)) {
                // ping repository scan, this means it has not yet been scanned
                // but scan only files/folders visible in IDE
                triggerGitScan = seenInUI;
                // fast ignore-test
                info = checkForIgnoredFile(file);
                if (info == null) {
                    // info could have changed in the previous call
                    info = getInfo(file);
                }
                if (file.isDirectory()) {
                    setInfo(file, info = (info != null && info.containsStatus(Status.NOTVERSIONED_EXCLUDED)
                            ? new FileInformation(EnumSet.of(Status.NOTVERSIONED_EXCLUDED), true)
                            : new FileInformation(EnumSet.of(Status.UPTODATE), true)));
                } else {
                    if (info == null || info.containsStatus(Status.UPTODATE)) {
                        info = FILE_INFORMATION_UPTODATE;
                        addUpToDate(file);
                        // XXX delete later
                        if (++upToDateAccess > UTD_NOTIFY_NUMBER) {
                            upToDateAccess = 0;
                            if (LOG.isLoggable(Level.FINE)) {
                                synchronized (upToDateFiles) {
                                    LOG.log(Level.FINE, "Another {0} U2D files added: {1}", new Object[] {UTD_NOTIFY_NUMBER, upToDateFiles}); //NOI18N
                                }
                            }
                        }
                    } else if (info.containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
                        addAsExcluded = true;
                    }
                }
            } else {
                // unmanaged files
                info = file.isDirectory() ? new FileInformation(EnumSet.of(Status.NOTVERSIONED_NOTMANAGED), true) : FILE_INFORMATION_NOTMANAGED;
            }
            LOG.log(Level.FINER, "getCachedStatus: default for file {0}: {1}", new Object[] {file, info}); //NOI18N
        } else {
            // an u-t-d file may be actually ignored. This needs to be checked since we skip ignored folders in the status scan
            // so ignored files appear as up-to-date after the scan finishes
            if (info.containsStatus(Status.UPTODATE) && checkForIgnoredFile(file) != null) {
                info = FILE_INFORMATION_EXCLUDED;
                addAsExcluded = true;
            }
            triggerGitScan = seenInUI && !info.seenInUI();
        }
        if (addAsExcluded) {
            // add ignored file to cache
            rp.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (FileStatusCache.this) {
                        FileInformation info = getInfo(file);
                        if (info == null || info.containsStatus(Status.UPTODATE)) {
                            refreshFileStatus(file, file.isDirectory() 
                                    ? new FileInformation(EnumSet.of(Status.NOTVERSIONED_EXCLUDED), true)
                                    : FILE_INFORMATION_EXCLUDED);
                        }
                    }
                }
            });
        }
        if (triggerGitScan) {
            info.setSeenInUI(true); // next time this file/folder will not trigger the git scan
            git.getVCSInterceptor().pingRepositoryRootFor(file);
        }
        return info;
    }

    /**
     * Fires an event into IDE
     * @param file
     * @param oldInfo
     * @param newInfo
     */
    private void fireFileStatusChanged(File file, FileInformation oldInfo, FileInformation newInfo) {
        fireFileStatusChanged(new ChangedEvent(file, oldInfo, newInfo));
    }

    private void fireFileStatusChanged (ChangedEvent event) {
        listenerSupport.firePropertyChange(PROP_FILE_STATUS_CHANGED, null, event);
    }
    
    /**
     * Updates cache with scanned information for the given file
     * @param file
     * @param fi
     * @param interestingFiles
     * @param alwaysFireEvent
     */
    private void refreshFileStatus(File file, FileInformation fi) {
        if(file == null || fi == null) return;
        FileInformation current;
        boolean fireEvent = true;
        synchronized (this) {
            file = FileUtil.normalizeFile(file);
            current = getInfo(file);
            fi = checkForIgnore(fi, current, file);
            if (equivalent(fi, current)) {
                // no need to fire an event
                if (Utilities.isWindows() || Utilities.isMac()) {
                    // but for these we need to update keys in cache because of renames AAA.java -> aaa.java
                    fireEvent = false;
                } else {
                    return;
                }
            }
            boolean addToIndex = updateCachedValue(fi, file);
            updateIndex(file, fi, addToIndex);
        }
        if (fireEvent) {
            fireFileStatusChanged(file, current, fi);
        }
    }
    
    private void refreshStatusesBatch (Map<File, GitStatus> interestingFiles) {
        List<ChangedEvent> events = new ArrayList<>(interestingFiles.size());
        synchronized (this) {
            List<IndexUpdateItem> indexUpdates = new ArrayList<>(interestingFiles.size());
            for (Map.Entry<File, GitStatus> interestingEntry : interestingFiles.entrySet()) {
                // put the file's FI into the cache
                File file = interestingEntry.getKey();
                FileInformation fi = new FileInformation(interestingEntry.getValue());
                LOG.log(Level.FINE, "refreshAllRoots() file status: {0} {1}", new Object[] {file.getAbsolutePath(), fi}); // NOI18N
                
                FileInformation current;
                boolean fireEvent = true;
                current = getInfo(file);
                fi = checkForIgnore(fi, current, file);
                if (equivalent(fi, current)) {
                    // no need to fire an event
                    if (Utilities.isWindows() || Utilities.isMac()) {
                        // but for these we need to update keys in cache because of renames AAA.java -> aaa.java
                        fireEvent = false;
                    } else {
                        continue;
                    }
                }
                boolean addToIndex = updateCachedValue(fi, file);
                indexUpdates.add(new IndexUpdateItem(file, fi, addToIndex));
                if (fireEvent) {
                    events.add(new ChangedEvent(file, current, fi));
                }
            }
            updateIndexBatch(indexUpdates);
        }
        for (ChangedEvent event : events) {
            fireFileStatusChanged(event);
        }
    }

    private FileInformation checkForIgnore (FileInformation fi, FileInformation current, File file) {
        if ((equivalent(FILE_INFORMATION_NEWLOCALLY, fi)
                || // ugly piece of code, call sharability for U2D files only when toggling between ignored and U2D, otherwise SQ is called for EVERY U2D file
                (current != null && fi.getStatus().contains(Status.UPTODATE) && current.getStatus().contains(Status.NOTVERSIONED_EXCLUDED))) && (GitUtils.isIgnored(file, true) || isParentIgnored(file))) {
            // file lies under an excluded parent
            LOG.log(Level.FINE, "refreshFileStatus() file: {0} was LocallyNew but is NotSharable", file.getAbsolutePath()); // NOI18N
            fi = file.isDirectory() ? new FileInformation(EnumSet.of(Status.NOTVERSIONED_EXCLUDED), true) : FILE_INFORMATION_EXCLUDED;
        }
        return fi;
    }

    private boolean updateCachedValue (FileInformation fi, File file) {
        boolean addToIndex = false;
        if (fi.getStatus().equals(EnumSet.of(Status.UNKNOWN))) {
            removeInfo(file);
        } else if (fi.getStatus().equals(EnumSet.of(Status.UPTODATE)) && file.isFile()) {
            removeInfo(file);
            addUpToDate(file);
        } else {
            setInfo(file, fi);
            addToIndex = true;
        }
        return addToIndex;
    }

    /**
     * Two FileInformation objects are equivalent if their status contants are equal AND they both reperesent a file (or
     * both represent a directory) AND Entries they cache, if they can be compared, are equal.
     *
     * @param other object to compare to
     * @return true if status constants of both object are equal, false otherwise
     */
    private static boolean equivalent (FileInformation main, FileInformation other) {
        boolean retval;
        if (other != null && main.getStatus().equals(other.getStatus()) && main.isDirectory() == other.isDirectory()) {
            retval = main.getStatusText().equals(other.getStatusText());
        } else {
            retval = false;
        }
        return retval;
    }

    private boolean containsFilesIntern (Set<File> indexRoots, Set<Status> includeStatus, boolean recursively, boolean addExcluded, int depth, Set<File> repositories) {
        if(indexRoots == null || indexRoots.isEmpty()) {
            return false;
        }
        // get as deep as possible, so Turbo.readEntry() - which accesses io - gets called the least times
        // in such case we may end up with just access to io - getting the status of indeed modified file
        // the other way around it would check status for all directories along the path
        for (File root : indexRoots) {
            Set<File> indexValues = getIndexValues(root, includeStatus, repositories);
            if(recursively && containsFilesIntern(indexValues, includeStatus, recursively, addExcluded, depth + 1, repositories)) {
                return true;
            }
        }
        for (File root : indexRoots) {
            FileInformation fi = getInfo(root);
            if (fi != null && fi.containsStatus(includeStatus) && (addExcluded || depth == 0
                    || !GitModuleConfig.getDefault().isExcludedFromCommit(root.getAbsolutePath()))) {
                return true;
            }
        }
        return false;
    }

    private Set<File> listFilesIntern (Collection<File> roots, EnumSet<Status> includeStatus, boolean recursively, Set<File> queriedRepositories) {
        if(roots == null || roots.isEmpty()) {
            return Collections.<File>emptySet();
        }
        Set<File> ret = new HashSet<>();
        for (File root : roots) {
            if(recursively) {
                ret.addAll(listFilesIntern(getIndexValues(root, includeStatus, queriedRepositories), includeStatus, recursively, queriedRepositories));
            }
            FileInformation fi = getInfo(root);
            if (fi == null || !fi.containsStatus(includeStatus)) {
                continue;
            }
            ret.add(root);
        }
        return ret;
    }

    private static CacheIndex createCacheIndex() {
        return new CacheIndex() {
            @Override
            protected boolean isManaged (File file) {
                return Git.getInstance().isManaged(file);
            }
        };
    }

    private Set<File> getIndexValues (File root, Set<Status> includeStatus, Set<File> queriedRepositories) {
        File[] modified = new File[0];
        File[] ignored = new File[0];
        if (includeStatus.contains(Status.NOTVERSIONED_EXCLUDED)) {
            ignored = ignoredFiles.get(root);
        }
        if (FileInformation.STATUS_LOCAL_CHANGES.clone().removeAll(includeStatus)) {
            if (includeStatus.equals(EnumSet.of(Status.IN_CONFLICT))) {
                modified = conflictedFiles.get(root);
            } else {
                modified = modifiedFiles.get(root);
            }
        }
        Set<File> values = new HashSet<>(Arrays.asList(ignored));
        values.addAll(Arrays.asList(modified));
        if (queriedRepositories != null) {
            values = checkBelongToRepository(values, queriedRepositories);
        }
        return values;
    }
    
    private Set<File> checkBelongToRepository (Set<File> files, Set<File> repositories) {
        for (Iterator<File> it = files.iterator(); it.hasNext(); ) {
            File f = it.next();
            File repo = git.getRepositoryRoot(f);
            if (!f.equals(repo) // git link
                    && !repositories.contains(repo)) { // from a subrepo
                it.remove();
            }
        }
        return files;
    }

    private void updateIndex(File file, FileInformation fi, boolean addToIndex) {
        File parent = file.getParentFile();
        if (parent != null) {
            Set<File> conflicted = new HashSet<>(Arrays.asList(conflictedFiles.get(parent)));
            Set<File> modified = new HashSet<>(Arrays.asList(modifiedFiles.get(parent)));
            Set<File> ignored = new HashSet<>(Arrays.asList(ignoredFiles.get(parent)));
            boolean modifiedChange = modified.remove(file);
            boolean conflictedChange = conflicted.remove(file);
            boolean ignoredChange = USE_IGNORE_INDEX && ignored.remove(file);
            if (addToIndex) {
                if (fi.containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
                    ignoredChange |= USE_IGNORE_INDEX && ignored.add(file);
                } else {
                    modifiedChange |= modified.add(file);
                    if (fi.containsStatus(Status.IN_CONFLICT)) {
                        conflictedChange |= conflicted.add(file);
                    }
                }
            }
            if (modifiedChange) {
                modifiedFiles.add(parent, modified);
            }
            if (conflictedChange) {
                conflictedFiles.add(parent, conflicted);
            }
            if (ignoredChange) {
                ignoredFiles.add(parent, ignored);
            }
        }
    }

    private void updateIndexBatch (List<IndexUpdateItem> updates) {
        Map<File, Set<File>> modifications = new HashMap<>();
        Map<File, Set<File>> conflicts = new HashMap<>();
        Map<File, Set<File>> ignores = new HashMap<>();
        for (IndexUpdateItem item : updates) {
            File file = item.getFile();
            File parent = file.getParentFile();
            if (parent != null) {
                Set<File> modified = get(modifications, parent, modifiedFiles);
                Set<File> conflicted = get(conflicts, parent, conflictedFiles);
                modified.remove(file);
                conflicted.remove(file);
                Set<File> ignored = null;
                if (USE_IGNORE_INDEX) {
                    ignored = get(ignores, parent, ignoredFiles);
                    ignored.remove(file);
                }
                if (item.isAdd()) {
                    FileInformation fi = item.getInfo();
                    if (fi.containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
                        if (USE_IGNORE_INDEX) {
                            ignored.add(file);
                        }
                    } else {
                        modified.add(file);
                        if (fi.containsStatus(Status.IN_CONFLICT)) {
                            conflicted.add(file);
                        }
                    }
                }
            }
        }
        for (Map.Entry<File, Set<File>> e : modifications.entrySet()) {
            modifiedFiles.add(e.getKey(), e.getValue());
        }
        for (Map.Entry<File, Set<File>> e : conflicts.entrySet()) {
            conflictedFiles.add(e.getKey(), e.getValue());
        }
        for (Map.Entry<File, Set<File>> e : ignores.entrySet()) {
            ignoredFiles.add(e.getKey(), e.getValue());
        }
    }

    private Set<File> get (Map<File, Set<File>> cached, File parent, CacheIndex index) {
        Set<File> modified = cached.get(parent);
        if (modified == null) {
            modified = new HashSet<>(Arrays.asList(index.get(parent)));
            cached.put(parent, modified);
        }
        return modified;
    }

    /**
     * Fast (can be run from AWT) version of {@link #handleIgnoredFiles(Set)}, tests a file if it's ignored, but never runs a SharebilityQuery.
     * If the file is not recognized as ignored, runs {@link #handleIgnoredFiles(Set)}.
     * @param file
     * @return {@link #FILE_INFORMATION_EXCLUDED} if the file is recognized as ignored (but not through a SharebilityQuery), <code>null</code> otherwise
     */
    private FileInformation checkForIgnoredFile (File file) {
        FileInformation fi = null;
        if (file.getParentFile() != null && isParentIgnored(file)) {
            fi = FILE_INFORMATION_EXCLUDED;
        } else {
            // run the full test with the SQ
            handleIgnoredFiles(Collections.singleton(file));
        }
        return fi;
    }

    /**
     * Checks if given files are ignored, also calls a SharebilityQuery. Cached status for ignored files is eventually refreshed.
     * Can be run from AWT, in that case it switches to a background thread.
     * @param files set of files to be ignore-tested.
     */
    private void handleIgnoredFiles(final Set<File> files) {
        boolean changed;
        synchronized (ignoredFilesHandler.toHandle) {
            changed = ignoredFilesHandler.toHandle.addAll(files);
        }
        if (changed) {
            ignoredFilesHandlerTask.schedule(0);
        }
    }

    private boolean isParentIgnored (File file) {
        File parentFile = file.getParentFile();
        boolean parentIgnored = getStatus(parentFile, false).containsStatus(Status.NOTVERSIONED_EXCLUDED);
        // but the parent may be another repository root ignored by the parent repository
        if (parentFile.equals(git.getRepositoryRoot(parentFile))) {
            parentIgnored = false;
        }
        return parentIgnored;
    }
    
    private class IgnoredFilesHandler implements Runnable {
        
        private final Set<File> toHandle = new LinkedHashSet<>();
        
        @Override
        public void run() {
            File f;
            while ((f = getNextFile()) != null) {
                if (GitUtils.isIgnored(f, true)) {
                    // refresh status for this file
                    boolean isDirectory = f.isDirectory();
                    boolean exists = f.exists();
                    if (!exists) {
                        // remove from cache
                        refreshFileStatus(f, FILE_INFORMATION_UNKNOWN);
                    } else {
                        // add to cache as ignored
                        refreshFileStatus(f, isDirectory ? new FileInformation(EnumSet.of(Status.NOTVERSIONED_EXCLUDED), true) : FILE_INFORMATION_EXCLUDED);
                    }
                }
            }
        }

        private File getNextFile() {
            File nextFile = null;
            synchronized (toHandle) {
                Iterator<File> it = toHandle.iterator();
                if (it.hasNext()) {
                    nextFile = it.next();
                    it.remove();
                }
            }
            return nextFile;
        }
        
    }

    private File getSyncRepository (File repository) {
        File cachedRepository = git.getRepositoryRoot(repository);
        if (repository.equals(cachedRepository)) {
            repository = cachedRepository;
        }
        synchronized (SYNC_REPOSITORIES) {
            cachedRepository = SYNC_REPOSITORIES.get(repository);
            if (cachedRepository == null) {
                // create a NEW instance, otherwise we'll lock also git commands that sync on repository root, too
                cachedRepository = new File(repository.getParentFile(), repository.getName());
                SYNC_REPOSITORIES.put(cachedRepository, cachedRepository);
            }
        }
        return cachedRepository;
    }

    private void addUnderRoot (HashMap<File, Collection<File>> rootFiles, File repository, File file) {
        // file is a gitlink inside another repository, we need to refresh also the file's status explicitely
        Collection<File> filesUnderRoot = rootFiles.get(repository);
        if (filesUnderRoot == null) {
            filesUnderRoot = new HashSet<>();
            rootFiles.put(repository, filesUnderRoot);
        }
        GitUtils.prepareRootFiles(repository, filesUnderRoot, file);
    }

    public static class ChangedEvent {

        private final File file;
        private final FileInformation oldInfo;
        private final FileInformation newInfo;

        public ChangedEvent(File file, FileInformation oldInfo, FileInformation newInfo) {
            this.file = file;
            this.oldInfo = oldInfo;
            this.newInfo = newInfo;
        }

        public File getFile() {
            return file;
        }

        public FileInformation getOldInfo() {
            return oldInfo;
        }

        public FileInformation getNewInfo() {
            return newInfo;
        }
    }

    private static class IndexUpdateItem {

        private final File file;
        private final FileInformation fi;
        private final boolean add;

        public IndexUpdateItem (File file, FileInformation fi, boolean toBeAdded) {
            this.file = file;
            this.fi = fi;
            this.add = toBeAdded;
        }

        public File getFile () {
            return file;
        }

        public FileInformation getInfo () {
            return fi;
        }

        public boolean isAdd () {
            return add;
        }
    }
}
