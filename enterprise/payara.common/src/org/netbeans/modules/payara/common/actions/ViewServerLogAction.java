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

import org.netbeans.modules.payara.tooling.utils.ServerUtils;
import org.netbeans.modules.payara.common.CommonServerSupport;
import org.netbeans.modules.payara.common.PayaraInstance;
import org.netbeans.modules.payara.common.LogViewMgr;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.payara.spi.PayaraModule.ServerState;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;

/** 
 * This action will open or focus the server log window for the selected server
 * instance.
 * 
 * @author Peter Williams
 */
@ActionID(id = "org.netbeans.modules.payara.common.actions.ViewServerLogAction", category = "Payara")
@ActionRegistration(displayName = "#CTL_ViewServerLogAction", lazy = false)
public class ViewServerLogAction extends NodeAction {

    private static final String SHOW_SERVER_LOG_ICONBASE =
            "org/netbeans/modules/payara/common/resources/serverlog.gif"; // NOI18N

    @Override
    protected void performAction(Node[] nodes) {
        Lookup lookup = nodes[0].getLookup();
        CommonServerSupport commonSupport = lookup.lookup(CommonServerSupport.class);
        if(commonSupport != null) {
            LogViewMgr.displayOutput(commonSupport.getInstance(), lookup);
        }
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length < 1 || nodes[0] == null) {
            return false;
        }
        PayaraModule commonSupport = nodes[0].getLookup().lookup(PayaraModule.class);
        if (commonSupport == null || !(commonSupport.getInstance() instanceof PayaraInstance)) {
            return false;
        }
        PayaraInstance server = (PayaraInstance) commonSupport.getInstance();
        String uri = server.getUrl();
        return uri != null && uri.length() > 0
                && ((!server.isRemote()
                && ServerUtils.getServerLogFile(server).canRead())
                || (commonSupport.isRestfulLogAccessSupported()
                && server.isRemote() && isRunning(commonSupport)));
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ViewServerLogAction.class, "CTL_ViewServerLogAction");
    }

    @Override
    protected String iconResource() {
        return SHOW_SERVER_LOG_ICONBASE;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    private boolean isRunning(PayaraModule commonSupport) {
        ServerState ss = commonSupport.getServerState();
        return ss == ServerState.RUNNING || ss == ServerState.RUNNING_JVM_DEBUG || ss == ServerState.RUNNING_JVM_PROFILER;
    }
}
