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
 * @author Peter Williams
 */
public class UnregisterResourceAction extends NodeAction {

    @Override
    protected void performAction(Node[] nodes) {
        if((nodes == null) || (nodes.length < 1)) {
            return;
        }
        
        for(Node node : nodes) {
            UnregisterResourceCookie uCookie = node.getCookie(UnregisterResourceCookie.class);

            if(uCookie != null) {
                final Future<ResultString> result = uCookie.unregister();
                final Node pNode = node.getParentNode().getParentNode();

                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            result.get(ServerUtilities.ACTION_TIMEOUT, ServerUtilities.ACTION_TIMEOUT_UNIT);
                            if(pNode != null) {
                                Node[] nodes = pNode.getChildren().getNodes();
                                for(Node node : nodes) {
                                    RefreshModulesCookie cookie = node.getCookie(RefreshModulesCookie.class);
                                    if(cookie != null) {
                                        cookie.refresh();
                                    }
                                }
                            }
                        } catch(TimeoutException ex) {
                            Logger.getLogger("payara").log(Level.WARNING, "Timeout waiting on unregister.", ex);
                        } catch(Exception ex) {
                            Logger.getLogger("payara").log(Level.INFO, ex.getLocalizedMessage(), ex);
                        }
                    }
                });
            }
        }
    }

    @Override
    protected boolean enable(Node[] nodes) {
        for(Node node : nodes) {
            UnregisterResourceCookie cookie = node.getCookie(UnregisterResourceCookie.class);
            if(cookie == null || cookie.isRunning()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(UnregisterResourceAction.class, "LBL_UnregisterAction");
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
