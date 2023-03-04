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
