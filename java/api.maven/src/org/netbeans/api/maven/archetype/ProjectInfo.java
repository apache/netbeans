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

package org.netbeans.api.maven.archetype;

/**
 * Information about one project to be created.
 * 
 * @author Tomas Stupka
 * @since 1.0
 */
public final class ProjectInfo {
    
    private final org.netbeans.modules.maven.api.archetype.ProjectInfo delegate;
    
    /**
     * C'tor
     * 
     * @param groupId the group id
     * @param artifactId the artifact id
     * @param version the version 
     * @param packageName the package name
     * @since 1.0
     */
    public ProjectInfo(String groupId, String artifactId, String version, String packageName) {
        delegate = new org.netbeans.modules.maven.api.archetype.ProjectInfo(groupId, artifactId, version, packageName);
    }

    /**
     * Returns the group id
     * 
     * @return 
     * @since 1.0
     */
    public String getGroupId() {
        return delegate.groupId;
    }

    /**
     * Returns the artifact id.
     * 
     * @return 
     * @since 1.0
     */
    public String getArtifactId() {
        return delegate.artifactId;
    }

    /**
     * Returns the version.
     * 
     * @return 
     * @since 1.0
     */
    public String getVersion() {
        return delegate.version;
    }

    /**
     * Returns the package name.
     * 
     * @return 
     * @since 1.0
     */
    public String getPackageName() {
        return delegate.packageName;
    }
    
}
