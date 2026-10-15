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

package org.netbeans.modules.j2ee.earproject.ui.actions;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.nodes.NodeOperation;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.openide.util.actions.CookieAction;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.j2ee.earproject.util.EarProjectUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.ContextAwareAction;

/**
 * Action that allows selection and assembly of J2EE module projects.
 * @author Chris Webster
 * @author vince kraemer
 */
public class AddModuleAction extends CookieAction {
    private static final long serialVersionUID = 1L;
    
    private static final String FOLDER_ICON = "org/netbeans/modules/j2ee/earproject/ui/resources/folder.gif";
    
    private static final Class[] COOKIE_ARRAY =
        new Class[] { Project.class };
    
    @Override
    public Class[] cookieClasses() {
        return COOKIE_ARRAY.clone();
    }
    
    @Override
    public int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }
    
    @Override
    public void performAction(Node[] activeNodes) {
        try {
            Project p = activeNodes[0].getLookup().lookup(Project.class);
            Project[] moduleProjects = getSelectedProjects(p.getProjectDirectory());
            // XXX Vince add code here to add to application.xml and
            // build script
            EarProject ep = p.getLookup().lookup(EarProject.class);
            EarProjectProperties.addJ2eeSubprojects(ep, moduleProjects);
        } catch (UserCancelException uce) {
            // this action has been cancelled
        }
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(AddModuleAction.class, "LBL_AddModuleAction");
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    private Project[] getSelectedProjects(FileObject projDir) throws UserCancelException {
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        List<Node> moduleProjectNodes = new LinkedList<Node>();
        for (int i = 0; i < allProjects.length; i++) {
            if (EarProjectUtil.isJavaEEModule(allProjects[i])) {
                LogicalViewProvider lvp = allProjects[i].getLookup().lookup(LogicalViewProvider.class);
                Node mn = lvp.createLogicalView();
                Node n = new FilterNode(mn, new FilterNode.Children(mn), Lookups.singleton(allProjects[i]));
                moduleProjectNodes.add(n);
            }
        }
        Children.Array children = new Children.Array();
        children.add(moduleProjectNodes.toArray(new Node[0]));
        final AbstractNode root = new AbstractNode(children);
        String moduleSelector = NbBundle.getMessage(AddModuleAction.class, "LBL_ModuleSelectorTitle");
        
        Project parent = FileOwnerQuery.getOwner(projDir);
        SubprojectProvider spp = parent.getLookup().lookup(SubprojectProvider.class);
        if (null != spp) {
            final Set s = spp.getSubprojects();
            NodeAcceptor na = new NodeAcceptor() {
                public boolean acceptNodes(Node[] nodes) {
                    for (int i = 0; i < nodes.length; i++) {
                        if (nodes[i].getParentNode() != root) {
                            return false;
                        }
                        // do not put this test befor the root test...
                        Project p = nodes[i].getLookup().lookup(Project.class);
                        if (null == p) {
                            return false;
                        }
                        if (s.contains(p)) {
                            return false;
                        }
                    }
                    return nodes.length > 0;
                }
            };
            root.setDisplayName(NbBundle.getMessage(AddModuleAction.class, "LBL_J2EEModules"));
            root.setIconBaseWithExtension(FOLDER_ICON);
            Node[] selected = NodeOperation.getDefault().select(moduleSelector, root.getDisplayName(), root, na);
            Project[] modules = new Project[selected.length];
            for (int i = 0; i < modules.length; i++) {
                modules[i] = selected[i].getLookup().lookup(Project.class);
            }
            return modules;
      }
        else {
            return new Project[0];
        }
    }
}
