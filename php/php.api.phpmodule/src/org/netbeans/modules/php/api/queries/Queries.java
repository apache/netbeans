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
package org.netbeans.modules.php.api.queries;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.filesystems.FileObject;

/**
 * Factory for all queries.
 * @since 2.24
 */
public final class Queries {

    private static final PhpVisibilityQuery DEFAULT_PHP_VISIBILITY_QUERY = new DefaultPhpVisibilityQuery();


    private Queries() {
    }

    /**
     * Get PHP visibility query for the given PHP module. If the PHP module is {@code null},
     * {@link VisibilityQuery#getDefault() default} visibility query is returned.
     * @param phpModule PHP module, can be {@code null}
     * @return PHP visibility query
     */
    public static PhpVisibilityQuery getVisibilityQuery(@NullAllowed PhpModule phpModule) {
        if (phpModule == null) {
            return DEFAULT_PHP_VISIBILITY_QUERY;
        }
        PhpVisibilityQuery visibilityQuery = phpModule.getLookup().lookup(PhpVisibilityQuery.class);
        assert visibilityQuery != null : "No php visibility query for php module " + phpModule.getClass().getName();
        return visibilityQuery;
    }

    //~ Inner classes

    private static final class DefaultPhpVisibilityQuery implements PhpVisibilityQuery {

        @Override
        public boolean isVisible(File file) {
            return VisibilityQuery.getDefault().isVisible(file);
        }

        @Override
        public boolean isVisible(FileObject file) {
            return VisibilityQuery.getDefault().isVisible(file);
        }

        @Override
        public Collection<FileObject> getIgnoredFiles() {
            return Collections.emptyList();
        }

        @Override
        public Collection<FileObject> getCodeAnalysisExcludeFiles() {
            return Collections.emptyList();
        }

    }

}
