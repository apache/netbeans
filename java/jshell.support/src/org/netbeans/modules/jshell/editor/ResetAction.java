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
package org.netbeans.modules.jshell.editor;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.env.ShellRegistry;
import org.openide.awt.ActionID;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */

@NbBundle.Messages({
    "ACTION_Reset=Reset Java Shell"
})
@EditorActionRegistration(
        name = ResetAction.NAME,
        iconResource = "org/netbeans/modules/jshell/resources/reset.png",
        mimeType = "text/x-repl",
        menuPath = "Edit",
        toolBarPosition = 20002,
        menuPosition = 20000
)
@ActionID(category = "Edit", id = "org.netbeans.modules.jshell.editor.ResetAction")
public class ResetAction extends BaseAction {
    public static final String NAME = "jshell-reset";

    public ResetAction() {
        super(NAME, BaseAction.CLEAR_STATUS_TEXT | BaseAction.MAGIC_POSITION_RESET | BaseAction.NO_RECORDING);
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        Document d = target.getDocument();
        
        if (d == null) {
            Toolkit.getDefaultToolkit().beep();
            return;
        } 
        FileObject f = EditorDocumentUtils.getFileObject(d);
        JShellEnvironment env = ShellRegistry.get().getOwnerEnvironment(f);
        if (env != null) {
            env.reset();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
