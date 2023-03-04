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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.tomcat5.deploy.TomcatManager;
import org.netbeans.modules.tomcat5.ui.nodes.TomcatInstanceNode;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 * Action which opens the Tomcat Administration console in a browser.
 *
 * @author sherold
 */
public class AdminConsoleAction extends NodeAction {
    
    @Override
    protected void performAction (Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            final TomcatInstanceNode cookie = (TomcatInstanceNode)nodes[i].getCookie(TomcatInstanceNode.class);
            if (cookie != null) {
                RequestProcessor.getDefault().post( () -> {
                    TomcatManager tm = cookie.getTomcatManager();
                    String adminUrl = tm.getServerUri() + "/admin"; // NOI18N
                    try {
                        URLDisplayer.getDefault().showURL(new URL(adminUrl));
                    } catch (MalformedURLException e) {
                        Logger.getLogger(AdminConsoleAction.class.getName()).log(Level.INFO, null, e);
                    }
                });
            }
        }
    }

    @Override
    protected boolean enable (Node[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            TomcatInstanceNode cookie = (TomcatInstanceNode)nodes[i].getCookie(TomcatInstanceNode.class);
            if (cookie == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName () {
        return NbBundle.getMessage(AdminConsoleAction.class, "LBL_AdminConsoleAction");
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }
}
