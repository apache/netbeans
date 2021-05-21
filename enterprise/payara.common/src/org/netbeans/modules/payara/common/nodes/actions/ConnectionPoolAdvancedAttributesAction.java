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

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public final class ConnectionPoolAdvancedAttributesAction extends NodeAction {

    @Override
    protected void performAction(Node[] nodes) {
        if((nodes == null) || (nodes.length < 1)) {
            return;
        }

        for(Node node : nodes) {
            ConnectionPoolAdvancedAttributesCookie uCookie = node.getCookie(ConnectionPoolAdvancedAttributesCookie.class);

            if(uCookie != null) {
                uCookie.openCustomizer();
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ConnectionPoolAdvancedAttributesAction.class, "CTL_ConnectionPoolAdvancedAttributesAction");
    }

    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Node[] nodes) {
        for(Node node : nodes) {
            ConnectionPoolAdvancedAttributesCookie cookie = node.getCookie(ConnectionPoolAdvancedAttributesCookie.class);
            if(cookie == null) {
                return false;
            }
        }

        return true;
    }
}

