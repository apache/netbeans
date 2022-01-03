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

import javax.swing.SwingUtilities;

import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;

import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.StateListener;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.RegistersWindow;

public class RegistersWindowAction extends CallableSystemAction implements StateListener {

    private final String menu_name;
    private final String group_name;

    /** generated Serialized Version UID */
    static final long serialVersionUID = -6814567172958445516L;    

    public RegistersWindowAction() {
	menu_name=Catalog.get("CTL_Registers");			// NOI18N
	group_name=Catalog.get("DEFAULT_MODE_RegistersWindow"); // NOI18N
    }

    // interface SystemAction
    @Override
    public String getName() {
	return menu_name;
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
    
    // interface CallableSystemAction
    @Override
    public void performAction() {
	NativeDebugger debugger = NativeDebuggerManager.get().currentDebugger();

 	if (debugger != null) {
	    if (SwingUtilities.isEventDispatchThread()) {
		RegistersWindow.getDefault().open();
		RegistersWindow.getDefault().requestActive();
	    } else {
		try {
		    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
			public void run() {
			    RegistersWindow.getDefault().open();
			    RegistersWindow.getDefault().requestActive();
			}
		    });
		} catch (Exception e) {
		}
	    }
 	}
    }

    // interface CallableSystemAction
    @Override
    public boolean asynchronous() {
	return false;
    }

    // interface SystemAction
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx ("CTL_Registers");		// NOI18N
    }

    // interface SystemAction
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/cnd/debugger/common2/icons/registers.png"; // NOI18N
    }
}
