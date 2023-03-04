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
package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.modules.java.api.common.util.CommonModuleUtils;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.ChangeSupport;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Compiler options for unit tests in a Multi-Module project.
 * @author Tomas Zezula
 */
final class MultiModuleUnitTestsCompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation {
    private static final Logger LOG = Logger.getLogger(MultiModuleUnitTestsCompilerOptionsQueryImpl.class.getName());

    private final Project project;
    private final MultiModule sourceModules;
    private final MultiModule testModules;
    private final AtomicReference<Result> result;

    MultiModuleUnitTestsCompilerOptionsQueryImpl(
            @NonNull final Project project,
            @NonNull final MultiModule sourceModules,
            @NonNull final MultiModule testModules) {
        Parameters.notNull("project", project);   //NOI18N
        Parameters.notNull("sourceModules", sourceModules); //NOI18N
        Parameters.notNull("testModules", testModules);     //NOI18N
        this.project = project;
        this.sourceModules = sourceModules;
        this.testModules = testModules;
        this.result = new AtomicReference<>();
    }

    @CheckForNull
    @Override
    public Result getOptions(FileObject file) {
        Result res = null;
        if (testModules.getModuleName(file) != null) {
            res = result.get();
            if (res == null) {
                res = new ResultImpl(project, sourceModules, testModules);
                if (!result.compareAndSet(null, res)) {
                    res = result.get();
                }
            }
        }
        return res;
    }

    private static final class ResultImpl extends Result implements ChangeListener, PropertyChangeListener, FileChangeListener {
        private static final String MODULE_INFO = "module-info.java";   //NOI18N
        private final MultiModule sourceModules;
        private final MultiModule testModules;
        private final SourceLevelQuery.Result slRes;
        private final ChangeSupport listeners;
        //@GuardedBy("this")
        private List<String> cache;
        private final Collection</*@GuardedBy("this")*/Pair<ClassPath,PropertyChangeListener>> currentCps;
        private final Set</*@GuardedBy("this")*/File> currentModuleInfos;

        ResultImpl(
                @NonNull final Project project,
                @NonNull final MultiModule sourceModules,
                @NonNull final MultiModule testModules) {
            Parameters.notNull("project", project);     //NOI18N
            Parameters.notNull("sourceModules", sourceModules); //NOI18N
            Parameters.notNull("testModules", testModules);     //NOI18N
            this.sourceModules = sourceModules;
            this.testModules = testModules;
            this.slRes = SourceLevelQuery.getSourceLevel2(project.getProjectDirectory());
            this.listeners = new ChangeSupport(this);
            this.sourceModules.addPropertyChangeListener(WeakListeners.propertyChange(this, this.sourceModules));
            this.testModules.addPropertyChangeListener(WeakListeners.propertyChange(this, this.testModules));
            this.currentCps = new ArrayList<>();
            this.currentModuleInfos = new HashSet<>();
        }

        @Override
        public List<? extends String> getArguments() {
            List<String> res;
            synchronized (this) {
                res = cache;
            }
            if (res == null) {
                final List<String> options = new ArrayList<>();
                final Optional<SpecificationVersion> hasModules = Optional.ofNullable(slRes.getSourceLevel())
                        .map((v) -> new SpecificationVersion(v))
                        .filter((v) -> CommonModuleUtils.JDK9.compareTo(v) <= 0);
                final Set<File> moduleInfosToListenOn = new HashSet<>();
                final List<ClassPath> cpsToListenOn = new ArrayList<>();
                if (hasModules.isPresent()) {
                    final Collection<? extends String> allModules = testModules.getModuleNames();
                    final List<Map<String,List<File>>> modulesByType = classifyModules(
                            allModules,
                            moduleInfosToListenOn,
                            cpsToListenOn);
                    final Map<String,List<File>> realModules = modulesByType.get(0);
                    final Map<String,List<File>> modulePatches = modulesByType.get(1);
                    final Map<String,List<File>> invalidModules = modulesByType.get(2);
                    //--patch-module for module patches
                    modulePatches.entrySet().stream()
                            .forEach((e) -> {
                                final String m = e.getKey();
                                final List<File> path = e.getValue();
                                if (!path.isEmpty()) {
                                    options.add("--patch-module");     //NOI18N
                                    final String testSourcePath = path.stream()
                                            .map((f) -> f.getAbsolutePath())
                                            .collect(Collectors.joining(":"));  //NOI18N
                                    options.add(String.format("%s=%s", m, testSourcePath)); //NOI18N
                                }
                            });
                    //--add-modules - enable all source modules
                    final String modList = allModules.stream()
                            .sorted()
                            .collect(Collectors.joining(","));
                    if (!modList.isEmpty()) {
                        options.add("--add-modules");     //NOI18N
                        options.add(modList);
                    }
                    //--add-reads ALL-UNNAMED - test libraries readable
                    allModules.stream()
                            .filter((m) -> !invalidModules.containsKey(m))
                            .forEach((m) -> {
                                options.add("--add-reads");     //NOI18N
                                options.add(String.format("%s=ALL-UNNAMED", m)); //NOI18N
                            });
                }
                res = Collections.unmodifiableList(options);
                synchronized (this) {
                    for (Iterator<Pair<ClassPath, PropertyChangeListener>> it = currentCps.iterator(); it.hasNext();) {
                        Pair<ClassPath,PropertyChangeListener> p = it.next();
                        p.first().removePropertyChangeListener(p.second());
                        it.remove();
                    }
                    assert currentCps.isEmpty();
                    for (ClassPath cp : cpsToListenOn) {
                        final PropertyChangeListener pcl = WeakListeners.propertyChange(this, cp);
                        cp.addPropertyChangeListener(pcl);
                        currentCps.add(Pair.of(cp, pcl));
                    }
                    final Set<File> toRemove = new HashSet<>(currentModuleInfos);
                    toRemove.removeAll(moduleInfosToListenOn);
                    moduleInfosToListenOn.removeAll(currentModuleInfos);
                    for (File f : toRemove) {
                        safeRemoveFileListener(f, this);
                        currentModuleInfos.remove(f);
                    }
                    for (File f : moduleInfosToListenOn) {
                        safeAddFileListener(f, this);
                        currentModuleInfos.add(f);
                    }
                    if (cache == null) {
                        cache = res;
                    } else {
                        res = cache;
                    }
                }
            }
            return res;
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener listener) {
            this.listeners.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener listener) {
            this.listeners.removeChangeListener(listener);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            reset();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final Object source = evt.getSource();
            switch (evt.getPropertyName()) {
                case MultiModule.PROP_MODULES:
                    if (source == testModules || source == sourceModules) {
                        reset();
                    }
                    break;
                case ClassPath.PROP_ENTRIES:
                    if (isActiveClassPath(source)) {
                        reset();
                    }
                    break;
            }
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            reset();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            reset();
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            reset();
        }

        @Override
        public void fileChanged(FileEvent fe) {
            //Not important
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            //Not important
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            //Not important
        }

        private synchronized boolean isActiveClassPath (Object source) {
            for (Pair<ClassPath,PropertyChangeListener> p : currentCps) {
                if (p.first() == source) {
                    return true;
                }
            }
            return false;
        }

        private void reset() {
            synchronized (this) {
                cache = null;
            }
            listeners.fireChange();
        }

        private static void safeAddFileListener(
                @NonNull final File file,
                @NonNull final FileChangeListener listener) {
            try {
                FileUtil.addFileChangeListener(listener, file);
            } catch (IllegalArgumentException e) {
                LOG.log(
                        Level.WARNING,
                        "Cannot add listener to: {0}",      //NOI18N
                        file);
            }
        }

        private static void safeRemoveFileListener(
                @NonNull final File file,
                @NonNull final FileChangeListener listener) {
            try {
                FileUtil.removeFileChangeListener(listener, file);
            } catch (IllegalArgumentException e) {
                LOG.log(
                        Level.WARNING,
                        "Cannot remove listener from: {0}",     //NOI18N
                        file);
            }
        }

        private List<Map<String,List<File>>> classifyModules(
                @NonNull final Collection<? extends String> toClassify,
                @NonNull final Set<? super File> moduleInfosToListenOn,
                @NonNull final Collection<? super ClassPath> cpsToListenOn) {
            final Map<String,List<File>> mods = new HashMap<>();
            final Map<String,List<File>> ptchs = new HashMap<>();
            final Map<String,List<File>> invd = new HashMap<>();
            for (String modName : toClassify) {
                ClassPath cp = testModules.getModuleSources(modName);
                if (cp == null) {
                    invd.put(modName, Collections.emptyList());
                    continue;
                }
                final Map<String,List<File>> into;
                FileObject modInfo;
                if ((modInfo = cp.findResource(MODULE_INFO)) != null && modInfo.isData()) {
                    into = mods;
                } else if (sourceModules.getModuleNames().contains(modName)) {
                    into = ptchs;
                } else {
                    into = invd;
                }
                final List<File> files = cp.entries().stream()
                        .map((e) -> FileUtil.archiveOrDirForURL(e.getURL()))
                        .filter((f) -> f != null)
                        .collect(Collectors.toList());
                into.put(modName, files);
                files.stream()
                        .map((f) -> new File(f, MODULE_INFO))
                        .forEach(moduleInfosToListenOn::add);
                cpsToListenOn.add(cp);
            }
            return Arrays.asList(mods, ptchs, invd);
        }
    }

}
