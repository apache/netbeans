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

import org.netbeans.modules.payara.common.PayaraState;
import org.netbeans.modules.payara.common.utils.Util;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.payara.spi.PayaraModule.ServerState;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Profile server action starts the server in the profile mode.
 *
 */
public class ProfileAction extends NodeAction {
    
    @Override
    protected void performAction(Node[] activatedNodes) {
        for(Node node : activatedNodes) {
            PayaraModule commonSupport = 
                    node.getLookup().lookup(PayaraModule.class);
            if(commonSupport != null) {
                performActionImpl(commonSupport,node);
                break;
            }
        }
    }
    
    private static void performActionImpl(final PayaraModule commonSupport, Node node) {
        commonSupport.setEnvironmentProperty(PayaraModule.JVM_MODE, PayaraModule.PROFILE_MODE, true);
        
        commonSupport.startServer(null, ServerState.STOPPED_JVM_PROFILER);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean result = false;
        if(activatedNodes != null && activatedNodes.length > 0) {
            for(Node node : activatedNodes) {
                PayaraModule commonSupport = node.getLookup().lookup(PayaraModule.class);
                if(commonSupport != null) {
                    result = enableImpl(commonSupport,node);
                } else {
                    // No server instance found for this node.
                    result = false;
                }
                if(!result) {
                    break;
                }
            }
        }
        return result;
    }
    
    private static boolean enableImpl(PayaraModule commonSupport, Node node) {
        return (! commonSupport.getInstance().isWSL()) &&
                PayaraState.canStart(commonSupport.getInstance()) &&
                null != commonSupport.getInstanceProperties().get(PayaraModule.DOMAINS_FOLDER_ATTR) &&
                Util.isDefaultOrServerTarget(commonSupport.getInstanceProperties());
    }

    @Override
    protected boolean asynchronous() { 
        return false; 
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(ProfileAction.class, "CTL_ProfileAction");
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
