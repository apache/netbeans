/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.profiler.api;

import java.util.Arrays;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.profiler.spi.ProjectUtilitiesProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;

/**
 * ProjectUtilities provides profiler with necessary functionality work accessing
 * project oriented data.
 * 
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 */
public final class ProjectUtilities {
    
    /**Retrieves the current main project set in the IDE.
     *
     * @return the current main project or null if none
     */
    public static Provider getMainProject() {
        return provider().getMainProject();
    }

    /**
     * Gets a list of currently open projects.
     * 
     * @return list of projects currently opened in the IDE's GUI; order not specified
     */
    public static Provider[] getOpenedProjects() {
        return provider().getOpenedProjects();
    }
    
    /**
     * Get a human-readable display name for the project.
     * May contain spaces, international characters, etc.
     * @param project project
     * @return a display name for the project
     */
    public static String getDisplayName(Lookup.Provider project) {
        return provider().getDisplayName(project);
    }

    /**
     * Gets an associated directory where the project metadata and possibly sources live.
     * In the case of a typical Ant project, this is the top directory, not the
     * project metadata subdirectory.
     * @return a directory
     */
    public static FileObject getProjectDirectory(Lookup.Provider project) {
        return provider().getProjectDirectory(project);
    }

    /** 
     * Gets icon for given project.
     * Usually determined by the project type.
     * @param project project
     * @return icon of the project.
     */
    public static Icon getIcon(Provider project) {
        return provider().getIcon(project);
    }
    
    /**
     * Returns true if the provided project has sub-projects.
     * 
     * @param project a project
     * @return true if the provided project has sub-projects, false otherwise
     */
    public static boolean hasSubprojects(Provider project) {
        return provider().hasSubprojects(project);
    }

    /**
     * Computes set of sub-projects of a project
     * @param project a project
     * @param subprojects map of sub-projects
     */
    public static void fetchSubprojects(Provider project, Set<Provider> subprojects) {
        provider().fetchSubprojects(project, subprojects);
    }
    
    /**
     * Find the project, if any, which "owns" the given file.
     * @param fobj the file (generally on disk)
     * @return a project which contains it, or null if there is no known project containing it
     */
    public static Provider getProject(FileObject fobj) {
        return provider().getProject(fobj);
    }
    /**
     * Adds a listener to be notified when set of open projects changes.
     * @param listener listener to be added
     */
    public static void addOpenProjectsListener(ChangeListener listener) {
        provider().addOpenProjectsListener(listener);
    }
    
    /**
     * Removes a listener to be notified when set of open projects changes.
     * @param listener listener to be removed
     */
    public static void removeOpenProjectsListener(ChangeListener listener) {
        provider().removeOpenProjectsListener(listener);
    }

    /**
     * Sorts projects by display name
     * @param projects
     * @return arrays of projects sorted by display name
     */
    public static Provider[] getSortedProjects(Provider[] projects) {
        Provider[] sorted = Arrays.copyOf(projects, projects.length);
        Arrays.sort(sorted, (p1, p2) -> getDisplayName(p1).compareToIgnoreCase(getDisplayName(p2)));
        return sorted;
    }

    private static ProjectUtilitiesProvider provider() {
        return Lookup.getDefault().lookup(ProjectUtilitiesProvider.class);
    }
    
}
