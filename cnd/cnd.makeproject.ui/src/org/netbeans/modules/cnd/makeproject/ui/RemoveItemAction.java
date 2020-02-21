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

package org.netbeans.modules.cnd.makeproject.ui;

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public class RemoveItemAction extends NodeAction {

    @Override
    protected boolean enable(Node[] activatedNodes)  {
	return true;
    }

    @Override
    public String getName() {
	return NbBundle.getBundle(getClass()).getString("CTL_RemoveItemActionName"); // NOI18N
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            Node n = activatedNodes[i];
            Project project = (Project)n.getValue("Project"); // NOI18N
            Folder folder = (Folder)n.getValue("Folder"); // NOI18N
            Item item = (Item)n.getValue("Item"); // NOI18N

                ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class );
                MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
                if (!makeConfigurationDescriptor.okToChange()) {
                    return;
                }

            folder.removeItemAction(item);
            makeConfigurationDescriptor.save();
            if (CndPathUtilities.isPathAbsolute(item.getPath())) {
                //((MakeSources)ProjectUtils.getSources(project)).descriptorChanged();
            }
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
	return null;
    }

    @Override
    protected boolean asynchronous() {
	return false;
    }
}
