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
import java.net.MalformedURLException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.apache.maven.model.Model;
import org.apache.maven.model.ModelBase;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.ModelReader;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.modules.maven.problems.BatchProblemNotifier;
import org.netbeans.modules.maven.queries.MavenFileOwnerQueryImpl;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * openhook implementation, register global classpath and also
 * register the project in the fileOwnerQuery impl, that's important for interproject
 * dependencies to work.
 * @author  Milos Kleint
 */
@SuppressWarnings("ClassWithMultipleLoggers")
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType="org-netbeans-modules-maven")
public class ProjectOpenedHookImpl extends ProjectOpenedHook {
    private static final String PROP_BINARIES_CHECKED = "binariesChecked";
    private static final String PROP_JAVADOC_CHECKED = "javadocChecked";
    private static final String PROP_SOURCE_CHECKED = "sourceChecked";
   
    private final Project proj;
    private TransientRepositories transRepos;
    private final List<URI> uriReferences = new ArrayList<URI>();

    // ui logging
    static final String UI_LOGGER_NAME = "org.netbeans.ui.maven.project"; //NOI18N
    static final Logger UI_LOGGER = Logger.getLogger(UI_LOGGER_NAME);

    static final String USG_LOGGER_NAME = "org.netbeans.ui.metrics.maven"; //NOI18N
    static final Logger USG_LOGGER = Logger.getLogger(USG_LOGGER_NAME);

    private static final Logger LOGGER = Logger.getLogger(ProjectOpenedHookImpl.class.getName());
    private static final AtomicBoolean checkedIndices = new AtomicBoolean();
    
    //here we handle properly the case when someone changes a
    // ../../src path to ../../src2 path in the lifetime of the project.
    private final PropertyChangeListener extRootChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (NbMavenProject.PROP_PROJECT.equals(pce.getPropertyName())) {
                NbMavenProjectImpl project = proj.getLookup().lookup(NbMavenProjectImpl.class);
                Set<URI> newuris = getProjectExternalSourceRoots(project);
                synchronized (uriReferences) {
                    Set<URI> olduris = new HashSet<URI>(uriReferences);
                    olduris.removeAll(newuris);
                    newuris.removeAll(uriReferences);
                    for (URI old : olduris) {
                        FileOwnerQuery.markExternalOwner(old, null, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
                    }
                    for (URI nw : newuris) {
                        FileOwnerQuery.markExternalOwner(nw, proj, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
                    }
                    uriReferences.removeAll(olduris);
                    uriReferences.addAll(newuris);
                }
            }
        }
    };
    
    public ProjectOpenedHookImpl(Project proj) {
        this.proj = proj;
        assert checkIssue224012(proj);
    }

    @Messages("UI_MAVEN_PROJECT_OPENED=A Maven project was opened. Appending the project's packaging type.")
    protected @Override void projectOpened() {
        NbMavenProjectImpl project = proj.getLookup().lookup(NbMavenProjectImpl.class);
        project.startHardReferencingMavenPoject();
        checkBinaryDownloads();
        checkSourceDownloads();
        checkJavadocDownloads();
        project.attachUpdater();
        registerWithSubmodules(FileUtil.toFile(proj.getProjectDirectory()), new HashSet<File>());
        //manually register the listener for this project, we know it's loaded and should be listening on changes.
        //registerCoordinates() doesn't attach listeners
        MavenFileOwnerQueryImpl.getInstance().attachProjectListener(project);
        Set<URI> uris = getProjectExternalSourceRoots(project);
        synchronized (uriReferences) {
            for (URI uri : uris) {
                FileOwnerQuery.markExternalOwner(uri, proj, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
                uriReferences.add(uri);
            }
        }
        NbMavenProject watcher = project.getProjectWatcher();
        //XXX: is there an ordering problem? should this be done first right after the project changes, instead of ordinary listener?
        watcher.addPropertyChangeListener(extRootChangeListener);
        
        // register project's classpaths to GlobalPathRegistry
        ProjectSourcesClassPathProvider cpProvider = proj.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
        GlobalPathRegistry.getDefault().register(ClassPath.EXECUTE, cpProvider.getProjectClassPaths(ClassPath.EXECUTE));
        BatchProblemNotifier.opened(project);
        
        //UI logging.. log what was the packaging type for the opened project..
        LogRecord record = new LogRecord(Level.INFO, "UI_MAVEN_PROJECT_OPENED"); //NOI18N
        record.setLoggerName(UI_LOGGER_NAME); //NOI18N
        record.setParameters(new Object[] {watcher.getPackagingType()});
        record.setResourceBundle(NbBundle.getBundle(ProjectOpenedHookImpl.class));
        UI_LOGGER.log(record);

        //USG logging.. log what was the packaging type for the opened project..
        record = new LogRecord(Level.INFO, "USG_PROJECT_OPEN_MAVEN"); //NOI18N
        record.setLoggerName(USG_LOGGER_NAME); //NOI18N
        record.setParameters(new Object[] {watcher.getPackagingType()});
        USG_LOGGER.log(record);

        if (transRepos == null) {
            transRepos = new TransientRepositories(watcher);
        }
        transRepos.register();

        project.getCopyOnSaveResources().opened();

        //only check for the updates of index, if the indexing was already used.
        if (checkedIndices.compareAndSet(false, true) && existsDefaultIndexLocation() && RepositoryPreferences.isIndexRepositories()) {
            final int freq = RepositoryPreferences.getIndexUpdateFrequency();
            new RequestProcessor("Maven Repo Index Transfer/Scan").post(new Runnable() { // #138102
                public @Override void run() {
                    List<RepositoryInfo> ris = RepositoryPreferences.getInstance().getRepositoryInfos();
                    Set<String> doNotIndexRepos = getDoNotIndexRepos();
                    for (final RepositoryInfo ri : ris) {
                        //check this repo can be indexed
                        if ( (!ri.isRemoteDownloadable() && !ri.isLocal()) || doNotIndexRepos.contains(ri.getId())) {
                            LOGGER.log(Level.FINER, "Skipping Index At Startup for :{0}", ri.getId());//NOI18N
                            continue;
                        }
                        if (freq != RepositoryPreferences.FREQ_NEVER) {
                            boolean run = false;
                            if (freq == RepositoryPreferences.FREQ_STARTUP) {
                                LOGGER.log(Level.FINER, "Index At Startup :{0}", ri.getId());//NOI18N
                                run = true;
                            } else if (freq == RepositoryPreferences.FREQ_ONCE_DAY && checkDiff(ri.getId(), 86400000L)) {
                                LOGGER.log(Level.FINER, "Index Once a Day :{0}", ri.getId());//NOI18N
                                run = true;
                            } else if (freq == RepositoryPreferences.FREQ_ONCE_WEEK && checkDiff(ri.getId(), 604800000L)) {
                                LOGGER.log(Level.FINER, "Index once a Week :{0}", ri.getId());//NOI18N
                                run = true;
                            }
                            if (run && ri.isRemoteDownloadable()) {
                                RepositoryIndexer.indexRepo(ri);
                            }
                        }
                    }
                }
            }, 1000 * 60 * 2);
        }
    }

    private Set<URI> getProjectExternalSourceRoots(NbMavenProjectImpl project) throws IllegalArgumentException {
        Set<URI> uris = new HashSet<URI>();
        Set<URI> toRet = new HashSet<URI>();
        uris.addAll(Arrays.asList(project.getSourceRoots(false)));
        uris.addAll(Arrays.asList(project.getSourceRoots(true)));
        //#167572 in the unlikely event that generated sources are located outside of
        // the project root.
        uris.addAll(Arrays.asList(project.getGeneratedSourceRoots(false)));
        uris.addAll(Arrays.asList(project.getGeneratedSourceRoots(true)));
        URI rootUri = Utilities.toURI(FileUtil.toFile(project.getProjectDirectory()));
        File rootDir = Utilities.toFile(rootUri);
        for (URI uri : uris) {
            if (FileUtilities.getRelativePath(rootDir, Utilities.toFile(uri)) == null) {
                toRet.add(uri);
            }
        }
        return toRet;
    }
    private boolean existsDefaultIndexLocation() {
        File cacheDir = new File(Places.getCacheDirectory(), "mavenindex");//NOI18N
        return cacheDir.exists() && cacheDir.isDirectory();
    }
    private boolean checkDiff(String repoid, long amount) {
        Date date = RepositoryPreferences.getLastIndexUpdate(repoid);
        Date now = new Date();
        LOGGER.log(Level.FINER, "Check Date Diff :{0}", repoid);//NOI18N
        LOGGER.log(Level.FINER, "Last Indexed Date :{0}", SimpleDateFormat.getInstance().format(date));//NOI18N
        LOGGER.log(Level.FINER, "Now :{0}", SimpleDateFormat.getInstance().format(now));//NOI18N
        long diff = now.getTime() - date.getTime();
        LOGGER.log(Level.FINER, "Diff :{0}", diff);//NOI18N
        return (diff < 0 || diff > amount);
    }

    protected @Override void projectClosed() {
        NbMavenProjectImpl project = proj.getLookup().lookup(NbMavenProjectImpl.class);
        //we stop listening for changes in external roots
        //but as before, we keep the latest known roots upon closing..
        project.getProjectWatcher().removePropertyChangeListener(extRootChangeListener);
        synchronized (uriReferences) {
            uriReferences.clear();
        }
        
        project.detachUpdater();
        // unregister project's classpaths to GlobalPathRegistry
        ProjectSourcesClassPathProvider cpProvider = proj.getLookup().lookup(ProjectSourcesClassPathProvider.class);
        GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
        GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
        GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
        GlobalPathRegistry.getDefault().unregister(ClassPath.EXECUTE, cpProvider.getProjectClassPaths(ClassPath.EXECUTE));
        BatchProblemNotifier.closed(project);
        project.getCopyOnSaveResources().closed();

        if (transRepos != null) { // XXX #212555 projectOpened was not called first?
            transRepos.unregister();
        }
        project.stopHardReferencingMavenPoject();
    }
   
   private void checkBinaryDownloads() {
       MavenSettings.DownloadStrategy ds = MavenSettings.getDefault().getBinaryDownloadStrategy();
       if (ds.equals(MavenSettings.DownloadStrategy.NEVER)) {
           return;
       }

       NbMavenProject watcher = proj.getLookup().lookup(NbMavenProject.class);
       Preferences prefs = ProjectUtils.getPreferences(proj, NbMavenProject.class, false);
       if (ds.equals(MavenSettings.DownloadStrategy.EVERY_OPEN)) {
            watcher.synchronousDependencyDownload();
            prefs.putBoolean(PROP_BINARIES_CHECKED, true);
            try {
                prefs.sync();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
       } else if (ds.equals(MavenSettings.DownloadStrategy.FIRST_OPEN)) {
           boolean alreadyChecked = prefs.getBoolean(PROP_BINARIES_CHECKED, false);
           if (!alreadyChecked) {
                watcher.synchronousDependencyDownload();
                prefs.putBoolean(PROP_BINARIES_CHECKED, true);
                try {
                    prefs.sync();
                } catch (BackingStoreException ex) {
                    Exceptions.printStackTrace(ex);
                }
           }
       }
   }

   private void checkJavadocDownloads() {
       MavenSettings.DownloadStrategy ds = MavenSettings.getDefault().getJavadocDownloadStrategy();
       if (ds.equals(MavenSettings.DownloadStrategy.NEVER)) {
           return;
       }

       NbMavenProject watcher = proj.getLookup().lookup(NbMavenProject.class);
       Preferences prefs = ProjectUtils.getPreferences(proj, NbMavenProject.class, false);
       if (ds.equals(MavenSettings.DownloadStrategy.EVERY_OPEN)) {
            watcher.triggerSourceJavadocDownload(true);
            prefs.putBoolean(PROP_JAVADOC_CHECKED, true);
            try {
                prefs.sync();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
       } else if (ds.equals(MavenSettings.DownloadStrategy.FIRST_OPEN)) {
           boolean alreadyChecked = prefs.getBoolean(PROP_JAVADOC_CHECKED, false);
           if (!alreadyChecked) {
                watcher.triggerSourceJavadocDownload(true);
                prefs.putBoolean(PROP_JAVADOC_CHECKED, true);
                try {
                    prefs.sync();
                } catch (BackingStoreException ex) {
                    Exceptions.printStackTrace(ex);
                }
           }
       }
   }

   private void checkSourceDownloads() {
       MavenSettings.DownloadStrategy ds = MavenSettings.getDefault().getSourceDownloadStrategy();
       if (ds.equals(MavenSettings.DownloadStrategy.NEVER)) {
           return;
       }

       NbMavenProject watcher = proj.getLookup().lookup(NbMavenProject.class);
       Preferences prefs = ProjectUtils.getPreferences(proj, NbMavenProject.class, false);
       if (ds.equals(MavenSettings.DownloadStrategy.EVERY_OPEN)) {
            watcher.triggerSourceJavadocDownload(false);
            prefs.putBoolean(PROP_SOURCE_CHECKED, true);
            try {
                prefs.sync();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
       } else if (ds.equals(MavenSettings.DownloadStrategy.FIRST_OPEN)) {
           boolean alreadyChecked = prefs.getBoolean(PROP_SOURCE_CHECKED, false);
           if (!alreadyChecked) {
                watcher.triggerSourceJavadocDownload(false);
                prefs.putBoolean(PROP_SOURCE_CHECKED, true);
                try {
                    prefs.sync();
                } catch (BackingStoreException ex) {
                    Exceptions.printStackTrace(ex);
                }
           }
       }
   }

    /** Similar to {@link SubprojectProviderImpl#addProjectModules} but more efficient for large numbers of modules. */
    private static void registerWithSubmodules(File basedir, Set<File> registered) { // #200445
        if (!registered.add(basedir)) {
            return;
        }
        File pom = new File(basedir, "pom.xml");
        if (!pom.isFile()) {
            return;
        }
        ModelReader reader = EmbedderFactory.getProjectEmbedder().lookupComponent(ModelReader.class);
        Model model;
        try {
            model = reader.read(pom, Collections.singletonMap(ModelReader.IS_STRICT, false));
        } catch (IOException x) {
            LOGGER.log(Level.FINE, "could not parse " + pom, x);
            return;
        }
        Parent parent = model.getParent();
        String groupId = model.getGroupId();
        if (groupId == null && parent != null) {
            groupId = parent.getGroupId();
        }
        if (groupId == null) {
            LOGGER.log(Level.WARNING, "no groupId in {0}", pom);
            return;
        }
        String artifactId = model.getArtifactId();
        if (artifactId == null && parent != null) {
            artifactId = parent.getArtifactId();
        }
        if (artifactId == null) {
            LOGGER.log(Level.WARNING, "no artifactId in {0}", pom);
            return;
        }
        String version = model.getVersion();
        if (version == null && parent != null) {
            version = parent.getVersion();
        }
        if (version == null) {
            LOGGER.log(Level.WARNING, "no version in {0}", pom);
            return;
        }        
        
        if (groupId.contains("${") || artifactId.contains("${") || version.contains("${")) {
            LOGGER.log(Level.FINE, "Unevaluated groupId/artifactId/version in {0}", basedir);
            FileObject basedirFO = FileUtil.toFileObject(basedir);
            if (basedirFO != null) {
                try {
                    Project p = ProjectManager.getDefault().findProject(basedirFO);
                    if (p != null) {
                        NbMavenProjectImpl nbmp = p.getLookup().lookup(NbMavenProjectImpl.class);
                        if (nbmp != null) {
                            MavenFileOwnerQueryImpl.getInstance().registerProject(nbmp, true);
                        } else {
                            LOGGER.log(Level.FINE, "not a Maven project in {0}", basedir);
                        }
                    } else {
                        LOGGER.log(Level.FINE, "no project in {0}", basedir);
                    }
                } catch (IOException x) {
                    LOGGER.log(Level.FINE, null, x);
                }
            } else {
                LOGGER.log(Level.FINE, "no FileObject for {0}", basedir);
            }
        } else {
            try {
                MavenFileOwnerQueryImpl.getInstance().registerCoordinates(groupId, artifactId, version, Utilities.toURI(basedir).toURL(), true);
            } catch (MalformedURLException x) {
                LOGGER.log(Level.FINE, null, x);
            }
        }
        scanForSubmodulesIn(model, basedir, registered);
        model.getProfiles();
        for (Profile profile : model.getProfiles()) {
            scanForSubmodulesIn(profile, basedir, registered);
        }
    }
    private static void scanForSubmodulesIn(ModelBase projectOrProfile, File basedir, Set<File> registered) throws IllegalArgumentException {
        for (String module : projectOrProfile.getModules()) {
            if (module == null) {
                //#205690 apparently in some rare scenarios module can be null, I was not able to reproduce myself
                //maven itself checks for null value during validation, but at later stages doesn't always check.
                //additional aspect for consideration is that in this case the value is taken from Model class not MavenProject
                continue;
            }
            registerWithSubmodules(FileUtilities.resolveFilePath(basedir, module), registered);
        }
    }

    private boolean checkIssue224012(Project project) {
        if (project instanceof NbMavenProjectImpl) { //unfortunately cannot use lookup here, rendering the assert useless for ergonomics turned on..
            NbMavenProjectImpl im = (NbMavenProjectImpl)project;
            return im.setIssue224012(this, new Exception("Thread:" + Thread.currentThread().getName() + " at " + System.currentTimeMillis() + " for " + im.getPOMFile()));
        }
        return true;
    }

    private Set<String> getDoNotIndexRepos() {
        String st = System.getProperty("maven.indexing.doNotAutoIndex");
        if(st == null || "".equals(st)) {
            return Collections.emptySet();
        }
        String[] repos = st.split(";");
        return new HashSet<>(Arrays.asList(repos));
    }
}
