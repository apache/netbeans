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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Parent;
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
 * MavenProject loaded by the base implementation contains placeholder artifacts.
 * Placeholder artifacts are created by org.netbeans.modules.maven.NbArtifactFixer for POM artifacts that
 * are not present in the local repository, i.e. library POMs, that would otherwise fail the model building process,
 * if the resolver reported them as missing. They are just empty placeholders that satisfy Maven implementation, but
 * naturally do not contain any further dependency info, so the complete project model is not complete.
 * 
 * If RESOLVED is requested, the implementation runs a priming build and then restarts the reload
 * operation, so that base maven impl will re-read the project.
 * To avoid reloading loops, the placeholder artifacts are stored between reloads. If the reported placeholder artifacts
 * are all already known - that is,they were not resolved by the priming build, the implementation ends
 * with an error, the project is not correctable.
 * @author sdedic
 */
@ProjectServiceProvider(service = ProjectReloadImplementation.class, projectTypes = {
    @LookupProvider.Registration.ProjectType(id = NbMavenProject.TYPE, position = 11000)
    
})
public class MavenPrimingReloadImplementation implements ProjectReloadImplementation {
    private static final Logger LOG = Logger.getLogger(MavenPrimingReloadImplementation.class.getName());
    
    /**
     * Names of artifacts that were injected by NbArtifactFixer. It's not productive to run a priming build to fix this known set of artifacts,
     * unless "force" is in effect - they are unlikely to be retrieved, since they already failed.
     */
    // @GuardedBy(this)
    private Set<String> placeholderArtifactNames = new HashSet<>();

    
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

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + Objects.hashCode(this.p);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ModelHolder other = (ModelHolder) obj;
            return Objects.equals(this.p, other.p);
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
        if (!MavenProjectCache.getPlaceholderArtifacts(mp).isEmpty()) {
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
                timestamp(ProjectStateData.TIME_RECHECK_ON_CHANGE);
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
               MavenProjectCache.getPlaceholderArtifacts(p).isEmpty();
    }

    static String artifactGav(Artifact a) {
        if (a == null) {
            return null;
        }
        // let's ignore version; no version may be specified in the raw model.
        String gav = String.format("%s:%s", a.getGroupId(), a.getArtifactId());
        if (a.getClassifier() != null && !a.getClassifier().isEmpty()) {
            return gav + ":" + a.getClassifier();
        } else {
            return gav;
        }
    }
    
    private static String artifactList(Collection<String> gavs) {
        List<String> sorted = new ArrayList<>(gavs);
        Collections.sort(sorted);
        
        String s = String.join(", ", sorted.subList(0, Math.min(sorted.size(), 5)));
        return sorted.size() > 5 ?  s + ", ..." : s;
    }
    
    private CompletableFuture<ProjectStateData> reportMissingArtifacts(Project project, ProjectReload.StateRequest request, MavenProject p, Collection<String> gavs) {
        String msg;
        if (placeholderArtifactNames.containsAll(gavs)) {
            msg = Bundle.ERR_IncompleteProjectRepeated(project, artifactList(gavs));
        } else {
            msg = Bundle.ERR_IncompleteProjectDownload(ProjectUtils.getInformation(project).getDisplayName(), artifactList(gavs));
        }
        PartialLoadException ex = new PartialLoadException(createStateData(p), msg, 
            new ProjectOperationException(project, request.isOfflineOperation() ? ProjectOperationException.State.OFFLINE : ProjectOperationException.State.BROKEN, msg)
        );
        CompletableFuture<ProjectStateData> future = new CompletableFuture<>();
        future.completeExceptionally(ex);
        return future;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "ERR_PrimingBuildFailed=Priming build of {0} failed.",
        "# {0} - project name",
        "# {1} - artifact names, up to 5",
        "ERR_PrimingBuildInsufficient=Priming of {0} failed, artifact(s) {1} could not be still resolved.",
        "# {0} - project name",
        "# {1} - artifact names, up to 5",
        "ERR_IncompleteProject=Project {0} is missing artifacts: {1}.",
        "# {0} - project name",
        "# {1} - artifact names, up to 5",
        "ERR_IncompleteProjectDownload=Project {0} needs to download artifacts: {1}.",
        "# {0} - project name",
        "# {1} - artifact names, up to 5",
        "ERR_IncompleteProjectRepeated=Project {0} was not able to resolve or acquire artifacts: {1}.",
    })
    @Override
    public CompletableFuture reload(Project project, ProjectReload.StateRequest request, LoadContext context) {
        MavenProject p = context.stateLookup().lookup(MavenProject.class);
        
        LC lc = (LC)context.ensureLoadContext(LC.class, LC::new);
        
        boolean ok = checkForMissingArtifacts(p);
        if (ok) {
            synchronized (this) {
                placeholderArtifactNames.clear();
            }
            LOG.log(Level.FINE, "Project {0}: no missing artifacts", project);
            return CompletableFuture.completedFuture(null);
        } 
        
        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        Collection<Artifact> placeholders = MavenProjectCache.getPlaceholderArtifacts(p);
        Set<String> gavs = new HashSet<>();
        placeholders.forEach(a -> { if (a != null) { gavs.add(artifactGav(a)); }});
        String parentGav = artifactGav(p.getParentArtifact());

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Project {0}: missing artifacts reported: {1}", new Object[] {
                project, gavs
            });
        }
        
        CompletableFuture<ProjectStateData> future = new CompletableFuture<>();
        // special reports for parent artifact:
        if (parentGav == null) {
            // this will execute in BROKEN project, with unresolvable parent,
            // but raw model can still contain the IDs.
            Parent pp = p.getModel().getParent();
            if (pp != null) {
                parentGav = pp.getGroupId() + ":" + pp.getArtifactId();
            }
        }
        if (gavs.contains(parentGav) || (parentGav != null && p.getParentArtifact() == null)) {
            if (request.getTargetQuality().isWorseThan(ProjectReload.Quality.RESOLVED) || request.isOfflineOperation()) {
                // covers both targetQuality > broken and minQuality > BROKEN, since "min" cannot be less than "target".
                String msg;

                if (placeholderArtifactNames.contains(parentGav)) {
                    msg = Bundle.ERR_IncompleteProjectRepeated(project, Collections.singletonList(parentGav));
                } else {
                    msg = request.isOfflineOperation() ?
                        Bundle.ERR_UnprimedInOfflineMode(ProjectUtils.getInformation(project).getDisplayName()):
                        Bundle.ERR_ParentPomMissing(ProjectUtils.getInformation(project).getDisplayName(), parentGav);
                }

                ProjectOperationException pex = new ProjectOperationException(project, 
                        request.isOfflineOperation() ? ProjectOperationException.State.OFFLINE : ProjectOperationException.State.BROKEN, msg);
                PartialLoadException ex = new PartialLoadException(createStateData(p),msg, pex);
                future.completeExceptionally(ex);
                return future;
            }
        }
        
        if (request.getTargetQuality().isWorseThan(ProjectReload.Quality.RESOLVED)) {
            future.complete(createStateData(p));
            return future;
        } 
        if (request.getMinQuality().isWorseThan(ProjectReload.Quality.RESOLVED) || request.isOfflineOperation()) {
            return reportMissingArtifacts(project, request, p, gavs);
        }
        
        synchronized (this) {
            if (!gavs.isEmpty() && placeholderArtifactNames.containsAll(gavs) && !request.isForceReload()) {
                // no point in running priming build again, when the artifacts are known to be broken.
                return reportMissingArtifacts(project, request, p, gavs);
            }
            placeholderArtifactNames = gavs;
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
