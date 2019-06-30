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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.tooling.admin.ResultString;
import org.netbeans.modules.payara.spi.ServerUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * @author vince kraemer
 *
 * Based on UndeployModuleAction
 * @author Michal Mocnak
 * @author Peter Williams
 */
public class EnableModulesAction extends NodeAction {

    @Override
    protected void performAction(Node[] nodes) {
        if((nodes == null) || (nodes.length < 1)) {
            return;
        }

        targets.clear();
        for (Node n : nodes) {
            targets.add(n.getDisplayName());
        }
        String aDup = getDup(targets);
        if (null != aDup) {
            // open dialog
            NotifyDescriptor m = new NotifyDescriptor.Message(NbBundle.getMessage(EnableModulesAction.class, "ERR_HAS_DUPS", aDup),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(m);
            return;
        }

        RequestProcessor enabler = new RequestProcessor("pf-enable-module");
        
        for(Node node : nodes) {
            EnableModulesCookie uCookie = node.getCookie(EnableModulesCookie.class);

            if(uCookie != null) {
                final Future<ResultString> result = uCookie.enableModule();
                final Node pNode = node.getParentNode().getParentNode();
                final Node fnode = node;

                enabler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            result.get(ServerUtilities.ACTION_TIMEOUT, ServerUtilities.ACTION_TIMEOUT_UNIT);
                        } catch(TimeoutException ex) {
                            Logger.getLogger("payara").log(Level.WARNING, "Enable action timed out for " + fnode.getDisplayName());
                        } catch (InterruptedException ie) {
                            // we can ignore this
                        }catch(Exception ex) {
                            Logger.getLogger("payara").log(Level.INFO, ex.getLocalizedMessage(), ex);
                        }
                        if(pNode != null) {
                            Node[] nodes = pNode.getChildren().getNodes();
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

    private List<String> targets = new ArrayList<String>();

    @Override
    protected boolean enable(Node[] nodes) {
        for(Node node : nodes) {
            EnableModulesCookie cookie = node.getCookie(EnableModulesCookie.class);
            if(cookie == null || cookie.isRunning()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(EnableModulesAction.class, "LBL_EnableAction");
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    static String getDup(List<String> targets) {
        Map<String,String> uniqTargets = new HashMap<String,String>();
        if (null == targets) {
            return null;
        }
        for (String target : targets) {
            int colon = target.indexOf(":");
            if (-1 == colon) {
                colon = target.length();
            }
            String shortName = target.substring(0,colon);
            if (uniqTargets.containsKey(shortName)) {
                return shortName;
            } else {
                uniqTargets.put(shortName,shortName);
            }
        }
        return null;
    }
}
