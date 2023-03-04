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

package org.netbeans.modules.maven.spi;

import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provides a means of detecting a project's packaging for purposes of IDE internals.
 * Semi deprecated. Please be very careful when implementing this, overriding
 * packaging can mean the original packaging's support will be gone. (eg. when a war project is turned into bundle project using this interface, 
 * then all war project functionality will be gone and project will only pose as bundle/osgi)
 * @see NbMavenProject#getPackagingType
 * @see ServiceProvider
 * @since 2.20
 */
public interface PackagingProvider {

    /**
     * Optionally supplies a packaging for a given project.
     * Note that this is called early in project construction
     * so only basic services may be available in project lookup.
     * @param project a Maven project
     * @return a packaging other than {@link MavenProject#getPackaging}, or null for no special behavior
     */
    @CheckForNull String packaging(@NonNull Project project);

}
