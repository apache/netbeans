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

package org.netbeans.modules.project.ui.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.loaders.DataFolder;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import static org.netbeans.modules.project.ui.actions.Bundle.*;

/**
 * Action to open the project(s) corresponding to selected folder(s).
 * Useful for opening projects encountered in the Favorites tab, as well as nested
 * projects found beneath open projects in the Files tab.
 * @see "#54122"
 * @author Jesse Glick
 */
@ActionID(id = "org.netbeans.modules.project.ui.actions.OpenProjectFolderAction", category = "Project")
@ActionRegistration(displayName = "#OpenProjectFolderAction.LBL_action", lazy=false)
@ActionReference(path = "Loaders/folder/any/Actions", position = 100)
@Messages("OpenProjectFolderAction.LBL_action=Open Project of Folder")
public final class OpenProjectFolderAction extends AbstractAction implements ContextAwareAction {

    private static final RequestProcessor RP = new RequestProcessor(OpenProjectFolderAction.class);
    
    public OpenProjectFolderAction() {
        super(OpenProjectFolderAction_LBL_action());
    }
    
    public @Override void actionPerformed(ActionEvent e) {
        // Cannot be invoked without any context.
        assert false;
    }
    
    public @Override Action createContextAwareInstance(Lookup context) {
        return new ContextAction(context);
    }
    
    private static final class ContextAction extends AbstractAction {
        
        private final Lookup context;
        
        public ContextAction(Lookup context) {
            super(OpenProjectFolderAction_LBL_action());
            this.context = context;
            boolean foundProject = false;
            for (DataFolder d : context.lookupAll(DataFolder.class)) {
                if (ProjectManager.getDefault().isProject(d.getPrimaryFile())) {
                    foundProject = true;
                    break;
                }
            }
            if (!foundProject) {
                putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
                setEnabled(false);
            }
            // #199137: do not try to adjust label, etc. according to actual projects
            // 1. such computation cannot be done in EQ without sometimes blocking
            // 2. even if done asynch, looks bad to update label after popup is posted
        }
        
        public @Override void actionPerformed(ActionEvent e) {
            // Run asynch so that UI is not blocked; might show progress dialog (?).
            RP.post(new Runnable() {
                public @Override void run() {
                    Set<Project> projects = new HashSet<Project>();
                    // Collect projects corresponding to selected folders.
                    for (DataFolder d : context.lookupAll(DataFolder.class)) {
                        try {
                            Project p = ProjectManager.getDefault().findProject(d.getPrimaryFile());
                            if (p != null) {
                                projects.add(p);
                            }
                            // Ignore folders not corresponding to projects (will not disable action if some correspond to projects).
                            // Similarly, do not worry about projects which are already open - no harm done.
                        } catch (IOException x) {
                            Logger.getLogger(OpenProjectFolderAction.class.getName()).log(Level.INFO, null, x);
                        }
                    }
                    OpenProjectList.getDefault().open(projects.toArray(new Project[0]), false, true);
                }
            });
        }
        
    }
    
}
