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

import java.awt.event.ActionEvent;
import org.netbeans.modules.payara.common.PayaraState;
import org.netbeans.modules.payara.common.utils.Util;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.payara.spi.PayaraModule.ServerState;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Peter Williams
 */
public class StopServerAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        for(Node node : activatedNodes) {
            PayaraModule commonSupport = 
                    node.getLookup().lookup(PayaraModule.class);
            if(commonSupport != null) {
                performActionImpl(commonSupport);
            }
        }
    }
    
    private static void performActionImpl(PayaraModule commonSupport) {
        commonSupport.stopServer(null);
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean result = false;
        if(activatedNodes != null && activatedNodes.length > 0) {
            for(Node node : activatedNodes) {
                PayaraModule commonSupport = node.getLookup().lookup(PayaraModule.class);
                if(commonSupport != null) {
                    result = enableImpl(commonSupport);
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

    private static boolean enableImpl(PayaraModule commonSupport) {
        if (commonSupport.getInstance().isWSL()) {
            return false;
        }
        boolean online = PayaraState.isOnline(commonSupport.getInstance());
        return (online
                || commonSupport.getServerState() == ServerState.STOPPED_JVM_PROFILER)
                && (null != commonSupport.getInstanceProperties().get(PayaraModule.DOMAINS_FOLDER_ATTR)
                // there is a target part of this server's url AND the das is running
                || (!Util.isDefaultOrServerTarget(commonSupport.getInstanceProperties())
                && online));
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(StopServerAction.class, "CTL_StopServerAction"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /** This action will be displayed in the server output window */
    public static class OutputAction extends AbstractOutputAction {
        
        private static final String ICON = 
                "org/netbeans/modules/payara/common/resources/stop.png"; // NOI18N
        
        public OutputAction(final PayaraModule commonSupport) {
            super(commonSupport, NbBundle.getMessage(StopServerAction.class, "LBL_StopOutput"), // NOI18N
                    NbBundle.getMessage(StopServerAction.class, "LBL_StopOutputDesc"), // NOI18N
                    ICON);
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            performActionImpl(commonSupport);
        }

        @Override
        public boolean isEnabled() {
            return enableImpl(commonSupport);
        }
        
    }
}
