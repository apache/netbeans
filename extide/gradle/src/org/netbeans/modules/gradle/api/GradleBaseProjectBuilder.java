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
import org.netbeans.modules.gradle.GradleArtifactStore;
import static org.netbeans.modules.gradle.api.GradleDependency.*;
import org.netbeans.modules.gradle.spi.ProjectInfoExtractor;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.openide.util.lookup.ServiceProvider;

/**
 * This is the de-serialization support class for GradleBaseProject.
 *
 * @author Laszlo Kishalmi
 */
@SuppressWarnings("unchecked")
class GradleBaseProjectBuilder implements ProjectInfoExtractor.Result {

    final static Map<String, List<String>> DEPENDENCY_TO_PLUGIN = new LinkedHashMap<>();

    static {
        addDependencyPlugin("javax:javaee-api:.*", "ejb", "jpa");
        addDependencyPlugin("javax:javaee-web-api:.*", "ejb", "jpa");
        addDependencyPlugin("javaee:javaee-api:.*", "ejb");
        addDependencyPlugin("org.hibernate.javax.persistence:hibernate-jpa-2.0-api:.*", "jpa");
        addDependencyPlugin("org.hibernate.javax.persistence:hibernate-jpa-2.1-api:.*", "jpa");
        addDependencyPlugin("org.eclipse.persistence:javax.persistence:.*", "jpa");
    }

    private static void addDependencyPlugin(String dependency, String... plugins) {
        DEPENDENCY_TO_PLUGIN.put(dependency, Arrays.asList(plugins));
    }

    final Map<String, Object> info;
    final GradleBaseProject prj = new GradleBaseProject();
    final Set<String> problems = new LinkedHashSet<>();
    final GradleArtifactStore artifactSore = GradleArtifactStore.getDefault();

    GradleBaseProjectBuilder(Map<String, Object> info) {
        this.info = info;
    }

    void build() {
        processBasicInfo();
        processTasks();
        processDependencies();
        processDependencyPlugins();
    }

    void processBasicInfo() {
        prj.name = (String) info.get("project_name");
        prj.path = (String) info.get("project_path");
        prj.status = (String) info.get("project_status");
        prj.parentName = (String) info.get("project_parent_name");
        prj.displayName = (String) info.get("project_display_name");
        prj.description = (String) info.get("project_description");
        prj.group = (String) info.get("project_group");
        prj.buildDir = (File) info.get("project_buildDir");
        prj.projectDir = (File) info.get("project_projectDir");
        prj.rootDir = (File) info.get("project_rootDir");
        prj.version = (String) info.get("project_version");
        prj.license = (String) info.get("license");

        prj.plugins = new TreeSet<>(createSet((Set<String>) info.get("plugins")));
        prj.subProjects = (Map<String, File>) info.get("project_subProjects");
        prj.includedBuilds = (Map<String, File>) info.get("project_includedBuilds");
        if (info.containsKey("buildClassPath")) {
            prj.buildClassPath = (Set<File>) info.get("buildClassPath");
        }
        if (info.containsKey("nbprops")) {
            Map<String,String> props = new HashMap<>((Map<String,String>) info.get("nbprops"));
            prj.netBeansProperties = Collections.unmodifiableMap(props);
        }
    }

    void processTasks() {
        Set<String[]> tasks = (Set<String[]>) info.get("tasks");
        if (tasks != null) {
            for (String[] arr : tasks) {
                GradleTask task = new GradleTask(arr[0], arr[1], arr[2], arr[3]);
                prj.tasksByName.put(task.getName(), task);
                List<GradleTask> group = prj.tasksByGroup.get(task.getGroup());
                if (group == null) {
                    group = new ArrayList<>();
                    prj.tasksByGroup.put(task.getGroup(), group);
                }
                group.add(task);
            }
        }
    }

    void processDependencies() {

        Set<File> sourceSetOutputs = new HashSet<>();
        Set<String> sourceSetNames = (Set<String>) info.get("sourcesets");
        if (sourceSetNames != null) {
            for (String name : sourceSetNames) {
                Set<File> dirs = (Set<File>) info.get("sourceset_" + name + "_output_classes");
                sourceSetOutputs.addAll(dirs);
                sourceSetOutputs.add((File) info.get("sourceset_" + name + "_output_resources"));
            }
        }
        prj.outputPaths = createSet(sourceSetOutputs);

        Set<String> configurationNames = createSet((Set<String>) info.get("configurations"));
        Map<String, File> prjs = (Map<String, File>) info.get("project_dependencies");
        prjs = prjs != null ? prjs : Collections.<String, File>emptyMap();

        Map<String, Set<File>> arts = (Map<String, Set<File>>) info.get("resolved_jvm_artifacts");
        arts = arts != null ? arts : Collections.<String, Set<File>>emptyMap();
        Map<String, Set<File>> sources = (Map<String, Set<File>>) info.get("resolved_sources_artifacts");
        sources = sources != null ? sources : Collections.<String, Set<File>>emptyMap();
        Map<String, Set<File>> javadocs = (Map<String, Set<File>>) info.get("resolved_javadoc_artifacts");
        javadocs = javadocs != null ? javadocs : Collections.<String, Set<File>>emptyMap();

        Map<String, String> unresolvedProblems = (Map<String, String>) info.get("unresolved_problems");
        unresolvedProblems = unresolvedProblems != null ? unresolvedProblems : Collections.<String, String>emptyMap();
        Map<String, ModuleDependency> components = new HashMap<>();
        for (Map.Entry<String, Set<File>> entry : arts.entrySet()) {
            ModuleDependency dep = new ModuleDependency(entry.getKey(), entry.getValue());

            components.put(entry.getKey(), dep);
            dep.sources = sources.get(entry.getKey());
            dep.javadoc = javadocs.get(entry.getKey());
        }
        Map<String, ProjectDependency> projects = new HashMap<>();
        for (Map.Entry<String, File> entry : prjs.entrySet()) {
            ProjectDependency dep = new ProjectDependency(entry.getKey(), entry.getValue());
            projects.put(entry.getKey(), dep);
        }
        Map<String, UnresolvedDependency> unresolved = new HashMap<>();
        for (Map.Entry<String, String> entry : unresolvedProblems.entrySet()) {
            UnresolvedDependency dep = new UnresolvedDependency(entry.getKey());
            dep.problem = entry.getValue();
            unresolved.put(entry.getKey(), dep);
        }
        if (configurationNames != null) {
            for (String name : configurationNames) {
                GradleConfiguration conf = prj.createConfiguration(name);
                conf.modules = new HashSet<>();
                Boolean nonResolvingConf = (Boolean)info.get("configuration_" + name + "_non_resolving");
                conf.canBeResolved = nonResolvingConf != null ? !nonResolvingConf : true;
                if (conf.isCanBeResolved()) {
                    Set<String> requiredComponents = (Set<String>) info.get("configuration_" + name + "_components");
                    for (String c : requiredComponents) {
                        ModuleDependency dep = components.get(c);
                        if (dep != null) {
                            conf.modules.add(dep);
                        } else {
                            Set<File> binaries = artifactSore.getBinaries(c);
                            if (binaries != null) {
                                dep = new ModuleDependency(c, binaries);
                                components.put(c, dep);
                                conf.modules.add(dep);
                            }
                        }
                    }
                    conf.projects = new HashSet<>();
                    Set<String> requiredProjects = (Set<String>) info.get("configuration_" + name + "_projects");
                    for (String p : requiredProjects) {
                        conf.projects.add(projects.get(p));
                    }
                    conf.unresolved = new HashSet<>();
                    Set<String> unresolvedComp = (Set<String>) info.get("configuration_" + name + "_unresolved");
                    for (String u : unresolvedComp) {
                        conf.unresolved.add(unresolved.get(u));
                    }
                    Set<File> files = (Set<File>) info.get("configuration_" + name + "_files");
                    if (files != null) {
                        files = new HashSet<>(files);
                        files.removeAll(sourceSetOutputs);
                    }
                    conf.files = new FileCollectionDependency(createSet(files));
                }
                Boolean transitive = (Boolean) info.get("configuration_" + name + "_transitive");
                conf.transitive = transitive == null ? true : transitive;

                conf.description = (String) info.get("configuration_" + name + "_description");
            }
            for (String name : configurationNames) {
                GradleConfiguration conf = prj.configurations.get(name);
                Set<String> extendsFrom = (Set<String>) info.get("configuration_" + name + "_extendsFrom");
                Set<GradleConfiguration> parents = new HashSet<>();
                if (extendsFrom != null) {
                    for (String n : extendsFrom) {
                        parents.add(prj.configurations.get(n));
                    }
                }
                conf.extendsFrom = createSet(parents);
            }

        }

        // Create file -> component map
        for (ModuleDependency dep : components.values()) {
            for (File f : dep.getArtifacts()) {
                prj.componentsByFile.put(f, dep);
            }
            for (File f : dep.getSources()) {
                prj.componentsByFile.put(f, dep);
            }
            for (File f : dep.getJavadoc()) {
                prj.componentsByFile.put(f, dep);
            }
        }

        // Add detailed problems on unresolved dependencies
        problems.addAll(unresolvedProblems.values());
    }

    private void processDependencyPlugins() {
        GradleConfiguration compile = prj.configurations.get("compile"); //NOI18N
        if (compile != null) {
            Set<GradleConfiguration> parents = compile.getAllParents();
            for (GradleConfiguration config : parents) {
                for (String dependency : DEPENDENCY_TO_PLUGIN.keySet()) {
                    if (!config.findModules(dependency).isEmpty()) {
                        prj.plugins.addAll(DEPENDENCY_TO_PLUGIN.get(dependency));
                    }
                }
            }
        }
    }


    private <T> Set<T> createSet(Set<T> origin) {
        if (origin == null) {
            return Collections.<T>emptySet();
        }
        switch (origin.size()) {
            case 0:
                return Collections.<T>emptySet();
            case 1:
                return Collections.singleton(origin.iterator().next());
            default: {
                return Collections.unmodifiableSet(origin);
            }
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Set getExtract() {
        return Collections.singleton(prj);
    }

    @Override
    public Set<String> getProblems() {
        return Collections.unmodifiableSet(problems);
    }

    @ServiceProvider(service = ProjectInfoExtractor.class, position = Integer.MIN_VALUE)
    @SuppressWarnings("rawtypes")
    public static final class Extractor implements ProjectInfoExtractor {

        @Override
        public Result extract(Map<String, Object> props, Map<Class, Object> otherInfo) {
            GradleBaseProjectBuilder result = new GradleBaseProjectBuilder(props);
            result.build();
            return result;
        }

        @Override
        public Result fallback(GradleFiles files) {
            return new DefaultResult(GradleBaseProject.getFallback(files));
        }

    }
}
