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
package org.netbeans.modules.docker.ui.node;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Petr Hejl
 */
public class RefreshAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            Refreshable refreshable = node.getLookup().lookup(Refreshable.class);
            if (refreshable != null) {
                refreshable.refresh();
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            if (node.getLookup().lookup(Refreshable.class) == null) {
                return false;
            }
        }
        return true;
    }

    @NbBundle.Messages("LBL_RefreshAction=Refresh")
    @Override
    public String getName() {
        return Bundle.LBL_RefreshAction();
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
