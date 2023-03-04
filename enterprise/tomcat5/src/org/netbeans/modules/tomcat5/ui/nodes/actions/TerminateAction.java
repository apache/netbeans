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

package org.netbeans.modules.tomcat5.ui.nodes.actions;

import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.ui.nodes.TomcatInstanceNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * Terminate Tomcat server action. If the Tomcat has been started from withing the
 * IDE, this action will terminate the running process.
 *
 * @author sherold
 */
public class TerminateAction extends NodeAction {
    
    @Override
    protected void performAction(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            TomcatInstanceNode cookie = (TomcatInstanceNode)nodes[i].getCookie(TomcatInstanceNode.class);
            if (cookie != null) {
                final TomcatManager tm = cookie.getTomcatManager();
                String name = tm.getTomcatProperties().getDisplayName();
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(TerminateAction.class, "MSG_terminate", name),
                        NotifyDescriptor.YES_NO_OPTION);
                Object retValue = DialogDisplayer.getDefault().notify(nd);
                if (retValue == DialogDescriptor.YES_OPTION) {
                    RequestProcessor.getDefault().post( () -> {
                        tm.terminate();
                        // wait a sec before refreshing the state
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ie) {}
                        tm.getInstanceProperties().refreshServerInstance();
                    });
                }
            }
        }
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            TomcatInstanceNode cookie = (TomcatInstanceNode)nodes[i].getCookie(TomcatInstanceNode.class);
            if (cookie == null) {
                return false;
            }
            TomcatManager tm = cookie.getTomcatManager();
            if (tm == null || !tm.isRunning(false)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(TerminateAction.class, "LBL_TerminateAction");
    }
    
    @Override
    protected boolean asynchronous() { return false; }
    
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
