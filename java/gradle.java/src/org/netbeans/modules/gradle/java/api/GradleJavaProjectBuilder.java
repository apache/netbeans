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

package org.netbeans.modules.gradle.java.api;

import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.modules.gradle.spi.ProjectInfoExtractor;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;
import static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.SourceType;
import static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.SourceType.*;

/**
 *
 * @author Laszlo Kishalmi
 */
@SuppressWarnings("unchecked")
final class GradleJavaProjectBuilder implements ProjectInfoExtractor.Result {

    public static final String JAVA_BASE_PLUGIN = "java-base";
    final Map<String, Object> info;
    final GradleJavaProject prj = new GradleJavaProject();

    GradleJavaProjectBuilder(Map<String, Object> info) {
        this.info = new TreeMap<>(info);
    }

    GradleJavaProjectBuilder build() {

        processTests();
        processSourceSets();
        processArtifacts();

        return this;
    }

    void processSourceSets() {
        Set<String> sourceSetNames = (Set<String>) info.get("sourcesets");
        if (sourceSetNames != null) {
            for (String name : sourceSetNames) {
                GradleJavaSourceSet sourceSet = prj.createSourceSet(name);
                for (SourceType type : SourceType.values()) {
                    Set<File> dirs = (Set<File>) info.get("sourceset_" + name + "_" + type.name());
                    if (dirs != null) {
                        Set<File> normalizedDirs = new LinkedHashSet<>();
                        for (File dir : dirs) {
                            normalizedDirs.add(FileUtil.normalizeFile(dir));
                        }
                        sourceSet.sources.put(type, normalizedDirs);
                    }
                }
                sourceSet.compileClassPath = (Set<File>) info.get("sourceset_" + name + "_classpath_compile");
                sourceSet.runtimeClassPath = (Set<File>) info.get("sourceset_" + name + "_classpath_runtime");
                sourceSet.annotationProcessorPath = (Set<File>) info.get("sourceset_" + name + "_classpath_annotation");
                sourceSet.compileConfigurationName = (String) info.get("sourceset_" + name + "_configuration_compile");
                sourceSet.runtimeConfigurationName = (String) info.get("sourceset_" + name + "_configuration_runtime");
                sourceSet.annotationProcessorConfigurationName = (String) info.get("sourceset_" + name + "_configuration_annotation");
                sourceSet.outputClassDirs = (Set<File>) info.get("sourceset_" + name + "_output_classes");
                sourceSet.outputResources = (File) info.get("sourceset_" + name + "_output_resources");

                Map<SourceType, String> sourceComp = new EnumMap<>(SourceType.class);
                Map<SourceType, String> targetComp = new EnumMap<>(SourceType.class);
                Map<SourceType, File> javaHomes = new EnumMap<>(SourceType.class);
                Map<SourceType, List<String>> compilerArgs = new EnumMap<>(SourceType.class);
                for (SourceType lang : Arrays.asList(JAVA, GROOVY, SCALA, KOTLIN)) {
                    String sc = (String) info.get("sourceset_" + name + "_" + lang.name() + "_source_compatibility");
                    String tc = (String) info.get("sourceset_" + name + "_" + lang.name() + "_target_compatibility");
                    if (sc != null) {
                        sourceComp.put(lang, sc);
                    }
                    if (tc != null) {
                        targetComp.put(lang, tc);
                    }
                    File javaHome = (File) info.get("sourceset_" + name + "_" + lang.name() + "_compiler_java_home");
                    if (javaHome != null) {
                        javaHomes.put(lang, javaHome);
                    }
                    List<String> compArgs = (List<String>) info.get("sourceset_" + name + "_" + lang.name() + "_compiler_args");
                    if (compArgs != null) {
                        compilerArgs.put(lang, Collections.unmodifiableList(compArgs));
                    }
                    // if detected, note the output dirs for individual language(s).
                    File f = (File)info.get("sourceset_" + name + "_" + lang.name() + "_output_classes");
                    if (f != null) {
                        sourceSet.outputs.put(lang, f);
                    }
                }
                sourceSet.sourcesCompatibility = Collections.unmodifiableMap(sourceComp);
                sourceSet.targetCompatibility = Collections.unmodifiableMap(targetComp);
                sourceSet.compilerJavaHomes = Collections.unmodifiableMap(javaHomes);
                sourceSet.compilerArgs = Collections.unmodifiableMap(compilerArgs);
                
                for (File out : sourceSet.getOutputClassDirs()) {
                    if (prj.getTestClassesRoots().contains(out)) {
                        sourceSet.testSourceSet = true;
                        break;
                    }
                }

                if ("main".equals(name)) {
                    sourceSet.webApp = (File) info.get("webapp_dir"); //NOI18N
                }
            }
        }
        prj.resolveSourceSetDependencies();
    }

    void processArtifacts() {
        prj.mainJar = (File) info.get("main_jar");
        prj.archives = (Map<String, File>) info.get("archives");
    }

    void processTests() {
        prj.testClassesRoots = (Set<File>) info.get("test_classes_dirs"); //NOI18N
        prj.testClassesRoots = prj.testClassesRoots != null ? Collections.unmodifiableSet(prj.testClassesRoots) : Collections.<File>emptySet();

        prj.coverageData = (Set<File>) info.get("jacoco_coverage_files"); //NOI18N
        prj.coverageData = prj.coverageData != null ? Collections.unmodifiableSet(prj.coverageData) : Collections.<File>emptySet();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Set getExtract() {
        return Collections.singleton(prj);
    }

    @Override
    public Set<String> getProblems() {
        return Collections.emptySet();
    }

    @ServiceProvider(service = ProjectInfoExtractor.class, position = 0)
    @SuppressWarnings("rawtypes")
    public static final class Extractor implements ProjectInfoExtractor {

        @Override
        public ProjectInfoExtractor.Result extract(Map<String, Object> props, Map<Class, Object> otherInfo) {
            GradleBaseProject gp = (GradleBaseProject) otherInfo.get(GradleBaseProject.class);
            assert gp != null : "GradleProject should have been evaluated first, check the position of this extractor!";
            return gp.getPlugins().contains(JAVA_BASE_PLUGIN)
                    ? new GradleJavaProjectBuilder(props).build()
                    : Result.NONE;
            }

        @Override
        public Result fallback(GradleFiles files) {

            File srcDir = new File(files.getProjectDir(), "src");

            if (!srcDir.isDirectory()) {
                return Result.NONE;
            }

            GradleJavaProject prj = new GradleJavaProject();

            File[] sourceSets = srcDir.listFiles();
            boolean isJava = false;
            for (File dir : sourceSets) {
                if (dir.isDirectory()) {
                    String name = dir.getName();
                    if (!"dist".equals(name)) {  //NOI18N
                        GradleJavaSourceSet gss = prj.createSourceSet(name);
                        File javaDir = new File(dir, "java");
                        if (javaDir.isDirectory()) {
                            gss.sources.put(GradleJavaSourceSet.SourceType.JAVA, Collections.singleton(javaDir));
                            isJava = true;
                        }
                        File resourcesDir = new File(dir, "resources");
                        if (resourcesDir.isDirectory()) {
                            gss.sources.put(GradleJavaSourceSet.SourceType.RESOURCES, Collections.singleton(resourcesDir));
                            isJava = true;
                        }
                        File groovyDir = new File(dir, "groovy");
                        if (groovyDir.isDirectory()) {
                            gss.sources.put(GradleJavaSourceSet.SourceType.GROOVY, Collections.singleton(groovyDir));
                            isJava = true;
                        }
                        File scalaDir = new File(dir, "scala");
                        if (scalaDir.isDirectory()) {
                            gss.sources.put(GradleJavaSourceSet.SourceType.SCALA, Collections.singleton(scalaDir));
                            isJava = true;
                        }
                        File kotlinDir = new File(dir, "kotlin");
                        if (kotlinDir.isDirectory()) {
                            gss.sources.put(GradleJavaSourceSet.SourceType.KOTLIN, Collections.singleton(kotlinDir));
                            isJava = true;
                        }
                    }
                }
            }
            return isJava ? new DefaultResult(prj) : Result.NONE;
        }
    }
}
