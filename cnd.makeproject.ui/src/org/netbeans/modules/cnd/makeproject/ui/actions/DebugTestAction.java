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

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.spi.project.ActionProvider;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

public class DebugTestAction extends NodeAction {

    @Override
    public String getName() {
        return getString("DebugTestActionName"); // NOI18N
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return;
        }
        Node n = activatedNodes[0];
        Project project = (Project) n.getValue("Project"); // NOI18N
        assert project != null;
//        Folder folder = (Folder) n.getValue("Folder"); // NOI18N
//        assert folder != null;
//        Node thisNode = (Node) n.getValue("This"); // NOI18N
//        assert thisNode != null;

//        List<Folder> list = folder.getAllTests();
//        if (folder.isTest()) {
//            list.add(folder);
//        }
//        if (list.size() > 0) {
//            StringBuffer message = new StringBuffer("Would debug the following test:\n\n"); // NOI18N
//            for (Folder f : list) {
//                message.append("  ").append(f.getDisplayName()).append("\n"); // NOI18N
//            }
//            message.append("\nTest would build and debug session would start debugging the test binary."); // NOI18N
//            NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE);
//            DialogDisplayer.getDefault().notify(nd);
//        }

        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        if (ap != null) {
            InstanceContent ic = new InstanceContent();
            ic.add(project);
            Folder targetFolder = (Folder) n.getValue("Folder"); // NOI18N
            if (targetFolder != null) {
                ic.add(targetFolder);
            }
            DataObject d = n.getLookup().lookup(DataObject.class);
            if (d != null) {
                ic.add(d.getPrimaryFile());
            }
            ap.invokeAction(MakeActionProvider.COMMAND_DEBUG_TEST, new AbstractLookup(ic));
        }
    }

    @Override
    public boolean enable(Node[] activatedNodes) {
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
