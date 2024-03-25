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

package org.netbeans.modules.maven.embedder;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.cli.configuration.SettingsXmlConfigurationProcessor;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequestPopulationException;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.mapping.Lifecycle;
import org.apache.maven.lifecycle.mapping.LifecycleMapping;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.RepositoryPolicy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
import org.netbeans.modules.maven.embedder.impl.NBModelBuilder;
import org.netbeans.modules.maven.embedder.impl.NbRepositoryCache;
import org.netbeans.modules.maven.embedder.impl.NbWorkspaceReader;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.BaseUtilities;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.impl.VersionResolver;
import org.eclipse.aether.internal.impl.EnhancedLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.aether.util.repository.DefaultAuthenticationSelector;
import org.eclipse.aether.util.repository.DefaultMirrorSelector;
import org.eclipse.aether.util.repository.DefaultProxySelector;
import org.netbeans.modules.maven.embedder.impl.NbVersionResolver2;

/**
 * Handle for the embedded Maven system, used to parse POMs and more.
 * Since 2.36, all File instances in Artifacts, MavenProjects or Models should be pre-emptively normalized.
 */
public final class MavenEmbedder {

    private static final Logger LOG = Logger.getLogger(MavenEmbedder.class.getName());
    private final PlexusContainer plexus;
    private final DefaultMaven maven;
    private final ProjectBuilder projectBuilder;
    private final RepositorySystem repositorySystem;
    private final MavenExecutionRequestPopulator populator;
    private final SettingsBuilder settingsBuilder;
    private final EmbedderConfiguration embedderConfiguration;
    private final SettingsDecrypter settingsDecrypter;
    private final NbVersionResolver2 versionResolver;
    private long settingsTimestamp;
    private static final Object lastLocalRepositoryLock = new Object();
    private static URI lastLocalRepository;
    private Settings settings;

    MavenEmbedder(EmbedderConfiguration configuration) throws ComponentLookupException {
        embedderConfiguration = configuration;
        plexus = configuration.getContainer();
        this.maven = (DefaultMaven) plexus.lookup(Maven.class);
        this.projectBuilder = plexus.lookup(ProjectBuilder.class);
        this.repositorySystem = plexus.lookup(RepositorySystem.class);
        this.settingsBuilder = plexus.lookup(SettingsBuilder.class);
        this.populator = plexus.lookup(MavenExecutionRequestPopulator.class);
        settingsDecrypter = plexus.lookup(SettingsDecrypter.class);
        
        VersionResolver vr = plexus.lookup(VersionResolver.class);
        if (vr instanceof NbVersionResolver2) {
            versionResolver = (NbVersionResolver2)vr;
        } else {
            versionResolver = null;
        }
    }
    
    public PlexusContainer getPlexus() {
        return plexus;
    }

    /**
     * contains System.getProperties() with some netbeans IDE JVM related items filtered out + environment variables prefixed with "env." 
     * @return 
     */
    public Properties getSystemProperties() {
        return embedderConfiguration.getSystemProperties();
    }

    boolean isOffline() {
        return embedderConfiguration.isOffline();
    }

    public ArtifactRepository getLocalRepository() {
        try {
            String localRepositoryPath = getSettings().getLocalRepository();
            if (localRepositoryPath != null) {
                return repositorySystem.createLocalRepository(FileUtil.normalizeFile(new File(localRepositoryPath)));
            }
            return repositorySystem.createDefaultLocalRepository();
        } catch (InvalidRepositoryException ex) {
            // can't happen
            throw new IllegalStateException(ex);
        }
    }
    
    /**
     * 
     * @return normalized File for local repository root
     * @since 2.26
     */
    public File getLocalRepositoryFile() {
        return FileUtil.normalizeFile(new File(getLocalRepository().getBasedir()));
    }
    
    //only for unit tests..
    private static Settings testSettings;

    @SuppressWarnings("NestedSynchronizedStatement")
    @org.netbeans.api.annotations.common.SuppressWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public synchronized Settings getSettings() {
        if (Boolean.getBoolean("no.local.settings")) { // for unit tests
            if (testSettings == null) {
                testSettings = new Settings();
            }
            return testSettings; // could instead make public void setSettings(Settings settingsOverride)
        }
        File settingsXml = embedderConfiguration.getSettingsXml();
        long newSettingsTimestamp = settingsXml.hashCode() ^ settingsXml.lastModified() ^ SettingsXmlConfigurationProcessor.DEFAULT_USER_SETTINGS_FILE.lastModified();
        // could be included but currently constant: hashCode() of those files; getSystemProperties.hashCode()
        if (settings != null && settingsTimestamp == newSettingsTimestamp) {
            LOG.log(Level.FINER, "settings.xml cache hit for {0}", this);
            return settings;
        }
        LOG.log(Level.FINE, "settings.xml cache miss for {0}", this);
        SettingsBuildingRequest req = new DefaultSettingsBuildingRequest();
        req.setGlobalSettingsFile(settingsXml);
        req.setUserSettingsFile(SettingsXmlConfigurationProcessor.DEFAULT_USER_SETTINGS_FILE);
        req.setSystemProperties(getSystemProperties());
        req.setUserProperties(embedderConfiguration.getUserProperties());
        try {
            settings = settingsBuilder.build(req).getEffectiveSettings();
            //now update the UNOWNED marker for FOQ at root of the local repository.
            String localRep = settings.getLocalRepository();
            if (localRep == null) {
                localRep = RepositorySystem.defaultUserLocalRepository.getAbsolutePath();
            }
            URI localRepU = BaseUtilities.toURI(FileUtil.normalizeFile(new File(localRep)));
            synchronized (lastLocalRepositoryLock) {
                if (lastLocalRepository == null || !lastLocalRepository.equals(localRepU)) {
                    FileOwnerQuery.markExternalOwner(localRepU, FileOwnerQuery.UNOWNED, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
                    if (lastLocalRepository != null) {
                        FileOwnerQuery.markExternalOwner(lastLocalRepository, null, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
                    }
                    lastLocalRepository = localRepU;
                }
            }
            
            settingsTimestamp = newSettingsTimestamp;
            return settings;
        } catch (SettingsBuildingException x) {
            LOG.log(Level.FINE, null, x); // #192768: do not even bother logging to console by default, too noisy
            return new Settings();
        }
    }
    
    @Deprecated
    public MavenExecutionResult readProjectWithDependencies(MavenExecutionRequest req) {
        return readProjectWithDependencies(req, true);
    }
    
    public MavenExecutionResult readProjectWithDependencies(MavenExecutionRequest req, boolean useWorkspaceResolution) {
        if (useWorkspaceResolution) {
            req.setWorkspaceReader(new NbWorkspaceReader(versionResolver));
        }
        File pomFile = req.getPom();
        MavenExecutionResult result = new DefaultMavenExecutionResult();
        try {
            ProjectBuildingRequest configuration = req.getProjectBuildingRequest();
            configuration.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
            configuration.setResolveDependencies(true);
            configuration.setRepositorySession(maven.newRepositorySession(req));
            ProjectBuildingResult projectBuildingResult = projectBuilder.build(pomFile, configuration);
            result.setProject(projectBuildingResult.getProject());
            result.setDependencyResolutionResult(projectBuildingResult.getDependencyResolutionResult());
        } catch (ProjectBuildingException ex) {
            //don't add the exception here. this should come out as a build marker, not fill
            //the error logs with msgs
            return result.addException(ex);
        }
        normalizePaths(result.getProject());
        return result;
    }

    public List<MavenExecutionResult> readProjectsWithDependencies(MavenExecutionRequest req, List<File> poms, boolean useWorkspaceResolution) {
        if (useWorkspaceResolution) {
            req.setWorkspaceReader(new NbWorkspaceReader(versionResolver));
        }
//        File pomFile = req.getPom();
        
        Map<File, MavenExecutionResult> results = new HashMap<>(poms.size());
        List<ProjectBuildingResult> projectBuildingResults = new LinkedList<>();
        
        ProjectBuildingRequest configuration = req.getProjectBuildingRequest();
        configuration.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        configuration.setResolveDependencies(true);
        configuration.setRepositorySession(maven.newRepositorySession(req));

        try {
            projectBuildingResults = projectBuilder.build(poms, true, configuration);
        } catch (ProjectBuildingException ex) {
            //don't add the exception here. this should come out as a build marker, not fill
            //the error logs with msgs
            List<ProjectBuildingResult> pbrs = ex.getResults();
            if(pbrs != null) {
                for (ProjectBuildingResult pbr : pbrs) {
                    if(!pbr.getProblems().isEmpty()) {
                        // exception holds info about all problematic projects
                        DefaultMavenExecutionResult r = new DefaultMavenExecutionResult();
                        results.put(pbr.getPomFile(), r.addException(ex));
                    } else {
                        setResult(pbr, results);
                    }
                }
            } else {
                for (File f : poms) {
                    DefaultMavenExecutionResult r = new DefaultMavenExecutionResult();
                    results.put(f, r.addException(ex));                
                } 
            }
        }

//        for (File pom : poms) {
//            try {
//                ProjectBuildingResult rs = projectBuilder.build(pom, configuration);
//                if(rs != null) {
//                    projectBuildingResults.add(rs);
//                }
//            } catch (ProjectBuildingException ex) {
//                //don't add the exception here. this should come out as a build marker, not fill
//                //the error logs with msgs
//                List<ProjectBuildingResult> pbrs = ex.getResults();
//                if(pbrs != null) {
//                    assert pbrs.size() == 1;
//                    for (ProjectBuildingResult pbr : pbrs) {
//                        if(!pbr.getProblems().isEmpty()) {
//                            // exception holds info about all problematic projects
//                            DefaultMavenExecutionResult r = new DefaultMavenExecutionResult();
//                            results.put(pbr.getPomFile(), r.addException(ex));
//                        } else {
//                            setResult(pbr, results, null);
//                        }
//                    }
//                } else {
////                        for (File f : poms) {
//                        DefaultMavenExecutionResult r = new DefaultMavenExecutionResult();
//                        results.put(pom, r.addException(ex));                
////                        } 
//                }
//            }
//        }

        for (ProjectBuildingResult pbr : projectBuildingResults) {
            MavenExecutionResult r = results.get(pbr.getPomFile());
            if( r == null ) {
                setResult(pbr, results);
            } 
        }
        return new ArrayList<>(results.values());
    }

    public Artifact createArtifactWithClassifier(@NonNull String groupId, @NonNull String artifactId, @NonNull String version, String type, String classifier) {
        return repositorySystem.createArtifactWithClassifier(groupId, artifactId, version, type, classifier);
    }

    public Artifact createArtifact(@NonNull String groupId, @NonNull String artifactId, @NonNull String version, @NonNull String packaging) {
         return repositorySystem.createArtifact(groupId,  artifactId,  version,  packaging);
    }

    public Artifact createArtifact(@NonNull String groupId, @NonNull String artifactId, @NonNull String version, String scope, String type) {
         return repositorySystem.createArtifact( groupId,  artifactId,  version,   scope,  type);
    }

    public Artifact createProjectArtifact(@NonNull String groupId, @NonNull String artifactId, @NonNull String version) {
        return repositorySystem.createProjectArtifact(groupId, artifactId, version);
    }
    
    
    /**
     * using this method one creates an ArtifactRepository instance with injected mirrors and proxies
     * @param url
     * @param id
     * @return 
     */
    public ArtifactRepository createRemoteRepository(String url, String id) {
        setUpLegacySupport();
        ArtifactRepositoryFactory fact = lookupComponent(ArtifactRepositoryFactory.class);
        assert fact!=null : "ArtifactRepositoryFactory component not found in maven";
        ArtifactRepositoryPolicy snapshotsPolicy = new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);
        ArtifactRepositoryPolicy releasesPolicy = new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);
        return fact.createArtifactRepository(id, url, new DefaultRepositoryLayout(), snapshotsPolicy, releasesPolicy);
    }
    

    /**
     * 
     * @param sources
     * @param remoteRepositories - these instances need to be properly mirrored and proxied. Either by creating via EmbedderFactory.createRemoteRepository()
     *              or by using instances from MavenProject
     * @param localRepository
     * @throws ArtifactResolutionException
     * @throws ArtifactNotFoundException 
     * @deprecated the Maven API used swallows certain {@link ArtifactNotFoundException} and does not report properly to the caller. Use {@link #resolveArtifact} instead.
     */
    @Deprecated
    public void resolve(Artifact sources, List<ArtifactRepository> remoteRepositories, ArtifactRepository localRepository) throws ArtifactResolutionException, ArtifactNotFoundException {
        setUpLegacySupport();
        ArtifactResolutionRequest req = new ArtifactResolutionRequest();
        req.setLocalRepository(localRepository);
        req.setRemoteRepositories(remoteRepositories);
        req.setArtifact(sources);
        req.setOffline(isOffline());
        ArtifactResolutionResult result = repositorySystem.resolve(req);
        normalizePath(sources);
        // XXX check result for exceptions and throw them now?
        for (Exception ex : result.getExceptions()) {
            LOG.log(Level.FINE, null, ex);
        }
    }
    
    /**
     * Resolves the artifact. Attaches version info according to project's dependency management configuration, resolves to a local file. Throws an exception
     * on missing on unresolvable artifact, trying to mimic the real build's behaviour. This method supersedes the deprecated {@link #resolve}.
     * 
     * @param toResolve artifact to resolve
     * @param remoteRepositories - these instances need to be properly mirrored and proxied. Either by creating via EmbedderFactory.createRemoteRepository()
     *              or by using instances from MavenProject
     * @param localRepository
     * @throws ArtifactResolutionException if the artifact is not found 
     * @throws ArtifactNotFoundException if the artifact is not found or is not updated to satisfy the build.
     * @since 2.76
     */
    public void resolveArtifact(Artifact toResolve, List<ArtifactRepository> remoteRepositories, ArtifactRepository localRepository) throws ArtifactResolutionException, ArtifactNotFoundException {
        setUpLegacySupport();
        
        // must call internal Resolver API directly, as the RepositorySystem does not report an exception, 
        // even in ArtifactResolutionResult: resolve(ArtifactResolutionRequest request) catches the exception and
        // swallows ArtifactNotFoundException.
        // The existing calling code that handles these exception cannot work, in fact, when using resolve(ArtifactResolutionRequest request) API.
        lookupComponent(ArtifactResolver.class).resolveAlways(toResolve, remoteRepositories, localRepository);
        normalizePath(toResolve);
    }

    //TODO possibly rename.. build sounds like something else..
    public ProjectBuildingResult buildProject(Artifact art, ProjectBuildingRequest req) throws ProjectBuildingException {
        if (req.getLocalRepository() == null) {
           req.setLocalRepository(getLocalRepository());
        }
        MavenExecutionRequest request = createMavenExecutionRequest();
        req.setProcessPlugins(false);
        req.setRepositorySession(maven.newRepositorySession(request));
        ProjectBuildingResult res = projectBuilder.build(art, req);
        normalizePaths(res.getProject());
        return res;
    }

    public MavenExecutionResult execute(MavenExecutionRequest req) {
        return maven.execute(req);
    }
    
    /**
     * Creates a list of POM models in an inheritance lineage.
     * Each resulting model is "raw", so contains no interpolation or inheritance.
     * In particular beware that groupId and/or version may be null if inherited from a parent; use {@link Model#getParent} to resolve.
     * Internally calls <code>executeModelBuilder</code> so if you need to call both just use the execute method.
     * @param pom a POM to inspect
     * @param embedder an embedder to use
     * @return a list of models, starting with the specified POM, going through any parents, finishing with the Maven superpom (with a null artifactId)
     * @throws ModelBuildingException if the POM or parents could not even be parsed; warnings are not reported
     */
    public List<Model> createModelLineage(File pom) throws ModelBuildingException {
        ModelBuildingResult res = executeModelBuilder(pom);
        List<Model> toRet = new ArrayList<Model>();

        for (String id : res.getModelIds()) {
            Model m = res.getRawModel(id);
            normalizePath(m);
            toRet.add(m);
        }
//        for (ModelProblem p : res.getProblems()) {
//            System.out.println("problem=" + p);
//            if (p.getException() != null) {
//                p.getException().printStackTrace();
//            }
//        }
        return toRet;
    }
    
    private ModelResolver createNBResolver() {
        MavenExecutionRequest rq = createMavenExecutionRequest();
        NBRepositoryModelResolver resolver = new NBRepositoryModelResolver(this);
        rq.getRemoteRepositories().stream().map(MavenEmbedder::settingsToModel).forEach(r -> {
            try {
                resolver.addRepository(r);
            } catch (org.apache.maven.model.resolution.InvalidRepositoryException ex) {
                // do nothing for now, maybe some one shot per url/id log in the future ?
            }
        });
        return resolver;
    }
    
    /**
     * 
     * @param pom
     * @return result object with access to effective pom model and raw models for each parent.
     * @throws ModelBuildingException if the POM or parents could not even be parsed; warnings are not reported
     */
    public ModelBuildingResult executeModelBuilder(File pom) throws ModelBuildingException {
        setUpLegacySupport();
        ModelBuilder mb = lookupComponent(ModelBuilder.class);
        assert mb!=null : "ModelBuilder component not found in maven";
        ModelBuildingRequest req = new DefaultModelBuildingRequest();
        req.setPomFile(pom);
        req.setProcessPlugins(false);
        req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        req.setLocationTracking(true);
        req.setModelResolver(createNBResolver());
        req.setSystemProperties(getSystemProperties());
        req.setUserProperties(embedderConfiguration.getUserProperties());
        return mb.build(req);
    }
    
    private static org.apache.maven.model.Repository settingsToModel(ArtifactRepository repo) {
        org.apache.maven.model.Repository modelRepo = new org.apache.maven.model.Repository();
        modelRepo.setId(repo.getId());
        modelRepo.setLayout(repo.getLayout().getId());
        modelRepo.setName(repo.getId());
        modelRepo.setUrl(repo.getUrl());
        return modelRepo;
    }
    
    private static org.apache.maven.model.RepositoryPolicy settingsToModel(ArtifactRepositoryPolicy p) {
        if (p == null) {
            return null;
        }
        org.apache.maven.model.RepositoryPolicy r = new org.apache.maven.model.RepositoryPolicy();
        r.setChecksumPolicy(p.getChecksumPolicy());
        r.setUpdatePolicy(p.getUpdatePolicy());
        r.setEnabled(p.isEnabled());
        return r;
    }
    
    private static org.apache.maven.model.Repository settingsToModel(Repository repo) {
        org.apache.maven.model.Repository modelRepo = new org.apache.maven.model.Repository();
        modelRepo.setId(repo.getId());
        modelRepo.setLayout(repo.getLayout());
        modelRepo.setName(repo.getName());
        modelRepo.setUrl(repo.getUrl());
        modelRepo.setReleases(settingsToModel(repo.getReleases()));
        modelRepo.setSnapshots(settingsToModel(repo.getSnapshots()));
        return modelRepo;
    }
    
    private static org.apache.maven.model.RepositoryPolicy settingsToModel(RepositoryPolicy p) {
        if (p == null) {
            return null;
        }
        org.apache.maven.model.RepositoryPolicy r = new org.apache.maven.model.RepositoryPolicy();
        r.setChecksumPolicy(p.getChecksumPolicy());
        r.setUpdatePolicy(p.getUpdatePolicy());
        r.setEnabled(p.isEnabled());
        return r;
    }
    
    public List<String> getLifecyclePhases() {

        LifecycleMapping lifecycleMapping = lookupComponent(LifecycleMapping.class);
        if (lifecycleMapping != null) {
            Set<String> phases = new TreeSet<String>();
            Map<String, Lifecycle> lifecycles = lifecycleMapping.getLifecycles();
            for (Lifecycle lifecycle : lifecycles.values()) {
                phases.addAll(lifecycle.getPhases().keySet());
            }
            return new ArrayList<String>(phases);
        }

        return Collections.<String>emptyList();
    }

    public  <T> T lookupComponent(Class<T> clazz) {
        try {
            return plexus.lookup(clazz);
        } catch (ComponentLookupException ex) {
            LOG.warning(ex.getMessage());
        }
        return null;
    }
    
    /**
     * a prepopulate maven execution request object, most notably but systemProperties and userProperties 
     * fields are prepopulated with default values, typically one should only add to these values, not replace them.
     * @return 
     */
    public MavenExecutionRequest createMavenExecutionRequest(){
        MavenExecutionRequest req = new DefaultMavenExecutionRequest();

        ArtifactRepository localRepository = getLocalRepository();
        req.setLocalRepository(localRepository);
        req.setLocalRepositoryPath(localRepository.getBasedir());

        //TODO: do we need to validate settings files?
        File settingsXml = embedderConfiguration.getSettingsXml();
        if (settingsXml !=null && settingsXml.exists()) {
            req.setGlobalSettingsFile(settingsXml);
        }
        if (SettingsXmlConfigurationProcessor.DEFAULT_USER_SETTINGS_FILE != null && SettingsXmlConfigurationProcessor.DEFAULT_USER_SETTINGS_FILE.exists()) {
          req.setUserSettingsFile(SettingsXmlConfigurationProcessor.DEFAULT_USER_SETTINGS_FILE);
        }
        
        req.setSystemProperties(getSystemProperties());
        req.setUserProperties(embedderConfiguration.getUserProperties());
        try {
            //#212214 populating from settings needs to come first
            //it adds mirrors and proxies to the request
            //later on populateDefaults() will use these to replace/configure the default "central" repository
            // and the repository id used is important down the road for resolution in EnhancedLocalRepositoryManager
            populator.populateFromSettings(req, getSettings());
            populator.populateDefaults(req);
        } catch (MavenExecutionRequestPopulationException x) {
            // XXX where to display this?
            Exceptions.printStackTrace(x);
        }
        req.setOffline(isOffline());
        req.setRepositoryCache(new NbRepositoryCache());

        return req;
    }
    
    /**
     * Do not keep the reference to the session. Plexus will hopefully keep it for us.
     */
    private volatile Reference<MavenSession> thisRepositorySession = new WeakReference<>(null);
    
    /**
     * Needed to avoid an NPE in {@link org.eclipse.org.eclipse.aether.DefaultArtifactResolver#resolveArtifacts} under some conditions.
     * (Also {@link org.eclipse.org.eclipse.aether.DefaultMetadataResolver#resolve}; wherever a {@link org.eclipse.aether.RepositorySystemSession} is used.)
     * Should be called in the same thread as whatever thread was throwing the NPE.
     */
    public void setUpLegacySupport() {
        LegacySupport support = lookupComponent(LegacySupport.class);
        MavenSession existing = support.getSession();
        MavenSession initializedSession = thisRepositorySession.get();
        if (existing != null) {
            RepositorySystemSession existingRepo = existing.getRepositorySession();
            if (initializedSession != null && initializedSession.getRepositorySession() == existingRepo) {
                return;
            }
        }
        if (initializedSession == null) {
            DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
            session.setOffline(isOffline());
            EnhancedLocalRepositoryManagerFactory f = lookupComponent(EnhancedLocalRepositoryManagerFactory.class);
            try {
                session.setLocalRepositoryManager(f.newInstance(session, new LocalRepository(getLocalRepository().getBasedir())));
            } catch (NoLocalRepositoryManagerException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
            // Adapted from DefaultMaven.newRepositorySession, but does not look like that can be called directly:
            DefaultMirrorSelector mirrorSelector = new DefaultMirrorSelector();
            Settings _settings = getSettings();
            for (Mirror m : _settings.getMirrors()) {
                mirrorSelector.add(m.getId(), m.getUrl(), m.getLayout(), false, m.getMirrorOf(), m.getMirrorOfLayouts());
            }
            session.setMirrorSelector(mirrorSelector);
            SettingsDecryptionResult decryptionResult = settingsDecrypter.decrypt(new DefaultSettingsDecryptionRequest(_settings));

            DefaultProxySelector proxySelector = new DefaultProxySelector();
            for (Proxy p : decryptionResult.getProxies()) {
                if (p.isActive()) {
                    AuthenticationBuilder ab = new AuthenticationBuilder();
                    ab.addUsername(p.getUsername());
                    ab.addPassword(p.getPassword());
                    Authentication a = ab.build();
                   //#null -> getProtocol() #209499
                   proxySelector.add(new org.eclipse.aether.repository.Proxy(p.getProtocol(), p.getHost(), p.getPort(), a), p.getNonProxyHosts());
                }
            }
            session.setProxySelector(proxySelector);
            DefaultAuthenticationSelector authenticationSelector = new DefaultAuthenticationSelector();
            for (Server s : decryptionResult.getServers()) {
                AuthenticationBuilder ab = new AuthenticationBuilder();
                ab.addUsername(s.getUsername());
                ab.addPassword(s.getPassword());
                ab.addPrivateKey(s.getPrivateKey(), s.getPassphrase());
                Authentication a = ab.build();            
                authenticationSelector.add(s.getId(), a);
            }
            session.setAuthenticationSelector(authenticationSelector);
            DefaultMavenExecutionRequest mavenExecutionRequest = new DefaultMavenExecutionRequest();
            mavenExecutionRequest.setSystemProperties(embedderConfiguration.getSystemProperties());
            mavenExecutionRequest.setOffline(isOffline());
            mavenExecutionRequest.setTransferListener(ProgressTransferListener.activeListener());
            session.setTransferListener(ProgressTransferListener.activeListener());

            MavenSession s = new MavenSession(getPlexus(), session, mavenExecutionRequest, new DefaultMavenExecutionResult());
            synchronized (this) {
                initializedSession = thisRepositorySession.get();
                if (initializedSession == null) {
                    initializedSession = s;
                    thisRepositorySession = new WeakReference<>(s);
                }
            }
        }
        lookupComponent(LegacySupport.class).setSession(initializedSession);
    }

    
    /**
     * during creation of the MAvenProject instance, the list of all profiles available in
     * the pom inheritance chain.
     * @param mp
     * @return list of available profiles or null if something went wrong..
     * @since 2.29
     */
    public static Set<String> getAllProjectProfiles(MavenProject mp) {
        return NBModelBuilder.getAllProfiles(mp.getModel());
    }
    /**
     * descriptions of models that went into effective pom, containing information that was lost in processing and is not cheap to obtain.
     * in the list the current project's model description comes first, second is it's parent and so on.
     * @param mp
     * @return null if the parameter passed was not created using the Project Maven Embedder.
     * @since 2.30
     */
    public static @CheckForNull List<ModelDescription> getModelDescriptors(MavenProject mp) {
        return NBModelBuilder.getModelDescriptors(mp.getModel());
    }

    /**
     * normalize all File references in the object tree.
     * @param project 
     * @since 2.36
     */
    public static void normalizePaths(MavenProject project) {
        if (project == null) {
            return;
        }
        File f = project.getFile();
        if (f != null) {
            project.setFile(FileUtil.normalizeFile(f));
        }
        normalizePath(project.getArtifact());
        normalizePaths(project.getAttachedArtifacts());
        f = project.getParentFile();
        if (f != null) {
            project.setParentFile(FileUtil.normalizeFile(f));
        }
        normalizePath(project.getParentArtifact());
        
        normalizePaths(project.getArtifacts());
        normalizePaths(project.getDependencyArtifacts());
        normalizePaths(project.getExtensionArtifacts());
        normalizePaths(project.getPluginArtifacts());
        
        normalizePath(project.getModel());
        normalizePath(project.getOriginalModel());
    }
    
    static void normalizePath(Model model) {
        if (model != null) {
            File f = model.getPomFile();
            if (f != null) {
                model.setPomFile(FileUtil.normalizeFile(f));
            }
        }
    }

    static void normalizePaths(Collection<Artifact> arts) {
        if (arts != null) {
            for (Artifact aa : arts) {
                normalizePath(aa);
            }
        }
    }
    
    static void normalizePath(Artifact a) {
        if (a != null) {
            File f = a.getFile();
            if (f != null) {
                a.setFile(FileUtil.normalizeFile(f));
            }
        }
    }

    private void setResult(ProjectBuildingResult pbr, Map<File, MavenExecutionResult> results) {
        DefaultMavenExecutionResult r = new DefaultMavenExecutionResult();
        normalizePaths(pbr.getProject());
        r.setProject(pbr.getProject());
            r.setDependencyResolutionResult(pbr.getDependencyResolutionResult());
        results.put(pbr.getPomFile(), r);
    }
    
    /**
     * descriptor containing some base information about the models collected while building
     * effective model. 
     * @since 2.30
     */
    public static interface ModelDescription {
        /*
         * groupId:artifactId:version
         */
        String getId();
        /**
         * artifactId as defined in the model
         * @return 
         */
        String getArtifactId();
        /**
         * version as defined in the model
         * @return 
         */
        String getVersion();
        /**
         * groupId as defined in the model
         * @return 
         */
        String getGroupId();
        /**
         * name as defined in the model
         * @return 
         */
        String getName();
        /**
         * location of the model pom file.
         * @return normalized path
         */
        File getLocation();
        /**
         * all profile ids as found in the model
         * @return 
         */
        List<String> getProfiles();
        /**
         * get all module declarations from base and from profile locations
         * @return 
         */
        List<String> getModules();
        
    }
}
