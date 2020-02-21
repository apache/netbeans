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

/*
 * "RunToFunctionAction.java"
 *
 * Our own version in places of ActionsManager.ACTION_RUN_INTO_METHOD handled
 * by RunIntoMethodActionProvider.
 * The reason we have our own version is that debuggercores doesn't have an
 * icon.
 */

package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.StateListener;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineDescriptor;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineCapability;

public class RunToFunctionAction extends CallableSystemAction implements StateListener {

    public RunToFunctionAction() {
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
            debugger.stepTo(EditorBridge.getCurrentSelection());
        }
    }

    // interface SystemAction
    @Override
    public String getName() {
        return Catalog.get("LBL_RunToFunctionAction"); // NOI18N
    }


    // interface SystemAction
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx ("Welcome_fdide_home"); // NOI18N
    }


    // interface SystemAction
    @Override
    protected String iconResource () {
        return "org/netbeans/modules/cnd/debugger/common2/icons/step_to_function.png"; // NOI18N
// NOI18N
    }


    // interface SystemAction
    @Override
    protected void initialize() {
        super.initialize();
        putValue(SHORT_DESCRIPTION, Catalog.get("TIP_RunToFunctionAction")); // NOI18N
        setEnabled(false);
    }

    // interface StateListener
    @Override
    public void update(State state) {
        boolean enable = false;
        NativeDebugger debugger = NativeDebuggerManager.get().currentDebugger();
        if (debugger == null) {
            setEnabled(false);
            return;
        }
        EngineDescriptor descriptor = debugger.getNDI().getEngineDescriptor();
        if (descriptor.hasCapability(EngineCapability.RUN_AUTOSTART)) {
            enable = state.isListening() && !state.isCore && state.isLoaded;
        } else {
            enable = state.isListening() && !state.isCore && state.isLoaded && state.isProcess ;
        }

        setEnabled(enable);
    }
}
