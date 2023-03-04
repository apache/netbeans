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

package org.netbeans.modules.java.j2semodule;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
final class MainClassUpdater extends FileChangeAdapter implements PropertyChangeListener {

    private static final RequestProcessor RP = new RequestProcessor ("main-class-updater",1);       //NOI18N
    private static final Logger LOG = Logger.getLogger(MainClassUpdater.class.getName());
    private static final int NEW      = 0;
    private static final int STARTED  = 1;
    private static final int FINISHED = 2;

    private final Project project;
    private final PropertyEvaluator eval;
    private final UpdateHelper helper;
    private final SourceRoots sourceRoots;
    private final String mainClassPropName;
    private final AtomicInteger state;
    //@GuardedBy("this")
    private FileObject currentFo;
    //@GuardedBy("this")
    private DataObject currentDo;
    //@GuardedBy("this")
    private FileChangeListener foListener;
    //@GuardedBy("this")
    private PropertyChangeListener doListener;
    //@GuardedBy("this")
    private long lc = 0;

    /** Creates a new instance of MainClassUpdater */
    MainClassUpdater(
            @NonNull final Project project,
            @NonNull final PropertyEvaluator eval,
            @NonNull final UpdateHelper helper,
            @NonNull final SourceRoots sourceRoots,
            @NonNull final String mainClassPropName) {
        assert project != null;
        assert eval != null;
        assert helper != null;
        assert sourceRoots != null;
        assert mainClassPropName != null;
        this.project = project;
        this.eval = eval;
        this.helper = helper;
        this.sourceRoots = sourceRoots;
        this.mainClassPropName = mainClassPropName;
        this.state = new AtomicInteger(NEW);
    }

    void start () {
        RP.submit(() -> {
            if (state.compareAndSet(NEW, STARTED)) {
                eval.addPropertyChangeListener(MainClassUpdater.this);
                addFileChangeListener ();
            } else {
                throw new IllegalStateException("Current State: " + state.get());   //NOI18N
            }
        });
    }

    public void stop() {
        RP.submit(() -> {
            if (state.compareAndSet(STARTED, FINISHED)) {
                synchronized (MainClassUpdater.this) {
                    if (currentFo != null && foListener != null) {
                        currentFo.removeFileChangeListener(foListener);
                    }
                    if (currentDo != null && doListener != null) {
                        currentDo.removePropertyChangeListener(doListener);
                    }
                }
            } else {
                throw new IllegalStateException("Current State: " + state.get());   //NOI18N
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (DataObject.PROP_PRIMARY_FILE.equals(evt.getPropertyName())) {
            final FileObject newFile = (FileObject) evt.getNewValue();
            final FileObject oldFile = (FileObject) evt.getOldValue();
            handleMainClassMoved(oldFile, newFile);
        } else if (this.mainClassPropName.equals(evt.getPropertyName())) {
            //Go out of the ProjectManager.MUTEX, see #118722
            RP.post(MainClassUpdater.this::addFileChangeListener);
        }
    }

    @Override
    public void fileRenamed (final FileRenameEvent evt) {
        handleMainClassMoved(evt.getFile(), evt.getFile());
    }

    private void handleMainClassMoved(final FileObject oldFile, final FileObject newFile) {
        if (!project.getProjectDirectory().isValid()) {
            return;
        }
        final FileObject _current;
        synchronized (this) {
            _current = this.currentFo;
        }
        if (oldFile == _current) {
            Runnable r = () -> {
                try {
                    final String oldMainClass = ProjectManager.mutex().readAccess((Mutex.ExceptionAction<String>) () -> eval.getProperty(mainClassPropName));
                    Collection<ElementHandle<TypeElement>> main = SourceUtils.getMainClasses(newFile);
                    String newMainClass = null;
                    if (!main.isEmpty()) {
                        ElementHandle<TypeElement> mainHandle = main.iterator().next();
                        newMainClass = mainHandle.getQualifiedName();
                    }
                    if (newMainClass != null && !newMainClass.equals(oldMainClass) && helper.requestUpdate() &&
                            // XXX ##84806: ideally should update nbproject/configs/*.properties in this case:
                            eval.getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG) == null) {
                        final String newMainClassFinal = newMainClass;
                        ProjectManager.mutex().writeAccess((Mutex.ExceptionAction<Void>) () -> {
                            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            props.put (mainClassPropName, newMainClassFinal);
                            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                            ProjectManager.getDefault().saveProject (project);
                            return null;
                        });
                    }
                } catch (IOException | MutexException e) {
                    Exceptions.printStackTrace(e);
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                r.run();
            } else {
                SwingUtilities.invokeLater(r);
            }
        }
    }

    private void addFileChangeListener () {
        final long clc;
        synchronized (MainClassUpdater.this) {
            if (currentFo != null && foListener != null) {
                currentFo.removeFileChangeListener(foListener);
                foListener = null;
                currentFo = null;
            }
            if (currentDo != null && doListener != null) {
                currentDo.removePropertyChangeListener(doListener);
                doListener = null;
                currentDo = null;
            }
            clc = ++lc;
        }
        final String mainClassName = eval.getProperty(mainClassPropName);
        if (mainClassName != null) {
            FileObject[] roots = sourceRoots.getRoots();
            if (roots.length>0) {
                final ClassPath bootCp = Optional.ofNullable(ClassPath.getClassPath(roots[0], ClassPath.BOOT))
                        .orElse(JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries());
                final ClassPath compileCp = Optional.ofNullable(ClassPath.getClassPath(roots[0], ClassPath.COMPILE))
                        .orElse(ClassPath.EMPTY);
                final ClassPath systemModules = ClassPath.getClassPath(roots[0], JavaClassPathConstants.MODULE_BOOT_PATH);
                final ClassPath modulePath = ClassPath.getClassPath(roots[0], JavaClassPathConstants.MODULE_COMPILE_PATH);
                final ClassPath allUnnamed = ClassPath.getClassPath(roots[0], JavaClassPathConstants.MODULE_CLASS_PATH);
                final ClassPath moduleSourcePath = ClassPath.getClassPath(roots[0], JavaClassPathConstants.MODULE_SOURCE_PATH);
                final ClasspathInfo cpInfo = new ClasspathInfo.Builder(bootCp)
                        .setClassPath(compileCp)
                        .setModuleBootPath(systemModules)
                        .setModuleCompilePath(modulePath)
                        .setModuleClassPath(allUnnamed)
                        .setModuleSourcePath(moduleSourcePath)
                        .build();
                final JavaSource js = JavaSource.create(cpInfo);
                // execute immediately, or delay if cannot find main class
                ScanUtils.postUserActionTask(js, (CompilationController c) -> {
                    c.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement te = ScanUtils.checkElement(c, c.getElements().getTypeElement(mainClassName));
                    if (te != null) {
                        final ClassPath allSources = ClassPathSupport.createClassPath(roots);
                        final FileObject fo = SourceUtils.getFile(
                                te,
                                new ClasspathInfo.Builder(bootCp)
                                        .setClassPath(compileCp)
                                        .setSourcePath(allSources)
                                        .setModuleBootPath(systemModules)
                                        .setModuleCompilePath(modulePath)
                                        .setModuleClassPath(allUnnamed)
                                        .setModuleSourcePath(moduleSourcePath)
                                        .build());
                        final boolean owned = allSources.contains(fo);
                        synchronized (MainClassUpdater.this) {
                            if (lc == clc && fo != null && owned) {
                                currentFo = fo;
                                foListener = WeakListeners.create(FileChangeListener.class, MainClassUpdater.this, currentFo);
                                currentFo.addFileChangeListener(foListener);
                                currentDo = DataObject.find(currentFo);
                                doListener = org.openide.util.WeakListeners.propertyChange(MainClassUpdater.this, currentDo);
                                currentDo.addPropertyChangeListener(doListener);
                            }
                        }
                    }
                });
            }
        }
    }
}
