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
package org.netbeans.modules.jshell.editor;

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.jshell.support.ShellSession;
import org.openide.*;
import org.openide.awt.ActionID;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * This Action generates class out of (active) snippets written into the
 * Shell.
 *
 * @author sdedic
 */
@EditorActionRegistration(
    mimeType = "text/x-repl",
    name = GenerateClassAction.NAME,
    popupPath = "",
    popupPosition = 20002,
    iconResource = "org/netbeans/modules/jshell/resources/saveToClass.gif" // NOI18N
)
@ActionID(category = "Source", id = "org.netbeans.modules.jshell.editor.GenerateClassAction")
public class GenerateClassAction extends ShellActionBase {
    public static final String NAME = "jshell-save-to-class"; // NOI18N
    
    public GenerateClassAction() {
        super(NAME);
    }

    @Override
    protected void doPerformAction(ActionEvent evt, JTextComponent target, ShellSession s) {
        Node sel = Utilities.actionsGlobalContext().lookup(Node.class);
        FileObject anchor = null;
        if (sel != null) {
            anchor = sel.getLookup().lookup(FileObject.class);
        }
        doGenerateForSession(target, s, anchor);
    }

    @NbBundle.Messages({
        "TITLE_GenerateClassFromSnippets=Copy Snippets to Class",
        "BTN_OK=Copy",
        "ERROR_NoClassTemplate=Class template not found; chech Templates in Tools menu."
    })
    private void doGenerateForSession(JTextComponent target, ShellSession s, FileObject anchor) {
        Project p = s.getEnv().getProject();
        String n = (String)target.getClientProperty(CLASSNAME_PROPERTY);
        if (anchor == null) {
            anchor = (FileObject)target.getClientProperty(FOLDER_PROPERTY);
        }
        ClassNamePanel panel = new ClassNamePanel(p, anchor, n);
        JButton b = new JButton(Bundle.BTN_OK());
        Object[] opts = new Object[] { b, DialogDescriptor.CANCEL_OPTION };
        DialogDescriptor desc = new DialogDescriptor(
                panel,
                Bundle.TITLE_GenerateClassFromSnippets(),
                true,
                opts,
                b,
                DialogDescriptor.RIGHT_ALIGN | DialogDescriptor.BOTTOM_ALIGN,
                new HelpCtx(GenerateClassAction.class.getName()),
                null
            );
        panel.setNotifier(desc.createNotificationLineSupport());
        desc.setClosingOptions(opts);
        
        panel.addChangeListener(e -> {
            b.setEnabled(!panel.hasErrors());
        });
        
        Object r = DialogDisplayer.getDefault().notify(desc);
        if (r != b) {
            return;
        }
        
        String cn = panel.getClassName();
        FileObject folder = panel.getTarget();
        target.putClientProperty(CLASSNAME_PROPERTY, cn);
        target.putClientProperty(FOLDER_PROPERTY, folder);
        
        SnippetClassGenerator task = new SnippetClassGenerator(p, s, folder, cn);
        RequestProcessor.getDefault().post(task).addTaskListener(e -> finishGeneration(task));
    }
    
    @NbBundle.Messages({
        "# {0} - file name",
        "MSG_CouldNotOpenTargetFile=Could not open generated file {0}.java in editor",
        "# {0} - the reported error message",
        "ERR_GenerationError=Error during class creation: {0}. See the log for the details.",
        "TITLE_GenerationError=Class creation failed"
    })
    private void finishGeneration(SnippetClassGenerator task) {
        if (task.getJavaFile() != null) {
            // open the file
            OpenCookie cake = task.getJavaFile().getLookup().lookup(OpenCookie.class);
            if (cake != null) {
                cake.open();
            } else {
                StatusDisplayer.getDefault().setStatusText(
                        Bundle.MSG_CouldNotOpenTargetFile(task.getJavaFile().getName()));
            }
        }
        
        if (task.getError() != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.ERR_GenerationError(
                task.getError().getLocalizedMessage()),NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }
    
    
    private static final String CLASSNAME_PROPERTY = GenerateClassAction.class.getName() + ".classname"; // NOI18N
    private static final String FOLDER_PROPERTY = GenerateClassAction.class.getName() + ".folder"; // NOI18N
}
