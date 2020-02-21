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

import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.spi.project.ActionProvider;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

public class RunTestAction extends NodeAction {

    @Override
    public String getName() {
        return getString("TestActionName"); // NOI18N
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
//
//        List<Folder> list = folder.getAllTests();
//        if (folder.isTest()) {
//            list.add(folder);
//        }
//        if (list.size() > 0) {
//            StringBuffer message = new StringBuffer("Would run the following test(s):\n\n"); // NOI18N
//            for (Folder f : list) {
//                message.append("  ").append(f.getDisplayName()).append("\n"); // NOI18N
//            }
//            message.append("\nTest(s) would build and run with output directed to output window. Two posibilities (will have to decided):\n"); // NOI18N
//            message.append("1): output is parsed similary to how build output is parsed and failed tests are hyperlinked for easy navigation back to the failed test.\n"); // NOI18N
//            message.append("2): full-featured GUI frontend (similar to JUnit GUI frontend) with support for test progress, summary, and hyperlinks back to failed tests.\n"); // NOI18N
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
            ap.invokeAction(ActionProvider.COMMAND_TEST_SINGLE, new AbstractLookup(ic));
        }
    }

    @Override
    public boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        Node n = activatedNodes[0];
        Folder folder = (Folder) n.getValue("Folder"); // NOI18N
        if (folder == null) {
            return false;
        }
        if (folder.isTest() || folder.isTestRootFolder()) {
            return true;
        }
        List<Folder> list = folder.getAllTests();
        return list.size() > 0;
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
