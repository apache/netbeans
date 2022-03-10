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
package org.netbeans.modules.cnd.lsp.makeproject.ui.actions;

import org.netbeans.modules.cnd.lsp.server.ClangdProcess;
import org.netbeans.modules.cnd.lsp.server.LSPServerState;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Restarts the clangd process.
 * @author antonio
 */
public class ClangdRestartAction extends NodeAction {

    public ClangdRestartAction() {
        super();
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(ClangdRestartAction.class, "ClangdStartAction.description")); // NOI18N
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length > 0) {
            ClangdProcess.getInstance().restart();
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length == 0) {
            return false;
        }
        LSPServerState clangdState = ClangdProcess.getInstance().getState();
        switch (clangdState) {
            case RUNNING:
            case ERROR:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ClangdRestartAction.class, "ClangdStartAction.name"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
