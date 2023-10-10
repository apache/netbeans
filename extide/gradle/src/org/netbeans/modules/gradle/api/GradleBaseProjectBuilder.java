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
import org.netbeans.modules.gradle.loaders.GradleArtifactStore;
import static org.netbeans.modules.gradle.api.GradleDependency.*;
import org.netbeans.modules.gradle.spi.ProjectInfoExtractor;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.gradle.GradleModuleFileCache21;
import org.netbeans.modules.gradle.spi.GradleSettings;
import org.openide.util.lookup.ServiceProvider;

/**
 * This is the de-serialization support class for GradleBaseProject.
 *
 * @author Laszlo Kishalmi
 */
@SuppressWarnings("unchecked")
class GradleBaseProjectBuilder implements ProjectInfoExtractor.Result {

    static final Map<String, List<String>> DEPENDENCY_TO_PLUGIN = new LinkedHashMap<>();
    static final Logger LOG = Logger.getLogger(GradleBaseProjectBuilder.class.getName());

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
        this.info = new TreeMap<>(info);
    }

    void build() {
        if (LOG.isLoggable(Level.FINE)) {
            for (Map.Entry<String, Object> entry : info.entrySet()) {
                LOG.log(Level.FINE, entry.getKey() + " = " + String.valueOf(entry.getValue()));
            }
        }
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
        Map<String, File> rawSubprojects = (Map<String, File>) info.get("project_subProjects");
        Map<String, File> refinedSubprojects = (Map<String, File>) info.get("project_subProjects");
        for (Map.Entry<String, File> entry : rawSubprojects.entrySet()) {
            refinedSubprojects.put(entry.getKey(), entry.getValue().isAbsolute() ? entry.getValue() : new File(prj.rootDir, entry.getValue().toString()));
        }
        prj.subProjects = Collections.unmodifiableMap(refinedSubprojects);
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
        Map<String, Object> taskInfos = (Map<String, Object>)info.getOrDefault("taskDetails", Collections.emptyMap()); // NOI18N
        for (String tn : prj.getTaskNames()) {
            Map<String, String> tinfo = (Map<String, String>)taskInfos.get(tn);
            if (tinfo != null) {
                prj.taskDependencies.put(tn, Arrays.asList(tinfo.getOrDefault("taskDependencies", "").split(","))); // NOI18N
                Set<String> inherited = new LinkedHashSet<>(Arrays.asList(tinfo.getOrDefault("inherits", "").split(","))); // NOI18N
                prj.taskTypes.put(tn, inherited);
            }
        }
    }

    void processDependencies() {

        File gradleUserHome = (File) info.get("gradle_user_home");
        gradleUserHome = gradleUserHome != null ? gradleUserHome : GradleSettings.getDefault().getGradleUserHome();
        
        Set<File> sourceSetOutputs = new HashSet<>();
        Set<String> sourceSetNames = (Set<String>) info.get("sourcesets");
        if (sourceSetNames != null) {
            for (String name : sourceSetNames) {
                Set<File> dirs = (Set<File>) info.get("sourceset_" + name + "_output_classes");
                sourceSetOutputs.addAll(dirs != null ? dirs : Collections.emptySet());
                sourceSetOutputs.add((File) info.get("sourceset_" + name + "_output_resources"));
            }
        }
        prj.outputPaths = createSet(sourceSetOutputs);

        Set<String> configurationNames = createSet((Set<String>) info.get("configurations"));
        Map<String, File> prjs = (Map<String, File>) info.get("project_dependencies");
        prjs = prjs != null ? prjs : Collections.<String, File>emptyMap();

        Map<String, Set<File>> arts = (Map<String, Set<File>>) info.get("resolved_jvm_artifacts");
        arts = arts != null ? arts : Collections.<String, Set<File>>emptyMap();
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "Resolved JVM artifacts: {0}", arts.toString().replace(",", "\n\t"));
        }
        Map<String, Set<File>> sources = (Map<String, Set<File>>) info.get("resolved_sources_artifacts");
        sources = sources != null ? sources : Collections.<String, Set<File>>emptyMap();
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "Resolved source artifacts: {0}", sources.toString().replace(",", "\n\t"));
        }
        Map<String, Set<File>> javadocs = (Map<String, Set<File>>) info.get("resolved_javadoc_artifacts");
        javadocs = javadocs != null ? javadocs : Collections.<String, Set<File>>emptyMap();
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "Resolved javadoc artifacts: {0}", javadocs.toString().replace(",", "\n\t"));
        }

        Map<String, String> unresolvedProblems = (Map<String, String>) info.get("unresolved_problems");
        unresolvedProblems = unresolvedProblems != null ? unresolvedProblems : Collections.<String, String>emptyMap();
        Map<String, ModuleDependency> components = new HashMap<>();
        
        // supplement project info with cache data as in online mode javadocs and sources are NOT queried. Especially
        // when doing a refresh / download, the project info is just partial, although Gradle has relevant artifacts in
        // its caches.
        GradleArtifactStore store = GradleArtifactStore.getDefault();
        for (Map.Entry<String, Set<File>> entry : arts.entrySet()) {
            String componentId = entry.getKey();
            LOG.log(Level.FINER, "Resolving JVM artifact {0}", componentId);
            // Looking at cache first as we might have the chance to find Sources and Javadocs
            ModuleDependency dep = resolveModuleDependency(gradleUserHome, componentId);
            if (!dep.getArtifacts().equals(entry.getValue())) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Created component {0} from JVM artifacts: {1}", new Object[] { componentId, entry.getValue() });
                }
                dep = new ModuleDependency(componentId, entry.getValue());
            }
            components.put(componentId, dep);
            if (LOG.isLoggable(Level.FINER) && dep.sources != null && !dep.sources.isEmpty()) {
                LOG.log(Level.FINER, "Replacing sources for {0} sources from {1}, used to be {2}", new Object[] {
                    componentId, sources.containsKey(componentId) ? "resolvedSources" : "artifactStore", dep.sources
                });
            }
            if (sources.containsKey(componentId)) {
                dep.sources = sources.get(entry.getKey());
            } else {
                dep.sources = store.getSources(entry.getValue());
            }
            if (LOG.isLoggable(Level.FINER) && dep.javadoc != null && !dep.javadoc.isEmpty()) {
                LOG.log(Level.FINER, "Replacing javadocs for {0} from {1}, used to be {2}", new Object[] {
                    componentId, javadocs.containsKey(componentId) ? "resolvedJavadocs" : "artifactStore", dep.javadoc
                });
            }
            if (javadocs.containsKey(componentId)) {
                dep.javadoc = javadocs.get(entry.getKey());
            } else {
                dep.javadoc = store.getJavadocs(entry.getValue());                
            }
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Done component: {0} -> {1}", new Object[] { componentId, dep });
            }
        }
        Map<String, ProjectDependency> projects = new HashMap<>();
        for (Map.Entry<String, File> entry : prjs.entrySet()) {
            ProjectDependency dep = new ProjectDependency(entry.getKey(), entry.getValue());
            projects.put(entry.getKey(), dep);
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Added project dependency: {0} -> {1} ", new Object[] { entry.getKey(), dep });
            }
        }
        Map<String, UnresolvedDependency> unresolved = new HashMap<>();
        for (Map.Entry<String, String> entry : unresolvedProblems.entrySet()) {
            UnresolvedDependency dep = new UnresolvedDependency(entry.getKey());
            dep.problem = entry.getValue();
            unresolved.put(entry.getKey(), dep);
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Adding UNRESOLVED dependency: {0} -> {1}", new Object[] { entry.getKey(), dep });
            }
        }
        
        GradleDependency.ProjectDependency rootDep = new GradleDependency.ProjectDependency(prj.getPath(), prj.getProjectDir());
        prj.projectDependencyNode = rootDep;
        
        if (configurationNames != null) {
            for (String name : configurationNames) {
                GradleConfiguration conf = prj.createConfiguration(name);
                conf.modules = new HashSet<>();
                Boolean nonResolvingConf = (Boolean)info.get("configuration_" + name + "_non_resolving");
                conf.canBeResolved = nonResolvingConf != null ? !nonResolvingConf : true;
                Set<String> requiredComponents = (Set<String>) info.get("configuration_" + name + "_components");
                if (requiredComponents != null) {
                    for (String c : requiredComponents) {
                        ModuleDependency dep = components.get(c);
                        if (dep != null) {
                            LOG.log(Level.FINER, "Configuration {0}, known component {1}", new Object[] { conf.getName(), dep });
                            conf.modules.add(dep);
                        } else {
                            dep = resolveModuleDependency(gradleUserHome, c);
                            LOG.log(Level.FINER, "Configuration {0}, resolved to {1}", new Object[] { conf.getName(), dep });
                            if (dep != null) {
                                components.put(c, dep);
                                conf.modules.add(dep);
                            } else {
                               // NETBEANS-5161: This could happen on composite projects
                               // TODO: Implement composite project module dependency
                            }
                        }
                    }
                }
                conf.projects = new HashSet<>();
                Set<String> requiredProjects = (Set<String>) info.get("configuration_" + name + "_projects");
                if (requiredProjects != null) {
                    for (String p : requiredProjects) {
                        conf.projects.add(projects.get(p));
                    }
                }
                Map<String, UnresolvedDependency> unresolved2 = new HashMap<>(unresolved);
                conf.unresolved = new HashSet<>();
                Set<String> unresolvedComp = (Set<String>) info.get("configuration_" + name + "_unresolved");
                if (unresolvedComp != null) {
                    for (String u : unresolvedComp) {
                        UnresolvedDependency dep = unresolved2.get(u);
                        if (dep == null) {
                            dep = new UnresolvedDependency(u);
                            unresolved2.put(u, dep);
                        }
                        conf.unresolved.add(dep);
                    }
                }
                Set<File> files = (Set<File>) info.get("configuration_" + name + "_files");
                if (files != null) {
                    files = new HashSet<>(files);
                    files.removeAll(sourceSetOutputs);
                }
                conf.files = new FileCollectionDependency(createSet(files));
                Boolean transitive = (Boolean) info.get("configuration_" + name + "_transitive");
                conf.transitive = transitive == null ? true : transitive;

                Boolean canBeConsumed = (Boolean) info.get("configuration_" + name + "_canBeConsumed");
                conf.canBeConsumed = canBeConsumed == null ? false : canBeConsumed;

                conf.attributes = (Map<String, String>) info.get("configuration_" + name + "_attributes");

                conf.description = (String) info.get("configuration_" + name + "_description");
                
                Map<String, Collection<String>> directDependencies = (Map<String, Collection<String>>)info.get("configuration_" + name + "_dependencies");
                Set<String> childSpecs = (Set<String>)info.get("configuration_" + name + "_directChildren");
                if (childSpecs == null) {
                    childSpecs = Collections.emptySet();
                }
                if (directDependencies == null) {
                    directDependencies = Collections.emptyMap();
                }
                Map<GradleDependency, Collection<GradleDependency>> deps = new HashMap<>();
                Set<GradleDependency> children = new LinkedHashSet<>();

                for (Map.Entry<String, Collection<String>> it : directDependencies.entrySet()) {
                    String parentId = it.getKey();

                    GradleDependency parentD;
                    boolean special = false;
                    if (parentId.equals("")) {
                        parentD = GradleConfiguration.SELF_DEPENDENCY;
                        special = true;
                    } else if (parentId.startsWith(DEPENDENCY_PROJECT_PREFIX)) {
                        int sep1 = parentId.indexOf(':', DEPENDENCY_PROJECT_PREFIX.length());
                        parentD = projects.get(parentId.substring(DEPENDENCY_PROJECT_PREFIX.length(), sep1));
                        special = true;
                    } else {
                        parentD = components.get(parentId);
                        if (parentD == null) {
                            parentD = unresolved.get(parentId);
                        }
                    }
                    if (parentD == null) {
                        continue;
                    }
                    
                    if (childSpecs.remove(parentId)) {
                        children.add(parentD);
                    } else if (!special) {
                        // special case - version may not be specified, but is implied somehow.
                        try {
                            String[] gav = GradleModuleFileCache21.gavSplit(parentId);
                            String versionLess = gav[0] + ':' + gav[1] + ':';
                            if (childSpecs.remove(versionLess)) {
                                children.add(parentD);
                            }
                        } catch (IllegalArgumentException ex) {
                            LOG.log(Level.FINE, "Unknown dependency GAV: parentId");
                        }
                    }
                    
                    for (String cid : it.getValue()) {
                        GradleDependency childD;
                        if (cid.startsWith(DEPENDENCY_PROJECT_PREFIX)) {
                            int sep1 = cid.indexOf(':', DEPENDENCY_PROJECT_PREFIX.length());
                            childD = projects.get(cid.substring(DEPENDENCY_PROJECT_PREFIX.length(), sep1));
                        } else {
                            childD = components.get(cid);
                            if (childD == null) {
                                childD = unresolved.get(cid);
                            }
                        }
                        if (childD == null) {
                            continue;
                        }
                        deps.computeIfAbsent(parentD, x -> new ArrayList<>()).
                            add(childD);
                    }
                }
                for (String s : childSpecs) {
                    GradleDependency ud = unresolved2.get(s);
                    if (ud != null) {
                        children.add(ud);
                    }
                }
                conf.directChildren = children;
                conf.dependencyMap = deps;
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
        
        prj.projectIds = (Map<String, String>)info.getOrDefault("project_ids", Collections.emptyMap());
        
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
    
    private static final String DEPENDENCY_PROJECT_PREFIX = "*project;";

    private ModuleDependency resolveModuleDependency(File gradleUserHome, String c) {
        GradleModuleFileCache21 moduleCache = GradleModuleFileCache21.getGradleFileCache(gradleUserHome.toPath());
        try {
            GradleModuleFileCache21.CachedArtifactVersion artVersion = moduleCache.resolveModule(c);
            Set<File> binaries = artifactSore.getBinaries(c);
            if (((binaries == null) || binaries.isEmpty()) && (artVersion.getBinary() != null)) {
                LOG.log(Level.FINER, "Resolving component {0} from module21: {0}", artVersion.getBinary());
                binaries = Collections.singleton(artVersion.getBinary().getPath().toFile());
            } else {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Resolving component {0} from artifactstore: {0}", new Object[] { c, binaries });
                }
            }
            ModuleDependency ret = new ModuleDependency(c, binaries);
            if (artVersion.getSources() != null) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Resolving sources {0} from module21: {0}", new Object[] { c, artVersion.getSources().getPath() });
                }
                ret.sources = Collections.singleton(artVersion.getSources().getPath().toFile());
            }
            if (artVersion.getJavaDoc() != null) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Resolving javadoc {0} from module21: {0}", new Object[] { c, artVersion.getJavaDoc().getPath() });
                }
                ret.javadoc = Collections.singleton(artVersion.getJavaDoc().getPath().toFile());
            }
            return ret;
        } catch (IllegalArgumentException iae) {
            // NETBEANS-5161: This could happen on composite projects
            return null;
        }
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
