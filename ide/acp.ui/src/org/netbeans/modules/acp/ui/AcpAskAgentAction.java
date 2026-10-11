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
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(category = "ACP", id = "org.netbeans.modules.acp.ui.AcpAskAgentAction")
@ActionRegistration(lazy = false, displayName = "#CTL_AcpAskAgentAction")
@ActionReferences({
    @ActionReference(path = "Editors/Popup", position = 5000, separatorBefore = 4999)
})
@NbBundle.Messages("CTL_AcpAskAgentAction=Ask ACP Agent...")
public final class AcpAskAgentAction extends CallableSystemAction {

    private String capturedContext;

    @Override
    public void performAction() {
        if (capturedContext != null && !capturedContext.isEmpty()) {
            final String context = capturedContext;
            capturedContext = null;
            SwingUtilities.invokeLater(() -> openChatWithContext(context));
        }
    }

    @Override
    public boolean isEnabled() {
        JTextComponent editor = EditorRegistry.lastFocusedComponent();
        if (editor == null) {
            return false;
        }
        capturedContext = buildContext(editor);
        return capturedContext != null && !capturedContext.isEmpty();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(AcpAskAgentAction.class, "CTL_AcpAskAgentAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    private static String buildContext(JTextComponent editor) {
        String selectedText = editor.getSelectedText();
        if (selectedText == null || selectedText.isEmpty()) {
            return null;
        }
        String filePath = getFilePath(editor.getDocument());
        StringBuilder context = new StringBuilder();
        if (filePath != null) {
            context.append("File: ").append(filePath).append("\n\n");
        }
        context.append("Selected code:\n```\n").append(selectedText).append("\n```\n");
        return context.toString();
    }

    private static String getFilePath(Document doc) {
        if (doc == null) {
            return null;
        }
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof DataObject dataObject) {
            FileObject fo = dataObject.getPrimaryFile();
            return fo.getPath();
        }
        return null;
    }

    private static void openChatWithContext(String context) {
        assert SwingUtilities.isEventDispatchThread();
        TopComponent tc = WindowManager.getDefault().findTopComponent("AcpChatTopComponent");
        if (tc != null) {
            tc.open();
            tc.requestActive();
            if (tc instanceof AcpContextReceiver receiver) {
                receiver.setContextForNextPrompt(context);
            }
        }
    }
}
