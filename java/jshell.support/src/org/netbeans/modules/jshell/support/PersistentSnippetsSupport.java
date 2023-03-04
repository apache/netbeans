/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.jshell.support;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import javax.lang.model.element.Modifier;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.env.ShellEvent;
import org.netbeans.modules.jshell.env.ShellListener;
import org.netbeans.modules.jshell.env.ShellRegistry;
import org.netbeans.modules.jshell.env.ShellStatus;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public class PersistentSnippetsSupport  {
    private static final PersistentSnippets EMPTY = new PersistentSnippets() {
        @Override
        public Collection<FileObject> getSavedClasses(String s) {
            return Collections.EMPTY_LIST;
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public FileObject savedClassFolder(String name) {
            return null;
        }

        @Override
        public FileObject saveClass(String name, String description, InputStream contents) {
            return null;
        }

        @Override
        public String getDescription(FileObject saved) {
            return null;
        }

        @Override
        public void setDescription(FileObject saved, String desc) {}

        @Override
        public void addChangeListener(ChangeListener l) {}

        @Override
        public void removeChangeListener(ChangeListener l) {}

        @Override
        public Collection<FileObject> startupSnippets(String action) {
            return Collections.emptyList();
        }
    };
    
    private final Lookup lookup;
    private final Project prj;
    private PersistentSnippets delegate;
    
    private static final Map<Project, Reference<PersistentSnippets>> cache = new WeakHashMap<>();

    public PersistentSnippetsSupport(Lookup lookup, Project prj, PersistentSnippets delegate) {
        this.lookup = lookup;
        this.prj = prj;
        this.delegate = delegate;
    }
    
    private static StartupSnippetsTracker tracker;
    
    private static void initialize() {
        synchronized (PersistentSnippetsSupport.class) {
            if (tracker != null) {
                return;
            }
            tracker = new StartupSnippetsTracker();
        }
        ShellRegistry.get().addShellListener(tracker);
        Collection<JShellEnvironment> envs = ShellRegistry.get().openedShells(null);
        for (JShellEnvironment e : envs) {
            ShellStatus st = e.getStatus();
            switch (st) {
                case INIT:
                case STARTING:
                    e.addShellListener(tracker);
                    break;
                case READY:
                case EXECUTE:
                    runStartupSnippets(e);
                    break;
            }
        }
    }
    
    private static class StartupSnippetsTracker implements ShellListener {
        @Override
        public void shellCreated(ShellEvent ev) {
            // fired by the registry, hook on the shell:
            ev.getEnvironment().addShellListener(this);
        }

        @Override
        public void shellStarted(ShellEvent ev) {
            runStartupSnippets(ev.getEnvironment());
        }

        @Override
        public void shellStatusChanged(ShellEvent ev) {
        }

        @Override
        public void shellShutdown(ShellEvent ev) {
        }

        @Override
        public void shellSettingsChanged(ShellEvent ev) {
        }
    }
    
    private static void runStartupSnippets(JShellEnvironment env) {
        if (env == null || env.isClosed()) {
            return;
        }
        Lookup lkp = env.getLookup();
        PersistentSnippets supp = PersistentSnippetsSupport.create(lkp);
        if (supp == null) {
            return;
        }
        Collection<FileObject> snips = supp.startupSnippets(env.getMode());
        ShellSession s = env.getSession();
        if (s == null) {
            return;
        }
        for (FileObject sn : snips) {
            try {
                runScript(sn, s, true);
            } catch (IOException ex) {
                s.reportErrorMessage(ex);
                break;
            }
        }
    }
    
    private static Project findProject(Lookup context) {
        Project prj = context.lookup(Project.class);
        if (prj == null) {
            FileObject f = context.lookup(FileObject.class);
            if (f != null) {
                File cache = Places.getCacheDirectory();
                FileObject cacheFO = FileUtil.toFileObject(cache);
                prj = FileOwnerQuery.getOwner(f);
                if (cacheFO != null && prj != null) {
                    if (FileUtil.isParentOf(prj.getProjectDirectory(), cacheFO)) {
                        prj = null;
                    }
                }
            }
        }
        return prj;
    }
    
    private static void unregister(Project prj) {
        synchronized (cache) {
            cache.remove(prj);
        }
    }
    
    public static PersistentSnippets create(Lookup context) {
        initialize();
        Project prj = findProject(context);
        if (prj == null) {
            return GLOBAL;
        }
        PersistentSnippets snips = null;
        synchronized (cache) {
            Reference<PersistentSnippets> refS = cache.get(prj);
            if (refS != null) {
                snips = refS.get();
            }
            if (snips != null) {
                return snips;
            }
        }
        SnippetStorage storage = prj.getLookup().lookup(SnippetStorage.class);
        if (storage == null) {
            return GLOBAL;
        }
        FileObject projectStorageDir = storage.getStorageFolder(false);
        if (projectStorageDir == null) {
            return GLOBAL;
        }
        try {
            SnippetFileSystem snipFS = new SnippetFileSystem(
                    projectStorageDir, 
                    FileUtil.getConfigRoot(), 
                    storage.resourcePrefix(), 
                    "jshell-snippets"); // NOI18N
            SnippetsFolder fld = new ProjectSnippetsFolder(snipFS.getRoot(), snipFS, storage, prj);
            snips = fld;
        } catch (IOException ex) {
            // should not happen, but anyway
            return GLOBAL;
        }
        if (snips == null) {
            return GLOBAL;
        } else {
            synchronized (cache) {
                Reference<PersistentSnippets> ref = cache.get(prj);
                if (ref != null) {
                    PersistentSnippets existing = ref.get();
                    if (existing != null) {
                        return existing;
                    }
                }
                cache.put(prj, new WeakReference<>(snips));
            }
            return snips;
        }
    }
    
    private static final String RUN_METHOD = "run"; // NOI18N

    @NbBundle.Messages({
        "# {0} - source filename",
        "ERR_NoSourceFile=Could not build source for file {0}",
        "ERR_UnexpectedFielContent=File must contain exactly one toplevel class"
    })
    public static void runScript(FileObject scriptFile, ShellSession session, 
            boolean shared) throws IOException {
        JavaSource src = JavaSource.forFileObject(scriptFile);
        if (src == null) {
            throw new IOException(Bundle.ERR_NoSourceFile(scriptFile.getPath()));
        }
        final StringBuilder sb = new StringBuilder();
        src.runUserActionTask(new Task<CompilationController>() {
            CharSequence snapText;
            CompilationController ctrl;
            SourcePositions spos;

            private int printHeaderWithoutModifiers(int start, ModifiersTree mods) {
                int modsEnd = (int) spos.getEndPosition(ctrl.getCompilationUnit(), mods);
                int modsStart = (int) spos.getStartPosition(ctrl.getCompilationUnit(), mods);
                sb.append(snapText.subSequence(start, modsStart));
                for (AnnotationTree at : mods.getAnnotations()) {
                    int atStart = (int) spos.getStartPosition(ctrl.getCompilationUnit(), at);
                    int atEnd = (int) spos.getEndPosition(ctrl.getCompilationUnit(), at);
                    if (atStart != -1 && atEnd != -1) {
                        sb.append(snapText.subSequence(atStart, atEnd)).append("\n");
                    }
                }
                sb.append(" ");
                return modsEnd;
            }

            @Override
            public void run(CompilationController ctrl) throws Exception {
                this.ctrl = ctrl;
                ctrl.toPhase(JavaSource.Phase.PARSED);
                List<? extends Tree> classes = ctrl.getCompilationUnit().getTypeDecls();
                if (classes.isEmpty()) {
                    return;
                }
                CompilationUnitTree cut = ctrl.getCompilationUnit();
                if (classes.size() > 1) {
                    throw new IOException(Bundle.ERR_UnexpectedFielContent());
                }
                // first copy over all imports
                for (ImportTree imp : ctrl.getCompilationUnit().getImports()) {
                    sb.append(imp).append("\n"); // NOI18N
                }
                ClassTree ct = (ClassTree) classes.get(0);
                snapText = ctrl.getSnapshot().getText();
                spos = ctrl.getTrees().getSourcePositions();
                for (Tree m : ct.getMembers()) {
                    int start = (int) spos.getStartPosition(cut, m);
                    int end = (int) spos.getEndPosition(cut, m);
                    if (start == -1 || end == -1) {
                        continue;
                    }
                    switch (m.getKind()) {
                        case METHOD:
                            {
                                MethodTree mt = (MethodTree) m;
                                if (mt.getName().contentEquals(RUN_METHOD) && mt.getParameters().isEmpty()) {
                                    // NOI18N
                                    // add everything from the first to the last statement in the body:
                                    BlockTree bt = mt.getBody();
                                    if (bt != null && !bt.getStatements().isEmpty()) {
                                        Tree first = bt.getStatements().get(0);
                                        Tree last = bt.getStatements().get(bt.getStatements().size() - 1);
                                        start = (int) spos.getStartPosition(cut, first);
                                        end = (int) spos.getEndPosition(cut, last);
                                    } else {
                                        continue;
                                    }
                                } else if (mt.getModifiers().getFlags().contains(Modifier.STATIC)) {
                                    start = printHeaderWithoutModifiers(start, ((MethodTree) m).getModifiers());
                                }
                                break;
                            }
                        case CLASS:
                        case INTERFACE:
                        case ENUM:
                            start = printHeaderWithoutModifiers(start, ((ClassTree) m).getModifiers());
                            break;
                        case VARIABLE:
                            start = printHeaderWithoutModifiers(start, ((VariableTree) m).getModifiers());
                            break;
                    }
                    sb.append(snapText.subSequence(start, end));
                    sb.append("\n");
                }
            }
        }, true);
        String execCommands = sb.toString();
        session.clearInputAndEvaluateExternal(execCommands, scriptFile.getName());
    }
    
    static final class ProjectSnippetsFolder extends SnippetsFolder implements PropertyChangeListener  {
        final SnippetStorage projectStorage;
        final Project project;
        
        public ProjectSnippetsFolder(FileObject parentFolder, Callable<FileObject> creator, SnippetStorage projectStorage, Project project) {
            super(parentFolder, creator);
            this.projectStorage = projectStorage;
            this.project = project;
            OpenProjects.getDefault().addPropertyChangeListener(this);
        }

        @Override
        public Collection<FileObject> startupSnippets(String runAction) {
            String fld = projectStorage.startupSnippets(runAction);
            if (fld == null) {
                return GLOBAL.startupSnippets(runAction);
            } else {
                return getSavedClasses(fld);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
                return;
            }
            if (!OpenProjects.getDefault().isProjectOpen(project)) {
                unregister(project);
                OpenProjects.getDefault().removePropertyChangeListener(this);
            }
        }
    }

    private static final PersistentSnippets GLOBAL = new SnippetsFolder(FileUtil.getConfigRoot().getFileObject("jshell-snippets"));
}
