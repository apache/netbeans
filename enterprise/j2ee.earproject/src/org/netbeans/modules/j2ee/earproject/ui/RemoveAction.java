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

package org.netbeans.modules.j2ee.earproject.ui;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author  vkraemer
 */
public class RemoveAction extends NodeAction {
    private static final long serialVersionUID = 1L;

    public String getName() {
        return NbBundle.getMessage(this.getClass(), "LBL_RemoveAction");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        return activatedNodes.length >= 0;
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        ModuleNode n = null;
        for (int i = 0; i < activatedNodes.length; i++) {
            n = activatedNodes[i].getCookie(ModuleNode.class);
            n.removeFromJarContent();
        }
    }
    
}
