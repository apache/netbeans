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
package org.openide.actions;

import org.openide.nodes.Node;
import org.openide.nodes.NodeOperation;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;


/** Open an Explorer window with a particular root node.
* @see NodeOperation#explore
* @author   Ian Formanek
*/
public final class OpenLocalExplorerAction extends NodeAction {
    protected void performAction(Node[] activatedNodes) {
        NodeOperation.getDefault().explore(activatedNodes[0]);
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        if ((activatedNodes == null) || (activatedNodes.length != 1) || (activatedNodes[0].isLeaf())) {
            return false;
        }

        return true;
    }

    public String getName() {
        return NbBundle.getMessage(OpenLocalExplorerAction.class, "OpenLocalExplorer");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(OpenLocalExplorerAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/actions/openLocalExplorer.gif"; // NOI18N
    }
}
