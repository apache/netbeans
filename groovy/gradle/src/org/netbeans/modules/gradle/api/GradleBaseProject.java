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

package org.netbeans.modules.gradle.api;

import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.modules.gradle.api.execute.RunUtils;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileUtil;

/**
 * This object holds the basic information of the Gradle project.
 * <p>
 * Note: Caching / storing this object inn a member field is discouraged. Use
 * {@link GradleBaseProject#get(Project)} instead.
 * </p>
 * @see @see <a href="https://docs.gradle.org/current/javadoc/org/gradle/api/Project.html">org.gradle.api.Project</a>
 * @since 1.0
 * @author Laszlo Kishalmi
 */
public final class GradleBaseProject implements Serializable, ModuleSearchSupport {
    public static final String PRIVATE_TASK_GROUP = "<private>"; //NOI18N

    String name;
    String group = "";
    String description;
    String version;
    String path;
    String status;
    String parentName;
    String displayName;

    File buildDir;
    File projectDir;
    File rootDir;
    String license;
    Set<String> plugins = Collections.emptySet();
    Set<File> gradleClassPath = Collections.emptySet();
    Set<File> buildClassPath = Collections.emptySet();
    Map<String, File> subProjects = Collections.emptyMap();
    Map<String, File> includedBuilds = Collections.emptyMap();
    Map<String, List<GradleTask>> tasksByGroup = new HashMap<>();
    Map<String, GradleTask> tasksByName = new HashMap<>();
    Map<String, String> netBeansProperties = Collections.<String, String>emptyMap();
    Map<File, GradleDependency.ModuleDependency> componentsByFile = new HashMap<>();
    Map<String, GradleConfiguration> configurations = new HashMap<>();
    Set<File> outputPaths = Collections.emptySet();

    transient Boolean resolved = null;

    GradleBaseProject() {
    }

    /**
     * This Gradle project name.
     *
     *
     *
     * @return the name of the project
     */
    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getStatus() {
        return status;
    }

    public String getParentName() {
        return parentName;
    }

    public String getGroup() {
        return group;
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getVersion() {
        return version;
    }

    public File getBuildDir() {
        return buildDir;
    }

    public Set<File> getOutputPaths() {
        return outputPaths;
    }

    public File getProjectDir() {
        return projectDir;
    }

    /**
     * The set of Gradle plugin ids used applied in this project.
     *
     * @return the set of applied plugins
     */
    public Set<String> getPlugins() {
        return plugins;
    }

    /**
     * The project directory of the root project.
     * @return the root project directory.
     */
    public File getRootDir() {
        return rootDir;
    }

    /**
     * The classpath used to
     * @return
     */
    public Set<File> getGradleClassPath() {
        return gradleClassPath;
    }

    public Set<File> getBuildClassPath() {
        return buildClassPath;
    }

    public Map<String, File> getIncludedBuilds() {
        return includedBuilds;
    }

    /**
     * Returns true if the project directory is the same as the root project's
     * project directory, in short if this project is a root project.
     *
     * @return true for root Gradle projects
     */
    public boolean isRoot() {
        return projectDir.equals(rootDir);
    }

    /**
     * Return the value of the property defined {@code netbeans.<key>} as
     * in this Gradle project or inherited from the root project.
     *
     * @param key the property name after the {@code netbeans.} prefix.
     * @return the property value or {@code null} if it is not defined.
     */
    public String getNetBeansProperty(String key) {
        return netBeansProperties.get(key);
    }

    /**
     * Returns the license identifier used in this Gradle project. It is
     * determined by the {@code netbeans.license} property of the project,
     * if that's not found {@code license} property is used. If even that
     * would be undefined the license {@code "default"} is used.
     * @return the project defined license or {@code default} if no license has
     *         been specified.
     */
    public String getLicense() {
        return license != null ? license : "default"; //NOI18N
    }

    /**
     * Returns the sub-project of this project, in a map as Gradle project path
     * and project directory pairs. In the current Gradle implementation only
     * root project can have sub-projects.
     *
     * @return the map of sub-projects.
     */
    public Map<String, File> getSubProjects() {
        return subProjects;
    }

    public Set<String> getTaskGroups() {
        return Collections.unmodifiableSet(tasksByGroup.keySet());
    }

    public List<GradleTask> getTasks(String group) {
        List<GradleTask> ret = tasksByGroup.get(group);
        return ret != null ? Collections.unmodifiableList(ret) : Collections.<GradleTask>emptyList();
    }

    public List<GradleTask> getTasks() {
        return Collections.unmodifiableList(new ArrayList<>(tasksByName.values()));
    }

    public Set<String> getTaskNames() {
        return Collections.unmodifiableSet(tasksByName.keySet());
    }

    public GradleTask getTaskByName(String name) {
        return tasksByName.get(name);
    }

    public Map<String, GradleConfiguration> getConfigurations() {
        return Collections.unmodifiableMap(configurations);
    }

    public Set<GradleDependency.ProjectDependency> getProjectDependencies() {
        Set<GradleDependency.ProjectDependency> ret = new HashSet<>();
        for (GradleConfiguration conf : configurations.values()) {
            ret.addAll(conf.getProjects());
        }
        return Collections.unmodifiableSet(ret);
    }

    public boolean hasPlugins(String... plugins) {
        for (String plugin : plugins) {
            if (!this.plugins.contains(plugin)) return false;
        }
        return true;
    }

    @Override
    public Set<GradleDependency.ModuleDependency> findModules(String group, String artifact, String version) {
        Set<GradleDependency.ModuleDependency> ret = new HashSet<>();
        for (GradleConfiguration conf : configurations.values()) {
            ret.addAll(conf.findModules(group, artifact, version));
        }
        return Collections.unmodifiableSet(ret);
    }

    @Override
    public Set<GradleDependency.ModuleDependency> findModules(String gav) {
        String parts[] = GradleDependency.gavSplit(gav);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid gav filter: "  + gav);
        }
        String groupId = parts[0].isEmpty() ? null : parts[0];
        String artifactId = parts[1].isEmpty() ? null : parts[1];
        String ver = parts[2].isEmpty() ? null : parts[2];

        return findModules(groupId, artifactId, ver);
    }

    /**
     * Returns {@code true} if all configurations are resolved.
     * @return true - if all configurations are resolved.
     */
    public boolean isResolved() {
        if (resolved == null) {
            boolean b = true;
            for (GradleConfiguration value : configurations.values()) {
                b &= value.isResolved();
                if (!b) {
                    break;
                }
            }
            resolved = b;
        }
        return resolved;
    }

    public boolean isRootOf(GradleBaseProject other) {
        if (other == null) return false;
        return isRoot()
                && !other.isRoot()
                && subProjects.containsKey(other.name)
                && projectDir.equals(other.rootDir);
    }

    public boolean isSibling(GradleBaseProject other) {
        if (other == null) return false;
        return !isRoot()
                && !other.isRoot()
                && rootDir.equals(other.rootDir)
                && !projectDir.equals(other.projectDir);
    }

    GradleConfiguration createConfiguration(String name) {
        GradleConfiguration conf = new GradleConfiguration(name);
        configurations.put(name, conf);
        return conf;
    }

    /**
     * Retrieve the actual GradleBaseProject from the given NetBeans
     * {@link Project project} lookup. If the project is not a Gradle project it
     * returns {@code null}. It always returns a non-null value for a Gradle
     * project.
     *
     * @see org.netbeans.modules.gradle.api.NbGradleProject.Quality
     *
     * @param project a NetBeans project
     * @return the basic Gradle project info stored for the given project or
     *         {@code null} for non-Gradle projects.
     */
    public static GradleBaseProject get(Project project) {
        NbGradleProject gp = NbGradleProject.get(project);
        return gp != null ? gp.projectLookup(GradleBaseProject.class) : null;
    }

    static GradleBaseProject getFallback(GradleFiles files) {
        GradleBaseProject ret = new GradleBaseProject();

        ret.name = files.getProjectDir().getName();
        ret.projectDir = files.getProjectDir();
        ret.buildDir = new File(files.getProjectDir(), "build");
        ret.rootDir = files.getRootDir();
        ret.version = "unspecified";
        StringBuilder path = new StringBuilder(":");       //NOI18N
        if (!files.isRootProject()) {
            Path prjPath = files.getProjectDir().toPath();
            Path rootPath = files.getRootDir().toPath();
            String separator = "";
            Path relPath = rootPath.relativize(prjPath);
            for(int i = 0; i < relPath.getNameCount() ; i++) {
                path.append(separator);
                path.append(relPath.getName(i));
                separator = ":"; //NOI18N
            }
        }
        ret.path = path.toString();
        ret.status = "release";
        ret.parentName = files.isRootProject() ? null : files.getRootDir().getName();

        //Make advanced compile options disabled by default.
        Map<String, String> nbprops = new HashMap<>();
        nbprops.put(RunUtils.PROP_AUGMENTED_BUILD, "false"); //NOI18N
        nbprops.put(RunUtils.PROP_COMPILE_ON_SAVE, "false"); //NOI18N
        ret.netBeansProperties = Collections.<String, String>unmodifiableMap(nbprops);

        Map<String, File> subPrj = Collections.emptyMap();
        if (files.isRootProject() && (files.getSettingsScript() != null)) {
            Set<File> subProjects = GradleFiles.SettingsFile.getSubProjects(files.getSettingsScript());
            if (!subProjects.isEmpty()) {
                subPrj = new HashMap<>();
                for (File sp : subProjects) {
                    subPrj.put(sp.getName(), sp);
                }
            }
        }
        ret.subProjects = Collections.unmodifiableMap(subPrj);

        Set<String> plugins = new HashSet<>();

        File srcDir = new File(files.getProjectDir(), "src");

        if (srcDir.isDirectory()) {
            Set<String> setNames = new HashSet<>();
            File[] sourceSets = srcDir.listFiles();
            for (File dir : sourceSets) {
                if (dir.isDirectory()) {
                    if ("dist".equals(dir.getName())) {
                        plugins.add("distribution");
                    }
                    if (new File(dir, "java").isDirectory()) {
                        plugins.add("java");
                    }
                    if (new File(dir, "resources").isDirectory()) {
                        plugins.add("java");
                    }
                    if (new File(dir, "groovy").isDirectory()) {
                        plugins.add("java");
                        plugins.add("groovy");
                        plugins.add("groovy-base");
                    }
                    if (new File(dir, "scala").isDirectory()) {
                        plugins.add("java");
                        plugins.add("scala");
                        plugins.add("scala-base");
                    }
                    if (new File(dir, "webapp").isDirectory()) {
                        plugins.add("java");
                        plugins.add("war");
                    }
                }
            }
            if (plugins.contains("java")) {
                plugins.add("java-base");
                plugins.add("base");
            }
            ret.plugins = Collections.unmodifiableSet(plugins);
        }
        return ret;
    }

    @Override
    public String toString() {
        return "GradleBaseProject{" + "name=" + name + ", projectDir=" + projectDir + ", plugins=" + plugins + '}';
    }


}
