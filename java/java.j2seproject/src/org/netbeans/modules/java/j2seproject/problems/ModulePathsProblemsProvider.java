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
package org.netbeans.modules.java.j2seproject.problems;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import javax.swing.JButton;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.netbeans.modules.java.j2seproject.J2SEProjectUtil;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import static org.netbeans.modules.java.j2seproject.J2SEProjectUtil.ref;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
/**
 *
 * @author Tomas Zezula
 */
@ProjectServiceProvider(service = ProjectProblemsProvider.class, projectType = "org-netbeans-modules-java-j2seproject")
public final class ModulePathsProblemsProvider implements ProjectProblemsProvider, PropertyChangeListener, FileChangeListener {
    private static final String PROP_TEST_MODULE_PATHS_CHECK = "ModulePathsProblems.test.paths.check";  //NOI18N
    private static final RequestProcessor RESOLVER = new RequestProcessor(ModulePathsProblemsProvider.class);

    private final J2SEProject project;
    private final PropertyChangeSupport listeners;
    private final Collection</*@GuardedBy("this")*/File> moduleInfoListeners ;
    //@GuardedBy("this")
    private Collection<ProjectProblem> cache;
    //@GuardedBy("this")
    private boolean listensOnRoots;
    //@GuardedBy("this")
    private boolean listensOnEval;

    public ModulePathsProblemsProvider(@NonNull final Lookup baseLkp) {
        this.project = baseLkp.lookup(J2SEProject.class);
        if (this.project == null) {
            throw new IllegalArgumentException(String.format(
                    "Unsupported project: %s of type: %s",  //NOI18N
                    project,
                    project.getClass()
                    ));
        }
        this.moduleInfoListeners = new HashSet<>();
        this.listeners = new PropertyChangeSupport(this);
    }

    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        Collection<ProjectProblem> res;
        synchronized (this) {
            res = cache;
        }
        if (res == null) {
            final SourceRoots src;
            final SourceRoots test;
            final Collection<File> roots;
            final AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
            final boolean disabled = props != null && Boolean.FALSE.toString().equals(props.get(PROP_TEST_MODULE_PATHS_CHECK, true));
            if (disabled) {
                res = Collections.emptySet();
                src = null;
                test = null;
                roots = null;
            } else {
                src = project.getSourceRoots();
                test = project.getTestSourceRoots();
                roots = new HashSet<>();
                final boolean modularSources = hasModuleInfo(src, roots);
                final boolean modularTests = hasModuleInfo(test, roots);
                res = test.getRoots().length == 0 ?
                        Collections.emptySet() :
                        createProblems(
                            project,
                            modularSources,
                            modularTests);
            }
            synchronized (this) {
                if (!listensOnEval) {
                    listensOnEval = true;
                    project.evaluator().addPropertyChangeListener(WeakListeners.propertyChange(this, project.evaluator()));
                }
                if (src != null && test != null) {
                    if (!listensOnRoots) {
                        listensOnRoots = true;
                        src.addPropertyChangeListener(WeakListeners.propertyChange(this, src));
                        test.addPropertyChangeListener(WeakListeners.propertyChange(this, test));
                    }
                }
                if (roots != null) {
                    final Set<File> toRemove = new HashSet<>(moduleInfoListeners);
                    toRemove.removeAll(roots);
                    roots.removeAll(moduleInfoListeners);
                    for (File f : toRemove) {
                        FileUtil.removeFileChangeListener(
                                this,
                                J2SEProjectUtil.getModuleInfo(f));
                        moduleInfoListeners.remove(f);
                    }
                    for (File f : roots) {
                        FileUtil.addFileChangeListener(
                                this,
                                J2SEProjectUtil.getModuleInfo(f));
                        moduleInfoListeners.add(f);
                    }
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
    public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        this.listeners.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        this.listeners.removePropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(@NonNull final PropertyChangeEvent evt) {
        final String propName = evt.getPropertyName();
        if (propName == null ||
            SourceRoots.PROP_ROOTS.equals(propName) ||
            ProjectProperties.JAVAC_TEST_CLASSPATH.equals(propName) ||
            ProjectProperties.JAVAC_TEST_MODULEPATH.equals(propName) ||
            ProjectProperties.RUN_TEST_CLASSPATH.equals(propName) ||
            ProjectProperties.RUN_TEST_MODULEPATH.equals(propName) ||
            propName.contains(PROP_TEST_MODULE_PATHS_CHECK)) {
            reset();
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

    private void reset() {
        synchronized (this) {
            cache = null;
        }
        listeners.firePropertyChange(PROP_PROBLEMS, null, null);
    }


    private static boolean hasModuleInfo(
            @NonNull final SourceRoots roots,
            @NonNull final Collection<? super File> rootsCollector) {
        boolean res = false;
        for (FileObject root : roots.getRoots()) {
            FileObject mInfo = J2SEProjectUtil.getModuleInfo(root);
            res |= Optional.ofNullable(mInfo).isPresent()
                    && ClassPath.getClassPath(root, ClassPath.SOURCE).contains(mInfo);
            Optional.ofNullable(FileUtil.toFile(root)).ifPresent(rootsCollector::add);
        }
        return res;
    }

    private static boolean hasRef(
            @NonNull final EditableProperties ep,
            @NonNull final String pathId,
            @NonNull final String propertyName) {
        return Optional.ofNullable(ep.getProperty(pathId))
                .map((val) -> {
                    final String ref = ref(propertyName, true);
                    return Arrays.stream(PropertyUtils.tokenizePath(val))
                            .anyMatch((element) -> ref.equals(element));
                })
                .orElse(Boolean.FALSE);
    }

    private static Collection<ProjectProblem> createProblems(
            @NonNull final J2SEProject project,
            @NonNull final boolean modularSources,
            @NonNull final boolean modularTests) {
        final EditableProperties ep = project.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        if (modularSources) {
            if (modularTests) {
                return hasRef(ep, ProjectProperties.JAVAC_TEST_CLASSPATH, ProjectProperties.BUILD_CLASSES_DIR) ||
                        !hasRef(ep, ProjectProperties.JAVAC_TEST_MODULEPATH, ProjectProperties.BUILD_CLASSES_DIR) ||
                        hasRef(ep, ProjectProperties.RUN_TEST_CLASSPATH, ProjectProperties.BUILD_TEST_CLASSES_DIR)||
                        !hasRef(ep, ProjectProperties.RUN_TEST_MODULEPATH, ProjectProperties.BUILD_TEST_CLASSES_DIR)?
                            Collections.singleton(ProjectProblemsProvider.ProjectProblem.createError(
                                NbBundle.getMessage(ModulePathsProblemsProvider.class, "TXT_InvalidModulePaths"),
                                NbBundle.getMessage(ModulePathsProblemsProvider.class, "DESC_InvalidModulePathsBlackBox"),
                                new FixBrokenModulePaths(project, modularSources, modularTests))) :
                            Collections.emptySet();
            } else {
                return hasRef(ep, ProjectProperties.JAVAC_TEST_CLASSPATH, ProjectProperties.BUILD_CLASSES_DIR) ||
                        !hasRef(ep, ProjectProperties.JAVAC_TEST_MODULEPATH, ProjectProperties.BUILD_CLASSES_DIR) ||
                        hasRef(ep, ProjectProperties.RUN_TEST_CLASSPATH, ProjectProperties.BUILD_TEST_CLASSES_DIR)||
                        hasRef(ep, ProjectProperties.RUN_TEST_MODULEPATH, ProjectProperties.BUILD_TEST_CLASSES_DIR)?
                            Collections.singleton(ProjectProblemsProvider.ProjectProblem.createError(
                                NbBundle.getMessage(ModulePathsProblemsProvider.class, "TXT_InvalidModulePaths"),
                                NbBundle.getMessage(ModulePathsProblemsProvider.class, "DESC_InvalidModulePathsWhiteBox"),
                                new FixBrokenModulePaths(project, modularSources, modularTests))) :
                            Collections.emptySet();
            }
        } else {
            return !hasRef(ep, ProjectProperties.JAVAC_TEST_CLASSPATH, ProjectProperties.BUILD_CLASSES_DIR) ||
                    hasRef(ep, ProjectProperties.JAVAC_TEST_MODULEPATH, ProjectProperties.BUILD_CLASSES_DIR) ||
                    !hasRef(ep, ProjectProperties.RUN_TEST_CLASSPATH, ProjectProperties.BUILD_TEST_CLASSES_DIR)||
                    hasRef(ep, ProjectProperties.RUN_TEST_MODULEPATH, ProjectProperties.BUILD_TEST_CLASSES_DIR)?
                        Collections.singleton(ProjectProblemsProvider.ProjectProblem.createError(
                            NbBundle.getMessage(ModulePathsProblemsProvider.class, "TXT_InvalidClassPaths"),
                            NbBundle.getMessage(ModulePathsProblemsProvider.class, "DESC_InvalidClassPaths"),
                            new FixBrokenModulePaths(project, modularSources, modularTests))) :
                        Collections.emptySet();
        }
    }

    private static final class FixBrokenModulePaths implements ProjectProblemResolver {
        private final J2SEProject project;
        private final boolean modularSources;
        private final boolean modularTests;

        FixBrokenModulePaths(
                @NonNull final J2SEProject project,
                final boolean modularSources,
                final boolean modularTests) {
            this.project = project;
            this.modularSources = modularSources;
            this.modularTests = modularTests;
        }

        @Override
        public boolean equals(@NullAllowed final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof FixBrokenModulePaths)) {
                return false;
            }
            return Objects.equals(
                project.getProjectDirectory(),
                ((FixBrokenModulePaths)obj).project.getProjectDirectory());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(project.getProjectDirectory());
        }

        @Override
        public Future<Result> resolve() {
            final JButton fix = new JButton(NbBundle.getMessage(ModulePathsProblemsProvider.class,"TXT_Update"));
            final JButton ignore = new JButton(NbBundle.getMessage(ModulePathsProblemsProvider.class, "TXT_Ignore"));
            final NotifyDescriptor nd = new NotifyDescriptor(
                    NbBundle.getMessage(ModulePathsProblemsProvider.class, "MSG_FixPaths",
                            NbBundle.getMessage(ModulePathsProblemsProvider.class, modularSources ? "MSG_Modular" : "MSG_NonModular")),
                    NbBundle.getMessage(ModulePathsProblemsProvider.class, "TITLE_FixPaths"),
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    new Object[] {fix, ignore},
                    fix
            );
            final Callable<Result> action;
            if (DialogDisplayer.getDefault().notify(nd) == fix) {
                action = () -> {
                    ProjectManager.mutex().writeAccess(() -> {
                        final EditableProperties ep = project.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        boolean changed = false;
                        if (modularSources) {
                            if (modularTests) {
                                changed |= removeRef(ep, ProjectProperties.JAVAC_TEST_CLASSPATH, ProjectProperties.BUILD_CLASSES_DIR);
                                changed |= addRefIfAbsent(ep, ProjectProperties.JAVAC_TEST_MODULEPATH, ProjectProperties.BUILD_CLASSES_DIR, ProjectProperties.JAVAC_MODULEPATH);
                                changed |= removeRef(ep, ProjectProperties.RUN_TEST_CLASSPATH, ProjectProperties.BUILD_TEST_CLASSES_DIR);
                                changed |= addRefIfAbsent(ep, ProjectProperties.RUN_TEST_MODULEPATH, ProjectProperties.BUILD_TEST_CLASSES_DIR, ProjectProperties.JAVAC_TEST_MODULEPATH);
                            } else {
                                changed |= removeRef(ep, ProjectProperties.JAVAC_TEST_CLASSPATH, ProjectProperties.BUILD_CLASSES_DIR);
                                changed |= addRefIfAbsent(ep, ProjectProperties.JAVAC_TEST_MODULEPATH, ProjectProperties.BUILD_CLASSES_DIR, ProjectProperties.JAVAC_MODULEPATH);
                                changed |= removeRef(ep, ProjectProperties.RUN_TEST_CLASSPATH, ProjectProperties.BUILD_TEST_CLASSES_DIR);
                                changed |= removeRef(ep, ProjectProperties.RUN_TEST_MODULEPATH, ProjectProperties.BUILD_TEST_CLASSES_DIR);
                            }
                        } else {
                            changed |= addRefIfAbsent(ep, ProjectProperties.JAVAC_TEST_CLASSPATH, ProjectProperties.BUILD_CLASSES_DIR, ProjectProperties.JAVAC_CLASSPATH);
                            changed |= removeRef(ep, ProjectProperties.JAVAC_TEST_MODULEPATH, ProjectProperties.BUILD_CLASSES_DIR);
                            changed |= addRefIfAbsent(ep, ProjectProperties.RUN_TEST_CLASSPATH, ProjectProperties.BUILD_TEST_CLASSES_DIR, ProjectProperties.JAVAC_TEST_CLASSPATH);
                            changed |= removeRef(ep, ProjectProperties.RUN_TEST_MODULEPATH, ProjectProperties.BUILD_TEST_CLASSES_DIR);
                        }
                        if (changed) {
                            try {
                                project.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                                ProjectManager.getDefault().saveProject(project);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    });
                    return Result.create(Status.RESOLVED);
                };
            } else {
                action = () -> {
                    ProjectManager.mutex().writeAccess(() -> {
                        try {
                            final AuxiliaryProperties props = project.getLookup().lookup(AuxiliaryProperties.class);
                            props.put(PROP_TEST_MODULE_PATHS_CHECK, Boolean.FALSE.toString(), true);
                            ProjectManager.getDefault().saveProject(project);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    });
                    return Result.create(Status.RESOLVED);
                };
            }
            return RESOLVER.submit(action);
        }

        private static boolean removeRef(
                @NonNull final EditableProperties ep,
                @NonNull final String pathId,
                @NonNull final String elementToRemove) {
            final String elementToRemoveRef = ref(elementToRemove, true);
            final boolean[] changed = new boolean[1];
            Optional.ofNullable(ep.getProperty(pathId))
                .map((val)->{
                    return Arrays.stream(PropertyUtils.tokenizePath(val))
                            .filter((element) -> {
                                final boolean remove = elementToRemoveRef.equals(element);
                                changed[0] |= remove;
                                return !remove;
                            })
                            .toArray((len) -> new String[len]);
                })
                .ifPresent((val) -> {
                    ep.setProperty(pathId, addPathSeparators(val));
                });
            return changed[0];
        }

        private static boolean addRefIfAbsent(
                @NonNull final EditableProperties ep,
                @NonNull final String pathId,
                @NonNull final String elementToAdd,
                @NullAllowed final String insertAfter) {
            final String elementToAddRef = ref(elementToAdd, true);
            final String insertAfterRef = insertAfter == null ?
                    null :
                    ref(insertAfter, true);
            final boolean[] changed = new boolean[1];
            Optional.ofNullable(ep.getProperty(pathId))
                .map((val)-> {
                    String[] path = PropertyUtils.tokenizePath(val);
                    if(!Arrays.stream(path).anyMatch((element) -> elementToAddRef.equals(element))) {
                        final List<String> newPath = new ArrayList<>(path.length + 1);
                        boolean added = false;
                        for (int i=0; i< path.length; i++) {
                            newPath.add(path[i]);
                            if (insertAfterRef != null && insertAfterRef.equals(path[i])) {
                                added = true;
                                newPath.add(elementToAddRef);
                            }
                        }
                        if (!added) {
                            newPath.add(elementToAddRef);
                        }
                        path = newPath.toArray(new String[0]);
                        changed[0] = true;
                    }
                    return path;
                })
                .ifPresent((val) -> ep.setProperty(pathId, addPathSeparators(val)));
            return changed[0];
        }

        @NonNull
        private static String[] addPathSeparators(@NonNull final String... path) {
            for (int i = 0; i < path.length; i++) {
                path[i] = i+1 == path.length ?
                        path[i] :
                        String.format("%s:", path[i]);  //NOI18N
            }
            return path;
        }
    }
}
