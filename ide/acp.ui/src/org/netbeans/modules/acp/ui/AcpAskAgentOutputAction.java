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

import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(category = "ACP", id = "org.netbeans.modules.acp.ui.AcpAskAgentOutputAction")
@ActionRegistration(lazy = false, displayName = "#CTL_AcpAskAgentOutputAction")
@ActionReference(path = "Menu/Tools", position = 1600, separatorBefore = 1599)
@NbBundle.Messages("CTL_AcpAskAgentOutputAction=Ask ACP Agent About Output...")
public final class AcpAskAgentOutputAction extends AbstractAction {

    public AcpAskAgentOutputAction() {
        super(NbBundle.getMessage(AcpAskAgentOutputAction.class, "CTL_AcpAskAgentOutputAction"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String outputText = getOutputText();
        if (outputText == null || outputText.isEmpty()) {
            return;
        }

        String context = "Build/test output:\n```\n" + outputText + "\n```\n\nPlease analyze this output and help me fix any errors.";

        TopComponent tc = WindowManager.getDefault().findTopComponent("AcpChatTopComponent");
        if (tc != null) {
            tc.open();
            tc.requestActive();
            if (tc instanceof AcpContextReceiver receiver) {
                receiver.setContextForNextPrompt(context);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return getOutputText() != null;
    }

    private static String getOutputText() {
        Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focused instanceof JTextComponent textComp) {
            String selected = textComp.getSelectedText();
            if (selected != null && !selected.isEmpty()) {
                return selected;
            }
        }
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        if (activeTC == null) {
            return null;
        }
        return findSelectedText(activeTC);
    }

    private static String findSelectedText(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextComponent textComp) {
                String selected = textComp.getSelectedText();
                if (selected != null && !selected.isEmpty()) {
                    return selected;
                }
            }
            if (comp instanceof Container child) {
                String result = findSelectedText(child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
