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
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleBinaryForSource implements BinaryForSourceQueryImplementation {

    private final Project project;
    private final Map<URL, Res> cache = new HashMap<>();

    public GradleBinaryForSource(Project project) {
        this.project = project;
    }

    @Override
    public BinaryForSourceQuery.Result findBinaryRoots(URL sourceRoot) {
        Res ret = cache.get(sourceRoot);
        if (ret == null) {
            if ("file".equals(sourceRoot.getProtocol())) {  //NOI18N
                try {
                    File root = FileUtil.normalizeFile(Utilities.toFile(sourceRoot.toURI()));
                    GradleJavaProject prj = GradleJavaProject.get(project);
                    for (GradleJavaSourceSet ss : prj.getSourceSets().values()) {
                        for (GradleJavaSourceSet.SourceType type : GradleJavaSourceSet.SourceType.values()) {
                            for (File sourceDir : ss.getSourceDirs(type)) {
                                if (root.equals(sourceDir)) {
                                    ret = new Res(project, ss.getName(),
                                            type == GradleJavaSourceSet.SourceType.RESOURCES);
                                    cache.put(sourceRoot, ret);
                                    return ret;
                                }
                            }
                        }
                    }
                } catch (URISyntaxException ex) {

                }
            }
        }
        return ret;
    }

    public static class Res implements BinaryForSourceQuery.Result {

        private final Project prj;
        private final String sourceSetName;
        private final boolean resource;

        public Res(Project prj, String sourceSetName, boolean resource) {
            this.prj = prj;
            this.sourceSetName = sourceSetName;
            this.resource = resource;
        }

        @Override
        public URL[] getRoots() {
            GradleJavaProject gjp = GradleJavaProject.get(prj);
            GradleJavaSourceSet ss = gjp != null ? gjp.getSourceSets().get(sourceSetName) : null;
            if (ss == null) {
                return new URL[0];
            }
            List<URL> urls = new ArrayList<>(2);
            List<File> targets = new ArrayList<>(2);
            if (resource) {
                if (ss.getOutputResources() != null) {
                    targets.add(ss.getOutputResources());
                }
            } else {
                targets.addAll(ss.getOutputClassDirs());
            }
            for (File target : targets) {
                if (target.exists()) {
                    urls.add(FileUtil.urlForArchiveOrDir(target));
                }
            }
            return urls.toArray(new URL[0]);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

    }
}
