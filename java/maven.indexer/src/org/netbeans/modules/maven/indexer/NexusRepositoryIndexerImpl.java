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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipError;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
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
import org.apache.maven.index.expr.StringSearchExpression;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdateResult;
import org.apache.maven.index.updater.IndexUpdater;
import org.apache.maven.index.updater.ResourceFetcher;
import org.apache.maven.search.api.SearchRequest;
import org.apache.maven.search.api.request.FieldQuery;
import org.apache.maven.search.api.request.Paging;
import org.apache.maven.search.backend.smo.SmoSearchBackend;
import org.apache.maven.search.backend.smo.SmoSearchBackendFactory;
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
import org.codehaus.plexus.util.FileUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.indexer.api.NBArtifactInfo;
import org.netbeans.modules.maven.indexer.api.NBGroupInfo;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.spi.ArchetypeQueries;
import org.netbeans.modules.maven.indexer.spi.BaseQueries;
import org.netbeans.modules.maven.indexer.spi.ChecksumQueries;
import org.netbeans.modules.maven.indexer.spi.ClassUsageQuery;
import org.netbeans.modules.maven.indexer.spi.ClassesQuery;
import org.netbeans.modules.maven.indexer.spi.ContextLoadedQuery;
import org.netbeans.modules.maven.indexer.spi.DependencyInfoQueries;
import org.netbeans.modules.maven.indexer.spi.GenericFindQuery;
import org.netbeans.modules.maven.indexer.spi.impl.RepositoryIndexerImplementation;
import org.netbeans.modules.maven.indexer.spi.ResultImplementation;
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
public class NexusRepositoryIndexerImpl implements RepositoryIndexerImplementation, RepositoryIndexQueryProvider,
        BaseQueries, ChecksumQueries, ArchetypeQueries, DependencyInfoQueries,
        ClassesQuery, ClassUsageQuery, GenericFindQuery, ContextLoadedQuery {

    private static final Logger LOGGER = Logger.getLogger(NexusRepositoryIndexerImpl.class.getName());

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
    
    private final SmoSearchBackend smo = SmoSearchBackendFactory.createDefault();

    @Override
    public boolean handlesRepository(RepositoryInfo repo) {
        // should always come as last when looked up
        // handles all remote repos
        return true; 
    }
    
    @Override
    public BaseQueries getBaseQueries() {
        return this;
    }

    @Override
    public ChecksumQueries getChecksumQueries() {
        return this;
    }

    @Override
    public ArchetypeQueries getArchetypeQueries() {
        return this;
    }

    @Override
    public DependencyInfoQueries getDependencyInfoQueries() {
        return this;
    }

    @Override
    public ClassesQuery getClassesQuery() {
        return this;
    }

    @Override
    public ClassUsageQuery getClassUsageQuery() {
        return this;
    }

    @Override
    public GenericFindQuery getGenericFindQuery() {
        return this;
    }

    @Override
    public ContextLoadedQuery getContextLoadedQuery() {
        return this;
    }
    
    private static Mutex getRepoMutex(RepositoryInfo repo) {
        return getRepoMutex(repo.getId());
    }
    
    private static Mutex getRepoMutex(String repoId) {
        synchronized (repoMutexMap) {
            return repoMutexMap.computeIfAbsent(repoId, k -> new Mutex());
        }
    }
    
    static final int MAX_RESULT_COUNT = 1024;
    static final int NO_CAP_RESULT_COUNT = AbstractSearchRequest.UNDEFINED;

    public NexusRepositoryIndexerImpl() {
    }

    private void initIndexer () {
        if (!inited) {
            try {
                ContainerConfiguration config = new DefaultContainerConfiguration();
                //#154755 - start
                ClassLoader indexerLoader = NexusRepositoryIndexerImpl.class.getClassLoader();
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

    private boolean loadIndexingContext(final RepositoryInfo info) throws IOException {
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
                    info.getId().equals(smo.getRepositoryId())
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

    private @CheckForNull IteratorSearchResponse repeatedPagedSearch(Query q, IndexingContext context, int count) throws IOException {
        return repeatedPagedSearch(q, List.of(context), count);
    }

    private @CheckForNull IteratorSearchResponse repeatedPagedSearch(Query q, List<IndexingContext> contexts, int count) throws IOException {
        IteratorSearchRequest isr = new IteratorSearchRequest(q, contexts, new NoJavadocSourceFilter());
        if (count > 0) {
            isr.setCount(count);
        }
        
        int MAX_MAX_CLAUSE = 1<<11;  // conservative maximum for too general queries, like "c:*class*"

        if (q instanceof BooleanQuery) {
            List<BooleanClause> list = ((BooleanQuery)q).clauses();
            if (list.size() == 1) {
                Query q1 = list.get(0).getQuery();
                if (q1 instanceof PrefixQuery && "u".equals(((PrefixQuery)q1).getPrefix().field())) {
                    // increase for queries like "+u:org.netbeans.modules|*" to succeed
                    MAX_MAX_CLAUSE = 1<<16;
                } else if (q1 instanceof TermQuery && "p".equals(((TermQuery) q1).getTerm().field())) {
                    // +p:nbm also produces several thousand hits
                    MAX_MAX_CLAUSE = 1<<16;
                }
            }
        }

        int oldMax = IndexSearcher.getMaxClauseCount();
        try {
            int max = oldMax;
            while (true) {
                IteratorSearchResponse response;
                try {
                    IndexSearcher.setMaxClauseCount(max);
                    response = searcher.searchIteratorPaged(isr, contexts);
                    LOGGER.log(Level.FINE, "passed on {0} clauses processing {1} with {2} hits", new Object[] {max, q, response.getTotalHitsCount()});
                    return response;
                } catch (IndexSearcher.TooManyClauses exc) {
                    LOGGER.log(Level.FINE, "TooManyClauses on {0} clauses processing {1}", new Object[] {max, q});
                    max *= 2;
                    if (max > MAX_MAX_CLAUSE) {
                        LOGGER.log(Level.WARNING, "Encountered more than {0} clauses processing {1}", new Object[] {MAX_MAX_CLAUSE, q});
                        return null;
                    }
                }
            }
        } finally {
            IndexSearcher.setMaxClauseCount(oldMax);
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
                    if (wagon instanceof HttpWagon) { //#216401
                        HttpWagon httpwagon = (HttpWagon) wagon;
                        //#215343
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
                        scan(indexingContext, null, repoListener, updateLocal);
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
            || (suppressed.length > 0 && Stream.of(suppressed).anyMatch(NexusRepositoryIndexerImpl::isNoSpaceLeftOnDevice));
    }

    private static boolean isCancellation(Throwable ex) {
        return Stream.of(ex.getSuppressed()).anyMatch(s -> s instanceof Cancellation);
    }

    private static boolean isDiag() {
        return Boolean.getBoolean("maven.indexing.diag");
    }

    //spawn the indexing into a separate thread..
    private boolean spawnIndexLoadedRepo(final RepositoryInfo repo) {

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
    private void scan(IndexingContext context, String fromPath, ArtifactScanningListener listener, boolean update) throws IOException {

        File repositoryDirectory = context.getRepository();
        if (repositoryDirectory == null) {
            return;  // nothing to scan
        }
 
        if (!repositoryDirectory.exists()) {
            throw new IOException( "Repository directory " + repositoryDirectory + " does not exist" );
        }
 
        // always use cache directory when reindexing
        File tmpDir = new File(Places.getCacheDirectory(), "tmp-" + context.getRepositoryId());
        if (!tmpDir.mkdirs()) {
            throw new IOException( "Cannot create temporary directory: " + tmpDir );
        }
        File tmpFile = new File(tmpDir, context.getId() + "-tmp"); // TODO: purpose of file?
 
        IndexingContext tmpContext = null;
        try {
            FSDirectory directory = FSDirectory.open(tmpDir.toPath());
            if (update) {
                IndexUtils.copyDirectory(context.getIndexDirectory(), directory);
            }
            tmpContext = new DefaultIndexingContext( context.getId() + "-tmp",
                                                     context.getRepositoryId(),
                                                     context.getRepository(),
                                                     tmpDir,
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
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            FileUtils.deleteDirectory(tmpDir);
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
                            IteratorSearchResponse response = repeatedPagedSearch(bq, indexingContext, MAX_RESULT_COUNT);
                            add = response == null || response.getTotalHitsCount() == 0;
                            if (response != null) {
                                response.close();
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
                try {
                    indexer.addArtifactsToIndex(artifactContexts, indexingContext);
                    storeGroupCache(repo, indexingContext);
                } catch (ZipError err) {
                    LOGGER.log(Level.INFO, "#230581 concurrent access to local repository file. Skipping..", err);
                }

                return null;
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
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

    @Override
    public ResultImplementation<String> getGroups(List<RepositoryInfo> repos) {
        return filterGroupIds("", repos);
    }

    private static boolean isIndexing(Mutex mutex) {
        synchronized (indexingMutexes) {
            return indexingMutexes.contains(mutex);
        }
    }

    private interface RepoAction {
        void run(RepositoryInfo repo, IndexingContext context) throws IOException;
    }
    
    private void iterate(List<RepositoryInfo> repos, final RepoAction action, final RepoAction actionSkip, final boolean skipUnIndexed) {
        if (repos == null) {
            repos = RepositoryPreferences.getInstance().getRepositoryInfos();
        }
        for (final RepositoryInfo repo : repos) {
            Mutex mutex = getRepoMutex(repo);
            if (skipUnIndexed && isIndexing(mutex)) {
                try {
                    actionSkip.run(repo, null);
                } catch (IOException ex) {
                    LOGGER.log(Level.FINER, "could not skip " + repo.getId(), ex);
                }
            } else {
                mutex.writeAccess((Mutex.Action<Void>) () -> {
                    try {
                        boolean index = loadIndexingContext(repo);
                        if (skipUnIndexed && index) {
                            if (!RepositoryPreferences.isIndexRepositories()) {
                                return null;
                            }
                            boolean spawned = spawnIndexLoadedRepo(repo);
                            if (spawned) {
                                actionSkip.run(repo, null);
                            }
                            return null;
                        }
                        IndexingContext context = getIndexingContexts().get(repo.getId());
                        if (context == null) {
                            if (skipUnIndexed) {
                                actionSkip.run(repo, null);
                            }
                            return null;
                        }
                        action.run(repo, context);
                    } catch (IOException x) {
                        LOGGER.log(Level.INFO, "could not process " + repo.getId(), x);
                    }
                    return null;
                });
            }
        }
    }

    private ResultImplementation<String> filterGroupIds(final String prefix, final List<RepositoryInfo> repos) {
        ResultImpl<String> result = new ResultImpl<>(
            (ResultImpl<String> res) -> filterGroupIds(prefix, res, res.getSkipped(), false)
        );
        return filterGroupIds(prefix, result, repos, true);
    }
    
    private ResultImplementation<String> filterGroupIds(final String prefix, final ResultImpl<String> result, 
                                                            final List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final Set<String> groups = new TreeSet<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);

        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            Set<String> all= context.getAllGroups();
            if (!all.isEmpty()) {
                if (prefix.isEmpty()) {
                    groups.addAll(all);
                } else {
                    for (String gr : all) {
                        if (gr.startsWith(prefix)) {
                            groups.add(gr);
                        }
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        
        result.setResults(groups);
        return result;
    }

    @Override
    public ResultImplementation<String> getGAVsForPackaging(final String packaging, List<RepositoryInfo> repos) {
        ResultImpl<String> result = new ResultImpl<>((ResultImpl<String> result1) -> {
            getGAVsForPackaging(packaging, result1, result1.getSkipped(), false);
        });
        return getGAVsForPackaging(packaging,result, repos, true);
    }
    
    private ResultImplementation<String> getGAVsForPackaging(final String packaging, final ResultImpl<String> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<String> infos = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        BooleanQuery bq = new BooleanQuery.Builder()
                .add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, packaging)), BooleanClause.Occur.MUST))
                .build();
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            IteratorSearchResponse response = repeatedPagedSearch(bq, context, NO_CAP_RESULT_COUNT);
            if (response != null) {
               try {
                    for (ArtifactInfo ai : response) {
                        String gav = ai.getGroupId() + ":" + ai.getArtifactId() + ":" + ai.getVersion();
                        if (!infos.contains(gav)) {
                            infos.add(gav);
                        }
                    }
                } finally {
                    result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                    result.addTotalResultCount(response.getTotalHitsCount());
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        result.setResults(infos);
        return result;        
    }
    
    @Override
    public ResultImplementation<NBVersionInfo> getRecords(final String groupId, final String artifactId, final String version, List<RepositoryInfo> repos) {
        ResultImpl<NBVersionInfo> result = new ResultImpl<>((ResultImpl<NBVersionInfo> result1) -> {
            getRecords(groupId, artifactId, version, result1, result1.getSkipped(), false);
        });
        return getRecords(groupId, artifactId, version, result, repos, true);
    }
    
    private ResultImplementation<NBVersionInfo> getRecords(final String groupId, final String artifactId, final String version, final ResultImpl<NBVersionInfo> result, 
                                             List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        String id = groupId + ArtifactInfo.FS + artifactId + ArtifactInfo.FS + version + ArtifactInfo.FS;
        BooleanQuery bq = new BooleanQuery.Builder()
                .add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST))
                .build();
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            IteratorSearchResponse response = repeatedPagedSearch(bq, context, MAX_RESULT_COUNT);
            if (response != null) {
                try {
                    for (ArtifactInfo ai : response) {
                        infos.add(convertToNBVersionInfo(ai));
                    }
                } finally {
                    result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                    result.addTotalResultCount(response.getTotalHitsCount());
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        doSortIssue226100(infos);
        result.setResults(infos);
        return result;
    }

    @Override
    public ResultImplementation<String> getArtifacts(final String groupId, final List<RepositoryInfo> repos) {
        ResultImpl<String> result = new ResultImpl<>((ResultImpl<String> result1) -> {
            getArtifacts(groupId, result1, result1.getSkipped(), false);
        });
        return getArtifacts(groupId, result, repos, true);
    }
    
    private  ResultImplementation<String> getArtifacts(final String groupId, final ResultImpl<String> result, final List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final Set<String> artifacts = new TreeSet<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        String id = groupId + ArtifactInfo.FS;
        BooleanQuery bq = new BooleanQuery.Builder()
                .add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST))
                .build();
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            //mkleint: this is not capped, because only a string is collected (and collapsed), the rest gets CGed fast
            IteratorSearchResponse response = repeatedPagedSearch(bq, context, NO_CAP_RESULT_COUNT);
            if (response != null) {
                try {
                    for (ArtifactInfo artifactInfo : response.getResults()) {
                        artifacts.add(artifactInfo.getArtifactId());
                    }
                } finally {
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        result.setResults(artifacts);
        return result;
    }

    @Override
    public ResultImplementation<NBVersionInfo> getVersions(final String groupId, final String artifactId, List<RepositoryInfo> repos) {
        ResultImpl<NBVersionInfo> result = new ResultImpl<>((ResultImpl<NBVersionInfo> result1) -> {
            getVersions(groupId, artifactId, result1, result1.getSkipped(), false);
        });
        return getVersions(groupId, artifactId, result, repos, true);
    }
    private ResultImplementation<NBVersionInfo> getVersions(final String groupId, final String artifactId, final ResultImpl<NBVersionInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        String id = groupId + ArtifactInfo.FS + artifactId + ArtifactInfo.FS;
        BooleanQuery bq = new BooleanQuery.Builder()
                .add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST))
                .build();
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            IteratorSearchResponse response = repeatedPagedSearch(bq, context, MAX_RESULT_COUNT);
            if (response != null) {
                try {
                    for (ArtifactInfo ai : response) {
                        infos.add(convertToNBVersionInfo(ai));
                    }
                } finally {
                    result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                    result.addTotalResultCount(response.getTotalHitsCount());
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        doSortIssue226100(infos);
        result.setResults(infos);
        return result;
    }

    @Override
    public ResultImplementation<NBVersionInfo> findVersionsByClass(final String className, List<RepositoryInfo> repos) {

        Optional<RepositoryInfo> central = repos.stream()
                .filter(repo -> repo.getId().equals(smo.getRepositoryId()))
                .findFirst();

        // remote index contains no class data -> use web service
        if (central.isPresent()) {
            List<RepositoryInfo> otherRepos = new ArrayList<>(repos);
            otherRepos.remove(central.get());

            SearchRequest request = new SearchRequest(new Paging(128), 
                    FieldQuery.fieldQuery(className.contains(".") ?
                            org.apache.maven.search.api.MAVEN.FQ_CLASS_NAME
                          : org.apache.maven.search.api.MAVEN.CLASS_NAME, className));

            return new CompositeResult<>(findVersionsByClass(className, otherRepos), new SMORequestResult(smo, request));
        } else {
            ResultImpl<NBVersionInfo> result = new ResultImpl<>((ResultImpl<NBVersionInfo> result1) -> {
                findVersionsByClass(className, result1, result1.getSkipped(), false);
            });
            return findVersionsByClass(className, result, repos, true);
        }
    }

    private ResultImplementation<NBVersionInfo> findVersionsByClass(final String className, final ResultImpl<NBVersionInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            String clsname = className.replace(".", "/");
            while (!clsname.isEmpty() && (clsname.startsWith("*") || clsname.startsWith("?"))) {
                //#238740
                clsname = clsname.substring(1);
            }
            if (clsname.isEmpty()) {
                return;
            }

            Query q = setBooleanRewrite(constructQuery(MAVEN.CLASSNAMES, clsname.toLowerCase(Locale.ENGLISH)));
            IteratorSearchResponse response = repeatedPagedSearch(q, context, MAX_RESULT_COUNT);
            if (response != null) {
                try {
                    infos.addAll(postProcessClasses(response.getResults(), clsname));
                } finally {
                    //?? really count in this case?
                    result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                    result.addTotalResultCount(response.getTotalHitsCount());
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        doSortIssue226100(infos);
        result.setResults(infos);
        return result;
    }

    private Query constructQuery(Field f, String qs) {
        return indexer.constructQuery(f, new StringSearchExpression(qs));
    }

    @Override 
    public ResultImplementation<RepositoryQueries.ClassUsage> findClassUsages(final String className, @NullAllowed List<RepositoryInfo> repos) {
        ResultImpl<RepositoryQueries.ClassUsage> result = new ResultImpl<>((ResultImpl<RepositoryQueries.ClassUsage> result1) -> {
            findClassUsages(className, result1, result1.getSkipped(), false);
        });
        return findClassUsages(className, result, repos, true);
        
    }
    
    private ResultImplementation<RepositoryQueries.ClassUsage> findClassUsages(final String className, ResultImpl<RepositoryQueries.ClassUsage> result, @NullAllowed List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        List<RepositoryInfo> localRepos = new ArrayList<>();
        if (repos == null) {
            repos = RepositoryPreferences.getInstance().getRepositoryInfos();
        }
        for (RepositoryInfo repo : repos) {
            if (repo.isLocal()) {
                localRepos.add(repo);
            }
        }
        final List<RepositoryQueries.ClassUsage> results = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(localRepos, (RepositoryInfo repo, IndexingContext context) -> {
            ClassDependencyIndexCreator.search(className, indexer, List.of(context), results);
        }, skipAction, skipUnIndexed);
        results.sort((RepositoryQueries.ClassUsage r1, RepositoryQueries.ClassUsage r2) -> r1.getArtifact().compareTo(r2.getArtifact()));
        result.setResults(results);
        return result;
    }
    
    @Override
    public ResultImplementation<NBVersionInfo> findDependencyUsage(final String groupId, final String artifactId, final String version, @NullAllowed List<RepositoryInfo> repos) {
        ResultImpl<NBVersionInfo> result = new ResultImpl<>((ResultImpl<NBVersionInfo> result1) -> {
            findDependencyUsage(groupId, artifactId, version, result1, result1.getSkipped(), false);
        });
        return findDependencyUsage(groupId, artifactId, version, result, repos, true);
    }
    private ResultImplementation<NBVersionInfo> findDependencyUsage(String groupId, String artifactId, String version, final ResultImpl<NBVersionInfo> result, @NullAllowed List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final Query q = ArtifactDependencyIndexCreator.query(groupId, artifactId, version);
        final List<NBVersionInfo> infos = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            IteratorSearchResponse response = repeatedPagedSearch(q, context, MAX_RESULT_COUNT);
            if (response != null) {
                try {
                    for (ArtifactInfo ai : response) {
                        infos.add(convertToNBVersionInfo(ai));
                    }
                } finally {
                    result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                    result.addTotalResultCount(response.getTotalHitsCount());
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        result.setResults(infos);
        return result;
    }
    
    @Override
    public ResultImplementation<NBGroupInfo> findDependencyUsageGroups(final String groupId, final String artifactId, final String version, List<RepositoryInfo> repos) {
        ResultImpl<NBGroupInfo> result = new ResultImpl<>((ResultImpl<NBGroupInfo> result1) -> {
            findDependencyUsageGroups(groupId, artifactId, version, result1, result1.getSkipped(), false);
        });
        return findDependencyUsageGroups(groupId, artifactId, version, result, repos, true);
    }

    private ResultImplementation<NBGroupInfo> findDependencyUsageGroups(String groupId, String artifactId, String version, ResultImpl<NBGroupInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        //tempmaps
        Map<String, NBGroupInfo> groupMap = new HashMap<>();
        Map<String, NBArtifactInfo> artifactMap = new HashMap<>();
        List<NBGroupInfo> groupInfos = new ArrayList<>(result.getResults());
        ResultImpl<NBVersionInfo> res = new ResultImpl<>((ResultImpl<NBVersionInfo> result1) -> {
            //noop will not be called
        });
        findDependencyUsage(groupId, artifactId, version, res, repos, skipUnIndexed);
        convertToNBGroupInfo(res.getResults(), groupMap, artifactMap, groupInfos);
        if (res.isPartial()) {
            result.addSkipped(res.getSkipped());
        }
        result.setResults(groupInfos);
        return result;
        
    }
    
    private static void convertToNBGroupInfo(Collection<NBVersionInfo> artifactInfos, 
                                      Map<String, NBGroupInfo> groupMap, 
                                      Map<String, NBArtifactInfo> artifactMap,
                                      List<NBGroupInfo> groupInfos) {
        for (NBVersionInfo ai : artifactInfos) {
            String groupId = ai.getGroupId();
            String artId = ai.getArtifactId();

            NBGroupInfo ug = groupMap.get(groupId);
            if (ug == null) {
                ug = new NBGroupInfo(groupId);
                groupInfos.add(ug);
                groupMap.put(groupId, ug);
            }
            NBArtifactInfo ua = artifactMap.get(artId);
            if (ua == null) {
                ua = new NBArtifactInfo(artId);
                ug.addArtifactInfo(ua);
                artifactMap.put(artId, ua);
            }
            ua.addVersionInfo(ai);
        }
    }
    
    @Override
    public ResultImplementation<NBVersionInfo> findBySHA1(final String sha1, List<RepositoryInfo> repos) {

        Optional<RepositoryInfo> central = repos.stream()
            .filter(repo -> repo.getId().equals(smo.getRepositoryId()))
            .findFirst();

        // remote index contains no sh1 data -> use web service
        if (central.isPresent()) {
            List<RepositoryInfo> otherRepos = new ArrayList<>(repos);
            otherRepos.remove(central.get());

            SearchRequest request = new SearchRequest(new Paging(8), 
                    FieldQuery.fieldQuery(org.apache.maven.search.api.MAVEN.SHA1, sha1));

            return new CompositeResult<>(findBySHA1(sha1, otherRepos), new SMORequestResult(smo, request));
        } else {
            ResultImpl<NBVersionInfo> result = new ResultImpl<>((ResultImpl<NBVersionInfo> result1) -> {
                findBySHA1(sha1, result1, result1.getSkipped(), false);
            });
            return findBySHA1(sha1, result, repos, true);
        }
    }
    
    private ResultImplementation<NBVersionInfo> findBySHA1(final String sha1, final ResultImpl<NBVersionInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            BooleanQuery bq = new BooleanQuery.Builder()
                    .add(new BooleanClause((setBooleanRewrite(constructQuery(MAVEN.SHA1, sha1))), BooleanClause.Occur.SHOULD))
                    .build();
            IteratorSearchResponse response = repeatedPagedSearch(bq, context, MAX_RESULT_COUNT);
            if (response != null) {
                try {
                    for (ArtifactInfo ai : response) {
                        infos.add(convertToNBVersionInfo(ai));
                    }
                } finally {
                    result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                    result.addTotalResultCount(response.getTotalHitsCount());
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        doSortIssue226100(infos);
        result.setResults(infos);
        return result;
    }

    @Override
    public ResultImplementation<NBVersionInfo> findArchetypes(List<RepositoryInfo> repos) {
        ResultImpl<NBVersionInfo> result = new ResultImpl<>((ResultImpl<NBVersionInfo> result1) -> {
            findArchetypes(result1, result1.getSkipped(), false);
        });
        return findArchetypes( result, repos, true);
    }
    
    private ResultImplementation<NBVersionInfo> findArchetypes(final ResultImpl<NBVersionInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        BooleanQuery bq = new BooleanQuery.Builder()
                // XXX also consider using NexusArchetypeDataSource
                .add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-archetype")), BooleanClause.Occur.MUST)) //NOI18N
                .build();
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            /* There are >512 archetypes in Central, and we want them all in ChooseArchetypePanel
            FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
            fsr.setCount(MAX_RESULT_COUNT);
            */
            IteratorSearchResponse response = repeatedPagedSearch(bq, context, NO_CAP_RESULT_COUNT);
            if (response != null) {
                try {
                    for (ArtifactInfo ai : response) {
                        infos.add(convertToNBVersionInfo(ai));
                    }
                } finally {
                    result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                    result.addTotalResultCount(response.getTotalHitsCount());
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        doSortIssue226100(infos);
        result.setResults(infos);
        return result;
    }

    @Override
    public ResultImplementation<String> filterPluginArtifactIds(final String groupId, final String prefix, List<RepositoryInfo> repos) {
        ResultImpl<String> result = new ResultImpl<>((ResultImpl<String> result1) -> {
            filterPluginArtifactIds(groupId, prefix, result1, result1.getSkipped(), false);
        });
        return filterPluginArtifactIds(groupId, prefix, result, repos, true);
    }
    
    private ResultImplementation<String> filterPluginArtifactIds(final String groupId, final String prefix, ResultImpl<String> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final Set<String> artifacts = new TreeSet<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        String id = groupId + ArtifactInfo.FS + prefix;
        BooleanQuery bq = new BooleanQuery.Builder()
                .add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-plugin")), BooleanClause.Occur.MUST))
                .add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST))
                .build();
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            //mkleint: this is not capped, because only a string is collected (and collapsed), the rest gets CGed fast
            IteratorSearchResponse response = repeatedPagedSearch(bq, context, NO_CAP_RESULT_COUNT);
            if (response != null) {
                try {
                    for (ArtifactInfo artifactInfo : response.getResults()) {
                        artifacts.add(artifactInfo.getArtifactId());
                    }
                } finally {
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        result.setResults(artifacts);
        return result;
    }

    @Override
    public ResultImplementation<String> filterPluginGroupIds(final String prefix, List<RepositoryInfo> repos) {
        ResultImpl<String> result = new ResultImpl<>((ResultImpl<String> result1) -> {
            filterPluginGroupIds(prefix, result1, result1.getSkipped(), false);
        });
        return filterPluginGroupIds( prefix, result, repos, true);
    }
    
    private ResultImplementation<String> filterPluginGroupIds(final String prefix, ResultImpl<String> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final Set<String> artifacts = new TreeSet<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-plugin")), BooleanClause.Occur.MUST));
        if (!prefix.isEmpty()) { //heap out of memory otherwise
            builder.add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.GROUP_ID, prefix))), BooleanClause.Occur.MUST));
        }
        BooleanQuery bq = builder.build();
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            //mkleint: this is not capped, because only a string is collected (and collapsed), the rest gets CGed fast
            IteratorSearchResponse response = repeatedPagedSearch(bq, context, NO_CAP_RESULT_COUNT);
            if (response != null) {
                try {
                    for (ArtifactInfo artifactInfo : response.getResults()) {
                        artifacts.add(artifactInfo.getGroupId());
                    }
                } finally {
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        result.setResults(artifacts);
        return result;
    }

    @Override
    public ResultImplementation<NBVersionInfo> find(final List<QueryField> fields, List<RepositoryInfo> repos) {
        ResultImpl<NBVersionInfo> result = new ResultImpl<>((ResultImpl<NBVersionInfo> result1) -> {
            find(fields, result1, result1.getSkipped(), false);
        });
        return find(fields,  result, repos, true);
    }
    private ResultImplementation<NBVersionInfo> find(final List<QueryField> fields, final ResultImpl<NBVersionInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery.Builder bq = new BooleanQuery.Builder();
                for (QueryField field : fields) {
                    BooleanClause.Occur occur = field.getOccur() == QueryField.OCCUR_SHOULD ? BooleanClause.Occur.SHOULD : BooleanClause.Occur.MUST;
                    String fieldName = toNexusField(field.getField());
                    String one = field.getValue();
                    while (!one.isEmpty() && (one.startsWith("*") || one.startsWith("?"))) {
                        //#196046
                        one = one.substring(1);
                    }
                    if (one.isEmpty()) {
                        continue;
                    }

                    if (fieldName != null) {
                        Query q;
                        if (ArtifactInfo.NAMES.equals(fieldName)) {
                            try {
                                String clsname = one.replace(".", "/"); //NOI18N
                                q = constructQuery(MAVEN.CLASSNAMES, clsname.toLowerCase(Locale.ENGLISH));
                            } catch (IllegalArgumentException iae) {
                                //#204651 only escape when problems occur
                                String clsname = QueryParser.escape(one.replace(".", "/")); //NOI18N
                                try {
                                    q = constructQuery(MAVEN.CLASSNAMES, clsname.toLowerCase(Locale.ENGLISH));
                                } catch (IllegalArgumentException iae2) {
                                    //#224088
                                    continue;
                                }
                            }
                        } else if (ArtifactInfo.ARTIFACT_ID.equals(fieldName)) {
                            String aid = one.replace("-", "?").replace(".", "?");
                            try {
                                q = constructQuery(MAVEN.ARTIFACT_ID, aid);
                            } catch (IllegalArgumentException iae) {
                                //#204651 only escape when problems occur
                                try {
                                    q = constructQuery(MAVEN.ARTIFACT_ID, QueryParser.escape(aid));
                                } catch (IllegalArgumentException iae2) {
                                    //#224088
                                    continue;
                                }
                            }
                        } else if (ArtifactInfo.GROUP_ID.equals(fieldName)) {
                            String gid = one.replace("-", "?").replace(".", "?");
                            try {
                                q = constructQuery(MAVEN.GROUP_ID, gid);
                            } catch (IllegalArgumentException iae) {
                                //#204651 only escape when problems occur
                                try {
                                    q = constructQuery(MAVEN.GROUP_ID, QueryParser.escape(gid));
                                } catch (IllegalArgumentException iae2) {
                                    //#224088
                                    continue;
                                }
                            }
                        } else {
                            if (field.getMatch() == QueryField.MATCH_EXACT) {
                                q = new TermQuery(new Term(fieldName, one));
                            } else {
                                q = new PrefixQuery(new Term(fieldName, one));
                            }
                        }
                        bq.add(new BooleanClause(setBooleanRewrite(q), occur));
                    } else {
                        //TODO when all fields, we need to create separate
                        //queries for each field.
                    }
                }
                IteratorSearchResponse resp = repeatedPagedSearch(bq.build(), context, MAX_RESULT_COUNT);
                if (resp != null) {
                    try {
                        for (ArtifactInfo ai : resp) {
                            infos.add(convertToNBVersionInfo(ai));
                        }
                    } finally {
                        result.addReturnedResultCount(resp.getTotalProcessedArtifactInfoCount());
                        result.addTotalResultCount(resp.getTotalHitsCount());
                        resp.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        doSortIssue226100(infos);
        result.setResults(infos);
        return result;
    }
    
    private void doSortIssue226100(List<NBVersionInfo> infos) {
        try {
            Collections.sort(infos);
        } catch (IllegalStateException | IllegalArgumentException ex) {
//            doLogError226100(infos, ex);
        }
    }

    private void doLogError226100(List<NBVersionInfo> infos, Exception ex) throws RuntimeException {
        //#226100
        StringBuilder versions = new StringBuilder();
        for (NBVersionInfo info : infos) {
            versions.append(info.getVersion()).append(",");
        }
        String message = "Issue #226100: Versions compared are:" + versions.toString();
        LOGGER.log(Level.WARNING, message);
        boolean rethrow = false;
        assert rethrow = true == false;
        if (rethrow) {
            throw new RuntimeException( message, ex);
        }
    }

    @Override
    public List<RepositoryInfo> getLoaded(final List<RepositoryInfo> repos) {
        final List<RepositoryInfo> toRet = new ArrayList<>(repos.size());
        for (final RepositoryInfo repo : repos) {
            Path loc = getIndexDirectory(repo); // index folder
            if (indexExists(loc)) {
                toRet.add(repo);
            }
        }
        return toRet;
    }

    private static boolean indexExists(Path path) {
        try {
            return Files.exists(path.resolve("timestamp")) && DirectoryReader.indexExists(new MMapDirectory(path));
        } catch (IOException ex) {
            LOGGER.log(Level.FINER, "Unable to verify index location at " + path, ex);
            return false;
        }
    }

    private String toNexusField(String field) {
        if (field != null) switch (field) {
            case QueryField.FIELD_ARTIFACTID: return ArtifactInfo.ARTIFACT_ID;
            case QueryField.FIELD_GROUPID: return ArtifactInfo.GROUP_ID;
            case QueryField.FIELD_VERSION: return ArtifactInfo.VERSION;
            case QueryField.FIELD_CLASSES: return ArtifactInfo.NAMES;
            case QueryField.FIELD_NAME: return ArtifactInfo.NAME;
            case QueryField.FIELD_DESCRIPTION: return ArtifactInfo.DESCRIPTION;
            case QueryField.FIELD_PACKAGING: return ArtifactInfo.PACKAGING;
        }
        return field;
    }

    private Collection<NBVersionInfo> postProcessClasses(IteratorResultSet artifactInfos, String classname) {
        List<NBVersionInfo> toRet = new ArrayList<>();
        int patter = Pattern.DOTALL + Pattern.MULTILINE;
        boolean isPath = classname.contains("/");
        if (isPath) {
            for (ArtifactInfo i : artifactInfos) {
                toRet.add(convertToNBVersionInfo(i));
            }
            return toRet;
        }
        //if I got it right, we need an exact match of class name, which the query doesn't provide? why?
        String pattStr = ".*/" + classname + "$.*";
        Pattern patt = Pattern.compile(pattStr, patter);
        //#217932 for some reason IteratorResultSet implementation decided
        //not to implemenent Iterator.remove().
        //we need to copy to our own list instead of removing from original.
        ArrayList<ArtifactInfo> altArtifactInfos = new ArrayList<>();
        for (ArtifactInfo ai : artifactInfos) {
            Matcher m = patt.matcher(ai.getClassNames());
            if (m.matches()) {
                altArtifactInfos.add(ai);
            }
        }
        for (ArtifactInfo i : altArtifactInfos) {
            toRet.add(convertToNBVersionInfo(i));
        }
        return toRet;
    }

    static List<NBVersionInfo> convertToNBVersionInfo(Collection<ArtifactInfo> artifactInfos) {
        List<NBVersionInfo> bVersionInfos = new ArrayList<>();
        for (ArtifactInfo ai : artifactInfos) {
            NBVersionInfo nbvi = convertToNBVersionInfo(ai);
            if (nbvi != null) {
              bVersionInfos.add(nbvi);
            }
        }
        return bVersionInfos;
    }
    static NBVersionInfo convertToNBVersionInfo(ArtifactInfo ai) {
        if ("javadoc".equals(ai.getClassifier()) || "sources".equals(ai.getClassifier())) { //NOI18N
            // we don't want javadoc and sources shown anywhere, we use the getJavadocExists(), getSourceExists() methods.
            return null;
        }
        // fextension != packaging - e.g a pom could be packaging "bundle" but from type/extension "jar"
        NBVersionInfo nbvi = new NBVersionInfo(ai.getRepository(), ai.getGroupId(), ai.getArtifactId(),
                ai.getVersion(), ai.getFileExtension(), ai.getPackaging(), ai.getName(), ai.getDescription(), ai.getClassifier());
        /*Javadoc & Sources*/
        nbvi.setJavadocExists(ai.getJavadocExists() == ArtifactAvailability.PRESENT);
        nbvi.setSourcesExists(ai.getSourcesExists() == ArtifactAvailability.PRESENT);
        nbvi.setSignatureExists(ai.getSignatureExists() == ArtifactAvailability.PRESENT);
//        nbvi.setSha(ai.sha1);
        nbvi.setLastModified(ai.getLastModified());
        nbvi.setSize(ai.getSize());
        nbvi.setLuceneScore(ai.getLuceneScore());
        return nbvi;
    }
    
    private static Query setBooleanRewrite (final Query q) {
        if (q instanceof MultiTermQuery) {
            ((MultiTermQuery)q).setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_REWRITE);
        } else if (q instanceof BooleanQuery) {
            for (BooleanClause c : ((BooleanQuery)q).clauses()) {
                setBooleanRewrite(c.getQuery());
            }
        }
        return q;
    }

    private static class SkippedAction implements RepoAction {

        private final ResultImpl<?> result;

        private SkippedAction(ResultImpl<?> result) {
            this.result = result;
        }
        
        @Override
        public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
            //indexing context is always null here..
            result.addSkipped(repo);
        }
        
    }

    private static class NoJavadocSourceFilter implements ArtifactInfoFilter {

        @Override
        public boolean accepts(IndexingContext ctx, ArtifactInfo ai) {
            return !("javadoc".equals(ai.getClassifier()) || "sources".equals(ai.getClassifier()));
        }
        
    }
    
    private ResourceFetcher createFetcher(final Wagon wagon, TransferListener listener, AuthenticationInfo authenticationInfo, ProxyInfo proxyInfo) {
        return new WagonFetcher(wagon, listener, authenticationInfo, proxyInfo);
    }

    private static Path getIndexDirectory() {
        return Places.getCacheSubdirectory("mavenindex").toPath();
    }

    private static Path getIndexDirectory(final RepositoryInfo info) {
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

    // somewhat based on maven-indexer impl (in WagonHelper) prior to removal in maven-indexer 7.0.0
    private static class WagonFetcher implements ResourceFetcher {

        private final TransferListener listener;
        private final AuthenticationInfo authenticationInfo;
        private final ProxyInfo proxyInfo;
        private final Wagon wagon;

        public WagonFetcher(Wagon wagon, TransferListener listener, AuthenticationInfo authenticationInfo, ProxyInfo proxyInfo) {
            Objects.requireNonNull(wagon);
            Objects.requireNonNull(listener);
            this.wagon = wagon;
            this.listener = listener;
            this.authenticationInfo = authenticationInfo;
            this.proxyInfo = proxyInfo;
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
