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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.ui.customizer.MakeContext;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public class PropertiesItemAction extends NodeAction {

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        Project golden = (Project) activatedNodes[0].getValue("Project");// NOI18N
        if (golden == null) {
            return false;
        }
        for (int i = 1; i < activatedNodes.length; i++) {
            if (!golden.equals((Project) activatedNodes[i].getValue("Project"))) {// NOI18N
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_PropertiesItemActionName"); // NOI18N
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        List<Item> list = new ArrayList<>();
        MakeCustomizerProviderImpl best = null;
        for (int i = 0; i < activatedNodes.length; i++) {
            Node n = activatedNodes[i];
            Folder folder = (Folder) n.getValue("Folder"); // NOI18N
            Item item = (Item) n.getValue("Item"); // NOI18N
            Project project = (Project) n.getValue("Project"); // NOI18N
            if (project == null) {
                continue; // FIXUP
            }
            MakeCustomizerProviderImpl cp = project.getLookup().lookup(MakeCustomizerProviderImpl.class);
            if (cp == null) {
                continue; // FIXUP
            }
            if (best == null) {
                best = cp;
            }
            list.add(item);
            //dumpNativeFileInfo(item);
        }
        if (best != null) {
            best.showCustomizer(best.getLastCurrentNodeName(MakeContext.Kind.Item), list, null);
        }
    }

    private void dumpNativeFileInfo(Item item) {
        System.out.println("---------------------------------------------------------- " + item.getPath()); // NOI18N
        dumpPathsList("SystemIncludePaths", item.getSystemIncludePaths()); // NOI18N
        dumpPathsList("UserIncludePaths", item.getUserIncludePaths()); // NOI18N
        dumpList("SystemMacroDefinitions", item.getSystemMacroDefinitions()); // NOI18N
        dumpList("UserMacroDefinitions", item.getUserMacroDefinitions()); // NOI18N
    }

    public void dumpList(String txt, List<String> list) {
        list.forEach((s) -> {
            System.out.println(txt + ":" + s); // NOI18N
        });
    }

    private void dumpPathsList(String txt, List<IncludePath> list) {
        list.forEach((s) -> {
            System.out.println(txt + ":" + s.getFSPath().getURL()); // NOI18N
        });
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
