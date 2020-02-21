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
import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.StateListener;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.EvaluationWindow;

/**
 * Action which "stepi" one assembly instruction
 */
public class EvaluateAction extends CallableSystemAction implements StateListener {

    private NativeDebugger debugger;

    static final long serialVersionUID = -8705899978543961455L;

    public EvaluateAction() {
        debugger = NativeDebuggerManager.get().currentDebugger();
        if (debugger != null)
            debugger.addStateListener(this);
    }

    // interface CallableSystemAction
    @Override
    protected boolean asynchronous() {
	return false;
    } 

    // interface CallableSystemAction
    @Override
    public void performAction() {
	NativeDebugger debugger = NativeDebuggerManager.get().currentDebugger();
	if (debugger != null) {
            // 6574620
            String selectedStr = EditorBridge.getCurrentSelection();
            selectedStr = (selectedStr == null ? "" : selectedStr); // NOI18N
            //bz#248470
            //we are in EDT thread already, no need to use SqingUtilitites
            EvaluationWindow evalWindow = EvaluationWindow.getDefault();
            evalWindow.open();
            evalWindow.requestActive();
            evalWindow.componentShowing();
            evalWindow.exprEval(selectedStr);
	}

    }
    

    // interface SystemAction
    @Override
    public String getName() {
	return Catalog.get("LBL_EvaluateAction"); // NOI18N
    }
    

    // interface SystemAction
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx ("Welcome_fdide_home"); // NOI18N
    }


    // interface SystemAction
    @Override
    protected String iconResource () {
        return "org/netbeans/modules/cnd/debugger/common2/icons/" +		// NOI18N
	       "evaluate_expression.png";			// NOI18N
    }


    // interface SystemAction
    @Override
    protected void initialize() {
	super.initialize();
	// 6640192
	setEnabled(false);
    }    

    // interface StateListener
    @Override
    public void update(State state) {
	if (state == null) {
	    setEnabled(false);
	} else {
	    setEnabled(state.isLoaded && state.isListening());
	}
    }
}
