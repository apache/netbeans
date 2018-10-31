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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = {ClassPathProvider.class, ProjectOpenedHook.class},
        projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/java-base")
public final class ClassPathProviderImpl extends ProjectOpenedHook implements ClassPathProvider {

    private static final Set<String> SUPPORTED_PATHS = new HashSet<>();


    static {
        SUPPORTED_PATHS.add(ClassPath.SOURCE);
        SUPPORTED_PATHS.add(ClassPath.BOOT);
        SUPPORTED_PATHS.add(ClassPath.COMPILE);
        SUPPORTED_PATHS.add(ClassPath.EXECUTE);
        SUPPORTED_PATHS.add(JavaClassPathConstants.PROCESSOR_PATH);
    }

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
        final ClassPath boot;
        final ClassPath source;
        final ClassPath compile;
        final ClassPath runtime;

        SourceSetCP(String group) {
            boot = ClassPathFactory.createClassPath(new BootClassPathImpl(project));
            source = ClassPathFactory.createClassPath(new SourceClassPathImpl(project, group));
            compile = ClassPathFactory.createClassPath(new CompileClassPathImpl(project, group));
            runtime = ClassPathFactory.createClassPath(new RuntimeClassPathImpl(project, group));
        }

        public ClassPath getClassPath(String type) {
            switch (type) {
                case ClassPath.BOOT: return boot;
                case ClassPath.SOURCE: return source;
                case ClassPath.COMPILE: return compile;
                case ClassPath.EXECUTE: return runtime;
                case JavaClassPathConstants.PROCESSOR_PATH: return compile;
                default: return null;
            }
        }

    }

}
