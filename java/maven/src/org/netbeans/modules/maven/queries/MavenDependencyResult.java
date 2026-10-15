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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.InputSource;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.ModelList;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.ProjectScopes;
import org.netbeans.modules.project.dependency.Scope;
import org.netbeans.modules.project.dependency.SourceLocation;
import org.netbeans.modules.xml.xam.ModelSource;
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
class MavenDependencyResult implements org.netbeans.modules.project.dependency.DependencyResult, ProjectScopes, PropertyChangeListener {
    final Project ideProject;
    final NbMavenProject mavenProject;
    final Dependency rootNode;
    final Collection<Scope> scopes;
    final Collection<ArtifactSpec> problems;
    PropertyChangeListener wL;
    volatile Model effectiveModel;
    Map<FileObject, StyledDocument> openedPoms = new HashMap<>();
    private List<ChangeListener> listeners;
    private List<ChangeListener> sourceListeners;
    // DWs are kept by document
    private Map<FileObject, DW> documentWatchers = new HashMap<>();
    private volatile boolean valid;

    public MavenDependencyResult(MavenProject proj, Dependency rootNode, Collection<Scope> scopes, Collection<ArtifactSpec> problems, 
            Project project, NbMavenProject mavenProject) {
        this.ideProject = project;
        this.mavenProject = mavenProject;
        this.rootNode = rootNode;
        this.scopes = scopes;
        this.problems = problems;
    }

    @Override
    public ProjectScopes getScopes() {
        return this;
    }

    @Override
    public Collection<? extends Scope> scopes() {
        return MavenDependenciesImplementation.scope2Maven.keySet();
    }

    @Override
    public Collection<? extends Scope> implies(Scope s, boolean direct) {
        return (direct ? MavenDependenciesImplementation.directScopes : MavenDependenciesImplementation.impliedScopes).getOrDefault(s, Collections.emptySet());
    }

    @Override
    public Collection<FileObject> getDependencyFiles() {
        File file = mavenProject.getMavenProject().getFile();
        if (file == null) {
            return Collections.emptyList();
        }
        return Collections.singleton(FileUtil.toFileObject(file));
    }

    @Override
    public Project getProject() {
        return ideProject;
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
        return valid;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        boolean attach = false;
        synchronized (this) {
            if (listeners == null) {
                attach = true;
                listeners = new ArrayList<>();
                wL = WeakListeners.propertyChange(this, mavenProject);
                mavenProject.addPropertyChangeListener(wL);
            }
            listeners.add(l);
        }
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        synchronized (this) {
            if (listeners == null) {
                return;
            }
            listeners.remove(l);
            if (listeners.isEmpty()) {
                mavenProject.removePropertyChangeListener(wL);
                listeners = null;
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
            valid = false;
            if (listeners == null || listeners.isEmpty()) {
                return;
            }
            ll = new ArrayList<>(listeners);
        }
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : ll) {
            l.stateChanged(e);
        }
    }

    @Override
    public void addSourceChangeListener(ChangeListener l) {
        boolean attach = false;
        synchronized (this) {
            if (sourceListeners == null) {
                sourceListeners = new ArrayList<>();
                attach = true;
            }
            sourceListeners.add(l);
        }
        if (attach) {
            FileObject fo = FileUtil.toFileObject(mavenProject.getMavenProject().getFile());
            if (fo != null) {
                addDocumentWatcher(fo);
            }
        }
    }

    @Override
    public void removeSourceChangeListener(ChangeListener l) {
        synchronized (this) {
            if (sourceListeners == null || sourceListeners.isEmpty()) {
                return;
            }
        }
    }

    public String toString() {
        return "Depdenencies for " + getRoot().getArtifact() + " " + scopes.toString();
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }
    
    class DW implements DocumentListener {
        final Document doc;
        final FileObject file;

        public DW(FileObject file, Document doc) {
            this.file = file;
            this.doc = doc;
        }
        
        @Override
        public void insertUpdate(DocumentEvent e) {
            documentChanged(file);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            documentChanged(file);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }
    }
    
    private void addDocumentWatcher(FileObject f) {
        synchronized (this) {
            if (f == null || documentWatchers.containsKey(f)) {
                return;
            }
        }
        EditorCookie cake = f.getLookup().lookup(EditorCookie.class);
        Document doc = null;
        if (cake != null) {
            doc = cake.getDocument();
        }
        DW dw;
        synchronized (this) {
            if (documentWatchers.containsKey(f)) {
                return;
            }
            dw = new DW(f, doc);
            documentWatchers.put(f, dw);
        }
        if (doc != null) {
            doc.addDocumentListener(WeakListeners.create(DocumentListener.class, dw, doc));
        }
    }
    
    void documentChanged(FileObject f) {
        ChangeListener[] ll;
        synchronized (this) {
            effectiveModel = null;
            if (sourceListeners == null || sourceListeners.isEmpty()) {
                return;
            }
            ll = sourceListeners.toArray(new ChangeListener[0]);
        }
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : ll) {
            l.stateChanged(e);
        }
    }

    @NbBundle.Messages(value = "ERR_ModelBuildFailed=Model building failed")
    @Override
    public SourceLocation getDeclarationRange(Dependency dep, String part) throws IOException {
        Model m;
        if (effectiveModel == null) {
            FileObject fo = FileUtil.toFileObject(mavenProject.getMavenProject().getFile());
            addDocumentWatcher(fo);
            ModelBuildingResult res;
            try {
                res = EmbedderFactory.getProjectEmbedder().executeModelBuilder(mavenProject.getMavenProject().getFile());
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
        
        if (part != null && !DependencyResult.PART_CONTAINER.equals(part)) {
            // not supported yet
            return null;
        }
        
        if (dep.getProjectData() instanceof DependencyNode) {
            DependencyNode pd = (DependencyNode) dep.getProjectData();
            if (!(pd == rootNode.getProjectData() || pd.getParent() == rootNode.getProjectData())) {
                do {
                    pd = pd.getParent();
                } while (pd.getParent() != rootNode.getProjectData());
                List<Dependency> rootDeps = rootNode.getChildren();
                for (Dependency cd : rootDeps) {
                    if (MavenDependenciesImplementation.artifactEquals(cd.getArtifact(), pd.getArtifact())) {
                        search = cd;
                        topLevel = cd;
                        break;
                    }
                }
            }
        }
        if (DependencyResult.PART_CONTAINER.equals(part)) {
            InputLocation location = effectiveModel.getLocation("dependencies");
            if (location != null) {
                return fromInputLocation(location, topLevel);
            }
            // make a fallback: locate POM component that corresponds to the dependencies element
            FileObject pomFileObject = FileUtil.toFileObject(mavenProject.getMavenProject().getFile());
            ModelSource source = Utilities.createModelSource(pomFileObject);
            POMModel model = POMModelFactory.getDefault().getModel(source);
            if (model == null) {
                return null;
            }
            org.netbeans.modules.maven.model.pom.Project project = model.getProject();
            for (ModelList ml : project.getChildren(ModelList.class)) {
                if (ml.getListClass() == org.netbeans.modules.maven.model.pom.Dependency.class) {
                    if ("dependencies".equals(ml.getPeer().getNodeName())) { // NOI18N
                        int from = ml.findPosition();
                        int to = ml.findEndPosition();
                        
                        if (from == 0 && to == 0) {
                            from = -1;
                            to = -1;
                        }
                        return new SourceLocation(pomFileObject, from, to, null);
                    }
                }
            }
            return fromInputLocation(effectiveModel.getLocation("dependencies"), topLevel);
        }
        for (org.apache.maven.model.Dependency d : effectiveModel.getDependencies()) {
            if (MavenDependenciesImplementation.dependencyEquals(search, d)) {
                selected = d;
                break;
            }
        }
        if (selected == null) {
            return null;
        }
        return fromInputLocation(selected.getLocation(""), topLevel);
    }
    
    private StyledDocument openDocument(FileObject fo) throws IOException {
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
                openedPoms.putIfAbsent(fo, d);
            }
        }
        return d;
    }
    
    private SourceLocation fromInputLocation(InputLocation l, Object topLevel) throws IOException {
        if (l == null) {
            return null;
        }
        InputSource s = l.getSource();
        String path = s.getLocation();
        FileObject fo = FileUtil.toFileObject(new File(path));
        StyledDocument d = openDocument(fo);
        LineDocument ld = LineDocumentUtils.as(d, LineDocument.class);
        if (ld == null) {
            return new SourceLocation(fo, -1, -1, topLevel);
        } else {
            int so = LineDocumentUtils.getLineStartFromIndex(ld, l.getLineNumber() - 1) + l.getColumnNumber() - 1;
            int[] depStart = new int[]{so};
            int[] depEnd = new int[]{so};
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
