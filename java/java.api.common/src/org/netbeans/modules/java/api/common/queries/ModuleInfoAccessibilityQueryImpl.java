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
import com.sun.source.tree.ModuleTree;
import com.sun.source.util.TreePath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ModuleElement;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.AccessibilityQuery;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.spi.java.queries.AccessibilityQueryImplementation2;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * An implementation of the {@link AccessibilityQueryImplementation2} based on the module-info.
 * Accessible through the {@link QuerySupport#createModuleInfoAccessibilityQuery}.
 * @author Tomas Zezula
 */
final class ModuleInfoAccessibilityQueryImpl implements AccessibilityQueryImplementation2, PropertyChangeListener, FileChangeListener {
    private static final String MODULE_INFO_JAVA = "module-info.java";  //NOI18N

    private final SourceRoots sourceModules;
    private final SourceRoots sources;
    private final SourceRoots testModules;
    private final SourceRoots tests;
    private final ChangeSupport listeners;
    private final Set</*@GuardedBy("this")*/File> moduleInfoListeners;
    //@GuardedBy("this")
    private ExportsCache exportsCache;
    //@GuardedBy("this")
    private boolean listensOnRoots;

    ModuleInfoAccessibilityQueryImpl(
            @NullAllowed final SourceRoots sourceModules,
            @NonNull final SourceRoots sources,
            @NullAllowed final SourceRoots testModules,
            @NonNull final SourceRoots tests) {
        Parameters.notNull("sources", sources);     //NOI18N
        Parameters.notNull("tests", tests);         //NOI18N
        this.sourceModules = sourceModules;
        this.sources = sources;
        this.testModules = testModules;
        this.tests = tests;
        this.moduleInfoListeners = new HashSet<>();
        this.listeners = new ChangeSupport(this);
    }


    @CheckForNull
    @Override
    @SuppressWarnings("NP_BOOLEAN_RETURN_NULL")
    public AccessibilityQueryImplementation2.Result isPubliclyAccessible(FileObject pkg) {
        final ExportsCache cache = getCache();
        if (!cache.isKnown(pkg)) {
            return null;
        }
        return new ResultImpl(pkg, this);
    }

    @Override
    public void propertyChange(@NonNull final PropertyChangeEvent evt) {
        final String propName = evt.getPropertyName();
        if (SourceRoots.PROP_ROOTS.equals(propName)) {
            reset();
        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        reset();
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        reset();
    }

    @Override
    public void fileChanged(FileEvent fe) {
        reset();
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        reset();
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        //Not important
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        //Not important
    }

    private void addChangeListener(@NonNull final ChangeListener listener) {
        this.listeners.addChangeListener(listener);
    }

    private void removeChangeListener(@NonNull final ChangeListener listener) {
        this.listeners.removeChangeListener(listener);
    }

    private void reset() {
        synchronized (this) {
            exportsCache = null;
        }
        listeners.fireChange();
    }

    @NonNull
    private ExportsCache getCache() {
        ExportsCache ec;
        synchronized (this) {
            ec = exportsCache;
        }
        if (ec == null) {
            final Set<FileObject> rootsCollector = new HashSet<>();
            final List<Pair<Set<FileObject>,Set<FileObject>>> data = new ArrayList<>(2);
            final Queue<FileObject[]> todo = new ArrayDeque<>();
            if (sourceModules != null) {
                todo.addAll(collectModuleRoots(sourceModules, sources));
            } else {
                todo.offer(sources.getRoots());
            }
            if (testModules != null) {
                todo.addAll(collectModuleRoots(testModules, tests));
            } else {
                todo.offer(tests.getRoots());
            }
            for (FileObject[] work : todo) {
                Collections.addAll(rootsCollector, work);
                extractExports(work).ifPresent(data::add);
            }

            ec = new ExportsCache(rootsCollector, data);
            synchronized (this) {
                if (exportsCache == null) {
                    exportsCache = ec;
                } else {
                    ec = exportsCache;
                }
                if (!listensOnRoots) {
                    listensOnRoots = true;
                    if (sourceModules != null) {
                        sourceModules.addPropertyChangeListener(WeakListeners.propertyChange(this, sourceModules));
                    }
                    if (testModules != null) {
                        testModules.addPropertyChangeListener(WeakListeners.propertyChange(this, testModules));
                    }
                    sources.addPropertyChangeListener(WeakListeners.propertyChange(this, sources));
                    tests.addPropertyChangeListener(WeakListeners.propertyChange(this, tests));
                }
                final Set<File> allRoots = rootsCollector.stream()
                        .map((fo) -> FileUtil.toFile(fo))
                        .filter((f) -> f != null)
                        .collect(Collectors.toSet());
                final Set<File> toRemove = new HashSet<>(moduleInfoListeners);
                toRemove.removeAll(allRoots);
                allRoots.removeAll(moduleInfoListeners);
                for (File f : toRemove) {
                    FileUtil.removeFileChangeListener(
                            this,
                            new File(f, MODULE_INFO_JAVA));
                    moduleInfoListeners.remove(f);
                }
                for (File f : allRoots) {
                    FileUtil.addFileChangeListener(
                            this,
                            new File(f, MODULE_INFO_JAVA));
                    moduleInfoListeners.add(f);
                }
            }
        }
        return ec;
    }

    @NonNull
    private Collection<FileObject[]> collectModuleRoots(
            @NonNull final SourceRoots mods,
            @NonNull final SourceRoots src) {
        final Collection<FileObject[]> res = new ArrayDeque<>();
        final MultiModule model = MultiModule.getOrCreate(mods, src);
        for (String modName : model.getModuleNames()) {
            final ClassPath cp = model.getModuleSources(modName);
            if (cp != null) {
                res.add(cp.getRoots());
            }
        }
        return res;
    }

    @NonNull
    private static Optional<Pair<Set<FileObject>, Set<FileObject>>> extractExports(
            @NonNull final FileObject[] roots) {
        return Arrays.stream(roots)
                .map(root -> root.getFileObject(MODULE_INFO_JAVA))
                .filter(Objects::nonNull)
                .findFirst()
                .map(mi -> {
                    Set<FileObject> rootsSet = new HashSet<>();
                    Collections.addAll(rootsSet, roots);
                    Set<FileObject> exportsSet = readExports(mi, rootsSet);
                    return Pair.of(rootsSet, exportsSet);
                });
    }

    @NonNull
    private static Set<FileObject> readExports(
            @NonNull final FileObject moduleInfo,
            @NonNull final Set<FileObject> roots) {
        final Set<FileObject> exports = new HashSet<>();
        final JavaSource src = JavaSource.forFileObject(moduleInfo);
        if (src != null) {
            try {
                src.runUserActionTask((cc) -> {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cu = cc.getCompilationUnit();
                    ModuleTree mt = cu.getModule();
                    if (mt != null) {
                        TreePath path = TreePath.getPath(cu, mt);
                        Element element = cc.getTrees().getElement(path);
                        if (element.getKind() == ElementKind.MODULE) {
                            ModuleElement me = (ModuleElement) element;
                            for (ModuleElement.Directive directive : me.getDirectives()) {
                                if (directive.getKind() == ModuleElement.DirectiveKind.EXPORTS) {
                                    ModuleElement.ExportsDirective export = (ModuleElement.ExportsDirective) directive;
                                    String pkgName = export.getPackage().getQualifiedName().toString();
                                    exports.addAll(findPackage(pkgName, roots));
                                }
                            }
                        }
                    }
                }, true);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return exports;
    }

    @NonNull
    private static Set<FileObject> findPackage(
            @NonNull final String pkgName,
            @NonNull final Collection<? extends FileObject> roots) {
        final String path = pkgName.replace('.', '/');  //NOI18N
        final Set<FileObject> res = new HashSet<>();
        for (FileObject root : roots) {
            final FileObject pkg = root.getFileObject(path);
            if (pkg != null) {
                res.add(pkg);
            }
        }
        return res;
    }

    private static final class ExportsCache {
        private final Set<FileObject> roots;    //All roots
        private final List<Pair<
                Set<FileObject>,    //Roots in compilation unit with module-info
                Set<FileObject>>>   //Exported packages
                    data;

        ExportsCache(
                @NonNull final Set<FileObject> roots,
                @NonNull final List<Pair<Set<FileObject>,Set<FileObject>>> data) {
            this.roots = roots;
            this.data = data;
        }

        boolean isKnown(@NonNull final FileObject pkg) {
            return roots.stream()
                    .anyMatch((root) -> {
                        return root.equals(pkg) || FileUtil.isParentOf(root, pkg);
                    });
        }

        boolean isInModule(@NonNull final FileObject pkg) {
            return data.stream()
                    .flatMap((p) -> p.first().stream())
                    .anyMatch((root) -> {
                        return root.equals(pkg) || FileUtil.isParentOf(root, pkg);
                });
        }

        boolean isExported(@NonNull final FileObject pkg) {
            return data.stream()
                    .flatMap((p) -> p.second().stream())
                    .anyMatch((exported) -> exported.equals(pkg));
        }
    }

    private static class ResultImpl implements AccessibilityQueryImplementation2.Result {
        private final FileObject pkg;
        private final ModuleInfoAccessibilityQueryImpl owner;

        public ResultImpl(
                @NonNull final FileObject pkg,
                @NonNull final ModuleInfoAccessibilityQueryImpl owner) {
            this.pkg = pkg;
            this.owner = owner;
        }

        @Override
        public AccessibilityQuery.Accessibility getAccessibility() {
            final ExportsCache cache = owner.getCache();
            if (!cache.isInModule(pkg)) {
                return AccessibilityQuery.Accessibility.UNKNOWN;
            }
            if (cache.isExported(pkg)) {
                return AccessibilityQuery.Accessibility.EXPORTED;
            }
            return AccessibilityQuery.Accessibility.PRIVATE;
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener listener) {
            owner.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener listener) {
            owner.removeChangeListener(listener);
        }
    }
}
