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
package org.netbeans.modules.gradle.java.queries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleConfiguration;
import org.netbeans.modules.gradle.api.GradleDependency;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.ProjectSpec;
import org.netbeans.modules.project.dependency.Scope;
import org.netbeans.modules.project.dependency.Scopes;
import org.netbeans.modules.project.dependency.spi.ProjectDependenciesImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(service = ProjectDependenciesImplementation.class, projectType=NbGradleProject.GRADLE_PROJECT_TYPE)
public class GradleDependenciesImplementation implements ProjectDependenciesImplementation {
    private static final int DEPENDENCIES_MAX_COUNT = 100000;
    private static final Logger LOG = Logger.getLogger(GradleDependenciesImplementation.class.getName());
    
    private final Project project;
    private final NbGradleProject nbgp;
    
    public GradleDependenciesImplementation(Project project) {
        this.project = project;
        nbgp = NbGradleProject.get(project);
    }
    
    private GradleScopes scopes;
    
    String toGradleConfigName(Scope s) {
        GradleScope gs = toGradleScope(s);
        if (gs != null) {
            return gs.getConfigurationName();
        } else {
            return null;
        }
    }
    
    GradleScopes gradleScopes() {
        if (scopes == null) {
            scopes = new GradleScopesBuilder(project).build();
        }
        return scopes;
    }
    
    Set<GradleScope> allScopes() {
        return new HashSet<>(gradleScopes().scopes());
    }
    
    GradleScope toGradleScope(Scope s) {
        GradleScope gs = gradleScopes().toGradleScope(s);
        if (gs != null) {
            return gs;
        }
        String n = toGradleConfigName(s);
        if (n != null) {
            return gradleScopes().toGradleScope(n);
        } else {
            return null;
        }
    }

    @NbBundle.Messages({
        "DESC_ObtainDependencies=Collect dependencies",
        "ERR_DependenciesCancelled=Cancelled",
        "# {0} - error message",
        "ERR_Unexpected=Unexpected error: {0}"
    })
    @Override
    public DependencyResult findDependencies(ProjectDependencies.DependencyQuery query) throws ProjectOperationException {
        CompletableFuture<DependencyResult> result = new CompletableFuture<>();
        
        nbgp.toQuality(Bundle.DESC_ObtainDependencies(), query.isOffline() ? NbGradleProject.Quality.FULL : NbGradleProject.Quality.FULL_ONLINE, query.isFlushChaches()).thenApply(p -> {
            DependencyResult dr = new Collector(query).processDependencies(nbgp);
            result.complete(dr);
            return null;
        });
        try {
            return result.get();
        } catch (InterruptedException ex) {
            throw new ProjectOperationException(project, ProjectOperationException.State.OK, Bundle.ERR_DependenciesCancelled(), ex);
        } catch (ExecutionException ex) {
            Throwable t = ex.getCause();
            if (t instanceof ProjectOperationException) {
                throw (ProjectOperationException)t;
            } else {
                throw new ProjectOperationException(project, ProjectOperationException.State.ERROR, Bundle.ERR_Unexpected(ex.getLocalizedMessage()), t);
            }
        }
    }

    @NbBundle.Messages({
        "# {0} - project directory path",
        "ERR_NoProjectDirectory=Unable to find project directory: {0}"
    })
    class Collector {
        final ProjectDependencies.DependencyQuery query;
        final GradleBaseProject base;
        
        boolean acceptUnresolved;
        GradleConfiguration cfg;
        Scope scope;
        List<Dependency> problems = new ArrayList<>();
        int counter = 0;
        
        /**
         * Marks files in file2FileObject without a FileObject.
         */
        final FileObject marker = FileUtil.getConfigRoot();

        /**
         * Fast cache: there may be many replicas of an artifact (and its file) in the tree,
         * and official FileUtil.toFileObject() goes through File > URI > URL > URMapper transitions.
         */
        final Map<File, FileObject> file2FileObject = new HashMap<>();
        
        public Collector(ProjectDependencies.DependencyQuery query) {
            this.query = query;
            base = GradleBaseProject.get(project);
        }
        
        /**
         * Creates a dependency result for project's declared dependencies. Since plugins may inject dependencies not present in the
         * build file, the dependencies from the model are filtered for those present in the build file.
         * 
         * @param allScopes all scopes that should be included.
         * @return dependency result instance
         */
        DependencyResult declaredDependencies(Set<Scope> allScopes) {
            acceptUnresolved = true;
            Set<String> cfgNames = new HashSet<>();
            
            List<Dependency> declared = new ArrayList<>();
            
            Set<? extends Scope> a;
            if (allScopes.isEmpty()) {
                a = allScopes();
            } else {
                a = allScopes;
            }
            
            Set<GradleScope> all = allScopes();
            
            Queue<GradleScope> toProcess = new ArrayDeque<>();
            for (Scope scope : a) {
                GradleScope gs = gradleScopes().toGradleScope(scope);
                if (gs != null) {
                    toProcess.add(gs);
                }
            }
            
            GradleScope gs;
            while ((gs = toProcess.poll()) != null) {
                cfgNames.add(gs.getConfigurationName());
                for (GradleScope t : all) {
                    if (t.includes(gs)) {
                        toProcess.add(t);
                    }
                }
            }
            
            for (String cn : cfgNames) {
                GradleConfiguration cfg = base.getConfigurations().get(cn);
                if (cfg == null) {
                    continue;
                }
                this.cfg = cfg;
                this.scope = gradleScopes().toGradleScope(cn);
                for (GradleDependency dep : cfg.getConfiguredDependencies()) {
                    GradleConfiguration origin = cfg.getDependencyOrigin(dep);
                    if (origin != null && origin != cfg) {
                        // inherited
                        continue;
                    }
                    declared.add(createDependency(dep, Collections.emptyList()));
                }
            }
            File f = nbgp.getGradleFiles().getProjectDir();
            FileObject pf = f == null ? null : FileUtil.toFileObject(f);
            if (pf == null) {
                throw new ProjectOperationException(project, ProjectOperationException.State.ERROR, Bundle.ERR_NoProjectDirectory(f));
            }
            ArtifactSpec part = createProjectArtifact(null, base.getPath(), null);
            ProjectSpec pspec = ProjectSpec.create(base.getPath(), pf);
            
            Dependency tempRoot = Dependency.create(pspec, part, null, declared, project);
            
            NbGradleProject ngp = NbGradleProject.get(project);
            GradleBaseProject gbp =  GradleBaseProject.get(project);
            DependencyText.Mapping map;
            try {
                map = GradleDependencyResult.computeTextMappings(ngp, gbp, tempRoot.getChildren(), true);
            } catch (IOException ex) {
                throw new ProjectOperationException(project, ProjectOperationException.State.ERROR, "Unable to match dependencies to build script", ex);
            }
            for (Iterator<Dependency> it = declared.iterator(); it.hasNext(); ) {
                if (map.getText(it.next(), null) == null) {
                    it.remove();
                }
            }
            return new GradleDependencyResult(project, scopes, tempRoot);
        }
        
        DependencyResult processDependencies(NbGradleProject nbgp) {
            GradleBaseProject base =  GradleBaseProject.get(project);
            Collection<Scope> userScopes = query.getScopes();
            
            ArrayDeque<Scope> processScopes = new ArrayDeque<>(userScopes);
            Set<Scope> allScopes = new HashSet<>();
            
            if (processScopes.remove(Scopes.DECLARED)) {
                return declaredDependencies(allScopes);
            }

            // process unknown scopes
            while (!processScopes.isEmpty()) {
                Scope user = processScopes.poll();
                GradleScope s = gradleScopes().toGradleScope(user);
                if (!s.getConfigurationName().equals(s.name())) {
                    Set<Scope> newScopes = new HashSet<>();
                    newScopes.addAll(s.getIncluded());
                    newScopes.removeAll(allScopes);
                    processScopes.addAll(newScopes);
                } else {
                    allScopes.add(s);
                }
            }
            
            List<Dependency> rootDeps = new ArrayList<>();
            LOG.log(Level.FINE, "** Computing dependencies for project {0}", project);
            for (Scope s : allScopes) {
                String cfgName = toGradleConfigName(s);
                if (cfgName == null) {
                    continue;
                }
                GradleConfiguration cfg = base.getConfigurations().get(cfgName);
                if (cfg == null) {
                    continue;
                }
                
                this.scope = s;
                
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Dependencies for configuration {0}: {1}", new Object[] { cfg, cfg.getDependencies() });
                }
                
                for (GradleDependency dep : cfg.getDependencies()) {
                    this.cfg = cfg.getDependencyOrigin(dep);
                    if (this.cfg == null) {
                        // safeguard: we cannot determine the origin, so let's assume this configuration defines the dependency
                        this.cfg = cfg;
                    }
                    this.scope = gradleScopes().toGradleScope(this.cfg.getName());
                    List<Dependency> ch = processLevel(cfg, dep, new LinkedHashSet<>());
                    Dependency n = createDependency(dep, ch);
                    rootDeps.add(n);
                }
            }
            
            File f = nbgp.getGradleFiles().getProjectDir();
            FileObject pf = f == null ? null : FileUtil.toFileObject(f);
            if (pf == null) {
                throw new ProjectOperationException(project, ProjectOperationException.State.ERROR, Bundle.ERR_NoProjectDirectory(f));
            }
            ArtifactSpec part = createProjectArtifact(null, base.getPath(), rootDeps);
            ProjectSpec pspec = ProjectSpec.create(base.getPath(), pf);
            
            Dependency root = Dependency.create(pspec, part, null, rootDeps, project);
            
            return new GradleDependencyResult(project, scopes, root);
        }
        
        ArtifactSpec createProjectArtifact(GradleDependencyResult.Info info, String projectId, List<Dependency> children) {
            ArtifactSpec spec = null;
            String gav = base.findProjectGav(projectId);
            if (gav != null) {
                String[] split = gavSplit(gav);
                if (split != null) {
                    String v = split.length > 2 ? split[2] : null;
                    boolean snap = v == null || v.toLowerCase().contains("snapshot-"); // NOI18N
                    spec = snap ? 
                            ArtifactSpec.createSnapshotSpec(split[0], split[1], null, null, split[2], false, null, info) :
                            ArtifactSpec.createVersionSpec(split[0], split[1], null, null, split[2], false, null, info);
                }
            }
            if (spec == null) {
                if (children != null && !children.isEmpty()) {
                    spec = ArtifactSpec.createVersionSpec(null, projectId, null, null, null, false, null, info);
                    Dependency ret = Dependency.create(spec, scope, children, info);
                    if (info != null && info.gradleDependency != null) {
                        problems.add(ret);
                    }
                }
            }
            return spec;
        }
        
        Dependency createDependency(GradleDependency dep, List<Dependency> children) {
            GradleDependencyResult.Info info = new GradleDependencyResult.Info(cfg, dep);
            if (dep instanceof GradleDependency.UnresolvedDependency) {
                if (acceptUnresolved) {
                    GradleDependency.UnresolvedDependency gud = (GradleDependency.UnresolvedDependency)dep;
                    String[] gav = gud.getId().split(":");
                    if (gav.length < 2) {
                        // not group:artifact, cannot represent as an artifact
                        return null;
                    }
                    ArtifactSpec spec = 
                            ArtifactSpec.createVersionSpec(gav[0], gav[1], 
                                    null, 
                                    gav.length >= 4 ? gav[3] : null,

                                    gav.length >= 3 ? gav[2] : null, false, null, dep);
                    return Dependency.create(spec, scope, children, info);
                } else {
                    return null;
                }
            }            
            
            if (dep instanceof GradleDependency.ProjectDependency) {
                GradleDependency.ProjectDependency pd = (GradleDependency.ProjectDependency)dep;
                String projectId = pd.getId();
                File f = pd.getPath();
                FileObject fo = FileUtil.toFileObject(f);
                ArtifactSpec spec = null;
                

                if (fo != null && ProjectManager.getDefault().isProject(fo)) {
                    ProjectSpec pspec = ProjectSpec.create(projectId, fo);
                    // will not declare output artifact at, requires some more digging in the configuration.
                    return Dependency.create(pspec, spec, scope, children, info);
                }
            } 
            if (!(dep instanceof GradleDependency.ModuleDependency)) {
                if (children != null && !children.isEmpty()) {
                    ArtifactSpec spec = ArtifactSpec.createVersionSpec(null, dep.getId(), null, null, null, false, null, info);
                    Dependency ret = Dependency.create(spec, scope, children, info);
                    problems.add(ret);
                    return ret;
                } else {
                    return null;
                }
            }
            // regular dependency
            GradleDependency.ModuleDependency md = (GradleDependency.ModuleDependency)dep;
            boolean snap = md.getVersion().toLowerCase().contains("snapshot-");
            FileObject file;
            
            if (md.getArtifacts().size() == 1) {
                File f = md.getArtifacts().iterator().next();
                FileObject cached = file2FileObject.get(f);
                if (cached == null) {
                    cached = FileUtil.toFileObject(f);
                    file2FileObject.put(f, cached != null ? cached : marker);
                }
                if (cached == marker) {
                    cached = null;
                }
                file = cached;
            } else {
                file = null;
            }
            ArtifactSpec spec = snap ?
                    ArtifactSpec.createSnapshotSpec(md.getGroup(), md.getName(), null, null, md.getVersion(), false, file, dep) :
                    ArtifactSpec.createVersionSpec(md.getGroup(), md.getName(), null, null, md.getVersion(), false, file, dep);
            Dependency ret = Dependency.create(spec, scope, children, info);
            return ret;
        }
        
        private int level = 0;
        
        List<Dependency> processLevel(GradleConfiguration c, GradleDependency d, Set<GradleDependency> allParents) {
            level++;
            try {
                return processLevel0(c, d, allParents);
            } finally {
                level--;
            }
        }
        
        List<Dependency> processLevel0(GradleConfiguration c, GradleDependency d, Set<GradleDependency> allParents) {
            if (counter > DEPENDENCIES_MAX_COUNT) {
                LOG.log(Level.WARNING, "Potential dependency cycle for {0} (parents: {1}), abort!", new Object[] { d, allParents });
                return Collections.emptyList();
            }
            counter++;
            Collection<GradleDependency> deps = c.getDependenciesOf(d);
            if (deps == null) {
                return Collections.emptyList();
            }
            if (LOG.isLoggable(Level.FINER)) {
                StringBuilder indent = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    indent.append("  ");
                }
                String chIds = deps.stream().sequential().filter(Objects::nonNull).map(GradleDependency::getId).collect(Collectors.joining(", "));
                LOG.log(Level.FINER, "Children: {0} {1} -> {2}", new Object[] { indent, d.getId(), chIds });
            }
            List<Dependency> res = new ArrayList<>();
            if (!allParents.add(d)) {
                return res;
            }
            for (GradleDependency child : deps) {
                List<Dependency> grandChildren = processLevel(c, child, allParents);
                Dependency n = createDependency(child, grandChildren);
                if (n != null) {
                    res.add(n);
                }
            }
            allParents.remove(d);
            return res;
        }
    }
    
    public static String[] gavSplit(String gav) {
        // the general GAV format is - <group>:<artifact>:<version/snapshot>[:<classifier>][@extension]
        int firstColon = gav.indexOf(':'); // NOI18N
        int versionColon = gav.indexOf(':', firstColon + 1); // NOI18N
        int versionEnd = versionColon > firstColon ? gav.indexOf(':', versionColon + 1) : -1; // NO18N

        if (firstColon == -1 || versionColon == -1 || firstColon == versionColon) {
            throw new IllegalArgumentException("Invalid GAV format: '" + gav + "'"); //NOI18N
        }
        int end = versionEnd == -1 ? gav.length() : versionEnd;

        return new String[]{
            gav.substring(0, firstColon),
            gav.substring(firstColon + 1, versionColon),
            gav.substring(versionColon + 1, end)
        };
    }
}
