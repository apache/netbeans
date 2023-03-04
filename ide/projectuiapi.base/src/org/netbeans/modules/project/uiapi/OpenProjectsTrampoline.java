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

package org.netbeans.modules.project.uiapi;

import java.beans.PropertyChangeListener;
import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.ProjectGroup;
import org.netbeans.api.project.ui.ProjectGroupChangeListener;
import org.openide.explorer.ExplorerManager;

/**
 * List of projects open in the GUI.
 * @author Petr Hrebejk
 */
public interface OpenProjectsTrampoline {

    public Project[] getOpenProjectsAPI();

    public void openAPI (Project[] projects, boolean openRequiredProjects, boolean showProgress);

    public void closeAPI (Project[] projects);

    public void addPropertyChangeListenerAPI( PropertyChangeListener listener, Object source );

    public Future<Project[]> openProjectsAPI();

    public void removePropertyChangeListenerAPI( PropertyChangeListener listener );
    
    public Project getMainProject();
    
    public void setMainProject(Project project);
    
    public ProjectGroup getActiveProjectGroupAPI();

    public void addProjectGroupChangeListenerAPI(ProjectGroupChangeListener listener);

    public void removeProjectGroupChangeListenerAPI(ProjectGroupChangeListener listener);
 
    public ExplorerManager createLogicalView();
    public ExplorerManager createPhysicalView();
}
