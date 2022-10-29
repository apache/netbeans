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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.GradleConfiguration;
import org.netbeans.modules.gradle.api.GradleDependency;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.project.dependency.ArtifactSpec;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.project.dependency.DependencyResult;
import org.netbeans.modules.project.dependency.SourceLocation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 * Exports dependency information through dependency API
 * 
 * @author sdedic
 */
public final class GradleDependencyResult implements DependencyResult, PropertyChangeListener {
    private final NbGradleProject gp;
    private final Project gradleProject;
    private final Dependency root;
    private boolean valid;

    // @GuardedBy(this)
    PropertyChangeListener wL;
    // @GuardedBy(this)
    private List<ChangeListener> listeners;
    // @GuardedBy(this)
    private List<ChangeListener> sourceListeners;
    // @GuardedBy(this)
    private Collection<ArtifactSpec> problems;
    // DWs are kept by document
    private Map<FileObject, DW> documentWatchers = new HashMap<>();
    
    private final FileObject projectFile;

    public GradleDependencyResult(Project gradleProject, Dependency root) {
        this.gradleProject = gradleProject;
        this.root = root;
        this.gp = NbGradleProject.get(gradleProject);
        File bs = gp.getGradleFiles().getBuildScript();
        this.projectFile = bs == null ? null : FileUtil.toFileObject(gp.getGradleFiles().getBuildScript());
    }
    
    @Override
    public Project getProject() {
        return gradleProject;
    }

    @Override
    public Dependency getRoot() {
        return root;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public Collection<ArtifactSpec> getProblemArtifacts() {
        return Collections.unmodifiableCollection(problems);
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        synchronized (this) {
            if (listeners == null) {
                listeners = new ArrayList<>();
                wL = WeakListeners.propertyChange(this, gradleProject);
                NbGradleProject.addPropertyChangeListener(gradleProject, wL);
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
                listeners = null;
                NbGradleProject.removePropertyChangeListener(gradleProject, wL);
            }
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
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

    // @GuardedBy(this)
    private DependencyText.Mapping  sourceMapping;

    @Override
    public SourceLocation getDeclarationRange(Dependency d, String part) throws IOException {
        DependencyText.Mapping  mapping;
        DependencyText.Part p = null;
        
        synchronized (this) {
            mapping = sourceMapping;
            if (mapping != null) {
                return getDeclarationRange0(mapping, d, part);
            }
        }
        
        GradleBaseProject gbp = GradleBaseProject.get(gradleProject);
        Collection<String> cfgNames = gbp.getConfigurations().keySet();
        TextDependencyScanner ts = new TextDependencyScanner().withConfigurations(cfgNames);
        
        root.getChildren().forEach(rd -> {
            Info info = (Info)rd.getProjectData();
            GradleConfiguration org = info.config.getDependencyOrigin(info.gradleDependency);
            if (org == null) {
                org = info.config;
            }
            ts.addDependencyOrigin(info.gradleDependency, org.getName());
        });
        
        String contents = null;
        if (projectFile != null) {
            EditorCookie cake = null;
            cake = projectFile.getLookup().lookup(EditorCookie.class);
            if (cake != null) {
                Document doc = cake.getDocument();
                if (doc != null) {
                    String[] docContent = new String[1];
                    doc.render(() -> {
                        try {
                            docContent[0] = doc.getText(0, doc.getLength());
                        } catch (BadLocationException ex) {
                            // ignore
                        }
                    });
                }
            }
        }
        if (contents == null) {
            contents = projectFile.asText();
        }
        ts.parseDependencyList(contents);
        
        mapping = ts.mapDependencies(root.getChildren());
        
        synchronized (this) {
            if (sourceMapping == null) {
                sourceMapping = mapping;
            }
        }
        return getDeclarationRange0(mapping, d, part);
    }
    
    private SourceLocation getDeclarationRange0(DependencyText.Mapping mapping, Dependency d, String part) {
        Dependency direct = d;
        while (direct.getParent() != null && direct.getParent() != root) {
            direct = direct.getParent();
        }
        DependencyText.Part found = mapping.getText(direct, part);
        if (found == null) {
            return null;
        }
        return partToLocation(direct == d ? null : direct, found);
    }
    
    private SourceLocation partToLocation(Dependency rootDep, DependencyText.Part p) {
        return new SourceLocation(projectFile, p.startPos, p.endPos, rootDep);
    }

    @Override
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public Collection<FileObject> getDependencyFiles() {
        FileObject fo = FileUtil.toFileObject(gp.getGradleFiles().getBuildScript());
        return fo == null ? Collections.emptyList() : Collections.singletonList(fo);
    }
    
    static class Info {
        final GradleConfiguration config;
        final GradleDependency gradleDependency;

        public Info(GradleConfiguration config, GradleDependency gradleDependency) {
            this.config = config;
            this.gradleDependency = gradleDependency;
        }
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
            FileObject fo = FileUtil.toFileObject(gp.getGradleFiles().getBuildScript());
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
            sourceMapping = null;
            if (sourceListeners == null || sourceListeners.isEmpty()) {
                return;
            }
            ll = sourceListeners.toArray(new ChangeListener[sourceListeners.size()]);
        }
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : ll) {
            l.stateChanged(e);
        }
    }
}
