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

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.StateListener;

public final class PioWindowAction extends CallableSystemAction implements StateListener {

    /** generated Serialized Version UID */
    static final long serialVersionUID = -6814567172958445516L;    

    // interface CallableSystemAction
    @Override
    public void performAction() {
	NativeDebuggerManager.get().enablePioWindow();
    }

    // interface SystemAction
    @Override
    public String getName() {
	    return Catalog.get("CTL_Pio"); // NOI18N
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
            if (!state.isProcess && !state.capabAutoRun) {
                enable = false;
            } else {
                enable = state.isListening();
            }
        }

        setEnabled(enable);
    }
    
    // interface SystemAction
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("CTL_Pio"); // NOI18N
    }


    // interface SystemAction
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/cnd/debugger/common2/icons/process_io.png"; // NOI18N
    }

    // interface CallableSystemAction
    @Override
    protected boolean asynchronous() {
	return false;
    }
}
