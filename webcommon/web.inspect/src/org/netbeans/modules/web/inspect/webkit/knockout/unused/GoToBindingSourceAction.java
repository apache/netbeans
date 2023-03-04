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
import java.awt.event.ActionEvent;
import org.netbeans.modules.web.inspect.webkit.DOMNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Go to (unused binding) source action.
 *
 * @author Jan Stola
 */
public class GoToBindingSourceAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        Node selection = activatedNodes[0];
        UnusedBinding unusedBinding = selection.getLookup().lookup(UnusedBinding.class);
        final DOMNode node = unusedBinding.getNode();
        if (node != null) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    node.getPreferredAction().actionPerformed(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, null));
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

    @Override
    @NbBundle.Messages({
        "GoToBindingSourceAction.name=Go to Source"
    })
    public String getName() {
        return Bundle.GoToBindingSourceAction_name();
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
