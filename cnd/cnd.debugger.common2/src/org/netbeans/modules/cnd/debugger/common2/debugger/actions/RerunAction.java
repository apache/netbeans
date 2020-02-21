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

/**
 * Action which reruns the user's program
 */
public class RerunAction extends CallableSystemAction implements StateListener {

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
	NativeDebugger debugger =
	    NativeDebuggerManager.get().currentNativeDebugger();

	if (debugger != null) {
	    debugger.rerun();
	}
    }
    

    // interface SystemAction
    @Override
    public String getName() {
        // removed call to DebuggerManager.isStandalone() to prevent loading of class
        // standalone tool rebrands LBL_RerunProcess and set text of LBL_RunProcess
        // loading settings here in UI thread is also not good idea, so use Rerun
        return Catalog.get("LBL_RerunProcess"); // NOI18N
    }
    

    // interface SystemAction
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx ("Welcome_fdide_home"); // NOI18N
    }


    private static final String ICON_BASE =
	"org/netbeans/modules/cnd/debugger/common2/icons/";		//NOI18N

    // interface SystemAction
    @Override
    protected String iconResource () {
    /*
	if (DebuggerManager.get().Standalone())
	    return ICON_BASE + "run.png";		// NOI18N
	else
    */
	    return ICON_BASE + "restart.png";		// NOI18N
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
	boolean enable;
        if (!state.isLoaded) {
            enable = false;
        } else {
	    enable = state.isListening();
        }

	setEnabled(enable);
    }
}
