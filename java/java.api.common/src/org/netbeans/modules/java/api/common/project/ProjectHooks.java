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

package org.netbeans.modules.java.api.common.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.AbstractClassPathProvider;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import static org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_MODIFIED;
import static org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_UNKNOWN;
import static org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_OLD_PROJECT_XML;
import static org.netbeans.spi.project.support.ant.GeneratedFilesHelper.FLAG_OLD_STYLESHEET;
import org.openide.modules.PatchedPublic;
import org.openide.modules.Places;
import org.openide.util.WeakListeners;


/**
 * Factory methods for creating {@link ProjectOpenedHook} and {@link ProjectXmlSavedHook}
 * implementations.
 * @since 1.62
 * @author Tomas Zezula
 */
public final class ProjectHooks {

    private static final Logger LOG = Logger.getLogger(ProjectHooks.class.getName());

    private ProjectHooks() {
        throw new IllegalStateException("No instance allowed"); //NOI18N
    }

    /**
     * Creates a new {@link ProjectOpenedHookBuilder}.
     * @param project the project for which the builder should be created
     * @param eval the project {@link PropertyEvaluator}
     * @param updateHelper the project {@link UpdateHelper}
     * @param genFilesHelper the project {@link GeneratedFilesHelper}
     * @param cpProvider the project {@link AbstractClassPathProvider}
     * @return a new {@link ProjectOpenedHookBuilder}
     */
    @NonNull
    public static ProjectOpenedHookBuilder createProjectOpenedHookBuilder(
        @NonNull final Project project,
        @NonNull final PropertyEvaluator eval,
        @NonNull final UpdateHelper updateHelper,
        @NonNull final GeneratedFilesHelper genFilesHelper,
        @NonNull final AbstractClassPathProvider cpProvider) {
        return new ProjectOpenedHookBuilder(project, eval, updateHelper, genFilesHelper, cpProvider);
    }

    @PatchedPublic
    private static ProjectOpenedHookBuilder createProjectOpenedHookBuilder(
        @NonNull final Project project,
        @NonNull final PropertyEvaluator eval,
        @NonNull final UpdateHelper updateHelper,
        @NonNull final GeneratedFilesHelper genFilesHelper,
        @NonNull final ClassPathProviderImpl cpProviderImpl) {
        return new ProjectOpenedHookBuilder(project, eval, updateHelper, genFilesHelper, cpProviderImpl);
    }

    /**
     * Creates a new {@link ProjectXmlSavedHookBuilder}.
     * @param eval the project {@link PropertyEvaluator}
     * @param updateHelper the project {@link UpdateHelper}
     * @param genFilesHelper the project {@link GeneratedFilesHelper}
     * @return a new {@link ProjectXmlSavedHookBuilder}
     */
    @NonNull
    public static ProjectXmlSavedHookBuilder createProjectXmlSavedHookBuilder(
        @NonNull final PropertyEvaluator eval,
        @NonNull final UpdateHelper updateHelper,
        @NonNull final GeneratedFilesHelper genFilesHelper) {
        return new ProjectXmlSavedHookBuilder(eval, updateHelper, genFilesHelper);
    }

    /**
     * The builder for default Ant project {@link ProjectOpenedHook} implementation.
     */
    public static final class ProjectOpenedHookBuilder {

        private final Project project;
        private final PropertyEvaluator eval;
        private final UpdateHelper updateHelper;
        private final GeneratedFilesHelper genFilesHelper;
        private final AbstractClassPathProvider cpProvider;
        private final Set<String> classPathTypes = new HashSet<String>();
        private final List<Runnable> preOpen = new LinkedList<Runnable>();
        private final List<Runnable> postOpen = new LinkedList<Runnable>();
        private final List<Runnable> preClose = new LinkedList<Runnable>();
        private final List<Runnable> postClose = new LinkedList<Runnable>();
        private String buildScriptProperty = ProjectProperties.BUILD_SCRIPT;
        private URL buildTemplate;
        private URL buildImplTemplate;
        
        private ProjectOpenedHookBuilder(
            @NonNull final Project project,
            @NonNull final PropertyEvaluator eval,
            @NonNull final UpdateHelper updateHelper,
            @NonNull final GeneratedFilesHelper genFilesHelper,
            @NonNull final AbstractClassPathProvider cpProvider) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("updateHelper", updateHelper);   //NOI18N
            Parameters.notNull("genFilesHelper", genFilesHelper);   //NOI18N
            Parameters.notNull("cpProviderImpl", cpProvider);   //NOI18N
            this.project = project;
            this.eval = eval;
            this.updateHelper = updateHelper;
            this.genFilesHelper = genFilesHelper;
            this.cpProvider = cpProvider;
        }

        /**
         * Adds a class path type among important class path types.
         * The important class path types are registered to {@link GlobalPathRegistry}
         * when the project is opened and unregistered when the project is closed.
         * @param classPathType the class path type to add.
         * @return the {@link ProjectOpenedHookBuilder}
         */
        @NonNull
        public ProjectOpenedHookBuilder addClassPathType(@NonNull final String classPathType) {
            Parameters.notNull("classPathType", classPathType); //NOI18N
            classPathTypes.add(classPathType);
            return this;
        }

        /**
         * Adds an action invoked at the beginning of the {@link ProjectOpenedHook#projectOpened}.
         * @param action the action to be added.
         * @return the {@link ProjectOpenedHookBuilder}
         */
        @NonNull
        public ProjectOpenedHookBuilder addOpenPreAction(@NonNull final Runnable action) {
            Parameters.notNull("action", action);   //NOI18N
            this.preOpen.add(action);
            return this;
        }

        /**
         * Adds an action invoked at the end of the {@link ProjectOpenedHook#projectOpened}.
         * @param action the action to be added.
         * @return the {@link ProjectOpenedHookBuilder}
         */
        @NonNull
        public ProjectOpenedHookBuilder addOpenPostAction(@NonNull final Runnable action) {
            Parameters.notNull("action", action);   //NOI18N
            this.postOpen.add(action);
            return this;
        }

        /**
         * Adds an action invoked at the beginning of {@link ProjectOpenedHook#projectClosed}.
         * @param action the action to be added.
         * @return the {@link ProjectOpenedHookBuilder}
         */
        @NonNull
        public ProjectOpenedHookBuilder addClosePreAction(@NonNull final Runnable action) {
            Parameters.notNull("action", action);   //NOI18N
            preClose.add(action);
            return this;
        }

        /**
         * Adds an action invoked at the end of {@link ProjectOpenedHook#projectClosed}.
         * @param action the action to be added.
         * @return the {@link ProjectOpenedHookBuilder}
         */
        @NonNull
        public ProjectOpenedHookBuilder addClosePostAction(@NonNull final Runnable action) {
            Parameters.notNull("action", action);   //NOI18N
            postClose.add(action);
            return this;
        }

        /**
         * Sets a XSL template to generate build.xml file.
         * @param template the {@link URL} of the template
         * @return the {@link ProjectOpenedHookBuilder}
         */
        @NonNull
        public ProjectOpenedHookBuilder setBuildTemplate(@NonNull final URL template) {
            Parameters.notNull("template", template);   //NOI18N
            this.buildTemplate = template;
            return this;
        }

        /**
         * Sets a XSL template to generate build-impl.xml file.
         * @param template the {@link URL} of the template
         * @return the {@link ProjectOpenedHookBuilder}
         */
        @NonNull
        public ProjectOpenedHookBuilder setBuildImplTemplate(@NonNull final URL template) {
            Parameters.notNull("template", template);   //NOI18N
            this.buildImplTemplate = template;
            return this;
        }

        /**
         * Sets the name of property referencing the project build script.
         * If not set the {@link ProjectProperties#BUILD_SCRIPT} is used.
         * @param propertyName the name of property holding the name of project's build script.
         * @return the {@link ProjectOperations.ProjectOperationsBuilder}
         */       
        @NonNull
        public ProjectOpenedHookBuilder setBuildScriptProperty(@NonNull final String propertyName) {
            Parameters.notNull("propertyName", propertyName); //NOI18N
            this.buildScriptProperty = propertyName;
            return this;
        }

        /**
         * Creates a new configured {@link ProjectOpenedHook}.
         * @return the {@link ProjectOpenedHook} instance
         */
        @NonNull
        public ProjectOpenedHook build() {
            return new ProjectOpenedHookImpl(
                project,
                eval,
                updateHelper,
                genFilesHelper,
                cpProvider,
                classPathTypes,
                preOpen,
                postOpen,
                preClose,
                postClose,                
                buildImplTemplate,
                buildTemplate,
                buildScriptProperty);
        }
        
        
    }


    /**
     * The builder for default Ant project {@link ProjectXmlSavedHook} implementation.
     */
    public static final class ProjectXmlSavedHookBuilder {

        private final PropertyEvaluator eval;
        private final UpdateHelper updateHelper;
        private final GeneratedFilesHelper genFilesHelper;
        private final List<Runnable> preActions = new LinkedList<Runnable>();
        private final List<Runnable> postActions = new LinkedList<Runnable>();
        private String buildScriptProperty = ProjectProperties.BUILD_SCRIPT;
        private URL buildImplTemplate;
        private URL buildTemplate;
        private Callable<Boolean> overridePredicate;
        
        private ProjectXmlSavedHookBuilder(
            @NonNull final PropertyEvaluator eval,
            @NonNull final UpdateHelper updateHelper,
            @NonNull final GeneratedFilesHelper genFilesHelper) {
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("updateHelper", updateHelper);   //NOI18N
            Parameters.notNull("genFilesHelper", genFilesHelper);   //NOI18N
            this.eval = eval;
            this.updateHelper = updateHelper;
            this.genFilesHelper = genFilesHelper;
        }

        /**
         * Adds an action invoked at the beginning of the {@link ProjectXmlSavedHook#projectXmlSaved}.
         * @param action the action to be added.
         * @return the {@link ProjectXmlSavedHookBuilder}
         */
        @NonNull
        public ProjectXmlSavedHookBuilder addPreAction(@NonNull final Runnable action) {
            Parameters.notNull("action", action);   //NOI18N
            preActions.add(action);
            return this;
        }

        /**
         * Adds an action invoked at the end of the {@link ProjectXmlSavedHook#projectXmlSaved}.
         * @param action the action to be added.
         * @return the {@link ProjectXmlSavedHookBuilder}
         */
        @NonNull
        public ProjectXmlSavedHookBuilder addPostAction(@NonNull final Runnable action) {
            Parameters.notNull("action", action);   //NOI18N
            postActions.add(action);
            return this;
        }

        /**
         * Sets a XSL template to generate build.xml file.
         * @param template the {@link URL} of the template
         * @return the {@link ProjectXmlSavedHookBuilder}
         */
        @NonNull
        public ProjectXmlSavedHookBuilder setBuildTemplate(@NonNull final URL template) {
            Parameters.notNull("template", template);   //NOI18N
            this.buildTemplate = template;
            return this;
        }

        /**
         * Sets a XSL template to generate build-impl.xml file.
         * @param template the {@link URL} of the template
         * @return the {@link ProjectXmlSavedHookBuilder}
         */
        @NonNull
        public ProjectXmlSavedHookBuilder setBuildImplTemplate(@NonNull final URL template) {
            Parameters.notNull("template", template);   //NOI18N
            this.buildImplTemplate = template;
            return this;
        }

        /**
         * Sets the name of property referencing the project build script.
         * If not set the {@link ProjectProperties#BUILD_SCRIPT} is used.
         * @param propertyName the name of property holding the name of project's build script.
         * @return the {@link ProjectOperations.ProjectOperationsBuilder}
         */
        @NonNull
        public ProjectXmlSavedHookBuilder setBuildScriptProperty(@NonNull final String propertyName) {
            Parameters.notNull("propertyName", propertyName); //NOI18N
            this.buildScriptProperty = propertyName;
            return this;
        }

        /**
         * Sets the predicate to control regeneration of modified build-impl.
         * @param predicate the predicate returning true if the user modified build-impl.xml should
         * be backed up and regenerated.
         * @return the {@link ProjectXmlSavedHookBuilder}
         */
        @NonNull
        public ProjectXmlSavedHookBuilder setOverrideModifiedBuildImplPredicate(@NonNull final Callable<Boolean> predicate) {
            Parameters.notNull("predicate", predicate); //NOI18N
            this.overridePredicate = predicate;
            return this;
        }

        /**
         * Creates a new configured {@link ProjectXmlSavedHook}.
         * @return the {@link ProjectXmlSavedHook} instance
         */
        @NonNull
        public ProjectXmlSavedHook build () {
            return new ProjectXmlSavedHookImpl(
                eval,
                updateHelper,
                genFilesHelper,
                preActions,
                postActions,
                buildImplTemplate,
                buildTemplate,
                buildScriptProperty,
                overridePredicate);
        }

    }




    private static final class ProjectOpenedHookImpl extends ProjectOpenedHook implements AbstractClassPathProvider.ClassPathsChangeListener {

        private static final RequestProcessor PROJECT_OPENED_RP = new RequestProcessor(ProjectOpenedHookImpl.class);

        private final Project project;
        private final PropertyEvaluator eval;
        private final UpdateHelper updateHelper;
        private final GeneratedFilesHelper genFilesHelper;
        private final AbstractClassPathProvider cpProvider;
        private final Set<String> classPathTypes;
        private final List<? extends Runnable> preClose;
        private final List<? extends Runnable> postClose;
        private final List<? extends Runnable> preOpen;
        private final List<? extends Runnable> postOpen;
        private final URL buildTemplate;
        private final URL buildImplTemplate;
        private final String buildScriptProperty;
        private final Map</*@GuardedBy("cpCache")*/String,Set<ClassPath>> cpCache;
        private final AtomicReference<AbstractClassPathProvider.ClassPathsChangeListener> cpListener;

        ProjectOpenedHookImpl(
            @NonNull final Project project,
            @NonNull final PropertyEvaluator eval,
            @NonNull final UpdateHelper updateHelper,
            @NonNull final GeneratedFilesHelper genFilesHelper,
            @NonNull final AbstractClassPathProvider cpProvider,
            @NonNull final Set<String> classPathTypes,
            @NonNull final List<? extends Runnable> preOpen,
            @NonNull final List<? extends Runnable> postOpen,
            @NonNull final List<? extends Runnable> preClose,
            @NonNull final List<? extends Runnable> postClose,
            @NullAllowed final URL buildImplTemplate,
            @NullAllowed final URL buildTemplate,
            @NonNull final String buildScriptProperty) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("eval", eval);       //NOI18
            Parameters.notNull("updateHelper", updateHelper);   //NOI18N
            Parameters.notNull("genFilesHelper", genFilesHelper);   //NOI18N
            Parameters.notNull("cpProvider", cpProvider);           //NOI18N
            Parameters.notNull("classPathTypes", classPathTypes);   //NOI18N
            Parameters.notNull("preOpen", preOpen);                 //NOI18N
            Parameters.notNull("postOpen", postOpen);               //NOI18N
            Parameters.notNull("preClose", preClose);               //NOI18N
            Parameters.notNull("postClose", postClose);             //NOI18N
            Parameters.notNull("buildScriptProperty", buildScriptProperty);   //NOI18N
            this.project = project;
            this.eval = eval;
            this.updateHelper = updateHelper;
            this.genFilesHelper = genFilesHelper;
            this.cpProvider = cpProvider;
            this.classPathTypes = classPathTypes;
            this.preOpen = preOpen;
            this.postOpen = postOpen;
            this.preClose = preClose;
            this.postClose = postClose;
            this.buildImplTemplate = buildImplTemplate;
            this.buildTemplate = buildTemplate;
            this.buildScriptProperty = buildScriptProperty;
            this.cpCache = new HashMap<>();
            this.cpListener = new AtomicReference<>();
        }

        @Override
        protected void projectOpened() {
            runAtomic(new Runnable() {
                @Override
                public void run() {
                    for (Runnable r : preOpen) {
                        runSafe(r);
                    }
                    // Check up on build scripts.
                    try {
                        if (updateHelper.isCurrent()) {
                            if (buildImplTemplate != null) {
                                genFilesHelper.refreshBuildScript(
                                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                                    buildImplTemplate,
                                    true);
                            }
                            if (buildTemplate != null) {
                                genFilesHelper.refreshBuildScript(
                                    CommonProjectUtils.getBuildXmlName(eval, buildScriptProperty),
                                    buildTemplate,
                                    true);
                            }
                        }
                    } catch (IOException e) {
                        LOG.log(
                           Level.INFO,
                           NbBundle.getMessage(ProjectHooks.class, "ERR_RegenerateProjectFiles"),
                           e);
                    }
                }
            });

            // register project's classpaths to GlobalPathRegistry
            final Map<String,Set<ClassPath>> snapshot = new HashMap<>();
            for (String classPathType : classPathTypes) {
                final ClassPath[] cps = cpProvider.getProjectClassPaths(classPathType);
                final Set<ClassPath> newCps = Collections.newSetFromMap(new IdentityHashMap<>());
                Collections.addAll(newCps,cps);
                snapshot.put(classPathType, newCps);
            }
            updateClassPathCache(snapshot);
            AbstractClassPathProvider.ClassPathsChangeListener l = cpListener.get();
            if (l == null) {
                l = WeakListeners.create(AbstractClassPathProvider.ClassPathsChangeListener.class, this, this.cpProvider);
                if (cpListener.compareAndSet(null, l)) {
                    this.cpProvider.addClassPathsChangeListener(l);
                }
            }
            runAtomic(new Runnable() {
                @Override
                public void run() {
                    updateUserBuildPropertiesRef();
                    for (Runnable r : postOpen) {
                        runSafe(r);
                    }
                    saveProject();
                }
            });
            verifyEncoding();
        }

        @Override
        protected void projectClosed() {
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    for (Runnable r : preClose) {
                            runSafe(r);
                        }
                    // just do if the whole project was not deleted...
                    if (project.getProjectDirectory().isValid()) {
                        // Probably unnecessary, but just in case:
                        try {
                            ProjectManager.getDefault().saveProject(project);
                        } catch (IOException e) {
                            if (!project.getProjectDirectory().canWrite()) {
                                // #91398 - ignore, we already reported on project open.
                                // not counting with someone setting the ro flag while the project is opened.
                            } else {
                                Exceptions.printStackTrace(e);
                            }
                        }
                    }
                    // unregister project's classpaths to GlobalPathRegistry
                    AbstractClassPathProvider.ClassPathsChangeListener l = cpListener.get();
                    if (l != null) {
                        if (cpListener.compareAndSet(l, null)) {
                            cpProvider.removeClassPathsChangeListener(l);
                        }
                    }
                    final Map<String,Set<ClassPath>> snapshot = new HashMap<>();
                    for (String classPathType : classPathTypes) {
                        snapshot.put(classPathType, Collections.emptySet());
                    }
                    updateClassPathCache(snapshot);
                    for (Runnable r : postClose) {
                        runSafe(r);
                    }
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                PROJECT_OPENED_RP.execute(r);
            } else {
                r.run();
            }
        }

        @Override
        public void classPathsChange(AbstractClassPathProvider.ClassPathsChangeEvent event) {
            final Map<String,Set<ClassPath>> snapshot = new HashMap<>();
            for (String cpType : event.getChangedClassPathTypes()) {
                if (classPathTypes.contains(cpType)) {
                    final ClassPath[] cps = cpProvider.getProjectClassPaths(cpType);
                    final Set<ClassPath> newCps = Collections.newSetFromMap(new IdentityHashMap<>());
                    Collections.addAll(newCps, cps);
                    snapshot.put(cpType, newCps);
                }
            }
            if (!snapshot.isEmpty()) {
                updateClassPathCache(snapshot);
            }
        }

        private void updateClassPathCache(@NonNull final Map<String,Set<ClassPath>> snapshot) {
            final GlobalPathRegistry gpr = GlobalPathRegistry.getDefault();
            synchronized (cpCache) {
                for (Map.Entry<String,Set<ClassPath>> e : snapshot.entrySet()) {
                    final String id = e.getKey();
                    final Set<ClassPath> newCps = e.getValue();
                    final Set<ClassPath> oldCps = cpCache.getOrDefault(id, Collections.emptySet());
                    final Set<ClassPath> toAdd = Collections.newSetFromMap(new IdentityHashMap<>());
                    toAdd.addAll(newCps);
                    toAdd.removeAll(oldCps);
                    final Set<ClassPath> toRemove = Collections.newSetFromMap(new IdentityHashMap<>());
                    toRemove.addAll(oldCps);
                    toRemove.removeAll(newCps);
                    cpCache.put(id, newCps);
                    gpr.unregister(id, toRemove.toArray(new ClassPath[0]));
                    gpr.register(id, toAdd.toArray(new ClassPath[0]));
                }
            }
        }

        private void updateUserBuildPropertiesRef() {
            final EditableProperties ep = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            final File buildProperties = new File(Places.getUserDirectory(), "build.properties"); // NOI18N
            ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N
            updateHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        }

        private void verifyEncoding() {
            String prop = eval.getProperty(ProjectProperties.SOURCE_ENCODING);
            if (prop != null) {
                try {
                    Charset.forName(prop);
                } catch (IllegalCharsetNameException e) {
                    //Broken property, log & ignore
                    LOG.log(
                        Level.WARNING,
                        "Illegal charset: {0} in project: {1}", //NOI18N
                        new Object[]{
                            prop,
                            FileUtil.getFileDisplayName(project.getProjectDirectory())
                        });
                } catch (UnsupportedCharsetException e) {
                    //todo: Needs UI notification like broken references.
                    LOG.log(
                        Level.WARNING,
                        "Unsupported charset: {0} in project: {1}", //NOI18N
                        new Object[]{
                            prop,
                            FileUtil.getFileDisplayName(project.getProjectDirectory())
                        });
                }
            }
        }

        private void runAtomic(@NonNull final Runnable r) {
            FileUtil.runAtomicAction(new Runnable() {
                @Override
                public void run() {
                    ProjectManager.mutex().writeAccess(new Runnable() {
                        @Override
                        public void run() {
                            r.run();
                        }
                    });
                }
            });
        }        

        private void saveProject() {
            try {
                ProjectManager.getDefault().saveProject(project);
            } catch (IOException e) {
                //#91398 provide a better error message in case of read-only location of project.
                if (!project.getProjectDirectory().canWrite()) {
                    final NotifyDescriptor nd = new NotifyDescriptor.Message(
                         NbBundle.getMessage(
                             ProjectHooks.class,
                             "ERR_ProjectReadOnly",
                             project.getProjectDirectory().getName()));
                    DialogDisplayer.getDefault().notify(nd);
                } else {
                    Exceptions.printStackTrace(e);
                }
            }
        }

    }

    private static final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {

        private final PropertyEvaluator eval;
        private final UpdateHelper updateHelper;
        private final GeneratedFilesHelper genFilesHelper;
        private final List<? extends Runnable> preActions;
        private final List<? extends Runnable> postActions;
        private final URL buildImplTemplate;
        private final URL buildTemplate;
        private final String buildScriptProperty;
        private final Callable<Boolean> overridePredicate;

        ProjectXmlSavedHookImpl(
            @NonNull final PropertyEvaluator eval,
            @NonNull final UpdateHelper updateHelper,
            @NonNull final GeneratedFilesHelper genFilesHelper,
            @NonNull final List<? extends Runnable> preActions,
            @NonNull final List<? extends Runnable> postActions,
            @NullAllowed final URL buildImplTemplate,
            @NullAllowed final URL buildTemplate,
            @NonNull final String buildScriptProperty,
            @NullAllowed final Callable<Boolean> overridePredicate) {
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("updateHelper", updateHelper);   //NOI18N
            Parameters.notNull("genFilesHelper", genFilesHelper);   //NOI18N
            Parameters.notNull("preActions", preActions);       //NOI18N
            Parameters.notNull("postActions", postActions);     //NOI18N
            Parameters.notNull("buildScriptProperty", buildScriptProperty); //NOI18N
            this.eval = eval;
            this.updateHelper = updateHelper;
            this.genFilesHelper = genFilesHelper;
            this.preActions = preActions;
            this.postActions = postActions;
            this.buildImplTemplate = buildImplTemplate;
            this.buildTemplate = buildTemplate;
            this.buildScriptProperty = buildScriptProperty;
            this.overridePredicate = overridePredicate;
        }

        @Override
        protected void projectXmlSaved() throws IOException {
            try {
                ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                    @Override
                    public Void run() throws Exception {
                        for (Runnable r : preActions) {
                            runSafe(r);
                        }
                        if (updateHelper.isCurrent()) {
                            //Refresh build-impl.xml only when it's up to date.
                            if (buildImplTemplate != null) {
                                final int state = genFilesHelper.getBuildScriptState(GeneratedFilesHelper.BUILD_IMPL_XML_PATH, buildImplTemplate);
                                boolean forceRewriteBuildImpl = false;
                                if ((shoulOverrideModifiedBuildImpl() && (state & FLAG_MODIFIED) == FLAG_MODIFIED) ||
                                    state == (FLAG_UNKNOWN | FLAG_MODIFIED | FLAG_OLD_PROJECT_XML | FLAG_OLD_STYLESHEET)) {  //missing genfiles.properties
                                    //When the project.xml was changed from the customizer and the build-impl.xml was modified
                                    //move build-impl.xml into the build-impl.xml~ to force regeneration of new build-impl.xml.
                                    //Never do this if it's not a customizer otherwise user modification of build-impl.xml will be deleted
                                    //when the project is opened.
                                    final FileObject projectDir = updateHelper.getAntProjectHelper().getProjectDirectory();
                                    final FileObject buildImpl = projectDir.getFileObject(GeneratedFilesHelper.BUILD_IMPL_XML_PATH);
                                    if (buildImpl  != null) {
                                        final String name = buildImpl.getName();
                                        final String backupext = String.format("%s~",buildImpl.getExt());   //NOI18N
                                        final FileObject oldBackup = buildImpl.getParent().getFileObject(name, backupext);
                                        if (oldBackup != null) {
                                            oldBackup.delete();
                                        }
                                        FileUtil.copyFile(buildImpl, buildImpl.getParent(), name, backupext);
                                        forceRewriteBuildImpl = true;
                                    }
                                }
                                if (forceRewriteBuildImpl) {
                                    genFilesHelper.generateBuildScriptFromStylesheet(
                                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                                        buildImplTemplate);
                                } else {
                                    genFilesHelper.refreshBuildScript(
                                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                                        buildImplTemplate,
                                        false);
                                }
                            }
                            if (buildTemplate != null) {
                                genFilesHelper.refreshBuildScript(
                                    CommonProjectUtils.getBuildXmlName(eval, buildScriptProperty),
                                    buildTemplate,
                                    false);
                            }
                        }
                        for (Runnable r : postActions) {
                            runSafe(r);
                        }
                        return null;
                    }});
            } catch (MutexException e) {
                final Exception inner = e.getException();
                throw inner instanceof IOException ? (IOException) inner : new IOException(inner);
            }
        }

        private boolean shoulOverrideModifiedBuildImpl() {
            try {
                return overridePredicate != null &&
                        overridePredicate.call() == Boolean.TRUE;
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                return false;
            }
        }
    }

    private static void runSafe(@NonNull final Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            } else {
                LOG.log(Level.INFO, "Action exception", t); //NOI18N
            }
        }
    }

}
