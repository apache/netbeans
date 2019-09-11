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

package org.netbeans.modules.gradle.tooling

import org.netbeans.modules.gradle.api.NbProjectInfo
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.SourceSet
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.artifacts.result.UnresolvedDependencyResult
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.ResolveException
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.component.ProjectComponentSelector
import org.gradle.api.artifacts.result.ArtifactResult
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.specs.Specs
import org.gradle.jvm.JvmLibrary
import org.gradle.api.component.Artifact
import org.gradle.api.initialization.IncludedBuild
import org.gradle.language.base.artifact.SourcesArtifact
import org.gradle.language.java.artifact.JavadocArtifact
import org.gradle.tooling.model.Task
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.bundling.Jar
import org.gradle.util.VersionNumber

/**
 *
 * @author Laszlo Kishalmi
 */
class NbProjectInfoBuilder {
    def NB_PREFIX = 'netbeans.'
    def CONFIG_EXCLUDES = ['archives', 'checkstyle', 'pmd', 'jacocoAgent', \
        'jacocoAnt', 'findbugs', 'findbugsPlugins', 'jdepend', 'codenarc', \
        'classycle']

    final Project project;
    final VersionNumber gradleVersion;

    NbProjectInfoBuilder(Project project) {
        this.project = project;
        this.gradleVersion = VersionNumber.parse(project.gradle.gradleVersion)
    }

    public NbProjectInfo buildAll() {
        NbProjectInfoModel model = new NbProjectInfoModel()
        model.ext.perf = new LinkedHashMap()
        detectProjectMetadata(model)
        detectProps(model)
        detectLicense(model)
        detectPlugins(model)
        detectSources(model)
        detectTests(model)
        detectDependencies(model)
        detectArtifacts(model)
        return model
    }

    private void detectLicense(NbProjectInfoModel model) {
        def license = project.hasProperty('netbeans.license') ? project.property('netbeans.license').toString() : null;
        if (license == null) {
            license = project.hasProperty('license') ? project.property('license').toString() : null;
        }
        model.info.license = license
    }

    private void detectProjectMetadata(NbProjectInfoModel model) {
        long time = System.currentTimeMillis()
        model.info.project_name = project.name;
        model.info.project_path = project.path;
        model.info.project_status = project.status;
        if (project.parent != null) {
            model.info.project_parent_name = project.parent.name;
        }
        model.info.project_description = project.description;
        model.info.project_group = project.group.toString();
        model.info.project_version = project.version.toString();
        model.info.project_buildDir = project.buildDir;
        model.info.project_projectDir = project.projectDir;
        model.info.project_rootDir = project.rootDir;

        def visibleConfigurations = configurationsToSave()
        model.info.configurations = storeSet(visibleConfigurations.collect() {conf -> conf.name})

        Map<String, File> sp = new HashMap<>();
        for(Project p: project.subprojects) {
            sp.put(p.path, p.projectDir);
        }
        model.info.project_subProjects = sp;

        Map<String, File> ib = new HashMap<>();
        println "Gradle Version: $gradleVersion"
        if (gradleVersion.compareTo(VersionNumber.parse('3.1')) >= 0) {
            for(IncludedBuild p: project.gradle.includedBuilds) {
                println "Include Build: ${p.name}"
                ib.put(p.name, p.projectDir);
            }
        }
        model.info.project_includedBuilds = ib;

        if (gradleVersion.compareTo(VersionNumber.parse('3.3')) >= 0) {
            model.info.project_display_name = project.displayName;
        }
        try {
            model.info.buildClassPath = storeSet(project.buildscript.configurations.classpath.files)
        } catch (Exception e) {
            model.noteProblem(e);
        }
        Set<String[]> tasks = new HashSet<>();
        for (org.gradle.api.Task t : project.getTasks()) {
            String[] arr = [t.path, t.group, t.name, t.description];
            tasks.add(arr);
        }
        model.info.tasks = tasks;
        model.ext.perf.meta = System.currentTimeMillis() - time
    }

    private void detectPlugins(NbProjectInfoModel model) {
        long time = System.currentTimeMillis()
        Set<String> plugins = new HashSet<>();
        for (String plugin: ['base', 'java-base', 'java', 'war', \
            'scala-base', 'scala', 'groovy-base', 'groovy',\
            'distribution', 'application', 'maven', 'osgi', \
            'jacoco', 'checkstyle', 'pmd', 'findbugs', 'ear', \
            'play', 'java-library-distribution', 'maven-publish',
            'ivy-publish', 'antlr', \
            'org.springframework.boot', \
            'com.github.lkishalmi.gatling', \
            'com.android.library', 'com.android.application']) {
            if (project.plugins.hasPlugin(plugin)) {
                plugins.add(plugin);
            }
        }
        model.info.plugins = plugins;
        model.ext.perf.plugins = System.currentTimeMillis() - time
    }

    private void detectTests(NbProjectInfoModel model) {
        Set<File> testClassesRoots = new HashSet<>()
        if (gradleVersion.compareTo(VersionNumber.parse('4.0')) >= 0) {
            project.tasks.withType(Test) { task ->
                task.testClassesDirs.each() { it -> testClassesRoots.add(it) }
            }
        } else {
            project.tasks.withType(Test) { task ->
                testClassesRoots.add(task.testClassesDir)
            }
        }
        model.info["test_classes_dirs"] = storeSet(testClassesRoots)

        if (project.plugins.hasPlugin('jacoco')) {
            Set<File> coverageData = new HashSet<>()
            project.tasks.withType(Test) { task ->
                coverageData.add(task.jacoco.destinationFile)
            }
            model.info["jacoco_coverage_files"] = storeSet(coverageData)
        }
    }

    private void detectProps(NbProjectInfoModel model) {
        Map<String, String> nbprops = new HashMap<>()
        project.properties.each {key, value ->
            if (key.startsWith(NB_PREFIX)) {
                nbprops.put(key - NB_PREFIX, String.valueOf(value))
            }
        }
        model.info.nbprops = nbprops
    }

    private void detectSources(NbProjectInfoModel model) {
        long time = System.currentTimeMillis()
        Map<String, Set<File>> sources = new HashMap<>()
        Map<String, File> outputs = new HashMap<>()
        Map<String, Map<String, Set<File>>> classpaths = new HashMap<>()

        boolean hasJava = project.plugins.hasPlugin('java-base')
        boolean hasGroovy = project.plugins.hasPlugin('groovy-base')
        boolean hasScala = project.plugins.hasPlugin('scala-base')

        if (hasJava) {
            if (project.sourceSets != null) {
                model.info.sourcesets = storeSet(project.sourceSets.names);
                project.sourceSets.each() { sourceSet ->
                    ['JAVA', 'GROOVY', 'SCALA'].each() { lang ->
                        def compileTask = project.tasks.findByName(sourceSet.getCompileTaskName(lang.toLowerCase()))
                        if (compileTask != null) {
                            model.info["sourceset_${sourceSet.name}_${lang}_source_compatibility"] = compileTask.sourceCompatibility
                            model.info["sourceset_${sourceSet.name}_${lang}_target_compatibility"] = compileTask.targetCompatibility
                            List<String> compilerArgs = []
                            try {
                                compilerArgs = compileTask.options.allCompilerArgs
                            } catch (Throwable ex) {
                                compilerArgs = compileTask.options.compilerArgs
                            }
                            model.info["sourceset_${sourceSet.name}_${lang}_compiler_args"] = new ArrayList<String>(compilerArgs)
                        }
                    }
                    model.info["sourceset_${sourceSet.name}_JAVA"] = storeSet(sourceSet.java.srcDirs);
                    model.info["sourceset_${sourceSet.name}_RESOURCES"] = storeSet(sourceSet.resources.srcDirs);
                    if (hasGroovy)
                        model.info["sourceset_${sourceSet.name}_GROOVY"] = storeSet(sourceSet.groovy.srcDirs);
                    if (hasScala)
                        model.info["sourceset_${sourceSet.name}_SCALA"] = storeSet(sourceSet.scala.srcDirs);
                    if (gradleVersion.compareTo(VersionNumber.parse('4.0')) >= 0) {
                        def dirs = new LinkedHashSet<File>();
                        // classesDirs is just an iteratable
                        for (def dir in sourceSet.output.classesDirs) {
                            dirs.add(dir);
                        }
                        model.info["sourceset_${sourceSet.name}_output_classes"] = storeSet(dirs);
                    } else {
                        model.info["sourceset_${sourceSet.name}_output_classes"] = Collections.singleton(sourceSet.output.classesDir);
                    }
                    model.info["sourceset_${sourceSet.name}_output_resources"] = sourceSet.output.resourcesDir;
                    try {
                        model.info["sourceset_${sourceSet.name}_classpath_compile"] = storeSet(sourceSet.compileClasspath.files);
                        model.info["sourceset_${sourceSet.name}_classpath_runtime"] = storeSet(sourceSet.runtimeClasspath.files);
                    } catch(Exception e) {
                        model.noteProblem(e)
                    }
                    model.info["sourceset_${sourceSet.name}_configuration_compile"] = sourceSet.compileConfigurationName;
                    model.info["sourceset_${sourceSet.name}_configuration_runtime"] = sourceSet.runtimeConfigurationName;
                }
            } else {
                model.info.sourcesets = Collections.emptySet();
                model.noteProblem('No sourceSets found on this project. This project mightbe a Model/Rule based one which is not supported at the moment.')
            }
        }
        model.ext.perf.sources = System.currentTimeMillis() - time
    }

    private void detectArtifacts(NbProjectInfoModel model) {
        long time = System.currentTimeMillis()
        if (project.plugins.hasPlugin('java')) {
            model.info.main_jar = project.jar.archivePath
        }
        if (project.plugins.hasPlugin('war')) {
            model.info.main_war = project.war.archivePath
            model.info.webapp_dir = project.webAppDir
            model.info.webxml = project.war.webXml
            try {
              model.info.exploded_war_dir = project.explodedWar.destinationDir
            } catch(Exception e) {
                model.noteProblem(e)
            }
            try {
                model.info.web_classpath = project.war.classpath.files
            } catch(Exception e) {
                model.noteProblem(e)
            }
        }
        def archives = [:]
        project.tasks.withType(Jar) { jar ->
            archives.put(jar.classifier, jar.archivePath)
        }
        model.info.archives = new HashMap(archives);
        model.ext.perf.artifacts = System.currentTimeMillis() - time
    }

    private static boolean resolvable(Configuration conf) {
        try{
            return conf.canBeResolved
        } catch (MissingPropertyException ex){
            return true
        }
    }

    private void detectDependencies(NbProjectInfoModel model) {
        long time = System.currentTimeMillis()
        Set ids = new HashSet();
        Map<String, File> projects = new HashMap();
        Map<String, String> unresolvedProblems = new HashMap()
        Map<String, Set<File>> resolvedJvmArtifacts = new HashMap()
        def visibleConfigurations = configurationsToSave()
        visibleConfigurations.each() {
            model.info["configuration_${it.name}_non_resolving"] = !resolvable(it)
            model.info["configuration_${it.name}_transitive"] = it.transitive
            model.info["configuration_${it.name}_extendsFrom"] = storeSet(it.extendsFrom.collect {it.name})
            model.info["configuration_${it.name}_description"] = it.description
        }
        visibleConfigurations = visibleConfigurations.findAll() { resolvable(it) }
        visibleConfigurations.each() {
            long time_inspect_conf = System.currentTimeMillis()
            def componentIds = []
            def unresolvedIds = []
            def projectNames = []
            try {
                it.incoming.resolutionResult.allDependencies.each {
                    if (it instanceof ResolvedDependencyResult) {
                        if (it.requested instanceof ModuleComponentSelector) {
                            componentIds.add(it.selected.id)
                        }
                    }
                    if (it instanceof UnresolvedDependencyResult) {
                        def id = it.requested.displayName
                        unresolvedIds.add(id)
                        unresolvedProblems.put(id, it.failure.message)
                    }
                }
            } catch (ResolveException ex) {
                model.noteProblem(ex)
            }
            ids.addAll(componentIds)
            long time_project_deps = System.currentTimeMillis()
            model.ext.perf["dependency_inspect_${it.name}_module"] = time_project_deps - time_inspect_conf

            it.dependencies.withType(ProjectDependency) {
                def prj = it.dependencyProject
                projects.put(prj.name.toString(), prj.projectDir)
                projectNames += prj.name.toString()
            }
            long time_file_deps = System.currentTimeMillis()
            model.ext.perf["dependency_inspect_${it.name}_project"] = time_file_deps - time_project_deps
            Set<File> fileDeps = new HashSet<>()
            it.dependencies.withType(FileCollectionDependency) {
                fileDeps.addAll(it.resolve())
            }
            long time_collect = System.currentTimeMillis()
            model.ext.perf["dependency_inspect_${it.name}_file"] = time_collect - time_file_deps

            try {
                it.resolvedConfiguration.lenientConfiguration.getFirstLevelModuleDependencies(Specs.SATISFIES_ALL).each {
                    collectArtifacts(it, resolvedJvmArtifacts)
                }
            } catch (NullPointerException ex) {
                //This can happen if the configuration resolution had issues
            }
            long time_report = System.currentTimeMillis()
            model.ext.perf["dependency_inspect_${it.name}_collect"] = time_report - time_collect

            model.info["configuration_${it.name}_components"] = new HashSet(componentIds.collect {it.toString()})
            model.info["configuration_${it.name}_projects"] = storeSet(projectNames)
            model.info["configuration_${it.name}_files"] = new HashSet(fileDeps)
            model.info["configuration_${it.name}_unresolved"] = new HashSet(unresolvedIds)
            model.ext.perf["dependency_inspect_${it.name}_report"] = System.currentTimeMillis() - time_report
            model.ext.perf["dependency_inspect_${it.name}"] = System.currentTimeMillis() - time_inspect_conf
        }

        long time_exclude = System.currentTimeMillis()
        visibleConfigurations.each() {
            Set exclude = new HashSet()
            collectModuleDependencies(model, it.name, false, exclude)
            model.info["configuration_${it.name}_components"].removeAll(exclude)
            model.info["configuration_${it.name}_unresolved"].removeAll(exclude)
            model.info["configuration_${it.name}_files"].removeAll(exclude)
        }
        model.ext.perf.excludes = System.currentTimeMillis() - time_exclude

        model.ext.perf.offline = project.gradle.startParameter.offline
        Map<String, Set<File>> resolvedSourcesArtifacts = new HashMap<>();
        Map<String, Set<File>> resolvedJavadocArtifacts = new HashMap<>();
        if (project.gradle.startParameter.offline || project.hasProperty('downloadSources') || project.hasProperty('downloadJavadoc')) {
            long filter_time = System.currentTimeMillis()
            def filteredIds = ids
            def artifactTypes = project.gradle.startParameter.offline ? [SourcesArtifact, JavadocArtifact] : []
            if (project.hasProperty('downloadSources')) {
                String filter = project.getProperty('downloadSources')
                if (!'ALL'.equals(filter)) {
                    filteredIds = ids.findAll() {id -> id.toString().equals(filter)}
                    model.miscOnly = true
                }
                artifactTypes += SourcesArtifact
            }
            if (project.hasProperty('downloadJavadoc')) {
                String filter = project.getProperty('downloadJavadoc')
                if (!'ALL'.equals(filter)) {
                    filteredIds = ids.findAll() {id -> id.toString().equals(filter)}
                    model.miscOnly = true
                }
                artifactTypes += JavadocArtifact
            }
            long query_time = System.currentTimeMillis()
            model.ext.perf.dependencies_filter = query_time - filter_time
            def result = project.dependencies.createArtifactResolutionQuery()
            .forComponents(filteredIds)
            .withArtifacts(JvmLibrary, artifactTypes.toArray(new Class[artifactTypes.size()]))
            .execute()
            long collect_time = System.currentTimeMillis()
            model.ext.perf.dependencies_query = collect_time - query_time

            for (component in result.resolvedComponents) {
                def sources = component.getArtifacts(SourcesArtifact)
                if (!sources.isEmpty()) {
                    resolvedSourcesArtifacts.put(component.id.toString(), collectResolvedArtifacts(sources))
                }
                def javadocs = component.getArtifacts(JavadocArtifact)
                if (!javadocs.isEmpty()) {
                    resolvedJavadocArtifacts.put(component.id.toString(), collectResolvedArtifacts(javadocs))
                }
            }
            model.ext.perf.dependencies_collect = System.currentTimeMillis() - collect_time
        }

        model.ext.resolved_jvm_artifacts = resolvedJvmArtifacts
        model.ext.resolved_sources_artifacts = resolvedSourcesArtifacts
        model.ext.resolved_javadoc_artifacts = resolvedJavadocArtifacts
        model.info.project_dependencies = projects
        model.info.unresolved_problems = unresolvedProblems
        model.ext.perf.dependencies = System.currentTimeMillis() - time
    }

    private static Set<File> collectResolvedArtifacts(Set<ArtifactResult> res) {
        Set<File> ret = new LinkedHashSet<>();
        res.each() {
            if (it instanceof ResolvedArtifactResult) {
                ret.add(it.file)
            }
        }
        return storeSet(ret)
    }
    private static void collectArtifacts(ResolvedDependency dep, final Map<String, Set<File>> resolvedArtifacts) {
        String key = "${dep.moduleGroup}:${dep.moduleName}:${dep.moduleVersion}"
        if (!resolvedArtifacts.containsKey(key)) {
            Set<File> artifacts = storeSet(dep.moduleArtifacts.collect { it.file})
            resolvedArtifacts.put(key, artifacts)
            dep.children.each {collectArtifacts( it, resolvedArtifacts)}
        }
    }

    private void collectModuleDependencies(final NbProjectInfoModel model, String confiurationName, boolean includeRoot, final Set deps) {
        if (includeRoot && !model.info["configuration_${confiurationName}_non_resolving"]) {
            deps.addAll(model.info["configuration_${confiurationName}_components"])
            deps.addAll(model.info["configuration_${confiurationName}_unresolved"])
            deps.addAll(model.info["configuration_${confiurationName}_files"])
        }
            model.info["configuration_${confiurationName}_extendsFrom"].each {
                collectModuleDependencies(model, it, true, deps)
            }
        }

    private static <T extends Serializable> Set<T> storeSet(Collection<? extends T> c) {
        switch (c.size()) {
            case 0: return Collections.emptySet();
            case 1: return Collections.singleton(c.first())
            default: return new LinkedHashSet(c)
        }
    }

    def configurationsToSave() {
        def start = project.configurations.findAll() {conf ->
            !CONFIG_EXCLUDES.contains(conf.name)
        }
        Set<Configuration> ret = new LinkedHashSet<>()
        start.each() {conf ->
            ret.addAll(conf.hierarchy)
        }
        return ret
    }
}

