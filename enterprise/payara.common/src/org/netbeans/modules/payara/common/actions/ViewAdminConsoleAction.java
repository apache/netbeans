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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.common.CommonServerSupport;
import org.netbeans.modules.payara.common.PayaraState;
import org.netbeans.modules.payara.spi.PayaraModule;
import org.netbeans.modules.payara.spi.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Peter Williams
 */
@ActionID(id = "org.netbeans.modules.payara.common.actions.ViewAdminConsoleAction", category = "Payara")
@ActionRegistration(displayName = "#CTL_ViewAdminConsoleAction", lazy = false)
public class ViewAdminConsoleAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        CommonServerSupport commonSupport
                = (CommonServerSupport)activatedNodes[0]
                .getLookup().lookup(PayaraModule.class);
        if(commonSupport != null) {
            if (PayaraState.isOnline(commonSupport.getInstance())) {
                try {
                    Map<String, String> ip = commonSupport.getInstanceProperties();
                    StringBuilder urlBuilder = new StringBuilder(128);
                    String port = !("false".equals(System.getProperty("payara.useadminport"))) ?
                        ip.get(PayaraModule.ADMINPORT_ATTR) : ip.get(PayaraModule.HTTPPORT_ATTR);
                    String host = ip.get(PayaraModule.HOSTNAME_ATTR);
                    String uri = ip.get(PayaraModule.URL_ATTR);
                    if (uri == null || !uri.contains("ee6wc")) {
                        urlBuilder.append(Utils.getHttpListenerProtocol(host, port));
                    } else {
                        urlBuilder.append("http");
                    }
                    urlBuilder.append("://"); // NOI18N
                    urlBuilder.append(ip.get(PayaraModule.HOSTNAME_ATTR));
                    urlBuilder.append(":");
                    urlBuilder.append(port);
                    if("false".equals(System.getProperty("payara.useadminport"))) {
                        // url for admin gui when on http port (8080)
                        urlBuilder.append("/admin");
                    }
                    URL url = new URL(urlBuilder.toString());
                    URLDisplayer.getDefault().showURL(url);
                } catch (MalformedURLException ex) {
                    Logger.getLogger("payara").log(Level.WARNING, ex.getLocalizedMessage(), ex); // NOI18N
                }
            } else {
                String message = NbBundle.getMessage(ViewAdminConsoleAction.class, 
                        "MSG_ServerMustBeRunning"); // NOI18N
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(message,
                        NotifyDescriptor.DEFAULT_OPTION);
                DialogDisplayer.getDefault().notify(nd);
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return activatedNodes != null && activatedNodes.length == 1;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ViewAdminConsoleAction.class, "CTL_ViewAdminConsoleAction"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
