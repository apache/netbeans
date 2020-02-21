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

import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;

import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.StateListener;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;

/**
 *  PopLastDebuggerCall the current process
 */
public class PopLastDebuggerCallAction
    extends CallableSystemAction implements StateListener {

    /** Generated serial version UID. */
    static final long serialVersionUID = -8705899978543961455L;
    

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
	    debugger.popLastDebuggerCall();
	}
    }
    
    // interface SystemAction
    @Override
    public String getName() {
	return Catalog.get("LBL_PopCall"); // NOI18N
    }
    
    // interface SystemAction
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx ("Welcome_fdide_home"); // NOI18N
    }

    // interface SystemAction
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/cnd/debugger/common2/icons/pop_last_debugger_call.gif"; // NOI18N
    }

    // interface SystemAction
    @Override
    protected void initialize() {
	super.initialize();
	setEnabled(false);
    }

    // interface StateListener
    @Override
    public void update(State state) {
	boolean enable = false;
	if (state != null) {
	    enable = state.isProcess && state.isListening();
	    enable &= state.isDebuggerCall;
	    if (state.isCore)
		enable = false;
	}
	setEnabled(enable);
    }
}
