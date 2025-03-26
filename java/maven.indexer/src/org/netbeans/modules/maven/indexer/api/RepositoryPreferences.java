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
/*
 * Contributor(s): theanuradha@netbeans.org
 */

package org.netbeans.modules.maven.indexer.api;

import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Settings;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.eclipse.aether.repository.MirrorSelector;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.DefaultMirrorSelector;
import org.openide.util.NbBundle;

/**
 * List of Maven repositories of interest.
 */
public final class RepositoryPreferences {

    private static final Logger LOG = Logger.getLogger(RepositoryPreferences.class.getName());

    private static RepositoryPreferences instance;

    private static final String KEY_DISPLAY_NAME = "name";//NOI18N
    private static final String KEY_PATH = "path";//NOI18N
    private static final String KEY_INDEX_URL = "index";//NOI18N
    private static final String KEY_REPO_URL = "url";//NOI18N
    /*index settings */
    public static final String PROP_INDEX_FREQ = "indexUpdateFrequency"; //NOI18N
    public static final String PROP_INDEX = "createIndex"; //NOI18N
    public static final String PROP_DOWNLOAD_INDEX = "downloadIndex"; //NOI18N
    public static final String PROP_LAST_INDEX_UPDATE = "lastIndexUpdate"; //NOI18N
    private static final String PROP_INDEX_DOWNLOAD_PERMISSIONS = "indexDownloadPermissions"; //NOI18N
    public static final String PROP_MT_INDEX_EXTRACTION = "indexMultiThreadedExtraction"; //NOI18N
    public static final String PROP_INDEX_DATE_CUTOFF_FILTER = "indexDateCotoffFilter"; //NOI18N
    private static final String ALT_CENTRAL_URL = "https://repo1.maven.org/maven2"; //NOI18N

    public static final int FREQ_ONCE_WEEK = 0;
    public static final int FREQ_ONCE_DAY = 1;
    public static final int FREQ_STARTUP = 2;
    private static volatile Instant indexDownloadPauseEnd = Instant.ofEpochMilli(0);
    private final Map<String,RepositoryInfo> infoCache = new HashMap<>();
    private final Map<Object,List<RepositoryInfo>> transients = new LinkedHashMap<>();
    private RepositoryInfo local;
    private final RepositoryInfo central;
    private final ChangeSupport cs = new ChangeSupport(this);
    
    private RepositoryPreferences() {
        try {
            central = new RepositoryInfo(RepositorySystem.DEFAULT_REMOTE_REPO_ID, /* XXX pull display name from superpom? */RepositorySystem.DEFAULT_REMOTE_REPO_ID, null, RepositorySystem.DEFAULT_REMOTE_REPO_URL);
            //this repository can be mirrored
            central.setMirrorStrategy(RepositoryInfo.MirrorStrategy.ALL);

        } catch (URISyntaxException x) {
            throw new AssertionError(x);
        }
    }

    private static Preferences getPreferences() {
        return NbPreferences.root().node("org/netbeans/modules/maven/nexus/indexing"); //NOI18N
    }

    private static Preferences storage() {
        return NbPreferences.root().node("org/netbeans/modules/maven/repositories");
    }

    public static synchronized RepositoryPreferences getInstance() {
        if (instance == null) {
            instance = new RepositoryPreferences();
        }
        return instance;
    }

    private static @CheckForNull RepositoryInfo createRepositoryInfo(Preferences p) throws URISyntaxException {
        String id = p.name();
        String name = p.get(KEY_DISPLAY_NAME, null);
        if (name == null) {
            return null;
        }
        String path = p.get(KEY_PATH, null);
        String repourl = p.get(KEY_REPO_URL, null);
        String indexurl = p.get(KEY_INDEX_URL, null);
        RepositoryInfo repo = new RepositoryInfo(id, name, path, repourl, indexurl);
        //repository infos from preferences cannot be wildcard mirrored.
        repo.setMirrorStrategy(RepositoryInfo.MirrorStrategy.NON_WILDCARD);
        return repo;
    }

    /** @since 2.2 */
    @Messages("local=Local")
    public @NonNull synchronized RepositoryInfo getLocalRepository() {
        if (local == null) {
            try {
                //TODO do we care about changing the instance when localrepo location changes?
                local = new RepositoryInfo(RepositorySystem.DEFAULT_LOCAL_REPO_ID, Bundle.local(), EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile().getAbsolutePath(), null);
                local.setMirrorStrategy(RepositoryInfo.MirrorStrategy.NONE);
            } catch (URISyntaxException x) {
                throw new AssertionError(x);
            }
        }
        return local;
    }

    /**
     * returns the RepositoryInfo object with the given id or a mirror repository info
     * that mirrors the given id.
     * @param id
     * @return 
     */
    public @CheckForNull RepositoryInfo getRepositoryInfoById(String id) {
        List<RepositoryInfo> infos = getRepositoryInfos();
        //repository infos are now including mirrors
        //first check if the repository itself in the list has the id
        for (RepositoryInfo ri : infos) {
            if (ri.getId().equals(id)) {
                return ri;
            }
        }
        //if not, then try checking the mirrored repos..
        for (RepositoryInfo ri : infos) {
            if (ri.isMirror()) {
                for (RepositoryInfo rii : ri.getMirroredRepositories()) {
                    if (rii.getId().equals(id)) {
                        return ri;
                    }
                }
            }
        }
        return null;
    }

    public List<RepositoryInfo> getRepositoryInfos() {
        List<RepositoryInfo> toRet = new ArrayList<>();
        toRet.add(getLocalRepository());
        Set<String> ids = new HashSet<>();
        ids.add(RepositorySystem.DEFAULT_LOCAL_REPO_ID);
        Set<String> urls = new HashSet<>();
        synchronized (infoCache) {
            Preferences storage = storage();
            try {
                Set<String> gone = new HashSet<>(infoCache.keySet());
                for (String c : storage.childrenNames()) {
                    RepositoryInfo ri = infoCache.get(c);
                    if (ri == null) {
                        Preferences child = storage.node(c);
                        try {
                            ri = createRepositoryInfo(child);
                            if (ri == null) {
                                continue;
                            }
                            infoCache.put(c, ri);
                        } catch (/*IllegalArgument,URISyntax*/Exception x) {
                            LOG.log(Level.INFO, c, x);
                            try {
                                child.removeNode();
                            } catch (BackingStoreException x2) {
                                LOG.log(Level.INFO, null, x2);
                            }
                            continue;
                        }
                    }
                    toRet.add(ri);
                    gone.remove(c);
                    ids.add(ri.getId());
                    urls.add(ri.getRepositoryUrl());
                }
                
                infoCache.keySet().removeAll(gone);
            } catch (BackingStoreException x) {
                LOG.log(Level.INFO, null, x);
            }
            if (transients.isEmpty()) {
                if (ids.add(central.getId()) && urls.add(central.getRepositoryUrl())) {
                    toRet.add(central);
                }
            } else {
                for (List<RepositoryInfo> infos : transients.values()) {
                    for (RepositoryInfo info : infos) {
                        if (ids.add(info.getId()) && urls.add(info.getRepositoryUrl())) {
                            toRet.add(info);
                        }
                    }
                }
            }
        }
        MavenEmbedder embedder2 = EmbedderFactory.getOnlineEmbedder();
        DefaultMirrorSelector selectorWithGroups = new DefaultMirrorSelector();
        DefaultMirrorSelector selectorWithoutGroups = new DefaultMirrorSelector();
        final Settings settings = embedder2.getSettings();
        for (Mirror mirror : settings.getMirrors()) {
            String mirrorOf = mirror.getMirrorOf();
            selectorWithGroups.add(mirror.getId(), mirror.getUrl(), mirror.getLayout(), false, false, mirrorOf, mirror.getMirrorOfLayouts());
            if (!mirrorOf.contains("*")) {
                selectorWithoutGroups.add(mirror.getId(), mirror.getUrl(), mirror.getLayout(), false, false, mirrorOf, mirror.getMirrorOfLayouts());
            }
        }

        List<RepositoryInfo> semiTreed = new ArrayList<>();
        for (RepositoryInfo in : toRet) {
            if (in.getMirrorStrategy() == RepositoryInfo.MirrorStrategy.ALL || in.getMirrorStrategy() == RepositoryInfo.MirrorStrategy.NON_WILDCARD) {
                RepositoryInfo processed = getMirrorInfo(in, in.getMirrorStrategy() == RepositoryInfo.MirrorStrategy.ALL ? selectorWithGroups : selectorWithoutGroups, settings);
                boolean isMirror = true;
                if (processed == null) {
                    isMirror = false;
                    processed = in;
                }
                int index = semiTreed.indexOf(processed);
                if (index > -1) {
                    processed = semiTreed.get(index);
                } else {
                    semiTreed.add(processed);
                }
                if (isMirror) {
                    processed.addMirrorOfRepository(in);
                }
            } else {
                semiTreed.add(in);
            }
        }
        return semiTreed;
    }
    
    /**
     * if the repository has a mirror, then create a repositoryinfo object for it..
     */
    
    private RepositoryInfo getMirrorInfo(RepositoryInfo info, MirrorSelector selector, Settings settings) {
        RemoteRepository original = new RemoteRepository.Builder(info.getId(), /* XXX do we even support any other layout?*/"default", info.getRepositoryUrl()).build();
        RemoteRepository mirror = selector.getMirror(original);
        if (mirror != null) {
            try {
                String name = mirror.getId();
                //#213078 need to lookup name for mirror
                for (Mirror m : settings.getMirrors()) {
                    if (m.getId() != null && m.getId().equals(mirror.getId())) {
                        name = m.getName();
                        break;
                    }
                }
                RepositoryInfo toret = new RepositoryInfo(mirror.getId(), name, null, mirror.getUrl());
                toret.setMirrorStrategy(RepositoryInfo.MirrorStrategy.NONE);
                return toret;
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    public void addOrModifyRepositoryInfo(RepositoryInfo info) {
        String id = info.getId();
        synchronized (infoCache) {
            infoCache.put(id, info);
            Preferences p = storage().node(id);
            p.put(KEY_DISPLAY_NAME, info.getName());
            put(p, KEY_PATH, info.getRepositoryPath());
            put(p, KEY_REPO_URL, info.getRepositoryUrl());
            if (info.getRepositoryUrl() != null) {
                put(p, KEY_INDEX_URL, info.getIndexUpdateUrl().equals(info.getRepositoryUrl() + RepositoryInfo.DEFAULT_INDEX_SUFFIX) ? null : info.getIndexUpdateUrl());
            }
        }
        cs.fireChange();
    }
    private static void put(@NonNull Preferences p, @NonNull String key, @NullAllowed String value) {
        if (value != null) {
            p.put(key, value);
        } else {
            p.remove(key);
        }
    }

    /**
     * Checks whether a given repository is persisted.
     * @param id the repository's ID
     * @return true if it is persistent (custom), false if it is the local repository or was added transiently
     * @since 2.1
     */
    public boolean isPersistent(String id) {
        return storage().node(id).get(KEY_DISPLAY_NAME, null) != null;
    }
    
    public void removeRepositoryInfo(RepositoryInfo info) {
        synchronized (infoCache) {
            String id = info.getId();
            infoCache.remove(id);
            try {
                storage().node(id).removeNode();
            } catch (BackingStoreException x) {
                LOG.log(Level.INFO, null, x);
            }
        }
        cs.fireChange();
    }

    public static void setIndexUpdateFrequency(int fr) {
        getPreferences().putInt(PROP_INDEX_FREQ, fr);
    }

    public static int getIndexUpdateFrequency() {
        int freq = getPreferences().getInt(PROP_INDEX_FREQ, getDefaultIndexUpdateFrequency());
        return freq > 2 ? FREQ_ONCE_WEEK : freq; // reset if out of bounds
    }

    @NbBundle.Messages({
        "# FREQ_ONCE_WEEK = 0, FREQ_ONCE_DAY = 1, FREQ_STARTUP = 2;",
        "DEFAULT_UPDATE_FREQ=0"
    })
    static int getDefaultIndexUpdateFrequency() throws NumberFormatException {
        return Integer.parseInt(Bundle.DEFAULT_UPDATE_FREQ());
    }

    /**
     * @since 2.27
     * @param bool 
     */
    public static void setIndexRepositories(boolean bool) {
        getPreferences().putBoolean(PROP_INDEX, bool);
    }

    /**
     * @since 2.27
     * @return 
     */
    public static boolean isIndexRepositories() {
        return getPreferences().getBoolean(PROP_INDEX, getDefaultIndexRepositories());
    }

    @NbBundle.Messages({
        "# true or false:",
        "DEFAULT_CREATE_INDEX=true"
    })
    static boolean getDefaultIndexRepositories() {
        return Boolean.parseBoolean(Bundle.DEFAULT_CREATE_INDEX());
    }

    /**
     * @since 2.60
     */
    public static void setIndexDownloadEnabled(boolean enabled) {
        getPreferences().putBoolean(PROP_DOWNLOAD_INDEX, enabled);
    }

    /**
     * @since 2.60
     */
    public static boolean isIndexDownloadEnabled() {
        return getPreferences().getBoolean(PROP_DOWNLOAD_INDEX, getDefaultDownloadIndexEnabled());
    }

    public static void setMultiThreadedIndexExtractionEnabled(boolean enabled) {
        getPreferences().putBoolean(PROP_MT_INDEX_EXTRACTION, enabled);
    }

    public static boolean isMultiThreadedIndexExtractionEnabled() {
        return getPreferences().getBoolean(PROP_MT_INDEX_EXTRACTION, getDefaultMultiThreadedIndexExtraction());
    }

    @NbBundle.Messages({
        "# true or false:",
        "DEFAULT_MULTI_THREADED_EXTRACTION=false"
    })
    static boolean getDefaultMultiThreadedIndexExtraction() {
        return Boolean.parseBoolean(Bundle.DEFAULT_MULTI_THREADED_EXTRACTION());
    }

    /**
     * Downloading the remote index should only happen if indexing in general
     * and downloading in particular are enabled.
     *
     * @since 2.60
     * @return true if indexing and downloading are both enabled.
     */
    public static synchronized boolean isIndexDownloadEnabledEffective() {
        return isIndexRepositories() && isIndexDownloadEnabled() && !isIndexDownloadPaused();
    }

    public static int getIndexDateCutoffFilter() {
        return getPreferences().getInt(PROP_INDEX_DATE_CUTOFF_FILTER, 0);
    }
    
    public static void setIndexDateCutoffFilter(int years) {
        getPreferences().putInt(PROP_INDEX_DATE_CUTOFF_FILTER, years);
    }

    public static boolean isIndexDownloadPaused() {
        return Instant.now().isBefore(indexDownloadPauseEnd);
    }

    public static void continueIndexDownloads() {
        indexDownloadPauseEnd = Instant.ofEpochMilli(0);
    }

    public static void pauseIndexDownloadsFor(long duration, ChronoUnit unit) {
        indexDownloadPauseEnd = Instant.now().plus(duration, unit);
    }
    
    // cache; lock free read, persistance write-through on write.
    private static final Map<String, Boolean> permissions = new ConcurrentHashMap<>();

    static {
        readIndexDownloadPreferences(permissions);
    }
    
    public static boolean isIndexDownloadAllowedFor(RepositoryInfo repo) {
        return permissions.getOrDefault(repo.getRepositoryUrl(), false);
    }
    
    public static boolean isIndexDownloadDeniedFor(RepositoryInfo repo) {
        return !permissions.getOrDefault(repo.getRepositoryUrl(), true);
    }

    public synchronized static void allowIndexDownloadFor(RepositoryInfo repo) {
        if (permissions.put(repo.getRepositoryUrl(), true) != Boolean.TRUE) {
            writeIndexDownloadPreferences(permissions);
        }
    }

    public synchronized static void denyIndexDownloadFor(RepositoryInfo repo) {
        if (permissions.put(repo.getRepositoryUrl(), false) != Boolean.FALSE) {
            writeIndexDownloadPreferences(permissions);
        }
    }
    
    public synchronized static Map<String, Boolean> getIndexDownloadPermissions() {
        return new HashMap<>(permissions);
    }
    
    public synchronized static void setIndexDownloadPermissions(Map<String, Boolean> newPermissions) {
        permissions.clear();
        permissions.putAll(newPermissions);
        writeIndexDownloadPreferences(newPermissions);
    }

    // format: url|boolean;url|boolean;...
    private static void writeIndexDownloadPreferences(Map<String, Boolean> permissions) {
        getPreferences().put(PROP_INDEX_DOWNLOAD_PERMISSIONS, permissions.entrySet().stream()
                .map(e -> e.getKey()+"|"+e.getValue()).sorted().collect(Collectors.joining(";")));
    }

    private static void readIndexDownloadPreferences(Map<String, Boolean> permissions) {
        for (String entry : getPreferences().get(PROP_INDEX_DOWNLOAD_PERMISSIONS, "").split(";")) {
            if (entry.contains("|")) {
                String[] perm = entry.split("\\|");
                permissions.put(perm[0], Boolean.valueOf(perm[1]));
            }
        }
    }

    @NbBundle.Messages({
        "# true or false:",
        "DEFAULT_DOWNLOAD_INDEX=true"
    })
    static boolean getDefaultDownloadIndexEnabled() {
        return Boolean.parseBoolean(Bundle.DEFAULT_DOWNLOAD_INDEX());
    }

    public static Date getLastIndexUpdate(String repoId) {
        if(repoId.contains("//")) {
            repoId = repoId.replace("//", "_");
            LOG.log(Level.FINE, "Getting last index update: Repo''s id contains consecutive slashes, replacing them with '_': {0}", repoId);
        }
        long old = getPreferences().getLong(PROP_LAST_INDEX_UPDATE + "." + repoId, 0); // compatibility
        if (old != 0) { // upgrade it
            getPreferences().remove(PROP_LAST_INDEX_UPDATE + "." + repoId);
            storage().node(repoId).putLong(PROP_LAST_INDEX_UPDATE, old);
        }
        return new Date(storage().node(repoId).getLong(PROP_LAST_INDEX_UPDATE, 0));
    }

    public static void setLastIndexUpdate(String repoId,Date date) {
        if(repoId.contains("//")) {
            repoId = repoId.replace("//", "_");
            LOG.log(Level.FINE, "Setting last index update: Repo''s id contains consecutive slashes, replacing them with '_': {0}", repoId);
        }
        getPreferences().remove(PROP_LAST_INDEX_UPDATE + "." + repoId);
        storage().node(repoId).putLong(PROP_LAST_INDEX_UPDATE, date.getTime());
    }

    /**
     * Register a transient repository, the effective url actually used depends on maven settings for mirrors.
     * Its definition will not be persisted.
     * Repositories whose ID or URL duplicate that of a persistent repository,
     * or previously registered transient repository, will be ignored
     * (unless and until that repository is removed).
     * @param key an arbitrary key for use with {@link #removeTransientRepositories}
     * @param id the repository ID
     * @param displayName a display name (may just be {@code id})
     * @param url the remote URL (prefer the canonical public URL to that of a mirror)
     * @param strategy how is the url parameter processed by local maven mirror settings
     * @throws URISyntaxException in case the URL is malformed
     * @since 2.11
     */
    public void addTransientRepository(Object key, String id, String displayName, String url, RepositoryInfo.MirrorStrategy strategy) throws URISyntaxException {
        if(id.contains("//")) {
            id = id.replace("//", "_");
            LOG.log(Level.FINE, "Adding transient repository: Repo''s id contains consecutive slashes, replacing them with '_': {0}", id);
        }
        if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("file:")) {
            //only register repositories we can safely handle.. #227322
            return;
        }
        if (url.equals(ALT_CENTRAL_URL) || url.equals(ALT_CENTRAL_URL+"/")) {
            // map to primary URL to avoid duplicates
            url = RepositorySystem.DEFAULT_REMOTE_REPO_URL;
        }
        synchronized (infoCache) {
            List<RepositoryInfo> infos = transients.computeIfAbsent(key, k -> new ArrayList<>());
            RepositoryInfo info = new RepositoryInfo(id, displayName, null, url);
            info.setMirrorStrategy(strategy);
            infos.add(info);
        }
        cs.fireChange();
    }

    

    /**
     * Remote all transient repositories associated with a given ID.
     * @param key a key as with {@link #addTransientRepository}
     * @since 2.1
     */
    public void removeTransientRepositories(Object key) {
        synchronized (infoCache) {
            transients.remove(key);
        }
        cs.fireChange();
    }

    /**
     * @since 2.1
     */
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    /**
     * @since 2.1
     */
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    /**
     * Produces a list of remote repositories.
     * @see MavenEmbedder#resolve
     * @see ProjectBuildingRequest#setRemoteRepositories
     * @since 2.12
     */
    public List<ArtifactRepository> remoteRepositories(MavenEmbedder embedder) {
        List<ArtifactRepository> remotes = new ArrayList<>();
        for (RepositoryInfo info : getRepositoryInfos()) {
            // XXX should there be a String preferredId parameter to limit the remote repositories used in case we have a "reference" ID somehow?
            if (!info.isLocal()) {
                remotes.add(embedder.createRemoteRepository(info.getRepositoryUrl(), info.getId()));
            }
            // XXX do we care to handle mirrors specially?
        }
        return remotes;
    }

}
