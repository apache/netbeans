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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.ProjectTab;
import org.netbeans.modules.project.ui.ProjectUtilities;
import org.netbeans.modules.project.ui.ProjectsRootNode;
import static org.netbeans.modules.project.ui.actions.Bundle.*;
import org.netbeans.modules.project.ui.api.UnloadedProjectInformation;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

@ActionID(id = "org.netbeans.modules.project.ui.RecentProjects", category = "Project")
@ActionRegistration(lazy = false, displayName = "#LBL_RecentProjectsAction_Name")
@ActionReferences({
    @ActionReference(path = ProjectsRootNode.ACTIONS_FOLDER, position = 500),
    @ActionReference(path = "Menu/File", position = 600)
})
@Messages("LBL_RecentProjectsAction_Name=Open Recent Project")
public class RecentProjects extends AbstractAction implements Presenter.Menu, Presenter.Popup, PropertyChangeListener {
    
    /** Key for remembering project in JMenuItem
     */
    private static final String PROJECT_URL_KEY = "org.netbeans.modules.project.ui.RecentProjectItem.Project_URL"; // NOI18N
    private final ProjectDirListener prjDirListener = new ProjectDirListener(); 

    private UpdatingMenu subMenu;
    
    private boolean recreate;
    private static final RequestProcessor RP = new RequestProcessor(RecentProjects.class);
    
    public RecentProjects() {
        super(LBL_RecentProjectsAction_Name());
        OpenProjectList.getDefault().addPropertyChangeListener( WeakListeners.propertyChange( this, OpenProjectList.getDefault() ) );
        recreate = true;
    }
    
        
    @Override public boolean isEnabled() {
        return !OpenProjectList.getDefault().isRecentProjectsEmpty();
    }
    
    /** Perform the action. Tries the performer and then scans the ActionMap
     * of selected topcomponent.
     */
    @Override public void actionPerformed(ActionEvent ev) {
        // no operation
    }
    
    @Override public JMenuItem getMenuPresenter() {
        createMainSubMenu();
        return subMenu;
    }
    
    @Override public JMenuItem getPopupPresenter() {
        JMenu menu = createSubMenu();
        fillSubMenu(menu);
        return menu;
    }
    
    @Messages("MNE_RecentProjectsAction_Name=j")
    private UpdatingMenu createSubMenu() {
        UpdatingMenu menu = new UpdatingMenu(this);
        //ok to have mnenomics here, not shown on mac anyway
        menu.setMnemonic(MNE_RecentProjectsAction_Name().charAt(0));
        return menu;
    }
    
    private void createMainSubMenu() {
        if ( subMenu == null ) {
            subMenu = createSubMenu();
            // model listening is the only lazy menu procedure that works on macosx
            subMenu.getModel().addChangeListener(subMenu);

        }
    }
        
    private void fillSubMenu(final JMenu menu) {
        menu.removeAll();

        List<UnloadedProjectInformation> projects = OpenProjectList.getDefault().getRecentProjectsInformation();
        if ( projects.isEmpty() ) {
            menu.setEnabled( false );
            return;
        }

        menu.setEnabled( true );
        ActionListener jmiActionListener = new MenuItemActionListener();
                        
        // Fill menu with items
        final List<URL> urls = new ArrayList<URL>();
        for (final UnloadedProjectInformation p : projects) {
                URL prjDirURL = p.getURL();
                urls.add(prjDirURL);
                JMenuItem jmi = new JMenuItem(p.getDisplayName(), p.getIcon()) {
                    public @Override void menuSelectionChanged(boolean isIncluded) {
                        super.menuSelectionChanged(isIncluded);
                        if (isIncluded) {
                            final FileObject prjDir = URLMapper.findFileObject(p.getURL());
                            if ( prjDir == null || !prjDir.isValid()) {
                                return;
                            }
                            StatusDisplayer.getDefault().setStatusText(FileUtil.getFileDisplayName(prjDir));
                        }
                    }
                };
                menu.add(jmi);
                jmi.putClientProperty( PROJECT_URL_KEY, prjDirURL );
                jmi.addActionListener( jmiActionListener );
        }
        RP.post(new Runnable() {

            @Override
            public void run() {
                for (URL u : urls) {
                    final FileObject prjDir = URLMapper.findFileObject(u);
                    if (prjDir == null || !prjDir.isValid()) {
                        continue;
                    }
                    prjDir.removeFileChangeListener(prjDirListener);
                    prjDir.addFileChangeListener(prjDirListener);
                }
            }
        });
        
    }

    // Implementation of change listener ---------------------------------------
    
    
    @Override public void propertyChange(PropertyChangeEvent e) {
        
        if ( OpenProjectList.PROPERTY_RECENT_PROJECTS.equals( e.getPropertyName() ) ) {
            final boolean en = !OpenProjectList.getDefault().isRecentProjectsEmpty();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    createMainSubMenu();
                    subMenu.setEnabled( en );
                    recreate = true;
                }
            });
        }
        
    }
    
    
    
    // Innerclasses ------------------------------------------------------------
    
    private static class MenuItemActionListener implements ActionListener {

        private static final RequestProcessor RP = new RequestProcessor(MenuItemActionListener.class); // #205652
        
        @Messages({
            "# {0} - URL to project directory", "STATUS_loading_recent=Loading project at {0}...",
            "ERR_InvalidProject=The project is either not valid or deleted"
        })
        @Override public void actionPerformed(ActionEvent e) {
            
            if ( e.getSource() instanceof JMenuItem ) {
                JMenuItem jmi = (JMenuItem)e.getSource();
                final URL url = (URL)jmi.getClientProperty( PROJECT_URL_KEY );
                StatusDisplayer.getDefault().setStatusText(STATUS_loading_recent(url));
                RP.post(new Runnable() {
                    @Override public void run() {
                Project project = null;

                FileObject dir = URLMapper.findFileObject( url );
                if ( dir != null && dir.isFolder() ) {
                    try {
                        project = ProjectManager.getDefault().findProject( dir );
                    }       
                    catch ( IOException ioEx ) {
                        // Ignore invalid folders
                    }
                }
                
                if ( project != null ) {
                    OpenProjectList.getDefault().open( new Project[] {project}, false, true );
                    final String name = ProjectUtils.getInformation(project).getName();
                    EventQueue.invokeLater(new Runnable() {
                        @Override public void run() {
                    ProjectTab ptLogical = ProjectTab.findDefault(ProjectTab.ID_LOGICAL);
                    Node root = ptLogical.getExplorerManager ().getRootContext ();
                    Node projNode = root.getChildren().findChild(name);
                    if (projNode != null) {
                        try {
                            ptLogical.getExplorerManager().setSelectedNodes(new Node[] {projNode});
                        } catch (PropertyVetoException ignore) {
                            // may ignore it
                        }
                    } else {
                        Logger.getLogger(RecentProjects.class.getName()).log(Level.WARNING, "Could not find {0} among {1}",
                                new Object[] {name, Arrays.asList(root.getChildren().getNodes())});
                    }
                    ProjectUtilities.makeProjectTabVisible();
                        }
                    });
                } else {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ERR_InvalidProject()));
                }
                    }
                });
            }
            
        }
        
    }
    
    private class ProjectDirListener extends FileChangeAdapter {
        @Override public void fileDeleted(FileEvent fe) {
            recreate = true;
        }
    }
    
    private class UpdatingMenu extends JMenu implements /*DynamicMenuContent,*/ ChangeListener {
        
        UpdatingMenu(Action action) {
            super(action);
        }
        
//        public JComponent[] synchMenuPresenters(JComponent[] items) {
//            return getMenuPresenters();
//        }
//        
//        public JComponent[] getMenuPresenters() {
//            return new JComponent[] { this };
//        }
        
        @Override public void stateChanged(ChangeEvent e) {
            if (recreate && getModel().isSelected()) {
                fillSubMenu(this);
                recreate = false;
            }
        }
        
    }
    
}
