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

package org.netbeans.modules.gradle.api;

import org.netbeans.modules.gradle.loaders.GradleArtifactStore;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.gradle.GradleModuleFileCache21;
import org.netbeans.modules.gradle.spi.GradleFiles;

/**
 * Utility methods working with Gradle projects and Artifacts.
 *
 * @since 1.0
 * @author Laszlo Kishalmi
 */
public final class GradleProjects {

    private GradleProjects() {}

    /**
     * Get the Source artifact for the given binary if available.
     * @param binary the location of the binary artifact.
     * @return the location of the Source artifact or {@code null} if that is
     * not available.
     */
    public static File getSources(File binary) {
        GradleModuleFileCache21 cache = GradleModuleFileCache21.getGradleFileCache();
        GradleModuleFileCache21.CachedArtifactVersion av = cache.resolveCachedArtifactVersion(binary.toPath());
        GradleModuleFileCache21.CachedArtifactVersion.Entry sources = av != null ? av.getSources() : null;
        return sources != null ? sources.getPath().toFile() : GradleArtifactStore.getDefault().getSources(binary);
    }
    
    public static boolean isGradleCacheArtifact(File toCheck) {
        try {
            GradleModuleFileCache21 cache = GradleModuleFileCache21.getGradleFileCache();
            GradleModuleFileCache21.CachedArtifactVersion av = cache.resolveCachedArtifactVersion(toCheck.toPath());
            return av != null;
        } catch (IllegalArgumentException ex) {
            // expected: not an artifact
            return false;
        }
    }

    /**
     * Get the JavaDoc artifact for the given binary if available.
     * @param binary the location of the binary artifact.
     * @return the location of the JavaDoc artifact or {@code null} if that is
     * not available.
     */
    public static File getJavadoc(File binary) {
        GradleModuleFileCache21 cache = GradleModuleFileCache21.getGradleFileCache();
        GradleModuleFileCache21.CachedArtifactVersion av = cache.resolveCachedArtifactVersion(binary.toPath());
        GradleModuleFileCache21.CachedArtifactVersion.Entry javadoc = av != null ? av.getJavaDoc() : null;
        return javadoc != null ? javadoc.getPath().toFile() : GradleArtifactStore.getDefault().getJavadoc(binary);
    }

    /**
     * Returns all the siblings project of the given project which are opened in the IDE.
     * It gives empty result on non-Gradle projects and root Gradle projects.
     * The map also won't contain the given project itself.
     *
     * @param project a project.
     * @return an unmodifiable {@code <name, project>} map of the siblings, never {@code null}
     */
    public static Map<String, Project> openedSiblings(Project project) {
        Map<String, Project> ret = new HashMap<>();
        GradleBaseProject gbp = GradleBaseProject.get(project);
        if (gbp != null) {
            for (Project openProject : OpenProjects.getDefault().getOpenProjects()) {
                GradleBaseProject test = GradleBaseProject.get(openProject);
                if ((test != null) && gbp.isSibling(test)) {
                    ret.put(test.getName(), openProject);
                }
            }
        }

        return Collections.unmodifiableMap(ret);
    }

    /**
     * Returns all the projects on which the given project depends and are opened in the IDE.
     * It gives empty result on non-Gradle projects. On root project it returns all its
     * opened sub-projects. The map also won't contain the given project itself.
     *
     * @param project a project.
     * @return an unmodifiable {@code <name, project>} map of dependencies, never {@code null}
     */
    public static Map<String, Project> openedProjectDependencies(Project project) {
        Map<String, Project> ret = new HashMap<>();
        GradleBaseProject gbp = GradleBaseProject.get(project);
        if (gbp != null) {
            if (gbp.isRoot()) {
                Map<String, File> subProjects = gbp.getSubProjects();
                for (Project openProject : OpenProjects.getDefault().getOpenProjects()) {
                    GradleBaseProject test = GradleBaseProject.get(openProject);
                    if ((test != null) && gbp.isRootOf(test)) {
                        ret.put(test.getName(), openProject);
                    }
                }

            } else {
                Map<String, Project> siblings = openedSiblings(project);
                collectProjectDependencies(ret, siblings, project);
            }
        }

        return Collections.unmodifiableMap(ret);
    }

    /**
     * Try to determine if the given directory belongs to a Gradle project.
     * This method use heuristics and usual project layout of project files.
     * The returned value is not necessary correct.
     *
     * @param dir the directory to test
     * @return true if the given directory is suspected as a Gradle project.
     */
    public static boolean testForProject(File dir) {
        return new GradleFiles(dir).isProject();
    }

    /**
     * Try to determine if the given directory belongs to a Gradle root project.
     * This method use heuristics and usual project layout of project files.
     * The returned value is not necessary correct.
     *
     * @param dir the directory to test
     * @return true if the given directory is suspected as a Gradle root project.
     */
    public static boolean testForRootProject(File dir) {
        return new GradleFiles(dir).isRootProject();
    }

    private static void collectProjectDependencies(final Map<String, Project> ret, Map<String, Project> siblings, final Project prj) {
        GradleBaseProject gbp = GradleBaseProject.get(prj);
        for (GradleDependency.ProjectDependency dep : gbp.getProjectDependencies()) {
            String id = dep.getId();
            if (!ret.containsKey(id) && siblings.containsKey(id)) {
                Project test = siblings.get(id);
                ret.put(id, test);
                collectProjectDependencies(ret, siblings, test);
            }
        }
    }

}
