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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.netbeans.modules.gradle.api.GradleReport;
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

    static final Logger LOG = Logger.getLogger(NbGradleProjectImpl.class.getName());

    public static final RequestProcessor RELOAD_RP = new RequestProcessor("Gradle project reloading", 1); //NOI18
    private final RequestProcessor.Task reloadTask = RELOAD_RP.create(new Runnable() {
        @Override
        public void run() {
            loadOwnProject(null, false, false, aimedQuality);
        }
    });

    private final FileObject projectDir;
    private final ProjectState projectState;
    private final Lookup lookup;
    private final Lookup basicLookup;
    private final Lookup completeLookup;
    private Updater openedProjectUpdater;
    
    // @GuardedBy(this)
    private volatile Quality aimedQuality = FALLBACK;
    
    private final @NonNull NbGradleProject watcher;
    @SuppressWarnings("MS_SHOULD_BE_FINAL")
    public static WatcherAccessor ACCESSOR = null;

    // @GuardedBy(this)
    private volatile GradleProject project;
    // @GuardedBy(this)
    private Quality attemptedQuality;

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

    public abstract static class WatcherAccessor {

        public abstract NbGradleProject createWatcher(NbGradleProjectImpl proj);

        public abstract void doFireReload(NbGradleProject watcher);

        public abstract void activate(NbGradleProject watcher);

        public abstract void passivate(NbGradleProject watcher);

        public abstract GradleReport createReport(GradleReport.Severity severity, String errorClass, String location, int line, String message, 
                GradleReport causedBy, String[] traceLines);

        public abstract void setProblems(GradleBaseProject baseProject, Set<GradleReport> problems);
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
        return projectWithQuality(null, EVALUATED, false, false);
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
                            File f = gf.getFile(kind);
                            if (f != null) {
                                ret.add(f);
                            }
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

    synchronized void dumpProject() {
        loading = null;
        project = null;
        attemptedQuality = null;
        loadedProjectSerial = 0;
        aimedQuality = FALLBACK;
    }

    public Quality getAimedQuality() {
        return aimedQuality;
    }

    public NbGradleProject getProjectWatcher() {
        return watcher;
    }
    
    /**
     * Obtains a project attempting at least the defined quality, without setting
     * that quality level for subsequent loads. Same as {@link #projectWithQualityTask}
     * but synchronous (runs in this thread).
     * @param desc optional description for the loading process, can be {@code null}.
     * @param aim aimed quality
     * @param interactive true, if user messages/confirmations can be displayed
     * @param force to force load even though the quality does not change.
     * @return project instance
     */
    public GradleProject projectWithQuality(String desc, Quality aim, boolean interactive, boolean force) {
       synchronized (this) {
            GradleProject c = project;
            if (c != null) {
                if (! force && c.getQuality().atLeast(aim)) {
                    LOG.log(Level.FINER, "Asked for {0}, got {1} already: ", new Object[] { aim, c.getQuality() });
                    return c;
                }
                if (!force && attemptedQuality.atLeast(aim)) {
                    LOG.log(Level.FINER, "Attempted quality was {0}, ignoring request to get {1}", new Object[] { attemptedQuality, aim });
                    return c;
                }
            }
        }
        try {
            return loadOwnProject0(desc, false, interactive, aim, true, force).get();
        } catch (InterruptedException | ExecutionException ex) {
            // should not happen, the event dispatch + potential issues happen
            // synchronously
            return null;
        }
    }
    
    /**
     * Obtains a project attempting at least the defined quality, without setting
     * that quality level for subsequent loads. Note that the returned project's quality
     * must be checked. If the currently loaded project declares the desired quality,
     * no load is performed.
     * <p>
     * This method should be used in preference to {@link #loadProject()} or {@link #loadOWnProject},
     * unless it's desired to force refresh the project contents to the current disk state.
     * <div class="nonnormative">
     * Implementation note: project reload events are dispatched <b>synchronously</b>
     * in the calling thread.
     * </div>
     * @param desc optional description for the loading process, can be {@code null}.
     * @param aim aimed quality
     * @param interactive true, if user messages/confirmations can be displayed
     * @param force to force load even though the quality does not change.
     * @return project instance
     */
    public CompletableFuture<GradleProject> projectWithQualityTask(String desc, Quality aim, boolean interactive, boolean force) {
        synchronized (this) {
            GradleProject c = project;
            if (c != null) {
                if (!force && c.getQuality().atLeast(aim)) {
                    return CompletableFuture.completedFuture(c);
                }
                if (!force && attemptedQuality.atLeast(aim)) {
                    return CompletableFuture.completedFuture(c);
                }
            }
        }
        CompletableFuture<GradleProject> toRet = new CompletableFuture<>();
        RELOAD_RP.post(() -> 
            loadOwnProject0(desc, false, interactive, aim, false, force)
                .handle((p, e) -> {
                   if (e == null) {
                       toRet.complete(p);
                   } else {
                       toRet.completeExceptionally(e);
                   }
                   return null;
                })
        );
        return toRet;
    }

    /**
     * Changes the aimed project's quality. Reloads the project, if the
     * current quality is lower. If the aimed quality is better than {@link #FALLBACK}
     * build script files are monitored and the project is eventually reloaded when a
     * change is detected.
     * <div class="nonnormative">
     * Implementation note: project reload events are dispatched <b>synchronously</b>
     * in the calling thread.
     * </div>
     * @param aim the aimed quality.
     */
    public void setAimedQuality(Quality aim) {
        // Locked so that watcher is active/inactive always in sync with aimedQuality
        // FIXME: in the case the project _actually_ loads with a LOWER quality,
        // the Watcher is still active.
        synchronized (this) {
            if ((aimedQuality == FALLBACK) && aim.betterThan(FALLBACK)) {
                ACCESSOR.activate(watcher);
            }
            if ((aim == FALLBACK) && aimedQuality.betterThan(FALLBACK)) {
                ACCESSOR.passivate(watcher);
            }
            this.aimedQuality = aim;
            if (!((project == null) || project.getQuality().worseThan(aim))) {
                return;
            }
        }
        loadOwnProject0(null, false, false, aimedQuality, true, false);
    }

    /**
     * Increasing stamp of load attempts.
     */
    private final AtomicInteger currentSerial = new AtomicInteger();
    
    /**
     * Stamp of the currently loaded project.
     */
    // @GuardedBy(this)
    private int loadedProjectSerial;
    
    CompletableFuture<GradleProject> loadOwnProject(String desc, boolean ignoreCache, boolean interactive, Quality aim, String... args) {
        return loadOwnProject0(desc, ignoreCache, interactive, aim, false, true, args);
    }

    /**
     * Future that is present during project load. Other load requests can be satisfied by this Future if they do not contain
     * the 'force' flag.
     */
    // @GuardedBy(this)
    private LoadingCF loading;
    
    private static class LoadingCF extends CompletableFuture<GradleProject> {
        private final Quality aim;
        private final boolean ignoreCache;
        private final boolean interactive;
        private final boolean sync;
        private final List<String> args;
        private ThreadLocal<GradleProject> ownThreadCompletion = new ThreadLocal<>();

        public LoadingCF(Quality aim, boolean ignoreCache, boolean interactive, boolean sync, List<String> args) {
            this.aim = aim;
            this.ignoreCache = ignoreCache;
            this.interactive = interactive;
            this.sync = sync;
            this.args = args;
        }
     
        public boolean satisifes(LoadingCF other) {
            if (aim.worseThan(other.aim)) {
                return false;
            }
            if (ignoreCache != other.ignoreCache || interactive != other.interactive || sync != other.sync) {
                return false;
            }
            return args.equals(other.args);
        }

        @Override
        public GradleProject getNow(GradleProject valueIfAbsent) {
            GradleProject p = ownThreadCompletion.get();
            return p != null ? p : super.getNow(valueIfAbsent);
        }

        @Override
        public GradleProject join() {
            GradleProject p = ownThreadCompletion.get();
            return p != null ? p : super.join(); 
        }

        @Override
        public GradleProject get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            GradleProject p = ownThreadCompletion.get();
            return p != null ? p : super.get(timeout, unit);
        }

        @Override
        public GradleProject get() throws InterruptedException, ExecutionException {
            GradleProject p = ownThreadCompletion.get();
            return p != null ? p : super.get();
        }
    }
    
    /**
     * Loads a project. After load, dispatches reload events. If "sync" is false (= asynchronous), dispatches events
     * and does possible fixups in {@link #RELOAD_RP}. The returned future completes only after all the event
     * listeners in RELOAD_RP complete.
     * <p/>
     * If "sync" is true, everything happens synchronously and the returned Future is always completed. Errors from the
     * project load and reload listeners are thrown from this method.
     * 
     * @param desc description of the reload, can be {@code null}.
     * @param ignoreCache true to ignore cached data
     * @param interactive true, if displaying UI is permitted (i.e. security questions)
     * @param aim aimed quality
     * @param sync true to run everything synchronously
     * @param args optional arguments for the reload
     * @return Future for the new GradleProject state. See notes about sync/async differences.
     */
    /* nonprivate: tests only */CompletableFuture<GradleProject> loadOwnProject0(String desc, boolean ignoreCache, boolean interactive, Quality aim, boolean sync, boolean force, String... args) {
        GradleProjectLoader loader = getLookup().lookup(GradleProjectLoader.class);
        if (loader == null) {
            throw new IllegalStateException("No loader implementation is present!");
        }
        LoadingCF f = new LoadingCF(aim, ignoreCache, interactive, sync, Arrays.asList(args));
        synchronized (this) {
            if (this.loading != null && this.loading.satisifes(f)) {
                if (!force) {
                    LOG.log(Level.FINER, "Project {2} is already loading to quality {0}, now attempted {1}, returning existing handle", new 
                            Object[] { this.loading.aim, aim, this });
                    return loading;
                }
            }
            this.loading = f;
        }
        int s = currentSerial.incrementAndGet();
        // do not block during project load.
        LOG.log(Level.FINER, "Starting project {2} load, serial {0}, attempted quality {1}", new Object[] { s, aim, this });
        GradleProject prj = loader.loadProject(aim, desc, ignoreCache, interactive, args);
        synchronized (this) {
            if (loadedProjectSerial > s && project != null) {
                // the load started LATER than this one: return that project, and do not replace anything as this.project is newer
                LOG.log(Level.FINER, "Future finished project load, returing {0} throwing away {1}", new Object[] { project, prj });
                return CompletableFuture.completedFuture(this.project);
            }
            loadedProjectSerial = s;
            this.attemptedQuality = aim;
            
            boolean replace = project == null || force;
            if (project != null) {
                if (prj.getQuality().betterThan(project.getQuality())) {
                    replace = true;
                } else if (
                        project.getQuality().equals(prj.getQuality()) && 
                        !project.getProblems().equals(prj.getProblems()) &&
                        !prj.getProblems().isEmpty()) {
                    // exception: if the new project is the same quality fallback, but contains (different) problem info, use it
                    replace = true;
                }
            }
            if (!replace) {
                // avoid replacing a project when nothing has changed.
                LOG.log(Level.FINER, "Current project {1} sufficient for attempted quality {0}", new Object[] { this.project, aim });
                return CompletableFuture.completedFuture(this.project);
            }
            LOG.log(Level.FINER, "Replacing {0} with {1}, attempted quality {2}", new Object[] { this.project, prj, attemptedQuality });
            this.project = prj;
        }
        // notify the project has been changed.
        if (sync || RELOAD_RP.isRequestProcessorThread()) {
            synchronized (this) {
                if (this.loading == f) {
                    this.loading = null;
                }
            }
            LOG.log(Level.FINER, "Firing changes/reload synchronously");
            try {
                f.ownThreadCompletion.set(prj);
                ACCESSOR.doFireReload(watcher);
            } finally {
                f.ownThreadCompletion.remove();
                f.complete(prj);
            }
            return f;
        } else {
            LOG.log(Level.FINER, "Firing changes/reload in RP");
            RELOAD_RP.post(() -> callAccessorReload(f, prj));
            return f;
        }
    }
    
    private CompletableFuture<GradleProject> callAccessorReload(LoadingCF f, GradleProject prj) {
        try {
            synchronized (this) {
                if (this.loading == f) {
                    this.loading = null;
                }
            }
            try {
                f.ownThreadCompletion.set(prj);
                ACCESSOR.doFireReload(watcher);
            } finally {
                f.ownThreadCompletion.remove();
                f.complete(prj);
            }
        } catch (ThreadDeath t) {
            throw t;
        } catch (RuntimeException | Error ex) {
            f.completeExceptionally(ex);
            throw ex;
        } catch (Throwable t) {
            f.completeExceptionally(t);
            LOG.log(Level.WARNING, "Unexpected exception from project listeners", t);
        }
        return f;
    }
    
    /**
     * Forces project reload with the given quality, ignoring caches. The 'aim' quality does not become the {@link #getAimedQuality() aimed one}, just
     * forces appropriate load scope. 
     * @param reloadReason optional reason for the reload operation
     * @param interactive true, if the originating action is interactive and UI can be displayed
     * @param aim the aimed quality
     * @param args optional argument for reload
     * @return Task representing the reloading process
     */
    RequestProcessor.Task forceReloadProject(String reloadReason, boolean interactive, final Quality aim, final String... args) {
        return reloadProject(reloadReason, true, interactive, aim, args);
    }
    
    private RequestProcessor.Task reloadProject(String desc, final boolean ignoreCache, final boolean interactive, final Quality aim, final String... args) {
        return RELOAD_RP.post(() -> loadOwnProject(desc, ignoreCache, interactive, aim, args));
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
        // synchronized was here, but is it may be called during Logger.log(), it may completely cause a deadlock
        // between LogHandler (that calls this toString() and other thread that locked this and tries to use Logger).
        GradleProject p = project;
        if (p != null) {
            return "Gradle: " + p.getBaseProject().getName() + "[" + p.getQuality() + "]";
        } else {
            return "Unloaded Gradle Project: " + gradleFiles.toString();
        }
    }
    
    final RequestProcessor GRADLE_PRIMING_RP = new RequestProcessor("gradle-project-resolver", 1); //NOI18N

    // @GuardedBy(this)
    private CompletableFuture<GradleProject>    primingBuild;
    
    boolean isProjectPrimingRequired() {
        return getPrimedProject() == null;
    }

    GradleProject getPrimedProject() {
        GradleProject gp = projectWithQuality(null, EVALUATED, false, false);
        return gp.getQuality().betterThan(EVALUATED) ? gp : null;
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
            GradleProject gradleProject = null;
            try {
                // this was explicitly invoked as project action, or problem resolution. Same level as
                // Build project, so trust the project.
                ProjectTrust.getDefault().trustProject(this, true);
                gradleProject = getPrimedProject();
                if (gradleProject != null) {
                    ret.complete(gradleProject);
                    return;
                }
                // get at least something to extract project name from:
                GradleProject fallback = projectWithQuality(null, FALLBACK, false, false);
                loadOwnProject0(Bundle.ACT_PrimingProject(fallback.getBaseProject().getName()), true, true, FULL_ONLINE, false, true).
                        // wait until after reload event is fired.
                        thenApply(p -> ret.complete(p)).
                        exceptionally((e) -> ret.completeExceptionally(e));
                LOG.log(Level.FINER, "Priming finished, reloaded {0}: {1}", gradleProject);
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
        private static final String NB_ROOT_PLUGIN = "root"; //NOI18N
        private final WeakReference<NbGradleProject> watcherRef;
        
        // @GuardedBy(this)
        private Map<String, Lookup> pluginLookups = Collections.emptyMap();
        
        // @GuardedBy(this)
        private List<String> pluginOrder = Collections.emptyList();

        @java.lang.SuppressWarnings("LeakingThisInConstructor")
        public PluginDependentLookup(NbGradleProject watcher) {
            // PENDING: is this ref really necessary ? If we added a strong PropertyChangeListener
            // to the `watcher', it would keep this Lookup alive as long as the watcher itself is alive
            watcherRef = new WeakReference<>(watcher);
            check();
            watcher.addPropertyChangeListener(WeakListeners.propertyChange(this, watcher));
        }
        
        /**
         * Path for the default Gradle project lookup contents
         */
        private static final String GRADLE_DEFAULT_LOOKUP = "Projects/" + NbGradleProject.GRADLE_PROJECT_TYPE + "/Lookup";
        
        /**
         * Path for the default Gradle project lookup contents
         */
        private static final String GRADLE_ANY_PLUGIN_LOOKUP = "Projects/" + NbGradleProject.GRADLE_PLUGIN_TYPE + "/_any/Lookup";
        
        /**
         * Root for plugin lookup registrations. Individual Plugins must register in "&lt;GRADLE_PLUGINS_ROOT>/&lt;plugin-id>/Lookup".
         */
        private static final String GRADLE_PLUGINS_ROOT = "Projects/" + NbGradleProject.GRADLE_PLUGIN_TYPE;
        
        private void check() {
            NbGradleProject watcher = watcherRef.get();
            if (watcher == null) {
                // shortcut
                return;
            }
            List<String> orderedPaths = new ArrayList<>();
            
            orderedPaths.add(GRADLE_DEFAULT_LOOKUP);
            if (watcher.isGradleProjectLoaded()) {
                GradleBaseProject prj = watcher.projectLookup(GradleBaseProject.class);
                // plugins are unordered initially
                Set<String> currentPlugins = new HashSet<>(prj.getPlugins());
                if (prj.isRoot()) {
                    currentPlugins.add(NB_ROOT_PLUGIN);
                }

                FileObject pluginRoot = FileUtil.getConfigFile(GRADLE_PLUGINS_ROOT);
                if (pluginRoot != null) {
                    // iterate in the file-system order to get at least SOME defined default order (according to module dependencies)
                    for (FileObject pl : pluginRoot.getChildren()) {
                        if (currentPlugins.remove(pl.getName())) {
                            orderedPaths.add(GRADLE_PLUGINS_ROOT + "/" + pl.getName() + "/Lookup");
                        }
                    }
                }
                // order the rest of plugins alphabetically
                List<String> remaining = new ArrayList<>(currentPlugins);
                Collections.sort(remaining);
                remaining.forEach(r -> orderedPaths.add(GRADLE_PLUGINS_ROOT + "/" + r + "/Lookup"));
            }
            orderedPaths.add(GRADLE_ANY_PLUGIN_LOOKUP);

            Map<String, Lookup> newLookups;
            Map<String, Lookup> prevLookups;

            synchronized (this) {
                if (this.pluginOrder.equals(orderedPaths)) {
                    return;
                }
                prevLookups = this.pluginLookups;
            }
            newLookups = new HashMap<>(prevLookups);
            newLookups.keySet().retainAll(orderedPaths);

            Lookup[] lkps = new Lookup[orderedPaths.size()];
            int i = 0;
            for (String s : orderedPaths) {
                Lookup l = newLookups.get(s);
                if (l == null) {
                    newLookups.put(s, l = Lookups.forPath(s));
                }
                lkps[i++] = l;
            }
            synchronized (this) {
                // double check: if a parallel execution took the pluginLookups (= later than us) and finished
                // before -> more recent data.
                if (pluginLookups != prevLookups) {
                    return;
                }
                pluginLookups = newLookups;
                pluginOrder = orderedPaths;
            }
            setLookups(lkps);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                // PENDING: maybe the Lookup change should synchronize into RELOAD_RP
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
