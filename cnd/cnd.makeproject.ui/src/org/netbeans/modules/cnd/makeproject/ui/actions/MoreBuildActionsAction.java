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
