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

import org.netbeans.modules.gradle.java.api.ProjectSourcesClassPathProvider;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import static org.netbeans.spi.java.classpath.ClassPathFactory.createClassPath;
import org.netbeans.spi.project.ui.ProjectOpenedHook;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GlobalClassPathProviderImpl extends ProjectOpenedHook implements ProjectSourcesClassPathProvider {

    private static final Logger LOG = Logger.getLogger(GlobalClassPathProviderImpl.class.getName());

    final Project project;

    private static final int BOOT = 0;
    private static final int SOURCE = 2;
    private static final int COMPILE = 4;
    private static final int RUNTIME = 6;

    final ClassPath[] cache = new ClassPath[RUNTIME + 2];

    public GlobalClassPathProviderImpl(Project project) {
        this.project = project;
    }

    @Override
    public ClassPath getProjectSourcesClassPath(String type) {
        return getClassPath(type, true);
    }

    @Override
    public ClassPath[] getProjectClassPath(String type) {
        ClassPath instance = getClassPath(type, false);
        return instance != null ? new ClassPath[] {instance} : new ClassPath[0];
    }

    private ClassPath getClassPath(String type, boolean excludeTests) {
        int index = type2Index(type, excludeTests);
        if (index < 0) return null;
        ClassPath cp = cache[index];
        if (cp == null) {
            cp = switch (type) {
                case ClassPath.BOOT -> createClassPath(new BootClassPathImpl(project, null));
                case ClassPath.SOURCE -> createClassPath(new GradleGlobalClassPathImpl.ProjectSourceClassPathImpl(project, excludeTests));
                case ClassPath.COMPILE -> createClassPath(new GradleGlobalClassPathImpl.ProjectCompileClassPathImpl(project, excludeTests));
                case ClassPath.EXECUTE -> createClassPath(new GradleGlobalClassPathImpl.ProjectRuntimeClassPathImpl(project, excludeTests));
                default -> null;
            };
            cache[index] = cp;
        }
        return cp;
    }

    private static int type2Index(String type, boolean excludeTests) {
        int index;
        index = switch (type) {
            case ClassPath.BOOT -> BOOT;
            case ClassPath.SOURCE -> SOURCE;
            case ClassPath.COMPILE -> COMPILE;
            case ClassPath.EXECUTE -> RUNTIME;
            default -> -1;
        };

        return (index >= 0) && excludeTests ? index + 1 : index;
    }

    @Override
    protected void projectOpened() {
        GlobalPathRegistry.getDefault().register(ClassPath.BOOT, getProjectClassPath(ClassPath.BOOT));
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, getProjectClassPath(ClassPath.SOURCE));
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, getProjectClassPath(ClassPath.COMPILE));
        GlobalPathRegistry.getDefault().register(ClassPath.EXECUTE, getProjectClassPath(ClassPath.EXECUTE));
    }

    @Override
    protected void projectClosed() {
        safeUnregister(ClassPath.BOOT, getProjectClassPath(ClassPath.BOOT));
        safeUnregister(ClassPath.SOURCE, getProjectClassPath(ClassPath.SOURCE));
        safeUnregister(ClassPath.COMPILE, getProjectClassPath(ClassPath.COMPILE));
        safeUnregister(ClassPath.EXECUTE, getProjectClassPath(ClassPath.EXECUTE));
        Arrays.fill(cache, null);
    }

    private void safeUnregister(String id, ClassPath[] paths) {
        try {
            GlobalPathRegistry.getDefault().unregister(id, paths);
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.INFO, "Could not unregister {0} from {1}", new Object[] {paths, id});
        }
    }
}
