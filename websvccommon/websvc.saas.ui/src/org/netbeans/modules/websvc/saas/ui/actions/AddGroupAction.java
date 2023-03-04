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
import org.openide.util.actions.NodeAction;
import org.openide.util.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;

/** 
 * Action that creates a web services group.
 */
public class AddGroupAction extends NodeAction {
    
    @Override
    protected boolean enable(org.openide.nodes.Node[] nodes) {
        if (nodes != null && nodes.length == 1) {
            SaasGroup g = nodes[0].getLookup().lookup(SaasGroup.class);
            return g != null;
        }
        return false;
    }
    
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(AddGroupAction.class, "ADD_GROUP"); // NOI18N
    }
    
    @Override
    protected void performAction(Node[] nodes) {
        String defaultName = NbBundle.getMessage(AddGroupAction.class, "NEW_GROUP");  // NOI18N
        NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(AddGroupAction.class, "CTL_GroupLabel"), // NOI18N
                NbBundle.getMessage(AddGroupAction.class, "CTL_GroupTitle")); // NOI18N
        dlg.setInputText(defaultName);
        
        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dlg))) {
            try {
                String newName = dlg.getInputText().trim();
                if (newName == null || newName.length() == 0) {
                    newName = defaultName;
                }
                SaasGroup parent = nodes[0].getLookup().lookup(SaasGroup.class);
                
                try {
                    SaasServicesModel.getInstance().createGroup(parent, newName);   
                } catch (Exception ex) {
                    NotifyDescriptor.Message msg = new NotifyDescriptor.Message(ex.getMessage());
                    DialogDisplayer.getDefault().notify(msg);
                }
            } catch (IllegalArgumentException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }
    
    /** @return <code>false</code> to be performed in event dispatch thread */
    @Override
    protected boolean asynchronous() {
        return false;
    }
}
