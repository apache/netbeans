/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makeproject.ui.actions;

import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.openide.awt.DynamicMenuContent;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 *
 */

public class MoreBuildActionsAction extends MakeProjectContextAwareAction implements Presenter.Menu, Presenter.Popup {
    private JMenu subMenu = null;
    private Project project;

    @Override
    protected void performAction(Node[] activatedNodes) {
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        //get
        project = getProject(activatedNodes);
        if (project == null) {
            return false;
        }        
        ConfigurationDescriptorProvider pdp  = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp == null) {
            return false;
        }
        subMenu = null;
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return null;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        createSubMenu();
        return subMenu;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        createSubMenu();
        return subMenu;
    }
    
    private ArrayList<Action> getActions() {
        if (project == null) {
            return null;
        }
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp == null) {
            return null;
        }
        MakeConfigurationDescriptor descriptor = pdp.getConfigurationDescriptor();
        MakeConfiguration active = (descriptor == null) ? null : descriptor.getActiveConfiguration();
        ArrayList<Action> actionsList = new ArrayList<>();
        boolean isDiskFolder = descriptor == null || active == null || active.isMakefileConfiguration();
        actionsList.addAll(Utilities.actionsForPath("Projects/org-netbeans-modules-cnd-makeproject/MoreBuildActions"));//NOI18N
        actionsList.addAll(Utilities.actionsForPath("CND/Actions/MoreBuildCommands/" + (isDiskFolder ? "DiskFolder" : "LogicalFolder")));//NOI18N               
        return actionsList;
    }

    private void createSubMenu() {
        if (subMenu == null) {
            String label = NbBundle.getMessage(MoreBuildActionsAction.class, "LBL_MoreBuildActionsAction_Name"); // NOI18N
            subMenu = new JMenu(label);            
            ArrayList<Action> actions = getActions();
            if (actions == null) {
                return;
            }
            for (Action action : actions) {
                //do now show if disabled and property Hide when Disable is set to TRUE
                if (Boolean.TRUE.equals(action.getValue(DynamicMenuContent.HIDE_WHEN_DISABLED)) && !action.isEnabled()) {
                    continue;
                }
                subMenu.add(action);
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(MoreBuildActionsAction.class, "LBL_MoreBuildActionsAction_Name");//NOI18N
    }
}
