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

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitURI;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.ui.history.SearchHistoryAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.util.DelayScanRegistry;
import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.versioning.util.SearchHistorySupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileUtil;
import org.openide.modules.OnStop;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * @author ondra
 */
class FilesystemInterceptor extends VCSInterceptor {

    private final FileStatusCache   cache;

    private final Set<File> filesToRefresh = new HashSet<>();
    private final Map<File, Set<File>> lockedRepositories = new HashMap<>(5);

    private final RequestProcessor.Task refreshTask, lockedRepositoryRefreshTask;
    private final RequestProcessor.Task refreshOwnersTask;

    private static final RequestProcessor rp = new RequestProcessor("GitRefresh", 1, true);
    private final GitFolderEventsHandler gitFolderEventsHandler;
    private final CommandUsageLogger commandLogger;
    // not final due to tests
    private static boolean AUTOMATIC_REFRESH_ENABLED = !"true".equals(System.getProperty("versioning.git.autoRefreshDisabled", "false")); //NOI18N
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
                git.versionedFilesChanged();
                VersioningSupport.versionedRootsChanged();
            }
        });
    }

    @Override
    public long refreshRecursively (File dir, long lastTimeStamp, List<? super File> children) {
        long retval = -1;
        if (GitUtils.DOT_GIT.equals(dir.getName()) || gitFolderEventsHandler.isMetadataFolder(dir)) {
            Git.STATUS_LOG.log(Level.FINER, "Interceptor.refreshRecursively: {0}", dir.getAbsolutePath()); //NOI18N
            children.clear();
            retval = gitFolderEventsHandler.refreshAdminFolder(dir);
            File[] ch = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept (File dir, String name) {
                    return REFS_FILE_NAME.equals(name);
                }
            });
            if (ch != null) {
                children.addAll(Arrays.asList(ch));
            }
        } else if (GitUtils.isPartOfGitMetadata(dir)) {
            // the condition above is to limit number of following code invocations
            // changes done in metadata not present under .git folder are not recognized - there's still the manual refresh
            File metadataFolder = gitFolderEventsHandler.getMetadataForReferences(dir);
            if (metadataFolder != null) {
                gitFolderEventsHandler.refreshReferences(metadataFolder, dir);
            }
        }
        return retval;
    }

    @Override
    public boolean beforeCreate (final File file, boolean isDirectory) {
        LOG.log(Level.FINE, "beforeCreate {0} - {1}", new Object[] { file, isDirectory }); //NOI18N
        if (GitUtils.isPartOfGitMetadata(file)) return false;
        if (!isDirectory && !file.exists()) {
            Git git = Git.getInstance();
            final File root = git.getRepositoryRoot(file);
            if (root == null) return false;
            GitClient client = null;
            try {
                client = git.getClient(root);
                client.reset(new File[] { file }, GitUtils.HEAD, true, GitUtils.NULL_PROGRESS_MONITOR);
            } catch (GitException.MissingObjectException ex) {
                if (!GitUtils.HEAD.equals(ex.getObjectName())) {
                    // log only if we already have a commit. Just initialized repository does not allow us to reset
                    LOG.log(Level.INFO, "beforeCreate(): File: {0} {1}", new Object[] { file.getAbsolutePath(), ex.toString()}); //NOI18N
                }
            } catch (GitException ex) {
                LOG.log(Level.INFO, "beforeCreate(): File: {0} {1}", new Object[] { file.getAbsolutePath(), ex.toString()}); //NOI18N
            } finally {
                if (client != null) {
                    client.release();
                }
            }
            LOG.log(Level.FINER, "beforeCreate(): finished: {0}", file); // NOI18N
        }
        return false;
    }

    @Override
    public void afterCreate (final File file) {
        LOG.log(Level.FINE, "afterCreate {0}", file); //NOI18N
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
    public boolean beforeDelete (File file) {
        LOG.log(Level.FINE, "beforeDelete {0}", file); //NOI18N
        if (file == null) return false;
        if (GitUtils.isPartOfGitMetadata(file)) return false;

        // do not handle delete for ignored files
        return !cache.getStatus(file).containsStatus(Status.NOTVERSIONED_EXCLUDED);
    }

    @Override
    public void doDelete (File file) throws IOException {
        LOG.log(Level.FINE, "doDelete {0}", file); //NOI18N
        if (file == null) return;
        Git git = Git.getInstance();
        File root = git.getRepositoryRoot(file);
        GitClient client = null;
        try {
            if (GitUtils.getGitFolderForRoot(root).exists()) {
                client = git.getClient(root);
                client.remove(new File[] { file }, false, GitUtils.NULL_PROGRESS_MONITOR);
            } else if (file.exists()) {
                Utils.deleteRecursively(file);
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
    public void afterDelete(final File file) {
        LOG.log(Level.FINE, "afterDelete {0}", file); //NOI18N
        if (file == null) return;
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
    public boolean beforeMove(File from, File to) {
        LOG.log(Level.FINE, "beforeMove {0} -> {1}", new Object[] { from, to }); //NOI18N
        if (from == null || to == null || to.exists()) return true;
        Git hg = Git.getInstance();
        return hg.isManaged(from) && hg.isManaged(to);
    }

    @Override
    public void doMove(final File from, final File to) throws IOException {
        LOG.log(Level.FINE, "doMove {0} -> {1}", new Object[] { from, to }); //NOI18N
        if (from == null || to == null || to.exists() && !equalPathsIgnoreCase(from, to)) return;

        Git git = Git.getInstance();
        File root = git.getRepositoryRoot(from);
        File dstRoot = git.getRepositoryRoot(to);
        GitClient client = null;
        try {
            if (root != null && root.equals(dstRoot) && !cache.getStatus(to).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
                // target does not lie under ignored folder and is in the same repo as src
                client = git.getClient(root);
                if (equalPathsIgnoreCase(from, to)) {
                    // must do rename --after because the files/paths equal on Win or Mac
                    if (!from.renameTo(to)) {
                        throw new IOException(NbBundle.getMessage(FilesystemInterceptor.class, "MSG_MoveFailed", new Object[] { from, to, "" })); //NOI18N
                    }
                    client.rename(from, to, true, GitUtils.NULL_PROGRESS_MONITOR);
                } else {
                    client.rename(from, to, false, GitUtils.NULL_PROGRESS_MONITOR);
                }
            } else {
                boolean result = from.renameTo(to);
                if (!result) {
                    throw new IOException(NbBundle.getMessage(FilesystemInterceptor.class, "MSG_MoveFailed", new Object[] { from, to, "" })); //NOI18N
                }
                if (root != null) {
                    client = git.getClient(root);
                    client.remove(new File[] { from }, true, GitUtils.NULL_PROGRESS_MONITOR);
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

    private boolean equalPathsIgnoreCase (final File from, final File to) {
        return Utilities.isWindows() && from.equals(to) || Utilities.isMac() && from.getPath().equalsIgnoreCase(to.getPath());
    }

    @Override
    public void afterMove(final File from, final File to) {
        LOG.log(Level.FINE, "afterMove {0} -> {1}", new Object[] { from, to }); //NOI18N
        if (from == null || to == null || !to.exists()) return;

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
    public boolean beforeCopy (File from, File to) {
        LOG.log(Level.FINE, "beforeCopy {0}->{1}", new Object[] { from, to }); //NOI18N
        if (from == null || to == null || to.exists()) return true;
        Git git = Git.getInstance();
        return git.isManaged(from) && git.isManaged(to);
    }

    @Override
    public void doCopy (final File from, final File to) throws IOException {
        LOG.log(Level.FINE, "doCopy {0}->{1}", new Object[] { from, to }); //NOI18N
        if (from == null || to == null || to.exists()) return;

        Git git = Git.getInstance();
        File root = git.getRepositoryRoot(from);
        File dstRoot = git.getRepositoryRoot(to);

        if (from.isDirectory()) {
            FileUtils.copyDirFiles(from, to);
        } else {
            FileUtils.copyFile(from, to);
        }

        if (root == null
                // target lies under ignored folder, do not add it
                || cache.getStatus(to).containsStatus(Status.NOTVERSIONED_EXCLUDED)
                // user choose that new files shall be excluded from commit by
                // default, so follow that decision
                || GitModuleConfig.getDefault().getExludeNewFiles()) {
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
    public void afterCopy (final File from, final File to) {
        LOG.log(Level.FINE, "afterCopy {0}->{1}", new Object[] { from, to }); //NOI18N
        if (to == null) return;

        addToCreated(to);
        // There is no point in refreshing the cache for ignored files.
        if (!cache.getStatus(to).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            reScheduleRefresh(800, Collections.singleton(to), true);
        }
    }

    @Override
    public void afterChange (final File file) {
        if (file.isDirectory()) return;
        LOG.log(Level.FINE, "afterChange {0}", new Object[] { file }); //NOI18N
        // There is no point in refreshing the cache for ignored files.
        if (!cache.getStatus(file).containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            reScheduleRefresh(800, Collections.singleton(file), true);
        }
    }

    @Override
    public boolean isMutable(File file) {
        return GitUtils.isPartOfGitMetadata(file) || super.isMutable(file);
    }

    @Override
    public Object getAttribute(File file, String attrName) {
        if (SearchHistorySupport.PROVIDED_EXTENSIONS_SEARCH_HISTORY.equals(attrName)){
            return new GitSearchHistorySupport(file);
        } else if("ProvidedExtensions.RemoteLocation".equals(attrName)) { //NOI18N
            File repoRoot = Git.getInstance().getRepositoryRoot(file);
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
            File repoRoot = Git.getInstance().getRepositoryRoot(file);
            Boolean modified = null;
            if (repoRoot != null) {
                Set<File> coll = Collections.singleton(file);
                cache.refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repoRoot, coll));
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
    void pingRepositoryRootFor(final File file) {
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
    Set<File> getSeenRoots (File repositoryRoot) {
        return gitFolderEventsHandler.getSeenRoots(repositoryRoot);
    }

    /**
     * Runs a given callable and disable listening for external repository events for the time the callable is running.
     * Refreshes cached modification timestamp of metadata for the given git repository after.
     * @param callable code to run
     * @param repository
     * @param commandName name of the git command if available
     */
    <T> T runWithoutExternalEvents(final File repository, String commandName, Callable<T> callable) throws Exception {
        assert repository != null;
        try {
            if (repository != null) {
                gitFolderEventsHandler.enableEvents(repository, false);
                commandLogger.lockedInternally(repository, commandName);
            }
            return callable.call();
        } finally {
            if (repository != null) {
                LOG.log(Level.FINER, "Refreshing index timestamp after: {0} on {1}", new Object[] { commandName, repository.getAbsolutePath() }); //NOI18N
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

    private final Map<File, Long> createdFolders = new LinkedHashMap<File, Long>() {

        @Override
        public Long put (File key, Long value) {
            long t = System.currentTimeMillis();
            for (Iterator<Map.Entry<File, Long>> it = entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<File, Long> e = it.next();
                if (e.getValue() < t - 600000) { // keep for 10 minutes
                    it.remove();
                }
            }
            return super.put(key, value);
        }

    };
    private void addToCreated (File createdFile) {
        if (!GitModuleConfig.getDefault().getAutoIgnoreFiles() || !createdFile.isDirectory()) {
            // no need to keep files and no need to keep anything if auto-ignore-files is disabled
            return;
        }
        synchronized (createdFolders) {
            for (File f : createdFolders.keySet()) {
                if (Utils.isAncestorOrEqual(f, createdFile)) {
                    // just keep created roots, no children
                    return;
                }
            }
            createdFolders.put(createdFile, createdFile.lastModified());
        }
    }

    Collection<File> getCreatedFolders () {
        synchronized (createdFolders) {
            return new HashSet<>(createdFolders.keySet());
        }
    }

    private class CommandUsageLogger {

        private final Map<File, Events> events = new HashMap<>();

        private void locked (File file) {
            File gitFolder = getGitFolderFor(file);
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
        private void lockedInternally (File repository, String commandName) {
            File gitFolder = GitUtils.getGitFolderForRoot(repository);
            Events ev = new Events();
            ev.timeStarted = System.currentTimeMillis();
            ev.commandName = commandName;
            synchronized (events) {
                events.put(gitFolder, ev);
            }
        }

        private void unlocked (File file) {
            File gitFolder = getGitFolderFor(file);
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
        private void unlockedInternally (File repository) {
            File gitFolder = GitUtils.getGitFolderForRoot(repository);
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
        private File getGitFolderFor (File wlockFile) {
            File repository = Git.getInstance().getRepositoryRoot(wlockFile);
            File gitFolder = GitUtils.getGitFolderForRoot(repository);
            return gitFolder.equals(wlockFile.getParentFile())
                    ? gitFolder
                    : null;
        }

        private void logModification (File file) {
            if (GitUtils.isPartOfGitMetadata(file)) {
                return;
            }
            File repository = Git.getInstance().getRepositoryRoot(file);
            File gitFolder = GitUtils.getGitFolderForRoot(repository);
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

    private static class Events {
        long timeStarted;
        long timeFinished;
        long modifications;
        String commandName;

        private boolean isExternal () {
            return commandName == null;
        }
    }

    final ProgressMonitor.DefaultProgressMonitor shutdownMonitor = new ProgressMonitor.DefaultProgressMonitor();
    private class RefreshTask implements Runnable {
        
        @Override
        public void run() {
            Thread.interrupted();
            if (DelayScanRegistry.getInstance().isDelayed(refreshTask, Git.STATUS_LOG, "GitInterceptor.refreshTask")) { //NOI18N
                return;
            }
            Collection<File> files;
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

    private Collection<File> checkLockedRepositories (Collection<File> additionalFilesToRefresh, boolean keepCached) {
        List<File> retval = new LinkedList<>();
        // at first sort the files under repositories
        Map<File, Set<File>> sortedFiles = GitUtils.sortByRepository(additionalFilesToRefresh);
        for (Map.Entry<File, Set<File>> e : sortedFiles.entrySet()) {
            Set<File> alreadyPlanned = lockedRepositories.get(e.getKey());
            if (alreadyPlanned == null) {
                alreadyPlanned = new HashSet<>();
                lockedRepositories.put(e.getKey(), alreadyPlanned);
            }
            alreadyPlanned.addAll(e.getValue());
        }
        // return all files that do not belong to a locked repository
        for (Iterator<Map.Entry<File, Set<File>>> it = lockedRepositories.entrySet().iterator(); it.hasNext();) {
            Map.Entry<File, Set<File>> entry = it.next();
            File repository = entry.getKey();
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
            if (!checkLockedRepositories(Collections.<File>emptySet(), true).isEmpty()) {
                // there are some newly unlocked repositories to refresh
                refreshTask.schedule(0);
            } else if (!lockedRepositories.isEmpty()) {
                lockedRepositoryRefreshTask.schedule(5000);
            }
        }
    }

    private void reScheduleRefresh (int delayMillis, Set<File> filesToRefresh, boolean log) {
        // refresh all at once
        Set<File> filteredFiles = new HashSet<>(filesToRefresh);
        for (Iterator<File> it = filteredFiles.iterator(); it.hasNext(); ) {
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
                for (File file : filteredFiles) {
                    commandLogger.logModification(file);
                }
            }
            refreshTask.schedule(delayMillis);
        }
    }

    private static class GitFolderTimestamps {
        private final File indexFile;
        private final long indexFileTS;
        private final File headFile;
        private final long headFileTS;
        private final File refFile;
        private final long refFileTS;
        private final File gitFolder;
        private final File metadataFolder;
        private long referencesFolderTS;

        public GitFolderTimestamps (File indexFile, File headFile, File refFile, File gitFolder, File metadataFolder) {
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

        private File getIndexFile () {
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

        private File getGitFolder () {
            return gitFolder;
        }

        private File getMetadataFolder () {
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

        private boolean updateReferences (File triggerFolder) {
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
        private final File metadataFolder;
        private final long ts;

        public MetadataMapping (File metadataFolder, long ts) {
            this.metadataFolder = metadataFolder;
            this.ts = ts;
        }
    }

    private class GitFolderEventsHandler {
        private final HashMap<File, Set<File>> seenRoots = new HashMap<>();
        private final HashMap<File, GitFolderTimestamps> timestamps = new HashMap<>(5);
        private final HashMap<File, MetadataMapping> gitToMetadataFolder = new HashMap<>(5);
        private final HashMap<File, File> metadataToGitFolder = new HashMap<>(5);
        private final HashMap<File, FileChangeListener> gitFolderRLs = new HashMap<>(5);
        private final HashSet<File> disabledEvents = new HashSet<>(5);

        private final HashSet<File> filesToInitialize = new HashSet<>();
        private final RequestProcessor.Task initializingTask = rp.create(new Runnable() {
            @Override
            public void run() {
                initializeFiles();
            }
        });

        private final HashSet<File> refreshedRepositories = new HashSet<>(5);
        private final RequestProcessor.Task refreshOpenFilesTask = rp.create(new Runnable() {
            @Override
            public void run() {
                Set<File> repositories;
                synchronized (refreshedRepositories) {
                    repositories = new HashSet<>(refreshedRepositories);
                    refreshedRepositories.clear();
                }
                GitUtils.headChanged(repositories.toArray(new File[0]));
            }
        });
        private final GitRepositories gitRepositories = GitRepositories.getInstance();

        public void initializeFor (File file) {
            if (addFileToInitialize(file)) {
                initializingTask.schedule(500);
            }
        }

        private Set<File> getSeenRoots (File repositoryRoot) {
            Set<File> retval = new HashSet<>();
            Set<File> seenRootsForRepository = getSeenRootsForRepository(repositoryRoot);
            synchronized (seenRootsForRepository) {
                retval.addAll(seenRootsForRepository);
            }
            return retval;
        }

        private boolean addSeenRoot (File repositoryRoot, File rootToAdd) {
            boolean added = false;
            Set<File> seenRootsForRepository = getSeenRootsForRepository(repositoryRoot);
            synchronized (seenRootsForRepository) {
                if (!seenRootsForRepository.contains(repositoryRoot)) {
                    // try to add the file only when the repository root is not yet registered
                    rootToAdd = FileUtil.normalizeFile(rootToAdd);
                    added = !GitUtils.prepareRootFiles(repositoryRoot, seenRootsForRepository, rootToAdd);
                }
            }
            return added;
        }

        private Set<File> getSeenRootsForRepository (File repositoryRoot) {
            synchronized (seenRoots) {
                 Set<File> seenRootsForRepository = seenRoots.get(repositoryRoot);
                 if (seenRootsForRepository == null) {
                     seenRoots.put(repositoryRoot, seenRootsForRepository = new HashSet<>());
                 }
                 return seenRootsForRepository;
            }
        }

        private boolean addFileToInitialize(File file) {
            synchronized (filesToInitialize) {
                return filesToInitialize.add(file);
            }
        }

        private File getFileToInitialize () {
            File nextFile = null;
            synchronized (filesToInitialize) {
                Iterator<File> iterator = filesToInitialize.iterator();
                if (iterator.hasNext()) {
                    nextFile = iterator.next();
                    iterator.remove();
                }
            }
            return nextFile;
        }

        private GitFolderTimestamps scanGitFolderTimestamps (File gitFolder) {
            File metadataFolder = translateToMetadataFolder(gitFolder);
            File indexFile = new File(metadataFolder, INDEX_FILE_NAME);
            File headFile = new File(metadataFolder, HEAD_FILE_NAME);
            GitBranch activeBranch = null;
            RepositoryInfo info = RepositoryInfo.getInstance(gitFolder.getParentFile());
            if (info != null) {
                info.refresh();
                activeBranch = info.getActiveBranch();
            }
            File refFile = headFile;
            if (activeBranch != null && !GitBranch.NO_BRANCH.equals(activeBranch.getName())) {
                refFile = new File(metadataFolder, (GitUtils.PREFIX_R_HEADS + activeBranch.getName()).replace("/", File.separator)); //NOI18N
            }
            return new GitFolderTimestamps(indexFile, headFile, refFile, gitFolder, metadataFolder);
        }

        public void refreshIndexFileTimestamp (File repository) {
            refreshIndexFileTimestamp(scanGitFolderTimestamps(GitUtils.getGitFolderForRoot(repository)));
        }

        private void refreshIndexFileTimestamp (GitFolderTimestamps newTimestamps) {
            if (Utils.isAncestorOrEqual(new File(System.getProperty("java.io.tmpdir")), newTimestamps.getIndexFile())) { //NOI18N
                // skip repositories in temp folder
                return;
            }
            File gitFolder = newTimestamps.getGitFolder(); // this can sadly be a link file gitrdir: PATH_TO_FOLDER
            final File metadataFolder = newTimestamps.getMetadataFolder();
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
                                FileUtil.addRecursiveListener(fList, metadataFolder);
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
                                FileUtil.removeRecursiveListener(fList, metadataFolder);
                                // repository was deleted, we should refresh versioned parents
                                Git.getInstance().versionedFilesChanged();
                            }
                        });
                    }
                    Git.STATUS_LOG.log(Level.FINE, "refreshAdminFolderTimestamp: {0} no longer exists", gitFolder.getAbsolutePath()); //NOI18N
                    remove = true;
                }
                if (remove) {
                    gitRepositories.remove(gitFolder.getParentFile(), false);
                    gitToMetadataFolder.remove(gitFolder);
                    metadataToGitFolder.remove(metadataFolder);
                } else if (add) {
                    File repository = gitFolder.getParentFile();
                    if (!repository.equals(Git.getInstance().getRepositoryRoot(repository))) {
                        // guess this is needed, versionedFilesChanged might not have been called yet (see InitAction)
                        Git.getInstance().versionedFilesChanged();
                    }
                    gitRepositories.add(repository, false);
                }
            }
        }

        private void initializeFiles() {
            File file;
            while ((file = getFileToInitialize()) != null) {
                Git.STATUS_LOG.log(Level.FINEST, "GitFolderEventsHandler.initializeFiles: {0}", file.getAbsolutePath()); //NOI18N
                // select repository root for the file and finds it's .git folder
                File repositoryRoot = Git.getInstance().getRepositoryRoot(file);
                if (repositoryRoot != null) {
                    if (addSeenRoot(repositoryRoot, file)) {
                        // this means the repository has not yet been scanned, so scan it
                        Git.STATUS_LOG.log(Level.FINE, "initializeFiles: planning a scan for {0} - {1}", new Object[]{repositoryRoot.getAbsolutePath(), file.getAbsolutePath()}); //NOI18N
                        reScheduleRefresh(4000, Collections.singleton(file), false);
                        File gitFolder = GitUtils.getGitFolderForRoot(repositoryRoot);
                        boolean refreshNeeded = false;
                        synchronized (timestamps) {
                            if (!timestamps.containsKey(gitFolder)) {
                                File metadataFolder = translateToMetadataFolder(gitFolder);
                                if (new File(metadataFolder, INDEX_FILE_NAME).canRead()) {
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
            Git.STATUS_LOG.log(Level.FINEST, "GitFolderEventsHandler.initializeFiles: finished"); //NOI18N
        }

        private long refreshAdminFolder (File metadataFolder) {
            long lastModified = 0;
            if (AUTOMATIC_REFRESH_ENABLED && !"false".equals(System.getProperty("versioning.git.handleExternalEvents", "true"))) { //NOI18N
                metadataFolder = FileUtil.normalizeFile(metadataFolder);
                Git.STATUS_LOG.log(Level.FINER, "refreshAdminFolder: special FS event handling for {0}", metadataFolder.getAbsolutePath()); //NOI18N
                GitFolderTimestamps cached;
                File gitFolder = translateToGitFolder(metadataFolder);
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

        private final Set<File> metadataFoldersToRefresh = new HashSet<>();
        private final RequestProcessor.Task refreshGitRepoTask = rp.create(new RefreshMetadata());
        
        private class RefreshMetadata implements Runnable {
            
            @Override
            public void run () {
                Set<File> stillLockedRepos = new HashSet<>();
                for (File gitFolder = getNextRepository(); gitFolder != null; gitFolder = getNextRepository()) {
                    if (GitUtils.isRepositoryLocked(gitFolder.getParentFile())) {
                        Git.STATUS_LOG.log(Level.FINE, "refreshAdminFolder: replanning repository scan for locked {0}", gitFolder); //NOI18N
                        stillLockedRepos.add(gitFolder);
                    } else {
                        refreshIndexFileTimestamp(scanGitFolderTimestamps(gitFolder));
                        File repository = gitFolder.getParentFile();
                        RepositoryInfo.refreshAsync(repository);
                        Git.STATUS_LOG.log(Level.FINE, "refreshAdminFolder: planning repository scan for {0}", repository.getAbsolutePath()); //NOI18N
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
            
            private File getNextRepository () {
                File gitFolder = null;
                synchronized (metadataFoldersToRefresh) {
                    if (!metadataFoldersToRefresh.isEmpty()) {
                        Iterator<File> it = metadataFoldersToRefresh.iterator();
                        gitFolder = it.next();
                        it.remove();
                    }
                }
                return gitFolder;
            }
                        
        }

        private void refreshReferences (File metadataFolder, File triggerFolder) {
            if (AUTOMATIC_REFRESH_ENABLED && !"false".equals(System.getProperty("versioning.git.handleExternalEvents", "true"))) { //NOI18N
                metadataFolder = FileUtil.normalizeFile(metadataFolder);
                Git.STATUS_LOG.log(Level.FINER, "refreshReferences: special FS event handling for {0}", triggerFolder.getAbsolutePath()); //NOI18N
                boolean refreshNeeded = false;
                GitFolderTimestamps cached;
                File gitFolder = translateToGitFolder(metadataFolder);
                if (isEnabled(gitFolder)) {
                    synchronized (timestamps) {
                        cached = timestamps.get(gitFolder);
                    }
                    if (cached != null && cached.updateReferences(triggerFolder)) {
                        refreshNeeded = true;
                    }
                    if (refreshNeeded) {
                        File repository = gitFolder.getParentFile();
                        RepositoryInfo.refreshAsync(repository);
                    }
                }
            }
        }

        private void refreshOpenFiles (File repository) {
            boolean refreshPlanned;
            synchronized (refreshedRepositories) {
                refreshPlanned = !refreshedRepositories.add(repository);
            }
            if (!refreshPlanned) {
                refreshOpenFilesTask.schedule(3000);
            }
        }

        private void enableEvents (File repository, boolean enabled) {
            File gitFolder = FileUtil.normalizeFile(GitUtils.getGitFolderForRoot(repository));
            synchronized (disabledEvents) {
                if (enabled) {
                    disabledEvents.remove(gitFolder);
                } else {
                    disabledEvents.add(gitFolder);
                }
            }
        }

        private boolean isEnabled (File gitFolder) {
            synchronized (disabledEvents) {
                return !disabledEvents.contains(gitFolder);
            }
        }

        private File translateToMetadataFolder (File gitFolder) {
            MetadataMapping mapping;
            synchronized(timestamps) {
                mapping = gitToMetadataFolder.get(gitFolder);
            }
            File metadataFolder;
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
                        br = new BufferedReader(new FileReader(gitFolder));
                        for (String line = br.readLine(); line != null; line = br.readLine()) {
                            line = line.trim();
                            if (line.startsWith("gitdir:")) { //NOI18N
                                line = line.substring(7).trim();
                                File tmp = new File(line);
                                if (!tmp.isAbsolute()) {
                                    tmp = new File(gitFolder, line).getCanonicalFile();
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
            return FileUtil.normalizeFile(metadataFolder);
        }

        private File translateToGitFolder (File metadataFolder) {
            File gitFolder;
            synchronized (timestamps) {
                gitFolder = metadataToGitFolder.get(metadataFolder);
            }
            if (gitFolder == null) {
                gitFolder = metadataFolder;
            }
            return gitFolder;
        }

        private boolean isMetadataFolder (File dir) {
            synchronized (timestamps) {
                return metadataToGitFolder.containsKey(dir);
            }
        }

        private File getMetadataForReferences (File file) {
            List<File> metadataFolders;
            synchronized (timestamps) {
                metadataFolders = new ArrayList<>(metadataToGitFolder.keySet());
            }
            File candidate = null;
            for (File metadataFolder : metadataFolders) {
                String refsPath = new File(metadataFolder.getAbsolutePath(), REFS_FILE_NAME).getAbsolutePath();
                if (file.getAbsolutePath().startsWith(refsPath)) {
                    if (candidate == null || candidate.getAbsolutePath().length() < metadataFolder.getAbsolutePath().length()) {
                        candidate = metadataFolder;
                    }
                }
            }
            return candidate;
        }
    }

    public class GitSearchHistorySupport extends SearchHistorySupport {
        public GitSearchHistorySupport(File file) {
            super(file);
        }
        @Override
        protected boolean searchHistoryImpl(final int line) throws IOException {
            File file = getFile();
            SearchHistoryAction.openSearch(Git.getInstance().getRepositoryRoot(file), file, file.getName(), line);
            return true;
        }

    }
}
