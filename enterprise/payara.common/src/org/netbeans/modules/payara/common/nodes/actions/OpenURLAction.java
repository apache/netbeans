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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action which displays selected web module in browser.
 *
 * @author Michal Mocnak
 */
public class OpenURLAction extends NodeAction {

    protected void performAction(Node[] nodes) {
        for(Node node : nodes) {
            OpenURLActionCookie oCookie = node.getCookie(OpenURLActionCookie.class);

            if(oCookie != null) {
                try {
                    URLDisplayer.getDefault().showURL(new URL(oCookie.getWebURL()));
                } catch(MalformedURLException ex) {
                    Logger.getLogger("payara").log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
            }
        }
    }

    protected boolean enable(Node[] nodes) {
        for(Node node : nodes) {
            OpenURLActionCookie oCookie = node.getCookie(OpenURLActionCookie.class);
            UndeployModuleCookie uCookie = node.getCookie(UndeployModuleCookie.class);

            if(uCookie != null) {
                if(uCookie.isRunning()) {
                    return false;
                }
            }
            if(oCookie != null && oCookie.getWebURL() != null) {
                return true;
            }
        }

        return false;
    }

    public String getName() {
        return NbBundle.getMessage(OpenURLAction.class, "LBL_OpenInBrowserAction"); // NOI18N

    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
