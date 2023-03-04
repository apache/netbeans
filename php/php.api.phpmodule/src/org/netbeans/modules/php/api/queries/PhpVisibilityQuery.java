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
import org.openide.filesystems.FileObject;

/**
 * PHP visibility query specific for PHP module.
 * @since 2.24
 */
public interface PhpVisibilityQuery {

    /**
     * Check whether a file is recommended to be visible.
     * @param file a file which should be checked
     * @return {@code true} if it is recommended to show this file
     */
    boolean isVisible(File file);

    /**
     * Check whether a file is recommended to be visible.
     * @param file a file which should be checked
     * @return {@code true} if it is recommended to show this file
     */
    boolean isVisible(FileObject file);

    /**
     * Get ignored files for this PHP module.
     * @return collection of ignored files, can be empty but never {@code null}
     */
    Collection<FileObject> getIgnoredFiles();

    /**
     * Get code analysis exclude files for this PHP module.
     * <p>
     * This method automatically returns all {@link #getIgnoredFiles() ignored files}
     * together with extra folders for code analysis.
     * @return collection of code analysis exclude files, can be empty but never {@code null}
     * @since 2.25
     */
    Collection<FileObject> getCodeAnalysisExcludeFiles();

}
