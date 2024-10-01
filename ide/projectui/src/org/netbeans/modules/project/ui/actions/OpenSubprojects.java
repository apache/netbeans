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
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.ui.OpenProjectList;
import static org.netbeans.modules.project.ui.actions.Bundle.*;
import org.netbeans.spi.project.ProjectContainerProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;

/** Action opening openAllProjectsItem "subprojects" of given project
 */
@ActionID(category="Project", id="org.netbeans.modules.project.ui.actions.OpenSubprojects")
@ActionRegistration(displayName="#LBL_OpenSubprojectsAction_Name", lazy=/* SubprojectProvider check */false)
@Messages("LBL_OpenSubprojectsAction_Name=Open Required Projects")
public class OpenSubprojects extends NodeAction implements Presenter.Popup{
    
    private static final RequestProcessor RP = new RequestProcessor("OpenSubprojects", 1);
    
    @StaticResource private static final String ICON = "org/netbeans/modules/project/ui/resources/openProject.png";

    @Override public String getName() {
        return LBL_OpenSubprojectsAction_Name();
    }
    
    @Override
    public String iconResource() {
        return ICON;
    }
    
    @Override public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous() {
        return true;
    }
    
    @Override protected boolean enable(Node[] activatedNodes) {
        
        if ( activatedNodes == null || activatedNodes.length == 0 ) {
            return false; // No nodes no closing
        }
        
        // Find out whether openAllProjectsItem nodes have project in lookup 
        boolean someSubprojects = false; // And have some subprojects;
        for( int i = 0; i < activatedNodes.length; i++ ) {
            Project p = activatedNodes[i].getLookup().lookup(Project.class);
            if ( p == null ) {
                return false;
            }
            else {
                
                SubprojectProvider spp = p.getLookup().lookup(SubprojectProvider.class);
                
                if ( spp != null //#97534 be satisfied with presence of the provider in lookup - && !spp.getSubprojects().isEmpty() 
                   ) {
                    someSubprojects = true;
                }                
            }
        }
        
        return someSubprojects;
    }
    
    @Override protected void performAction(Node[] activatedNodes) {
        openAllRequiredProjects(activatedNodes);
    }
    
    @Messages({
        "OpenProjectMenu.Open_All_Projects=&Open All Projects",
        "OpenProjectMenu.Nothing=Nothing"
    })
    @Override public JMenuItem getPopupPresenter() {        
        Node [] activatedNodes = getActivatedNodes();
        return new LazyMenu(activatedNodes);
    }
    
    private void openAllRequiredProjects(Node [] activatedNodes) {
        if(activatedNodes != null) {
            for( int i = 0; i < activatedNodes.length; i++ ) {
                Project p = activatedNodes[i].getLookup().lookup(Project.class);
                if ( p != null ) {
                    OpenProjectList.getDefault().open(new Project[] {p}, true, true);
                }
            }
        }
    }
    
    /*private void fillRecursiveSubProjects(Project p, Set<Project> subProjects) {
        SubprojectProvider spp = p.getLookup().lookup(SubprojectProvider.class);
        if(spp.getSubprojects() == null 
                || (spp.getSubprojects() != null && spp.getSubprojects().isEmpty())) {
            return;
        } 
        subProjects.addAll(spp.getSubprojects());
        for(Project prjIter:spp.getSubprojects()) {
            fillRecursiveSubProjects(prjIter, subProjects);
        }
    }*/
    
    private class LazyMenu extends JMenu {
        private final Node[] activatedNodes;
        private boolean menuCreated; // create only once, prevents recreating items when user repeatedly expends and collapses the menu
        private volatile Set<? extends Project> subProjects;

        @NbBundle.Messages("LBL_SubProjectPopupMenu_Initializing=Initializing...")
        private LazyMenu(Node[] nodes) {
            super(LBL_OpenSubprojectsAction_Name());
            this.activatedNodes = nodes;
            JMenuItem item = new JMenuItem(Bundle.LBL_SubProjectPopupMenu_Initializing());
            item.setEnabled(false);
            add(item);
            RP.post(this::asyncFindSubProjects);
        }

        @Override
        public JPopupMenu getPopupMenu() {
            if (!menuCreated && subProjects != null) {
                createSubMenu();
            }
            return super.getPopupMenu(); 
        }
        
        private void createSubMenu() {
            removeAll();
            if (!subProjects.isEmpty()) {
                super.getPopupMenu().setLayout(new VerticalGridLayout());
                final JMenuItem openAllProjectsItem = new JMenuItem(new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        openAllRequiredProjects(getActivatedNodes());
                    }
                });
                Mnemonics.setLocalizedText(openAllProjectsItem, OpenProjectMenu_Open_All_Projects());
                add(openAllProjectsItem);
                addSeparator();
            } else {
                JMenuItem nothingItem = new JMenuItem(OpenProjectMenu_Nothing());
                nothingItem.setEnabled(false);
                add(nothingItem);
            }
            if (!subProjects.isEmpty()) {
                for(final Project prjIter:subProjects) {
                    JMenuItem selectPrjAction = new JMenuItem(new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            OpenProjectList.getDefault().open(new Project[] {prjIter}, true, true);
                        }
                    });
                    selectPrjAction.setText(ProjectUtils.getInformation(prjIter).getDisplayName());
                    add(selectPrjAction);
                }
            }
            menuCreated = true;
        }

        private void asyncFindSubProjects() {
            try {
                if(activatedNodes != null) {
                    for( int i = 0; i < activatedNodes.length; i++ ) {
                        Project p = activatedNodes[i].getLookup().lookup(Project.class);
                        if ( p != null ) {
                            ProjectContainerProvider pcp = p.getLookup().lookup(ProjectContainerProvider.class);
                            if (pcp != null) {
                                subProjects = pcp.getContainedProjects().getProjects();
                            } else {
                                SubprojectProvider spp = p.getLookup().lookup(SubprojectProvider.class);
                                if(spp != null) {
                                    subProjects = spp.getSubprojects();
                                }
                            }
                        }
                    }
                }
            } finally {
                if (subProjects == null) {
                    subProjects = Set.of();
                } else {
                    // trigger project name query tasks
                    // without this, the first call to getDisplayName() will likely return a project path fallback
                    for (Project project : subProjects) {
                        ProjectUtils.getInformation(project).getDisplayName();
                    }
                }
            }
        }
    }
    
}
