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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.GradleModuleFileCache21;
import org.netbeans.modules.gradle.cache.SubProjectDiskCache;
import org.netbeans.modules.gradle.cache.SubProjectDiskCache.SubProjectInfo;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;

/**
 * This object holds the basic information of the Gradle project.
 * <p>
 * Note: Caching / storing this object inn a member field is discouraged. Use
 * {@link GradleBaseProject#get(Project)} instead.
 * </p>
 * @see <a href="https://docs.gradle.org/current/javadoc/org/gradle/api/Project.html">org.gradle.api.Project</a>
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
    Map<String, String> projectIds = Collections.emptyMap();
    GradleDependency projectDependencyNode;
    Set<GradleReport> problems = Collections.emptySet();
    Map<String, List<String>> taskDependencies = new HashMap<>();
    Map<String, Set<String>> taskTypes = new HashMap<>();
    
    // @GuardedBy(this)
    /**
     * Lazy-computed list of tasks mapped to either tasksByName or created descriptions.
     */
    private transient Map<String, List<GradleTask>> taskDeepDependencies = new HashMap<>();

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
    
    public boolean isVersionSpecified() {
        return version != null && !"".equals(version) && !"unspecified".equals(version); // NOI18N
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
     * Return the list of problems reported by Gradle on
     * project inspection. In an ideal case that should be an
     * empty set.
     *
     * @return Gradle reported problems during inspection.
     * 
     * @since 2.27
     */
    public Set<GradleReport> getProblems() {
        return problems;
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
    
    /**
     * Finds a GAV for the given project. Returns {@code null} if the project path
     * is not known (it is not referenced anywhere by this project), or has no known
     * GAV. The project's own GAV should be always present, if defined by the project 
     * file(s).
     * 
     * @param projectPath Gradle project path
     * @return GAV coordinates, or {@code null}
     * @since 2.27
     */
    public String findProjectGav(@NonNull String projectPath) {
        if ("".equals(projectPath) || getPath().equals(projectPath)) {
            String n = getName();
            String g = getGroup();
            String v = getVersion();
            if (n == null || n.isEmpty() || g == null || g.isEmpty() || v == null || v.isEmpty() || "unspecified".equals(v)) {  // NOI18N
                return null;
            }
            return String.format("%s:%s:%s", g, n, v);
        }
        return projectIds.get(projectPath);
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
    
    /**
     * For a given task, lists all predecessors of the task within the current project.
     * If tasks from other projects are referenced, they are returned as mock objects,
     * that have no group and no description and no dependencies.
     * <p>
     * The result is partially sorted so that dependencies always precede the dependent
     * task.
     * 
     * @param gt gradle task to inspect
     * @param directs if true, only direct predecessors are returned
     * @return list of predecessor tasks.
     * @since 2.28
     */
    public List<GradleTask> getTaskPredecessors(GradleTask gt, boolean directs) {
        // sanity check to rule out tasks from other projects or mocks.
        if (gt.getName() == null || getTaskByName(gt.getName()) != gt) {
            return Collections.emptyList();
        }
        // do not cache direct dependencies, they're cheap.
        if (!directs) {
            synchronized (this) {
                List<GradleTask> cached = taskDeepDependencies.get(gt.getName());
                if (cached != null) {
                    return cached;
                }
            }
        }
        Set<String> paths = new HashSet<>();
        Queue<String> toProcess = new ArrayDeque<>();
        toProcess.add(gt.getPath());
        
        String taskPath;
        Map<String, String> taskNamesAndPaths = new HashMap<>();
        boolean first = true;
        Set<String> ownTasks = new HashSet<>();
        while ((taskPath = toProcess.poll()) != null) {
            if (taskPath.equals("") || !paths.add(taskPath)) {
                continue;
            }
            int lastColon = taskPath.lastIndexOf(':');
            // path for the root project (lastColon == 0) is ":". Path for any subproject must not contain a possible colon delimiter between project path and the task.
            String p = taskPath.substring(0, Math.max(1, lastColon));
            String n = taskPath.substring(lastColon + 1);
            taskNamesAndPaths.put(taskPath, n);
            if (path.equals(p)) {
                ownTasks.add(taskPath);
                if (!directs || first){
                    // if directs, allow just the 1st level to be added to toProcess.
                    toProcess.addAll(taskDependencies.getOrDefault(n, Collections.emptyList()));
                }
            }
            first = false;
        }
        paths.remove(gt.getPath());
        
        Map<String, List<String>> edges = new HashMap<>();
        for (String tn : ownTasks) {
            String sn = taskNamesAndPaths.get(tn);
            for (String pred : taskDependencies.getOrDefault(sn, Collections.emptyList())) {
                if (pred.isEmpty()) {
                    continue;
                }
                edges.computeIfAbsent(pred, (k) -> new ArrayList<>()).add(tn);
            }
        }
        List<String> orderedTasks;
        
        try {
            orderedTasks = Utilities.topologicalSort(paths, edges);
        } catch (TopologicalSortException ex) {
            orderedTasks = new ArrayList<>(taskNamesAndPaths.keySet());
        }
        List<GradleTask> result = new ArrayList<>();
        for (String p : orderedTasks) {
            String n = taskNamesAndPaths.get(p);
            GradleTask toAdd = null;
            
            if (ownTasks.contains(p)) {
                toAdd = getTaskByName(n);
            }
            if (toAdd == null) {
                toAdd = new GradleTask(p, n);
            }
            result.add(toAdd);
        }
        if (!directs) {
            synchronized (this) {
                taskDeepDependencies.putIfAbsent(gt.getName(), result);
            }
        }
        return result;
    }
    
    /**
     * Determines if a task is a subtype of a certain Gradle type. 
     * User-defined tasks may define different names, but the can be identified
     * by the gradle type they extend or implement.
     * Use fully qualified API class names for {@code gradleFQN} parameter.
     * @param gradleFQN the fully qualified type name
     * @return true, if the task type matches.
     * @since 2.28
     */
    public boolean isTaskInstanceOf(String name, String gradleFQN) {
        Set s = taskTypes.get(name);
        return s == null ? false : s.contains(gradleFQN);
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
    public Set<GradleDependency.ModuleDependency> findModules(String gav) throws IllegalArgumentException {
        String parts[] = GradleModuleFileCache21.gavSplit(gav);
        String groupId = parts[0].isEmpty() ? null : parts[0];
        String artifactId = parts[1].isEmpty() ? null : parts[1];
        String ver = parts[2].isEmpty() ? null : parts[2];

        return findModules(groupId, artifactId, ver);
    }

    /**
     * Returns {@code true} if all resolvable configurations are resolved.
     * @return true - if all resolvable configurations are resolved.
     */
    public boolean isResolved() {
        if (resolved == null) {
            boolean b = true;
            for (GradleConfiguration value : configurations.values()) {
                if (value.isCanBeResolved()) {
                    b &= value.isResolved();
                    if (!b) {
                        break;
                    }
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
        SubProjectInfo structure = SubProjectDiskCache.get(files.getRootDir()).loadData();
        if (structure != null) {
            // Note: The structure information might be invalid, though we are just guessing here
            ret.path = structure.getProjectPath(files.getProjectDir());
            ret.description = structure.getProjectDescription(files.getProjectDir());
            ret.name = structure.getProjectName(files.getProjectDir());
        }
        if (ret.path == null) {
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
        }
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
