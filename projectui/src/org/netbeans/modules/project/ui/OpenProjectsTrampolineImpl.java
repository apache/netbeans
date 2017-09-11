/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    
}
