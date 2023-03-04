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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

public class ProjectTabAction extends AbstractAction {

    private static final String ICON1 = "org/netbeans/modules/project/ui/resources/projectTab.png"; //NOI18N
    private static final String ICON2 = "org/netbeans/modules/project/ui/resources/filesTab.png"; //NOI18N
    
    private int type;
    
    @ActionID(id = "org.netbeans.modules.project.ui.physical.tab.action", category = "Project")
    @ActionRegistration(displayName = "#LBL_ProjectsPhysicalTabAction_Name", iconBase = "org/netbeans/modules/project/ui/resources/filesTab.png")
    @ActionReferences(value = {
        @ActionReference(path = "Shortcuts", name = "D-2"),
        @ActionReference(path = "Menu/Window", position = 200)})
    @Messages("LBL_ProjectsPhysicalTabAction_Name=&Files")
    public static Action projectsPhysical() {
        return new ProjectTabAction(Bundle.LBL_ProjectsPhysicalTabAction_Name(), ICON2, 0);
    }
    
    @ActionID(id = "org.netbeans.modules.project.ui.logical.tab.action", category = "Project")
    @ActionRegistration(displayName = "#LBL_ProjectsLogicalTabAction_Name", iconBase = "org/netbeans/modules/project/ui/resources/projectTab.png")
    @ActionReferences(value = {
        @ActionReference(path = "Shortcuts", name = "D-1"),
        @ActionReference(path = "Menu/Window", position = 100)})
    @Messages("LBL_ProjectsLogicalTabAction_Name=Pro&jects")
    public static Action projectsLogical() {
        return new ProjectTabAction(Bundle.LBL_ProjectsLogicalTabAction_Name(), ICON1, 1);
    }
    
    /** Creates a new instance of BrowserAction */
    public ProjectTabAction( String name, String iconResource, int type ) {
        super( name );
        putValue("iconBase", iconResource); // NOI18N
        this.type = type;
    }
    
    @Override public void actionPerformed(ActionEvent e) {
        TopComponent tc = ProjectTab.findDefault( type == 1 ? ProjectTab.ID_LOGICAL : ProjectTab.ID_PHYSICAL );
        tc.open();
        tc.requestActive();
    }
    
}
