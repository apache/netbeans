/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
