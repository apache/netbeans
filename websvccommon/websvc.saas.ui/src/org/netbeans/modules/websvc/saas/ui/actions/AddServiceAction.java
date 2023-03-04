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
import org.netbeans.modules.websvc.saas.ui.wizards.AddWebServiceDlg;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;
import org.openide.util.*;

/**
 * Add web service action.
 * 
 * @author  nam
 */
public class AddServiceAction extends NodeAction {
    
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
        return NbBundle.getMessage(AddServiceAction.class, "ADD_WEB_SERVICE_Action"); // NOI18N
    }
    
    @Override
    protected void performAction(Node[] nodes) {
        SaasGroup g = nodes[0].getLookup().lookup(SaasGroup.class);
        new AddWebServiceDlg(g).displayDialog();
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/websvc/manager/resources/webservice.png"; // NOI18N
    }
    
}
