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

import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipError;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.codehaus.plexus.PlexusConstants;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.index.*;
import org.apache.maven.index.Scanner;
import org.apache.maven.index.artifact.ArtifactPackagingMapper;
import org.apache.maven.index.context.DefaultIndexingContext;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexUtils;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.creator.OsgiArtifactIndexCreator;
import org.apache.maven.index.expr.StringSearchExpression;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdater;
import org.apache.maven.index.updater.ResourceFetcher;
import org.apache.maven.index.updater.WagonHelper;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.providers.http.HttpWagon;
import org.apache.maven.wagon.proxy.ProxyInfo;
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
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.indexer.api.NBArtifactInfo;
import org.netbeans.modules.maven.indexer.api.NBGroupInfo;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.QueryField;
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
import org.netbeans.modules.maven.indexer.spi.ResultImplementation;
import org.netbeans.modules.maven.indexer.spi.impl.Redo;
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

@ServiceProviders({
    @ServiceProvider(service=RepositoryIndexerImplementation.class),
    @ServiceProvider(service=RepositoryIndexQueryProvider.class, position = Integer.MAX_VALUE)
})
public class NexusRepositoryIndexerImpl implements RepositoryIndexerImplementation, RepositoryIndexQueryProvider,
        BaseQueries, ChecksumQueries, ArchetypeQueries, DependencyInfoQueries,
        ClassesQuery, ClassUsageQuery, GenericFindQuery, ContextLoadedQuery {

    private static final Logger LOGGER = Logger.getLogger(NexusRepositoryIndexerImpl.class.getName());
       
    static {
        // invokes static initializer of RepositoryQueries.class
        // that will assign value to the ACCESSOR field above
        Class<?> c = RepositoryQueries.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException x) {
            throw new ExceptionInInitializerError(x);
        }
    }     

    private PlexusContainer embedder;
    private Indexer indexer;
    private org.apache.maven.index.Scanner scanner;
    private SearchEngine searcher;
    private IndexUpdater remoteIndexUpdater;
    private ArtifactContextProducer contextProducer;
    private final Map<String, IndexingContext> indexingContexts = new ConcurrentHashMap<String, IndexingContext>();
    
    private boolean inited = false;
    /**
     * any reads, writes from/to index shal be done under mutex access.
     */
    private static final HashMap<String,Mutex> repoMutexMap = new HashMap<String, Mutex>(4);

    private static final Set<Mutex> indexingMutexes = new HashSet<Mutex>();
    private static final RequestProcessor RP = new RequestProcessor("indexing", 1);

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
    
    private Mutex getRepoMutex(RepositoryInfo repo) {
        return getRepoMutex(repo.getId());
    }
    
    private Mutex getRepoMutex(String repoId) {
        synchronized (repoMutexMap) {
            Mutex m = repoMutexMap.get(repoId);
            if (m == null) {
                m = new Mutex();
                repoMutexMap.put(repoId, m);
            }
            return m;
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
	            ClassWorld world = new ClassWorld();
	            ClassRealm embedderRealm = world.newRealm("maven.embedder", MavenEmbedder.class.getClassLoader()); //NOI18N
                ClassLoader indexerLoader = NexusRepositoryIndexerImpl.class.getClassLoader();
	            ClassRealm indexerRealm = world.newRealm("maven.indexer", indexerLoader); //NOI18N
	            ClassRealm plexusRealm = world.newRealm("plexus.core", indexerLoader); //NOI18N
	            //need to import META-INF/plexus stuff, otherwise the items in META-INF will not be loaded,
	            // and the Dependency Injection won't work.
	            plexusRealm.importFrom(embedderRealm.getId(), "META-INF/plexus"); //NOI18N
	            plexusRealm.importFrom(embedderRealm.getId(), "META-INF/maven"); //NOI18N
	            plexusRealm.importFrom(indexerRealm.getId(), "META-INF/plexus"); //NOI18N
	            plexusRealm.importFrom(indexerRealm.getId(), "META-INF/maven"); //NOI18N
	            config.setClassWorld(world);
                    config.setClassPathScanning( PlexusConstants.SCANNING_INDEX );
	            //#154755 - end
                embedder = new DefaultPlexusContainer(config);

                ComponentDescriptor<ArtifactContextProducer> desc = new ComponentDescriptor<ArtifactContextProducer>();
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
    
    public Map<String, IndexingContext> getIndexingContexts()
    {
        return Collections.unmodifiableMap( indexingContexts );
    }
    
    //TODO try to experiment with the non-forced version of the context addition
    public IndexingContext addIndexingContextForced( String id, String repositoryId, File repository,
        File indexDirectory, String repositoryUrl, String indexUpdateUrl,
        List<? extends IndexCreator> indexers )
        throws IOException
    {
        IndexingContext context =
            indexer.createIndexingContext( id, repositoryId, repository, indexDirectory, repositoryUrl, indexUpdateUrl,
                                           true, true, indexers );
        indexingContexts.put( context.getId(), context );
        return context;
    } 
    
    public void removeIndexingContext( IndexingContext context, boolean deleteFiles )
        throws IOException
    {
        if ( indexingContexts.containsKey( context.getId() ) )
        {
            indexingContexts.remove( context.getId() );
            indexer.closeIndexingContext( context, deleteFiles );
        }
    }    

    private boolean loadIndexingContext2(final RepositoryInfo info) throws IOException {
        boolean index = false;
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
                File loc = new File(getDefaultIndexLocation(), info.getId()); // index folder

                List<IndexCreator> creators = new ArrayList<IndexCreator>();
                try {
                    for (IndexCreator creator : embedder.lookupList(IndexCreator.class)) {
                        if (OsgiArtifactIndexCreator.ID.equals(creator.getId())) {
                            continue; //we are no interested in osgi related content in lucene documents or ArtifactInfo objects.
                            //they take up a lot of memory and we never query them AFAIK. (import/export packages can take up to 300k
                            //239915, 240150 + according to my knowledge we don't expose any api that would allow 3rd party plugins to query the osgi stuff
                        }
                        creators.add(creator);
                    }
                } catch (ComponentLookupException x) {
                    throw new IOException(x);
                }
                if (info.isLocal()) { // #164593
                    creators.add(new ArtifactDependencyIndexCreator());
                    creators.add(new ClassDependencyIndexCreator());
                } else {
                    creators.add(new NotifyingIndexCreator());
                }
                try {
                    addIndexingContextForced(
                            info.getId(), // context id
                            info.getId(), // repository id
                            info.isLocal() ? new File(info.getRepositoryPath()) : null, // repository folder
                            loc,
                            info.isRemoteDownloadable() ? info.getRepositoryUrl() : null, // repositoryUrl
                            info.isRemoteDownloadable() ? indexUpdateUrl : null,
                            creators);
                    LOGGER.log(Level.FINE, "using index creators: {0}", creators);
                } catch (IOException ex) {
                    LOGGER.log(Level.INFO, "Found a broken index at " + loc + " with loaded contexts " + getIndexingContexts().keySet(), ex);
                    break LOAD;
                }
                }

        //figure if a repository was removed from list, remove from context.
        Set<String> currents = new HashSet<String>();
        for (RepositoryInfo info2 : RepositoryPreferences.getInstance().getRepositoryInfos()) {
            currents.add(info2.getId());
        }
        Set<String> toRemove = new HashSet<String>(getIndexingContexts().keySet());
        toRemove.removeAll(currents);
        if (!toRemove.isEmpty()) {
            for (final String repo : toRemove) {
                try {
                    getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                        public @Override Void run() throws Exception {
                            unloadIndexingContext(repo);
                            return null;
                        }
                    });
                } catch (MutexException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        File loc = new File(getDefaultIndexLocation(), info.getId()); // index folder
        try {
            if (!loc.exists() || !new File(loc, "timestamp").exists()) {
                index = true;
                LOGGER.log(Level.FINER, "Index Not Available: {0} at: {1}", new Object[]{info.getId(), loc.getAbsolutePath()});
            } else if (!DirectoryReader.indexExists(new SimpleFSDirectory(loc.toPath()))) {
                index = true;
                LOGGER.log(Level.FINER, "Index Not Available: {0} at: {1}", new Object[]{info.getId(), loc.getAbsolutePath()});
            }
        } catch (IOException ex) {
            index = true;
            LOGGER.log(Level.FINER, "Index Not Available: " + info.getId() + " at: " + loc.getAbsolutePath(), ex);
        }
        
        return index;
    }

    private @CheckForNull IteratorSearchResponse repeatedPagedSearch(Query q, final List<IndexingContext> contexts, int count) throws IOException {
        IteratorSearchRequest isr = new IteratorSearchRequest(q, contexts, new NoJavadocSourceFilter());
        if (count > 0) {
            isr.setCount(count);
        }
        
        int MAX_MAX_CLAUSE = 1<<11;  // conservative maximum for too general queries, like "c:*class*"

        if (q instanceof BooleanQuery) {
            BooleanClause[] c = ((BooleanQuery)q).getClauses();
            if (c.length==1) {
                Query q1 = c[0].getQuery();
                if (q1 instanceof PrefixQuery && "u".equals(((PrefixQuery)q1).getPrefix().field())) {
                    // increase for queries like "+u:org.netbeans.modules|*" to succeed
                    MAX_MAX_CLAUSE = 1<<16;
                } else if (q1 instanceof TermQuery && "p".equals(((TermQuery) q1).getTerm().field())) {
                    // +p:nbm also produces several thousand hits
                    MAX_MAX_CLAUSE = 1<<16;
                }
            }
        }

        int oldMax = BooleanQuery.getMaxClauseCount();
        try {
            int max = oldMax;
            while (true) {
                IteratorSearchResponse response;
                try {
                    BooleanQuery.setMaxClauseCount(max);
                    response = searcher.searchIteratorPaged(isr, contexts);
                    LOGGER.log(Level.FINE, "passed on {0} clauses processing {1} with {2} hits", new Object[] {max, q, response.getTotalHitsCount()});
                    return response;
                } catch (BooleanQuery.TooManyClauses exc) {
                    LOGGER.log(Level.FINE, "TooManyClauses on {0} clauses processing {1}", new Object[] {max, q});
                    max *= 2;
                    if (max > MAX_MAX_CLAUSE) {
                        LOGGER.log(Level.WARNING, "Encountered more than {0} clauses processing {1}", new Object[] {MAX_MAX_CLAUSE, q});
                        return null;
                    } else {
                        continue;
                    }
                }
            }
        } finally {
            BooleanQuery.setMaxClauseCount(oldMax);
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
    
    @Messages({"# {0} - folder path",
               "# {1} - repository name",
               "MSG_NoSpace=There is not enough space in {0} to download and unpack the index for ''{1}''.",
               "# {0} - folder path",
               "# {1} - repository name",
               "MSG_SeemsNoSpace=It seems that there is not enough space in {0} to download and unpack the index for ''{1}''."})
    private void indexLoadedRepo(final RepositoryInfo repo, boolean updateLocal) throws IOException {
        Mutex mutex = getRepoMutex(repo);
        assert mutex.isWriteAccess();
        synchronized (indexingMutexes) {
            indexingMutexes.add(mutex);
        }
        boolean fetchFailed = false;
        long t = System.currentTimeMillis();
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
                            
                    ResourceFetcher fetcher = createFetcher(wagon, listener, wagonAuth, wagonProxy);
                    listener.setFetcher(fetcher);
                    IndexUpdateRequest iur = new IndexUpdateRequest(indexingContext, fetcher);
                    
                    NotifyingIndexCreator nic = null;
                    for (IndexCreator ic : indexingContext.getIndexCreators()) {
                        if (ic instanceof NotifyingIndexCreator) {
                            nic = (NotifyingIndexCreator) ic;
                            break;
                        }
                    }
                    if (nic != null) {
                        nic.start(listener);
                    }
                    try {
                        remoteIndexUpdater.fetchAndUpdateIndex(iur);
                    } catch (IllegalArgumentException ex) {
                        // This exception is raised from the maven-indexer.
                        // maven-indexer supported two formats:
                        // - legacy/zip: zipped lucene (v2.3) index files
                        // - gz: maven-indexer specific file format
                        // The legacy format is no longer supported and when
                        // the indexer encounters old index files it raises
                        // this exception
                        //
                        // Convert to IOException to utilize the existing error
                        // handling paths
                        fetchFailed = true;
                        throw new IOException("Failed to load maven-index for: " + indexingContext.getRepositoryUrl(), ex);
                    } catch (IOException ex) {
                        fetchFailed = true;
                        throw ex;
                    } finally {
                        if (nic != null) {
                            nic.end();
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
                        scan(indexingContext, null, repoListener, updateLocal);
                    } finally {
                        repoListener.close();
                    }
                }
            }
        } catch (IOException e) {            
            if(e.getCause() instanceof ResourceDoesNotExistException) {
                fireChange(repo, () -> repo.fireNoIndex());
            }
            File tmpFolder = Places.getCacheDirectory();
            // see also issue #250365
            String noSpaceLeftMsg = null;
            if(e.getMessage().contains("No space left on device")) {
                noSpaceLeftMsg = Bundle.MSG_NoSpace(tmpFolder.getAbsolutePath(), repo.getName());
            }
            
            long downloaded = listener != null ? listener.getUnits() * 1024 : -1;
            long usableSpace = -1;
            try {
                FileStore store = Files.getFileStore(tmpFolder.toPath());
                usableSpace = store.getUsableSpace();                    
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            LOGGER.log(Level.INFO, "Downloaded maven index file has size {0} (zipped). The usable space in {1} is {2}.", new Object[]{downloaded, tmpFolder, usableSpace});

            // still might be a problem with a too small tmp,
            // let's try to figure out ...
            if(noSpaceLeftMsg == null && downloaded > -1 && downloaded * 15 > usableSpace) {
                noSpaceLeftMsg = Bundle.MSG_SeemsNoSpace(tmpFolder.getAbsolutePath(), repo.getName());
            }

            if(noSpaceLeftMsg != null) {
                LOGGER.log(Level.INFO, null, e);
                IndexingNotificationProvider np = Lookup.getDefault().lookup(IndexingNotificationProvider.class);
                if(np != null) {
                    np.notifyError(noSpaceLeftMsg);
                    unloadIndexingContext(repo.getId());
                } else {
                    throw e;
                }
            } else {
                throw e;
            }
        } catch (Cancellation x) {
            throw new IOException("canceled indexing");
        } catch (ComponentLookupException x) {
            throw new IOException("could not find protocol handler for " + repo.getRepositoryUrl(), x);
        } finally {
            if(isDiag()) {
                LOGGER.log(Level.INFO, "Indexing of {0} took {1} millis.", new Object[]{repo.getId(), System.currentTimeMillis() - t});
            }
            synchronized (indexingMutexes) {
                indexingMutexes.remove(mutex);
            }
            if(!fetchFailed) {
                RepositoryPreferences.setLastIndexUpdate(repo.getId(), new Date());
                fireChange(repo, () -> repo.fireIndexChange());
            }
        }
    }

    private static boolean isDiag() {
        return Boolean.getBoolean("maven.indexing.diag");
    }

    //spawn the indexing into a separate thread..
    private void spawnIndexLoadedRepo(final RepositoryInfo repo) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                getRepoMutex(repo).writeAccess(new Mutex.Action<Void>() {
                    public @Override Void run() {
                        try {
                            indexLoadedRepo(repo, true);
                        } catch (IOException ex) {
                            LOGGER.log(Level.INFO, "could not (re-)index " + repo.getId(), ex);
                        }
                        return null;
                    }
                });
            }
        });
    }    

    @Override
    public void indexRepo(final RepositoryInfo repo) {
        LOGGER.log(Level.FINER, "Indexing Context: {0}", repo);
        try {
            RemoteIndexTransferListener.addToActive(Thread.currentThread());
            getRepoMutex(repo).writeAccess(new Mutex.Action<Void>() {
                public @Override Void run() {
                    try {
                        initIndexer();
                        assert indexer != null;
                        boolean noIndexExists = loadIndexingContext2(repo);
                        //here we always index repo, no matter what RepositoryPreferences.isIndexRepositories() value
                        indexLoadedRepo(repo, !noIndexExists);
                    } catch (IOException x) {
                        LOGGER.log(Level.INFO, "could not (re-)index " + repo.getId(), x);
                    }
                    return null;
                }
            });
        } finally {
            RemoteIndexTransferListener.removeFromActive(Thread.currentThread());
        }

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
    private void scan( final IndexingContext context, final String fromPath, final ArtifactScanningListener listener,
        final boolean update )
        throws IOException
    {
        final File repositoryDirectory = context.getRepository();
        if ( repositoryDirectory == null )
        {
            // nothing to scan
            return;
        }
 
        if ( !repositoryDirectory.exists() )
        {
            throw new IOException( "Repository directory " + repositoryDirectory + " does not exist" );
        }
 
        // always use cache directory when reindexing
        final File tmpDir = new File(Places.getCacheDirectory(), "tmp-" + context.getRepositoryId());
        if ( !tmpDir.mkdirs() )
        {
            throw new IOException( "Cannot create temporary directory: " + tmpDir );
        }
        final File tmpFile = new File(tmpDir, context.getId() + "-tmp");
 
        IndexingContext tmpContext = null;
        try
        {
            final FSDirectory directory = FSDirectory.open( tmpDir.toPath() );
            if ( update )
            {
                IndexUtils.copyDirectory( context.getIndexDirectory(), directory );
            }
            tmpContext = new DefaultIndexingContext( context.getId() + "-tmp", //
                                                     context.getRepositoryId(), //
                                                     context.getRepository(), //
                                                     directory, //
                                                     context.getRepositoryUrl(), //
                                                     context.getIndexUpdateUrl(), //
                                                     context.getIndexCreators(), //
                                                     true );
 
            scanner.scan( new ScanningRequest( tmpContext, //
                                               new DefaultScannerListener( tmpContext, embedder.lookup(IndexerEngine.class),
                                                                           update, listener ), fromPath ) );
 
            tmpContext.updateTimestamp( true );
            context.replace( tmpContext.getIndexDirectory() );
        }
        catch ( Exception ex )
        {
            throw new IOException("Error scanning context " + context.getId() + ": " + ex, ex);
        }
        finally
        {
            if ( tmpContext != null )
            {
                tmpContext.close( true );
            }
 
            if ( tmpFile.exists() )
            {
                tmpFile.delete();
            }
 
            FileUtils.deleteDirectory( tmpDir );
        }
    }    

    @Override
    public void updateIndexWithArtifacts(final RepositoryInfo repo, final Collection<Artifact> artifacts) {
        if (!RepositoryPreferences.isIndexRepositories()) {
            return;
        }
        final ArtifactRepository repository = EmbedderFactory.getProjectEmbedder().getLocalRepository();
        try {
            getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                public @Override Void run() throws Exception {
                    boolean index = loadIndexingContext2(repo);                    
                    if (index) {    
                        //do not bother indexing
                        return null; 
                    }
                    Map<String, IndexingContext> indexingContexts = getIndexingContexts();
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
                    Set<ArtifactContext> artifactContexts = new HashSet<ArtifactContext>();
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
                                BooleanQuery bq = new BooleanQuery();
                                String id = artifact.getGroupId() + ArtifactInfo.FS + artifact.getArtifactId() + ArtifactInfo.FS + artifact.getVersion() + ArtifactInfo.FS + ArtifactInfo.nvl(artifact.getClassifier());
                                bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST));
                                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(indexingContext), MAX_RESULT_COUNT);
                                add = response == null || response.getTotalHitsCount() == 0;
                                if (response != null) {
                                    response.close();
                                }
                            }
                            if (add) {
                                LOGGER.log(Level.FINE, "indexing " + artifact.getId() );
                                ArtifactContext ac = contextProducer.getArtifactContext(indexingContext, art);
                                artifactContexts.add(ac);
    //                            System.out.println("ac gav=" + ac.getGav());
    //                            System.out.println("ac pom=" + ac.getPom());
    //                            System.out.println("ac art=" + ac.getArtifact());
    //                            System.out.println("ac info=" + ac.getArtifactInfo());
    //                                assert indexingContext.getIndexSearcher() != null;
                            } else {
                                LOGGER.log(Level.FINE, "Skipped " + artifact.getId() + " already in index.");
                            }
                        }

                    }
                    try {
                        indexer.addArtifactsToIndex(artifactContexts, indexingContext);
                    } catch (ZipError err) {
                        LOGGER.log(Level.INFO, "#230581 concurrent access to local repository file. Skipping..", err);
                    }
                    
                    return null;
                }
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
            getRepoMutex(repo).writeAccess(new Mutex.ExceptionAction<Void>() {
                public @Override Void run() throws Exception {
                    boolean index = loadIndexingContext2(repo);
                    if (index) {                        
                        return null; //do not bother indexing
                    }
                    Map<String, IndexingContext> indexingContexts = getIndexingContexts();
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
                        indexer.deleteArtifactsFromIndex(Collections.singleton(contextProducer.getArtifactContext(indexingContext, pom)), indexingContext);
                    }
                    return null;
                }

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

    private File getDefaultIndexLocation() {
        return Places.getCacheSubdirectory("mavenindex");
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
                mutex.writeAccess(new Mutex.Action<Void>() {
                public @Override Void run() {
                    try {
                        boolean index = loadIndexingContext2(repo);
                        if (skipUnIndexed && index) {
                            if (!RepositoryPreferences.isIndexRepositories()) {
                                return null;
                            }
                            actionSkip.run(repo, null);
                            spawnIndexLoadedRepo(repo);
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
                }
            });
        }
    }
    }

    private ResultImplementation<String> filterGroupIds(final String prefix, final List<RepositoryInfo> repos) {
        Redo<String> redo = new Redo<String>() {
            @Override
            public void run(ResultImpl<String> result) {
                filterGroupIds(prefix, result, result.getSkipped(), false);
            }
        };
        ResultImpl<String> result = new ResultImpl<String>(redo);
        return filterGroupIds(prefix, result, repos, true);
    }
    
    private ResultImplementation<String> filterGroupIds(final String prefix, final ResultImpl<String> result, 
                                                            final List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final Set<String> groups = new TreeSet<String>(result.getResults());
        final List<RepositoryInfo> slowCheck = new ArrayList<RepositoryInfo>();
        
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                Set<String> all= context.getAllGroups();
                if (all.size() > 0) {
                    if (prefix.length() == 0) {
                        groups.addAll(all);
                    } else {
                        for (String gr : all) {
                            if (gr.startsWith(prefix)) {
                                groups.add(gr);
                            }
                        }
                    }
                } else {
                    slowCheck.add(repo);
                }
            }

        }, skipAction, skipUnIndexed);
        
//        //the slow check kicking in is nowadays very rare, used to be a workaround for old versions of indexing data..
// #240150 can cause OOME as the number of grouped results (ArtifactInfo instances) in this case is huge.
//        
//        iterate(slowCheck, new RepoAction() {
//            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
//                BooleanQuery bq = new BooleanQuery();
//                bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, prefix)), BooleanClause.Occur.MUST));
//                GroupedSearchRequest gsr = new GroupedSearchRequest(bq, new GGrouping(), new Comparator<String>() {
//                    @Override public int compare(String o1, String o2) {
//                        return o1.compareTo(o2);
//                    }
//                });
//                GroupedSearchResponse response = searcher.searchGrouped(gsr, Collections.singletonList(context));
//                groups.addAll(response.getResults().keySet());
//            }
//        }, skipAction, skipUnIndexed);
        
        result.setResults(groups);
        return result;
    }

    @Override
    public ResultImplementation<String> getGAVsForPackaging(final String packaging, List<RepositoryInfo> repos) {
        ResultImpl<String> result = new ResultImpl<>(new Redo<String>() {
            @Override
            public void run(ResultImpl<String> result) {
                getGAVsForPackaging(packaging, result, result.getSkipped(), false);
            }
        });
        return getGAVsForPackaging(packaging,result, repos, true);
    }
    
    private ResultImplementation<String> getGAVsForPackaging(final String packaging, final ResultImpl<String> result, 
                                             List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<String> infos = new ArrayList<String>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                bq.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, packaging)), BooleanClause.Occur.MUST));
                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(context), NO_CAP_RESULT_COUNT);
                if (response != null) {
                   try {
                        for (ArtifactInfo ai : response.iterator()) {
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
            }
        }, skipAction, skipUnIndexed);
        result.setResults(infos);
        return result;        
    }
    
    @Override
    public ResultImplementation<NBVersionInfo> getRecords(final String groupId, final String artifactId, final String version, List<RepositoryInfo> repos) {
        ResultImpl<NBVersionInfo> result = new ResultImpl<>(new Redo<NBVersionInfo>() {
            @Override
            public void run(ResultImpl<NBVersionInfo> result) {
                getRecords(groupId, artifactId, version, result, result.getSkipped(), false);
            }
        });
        return getRecords(groupId, artifactId, version, result, repos, true);
    }
    
    private ResultImplementation<NBVersionInfo> getRecords(final String groupId, final String artifactId, final String version, final ResultImpl<NBVersionInfo> result, 
                                             List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                String id = groupId + ArtifactInfo.FS + artifactId + ArtifactInfo.FS + version + ArtifactInfo.FS;
                bq.add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST));
                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(context), MAX_RESULT_COUNT);
                if (response != null) {
                   try {
                        for (ArtifactInfo ai : response.iterator()) {
                            infos.add(convertToNBVersionInfo(ai));
                        }
                    } finally {
                        result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                        result.addTotalResultCount(response.getTotalHitsCount());
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        doSortIssue226100(infos);
        result.setResults(infos);
        return result;
    }

    @Override
    public ResultImplementation<String> getArtifacts(final String groupId, final List<RepositoryInfo> repos) {
        ResultImpl<String> result = new ResultImpl<>(new Redo<String>() {
            @Override
            public void run(ResultImpl<String> result) {
                getArtifacts(groupId, result, result.getSkipped(), false);
            }
        });
        return getArtifacts(groupId, result, repos, true);
    }
    
    private  ResultImplementation<String> getArtifacts(final String groupId, final ResultImpl<String> result, final List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final Set<String> artifacts = new TreeSet<String>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                String id = groupId + ArtifactInfo.FS;
                bq.add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST));
                //mkleint: this is not capped, because only a string is collected (and collapsed), the rest gets CGed fast
                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(context), NO_CAP_RESULT_COUNT);
                if (response != null) {
                    try {
                    for (ArtifactInfo artifactInfo : response.getResults()) {
                        artifacts.add(artifactInfo.getArtifactId());
                    }
                    } finally {
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        result.setResults(artifacts);
        return result;
    }

    @Override
    public ResultImplementation<NBVersionInfo> getVersions(final String groupId, final String artifactId, List<RepositoryInfo> repos) {
        ResultImpl<NBVersionInfo> result = new ResultImpl<>(new Redo<NBVersionInfo>() {
            @Override
            public void run(ResultImpl<NBVersionInfo> result) {
                getVersions(groupId, artifactId, result, result.getSkipped(), false);
            }
        });
        return getVersions(groupId, artifactId, result, repos, true);
    }
    private ResultImplementation<NBVersionInfo> getVersions(final String groupId, final String artifactId, final ResultImpl<NBVersionInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                String id = groupId + ArtifactInfo.FS + artifactId + ArtifactInfo.FS;
                bq.add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST));
                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(context), MAX_RESULT_COUNT);
                if (response != null) {
                   try {
                        for (ArtifactInfo ai : response.iterator()) {
                            infos.add(convertToNBVersionInfo(ai));
                        }
                    } finally {
                        result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                        result.addTotalResultCount(response.getTotalHitsCount());
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        doSortIssue226100(infos);
        result.setResults(infos);
        return result;
    }

    @Override
    public ResultImplementation<NBVersionInfo> findVersionsByClass(final String className, List<RepositoryInfo> repos) {
        ResultImpl<NBVersionInfo> result = new ResultImpl<>(new Redo<NBVersionInfo>() {
            @Override
            public void run(ResultImpl<NBVersionInfo> result) {
                findVersionsByClass(className, result, result.getSkipped(), false);
            }
        });
        return findVersionsByClass(className, result, repos, true);
    }
    
    private ResultImplementation<NBVersionInfo> findVersionsByClass(final String className, final ResultImpl<NBVersionInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                String clsname = className.replace(".", "/");
                    while (!clsname.isEmpty() && (clsname.startsWith("*") || clsname.startsWith("?"))) {
                        //#238740
                        clsname = clsname.substring(1);
                    }
                    if (clsname.isEmpty()) {
                        return;
                    }
                
                Query q = setBooleanRewrite(constructQuery(MAVEN.CLASSNAMES, clsname.toLowerCase(Locale.ENGLISH)));
                IteratorSearchResponse response = repeatedPagedSearch(q, Collections.singletonList(context), MAX_RESULT_COUNT);
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
        ResultImpl<RepositoryQueries.ClassUsage> result = new ResultImpl<>(new Redo<RepositoryQueries.ClassUsage>() {
            @Override
            public void run(ResultImpl<RepositoryQueries.ClassUsage> result) {
                findClassUsages(className, result, result.getSkipped(), false);
            }
        });
        return findClassUsages(className, result, repos, true);
        
    }
    
    private ResultImplementation<RepositoryQueries.ClassUsage> findClassUsages(final String className, ResultImpl<RepositoryQueries.ClassUsage> result, @NullAllowed List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        List<RepositoryInfo> localRepos = new ArrayList<RepositoryInfo>();
        if (repos == null) {
            repos = RepositoryPreferences.getInstance().getRepositoryInfos();
        }
        for (RepositoryInfo repo : repos) {
            if (repo.isLocal()) {
                localRepos.add(repo);
            }
        }
        final List<RepositoryQueries.ClassUsage> results = new ArrayList<RepositoryQueries.ClassUsage>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(localRepos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                ClassDependencyIndexCreator.search(className, indexer, Collections.singletonList(context), results);
            }
        }, skipAction, skipUnIndexed);
        Collections.sort(results, new Comparator<RepositoryQueries.ClassUsage>() {
            @Override public int compare(RepositoryQueries.ClassUsage r1, RepositoryQueries.ClassUsage r2) {
                return r1.getArtifact().compareTo(r2.getArtifact());
            }
        });
        result.setResults(results);
        return result;
    }
    
    @Override
    public ResultImplementation<NBVersionInfo> findDependencyUsage(final String groupId, final String artifactId, final String version, @NullAllowed List<RepositoryInfo> repos) {
        ResultImpl<NBVersionInfo> result = new ResultImpl<>(new Redo<NBVersionInfo>() {
            @Override
            public void run(ResultImpl<NBVersionInfo> result) {
                findDependencyUsage(groupId, artifactId, version, result, result.getSkipped(), false);
            }
        });
        return findDependencyUsage(groupId, artifactId, version, result, repos, true);
    }
    private ResultImplementation<NBVersionInfo> findDependencyUsage(String groupId, String artifactId, String version, final ResultImpl<NBVersionInfo> result, @NullAllowed List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final Query q = ArtifactDependencyIndexCreator.query(groupId, artifactId, version);
        final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                IteratorSearchResponse response = repeatedPagedSearch(q, Collections.singletonList(context), MAX_RESULT_COUNT);
                if (response != null) {
                    try {
                        for (ArtifactInfo ai : response.iterator()) {
                            infos.add(convertToNBVersionInfo(ai));
                        }
                    } finally {
                        result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                        result.addTotalResultCount(response.getTotalHitsCount());
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        result.setResults(infos);
        return result;
    }
    
    @Override
    public ResultImplementation<NBGroupInfo> findDependencyUsageGroups(final String groupId, final String artifactId, final String version, List<RepositoryInfo> repos) {
        ResultImpl<NBGroupInfo> result = new ResultImpl<>(new Redo<NBGroupInfo>() {
            @Override
            public void run(ResultImpl<NBGroupInfo> result) {
                findDependencyUsageGroups(groupId, artifactId, version, result, result.getSkipped(), false);
            }
        });
        return findDependencyUsageGroups(groupId, artifactId, version, result, repos, true);
    }

    private ResultImplementation<NBGroupInfo> findDependencyUsageGroups(String groupId, String artifactId, String version, ResultImpl<NBGroupInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        //tempmaps
        Map<String, NBGroupInfo> groupMap = new HashMap<String, NBGroupInfo>();
        Map<String, NBArtifactInfo> artifactMap = new HashMap<String, NBArtifactInfo>();
        List<NBGroupInfo> groupInfos = new ArrayList<NBGroupInfo>(result.getResults());
        ResultImpl<NBVersionInfo> res = new ResultImpl<>(new Redo<NBVersionInfo>() {

            @Override
            public void run(ResultImpl<NBVersionInfo> result) {
                //noop will not be called
            }
        });
        findDependencyUsage(groupId, artifactId, version, res, repos, skipUnIndexed);
        convertToNBGroupInfo(res.getResults(),
                groupMap, artifactMap, groupInfos);
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
        ResultImpl<NBVersionInfo> result = new ResultImpl<>(new Redo<NBVersionInfo>() {
            @Override
            public void run(ResultImpl<NBVersionInfo> result) {
                findBySHA1(sha1, result, result.getSkipped(), false);
            }
        });
        return findBySHA1(sha1, result, repos, true);
    }
    
    private ResultImplementation<NBVersionInfo> findBySHA1(final String sha1, final ResultImpl<NBVersionInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                bq.add(new BooleanClause((setBooleanRewrite(constructQuery(MAVEN.SHA1, sha1))), BooleanClause.Occur.SHOULD));
                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(context), MAX_RESULT_COUNT);
                if (response != null) {
                    try {
                        for (ArtifactInfo ai : response.iterator()) {
                            infos.add(convertToNBVersionInfo(ai));
                        }
                    } finally {
                        result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                        result.addTotalResultCount(response.getTotalHitsCount());
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        doSortIssue226100(infos);
        result.setResults(infos);
        return result;
    }

    @Override
    public ResultImplementation<NBVersionInfo> findArchetypes(List<RepositoryInfo> repos) {
        ResultImpl<NBVersionInfo> result = new ResultImpl<>(new Redo<NBVersionInfo>() {
            @Override
            public void run(ResultImpl<NBVersionInfo> result) {
                findArchetypes( result, result.getSkipped(), false);
            }
        });
        return findArchetypes( result, repos, true);
    }
    
    private ResultImplementation<NBVersionInfo> findArchetypes(final ResultImpl<NBVersionInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                // XXX also consider using NexusArchetypeDataSource
                bq.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-archetype")), BooleanClause.Occur.MUST)); //NOI18N
                FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
                /* There are >512 archetypes in Central, and we want them all in ChooseArchetypePanel
                fsr.setCount(MAX_RESULT_COUNT);
                */
                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(context), NO_CAP_RESULT_COUNT);
                if (response != null) {
                    try {
                        for (ArtifactInfo ai : response.iterator()) {
                            infos.add(convertToNBVersionInfo(ai));
                        }
                    } finally {
                        result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                        result.addTotalResultCount(response.getTotalHitsCount());
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        doSortIssue226100(infos);
        result.setResults(infos);
        return result;
    }

    @Override
    public ResultImplementation<String> filterPluginArtifactIds(final String groupId, final String prefix, List<RepositoryInfo> repos) {
        ResultImpl<String> result = new ResultImpl<>(new Redo<String>() {
            @Override
            public void run(ResultImpl<String> result) {
                filterPluginArtifactIds(groupId, prefix, result, result.getSkipped(), false);
            }
        });
        return filterPluginArtifactIds(groupId, prefix, result, repos, true);
    }
    
    private ResultImplementation<String> filterPluginArtifactIds(final String groupId, final String prefix, ResultImpl<String> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final Set<String> artifacts = new TreeSet<String>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                String id = groupId + ArtifactInfo.FS + prefix;
                bq.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-plugin")), BooleanClause.Occur.MUST));
                bq.add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST));
                //mkleint: this is not capped, because only a string is collected (and collapsed), the rest gets CGed fast
                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(context), NO_CAP_RESULT_COUNT);
                if (response != null) {
                    try {
                        for (ArtifactInfo artifactInfo : response.getResults()) {
                            artifacts.add(artifactInfo.getArtifactId());
                        }
                    } finally {
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        result.setResults(artifacts);
        return result;
    }

    @Override
    public ResultImplementation<String> filterPluginGroupIds(final String prefix, List<RepositoryInfo> repos) {
        ResultImpl<String> result = new ResultImpl<>(new Redo<String>() {
            @Override
            public void run(ResultImpl<String> result) {
                filterPluginGroupIds( prefix, result, result.getSkipped(), false);
            }
        });
        return filterPluginGroupIds( prefix, result, repos, true);
    }
    
    private ResultImplementation<String> filterPluginGroupIds(final String prefix, ResultImpl<String> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final Set<String> artifacts = new TreeSet<String>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
                bq.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-plugin")), BooleanClause.Occur.MUST));
                if (prefix.length() > 0) { //heap out of memory otherwise
                    bq.add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.GROUP_ID, prefix))), BooleanClause.Occur.MUST));
                }
                //mkleint: this is not capped, because only a string is collected (and collapsed), the rest gets CGed fast
                IteratorSearchResponse response = repeatedPagedSearch(bq, Collections.singletonList(context), NO_CAP_RESULT_COUNT);
                if (response != null) {
                    try {
                        for (ArtifactInfo artifactInfo : response.getResults()) {
                            artifacts.add(artifactInfo.getGroupId());
                        }
                    } finally {
                        response.close();
                    }
                }
            }
        }, skipAction, skipUnIndexed);
        result.setResults(artifacts);
        return result;
    }

    @Override
    public ResultImplementation<NBVersionInfo> find(final List<QueryField> fields, List<RepositoryInfo> repos) {
        ResultImpl<NBVersionInfo> result = new ResultImpl<>(new Redo<NBVersionInfo>() {
            @Override
            public void run(ResultImpl<NBVersionInfo> result) {
                find( fields, result, result.getSkipped(), false);
            }
        });
        return find(fields,  result, repos, true);
    }
    private ResultImplementation<NBVersionInfo> find(final List<QueryField> fields, final ResultImpl<NBVersionInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, new RepoAction() {
            @Override public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
                BooleanQuery bq = new BooleanQuery();
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
                            String aid = one.replaceAll("-", "?").replaceAll("\\.", "?");
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
                            String gid = one.replaceAll("-", "?").replaceAll("\\.", "?");
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
                IteratorSearchResponse resp = repeatedPagedSearch(bq, Collections.singletonList(context), MAX_RESULT_COUNT);
                if (resp != null) {
                    try {
                        for (ArtifactInfo ai : resp.iterator()) {
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
        } catch (IllegalStateException ex) {
//            doLogError226100(infos, ex);
        } catch (IllegalArgumentException ex2) {
//            doLogError226100(infos, ex2);
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
        final List<RepositoryInfo> toRet = new ArrayList<RepositoryInfo>(repos.size());
        for (final RepositoryInfo repo : repos) {
            File loc = new File(getDefaultIndexLocation(), repo.getId()); // index folder
            try {
                if (loc.exists() && new File(loc, "timestamp").exists() && DirectoryReader.indexExists(new SimpleFSDirectory(loc.toPath()))) {
                    toRet.add(repo);
                }
            } catch (IOException ex) {
                LOGGER.log(Level.FINER, "Index Not Available: " +repo.getId() + " at: " + loc.getAbsolutePath(), ex);
            }
        }
        return toRet;
    }

    private String toNexusField(String field) {
        if (QueryField.FIELD_ARTIFACTID.equals(field)) {
            return ArtifactInfo.ARTIFACT_ID;
        } else if (QueryField.FIELD_GROUPID.equals(field)) {
            return ArtifactInfo.GROUP_ID;
        } else if (QueryField.FIELD_VERSION.equals(field)) {
            return ArtifactInfo.VERSION;
        } else if (QueryField.FIELD_CLASSES.equals(field)) {
            return ArtifactInfo.NAMES;
        } else if (QueryField.FIELD_NAME.equals(field)) {
            return ArtifactInfo.NAME;
        } else if (QueryField.FIELD_DESCRIPTION.equals(field)) {
            return ArtifactInfo.DESCRIPTION;
        } else if (QueryField.FIELD_PACKAGING.equals(field)) {
            return ArtifactInfo.PACKAGING;
        }
        return field;
    }

    private Collection<NBVersionInfo> postProcessClasses(IteratorResultSet artifactInfos, String classname) {
        List<NBVersionInfo> toRet = new ArrayList<NBVersionInfo>();
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
        ArrayList<ArtifactInfo> altArtifactInfos = new ArrayList<ArtifactInfo>();
        Iterator<ArtifactInfo> it = artifactInfos.iterator();        
        while (it.hasNext()) {
            ArtifactInfo ai = it.next();
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
        List<NBVersionInfo> bVersionInfos = new ArrayList<NBVersionInfo>();
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
//            nbvi.setSha(ai.sha1);
            nbvi.setLastModified(ai.getLastModified());
            nbvi.setSize(ai.getSize());
        nbvi.setLuceneScore(ai.getLuceneScore());
        return nbvi;
        }
    
    private static Query setBooleanRewrite (final Query q) {
        if (q instanceof MultiTermQuery) {
            ((MultiTermQuery)q).setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE);
        } else if (q instanceof BooleanQuery) {
            for (BooleanClause c : ((BooleanQuery)q).getClauses()) {
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
            if ("javadoc".equals(ai.getClassifier()) || "sources".equals(ai.getClassifier())) {
                return false;
            }
            return true;
        }
        
    }
    
    private WagonHelper.WagonFetcher createFetcher(final Wagon wagon, TransferListener listener, AuthenticationInfo authenticationInfo, ProxyInfo proxyInfo) {
        if(isDiag()) {
            return new WagonHelper.WagonFetcher(wagon, listener, authenticationInfo, proxyInfo) {
                @Override
                public InputStream retrieve(String name) throws IOException, FileNotFoundException {
                    String id = wagon.getRepository().getId();
                    if(name.contains("properties") && System.getProperty("maven.diag.index.properties." + id) != null) { // NOI18N
                        LOGGER.log(Level.INFO, "maven indexer will use local properties file: {0}", System.getProperty("maven.diag.index.properties." + id)); // NOI18N
                        return new FileInputStream(new File(System.getProperty("maven.diag.index.properties." + id))); // NOI18N
                    } else if(name.contains(".gz") && System.getProperty("maven.diag.index.gz." + id) != null) { // NOI18N
                        LOGGER.log(Level.INFO, "maven indexer will use gz file: {0}", System.getProperty("maven.diag.index.gz." + id)); // NOI18N
                        return new FileInputStream(new File(System.getProperty("maven.diag.index.gz." + id))); // NOI18N
                    }
                    return super.retrieve(name);
                }
            };
        } else {
            return new WagonHelper.WagonFetcher(wagon, listener, authenticationInfo, proxyInfo);
        }
    }
    
}
