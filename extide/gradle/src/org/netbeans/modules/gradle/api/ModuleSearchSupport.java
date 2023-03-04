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

package org.netbeans.modules.gradle.api;

import java.util.Set;

/**
 * Classes implementing this interface allows searching for
 * {@link org.netbeans.modules.gradle.api.GradleDependency.ModuleDependency ModuleDependency}.
 *
 * This can be used to determine the used dependencies and their version,
 * like the used JUnit or Java EE versions in a project or configuration.
 *
 * @since 1.0
 * @author Laszlo Kishalmi
 */
public interface ModuleSearchSupport {

    /**
     * Search for module dependencies, matches the given filter criteria.
     * Specifying {@code null} for any of the parameters would mean match
     * all for that parameter.
     *
     * @see java.util.regex.Pattern
     * {@
     * @param group regexp matcher for the group part.
     * @param artifact regexp matcher for the artifact part.
     * @param version regexp matcher for the version part.
     *
     * @return Set of module dependencies that match the criteria.
     */
    public Set<GradleDependency.ModuleDependency> findModules(String group, String artifact, String version);

    /**
     * Search for module dependencies, matches the given filter criteria.
     * The format of the criteria is a colon separated string:
     * {@literal "<group>:<atrifact>:<version>"}, empty pattern mean match all
     * for that part.
     *
     * @see #findModules(java.lang.String, java.lang.String, java.lang.String)
     *
     * @param gav the filter criteria in string form
     *
     * @return Set of module dependencies that match the criteria.
     */
    public Set<GradleDependency.ModuleDependency> findModules(String gav);
}
