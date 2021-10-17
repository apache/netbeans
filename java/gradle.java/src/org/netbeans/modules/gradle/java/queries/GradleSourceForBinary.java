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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.SourceType;
import static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.SourceType.*;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.MAIN_SOURCESET_NAME;
import static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.TEST_SOURCESET_NAME;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import static org.netbeans.modules.gradle.api.NbGradleProject.Quality.*;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleSourceForBinary implements SourceForBinaryQueryImplementation2 {

    private final Project project;
    private final Map<URL, Res> cache = new HashMap<>();

    public GradleSourceForBinary(Project project) {
        this.project = project;
    }

    @Override
    public Result findSourceRoots2(URL binaryRoot) {
        Res ret = cache.get(binaryRoot);
        if (ret == null) {
            try {
                NbGradleProject watcher = NbGradleProject.get(project);
                if (watcher.getQuality().atLeast(FALLBACK)) {
                    GradleJavaProject prj = GradleJavaProject.get(project);
                    switch (binaryRoot.getProtocol()) {
                        case "file": {  //NOI18N
                            File root = FileUtil.normalizeFile(Utilities.toFile(binaryRoot.toURI()));
                            for (GradleJavaSourceSet ss : prj.getSourceSets().values()) {
                                File outputDir = ss.getCompilerArgs(JAVA).contains("--module-source-path") ? //NOI18N
                                        root.getParentFile() : root;
                                if (ss.getOutputClassDirs().contains(outputDir)) {
                                    ret = new Res(project, ss.getName(), EnumSet.of(JAVA, GROOVY, SCALA, GENERATED));
                                    break;
                                }
                                if ((ret == null) && root.equals(ss.getOutputResources())) {
                                    ret = new Res(project, ss.getName(), EnumSet.of(RESOURCES));
                                }
                                if (ret != null) {
                                    break;
                                }
                            }
                            break;
                        }
                        case "jar": { //NOI18N
                            File jar = FileUtil.normalizeFile(Utilities.toFile(FileUtil.getArchiveFile(binaryRoot).toURI()));
                            if (jar.equals(prj.getMainJar()) && prj.getSourceSets().containsKey(MAIN_SOURCESET_NAME)) {
                                ret = new Res(project, MAIN_SOURCESET_NAME, EnumSet.allOf(SourceType.class));
                            } else if (jar.equals(prj.getArchive(GradleJavaProject.CLASSIFIER_TESTS)) && prj.getSourceSets().containsKey(TEST_SOURCESET_NAME)) {
                                ret = new Res(project, TEST_SOURCESET_NAME, EnumSet.allOf(SourceType.class));
                            }
                            break;
                        }
                    }
                }
                if (ret != null) {
                    cache.put(binaryRoot, ret);
                }

            } catch (URISyntaxException ex) {

            }
        }
        return ret;
    }

    @Override
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        return findSourceRoots2(binaryRoot);
    }

    public static class Res implements Result {

        private final Project project;
        private final String sourceSet;
        private final Set<SourceType> sourceTypes;
        private final PropertyChangeListener listener;
        private final ChangeSupport support = new ChangeSupport(this);

        public Res(Project project, String sourceSet, Set<SourceType> sourceTypes) {
            this.project = project;
            this.sourceSet = sourceSet;
            this.sourceTypes = sourceTypes;
            listener = (PropertyChangeEvent evt) -> {
                if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                    support.fireChange();
                }
            };
            NbGradleProject.get(project).addPropertyChangeListener(WeakListeners.propertyChange(listener, project));
        }

        @Override
        public boolean preferSources() {
            return true;
        }

        @Override
        public FileObject[] getRoots() {
            List<FileObject> roots = new ArrayList<>();
            GradleJavaSourceSet ss = GradleJavaProject.get(project) != null
                    ? GradleJavaProject.get(project).getSourceSets().get(sourceSet)
                    : null;
            if (ss != null) {
                for (SourceType type : sourceTypes) {
                    Set<File> dirs = ss.getSourceDirs(type);
                    for (File dir : dirs) {
                        FileObject fo = FileUtil.toFileObject(dir);
                        if (fo != null) {
                            roots.add(fo);
                        }
                    }
                }
            }
            return roots.toArray(new FileObject[roots.size()]);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            synchronized (support) {
                support.addChangeListener(l);
            }
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            synchronized (support) {
                support.removeChangeListener(l);
            }
        }

    }
}
