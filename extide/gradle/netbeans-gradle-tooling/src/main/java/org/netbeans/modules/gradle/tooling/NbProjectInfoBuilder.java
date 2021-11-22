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

package org.netbeans.modules.gradle.tooling;

import groovy.lang.MissingPropertyException;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.FileCollectionDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.ResolveException;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentSelector;
import org.gradle.api.artifacts.result.ArtifactResolutionResult;
import org.gradle.api.artifacts.result.ArtifactResult;
import org.gradle.api.artifacts.result.ComponentArtifactsResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.artifacts.result.UnresolvedDependencyResult;
import org.gradle.api.distribution.DistributionContainer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.initialization.IncludedBuild;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.testing.Test;
import org.gradle.jvm.JvmLibrary;
import org.gradle.language.base.artifact.SourcesArtifact;
import org.gradle.language.java.artifact.JavadocArtifact;
import org.gradle.util.VersionNumber;
import org.netbeans.modules.gradle.api.NbProjectInfo;

/**
 *
 * @author Laszlo Kishalmi
 */
class NbProjectInfoBuilder {
    private static final String NB_PREFIX = "netbeans.";
    private static final Set<String> CONFIG_EXCLUDES = new HashSet<>(asList( new String[]{
        "archives",
        "checkstyle",
        "classycle",
        "codenarc",
        "findbugs",
        "findbugsPlugins",
        "jacocoAgent",
        "jacocoAnt",
        "jdepend",
        "pmd",
    }));

    private static final Set<String> RECOGNISED_PLUGINS = new HashSet<>(asList(new String[]{
        "antlr",
        "application",
        "base",
        "checkstyle",
        "com.android.application",
        "com.android.library",
        "com.github.lkishalmi.gatling",
        "distribution",
        "ear",
        "findbugs",
        "groovy",
        "groovy-base",
        "io.micronaut.application",
        "ivy-publish",
        "jacoco",
        "java",
        "java-base",
        "java-library-distribution",
        "java-platform",
        "maven",
        "maven-publish",
        "org.jetbrains.kotlin.js",
        "org.jetbrains.kotlin.jvm",
        "org.jetbrains.kotlin.android",
        "org.springframework.boot",
        "osgi",
        "play",
        "pmd",
        "scala",
        "scala-base",
        "war"
    }));

    final Project project;
    final VersionNumber gradleVersion;

    NbProjectInfoBuilder(Project project) {
        this.project = project;
        this.gradleVersion = VersionNumber.parse(project.getGradle().getGradleVersion());
    }

    public NbProjectInfo buildAll() {
        NbProjectInfoModel model = new NbProjectInfoModel();
        detectProjectMetadata(model);
        detectProps(model);
        detectLicense(model);
        detectPlugins(model);
        detectSources(model);
        detectTests(model);
        detectDependencies(model);
        detectArtifacts(model);
        detectDistributions(model);
        return model;
    }

    @SuppressWarnings("null")
    private void detectDistributions(NbProjectInfoModel model) {
        if (project.getPlugins().hasPlugin("distribution")) {
            DistributionContainer distributions = project.getExtensions().findByType(DistributionContainer.class);
            model.getInfo().put("distributions", storeSet(distributions.getNames()));
        }
    }

    @SuppressWarnings("null")
    private void detectLicense(NbProjectInfoModel model) {
        String license = project.hasProperty("netbeans.license") ? project.property("netbeans.license").toString() : null;
        if (license == null) {
            license = project.hasProperty("license") ? project.property("license").toString() : null;
        }
        model.getInfo().put("license", license);
    }

    @SuppressWarnings("null")
    private void detectProjectMetadata(NbProjectInfoModel model) {
        long time = System.currentTimeMillis();
        model.getInfo().put("project_name", project.getName());
        model.getInfo().put("project_path", project.getPath());
        model.getInfo().put("project_status", project.getStatus());
        if (project.getParent() != null) {
            model.getInfo().put("project_parent_name", project.getParent().getName());
        }
        model.getInfo().put("project_description", project.getDescription());
        model.getInfo().put("project_group", project.getGroup().toString());
        model.getInfo().put("project_version", project.getVersion().toString());
        model.getInfo().put("project_buildDir", project.getBuildDir());
        model.getInfo().put("project_projectDir", project.getProjectDir());
        model.getInfo().put("project_rootDir", project.getRootDir());
        model.getInfo().put("gradle_user_home", project.getGradle().getGradleHomeDir());

        Set<Configuration> visibleConfigurations = configurationsToSave();
        model.getInfo().put("configurations", visibleConfigurations.stream().map(conf->conf.getName()).collect(Collectors.toCollection(HashSet::new )));

        Map<String, File> sp = new HashMap<>();
        for(Project p: project.getSubprojects()) {
            sp.put(p.getPath(), p.getProjectDir());
        }
        model.getInfo().put("project_subProjects", sp);

        Map<String, File> ib = new HashMap<>();
        System.out.printf("Gradle Version: %s%n", gradleVersion);
        sinceGradle("3.1", () -> {
            for(IncludedBuild p: project.getGradle().getIncludedBuilds()) {
                System.out.printf("Include Build: %s%n", p.getName());
                ib.put(p.getName(), p.getProjectDir());
            }
        });
        model.getInfo().put("project_includedBuilds", ib);

        sinceGradle("3.3", () -> {
            model.getInfo().put("project_display_name", project.getDisplayName());
        });

        try {
            model.getInfo().put("buildClassPath", storeSet(project.getBuildscript().getConfigurations().getByName("classpath").getFiles()));
        } catch (RuntimeException e) {
            model.noteProblem(e);
        }
        Set<String[]> tasks = new HashSet<>();
        for (org.gradle.api.Task t : project.getTasks()) {
            String[] arr = new String[]{t.getPath(), t.getGroup(), t.getName(), t.getDescription()};
            tasks.add(arr);
        }
        model.getInfo().put("tasks", tasks);
        model.registerPerf("meta", System.currentTimeMillis() - time);
    }

    private void detectPlugins(NbProjectInfoModel model) {
        long time = System.currentTimeMillis();
        Set<String> plugins = new HashSet<>();
        for (String plugin : RECOGNISED_PLUGINS) {
            if (project.getPlugins().hasPlugin(plugin)) {
                plugins.add(plugin);
            }
        }
        model.getInfo().put("plugins", plugins);
        model.registerPerf("plugins", System.currentTimeMillis() - time);
    }

    private void detectTests(NbProjectInfoModel model) {
        Set<File> testClassesRoots = new HashSet<>();
        sinceGradle("4.0", () -> {
            project.getTasks().withType(Test.class).stream().forEach(task -> {
                task.getTestClassesDirs().forEach(dir -> testClassesRoots.add(dir));
            });
        });
        beforeGradle("4.0", () -> {
            project.getTasks().withType(Test.class).stream().forEach(task -> {
                testClassesRoots.add((File) getProperty(task, "testClassesDir"));
            });
        });
        model.getInfo().put("test_classes_dirs", testClassesRoots);

        if (project.getPlugins().hasPlugin("jacoco")) {
            Set<File> coverageData = new HashSet<>();
            project.getTasks().withType(Test.class).stream().forEach(task -> {
                coverageData.add((File) getProperty(task, "jacoco", "destinationFile"));
            });
            model.getInfo().put("jacoco_coverage_files", coverageData);
        }
    }

    private void detectProps(NbProjectInfoModel model) {
        Map<String, String> nbprops = new HashMap<>();
        project.getProperties()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith(NB_PREFIX))
                .forEach(e -> nbprops.put(e.getKey().substring(NB_PREFIX.length()), String.valueOf(e.getValue())));
        model.getInfo().put("nbprops", nbprops);
    }

    private void detectSources(NbProjectInfoModel model) {
        long time = System.currentTimeMillis();

        boolean hasJava = project.getPlugins().hasPlugin("java-base");
        boolean hasGroovy = project.getPlugins().hasPlugin("groovy-base");
        boolean hasScala = project.getPlugins().hasPlugin("scala-base");
        boolean hasKotlin = project.getPlugins().hasPlugin("org.jetbrains.kotlin.android") ||
                            project.getPlugins().hasPlugin("org.jetbrains.kotlin.js") ||
                            project.getPlugins().hasPlugin("org.jetbrains.kotlin.jvm");

        if (hasJava) {
            SourceSetContainer sourceSets = (SourceSetContainer) getProperty(project, "sourceSets");
            if (sourceSets != null) {
                model.getInfo().put("sourcesets", storeSet(sourceSets.getNames()));
                for(SourceSet sourceSet: sourceSets) {
                    String propBase = "sourceset_" + sourceSet.getName() + "_";
                    for(String lang: new String[] {"JAVA", "GROOVY", "SCALA", "KOTLIN"}) {
                        Task compileTask = project.getTasks().findByName(sourceSet.getCompileTaskName(lang.toLowerCase()));
                        if (compileTask != null) {
                            model.getInfo().put(
                                    propBase + lang + "_source_compatibility",
                                    compileTask.property("sourceCompatibility"));
                            model.getInfo().put(
                                    propBase + lang + "_target_compatibility",
                                    compileTask.property("targetCompatibility"));

                            List<String> compilerArgs;

                            try {
                                compilerArgs = (List<String>) getProperty(compileTask, "options", "allCompilerArgs");
                            } catch (Throwable ex) {
                                try {
                                    compilerArgs = (List<String>) getProperty(compileTask, "options", "compilerArgs");
                                } catch (Throwable ex2) {
                                    compilerArgs = (List<String>) getProperty(compileTask, "kotlinOptions", "getFreeCompilerArgs");
                                }
                            }
                            model.getInfo().put(propBase + lang + "_compiler_args", new ArrayList<>(compilerArgs));
                        }
                    }
   
                    model.getInfo().put(propBase + "JAVA", storeSet(getProperty(sourceSet, "java", "srcDirs")));
                    model.getInfo().put(propBase + "RESOURCES", storeSet(sourceSet.getResources().getSrcDirs()));
                    if(hasGroovy) {
                        model.getInfo().put(propBase + "GROOVY", storeSet(getProperty(sourceSet, "groovy", "srcDirs")));
                    }
                    if (hasScala) {
                        model.getInfo().put(propBase + "SCALA", storeSet(getProperty(sourceSet, "scala", "srcDirs")));
                    }
                    if (hasKotlin) {
                        model.getInfo().put(propBase + "KOTLIN", storeSet(getProperty(getProperty(sourceSet, "kotlin"), "srcDirs")));
                    }
                    sinceGradle("4.0", () -> {
                        Set<File> dirs = new LinkedHashSet<>();
                        // classesDirs is just an iterable
                        for (File dir: (ConfigurableFileCollection) getProperty(sourceSet, "output", "classesDirs")) {
                            dirs.add(dir);
                        }
                        model.getInfo().put(propBase + "output_classes", dirs);
                    });
                    beforeGradle("4.0", () -> {
                        model.getInfo().put(propBase + "output_classes", Collections.singleton(getProperty(sourceSet, "output", "classesDir")));
                    });
                    model.getInfo().put(propBase + "output_resources", sourceSet.getOutput().getResourcesDir());
                    sinceGradle("5.2", () -> {
                        model.getInfo().put(propBase + "GENERATED", storeSet(getProperty(sourceSet, "output", "generatedSourcesDirs", "files")));
                    });
                    try {
                        model.getInfo().put(propBase + "classpath_compile", storeSet(sourceSet.getCompileClasspath().getFiles()));
                        model.getInfo().put(propBase + "classpath_runtime", storeSet(sourceSet.getRuntimeClasspath().getFiles()));
                    } catch(Exception e) {
                        model.noteProblem(e);
                    }
                    sinceGradle("4.6", () -> {
                        try {
                            model.getInfo().put(propBase + "classpath_annotation", storeSet(getProperty(sourceSet, "annotationProcessorPath", "files")));
                        } catch(Exception e) {
                            model.noteProblem(e);
                        }
                        model.getInfo().put(propBase + "configuration_annotation", getProperty(sourceSet, "annotationProcessorConfigurationName"));
                    });
                    beforeGradle("5.0", () -> {
                        if (model.getInfo().get(propBase + "classpath_annotation") == null || ((Collection<?>) model.getInfo().get(propBase + "classpath_annotation")).isEmpty()) {
                            model.getInfo().put(propBase + "classpath_annotation", storeSet(getProperty(sourceSet, "compileClasspath", "files")));
                        }
                    });
                    beforeGradle("7.0", () -> {
                        model.getInfo().put(propBase + "configuration_compile", getProperty(sourceSet, "compileClasspathConfigurationName"));
                        model.getInfo().put(propBase + "configuration_runtime", getProperty(sourceSet, "runtimeClasspathConfigurationName"));
                    });
                }
            } else {
                model.getInfo().put("sourcesets", Collections.emptySet());
                model.noteProblem("No sourceSets found on this project. This project mightbe a Model/Rule based one which is not supported at the moment.");
            }
        }
        model.registerPerf("plugins", System.currentTimeMillis() - time);
    }

    private void detectArtifacts(NbProjectInfoModel model) {
        long time = System.currentTimeMillis();
        if (project.getPlugins().hasPlugin("java")) {
            model.getInfo().put("main_jar", getProperty(project, "jar", "archivePath"));
        }
        if (project.getPlugins().hasPlugin("war")) {
            model.getInfo().put("main_war", getProperty(project, "war", "archivePath"));
            model.getInfo().put("webapp_dir", getProperty(project, "webAppDir"));
            model.getInfo().put("webxml", getProperty(project, "war", "webXml"));
            try {
                model.getInfo().put("exploded_war_dir", getProperty(project, "explodedWar", "destinationDir"));
            } catch(Exception e) {
                model.noteProblem(e);
            }
            try {
                model.getInfo().put("web_classpath", getProperty(project, "war", "classpath", "files"));
            } catch(Exception e) {
                model.noteProblem(e);
            }
        }
        Map<String, Object> archives = new HashMap<>();
        project.getTasks().withType(Jar.class).forEach(jar -> {
            archives.put(jar.getClassifier(), jar.getArchivePath());
        });
        model.getInfo().put("archives", archives);
        model.registerPerf("artifacts", System.currentTimeMillis() - time);
    }

    private static boolean resolvable(Configuration conf) {
        try{
            return (boolean) getProperty(conf, "canBeResolved");
        } catch (MissingPropertyException ex){
            return true;
        }
    }

    private void detectDependencies(NbProjectInfoModel model) {
        long time = System.currentTimeMillis();
        Set<ComponentIdentifier> ids = new HashSet();
        Map<String, File> projects = new HashMap();
        Map<String, String> unresolvedProblems = new HashMap();
        Map<String, Set<File>> resolvedJvmArtifacts = new HashMap();
        Set<Configuration> visibleConfigurations = configurationsToSave();
        visibleConfigurations.forEach(it -> {
            String propBase = "configuration_" + it.getName() + "_";
            model.getInfo().put(propBase + "non_resolving", !resolvable(it));
            model.getInfo().put(propBase + "transitive",  it.isTransitive());
            model.getInfo().put(propBase + "extendsFrom",  it.getExtendsFrom().stream().map(c -> c.getName()).collect(Collectors.toCollection(HashSet::new)));
            model.getInfo().put(propBase + "description",  it.getDescription());
        });
        //visibleConfigurations = visibleConfigurations.findAll() { resolvable(it) }
        visibleConfigurations.forEach(it -> {
            Set<String> componentIds = new HashSet<>();
            Set<String> unresolvedIds = new HashSet<>();
            Set<String> projectNames = new HashSet<>();
            long time_inspect_conf = System.currentTimeMillis();

            it.getDependencies().withType(ModuleDependency.class).forEach(it2 -> {
                String group = it2.getGroup() != null ? it2.getGroup() : "";
                String name = it2.getName();
                String version = it2.getVersion() != null ? it2.getVersion() : "";
                String id = group + ":" + name + ":" + version;
                componentIds.add(id);
            });

            if (resolvable(it)) {
                try {
                    it.getIncoming().getResolutionResult().getAllDependencies().forEach( it2 -> {
                        if (it2 instanceof ResolvedDependencyResult) {
                            ResolvedDependencyResult rdr = (ResolvedDependencyResult) it2;
                            if (rdr.getRequested() instanceof ModuleComponentSelector) {
                                ids.add(rdr.getSelected().getId());
                                componentIds.add(rdr.getSelected().getId().toString());
                            }
                        }
                        if (it2 instanceof UnresolvedDependencyResult) {
                            UnresolvedDependencyResult udr = (UnresolvedDependencyResult) it2;
                            String id = udr.getRequested().getDisplayName();
                            if(componentIds.contains(id)) {
                                unresolvedIds.add(id);
                            }
                            if(! project.getPlugins().hasPlugin("java-platform")) {
                                unresolvedProblems.put(id, ((UnresolvedDependencyResult) it2).getFailure().getMessage());
                            }
                            unresolvedProblems.put(id, udr.getFailure().getMessage());
                        }
                    });
                } catch (ResolveException ex) {
                    model.noteProblem(ex);
                }
            } else {
                unresolvedIds.addAll(componentIds);
                componentIds.clear();
            }
            String depBase = "dependency_inspect_" + it.getName();
            String depPrefix = depBase + "_";
            String configPrefix = "configuration_" + it.getName() + "_";
            long time_project_deps = System.currentTimeMillis();
            model.registerPerf(depPrefix + "module", time_project_deps - time_inspect_conf);
            it.getDependencies().withType(ProjectDependency.class).forEach(it2 -> {
                Project prj = it2.getDependencyProject();
                projects.put(prj.getPath(), prj.getProjectDir());
                projectNames.add(prj.getPath());
            });
            long time_file_deps = System.currentTimeMillis();
            model.registerPerf(depPrefix + "project", time_file_deps - time_project_deps);
            Set<File> fileDeps = new HashSet<>();
            it.getDependencies().withType(FileCollectionDependency.class).forEach(it2 -> {
                fileDeps.addAll(it2.resolve());
            });
            long time_collect = System.currentTimeMillis();
            model.registerPerf(depPrefix + "file", time_collect - time_file_deps);

            if (resolvable(it)) {
                try {
                    it.getResolvedConfiguration()
                            .getLenientConfiguration()
                            .getFirstLevelModuleDependencies(Specs.SATISFIES_ALL)
                            .forEach(rd -> collectArtifacts(rd, resolvedJvmArtifacts));
                } catch (NullPointerException ex) {
                    //This can happen if the configuration resolution had issues
                }
            }
            long time_report = System.currentTimeMillis();
            model.registerPerf(depPrefix + "collect", time_report - time_collect);

            model.getInfo().put(configPrefix + "components", componentIds);
            model.getInfo().put(configPrefix + "projects", projectNames);
            model.getInfo().put(configPrefix + "files", fileDeps);
            model.getInfo().put(configPrefix + "unresolved", unresolvedIds);
            model.registerPerf(depPrefix + "file", System.currentTimeMillis() - time_report);
            model.registerPerf(depBase, System.currentTimeMillis() - time_inspect_conf);
        });

        long time_exclude = System.currentTimeMillis();
        visibleConfigurations.stream().forEach(it -> {
            String propBase = "configuration_" + it.getName() + "_";
            Set exclude = new HashSet();
            collectModuleDependencies(model, it.getName(), false, exclude);
            ((Set<String>) model.getInfo().get(propBase + "components")).removeAll(exclude);
            ((Set<String>) model.getInfo().get(propBase + "unresolved")).removeAll(exclude);
            ((Set<String>) model.getInfo().get(propBase + "files")).removeAll(exclude);
        });
        model.registerPerf("excludes", System.currentTimeMillis() - time_exclude);

        model.registerPerf("offline", project.getGradle().getStartParameter().isOffline());

        Map<String, Set<File>> resolvedSourcesArtifacts = new HashMap<>();
        Map<String, Set<File>> resolvedJavadocArtifacts = new HashMap<>();
        if (project.getGradle().getStartParameter().isOffline() || project.hasProperty("downloadSources") || project.hasProperty("downloadJavadoc")) {
            long filter_time = System.currentTimeMillis();
            Set<ComponentIdentifier> filteredIds = ids;
            List artifactTypes = project.getGradle().getStartParameter().isOffline() ? new ArrayList<>(asList(SourcesArtifact.class, JavadocArtifact.class)) : new ArrayList<>();
            if (project.hasProperty("downloadSources")) {
                String filter = (String) getProperty(project, "downloadSources");
                if (!"ALL".equals(filter)) {
                    filteredIds = ids.stream()
                            .filter(id -> id.toString().equals(filter))
                            .collect(Collectors.toSet());
                    model.setMiscOnly(true);
                }
                artifactTypes.add(SourcesArtifact.class);
            }
            if (project.hasProperty("downloadJavadoc")) {
                String filter = (String) getProperty(project, "downloadJavadoc");
                if (!"ALL".equals(filter)) {
                    filteredIds = ids.stream()
                            .filter(id -> id.toString().equals(filter))
                            .collect(Collectors.toSet());
                    model.setMiscOnly(true);
                }
                artifactTypes.add(JavadocArtifact.class);
            }
            long query_time = System.currentTimeMillis();
            model.registerPerf("dependencies_filter", query_time - filter_time);
            ArtifactResolutionResult result = project.getDependencies().createArtifactResolutionQuery()
                    .forComponents(filteredIds)
                    .withArtifacts(JvmLibrary.class, artifactTypes)
                    .execute();
            long collect_time = System.currentTimeMillis();
            model.registerPerf("dependencies_query", collect_time - query_time);

            for (ComponentArtifactsResult component: result.getResolvedComponents()) {
                Set<ArtifactResult> sources = component.getArtifacts(SourcesArtifact.class);
                if (!sources.isEmpty()) {
                    resolvedSourcesArtifacts.put(component.getId().toString(), collectResolvedArtifacts(sources));
                }
                Set<ArtifactResult> javadocs = component.getArtifacts(JavadocArtifact.class);
                if (!javadocs.isEmpty()) {
                    resolvedJavadocArtifacts.put(component.getId().toString(), collectResolvedArtifacts(javadocs));
                }
            }

            model.registerPerf("dependencies_collect", System.currentTimeMillis() - collect_time);
        }

        model.getExt().put("resolved_jvm_artifacts", resolvedJvmArtifacts);
        model.getExt().put("resolved_sources_artifacts", resolvedSourcesArtifacts);
        model.getExt().put("resolved_javadoc_artifacts", resolvedJavadocArtifacts);
        model.getInfo().put("project_dependencies", projects);
        model.getInfo().put("unresolved_problems", unresolvedProblems);
        model.registerPerf("dependencies", System.currentTimeMillis() - time);
    }

    private static Set<File> collectResolvedArtifacts(Set<ArtifactResult> res) {
        return res
                .stream()
                .filter(it -> it instanceof ResolvedArtifactResult)
                .map(rar -> ((ResolvedArtifactResult) rar).getFile())
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static void collectArtifacts(ResolvedDependency dep, final Map<String, Set<File>> resolvedArtifacts) {
        String key = dep.getModuleGroup() + ":" +dep.getModuleName() + ":" + dep.getModuleVersion();
        if (!resolvedArtifacts.containsKey(key)) {
            resolvedArtifacts.put(key, dep.getModuleArtifacts().stream().map(r -> r.getFile()).collect(Collectors.toCollection(HashSet::new)));
            dep.getChildren().forEach(rd -> collectArtifacts(rd, resolvedArtifacts));
        }
    }

    private void collectModuleDependencies(final NbProjectInfoModel model, String configurationName, boolean includeRoot, final Set deps) {
        String propBase = "configuration_" + configurationName + "_";
        if (includeRoot) {
            deps.addAll((Collection<?>) model.getInfo().get(propBase + "components"));
            deps.addAll((Collection<?>) model.getInfo().get(propBase + "files"));
            if (!model.getInfo().containsKey(propBase + "non_resolving")) {
                deps.addAll((Collection<?>) model.getInfo().get(propBase + "unresolved"));
            }
        }
        ((Collection<String>) model.getInfo().get(propBase + "extendsFrom")).forEach(it -> {
            collectModuleDependencies(model, it, true, deps);
        });
    }

    private static <T extends Serializable> Set storeSet(Object o) {
        if (o == null) {
            return null;
        }
        if(! ( o instanceof Collection)) {
            throw new IllegalStateException("storeSet can only be used with Collections, but was: " + o.getClass().getName());
        }
        Collection c = (Collection) o;
        switch (c.size()) {
            case 0:
                return Collections.emptySet();
            case 1:
                return Collections.singleton(c.iterator().next());
            default:
                return new LinkedHashSet(c);
        }
    }

    private Set<Configuration> configurationsToSave() {
        return project
                .getConfigurations()
                .matching(c -> !CONFIG_EXCLUDES.contains(c.getName()))
                .stream()
                .flatMap(c -> c.getHierarchy().stream())
                .collect(Collectors.toSet());
    }

    private void sinceGradle(String version, Runnable r) {
        if (gradleVersion.compareTo(VersionNumber.parse(version)) >= 0) {
            r.run();
        }
    }

    private void beforeGradle(String version, Runnable r) {
        if (gradleVersion.compareTo(VersionNumber.parse(version)) < 0) {
            r.run();
        }
    }

    private static Object getProperty(Object obj, String... propPath) {
        Object currentObject = obj;
        for(String prop: propPath) {
            currentObject = InvokerHelper.getPropertySafe(currentObject, prop);
        }
        return currentObject;
    }
}

