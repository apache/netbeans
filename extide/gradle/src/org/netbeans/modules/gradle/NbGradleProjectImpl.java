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

package org.netbeans.modules.gradle;

import org.netbeans.modules.gradle.loaders.GradleProjectLoaderImpl;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.api.NbGradleProject.Quality;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;

import static org.netbeans.modules.gradle.api.NbGradleProject.Quality.*;

import static java.util.logging.Level.*;

import java.util.logging.Logger;
import org.gradle.tooling.ProjectConnection;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.options.GradleExperimentalSettings;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class NbGradleProjectImpl implements Project {

    private static final Logger LOG = Logger.getLogger(NbGradleProjectImpl.class.getName());

    public static final RequestProcessor RELOAD_RP = new RequestProcessor("Gradle project reloading", 1); //NOI18
    private final RequestProcessor.Task reloadTask = RELOAD_RP.create(new Runnable() {
        @Override
        public void run() {
            project = loadProject();
            ACCESSOR.doFireReload(watcher);
        }
    });

    private final FileObject projectDir;
    private final ProjectState projectState;
    private final Lookup lookup;
    private final Lookup basicLookup;
    private final Lookup completeLookup;
    private Updater openedProjectUpdater;
    private Quality aimedQuality = FALLBACK;
    private final @NonNull NbGradleProject watcher;
    @SuppressWarnings("MS_SHOULD_BE_FINAL")
    public static WatcherAccessor ACCESSOR = null;

    GradleProject project;

    static {
        // invokes static initializer of ModelHandle.class
        // that will assign value to the ACCESSOR field above
        Class<?> c = NbGradleProject.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException ex) {
            LOG.log(SEVERE, "very wrong, very wrong, yes indeed", ex);
        }
    }

    private final GradleFiles gradleFiles;

    public boolean isGradleProjectLoaded() {
        return project != null;
    }

    public static abstract class WatcherAccessor {

        public abstract NbGradleProject createWatcher(NbGradleProjectImpl proj);

        public abstract void doFireReload(NbGradleProject watcher);

        public abstract void activate(NbGradleProject watcher);

        public abstract void passivate(NbGradleProject watcher);
    }

    @java.lang.SuppressWarnings("LeakingThisInConstructor")
    public NbGradleProjectImpl(final FileObject projectDir, ProjectState projectState) {
        this.projectDir = projectDir;
        this.projectState = projectState;
        this.gradleFiles = new GradleFiles(FileUtil.normalizeFile(FileUtil.toFile(projectDir)), true);
        lookup = Lookups.proxy(new Lookup.Provider() {
            @Override
            public Lookup getLookup() {
                if (completeLookup == null) {
                    //not fully initialized constructor
                    LOG.log(Level.FINE, "Accessing project's lookup before the instance is fully initialized at " + gradleFiles.getBuildScript(), new Exception());
                    assert basicLookup != null;
                    return basicLookup;
                } else {
                    return completeLookup;
                }
            }
        });
        watcher = ACCESSOR.createWatcher(this);
        GradleAuxiliaryConfigImpl aux = new GradleAuxiliaryConfigImpl(projectDir, true);
        basicLookup = createBasicLookup(projectState, aux);
        completeLookup = LookupProviderSupport.createCompositeLookup(basicLookup, new PluginDependentLookup(watcher));
    }

    public GradleFiles getGradleFiles() {
        return gradleFiles;
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    private Lookup createBasicLookup(ProjectState state, GradleAuxiliaryConfigImpl aux) {
        return Lookups.fixed(this,
                watcher,
                new CacheDirProvider(),
                aux,
                aux.getProblemProvider(),
                new GradleAuxiliaryPropertiesImpl(this),
                UILookupMergerSupport.createProjectOpenHookMerger(new ProjectOpenedHookImpl()),
                UILookupMergerSupport.createProjectProblemsProviderMerger(),
                UILookupMergerSupport.createRecommendedTemplatesMerger(),
                UILookupMergerSupport.createPrivilegedTemplatesMerger(),
                LookupProviderSupport.createSourcesMerger(),
                LookupProviderSupport.createSharabilityQueryMerger(),
                new GradleProjectLoaderImpl(this),
                new GradleProjectErrorNotifications(),
                state
        );
    }

    public GradleProject getGradleProject() {
        if (project == null) {
            project = loadProject();
        }
        return project;
    }

    public void fireProjectReload(boolean wait) {
        reloadTask.schedule(0);
        if (wait) {
            reloadTask.waitFinished();
        }
    }


    void attachAllUpdater() {
        synchronized (this) {
            if (openedProjectUpdater == null) {
                openedProjectUpdater = new Updater((new FileProvider() {

                    @Override
                    public Set<File> getFiles() {
                        GradleFiles gf = getGradleFiles();
                        Set<File> ret = new LinkedHashSet<>();
                        for (GradleFiles.Kind kind : GradleFiles.Kind.PROJECT_FILES) {
                            ret.add(gf.getFile(kind));
                        }
                        return ret;
                    }
                }));
            }
        }

        openedProjectUpdater.attachAll();
    }

    void detachAllUpdater() {
        synchronized (this) {
            if (openedProjectUpdater != null) {
                openedProjectUpdater.detachAll();
            }
        }
    }

    void dumpProject() {
        project = null;
    }

    public Quality getAimedQuality() {
        return aimedQuality;
    }

    public NbGradleProject getProjectWatcher() {
        return watcher;
    }

    public void setAimedQuality(Quality aim) {
        //TODO: Shall we do some locking here?
        if ((aimedQuality == FALLBACK) && aim.betterThan(FALLBACK)) {
            ACCESSOR.activate(watcher);
        }
        if ((aim == FALLBACK) && aimedQuality.betterThan(FALLBACK)) {
            ACCESSOR.passivate(watcher);
        }
        this.aimedQuality = aim;
        if ((project == null) || project.getQuality().worseThan(aim)) {
            project = loadProject();
            ACCESSOR.doFireReload(watcher);
        }
    }

    private GradleProject loadProject() {
        return loadProject(null, false, aimedQuality);
    }

    private GradleProject loadProject(String desc, boolean ignoreCache, Quality aim, String... args) {
        GradleProjectLoader loader = getLookup().lookup(GradleProjectLoader.class);
        GradleProject prj = loader != null ? loader.loadProject(aim, desc, ignoreCache,  false, args) : null;
        return prj;
    }
    
    RequestProcessor.Task reloadProject(final boolean ignoreCache, final Quality aim, final String... args) {
        return reloadProject(null,  ignoreCache, aim, args);
    }
    
    RequestProcessor.Task reloadProject(String desc, final boolean ignoreCache, final Quality aim, final String... args) {
        return RELOAD_RP.post(() -> {
            project = loadProject(desc, ignoreCache, aim, args);
            ACCESSOR.doFireReload(watcher);
        });
    }

    @Override
    public int hashCode() {
        return gradleFiles.hashCode() * 3;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Project) {
            NbGradleProjectImpl impl = ((Project) obj).getLookup().lookup(NbGradleProjectImpl.class);
            if (impl != null) {
                return getGradleFiles().equals(impl.getGradleFiles());
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if (isGradleProjectLoaded()) {
            return "Gradle: " + project.getBaseProject().getName() + "[" + project.getQuality() + "]";
        } else {
            return "Unloaded Gradle Project: " + gradleFiles.toString();
        }
    }
    
    final RequestProcessor GRADLE_PRIMING_RP = new RequestProcessor("gradle-project-resolver", 1); //NOI18N

    // @GuardedBy(this)
    private CompletableFuture<GradleProject>    primingBuild;

    boolean isProjectPrimingRequired() {
        GradleProject gp = getGradleProject();
        return gp.getQuality().notBetterThan(EVALUATED) || !gp.getProblems().isEmpty();
    }
    
    /**
     * The core implementation is tied to project quality itself, so it is extracted here from
     * {@link GradleProjectProblemProvider}. 
     * <p>
     * <b>Note: Priming build makes the project trusted</b>
     * 
     * @return future that produces the result.
     */
    @NbBundle.Messages({
        "# {0} - project name",
        "ACT_PrimingProject=Preparing project {0}"
    })
    CompletableFuture<GradleProject> primeProject() {
        CompletableFuture<GradleProject> ret;
        synchronized (this) {
            if (primingBuild != null && !primingBuild.isDone()) {
                // avoid priming twice, piggyback on the old one
                LOG.log(Level.FINER, "Priming build runs for {0}: {1}", new Object[] { this, primingBuild });
                return primingBuild;
            }
            ret = new CompletableFuture<>();
            primingBuild = ret;
        }
        LOG.log(Level.FINER, "Submitting priming build runs for {0}: {1}", new Object[] { this, ret });
        GRADLE_PRIMING_RP.submit(() -> {
            try {
                // this was explicitly invoked as project action, or problem resolution. Same level as
                // Build project, so trust the project.
                ProjectTrust.getDefault().trustProject(this, true);
                GradleProjectLoader loader = getLookup().lookup(GradleProjectLoader.class);
                GradleProject gradleProject = loader.loadProject(FULL_ONLINE, Bundle.ACT_PrimingProject(project.getBaseProject().getName()), true, true);
                LOG.log(Level.FINER, "Priming finished, reloading {0}: {1}", project);
                fireProjectReload(false);
                ret.complete(gradleProject);
            } catch (Throwable t) {
                LOG.log(Level.FINER, t, () -> String.format("Priming errored for %s", project));
                ret.completeExceptionally(t);
                if (t instanceof ThreadDeath) {
                    throw t;
                }
            }
        });
        return ret;
    }
    
    public static File getCacheDir(GradleFiles gf) {
        return getCacheDir(gf.getRootDir(), gf.getProjectDir());
    }

    public static File getCacheDir(GradleProject gp) {
        GradleBaseProject base = gp.getBaseProject();
        return getCacheDir(base.getRootDir(), base.getProjectDir());
    }

    private static File getCacheDir(File rootDir, File projectDir) {
        int code = Math.abs(projectDir.getAbsolutePath().hashCode());
        String dirName = projectDir.getName() + "-" + code; //NOI18N
        File dir = new File(rootDir, ".gradle/nb-cache/" + dirName); //NOI18N
        return dir;
    }

    private class ProjectOpenedHookImpl extends ProjectOpenedHook {

        @Override
        protected void projectOpened() {
            Runnable open = () -> {
                setAimedQuality(FULL);
                attachAllUpdater();
                if (ProjectProblems.isBroken(NbGradleProjectImpl.this)) {
                    ProjectProblems.showAlert(NbGradleProjectImpl.this);
                }
            };
            if (GradleExperimentalSettings.getDefault().isOpenLazy()) {
                RELOAD_RP.post(open, 100);
            } else {
                open.run();
            }
        }

        @Override
        protected void projectClosed() {
            setAimedQuality(Quality.FALLBACK);
            detachAllUpdater();
            dumpProject();
            getLookup().lookup(ProjectConnection.class).close();
            getLookup().lookup(GradleProjectErrorNotifications.class).clear();
        }
    }

    interface FileProvider {

        Set<File> getFiles();
    }

    private class CacheDirProvider implements CacheDirectoryProvider {

        @Override
        public FileObject getCacheDirectory() throws IOException {
            return FileUtil.createFolder(getCacheDir(gradleFiles));
        }
    }

    private static class PluginDependentLookup extends ProxyLookup implements PropertyChangeListener {

        private static final String NB_GENERAL = "<nb-general>"; //NOI18N
        private static final String NB_ROOT_PLUGIN = "root"; //NOI18N
        private final WeakReference<NbGradleProject> watcherRef;
        private final Map<String, Lookup> pluginLookups = new HashMap<>();

        @java.lang.SuppressWarnings("LeakingThisInConstructor")
        public PluginDependentLookup(NbGradleProject watcher) {
            watcherRef = new WeakReference<>(watcher);
            Lookup general = Lookups.forPath("Projects/" + NbGradleProject.GRADLE_PROJECT_TYPE + "/Lookup"); //NOI18N
            pluginLookups.put(NB_GENERAL, general); //NOI18N
            setLookups(general);
            watcher.addPropertyChangeListener(WeakListeners.propertyChange(this, watcher));
        }

        private void check() {
            boolean lookupsChanged = false;
            NbGradleProject watcher = watcherRef.get();
            if (watcher != null) {
                lookupsChanged = !watcher.isGradleProjectLoaded();
                if (watcher.isGradleProjectLoaded()) {
                    GradleBaseProject prj = watcher.projectLookup(GradleBaseProject.class);
                    Set<String> currentPlugins = new HashSet<>(prj.getPlugins());
                    if (prj.isRoot()) {
                        currentPlugins.add(NB_ROOT_PLUGIN);
                    }
                    for (String cp : currentPlugins) {
                        //Add Lookups for new plugins
                        if (!pluginLookups.containsKey(cp)) {
                            Lookup pluginLookup = Lookups.forPath("Projects/" + NbGradleProject.GRADLE_PLUGIN_TYPE + "/" + cp + "/Lookup"); //NOI18N
                            pluginLookups.put(cp, pluginLookup);
                            lookupsChanged = true;
                        }
                    }
                    Iterator<String> it = pluginLookups.keySet().iterator();
                    while (it.hasNext()) {
                        String oldPlugin = it.next();
                        if (!currentPlugins.contains(oldPlugin) && !NB_GENERAL.equals(oldPlugin)) {
                            it.remove();
                            lookupsChanged = true;
                        }
                    }
                }
            }
            if (lookupsChanged) {
                setLookups(pluginLookups.values().toArray(new Lookup[pluginLookups.size()]));
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                check();
            }
        }

    }

    private class Updater implements FileChangeListener {

        final FileProvider fileProvider;
        Set<File> filesToWatch;
        long lastEventTime = 0;

        Updater(FileProvider fp) {
            fileProvider = fp;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            if (lastEventTime < fe.getTime()) {
                lastEventTime = System.currentTimeMillis();
                fireProjectReload(false);
            }
        }

        @Override
        public void fileChanged(FileEvent fe) {
            if (lastEventTime < fe.getTime()) {
                lastEventTime = System.currentTimeMillis();
                fireProjectReload(false);
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            lastEventTime = System.currentTimeMillis();
            fireProjectReload(false);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        synchronized void attachAll() {
            filesToWatch = fileProvider.getFiles();
            if (filesToWatch != null) {
                for (File f : filesToWatch) {
                    if (f != null) {
                        try {
                            FileUtil.addFileChangeListener(this, f);
                        } catch (IllegalArgumentException ex) {
                            assert false : "Project opened twice in a row";
                        }
                    }
                }
            }
        }

        synchronized void detachAll() {
            if (filesToWatch != null) {
                for (File f : filesToWatch) {
                    if (f != null) {
                        try {
                            FileUtil.removeFileChangeListener(this, f);
                        } catch (IllegalArgumentException ex) {
                            assert false : "Project closed twice in a row";
                        }
                    }
                }
            }
        }
    }

}
