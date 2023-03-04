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

import javax.swing.Action;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.project.ui.ProjectTab;
import static org.netbeans.modules.project.ui.actions.Bundle.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

public class SelectNodeAction extends LookupSensitiveAction implements ContextAwareAction {
    
    @StaticResource private static final String SELECT_IN_PROJECTS_ICON = "org/netbeans/modules/project/ui/resources/projectTab.png";
    @StaticResource private static final String SELECT_IN_FILES_ICON = "org/netbeans/modules/project/ui/resources/filesTab.png";
    
    private final String findIn;
    
    @ActionID(id = "org.netbeans.modules.project.ui.SelectInProjects", category = "Window/SelectDocumentNode")
    @ActionRegistration(displayName = "#LBL_SelectInProjectsAction_MainMenuName", lazy=false)
    @ActionReferences({
        @ActionReference(path = "Shortcuts", name = "DS-1"),
        @ActionReference(path = "Menu/GoTo", position = 2600, separatorBefore = 2500),
        @ActionReference(path = "Editors/TabActions", position = 100)
    })
    @Messages("LBL_SelectInProjectsAction_MainMenuName=Select in Pro&jects")
    public static SelectNodeAction inProjects() {
        return new SelectNodeAction(SELECT_IN_PROJECTS_ICON, LBL_SelectInProjectsAction_MainMenuName(), ProjectTab.ID_LOGICAL, null);
    }
    
    @ActionID(id = "org.netbeans.modules.project.ui.SelectInFiles", category = "Window/SelectDocumentNode")
    @ActionRegistration(displayName = "#LBL_SelectInFilesAction_MainMenuName", lazy=false)
    @ActionReferences({
        @ActionReference(path = "Shortcuts", name = "DS-2"),
        @ActionReference(path = "Menu/GoTo", position = 2700)
    })
    @Messages("LBL_SelectInFilesAction_MainMenuName=Sele&ct in Files")
    public static SelectNodeAction inFiles() {
        return new SelectNodeAction(SELECT_IN_FILES_ICON, LBL_SelectInFilesAction_MainMenuName(), ProjectTab.ID_PHYSICAL, null);
    }
    
    private SelectNodeAction(String icon, String name, String findIn, Lookup lookup) {
        super(null, lookup, new Class<?>[] {DataObject.class, FileObject.class});
        putValue("iconBase", icon);
        this.findIn = findIn;
        this.setDisplayName( name );
    }
       
    protected @Override void actionPerformed( Lookup context ) {
        FileObject fo = getFileFromLookup( context );
        if ( fo != null ) {
            ProjectTab pt  = ProjectTab.findDefault( findIn );      
            pt.selectNodeAsync( fo );
        }
    }
    
    protected @Override void refresh(Lookup context, boolean immediate) {
        FileObject fo = getFileFromLookup( context );
        setEnabled( fo != null );        
    }
    
    private FileObject getFileFromLookup( Lookup context ) {
   
        FileObject fo = context.lookup(FileObject.class);     
        if (fo != null) {
            return fo;
        }

        DataObject dobj = context.lookup(DataObject.class);
        
        return dobj == null ? null : dobj.getPrimaryFile();
    }

    @Override public Action createContextAwareInstance(Lookup context) {
        return new SelectNodeAction((String) getValue("iconBase"), (String) getValue(NAME), findIn, context);
    }
    
}
