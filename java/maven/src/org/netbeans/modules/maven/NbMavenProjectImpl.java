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
package org.netbeans.modules.maven;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.configurations.ProjectProfileHandlerImpl;
import org.netbeans.modules.maven.cos.CopyResourcesOnSave;
import org.netbeans.modules.maven.debug.MavenJPDAStart;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.modelcache.MavenProjectCache;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.modules.maven.problems.ProblemReporterImpl;
import org.netbeans.modules.maven.queries.PomCompilerOptionsQueryImpl;
import org.netbeans.modules.maven.queries.UnitTestsCompilerOptionsQueryImpl;
import org.netbeans.modules.maven.spi.nodes.OtherSourcesExclude;
import org.netbeans.modules.maven.spi.queries.JavaLikeRootProvider;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * A Maven-based project.
 */
public final class NbMavenProjectImpl implements Project {


    private static final Logger LOG = Logger.getLogger(NbMavenProjectImpl.class.getName());
    
    //sequential execution might be necesary for #166919
    public static final RequestProcessor RELOAD_RP = new RequestProcessor("Maven project reloading", 1); //NOI18
    //minor optimization. In case the queue already holds the task and is not run, delay, if running reschedule.
    private final RequestProcessor.Task reloadTask = RELOAD_RP.create(new Runnable() {
        @Override
        public void run() {
            problemReporter.clearReports(); //#167741 -this will trigger node refresh?
            MavenProject prj = loadOriginalMavenProject(true);
            synchronized (NbMavenProjectImpl.this) {
                MavenProject old = project == null ? null : project.get();
                if (old != null && MavenProjectCache.isFallbackproject(prj)) {
                    prj.setPackaging(old.getPackaging()); //#229366 preserve packaging for broken projects to avoid changing lookup.
                }
                project = new SoftReference<MavenProject>(prj);
                if (hardReferencingMavenProject) {
                    hardRefProject = prj;
                }
            }
            ACCESSOR.doFireReload(watcher);
        }
    });
    private final FileObject fileObject;
    private final FileObject folderFileObject;
    private final File projectFile;
    private final Lookup basicLookup;
    private final Lookup completeLookup;
    private final Lookup lookup;
    private final Updater openedProjectUpdater;
    
    private Reference<MavenProject> project;
    private boolean hardReferencingMavenProject = false; //only should be true when project is open.
    private MavenProject hardRefProject;
    
    private ProblemReporterImpl problemReporter;
    private final @NonNull NbMavenProject watcher;
    private final M2ConfigProvider configProvider;
    private final @NonNull MavenProjectPropsImpl auxprops;
    private ProjectProfileHandlerImpl profileHandler;
    private CopyResourcesOnSave copyResourcesOnSave;
    private final Object COPYRESOURCES_LOCK = new Object();
    @org.netbeans.api.annotations.common.SuppressWarnings("MS_SHOULD_BE_FINAL")
    public static WatcherAccessor ACCESSOR = null;

    static {
        // invokes static initializer of ModelHandle.class
        // that will assign value to the ACCESSOR field above
        Class<?> c = NbMavenProject.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "very wrong, very wrong, yes indeed", ex);
        }
    }

    //#224012
    private ProjectOpenedHookImpl hookImpl;
    private Exception ex;
    private final Object LOCK_224012 = new Object();
    boolean setIssue224012(ProjectOpenedHookImpl hook, Exception exception) {
        synchronized (LOCK_224012) {
            if (hookImpl == null) {
                hookImpl = hook;
                ex = exception;
                return true;
            } else {
                LOG.log(Level.INFO, "    first creation stacktrace", ex);
                LOG.log(Level.INFO, "    second creation stacktrace", exception);
                LOG.log(Level.WARNING, "Spotted issue 224012 (https://netbeans.org/bugzilla/show_bug.cgi?id=224012). Please report the incident wth IDE log attached.");
                return false;
            }
        }
    }

    
    private final Object MODEL_LOCK = new Object();
    private Model model;
    public Model getRawModel() throws ModelBuildingException {
        synchronized(MODEL_LOCK) {
            if(model == null) {
                MavenEmbedder projectEmbedder = EmbedderFactory.getProjectEmbedder();
                ModelBuildingResult br = projectEmbedder.executeModelBuilder(getPOMFile());
                model = br.getRawModel();
            }
            return model;
        }
    }


    public static abstract class WatcherAccessor {

        public abstract NbMavenProject createWatcher(NbMavenProjectImpl proj);

        public abstract void doFireReload(NbMavenProject watcher);
    }

    /**
     * Creates a new instance of MavenProject, should never be called by user code.
     * but only by MavenProjectFactory!!!
     */
    NbMavenProjectImpl(FileObject folder, FileObject projectFO, ProjectState projectState) {
        this.projectFile = FileUtil.normalizeFile(FileUtil.toFile(projectFO));
        fileObject = projectFO;
        folderFileObject = folder;
        lookup = Lookups.proxy(new Lookup.Provider() {
            @Override
            public Lookup getLookup() {
                if (completeLookup == null) {
                    //not fully initialized constructor
                    LOG.log(Level.FINE, "accessing project's lookup before the instance is fully initialized at " + projectFile, new Exception());
                    assert basicLookup != null;
                    return basicLookup;
                } else {
                    return completeLookup;
                }
            }
        });
        watcher = ACCESSOR.createWatcher(this);
        openedProjectUpdater = new Updater(new FileProvider() {

            @Override
            public File[] getFiles() {
                File homeFile = FileUtil.normalizeFile(MavenCli.USER_MAVEN_CONFIGURATION_HOME);
                return new File[] {
                    new File(projectFile.getParentFile(), "nb-configuration.xml"), //NOI18N
                    projectFile,
                    new File(new File(projectFile.getParentFile(), ".mvn"), "maven.config"), //NOI18N
                    new File(homeFile, "settings.xml"), //NOI18N
                };
            }
        });
        problemReporter = new ProblemReporterImpl(this);
        M2AuxilaryConfigImpl auxiliary = new M2AuxilaryConfigImpl(folder, true);
        auxprops = new MavenProjectPropsImpl(auxiliary, this);
        profileHandler = new ProjectProfileHandlerImpl(this, auxiliary);
        configProvider = new M2ConfigProvider(this, auxiliary, profileHandler);
        // @PSP's and the like, and PackagingProvider impls, may check project lookup for e.g. NbMavenProject, so init lookup in two stages:
        basicLookup = createBasicLookup(projectState, auxiliary);
        //here we always load the MavenProject instance because we need to touch the packaging from pom.
        completeLookup = LookupProviderSupport.createCompositeLookup(basicLookup, new PackagingTypeDependentLookup(watcher));
    }

    public File getPOMFile() {
        return projectFile;
    }

    public @NonNull NbMavenProject getProjectWatcher() {
        return watcher;
    }

    public ProblemReporterImpl getProblemReporter() {
        return problemReporter;
    }

    public String getHintJavaPlatform() {
        String hint = getAuxProps().get(Constants.HINT_JDK_PLATFORM, true);
        if (hint == null) {
            hint = MavenSettings.getDefault().getDefaultJdk();
        }
        return hint == null || hint.isEmpty() ? null : hint;
    }

    /**
     * load a project with properties and profiles other than the current ones.
     * @param embedder embedder to use
     * @param activeProfiles
     * @param properties
     * @return
     */
    //TODO revisit usage, eventually should be only reuse MavenProjectCache
    public @NonNull MavenProject loadMavenProject(MavenEmbedder embedder, List<String> activeProfiles, Properties properties) {
        try {
            MavenExecutionRequest req = embedder.createMavenExecutionRequest();
            req.addActiveProfiles(activeProfiles);
            req.setPom(projectFile);
            req.setNoSnapshotUpdates(true);
            req.setUpdateSnapshots(false);
            //#238800 important to merge, not replace
            if (properties != null) {
                Properties uprops = req.getUserProperties();
                uprops.putAll(properties);
                req.setUserProperties(uprops);
            }
            //MEVENIDE-634 i'm wondering if this fixes the issue
            req.setInteractiveMode(false);
            req.setOffline(true);
            // recursive == false is important to avoid checking all submodules for extensions
            // that will not be used in current pom anyway..
            // #135070
            req.setRecursive(false);
            MavenExecutionResult res = embedder.readProjectWithDependencies(req, true);
            //#215159 clear the project building request, it references multiple Maven Models via the RepositorySession cache
            //is not used in maven itself, most likely used by m2e only..
            if (!res.hasExceptions()) {
                res.getProject().setProjectBuildingRequest(null);
                return res.getProject();
            } else {
                List<Throwable> exc = res.getExceptions();
                for (Throwable ex : exc) {
                    LOG.log(Level.FINE, "Exception thrown while loading maven project at " + getProjectDirectory(), ex); //NOI18N
                }
            }
        } catch (RuntimeException exc) {
            //guard against exceptions that are not processed by the embedder
            //#136184 NumberFormatException
            LOG.log(Level.INFO, "Runtime exception thrown while loading maven project at " + getProjectDirectory(), exc); //NOI18N
        }
        return MavenProjectCache.getFallbackProject(this.getPOMFile());
    }
    
    /**
     * replacement for MavenProject.getParent() which has bad long term memory behaviour. We offset it by recalculating/reparsing everything
     * therefore should not be used lightly!
     * pass a MavenProject instance and current configuration and other settings will be applied when loading the parent.
     * @param project
     * @return null or the parent mavenproject
     */
    
    public MavenProject loadParentOf(MavenEmbedder embedder, MavenProject project) throws ProjectBuildingException {

        MavenProject parent = null;
        ProjectBuilder builder = embedder.lookupComponent(ProjectBuilder.class);
        MavenExecutionRequest req = embedder.createMavenExecutionRequest();
        M2Configuration active = configProvider.getActiveConfiguration();
        req.addActiveProfiles(active.getActivatedProfiles());
        req.setNoSnapshotUpdates(true);
        req.setUpdateSnapshots(false);
        req.setInteractiveMode(false);
        req.setRecursive(false);
        req.setOffline(true);
        //#238800 important to merge, not replace
        Properties uprops = req.getUserProperties();
        uprops.putAll(MavenProjectCache.createUserPropsForProjectLoading(active.getProperties()));
        req.setUserProperties(uprops);
        
        ProjectBuildingRequest request = req.getProjectBuildingRequest();
        request.setRemoteRepositories(project.getRemoteArtifactRepositories());
        DefaultMaven maven = (DefaultMaven) embedder.lookupComponent(Maven.class);
        
        request.setRepositorySession(maven.newRepositorySession(req));

        if (project.getParentFile() != null) {
            parent = builder.build(project.getParentFile(), request).getProject();
        } else if (project.getModel().getParent() != null) {
            parent = builder.build(project.getParentArtifact(), request).getProject();
        }
        //clear the project building request, it references multiple Maven Models via the RepositorySession cache
        //is not used in maven itself, most likely used by m2e only..
        if (parent != null) {
            parent.setProjectBuildingRequest(null);
        }
        MavenEmbedder.normalizePaths(parent);
        return parent;
    }

    public List<String> getCurrentActiveProfiles() {
        List<String> toRet = new ArrayList<String>();
        toRet.addAll(configProvider.getActiveConfiguration().getActivatedProfiles());
        return toRet;
    }


    //#172952 for property expression resolution we need this to include
    // the properties of the platform to properly resolve stuff like com.sun.boot.class.path
    public Map<? extends String,? extends String> createSystemPropsForPropertyExpressions() {
        Map<String,String> props = NbCollections.checkedMapByCopy(EmbedderFactory.getProjectEmbedder().getSystemProperties(), String.class, String.class, true);
        ActiveJ2SEPlatformProvider platformProvider = getLookup().lookup(ActiveJ2SEPlatformProvider.class);
        if (platformProvider != null) { // may be null inside PackagingProvider
            props.putAll(platformProvider.getJavaPlatform().getSystemProperties());
        }       
        return props;
    }
    
    public  Map<? extends String,? extends String> createUserPropsForPropertyExpressions() {
         return NbCollections.checkedMapByCopy(configProvider.getActiveConfiguration().getProperties(), String.class, String.class, true);
    }

    /**
     * getter for the maven's own project representation.. this instance is cached but gets reloaded
     * when one the pom files have changed.
     */
    public @NonNull MavenProject getOriginalMavenProject() {
        MavenProject mp;
        synchronized (this) {
            mp = project == null ? null : project.get();
            if (mp != null) {
                return mp;
            }
            if (mp == null) {
                // PENDING: should be the whole project load synchronized ?
                mp = loadOriginalMavenProject(false);
                project = new SoftReference<MavenProject>(mp);
                if (hardReferencingMavenProject) {
                    hardRefProject = mp;
                }
            }
        }
        // in case someone got already information from the NbMavenProject:
        ACCESSOR.doFireReload(watcher);
        return mp;
    }
    
    /**
     * a marginally unreliable, non blocking method for figuring if the model is loaded or not.
     * @return 
     */
    public boolean isMavenProjectLoaded() {
        Reference<MavenProject> prj = project;
        if (prj != null) {
            return prj.get() != null;
        }
        return false;
    }
    
    /**
     * open projects should always hard reference the Mavenproject instance to prevent it from
     * being GCed, the instance will get reloaded almost instantly anyway
     */
    void startHardReferencingMavenPoject() {
        synchronized (this) {
            hardReferencingMavenProject = true;
            MavenProject mp = project == null ? null : project.get();
            hardRefProject = mp;
        }
    }
    /**
     * open projects should always hard reference the Mavenproject instance to prevent it from
     * being GCed, the instance will get reloaded almost instantly anyway
     */
    void stopHardReferencingMavenPoject() {
        synchronized (this) {
            hardReferencingMavenProject = false;
            hardRefProject = null;
        }
    }
    

    @Messages({
        "TXT_RuntimeException=RuntimeException occurred in Apache Maven embedder while loading",
        "TXT_RuntimeExceptionLong=RuntimeException occurred in Apache Maven embedder while loading the project. \n"
            + "This is preventing the project model from loading properly. \n"
            + "Please file a bug report with details about your project and the IDE's log file.\n\n"
    })
    private @NonNull MavenProject loadOriginalMavenProject(boolean reload) {
        MavenProject newproject;
        try {
            synchronized(MODEL_LOCK) {
                model = null;
            }
            newproject = MavenProjectCache.getMavenProject(this.getPOMFile(), reload);
            if (newproject == null) { //null when no pom.xml in project folder..
                newproject = MavenProjectCache.getFallbackProject(projectFile);
            }
            final MavenExecutionResult res = MavenProjectCache.getExecutionResult(newproject);
            final MavenProject np = newproject;
        } finally {
            if (LOG.isLoggable(Level.FINE) && SwingUtilities.isEventDispatchThread()) {
                LOG.log(Level.FINE, "Project " + getProjectDirectory().getPath() + " loaded in AWT event dispatching thread!", new RuntimeException());
            }
        }
        assert newproject != null;
        return newproject;
    }



    public RequestProcessor.Task fireProjectReload() {
        //#227101 not only AWT and project read/write mutex has to be checked, there are some additional more
        //complex scenarios that can lead to deadlock. Just give up and always fire changes in separate RP.
        if (Boolean.getBoolean("test.reload.sync")) {
            reloadTask.run();
            //for tests just do sync reload, even though silly, even sillier is to attempt to sync the threads..
        } else {
            reloadTask.schedule(0); //asuming here that schedule(0) will move the scheduled task in the queue if not yet executed
        }
        return reloadTask;
    }

    public static void refreshLocalRepository(NbMavenProjectImpl project) {
        File file = project.getEmbedder().getLocalRepositoryFile();
        FileUtil.refreshFor(file);
    }

    /** Begin listening to pom.xml changes. */
    void attachUpdater() {
        openedProjectUpdater.attachAll();
    }
   void detachUpdater() {
        openedProjectUpdater.detachAll();
    }

    /**
     * The root directory of the project where the POM resides.
     */
    @Override
    public FileObject getProjectDirectory() {
        return folderFileObject;
    }

    public @CheckForNull String getArtifactRelativeRepositoryPath() {
        Artifact artifact = getOriginalMavenProject().getArtifact();
        if (artifact == null) {
            return null;
        }
        return getArtifactRelativeRepositoryPath(artifact);
    }

    /**
     * path of test artifact in local repository
     * @return
     */
    public @CheckForNull String getTestArtifactRelativeRepositoryPath() {
        Artifact main = getOriginalMavenProject().getArtifact();
        if (main == null) {
            return null;
        }

        ArtifactHandlerManager artifactHandlerManager = getEmbedder().lookupComponent(ArtifactHandlerManager.class);
        assert artifactHandlerManager != null : "ArtifactHandlerManager component not found in maven";

        Artifact test = new DefaultArtifact(main.getGroupId(), main.getArtifactId(), main.getVersionRange(),
                Artifact.SCOPE_TEST, "test-jar", "tests", artifactHandlerManager.getArtifactHandler("test-jar"));
        return getArtifactRelativeRepositoryPath(test);

    }

    public String getArtifactRelativeRepositoryPath(@NonNull Artifact artifact) {
        return getEmbedder().getLocalRepository().pathOf(artifact);
    }

    public MavenEmbedder getEmbedder() {
        return EmbedderFactory.getProjectEmbedder();
    }

    public @NonNull MavenProjectPropsImpl getAuxProps() {
        return auxprops;
    }

    public URI[] getSourceRoots(boolean test) {
        List<URI> uris = new ArrayList<URI>();
        for (String root : test ? getOriginalMavenProject().getTestCompileSourceRoots() : getOriginalMavenProject().getCompileSourceRoots()) {
            uris.add(FileUtilities.convertStringToUri(root));
        }
        for (JavaLikeRootProvider rp : getLookup().lookupAll(JavaLikeRootProvider.class)) {
            // XXX for a few purposes (listening) it is desirable to list these even before they exist, but usually it is just noise (cf. #196414 comment #2)
            FileObject root = getProjectDirectory().getFileObject("src/" + (test ? "test" : "main") + "/" + rp.kind());
            if (root != null && root.isFolder()) {
                uris.add(root.toURI());
            }
        }
        return uris.toArray(new URI[uris.size()]);
    }

    public URI[] getGeneratedSourceRoots(boolean test) {
        //#241874 calculate the test source roots up front just in case they are in target/generated-sources. if so, remove the from non-test generated source roots
        Set<URI> BHTestUris = new HashSet<URI>();
        String[] buildHelpers = PluginPropertyUtils.getPluginPropertyList(this,
                "org.codehaus.mojo", //NOI18N
                "build-helper-maven-plugin", "sources", "source", "add-test-source"); //NOI18N
        if (buildHelpers != null && buildHelpers.length > 0) {
            File root = FileUtil.toFile(getProjectDirectory());
            for (String helper : buildHelpers) {
                BHTestUris.add(FileUtilities.getDirURI(root, helper));
            }
        }
        
        
        
        URI uri = FileUtilities.getDirURI(getProjectDirectory(), test ? "target/generated-test-sources" : "target/generated-sources"); //NOI18N
        Set<URI> uris = new HashSet<URI>();
        File[] roots = Utilities.toFile(uri).listFiles();
        if (roots != null) {
            for (File root : roots) {
                if (!VisibilityQuery.getDefault().isVisible(root)) { //#214002
                   continue;
                }
                if (!test && root.getName().startsWith("test-")) {
                    continue;
                }
                File[] kids = root.listFiles();
                URI u = Utilities.toURI(root);
                if (!test && BHTestUris.contains(u)) {
                    continue; //a test source root was put in target/generated-sources - #241874
                }
                if (kids != null && /* #190626 */kids.length > 0) {
                    uris.add(u);
                } else {
                    watcher.addWatchedPath(u); //TODO who reacts to this?
                }
            }
        }
        if (test) { // MCOMPILER-167
            roots = Utilities.toFile(FileUtilities.getDirURI(getProjectDirectory(), "target/generated-sources")).listFiles();
            if (roots != null) {
                for (File root : roots) {
                    if (!VisibilityQuery.getDefault().isVisible(root)) { //#214002
                       continue;
                    }                    
                    if (root.getName().startsWith("test-")) {
                        File[] kids = root.listFiles();
                        if (kids != null && kids.length > 0) {
                            uris.add(Utilities.toURI(root));
                        } else {
                            watcher.addWatchedPath(Utilities.toURI(root)); //TODO who reacts to this?
                        }
                    }
                }
            }
        }

        if (!test) {
            buildHelpers = PluginPropertyUtils.getPluginPropertyList(this,
                    "org.codehaus.mojo", //NOI18N
                    "build-helper-maven-plugin", "sources", "source", "add-source"); //NOI18N
            if (buildHelpers != null && buildHelpers.length > 0) {
                File root = FileUtil.toFile(getProjectDirectory());
                for (String helper : buildHelpers) {
                    uris.add(FileUtilities.getDirURI(root, helper));
                }
            }
        } else {
            uris.addAll(BHTestUris);
        }

        return uris.toArray(new URI[uris.size()]);
    }

    public URI getWebAppDirectory() {
        //TODO hack, should be supported somehow to read this..
        String prop = PluginPropertyUtils.getPluginProperty(this, Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_WAR, //NOI18N
                "warSourceDirectory", //NOI18N
                "war", null); //NOI18N

        prop = prop == null ? "src/main/webapp" : prop; //NOI18N

        return FileUtilities.getDirURI(getProjectDirectory(), prop);
    }

    public URI getSiteDirectory() {
        //TODO hack, should be supported somehow to read this..
        String prop = PluginPropertyUtils.getPluginProperty(this, Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_SITE, //NOI18N
                "siteDirectory", //NOI18N
                "site", null); //NOI18N

        prop = prop == null ? "src/site" : prop; //NOI18N

        return FileUtilities.getDirURI(getProjectDirectory(), prop);
    }

    public URI getEarAppDirectory() {
        //TODO hack, should be supported somehow to read this..
        String prop = PluginPropertyUtils.getPluginProperty(this, Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_EAR, //NOI18N
                "earSourceDirectory", //NOI18N
                "ear", null); //NOI18N

        prop = prop == null ? "src/main/application" : prop; //NOI18N

        return FileUtilities.getDirURI(getProjectDirectory(), prop);
    }

    public URI[] getResources(boolean test) {
        List<URI> toRet = new ArrayList<URI>();
        URI projectroot = getProjectDirectory().toURI();
        Set<URI> sourceRoots = null;
        List<Resource> res = test ? getOriginalMavenProject().getTestResources() : getOriginalMavenProject().getResources();
        LBL : for (Resource elem : res) {
            String dir = elem.getDirectory();
            if (dir == null) {
                continue; // #191742
            }
            URI uri = FileUtilities.getDirURI(getProjectDirectory(), dir);
            if (elem.getTargetPath() != null || !elem.getExcludes().isEmpty() || !elem.getIncludes().isEmpty()) {
                URI rel = projectroot.relativize(uri);
                if (rel.isAbsolute()) { //outside of project directory
                    continue;// #195928, #231517
                }
                if (sourceRoots == null) {
                    sourceRoots = new HashSet<URI>();
                    sourceRoots.addAll(Arrays.asList(getSourceRoots(true)));
                    sourceRoots.addAll(Arrays.asList(getSourceRoots(false)));
                    //should we also consider generated sources? most like not necessary
                }
                for (URI sr : sourceRoots) {
                    if (!uri.relativize(sr).isAbsolute()) {
                        continue LBL;// #195928, #231517
                    }
                }
                //hope for the best now
            }
//            if (new File(uri).exists()) {
            toRet.add(uri);
//            }
        }
        return toRet.toArray(new URI[toRet.size()]);
    }

    public File[] getOtherRoots(boolean test) {
        URI uri = FileUtilities.getDirURI(getProjectDirectory(), test ? "src/test" : "src/main"); //NOI18N
        Set<File> toRet = new HashSet<File>();
        File fil = Utilities.toFile(uri);
        if (fil.exists()) {
            try {
                Path sourceRoot = fil.toPath();
                OtherRootsVisitor visitor = new OtherRootsVisitor(getLookup(), sourceRoot);
                Files.walkFileTree(sourceRoot, visitor);
                toRet.addAll(visitor.getOtherRoots());
            } catch (IOException ex) {
                // log as info to keep trace about possible problems, 
                // but lets not be too agressive with level and notification                
                // see also issue #251071
                LOG.log(Level.INFO, null, ex);
            }
        }
        URI[] res = getResources(test);
        for (URI rs : res) {
            File fl = Utilities.toFile(rs);
            //in node view we need only the existing ones, if anything else needs all,
            // a new method is probably necessary..
            if (fl.exists()) {
                toRet.add(fl);
            }
        }
        return toRet.toArray(new File[0]);
    }

    private static class OtherRootsVisitor extends SimpleFileVisitor<Path> {

        private final Lookup lookup;
        private final List<Path> otherRoots;
        private final Path sourceRoot;

        public OtherRootsVisitor(Lookup lookup, Path sourceRoot) {
            this.lookup = lookup;
            this.sourceRoot = sourceRoot;
            this.otherRoots = new ArrayList<>();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            // To avoid including src/main and src/test directories
            if (sourceRoot.equals(dir)) {
                return FileVisitResult.CONTINUE;
            }

            if(!VisibilityQuery.getDefault().isVisible(dir.toFile())) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            
            for (OtherSourcesExclude rp : lookup.lookupAll(OtherSourcesExclude.class)) {
                for (Path folder : rp.excludedFolders()) {
                    // In case of excluded folders (e.g. src/main/java) we can simply skip whole subtree
                    if (folder.equals(dir)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    // We are vising directory which is a parent of one of excluded directory.
                    // In such case we don't want to skip the subtree (b/c it might contain directories
                    // that falling under other roots), but we also don't want to add it to the results
                    // (to avoid adding everything)
                    if (folder.startsWith(dir)) {
                        return FileVisitResult.CONTINUE;
                    }
                }
            }
            for (JavaLikeRootProvider rp : lookup.lookupAll(JavaLikeRootProvider.class)) {
                if (rp.kind().equalsIgnoreCase(dir.getFileName().toString())) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
            }

            // If the directory wasn't excluded until now, it should be shown in Other Sources node
            if (isOtherRoot(dir)) {
                otherRoots.add(dir);
            }
            return FileVisitResult.CONTINUE;
        }

        private boolean isOtherRoot(Path dir) throws IOException {
            if (!dir.toFile().isDirectory() || Files.isHidden(dir)) {
                return false;
            }

            // Walk through the other roots and check if a parent of this dir is
            // already available in other roots to avoid folder duplication
            for (Path path : otherRoots) {
                if (dir.startsWith(path)) {
                    return false;
                }
            }
            return true;
        }

        public List<File> getOtherRoots() {
            List<File> result = new ArrayList<>();
            for (Path path : otherRoots) {
                result.add(path.toFile());
            }
            return Collections.unmodifiableList(result);
        }
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }
    
    CopyResourcesOnSave getCopyOnSaveResources() {
        synchronized (COPYRESOURCES_LOCK) {
            if (copyResourcesOnSave == null) {
                copyResourcesOnSave = new CopyResourcesOnSave(watcher, this);
            }
            return copyResourcesOnSave;
        }
    }

    private static class PackagingTypeDependentLookup extends ProxyLookup implements PropertyChangeListener {

        //#243866 both NbMavenProject and PackagingTypeDependentLookup are hard referenced from NbMavenProjectImpl
        //it should be safe to weak reference here, all should be GCed together.
        private final WeakReference<NbMavenProject> watcherRef;
        private String packaging;
        private final Lookup general;
        
        private volatile List<String> currentIds = new ArrayList<>();

        @SuppressWarnings("LeakingThisInConstructor")
        PackagingTypeDependentLookup(NbMavenProject watcher) {
            this.watcherRef = new WeakReference<NbMavenProject>(watcher);
            //needs to be kept around to prevent recreating instances
            general = Lookups.forPath("Projects/org-netbeans-modules-maven/Lookup"); //NOI18N
            check();
            watcher.addPropertyChangeListener(WeakListeners.propertyChange(this, watcher));
        }
        
        private String pluginDirectory(Artifact pluginArtifact) {
            String groupId = pluginArtifact.getGroupId();
            String artId = pluginArtifact.getArtifactId();
            
            return groupId + ":" + artId;
        }
        
        /**
         * Defines at least some order: let the layer positions to 
         * @param componentSet
         * @return 
         */
        private List<String> partialComponentsOrder(Collection<String> componentSet) {
            List<FileObject> fos = new ArrayList<>();
            FileObject root = FileUtil.getConfigFile("Projects/org-netbeans-modules-maven");
            for (String s : componentSet) {
                FileObject f = root.getFileObject(s);
                if (f != null) {
                    fos.add(f);
                }
            }
            List<String> orderedNames = FileUtil.getOrder(fos, false).stream().map(FileObject::getNameExt).collect(Collectors.toList());
            List<String> origList = new ArrayList<>(componentSet);
            origList.removeAll(orderedNames);
            orderedNames.addAll(origList);
            return orderedNames;
        }

        private void check() {
            //this call effectively calls project.getLookup(), when called in constructor will get back to the project's baselookup only.
            // but when called from propertyChange() then will call on entire composite lookup, is it a problem?  #230469
            List<String> newComponents = new ArrayList<>();
            NbMavenProject watcher = watcherRef.get();
            String newPackaging = packaging != null ? packaging : NbMavenProject.TYPE_JAR;
            List<Lookup> lookups = new ArrayList<>();
            List<String> old = currentIds;
            if (watcher != null) {
                newPackaging = watcher.getPackagingType(); 
                if (newPackaging == null) {
                    newPackaging = NbMavenProject.TYPE_JAR;
                }
                Set<Artifact> arts = watcher.getMavenProject().getPluginArtifacts();
                List<String> compNames = new ArrayList<>();
                if (arts != null) {
                    for (Artifact a : arts) {
                        compNames.add(pluginDirectory(a));
                    }
                }
                compNames.add(newPackaging);
                
                newComponents = partialComponentsOrder(compNames);
            } else {
                newComponents.add(newPackaging);
            }
            
            if (!newComponents.equals(old)) {
                for (String s : newComponents) {
                    lookups.add(Lookups.forPath("Projects/org-netbeans-modules-maven/" + s + "/Lookup")); // NOI18N
                }
                // put the general lookup last, so plugin - specific ones can override it
                lookups.add(general);
                lookups.add(Lookups.forPath("Projects/org-netbeans-modules-maven/_any/Lookup")); // NOI18N
                synchronized (this) {
                    if (currentIds != old) {
                        // the next computation started after us, do not interfere.
                        return;
                    }
                    currentIds = newComponents;
                }
                setLookups(lookups.toArray(new Lookup[lookups.size()]));
            }
        }
        
        public @Override void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                check();
            }
        }
    }

    private Lookup createBasicLookup(ProjectState state, M2AuxilaryConfigImpl auxiliary) {
        return Lookups.fixed(
                    this,
                    fileObject,
                    auxiliary,
                    auxiliary.getProblemProvider(),
                    auxprops,
                    new MavenProjectPropsImpl.Merger(auxprops),
                    profileHandler,
                    configProvider,
                    problemReporter,
                    watcher,
                    state,
                    UILookupMergerSupport.createPrivilegedTemplatesMerger(),
                    UILookupMergerSupport.createRecommendedTemplatesMerger(),
                    UILookupMergerSupport.createProjectProblemsProviderMerger(),
                    LookupProviderSupport.createSourcesMerger(),
                    LookupProviderSupport.createSharabilityQueryMerger(),
                    ProjectClassPathModifier.extenderForModifier(this),
                    LookupMergerSupport.createClassPathModifierMerger(),
                    new UnitTestsCompilerOptionsQueryImpl(this),
                    new PomCompilerOptionsQueryImpl(this),
                    LookupMergerSupport.createCompilerOptionsQueryMerger(),
                    MavenJPDAStart.create(this)
        );
    }

    //MEVENIDE-448 seems to help against creation of duplicate project instances
    // no idea why, it's supposed to be ProjectManager job.. maybe related to
    // maven impl of SubProjectProvider or FileOwnerQueryImplementation
    //TODO need to investigate why it's like that..
    
    //a renamed FileObject for project folder stays the same, changing the identity of the project, we have to use File.
    @Override
    public int hashCode() {
        return getPOMFile().hashCode() * 13;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Project) {
            NbMavenProjectImpl impl;
            if (obj instanceof NbMavenProjectImpl) {
                impl = ((NbMavenProjectImpl) obj);
            } else {
                impl = ((Project) obj).getLookup().lookup(NbMavenProjectImpl.class);
            }
            if (impl != null) {
                return getPOMFile().equals(impl.getPOMFile());
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Maven[" + getPOMFile().getAbsolutePath() + "]"; //NOI18N

    }
    
    interface FileProvider {
        File[] getFiles();
    }

    private class Updater implements FileChangeListener {

        
        private final FileProvider fileProvider;
        private List<File> filesToWatch;
        private long lastTime = 0;
        
        private Map<File, Long> lastMods;
        
        /** Relative file paths to watch. */
        Updater(FileProvider toWatch) {
            fileProvider = toWatch;            
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
        }

        @Override
        public void fileChanged(FileEvent fileEvent) {
                if (lastTime < fileEvent.getTime()) {
                    lastTime = System.currentTimeMillis();
//                    System.out.println("fired based on " + fileEvent.getFile() + fileEvent.getTime());
                    NbMavenProject.fireMavenProjectReload(NbMavenProjectImpl.this);
                }
        }

        @Override
        public void fileDataCreated(FileEvent fileEvent) {
                if (lastTime < fileEvent.getTime()) {
                    lastTime = System.currentTimeMillis();
//                    System.out.println("fired based on " + fileEvent.getFile() + fileEvent.getTime());
                    NbMavenProject.fireMavenProjectReload(NbMavenProjectImpl.this);
                }
        }

        @Override
        public void fileDeleted(FileEvent fileEvent) {
                lastTime = System.currentTimeMillis();
                NbMavenProject.fireMavenProjectReload(NbMavenProjectImpl.this);
        }

        @Override
        public void fileFolderCreated(FileEvent fileEvent) {
            //TODO possibly remove this fire.. watch for actual path..
//            NbMavenProject.fireMavenProjectReload(NbMavenProjectImpl.this);
        }    

        @Override
        public void fileRenamed(FileRenameEvent fileRenameEvent) {
        }

        synchronized void attachAll() {
            this.filesToWatch = new ArrayList<>(Arrays.asList(fileProvider.getFiles()));
            
            filesToWatch.addAll(getParents()); 
            Collections.sort(filesToWatch);
            for (File file : filesToWatch) {
                try {
                    FileUtil.addFileChangeListener(this, file);
                } catch (IllegalArgumentException ex) {
                    //giving up  on ever figuring why OPH is sometimes calls opened() twice in a row on single project.
                    //There's way too many moving parts. 
                    // * project lookup could be creating multiple instances of OPH
                    // * a close or open method for a random project/OPH could throw exception skipping our close? 
                    //   while OPL catches RuntimeExceptions, OPH merger bypasses that behaviour and handles all OPH as unit.
                    // * something in OPL or Group is wrong in terms of threading, timing or open/close projects calculation (could be equals/hascode on project related)
                    LOG.log(Level.INFO, "project opened twice in a row, issue #236211 for " + projectFile.getAbsolutePath(), ex);
                    Thread.dumpStack();
                    assert false : "project opened twice in a row, issue #236211 for " + projectFile.getAbsolutePath();
                }
            }
            
            if(lastMods == null) {
                // attached for the first time, 
                // preserve lastModified of interestig files 
                lastMods = new HashMap<>(filesToWatch.size());
                for (File file : filesToWatch) {
                    lastMods.put(file, file.lastModified());
                }
            } else {
                for (Map.Entry<File, Long> e : lastMods.entrySet()) {
                    File file = e.getKey();
                    long ts = file.lastModified();
                    if( e.getValue() < ts ) {
                        // attached after being previously dettached and 
                        // lastModified of an interesting file changed in the meantime 
                        // -> force pom refresh
                        lastMods.put(file, ts);
                        NbMavenProject.fireMavenProjectReload(NbMavenProjectImpl.this);
                    }
                }
                
            }            
        }

        protected List<File> getParents() {
            LinkedList<File> ret = new LinkedList<>();
            MavenProject project = getOriginalMavenProject();
            while(true) {
                try {
                    MavenProject parent = loadParentOf(getEmbedder(), project);
                    File parentFile = parent != null ? parent.getFile() : null;
                    if(parentFile != null) {
                        ret.add(parentFile);
                        project = parent;
                    } else {
                        break;
                    }
                } catch (ProjectBuildingException ex) {
                    break;
                }
            } 
            return ret;
        }

        synchronized void detachAll() {
            if (filesToWatch != null) {
                List<File> toWatch = filesToWatch;
                filesToWatch = null;
                for (File file : toWatch) {
                    try {
                        FileUtil.removeFileChangeListener(this, file);
                    } catch (IllegalArgumentException ex) {
                        LOG.log(Level.INFO, "project closed twice in a row, issue #236211 for " + projectFile.getAbsolutePath(), ex);
                        Thread.dumpStack();
                        assert false : "project closed twice in a row, issue #236211 for " + projectFile.getAbsolutePath();
                    }
                }
            }
        }
    }

}
