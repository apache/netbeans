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

import org.netbeans.api.annotations.common.NonNull;

/**
 * Simple model class to describe a Maven archetype. To be created by ArchetypeProvider 
 * implementations, consumed by the New Maven Project wizard.
 * 
 * @author Tomas Stupka
 * @since 1.0
 */
public final class Archetype {

    private final org.netbeans.modules.maven.api.archetype.Archetype delegate;

    /**
     * C'tor 
     * 
     * @since 1.0
     */
    public Archetype() {
        delegate = new org.netbeans.modules.maven.api.archetype.Archetype();
    }
    
    /**
     * Returns the artifact id.
     * 
     * @return the artifact id
     * @since 1.0
     */
    public String getArtifactId() {
        return delegate.getArtifactId();
    }
    
    /**
     * Sets the artifact id
     * 
     * @param artifactId 
     * @since 1.0
     */
    public void setArtifactId(String artifactId) {
        delegate.setArtifactId(artifactId);
    }
    
    /**
     * Returns the group id.
     * 
     * @return the group id
     * @since 1.0
     */
    public String getGroupId() {
        return delegate.getGroupId();
    }
    
    /**
     * Sets the group id
     * 
     * @param groupId 
     * @since 1.0
     */
    public void setGroupId(String groupId) {
        delegate.setGroupId(groupId);
    }
    
    /**
     * Returns the version.
     * 
     * @return the version
     * @since 1.0
     */
    public String getVersion() {
        return delegate.getVersion();
    }
    
    /**
     * Sets the version.
     * 
     * @param version 
     * @since 1.0
     */
    public void setVersion(String version) {
        delegate.setVersion(version);
    }
    
    /**
     * Returns the name.
     * 
     * @return the name
     * @since 1.0
     */
    public @NonNull String getName() {
        return delegate.getName();
    }
    
    /**
     * Sets the name.
     * 
     * @param name 
     * @since 1.0
     */
    public void setName(String name) {
        delegate.setName(name);
    }
    
    /**
     * Returns the description. Is an optional property.
     * 
     * @return the description or <code>null</code> if none available
     * @since 1.0
     */
    public String getDescription() {
        return delegate.getDescription();
    }
    
    /**
     * Sets the description.
     * 
     * @param description 
     * @since 1.0
     */
    public void setDescription(String description) {
        delegate.setDescription(description);
    }
    
    /**
     * Sets the repository. Is an optional property.
     *
     * @param repository
     * @since 1.0
     */
    public void setRepository(String repository) {
        delegate.setRepository(repository);
    }
    
    /**
     * Returns the repository. Is an optional property.
     *
     * @return the repository or <code>null</code> if none available
     * @since 1.0
     */
    public String getRepository() {
        return delegate.getRepository();
    }
    
    @Override
    public int hashCode() {
        return getGroupId().trim().hashCode() + 13 * getArtifactId().trim().hashCode() + 23 * getVersion().trim().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Archetype)) {
            return false;
        }
        Archetype ar1 = (Archetype)obj;
        boolean gr = ar1.getGroupId().trim().equals(getGroupId().trim());
        if (!gr) {
            return false;
        }
        boolean ar = ar1.getArtifactId().trim().equals(getArtifactId().trim());
        if (!ar) {
            return false;
        }
        boolean ver =  ar1.getVersion().trim().equals(getVersion().trim());
        return ver;
    }

    @Override 
    public String toString() {
        return delegate.toString();
    }

}
