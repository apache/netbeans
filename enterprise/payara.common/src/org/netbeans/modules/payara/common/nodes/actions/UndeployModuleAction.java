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
package org.netbeans.modules.payara.common.nodes.actions;

import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.tooling.admin.ResultString;
import org.netbeans.modules.payara.spi.ServerUtilities;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Michal Mocnak
 * @author Peter Williams
 */
public class UndeployModuleAction extends NodeAction {

    @Override
    protected void performAction(Node[] nodes) {
        if((nodes == null) || (nodes.length < 1)) {
            return;
        }

        RequestProcessor undeployer = new RequestProcessor("pf-undeployer");
        
        for(Node node : nodes) {
            UndeployModuleCookie uCookie = node.getCookie(UndeployModuleCookie.class);

            if(uCookie != null) {
                final Future<ResultString> result = uCookie.undeploy();
                Node pNode = node.getParentNode();
                final Node gpNode = null != pNode ? pNode.getParentNode() : null;
                final Node fnode = node;

                undeployer.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            result.get(ServerUtilities.ACTION_TIMEOUT, ServerUtilities.ACTION_TIMEOUT_UNIT);
                        } catch(TimeoutException ex) {
                            Logger.getLogger("payara").log(Level.WARNING, "Undeploy action timed out for " + fnode.getDisplayName());
                        } catch (InterruptedException ie) {
                            // we can ignore this
                        }catch(Exception ex) {
                            Logger.getLogger("payara").log(Level.INFO, ex.getLocalizedMessage(), ex);
                        }
                        if(gpNode != null) {
                            Node[] nodes = gpNode.getChildren().getNodes();
                            for(Node node : nodes) {
                                RefreshModulesCookie cookie = node.getCookie(RefreshModulesCookie.class);
                                if(cookie != null) {
                                    cookie.refresh(null, fnode.getDisplayName());
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    protected boolean enable(Node[] nodes) {
        for(Node node : nodes) {
            UndeployModuleCookie cookie = node.getCookie(UndeployModuleCookie.class);
            if(cookie == null || cookie.isRunning()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(UndeployModuleAction.class, "LBL_UndeployAction");
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
