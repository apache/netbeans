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

package org.netbeans.modules.javaee.wildfly.nodes.actions;

import java.util.Enumeration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Michal Mocnak
 */
public class UndeployModuleAction extends NodeAction {

    private static final RequestProcessor PROCESSOR = new RequestProcessor ("JBoss undeploy UI", 1); // NOI18N

    protected void performAction(Node[] nodes) {
        if( (nodes == null) || (nodes.length < 1) )
            return;

        for (int i = 0; i < nodes.length; i++) {
            UndeployModuleCookie uCookie = (UndeployModuleCookie)nodes[i].getCookie(UndeployModuleCookie.class);
            if (uCookie != null) {
                final Task t = uCookie.undeploy();
                final Node node = nodes[i].getParentNode();

                PROCESSOR.post(new Runnable() {
                    public void run() {
                        t.waitFinished();
                        if(node != null) {
                            Node apps = node.getParentNode();
                            if (apps != null) {
                                Enumeration appTypes = apps.getChildren().nodes();
                                while (appTypes.hasMoreElements()) {
                                    Node appType = (Node)appTypes.nextElement();
                                    RefreshModulesCookie cookie = (RefreshModulesCookie)
                                            appType.getCookie(RefreshModulesCookie.class);
                                    if (cookie != null) {
                                        cookie.refresh();
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    protected boolean enable(Node[] nodes) {
        UndeployModuleCookie cookie;
        for (int i=0; i<nodes.length; i++) {
            cookie = (UndeployModuleCookie)nodes[i].getCookie(UndeployModuleCookie.class);
            if (cookie == null || cookie.isRunning())
                return false;
        }

        return true;
    }

    public String getName() {
        return NbBundle.getMessage(UndeployModuleAction.class, "LBL_UndeployAction");
    }

    protected boolean asynchronous() { return false; }

    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
