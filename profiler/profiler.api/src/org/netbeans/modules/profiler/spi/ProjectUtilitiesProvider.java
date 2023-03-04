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
package org.netbeans.modules.profiler.spi;

import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup.Provider;

/**
 *
 * @author Tomas Hurka
 */
public abstract class ProjectUtilitiesProvider {

     /** 
     * Gets icon for given project.
     * Usually determined by the project type.
     * @param project project
     * @return icon of the project.
     */
   public abstract Icon getIcon(Provider project);
    
    
    /**Retrieves the current main project set in the IDE.
     *
     * @return the current main project or null if none
     */
    public abstract Provider getMainProject();
    
    /**
     * Get a human-readable display name for the project.
     * May contain spaces, international characters, etc.
     * @param project project
     * @return a display name for the project
     */
    public abstract String getDisplayName(Provider project);

    /**
     * Gets an associated directory where the project metadata and possibly sources live.
     * In the case of a typical Ant project, this is the top directory, not the
     * project metadata subdirectory.
     * @return a directory
     */
    public abstract FileObject getProjectDirectory(Provider project);

    /**
     * Gets a list of currently open projects.
     * 
     * @return list of projects currently opened in the IDE's GUI; order not specified
     */
    public abstract Provider[] getOpenedProjects();
    
    /**
     * Returns true if the provided project has sub-projects.
     * 
     * @param project a project
     * @return true if the provided project has sub-projects, false otherwise
     */
    public abstract boolean hasSubprojects(Provider project);

    /**
     * Computes set of sub-projects of a project
     * @param project a project
     * @param subprojects map of sub-projects
     */
    public abstract void fetchSubprojects(Provider project, Set<Provider> subprojects);

    /**
     * Find the project, if any, which "owns" the given file.
     * @param fobj the file (generally on disk)
     * @return a project which contains it, or null if there is no known project containing it
     */
    public abstract Provider getProject(FileObject fobj);
    
    /**
     * Adds a listener to be notified when set of open projects changes.
     * @param listener listener to be added
     */
    public abstract void addOpenProjectsListener(ChangeListener listener);
    
    /**
     * Removes a listener to be notified when set of open projects changes.
     * @param listener listener to be removed
     */
    public abstract void removeOpenProjectsListener(ChangeListener listener);
}
