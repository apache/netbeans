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

package org.netbeans.modules.gradle.java.queries;

import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.SourceType;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleTestForSourceImpl implements MultipleRootsUnitTestForSourceQueryImplementation {

    private static final SourceType[] MAIN_SOURCES = new SourceType[]{
        SourceType.JAVA, SourceType.GROOVY, SourceType.SCALA, SourceType.KOTLIN
    };

    private final Project project;

    public GradleTestForSourceImpl(Project project) {
        this.project = project;
    }

    @Override
    public URL[] findUnitTests(FileObject fo) {
        Map<String, GradleJavaSourceSet> sourceSets = GradleJavaProject.get(project).getSourceSets();
        List<File> retFile = new ArrayList<>();
        File param = FileUtil.toFile(fo);
        for (String name : sourceSets.keySet()) {
            //TODO: Cold use the test task to determine test source roots.
            if (name.contains("test") || name.contains("Test")) { //NOI18N
                GradleJavaSourceSet ss = sourceSets.get(name);
                for (SourceType type : MAIN_SOURCES) {
                    for (File src : ss.getSourceDirs(type)) {
                        if (param.equals(src)) {
                            return null;
                        } else {
                            retFile.add(src);
                        }
                    }
                }
            }
        }
        if (retFile.isEmpty()) {
            return null;
        }
        List<URL> ret = new ArrayList<>(retFile.size());
        for (File f : retFile) {
            ret.add(fileToURL(f));
        }
        return ret.toArray(new URL[0]);
    }

    @Override
    public URL[] findSources(FileObject fo) {
        Map<String, GradleJavaSourceSet> sourceSets = GradleJavaProject.get(project).getSourceSets();
        List<File> retFile = new ArrayList<>();
        File param = FileUtil.toFile(fo);
        for (String name : sourceSets.keySet()) {
            //TODO: Cold use the test task to determine test source roots.
            if (!name.contains("test") && !name.contains("Test")) { //NOI18N
                GradleJavaSourceSet ss = sourceSets.get(name);
                for (SourceType type : MAIN_SOURCES) {
                    for (File src : ss.getSourceDirs(type)) {
                        if (param.equals(src)) {
                            return null;
                        } else {
                            retFile.add(src);
                        }
                    }
                }
            }
        }
        if (retFile.isEmpty()) {
            return null;
        }
        List<URL> ret = new ArrayList<>(retFile.size());
        for (File f : retFile) {
            if (f.isDirectory()) {
                ret.add(fileToURL(f));
            }
        }
        return ret.toArray(new URL[0]);
    }

    private static URL fileToURL(File f) {
        URL ret = null;
        try {
            URI uri = Utilities.toURI(f);
            ret = uri.toURL();
            if (!ret.toExternalForm().endsWith("/")) { //NOI18N
                ret = new URL(ret.toExternalForm() + "/"); //NOI18N
            }
        } catch (MalformedURLException ex) {
            //Shall not happen
        }
        return ret;
    }
}
