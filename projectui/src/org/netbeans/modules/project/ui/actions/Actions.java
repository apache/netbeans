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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.modules.project.uiapi.ActionsFactory;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileActionPerformer;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.ServiceProvider;

/** Factory for all kinds of actions used in projectui and
 *projectuiapi.
 */
@ServiceProvider(service=ActionsFactory.class)
public class Actions implements ActionsFactory {
    
    //private static final Actions INSTANCE = new Actions();  
    
    public Actions() {}
    
    
    // Implementation of ActionFactory -----------------------------------------
    
    private static Action SET_AS_MAIN_PROJECT;
    private static Action CUSTOMIZE_PROJECT;
    private static Action CLOSE_PROJECT;
    private static Action NEW_FILE;
    private static Action COPY_PROJECT;
    private static Action MOVE_PROJECT;
    private static Action RENAME_PROJECT;

    @Override public synchronized Action setAsMainProjectAction() {
        if ( SET_AS_MAIN_PROJECT == null ) {
            SET_AS_MAIN_PROJECT = new SetMainProject();
        }
        return SET_AS_MAIN_PROJECT;
    }
    
    @Override public synchronized Action customizeProjectAction() {
        if ( CUSTOMIZE_PROJECT == null ) {
            CUSTOMIZE_PROJECT = new CustomizeProject();
        }
        return CUSTOMIZE_PROJECT;
    }
    
    @Override public synchronized Action openSubprojectsAction() {
        return SystemAction.get(OpenSubprojects.class);
    }
    
    @Override public Action closeProjectAction() {
        return closeProject();
    }

    public static synchronized Action closeProject() {
        if ( CLOSE_PROJECT == null ) {
            CLOSE_PROJECT = new CloseProject();
        }
        return CLOSE_PROJECT;        
    }
    
    @Override public Action newFileAction() {
        return newFile();
    }

    public static synchronized Action newFile() {
        if ( NEW_FILE == null ) {
            NEW_FILE = new NewFile.WithSubMenu();
        }
        return NEW_FILE;
    }
    
    @Override public Action deleteProjectAction() {
        return deleteProject();
    }

    @Override public Action copyProjectAction() {
        return copyProject();
    }
    
    @Override public Action moveProjectAction() {
        return moveProject();
    }
    
    @Override public Action renameProjectAction() {
        return renameProject();
    }
    
    @Override public synchronized Action newProjectAction() {
        return new NewProject();
    }
    
    @Override public ContextAwareAction projectCommandAction(String command, String namePattern, Icon icon ) {
        return new ProjectAction( command, namePattern, icon, null );
    }
    
    @Override public Action projectSensitiveAction( ProjectActionPerformer performer, String namePattern, Icon icon ) {
        return new ProjectAction( performer, namePattern, icon, null );
    }
    
    @Override public Action mainProjectCommandAction(String command, String name, Icon icon) {
        return new MainProjectAction( command, name, icon );
    }
    
    @Override public Action mainProjectSensitiveAction(ProjectActionPerformer performer, String name, Icon icon) {
        return new MainProjectAction( performer, name, icon );
    }

    
    @Override public Action fileCommandAction(String command, String name, Icon icon) {
        return new FileAction( command, name, icon, null );
    }

    @Override
    public Action fileSensitiveAction(FileActionPerformer performer, String name, Icon icon) {
        return new FileAction(performer, name, icon, null);
    }
    
    // Project specific actions ------------------------------------------------
    
    public static Action javadocProject() {
        return new ProjectAction (
            "javadoc", // XXX move to java.project and use JavaProjectConstants.COMMAND_JAVADOC
            NbBundle.getMessage(Actions.class, "LBL_JavadocProjectAction_Name"),
            NbBundle.getMessage(Actions.class, "LBL_JavadocProjectAction_Name_popup"),
            null, 
            null ); 
    }
    
    public static Action testProject() {        
        Action a = new ProjectAction (
            ActionProvider.COMMAND_TEST,
            NbBundle.getMessage(Actions.class, "LBL_TestProjectAction_Name"),
            NbBundle.getMessage(Actions.class, "LBL_TestProjectAction_Name_popup"),
            null,
            null ); 
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/testProject.png"); //NOI18N
        a.putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        return a;
    }
        
    
    public static Action buildProject() {
        Action a = new ProjectAction (
            ActionProvider.COMMAND_BUILD, 
            NbBundle.getMessage(Actions.class, "LBL_BuildProjectAction_Name"),
            NbBundle.getMessage(Actions.class, "LBL_BuildProjectAction_Name_popup"),
            null,
            null );  
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/buildCurrentProject.gif"); //NOI18N
        return a;
    }
    
    public static Action cleanProject() {
        Action a = new ProjectAction(
                ActionProvider.COMMAND_CLEAN,
                NbBundle.getMessage(Actions.class, "LBL_CleanProjectAction_Name"),
                NbBundle.getMessage(Actions.class, "LBL_CleanProjectAction_Name_popup"),
                null,
                null );
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/cleanCurrentProject.gif"); //NOI18N
        return a;
    }
    
    public static Action rebuildProject() {
        Action a = new ProjectAction(
            ActionProvider.COMMAND_REBUILD,
            NbBundle.getMessage(Actions.class, "LBL_RebuildProjectAction_Name"),
            NbBundle.getMessage(Actions.class, "LBL_RebuildProjectAction_Name_popup"),
            null,
            null ); 
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/rebuildCurrentProject.gif"); //NOI18N
        return a;
    }
        
    public static Action runProject() {
        Action a = new ProjectAction(
            ActionProvider.COMMAND_RUN, 
            NbBundle.getMessage(Actions.class, "LBL_RunProjectAction_Name"),
            NbBundle.getMessage(Actions.class, "LBL_RunProjectAction_Name_popup"),
            null,
            null ); 
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/runCurrentProject.gif"); //NOI18N
        return a;
    }
    
    public static synchronized Action deleteProject() {
        final Action a = new ProjectAction(
            ActionProvider.COMMAND_DELETE, 
            NbBundle.getMessage(Actions.class, "LBL_DeleteProjectAction_Name"),
            null,
            null );

        try {
            final Action delete = org.openide.awt.Actions.forID("Edit", "org.openide.actions.DeleteAction");
            a.putValue(Action.ACCELERATOR_KEY, delete.getValue(Action.ACCELERATOR_KEY));
            delete.addPropertyChangeListener(new PropertyChangeListener() {
                public @Override void propertyChange(PropertyChangeEvent evt) {
                    a.putValue(Action.ACCELERATOR_KEY, delete.getValue(Action.ACCELERATOR_KEY));
                }
            });
        } catch (Exception x) {
            Exceptions.printStackTrace(x);
        }
        
        return a;
    }
    
    public static synchronized Action copyProject() {
        if (COPY_PROJECT == null) {
            Action a = new ProjectAction(
                    ActionProvider.COMMAND_COPY,
		    NbBundle.getMessage(Actions.class, "LBL_CopyProjectAction_Name"),
                    null, //NOI18N
                    null );
            COPY_PROJECT = a;
        }
        
        return COPY_PROJECT;
    }
    
    public static synchronized Action moveProject() {
        if (MOVE_PROJECT == null) {
            Action a = new ProjectAction(
                    ActionProvider.COMMAND_MOVE,
		    NbBundle.getMessage(Actions.class, "LBL_MoveProjectAction_Name"),
                    null, //NOI18N
                    null );
            MOVE_PROJECT = a;
        }
        
        return MOVE_PROJECT;
    }
    
    public static synchronized Action renameProject() {
        if (RENAME_PROJECT == null) {
            Action a = new ProjectAction(
                    ActionProvider.COMMAND_RENAME,
		    NbBundle.getMessage(Actions.class, "LBL_RenameProjectAction_Name"),
                    null, //NOI18N
                    null );
            RENAME_PROJECT = a;
        }
        
        return RENAME_PROJECT;
    }
    
    // 1-off actions -----------------------------------------------------------
    
    public static Action compileSingle() {
        Action a = new FileAction(
            ActionProvider.COMMAND_COMPILE_SINGLE,
            NbBundle.getMessage(Actions.class, "LBL_CompileSingleAction_Name"),
            null,
            null);
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/compileSingle.png"); //NOI18N
        a.putValue("noIconInMenu", true); //NOI18N
        return a;
    }
    
    public static Action runSingle() {
        Action a = new FileAction(
            ActionProvider.COMMAND_RUN_SINGLE,
            NbBundle.getMessage(Actions.class, "LBL_RunSingleAction_Name"),
            null,
            null);
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/runSingle.png"); //NOI18N
        a.putValue("noIconInMenu", true); //NOI18N
        return a;
    }
    
    public static Action testSingle() {
        Action a = new FileAction(
            ActionProvider.COMMAND_TEST_SINGLE,
            NbBundle.getMessage(Actions.class, "LBL_TestSingleAction_Name"),
            null,
            null);
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/testSingle.png"); //NOI18N
        a.putValue("noIconInMenu", true); //NOI18N
        return a;
    }
    
    // Main Project actions ----------------------------------------------------


    public static Action buildMainProject() {
        Action a = new MainProjectAction (
            ActionProvider.COMMAND_BUILD, 
            NbBundle.getMessage(Actions.class, "LBL_BuildMainProjectAction_Name" ),null);  //NOI18N
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/buildProject.png"); //NOI18N
        return a;
    }
    
    public static Action cleanMainProject() {
        Action a = new MainProjectAction(
                ActionProvider.COMMAND_CLEAN,
                NbBundle.getMessage(Actions.class, "LBL_CleanMainProjectAction_Name" ),null);  //NOI18N
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/cleanProject.gif"); //NOI18N
        return a;
    }

    public static Action rebuildMainProject() {
        Action a = new MainProjectAction(
            ActionProvider.COMMAND_REBUILD,
            NbBundle.getMessage(Actions.class, "LBL_RebuildMainProjectAction_Name"),null); //NOI18N
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/rebuildProject.png"); //NOI18N
        return a;
    }
        
    public static Action runMainProject() {
        Action a = new MainProjectActionWithHistory(
            ActionProvider.COMMAND_RUN,
            NbBundle.getMessage(Actions.class, "LBL_RunMainProjectAction_Name"),null); //NOI18N
        a.putValue("iconBase","org/netbeans/modules/project/ui/resources/runProject.png"); //NOI18N
        return a;
    }
    
    @Override public Action setProjectConfigurationAction() {
        return SystemAction.get(ActiveConfigAction.class);
    }

}
