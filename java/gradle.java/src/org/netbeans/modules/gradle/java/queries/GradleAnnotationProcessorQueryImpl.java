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

package org.netbeans.modules.gradle.java.queries;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Result;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Trigger;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.SourceType.JAVA;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Laszlo Kishalmi
 */
public class GradleAnnotationProcessorQueryImpl implements AnnotationProcessingQueryImplementation {

    private static final SpecificationVersion VER16 = new SpecificationVersion("1.6");
    final Map<GradleJavaSourceSet, Result> cache = new WeakHashMap<>();

    final Project project;

    public GradleAnnotationProcessorQueryImpl(Project project) {
        this.project = project;
    }

    @Override
    public Result getAnnotationProcessingOptions(FileObject file) {
        String sourceLevel = SourceLevelQuery.getSourceLevel(file);
        Result ret = null;
        if (isJava16orLater(sourceLevel)) {
            GradleJavaProject gjp = GradleJavaProject.get(project);
            GradleJavaSourceSet ss = gjp.containingSourceSet(FileUtil.toFile(file));
            if (ss != null) {
                ret = cache.get(ss);
                if (ret == null) {
                    ret = new GradleSourceSetResult(ss);
                    cache.put(ss, ret);
                }
            }
        }
        return ret;
    }

    private boolean isJava16orLater(String sourceLevel) {
        return sourceLevel != null
                ? (new SpecificationVersion(sourceLevel).compareTo(VER16) >= 0)
                : true; // We assume that the source level is at least 1.6 if unknown.
    }

    private static class GradleSourceSetResult implements Result {

        URL outputDir = null;
        List<String> annotationProcessors = null;
        boolean enabled = true;

        GradleSourceSetResult(GradleJavaSourceSet ss) {
            for (File dir : ss.getGeneratedSourcesDirs()) {
                if (dir.getPath().contains("annotationProcessor")) { //NOI18N
                    try {
                        outputDir = dir.toURI().toURL();
                    } catch (MalformedURLException ex) {}
                }
            }
            Iterator<String> compilerArgs = ss.getCompilerArgs(JAVA).iterator();
            while(compilerArgs.hasNext()) {
                String arg = compilerArgs.next();
                if ("-proc:none".equals(arg)) { //NOI18N
                    enabled = false;
                }
                if ("-processor".equals(arg) && compilerArgs.hasNext()) {
                    if (annotationProcessors == null) {
                        annotationProcessors = new LinkedList<>();
                    }
                    String[] processors = compilerArgs.next().split(",");
                    annotationProcessors.addAll(Arrays.asList(processors));
                }
            }
            
        }

        @Override
        public Set<? extends Trigger> annotationProcessingEnabled() {
            return enabled ? EnumSet.allOf(Trigger.class) : EnumSet.noneOf(Trigger.class);
        }

        @Override
        public Iterable<? extends String> annotationProcessorsToRun() {
            return annotationProcessors;
        }

        @Override
        public URL sourceOutputDirectory() {
            return outputDir;
        }

        @Override
        public Map<? extends String, ? extends String> processorOptions() {
            return Collections.<String, String>emptyMap();
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

    };
}
