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

import org.netbeans.modules.gradle.api.NbGradleProject;
import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Result;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Trigger;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = AnnotationProcessingQueryImplementation.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/java-base")
public class GradleAnnotationProcessorQueryImpl implements AnnotationProcessingQueryImplementation {

    private static final SpecificationVersion VER16 = new SpecificationVersion("1.6");

    final Project project;

    public GradleAnnotationProcessorQueryImpl(Project project) {
        this.project = project;
    }

    @Override
    public Result getAnnotationProcessingOptions(FileObject file) {
        String sourceLevel = SourceLevelQuery.getSourceLevel(file);
        return isJava16orLater(sourceLevel) ? ALWAYS : null;
    }

    private boolean isJava16orLater(String sourceLevel) {
        return sourceLevel != null
                ? (new SpecificationVersion(sourceLevel).compareTo(VER16) >= 0)
                : true; // We assume that the source level is at least 1.6 if unknown.
    }

    private static final Result ALWAYS = new Result() {

        @Override
        public Set<? extends Trigger> annotationProcessingEnabled() {
            return EnumSet.allOf(Trigger.class);
        }

        @Override
        public Iterable<? extends String> annotationProcessorsToRun() {
            return null;
        }

        @Override
        public URL sourceOutputDirectory() {
            return null;
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
