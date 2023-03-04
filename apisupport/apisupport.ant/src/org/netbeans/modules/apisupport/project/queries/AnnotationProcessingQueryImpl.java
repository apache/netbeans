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

package org.netbeans.modules.apisupport.project.queries;

import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Result;
import org.netbeans.api.java.queries.AnnotationProcessingQuery.Trigger;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.java.queries.AnnotationProcessingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class AnnotationProcessingQueryImpl implements AnnotationProcessingQueryImplementation {

    private final NbModuleProject project;

    public AnnotationProcessingQueryImpl(NbModuleProject project) {
        this.project = project;
    }
    
    public @Override Result getAnnotationProcessingOptions(FileObject file) {
        if (inside(project.getSourceDirectory(), file)) {
            return new ResultImpl(FileUtil.urlForArchiveOrDir(project.getGeneratedClassesDirectory()));
        } else if (inside(project.getTestSourceDirectory("unit"), file)) {
            return new ResultImpl(FileUtil.urlForArchiveOrDir(project.getTestGeneratedClassesDirectory("unit")));
        } else if (inside(project.getTestSourceDirectory("qa-functional"), file)) {
            return new ResultImpl(FileUtil.urlForArchiveOrDir(project.getTestGeneratedClassesDirectory("qa-functional")));
        } else {
            return null;
        }
    }

    private static boolean inside(FileObject root, FileObject file) {
        return root != null && (file == root || FileUtil.isParentOf(root, file));
    }

    private static final class ResultImpl implements Result {

        private final URL dashS;

        ResultImpl(URL dashS) {
            this.dashS = dashS;
        }

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
            return dashS;
        }

        @Override
        public Map<? extends String, ? extends String> processorOptions() {
            return Collections.emptyMap();
        }

        @Override
        public void addChangeListener(ChangeListener l) {}

        @Override
        public void removeChangeListener(ChangeListener l) {}

    }

}
