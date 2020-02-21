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

package org.netbeans.modules.cnd.makeproject.api.ui.actions;

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public class NewFolderAction extends NodeAction {
    
    
    public NewFolderAction() {
         //TODO: uncomment when problem iwth MakeProjectLogicalViewRootNode folder will be fixed, now "Folder" can be null when it should not be null
        //putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
    }
    
    @Override
    public String getName() {
	return NbBundle.getBundle(getClass()).getString("CTL_NewFolderAction"); // NOI18N
    }

    @Override
    public void performAction(Node[] activatedNodes) {
	Node n = activatedNodes[0];
	Folder folder = (Folder)n.getValue("Folder"); // NOI18N
	assert folder != null;
	Node thisNode = (Node)n.getValue("This"); // NOI18N
	assert thisNode != null;
	Project project = (Project)n.getValue("Project"); // NOI18N
	assert project != null;
        
        ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class );
        MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        if (!makeConfigurationDescriptor.okToChange()) {
            return;
        }

        NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(getString("FolderNameTxt"), getString("NewFolderName"));
        dlg.setInputText(folder.suggestedNewFolderName());
        String newname = null;

        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dlg))) {
            newname = dlg.getInputText();
        }
        else {
            return;
        }
        
	Folder newFolder = folder.addNewFolder(true);
        newFolder.setDisplayName(newname);
        makeConfigurationDescriptor.save();
	MakeLogicalViewProvider.setVisible(project, newFolder); 
    }

    @Override
    public boolean enable(Node[] activatedNodes) {
	if (activatedNodes.length != 1) {
	    return false;
        }
        Object project = activatedNodes[0].getValue("Project"); // NOI18N
        if (project == null || (!(project instanceof Project))) {
            return false;
        }
        
	Folder folder = (Folder)activatedNodes[0].getValue("Folder"); // NOI18N
	if (folder == null) {
	    return false;
        }
//	if (!folder.isProjectFiles())
//	    return false;
	return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
	return null;
    }

    @Override
    protected boolean asynchronous() {
	return false;
    }

    private String getString(String s) {
        return NbBundle.getBundle(getClass()).getString(s);
    }
}
