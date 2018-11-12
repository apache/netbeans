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

import org.netbeans.modules.gradle.api.execute.RunUtils;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;

/**
 *
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

    public Set<String> getPlugins() {
        return plugins;
    }

    public File getRootDir() {
        return rootDir;
    }

    public Set<File> getGradleClassPath() {
        return gradleClassPath;
    }

    public Set<File> getBuildClassPath() {
        return buildClassPath;
    }

    public Map<String, File> getIncludedBuilds() {
        return includedBuilds;
    }
    
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
        String parts[] = gav.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid gav filter: "  + gav);
        }
        String groupId = parts[0].isEmpty() ? null : parts[0];
        String artifactId = parts[1].isEmpty() ? null : parts[1];
        String ver = parts[2].isEmpty() ? null : parts[2];

        return findModules(groupId, artifactId, ver);
    }

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

    public static GradleBaseProject get(Project project) {
        NbGradleProject gp = NbGradleProject.get(project);
        return gp != null ? get(gp) : null;
    }

    public static GradleBaseProject get(NbGradleProject project) {
        return project.projectLookup(GradleBaseProject.class);
    }

    public static GradleBaseProject getFallback(GradleFiles files) {
        GradleBaseProject ret = new GradleBaseProject();

        ret.name = files.projectDir.getName();
        ret.projectDir = files.projectDir;
        ret.buildDir = new File(files.projectDir, "build");
        ret.rootDir = files.rootDir;
        ret.version = "unspecified";
        ret.path = files.isRootProject() ? ":" : ":" + ret.name;
        ret.status = "release";
        ret.parentName = files.isRootProject() ? null : files.rootDir.getName();

        //Make advanced compile options disabled by default.
        Map<String, String> nbprops = new HashMap<>();
        nbprops.put(RunUtils.PROP_AUGMENTED_BUILD, "false"); //NOI18N
        nbprops.put(RunUtils.PROP_COMPILE_ON_SAVE, "false"); //NOI18N
        ret.netBeansProperties = Collections.<String, String>unmodifiableMap(nbprops);

        Map<String, File> subPrj = Collections.emptyMap();
        if (files.isRootProject() && (files.settingsScript != null)) {
            Set<File> subProjects = GradleFiles.SettingsFile.getSubProjects(files.settingsScript);
            if (!subProjects.isEmpty()) {
                subPrj = new HashMap<>();
                for (File sp : subProjects) {
                    subPrj.put(sp.getName(), sp);
                }
            }
        }
        ret.subProjects = Collections.unmodifiableMap(subPrj);

        Set<String> plugins = new HashSet<>();

        File srcDir = new File(files.projectDir, "src");

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
