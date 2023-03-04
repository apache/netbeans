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
package org.netbeans.modules.websvc.saas.ui.actions;

import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.openide.awt.HtmlBrowser;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Displays the documentation of a web service.
 *
 * @author nam
 * @author Jan Stola
 */
public class ViewApiDocAction extends NodeAction {

    @Override
    protected boolean enable(Node[] nodes) {
        return (nodes.length == 1) && (getApiDocUrl(nodes[0]) != null);
    }

    private String getApiDocUrl(Node node) {
        Saas saas = node.getLookup().lookup(Saas.class);
        return (saas == null) ? null : saas.getApiDoc();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ViewWSDLAction.class, "VIEW_API_Action"); // NOI18N
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            try {
                String apiUrl = getApiDocUrl(node);
                URL href = new URL(apiUrl);
                HtmlBrowser.URLDisplayer.getDefault().showURL(href);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public boolean asynchronous() {
        return true;
    }
}
