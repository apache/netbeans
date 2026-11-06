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
package org.netbeans.modules.acp.ui;

import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(category = "ACP", id = "org.netbeans.modules.acp.ui.AcpAskAgentOutputAction")
@ActionRegistration(lazy = false, displayName = "#CTL_AcpAskAgentOutputAction")
@ActionReferences({
    @ActionReference(path = "Editors/Popup", position = 5010)
})
@NbBundle.Messages("CTL_AcpAskAgentOutputAction=Ask ACP Agent About Output...")
public final class AcpAskAgentOutputAction extends CallableSystemAction {

    private String capturedContext;

    @Override
    public void performAction() {
        if (capturedContext != null && !capturedContext.isEmpty()) {
            final String context = capturedContext;
            capturedContext = null;
            SwingUtilities.invokeLater(() -> {
                TopComponent tc = WindowManager.getDefault().findTopComponent("AcpChatTopComponent");
                if (tc != null) {
                    tc.open();
                    tc.requestActive();
                    if (tc instanceof AcpContextReceiver receiver) {
                        receiver.setContextForNextPrompt(context);
                    }
                }
            });
        }
    }

    @Override
    public boolean isEnabled() {
        JTextComponent editor = EditorRegistry.lastFocusedComponent();
        if (editor == null) {
            return false;
        }
        String selected = editor.getSelectedText();
        if (selected == null || selected.isEmpty()) {
            capturedContext = null;
            return false;
        }
        capturedContext = "Build/test output:\n```\n" + selected
                + "\n```\n\nPlease analyze this output and help me fix any errors.";
        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(AcpAskAgentOutputAction.class, "CTL_AcpAskAgentOutputAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
