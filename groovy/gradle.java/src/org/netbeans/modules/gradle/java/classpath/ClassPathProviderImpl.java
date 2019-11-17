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

package org.netbeans.modules.gradle.java.classpath;

import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupportFactory;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

import static org.netbeans.api.java.classpath.ClassPath.*;
import static org.netbeans.api.java.classpath.JavaClassPathConstants.*;
/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = {ClassPathProvider.class, ProjectOpenedHook.class},
        projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/java-base")
public final class ClassPathProviderImpl extends ProjectOpenedHook implements ClassPathProvider {

    public static final String MODULE_INFO_JAVA = "module-info.java"; // NOI18N
    private static final Set<String> SUPPORTED_PATHS = new HashSet<>(Arrays.asList(
            SOURCE,
            BOOT,
            COMPILE,
            EXECUTE,
            PROCESSOR_PATH,

            MODULE_BOOT_PATH,
            MODULE_COMPILE_PATH,
            MODULE_CLASS_PATH,
            MODULE_EXECUTE_PATH,
            MODULE_EXECUTE_CLASS_PATH
    ));


    private final Map<String, SourceSetCP> groups = new HashMap<>();
    private final Project project;
    private final PropertyChangeListener pcl;

    public ClassPathProviderImpl(Project project) {
        this.project = project;
        this.pcl = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                    GradleJavaProject p = GradleJavaProject.get(ClassPathProviderImpl.this.project);
                    if (p != null) {
                        updateGroups(p.getSourceSets().keySet());
                    } else {
                        //We are no longer a Java Project
                        updateGroups(Collections.<String>emptySet());

                    }
                }
                if (NbGradleProject.PROP_RESOURCES.endsWith(evt.getPropertyName())) {
                    URI uri = (URI) evt.getNewValue();
                    if ((uri != null) && (uri.getPath() != null) && uri.getPath().endsWith(MODULE_INFO_JAVA)) {
                        GradleJavaProject gjp = GradleJavaProject.get(ClassPathProviderImpl.this.project);
                        if (gjp != null) {
                            GradleJavaSourceSet ss = gjp.containingSourceSet(Utilities.toFile(uri));
                            if ((ss != null) && (groups.get(ss.getName()) != null)) {
                                groups.get(ss.getName()).reset();
                            }
                        }
                    }
                }
            }
        };
    }

    @Override
    public ClassPath findClassPath(FileObject fo, String type) {
        GradleJavaProject prj = GradleJavaProject.get(project);
        if (!SUPPORTED_PATHS.contains(type) || (prj == null)) {
            return null;
        }

        GradleJavaSourceSet sourceSet = prj.containingSourceSet(FileUtil.toFile(fo));
        return sourceSet != null ? getSourceSetPath(type, sourceSet) : null;
    }

    private ClassPath getSourceSetPath(String type, GradleJavaSourceSet sourceSet) {
        SourceSetCP scp = groups.get(sourceSet.getName());
        return scp != null ? scp.getClassPath(type) : null;
    }

    @Override
    protected void projectOpened() {
        NbGradleProject.addPropertyChangeListener(project, pcl);
        GradleJavaProject p = GradleJavaProject.get(project);
        if (p != null) {
            updateGroups(p.getSourceSets().keySet());
        }
    }

    @Override
    protected void projectClosed() {
        updateGroups(Collections.<String>emptySet());
        NbGradleProject.removePropertyChangeListener(project, pcl);
    }

    private void updateGroups(Set<String> newGroups) {
        synchronized(groups) {
            Iterator<Map.Entry<String, SourceSetCP>> it = groups.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<String, SourceSetCP> oldGroup = it.next();
                if (!newGroups.contains(oldGroup.getKey())) {
                    it.remove();
                }
            }
            for (String newGroup : newGroups) {
                if (!groups.containsKey(newGroup)) {
                    SourceSetCP scp = new SourceSetCP(newGroup);
                    groups.put(newGroup, scp);
                }
            }
        }
    }

    private class SourceSetCP {
        ClassPath boot;
        ClassPath source;
        ClassPath compile;
        ClassPath runtime;

        ClassPath platformModules;

        ClassPath compileTime;
        ClassPath runTime;

        ClassPath moduleBoot;
        ClassPath moduleCompile;
        ClassPath moduleLegacy;
        ClassPath moduleExecute;
        ClassPath moduleLegacyRuntime;

        final String group;
        List<SourceSetAwareSelector> selectors = new ArrayList<>();

        SourceSetCP(String group) {
            this.group = group;
        }

        public ClassPath getClassPath(String type) {
            switch (type) {
                case BOOT: return getBootClassPath();
                case SOURCE: return getSourcepath();
                case COMPILE: return getCompileTimeClasspath();
                case EXECUTE: return getRuntimeClassPath();

                case MODULE_BOOT_PATH: return getModuleBoothPath();
                case MODULE_COMPILE_PATH: return getModuleCompilePath();
                case MODULE_CLASS_PATH: return getModuleLegacyClassPath();
                case MODULE_EXECUTE_PATH: return getModuleExecutePath();
                case MODULE_EXECUTE_CLASS_PATH: return getModuleLegacyRuntimeClassPath();

                case PROCESSOR_PATH: return getCompileTimeClasspath();

                default: return null;
            }
        }

        private synchronized ClassPath getCompileTimeClasspath() {
            if (compileTime == null) {
                compileTime = createMultiplexClassPath(
                        ClassPathFactory.createClassPath(
                            ClassPathSupportFactory.createModuleInfoBasedPath(
                                    getModuleCompilePath(),
                                    getSourcepath(),
                                    getModuleBoothPath(),
                                    getModuleCompilePath(),
                                    getJava8CompileClassPath(),
                                    null)
                        ),
                        getJava8CompileClassPath()
                );
            }
            return compileTime;
        }

        private synchronized ClassPath getRuntimeClassPath() {
            if (runTime == null) {
                runTime = createMultiplexClassPath(
                        ClassPathFactory.createClassPath(
                            ClassPathSupportFactory.createModuleInfoBasedPath(
                                    getJava8RuntimeClassPath(),
                                    getSourcepath(),
                                    getModuleBoothPath(),
                                    getJava8RuntimeClassPath(),
                                    getJava8RuntimeClassPath(),
                                    null)
                        ),
                        getJava8RuntimeClassPath()
                );
            }
            return runTime;
        }

        private synchronized ClassPath getModuleLegacyClassPath() {
            if (moduleLegacy == null) {
                moduleLegacy = createMultiplexClassPath(EMPTY, getJava8CompileClassPath());
            }
            return moduleLegacy;
        }
        private synchronized ClassPath getModuleExecutePath() {
            if (moduleExecute == null) {
                moduleExecute = createMultiplexClassPath(getJava8RuntimeClassPath(), EMPTY);
            }
            return moduleExecute;
        }

        private synchronized ClassPath getModuleLegacyRuntimeClassPath() {
            if (moduleLegacyRuntime == null) {
                moduleLegacyRuntime = createMultiplexClassPath(EMPTY, getJava8RuntimeClassPath());
            }
            return  moduleLegacyRuntime;
        }

        private synchronized ClassPath getBootClassPath() {
            if (boot == null) {
                boot = ClassPathFactory.createClassPath(new BootClassPathImpl(project, false));
            }
            return boot;
        }

        private synchronized ClassPath getSourcepath() {
            if (source == null) {
                source = ClassPathFactory.createClassPath(new SourceClassPathImpl(project, group));
            }
            return source;
        }

        private synchronized ClassPath getJava8CompileClassPath() {
            if (compile == null) {
                compile = ClassPathFactory.createClassPath(new CompileClassPathImpl(project, group));
            }
            return compile;
        }

        private synchronized ClassPath getPlatformModulesPath() {
            if (platformModules == null) {
                platformModules = ClassPathFactory.createClassPath(new BootClassPathImpl(project, true));
            }
            return platformModules;
        }

        private synchronized ClassPath getJava8RuntimeClassPath() {
            if (runtime == null) {
                runtime = ClassPathFactory.createClassPath(new RuntimeClassPathImpl(project, group));
            }
            return runtime;
        }

        private synchronized ClassPath getModuleBoothPath() {
            if (moduleBoot == null) {
                //TODO: Is this Ok? Made after the Maven's ClassPathProviderImpl.getModuleBootPath
                moduleBoot = createMultiplexClassPath(getPlatformModulesPath(), getPlatformModulesPath());
            }
            return moduleBoot;
        }

        private synchronized ClassPath getModuleCompilePath() {
            if (moduleCompile == null) {
                moduleCompile = createMultiplexClassPath(getJava8CompileClassPath(), ClassPath.EMPTY);
            }
            return moduleCompile;
        }

        private ClassPath createMultiplexClassPath(ClassPath modulePath, ClassPath classPath) {
            SourceSetAwareSelector selector = new SourceSetAwareSelector(modulePath, classPath);
            selectors.add(selector);
            return org.netbeans.spi.java.classpath.support.ClassPathSupport.createMultiplexClassPath(selector);
        }

        public boolean  hasModuleInfo() {
            GradleJavaProject gjp = GradleJavaProject.get(ClassPathProviderImpl.this.project);
            GradleJavaSourceSet ss = gjp.getSourceSets().get(group);
            return ss != null && ss.findResource(MODULE_INFO_JAVA, false, GradleJavaSourceSet.SourceType.JAVA) != null;
        }

        public void reset() {
            for (SourceSetAwareSelector selector : selectors) {
                selector.reset();
            }
        }

        private class SourceSetAwareSelector implements ClassPathSupport.Selector {
            PropertyChangeSupport support = new PropertyChangeSupport(this);

            final ClassPath modulePath;
            final ClassPath classPath;

            ClassPath active;

            public SourceSetAwareSelector(ClassPath modulePath, ClassPath classPath) {
                this.modulePath = modulePath;
                this.classPath = classPath;
            }

            @Override
            public ClassPath getActiveClassPath() {
                if (active == null) {
                    active = hasModuleInfo() ? modulePath : classPath;
                }
                return active;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                support.addPropertyChangeListener(listener);
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                support.removePropertyChangeListener(listener);
            }

            private void reset() {
                ClassPath oldActive = active;
                active = hasModuleInfo() ? modulePath : classPath;
                if (oldActive != active) {
                    support.firePropertyChange(PROP_ACTIVE_CLASS_PATH, null, null);
                }
            }
        }
    }
}
