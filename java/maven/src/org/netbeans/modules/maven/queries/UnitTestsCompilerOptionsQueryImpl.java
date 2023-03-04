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
package org.netbeans.modules.maven.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import static org.netbeans.modules.maven.classpath.ClassPathProviderImpl.MODULE_INFO_JAVA;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.ChangeSupport;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Implementation of the {@link CompilerOptionsQueryImplementation} for unit tests.
 * @author Tomas Zezula
 * @author Tomas Stupka
 * @see org.netbeans.modules.java.api.common.queries.UnitTestsCompilerOptionsQueryImpl
 */
public final class UnitTestsCompilerOptionsQueryImpl implements CompilerOptionsQueryImplementation {
    private static final SpecificationVersion JDK9 = new SpecificationVersion("9"); //NOI18N

    private final AtomicReference<ResultImpl> result;
    private final NbMavenProjectImpl proj;

    public UnitTestsCompilerOptionsQueryImpl(
            NbMavenProjectImpl proj) {
        Parameters.notNull("proj", proj);   //NOI18N
        this.proj = proj;
        this.result = new AtomicReference<>();
    }

    @CheckForNull
    @Override
    public Result getOptions(@NonNull final FileObject file) {
        for (String r : proj.getOriginalMavenProject().getTestCompileSourceRoots()) {
            FileObject root = FileUtil.toFileObject(new File(r));
            if (root != null && isArtifact(root, file)) {
                ResultImpl res = result.get();
                if (res == null) {
                    res = new ResultImpl(proj);
                    if (!result.compareAndSet(null, res)) {
                        res = result.get();
                    }
                    assert res != null;
                }
                return res;
            }
        }
        return null;
    }

    private static boolean isArtifact(
            @NonNull final FileObject root,
            @NonNull final FileObject file) {
        return root.equals(file) || FileUtil.isParentOf(root, file);
    }

    private static final class ResultImpl extends Result implements ChangeListener,
            PropertyChangeListener, FileChangeListener {
        private final ChangeSupport cs;
        private final ThreadLocal<Boolean> reenter;
        private final Collection</*@GuardedBy("this")*/File> moduleInfoListeners ;
        //@GuardedBy("this")
        private List<String> cache;
        //@GuardedBy("this")
        private SourceLevelQuery.Result sourceLevel;
        //@GuardedBy("this")
        private boolean listensOnRoots;
        private final NbMavenProjectImpl proj;

        ResultImpl(NbMavenProjectImpl proj) {
            this.proj = proj;
            this.cs = new ChangeSupport(this);
            this.reenter = new ThreadLocal<>();
            this.moduleInfoListeners = new HashSet<>();
        }

        @Override
        public List<? extends String> getArguments() {
            List<String> args;
            SourceLevelQuery.Result[] slq = new SourceLevelQuery.Result[1];
            synchronized (this) {
                args = cache;
                slq[0] = sourceLevel;
            }
            if (args == null) {
                if (reenter.get() == Boolean.TRUE) {
                    args = Collections.emptyList();
                } else {
                    reenter.set(Boolean.TRUE);
                    try {
                        TestMode mode;
                        final Collection<File> allRoots = new HashSet<>();
                        final FileObject srcModuleInfo = findModuleInfo(proj.getOriginalMavenProject().getCompileSourceRoots(), allRoots, null);
                        final FileObject testModuleInfo = findModuleInfo(proj.getOriginalMavenProject().getTestCompileSourceRoots(), allRoots, slq);
                        final boolean isLegacy = Optional.ofNullable(slq[0])
                            .map((r) -> r.getSourceLevel())
                            .map((sl) -> JDK9.compareTo(new SpecificationVersion(sl)) > 0)
                            .orElse(Boolean.TRUE);
                        mode = isLegacy ?
                            TestMode.LEGACY :
                            srcModuleInfo == null ?
                                TestMode.UNNAMED :
                                testModuleInfo == null ?
                                    TestMode.INLINED:
                                    TestMode.MODULE;
                        args = mode.createArguments(proj, srcModuleInfo, testModuleInfo);
                        synchronized (this) {
                            if (cache == null) {
                                cache = args;
                            } else {
                                args = cache;
                            }
                            if (sourceLevel == null && slq[0] != null) {
                                sourceLevel = slq[0];
                                if (sourceLevel.supportsChanges()) {
                                    sourceLevel.addChangeListener(WeakListeners.change(this, sourceLevel));
                                }
                            }
                            if (!listensOnRoots) {
                                listensOnRoots = true;                                
                                NbMavenProject watcher = proj.getLookup().lookup(NbMavenProject.class);
                                watcher.addPropertyChangeListener(WeakListeners.propertyChange(this, watcher));
                            }
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
                    } finally {
                        reenter.remove();
                    }
                }
            }
            return args;
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener listener) {
            cs.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener listener) {
            cs.removeChangeListener(listener);
        }

        @Override
        public void stateChanged(@NonNull final ChangeEvent e) {
            reset();
        }

        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent evt) {
            if (NbMavenProject.PROP_PROJECT.equals(evt.getPropertyName())) {
                reset();
            }
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
        public void fileRenamed(FileRenameEvent fe) {
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

        private void reset() {
            synchronized (this) {
                cache = null;
            }
            cs.fireChange();
        }

        private FileObject findModuleInfo( 
                List<String> roots,
                @NonNull final Collection<? super File> rootCollector,
                @NullAllowed final SourceLevelQuery.Result[] holder) {
            
            FileObject result = null;
            for (String rootPath : roots) {
                File rootFile = new File(rootPath);
                FileObject rootFileObject = FileUtil.toFileObject(rootFile);
                if(rootFile.exists() && rootFileObject != null) {
                    if (holder != null) {
                        if (holder[0] == null) {
                            holder[0] = SourceLevelQuery.getSourceLevel2(rootFileObject);
                        }
                    }                    
                    rootCollector.add(rootFile);
                    if (result == null) {
                        final FileObject moduleInfo = rootFileObject.getFileObject(MODULE_INFO_JAVA);
                        if (moduleInfo != null) {
                            result = moduleInfo;
                        }
                    }
                }
            }
            return result;
        }
        
        @CheckForNull
        private static String getModuleName(@NonNull final FileObject moduleInfo) {
            return SourceUtils.parseModuleName(moduleInfo);
        }
    
        private static enum TestMode {
            /**
             * Tests for pre JDK9 sources.
             */
            LEGACY {
                @Override
                List<String> createArguments(NbMavenProjectImpl project,
                        @NullAllowed final FileObject srcModuleInfo,
                        @NullAllowed final FileObject testModuleInfo) {
                    return Collections.emptyList();
                }
            },
            /**
             * Tests for an unnamed module.
             */
            UNNAMED {
                @Override
                List<String> createArguments(NbMavenProjectImpl project,
                        @NullAllowed final FileObject srcModuleInfo,
                        @NullAllowed final FileObject testModuleInfo) {
                    return Collections.emptyList();
                }
            },
            /**
             * Tests inlined into names module in sources.
             */
            INLINED {
                @Override
                List<String> createArguments(NbMavenProjectImpl project,
                        @NullAllowed final FileObject srcModuleInfo,
                        @NullAllowed final FileObject testModuleInfo) {
                    final String moduleName = getModuleName(srcModuleInfo);
                    if (moduleName == null) {
                        return Collections.emptyList();
                    }
                    final List<String> result = Arrays.asList(
                        String.format("-XD-Xmodule:%s", moduleName),       //NOI18N
                        "--add-reads",                                  //NOI18N
                        String.format("%s=ALL-UNNAMED", moduleName));   //NOI18N
                    return Collections.unmodifiableList(result);
                }
            },
            /**
             * Tests have its own module.
             */
            MODULE {
                @Override
                List<String> createArguments(NbMavenProjectImpl project,
                        @NullAllowed final FileObject srcModuleInfo,
                        @NullAllowed final FileObject testModuleInfo) {
                    final String testModuleName = getModuleName(testModuleInfo);
                    if (testModuleName == null) {
                        return Collections.emptyList();
                    }
                    final String srcModuleName = getModuleName(srcModuleInfo);
                    if (srcModuleName == null) {
                        return Collections.emptyList();
                    }
                    if (testModuleName != null && srcModuleName != null && testModuleName.equals(srcModuleName)) {
                        String paths =
                            Stream.concat(Arrays.stream(project.getSourceRoots(false)),
                                          Arrays.stream(project.getGeneratedSourceRoots(false)))
                                  .filter(u -> "file".equals(u.getScheme()))
                                  .map(u -> u.getPath())
                                  .collect(Collectors.joining(System.getProperty("path.separator")));
                        return Collections.unmodifiableList(Arrays.asList("--patch-module", srcModuleName + "=" + paths));
                    }
                    final List<String> result = Arrays.asList(
                        "--add-reads",                                  //NOI18N
                        String.format("%s=ALL-UNNAMED", testModuleName));   //NOI18N
                    return Collections.unmodifiableList(result);
                }
            };

            @NonNull
            abstract List<String> createArguments(NbMavenProjectImpl project,
                    @NullAllowed final FileObject srcModuleInfo,
                    @NullAllowed final FileObject testModuleInfo);
        }
    }
}
