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

package org.netbeans.modules.maven.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.InvalidArtifactRTException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.BasicAggregateProgressFactory;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectActionContext;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import static org.netbeans.modules.maven.api.Bundle.*;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
import org.netbeans.modules.maven.modelcache.MavenProjectCache;
import org.netbeans.modules.maven.options.MavenSettings;
import org.netbeans.modules.maven.options.MavenSettings.DownloadStrategy;
import org.netbeans.modules.maven.spi.PackagingProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * an instance resides in project lookup, allows to get notified on project and 
 * relative path changes.
 * <p/>
 * <b>From version 2.148</b> plugin-specific services can be registered using {@link ProjectServiceProvider} 
 * annotation in subfolders of the project Lookup registration area whose names follow a Plugin group and 
 * artifact ID. 
 * <p/>
 * <div class="nonnormative">
 * {@snippet file="org/netbeans/modules/maven/NbMavenProjectImplTest.java" region="ProjectServiceProvider.pluginSpecific"}
 * Shows a service, that will become available from project Lookup whenever the project uses {@code org.netbeans.modules.maven:test.plugin}
 * plugin in its model.
 * </div>
 * 
 * @author mkleint
 */
public final class NbMavenProject {
    private static final Logger LOG = Logger.getLogger(NbMavenProject.class.getName());
    /**
     * the only property change fired by the class, means that the pom file
     * has changed.
     */
    public static final String PROP_PROJECT = "MavenProject"; //NOI18N

    /**
     * ID of the Maven project type.
     * @since 2.148
     */
    public static final String TYPE = "org-netbeans-modules-maven"; //NOI18N
    
    /**
     * TODO comment
     * 
     */
    public static final String PROP_RESOURCE = "RESOURCES"; //NOI18N
    
    private final NbMavenProjectImpl project;
    private final PropertyChangeSupport support;
    private final FCHSL listener = new FCHSL();
    //#216001 addWatchedPath appeared to accumulate files forever on each project reload.
    //that's because source roots get added repeatedly but never get removed and listening to changed happens elsewhere.
    //the imagined fix for 216001 is to keep each item just once. That would break once multiple sources add a given file and one of them removes it
    //as a hotfix this solution is ok, if we don't get some data updated, we should remove the watchedPath pattern altogether.
    private final Set<File> files = new HashSet<File>();
    
    static {
        AccessorImpl impl = new AccessorImpl();
        impl.assign();
    }
    private final RequestProcessor.Task task;
    private static final RequestProcessor BINARYRP = new RequestProcessor("Maven projects Binary Downloads", 1);
    private static final RequestProcessor NONBINARYRP = new RequestProcessor("Maven projects Source/Javadoc Downloads", 1);

    static class AccessorImpl extends NbMavenProjectImpl.WatcherAccessor {
        
        
         public void assign() {
             if (NbMavenProjectImpl.ACCESSOR == null) {
                 NbMavenProjectImpl.ACCESSOR = this;
             }
         }
    
        @Override
        public NbMavenProject createWatcher(NbMavenProjectImpl proj) {
            return new NbMavenProject(proj);
        }
        
        @Override
        public void doFireReload(NbMavenProject watcher) {
            watcher.doFireReload();
        }

    }


    
    private class FCHSL implements FileChangeListener, PropertyChangeListener {


        @Override
        public void fileFolderCreated(FileEvent fe) {
            fireChange(Utilities.toURI(FileUtil.toFile(fe.getFile())));
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            fireChange(Utilities.toURI(FileUtil.toFile(fe.getFile())));
        }

        @Override
        public void fileChanged(FileEvent fe) {
            fireChange(Utilities.toURI(FileUtil.toFile(fe.getFile())));
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            fireChange(Utilities.toURI(FileUtil.toFile(fe.getFile())));
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            fireChange(Utilities.toURI(FileUtil.toFile(fe.getFile())));
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            doFireReload();
        }
    }
    
    
    /** Creates a new instance of NbMavenProject */
    private NbMavenProject(NbMavenProjectImpl proj) {
        project = proj;
        //TODO oh well, the sources is the actual project instance not the watcher.. a problem?
        support = new PropertyChangeSupport(proj);
        task = createBinaryDownloadTask(BINARYRP);
        MavenSettings.getDefault().addWeakPropertyChangeListener(listener);
    }
    
    /**
     * Checks if the project is completely broken. Also see {@link #getPartialProject}.
     * @return true, if the project is broken and could not be loaded
     * @see #getPartialProject
     */
    public boolean isUnloadable() {
        return MavenProjectCache.isFallbackproject(getMavenProject());
    }
    

    @Messages({
        "Progress_Download=Downloading Maven dependencies", 
        "# {0} - error message",
        "MSG_Failed=Failed to download - {0}", 
        "MSG_Done=Finished retrieving dependencies from remote repositories."})
    private RequestProcessor.Task createBinaryDownloadTask(RequestProcessor rp) {
        return rp.create(new Runnable() {
            @Override
            public void run() {
                    //#146171 try the hardest to avoid NPE for files/directories that
                    // seemed to have been deleted while the task was scheduled.
                    FileObject fo = project.getProjectDirectory();
                    if (fo == null || !fo.isValid()) {
                        return;
                    }
                    fo = fo.getFileObject("pom.xml"); //NOI18N
                    if (fo == null) {
                        return;
                    }
                    File pomFile = FileUtil.toFile(fo);
                    if (pomFile == null) {
                        return;
                    }
                    MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
                    AggregateProgressHandle hndl = BasicAggregateProgressFactory.createHandle(Progress_Download(),
                            new ProgressContributor[] {
                                BasicAggregateProgressFactory.createProgressContributor("zaloha") },  //NOI18N
                            ProgressTransferListener.cancellable(), null);

                    boolean ok = true;
                    try {
                        ProgressTransferListener.setAggregateHandle(hndl);
                        hndl.start();
                        MavenExecutionRequest req = online.createMavenExecutionRequest();
                        req.setPom(pomFile);
                        req.setTransferListener(ProgressTransferListener.activeListener());
                        MavenExecutionResult res = online.readProjectWithDependencies(req, false); //NOI18N
                        if (res.hasExceptions()) {
                            ok = false;
                            Exception ex = (Exception)res.getExceptions().get(0);
                            StatusDisplayer.getDefault().setStatusText(MSG_Failed(ex.getLocalizedMessage()));
                        }
                    } catch (ThreadDeath d) { // download interrupted
                    } catch (IllegalStateException x) {
                        if (x.getCause() instanceof ThreadDeath) {
                            // #197261: download interrupted
                        } else {
                            throw x;
                        }
                    } catch (RuntimeException exc) {
                        //guard against exceptions that are not processed by the embedder
                        //#136184 NumberFormatException, #214152 InvalidArtifactRTException
                        StatusDisplayer.getDefault().setStatusText(MSG_Failed(exc.getLocalizedMessage()));
                    } finally {
                        hndl.finish();
                        ProgressTransferListener.clearAggregateHandle();
                    }
                    if (ok) {
                        StatusDisplayer.getDefault().setStatusText(MSG_Done());
                    }
                    if (support.hasListeners(NbMavenProject.PROP_PROJECT)) {
                        NbMavenProject.fireMavenProjectReload(project);
                    }
            }
        });
    }
    
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener(propertyChangeListener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener(propertyChangeListener);
    }
    
    /**
     * Returns the current maven project model from the embedder.
     * Should never be kept around for long but always reloaded from here, on 
     * a project change the correct instance changes as the embedder reloads it.
     * Never returns null but check {@link #isErrorPlaceholder} if necessary.
     * @return 
     */ 
    public @NonNull MavenProject getMavenProject() {
        return project.getOriginalMavenProject();
    }
    
    /**
     * Returns a project evaluated for a certain purpose. The while {@link #getMavenProject}
     * works with the <b>active configuration</b> and does not apply any action-specific properties,
     * this method tries to apply mappings, configurations, etc when loading the project model.
     * <p>
     * Note that loading an evaluated project may take significant time (comparable to loading
     * the base project itself). The implementation might optimize if the passed context does not
     * prescribe different profiles, properties etc than have been used for the default model.
     * 
     * @param context the loading context
     * @return evaluated project
     */
    public @NonNull MavenProject getEvaluatedProject(ProjectActionContext context) {
        return project.getEvaluatedProject(context);
    }
    
    /**
     * a marginally unreliable, non blocking method for figuring if the model is loaded or not.
     * @return 
     */
    public boolean isMavenProjectLoaded() {
        return project.isMavenProjectLoaded();
    }

    public @NonNull MavenProject loadAlternateMavenProject(MavenEmbedder embedder, List<String> activeProfiles, Properties properties) {
        return project.loadMavenProject(embedder, activeProfiles, properties);
    }

    /**
     * 
     * @param test are test resources requested, if false, resources for base sources are returned
     * @return
     */
    public URI[] getResources(boolean test) {
        return project.getResources(test);
    }

    /**
     * Standardized way of finding output directory even for broken projects.
     * @param test true for {@code target/test-classes}, false for {@code target/classes}
     * @return the configured output directory (normalized)
     */
    public File getOutputDirectory(boolean test) {
        Build build = getMavenProject().getBuild();
        String path = build != null ? (test ? build.getTestOutputDirectory() : build.getOutputDirectory()) : null;
        File toRet;
        if (path != null) {
            toRet = FileUtil.normalizeFile(new File(path));
        } else { // #189092
            //getMavenProject().getBasedir() is normalized.
            toRet =  new File(new File(getMavenProject().getBasedir(), "target"), test ? "test-classes" : "classes"); // NOI18N
        }
        return toRet;
    }

    /**
     * 
     * @return
     */
    public URI getWebAppDirectory() {
        return project.getWebAppDirectory();
    }
    
    public URI getEarAppDirectory() {
        return project.getEarAppDirectory();
    }
    
    public static final String TYPE_JAR = "jar"; //NOI18N
    public static final String TYPE_WAR = "war"; //NOI18N
    public static final String TYPE_EAR = "ear"; //NOI18N
    public static final String TYPE_EJB = "ejb"; //NOI18N
    public static final String TYPE_APPCLIENT = "app-client"; //NOI18N
    public static final String TYPE_NBM = "nbm"; //NOI18N
    public static final String TYPE_NBM_APPLICATION = "nbm-application"; //NOI18N
    public static final String TYPE_OSGI = "bundle"; //NOI18N
    public static final String TYPE_POM = "pom"; //NOI18N
    
    /**
     * Gets an "effective" packaging for the project.
     * Normally this is just the Maven model's declared packaging.
     * But {@link PackagingProvider}s can affect the decision.
     * The resulting type will be used to control most IDE functions, including packaging-specific lookup.
     * @return 
     */
    public String getPackagingType() {
        for (PackagingProvider pp : Lookup.getDefault().lookupAll(PackagingProvider.class)) {
            String p = pp.packaging(project);
            if (p != null) {
                LOG.log(Level.FINE, "Packaging provider {0} returned packacing: {1}", new Object[] { pp, p });
                return p;
            }
        }
        return getMavenProject().getPackaging();
    }
    
    /**
     * Returns the raw pom model for the project.
     * @return 
     */
    public Model getRawModel() throws ModelBuildingException {
        return project.getRawModel();
    }
    
    /**
     * 
     * @param relPath
     */
    public void addWatchedPath(String relPath) {
        addWatchedPath(FileUtilities.getDirURI(project.getProjectDirectory(), relPath));
    } 
    /**
     * 
     * @param uri
     */
    public void addWatchedPath(URI uri) {
        //#110599
        boolean addListener = false;
        File fil = Utilities.toFile(uri);
        synchronized (files) {
    //#216001 addWatchedPath appeared to accumulate files forever on each project reload.
    //that's because source roots get added repeatedly but never get removed and listening to changed happens elsewhere.
    //the imagined fix for 216001 is to keep each item just once. That would break once multiple sources add a given file and one of them removes it
    //as a hotfix this solution is ok, if we don't get some data updated, we should remove the watchedPath pattern altogether.
            if (files.add(fil)) {
                addListener = true;
            }
        }
        if (addListener) {
            FileUtil.addFileChangeListener(listener, fil);
        }
    } 

    /**
     * asynchronous dependency download, scheduled to some time in the future. Useful
     * for cases when a 3rd party codebase calls maven classes and can do so repeatedly in one sequence.
     */
    public void triggerDependencyDownload() {
        synchronized (task) {
            task.schedule(1000);
        }
    }

    /**
     * Not to be called from AWT, will wait til the project binary dependency resolution finishes.
     */
    public void synchronousDependencyDownload() {
        assert !SwingUtilities.isEventDispatchThread() : " Not to be called from AWT, can take significant amount ot time to download dependencies from the network."; //NOI18N
        synchronized (task) {
            task.schedule(0);
            task.waitFinished();
        }
    }

    /**
     * @deprecated Use {@link #downloadDependencyAndJavadocSource(boolean)} with {@code true}.
     */
    @Deprecated
    public void downloadDependencyAndJavadocSource() {
        downloadDependencyAndJavadocSource(true);
    }
    /**
     * Download binaries and then trigger dependency javadoc/source download (in async mode) if download strategy is not DownloadStrategy.NEVER in options
     * Not to be called from AWT thread in synch mode; the current thread will continue after downloading binaries and firing project change event.
     * @param synch true to download dependencies binaries (not source/Javadoc) synchronously; false to download everything asynch
     */
    public void downloadDependencyAndJavadocSource(boolean synch) {
        if (synch) {
            synchronousDependencyDownload();
        } else {
            triggerDependencyDownload();
        }
        //see Bug 189350 : honer global  maven settings
        if (MavenSettings.getDefault().getJavadocDownloadStrategy() != DownloadStrategy.NEVER) {
            triggerSourceJavadocDownload(true);
        }
        if (MavenSettings.getDefault().getSourceDownloadStrategy() != DownloadStrategy.NEVER) {
            triggerSourceJavadocDownload(false);
        }
    }


    @Messages({"Progress_Javadoc=Downloading Javadoc", "Progress_Source=Downloading Sources"})
    public void triggerSourceJavadocDownload(final boolean javadoc) {
        NONBINARYRP.post(new Runnable() {
            @Override
            public void run() {
                Set<Artifact> arts = project.getOriginalMavenProject().getArtifacts();
                ProgressContributor[] contribs = new ProgressContributor[arts.size()];
                for (int i = 0; i < arts.size(); i++) {
                    contribs[i] = BasicAggregateProgressFactory.createProgressContributor("multi-" + i); //NOI18N
                }
                String label = javadoc ? Progress_Javadoc() : Progress_Source();
                AggregateProgressHandle handle = BasicAggregateProgressFactory.createHandle(label,
                        contribs, ProgressTransferListener.cancellable(), null);
                handle.start();
                try {
                    ProgressTransferListener.setAggregateHandle(handle);
                    int index = 0;
                    for (Artifact a : arts) {
                        downloadOneJavadocSources(contribs[index], project, a, javadoc);
                        index++;
                    }
                } catch (ThreadDeath d) { // download interrupted
                } catch (IllegalStateException ise) { //download interrupted in dependent thread. #213812
                    if (!(ise.getCause() instanceof ThreadDeath)) {
                        throw ise;
                    }
                } finally {
                    handle.finish();
                    ProgressTransferListener.clearAggregateHandle();
                    fireProjectReload();
                }
            }
        });
    }


    @Messages({
        "# {0} - artifact id",
        "MSG_Checking_Javadoc=Checking Javadoc for {0}", 
        "# {0} - artifact id",
        "MSG_Checking_Sources=Checking Sources for {0}"})
    private static void downloadOneJavadocSources(ProgressContributor progress,
                                               NbMavenProjectImpl project, Artifact art, boolean isjavadoc) {
        MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
        progress.start(2);
        if ( Artifact.SCOPE_SYSTEM.equals(art.getScope())) {
            progress.finish();
            return;
        }
        try {
            if (isjavadoc) {
                Artifact javadoc = project.getEmbedder().createArtifactWithClassifier(
                    art.getGroupId(),
                    art.getArtifactId(),
                    art.getVersion(),
                    art.getType(),
                    "javadoc"); //NOI18N
                progress.progress(MSG_Checking_Javadoc(art.getId()), 1);
                online.resolveArtifact(javadoc, project.getOriginalMavenProject().getRemoteArtifactRepositories(), project.getEmbedder().getLocalRepository());
            } else {
                Artifact sources = project.getEmbedder().createArtifactWithClassifier(
                    art.getGroupId(),
                    art.getArtifactId(),
                    art.getVersion(),
                    art.getType(),
                    "sources"); //NOI18N
                progress.progress(MSG_Checking_Sources(art.getId()), 1);
                online.resolveArtifact(sources, project.getOriginalMavenProject().getRemoteArtifactRepositories(), project.getEmbedder().getLocalRepository());
            }
        } catch (ThreadDeath td) {
        } catch (IllegalStateException ise) { //download interrupted in dependent thread. #213812
            if (!(ise.getCause() instanceof ThreadDeath)) {
                throw ise;
            }   
        } catch (ArtifactNotFoundException ex) {
            // just ignore..ex.printStackTrace();
        } catch (ArtifactResolutionException ex) {
            // just ignore..ex.printStackTrace();
        } catch (InvalidArtifactRTException ex) { //214152 InvalidArtifactRTException
            // just ignore..ex.printStackTrace();
        } finally {
            progress.finish();
        }
    }

    
    public void removeWatchedPath(String relPath) {
        removeWatchedPath(FileUtilities.getDirURI(project.getProjectDirectory(), relPath));
    }
    
    public void removeWatchedPath(URI uri) {
        //#110599
        boolean removeListener;
        File fil = Utilities.toFile(uri);
        synchronized (files) {
    //#216001 addWatchedPath appeared to accumulate files forever on each project reload.
    //that's because source roots get added repeatedly but never get removed and listening to changed happens elsewhere.
    //the imagined fix for 216001 is to keep each item just once. That would break once multiple sources add a given file and one of them removes it
    //as a hotfix this solution is ok, if we don't get some data updated, we should remove the watchedPath pattern altogether.
            removeListener = files.remove(fil);
        }
        if (removeListener) {
            FileUtil.removeFileChangeListener(listener, fil);
        }
    } 
    
    
    //TODO better do in ReqProcessor to break the listener chaining??
    private void fireChange(URI uri) {
        support.firePropertyChange(PROP_RESOURCE, null, uri);
    }
    
    /**
     * 
     */ 
    private RequestProcessor.Task fireProjectReload() {
        return project.fireProjectReload(true);
    }
    
    private void doFireReload() {
        MavenProject p = project.getOriginalMavenProjectOrNull();
        LOG.log(Level.FINE, "Firing PROJECT change for maven project {0}, mavenprj {1}", new Object[] { this, System.identityHashCode(p == null ? this : p) });
        FileUtil.refreshFor(FileUtil.toFile(project.getProjectDirectory()));
        NbMavenProjectImpl.refreshLocalRepository(project);
        support.firePropertyChange(PROP_PROJECT, null, null);
    }
    
    /**
     * utility method for triggering a maven project reload. 
     * if the project passed in is a Maven based project, will
     * fire reload of the project, otherwise will do nothing.
     * @param prj
     */ 
    
    public static void fireMavenProjectReload(Project prj) {
        if (prj != null) {
            NbMavenProject watcher = prj.getLookup().lookup(NbMavenProject.class);
            if (watcher != null) {
                watcher.fireProjectReload();
            }
        }
    }

    public static void addPropertyChangeListener(Project prj, PropertyChangeListener listener) {
        if (prj instanceof NbMavenProjectImpl) {
            // cannot call getLookup() -> stackoverflow when called from NbMavenProjectImpl.createBasicLookup()..
            NbMavenProject watcher = ((NbMavenProjectImpl)prj).getProjectWatcher();
            watcher.addPropertyChangeListener(listener);
        } else {
            assert false : "Attempted to add PropertyChangeListener to project " + prj; //NOI18N
        }
    }
    
    public static void removePropertyChangeListener(Project prj, PropertyChangeListener listener) {
        if (prj instanceof NbMavenProjectImpl) {
            // cannot call getLookup() -> stackoverflow when called from NbMavenProjectImpl.createBasicLookup()..
            NbMavenProject watcher = ((NbMavenProjectImpl)prj).getProjectWatcher();
            watcher.removePropertyChangeListener(listener);
        } else {
            assert false : "Attempted to remove PropertyChangeListener from project " + prj; //NOI18N
        }
    }
    
    /**
     * Retrieves at least partial project information. A MavenProject instance may be a <b>fallback</b> in case the reading
     * fails because of locally missing artifacts and/or referenced parents. However partial model may be available. The function
     * returns the passed project, if it read correctly. Otherwise, it attempts to locate a partially load project and returns that one.
     * If a partial project is not available, it will return the passed (fallback) project.
     * <p>
     * The result can be checked to be {@link #isErrorPlaceholder} to determine if the result was a returned partial project or not.
     * Note that partial projects may not resolve all references properly, be prepared for unresolved artifacts and/or plugins. Do not pass
     * partial projects blindly around.
     * <p>
     * Returns {@code null} if the passed project is {@code null}
     * @param project the project to check
     * @return partial project if the passed project did not load properly and the partial project is available.
     * @since 2.161
     */
    public static MavenProject getPartialProject(MavenProject project) {
        if (project == null) {
            return null;
        }
        if (isIncomplete(project)) {
            MavenProject pp = MavenProjectCache.getPartialProject(project);
            if (pp != null) {
                return pp;
            }
        }
        return project;
    }

    /**
     * Checks whether a given project is just an error placeholder. Such project may be fundamentally broken, i.e. missing
     * declarations from the parent, unresolved dependencies or versions. Also see {@link #isIncomplete}
     * @param project a project loaded by e.g. {@link #getMavenProject}
     * @return true if it was loaded as an error fallback, false for a normal project
     * @since 2.24
     * @see #isIncomplete
     * @see #getPartialProject
     */
    public static boolean isErrorPlaceholder(@NonNull MavenProject project) {
        return MavenProjectCache.isFallbackproject(project); // see NbMavenProjectImpl.getFallbackProject
    }
    
    /**
     * Checks if the project resolved using incomplete or missing information. Each {@link #isErrorPlaceholder} is an incomplete project.
     * If the project is just missing proper referenced artifacts, it will not be reported as a {@link #isErrorPlaceholder}, but as {@link #isIncomplete}.
     * @param project
     * @return true, if the project is not completely resolved
     * @since 2.161
     */
    public static boolean isIncomplete(@NonNull MavenProject project) {
        return MavenProjectCache.isIncompleteProject(project); 
    }

    @Override public String toString() {
        return project.toString();
    }
    
}
