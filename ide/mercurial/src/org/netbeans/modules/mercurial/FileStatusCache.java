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
package org.netbeans.modules.mercurial;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;


/**
 * Central part of status management, deduces and caches statuses of files under version control.
 *
 * @author Ondra Vrabec
 */
public class FileStatusCache {

    /**
     * Indicates that status of a file changed and listeners SHOULD check new status
     * values if they are interested in this file.
     * The New value is a ChangedEvent object (old FileInformation object may be null)
     */
    public static final String PROP_FILE_STATUS_CHANGED = "status.changed"; // NOI18N
    public static final FileStatus REPOSITORY_STATUS_UNKNOWN  = null;
    public static final boolean FULL_REPO_SCAN_ENABLED = "true".equals(System.getProperty("versioning.mercurial.fullRepoScanEnabled", "false")); //NOI18N

    private static final FileInformation FILE_INFORMATION_EXCLUDED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, false);
    private static final FileInformation FILE_INFORMATION_UPTODATE = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, false);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, true);
    private static final FileInformation FILE_INFORMATION_UNKNOWN = new FileInformation(FileInformation.STATUS_UNKNOWN, false);
    private static final FileInformation FILE_INFORMATION_NEWLOCALLY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, false);
    public static final FileInformation FILE_INFORMATION_CONFLICT = new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT, false);
    public static final FileInformation FILE_INFORMATION_REMOVEDLOCALLY = new FileInformation(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, false);
    private int MAX_COUNT_UPTODATE_FILES = 1024;
    private static final int CACHE_SIZE_WARNING_THRESHOLD = 50000; // log when cache gets too big and steps over this threshold
    private boolean hugeCacheWarningLogged;

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.mercurial.fileStatusCacheNewGeneration"); //NOI18N
    private static final Logger LOG_UPTODATE_FILES = Logger.getLogger("mercurial.cache.upToDateFiles"); //NOI18N

    private PropertyChangeSupport listenerSupport = new PropertyChangeSupport(this);
    private Mercurial     hg;
    /**
     * Keeps cached statuses for managed files
     */
    private final Map<File, FileInformation> cachedFiles;
    private final LinkedHashSet<File> upToDateFiles = new LinkedHashSet<File>(MAX_COUNT_UPTODATE_FILES);
    private final RequestProcessor rp = new RequestProcessor("Mercurial.cacheNG", 1, true);
    private final HashSet<File> nestedRepositories = new HashSet<File>(2); // mainly for logging

    FileStatusCache (Mercurial hg) {
        this.hg = hg;
        cachedFiles = new HashMap<File, FileInformation>();
    }

    /**
     * Checks if given files are ignored, also calls a SharebilityQuery. Cached status for ignored files is eventually refreshed.
     * Can be run from AWT, in that case it switches to a background thread.
     * @param files set of files to be ignore-tested.
     */
    private void handleIgnoredFiles(final Set<File> files) {
        Runnable outOfAWT = new Runnable() {
            @Override
            public void run() {
                for (File f : files) {
                    if (HgUtils.isIgnored(f, true)) {
                        // refresh status for this file
                        boolean isDirectory = f.isDirectory();
                        boolean exists = f.exists();
                        if (!exists) {
                            // remove from cache
                            refreshFileStatus(f, FILE_INFORMATION_UNKNOWN);
                        } else {
                            // add to cache as ignored
                            refreshFileStatus(f, isDirectory ? new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, true) : FILE_INFORMATION_EXCLUDED);
                        }
                    }
                }
            }
        };
        // always run outside of AWT, SQ inside isIgnored can last a long time
        if (EventQueue.isDispatchThread()) {
            rp.post(outOfAWT);
        } else {
            outOfAWT.run();
        }
    }

    /**
     * Fast (can be run from AWT) version of {@link #handleIgnoredFiles(Set)}, tests a file if it's ignored, but never runs a SharebilityQuery.
     * If the file is not recognized as ignored, runs {@link #handleIgnoredFiles(Set)}.
     * @param file
     * @return true if the file is recognized as ignored (but not through a SharebilityQuery)
     */
    private FileInformation checkForIgnoredFile (File file) {
        FileInformation fi = null;
        if (HgUtils.isIgnored(file, false)) {
            fi = FILE_INFORMATION_EXCLUDED;
        } else {
            // run the full test with the SQ
            handleIgnoredFiles(Collections.singleton(file));
        }
        return fi;
    }

    /**
     * Returns the cached file information or null if it does not exist in the cache.
     * @param file
     * @return
     */
    private FileInformation getInfo(File file) {
        FileInformation info = null;
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
                if (LOG_UPTODATE_FILES.isLoggable(Level.FINE)) {
                    // trying to find a reasonable limit for uptodate files in cache
                    LOG_UPTODATE_FILES.log(Level.WARNING, "Cache of uptodate files grows too quickly: {0}", upToDateFiles.size()); //NOI18N
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
     * Do not call from AWT.
     * Can result in a status call. Returns a cached status and runs a status command for not cached files (e.g. up to date files)
     * @param file
     * @return
     */
    public FileInformation getStatus(File file) {
        // at first get cached info
        FileInformation fi = getInfo(file);
        if (fi == null) { // no info cached
            boolean isAdministrative = HgUtils.isAdministrative(file); // is the file a .hg folder?
            boolean isDirectory = isAdministrative || file.isDirectory(); // is the file a directory? .hg folder is also a directory
            if (isDirectory && (isAdministrative || HgUtils.isIgnored(file))) { // is ignored?
                return new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, true);
            }
            if (!isDirectory && !exists(file)) { // does not exist - if isDirectory, then it SHOULD exist
                fi = FILE_INFORMATION_UNKNOWN;
            } else if (!isDirectory && HgUtils.isIgnored(file)) { // ignored file
                fi = FILE_INFORMATION_EXCLUDED;
            } else if (isDirectory) { // is a dir and not in cache - do refresh in here
                fi = refresh(file);
            } else { // exists, is a file and is not ignored and is not in cache - so is probably up to date
                fi = FILE_INFORMATION_UPTODATE;
            }
        }
        return fi;
    }

    int upToDateAccess = 0;
    private static final int UTD_NOTIFY_NUMBER = 100;

    /**
     * Fast version of {@link #getStatus(java.io.File)}.
     * @param file
     * @return always returns a not null value
     */
    public FileInformation getCachedStatus(final File file) {
        return getCachedStatus(file, false);
    }

    /**
     * Marks the file seen in UI. The file is added to seen roots and from now
     * on all external changes will trigger status refresh on this file.
     *
     * @param file
     * @return file's current cached status
     */
    FileInformation markAsSeenInUI (File file) {
        return getCachedStatus(file, true);
    }

    /**
     * Fast version of {@link #getStatus(java.io.File)}.
     * @param file
     * @param seenInUI false value means the file/folder is not visible in UI and thus cannot trigger initial hg status scan
     * @return always returns a not null value
     */
    private FileInformation getCachedStatus(final File file, boolean seenInUI) {
        FileInformation info = getInfo(file); // cached value
        LOG.log(Level.FINER, "getCachedStatus for file {0}: {1}", new Object[] {file, info}); //NOI18N
        boolean triggerHgScan = false;
        if (info == null) {
            if (hg.isManaged(file)) {
                // ping repository scan, this means it has not yet been scanned
                // but scan only files/folders visible in IDE
                triggerHgScan = seenInUI;
                // fast ignore-test
                info = checkForIgnoredFile(file);
                if (file.isDirectory()) {
                    info = createFolderFileInformation(file, info == null ? null : new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, true));
                } else {
                    if (info == null) {
                        info = FILE_INFORMATION_UPTODATE;
                        addUpToDate(file);
                        // XXX delete later
                        if (++upToDateAccess > UTD_NOTIFY_NUMBER) {
                            upToDateAccess = 0;
                            if (LOG_UPTODATE_FILES.isLoggable(Level.FINE)) {
                                synchronized (upToDateFiles) {
                                    LOG_UPTODATE_FILES.log(Level.FINE, "Another {0} U2D files added: {1}", new Object[] {Integer.valueOf(UTD_NOTIFY_NUMBER), upToDateFiles});
                                }
                            }
                        }
                    } else {
                        // add ignored file to cache
                        rp.post(new Runnable() {
                            @Override
                            public void run() {
                                refreshFileStatus(file, FILE_INFORMATION_EXCLUDED);
                            }
                        });
                    }
                }
            } else {
                // unmanaged files
                info = file.isDirectory() ? FILE_INFORMATION_NOTMANAGED_DIRECTORY : FILE_INFORMATION_NOTMANAGED;
            }
            LOG.log(Level.FINER, "getCachedStatus: default for file {0}: {1}", new Object[] {file, info}); //NOI18N
        } else {
            triggerHgScan = seenInUI && !info.wasSeenInUi();
        }
        if (triggerHgScan) {
            info.setSeenInUI(true); // next time this file/folder will not trigger the hg scan
            hg.getMercurialInterceptor().pingRepositoryRootFor(file);
        }
        return info;
    }

    /**
     * Puts folder's information into the cache.
     * @param folder
     * @param fi null means an up-to-date folder.
     * @return
     */
    private FileInformation createFolderFileInformation (File folder, FileInformation fi) {
        FileInformation info;
        // must lock, so possibly elsewhere-created information is not overwritten
        synchronized (cachedFiles) {
            info = getInfo(folder);
            if (info == null || !info.isDirectory()) { // not yet in cache or is stored as a file
                // create an uptodate directory
                info = fi == null ? new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, true) : fi;
                setInfo(folder, info);
            }
        }
        return info;
    }

    private Map<File, FileInformation> getModifiedFiles (File root, int includeStatus) {
        boolean check = false;
        assert check = true;
        Map<File, FileInformation> modifiedFiles = new HashMap<File, FileInformation>();
        FileInformation info = getCachedStatus(root);
        if ((info.getStatus() & includeStatus) != 0) {
            modifiedFiles.put(root, info);
        }
        for (File child : info.getModifiedChildren(false)) {
            if (check) {
                checkIsParentOf(root, child);
            }
            modifiedFiles.putAll(getModifiedFiles(child, includeStatus));
        }
        return modifiedFiles;
    }

    /**
     * Refreshes all files under given roots in the cache.
     * @param rootFiles root files to scan sorted under their repository roots
     */
    public void refreshAllRoots (Map<File, Set<File>> rootFiles) {
        for (Map.Entry<File, Set<File>> refreshEntry : rootFiles.entrySet()) {
            File repository = refreshEntry.getKey();
            if (repository == null) {
                continue;
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "refreshAllRoots() roots: {0}, repositoryRoot: {1} ", new Object[] {refreshEntry.getValue(), repository.getAbsolutePath()}); // NOI18N
            }
            Map<File, FileInformation> interestingFiles;
            try {
                // find all files with not up-to-date or ignored status
                interestingFiles = HgCommand.getStatus(repository, new LinkedList<File>(refreshEntry.getValue()), null, null);
                for (Map.Entry<File, FileInformation> interestingEntry : interestingFiles.entrySet()) {
                    // put the file's FI into the cache
                    File file = interestingEntry.getKey();
                    FileInformation fi = interestingEntry.getValue();
                    LOG.log(Level.FINE, "refreshAllRoots() file: {0} {1} ", new Object[] {file.getAbsolutePath(), fi}); // NOI18N
                    refreshFileStatus(file, fi);
                }
                for (File root : refreshEntry.getValue()) {
                // clean all files originally in the cache but now being up-to-date or obsolete (as ignored && deleted)
                for (Map.Entry<File, FileInformation> entry : getModifiedFiles(root, ~FileInformation.STATUS_VERSIONED_UPTODATE).entrySet()) {
                    File file = entry.getKey();
                    FileInformation fi = entry.getValue();
                    boolean exists = file.exists();
                    File filesOwner = null;
                    boolean correctRepository = true;
                    if (!interestingFiles.containsKey(file) // file no longer has an interesting status
                            && ((fi.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0 && !exists || // file was ignored and is now deleted
                            (fi.getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0 && (!exists || file.isFile())) // file is now up-to-date or also ignored by .hgignore
                            && (correctRepository = repository.equals(filesOwner = hg.getRepositoryRoot(file)))) { // do not remove info for nested repositories
                        LOG.log(Level.FINE, "refreshAllRoots() uninteresting file: {0} {1}", new Object[]{file, fi}); // NOI18N
                        // TODO better way to detect conflicts
                        if (HgCommand.existsConflictFile(file.getAbsolutePath())) {
                            refreshFileStatus(file, FILE_INFORMATION_CONFLICT); // set the files status to 'IN CONFLICT'
                        } else {
                            refreshFileStatus(file, FILE_INFORMATION_UNKNOWN); // remove the file from cache
                        }
                    }
                    if (!correctRepository) {
                        if (nestedRepositories.add(filesOwner)) {
                            LOG.log(Level.INFO, "refreshAllRoots: nested repository found: {0} contains {1}", new File[] {repository, filesOwner}); //NOI18N
                        }
                    }
                }
                }
            } catch (HgException ex) {
                LOG.log(Level.INFO, "refreshAll() file: {0} {1} {2} ", new Object[] {repository.getAbsolutePath(), refreshEntry.getValue(), ex.toString()}); //NOI18N
            }
        }
    }

    /**
     * Prepares refresh candidates, sorts them under their repository roots and eventually calls the cache refresh
     * @param files roots to refresh
     */
    public void refreshAllRoots (final Collection<File> files) {
        long startTime = 0;
        if (Mercurial.STATUS_LOG.isLoggable(Level.FINE)) {
            startTime = System.currentTimeMillis();
            Mercurial.STATUS_LOG.fine("refreshAll: starting for " + files.size() + " files."); //NOI18N
        }
        if (files.isEmpty()) {
            return;
        }
        HashMap<File, Set<File>> rootFiles = new HashMap<File, Set<File>>(5);

        for (File file : files) {
            // go through all files and sort them under repository roots
            file = FileUtil.normalizeFile(file);
            File repository = Mercurial.getInstance().getRepositoryRoot(file);
            if (repository == null) {
                // we have an unversioned root, maybe the whole subtree should be removed from cache (VCS owners might have changed)
                continue;
            }
            Set<File> filesUnderRoot = rootFiles.get(repository);
            if (filesUnderRoot == null) {
                filesUnderRoot = new HashSet<File>();
                rootFiles.put(repository, filesUnderRoot);
            }
            HgUtils.prepareRootFiles(repository, filesUnderRoot, file);
        }
        if (Mercurial.STATUS_LOG.isLoggable(Level.FINE)) {
            Mercurial.STATUS_LOG.fine("refreshAll: starting status scan for " + rootFiles.values() + " after " + (System.currentTimeMillis() - startTime)); //NOI18N
            startTime = System.currentTimeMillis();
        }
        if (!rootFiles.isEmpty()) {
            refreshAllRoots(rootFiles);
        }
        if (Mercurial.STATUS_LOG.isLoggable(Level.FINE)) {
            Mercurial.STATUS_LOG.fine("refreshAll: finishes status scan after " + (System.currentTimeMillis() - startTime)); //NOI18N
        }
    }

    /**
     * Refreshes the status of the root and all files under the root
     * @param root
     * @return status of the root itself
     */
    public FileInformation refresh (File root) {
        File repositoryRoot = hg.getRepositoryRoot(root);
        FileInformation fi;
        if (repositoryRoot == null) {
            if (root.isDirectory()) {
                fi = FILE_INFORMATION_NOTMANAGED_DIRECTORY;
            } else {
                fi = FILE_INFORMATION_NOTMANAGED;
            }
        } else {
            // start the recursive refresh
            refreshAllRoots(Collections.singletonMap(repositoryRoot, Collections.singleton(root)));
            // and return scanned value
            fi = getCachedStatus(root);
        }
        return fi;
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
        synchronized (this) {
            // XXX the question here is: do we want to keep ignored files in the cache (i mean those ignored by hg, not by SQ)?
            // if yes, add equivalent(FILE_INFORMATION_UNKNOWN, fi) into the following test
            if ((equivalent(FILE_INFORMATION_NEWLOCALLY, fi)) && (HgUtils.isIgnored(file)
                    || (getCachedStatus(file.getParentFile()).getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) != 0)) {
                // Sharebility query recognized this file as ignored
                LOG.log(Level.FINE, "refreshFileStatus() file: {0} was LocallyNew but is NotSharable", file.getAbsolutePath()); // NOI18N
                fi = file.isDirectory() ? new FileInformation(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, true) : FILE_INFORMATION_EXCLUDED;
            }
            file = FileUtil.normalizeFile(file);
            current = getInfo(file);
            if (equivalent(fi, current)) {
                // no need to fire an event
                return;
            }
            if (fi.getStatus() == FileInformation.STATUS_UNKNOWN) {
                removeInfo(file);
            } else if (fi.getStatus() == FileInformation.STATUS_VERSIONED_UPTODATE && file.isFile()) {
                removeInfo(file);
                addUpToDate(file);
            } else {
                setInfo(file, fi);
            }
            updateParentInformation(file, current, fi);
        }
        fireFileStatusChanged(file, current, fi);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listenerSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listenerSupport.removePropertyChangeListener(listener);
    }

    /**
     * Updates parent information
     * @param file
     * @param oldInfo
     * @param newInfo
     */
    private void updateParentInformation (File file, FileInformation oldInfo, FileInformation newInfo) {
        boolean check = false;
        assert check = true;
        File parent = file;
        FileInformation info;
        FileInformation childInfo = newInfo;
        // update all managed parents
        File child = file;
        while ((parent = parent.getParentFile()) != null && (info = getCachedStatus(parent, false)) != null && (info.getStatus() & FileInformation.STATUS_MANAGED) != 0) {
            if (!info.isDirectory()) {
                info = createFolderFileInformation(parent, null);
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "updateParentInformation: updating {0} with {1} triggered by {2}", new Object[]{parent, newInfo, file});
            }
            if (check) {
                checkIsParentOf(parent, child);
                if (info == FILE_INFORMATION_EXCLUDED || info == FILE_INFORMATION_UPTODATE || info == FILE_INFORMATION_NOTMANAGED
                        || info == FILE_INFORMATION_NOTMANAGED_DIRECTORY || info == FILE_INFORMATION_UNKNOWN || info == FILE_INFORMATION_NEWLOCALLY) {
                    throw new IllegalStateException("Wrong info, expected an own instance for " + parent + ", " + info.getStatusText() + " - " + info.getStatus()); //NOI18N
                    }
            }
            if (!info.setModifiedChild(child, childInfo)) {
                // do not notify parent
                break;
            }
            if (!info.getModifiedChildren(true).isEmpty() && (newInfo.getStatus() & FileInformation.STATUS_VERSIONED_CONFLICT) == 0) {
                childInfo = new FileInformation(FileInformation.STATUS_VERSIONED_CONFLICT, true);
            }
            child = parent;
        }
    }

    /**
     * Fires an event into IDE
     * @param file
     * @param oldInfo
     * @param newInfo
     */
    private void fireFileStatusChanged(File file, FileInformation oldInfo, FileInformation newInfo) {
        listenerSupport.firePropertyChange(PROP_FILE_STATUS_CHANGED, null, new ChangedEvent(file, oldInfo, newInfo));
    }

    private boolean exists(File file) {
        if (!file.exists()) return false;
        return file.getAbsolutePath().equals(FileUtil.normalizeFile(file).getAbsolutePath());
    }

    /**
     * Two FileInformation objects are equivalent if their status contants are equal AND they both reperesent a file (or
     * both represent a directory) AND Entries they cache, if they can be compared, are equal.
     *
     * @param other object to compare to
     * @return true if status constants of both object are equal, false otherwise
     */
    private static boolean equivalent(FileInformation main, FileInformation other) {
        if (other == null || main.getStatus() != other.getStatus() || main.isDirectory() != other.isDirectory()) return false;

        FileStatus e1 = main.getStatus(null);
        FileStatus e2 = other.getStatus(null);
        return e1 == e2 || e1 == null || e2 == null || equal(e1, e2);
    }

    /**
     * Replacement for missing Entry.equals(). It is implemented as a separate method to maintain compatibility.
     *
     * @param e1 first entry to compare
     * @param e2 second Entry to compare
     * @return true if supplied entries contain equivalent information
     */
    private static boolean equal(FileStatus e1, FileStatus e2) {
        // TODO: use your own logic here
        return true;
    }

    /**
     * Lists <b>modified files</b> that are known to be inside
     * this folder. There are locally modified files present
     * plus any files that exist in the folder in the remote repository.
     * Not recursive.
     *
     * @param dir folder to list
     * @return
     */
    public File [] listFiles (File dir) {
        Set<File> set = getStatus(dir).getModifiedChildren(false);
        return set.toArray(new File[0]);
    }


    /**
     * Check if this context has at least one file with the passed in status
     * @param context context to examine
     * @param includeStatus file status to check for
     * @param checkCommitExclusions if set to true then files excluded from commit will not be tested
     * @param cached if set to <code>true</code>, only cached values will be checked otherwise it may call I/O operations
     * @return boolean true if this context contains at least one file with the includeStatus, false otherwise
     */
    public boolean containsFileOfStatus(VCSContext context, int includeStatus, boolean checkCommitExclusions){
        return containsFileOfStatus(context.getRootFiles(), includeStatus, checkCommitExclusions);
    }

    /**
     * Check if any file from the roots has at least one file with the passed in status
     * @param roots root files to recursively check 
     * @param includeStatus file status to check for
     * @param checkCommitExclusions if set to true then files excluded from commit will not be tested
     * @param cached if set to <code>true</code>, only cached values will be checked otherwise it may call I/O operations
     * @return boolean true if this context contains at least one file with the includeStatus, false otherwise
     */
    public boolean containsFileOfStatus(Collection<File> roots, int includeStatus, boolean checkCommitExclusions) {
        for (File root : roots) {
            if (hasStatus(root, includeStatus, checkCommitExclusions)
                    || containsFileOfStatus(root, includeStatus, checkCommitExclusions, !VersioningSupport.isFlat(root))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsFileOfStatus(File root, int includeStatus, boolean checkExclusions, boolean recursive) {
        boolean check = false;
        assert check = true;
        FileInformation info = getCachedStatus(root);
        for (File child : info.getModifiedChildren(includeStatus == FileInformation.STATUS_VERSIONED_CONFLICT)) {
            if (check) {
                checkIsParentOf(root, child);
            }
            if (hasStatus(child, includeStatus, checkExclusions)) {
                return true;
            } else if (recursive && containsFileOfStatus(child, includeStatus, checkExclusions, recursive)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasStatus (File file, int includeStatus, boolean checkExclusions) {
        FileInformation info = getCachedStatus(file);
        return (info.getStatus() & includeStatus) != 0
                && (!checkExclusions || !HgModuleConfig.getDefault().isExcludedFromCommit(file.getAbsolutePath()));
    }

    /**
     * Lists <b>interesting files</b> that are known to be inside given folders.
     * These are locally and remotely modified and ignored files.
     *
     * @param context context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return File [] array of interesting files
     */
    public File [] listFiles(VCSContext context, int includeStatus) {
        Set<File> roots = context.getRootFiles();
        Set<File> set = listFilesIntern(roots.toArray(new File[0]), includeStatus);
        for (File excluded : context.getExclusions()) {
            for (Iterator j = set.iterator(); j.hasNext();) {
                File file = (File) j.next();
                if (Utils.isAncestorOrEqual(excluded, file)) {
                    j.remove();
                }
            }
        }
        return set.toArray(new File[0]);
    }

    /**
     * Lists <b>interesting files</b> that are known to be inside given folders.
     * These are locally modified and ignored files.
     *
     * Is not recursive for flat roots
     * 
     * @param roots context to examine
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return File [] array of interesting files
     */
    public File [] listFiles(File[] roots, int includeStatus) {
        Set<File> listedFiles = listFilesIntern(roots, includeStatus);
        return listedFiles.toArray(new File[0]);
    }

    private Set<File> listFilesIntern(File[] roots, int includeStatus) {
        Set<File> listedFiles = new HashSet<File>();
        for (File root : roots) {
            if (VersioningSupport.isFlat(root)) {
                for (File listed : listFiles(root)) {
                    if ((getCachedStatus(listed).getStatus() & includeStatus) != 0) {
                        listedFiles.add(listed);
                    }
                }
            } else {
                Map<File, FileInformation> modified = getModifiedFiles(root, includeStatus);
                for (File listed : modified.keySet()) {
                    listedFiles.add(listed);
                }
            }
        }
        return listedFiles;
    }

    public void notifyFileChanged(File file) {
        fireFileStatusChanged(file, null, FILE_INFORMATION_UPTODATE);
    }

    /**
     * Refreshes cached information about file and all its descendants.
     * Experimental method mainly for Ignore Action, SHOULD NOT be called elsewhere.
     * We cannot use pure refresh because hg does not track folders and folder info is permanently kept in cache.
     * @param file
     */
    public void refreshIgnores (File file) {
        Map<File, FileInformation> files = getModifiedFiles(file, FileInformation.STATUS_ALL);
        synchronized (this) {
            for (File f : files.keySet()) {
                refreshFileStatus(f, FILE_INFORMATION_UNKNOWN);
            }
        }
        refresh(file);
        LOG.log(Level.FINER, "refreshIgnores: File {0} refreshed", file); //NOI18N
    }

    public static class ChangedEvent {

        private File file;
        private FileInformation oldInfo;
        private FileInformation newInfo;

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

    private void checkIsParentOf(File parent, File child) {
        if (!parent.equals(child.getParentFile())) {
            throw new IllegalStateException(parent.getAbsolutePath() + " is not parent of " + child.getAbsolutePath());
        }
    }
}
