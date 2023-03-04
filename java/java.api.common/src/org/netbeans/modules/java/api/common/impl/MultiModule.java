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
package org.netbeans.modules.java.api.common.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * A source path model for multi-module project.
 * @author Tomas Zezula
 */
public final class MultiModule implements PropertyChangeListener {

    public static final String PROP_MODULES = "modules";    //NOI18N

    //@GuardedBy("knownRoots")
    private static final List<Entry> knownRoots = new LinkedList<>();

    private final SourceRoots moduleRoots;
    private final SourceRoots srcRoots;
    private final AtomicReference<Map<String,Pair<ClassPath,CPImpl>>> cpCache;
    private final AtomicReference<ClassPath> mpCache;
    private final PropertyChangeSupport listeners;

    private MultiModule(
            @NonNull final SourceRoots moduleRoots,
            @NonNull final SourceRoots srcRoots) {
        Parameters.notNull("moduleRoots", moduleRoots); //NOI18N
        Parameters.notNull("srcRoots", srcRoots);       //NOI18N
        this.moduleRoots = moduleRoots;
        this.srcRoots = srcRoots;
        this.cpCache = new AtomicReference<>(Collections.emptyMap());
        this.mpCache = new AtomicReference<>();
        this.listeners = new PropertyChangeSupport(this);
        this.moduleRoots.addPropertyChangeListener(WeakListeners.propertyChange(this, this.moduleRoots));
        this.srcRoots.addPropertyChangeListener(WeakListeners.propertyChange(this, this.srcRoots));
        updateCache();
    }

    @NonNull
    public Collection<? extends String> getModuleNames() {
        return Collections.unmodifiableSet(getCache().keySet());
    }

    @CheckForNull
    public String getModuleName(@NonNull final FileObject artifact) {
        return findImpl(artifact)
                .map((p) -> p.first())
                .orElse(null);
    }

    @CheckForNull
    public ClassPath getModuleSources(@NonNull final String moduleName) {
        return Optional.ofNullable(getCache().get(moduleName))
                .map((p) -> p.first())
                .orElse(null);
    }

    @CheckForNull
    public ClassPath getModuleSources(@NonNull final FileObject artifact) {
        return findImpl(artifact)
                .map((p) -> p.second())
                .orElse(null);
    }

    @NonNull
    public ClassPath getSourceModulePath() {
        ClassPath mp = mpCache.get();
        if (mp == null) {
            mp = ClassPathFactory.createClassPath(new MPImpl(moduleRoots));
            if (!mpCache.compareAndSet(null, mp)) {
                mp = mpCache.get();
            }
        }
        return mp;
    }

    public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        this.listeners.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        this.listeners.removePropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final String propName = evt.getPropertyName();
        if (SourceRoots.PROP_ROOTS.equals(propName)) {
            ProjectManager.mutex().postReadRequest(()->updateCache());
        }
    }

    //Todo: Make me faster, trie or at least Map<URL,CP>.
    @NonNull
    private Optional<Pair<String,ClassPath>> findImpl(@NonNull final FileObject artifact) {
        final Map<String,Pair<ClassPath,CPImpl>> data = getCache();
        final URL artifactURL = artifact.toURL();
        for (Map.Entry<String,Pair<ClassPath,CPImpl>> e : data.entrySet()) {
            final ClassPath cp = e.getValue().first();
            for (ClassPath.Entry cpe : cp.entries()) {
                if (Utilities.isParentOf(cpe.getURL(), artifactURL)) {
                    return Optional.of(Pair.of(e.getKey(), cp));
                }
            }
        }
        return Optional.empty();
    }

    @NonNull
    private Map<String,Pair<ClassPath,CPImpl>> getCache() {
        return cpCache.get();
    }

    private void updateCache() {
        final Map<String,Pair<ClassPath,CPImpl>> prev = cpCache.get();
        final Map<String,Collection<URL>> modulesByName = new HashMap<>();
        for (FileObject moduleRoot : moduleRoots.getRoots()) {
            collectModuleRoots(moduleRoot, srcRoots.getRootURLs(), modulesByName);
        }
        final Set<String> toKeep = new HashSet<>(prev.keySet());
        final Set<String> toRemove = new HashSet<>(prev.keySet());
        final Set<String> toAdd = new HashSet<>(modulesByName.keySet());
        boolean fire = toKeep.retainAll(modulesByName.keySet());
        toRemove.removeAll(modulesByName.keySet());
        toAdd.removeAll(prev.keySet());
        fire |= !toAdd.isEmpty();

        final Map<String,Pair<ClassPath,CPImpl>> next = new HashMap<>();
        for (String modName : toKeep) {
            next.put(modName, prev.get(modName));
        }
        for (String modName : toAdd) {
            final CPImpl impl = new CPImpl(modulesByName.get(modName));
            final ClassPath cp = ClassPathFactory.createClassPath(impl);
            next.put(
                    modName,
                    Pair.of(cp,impl));
        }
        if (cpCache.compareAndSet(prev, next)) {
            if (fire) {
                this.listeners.firePropertyChange(PROP_MODULES, prev.keySet(), next.keySet());
            }
            for (String modName : toKeep) {
                next.get(modName).second().update(modulesByName.get(modName));
            }
            for (String modName : toRemove) {
                prev.get(modName).second().update(Collections.emptyList());
            }
        }
    }

    private static void collectModuleRoots(
            @NonNull final FileObject moduleRoot,
            @NonNull final URL[] srcRoots,
            @NonNull final Map<String,Collection<URL>> collector) {
        Arrays.stream(moduleRoot.getChildren())
                .filter((fo) -> fo.isFolder() && !fo.getName().startsWith(".")) //NOI18N
                .forEach((module) -> {
                    final URL moduleUrl = module.toURL();
                    final String moduleName = module.getNameExt();
                    for (URL src : srcRoots) {
                        if (Utilities.isParentOf(moduleUrl, src)) {
                            Collection<URL> roots = collector.get(moduleName);
                            if (roots == null) {
                                roots = new ArrayList<>();
                                collector.put(moduleName, roots);
                            }
                            roots.add(src);
                        }
                    }
                });
    }

    @NonNull
    public static MultiModule create(
            @NonNull final SourceRoots moduleRoots,
            @NonNull final SourceRoots srcRoots) {
        return new MultiModule(moduleRoots, srcRoots);
    }

    @NonNull
    public static MultiModule getOrCreate(
            @NonNull final SourceRoots moduleRoots,
            @NonNull final SourceRoots srcRoots) {
        Parameters.notNull("moduleRoots", moduleRoots); //NOI18N
        Parameters.notNull("srcRoots", srcRoots);       //NOI18N
        synchronized(knownRoots) {
            for (Iterator<Entry> it = knownRoots.iterator(); it.hasNext();) {
                try {
                    final Entry e = it.next();
                    if (e.matches(moduleRoots, srcRoots)) {
                        return e.getModel();
                    }
                } catch (NoSuchElementException r) {
                    it.remove();
                }
            }
            final MultiModule model = create(moduleRoots, srcRoots);
            final Entry e = new Entry(
                    moduleRoots,
                    srcRoots,
                    model);
            knownRoots.add(e);
            return model;
        }
    }

    private static final class Entry {
        private static final NoSuchElementException REMOVE =
                new NoSuchElementException(){
                    @Override
                    public synchronized Throwable fillInStackTrace() {
                        return this;
                    }
         };
        private final Reference<SourceRoots> modules;
        private final Reference<SourceRoots> sources;
        private final Reference<MultiModule> model;

        Entry(
                @NonNull final SourceRoots modules,
                @NonNull final SourceRoots sources,
                @NonNull final MultiModule model) {
            Parameters.notNull("modules", modules); //NOI18N
            Parameters.notNull("sources", sources); //NOI18N
            Parameters.notNull("model", model);     //NOI18N
            this.modules = new WeakReference<>(modules);
            this.sources = new WeakReference<>(sources);
            this.model = new WeakReference<>(model);
        }

        boolean matches(
                @NullAllowed final SourceRoots modules,
                @NullAllowed final SourceRoots sources) {
            final SourceRoots myModules = this.modules.get();
            if (myModules == null) {
                throw REMOVE;
            }
            if (myModules != modules) {
                return false;
            }
            final SourceRoots mySources = this.sources.get();
            if (mySources == null) {
                throw REMOVE;
            }
            return mySources == sources;
        }

        @NonNull
        MultiModule getModel() {
            final MultiModule res = this.model.get();
            if (res == null) {
                throw REMOVE;
            }
            return res;
        }
    }

    private static final class CPImpl implements ClassPathImplementation {
        private final PropertyChangeSupport listeners;
        private final AtomicReference<List<? extends PathResourceImplementation>> cache;

        CPImpl(final Collection<? extends URL> roots) {
            this.cache = new AtomicReference<>(Collections.unmodifiableList(
                    roots.stream()
                            .map((root) -> ClassPathSupport.createResource(root))
                            .collect(Collectors.toList())));
            this.listeners = new PropertyChangeSupport(this);
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            return cache.get();
        }

        @Override
        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            this.listeners.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            this.listeners.removePropertyChangeListener(listener);
        }

        void update(@NonNull final Collection<? extends URL> update) {
            final List<? extends PathResourceImplementation> current = cache.get();
            final List<PathResourceImplementation> next = new ArrayList<>();
            final Set<URL> updateSet = new LinkedHashSet<>(update);
            boolean dirty = false;
            for (PathResourceImplementation pr : current) {
                final URL[] roots = pr.getRoots();
                assert roots.length == 1;
                if (updateSet.remove(roots[0])) {
                    next.add(pr);
                } else {
                    dirty = true;
                }
            }
            for (URL newRoot : updateSet) {
                next.add(ClassPathSupport.createResource(newRoot));
                dirty = true;
            }
            if (dirty && cache.compareAndSet(current, next)) {
                this.listeners.firePropertyChange(PROP_RESOURCES, null, null);
            }
        }
    }

    private static final class MPImpl implements ClassPathImplementation, PropertyChangeListener {
        private final SourceRoots roots;
        private final PropertyChangeSupport listeners;
        private final AtomicReference<List<PathResourceImplementation>> cache;

        MPImpl(@NonNull final SourceRoots roots) {
            this.roots = roots;
            this.listeners = new PropertyChangeSupport(this);
            this.cache = new AtomicReference<>();
            this.roots.addPropertyChangeListener(WeakListeners.propertyChange(this, this.roots));
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            List<PathResourceImplementation> res = cache.get();
            if (res == null) {
                res = Collections.unmodifiableList(Arrays.stream(roots.getRootURLs())
                        .map((url) -> ClassPathSupport.createResource(url))
                        .collect(Collectors.toList()));
                cache.set(res);
            }
            return res;
        }

        @Override
        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            this.listeners.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            this.listeners.removePropertyChangeListener(listener);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (SourceRoots.PROP_ROOTS.equals(evt.getPropertyName())) {
                cache.set(null);
                listeners.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
            }
        }
    }
}
