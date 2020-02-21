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

package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorContextBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.StateListener;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.DisassemblyUtils;


public class RunToCursorInstAction extends CallableSystemAction implements StateListener {

    private NativeDebugger debugger;

    public RunToCursorInstAction() {
        debugger = NativeDebuggerManager.get().currentDebugger();
        if (debugger != null)
            debugger.addStateListener(this);
    }

    // interface CallableSystemAction
    @Override
    protected boolean asynchronous() {
	return false;
    } 
    
    static void runToDisLine(int lineNo) {
        String address = DisassemblyUtils.getLineAddress(lineNo);
        if (address == null || address.isEmpty()) {
            return;
        }
        NativeDebugger debugger = NativeDebuggerManager.get().currentDebugger();
	if (debugger == null) {
	    return;
        }
	debugger.runToCursorInst(address);
    }

    // interface CallableSystemAction
    @Override
    public void performAction() {
        int lineNo = EditorContextBridge.getCurrentLineNumber();
        if (lineNo < 0) {
            return;
        }
	runToDisLine(lineNo);
    }
    

    // interface SystemAction
    @Override
    public String getName() {
	return Catalog.get("LBL_RunToCursorInstAction"); // NOI18N
    }
    

    // interface SystemAction
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx ("Welcome_fdide_home"); // NOI18N
    }


    // interface SystemAction
    @Override
    protected String iconResource () {
        return "org/netbeans/modules/cnd/debugger/common2/icons/run_to_cursor_instruction.png"; // NOI18N
    }


    // interface SystemAction
    @Override
    protected void initialize() {
	super.initialize();
	putValue(SHORT_DESCRIPTION, Catalog.get("TIP_RunToCursorInstAction")); // NOI18N
	setEnabled(false);
    }    

    // interface StateListener
    @Override
    public void update(State state) {
	if (state == null) {
	    setEnabled(false);
	} else {
	    setEnabled(state.isLoaded && state.isListening() && !state.isCore);
	}
    }
}
