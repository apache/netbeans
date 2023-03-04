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
package org.netbeans.modules.web.project.spi;

import org.netbeans.api.project.Project;

/**
 * Allows removing broken library references from a project while the project
 * is being open.
 *
 * <p>Implementations of this interface are registered in the
 * <code>Projects/org-netbeans-modules-web-project/BrokenLibraryRefFilterProviders</code>
 * folder in the default file system. When a web project is opened,
 * the {@link #createFilter} method of all implementations is called
 * to create {@link BrokenLibraryRefFilter a filter for broken references}. 
 * This filter is then queried for all broken library references in the project.
 * If at least one filter returns <code>true</code>, the library reference is removed.</p>
 *
 * @author Andrei Badea
 */
public interface BrokenLibraryRefFilterProvider {

    /**
     * Creates a filter for the broken library references in the given
     * project.
     * 
     * @param  project the project being opened; never null.
     * @return a filter for the broken library references in <code>project</code>
     *         or null if this project does not need to be filtered.
     */
    public BrokenLibraryRefFilter createFilter(Project project);
}
