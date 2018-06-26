/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.v8debug.ui.callstack.models;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import javax.swing.Action;
import org.netbeans.lib.v8debug.V8Script;
import org.netbeans.modules.javascript.v8debug.ui.EditorUtils;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.V8DebuggerSessionProvider;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.modules.javascript.v8debug.frames.CallStack;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Entlicher
 */
@NbBundle.Messages({
    "CTL_DebuggingActionsModel_Copy2CLBD_Label=Copy Stack",
    "CTL_DebuggingActionsModel_MakeCurrent_Label=Make Current",
    "CTL_DebuggingActionsModel_RestartFrame_Label=Restart Frame"
})
@DebuggerServiceRegistrations({
 @DebuggerServiceRegistration(path=V8DebuggerSessionProvider.SESSION_NAME+"/DebuggingView",
                              types={ NodeActionsProvider.class }),
 @DebuggerServiceRegistration(path=V8DebuggerSessionProvider.SESSION_NAME+"/CallStackView",
                              types={ NodeActionsProvider.class })
})
public class DebuggingActionsModel implements NodeActionsProvider {
    
    private final V8Debugger dbg;
    
    private Action GO_TO_SOURCE;
    private Action MAKE_CURRENT_ACTION = Models.createAction (
        Bundle.CTL_DebuggingActionsModel_MakeCurrent_Label(),
        new Models.ActionPerformer() {
            @Override public boolean isEnabled (Object node) {
                return node != dbg.getCurrentFrame();
            }
            @Override public void perform (Object[] nodes) {
                dbg.setCurrentFrame((CallFrame) nodes [0]);
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    private final Action COPY_TO_CLBD_ACTION = Models.createAction (
            Bundle.CTL_DebuggingActionsModel_Copy2CLBD_Label(),
        new Models.ActionPerformer() {
            @Override public boolean isEnabled (Object node) {
                return dbg.isSuspended();
            }
            @Override public void perform (Object[] nodes) {
                stackToCLBD();
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
    
    public DebuggingActionsModel(final ContextProvider contextProvider) {
        this.dbg = contextProvider.lookupFirst(null, V8Debugger.class);
    }

    @Override
    public void performDefaultAction(Object node) throws UnknownTypeException {
        if (node instanceof CallFrame) {
            CallFrame frame = (CallFrame)node;
            if (frame != dbg.getCurrentFrame()) {
                dbg.setCurrentFrame(frame);
            } else {
                EditorUtils.showFrameLine(dbg, frame, true);
            }
        }
    }

    @Override
    public Action[] getActions(Object node) throws UnknownTypeException {
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
        if (!dbg.isSuspended()) {
            return ;
        }
        StringBuilder frameStr = new StringBuilder(500);
        CallStack stack = dbg.getCurrentCallStack();
        if (stack != null) {
            for (CallFrame frame : stack.getCallFrames()) {
                String thisName = frame.getThisName();
                if ("Object".equals(thisName) || "global".equals(thisName)) {
                    thisName = null;
                }
                if (thisName != null && !thisName.isEmpty()) {
                    frameStr.append(thisName);
                    frameStr.append('.');
                }
                String functionName = frame.getFunctionName();
                frameStr.append(functionName);
                
                String scriptName = DebuggingModel.getScriptName(frame);
                if (scriptName == null) {
                    scriptName = "?";
                }
                frameStr.append(" (");
                frameStr.append(scriptName);
                long line = frame.getFrame().getLine()+1;
                long column = frame.getFrame().getColumn()+1;
                frameStr.append(":");
                frameStr.append(line + 1);
                frameStr.append(":");
                frameStr.append(column + 1);
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
