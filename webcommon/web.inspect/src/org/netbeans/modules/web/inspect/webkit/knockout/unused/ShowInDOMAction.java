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

package org.netbeans.modules.web.inspect.webkit.knockout.unused;

import java.awt.EventQueue;
import java.util.Collections;
import org.netbeans.modules.web.inspect.ui.DomTC;
import org.netbeans.modules.web.inspect.webkit.DOMNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Shows the owner of an unused binding in DOM Tree view.
 *
 * @author Jan Stola
 */
public class ShowInDOMAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        Node selection = activatedNodes[0];
        UnusedBinding unusedBinding = selection.getLookup().lookup(UnusedBinding.class);
        DOMNode node = unusedBinding.getNode();
        if (node != null) {
            unusedBinding.getPage().setSelectedNodes(Collections.singletonList(node));
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    activateDOMView();
                }
            });
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            Node selection = activatedNodes[0];
            UnusedBinding unusedBinding = selection.getLookup().lookup(UnusedBinding.class);
            return (unusedBinding != null) && !unusedBinding.isRemoved();
        }
        return false;
    }

    /**
     * Activates the DOM Tree view.
     */
    void activateDOMView() {
        TopComponent tc = WindowManager.getDefault().findTopComponent(DomTC.ID);
        tc.open();
        tc.requestActive();
    }

    @Override
    @NbBundle.Messages({
        "ShowInDOMAction.name=Show in Browser DOM"
    })
    public String getName() {
        return Bundle.ShowInDOMAction_name();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

}
