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

package org.netbeans.modules.maven.indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.codehaus.plexus.PlexusConstants;
import org.apache.lucene.search.*;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.index.*;
import org.apache.maven.index.Scanner;
import org.apache.maven.index.artifact.ArtifactPackagingMapper;
import org.apache.maven.index.context.DefaultIndexingContext;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexUtils;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.creator.JarFileContentsIndexCreator;
import org.apache.maven.index.creator.MavenArchetypeArtifactInfoIndexCreator;
import org.apache.maven.index.creator.MavenPluginArtifactInfoIndexCreator;
import org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdateResult;
import org.apache.maven.index.updater.IndexUpdater;
import org.apache.maven.index.updater.ResourceFetcher;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.providers.http.HttpWagon;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.spi.ArchetypeQueries;
import org.netbeans.modules.maven.indexer.spi.BaseQueries;
import org.netbeans.modules.maven.indexer.spi.ChecksumQueries;
import org.netbeans.modules.maven.indexer.spi.ClassUsageQuery;
import org.netbeans.modules.maven.indexer.spi.ClassesQuery;
import org.netbeans.modules.maven.indexer.spi.ContextLoadedQuery;
import org.netbeans.modules.maven.indexer.spi.DependencyInfoQueries;
import org.netbeans.modules.maven.indexer.spi.GenericFindQuery;
import org.netbeans.modules.maven.indexer.spi.impl.RepositoryIndexerImplementation;
import org.netbeans.modules.maven.indexer.spi.impl.IndexingNotificationProvider;
import org.openide.modules.Places;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.util.NbBundle.Messages;
import org.netbeans.modules.maven.indexer.spi.RepositoryIndexQueryProvider;

import static org.apache.maven.index.creator.MinimalArtifactInfoIndexCreator.FLD_LAST_MODIFIED;


//index fields
//https://maven.apache.org/maven-indexer-archives/maven-indexer-LATEST/indexer-core/

@ServiceProviders({
    @ServiceProvider(service=RepositoryIndexerImplementation.class),
    @ServiceProvider(service=RepositoryIndexQueryProvider.class, position = Integer.MAX_VALUE)
})
public final class NexusRepositoryIndexManager implements RepositoryIndexerImplementation, RepositoryIndexQueryProvider {

    private static final Logger LOGGER = Logger.getLogger(NexusRepositoryIndexManager.class.getName());

    private static final String GROUP_CACHE_ALL_PREFIX = "nb-groupcache-all-v1"; // NOI18N
    private static final String GROUP_CACHE_ALL_SUFFIX = "txt"; // NOI18N
    private static final String GROUP_CACHE_ROOT_PREFIX = "nb-groupcache-root-v1"; // NOI18N
    private static final String GROUP_CACHE_ROOT_SUFFIX = "txt"; // NOI18N

    private PlexusContainer embedder;
    private Indexer indexer;
    private org.apache.maven.index.Scanner scanner;
    private SearchEngine searcher;
    private IndexUpdater remoteIndexUpdater;
    private ArtifactContextProducer contextProducer;
    private final Map<String, IndexingContext> indexingContexts = new ConcurrentHashMap<>();
    
    private boolean inited = false;
    /**
     * any reads, writes from/to index shall be done under mutex access.
     */
    private static final HashMap<String,Mutex> repoMutexMap = new HashMap<>(4);

    private static final Set<Mutex> indexingMutexes = new HashSet<>();

    /**
     * For local IO heavy repo indexing tasks and everything what does not involve downloads.
     */
    private static final RequestProcessor RP_LOCAL = new RequestProcessor("maven-local-indexing");

    /**
     * For remote repo download and indexing tasks.
     */
    private static final RequestProcessor RP_REMOTE = new RequestProcessor("maven-remote-indexing");

    private final NexusRepositoryQueries queries;
    
    static final int MAX_RESULT_COUNT = 1024;
    static final int NO_CAP_RESULT_COUNT = AbstractSearchRequest.UNDEFINED;

    @SuppressWarnings("this-escape")
    public NexusRepositoryIndexManager() {
        this.queries = new NexusRepositoryQueries(this);
    }

    @Override
    public boolean handlesRepository(RepositoryInfo repo) {
        // should always come as last when looked up
        // handles all remote repos
        return true;
    }
    
    @Override
    public BaseQueries getBaseQueries() {
        return queries;
    }

    @Override
    public ChecksumQueries getChecksumQueries() {
        return queries;
    }

    @Override
    public ArchetypeQueries getArchetypeQueries() {
        return queries;
    }

    @Override
    public DependencyInfoQueries getDependencyInfoQueries() {
        return queries;
    }

    @Override
    public ClassesQuery getClassesQuery() {
        return queries;
    }

    @Override
    public ClassUsageQuery getClassUsageQuery() {
        return queries;
    }

    @Override
    public GenericFindQuery getGenericFindQuery() {
        return queries;
    }

    @Override
    public ContextLoadedQuery getContextLoadedQuery() {
        return queries;
    }
    
    static Mutex getRepoMutex(RepositoryInfo repo) {
        return getRepoMutex(repo.getId());
    }
    
    static Mutex getRepoMutex(String repoId) {
        synchronized (repoMutexMap) {
            return repoMutexMap.computeIfAbsent(repoId, k -> new Mutex());
        }
    }

    private void initIndexer () {
        if (!inited) {
            try {
                ContainerConfiguration config = new DefaultContainerConfiguration();
                //#154755 - start
                ClassLoader indexerLoader = NexusRepositoryIndexManager.class.getClassLoader();
                ClassWorld classWorld = new ClassWorld();
                ClassRealm plexusRealm = classWorld.newRealm("plexus.core", EmbedderFactory.class.getClassLoader()); //NOI18N
                plexusRealm.importFrom(indexerLoader, "META-INF/sisu"); //NOI18N
                plexusRealm.importFrom(indexerLoader, "org.apache.maven.index"); //NOI18N
                plexusRealm.importFrom(indexerLoader, "org.netbeans.modules.maven.indexer"); //NOI18N
                config.setClassWorld(classWorld);
                config.setClassPathScanning( PlexusConstants.SCANNING_INDEX );
                //#154755 - end
                embedder = new DefaultPlexusContainer(config);

                ComponentDescriptor<ArtifactContextProducer> desc = new ComponentDescriptor<>();
                desc.setRoleClass(ArtifactContextProducer.class);
                desc.setImplementationClass(CustomArtifactContextProducer.class);
                ComponentRequirement req = new ComponentRequirement(); // XXX why is this not automatic?
                req.setFieldName("mapper");
                req.setRole(ArtifactPackagingMapper.class.getName());
                desc.addRequirement(req);
                embedder.addComponentDescriptor(desc);
                indexer = embedder.lookup(Indexer.class);
                searcher = embedder.lookup(SearchEngine.class);
                remoteIndexUpdater = embedder.lookup(IndexUpdater.class);
                contextProducer = embedder.lookup(ArtifactContextProducer.class);
                scanner = new FastScanner(contextProducer);
                inited = true;
            } catch (Exception x) {
                Exceptions.printStackTrace(x);
            }
        }
    }
    
    public Map<String, IndexingContext> getIndexingContexts() {
        return Collections.unmodifiableMap(indexingContexts);
    }
    
    //TODO try to experiment with the non-forced version of the context addition
    private IndexingContext addIndexingContextForced(
            RepositoryInfo repo,
            List<? extends IndexCreator> indexers
    ) throws IOException {

        IndexingContext context = indexer.createIndexingContext(
                /* id */ repo.getId(),
                /* repositoryId */ repo.getId(),
                /* repository */ repo.isLocal() ? new File(repo.getRepositoryPath()) : null,
                /* indexDirectory */ getIndexDirectory(repo).toFile(),
                /* repositoryUrl */ repo.isRemoteDownloadable() ? repo.getRepositoryUrl() : null,
                /* indexUpdateUrl */ repo.isRemoteDownloadable() ? repo.getIndexUpdateUrl() : null,
                /* searchable */ true,
                /* reclaim */ true,
                /* indexers */ indexers);

        // The allGroups and rootGroups properties of the IndexingContext are
        // not persistent anymore, so need to be saved outside the context
        try {
            context.setAllGroups(Files.readAllLines(getAllGroupCacheFile(repo)));
            context.setRootGroups(Files.readAllLines(getRootGroupCacheFile(repo)));
        } catch (IOException ex) {
            // At least one of the group caches could not be loaded, so rebuild it
            rebuildGroupCache(repo, context);
        }
        indexingContexts.put(context.getId(), context);
        return context;
    }

    public void removeIndexingContext(IndexingContext context, boolean deleteFiles) throws IOException {
        if (indexingContexts.remove(context.getId()) != null ) {
            indexer.closeIndexingContext( context, deleteFiles );
        }
    }

    boolean loadIndexingContext(final RepositoryInfo info) throws IOException {
        LOAD: {
            assert getRepoMutex(info).isWriteAccess();
            initIndexer();

            IndexingContext context = getIndexingContexts().get(info.getId());
            String indexUpdateUrl = info.getIndexUpdateUrl();
            if (context != null) {
                String contexturl = context.getIndexUpdateUrl();
                File contextfile = context.getRepository();
                File repofile = info.getRepositoryPath() != null ? new File(info.getRepositoryPath()) : null;
                //try to figure if context reload is necessary
                if (!BaseUtilities.compareObjects(contexturl, indexUpdateUrl)) {
                    LOGGER.log(Level.FINE, "Remote context changed: {0}, unload/load", info.getId());
                    unloadIndexingContext(info.getId());
                } else if (!BaseUtilities.compareObjects(contextfile, repofile)) {
                    LOGGER.log(Level.FINE, "Local context changed: {0}, unload/load", info.getId());
                    unloadIndexingContext(info.getId());
                } else {
                    LOGGER.log(Level.FINER, "Skipping Context: {0}, already loaded.", info.getId());
                    break LOAD; // XXX does it suffice to just return here, or is code after block needed?
                }
            }
            LOGGER.log(Level.FINE, "Loading Context: {0}", info.getId());

            List<IndexCreator> creators;
            if (info.isLocal()) {
                creators = List.of(
                    new JarFileContentsIndexCreator(),
                    new MinimalArtifactInfoIndexCreator(),
                    new MavenArchetypeArtifactInfoIndexCreator(),
                    new MavenPluginArtifactInfoIndexCreator(),
                    new ArtifactDependencyIndexCreator(),
                    new ClassDependencyIndexCreator()
                );
            } else {
                creators = List.of(
                    info.getId().equals(queries.getSMO().getRepositoryId())
                            ? new MinimalArtifactInfoRemoteIndexCreator()
                            : new MinimalArtifactInfoIndexCreator(),
                    new NotifyingIndexCreator()
                );
            }
            try {
                addIndexingContextForced(info, creators);
                LOGGER.log(Level.FINE, "using index creators: {0}", creators);
            } catch (IOException | IllegalArgumentException ex) { // IAE thrown by lucene on index version incompatibilites
                LOGGER.log(Level.WARNING, "Found an incompatible or broken index at " + getIndexDirectory(info) + " with loaded contexts " + getIndexingContexts().keySet()+", resetting.", ex);
                removeDir(getIndexDirectory(info));
//                break LOAD;  // todo: too dangerous to loop here
            }
        }

        //figure if a repository was removed from list, remove from context.
        Set<String> currents = new HashSet<>();
        for (RepositoryInfo info2 : RepositoryPreferences.getInstance().getRepositoryInfos()) {
            currents.add(info2.getId());
        }
        Set<String> toRemove = new HashSet<>(getIndexingContexts().keySet());
        toRemove.removeAll(currents);
        if (!toRemove.isEmpty()) {
            for (String repo : toRemove) {
                try {
                    getRepoMutex(repo).writeAccess((Mutex.ExceptionAction<Void>) () -> {
                        unloadIndexingContext(repo);
                        return null;
                    });
                } catch (MutexException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        Path indexDir = getIndexDirectory(info);
        if (!indexExists(indexDir)) {
            LOGGER.log(Level.FINER, "Index Not Available: {0} at: {1}", new Object[]{info.getId(), indexDir.toAbsolutePath()});
            return true;
        } else {
            return false;
        }
    }

    //always call from mutex.writeAccess
    private void unloadIndexingContext(final String repo) throws IOException {
        assert getRepoMutex(repo).isWriteAccess();
        LOGGER.log(Level.FINE, "Unloading Context: {0}", repo);
        IndexingContext ic = getIndexingContexts().get(repo);
        if (ic != null) {
            removeIndexingContext(ic, false);
        }
    }

    @Messages({"# {0} - repository name",
               "# {1} - cache path",
               "# {2} - cache free storage",
               "# {3} - tmp path",
               "# {4} - tmp free storage",
               "MSG_NoSpace="
                       +"<html>There is not enough space to download and unpack the index for ''{0}''.<br/><br/>"
                       +"''{1}'' has {2} MB free<br/>"
                       +"''{3}'' has {4} MB free<br/><br/>"
                       +"Maven indexing is now disabled and can be enabled again in the maven settings.</html>",
    })
    private void indexLoadedRepo(final RepositoryInfo repo, boolean updateLocal) throws IOException {
        Mutex mutex = getRepoMutex(repo);
        assert mutex.isWriteAccess();
        synchronized (indexingMutexes) {
            indexingMutexes.add(mutex);
        }
        boolean fetchFailed = false;
        long t = System.currentTimeMillis();
        IndexUpdateResult fetchUpdateResult = null;
        RemoteIndexTransferListener listener = null;
        try {
            IndexingContext indexingContext = getIndexingContexts().get(repo.getId());
            if (indexingContext == null) {
                LOGGER.log(Level.WARNING, "Indexing context could not be found: {0}", repo.getId());
                return;
            }
            if (repo.isRemoteDownloadable()) {
                LOGGER.log(Level.FINE, "Indexing Remote Repository: {0}", repo.getId());
                listener = new RemoteIndexTransferListener(repo);
                try {
                    String protocol = URI.create(indexingContext.getIndexUpdateUrl()).getScheme();
                    SettingsDecryptionResult settings = embedder.lookup(SettingsDecrypter.class).decrypt(new DefaultSettingsDecryptionRequest(EmbedderFactory.getOnlineEmbedder().getSettings()));
                    AuthenticationInfo wagonAuth = null;
                    for (Server server : settings.getServers()) {
                        if (repo.getId().equals(server.getId())) {
                            wagonAuth = new AuthenticationInfo();
                            wagonAuth.setUserName(server.getUsername());
                            wagonAuth.setPassword(server.getPassword());
                            wagonAuth.setPassphrase(server.getPassphrase());
                            wagonAuth.setPrivateKey(server.getPrivateKey());
                            break;
                        }
                    }
                    ProxyInfo wagonProxy = null;
                    for (Proxy proxy : settings.getProxies()) {
                        if (proxy.isActive()) {
                            wagonProxy = new ProxyInfo();
                            wagonProxy.setHost(proxy.getHost());
                            wagonProxy.setPort(proxy.getPort());
                            wagonProxy.setNonProxyHosts(proxy.getNonProxyHosts());
                            wagonProxy.setUserName(proxy.getUsername());
                            wagonProxy.setPassword(proxy.getPassword());
                            wagonProxy.setType(protocol);
                            break;
                        }
                    }
                    // MINDEXER-42: cannot use WagonHelper.getWagonResourceFetcher
                    Wagon wagon = embedder.lookup(Wagon.class, protocol);
                    if (wagon instanceof HttpWagon httpwagon) { //#215343
                        Properties p = new Properties();
                        p.setProperty("User-Agent", "netBeans/" + System.getProperty("netbeans.buildnumber"));
                        httpwagon.setHttpHeaders(p);
                    }

                    Path tmpStorage = Files.createTempDirectory(getIndexDirectory(), "extractor-");
                    ResourceFetcher fetcher = createFetcher(wagon, listener, wagonAuth, wagonProxy);
                    listener.setFetcher(fetcher);

                    IndexUpdateRequest iur = new IndexUpdateRequest(indexingContext, fetcher);
                    iur.setIndexTempDir(tmpStorage.toFile());
                    iur.setFSDirectoryFactory((File file) -> MMapDirectory.open(file.toPath()));
                    
                    if (RepositoryPreferences.isMultiThreadedIndexExtractionEnabled()) {
                        // Thread count for maven-indexer remote index extraction, lucene will create one additional merge
                        // thread per extractor. 4 seems to be the sweetspot.
                        iur.setThreads(Math.min(4, Math.max(Runtime.getRuntime().availableProcessors() - 1, 1)));
                    } else {
                        iur.setThreads(1);
                    }
                    if (RepositoryPreferences.getIndexDateCutoffFilter() > 0) {
                        Instant cutoff = ZonedDateTime.now()
                                .minusYears(RepositoryPreferences.getIndexDateCutoffFilter())
                                .toInstant();
                        iur.setExtractionFilter(doc -> {
                            IndexableField date = doc.getField(FLD_LAST_MODIFIED.getKey()); // usually never null
                            return date != null && Instant.ofEpochMilli(Long.parseLong(date.stringValue())).isAfter(cutoff);
                        });
                    }

                    NotifyingIndexCreator nic = (NotifyingIndexCreator) indexingContext.getIndexCreators().stream()
                            .filter(c -> c instanceof NotifyingIndexCreator)
                            .findAny().orElse(null);

                    if (nic != null) {
                        nic.start(listener);
                    }
                    try {
                        removeGroupCache(repo);
                        fetchUpdateResult = remoteIndexUpdater.fetchAndUpdateIndex(iur);
                        storeGroupCache(repo, indexingContext);
                        // register indexed repo in services view
                        if (fetchUpdateResult.isFullUpdate() && fetchUpdateResult.isSuccessful()) {
                            RepositoryPreferences.getInstance().addOrModifyRepositoryInfo(repo);
                        }
                    } catch (IOException | AlreadyClosedException | IllegalArgumentException ex) {
                        // AlreadyClosedException can happen in low storage situations when lucene is trying to handle IOEs
                        // IllegalArgumentException signals remote archive format problems
                        fetchFailed = true;
                        throw new IOException("Failed to load maven-index for: " + indexingContext.getRepositoryUrl(), ex);
                    } catch (RuntimeException ex) {
                        // thread pools, like the one used in maven-indexer's IndexDataReader, may suppress cancellation exceptions
                        // lets try to find them again
                        fetchFailed = true;
                        if (isCancellation(ex)) {
                            Cancellation cancellation = new Cancellation();
                            cancellation.addSuppressed(ex);
                            throw cancellation;
                        } else {
                            throw ex;
                        }
                    } finally {
                        if (nic != null) {
                            nic.end();
                        }
                        try{
                            // make sure no temp files remain after extraction
                            removeDir(tmpStorage);
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, "cleanup failed");
                        }
                    }
                } finally {
                    listener.close();
                }
            } else {
                LOGGER.log(Level.FINE, "Indexing Local Repository: {0}", repo.getId());
                if (!indexingContext.getRepository().exists()) {
                    //#210743
                    LOGGER.log(Level.FINE, "Local repository at {0} doesn't exist, no scan.", indexingContext.getRepository());
                } else {
                    RepositoryIndexerListener repoListener = new RepositoryIndexerListener(indexingContext);
                    try {
                        // Ensure no stale cache files are left
                        removeGroupCache(repo);
                        scanLocalRepo(indexingContext, null, repoListener, updateLocal);
                        storeGroupCache(repo, indexingContext);
                    } finally {
                        repoListener.close();
                    }
                }
            }
        } catch (IOException e) {
            if(e.getCause() instanceof ResourceDoesNotExistException) {
                fireChange(repo, () -> repo.fireNoIndex());
            }
            Path tmpFolder = Path.of(System.getProperty("java.io.tmpdir"));
            Path cacheFolder = getIndexDirectory();

            long freeTmpSpace = getFreeSpaceInMB(tmpFolder);
            long freeCacheSpace = getFreeSpaceInMB(cacheFolder);

            if (isNoSpaceLeftOnDevice(e) || freeCacheSpace < 1000 || freeTmpSpace < 1000) {

                long downloaded = listener != null ? listener.getUnits() * 1024 : -1;
                LOGGER.log(Level.INFO, "Downloaded maven index file has size {0} (zipped). The usable space in [cache]:{1} is {2} MB and in [tmp]:{3} is {4} MB.",
                        new Object[] {downloaded, cacheFolder, freeCacheSpace, tmpFolder, freeTmpSpace});
                LOGGER.log(Level.WARNING, "Download/Extraction failed due to low storage, indexing is now disabled.", e);

                // disable indexing and tell user about it
                RepositoryPreferences.setIndexRepositories(false);

                IndexingNotificationProvider np = Lookup.getDefault().lookup(IndexingNotificationProvider.class);
                if(np != null) {
                    np.notifyError(Bundle.MSG_NoSpace(repo.getName(), "[cache]:"+cacheFolder.toString(), freeCacheSpace, "[tmp]:"+tmpFolder.toString(), freeTmpSpace));
                }
                unloadIndexingContext(repo.getId());
            }
            throw e;
        } catch (Cancellation x) {
            pauseRemoteRepoIndexing(120); // pause a while
            LOGGER.log(Level.INFO, "user canceled indexing", x);
        } catch (ComponentLookupException x) {
            throw new IOException("could not find protocol handler for " + repo.getRepositoryUrl(), x);
        } finally {
            String kind;
            if (fetchUpdateResult != null) {
                kind = fetchUpdateResult.isFullUpdate() ? "download, create" : "incremental download, update";
            } else {
                kind = "scan";
            }
            LOGGER.log(Level.INFO, "Indexing [{0}] of {1} took {2}s.", new Object[]{kind, repo.getId(), String.format("%.2f", (System.currentTimeMillis() - t)/1000.0f)});
            synchronized (indexingMutexes) {
                indexingMutexes.remove(mutex);
            }
            if(!fetchFailed) {
                RepositoryPreferences.setLastIndexUpdate(repo.getId(), new Date());
                fireChange(repo, () -> repo.fireIndexChange());
            }
        }
    }

    private static void pauseRemoteRepoIndexing(int minutes) {
        LOGGER.log(Level.INFO, "pausing index downloads for {0} {1}.", new Object[] {minutes, ChronoUnit.MINUTES});
        RepositoryPreferences.pauseIndexDownloadsFor(minutes, ChronoUnit.MINUTES);
    }

    private static boolean isNoSpaceLeftOnDevice(Throwable ex) {
        String msg = ex.getMessage();
        Throwable cause = ex.getCause();
        Throwable[] suppressed = ex.getSuppressed();
        return (msg != null && msg.contains("No space left on device"))
            || (cause != null && isNoSpaceLeftOnDevice(cause))
            || (suppressed.length > 0 && Stream.of(suppressed).anyMatch(NexusRepositoryIndexManager::isNoSpaceLeftOnDevice));
    }

    private static boolean isCancellation(Throwable ex) {
        return Stream.of(ex.getSuppressed()).anyMatch(s -> s instanceof Cancellation);
    }

    private static boolean isDiag() {
        return Boolean.getBoolean("maven.indexing.diag");
    }

    //spawn the indexing into a separate thread..
    boolean spawnIndexLoadedRepo(final RepositoryInfo repo) {

        if (shouldSkipIndexRequest(repo)) {
            return false;
        }

        // 2 RPs allow concurrent local repo indexing during remote index downloads
        // while also largely avoiding to run two disk-IO heavy tasks at once.
        RequestProcessor rp = repo.isLocal() ? RP_LOCAL : RP_REMOTE;

        rp.post(() -> {
            getRepoMutex(repo).writeAccess((Mutex.Action<Void>) () -> {

                if (shouldSkipIndexRequest(repo)) {
                    return null;
                }

                try {
                    indexLoadedRepo(repo, true);
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, "could not (re-)index " + repo.getId(), ex);
                }
                return null;
            });
        });
        return true;
    }

    @Override
    public void indexRepo(final RepositoryInfo repo) {
        
        if (shouldSkipIndexRequest(repo)) {
            return;
        }

        LOGGER.log(Level.FINER, "Indexing Context: {0}", repo);
        try {
            RemoteIndexTransferListener.addToActive(Thread.currentThread());
            getRepoMutex(repo).writeAccess((Mutex.Action<Void>) () -> {

                if (shouldSkipIndexRequest(repo)) {
                    return null;
                }

                try {
                    initIndexer();
                    assert indexer != null;
                    boolean noIndexExists = loadIndexingContext(repo);
                    //here we always index repo, no matter what RepositoryPreferences.isIndexRepositories() value
                    indexLoadedRepo(repo, !noIndexExists);
                } catch (IOException x) {
                    LOGGER.log(Level.INFO, "could not (re-)index " + repo.getId(), x);
                }
                return null;
            });
        } finally {
            RemoteIndexTransferListener.removeFromActive(Thread.currentThread());
        }

    }
    
    private static boolean shouldSkipIndexRequest(RepositoryInfo repo) {
        if (repo.isRemoteDownloadable()) {
            if (!RepositoryPreferences.isIndexDownloadEnabledEffective()) {
                return true;
            }
            if (RepositoryPreferences.isIndexDownloadDeniedFor(repo)) {
                return true;
            }
            if (!RepositoryPreferences.isIndexDownloadAllowedFor(repo)) {
                IndexingNotificationProvider np = Lookup.getDefault().lookup(IndexingNotificationProvider.class);
                if(np != null) {
                    np.requestPermissionsFor(repo);
                }
                return true;
            }
        }
        return false;
    }

    public void shutdownAll() {
        LOGGER.fine("Shutting Down All Contexts");
        // Do not acquire write access since that can block waiting for a hung download.
        try {
            if (inited) {
                for (IndexingContext ic : getIndexingContexts().values()) {
                    LOGGER.log(Level.FINER, "Shutting Down: {0}", ic.getId());
                    removeIndexingContext(ic, false);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    /**
     * Uses {@link Scanner} to scan repository content. A {@link ArtifactScanningListener} is used to process found
     * artifacts and to add them to the index using
     * {@link NexusIndexer#artifactDiscovered(ArtifactContext, IndexingContext)}.
     *
     * @see DefaultScannerListener
     * @see #artifactDiscovered(ArtifactContext, IndexingContext)
     */
    private void scanLocalRepo(IndexingContext context, String fromPath, ArtifactScanningListener listener, boolean update) throws IOException {

        File repositoryDirectory = context.getRepository();
        if (repositoryDirectory == null) {
            return;  // nothing to scan
        }

        if (!repositoryDirectory.exists()) {
            throw new IOException( "Repository directory " + repositoryDirectory + " does not exist" );
        }

        // always use cache directory when reindexing
        Path tmpDir = Places.getCacheDirectory().toPath().resolve("tmp-" + context.getRepositoryId());
        if (Files.exists(tmpDir)) {
            removeDir(tmpDir);
        }
        Files.createDirectory(tmpDir);

        IndexingContext tmpContext = null;
        try {
            FSDirectory directory = FSDirectory.open(tmpDir);
            if (update) {
                IndexUtils.copyDirectory(context.getIndexDirectory(), directory);
            }
            tmpContext = new DefaultIndexingContext( context.getId() + "-tmp",
                                                     context.getRepositoryId(),
                                                     context.getRepository(),
                                                     tmpDir.toFile(),
                                                     context.getRepositoryUrl(),
                                                     context.getIndexUpdateUrl(),
                                                     context.getIndexCreators(),
                                                     true );

            DefaultScannerListener defaultListener = new DefaultScannerListener(tmpContext, embedder.lookup(IndexerEngine.class), update, listener);
            scanner.scan(new ScanningRequest(tmpContext, defaultListener, fromPath));
 
            tmpContext.updateTimestamp( true );
            context.replace( tmpContext.getIndexDirectory() );
        } catch (Exception ex) {
            throw new IOException("Error scanning context " + context.getId() + ": " + ex, ex);
        } finally {
            if (tmpContext != null) {
                tmpContext.close( true );
            }
            removeDir(tmpDir);
        }
    }

    @Override
    public void updateIndexWithArtifacts(final RepositoryInfo repo, final Collection<Artifact> artifacts) {
        if (!RepositoryPreferences.isIndexRepositories()) {
            return;
        }
        final ArtifactRepository repository = EmbedderFactory.getProjectEmbedder().getLocalRepository();
        try {
            getRepoMutex(repo).writeAccess((Mutex.ExceptionAction<Void>) () -> {
                boolean index = loadIndexingContext(repo);
                if (index) {
                    //do not bother indexing
                    return null;
                }
                IndexingContext indexingContext = indexingContexts.get(repo.getId());
                if (indexingContext == null) {
                    LOGGER.log(Level.WARNING, "Indexing context could not be created: {0}", repo.getId());
                    return null;
                }

                if (!indexingContext.getRepository().exists()) {
                    //#210743
                    LOGGER.log(Level.FINE, "Local repository at {0} doesn't exist, no update.", indexingContext.getRepository());
                    return null;
                }
                Set<ArtifactContext> artifactContexts = new HashSet<>();
                for (Artifact artifact : artifacts) {
                    String absolutePath;
                    if (artifact.getFile() != null) {
                        absolutePath = artifact.getFile().getAbsolutePath();
                    } else if (artifact.getVersion() != null) { //#129025 avoid a NPE down the road
                        //well sort of hack, assume the default repo layout in the repository..
                        absolutePath = repo.getRepositoryPath() + File.separator + repository.pathOf(artifact);
                    } else {
                        continue;
                    }
                    File art = new File(absolutePath);
                    if (art.exists()) {
                        //#229296 don't reindex stuff that is already in the index, with exception of snapshots
                        boolean add = artifact.isSnapshot();
                        if (!artifact.isSnapshot()) {
                            String id = artifact.getGroupId() + ArtifactInfo.FS + artifact.getArtifactId() + ArtifactInfo.FS + artifact.getVersion() + ArtifactInfo.FS + ArtifactInfo.nvl(artifact.getClassifier());
                            BooleanQuery bq = new BooleanQuery.Builder()
                                    .add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST))
                                    .build();
                            try (IteratorSearchResponse response = queries.repeatedPagedSearch(bq, indexingContext, MAX_RESULT_COUNT)) {
                                add = response == null || response.getTotalHitsCount() == 0;
                            }
                        }
                        if (add) {
                            LOGGER.log(Level.FINE, "indexing " + artifact.getId() );
                            ArtifactContext ac = contextProducer.getArtifactContext(indexingContext, art);
                            artifactContexts.add(ac);
//                            assert indexingContext.getIndexSearcher() != null;
                        } else {
                            LOGGER.log(Level.FINE, "Skipped " + artifact.getId() + " already in index.");
                        }
                    }

                }
                indexer.addArtifactsToIndex(artifactContexts, indexingContext);
                storeGroupCache(repo, indexingContext);

                return null;
            });
        } catch (MutexException ex) {
            List<Artifact> sample = artifacts.stream().limit(5).toList();
            LOGGER.log(Level.WARNING,
                "Unable to update index with artifact(s): " + sample
                        + (artifacts.size() > sample.size() ? (" +" + (artifacts.size() - sample.size()) + " more") : ""), ex);
        } catch (NullPointerException x) {
            LOGGER.log(Level.INFO, "#201057", x);
        }
        fireChange(repo, () -> repo.fireIndexChange());
    }
    
    @Override
    public void deleteArtifactFromIndex(final RepositoryInfo repo, final Artifact artifact) {
        if (!RepositoryPreferences.isIndexRepositories()) {
            return;
        }
        final ArtifactRepository repository = EmbedderFactory.getProjectEmbedder().getLocalRepository();
        try {
            getRepoMutex(repo).writeAccess((Mutex.ExceptionAction<Void>) () -> {
                boolean index = loadIndexingContext(repo);
                if (index) {
                    return null; //do not bother indexing
                }
                IndexingContext indexingContext = indexingContexts.get(repo.getId());
                if (indexingContext == null) {
                    LOGGER.log(Level.WARNING, "Indexing context could not be created: {0}", repo.getId());
                    return null;
                }
                if (!indexingContext.getRepository().exists()) {
                    //#210743
                    LOGGER.log(Level.FINE, "Local repository at {0} doesn't exist, no update.", indexingContext.getRepository());
                    return null;
                }

                String absolutePath;
                if (artifact.getFile() != null) {
                    absolutePath = artifact.getFile().getAbsolutePath();
                } else if (artifact.getVersion() != null) { //#129025 avoid a NPE down the road
                    //well sort of hack, assume the default repo layout in the repository..
                    absolutePath = repo.getRepositoryPath() + File.separator + repository.pathOf(artifact);
                } else {
                    return null;
                }
                String extension = artifact.getArtifactHandler().getExtension();

                String pomPath = absolutePath.substring(0, absolutePath.length() - extension.length());
                pomPath += "pom"; //NOI18N
                File pom = new File(pomPath);
                if (pom.exists()) {
                    //TODO batch removal??
                    indexer.deleteArtifactsFromIndex(List.of(contextProducer.getArtifactContext(indexingContext, pom)), indexingContext);
                    storeGroupCache(repo, indexingContext);
                }
                return null;
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        fireChange(repo, () -> repo.fireIndexChange());
    }

    private void fireChange(final RepositoryInfo repo, Runnable r) {
        if (getRepoMutex(repo).isWriteAccess()) {
            RequestProcessor.getDefault().post(() -> {
                fireChange(repo, r);
            });
            return;
        }
        assert !getRepoMutex(repo).isWriteAccess() && !getRepoMutex(repo).isReadAccess();
        r.run();
    }

    static boolean isIndexing(Mutex mutex) {
        synchronized (indexingMutexes) {
            return indexingMutexes.contains(mutex);
        }
    }
    
    static boolean indexExists(Path path) {
        try {
            return Files.exists(path.resolve("timestamp")) && DirectoryReader.indexExists(new MMapDirectory(path));
        } catch (IOException ex) {
            LOGGER.log(Level.FINER, "Unable to verify index location at " + path, ex);
            return false;
        }
    }
   
    private ResourceFetcher createFetcher(final Wagon wagon, TransferListener listener, AuthenticationInfo authenticationInfo, ProxyInfo proxyInfo) {
        return new WagonFetcher(wagon, listener, authenticationInfo, proxyInfo);
    }

    private static Path getIndexDirectory() {
        return Places.getCacheSubdirectory("mavenindex").toPath();
    }

    static Path getIndexDirectory(final RepositoryInfo info) {
        return getIndexDirectory().resolve(info.getId());
    }

    private static Path getAllGroupCacheFile(final RepositoryInfo info) {
        return getIndexDirectory(info).resolve(GROUP_CACHE_ALL_PREFIX + "." + GROUP_CACHE_ALL_SUFFIX);
    }

    private static Path getRootGroupCacheFile(final RepositoryInfo info) {
        return getIndexDirectory(info).resolve(GROUP_CACHE_ROOT_PREFIX + "." + GROUP_CACHE_ROOT_SUFFIX);
    }

    private static void storeGroupCache(RepositoryInfo repo, IndexingContext context) throws IOException {
        Path indexDir = getIndexDirectory(repo);
        Path tempAllCache = Files.createTempFile(indexDir, GROUP_CACHE_ALL_PREFIX, GROUP_CACHE_ALL_SUFFIX);
        Path tempRootCache = Files.createTempFile(indexDir, GROUP_CACHE_ROOT_PREFIX, GROUP_CACHE_ROOT_SUFFIX);
        try {
            Files.write(tempAllCache, context.getAllGroups());
            Files.move(tempAllCache, getAllGroupCacheFile(repo), StandardCopyOption.REPLACE_EXISTING);

            Files.write(tempRootCache, context.getRootGroups());
            Files.move(tempRootCache, getRootGroupCacheFile(repo), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tempAllCache);
            Files.deleteIfExists(tempRootCache);
        }
    }

    private static void removeGroupCache(RepositoryInfo repo) throws IOException {
        Files.deleteIfExists(getAllGroupCacheFile(repo));
        Files.deleteIfExists(getRootGroupCacheFile(repo));
    }

    private static void rebuildGroupCache(RepositoryInfo repo, IndexingContext context) throws IOException {
        removeGroupCache(repo);
        (repo.isLocal() ? RP_LOCAL : RP_REMOTE).submit(() -> {
            getRepoMutex(repo).writeAccess(() -> {
                Path allGroupsPath = getAllGroupCacheFile(repo);
                Path rootGroupsPath = getRootGroupCacheFile(repo);
                if (Files.exists(allGroupsPath) && Files.exists(rootGroupsPath)) {
                    return; // already rebuilt
                }
                try {
                    LOGGER.log(Level.FINE, "Rebuilding group cache for {0}", repo.getId());
                    long start = System.currentTimeMillis();
                    context.rebuildGroups();
                    storeGroupCache(repo, context);
                    LOGGER.log(Level.INFO, "Group cache rebuilding of {0} took {1}s.", new Object[] {repo.getId(), (System.currentTimeMillis()-start)});
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to rebuild groups for repo: " + repo.getId(), e);
                }
            });
        });
    }

    Indexer getIndexer() {
        return indexer;
    }

    SearchEngine getSearcher() {
        return searcher;
    }

    // somewhat based on maven-indexer impl (in WagonHelper) prior to removal in maven-indexer 7.0.0
    private record WagonFetcher(Wagon wagon, TransferListener listener, AuthenticationInfo authenticationInfo, ProxyInfo proxyInfo) implements ResourceFetcher {

        public WagonFetcher {
            Objects.requireNonNull(wagon);
            Objects.requireNonNull(listener);
        }

        @Override
        public void connect(String id, String url) throws IOException {
            Repository repository = new Repository(id, url);

            try {
                wagon.addTransferListener(listener);

                if (authenticationInfo != null) {
                    if (proxyInfo != null) {
                        wagon.connect(repository, authenticationInfo, proxyInfo);
                    } else {
                        wagon.connect(repository, authenticationInfo);
                    }
                } else {
                    if (proxyInfo != null) {
                        wagon.connect(repository, proxyInfo);
                    } else {
                        wagon.connect(repository);
                    }
                }
            } catch (AuthenticationException ex) {
                String msg = "Authentication exception connecting to " + repository;
                logError(msg, ex);
                throw new IOException(msg, ex);
            } catch (WagonException ex) {
                String msg = "Wagon exception connecting to " + repository;
                logError(msg, ex);
                throw new IOException(msg, ex);
            }
        }

        @Override
        public void disconnect() throws IOException {
            try {
                wagon.disconnect();
            } catch (ConnectionException ex) {
                throw new IOException(ex.toString(), ex);
            }
        }

        @Override
        public InputStream retrieve(String name) throws IOException, FileNotFoundException {
            if (isDiag()) {
                String id = wagon.getRepository().getId();
                if(name.endsWith(".properties") && System.getProperty("maven.diag.index.properties." + id) != null) { // NOI18N
                    LOGGER.log(Level.INFO, "maven indexer will use local properties file: {0}", System.getProperty("maven.diag.index.properties." + id)); // NOI18N
                    return new FileInputStream(new File(System.getProperty("maven.diag.index.properties." + id))); // NOI18N
                } else if(name.endsWith(".gz") && System.getProperty("maven.diag.index.gz." + id) != null) { // NOI18N
                    LOGGER.log(Level.INFO, "maven indexer will use gz file: {0}", System.getProperty("maven.diag.index.gz." + id)); // NOI18N
                    return new FileInputStream(new File(System.getProperty("maven.diag.index.gz." + id))); // NOI18N
                }
            }

            File target = Files.createTempFile(/*getTempIndexDirectory(), */"fetcher-" + name, "").toFile();
            target.deleteOnExit();

            try {
                retrieve(name, target);
            } catch (Cancellation | Exception ex) {
                target.delete();
                throw ex;
            }

            return new FileInputStream(target) {
                @Override public void close() throws IOException {
                    super.close();
                    target.delete();
                }
            };
        }

        public void retrieve(String name, File targetFile) throws IOException {
            try {
                wagon.get(name, targetFile);
            } catch (AuthorizationException e) {
                targetFile.delete();
                String msg = "Authorization exception retrieving " + name;
                logError(msg, e);
                throw new IOException(msg, e);
            } catch (ResourceDoesNotExistException e) {
                targetFile.delete();
                String msg = "Resource " + name + " does not exist";
                logError(msg, e);
                FileNotFoundException fileNotFoundException = new FileNotFoundException(msg);
                fileNotFoundException.initCause(e);
                throw fileNotFoundException;
            } catch (WagonException e) {
                targetFile.delete();
                String msg = "Transfer for " + name + " failed";
                logError(msg, e);
                throw new IOException(msg + "; " + e.getMessage(), e);
            }
        }

        private void logError(String msg, Exception ex) {
            if (listener != null) {
                listener.debug(msg + "; " + ex.getMessage());
            }
        }
    }

    private static void removeDir(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static long getFreeSpaceInMB(Path path) {
        try {
            return Files.getFileStore(path).getUsableSpace() / (1024 * 1024);
        } catch (IOException ignore) {
            return -1;
        }
    }

}
