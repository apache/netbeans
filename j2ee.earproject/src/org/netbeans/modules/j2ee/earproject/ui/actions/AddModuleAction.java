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
        children.add(moduleProjectNodes.toArray(new Node[moduleProjectNodes.size()]));
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
