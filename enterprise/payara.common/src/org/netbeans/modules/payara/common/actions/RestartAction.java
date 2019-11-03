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
import org.netbeans.modules.payara.spi.PayaraModule;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;


/**
 * Restart action stops the server and then starts it again in the mode it 
 * was running in before (normal or debug).
 *
 * @author sherold
 */
public class RestartAction extends NodeAction {
    
    @Override
    public String getName() {
        return NbBundle.getMessage(RestartAction.class, "CTL_RestartAction");
    }
    
    @Override
    protected void performAction(Node[] activatedNodes) {
        for(Node node : activatedNodes) {
            PayaraModule commonSupport = 
                    node.getLookup().lookup(PayaraModule.class);
            if(commonSupport != null) {
                if (commonSupport.isRemote()) {
                    // always restart remote instances in NORMAL mode
                    if (commonSupport.supportsRestartInDebug())
                        commonSupport.setEnvironmentProperty(PayaraModule.JVM_MODE, PayaraModule.NORMAL_MODE, true);
                }
                performActionImpl(commonSupport);
            }
        }
    }
    
    private static void performActionImpl(final PayaraModule commonSupport) {
        commonSupport.restartServer(null);
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
    
    private static boolean enableImpl(final PayaraModule commonSupport) {
        return PayaraState.isOnline(commonSupport.getInstance());
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous() { 
        return false; 
    }
    
    /** This action will be displayed in the server output window */
    public static class OutputAction extends AbstractOutputAction {
        
        private static final String ICON = 
                "org/netbeans/modules/payara/common/resources/restart.png"; // NOI18N
        
        public OutputAction(final PayaraModule commonSupport) {
            super(commonSupport, NbBundle.getMessage(RestartAction.class, "LBL_RestartOutput"), // NOI18N
                    NbBundle.getMessage(RestartAction.class, "LBL_RestartOutputDesc"), // NOI18N
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
