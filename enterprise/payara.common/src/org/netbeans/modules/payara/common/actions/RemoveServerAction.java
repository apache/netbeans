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

package org.netbeans.modules.payara.common.actions;

import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.payara.common.PayaraInstanceProvider;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Peter Williams
 */
public class RemoveServerAction extends NodeAction {

    @Override
    protected void performAction(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            ServerInstance si = getServerInstance(nodes[i]);
            if(si == null || !si.isRemovable()) {
                continue;
            }
            
            String title = NbBundle.getMessage(RemoveServerAction.class, 
                    "MSG_RemoveServerTitle", si.getDisplayName());
            String msg = NbBundle.getMessage(RemoveServerAction.class, 
                    "MSG_RemoveServerMessage", si.getDisplayName());
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, title, 
                    NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
                si.remove();
            }
        }
    }
    
    private ServerInstance getServerInstance(Node node) {
        // !PW FIXME should the server instance be in the node lookup?
        ServerInstance si = null;
        PayaraModule commonSupport = node.getLookup().lookup(PayaraModule.class);
        if(commonSupport != null) {
            String uri = commonSupport.getInstanceProperties().get(PayaraModule.URL_ATTR);
            PayaraInstanceProvider pip = commonSupport.getInstanceProvider();
            si = pip.getInstance(uri);
        }
        return si;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean result = true;
        for (int i = 0; i < activatedNodes.length; i++) {
            ServerInstance si = getServerInstance(activatedNodes[i]);
            if(si == null || !si.isRemovable()
                    // !PW FIXME is this a state we need to handle?
//                  || si.getServerState() == ServerInstance.STATE_WAITING
                    ) {
                result = false;
                break;
            }
        }
        return result;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(RemoveServerAction.class, "CTL_RemoveServerAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
