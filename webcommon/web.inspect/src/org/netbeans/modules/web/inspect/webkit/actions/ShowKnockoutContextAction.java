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
package org.netbeans.modules.web.inspect.webkit.actions;

import org.netbeans.modules.web.inspect.webkit.knockout.KnockoutTCController;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action that shows Knockout Binding Context for the selected node.
 *
 * @author Jan Stola
 */
public class ShowKnockoutContextAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        KnockoutTCController.showKnockoutContext();
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return (activatedNodes.length == 1) && KnockoutTCController.isKnockoutUsed();
    }

    @Override
    @NbBundle.Messages({
        "ShowKnockoutContextAction.displayName=Show Knockout Binding Context" // NOI18N
    })
    public String getName() {
        return Bundle.ShowKnockoutContextAction_displayName();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

}
