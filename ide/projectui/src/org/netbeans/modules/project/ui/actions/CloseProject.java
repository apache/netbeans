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

package org.netbeans.modules.project.ui.actions;

import java.awt.EventQueue;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** Action for removing project from the open projects tab
 */
public class CloseProject extends ProjectAction {
    
    private static final String namePattern = NbBundle.getMessage( CloseProject.class, "LBL_CloseProjectAction_Name" ); // NOI18N
    private static final String namePatternPopup = NbBundle.getMessage( CloseProject.class, "LBL_CloseProjectAction_Popup_Name" ); // NOI18N
    
    
    /** Creates a new instance of CloseProject */
    public CloseProject() {
        this( null );        
    }
    
    public CloseProject( Lookup context ) {
        super( (String)null, namePattern, namePatternPopup, null, context );        
        refresh(getLookup(), true);
    }
        
    @Override
    protected void actionPerformed( final Lookup context ) {
        if (EventQueue.isDispatchThread()) {
            OpenProjectList.OPENING_RP.post(new Runnable() {
                @Override
                public void run() {
                    actionPerformed(context);
                }
            });
            return;
        }
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );        
        // show all modified documents, if an user cancel it then no project is closed        
        OpenProjectList.getDefault().close( projects, true );
    }
    
    @Override
    public void refresh(Lookup context, boolean immediate) {
        
        super.refresh(context, immediate);
        
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );
        // XXX make it work better for mutliple open projects
        if ( projects.length == 0 || !OpenProjectList.getDefault().isOpen( projects[0] ) ) {
            enable( false );
        }
        else {
            enable( true );
        }        
    }
    
    private void enable(final boolean enable) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setEnabled(enable);
                }
            });
        } else {
            setEnabled(enable);
        }
    }
    
    @Override
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new CloseProject( actionContext );
    }
    
    
}
