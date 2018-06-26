/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.web.javascript.debugger.callstack;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.List;

import javax.swing.Action;

import org.netbeans.api.project.Project;
import org.netbeans.modules.web.javascript.debugger.MiscEditorUtil;
import org.netbeans.modules.javascript2.debug.ui.models.ViewModelSupport;
import org.netbeans.modules.web.javascript.debugger.browser.ProjectContext;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.modules.web.webkit.debugging.api.debugger.Script;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

@NbBundle.Messages({
    "CTL_CallstackAction_Copy2CLBD_Label=Copy Stack",
    "CTL_CallstackAction_MakeCurrent_Label=Make Current",
    "CTL_CallstackAction_RestartFrame_Label=Restart Frame"
})
@DebuggerServiceRegistration(path="javascript-debuggerengine/CallStackView", types={ NodeActionsProvider.class })
public final class CallStackActionsModel extends ViewModelSupport implements 
        NodeActionsProvider {

    private Debugger debugger;
    private ProjectContext pc;

    private Action GO_TO_SOURCE;
    private Action MAKE_CURRENT_ACTION = Models.createAction (
        Bundle.CTL_CallstackAction_MakeCurrent_Label(),
        new Models.ActionPerformer() {
            @Override public boolean isEnabled (Object node) {
                return node != debugger.getCurrentCallFrame();
            }
            @Override public void perform (Object[] nodes) {
                debugger.setCurrentCallFrame((CallFrame) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    /*
    private Action RESTART_FRAME_ACTION = Models.createAction (
        Bundle.CTL_CallstackAction_RestartFrame_Label(),
        new Models.ActionPerformer() {
            @Override public boolean isEnabled (Object node) {
                return node != null;
            }
            @Override public void perform (Object[] nodes) {
                final CallFrame frame = (CallFrame) nodes [0];
                RP.post(new Runnable() {
                    @Override public void run() {
                        debugger.restartFrame(frame);
                    }
                });
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    */
    private final Action COPY_TO_CLBD_ACTION = Models.createAction (
            Bundle.CTL_CallstackAction_Copy2CLBD_Label(),
        new Models.ActionPerformer() {
            @Override public boolean isEnabled (Object node) {
                return debugger.isSuspended();
            }
            @Override public void perform (Object[] nodes) {
                stackToCLBD();
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );

    private RequestProcessor RP = new RequestProcessor(CallStackActionsModel.class.getName());
    
    public CallStackActionsModel(final ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, Debugger.class);
        pc = contextProvider.lookupFirst(null, ProjectContext.class);
        GO_TO_SOURCE = MiscEditorUtil.createDebuggerGoToAction(pc, debugger);
    }

    // NodeActionsProvider implementation ......................................

    @Override
    public void performDefaultAction(Object node)
            throws UnknownTypeException {
        if (node instanceof CallFrame) {
            CallFrame frame = (CallFrame)node;
            if (frame != debugger.getCurrentCallFrame()) {
                debugger.setCurrentCallFrame(frame);
            } else {
                Project project = pc.getProject();
                Line line = MiscEditorUtil.getLine(debugger, project, frame.getScript(),
                                                   frame.getLineNumber(), frame.getColumnNumber());
                MiscEditorUtil.showLine(line, true);
            }
        }
    }

    @Override
    public Action[] getActions(Object node)
            throws UnknownTypeException {
        if (node instanceof CallFrame ) {
            return new Action [] {
                MAKE_CURRENT_ACTION,
                GO_TO_SOURCE,
                //RESTART_FRAME_ACTION,
                COPY_TO_CLBD_ACTION,
            };
        } else {
            return new Action[]{
                COPY_TO_CLBD_ACTION,
            };
        }
    }
    
    private void stackToCLBD() {
        if (!debugger.isSuspended()) {
            return ;
        }
        StringBuilder frameStr = new StringBuilder(50);
        List<CallFrame> stack = debugger.getCurrentCallStack();
        if (stack != null) {
            for (CallFrame frame : stack) {
                String functionName = frame.getFunctionName();
                if (functionName.isEmpty()) {
                    functionName = "(anonymous function)";
                }
                frameStr.append(functionName);
                Script script = frame.getScript();
                String sourceName;
                if (script != null) {
                    sourceName = script.getURL().toString();
                    int sourceNameIndex = sourceName.lastIndexOf('/');
                    if (sourceNameIndex > 0) {
                        sourceName = sourceName.substring(sourceNameIndex + 1);
                    }
                } else {
                    sourceName = "?";
                }
                frameStr.append(" (");
                frameStr.append(sourceName);
                int line = frame.getLineNumber();
                if (line > 0) {
                    frameStr.append(":");
                    frameStr.append(line + 1);
                }
                frameStr.append(")\n");
            }
        }
        Clipboard systemClipboard = getClipboard();
        Transferable transferableText = new StringSelection(frameStr.toString());
        systemClipboard.setContents(transferableText, null);
    }
        
    private static Clipboard getClipboard() {
        Clipboard clipboard = org.openide.util.Lookup.getDefault().lookup(Clipboard.class);
        if (clipboard == null) {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        return clipboard;
    }

}
