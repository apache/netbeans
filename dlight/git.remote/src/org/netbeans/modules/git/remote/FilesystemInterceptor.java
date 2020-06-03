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

package org.netbeans.modules.git.remote;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.cli.GitRemoteConfig;
import org.netbeans.modules.git.remote.cli.GitURI;
import org.netbeans.modules.git.remote.cli.progress.ProgressMonitor;
import org.netbeans.modules.git.remote.FileInformation.Status;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.ui.history.SearchHistoryAction;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.util.DelayScanRegistry;
import org.netbeans.modules.remotefs.versioning.api.SearchHistorySupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.modules.OnStop;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 */
class FilesystemInterceptor extends VCSInterceptor {

    private final FileStatusCache   cache;

    private final Set<VCSFileProxy> filesToRefresh = new HashSet<>();
    private final Map<VCSFileProxy, Set<VCSFileProxy>> lockedRepositories = new HashMap<>(5);

    private final RequestProcessor.Task refreshTask, lockedRepositoryRefreshTask;
    private final RequestProcessor.Task refreshOwnersTask;

    private static final RequestProcessor rp = new RequestProcessor("GitRemoteRefresh", 1, true);
    private final GitFolderEventsHandler gitFolderEventsHandler;
    private final CommandUsageLogger commandLogger;
    // not final due to tests
    private static final boolean AUTOMATIC_REFRESH_ENABLED = !"true".equals(System.getProperty("versioning.git.autoRefreshDisabled", "false")); //NOI18N
    private static final String INDEX_FILE_NAME = "index"; //NOI18N
    private static final String HEAD_FILE_NAME = "HEAD"; //NOI18N
    private static final String REFS_FILE_NAME = "refs"; //NOI18N
    private static final Logger LOG = Logger.getLogger(FilesystemInterceptor.class.getName());
    private static final EnumSet<Status> STATUS_VCS_MODIFIED_ATTRIBUTE = EnumSet.of(
            Status.NEW_HEAD_WORKING_TREE,
            Status.IN_CONFLICT,
            Status.MODIFIED_HEAD_INDEX,
            Status.MODIFIED_HEAD_WORKING_TREE,
            Status.MODIFIED_INDEX_WORKING_TREE
    );

    public FilesystemInterceptor () {
        cache = Git.getInstance().getFileStatusCache();
        refreshTask = rp.create(new RefreshTask(), true);
        lockedRepositoryRefreshTask = rp.create(new LockedRepositoryRefreshTask());
        gitFolderEventsHandler = new GitFolderEventsHandler();
        commandLogger = new CommandUsageLogger();
        refreshOwnersTask = rp.create(new Runnable() {
            @Override
            public void run() {
                Git git = Git.getInstance();
                git.clearAncestorCaches();
                git.versionedFilesChanged();
                VersioningSupport.versionedRootsChanged();
            }
        });
    }

    @Override
    public long refreshRecursively (VCSFileProxy dir, long lastTimeStamp, List<? super VCSFileProxy> children) {
        long retval = -1;
        if (GitUtils.DOT_GIT.equals(dir.getName()) || gitFolderEventsHandler.isMetadataFolder(dir)) {
            if (Git.STATUS_LOG.isLoggable(Level.FINER)) {
                Git.STATUS_LOG.log(Level.FINER, "Interceptor.refreshRecursively: {0}", dir.getPath()); //NOI18N
            }
            children.clear();
            retval = gitFolderEventsHandler.refreshAdminFolder(dir);
            final VCSFileProxy[] listFiles = dir.listFiles();
            if (listFiles != null) {
                for(VCSFileProxy ch : listFiles) {
                    if (REFS_FILE_NAME.equals(ch.getName())) {
                        children.add(ch);
                    }
                }
            }
        } else if (GitUtils.isPartOfGitMetadata(dir)) {
            // the condition above is to limit number of following code invocations
            // changes done in metadata not present under .git folder are not recognized - there's still the manual refresh
            VCSFileProxy metadataFolder = gitFolderEventsHandler.getMetadataForReferences(dir);
            if (metadataFolder != null) {
                gitFolderEventsHandler.refreshReferences(metadataFolder, dir);
            }
        }
        return retval;
    }

    @Override
    public boolean beforeCreate (final VCSFileProxy file, boolean isDirectory) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "beforeCreate {0} - {1}", new Object[] { file, isDirectory }); //NOI18N
        }
        if (GitUtils.isPartOfGitMetadata(file)) {
            return false;
        }
        if (!isDirectory && !file.exists()) {
            Git git = Git.getInstance();
            final VCSFileProxy root = git.getRepositoryRoot(file);
            if (root == null) {
                return false;
            }
            GitClient client = null;
            try {
                client = git.getClient(root);
                client.reset(new VCSFileProxy[] { file }, GitUtils.HEAD, true, GitUtils.NULL_PROGRESS_MONITOR);
            } catch (GitException.MissingObjectException ex) {
                if (!GitUtils.HEAD.equals(ex.getObjectName())) {
                    // log only if we already have a commit. Just initialized repository does not allow us to reset
                    LOG.log(Level.INFO, "beforeCreate(): File: {0} {1}", new Object[] { file.getPath(), ex.toString()}); //NOI18N
                }
            } catch (GitException ex) {
                LOG.log(Level.INFO, "beforeCreate(): File: {0} {1}", new Object[] { file.getPath(), ex.toString()}); //NOI18N
            } finally {
                if (client != null) {
                    client.release();
                }
            }
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "beforeCreate(): finished: {0}", file); // NOI18N
            }
        }
        return false;
    }

    @Override
    public void afterCreate (final VCSFileProxy file) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "afterCreate {0}", file); //NOI18N
        }
        if (GitUtils.isPartOfGitMetadata(file) && GitUtils.INDEX_LOCK.equals(file.getName())) {
            commandLogger.locked(file);
        }
        if (GitUtils.isAdministrative(file)) {
            // new metadata created, we should refresh owners
            refreshOwnersTask.schedule(0);
        }
        // There is no point in refreshing the cache for ignored files.
        addToCreated(file);
        if (!cache.getStatus(file).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            reScheduleRefresh(800, Collections.singleton(file), true);
        }
    }

    @Override
    public boolean beforeDelete (VCSFileProxy file) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "beforeDelete {0}", file); //NOI18N
        }
        if (file == null) {
            return false;
        }
        if (GitUtils.isPartOfGitMetadata(file)) {
            return false;
        }

        // do not handle delete for ignored files
        return !cache.getStatus(file).containsStatus(Status.NOTVERSIONED_EXCLUDED);
    }

    @Override
    public void doDelete (VCSFileProxy file) throws IOException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "doDelete {0}", file); //NOI18N
        }
        if (file == null) {
            return;
        }
        Git git = Git.getInstance();
        VCSFileProxy root = git.getRepositoryRoot(file);
        GitClient client = null;
        try {
            if (GitUtils.getGitFolderForRoot(root).exists()) {
                client = git.getClient(root);
                client.remove(new VCSFileProxy[] { file }, false, GitUtils.NULL_PROGRESS_MONITOR);
            } else if (file.exists()) {
                VCSFileProxySupport.delete(file);
                if (file.exists()) {
                    IOException ex = new IOException();
                    Exceptions.attachLocalizedMessage(ex, NbBundle.getMessage(FilesystemInterceptor.class, "MSG_DeleteFailed", new Object[] { file, "" })); //NOI18N
                    throw ex;
                }
            }
            if (file.equals(root)) {
                // the whole repository was deleted -> release references to the repository folder
                gitFolderEventsHandler.refreshIndexFileTimestamp(root);
            }
        } catch (GitException e) {
            IOException ex = new IOException();
            Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(FilesystemInterceptor.class, "MSG_DeleteFailed", new Object[] { file, e.getLocalizedMessage() })); //NOI18N
            ex.initCause(e);
            throw ex;
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }

    @Override
    public void afterDelete(final VCSFileProxy file) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "afterDelete {0}", file); //NOI18N
        }
        if (file == null) {
            return;
        }
        if (GitUtils.isPartOfGitMetadata(file) && GitUtils.INDEX_LOCK.equals(file.getName())) {
            commandLogger.unlocked(file);
        }
        if (GitUtils.DOT_GIT.equals(file.getName())) {
            // new metadata created, we should refresh owners
            refreshOwnersTask.schedule(3000);
        }
        // we don't care about ignored files
        if (!cache.getStatus(file).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            reScheduleRefresh(800, Collections.singleton(file), true);
        }
    }

    @Override
    public boolean beforeMove(VCSFileProxy from, VCSFileProxy to) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "beforeMove {0} -> {1}", new Object[] { from, to }); //NOI18N
        }
        if (from == null || to == null || to.exists()) {
            return true;
        }
        Git hg = Git.getInstance();
        return hg.isManaged(from) && hg.isManaged(to);
    }

    @Override
    public void doMove(final VCSFileProxy from, final VCSFileProxy to) throws IOException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "doMove {0} -> {1}", new Object[] { from, to }); //NOI18N
        }
        if (from == null || to == null || to.exists() && !equalPathsIgnoreCase(from, to)) {
            return;
        }

        Git git = Git.getInstance();
        VCSFileProxy root = git.getRepositoryRoot(from);
        VCSFileProxy dstRoot = git.getRepositoryRoot(to);
        GitClient client = null;
        try {
            if (root != null && root.equals(dstRoot) && !cache.getStatus(to).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
                // target does not lie under ignored folder and is in the same repo as src
                client = git.getClient(root);
                if (equalPathsIgnoreCase(from, to)) {
                    // must do rename --after because the files/paths equal on Win or Mac
                    if (!VCSFileProxySupport.renameTo(from, to)) {
                        throw new IOException(NbBundle.getMessage(FilesystemInterceptor.class, "MSG_MoveFailed", new Object[] { from, to, "" })); //NOI18N
                    }
                    client.rename(from, to, true, GitUtils.NULL_PROGRESS_MONITOR);
                } else {
                    client.rename(from, to, false, GitUtils.NULL_PROGRESS_MONITOR);
                }
            } else {
                boolean result = VCSFileProxySupport.renameTo(from, to);
                if (!result) {
                    throw new IOException(NbBundle.getMessage(FilesystemInterceptor.class, "MSG_MoveFailed", new Object[] { from, to, "" })); //NOI18N
                }
                if (root != null) {
                    client = git.getClient(root);
                    client.remove(new VCSFileProxy[] { from }, true, GitUtils.NULL_PROGRESS_MONITOR);
                }
            }
        } catch (GitException e) {
            IOException ex = new IOException();
            Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(FilesystemInterceptor.class, "MSG_MoveFailed", new Object[] { from, to, e.getLocalizedMessage() })); //NOI18N
            ex.initCause(e);
            throw ex;
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }

    private boolean equalPathsIgnoreCase (final VCSFileProxy from, final VCSFileProxy to) {
        return Utilities.isWindows() && from.equals(to) || Utilities.isMac() && from.getPath().equalsIgnoreCase(to.getPath());
    }

    @Override
    public void afterMove(final VCSFileProxy from, final VCSFileProxy to) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "afterMove {0} -> {1}", new Object[] { from, to }); //NOI18N
        }
        if (from == null || to == null || !to.exists()) {
            return;
        }

        if (from.equals(Git.getInstance().getRepositoryRoot(from))
                || to.equals(Git.getInstance().getRepositoryRoot(to))) {
            // whole repository was renamed/moved, need to refresh versioning roots
            refreshOwnersTask.schedule(0);
        }
        // There is no point in refreshing the cache for ignored files.
        if (!cache.getStatus(from).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            reScheduleRefresh(800, Collections.singleton(from), true);
        }
        addToCreated(to);
        // There is no point in refreshing the cache for ignored files.
        if (!cache.getStatus(to).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            reScheduleRefresh(800, Collections.singleton(to), true);
        }
    }

    @Override
    public boolean beforeCopy (VCSFileProxy from, VCSFileProxy to) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "beforeCopy {0}->{1}", new Object[] { from, to }); //NOI18N
        }
        if (from == null || to == null || to.exists()) {
            return true;
        }
        Git git = Git.getInstance();
        return git.isManaged(from) && git.isManaged(to);
    }

    @Override
    public void doCopy (final VCSFileProxy from, final VCSFileProxy to) throws IOException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "doCopy {0}->{1}", new Object[] { from, to }); //NOI18N
        }
        if (from == null || to == null || to.exists()) {
            return;
        }

        Git git = Git.getInstance();
        VCSFileProxy root = git.getRepositoryRoot(from);
        VCSFileProxy dstRoot = git.getRepositoryRoot(to);

        if (from.isDirectory()) {
            VCSFileProxySupport.copyDirFiles(from, to, true);
        } else {
            VCSFileProxySupport.copyFile(from, to);
        }

        if (root == null || cache.getStatus(to).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            // target lies under ignored folder, do not add it
            return;
        }
        GitClient client = null;
        try {
            if (root.equals(dstRoot)) {
                client = git.getClient(root);
                client.copyAfter(from, to, GitUtils.NULL_PROGRESS_MONITOR);
            }
        } catch (GitException e) {
            IOException ex = new IOException();
            Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(FilesystemInterceptor.class, "MSG_CopyFailed", new Object[] { from, to, e.getLocalizedMessage() })); //NOI18N
            ex.initCause(e);
            throw ex;
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }

    @Override
    public void afterCopy (final VCSFileProxy from, final VCSFileProxy to) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "afterCopy {0}->{1}", new Object[] { from, to }); //NOI18N
        }
        if (to == null) {
            return;
        }

        addToCreated(to);
        // There is no point in refreshing the cache for ignored files.
        if (!cache.getStatus(to).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            reScheduleRefresh(800, Collections.singleton(to), true);
        }
    }

    @Override
    public void afterChange (final VCSFileProxy file) {
        if (file.isDirectory()) {
            return;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "afterChange {0}", new Object[] { file }); //NOI18N
        }
        // There is no point in refreshing the cache for ignored files.
        if (!cache.getStatus(file).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            reScheduleRefresh(800, Collections.singleton(file), true);
        }
    }

    @Override
    public boolean isMutable(VCSFileProxy file) {
        return GitUtils.isPartOfGitMetadata(file) || super.isMutable(file);
    }

    @Override
    public Object getAttribute(VCSFileProxy file, String attrName) {
        if (SearchHistorySupport.PROVIDED_EXTENSIONS_SEARCH_HISTORY.equals(attrName)){
            return new GitSearchHistorySupport(file);
        } else if("ProvidedExtensions.RemoteLocation".equals(attrName)) { //NOI18N
            VCSFileProxy repoRoot = Git.getInstance().getRepositoryRoot(file);
            RepositoryInfo info = RepositoryInfo.getInstance(repoRoot);
            Map<String, GitRemoteConfig> remotes = info.getRemotes();
            StringBuilder sb = new StringBuilder();
            for (GitRemoteConfig rc : remotes.values()) {
                List<String> uris = rc.getUris();
                for (int i = 0; i < uris.size(); i++) {
                    try {
                        GitURI u = new GitURI(uris.get(i));
                        u = u.setUser(null).setPass(null);
                        sb.append(u.toString()).append(';');
                    } catch (URISyntaxException ex) {
                        LOG.log(Level.FINE, null, ex);
                    }
                }
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        } else if ("ProvidedExtensions.VCSIsModified".equals(attrName)) {
            VCSFileProxy repoRoot = Git.getInstance().getRepositoryRoot(file);
            Boolean modified = null;
            if (repoRoot != null) {
                Set<VCSFileProxy> coll = Collections.singleton(file);
                cache.refreshAllRoots(Collections.<VCSFileProxy, Collection<VCSFileProxy>>singletonMap(repoRoot, coll));
                modified = cache.containsFiles(coll, STATUS_VCS_MODIFIED_ATTRIBUTE, true);
            }
            return modified;
        } else {
            return super.getAttribute(file, attrName);
        }
    }

    /**
     * Checks if administrative folder for a repository with the file is registered.
     * @param file
     */
    void pingRepositoryRootFor(final VCSFileProxy file) {
        if (!AUTOMATIC_REFRESH_ENABLED) {
            return;
        }
        gitFolderEventsHandler.initializeFor(file);
    }

    /**
     * Returns a set of known repository roots (those visible or open in IDE)
     * @param repositoryRoot
     * @return
     */
    Set<VCSFileProxy> getSeenRoots (VCSFileProxy repositoryRoot) {
        return gitFolderEventsHandler.getSeenRoots(repositoryRoot);
    }

    /**
     * Runs a given callable and disable listening for external repository events for the time the callable is running.
     * Refreshes cached modification timestamp of metadata for the given git repository after.
     * @param callable code to run
     * @param repository
     * @param commandName name of the git command if available
     */
    <T> T runWithoutExternalEvents(final VCSFileProxy repository, String commandName, Callable<T> callable) throws Exception {
        assert repository != null;
        try {
            if (repository != null) {
                gitFolderEventsHandler.enableEvents(repository, false);
                commandLogger.lockedInternally(repository, commandName);
            }
            return callable.call();
        } finally {
            if (repository != null) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Refreshing index timestamp after: {0} on {1}", new Object[] { commandName, repository.getPath() }); //NOI18N
                }
                if (EventQueue.isDispatchThread()) {
                    Git.getInstance().getRequestProcessor().post(new Runnable() {
                        @Override
                        public void run () {
                            gitFolderEventsHandler.refreshIndexFileTimestamp(repository);
                        }
                    });
                } else {
                    gitFolderEventsHandler.refreshIndexFileTimestamp(repository);
                }
                commandLogger.unlockedInternally(repository);
                gitFolderEventsHandler.enableEvents(repository, true);
            }
        }
    }

    private final Map<VCSFileProxy, Long> createdFolders = new LinkedHashMap<VCSFileProxy, Long>() {

        @Override
        public Long put (VCSFileProxy key, Long value) {
            long t = System.currentTimeMillis();
            for (Iterator<Map.Entry<VCSFileProxy, Long>> it = entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<VCSFileProxy, Long> e = it.next();
                if (e.getValue() < t - 600000) { // keep for 10 minutes
                    it.remove();
                }
            }
            return super.put(key, value);
        }

    };
    private void addToCreated (VCSFileProxy createdFile) {
        if (!GitModuleConfig.getDefault().getAutoIgnoreFiles() || !createdFile.isDirectory()) {
            // no need to keep files and no need to keep anything if auto-ignore-files is disabled
            return;
        }
        synchronized (createdFolders) {
            for (VCSFileProxy f : createdFolders.keySet()) {
                if (VCSFileProxySupport.isAncestorOrEqual(f, createdFile)) {
                    // just keep created roots, no children
                    return;
                }
            }
            createdFolders.put(createdFile, createdFile.lastModified());
        }
    }

    Collection<VCSFileProxy> getCreatedFolders () {
        synchronized (createdFolders) {
            return new HashSet<>(createdFolders.keySet());
        }
    }

    private static class CommandUsageLogger {

        private final Map<VCSFileProxy, Events> events = new HashMap<>();

        private void locked (VCSFileProxy file) {
            VCSFileProxy gitFolder = getGitFolderFor(file);
            // it is a lock file, lock file still exists
            if (gitFolder != null && file.exists()) {
                long time = System.currentTimeMillis();
                synchronized (events) {
                    Events ev = events.get(gitFolder);
                    if (ev == null || ev.isExternal() || ev.timeFinished > 0
                            && ev.timeFinished < time - 10000) {
                        // is new lock or is an old unfinished stale event
                        // and is not part of any internal command that could leave
                        // pending events to be delivered with 10s delay
                        ev = new Events();
                        ev.timeStarted = time;
                        events.put(gitFolder, ev);
                    }
                }
            }
        }

        /**
         * Command run internally from the IDE
         */
        private void lockedInternally (VCSFileProxy repository, String commandName) {
            VCSFileProxy gitFolder = GitUtils.getGitFolderForRoot(repository);
            Events ev = new Events();
            ev.timeStarted = System.currentTimeMillis();
            ev.commandName = commandName;
            synchronized (events) {
                events.put(gitFolder, ev);
            }
        }

        private void unlocked (VCSFileProxy file) {
            VCSFileProxy gitFolder = getGitFolderFor(file);
            if (gitFolder != null) {
                Events ev;
                synchronized (events) {
                    ev = events.remove(gitFolder);
                    if (ev != null && !ev.isExternal()) {
                        // this does not log internal commands
                        events.put(gitFolder, ev);
                        return;
                    }
                }
                if (ev != null) {
                    long time = System.currentTimeMillis() - ev.timeStarted;
                    Utils.logVCSCommandUsageEvent("GIT", time, ev.modifications, ev.commandName, ev.isExternal());
                }
            }
        }

        /**
         * Internal command finish
         */
        private void unlockedInternally (VCSFileProxy repository) {
            VCSFileProxy gitFolder = GitUtils.getGitFolderForRoot(repository);
            Events ev;
            synchronized (events) {
                ev = events.get(gitFolder);
                if (ev == null) {
                    return;
                } else if (ev.isExternal()) {
                    events.remove(gitFolder);
                }
            }
            ev.timeFinished = System.currentTimeMillis();
            long time = ev.timeFinished - ev.timeStarted;
            Utils.logVCSCommandUsageEvent("GIT", time, ev.modifications, ev.commandName, ev.isExternal());
        }

        /**
         *
         * @param wlockFile
         * @return parent git folder for wlock file or null if the file is not
         * a write lock repository file
         */
        private VCSFileProxy getGitFolderFor (VCSFileProxy wlockFile) {
            VCSFileProxy repository = Git.getInstance().getRepositoryRoot(wlockFile);
            VCSFileProxy gitFolder = GitUtils.getGitFolderForRoot(repository);
            return gitFolder.equals(wlockFile.getParentFile())
                    ? gitFolder
                    : null;
        }

        private void logModification (VCSFileProxy file) {
            if (GitUtils.isPartOfGitMetadata(file)) {
                return;
            }
            VCSFileProxy repository = Git.getInstance().getRepositoryRoot(file);
            if (repository != null) {
                VCSFileProxy gitFolder = GitUtils.getGitFolderForRoot(repository);
                if (gitFolder != null) {
                    synchronized (events) {
                        Events ev = events.get(gitFolder);
                        if (ev != null) {
                            ++ev.modifications;
                        }
                    }
                }
            }
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

    // for testing only
    void waitEmptyRefreshQueue() {
        while(true) {
            if (refreshTask.isFinished()) {
                if (filesToRefresh.isEmpty()) {
                    //System.err.println("Refresh Queue is empty");
                    return;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }
    }


    final ProgressMonitor.DefaultProgressMonitor shutdownMonitor = new ProgressMonitor.DefaultProgressMonitor();
    private class RefreshTask implements Runnable {
        
        @Override
        public void run() {
            Thread.interrupted();
            if (DelayScanRegistry.getInstance().isDelayed(refreshTask, Git.STATUS_LOG, "GitRemoteInterceptor.refreshTask")) { //NOI18N
                return;
            }
            Collection<VCSFileProxy> files;
            synchronized (filesToRefresh) {
                files = new HashSet<>(filesToRefresh);
                filesToRefresh.clear();
            }
            if (shutdownMonitor.isCanceled()) {
                return;
            }
            if (!"false".equals(System.getProperty("versioning.git.delayStatusForLockedRepositories"))) {
                files = checkLockedRepositories(files, false);
            }
            if (!files.isEmpty()) {
                cache.refreshAllRoots(files, shutdownMonitor);
            }
            if (!lockedRepositories.isEmpty()) {
                lockedRepositoryRefreshTask.schedule(5000);
            }
        }
    }

    @OnStop
    public static class ShutdownCallable implements Callable<Boolean> {

        @Override
        public Boolean call () throws Exception {
            LOG.log(Level.FINE, "Canceling the auto refresh progress monitor");
            Git.getInstance().getVCSInterceptor().shutdownMonitor.cancel();
            try {
                Git.getInstance().getVCSInterceptor().refreshTask.waitFinished(3000);
            } catch (InterruptedException ex) {}
            return true;
        }

    }

    private Collection<VCSFileProxy> checkLockedRepositories (Collection<VCSFileProxy> additionalFilesToRefresh, boolean keepCached) {
        List<VCSFileProxy> retval = new LinkedList<>();
        // at first sort the files under repositories
        Map<VCSFileProxy, Set<VCSFileProxy>> sortedFiles = GitUtils.sortByRepository(additionalFilesToRefresh);
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
            if (!repository.exists()) {
                // repository does not exist, no need to keep it
                it.remove();
            } else if (GitUtils.isRepositoryLocked(repository)) {
                Git.STATUS_LOG.log(Level.FINE, "checkLockedRepositories(): Repository {0} locked, status refresh delayed", repository); //NOI18N
            } else {
                // repo not locked, add all files into the returned collection
                retval.addAll(entry.getValue());
                if (!keepCached) {
                    it.remove();
                }
            }
        }
        return retval;
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

    private void reScheduleRefresh (int delayMillis, Set<VCSFileProxy> filesToRefresh, boolean log) {
        // refresh all at once
        Set<VCSFileProxy> filteredFiles = new HashSet<>(filesToRefresh);
        for (Iterator<VCSFileProxy> it = filteredFiles.iterator(); it.hasNext(); ) {
            if (GitUtils.isPartOfGitMetadata(it.next())) {
                it.remove();
            }
        }
        boolean changed;
        synchronized (this.filesToRefresh) {
            changed = this.filesToRefresh.addAll(filteredFiles);
        }
        if (changed) {
            Git.STATUS_LOG.log(Level.FINE, "reScheduleRefresh: adding {0}", filteredFiles);
            if (log) {
                for (VCSFileProxy file : filteredFiles) {
                    commandLogger.logModification(file);
                }
            }
            refreshTask.schedule(delayMillis);
        }
    }

    private static class GitFolderTimestamps {
        private final VCSFileProxy indexFile;
        private final long indexFileTS;
        private final VCSFileProxy headFile;
        private final long headFileTS;
        private final VCSFileProxy refFile;
        private final long refFileTS;
        private final VCSFileProxy gitFolder;
        private final VCSFileProxy metadataFolder;
        private long referencesFolderTS;

        public GitFolderTimestamps (VCSFileProxy indexFile, VCSFileProxy headFile, VCSFileProxy refFile, VCSFileProxy gitFolder, VCSFileProxy metadataFolder) {
            this.indexFile = indexFile;
            this.indexFileTS = indexFile.lastModified();
            this.headFile = headFile;
            this.headFileTS = headFile.lastModified();
            this.refFile = refFile;
            this.refFileTS = refFile.lastModified();
            this.gitFolder = gitFolder;
            this.metadataFolder = metadataFolder;
            referencesFolderTS = System.currentTimeMillis();
        }

        private VCSFileProxy getIndexFile () {
            return indexFile;
        }

        private boolean isNewer (GitFolderTimestamps other) {
            boolean newer = true;
            if (other != null) {
                newer = indexFileTS > other.indexFileTS || headFileTS > other.headFileTS
                        || refFileTS > other.refFileTS;
            }
            return newer;
        }

        private VCSFileProxy getGitFolder () {
            return gitFolder;
        }

        private VCSFileProxy getMetadataFolder () {
            return metadataFolder;
        }

        private boolean repositoryExists () {
            return indexFileTS > 0 || gitFolder.exists();
        }

        private boolean isOutdated () {
            // first check the index
            boolean upToDate = indexFileTS >= indexFile.lastModified();
            // then check the current head
            if (upToDate) {
                upToDate = headFileTS >= headFile.lastModified();
            }
            // if pointer to branch did not change, there could still be a commit to the same branch - in that case refs/heads/... file changed
            if (upToDate) {
                upToDate = refFileTS >= refFile.lastModified();
            }
            return !upToDate;
        }

        private boolean updateReferences (VCSFileProxy triggerFolder) {
            boolean updated = false;
            long ts = triggerFolder.lastModified();
            if (ts > referencesFolderTS) {
                updated = true;
                referencesFolderTS = System.currentTimeMillis();
            }
            return updated;
        }
    }

    private static class MetadataMapping {
        private final VCSFileProxy metadataFolder;
        private final long ts;

        public MetadataMapping (VCSFileProxy metadataFolder, long ts) {
            this.metadataFolder = metadataFolder;
            this.ts = ts;
        }
    }

    private class GitFolderEventsHandler {
        private final HashMap<VCSFileProxy, Set<VCSFileProxy>> seenRoots = new HashMap<>();
        private final HashMap<VCSFileProxy, GitFolderTimestamps> timestamps = new HashMap<>(5);
        private final HashMap<VCSFileProxy, MetadataMapping> gitToMetadataFolder = new HashMap<>(5);
        private final HashMap<VCSFileProxy, VCSFileProxy> metadataToGitFolder = new HashMap<>(5);
        private final HashMap<VCSFileProxy, FileChangeListener> gitFolderRLs = new HashMap<>(5);
        private final HashSet<VCSFileProxy> disabledEvents = new HashSet<>(5);

        private final HashSet<VCSFileProxy> filesToInitialize = new HashSet<>();
        private final RequestProcessor.Task initializingTask = rp.create(new Runnable() {
            @Override
            public void run() {
                initializeFiles();
            }
        });

        private final HashSet<VCSFileProxy> refreshedRepositories = new HashSet<>(5);
        private final RequestProcessor.Task refreshOpenFilesTask = rp.create(new Runnable() {
            @Override
            public void run() {
                Set<VCSFileProxy> repositories;
                synchronized (refreshedRepositories) {
                    repositories = new HashSet<>(refreshedRepositories);
                    refreshedRepositories.clear();
                }
                GitUtils.headChanged(repositories.toArray(new VCSFileProxy[repositories.size()]));
            }
        });
        private final GitRepositories gitRepositories = GitRepositories.getInstance();

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

        private boolean addSeenRoot (VCSFileProxy repositoryRoot, VCSFileProxy rootToAdd) {
            boolean added = false;
            Set<VCSFileProxy> seenRootsForRepository = getSeenRootsForRepository(repositoryRoot);
            synchronized (seenRootsForRepository) {
                if (!seenRootsForRepository.contains(repositoryRoot)) {
                    // try to add the file only when the repository root is not yet registered
                    rootToAdd = rootToAdd.normalizeFile();
                    added = !GitUtils.prepareRootFiles(repositoryRoot, seenRootsForRepository, rootToAdd);
                }
            }
            return added;
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

        private GitFolderTimestamps scanGitFolderTimestamps (VCSFileProxy gitFolder) {
            VCSFileProxy metadataFolder = translateToMetadataFolder(gitFolder);
            VCSFileProxy indexFile = VCSFileProxy.createFileProxy(metadataFolder, INDEX_FILE_NAME);
            VCSFileProxy headFile = VCSFileProxy.createFileProxy(metadataFolder, HEAD_FILE_NAME);
            GitBranch activeBranch = null;
            RepositoryInfo info = RepositoryInfo.getInstance(gitFolder.getParentFile());
            if (info != null) {
                info.refresh();
                activeBranch = info.getActiveBranch();
            }
            VCSFileProxy refFile = headFile;
            if (activeBranch != null && !GitBranch.NO_BRANCH.equals(activeBranch.getName())) {
                refFile = VCSFileProxy.createFileProxy(metadataFolder, GitUtils.PREFIX_R_HEADS + activeBranch.getName());
            }
            return new GitFolderTimestamps(indexFile, headFile, refFile, gitFolder, metadataFolder);
        }

        public void refreshIndexFileTimestamp (VCSFileProxy repository) {
            refreshIndexFileTimestamp(scanGitFolderTimestamps(GitUtils.getGitFolderForRoot(repository)));
        }

        private void refreshIndexFileTimestamp (GitFolderTimestamps newTimestamps) {
            VCSFileProxy gitFolder = newTimestamps.getGitFolder(); // this can sadly be a link file gitrdir: PATH_TO_FOLDER
            try {
                if (VCSFileProxySupport.isAncestorOrEqual(VCSFileProxy.createFileProxy(VCSFileProxySupport.getFileSystem(gitFolder).getTempFolder()), newTimestamps.getIndexFile())) { //NOI18N
                    // skip repositories in temp folder
                    return;
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
            final VCSFileProxy metadataFolder = newTimestamps.getMetadataFolder();
            boolean exists = newTimestamps.repositoryExists();
            synchronized (timestamps) {
                if (exists && !newTimestamps.isNewer(timestamps.get(gitFolder))) {
                    // do not enter the filesystem module unless really need to
                    return;
                }
            }
            boolean add = false;
            boolean remove = false;
            synchronized (timestamps) {
                timestamps.remove(gitFolder);
                FileChangeListener list = gitFolderRLs.remove(gitFolder);
                if (exists) {
                    timestamps.put(gitFolder, newTimestamps);
                    if (list == null) {
                        final FileChangeListener fList = list = new FileChangeAdapter();
                        // has to run in a different thread, otherwise we may get a deadlock
                        rp.post(new Runnable () {
                            @Override
                            public void run() {
                                final FileObject fo = metadataFolder.toFileObject();
                                if (fo != null) {
                                    fo.addRecursiveListener(fList);
                                } else {
                                    //TODO:
                                    //FileUtil.addRecursiveListener(fList, metadataFolder);
                                }
                            }
                        });
                    }
                    gitFolderRLs.put(gitFolder, list);
                    add = true;
                } else {
                    if (list != null) {
                        final FileChangeListener fList = list;
                        // has to run in a different thread, otherwise we may get a deadlock
                        rp.post(new Runnable () {
                            @Override
                            public void run() {
                                final FileObject fo = metadataFolder.toFileObject();
                                if (fo != null) {
                                    fo.removeRecursiveListener(fList);
                                } else {
                                    //TODO:
                                    //FileUtil.removeRecursiveListener(fList, metadataFolder);
                                }
                                // repository was deleted, we should refresh versioned parents
                                Git.getInstance().versionedFilesChanged();
                            }
                        });
                    }
                    Git.STATUS_LOG.log(Level.FINE, "refreshAdminFolderTimestamp: {0} no longer exists", gitFolder.getPath()); //NOI18N
                    remove = true;
                }
                if (remove) {
                    gitRepositories.remove(gitFolder.getParentFile(), false);
                    gitToMetadataFolder.remove(gitFolder);
                    metadataToGitFolder.remove(metadataFolder);
                } else if (add) {
                    VCSFileProxy repository = gitFolder.getParentFile();
                    if (!repository.equals(Git.getInstance().getRepositoryRoot(repository))) {
                        // guess this is needed, versionedFilesChanged might not have been called yet (see InitAction)
                        Git.getInstance().versionedFilesChanged();
                    }
                    gitRepositories.add(repository, false);
                }
            }
        }

        private void initializeFiles() {
            VCSFileProxy file;
            while ((file = getFileToInitialize()) != null) {
                if (Git.STATUS_LOG.isLoggable(Level.FINEST)) {
                    Git.STATUS_LOG.log(Level.FINEST, "GitFolderEventsHandler.initializeFiles: {0}", file.getPath()); //NOI18N
                }
                // select repository root for the file and finds it's .git folder
                VCSFileProxy repositoryRoot = Git.getInstance().getRepositoryRoot(file);
                if (repositoryRoot != null) {
                    if (addSeenRoot(repositoryRoot, file)) {
                        // this means the repository has not yet been scanned, so scan it
                        if (Git.STATUS_LOG.isLoggable(Level.FINE)) {
                            Git.STATUS_LOG.log(Level.FINE, "initializeFiles: planning a scan for {0} - {1}", new Object[]{repositoryRoot.getPath(), file.getPath()}); //NOI18N
                        }
                        reScheduleRefresh(4000, Collections.singleton(file), false);
                        VCSFileProxy gitFolder = GitUtils.getGitFolderForRoot(repositoryRoot);
                        boolean refreshNeeded = false;
                        synchronized (timestamps) {
                            if (!timestamps.containsKey(gitFolder)) {
                                VCSFileProxy metadataFolder = translateToMetadataFolder(gitFolder);
                                if (VCSFileProxySupport.canRead(VCSFileProxy.createFileProxy(metadataFolder, INDEX_FILE_NAME))) {
                                    timestamps.put(gitFolder, null);
                                    refreshNeeded = true;
                                }
                            }
                        }
                        if (refreshNeeded) {
                            refreshIndexFileTimestamp(scanGitFolderTimestamps(gitFolder));
                        }
                    }
                }
            }
            if (Git.STATUS_LOG.isLoggable(Level.FINEST)) {
                Git.STATUS_LOG.log(Level.FINEST, "GitFolderEventsHandler.initializeFiles: finished"); //NOI18N
            }
        }

        private long refreshAdminFolder (VCSFileProxy metadataFolder) {
            long lastModified = 0;
            if (AUTOMATIC_REFRESH_ENABLED && !"false".equals(System.getProperty("versioning.git.handleExternalEvents", "true"))) { //NOI18N
                metadataFolder = metadataFolder.normalizeFile();
                if (Git.STATUS_LOG.isLoggable(Level.FINER)) {
                    Git.STATUS_LOG.log(Level.FINER, "refreshAdminFolder: special FS event handling for {0}", metadataFolder.getPath()); //NOI18N
                }
                GitFolderTimestamps cached;
                VCSFileProxy gitFolder = translateToGitFolder(metadataFolder);
                if (isEnabled(gitFolder)) {
                    synchronized (timestamps) {
                        cached = timestamps.get(gitFolder);
                    }
                    if (cached == null || !cached.repositoryExists() || cached.isOutdated()) {
                        synchronized (metadataFoldersToRefresh) {
                            if (metadataFoldersToRefresh.add(gitFolder)) {
                                refreshGitRepoTask.schedule(1000);
                            }
                        }
                    }
                }
            }
            return lastModified;
        }

        private final Set<VCSFileProxy> metadataFoldersToRefresh = new HashSet<>();
        private final RequestProcessor.Task refreshGitRepoTask = rp.create(new RefreshMetadata());
        
        private class RefreshMetadata implements Runnable {
            
            @Override
            public void run () {
                Set<VCSFileProxy> stillLockedRepos = new HashSet<>();
                for (VCSFileProxy gitFolder = getNextRepository(); gitFolder != null; gitFolder = getNextRepository()) {
                    if (GitUtils.isRepositoryLocked(gitFolder.getParentFile())) {
                        Git.STATUS_LOG.log(Level.FINE, "refreshAdminFolder: replanning repository scan for locked {0}", gitFolder); //NOI18N
                        stillLockedRepos.add(gitFolder);
                    } else {
                        refreshIndexFileTimestamp(scanGitFolderTimestamps(gitFolder));
                        VCSFileProxy repository = gitFolder.getParentFile();
                        RepositoryInfo.refreshAsync(repository);
                        Git.STATUS_LOG.log(Level.FINE, "refreshAdminFolder: planning repository scan for {0}", repository.getPath()); //NOI18N
                        reScheduleRefresh(3000, getSeenRoots(repository), false); // scan repository root
                        refreshOpenFiles(repository);
                    }
                }
                synchronized (metadataFoldersToRefresh) {
                    if (metadataFoldersToRefresh.addAll(stillLockedRepos)) {
                        refreshGitRepoTask.schedule(2000);
                    }
                }
            }
            
            private VCSFileProxy getNextRepository () {
                VCSFileProxy gitFolder = null;
                synchronized (metadataFoldersToRefresh) {
                    if (!metadataFoldersToRefresh.isEmpty()) {
                        Iterator<VCSFileProxy> it = metadataFoldersToRefresh.iterator();
                        gitFolder = it.next();
                        it.remove();
                    }
                }
                return gitFolder;
            }
                        
        }

        private void refreshReferences (VCSFileProxy metadataFolder, VCSFileProxy triggerFolder) {
            if (AUTOMATIC_REFRESH_ENABLED && !"false".equals(System.getProperty("versioning.git.handleExternalEvents", "true"))) { //NOI18N
                metadataFolder = metadataFolder.normalizeFile();
                if (Git.STATUS_LOG.isLoggable(Level.FINER)) {
                    Git.STATUS_LOG.log(Level.FINER, "refreshReferences: special FS event handling for {0}", triggerFolder.getPath()); //NOI18N
                }
                boolean refreshNeeded = false;
                GitFolderTimestamps cached;
                VCSFileProxy gitFolder = translateToGitFolder(metadataFolder);
                if (isEnabled(gitFolder)) {
                    synchronized (timestamps) {
                        cached = timestamps.get(gitFolder);
                    }
                    if (cached != null && cached.updateReferences(triggerFolder)) {
                        refreshNeeded = true;
                    }
                    if (refreshNeeded) {
                        VCSFileProxy repository = gitFolder.getParentFile();
                        RepositoryInfo.refreshAsync(repository);
                    }
                }
            }
        }

        private void refreshOpenFiles (VCSFileProxy repository) {
            boolean refreshPlanned;
            synchronized (refreshedRepositories) {
                refreshPlanned = !refreshedRepositories.add(repository);
            }
            if (!refreshPlanned) {
                refreshOpenFilesTask.schedule(3000);
            }
        }

        private void enableEvents (VCSFileProxy repository, boolean enabled) {
            VCSFileProxy gitFolder = GitUtils.getGitFolderForRoot(repository).normalizeFile();
            synchronized (disabledEvents) {
                if (enabled) {
                    disabledEvents.remove(gitFolder);
                } else {
                    disabledEvents.add(gitFolder);
                }
            }
        }

        private boolean isEnabled (VCSFileProxy gitFolder) {
            synchronized (disabledEvents) {
                return !disabledEvents.contains(gitFolder);
            }
        }

        private VCSFileProxy translateToMetadataFolder (VCSFileProxy gitFolder) {
            MetadataMapping mapping;
            synchronized(timestamps) {
                mapping = gitToMetadataFolder.get(gitFolder);
            }
            VCSFileProxy metadataFolder;
            long ts;
            if (mapping == null) {
                metadataFolder = gitFolder;
                ts = System.currentTimeMillis();
            } else {
                metadataFolder = mapping.metadataFolder;
                ts = mapping.ts;
            }
            if (gitFolder.isFile()) {
                ts = gitFolder.lastModified();
                if (mapping == null || mapping.ts < ts) {
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new InputStreamReader(gitFolder.getInputStream(false), "UTF-8")); //NOI18N
                        for (String line = br.readLine(); line != null; line = br.readLine()) {
                            line = line.trim();
                            if (line.startsWith("gitdir:")) { //NOI18N
                                line = line.substring(7).trim();
                                VCSFileProxy tmp;
                                if (line.startsWith("/")) {
                                    tmp = VCSFileProxySupport.getResource(gitFolder, line);
                                } else {
                                    tmp = VCSFileProxySupport.getCanonicalFile(VCSFileProxy.createFileProxy(gitFolder, line));
                                }
                                metadataFolder = tmp;
                                break;
                            }
                        }
                    } catch (IOException ex) {
                        //
                    } finally {
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException ex) {
                            }
                        }
                    }
                }
            }
            synchronized (timestamps) {
                gitToMetadataFolder.put(gitFolder, new MetadataMapping(metadataFolder, ts));
                metadataToGitFolder.put(metadataFolder, gitFolder);
            }
            return metadataFolder;
        }

        private VCSFileProxy translateToGitFolder (VCSFileProxy metadataFolder) {
            VCSFileProxy gitFolder;
            synchronized (timestamps) {
                gitFolder = metadataToGitFolder.get(metadataFolder);
            }
            if (gitFolder == null) {
                gitFolder = metadataFolder;
            }
            return gitFolder;
        }

        private boolean isMetadataFolder (VCSFileProxy dir) {
            synchronized (timestamps) {
                return metadataToGitFolder.containsKey(dir);
            }
        }

        private VCSFileProxy getMetadataForReferences (VCSFileProxy file) {
            List<VCSFileProxy> metadataFolders;
            synchronized (timestamps) {
                metadataFolders = new ArrayList<>(metadataToGitFolder.keySet());
            }
            VCSFileProxy candidate = null;
            for (VCSFileProxy metadataFolder : metadataFolders) {
                String refsPath = VCSFileProxy.createFileProxy(metadataFolder, REFS_FILE_NAME).getPath();
                if (file.getPath().startsWith(refsPath)) {
                    if (candidate == null || candidate.getPath().length() < metadataFolder.getPath().length()) {
                        candidate = metadataFolder;
                    }
                }
            }
            return candidate;
        }
    }

    public static class GitSearchHistorySupport extends SearchHistorySupport {
        public GitSearchHistorySupport(VCSFileProxy file) {
            super(file);
        }
        @Override
        protected boolean searchHistoryImpl(final int line) throws IOException {
            VCSFileProxy file = getFile();
            SearchHistoryAction.openSearch(Git.getInstance().getRepositoryRoot(file), file, file.getName(), line);
            return true;
        }

    }
}
