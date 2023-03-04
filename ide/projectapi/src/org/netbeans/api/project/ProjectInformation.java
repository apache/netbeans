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

package org.netbeans.api.project;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import org.netbeans.spi.project.ProjectIconAnnotator;

/**
 * General information about a project.
 * <strong>Use {@link ProjectUtils#getInformation} as a client.</strong>
 * Use {@link Project#getLookup} as a provider.
 * @author Jesse Glick
 */
public interface ProjectInformation {

    /** Property name fired when the code name changes. */
    String PROP_NAME = "name"; // NOI18N

    /** Property name fired when the display name changes. */
    String PROP_DISPLAY_NAME = "displayName"; // NOI18N
    
    /** Property name fired when the icon changes. */
    String PROP_ICON = "icon"; // NOI18N
    
    /**
     * Get a programmatic code name suitable for use in build scripts or other
     * references.
     * <p>
     * Project names should typically be distinctive enough to distinguish
     * between different projects with some kind of relationships, <em>but</em>
     * any usage of this name must take into account that they are not forced
     * to be unique.
     * <p>
     * Should not contain odd characters; should be usable as a directory name
     * on disk, as (part of) an Ant property name, etc.
     * XXX precise format - at least conforms to XML NMTOKEN or ID
     * @return a code name
     * @see <a href="@org-netbeans-modules-project-ant@/org/netbeans/spi/project/support/ant/PropertyUtils.html#getUsablePropertyName(java.lang.String)"><code>PropertyUtils.getUsablePropertyName</code></a>
     */
    String getName();
    
    /**
     * Get a human-readable display name for the project.
     * May contain spaces, international characters, etc.
     * XXX precise format - probably XML PCDATA
     * @return a display name for the project
     */
    String getDisplayName();
    
    /** 
     * Gets icon for given project.
     * Usually determined by the project type.
     * @return icon of the project.
     * @see ProjectIconAnnotator
     */
    Icon getIcon();
    
    /**
     * Get the associated project.
     * @return the project for which information is being provided
     */
    Project getProject();
    
    /**
     * Add a listener to property changes.
     * Only {@link #PROP_NAME}, {@link #PROP_DISPLAY_NAME}, and {@link #PROP_ICON} may be fired.
     * Since the event source is the info object, you may use {@link #getProject}.
     * @param listener a listener to add
     */
    void addPropertyChangeListener(PropertyChangeListener listener);
    
    /**
     * Remove a listener to property changes.
     * @param listener a listener to remove
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
    
}
