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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.modelcache.MavenProjectCache;
import org.netbeans.modules.project.dependency.ProjectReload;
import org.netbeans.modules.project.dependency.ProjectReload.Quality;
import org.netbeans.modules.project.dependency.ProjectReload.StateRequest;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 * Basic maven implementation. It uses Maven internal API to load or force-reload the project to
 * get MavenProject instance. Hooks to PROP_PROJECT change event and invalidates last-known state.
 * @author sdedic
 */
@ProjectServiceProvider(service = ProjectReloadImplementation.class, projectTypes = {
    @LookupProvider.Registration.ProjectType(id = NbMavenProject.TYPE, position = 10000)
    
})
public class MavenReloadImplementation implements ProjectReloadImplementation, PropertyChangeListener {
    private final Project project;
    private volatile Reference<ProjectStateData> lastData = new WeakReference<>(null);

    
    public MavenReloadImplementation(Project project) {
        this.project = project;
        NbMavenProject.addPropertyChangeListener(project, 
                WeakListeners.propertyChange(this, project)
        );
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null) {
            return;
        }
        ProjectStateData st = lastData.get();
        if (st == null) {
            return;
        }
        if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
            st.fireChanged(true, false);
        }
    }
    
    /**
     * Returns project metadata files. If 'forReload' is false, it just returns project's
     * pom.xml file. If the 'forReload' is true, it will return all the settings and 
     * referenced parent project's files.
     * 
     * @param forReload
     * @return 
     */
    public Set<FileObject> findProjectFiles() {
        NbMavenProject p = project.getLookup().lookup(NbMavenProject.class);
        return findProjectFiles0(p.getMavenProject());
    }

    Set<FileObject> findProjectFiles0(MavenProject mp) {
        File pomFile = mp.getFile();
        Set<FileObject> fileSet = new HashSet<>();
        fileSet.add(FileUtil.toFileObject(pomFile));
        
        MavenExecutionRequest rq = EmbedderFactory.getProjectEmbedder().createMavenExecutionRequest();
        File userSettings = rq.getUserSettingsFile();
        File toolchains = rq.getGlobalToolchainsFile();
        File globalSettings = rq.getGlobalSettingsFile();
        if (userSettings != null && userSettings.exists()) {
            fileSet.add(FileUtil.toFileObject(userSettings));
        }
        if (toolchains != null && toolchains.exists()) {
            fileSet.add(FileUtil.toFileObject(toolchains));
        }
        if (globalSettings != null && globalSettings.exists()) {
            fileSet.add(FileUtil.toFileObject(globalSettings));
        }
        
        Set<Artifact> processed = new HashSet<>();
        ArrayDeque<Artifact> toProcess = new ArrayDeque<>();
        Artifact a = mp.getParentArtifact();
        if (a != null) {
            toProcess.add(a);
        }
        toProcess.addAll(mp.getArtifacts());
        
        while ((a = toProcess.poll()) != null) {
            if (!processed.add(a)) {
                continue;
            }
            File pom = MavenFileOwnerQueryImpl.getInstance().getOwnerPOM(a.getGroupId(), a.getArtifactId(), a.getVersion());
            if (pom != null) {
                Project parentOwner = FileOwnerQuery.getOwner(FileUtil.toFileObject(pom));
                // can't call getProjectState, since that may block 
                FileObject parentPom = parentOwner.getProjectDirectory().getFileObject("pom.xml");
                if (parentPom != null) {
                    fileSet.add(parentPom);
                }
                NbMavenProject p2 = project.getLookup().lookup(NbMavenProject.class);
                a = p2.getMavenProject().getParentArtifact();
                if (a != null) {
                    toProcess.add(a);
                }
                toProcess.addAll(p2.getMavenProject().getArtifacts());
            }
        }
        return fileSet;
    }
    
    private static class CF extends CompletableFuture<ProjectStateData<MavenProject>> {}

    static ProjectReload.Quality getProjectQuality(MavenProject mp) {
        if (mp == null) {
            return ProjectReload.Quality.NONE;
        }
        if (MavenProjectCache.isFallbackproject(mp)) {
            // could not load at all.
            MavenProject fallback = MavenProjectCache.getPartialProject(mp);
            return fallback != null ? 
                    ProjectReload.Quality.BROKEN:
                    ProjectReload.Quality.FALLBACK;
        }
        if (!MavenProjectCache.getPlaceholderArtifacts(mp).isEmpty()) {
            return ProjectReload.Quality.LOADED;
        }
        return ProjectReload.Quality.RESOLVED;
    }
    
    private ProjectStateData createStateData(MavenProject mp, Quality q) {
        ProjectStateData d;
        
        if (q == null) {
            q = getProjectQuality(mp);
        }
        
        ProjectStateBuilder builder = ProjectStateData.builder(q).
                files(findProjectFiles()).
                timestamp(MavenProjectCache.getLoadTimestamp(mp));
        builder.data(mp);
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

    @NbBundle.Messages({
        "# {0} - project name",
        "ERR_UnprimedInOfflineMode=Priming build for {0} is required, but offline operation was requested.",
        "# {0} - project name",
        "# {1} - parent gav",
        "ERR_ParentPomMissing=Project {0} is missing its parent artifact ({1}) is missing and offline operation was requested"
    })
    @Override
    public CompletableFuture reload(Project project, StateRequest request, LoadContext context) {
        CF cf = new CF();
        loadMavenProject(request, cf);
        return cf;
    }

    private void loadMavenProject(StateRequest request, CF future) {
        NbMavenProjectImpl nbImpl = project.getLookup().lookup(NbMavenProjectImpl.class);
        if (!request.isForceReload()) {
            if (request.isConsistent()) {
                nbImpl.getFreshOriginalMavenProject().thenAccept((mp) ->loadMavenProject2(mp, request, future));
            } else {
                MavenProject mp = nbImpl.getOriginalMavenProjectOrNull();
                if (mp != null) {
                    loadMavenProject2(mp, request, future);
                }
            }
            return;
        }
        loadMavenProject3(future);
    }
    
    private void loadMavenProject2(MavenProject p, StateRequest request, CF future) {
        if (p != null) {
            ProjectReload.Quality q = getProjectQuality(p);
            if (q.isAtLeast(request.getMinQuality())) {
                if (!request.isConsistent()) {
                    future.complete(createStateData(p, null));
                    return;
                }

                // check that the project is consistent with known files:
                long ts = MavenProjectCache.getLoadTimestamp(p);
                Collection<FileObject> fos = findProjectFiles0(p);
                boolean obsolete = false;
                for (FileObject f : fos) {
                    if (f.lastModified().getTime() > ts) {
                        obsolete = true;
                    }
                }
                if (!obsolete) {
                    future.complete(createStateData(p, null));
                    return;
                }
            }
        }
        loadMavenProject3(future);
    }
    
    private void loadMavenProject3(CF future) {
        NbMavenProjectImpl nbImpl = project.getLookup().lookup(NbMavenProjectImpl.class);
        MavenProject current = nbImpl.getOriginalMavenProjectOrNull();
        RequestProcessor.Task t = nbImpl.fireProjectReload(true);
        t.addTaskListener(new TaskListener() {
            @Override
            public void taskFinished(Task task) {
                task.removeTaskListener(this);
                // retain initial = true for the first project load.
                nbImpl.getFreshOriginalMavenProject().thenAccept((mp) -> {
                    future.complete(createStateData(mp, null));
                }).exceptionally(ex -> {
                    MavenProject renewed = nbImpl.getOriginalMavenProjectOrNull();
                    if (renewed == current) {
                        ProjectStateData sd = createStateData(current, Quality.BROKEN);
                        sd.fireChanged(false, true);
                        future.complete(sd);
                    } else {
                        future.complete(createStateData(renewed, Quality.BROKEN));
                    }
                    return null;
                });
            }
        });
    }
}
