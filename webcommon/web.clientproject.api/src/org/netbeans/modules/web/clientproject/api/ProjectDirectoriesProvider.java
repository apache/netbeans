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

package org.netbeans.modules.web.clientproject.api;

import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.filesystems.FileObject;

/**
 * Provider for project directories.
 * <p>
 * If supported, implementations can be found in project's lookup.
 * @since 1.49
 */
public interface ProjectDirectoriesProvider {

    /**
     * Get test directory. If the test directory is not set yet, user will be asked for selecting it
     * if {@code showFileChooser} is {@code true}.
     * @param showFileChooser show file chooser if there is no test directory set yet
     * @return test directory; can be {@code null} for none, corrupted etc. folder
     * @since 1.61
     */
    @CheckForNull
    FileObject getTestDirectory(boolean showFileChooser);

    /**
     * Get selenium test directory. If the selenium test directory is not set yet, user will be asked for selecting it
     * if {@code showFileChooser} is {@code true}.
     * @param showFileChooser show file chooser if there is no selenium test directory set yet
     * @return selenium test directory; can be {@code null} for none, corrupted etc. folder
     * @since 1.83
     */
    @CheckForNull
    FileObject getTestSeleniumDirectory(boolean showFileChooser);

}
