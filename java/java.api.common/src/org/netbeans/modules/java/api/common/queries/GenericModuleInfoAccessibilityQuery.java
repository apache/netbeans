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
package org.netbeans.modules.java.api.common.queries;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.DirectiveTree;
import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.JavacTask;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.AccessibilityQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation2;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=AccessibilityQueryImplementation2.class)
public class GenericModuleInfoAccessibilityQuery implements AccessibilityQueryImplementation2 {

    private static final Logger LOG = Logger.getLogger(GenericModuleInfoAccessibilityQuery.class.getName());

    private final Map<ClassPath, Reference<ClassPathListener>> sourcePath2Listener = new WeakHashMap<>();
    private final Map<FileObject, Reference<Result>> path2Result = new HashMap<>();

    @Override
    public Result isPubliclyAccessible(FileObject folder) {
        Result result;
        synchronized (this) {
            Reference<Result> ref = path2Result.get(folder);
            result = ref != null ? ref.get() : null;
            if (result != null) {
                return result;
            }
        }

        Project p = FileOwnerQuery.getOwner(folder);
        if (p != null && (p.getLookup().lookup(AccessibilityQueryImplementation2.class) != null ||
                          p.getLookup().lookup(AccessibilityQueryImplementation.class) != null)) {
            //if there's a project-based AccessibilityQuery for this file, don't provide the generic results
            return null;
        }
        ClassPath sourcePath = ClassPath.getClassPath(folder, ClassPath.SOURCE);
        if (sourcePath == null) {
            return null;
        }

        synchronized (this) {
            Reference<Result> ref = path2Result.get(folder);

            result = ref != null ? ref.get() : null;

            if (result != null) {
                return result;
            }

            Reference<ClassPathListener> listenerRef = sourcePath2Listener.get(sourcePath);
            ClassPathListener cpl = listenerRef != null ? listenerRef.get() : null;

            if (cpl == null) {
                cpl = new ClassPathListener(sourcePath);
            }

            sourcePath2Listener.put(sourcePath, new WeakReference<>(cpl));

            result = new ResultImpl(cpl, sourcePath, folder);

            path2Result.put(folder, new CleanPath2Result(result, folder));

            return result;
        }
    }

    private final class CleanPath2Result extends WeakReference<Result> implements Runnable {

        private final FileObject key;

        public CleanPath2Result(Result value, FileObject key) {
            super(value, Utilities.activeReferenceQueue());
            this.key = key;
        }

        @Override
        public void run() {
            synchronized (GenericModuleInfoAccessibilityQuery.this) {
                path2Result.remove(key);
            }
        }

    }

    private static final class ResultImpl implements Result, ChangeListener {

        private final ChangeSupport cs = new ChangeSupport(this);
        private final ClassPathListener listener;
        private final Reference<ClassPath> sourcePath;
        private final FileObject folder;

        public ResultImpl(ClassPathListener listener, ClassPath sourcePath, FileObject folder) {
            this.listener = listener;
            this.sourcePath = new WeakReference<>(sourcePath);
            this.folder = folder;
            listener.addChangeListener(this);
        }

        @Override
        public AccessibilityQuery.Accessibility getAccessibility() {
            ClassPath sourcePath = this.sourcePath.get();
            Set<String> exported = listener.getExportedPackages();

            if (sourcePath == null || folder == null || exported == null) {
                return AccessibilityQuery.Accessibility.UNKNOWN;
            }
            String packageName = sourcePath.getResourceName(folder).replace('/', '.');
            return exported.contains(packageName) ? AccessibilityQuery.Accessibility.EXPORTED
                                                  : AccessibilityQuery.Accessibility.PRIVATE;
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            cs.removeChangeListener(listener);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            cs.fireChange();
        }

    }

    private static final class ClassPathListener implements PropertyChangeListener {

        private static final RequestProcessor WORKER = new RequestProcessor(ClassPathListener.class.getName(), 1, false, false);
        private static final int DELAY = 100;
        private final ChangeSupport cs = new ChangeSupport(this);
        private final AtomicReference<Set<String>> exportedPackages = new AtomicReference<>(null);
        private final Reference<ClassPath> sourcePath;
        private final Task parseTask;
        private final Task rootsTask;
        private final FileChangeAdapter folderListener = new FileChangeAdapter() {
            @Override
            public void fileDataCreated(FileEvent fe) {
                if (fe.getFile().getNameExt().equalsIgnoreCase("module-info.java")) {
                    rootsTask.schedule(DELAY);
                }
            }
        };
        private final FileChangeAdapter moduleInfoListener = new FileChangeAdapter() {
            @Override
            public void fileChanged(FileEvent fe) {
                parseTask.schedule(DELAY);
            }
        };

        private Set<FileObject> oldRoots = new HashSet<>();
        private Set<FileObject> oldModuleInfos = new HashSet<>();

        public ClassPathListener(ClassPath sourcePath) {
            this.sourcePath = new WeakReference<>(sourcePath);
            this.parseTask = WORKER.create(() -> {
                FileObject moduleInfo = sourcePath.findResource("module-info.java");
                Set<String> exported;

                if (moduleInfo != null) {
                    exported = new HashSet<>();

                    try {
                        String code = moduleInfo.asText();
                        JavacTask compilerTask = (JavacTask) ToolProvider.getSystemJavaCompiler().getTask(null, null, null, null, null, Collections.singleton(new TextJFO(code, moduleInfo.toURI())));
                        CompilationUnitTree cut = compilerTask.parse().iterator().next();
                        ModuleTree mt = cut.getModule();
                        if (mt != null) {
                            for (DirectiveTree dt : mt.getDirectives()) {
                                if (dt.getKind() == Kind.EXPORTS) {
                                    ExportsTree et = (ExportsTree) dt;
                                    if (et.getModuleNames() == null || et.getModuleNames().isEmpty()) {
                                        exported.add(et.getPackageName().toString());
                                    }
                                }
                            }
                        }
                    } catch (IOException ex) {
                        LOG.log(Level.FINE, null, ex);
                    }
                } else {
                    exported = null;
                }

                exportedPackages.set(exported);
                cs.fireChange();
            });
            rootsTask = WORKER.create(() -> {
                ClassPath cp = ClassPathListener.this.sourcePath.get();

                if (cp == null) {
                    return ;
                }

                Set<FileObject> removedRoots = new HashSet<>(oldRoots);
                Set<FileObject> removedModuleInfos = new HashSet<>(oldModuleInfos);

                for (FileObject root : cp.getRoots()) {
                    removedRoots.remove(root);
                    if (oldRoots.add(root)) {
                        root.addFileChangeListener(folderListener);
                    }
                    FileObject moduleInfo = root.getFileObject("module-info.java");
                    if (moduleInfo != null) {
                        removedModuleInfos.remove(moduleInfo);
                        if (oldModuleInfos.add(moduleInfo)) {
                            moduleInfo.addFileChangeListener(moduleInfoListener);
                        }
                    }
                }

                for (FileObject root : removedRoots) {
                    root.removeFileChangeListener(folderListener);
                }

                for (FileObject moduleInfo : removedModuleInfos) {
                    moduleInfo.removeFileChangeListener(moduleInfoListener);
                }

                parseTask.schedule(DELAY);
            });
            rootsTask.schedule(DELAY);
            sourcePath.addPropertyChangeListener(this);
        }

        public Set<String> getExportedPackages() {
            return exportedPackages.get();
        }

        public void addChangeListener(ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            cs.removeChangeListener(listener);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            rootsTask.schedule(DELAY);
        }

    }
    private static final class TextJFO extends SimpleJavaFileObject {
        private final String code;

        public TextJFO(String code, URI uri) {
            super(uri, Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return code;
        }

    }
}
