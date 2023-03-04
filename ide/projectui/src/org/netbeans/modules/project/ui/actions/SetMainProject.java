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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.ProjectsRootNode;
import static org.netbeans.modules.project.ui.actions.Bundle.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

@ActionID(id = "org.netbeans.modules.project.ui.SetMainProject", category = "Project")
@ActionRegistration(lazy = false, displayName = "#LBL_SetAsMainProjectAction_Name")
@ActionReferences({
    @ActionReference(path = "Menu/BuildProject", position = 310),
    @ActionReference(path = ProjectsRootNode.ACTIONS_FOLDER, position = 1400)
})
@Messages("LBL_SetAsMainProjectAction_Name=Set as Main Project")
public class SetMainProject extends ProjectAction implements PropertyChangeListener {

    /** Key for remembering project in JMenuItem
     */
    private static final String PROJECT_KEY = "org.netbeans.modules.project.ui.MainProjectItem"; // NOI18N

    /** #210148: whether the context menu should be displayed at the moment, even if there is no main project set. */
    private static final String CONTEXT_MENU_ITEM_ENABLED = "setMainProjectContextEnabled";

    private static Preferences prefs() {
        return NbPreferences.forModule(SetMainProject.class);
    }
    private static RequestProcessor RP = new RequestProcessor(SetMainProject.class);
    
    protected JMenu subMenu;
    private boolean empty;
    
    // private PropertyChangeListener wpcl;
    
    public SetMainProject() {
        this( null );
    }
    
    @SuppressWarnings("LeakingThisInConstructor")
    public SetMainProject( Lookup context ) {
        super( SetMainProject.class.getName() /*this is a fake command to make ActionUtils.SHORTCUTS_MANAGER work*/, LBL_SetAsMainProjectAction_Name(), null, context );
        // wpcl = WeakListeners.propertyChange( this, OpenProjectList.getDefault() );
        // OpenProjectList.getDefault().addPropertyChangeListener( wpcl );
        if ( context == null ) { 
            OpenProjectList.getDefault().addPropertyChangeListener( WeakListeners.propertyChange( this, OpenProjectList.getDefault() ) );
        }
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        refresh(getLookup(), true);
    }
    
    @Override protected void actionPerformed(Lookup context) {
            final Pair<List<Project>, List<FileObject>> data = ActionsUtil.mineFromLookup(context);
            RP.post(new Runnable() {
                @Override
                public void run() {
                    final Project[] projects = ActionsUtil.getProjects(data);
                    if (projects.length == 1) {
                        if (projects[0] == OpenProjectList.getDefault().getMainProject()) {
                            OpenProjectList.getDefault().setMainProject(null);
                        } else {
                            OpenProjectList.getDefault().setMainProject(projects[0]);
                        }
                    }        
                }
            });            
    }

    @Messages("LBL_UnSetAsMainProjectAction_Name=Unset as Main Project")
    @Override public final void refresh(Lookup context, boolean immediate) {
        
        super.refresh(context, immediate);
        
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );
        if ( projects.length != 1 /* Some projects have to be open !OpenProjectList.getDefault().isOpen( projects[0] ) */ ) {
            setEnabled( false );
        }
        else {
            Project main = OpenProjectList.getDefault().getMainProject();
            setEnabled(prefs().getBoolean(CONTEXT_MENU_ITEM_ENABLED, main != null));
            if (projects[0] == main) {
                putValue("popupText", LBL_UnSetAsMainProjectAction_Name());
            } else {
                putValue("popupText", null);
            }
        }
        empty = projects.length == 0;
    }
    
    @Override public Action createContextAwareInstance(Lookup actionContext) {
        return new SetMainProject( actionContext );
    }
    
    @Override public JMenuItem getMenuPresenter() {
        createSubMenu(OpenProjectList.getDefault().getOpenProjects());
        return subMenu;
    }
    
    @Override public JMenuItem getPopupPresenter() {
        //#220595 hack in a hack.. merging of multiple instance of @ActionReference into one class leads to confusing code.
        if (empty) {
            // Hack!
            subMenu = null;
            return getMenuPresenter();
        } else {
            return super.getPopupPresenter();
        }
    }    
        
    @Messages({
        "LBL_SetMainProjectAction_Name=Set Main Project",
        "MNE_SetMainProjectAction_Name=M",
        "LBL_NoneMainProject_Name=&None"
    })
    private void createSubMenu(Project[] projects) {    
        Arrays.sort(projects, OpenProjectList.projectByDisplayName());
        
        // Enable disable the action according to number of open projects
        if (projects.length == 0) {
            setEnabled( false );
        }
        else {
            setEnabled( true );
        }
        
        List<Project> newlyAdded = new ArrayList<Project>(Arrays.asList(projects));
        if ( subMenu == null ) {
            subMenu = new JMenu(LBL_SetMainProjectAction_Name());
            subMenu.getPopupMenu().setLayout(new VerticalGridLayout());
            //ok to have mnenomics here, not shown on mac anyway
            subMenu.setMnemonic(MNE_SetMainProjectAction_Name().charAt(0));
            //#70835: the menu bar holds only subMenu not this action. As this action listens only weakly on OPL, noone holds this action.
            //The action is the garbage collected and the sub menu does not react on changes of opened projects.
            //The action instance has to exists as long as the subMenu:
            subMenu.putClientProperty(SetMainProject.class, this);
        } else {
            List<Project> projectList = new ArrayList<Project>(newlyAdded);
            for(Component componentIter : subMenu.getMenuComponents()) {
                if(componentIter instanceof JRadioButtonMenuItem) {
                    Project p = getItemProject((JRadioButtonMenuItem) componentIter);
                    if(p != null) {
                        newlyAdded.remove(p);
                        if(!projectList.contains(p)) {
                            final ProjectInformation projectInformation = p.getLookup().lookup(ProjectInformation.class);
                            if(projectInformation != null) {
                                final SetMainProject self = this;
                                RP.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        projectInformation.removePropertyChangeListener(self);
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }
        
        subMenu.removeAll();
        final ActionListener jmiActionListener = new MenuItemActionListener(); 
        
        JRadioButtonMenuItem jmiNone = new JRadioButtonMenuItem((javax.swing.Icon) null, false);
        Mnemonics.setLocalizedText(jmiNone, LBL_NoneMainProject_Name());
        jmiNone.addActionListener(jmiActionListener);
        subMenu.add(jmiNone);
        subMenu.add(new Separator());
        
        // Fill menu with items
        for ( int i = 0; i < projects.length; i++ ) {
            final ProjectInformation projectInformation = projects[i].getLookup().lookup(ProjectInformation.class);
            if(projectInformation != null && newlyAdded.contains(projects[i])) {
                final SetMainProject self = this;
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        projectInformation.addPropertyChangeListener(self);
                    }
                });
            }
            final ProjectInformation pi = ProjectUtils.getInformation(projects[i]);
            JRadioButtonMenuItem jmi = new JRadioButtonMenuItem(pi.getDisplayName(), pi.getIcon(), false);
            subMenu.add( jmi );
            setItemProject(jmi, projects[i]);
            jmi.addActionListener( jmiActionListener );
        }

        // Set main project
        selectMainProject();
        
        subMenu.setEnabled( projects.length > 0 );
        
    }
    
    private void selectMainProject() {

        boolean prjSelected = false;
        JRadioButtonMenuItem noneItem = null;
        for( int i = 0; i < subMenu.getItemCount(); i++ ) {
            JMenuItem jmi = subMenu.getItem( i );
            if (jmi != null) {
                Project project = getItemProject(jmi);
                if (project == null) {
                    noneItem = (JRadioButtonMenuItem) jmi;
                }
                if ( jmi instanceof JRadioButtonMenuItem ) {
                    if ( OpenProjectList.getDefault().isMainProject( project ) ) {
                        ((JRadioButtonMenuItem)jmi).setSelected( true );
                        prjSelected = true;
                    }
                    else {
                        ((JRadioButtonMenuItem)jmi).setSelected( false );
                    }
                }
            }
        }
        if (!prjSelected && noneItem != null) {
            noneItem.setSelected(true);
        }
        
    }
    
    private void checkProjectNames() {
        for(Component componentIter : subMenu.getMenuComponents()) {
            if(componentIter instanceof JRadioButtonMenuItem) {
                JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem) componentIter;
                Project projectIter = getItemProject(menuItem);
                if(projectIter != null && !ProjectUtils.getInformation(projectIter).getDisplayName().equals(menuItem.getText())) {
                    menuItem.setText(ProjectUtils.getInformation(projectIter).getDisplayName());
                }
            }
        }
    }
    
    // Implementation of change listener ---------------------------------------
    
    
    @Override public void propertyChange(PropertyChangeEvent e) {
        
        if ( OpenProjectList.PROPERTY_OPEN_PROJECTS.equals( e.getPropertyName() )) {
            final Project projects[] = OpenProjectList.getDefault().getOpenProjects();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                   createSubMenu(projects);
                }
            });
            
        } else if ( ProjectInformation.PROP_DISPLAY_NAME.equals( e.getPropertyName() )) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (subMenu != null) {
                        checkProjectNames();
                    }
                }
            });
            
        } else if ( OpenProjectList.PROPERTY_MAIN_PROJECT.equals( e.getPropertyName() )) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (subMenu != null) {
                        selectMainProject();
                    }
                }
            });            
            
        }
        
        
    }
    
    /**
     * Get project weakly-referenced from an item.
     *
     * @param menuItem Menu item.
     *
     * @return The project, or null if it is not set or has been
     * garbage-collected.
     */
    private static Project getItemProject(JMenuItem menuItem) {
        Reference<Project> p = (Reference<Project>) menuItem.getClientProperty(PROJECT_KEY);
        if (p == null) {
            return null;
        } else {
            return p.get();
        }
    }

    /**
     * Set weakly-referenced project as item's client property.
     *
     * @param menuItem Menu item.
     * @param project The project.
     */
    private static void setItemProject(JMenuItem menuItem, Project project) {
        menuItem.putClientProperty(PROJECT_KEY, new WeakReference<Project>(project));
    }

    // Innerclasses ------------------------------------------------------------
    
    private static class MenuItemActionListener implements ActionListener {
        
        @Override public void actionPerformed(ActionEvent e) {
            
            if ( e.getSource() instanceof JMenuItem ) {
                JMenuItem jmi = (JMenuItem)e.getSource();
                final Project project = getItemProject(jmi);
                prefs().putBoolean(CONTEXT_MENU_ITEM_ENABLED, project != null);
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        OpenProjectList.getDefault().setMainProject(project);
                    }
                });   
            }            
        }   
    }       
}
