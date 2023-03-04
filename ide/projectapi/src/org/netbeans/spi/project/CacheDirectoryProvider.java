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

package org.netbeans.spi.project;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;

/**
 * Ability for a project to permit other modules to store arbitrary cache
 * data associated with the project.
 * <p>Implementors should place an instance in {@link Project#getLookup}.
 * Callers should use {@link ProjectUtils#getCacheDirectory} rather than looking for this interface.
 * @author Jesse Glick
 */
public interface CacheDirectoryProvider {

    /**
     * Get a directory in which modules may store disposable cached information
     * about the project, such as an index of classes it contains.
     * This directory should be considered non-sharable by
     * {@link org.netbeans.api.queries.SharabilityQuery}.
     * Modules are responsible for preventing name clashes in this directory by
     * using sufficiently unique names for child files and folders.
     * @return a cache directory
     * @throws IOException if it cannot be created or loaded
     */
    FileObject getCacheDirectory() throws IOException;

}
