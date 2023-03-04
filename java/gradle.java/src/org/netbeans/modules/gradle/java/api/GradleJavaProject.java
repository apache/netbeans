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

package org.netbeans.modules.gradle.java.api;

import org.netbeans.modules.gradle.api.NbGradleProject;
import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class GradleJavaProject implements Serializable {

    public static final String CLASSIFIER_NONE = "";           //NOI18N
    public static final String CLASSIFIER_TESTS = "tests";     //NOI18N
    public static final String CLASSIFIER_JAVADOC = "javadoc"; //NOI18N
    public static final String CLASSIFIER_SOURCES = "sources"; //NOI18N

    File mainJar;
    Set<File> testClassesRoots;
    Set<File> coverageData;
    Map<String, File> archives;

    transient Map<File, GradleJavaSourceSet> fileToSourceSetCache;

    Map<String, GradleJavaSourceSet> sourceSets = new LinkedHashMap<>();

    GradleJavaProject() {
    }

    public File getMainJar() {
        return mainJar;
    }

    public File getArchive(String classifier) {
        return archives != null ? archives.get(classifier) : null;
    }

    public Set<File> getCoverageData() {
        return coverageData;
    }

    public Set<File> getTestClassesRoots() {
        return testClassesRoots;
    }

    public Map<String, GradleJavaSourceSet> getSourceSets() {
        return Collections.unmodifiableMap(sourceSets);
    }

    public GradleJavaSourceSet getMainSourceSet() {
        return sourceSets.get(GradleJavaSourceSet.MAIN_SOURCESET_NAME);
    }
    
    public GradleJavaSourceSet containingSourceSet(File f) {
        if (fileToSourceSetCache == null) {
            fileToSourceSetCache = new WeakHashMap<>();
        }
        GradleJavaSourceSet ret = fileToSourceSetCache.get(f);
        if (ret == null) {
            for (GradleJavaSourceSet sourceSet : sourceSets.values()) {
                if (sourceSet.contains(f)) {
                    ret = sourceSet;
                    fileToSourceSetCache.put(f, ret);
                    break;
                }
            }
        }
        return ret;
    }

    protected GradleJavaSourceSet createSourceSet(String name) {
        GradleJavaSourceSet ret = new GradleJavaSourceSet(name);
        sourceSets.put(name, ret);
        return ret;
    }

    void resolveSourceSetDependencies() {
        for (GradleJavaSourceSet source : sourceSets.values()) {
            if (source.compileClassPath != null) {
                for (File cpEntry : source.getCompileClassPath()) {
                    for (GradleJavaSourceSet depends : sourceSets.values()) {
                        if (depends == source) {
                            continue;
                        }
                        if (depends.outputContains(cpEntry)) {
                            if (source.sourceDependencies.isEmpty()) {
                                source.sourceDependencies = new LinkedHashSet<>();
                            }
                            source.sourceDependencies.add(depends);
                        }
                    }
                }
            }
        }
    }

    public static GradleJavaProject get(Project project) {
        NbGradleProject gprj = NbGradleProject.get(project);
        return gprj != null ? gprj.projectLookup(GradleJavaProject.class) : null;
    }

    static boolean parentOrSame(File f, File supposedParent) {
        if ((f == null) || (supposedParent == null)) {
            return false;
        }
        boolean ret = supposedParent.equals(f);
        File sparent = supposedParent.getParentFile();
        File parent = f;
        while (!ret && (parent != null) && !parent.equals(sparent)) {
            parent = parent.getParentFile();
            ret = supposedParent.equals(parent);
        }
        return ret;
    }

}
