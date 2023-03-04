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
package org.netbeans.modules.debugger.jpda.visual.actions;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Martin Entlicher
 */
public class ToggleComponentBreakpointAction extends NodeAction {

    @Override
    public String getName() {
        return NbBundle.getMessage(ToggleComponentBreakpointAction.class, "CTL_ToggleComponentBreakpointAction");
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        ComponentBreakpointActionProvider.doAction(activatedNodes);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ToggleComponentBreakpointAction.class);
    }
    
}
