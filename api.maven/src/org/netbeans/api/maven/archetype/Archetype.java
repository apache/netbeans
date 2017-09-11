/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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

    private org.netbeans.modules.maven.api.archetype.Archetype delegate;

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
