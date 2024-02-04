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
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

import static org.netbeans.api.java.classpath.ClassPath.*;
import static org.netbeans.api.java.classpath.JavaClassPathConstants.*;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
/**
 *
 * @author Laszlo Kishalmi
 */
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


    private final Project project;
    private final PropertyChangeListener pcl;
    private final PropertyChangeListener wPcl;

    // @GuardedBy(this)
    private volatile Map<String, SourceSetCP> groups = new HashMap<>();

    public ClassPathProviderImpl(Project project) {
        this.project = project;
        this.pcl = (PropertyChangeEvent evt) -> {
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                updateGroups();
            }
            if (NbGradleProject.PROP_RESOURCES.equals(evt.getPropertyName())) {
                URI uri = (URI) evt.getNewValue();
                updateResources(uri);
            }
        };
        // by some miracle, the project might have been loaded!
        NbGradleProject gp = NbGradleProject.get(project);
        this.wPcl = WeakListeners.propertyChange(pcl, null, gp);
        if (gp.isGradleProjectLoaded()) {
            updateGroups();
        }
        gp.addPropertyChangeListener(wPcl);
    }
    
    private void updateResources(URI uri) {
        if ((uri != null) && (uri.getPath() != null) && uri.getPath().endsWith(MODULE_INFO_JAVA)) {
            GradleJavaProject gjp = GradleJavaProject.get(project);
            if (gjp != null) {
                GradleJavaSourceSet ss = gjp.containingSourceSet(Utilities.toFile(uri));
                if (ss == null) {
                    return;
                }
                SourceSetCP ssp = groups.get(ss.getName());
                if (ssp != null) {
                    // reset may fire events
                    ssp.reset();
                }
            }
        }
    }
    
    private void updateGroups() {
        GradleJavaProject p = GradleJavaProject.get(ClassPathProviderImpl.this.project);
        if (p != null) {
            updateGroups(p.getSourceSets().keySet());
        } else {
            //We are no longer a Java Project
            updateGroups(Collections.<String>emptySet());
        }
    }
    
    /**
     * If true, the impl has attempted project load upon CP request.
     * Will try once.
     */
    private boolean lateProjectLoadAttempted;

    @NbBundle.Messages({
        "MSG_ObtainClasspath=Attempting to obtain classpath"
    })
    @Override
    public ClassPath findClassPath(FileObject fo, String type) {
        GradleJavaProject prj = GradleJavaProject.get(project);
        if (!SUPPORTED_PATHS.contains(type) || (prj == null)) {
            return null;
        }
        NbGradleProject ngp = NbGradleProject.get(project);
        if (ngp != null) {
            boolean attempt;
            if (ngp.getQuality().worseThan(NbGradleProject.Quality.EVALUATED)) {
                synchronized (this) {
                    attempt = !lateProjectLoadAttempted;
                    lateProjectLoadAttempted = true;
                }
            } else {
                attempt = false;
            }
            if (attempt) {
                // someone is asking for a classpath. Try to go for FULL at least. Runs asynchronously.
                ngp.toQuality(Bundle.MSG_ObtainClasspath(), NbGradleProject.Quality.FULL, false).
                    thenAccept((p) -> {
                        if (p.getQuality().atLeast(NbGradleProject.Quality.EVALUATED)) {
                            updateGroups();
                        }
                    });
            }
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
        GradleJavaProject p = GradleJavaProject.get(project);
        if (p != null) {
            updateGroups(p.getSourceSets().keySet());
        }
    }

    @Override
    protected void projectClosed() {
        NbGradleProject.removePropertyChangeListener(project, pcl);
    }

    private void updateGroups(Set<String> newGroups) {
        // Note: ClassPathProviderImplTest timing relies on the provider instance locked
        synchronized(this) {
            Map<String, SourceSetCP> g = new HashMap<>(groups);
            Iterator<Map.Entry<String, SourceSetCP>> it = g.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<String, SourceSetCP> oldGroup = it.next();
                if (!newGroups.contains(oldGroup.getKey())) {
                    it.remove();
                }
            }
            for (String newGroup : newGroups) {
                if (!g.containsKey(newGroup)) {
                    SourceSetCP scp = new SourceSetCP(newGroup);
                    g.put(newGroup, scp);
                }
            }
            this.groups = g;
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
        ClassPath annotationProcessor;

        ClassPath moduleBoot;
        ClassPath moduleCompile;
        ClassPath moduleAnnotationProcessor;
        ClassPath moduleLegacy;
        ClassPath moduleExecute;
        ClassPath moduleLegacyRuntime;

        final String group;
        
        // @GuardedBy(this)
        final List<SourceSetAwareSelector> selectors = new ArrayList<>();

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

                case PROCESSOR_PATH: return getJava8AnnotationProcessorPath();

                default: return null;
            }
        }

        // Note: ClassPathProviderImplTest timing relies on the provider instance locked
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
                boot = ClassPathFactory.createClassPath(new BootClassPathImpl(project, group, false));
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

        private synchronized ClassPath getJava8AnnotationProcessorPath() {
            if (annotationProcessor == null) {
                annotationProcessor = ClassPathFactory.createClassPath(new AnnotationProcessorPathImpl(project, group));
            }
            return annotationProcessor;
        }

        private synchronized ClassPath getPlatformModulesPath() {
            if (platformModules == null) {
                platformModules = ClassPathFactory.createClassPath(new BootClassPathImpl(project, group, true));
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
                moduleBoot = createMultiplexClassPath(getPlatformModulesPath(), getBootClassPath());
            }
            return moduleBoot;
        }

        private synchronized ClassPath getModuleCompilePath() {
            if (moduleCompile == null) {
                moduleCompile = createMultiplexClassPath(getJava8CompileClassPath(), getJava8CompileClassPath());
            }
            return moduleCompile;
        }

        // @GuardedBy(this)
        private ClassPath createMultiplexClassPath(ClassPath modulePath, ClassPath classPath) {
            SourceSetAwareSelector selector = new SourceSetAwareSelector(modulePath, classPath);
            selectors.add(selector);
            return org.netbeans.spi.java.classpath.support.ClassPathSupport.createMultiplexClassPath(selector);
        }

        public boolean  hasModuleInfo() {
            GradleJavaProject gjp = GradleJavaProject.get(ClassPathProviderImpl.this.project);
            GradleJavaSourceSet ss = gjp != null ? gjp.getSourceSets().get(group) : null;
            return ss != null && ss.findResource(MODULE_INFO_JAVA, false, GradleJavaSourceSet.SourceType.JAVA) != null;
        }

        public void reset() {
            List<SourceSetAwareSelector> copy;
            synchronized (this) {
                copy = new ArrayList<>(selectors);
            }
            // fire events outside lock
            for (SourceSetAwareSelector selector : copy) {
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
                synchronized (this) {
                    ClassPath oldActive = active;
                    active = hasModuleInfo() ? modulePath : classPath;
                    if (oldActive == active) {
                        return;
                    }
                }
                support.firePropertyChange(PROP_ACTIVE_CLASS_PATH, null, null);
            }
        }
    }
}
