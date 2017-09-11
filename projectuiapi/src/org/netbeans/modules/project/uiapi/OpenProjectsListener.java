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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
