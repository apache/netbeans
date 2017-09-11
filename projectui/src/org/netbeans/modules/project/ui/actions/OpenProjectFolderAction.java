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
                    OpenProjectList.getDefault().open(projects.toArray(new Project[projects.size()]), false, true);
                }
            });
        }
        
    }
    
}
