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

package org.netbeans.modules.gsf.testrunner.ui;

import org.netbeans.modules.gsf.testrunner.ui.api.TestCreatorPanelDisplayer;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.gsf.testrunner.ui.api.UICommonUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * 
 * @author  theofanis
 */
@ActionID(id = "org.netbeans.modules.gsf.testrunner.ui.TestCreatorAction", category = "UnitTests")
@ActionRegistration(displayName = "#LBL_CreateCommonTestAction")
@ActionReferences(value = {
    @ActionReference(path = "UI/ToolActions")})
@NbBundle.Messages({"LBL_CreateCommonTestAction=Create/Update Tests"})
public class TestCreatorAction extends NodeAction {
    
    private static final Logger LOGGER = Logger.getLogger(TestCreatorAction.class.getName());
    
    /** Creates a new instance of TestCreatorAction */
    public TestCreatorAction() {
        putValue("noIconInMenu", Boolean.TRUE);                         //NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(TestCreatorAction.class, "LBL_CreateCommonTestAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    public boolean asynchronous() {
        return false;
    }
    
    @NbBundle.Messages({"MSG_no_FOs_from_Nodes=No File Object found for the selected Nodes."})
    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        if (activatedNodes.length == 1) {
            FileObject fo = UICommonUtils.getFileObjectFromNode(activatedNodes[0]);
            if(fo == null) {
                return false;
            }
            Project p = FileOwnerQuery.getOwner(fo);
            if(p == null) {
                return false;
            }
            // do not display the action when user selects a node under project's node
            // that is identical to project's root (e.g. web.clientproject's Sources node)
            if(p.getProjectDirectory().equals(fo) && !ProjectUtils.getInformation(p).getDisplayName().equals(activatedNodes[0].getDisplayName())) {
                return false;
            }
        }
        FileObject[] activatedFOs = UICommonUtils.getFileObjectsFromNodes(activatedNodes);
        if(activatedFOs == null) {
            LOGGER.log(Level.FINE, "{0}", Bundle.MSG_no_FOs_from_Nodes());
            return false;
        }
        Collection<? extends Lookup.Item<TestCreatorProvider>> providers = Lookup.getDefault().lookupResult(TestCreatorProvider.class).allItems();
        boolean enable;
        for (Lookup.Item<TestCreatorProvider> provider : providers) {
            enable = provider.getInstance().enable(activatedFOs);
            if(enable) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
	TestCreatorPanelDisplayer.getDefault().displayPanel(UICommonUtils.getFileObjectsFromNodes(activatedNodes), null, null);
    }
    
}
