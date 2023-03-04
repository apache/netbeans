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

package org.netbeans.modules.php.project;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.filesystems.FileObject;

/**
 * @author Tomas Mysik
 */
public abstract class PhpVisibilityQuery {
    private static final PhpVisibilityQuery DEFAULT = new PhpVisibilityQuery() {
        @Override
        public boolean isVisible(FileObject file) {
            return VisibilityQuery.getDefault().isVisible(file);
        }

        @Override
        public boolean isVisible(File file) {
            return VisibilityQuery.getDefault().isVisible(file);
        }
    };

    private PhpVisibilityQuery() {
    }

    public abstract boolean isVisible(FileObject file);
    public abstract boolean isVisible(File file);

    public static PhpVisibilityQuery forProject(final PhpProject project) {
        return new PhpVisibilityQuery() {
            @Override
            public boolean isVisible(FileObject file) {
                return project.isVisible(file);
            }

            @Override
            public boolean isVisible(File file) {
                return project.isVisible(file);
            }
        };
    }

    public static PhpVisibilityQuery getDefault() {
        return DEFAULT;
    }

    //~ Inner classes

    public static final class PhpVisibilityQueryImpl implements org.netbeans.modules.php.api.queries.PhpVisibilityQuery {

        private final PhpProject project;


        public PhpVisibilityQueryImpl(PhpProject project) {
            assert project != null;
            this.project = project;
        }

        @Override
        public boolean isVisible(File file) {
            return PhpVisibilityQuery.forProject(project).isVisible(file);
        }

        @Override
        public boolean isVisible(FileObject file) {
            return PhpVisibilityQuery.forProject(project).isVisible(file);
        }

        @Override
        public Collection<FileObject> getIgnoredFiles() {
            return project.getIgnoredFileObjects();
        }

        @Override
        public Collection<FileObject> getCodeAnalysisExcludeFiles() {
            Set<FileObject> excludedFileObjects = new HashSet<>();
            excludedFileObjects.addAll(getIgnoredFiles());
            excludedFileObjects.addAll(project.getCodeAnalysisExcludeFileObjects());
            return excludedFileObjects;
        }

    }

}
