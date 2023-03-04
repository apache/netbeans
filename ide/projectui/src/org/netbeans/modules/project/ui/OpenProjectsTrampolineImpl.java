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

package org.netbeans.modules.project.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.ui.ProjectGroup;
import org.netbeans.api.project.ui.ProjectGroupChangeListener;
import org.netbeans.modules.project.ui.groups.Group;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;
import org.openide.explorer.ExplorerManager;

/**
 * List of projects open in the GUI.
 * @author Petr Hrebejk
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.project.uiapi.OpenProjectsTrampoline.class)
public final class OpenProjectsTrampolineImpl implements OpenProjectsTrampoline, PropertyChangeListener  {

    /** Property change listeners registered through API */
    private PropertyChangeSupport pchSupport;
    
    private boolean listenersRegistered;
    
    public OpenProjectsTrampolineImpl() {
    }
    
    @Override
    public Project[] getOpenProjectsAPI() {
        return OpenProjectList.getDefault().getOpenProjects();
    }

    @Override
    public void openAPI (Project[] projects, boolean openRequiredProjects, boolean showProgress) {
        OpenProjectList.getDefault().open (projects, openRequiredProjects, showProgress);
    }

    @Override
    public void closeAPI(Project[] projects) {
        OpenProjectList.getDefault().close(projects, false);
    }

    @Override
    public void addPropertyChangeListenerAPI( PropertyChangeListener listener, Object source ) {
        boolean shouldRegisterListener;
        
        synchronized (this) {
            if (shouldRegisterListener = !listenersRegistered) {
                listenersRegistered = true;
                pchSupport = new PropertyChangeSupport( source );
            }
        }
        
        if (shouldRegisterListener) {
            //make sure we are listening on OpenProjectList so the events are be propagated.
            //see issue #65928:
            OpenProjectList.getDefault().addPropertyChangeListener( this );
        }
        assert pchSupport != null;
        
        pchSupport.addPropertyChangeListener( listener );        
    }
    
    @Override
    public void removePropertyChangeListenerAPI( PropertyChangeListener listener ) {
        if (pchSupport != null) {
            pchSupport.removePropertyChangeListener( listener );        
        }
    }
    
    @Override
    public void propertyChange( PropertyChangeEvent e ) {
        
        if ( e.getPropertyName().equals( OpenProjectList.PROPERTY_OPEN_PROJECTS ) ) {        
            pchSupport.firePropertyChange( OpenProjects.PROPERTY_OPEN_PROJECTS, e.getOldValue(), e.getNewValue() );
        }
        if ( e.getPropertyName().equals( OpenProjectList.PROPERTY_WILL_OPEN_PROJECTS ) ) {        
            pchSupport.firePropertyChange( OpenProjectList.PROPERTY_WILL_OPEN_PROJECTS, e.getOldValue(), e.getNewValue() );
        }
        if ( e.getPropertyName().equals( OpenProjectList.PROPERTY_MAIN_PROJECT ) ) {        
            pchSupport.firePropertyChange( OpenProjects.PROPERTY_MAIN_PROJECT, e.getOldValue(), e.getNewValue() );
        }
    }
        
    @Override
    public Project getMainProject() {
        return OpenProjectList.getDefault().getMainProject();
    }
    
    @Override
    public void setMainProject(Project project) {
        OpenProjectList.getDefault().setMainProject(project);
    }
    
    @Override
    public Future<Project[]> openProjectsAPI() {
        return OpenProjectList.getDefault().openProjectsAPI();
}
    
    @Override
    public void addProjectGroupChangeListenerAPI(ProjectGroupChangeListener listener) {
        OpenProjectList.getDefault().addProjectGroupChangeListener(listener);
}

    @Override
    public void removeProjectGroupChangeListenerAPI(ProjectGroupChangeListener listener) {
        OpenProjectList.getDefault().removeProjectGroupChangeListener(listener);
    }

    @Override
    public ProjectGroup getActiveProjectGroupAPI() {
        Group gr = Group.getActiveGroup();
        if (gr != null) {
            return org.netbeans.modules.project.uiapi.BaseUtilities.ACCESSOR.createGroup(gr.getName(), gr.prefs());
        }
        return null;
    }

    @Override
    public ExplorerManager createLogicalView() {
        ExplorerManager em = new ExplorerManager();
        ProjectsRootNode root = new ProjectsRootNode(ProjectsRootNode.LOGICAL_VIEW);
        em.setRootContext(root);
        return em;
    }

    @Override
    public ExplorerManager createPhysicalView() {
        ExplorerManager em = new ExplorerManager();
        ProjectsRootNode root = new ProjectsRootNode(ProjectsRootNode.PHYSICAL_VIEW);
        em.setRootContext(root);
        return em;
    }
}
