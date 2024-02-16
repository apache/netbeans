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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainerException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.DependencyTreeFactory;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyChangeException;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.Scope;
import org.netbeans.modules.project.dependency.Scopes;
import org.netbeans.modules.project.dependency.spi.ProjectDependenciesImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@ProjectServiceProvider(service = ProjectDependenciesImplementation.class, projectType="org-netbeans-modules-maven")
public class MavenDependenciesImplementation implements ProjectDependenciesImplementation {
    private static final Logger LOG = Logger.getLogger(MavenDependenciesImplementation.class.getName());
    
    private final Project project;
    private NbMavenProject nbMavenProject;
    
    private static final Set<Scope> SCOPES = new HashSet<>();
    
    static {
        SCOPES.add(Scopes.COMPILE);
        SCOPES.add(Scopes.RUNTIME);
        SCOPES.add(Scopes.EXTERNAL);
        SCOPES.add(Scopes.TEST);
    }
    
    public MavenDependenciesImplementation(Project project) {
        this.project = project;
    }
    
    private void init() {
        synchronized (this) {
            if (nbMavenProject != null) {
                return;
            }
            nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        }
    }
    
    /**
     * Mapping from the abstract scopes to Maven
     */
    static final Map<Scope, String> scope2Maven = new HashMap<>();

    /**
     * Mapping from maven to the abstract scopes
     */
    static final Map<String, Scope> maven2Scope = new HashMap<>();
    
    static final Map<Scope, Collection<Scope>> directScopes = new HashMap<>();
    static final Map<Scope, Collection<Scope>> impliedScopes = new HashMap<>();
    static final Map<Scope, Collection<Scope>> reverseImplied = new HashMap<>();
    
    static {
        scope2Maven.put(Scopes.PROCESS, "compile");
        scope2Maven.put(Scopes.COMPILE, "compile");
        scope2Maven.put(Scopes.RUNTIME, "runtime");
        scope2Maven.put(Scopes.TEST, "test");
        scope2Maven.put(Scopes.EXTERNAL, "provided");
        
        maven2Scope.put("compile", Scopes.COMPILE);
        maven2Scope.put("runtime", Scopes.RUNTIME);
        maven2Scope.put("test", Scopes.TEST);
        maven2Scope.put("provided", Scopes.EXTERNAL);
        
        directScopes.put(Scopes.API, Arrays.asList(Scopes.COMPILE));
        directScopes.put(Scopes.PROCESS, Arrays.asList(Scopes.COMPILE));
        directScopes.put(Scopes.EXTERNAL, Arrays.asList(Scopes.COMPILE));
        directScopes.put(Scopes.COMPILE, Arrays.asList(Scopes.RUNTIME, Scopes.TEST));
        directScopes.put(Scopes.RUNTIME, Arrays.asList(Scopes.TEST));
        
        impliedScopes.put(Scopes.API, Arrays.asList(Scopes.COMPILE, Scopes.RUNTIME, Scopes.TEST));
        impliedScopes.put(Scopes.PROCESS, Arrays.asList(Scopes.COMPILE, Scopes.RUNTIME, Scopes.TEST));
        impliedScopes.put(Scopes.EXTERNAL, Arrays.asList(Scopes.COMPILE, Scopes.RUNTIME, Scopes.TEST));
        impliedScopes.put(Scopes.COMPILE, Arrays.asList(Scopes.RUNTIME, Scopes.TEST));
        impliedScopes.put(Scopes.RUNTIME, Arrays.asList(Scopes.TEST));
        
        reverseImplied.put(Scopes.TEST, Arrays.asList(Scopes.RUNTIME, Scopes.COMPILE, Scopes.API,Scopes.EXTERNAL, Scopes.PROCESS));
        reverseImplied.put(Scopes.RUNTIME, Arrays.asList(Scopes.COMPILE, Scopes.API, Scopes.EXTERNAL, Scopes.PROCESS));
        reverseImplied.put(Scopes.COMPILE, Arrays.asList(Scopes.API, Scopes.EXTERNAL, Scopes.PROCESS));
    }
    
    static String mavenScope(Scope s) {
        return scope2Maven.getOrDefault(s, "compile");
    }
    
    private ArtifactSpec mavenToArtifactSpec(Artifact a) {
        FileObject f = a.getFile() == null ? null : FileUtil.toFileObject(a.getFile());
        if (a.isSnapshot()) {
            return ArtifactSpec.createSnapshotSpec(a.getGroupId(), a.getArtifactId(), 
                    a.getType(), a.getClassifier(), a.getVersion(), a.isOptional(), f, a);
        } else {
            return ArtifactSpec.createVersionSpec(a.getGroupId(), a.getArtifactId(), 
                    a.getType(), a.getClassifier(), a.getVersion(), a.isOptional(), f, a);
        }
    }
    
    /**
     * Returns dependencies declared right in the POM file. Respects the user's query filter for artifacts.
     * @param query
     * @param embedder
     * @return 
     */
    private DependencyResult findDeclaredDependencies(ProjectDependencies.DependencyQuery query, MavenEmbedder embedder) {
        NbMavenProjectImpl impl = (NbMavenProjectImpl)project.getLookup().lookup(NbMavenProjectImpl.class);
        MavenProject proj;
        
        try {
            proj = impl.getFreshOriginalMavenProject().get();
        } catch (ExecutionException | InterruptedException | CancellationException ex) {
            throw new ProjectOperationException(project, ProjectOperationException.State.ERROR, "Unexpected exception", ex);
        }
        List<Dependency> children = new ArrayList<>();
        for (org.apache.maven.model.Dependency d : proj.getDependencies()) {
            String aId = d.getArtifactId();
            String gID = d.getGroupId();
            String scope = d.getScope();
            String classsifier = d.getClassifier();
            String type = d.getType();
            String version = d.getVersion();
            
            ArtifactSpec a;
            
            if (version != null && version.endsWith("-SNAPSHOT")) {
                a = ArtifactSpec.createSnapshotSpec(gID, aId, type, classsifier, version, d.isOptional(), 
                        d.getSystemPath() == null ? null : FileUtil.toFileObject(new File(d.getSystemPath(), aId)), d);
            } else {            
                a = ArtifactSpec.createVersionSpec(gID, aId, type, classsifier, version, d.isOptional(), 
                        d.getSystemPath() == null ? null : FileUtil.toFileObject(new File(d.getSystemPath(), aId)), d);
            }
            Scope s = scope == null ? Scopes.COMPILE : maven2Scope.get(scope);
            if (s == null) {
                s = Scopes.COMPILE;
            }
            Dependency dep = Dependency.create(a, s, Collections.emptyList(), d);
            children.add(dep);
        }

        ArtifactSpec prjSpec = mavenToArtifactSpec(proj.getArtifact());
        Dependency root = Dependency.create(prjSpec, Scopes.DECLARED, children, proj);
        
        return new MavenDependencyResult(proj, root, query.getScopes(), Collections.emptyList(), project, impl.getProjectWatcher());
    }
    
    static Collection<Scope> implies(Scope s) {
        return impliedScopes.getOrDefault(s, Collections.emptyList());
    }
    
    static Collection<Scope> impliedBy(Scope s) {
        return reverseImplied.getOrDefault(s, Collections.emptyList());
    }
    
    @NbBundle.Messages({
        "ERR_DependencyOnBrokenProject=Unable to collect dependencies from a broken project",
        "ERR_DependencyNotPrimed=Unable to collect dependencies from a broken project",
        "ERR_DependencyMissing=Cannot resolve project dependencies",
        "ERR_DependencyGraphError=Cannot construct dependency graph",
        "ERR_DependencyGraphOffline=Not all artifacts are available locally, run priming build."
    })
    @Override
    public DependencyResult findDependencies(ProjectDependencies.DependencyQuery query) {
        init();
        Collection<Scope> scopes = query.getScopes();
        Dependency.Filter filter = query.getFilter();
        
        MavenProject mp = nbMavenProject.getMavenProject();
        if (NbMavenProject.isErrorPlaceholder(mp)) {
            if (nbMavenProject.isMavenProjectLoaded()) {
                throw new ProjectOperationException(project, ProjectOperationException.State.BROKEN, Bundle.ERR_DependencyOnBrokenProject());
            } else {
                throw new ProjectOperationException(project, ProjectOperationException.State.UNINITIALIZED, Bundle.ERR_DependencyNotPrimed());
            }
        }
        
        MavenEmbedder embedder;
        
        if (query.isOffline()) {
            try {
                if (query.isFlushChaches()) {
                    embedder = EmbedderFactory.createProjectLikeEmbedder();
                } else {
                    embedder = EmbedderFactory.getProjectEmbedder();
                }
            } catch (PlexusContainerException ex) {
                throw new ProjectOperationException(project, ProjectOperationException.State.ERROR, Bundle.ERR_DependencyGraphError(), ex.getCause());
            }
        } else {
            try {
                embedder = EmbedderFactory.getOnlineEmbedder();
            } catch (IllegalStateException ex) {
                // yuck, the real exc. is wrapped
                throw new ProjectOperationException(project, ProjectOperationException.State.ERROR, Bundle.ERR_DependencyGraphError(), ex.getCause());
            }
        }
        
        if (query.getScopes().contains(Scopes.DECLARED)) {
            return findDeclaredDependencies(query, embedder);
        }
        
        Collection<String> mavenScopes = scopes.stream().
                map(MavenDependenciesImplementation::mavenScope).
                filter(Objects::nonNull).
                collect(Collectors.toList());
        
        org.apache.maven.shared.dependency.tree.DependencyNode n;
        try {
            n = DependencyTreeFactory.createDependencyTree(mp, embedder, mavenScopes);
        } catch (MavenExecutionException ex) {
            throw new ProjectOperationException(project, ProjectOperationException.State.OK, Bundle.ERR_DependencyGraphError(), ex.getCause());
        } catch (AssertionError e) {
            if (EmbedderFactory.isOfflineException(e)) {
                // HACK: special assertion error from our embedder
                throw new ProjectOperationException(project, ProjectOperationException.State.OFFLINE, Bundle.ERR_DependencyGraphOffline());
            } else {
                throw e;
            }
        }
        Set<Scope> allScopes = Stream.concat(scopes.stream(), scopes.stream().flatMap(x -> impliedBy(x).stream())).collect(Collectors.toSet());
        Dependency.Filter compositeFiter = new Dependency.Filter() {
            @Override
            public boolean accept(Scope s, ArtifactSpec a) {
                return allScopes.contains(s) && 
                    (filter == null || filter.accept(s, a));
            }
        };
        Converter c = new Converter(compositeFiter);
        Dependency root = c.convertDependencies(n);
        return new MavenDependencyResult(nbMavenProject.getMavenProject(), root, new ArrayList<>(scopes), c.broken, 
                project, nbMavenProject);
    }
    
    static Scope scope(Artifact a) {
        String as = a.getScope();
        if (as == null) {
            return Scopes.COMPILE;
        }
        switch (as) {
            case Artifact.SCOPE_COMPILE:
                return Scopes.COMPILE;
                
            case Artifact.SCOPE_RUNTIME:
            case Artifact.SCOPE_COMPILE_PLUS_RUNTIME:
            case Artifact.SCOPE_RUNTIME_PLUS_SYSTEM:
                return Scopes.RUNTIME;
                
            case Artifact.SCOPE_IMPORT:
            case Artifact.SCOPE_SYSTEM:
                return Scopes.EXTERNAL;
                
            case Artifact.SCOPE_TEST:
                return Scopes.TEST;
            default:
                return Scopes.COMPILE;
        }
    }
    
    private static String getFullArtifactId(Artifact a) {
        if (a.getDependencyTrail() == null) {
            return "/" + a.getId();
        } else {
            return String.join("/", a.getDependencyTrail()) + "/" + a.getId(); // NOI18N
        }
    }
    
    private class Converter {
        final Map<String, List<org.apache.maven.shared.dependency.tree.DependencyNode>> realNodes = new HashMap<>();
        final Dependency.Filter filter;
        final Set<ArtifactSpec> broken = new HashSet<>();

        public Converter(Dependency.Filter filter) {
            this.filter = filter;
        }

        private void findRealNodes(org.apache.maven.shared.dependency.tree.DependencyNode n) {
            if (n.getArtifact() == null) {
                return;
            }
            Artifact a = n.getArtifact();
            if (n.getState() != org.apache.maven.shared.dependency.tree.DependencyNode.INCLUDED) {
                return;
            }
            // register (if not present) using plain artifact ID, but also using the full path, which will be preferred for the lookup.
            realNodes.putIfAbsent(a.getId(), n.getChildren());
            realNodes.put(getFullArtifactId(a), n.getChildren());

            for (org.apache.maven.shared.dependency.tree.DependencyNode c : n.getChildren()) {
                findRealNodes(c);
            }
        }

        private Dependency convertDependencies(org.apache.maven.shared.dependency.tree.DependencyNode n) {
            findRealNodes(n);
            return convert2(true, n);
        }


        private Dependency convert2(boolean root, org.apache.maven.shared.dependency.tree.DependencyNode n) {
            List<Dependency> ch = new ArrayList<>();

            List<org.apache.maven.shared.dependency.tree.DependencyNode> children = n.getChildren();
            org.apache.maven.artifact.Artifact thisArtifact = n.getArtifact();
            org.apache.maven.artifact.Artifact relatedArtifact = n.getRelatedArtifact();

            switch (n.getState()) {
                case org.apache.maven.shared.dependency.tree.DependencyNode.OMITTED_FOR_CYCLE:
                case org.apache.maven.shared.dependency.tree.DependencyNode.OMITTED_FOR_DUPLICATE:
                    // TODO: unless the client specifies NOT to eliminate duplicates from the tree,
                    // we need to include the duplicate including the children, to form a correct full dependency tree. 
                    if (relatedArtifact != null) {
                        children = realNodes.get(getFullArtifactId(n.getRelatedArtifact()));
                    }
                    if (children == null) {
                        children = realNodes.getOrDefault(n.getArtifact().getId(), n.getChildren());
                    }
                    break;
                case org.apache.maven.shared.dependency.tree.DependencyNode.OMITTED_FOR_CONFLICT:
                    // there are two cases when OMITTED_FOR_CONFLICT is used: 
                    // 1. another version is actually used, which means that unless the client requests to omit 
                    //    duplicates, we need to include the ACTUAL dependency's artifact version and its children.
                    // 2. different artifact is related, meaning this dependency is forcibly excluded. In this
                    //    case, the dependency should not be reported at all unless (TODO:) client requests full 
                    //    dependency info
                    if (relatedArtifact != null) {
                        if (Objects.equals(relatedArtifact.getGroupId(), thisArtifact.getGroupId()) &&
                            Objects.equals(relatedArtifact.getArtifactId(), thisArtifact.getArtifactId())) {
                            thisArtifact = relatedArtifact;
                            // TODO: report the original artifact to the client when Relations appear in the API.
                            // use children from the artifact:
                            children = realNodes.getOrDefault(relatedArtifact.getId(), n.getChildren());
                        } else {
                            // exclude the artifact
                            return null;
                        }
                    } else {
                        // the conflict is not known. Omit, because we do not have any information and this 
                        // artifact does not appear in the tree.
                        return null;
                    }
            }

            for (org.apache.maven.shared.dependency.tree.DependencyNode c : children) {
                Dependency cd = convert2(false, c);
                if (cd != null) {
                    ch.add(cd);
                }
            }
            ArtifactSpec aspec;
            String cs = thisArtifact.getClassifier();
            if ("".equals(cs)) {
                cs = null;
            }
            aspec = mavenToArtifactSpec(thisArtifact);
            if (aspec.getLocalFile() == null) {
                broken.add(aspec);
            }
            Scope s = scope(thisArtifact);

            if (!root && !filter.accept(s, aspec)) {
                return null;
            }

            return Dependency.create(aspec, s, ch, n);
        }
    }
    
    static boolean dependencyEquals(Dependency dspec, org.apache.maven.model.Dependency mavenD) {
        ArtifactSpec spec = dspec.getArtifact();
        String mavenClass = mavenD.getClassifier();
        if ("".equals(mavenClass)) {
            mavenClass = null;
        }
        if (!(
            Objects.equals(spec.getGroupId(), mavenD.getGroupId()) &&
            Objects.equals(spec.getArtifactId(), mavenD.getArtifactId()) &&    
            Objects.equals(spec.getClassifier(), mavenClass) &&
            Objects.equals(spec.getVersionSpec(), mavenD.getVersion()))) {
            return false;
        }
        if (spec.getType() != null && !Objects.equals(spec.getType(), mavenD.getType())) {
            return false;
        }
        if (dspec.getScope() != null) {
            if (!Objects.equals(mavenScope(dspec.getScope()), mavenD.getScope())) {
                return false;
            }
        }
        return true;
    }
    
    static boolean artifactEquals(ArtifactSpec spec, Artifact mavenA) {
        String mavenClass = mavenA.getClassifier();
        if ("".equals(mavenClass)) {
            mavenClass = null;
        }
        if (!(
            Objects.equals(spec.getGroupId(), mavenA.getGroupId()) &&
            Objects.equals(spec.getArtifactId(), mavenA.getArtifactId()) &&    
            Objects.equals(spec.getClassifier(), mavenClass) &&
            Objects.equals(spec.getVersionSpec(), mavenA.getVersion()))) {
            return false;
        }
        if (spec.getType() != null && !Objects.equals(spec.getType(), mavenA.getType())) {
            return false;
        }
        return true;
    }
}
