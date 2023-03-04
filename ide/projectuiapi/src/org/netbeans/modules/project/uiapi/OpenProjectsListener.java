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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 *
 * @author S. Aubrecht
 */
class OpenProjectsListener implements PropertyChangeListener {
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if( OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName()) ) {
            // open/close navigator and task list windows when project is opened/closed
            openCloseWindowGroup();
        }
    }
    
    private void openCloseWindowGroup() {
        final Project[] projects = OpenProjects.getDefault().getOpenProjects();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                TopComponentGroup projectsGroup = WindowManager.getDefault().findTopComponentGroup("OpenedProjects"); //NOI18N
                if( null == projectsGroup )
                    Logger.getLogger(OpenProjectsListener.class.getName()).log( Level.FINE, "OpenedProjects TopComponent Group not found." );
                TopComponentGroup taskListGroup = WindowManager.getDefault().findTopComponentGroup("TaskList"); //NOI18N
                if( null == taskListGroup )
                    Logger.getLogger(OpenProjectsListener.class.getName()).log( Level.FINE, "TaskList TopComponent Group not found." );
                boolean show = projects.length > 0;
                if( show ) {
                    if( null != projectsGroup )
                        projectsGroup.open();
                    if( null != taskListGroup && supportsTaskList(projects) )
                        taskListGroup.open();
                } else {
                    if( null != projectsGroup )
                        projectsGroup.close();
                    if( null != taskListGroup )
                        taskListGroup.close();
                }
            }
        };
        if( SwingUtilities.isEventDispatchThread() )
            r.run();
        else
            SwingUtilities.invokeLater(r);
    }

    private boolean supportsTaskList(Project[] projects) {
        boolean res = false;
        for( Project p : projects ) {
            //#184291 - don't show task list for cnd MakeProjects, see also #185488
            if( !p.getClass().getName().equals("org.netbeans.modules.cnd.makeproject.MakeProject") ) { //NOI18N
                res = true;
                break;
            }
        }
        return res;
    }
}
