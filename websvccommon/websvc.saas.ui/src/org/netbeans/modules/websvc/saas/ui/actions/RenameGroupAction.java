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

package org.netbeans.modules.websvc.saas.ui.actions;

import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.RenameAction;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Action that renames a web service group.
 */
public class RenameGroupAction extends RenameAction {
    
    @Override
    protected void performAction(Node[] nodes) {
        if (nodes != null && nodes.length == 1) {
            SaasGroup group = nodes[0].getLookup().lookup(SaasGroup.class);
            if (group == null) {
                return;
            }

            Node n = nodes[0];
            NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(
                    NbBundle.getMessage(RenameAction.class, "CTL_RenameLabel"), // NOI18N
                    NbBundle.getMessage(RenameAction.class, "CTL_RenameTitle")); // NOI18N
            dlg.setInputText(n.getName());
            if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dlg))) {
                String name = dlg.getInputText().trim();
                if (group.getParent().getChildGroup(name) != null) {
                    String msg = NbBundle.getMessage(RenameGroupAction.class, "MSG_DuplicateGroupName"); // NOI18N
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                    return;
                }
                SaasServicesModel.getInstance().renameGroup(group, name);
                n.setName(name);
            }
        }
    }
}
