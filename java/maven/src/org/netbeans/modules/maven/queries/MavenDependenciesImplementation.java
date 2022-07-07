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
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.InputSource;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.codehaus.plexus.PlexusContainerException;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.DependencyTreeFactory;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectDependencies;
import org.netbeans.modules.project.dependency.ProjectOperationException;
import org.netbeans.modules.project.dependency.Scope;
import org.netbeans.modules.project.dependency.Scopes;
import org.netbeans.modules.project.dependency.SourceLocation;
import org.netbeans.modules.project.dependency.spi.ProjectDependenciesImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

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
    
    static final Map<Scope, String> mavenScopes;
    
    static {
        mavenScopes = new HashMap<>();
        mavenScopes.put(Scopes.PROCESS, "compile");
        mavenScopes.put(Scopes.COMPILE, "compile");
        mavenScopes.put(Scopes.RUNTIME, "runtime");
        mavenScopes.put(Scopes.TEST, "test");
        mavenScopes.put(Scopes.EXTERNAL, "provided");
    }
    
    static String mavenScope(Scope s) {
        return mavenScopes.getOrDefault(s, "runtime");
    }
    
    @Override
    public ArtifactSpec getProjectArtifact() {
        init();
        return mavenToArtifactSpec(nbMavenProject.getMavenProject().getArtifact());
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
        
        Collection<String> mavenScopes = scopes.stream().
                map(MavenDependenciesImplementation::mavenScope).
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
        Set<Scope> allScopes = new HashSet<>();
        
        Queue<Scope> processScopes = new ArrayDeque<>(scopes);
        while (!processScopes.isEmpty()) {
            Scope s = processScopes.poll();
            Set<Scope> newScopes = new HashSet<>();
            newScopes.add(s);
            for (Scope t : SCOPES) {
                if (s.includes(t)) {
                    newScopes.add(t);
                } else if (t.implies(s)) {
                    newScopes.add(t);
                }
            }
            newScopes.removeAll(allScopes);
            allScopes.addAll(newScopes);
            processScopes.addAll(newScopes);
        }
        Set<ArtifactSpec> broken = new HashSet<>();
        Dependency.Filter compositeFiter = new Dependency.Filter() {
            @Override
            public boolean accept(Scope s, ArtifactSpec a) {
                return allScopes.contains(s) && 
                    (filter == null || filter.accept(s, a));
            }
        };
        
        return new Result(nbMavenProject.getMavenProject(), 
                convert2(n, compositeFiter, broken), new ArrayList<>(scopes), broken);
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
    
    private Dependency convert2(org.apache.maven.shared.dependency.tree.DependencyNode n, Dependency.Filter filter, Set<ArtifactSpec> broken) {
        List<Dependency> ch = new ArrayList<>();
        for (org.apache.maven.shared.dependency.tree.DependencyNode c : n.getChildren()) {
            Dependency cd = convert2(c, filter, broken);
            if (cd != null) {
                ch.add(cd);
            }
        }
        Artifact a = n.getArtifact();
        ArtifactSpec aspec;
        String cs = a.getClassifier();
        if ("".equals(cs)) {
            cs = null;
        }
        aspec = mavenToArtifactSpec(a);
        if (aspec.getLocalFile() == null) {
            broken.add(aspec);
        }
        Scope s = scope(a);
        
        if (!filter.accept(s, aspec)) {
            return null;
        }
        
        return Dependency.create(aspec, s, ch, n);
    }
    
    class Result implements DependencyResult, PropertyChangeListener {
        final MavenProject  project;
        final Dependency    rootNode;
        final Collection<Scope> scopes;
        final Collection<ArtifactSpec> problems;
        
        PropertyChangeListener wL;
        Model effectiveModel;
        Map<FileObject, StyledDocument> openedPoms = new HashMap<>();
        private List<ChangeListener> listeners;

        public Result(MavenProject proj, Dependency rootNode, Collection<Scope> scopes, Collection<ArtifactSpec> problems) {
            this.project = proj;
            this.rootNode = rootNode;
            this.scopes = scopes;
            this.problems = problems;
        }
        
        @Override
        public Project getProject() {
            return MavenDependenciesImplementation.this.project;
        }

        @Override
        public Collection<ArtifactSpec> getProblemArtifacts() {
            return Collections.unmodifiableCollection(problems);
        }

        @Override
        public Dependency getRoot() {
            return rootNode;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            boolean attach = false;
            synchronized (this) {
                if (listeners == null) {
                    attach = true;
                    listeners = new ArrayList<>();
                }
                listeners.add(l);
            }
            if (!attach) {
                return;
            }
            wL = WeakListeners.propertyChange(this, nbMavenProject);
            nbMavenProject.addPropertyChangeListener(wL);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            boolean detach = false;
            synchronized (this) {
                if (listeners == null) {
                    return;
                }
                listeners.remove(l);
                if (listeners.isEmpty()) {
                    nbMavenProject.removePropertyChangeListener(wL);
                }
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                return;
            }
            
            List<ChangeListener> ll;
            synchronized (this) {
                if (listeners == null || listeners.isEmpty()) {
                    return;
                }
                ll = new ArrayList<>(listeners);
            }
            ChangeEvent e = new ChangeEvent(this);
            for (ChangeListener l : ll ) {
                l.stateChanged(e);
            }
        }
        
        public String toString() {
            return "Depdenencies for " + getRoot().getArtifact() + " "
                    + scopes.toString();
        }
        
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @NbBundle.Messages("ERR_ModelBuildFailed=Model building failed")
        @Override
        public SourceLocation getDeclarationRange(Dependency dep) throws IOException {
            Model m;
            if (effectiveModel == null) {
                ModelBuildingResult res;
                try {
                    res = EmbedderFactory.getProjectEmbedder().executeModelBuilder(
                            project.getFile());
                } catch (ModelBuildingException ex) {
                    throw new IOException(Bundle.ERR_ModelBuildFailed(), ex);
                }
                synchronized (this) {
                    if (effectiveModel == null) {
                        effectiveModel = res.getEffectiveModel();
                    }
                }
            }
            
            Dependency topLevel = null;
            Dependency search = dep;
            org.apache.maven.model.Dependency selected = null;
            if (dep.getProjectData() instanceof DependencyNode) {
                DependencyNode pd = (DependencyNode)dep.getProjectData();
                if (!(pd == rootNode.getProjectData() || pd.getParent() == rootNode.getProjectData())) {
                    do {
                        pd = pd.getParent();
                    } while (pd.getParent() != rootNode.getProjectData());
                    
                    List<Dependency> rootDeps = rootNode.getChildren();
                    for (Dependency cd : rootDeps) {
                        if (artifactEquals(cd.getArtifact(), pd.getArtifact())) {
                            search = cd;
                            topLevel = cd;
                            break;
                        }
                    }
                }
            }
            
            for (org.apache.maven.model.Dependency d : effectiveModel.getDependencies()) {
                if (dependencyEquals(dep, d)) {
                    selected = d;
                    break;
                }
            }
            if (selected == null) {
                return null;
            }                
            InputLocation l = selected.getLocation("");
            InputSource s = l.getSource();
            String path = s.getLocation();
            FileObject fo = FileUtil.toFileObject(new File(path));
            if (fo == null) {
                return null;
            }
            StyledDocument d;
            
            synchronized (this) {
                d = openedPoms.get(fo);
            }
            if (d == null) {
                DataObject dobj = DataObject.find(fo);
                EditorCookie cake = dobj.getLookup().lookup(EditorCookie.class);
                if (cake == null) {
                    return null;
                }
                d = cake.openDocument();
                synchronized (this) {
                    openedPoms.put(fo, d);
                }
            }
            LineDocument ld = LineDocumentUtils.as(d, LineDocument.class);
            if (ld == null) {
                return new SourceLocation(fo, -1, -1, topLevel);
            } else {
                int so = LineDocumentUtils.getLineStartFromIndex(ld, l.getLineNumber() - 1) 
                        + l.getColumnNumber() - 1;
                int[] depStart = new int[] { so };
                int[] depEnd = new int[] { so };
                d.render(() -> {
                    try {
                        int from = Math.max(0, so - 15);
                        String text = ld.getText(from, ld.getLength() - from);
                        int start = text.lastIndexOf("<dependency", so - from);
                        if (start > -1) {
                            depStart[0] = start + from;
                        }
                        int end = text.indexOf("</dependency", so - from);
                        if (end != -1) {
                            // find the closing ">"
                            end += 10;
                            int end2 = text.indexOf(">", end);
                            if (end2 > 0) {
                                end = end2 + 1;
                            }
                            depEnd[0] = end + from;
                        }
                    } catch (BadLocationException ex) {
                    }
                });
                
                return new SourceLocation(fo, depStart[0], depEnd[0], topLevel);
            }
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
            Objects.equals(spec.getClassifier(), mavenClass)) &&
            Objects.equals(spec.getVersionSpec(), mavenD.getVersion())) {
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
            Objects.equals(spec.getClassifier(), mavenClass)) &&
            Objects.equals(spec.getVersionSpec(), mavenA.getVersion())) {
            return false;
        }
        if (spec.getType() != null && !Objects.equals(spec.getType(), mavenA.getType())) {
            return false;
        }
        return true;
    }
}
