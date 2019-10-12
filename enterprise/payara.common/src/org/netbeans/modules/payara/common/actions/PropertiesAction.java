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

import org.netbeans.api.server.CommonServerUIs;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.payara.common.PayaraInstanceProvider;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Displays server manager with selected server instance focused.
 *
 * @author Peter Williams
 */
public class PropertiesAction extends NodeAction {

    public void performAction(Node[] nodes) {
        ServerInstance instance = getServerInstance(nodes[0]);
        CommonServerUIs.showCustomizer(instance);
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
    
    protected boolean enable(Node[] nodes) {
        return nodes != null && nodes.length > 0;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(PropertiesAction.class, "CTL_Properties"); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
}
