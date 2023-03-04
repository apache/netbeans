/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.git.ui.repository;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitRepositoryState;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitTag;
import org.netbeans.libs.git.SearchCriteria;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.git.utils.JGitUtils;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ondra
 */
public class RepositoryInfo {

    /**
     * fired when the active branch for the repository changed. Old and new values are instances of {@link GitBranch}.
     */
    public static final String PROPERTY_ACTIVE_BRANCH = "prop.activeBranch"; //NOI18N
    /**
     * fired when the HEAD changes, old and new values are instances of {@link GitBranch}.
     */
    public static final String PROPERTY_HEAD = "prop.head"; //NOI18N
    /**
     * fired when repository state changes, old and new values are instances of {@link GitRepositoryState}.
     */
    public static final String PROPERTY_STATE = "prop.state"; //NOI18N
    /**
     * fired when a set of known branches changes (a branch is added, removed, etc.). Old and new values are instances of {@link Map}&lt;String, GitBranch&gt;.
     */
    public static final String PROPERTY_BRANCHES = "prop.branches"; //NOI18N
    /**
     * fired when a set of known tags changes (a tag is added, removed, etc.). Old and new values are instances of {@link Map}&lt;String, GitTag&gt;.
     */
    public static final String PROPERTY_TAGS = "prop.tags"; //NOI18N
    /**
     * fired when a set of known remotes changes (a remote is added, removed, etc.). Old and new values are instances of {@link Map}&lt;String, GitRemoteConfig&gt;.
     */
    public static final String PROPERTY_REMOTES = "prop.remotes"; //NOI18N
    /**
     * fired when a git stash state changes. Old and new values are instances of {@link List}&lt;GitRevisionInfo&gt;.
     */
    public static final String PROPERTY_STASH = "prop.stashes"; //NOI18N
    /**
     * fired when the tracking info for the current branch changes.
     */
    public static final String PROPERTY_TRACKING_INFO = "prop.trackingInfo"; //NOI18N

    private final Reference<File> rootRef;
    private static final WeakHashMap<File, RepositoryInfo> cache = new WeakHashMap<File, RepositoryInfo>(5);
    private static final Logger LOG = Logger.getLogger(RepositoryInfo.class.getName());
    private static final RequestProcessor rp = new RequestProcessor("RepositoryInfo", 1, true); //NOI18N
    private static final RequestProcessor.Task refreshTask = rp.create(new RepositoryRefreshTask());
    private static final Set<RepositoryInfo> repositoriesToRefresh = new HashSet<RepositoryInfo>(2);
    private final PropertyChangeSupport propertyChangeSupport;
    private final Map<String, GitBranch> branches;
    private final Map<String, GitTag> tags;
    private final Map<String, GitRemoteConfig> remotes;
    private final List<GitRevisionInfo> stashes;

    private GitBranch activeBranch;
    private GitRepositoryState repositoryState;
    private final String name;
    private static final Set<String> logged = Collections.synchronizedSet(new HashSet<String>());
    private NBGitConfig nbConfig;
    private static final String NETBEANS_CONFIG_FILE = "nbconfig"; //NOI18N
    private PushMode pushMode = PushMode.ASK;
    private TrackingInfo trackingInfo;
    
    private RepositoryInfo (File root) {
        this.rootRef = new WeakReference<File>(root);
        this.name = root.getName();
        this.branches = new LinkedHashMap<String, GitBranch>();
        this.tags = new HashMap<String, GitTag>();
        this.remotes = new HashMap<String, GitRemoteConfig>();
        this.stashes = new ArrayList<>();
        this.activeBranch = GitBranch.NO_BRANCH_INSTANCE;
        this.repositoryState = GitRepositoryState.SAFE;
        this.trackingInfo = new TrackingInfo(activeBranch);
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Can be called from EDT, but if no info is yet available it will be
     * refreshed and initialized asynchronously
     * @param repositoryRoot existing repository root
     * @return null if repositoryRoot is not an existing git repository
     */
    public static RepositoryInfo getInstance (File repositoryRoot) {
        RepositoryInfo info = null;
        // this should return alwaus the same instance, so the cache can be implemented as a weak map.
        File repositoryRootSingleInstance = Git.getInstance().getRepositoryRoot(repositoryRoot);
        if (repositoryRoot.equals(repositoryRootSingleInstance)) {
            boolean refresh = false;
            synchronized (cache) {
                info = cache.get(repositoryRootSingleInstance);
                if (info == null) {
                    cache.put(repositoryRootSingleInstance, info = new RepositoryInfo(repositoryRootSingleInstance));
                    refresh = true;
                }
            }
            if (refresh) {
                if (java.awt.EventQueue.isDispatchThread()) {
                    LOG.log(Level.FINE, "getInstance (): had to schedule an async refresh for {0}", repositoryRoot); //NOI18N
                    refreshAsync(repositoryRoot);
                } else {
                    info.refresh();
                }
            }
        }
        return info;
    }

    private final ThreadLocal<List<PropertyChangeEvent>> eventsToFire = new ThreadLocal<>();
    
    /**
     * Do NOT call from EDT
     * @return
     */
    public void refresh () {
        assert !java.awt.EventQueue.isDispatchThread();
        File root = rootRef.get();
        GitClient client = null;
        try {
            if (root == null) {
                LOG.log(Level.WARNING, "refresh (): root is null, it has been collected in the meantime"); //NOI18N
            } else {
                LOG.log(Level.FINE, "refresh (): starting for {0}", root); //NOI18N
                try {
                    eventsToFire.set(new ArrayList<PropertyChangeEvent>());
                    NBGitConfig cfg = getNetbeansConfig(root);
                    client = Git.getInstance().getClient(root);
                    // get all needed information at once before firing events. Thus we supress repeated annotations' refreshing
                    Map<String, GitBranch> newBranches = client.getBranches(true, GitUtils.NULL_PROGRESS_MONITOR);
                    setBranches(newBranches);
                    cfg.removeDeletedBranches(newBranches);
                    Map<String, GitTag> newTags = client.getTags(GitUtils.NULL_PROGRESS_MONITOR, false);
                    setTags(newTags);
                    try {
                        refreshRemotes(client);
                    } catch (GitException ex) {
                        LOG.log(logged.add(root.getAbsolutePath() + ex.getMessage()) ? Level.INFO : Level.FINE, null, ex);
                    }
                    try {
                        refreshStashes(client);
                    } catch (GitException ex) {
                        LOG.log(logged.add(root.getAbsolutePath() + ex.getMessage()) ? Level.INFO : Level.FINE, null, ex);
                    }
                    GitRepositoryState newState = client.getRepositoryState(GitUtils.NULL_PROGRESS_MONITOR);
                    // now set new values and fire events when needed
                    setActiveBranch(newBranches);
                    setRepositoryState(newState);
                    updateTracking(client);
                } finally {
                    List<PropertyChangeEvent> events = eventsToFire.get();
                    for (PropertyChangeEvent e : events) {
                        propertyChangeSupport.firePropertyChange(e);
                    }
                    eventsToFire.remove();
                }
            }
        } catch (GitException ex) {
            Level level = root.exists() ? Level.INFO : Level.FINE; // do not polute the message log with messages concerning temporary or deleted repositories
            LOG.log(level, null, ex);
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }

    /**
     * Do NOT call from EDT
     * @return
     */
    public void refreshRemotes () throws GitException {
        assert !java.awt.EventQueue.isDispatchThread();
        GitClient client = null;
        try {
            File root = rootRef.get();
            if (root == null) {
                LOG.log(Level.WARNING, "refreshRemotes (): root is null, it has been collected in the meantime"); //NOI18N
            } else {
                LOG.log(Level.FINE, "refreshRemotes (): starting for {0}", root); //NOI18N
                client = Git.getInstance().getClient(root);
                refreshRemotes(client);
            }
        } finally {
            if (client != null) {
                client.release();
            }
        }
    }

    /**
     * Do NOT call from EDT
     * @return
     */
    public List<GitRevisionInfo> refreshStashes () throws GitException {
        assert !java.awt.EventQueue.isDispatchThread();
        GitClient client = null;
        try {
            File root = rootRef.get();
            if (root == null) {
                LOG.log(Level.WARNING, "refreshRemotes (): root is null, it has been collected in the meantime"); //NOI18N
            } else {
                LOG.log(Level.FINE, "refreshRemotes (): starting for {0}", root); //NOI18N
                client = Git.getInstance().getClient(root);
                refreshStashes(client);
            }
        } finally {
            if (client != null) {
                client.release();
            }
        }
        return new ArrayList<>(stashes);
    }

    public NBGitConfig getNetbeansConfig () {
        return getNetbeansConfig(rootRef.get());
    }

    private NBGitConfig getNetbeansConfig (File root) {
        if (root == null) {
            return null;
        }
        if (nbConfig == null) {
            nbConfig = new NBGitConfig(root);
        }
        if (!EventQueue.isDispatchThread()) {
            nbConfig.refresh();
        }
        return nbConfig;
    }
    
    private void setActiveBranch (Map<String, GitBranch> branches) throws GitException {
        for (Map.Entry<String, GitBranch> e : branches.entrySet()) {
            if (e.getValue().isActive()) {
                GitBranch oldActiveBranch = activeBranch;
                activeBranch = e.getValue();
                if (oldActiveBranch == null || !oldActiveBranch.getName().equals(activeBranch.getName())) {
                    LOG.log(Level.FINE, "active branch changed: {0} --- {1}", new Object[] { rootRef, activeBranch.getName() }); //NOI18N
                    firePropertyChange(new PropertyChangeEvent(this, PROPERTY_ACTIVE_BRANCH, oldActiveBranch, activeBranch));
                }
                if (oldActiveBranch == null || !oldActiveBranch.getId().equals(activeBranch.getId())) {
                    LOG.log(Level.FINE, "current HEAD changed: {0} --- {1}", new Object[] { rootRef, activeBranch.getId() }); //NOI18N
                    firePropertyChange(new PropertyChangeEvent(this, PROPERTY_HEAD, oldActiveBranch, activeBranch));
                }
            }
        }
    }

    private void setRepositoryState (GitRepositoryState repositoryState) {
        GitRepositoryState oldState = this.repositoryState;
        this.repositoryState = repositoryState;
        if (!repositoryState.equals(oldState)) {
            LOG.log(Level.FINE, "repository state changed: {0} --- {1}", new Object[] { oldState, repositoryState }); //NOI18N
            firePropertyChange(new PropertyChangeEvent(this, PROPERTY_STATE, oldState, repositoryState));
        }
    }

    private void setBranches (Map<String, GitBranch> newBranches) {
        Map<String, GitBranch> oldBranches;
        boolean changed = false;
        synchronized (branches) {
            oldBranches = new LinkedHashMap<String, GitBranch>(branches);
            branches.clear();
            branches.putAll(newBranches);
            changed = !equalsBranches(oldBranches, newBranches);
        }
        if (changed) {
            firePropertyChange(new PropertyChangeEvent(this, PROPERTY_BRANCHES, Collections.unmodifiableMap(oldBranches), Collections.unmodifiableMap(new HashMap<String, GitBranch>(newBranches))));
        }
    }

    private void setTags (Map<String, GitTag> newTags) {
        Map<String, GitTag> oldTags;
        boolean changed = false;
        synchronized (tags) {
            oldTags = new HashMap<String, GitTag>(tags);
            if (!equalsTags(oldTags, newTags)) {
                tags.clear();
                tags.putAll(newTags);
                changed = true;
            }
        }
        if (changed) {
            firePropertyChange(new PropertyChangeEvent(this, PROPERTY_TAGS, Collections.unmodifiableMap(oldTags), Collections.unmodifiableMap(new HashMap<String, GitTag>(newTags))));
        }
    }

    private void setRemotes (Map<String, GitRemoteConfig> newRemotes) {
        Map<String, GitRemoteConfig> oldRemotes;
        boolean changed = false;
        synchronized (remotes) {
            oldRemotes = new HashMap<String, GitRemoteConfig>(remotes);
            if (!equalsRemotes(oldRemotes, newRemotes)) {
                remotes.clear();
                remotes.putAll(newRemotes);
                changed = true;
            }
        }
        if (changed) {
            firePropertyChange(new PropertyChangeEvent(this, PROPERTY_REMOTES, Collections.unmodifiableMap(oldRemotes), Collections.unmodifiableMap(new HashMap<String, GitRemoteConfig>(newRemotes))));
        }
    }

    private void setStashes (List<GitRevisionInfo> newStashes) {
        List<GitRevisionInfo> oldStash;
        boolean changed = false;
        synchronized (stashes) {
            oldStash = new ArrayList<>(stashes);
            if (!equals(oldStash, newStashes)) {
                stashes.clear();
                stashes.addAll(newStashes);
                changed = true;
            }
        }
        if (changed) {
            firePropertyChange(new PropertyChangeEvent(this, PROPERTY_STASH, Collections.unmodifiableList(oldStash), Collections.unmodifiableList(new ArrayList<>(newStashes))));
        }
    }

    public void addPropertyChangeListener (PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * May be {@link GitBranch#NO_BRANCH_INSTANCE} if repository could not be initialized
     * @return 
     */
    public GitBranch getActiveBranch () {
        return activeBranch;
    }

    public GitRepositoryState getRepositoryState () {
        return repositoryState;
    }

    public String getName () {
        return name;
    }
    
    public Map<String, GitBranch> getBranches () {
        synchronized (branches) {
            return new LinkedHashMap<String, GitBranch>(branches);
        }
    }

    public Map<String, GitTag> getTags () {
        synchronized (tags) {
            return new HashMap<String, GitTag>(tags);
        }
    }

    public Map<String, GitRemoteConfig> getRemotes () {
        synchronized (remotes) {
            return new HashMap<String, GitRemoteConfig>(remotes);
        }
    }

    public List<GitRevisionInfo> getStashes () {
        synchronized (stashes) {
            return new ArrayList<>(stashes);
        }
    }

    public static void refreshAsync (File repositoryRoot) {
        RepositoryInfo info = null;
        synchronized (cache) {
            info = cache.get(repositoryRoot);
        }
        if (info != null) {
            info.refreshAsync();
        }
    }

    private void refreshAsync () {
        boolean start = false;
        synchronized (repositoriesToRefresh) {
            start = repositoriesToRefresh.add(this);
        }
        if (start) {
            LOG.log(Level.FINE, "Planning refresh for {0}", rootRef.get()); //NOI18N
            refreshTask.schedule(3000);
        }
    }

    private static boolean equalsRemotes (Map<String, GitRemoteConfig> oldRemotes, Map<String, GitRemoteConfig> newRemotes) {
        boolean retval = oldRemotes.size() == newRemotes.size() && oldRemotes.keySet().equals(newRemotes.keySet());
        if (retval) {
            for (Map.Entry<String, GitRemoteConfig> e : oldRemotes.entrySet()) {
                GitRemoteConfig oldRemote = e.getValue();
                GitRemoteConfig newRemote = newRemotes.get(e.getKey());
                if (!(oldRemote.getFetchRefSpecs().equals(newRemote.getFetchRefSpecs()) && oldRemote.getPushRefSpecs().equals(newRemote.getPushRefSpecs()) && 
                        oldRemote.getUris().equals(newRemote.getUris()) && oldRemote.getPushUris().equals(newRemote.getPushUris()))) {
                    retval = false;
                    break;
                }
            }
        }
        return retval;
    }

    private static boolean equals (List<GitRevisionInfo> oldList, List<GitRevisionInfo> newList) {
        boolean retval = oldList.size() == newList.size();
        if (retval) {
            for (ListIterator<GitRevisionInfo> itOld = oldList.listIterator(), itNew = newList.listIterator(); itOld.hasNext();) {
                GitRevisionInfo oldInfo = itOld.next();
                GitRevisionInfo newInfo = itNew.next();
                if (!oldInfo.getRevision().equals(newInfo.getRevision())) {
                    retval = false;
                    break;
                }
            }
        }
        return retval;
    }
    
    private static boolean equalsBranches (Map<String, GitBranch> oldBranches, Map<String, GitBranch> newBranches) {
        boolean retval = oldBranches.size() == newBranches.size() && oldBranches.keySet().equals(newBranches.keySet());
        if (retval) {
            for (Map.Entry<String, GitBranch> e : oldBranches.entrySet()) {
                GitBranch oldBranch = e.getValue();
                GitBranch newBranch = newBranches.get(e.getKey());
                if (!oldBranch.getId().equals(newBranch.getId())
                        || !equalTracking(newBranch, oldBranch)) {
                    retval = false;
                    break;
                }
            }
        }
        return retval;
    }

    private static boolean equalTracking (GitBranch newBranch, GitBranch oldBranch) {
        GitBranch tracked1 = newBranch.getTrackedBranch();
        GitBranch tracked2 = oldBranch.getTrackedBranch();
        boolean equal = tracked1 == tracked2;
        if (!equal) {
            equal = tracked1 != null && tracked2 != null
                    && tracked1.getName().equals(tracked2.getName())
                    && tracked1.getId().equals(tracked2.getId());
        }
        return equal;
    }

    private static boolean equalsTags (Map<String, GitTag> oldTags, Map<String, GitTag> newTags) {
        boolean retval = oldTags.size() == newTags.size() && oldTags.keySet().equals(newTags.keySet());
        if (retval) {
            for (Map.Entry<String, GitTag> e : oldTags.entrySet()) {
                GitTag oldTag = e.getValue();
                GitTag newTag = newTags.get(e.getKey());
                if (!(oldTag.getMessage().equals(newTag.getMessage())
                        && oldTag.getTagId().equals(newTag.getTagId())
                        && oldTag.getTagName().equals(newTag.getTagName())
                        && oldTag.getTaggedObjectId().equals(newTag.getTaggedObjectId())
                        && oldTag.getTaggedObjectType().equals(newTag.getTaggedObjectType())
                        && oldTag.getTagger().toString().equals(newTag.getTagger().toString()))) {
                    retval = false;
                    break;
                }
            }
        }
        return retval;
    }

    private void refreshRemotes (GitClient client) throws GitException {
        Map<String, GitRemoteConfig> newRemotes = client.getRemotes(GitUtils.NULL_PROGRESS_MONITOR);
        setRemotes(newRemotes);
    }

    private void refreshStashes (GitClient client) throws GitException {
        setStashes(Arrays.asList(client.stashList(GitUtils.NULL_PROGRESS_MONITOR)));
    }

    private boolean refreshIfNotLocked () {
        File root = rootRef.get();
        if (root != null && GitUtils.isRepositoryLocked(root)) {
            return false;
        } else {
            refresh();
            return true;
        }
    }

    private void firePropertyChange (PropertyChangeEvent event) {
        List<PropertyChangeEvent> events = eventsToFire.get();
        if (events != null) {
            events.add(event);
        } else {
            propertyChangeSupport.firePropertyChange(event);
        }
    }

    public TrackingInfo getTracking () {
        return trackingInfo;
    }
    
    public static enum PushMode {
        UPSTREAM,
        ASK
    }

    public PushMode getPushMode () {
        return getPushMode(rootRef.get());
    }

    private PushMode getPushMode (File root) {
        if (root == null) {
            return pushMode;
        }
        if (!EventQueue.isDispatchThread()) {
            pushMode = JGitUtils.getPushMode(root);
        }
        return pushMode;
    }

    private void updateTracking (GitClient client) {
        TrackingInfo old = getTracking();
        GitBranch currBranch = getActiveBranch();
        if (old.needsUpdate(currBranch)) {
            TrackingInfo newInfo;
            String id = currBranch.getId();
            GitBranch trackedBranch = currBranch.getTrackedBranch();
            if (trackedBranch == null || id.equals(trackedBranch.getId())) {
                newInfo = new TrackingInfo(currBranch);
            } else {
                try {
                    String trackedId = trackedBranch.getId();
                    GitRevisionInfo info = client.getCommonAncestor(new String[] { id, trackedId }, GitUtils.NULL_PROGRESS_MONITOR);
                    if (info == null) {
                        // no base???
                        newInfo = new TrackingInfo(currBranch);
                    } else {
                        SearchCriteria critIn = new SearchCriteria();
                        critIn.setRevisionFrom(info.getRevision());
                        critIn.setRevisionTo(trackedId);
                        SearchCriteria critOut = new SearchCriteria();
                        critOut.setRevisionFrom(info.getRevision());
                        critOut.setRevisionTo(id);
                        int revsIn, revsOut;
                        if (info.getRevision().equals(id)) {
                            // incoming
                            revsIn = client.log(critIn, false, GitUtils.NULL_PROGRESS_MONITOR).length - 1; // -1 is required because search returns also the from commit
                            revsOut = 0;
                        } else if (info.getRevision().equals(trackedId)) {
                            // outgoing
                            revsIn = 0;
                            revsOut = client.log(critOut, false, GitUtils.NULL_PROGRESS_MONITOR).length - 1; // -1 is required because search returns also the from commit
                        } else {
                            // merge
                            revsIn = client.log(critIn, false, GitUtils.NULL_PROGRESS_MONITOR).length - 1;
                            revsOut = client.log(critOut, false, GitUtils.NULL_PROGRESS_MONITOR).length - 1;
                        }
                        newInfo = new TrackingInfo(currBranch, revsIn, revsOut);
                    }
                } catch (GitException ex) {
                    LOG.log(Level.INFO, null, ex);
                    newInfo = new TrackingInfo(currBranch);
                }
            }
            trackingInfo = newInfo;
            firePropertyChange(new PropertyChangeEvent(this, PROPERTY_TRACKING_INFO, old, newInfo));
        }
    }

    private static class RepositoryRefreshTask implements Runnable {
        @Override
        public void run() {
            RepositoryInfo info;
            Set<RepositoryInfo> delayed = new HashSet<>();
            while ((info = getNextRepositoryInfo()) != null) {
                if (!info.refreshIfNotLocked()) {
                    LOG.log(Level.FINE, "RepositoryRefreshTask: Repository {0} locked, info refresh delayed", info.getName()); //NOI18N
                    delayed.add(info);
                }
            }
            for (RepositoryInfo toRefresh : delayed) {
                toRefresh.refreshAsync();
            }
        }

        private RepositoryInfo getNextRepositoryInfo () {
            RepositoryInfo info = null;
            synchronized (repositoriesToRefresh) {
                Iterator<RepositoryInfo> it = repositoriesToRefresh.iterator();
                if (it.hasNext()) {
                    info = it.next();
                    it.remove();
                }
            }
            return info;
        }
    }

    private static final String CONFIG_AUTO_SYNC = "autoSync"; //NOI18N
    
    public static class NBGitConfig {
        private final FileBasedConfig config;
        private final RequestProcessor.Task saveTask;
        private Set<String> cachedBranches = Collections.<String>emptySet();

        public NBGitConfig (File root) {
            this.config = new FileBasedConfig(new File(GitUtils.getGitFolderForRoot(root), NETBEANS_CONFIG_FILE), FS.DETECTED);
            saveTask = rp.create(new SaveTask());
        }

        public boolean getAutoSyncBranch (String branch) {
            return config.getBoolean(ConfigConstants.CONFIG_BRANCH_SECTION, branch, CONFIG_AUTO_SYNC, false);
        }

        public void setAutoSyncBranch (String branch, boolean autoSync) {
            boolean oldVal = getAutoSyncBranch(branch);
            if (oldVal != autoSync) {
                if (autoSync) {
                    config.setBoolean(ConfigConstants.CONFIG_BRANCH_SECTION, branch, CONFIG_AUTO_SYNC, autoSync);
                } else {
                    config.unset(ConfigConstants.CONFIG_BRANCH_SECTION, branch, CONFIG_AUTO_SYNC);
                    if (config.getNames(ConfigConstants.CONFIG_BRANCH_SECTION, branch).isEmpty()) {
                        config.unsetSection(ConfigConstants.CONFIG_BRANCH_SECTION, branch);
                    }
                }
                save();
            }
        }

        private void refresh () {
            try {
                if (config.isOutdated()) {
                    config.load();
                }
            } catch (IOException | ConfigInvalidException ex) {
                LOG.log(Level.INFO, null, ex);
            }
        }

        private void save () {
            saveTask.schedule(1000);
        }

        private void removeDeletedBranches (Map<String, GitBranch> branches) {
            Set<String> localBranches = new HashSet<>();
            for (Map.Entry<String, GitBranch> b : branches.entrySet()) {
                if (!b.getValue().isRemote() && !b.getKey().equals(GitBranch.NO_BRANCH)) {
                    localBranches.add(b.getKey());
                }
            }
            if (!localBranches.equals(cachedBranches)) {
                cachedBranches = localBranches;
                List<String> toRemove = new ArrayList<>(config.getSubsections(ConfigConstants.CONFIG_BRANCH_SECTION));
                boolean save = false;
                for (String branchName : toRemove) {
                    if (!localBranches.contains(branchName)) {
                        config.unsetSection(ConfigConstants.CONFIG_BRANCH_SECTION, branchName);
                        save = true;
                    }
                }
                if (save) {
                    save();
                }
            }
        }
        
        private class SaveTask implements Runnable {

            @Override
            public void run () {
                try {
                    config.save();
                } catch (IOException ex) {
                    LOG.log(Level.INFO, null, ex);
                }
            }
            
        }
    }
    
    @NbBundle.Messages({
        "CTL_TrackingInfo.inSync=in sync with upstream branch",
        "# {0} - number of outgoing commits",
        "CTL_TrackingInfo.out={0} outgoing {0,choice,1#commit|1<commits}",
        "# {0} - number of incoming commits",
        "CTL_TrackingInfo.in={0} incoming {0,choice,1#commit|1<commits}",
        "# {0} - number of outgoing commits", "# {1} - number of incoming commits",
        "CTL_TrackingInfo.inout={0} outgoing, {1} incoming {1,choice,1#commit|1<commits}"
    })
    public static class TrackingInfo {
        
        private final String currentId;
        private final String trackedId;
        private final int commitsIn;
        private final int commitsOut;
        
        private TrackingInfo (GitBranch branch) {
            currentId = branch.getId();
            GitBranch tracked = branch.getTrackedBranch();
            trackedId = tracked == null ? "" : tracked.getId();
            if (currentId.equals(trackedId)) {
                commitsIn = 0;
                commitsOut = 0;
            } else {
                commitsIn = -1;
                commitsOut = -1;
            }
        }

        private TrackingInfo (GitBranch branch, int revsIn, int revsOut) {
            currentId = branch.getId();
            trackedId = branch.getTrackedBranch().getId();
            commitsIn = revsIn;
            commitsOut = revsOut;
        }

        private boolean needsUpdate (GitBranch branch) {
            boolean retval = false;
            GitBranch tracked = branch.getTrackedBranch();
            if (!currentId.equals(branch.getId())) {
                retval = true;
            } else if (!trackedId.equals(tracked == null ? "" : tracked.getId())) {
                retval = true;
            }
            return retval;
        }

        public int getIncomingCommits () {
            return commitsIn;
        }

        public int getOutgoingCommits () {
            return commitsOut;
        }

        public boolean isKnown () {
            return commitsIn >= 0;
        }
        
        public String getDescription () {
            if (commitsIn == 0 && commitsOut == 0) {
                return Bundle.CTL_TrackingInfo_inSync();
            } else if (commitsOut == 0) {
                return Bundle.CTL_TrackingInfo_in(commitsIn);
            } else if (commitsIn == 0) {
                return Bundle.CTL_TrackingInfo_out(commitsOut);
            } else {
                return Bundle.CTL_TrackingInfo_inout(commitsOut, commitsIn);
            }
        }

    }
}
