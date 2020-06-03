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

import java.util.Map;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import java.io.IOException;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.openide.util.RequestProcessor;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Callable;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.netbeans.modules.mercurial.remote.util.HgSearchHistorySupport;
import org.netbeans.modules.remotefs.versioning.api.SearchHistorySupport;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.util.DelayScanRegistry;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * Listens on file system changes and reacts appropriately, mainly refreshing affected files' status.
 * 
 * 
 */
public class MercurialInterceptor extends VCSInterceptor {

    private final FileStatusCache   cache;

    private final ConcurrentLinkedQueue<VCSFileProxy> filesToRefresh = new ConcurrentLinkedQueue<>();
    private final Map<VCSFileProxy, Set<VCSFileProxy>> lockedRepositories = new HashMap<>(5);

    private final RequestProcessor.Task refreshTask, lockedRepositoryRefreshTask;
    private final RequestProcessor.Task refreshOwnersTask;

    private static final RequestProcessor rp = new RequestProcessor("MercurialRemoteRefresh", 1, true); //NOI18N
    private final HgFolderEventsHandler hgFolderEventsHandler;
    private final CommandUsageLogger commandLogger;
    private static final boolean AUTOMATIC_REFRESH_ENABLED = !"true".equals(System.getProperty("versioning.mercurial.autoRefreshDisabled", "false")); //NOI18N
    private final Mercurial hg;
    private static final int STATUS_VCS_MODIFIED_ATTRIBUTE =
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | 
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY | 
            FileInformation.STATUS_VERSIONED_CONFLICT | 
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY;

    MercurialInterceptor(Mercurial hg, FileStatusCache cache) {
        this.cache = cache;
        this.hg = hg;
        refreshTask = rp.create(new RefreshTask());
        lockedRepositoryRefreshTask = rp.create(new LockedRepositoryRefreshTask());
        hgFolderEventsHandler = new HgFolderEventsHandler();
        commandLogger = new CommandUsageLogger();
        refreshOwnersTask = rp.create(new Runnable() {
            @Override
            public void run() {
                Mercurial hg = Mercurial.getInstance();
                hg.clearAncestorCaches();
                hg.versionedFilesChanged();
                VersioningSupport.versionedRootsChanged();
            }
        });
    }

    @Override
    public boolean beforeDelete(VCSFileProxy file) {
        Mercurial.LOG.log(Level.FINE, "beforeDelete {0}", file);
        if (file == null) {
            return false;
        }
        if (HgUtils.isPartOfMercurialMetadata(file)) {
            return false;
        }

        // we don't care about ignored files
        // IMPORTANT: false means mind checking the sharability as this might cause deadlock situations
        if(HgUtils.isIgnored(file, false)) {
            return false; // XXX what about other events?
        }
        return true;
    }

    @Override
    public void doDelete(VCSFileProxy file) throws IOException {
        // XXX runnig hg rm for each particular file when removing a whole firectory might no be neccessery:
        //     just delete it via file.delete and call, group the files in afterDelete and schedule a delete
        //     fo the parent or for a bunch of files at once. 
        Mercurial.LOG.log(Level.FINE, "doDelete {0}", file);
        if (file == null) {
            return;
        }
        VCSFileProxy root = hg.getRepositoryRoot(file);
        VCSFileProxySupport.delete(file);
        if (file.exists()) {
            IOException ex = new IOException();
            Exceptions.attachLocalizedMessage(ex, NbBundle.getMessage(MercurialInterceptor.class, "MSG_DeleteFailed", new Object[] { file })); //NOI18N
            throw ex;
        }
        try {
            HgCommand.doRemove(root, file, null);
        } catch (HgException ex) {
            Mercurial.LOG.log(Level.FINE, "doDelete(): File: {0} {1}", new Object[] {file.getPath(), ex.toString()}); // NOI18N
        }  
    }

    @Override
    public void afterDelete(final VCSFileProxy file) {
        Mercurial.LOG.log(Level.FINE, "afterDelete {0}", file);
        if (file == null) {
            return;
        }
        if (HgUtils.isPartOfMercurialMetadata(file) && HgUtils.WLOCK_FILE.equals(file.getName())) {
            commandLogger.unlocked(file);
        }
        if (HgUtils.HG_FOLDER_NAME.equals(file.getName())) {
            // new metadata created, we should refresh owners
            refreshOwnersTask.schedule(3000);
        }
        // we don't care about ignored files
        // IMPORTANT: false means mind checking the sharability as this might cause deadlock situations
        if(HgUtils.isIgnored(file, false)) {
            if (Mercurial.LOG.isLoggable(Level.FINER)) {
                Mercurial.LOG.log(Level.FINE, "skipping afterDelete(): File: {0} is ignored", new Object[] {file.getPath()}); // NOI18N
            }
            return;
        }
        reScheduleRefresh(800, file, true);
    }

    @Override
    public boolean beforeMove(VCSFileProxy from, VCSFileProxy to) {
        Mercurial.LOG.log(Level.FINE, "beforeMove {0}->{1}", new Object[]{from, to});
        if (from == null || to == null || to.exists()) {
            return true;
        }
        
        if (hg.isManaged(from)) {
            return hg.isManaged(to);
        }
        return super.beforeMove(from, to);
    }

    @Override
    public void doMove(final VCSFileProxy from, final VCSFileProxy to) throws IOException {
        Mercurial.LOG.log(Level.FINE, "doMove {0}->{1}", new Object[]{from, to});
        if (from == null || to == null || to.exists() && !equalPathsIgnoreCase(from, to)) {
            return;
        }
        hgMoveImplementation(from, to);
    }

    @Override
    public long refreshRecursively(VCSFileProxy dir, long lastTimeStamp, List<? super VCSFileProxy> children) {
        long retval = -1;
        if (HgUtils.HG_FOLDER_NAME.equals(dir.getName())) {
            Mercurial.STATUS_LOG.log(Level.FINER, "Interceptor.refreshRecursively: {0}", dir.getPath()); //NOI18N
            children.clear();
            retval = hgFolderEventsHandler.handleHgFolderEvent(dir);
        }
        return retval;
    }

    @Override
    public boolean isMutable(VCSFileProxy file) {
        return HgUtils.isPartOfMercurialMetadata(file) || super.isMutable(file);
    }

    private void hgMoveImplementation(final VCSFileProxy srcFile, final VCSFileProxy dstFile) throws IOException {
        final VCSFileProxy root = hg.getRepositoryRoot(srcFile);
        final VCSFileProxy dstRoot = hg.getRepositoryRoot(dstFile);

        Mercurial.LOG.log(Level.FINE, "hgMoveImplementation(): File: {0} {1}", new Object[] {srcFile, dstFile}); // NOI18N

        boolean result = VCSFileProxySupport.renameTo(srcFile, dstFile);
        if (!result && equalPathsIgnoreCase(srcFile, dstFile)) {
            Mercurial.LOG.log(Level.FINE, "hgMoveImplementation: magic workaround for filename case change {0} -> {1}", new Object[] { srcFile, dstFile }); //NOI18N
            VCSFileProxy temp = VCSFileProxySupport.generateTemporaryFile(dstFile.getParentFile(), srcFile.getName());
            Mercurial.LOG.log(Level.FINE, "hgMoveImplementation: magic workaround, step 1: {0} -> {1}", new Object[] { srcFile, temp }); //NOI18N
            VCSFileProxySupport.renameTo(srcFile, temp);
            Mercurial.LOG.log(Level.FINE, "hgMoveImplementation: magic workaround, step 2: {0} -> {1}", new Object[] { temp, dstFile }); //NOI18N
            result = VCSFileProxySupport.renameTo(temp, dstFile);
            Mercurial.LOG.log(Level.FINE, "hgMoveImplementation: magic workaround completed"); //NOI18N
        }
        if (!result) {
            Mercurial.LOG.log(Level.WARNING, "Cannot rename file {0} to {1}", new Object[] {srcFile, dstFile});
            IOException ex = new IOException();
            Exceptions.attachLocalizedMessage(ex, NbBundle.getMessage(MercurialInterceptor.class, "MSG_MoveFailed", new Object[] { srcFile, dstFile })); //NOI18N
            throw ex;
        }
        if (root == null) {
            return;
        }
        // no need to do rename after in a background thread, code requiring the bg thread (see #125673) no more exists
        OutputLogger logger = OutputLogger.getLogger(root);
        try {
            if (root.equals(dstRoot) && !HgUtils.isIgnored(dstFile, false)) {
                // target does not lie under ignored folder and is in the same repo as src
                HgCommand.doRenameAfter(root, srcFile, dstFile, logger);
            } else {
                // just hg rm the old file
                HgCommand.doRemove(root, srcFile, logger);
            }
        } catch (HgException e) {
            Mercurial.LOG.log(Level.FINE, "Mercurial failed to rename: File: {0} {1}", new Object[]{srcFile.getPath(), dstFile.getPath()}); // NOI18N
        } finally {
            logger.closeLog();
        }
    }

    /**
     * On Windows and Mac platforms files are the same if the paths equal with ignored case
     */
    private boolean equalPathsIgnoreCase(VCSFileProxy srcFile, VCSFileProxy dstFile) {
        return VCSFileProxySupport.isMac(srcFile) && srcFile.getPath().equalsIgnoreCase(dstFile.getPath());
    }

    @Override
    public void afterMove(final VCSFileProxy from, final VCSFileProxy to) {
        Mercurial.LOG.log(Level.FINE, "afterMove {0}->{1}", new Object[]{from, to});
        if (from == null || to == null || !to.exists()) {
            return;
        }

        if (from.equals(Mercurial.getInstance().getRepositoryRoot(from))
                || to.equals(Mercurial.getInstance().getRepositoryRoot(to))) {
            // whole repository was renamed/moved, need to refresh versioning roots
            refreshOwnersTask.schedule(0);
        }
        VCSFileProxy parent = from.getParentFile();
        // There is no point in refreshing the cache for ignored files.
        if (parent != null && !HgUtils.isIgnored(parent, false)) {
            reScheduleRefresh(800, from, true);
        }
        // target needs to refreshed, too
        parent = to.getParentFile();
        // There is no point in refreshing the cache for ignored files.
        if (parent != null && !HgUtils.isIgnored(parent, false)) {
            reScheduleRefresh(800, to, true);
        }
    }

    @Override
    public boolean beforeCopy (VCSFileProxy from, VCSFileProxy to) {
        Mercurial.LOG.log(Level.FINE, "beforeCopy {0}->{1}", new Object[]{from, to});
        if (from == null || to == null || to.exists()) {
            return true;
        }

        return hg.isManaged(from) && hg.isManaged(to);
    }

    @Override
    public void doCopy (final VCSFileProxy from, final VCSFileProxy to) throws IOException {
        Mercurial.LOG.log(Level.FINE, "doCopy {0}->{1}", new Object[]{from, to});
        if (from == null || to == null || to.exists()) {
            return;
        }

        VCSFileProxy root = hg.getRepositoryRoot(from);
        VCSFileProxy dstRoot = hg.getRepositoryRoot(to);

        if (from.isDirectory()) {
            VCSFileProxySupport.copyDirFiles(from, to, false);
        } else {
            VCSFileProxySupport.copyFile(from, to);
        }

        if (root == null || HgUtils.isIgnored(to, false)) {
            // target lies under ignored folder, do not add it
            return;
        }
        OutputLogger logger = OutputLogger.getLogger(root);
        try {
            if (root.equals(dstRoot)) {
                HgCommand.doCopy(root, from, to, true, logger);
            }
        } catch (HgException e) {
            Mercurial.LOG.log(Level.FINE, "Mercurial failed to copy: File: {0} {1}", new Object[] { from.getPath(), to.getPath() } ); // NOI18N
        } finally {
            logger.closeLog();
        }
    }

    @Override
    public void afterCopy (final VCSFileProxy from, final VCSFileProxy to) {
        Mercurial.LOG.log(Level.FINE, "afterCopy {0}->{1}", new Object[]{from, to});
        if (to == null) {
            return;
        }

        VCSFileProxy parent = to.getParentFile();
        // There is no point in refreshing the cache for ignored files.
        if (parent != null && !HgUtils.isIgnored(parent, false)) {
            reScheduleRefresh(800, to, true);
        }
    }
    
    @Override
    public boolean beforeCreate(final VCSFileProxy file, boolean isDirectory) {
        Mercurial.LOG.log(Level.FINE, "beforeCreate {0} {1}", new Object[]{file, isDirectory});
        if (HgUtils.isPartOfMercurialMetadata(file)) {
            return false;
        }
        if (!isDirectory && !file.exists()) {
            VCSFileProxy root = hg.getRepositoryRoot(file);
            FileInformation info = null;
            try {
                Map<VCSFileProxy, FileInformation> statusMap = HgCommand.getStatus(root, Arrays.asList(file), null, null);
                info = statusMap != null ? statusMap.get(file) : null;
            } catch (HgException ex) {
                Mercurial.LOG.log(Level.FINE, "beforeCreate(): getStatus failed for file: {0} {1}", new Object[]{file.getPath(), ex.toString()}); // NOI18N
            }
            if (info != null && info.getStatus() == FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
                Mercurial.LOG.log(Level.FINE, "beforeCreate(): LocallyDeleted: {0}", file); // NOI18N
                if (root == null) {
                    return false;
                }
                final OutputLogger logger = hg.getLogger(root.getPath());
                try {
                    List<VCSFileProxy> revertFiles = new ArrayList<>();
                    revertFiles.add(file);
                    HgCommand.doRevert(root, revertFiles, null, false, logger);
                } catch (HgException ex) {
                    Mercurial.LOG.log(Level.FINE, "beforeCreate(): File: {0} {1}", new Object[]{file.getPath(), ex.toString()}); // NOI18N
                }
                Mercurial.LOG.log(Level.FINE, "beforeCreate(): afterWaitFinished: {0}", file); // NOI18N
                logger.closeLog();
                VCSFileProxySupport.delete(file);
            }
        }
        return false;
    }

    @Override
    public void doCreate(VCSFileProxy file, boolean isDirectory) throws IOException {
        Mercurial.LOG.log(Level.FINE, "doCreate {0} {1}", new Object[]{file, isDirectory});
        super.doCreate(file, isDirectory);
    }

    @Override
    public void afterCreate(final VCSFileProxy file) {
        Mercurial.LOG.log(Level.FINE, "afterCreate {0}", file);
        if (HgUtils.isPartOfMercurialMetadata(file) && HgUtils.WLOCK_FILE.equals(file.getName())) {
            commandLogger.locked(file);
        }
        if (HgUtils.isAdministrative(file)) {
            // new metadata created, we should refresh owners
            refreshOwnersTask.schedule(0);
        }
        // There is no point in refreshing the cache for ignored files.
        if (!HgUtils.isIgnored(file, false)) {
            reScheduleRefresh(800, file, true);
        }
    }
    
    @Override
    public void afterChange(final VCSFileProxy file) {
        if (file.isDirectory()) {
            return;
        }
        Mercurial.LOG.log(Level.FINE, "afterChange(): {0}", file);      //NOI18N
        // There is no point in refreshing the cache for ignored files.
        if (!HgUtils.isIgnored(file, false)) {
            reScheduleRefresh(800, file, true);
        }
    }

    @Override
    public Object getAttribute(final VCSFileProxy file, String attrName) {
        if("ProvidedExtensions.RemoteLocation".equals(attrName)) { //NOI18N
            return getRemoteRepository(file);
        } else if("ProvidedExtensions.Refresh".equals(attrName)) { //NOI18N
            return new Runnable() {
                @Override
                public void run() {
                    FileStatusCache cache = hg.getFileStatusCache();
                    cache.refresh(file);
                }
            };
        } else if (SearchHistorySupport.PROVIDED_EXTENSIONS_SEARCH_HISTORY.equals(attrName)){
            return new HgSearchHistorySupport(file);
        } else if ("ProvidedExtensions.VCSIsModified".equals(attrName)) { //NOI18N
            VCSFileProxy repoRoot = Mercurial.getInstance().getRepositoryRoot(file);
            Boolean modified = null;
            if (repoRoot != null) {
                Set<VCSFileProxy> coll = Collections.singleton(file);
                cache.refreshAllRoots(Collections.<VCSFileProxy, Set<VCSFileProxy>>singletonMap(repoRoot, coll));
                modified = cache.containsFileOfStatus(coll, STATUS_VCS_MODIFIED_ATTRIBUTE, true);
            }
            return modified;
        } else {
            return super.getAttribute(file, attrName);
        }
    }

    private String getRemoteRepository(VCSFileProxy file) {
        return HgUtils.getRemoteRepository(file);
    }

    private void reScheduleRefresh(int delayMillis, VCSFileProxy fileToRefresh, boolean log) {
        // refresh all at once
        Mercurial.STATUS_LOG.log(Level.FINE, "reScheduleRefresh: adding {0}", fileToRefresh.getPath());
        if (!HgUtils.isPartOfMercurialMetadata(fileToRefresh)) {
            filesToRefresh.add(fileToRefresh);
            if (log) {
                commandLogger.logModification(fileToRefresh);
            }
        }
        refreshTask.schedule(delayMillis);
    }

    private void reScheduleRefresh (int delayMillis, Set<VCSFileProxy> filesToRefresh) {
        // refresh all at once
        Mercurial.STATUS_LOG.log(Level.FINE, "reScheduleRefresh: adding {0}", filesToRefresh);
        this.filesToRefresh.addAll(filesToRefresh);
        refreshTask.schedule(delayMillis);
    }

    /**
     * Checks if administrative folder for a repository with the file is registered.
     * @param file
     */
    void pingRepositoryRootFor(final VCSFileProxy file) {
        if (!AUTOMATIC_REFRESH_ENABLED) {
            return;
        }
        hgFolderEventsHandler.initializeFor(file);
    }

    /**
     * Runs a given callable and disable listening for external repository events for the time the callable is running.
     * Refreshes cached modification timestamp of hg administrative folder file for the given repository after.
     * @param callable code to run
     * @param repository
     * @param commandName name of the hg command if available
     */
    <T> T runWithoutExternalEvents(VCSFileProxy repository, String commandName, Callable<T> callable) throws Exception {
        assert repository != null;
        try {
            if (repository != null) {
                hgFolderEventsHandler.enableEvents(repository, false);
                commandLogger.lockedInternally(repository, commandName);
            }
            return callable.call();
        } finally {
            if (repository != null) {
                commandLogger.unlockedInternally(repository);
                hgFolderEventsHandler.refreshRepositoryTimestamps(repository);
                hgFolderEventsHandler.enableEvents(repository, true);
            }
        }
    }

    /**
     * Returns a set of known repository roots (those visible or open in IDE)
     * @param repositoryRoot
     * @return
     */
    Set<VCSFileProxy> getSeenRoots (VCSFileProxy repositoryRoot) {
        return hgFolderEventsHandler.getSeenRoots(repositoryRoot);
    }

    private class CommandUsageLogger {
        
        private final Map<VCSFileProxy, Events> events = new HashMap<>();
        
        private void locked (VCSFileProxy file) {
            VCSFileProxy hgFolder = getHgFolderFor(file);
            // it is a lock file, lock file still exists=repository is locked
            if (hgFolder != null && HgUtils.isRepositoryLocked(hgFolder.getParentFile())) {
                long time = System.currentTimeMillis();
                synchronized (events) {
                    Events ev = events.get(hgFolder);
                    if (ev == null || ev.timeFinished > 0 && ev.timeFinished < time - 10000) {
                        // is new lock or is an old unfinished stale event
                        // and is not part of any internal command that could leave
                        // pending events to be delivered with 10s delay
                        ev = new Events();
                        ev.timeStarted = time;
                        events.put(hgFolder, ev);
                        scheduleUnlock(hgFolder.getParentFile());
                    }
                }
            }
        }
        
        /**
         * Command run internally from the IDE
         */
        private void lockedInternally (VCSFileProxy repository, String commandName) {
            VCSFileProxy hgFolder = HgUtils.getHgFolderForRoot(repository);
            Events ev = new Events();
            ev.timeStarted = System.currentTimeMillis();
            ev.commandName = commandName;
            synchronized (events) {
                events.put(hgFolder, ev);
            }
        }

        private void unlocked (VCSFileProxy file) {
            VCSFileProxy hgFolder = getHgFolderFor(file);
            if (hgFolder != null) {
                Events ev;
                synchronized (events) {
                    ev = events.remove(hgFolder);
                    if (ev != null && !ev.isExternal()) {
                        // this does not log internal commands
                        events.put(hgFolder, ev);
                        return;
                    }
                }
                if (ev != null) {
                    long time = System.currentTimeMillis() - ev.timeStarted;
                    Utils.logVCSCommandUsageEvent("HG", time, ev.modifications, ev.commandName, ev.isExternal()); //NOI18N
                }
            }
        }
        
        /**
         * Internal command finish
         */
        private void unlockedInternally (VCSFileProxy repository) {
            VCSFileProxy hgFolder = HgUtils.getHgFolderForRoot(repository);
            Events ev;
            synchronized (events) {
                ev = events.get(hgFolder);
                if (ev == null) {
                    return;
                } else if (ev.isExternal()) {
                    events.remove(hgFolder);
                }
            }
            ev.timeFinished = System.currentTimeMillis();
            long time = ev.timeFinished - ev.timeStarted;
            Utils.logVCSCommandUsageEvent("HG", time, ev.modifications, ev.commandName, ev.isExternal()); //NOI18N
        }

        /**
         * 
         * @param wlockFile
         * @return parent hg folder for wlock file or null if the file is not 
         * a write lock repository file
         */
        private VCSFileProxy getHgFolderFor (VCSFileProxy wlockFile) {
            VCSFileProxy repository = Mercurial.getInstance().getRepositoryRoot(wlockFile);
            VCSFileProxy hgFolder = HgUtils.getHgFolderForRoot(repository);
            return hgFolder.equals(wlockFile.getParentFile())
                    ? hgFolder
                    : null;
        }

        private void logModification (final VCSFileProxy file) {
            if (HgUtils.isPartOfMercurialMetadata(file)) {
                return;
            }
            final VCSFileProxy repository = Mercurial.getInstance().getRepositoryRoot(file);
            if (repository == null) {
                return;
            }
            final VCSFileProxy hgFolder = HgUtils.getHgFolderForRoot(repository);
            if (hgFolder != null) {
                long time = System.currentTimeMillis();
                synchronized (events) {
                    Events ev = events.get(hgFolder);
                    if ((ev == null || ev.timeFinished > 0 && ev.timeFinished < time - 10000)
                            && HgUtils.isRepositoryLocked(repository)) {
                        // is new lock or is an old unfinished stale event
                        // and is not part of any internal command that could leave
                        // pending events to be delivered with 10s delay
                        ev = new Events();
                        ev.timeStarted = time;
                        events.put(hgFolder, ev);
                        scheduleUnlock(repository);
                    }
                    if (ev != null) {
                        ++ev.modifications;
                    }
                }
            }
        }

        private void scheduleUnlock (final VCSFileProxy repository) {
            Mercurial.getInstance().getParallelRequestProcessor().post(new Runnable() {
                @Override
                public void run () {
                    long timeout = 1000;
                    while (HgUtils.isRepositoryLocked(repository)) {
                        long t = System.currentTimeMillis();
                        synchronized (commandLogger) {
                            try {
                                commandLogger.wait(timeout);
                            } catch (InterruptedException ex) {
                            }
                        }
                        if (timeout <= System.currentTimeMillis() - t) {
                            // timeouted
                            timeout = Math.min(30000, timeout << 1);
                        }
                    }
                    unlocked(VCSFileProxy.createFileProxy(HgUtils.getHgFolderForRoot(repository), HgUtils.WLOCK_FILE));
                }
            });
        }

    }

    private static class Events {
        long timeStarted;
        long timeFinished;
        long modifications;
        String commandName;

        private boolean isExternal () {
            return commandName == null;
        }
    }

    private class RefreshTask implements Runnable {
        @Override
        public void run() {
            Thread.interrupted();
            if (DelayScanRegistry.getInstance().isDelayed(refreshTask, Mercurial.STATUS_LOG, "MercurialInterceptor.refreshTask")) { //NOI18N
                return;
            }
            // fill a fileset with all the modified files
            Collection<VCSFileProxy> files = new HashSet<>(filesToRefresh.size());
            VCSFileProxy file;
            while ((file = filesToRefresh.poll()) != null) {
                files.add(file);
            }
            if (!"false".equals(System.getProperty("versioning.mercurial.delayStatusForLockedRepositories"))) {
                files = checkLockedRepositories(files, false);
            }
            if (!files.isEmpty()) {
                cache.refreshAllRoots(files);
            }
            if (!lockedRepositories.isEmpty()) {
                lockedRepositoryRefreshTask.schedule(5000);
            }
        }
    }

    private Collection<VCSFileProxy> checkLockedRepositories (Collection<VCSFileProxy> additionalFilesToRefresh, boolean keepCached) {
        List<VCSFileProxy> retval = new LinkedList<>();
        // at first sort the files under repositories
        Map<VCSFileProxy, Set<VCSFileProxy>> sortedFiles = sortByRepository(additionalFilesToRefresh);
        for (Map.Entry<VCSFileProxy, Set<VCSFileProxy>> e : sortedFiles.entrySet()) {
            Set<VCSFileProxy> alreadyPlanned = lockedRepositories.get(e.getKey());
            if (alreadyPlanned == null) {
                alreadyPlanned = new HashSet<>();
                lockedRepositories.put(e.getKey(), alreadyPlanned);
            }
            alreadyPlanned.addAll(e.getValue());
        }
        // return all files that do not belong to a locked repository 
        for (Iterator<Map.Entry<VCSFileProxy, Set<VCSFileProxy>>> it = lockedRepositories.entrySet().iterator(); it.hasNext();) {
            Map.Entry<VCSFileProxy, Set<VCSFileProxy>> entry = it.next();
            VCSFileProxy repository = entry.getKey();
            boolean unlocked = true;
            if (!repository.exists()) {
                // repository does not exist, no need to keep it
                it.remove();
            } else if (HgUtils.isRepositoryLocked(repository)) {
                unlocked = false;
                Mercurial.STATUS_LOG.log(Level.FINE, "checkLockedRepositories(): Repository {0} locked, status refresh delayed", repository); //NOI18N
            } else {
                // repo not locked, add all files into the returned collection
                retval.addAll(entry.getValue());
                if (!keepCached) {
                    it.remove();
                }
            }
            if (unlocked) {
                synchronized (commandLogger) {
                    commandLogger.notifyAll();
                }
            }
        }
        return retval;
    }

    private Map<VCSFileProxy, Set<VCSFileProxy>> sortByRepository (Collection<VCSFileProxy> files) {
        Map<VCSFileProxy, Set<VCSFileProxy>> sorted = new HashMap<>(5);
        for (VCSFileProxy f : files) {
            VCSFileProxy repository = hg.getRepositoryRoot(f);
            if (repository != null) {
                Set<VCSFileProxy> repoFiles = sorted.get(repository);
                if (repoFiles == null) {
                    repoFiles = new HashSet<>();
                    sorted.put(repository, repoFiles);
                }
                repoFiles.add(f);
            }
        }
        return sorted;
    }

    private class LockedRepositoryRefreshTask implements Runnable {
        @Override
        public void run() {
            if (!checkLockedRepositories(Collections.<VCSFileProxy>emptySet(), true).isEmpty()) {
                // there are some newly unlocked repositories to refresh
                refreshTask.schedule(0);
            } else if (!lockedRepositories.isEmpty()) {
                lockedRepositoryRefreshTask.schedule(5000);
            }
        }
    }

    
    private static class HgFolderTimestamps {
        private final VCSFileProxy hgFolder;
        private final Map<String, Long> interestingTimestamps;
        // dirstate might change its ts even after a simple hg status call, so listen only on its size
        private final long dirstateSize;
        private final VCSFileProxy dirstateFile;
        private static final String DIRSTATE = "dirstate"; //NOI18N
        private static final String[] INTERESTING_FILENAMES = { 
            "branch", //NOI18N
            "branchheads.cache", //NOI18N
            "localtags", //NOI18N
            "tags.cache", //NOI18N
            "undo.branch", //NOI18N
            "undo.dirstate" //NOI18N
        };

        public HgFolderTimestamps (VCSFileProxy hgFolder) {
            this.hgFolder = hgFolder;
            Map<String, Long> ts = new HashMap<>(INTERESTING_FILENAMES.length);
            for (String fn : INTERESTING_FILENAMES) {
                ts.put(fn, VCSFileProxy.createFileProxy(hgFolder, fn).lastModified());
            }
            dirstateFile = VCSFileProxy.createFileProxy(hgFolder, DIRSTATE);
            dirstateSize = VCSFileProxySupport.length(dirstateFile);
            interestingTimestamps = Collections.unmodifiableMap(ts);
        }

        private boolean isNewer (HgFolderTimestamps other) {
            boolean newer = true;
            if (other != null) {
                newer = dirstateSize != other.dirstateSize;
                for (Map.Entry<String, Long> e : interestingTimestamps.entrySet()) {
                    // has a newer (higher) ts or the file is deleted
                    if (e.getValue() > other.interestingTimestamps.get(e.getKey()) ||
                        e.getValue() == 0 && other.interestingTimestamps.get(e.getKey()).longValue() != e.getValue().longValue()) {
                        newer = true;
                        break;
                    }
                }
            }
            return newer;
        }

        private VCSFileProxy getHgFolder () {
            return hgFolder;
        }

        private boolean repositoryExists () {
            return dirstateSize > 0 || hgFolder.exists();
        }

        private boolean isOutdated () {
            boolean upToDate = dirstateSize == VCSFileProxySupport.length(dirstateFile);
            if (upToDate) {
                for (Map.Entry<String, Long> e : interestingTimestamps.entrySet()) {
                    VCSFileProxy f = VCSFileProxy.createFileProxy(hgFolder, e.getKey());
                    long ts = f.lastModified();
                    // file is now either modified (higher ts) or deleted
                    if (e.getValue() < ts || e.getValue() > ts && ts == 0) {
                        upToDate = false;
                        break;
                    }
                }
            }
            return !upToDate;
        }
    }

    private class HgFolderEventsHandler {
        private final HashMap<VCSFileProxy, HgFolderTimestamps> hgFolders = new HashMap<>(5);
        private final HashMap<VCSFileProxy, FileChangeListener> hgFolderRLs = new HashMap<>(5);
        private final HashMap<VCSFileProxy, Set<VCSFileProxy>> seenRoots = new HashMap<>(5);
        private final HashSet<VCSFileProxy> disabledEvents = new HashSet<>(5);

        private final HashSet<VCSFileProxy> filesToInitialize = new HashSet<>();
        private final RequestProcessor rp = new RequestProcessor("MercurialInterceptorEventsHandlerRP", 1); //NOI18N
        private final RequestProcessor.Task initializingTask = rp.create(new Runnable() {
            @Override
            public void run() {
                initializeFiles();
            }
        });
        private final RequestProcessor.Task refreshOpenFilesTask = rp.create(new Runnable() {
            @Override
            public void run() {
                Set<VCSFileProxy> openFiles = VCSFileProxySupport.getOpenFiles();
                for (VCSFileProxy file : openFiles) {
                    hg.notifyFileChanged(file);
                }
            }
        });
        private final Set<VCSFileProxy> historyChandegRepositories = new HashSet<>(5);
        private final RequestProcessor.Task refreshHistoryTabTask = rp.create(new Runnable() {
            @Override
            public void run() {
                List<VCSFileProxy> toRefresh;
                synchronized (historyChandegRepositories) {
                    toRefresh = new ArrayList<>(historyChandegRepositories);
                    historyChandegRepositories.clear();
                }
                if (!toRefresh.isEmpty()) {
                    for (VCSFileProxy repo : toRefresh) {
                        Mercurial.getInstance().historyChanged(repo);
                    }
                }
            }
        });

        private HgFolderTimestamps scanHgFolderTimestamps (VCSFileProxy hgFolder) {
            return new HgFolderTimestamps(hgFolder);
        }

        public void refreshRepositoryTimestamps (VCSFileProxy repository) {
            refreshHgFolderTimestamp(scanHgFolderTimestamps(HgUtils.getHgFolderForRoot(repository)));
        }

        /**
         *
         * @param hgFolder
         * @param timestamp new timestamp, value 0 will remove the item from the cache
         */
        private void refreshHgFolderTimestamp (HgFolderTimestamps newTimestamps) {
            final VCSFileProxy hgFolder = newTimestamps.getHgFolder();
            boolean exists = newTimestamps.repositoryExists();
            synchronized (hgFolders) {
                if (exists && !newTimestamps.isNewer(hgFolders.get(hgFolder))) {
                    // do not enter the filesystem module unless really need to
                    return;
                }
            }
            synchronized (hgFolders) {
                hgFolders.remove(hgFolder);
                FileChangeListener list = hgFolderRLs.remove(hgFolder);
                if (exists) {
                    hgFolders.put(hgFolder, newTimestamps);
                    if (list == null) {
                        final FileChangeListener fList = list = new FileChangeAdapter();
                        // has to run in a different thread, otherwise we may get a deadlock
                        rp.post(new Runnable () {
                            @Override
                            public void run() {
                                final FileObject fo = hgFolder.toFileObject();
                                if (fo != null) {
                                    fo.addRecursiveListener(fList);
                                } else {
                                    //TODO:
                                    //FileUtil.addRecursiveListener(fList, hgFolder);
                                }
                            }
                        });
                    }
                    hgFolderRLs.put(hgFolder, list);
                } else {
                    if (list != null) {
                        final FileChangeListener fList = list;
                        // has to run in a different thread, otherwise we may get a deadlock
                        rp.post(new Runnable () {
                            @Override
                            public void run() {
                                final FileObject fo = hgFolder.toFileObject();
                                if (fo != null) {
                                    fo.removeRecursiveListener(fList);;
                                } else {
                                    //TODO:
                                    //FileUtil.removeRecursiveListener(fList, hgFolder);
                                }
                            }
                        });
                    }
                    Mercurial.STATUS_LOG.log(Level.FINE, "refreshHgFolderTimestamp: {0} no longer exists", hgFolder.getPath()); //NOI18N
                }
            }
        }

        private long handleHgFolderEvent(VCSFileProxy hgFolder) {
            long lastModified = 0;
            if (AUTOMATIC_REFRESH_ENABLED && !"false".equals(System.getProperty("mercurial.handleDirstateEvents", "true"))) { //NOI18N
                hgFolder = hgFolder.normalizeFile();
                Mercurial.STATUS_LOG.log(Level.FINER, "handleHgFolderEvent: special FS event handling for {0}", hgFolder.getPath()); //NOI18N
                boolean refreshNeeded = false;
                HgFolderTimestamps cached;
                if (isEnabled(hgFolder)) {
                    commandLogger.locked(VCSFileProxy.createFileProxy(hgFolder, HgUtils.WLOCK_FILE));
                    synchronized (hgFolders) {
                        cached = hgFolders.get(hgFolder);
                    }
                    if (cached == null || !cached.repositoryExists() || cached.isOutdated()) {
                        refreshHgFolderTimestamp(scanHgFolderTimestamps(hgFolder));
                        refreshNeeded = true;
                    }
                    if (refreshNeeded) {
                        VCSFileProxy repository = hgFolder.getParentFile();
                        Mercurial.STATUS_LOG.log(Level.FINE, "handleDirstateEvent: planning repository scan for {0}", repository.getPath()); //NOI18N
                        reScheduleRefresh(3000, getSeenRoots(repository)); // scan repository root
                        refreshOpenFilesTask.schedule(3000);
                        WorkingCopyInfo.refreshAsync(repository);
                        // make history tab up to date
                        refreshHistoryTab(repository);
                    }
                }
            }
            return lastModified;
        }

        public void initializeFor (VCSFileProxy file) {
            if (addFileToInitialize(file)) {
                initializingTask.schedule(500);
            }
        }

        private Set<VCSFileProxy> getSeenRoots (VCSFileProxy repositoryRoot) {
            Set<VCSFileProxy> retval = new HashSet<>();
            Set<VCSFileProxy> seenRootsForRepository = getSeenRootsForRepository(repositoryRoot);
            synchronized (seenRootsForRepository) {
                 retval.addAll(seenRootsForRepository);
            }
            return retval;
        }

        private VCSFileProxy addSeenRoot (VCSFileProxy repositoryRoot, VCSFileProxy rootToAdd) {
            VCSFileProxy addedRoot = null;
            Set<VCSFileProxy> seenRootsForRepository = getSeenRootsForRepository(repositoryRoot);
            synchronized (seenRootsForRepository) {
                if (!seenRootsForRepository.contains(repositoryRoot)) {
                    // try to add the file only when the repository root is not yet registered
                    rootToAdd = rootToAdd.normalizeFile();
                    addedRoot = HgUtils.prepareRootFiles(repositoryRoot, seenRootsForRepository, rootToAdd);
                }
            }
            return addedRoot;
        }

        private Set<VCSFileProxy> getSeenRootsForRepository (VCSFileProxy repositoryRoot) {
            synchronized (seenRoots) {
                 Set<VCSFileProxy> seenRootsForRepository = seenRoots.get(repositoryRoot);
                 if (seenRootsForRepository == null) {
                     seenRoots.put(repositoryRoot, seenRootsForRepository = new HashSet<>());
                 }
                 return seenRootsForRepository;
            }
        }

        private boolean addFileToInitialize(VCSFileProxy file) {
            synchronized (filesToInitialize) {
                return filesToInitialize.add(file);
            }
        }

        private VCSFileProxy getFileToInitialize () {
            VCSFileProxy nextFile = null;
            synchronized (filesToInitialize) {
                Iterator<VCSFileProxy> iterator = filesToInitialize.iterator();
                if (iterator.hasNext()) {
                    nextFile = iterator.next();
                    iterator.remove();
                }
            }
            return nextFile;
        }
        
        private void initializeFiles () {
            VCSFileProxy file = null;
            while ((file = getFileToInitialize()) != null) {
                // select repository root for the file and finds it's .hg folder
                VCSFileProxy repositoryRoot = hg.getRepositoryRoot(file);
                if (repositoryRoot != null) {
                    VCSFileProxy newlyAddedRoot = addSeenRoot(repositoryRoot, file);
                    if (newlyAddedRoot != null) {
                        // this means the repository has not yet been scanned, so scan it
                        Mercurial.STATUS_LOG.log(Level.FINE, "pingRepositoryRootFor: planning a scan for {0} - {1}", new Object[]{repositoryRoot.getPath(), file.getPath()}); //NOI18N
                        reScheduleRefresh(4000, newlyAddedRoot, false);
                        VCSFileProxy hgFolder = HgUtils.getHgFolderForRoot(repositoryRoot).normalizeFile();
                        boolean refreshNeeded = false;
                        synchronized (hgFolders) {
                            if (!hgFolders.containsKey(hgFolder)) {
                                if (hgFolder.isDirectory()) {
                                    // however there might be NO .hg folder, especially for just initialized repositories
                                    // so keep the reference only for existing and valid .hg folders
                                    hgFolders.put(hgFolder, null);
                                    refreshNeeded = true;
                                }
                            }
                        }
                        if (refreshNeeded) {
                            refreshHgFolderTimestamp(scanHgFolderTimestamps(hgFolder));
                        }
                    }
                }
            }
        }

        private void enableEvents (VCSFileProxy repository, boolean enabled) {
            VCSFileProxy hgFolder = HgUtils.getHgFolderForRoot(repository).normalizeFile();
            synchronized (disabledEvents) {
                if (enabled) {
                    disabledEvents.remove(hgFolder);
                } else {
                    disabledEvents.add(hgFolder);
                }
            }
        }

        private boolean isEnabled (VCSFileProxy hgFolder) {
            synchronized (disabledEvents) {
                return !disabledEvents.contains(hgFolder);
            }
        }

        private void refreshHistoryTab (VCSFileProxy repository) {
            boolean refresh;
            synchronized (historyChandegRepositories) {
                refresh = historyChandegRepositories.add(repository);
            }
            if (refresh) {
                refreshHistoryTabTask.schedule(3000);
            }
        }
    }
}
