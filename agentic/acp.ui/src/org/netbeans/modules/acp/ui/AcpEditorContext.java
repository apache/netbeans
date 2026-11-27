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

import java.awt.event.ActionEvent;
import java.util.function.BiFunction;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@NbBundle.Messages("MSG_SelectEditorText=Select text in the editor to send it to the ACP agent.")
final class AcpEditorContext {

    private AcpEditorContext() {
    }

    static void send(ActionEvent event, BiFunction<JTextComponent, String, String> format) {
        JTextComponent editor = event.getSource() instanceof JTextComponent source
                ? source : EditorRegistry.lastFocusedComponent();
        String selected = editor == null ? null : editor.getSelectedText();
        if (selected == null || selected.isEmpty()) {
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(Bundle.MSG_SelectEditorText()));
            return;
        }

        String context = format.apply(editor, selected);
        TopComponent chat = WindowManager.getDefault().findTopComponent("AcpChatTopComponent");
        if (chat != null) {
            chat.open();
            chat.requestActive();
            if (chat instanceof AcpContextReceiver receiver) {
                receiver.setContextForNextPrompt(context);
            }
        }
    }

    static String filePath(JTextComponent editor) {
        Object source = editor.getDocument().getProperty(Document.StreamDescriptionProperty);
        if (source instanceof DataObject data) {
            return data.getPrimaryFile().getPath();
        }
        return source instanceof FileObject file ? file.getPath() : null;
    }
}
