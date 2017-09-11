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
        boolean initialized; // create only once, prevents recreating items when user repeatedly expends and collapses the menu
        private Set<? extends Project> subProjects;

        @NbBundle.Messages("LBL_SubProjectPopupMenu_Initializing=Initializing...")
        private LazyMenu(Node[] nodes) {
            super(LBL_OpenSubprojectsAction_Name());
            this.activatedNodes = nodes;
            JMenuItem item = new JMenuItem(Bundle.LBL_SubProjectPopupMenu_Initializing());
            item.setEnabled(false);
            add(item);
            RP.post(new Runnable() {
                @Override
                public void run() {
                    getSubProjects();
                }
            });
        }

        @Override
        public JPopupMenu getPopupMenu() {
            if(initialized) {
                createSubMenu();
            }
            return super.getPopupMenu(); 
        }
        
        private void createSubMenu() {
            removeAll();
            if(subProjects != null && !subProjects.isEmpty()) {
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
            if(subProjects != null && !subProjects.isEmpty()) {
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
        }

        private void getSubProjects() {
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
                initialized = true;
            }
        }
    }
    
}
