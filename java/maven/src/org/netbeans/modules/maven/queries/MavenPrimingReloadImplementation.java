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
package org.netbeans.modules.maven.queries;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.modelcache.MavenProjectCache;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.ProjectReload;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * This class steps in, if the requested quality is higher than LOAD and the 
 * MavenProject loaded by the base implementation contains fake artifacts.
 * If RESOLVED is requested, the implementation runs a priming build and then restarts the reload
 * operation, so that base maven impl will re-read the project.
 * To avoid reloading loops, the faked artifacts are stored between reloads. If the reported fake artifacts
 * are all already known - that is,they were not resolved by the priming build, the implementation ends
 * with an error, the project is not correctable.
 * @author sdedic
 */
@ProjectServiceProvider(service = ProjectReloadImplementation.class, projectTypes = {
    @LookupProvider.Registration.ProjectType(id = NbMavenProject.TYPE, position = 11000)
    
})
public class MavenPrimingReloadImplementation implements ProjectReloadImplementation {
    /**
     * Names of artifacts that were faked. It's not productive to run a priming build to fix this known set of artifacts,
     * unless "force" is in effect - they are unlikely to be retrieved, since they already failed.
     */
    // @GuardedBy(this)
    private Set<String> fakeArtifactNames = new HashSet<>();

    
    private Reference<ProjectStateData> lastData = new WeakReference<>(null);
    
    static class LC {
        boolean firstRun = true;
    }
    
    /**
     * Holds the MavenProject instance. MavenProject instance is already published
     * as data by base reload implementation.
     */
    private static class ModelHolder {
        final MavenProject p;

        public ModelHolder(MavenProject p) {
            this.p = p;
        }
    }

    private ProjectReload.Quality getProjectQuality(MavenProject mp) {
        if (MavenProjectCache.isFallbackproject(mp)) {
            // could not load at all.
            MavenProject fallback = MavenProjectCache.getPartialProject(mp);
            return fallback != null ? 
                    ProjectReload.Quality.BROKEN:
                    ProjectReload.Quality.NONE;
        }
        if (!MavenProjectCache.getFakedArtifacts(mp).isEmpty()) {
            return ProjectReload.Quality.LOADED;
        }
        return ProjectReload.Quality.RESOLVED;
    }
    
    
    private ProjectStateData createStateData(MavenProject mp) {
        synchronized (this) {
            ProjectStateData<ModelHolder> d2 = lastData.get();
            if (d2 != null) {
                ModelHolder h = d2.getProjectData();
                if (h != null && h.p == mp) {
                    return d2;
                }
            }
        }
        ProjectStateData d;
        
        ProjectStateBuilder builder = ProjectStateData.builder(getProjectQuality(mp)).
                timestamp(MavenProjectCache.getLoadTimestamp(mp));
        builder.data(new ModelHolder(mp));
        builder.attachLookup(Lookups.fixed(mp));
        d = builder.build();
        synchronized (this) {
            ProjectStateData d2 = lastData.get();
            if (d2 != null) {
                if (d2.getProjectData() == mp) {
                    return d2;
                }
                d2.fireChanged(true, false);
            }
            lastData = new WeakReference<>(d);
        }
        return d;
    }

    static boolean checkForMissingArtifacts(MavenProject p) {       
        return p == null || !MavenProjectCache.isIncompleteProject(p) ||
               MavenProjectCache.getFakedArtifacts(p).isEmpty();
    }

    static String artifactGav(Artifact a) {
        return String.format("%s:%s:%s:%s", a.getGroupId(), a.getArtifactId(), a.getVersion(), a.getClassifier());
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "ERR_PrimingBuildFailed=Priming build of {0} failed."
    })
    @Override
    public CompletableFuture reload(Project project, ProjectReload.StateRequest request, LoadContext context) {
        MavenProject p = context.stateLookup().lookup(MavenProject.class);
        
        LC lc = (LC)context.ensureLoadContext(LC.class, LC::new);
        
        boolean ok = checkForMissingArtifacts(p);
        if (ok) {
            synchronized (this) {
                fakeArtifactNames.clear();
            }
            context.saveLoadContext(this);
            return CompletableFuture.completedFuture(null);
        } 
        
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        if (!ap.isActionEnabled(ActionProvider.COMMAND_PRIME, Lookup.EMPTY)) {
            return CompletableFuture.completedFuture(null);
        }

        Collection<Artifact> faked = MavenProjectCache.getFakedArtifacts(p);
        Set<String> gavs = new HashSet<>();
        faked.forEach(a -> gavs.add(artifactGav(a)));
        String parentGav = artifactGav(p.getParentArtifact());
        
        CompletableFuture<ProjectStateData> future = new CompletableFuture<>();
        if (gavs.contains(parentGav)) {
            if (request.getMinQuality().isWorseThan(ProjectReload.Quality.BROKEN)) {
                future.complete(createStateData(p));
            } else if (request.isOfflineOperation()) {
                PartialLoadException ex = new PartialLoadException(createStateData(p),
                     Bundle.ERR_ParentPomMissing(ProjectUtils.getInformation(project).getDisplayName(), parentGav), 
                    new ProjectOperationException(project, ProjectOperationException.State.OFFLINE, 
                    Bundle.ERR_UnprimedInOfflineMode(ProjectUtils.getInformation(project).getDisplayName()))
                );
                future.completeExceptionally(ex);
                return future;
            }
        }
        if (request.getMinQuality().isWorseThan(ProjectReload.Quality.RESOLVED)) {
            future.complete(createStateData(p));
            return future;
        } else if (request.isOfflineOperation()) {
            PartialLoadException ex = new PartialLoadException(createStateData(p),
                 Bundle.ERR_ParentPomMissing(ProjectUtils.getInformation(project).getDisplayName(), parentGav), 
                new ProjectOperationException(project, ProjectOperationException.State.OFFLINE, 
                    Bundle.ERR_UnprimedInOfflineMode(ProjectUtils.getInformation(project).getDisplayName()))
            );
            future.completeExceptionally(ex);
            return future;
        }
        
        synchronized (this) {
            if (!lc.firstRun && fakeArtifactNames.containsAll(gavs) && !request.isForceReload()) {
                // no point in running priming build again, when the artifacts are known to be broken.
                future.complete(createStateData(p));
                return future;
            }
            fakeArtifactNames = gavs;
        }

        lc.firstRun = false;

        /**
         * Action looks for Consumer&lt;Cancellable> in the context lookup and gives it
         * its {@link Cancellable} capable to abort the priming build. The class will receive
         * the Cancellable during priming build initialization.
         */
        class CancelSignalDelegator implements Consumer<Cancellable>, Cancellable {
            private final AtomicReference<Cancellable> ref = new AtomicReference<>();
            
            @Override
            public void accept(Cancellable other) {
                ref.set(other);
            }
            @Override
            public boolean cancel() {
                Cancellable other = ref.get();
                return other != null && other.cancel();
            }
        }
        Cancellable csd = new CancelSignalDelegator();
        context.setCancellable(csd);
        
        // PENDING: report progress through progress API, use request.getReason().
        ActionProgress prg = new ActionProgress() {
            @Override
            protected void started() {
            }

            @Override
            public void finished(boolean success) {
                if (success) {
                    context.retryReload();
                    future.complete(null);
                } else if (context.isCancelled()) {
                    future.completeExceptionally(context.getCancelled());
                } else {
                    String n = ProjectUtils.getInformation(project).getDisplayName();
                    ProjectOperationException cause = new ProjectOperationException(project, ProjectOperationException.State.BROKEN, Bundle.ERR_PrimingBuildFailed(n));
                    ProjectStateData partialdata = createStateData(p);
                    // allow the project operation to complete, with degraded quality:
                    PartialLoadException ex = new PartialLoadException(partialdata, Bundle.ERR_PrimingBuildFailed(n), cause);
                    future.completeExceptionally(ex);
                }
            }
        };
        if (context.isCancelled()) {
            future.cancel(true);
            return future;
        }
        ap.invokeAction(ActionProvider.COMMAND_PRIME, Lookups.fixed(prg, csd));
        return future;
    }
}
