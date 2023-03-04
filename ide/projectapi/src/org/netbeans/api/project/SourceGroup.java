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
import org.openide.filesystems.FileObject;

// XXX should be PROP_* constants for rootFolder, name, displayName, icon

/**
 * Representation of one area of sources.
 * @author Jesse Glick
 * @see Sources
 */
public interface SourceGroup {

    /**
     * Pseudo-property used to indicate changes in containership of some subfiles.
     * (The old and new value should be left null.)
     */
    String PROP_CONTAINERSHIP = "containership"; // NOI18N

    /**
     * Get the folder forming the root of this group of sources.
     * @return the root folder (must be a folder, not a file)
     */
    FileObject getRootFolder();
    
    /**
     * Get a code name suitable for internal identification of this source group.
     * Should be unique among the source groups of a given type
     * contained in a single {@link Sources} object.
     * @return a code name
     */
    String getName();

    /**
     * Get a display name suitable for presentation to a user.
     * Should preferably be unique among the source groups of a given type
     * contained in a single {@link Sources} object.
     * @return a display name
     */
    String getDisplayName();

    /**
     * Get an icon for presentation to a user.
     * @param opened if true, may select an alternative "open" variant
     * @return an icon, or null if no specific icon is needed
     */
    Icon getIcon(boolean opened);

    /**
     * Check whether the given file is contained in this group.
     * <p>
     * A constraint is that the root folder must be contained and
     * if any file or folder (other than the root folder) is contained then
     * its parent must be as well. Therefore, while the return value is precise
     * for files, and a false return value means what it sounds like for folders,
     * a true return value for folders may mean that just parts of the folder are
     * contained in the group.
     * </p>
     * @param file a file or folder; must be a descendant of the root folder
     * @return true if the group contains that file; false if it is to be excluded, or is not inside the root
     */
    boolean contains(FileObject file);
    
    /**
     * Add a listener to changes in aspects of the source group.
     * The property names used may be normal JavaBean names
     * (<code>rootFolder</code>, <code>name</code>, <code>displayName</code>,
     * <code>icon</code>) or {@link #PROP_CONTAINERSHIP}.
     * @param listener a listener to add
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove a listener to changes in aspects of the source group.
     * @param listener a listener to remove
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

}
