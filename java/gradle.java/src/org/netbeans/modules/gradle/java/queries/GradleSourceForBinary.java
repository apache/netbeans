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
import java.util.EnumMap;
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
    
    private static Map<SourceType, String> sourceMimeTypes = new EnumMap<>(SourceType.class);
    
    static {
        sourceMimeTypes.put(JAVA, "text/x-java");
        sourceMimeTypes.put(GROOVY, "text/x-groovy");
        sourceMimeTypes.put(SCALA, "text/x-scala");
        sourceMimeTypes.put(KOTLIN, "text/x-kotlin");
    }

    private final Project project;
    private final Map<URL, Res> cache = new HashMap<>();
    
    public GradleSourceForBinary(Project project) {
        this.project = project;
    }
    
    private static final EnumSet<SourceType> ALL_LANGUAGES;
    
    static {
        EnumSet<SourceType> s = EnumSet.allOf(SourceType.class);
        s.remove(SourceType.RESOURCES);
        s.remove(SourceType.GENERATED);
        
        ALL_LANGUAGES = s;
    }
    
    private Result languageSourceForOutput(GradleJavaSourceSet ss, File outputDir) {
        
        for (SourceType st : ALL_LANGUAGES) {
            File f = ss.getOutputClassDir(st);
            if (outputDir.equals(f)) {
                // special case for java here; Java replaces its classpath entries by corresponding cache entries with
                // indexed sources (.sig files) so that it tracks not compiled source changes. Other languages do not generate
                // the expected files.
                // The proper solution will be to change java implementation to use output folder as a fallback if nothing
                // is found in the cache; then this special case may be removed.
                boolean canPreferSource = st == SourceType.JAVA;
                return new Res(project, ss.getName(), EnumSet.of(st), canPreferSource);
            }
        }
        return new Res(project, ss.getName(), ALL_LANGUAGES, false);
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
                                    return languageSourceForOutput(ss, outputDir);
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
        private final boolean preferSources;
        
        public Res(Project project, String sourceSet, Set<SourceType> sourceTypes) {
            this(project, sourceSet, sourceTypes, true);
        }

        public Res(Project project, String sourceSet, Set<SourceType> sourceTypes, boolean preferSources) {
            this.project = project;
            this.sourceSet = sourceSet;
            this.sourceTypes = sourceTypes;
            this.preferSources = preferSources;
            listener = (PropertyChangeEvent evt) -> {
                if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                    support.fireChange();
                }
            };
            NbGradleProject.addPropertyChangeListener(project, WeakListeners.propertyChange(listener, NbGradleProject.get(project)));
        }

        @Override
        public boolean preferSources() {
            return preferSources;
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
            return roots.toArray(new FileObject[0]);
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
