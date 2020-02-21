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

package org.netbeans.modules.cnd.debugger.dbx.actions;

import javax.swing.SwingUtilities;

import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;

import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.StateListener;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.dbx.arraybrowser.ArrayBrowserWindow;

public class ArrayBrowserWindowAction extends CallableSystemAction implements StateListener {

    String menu_name;
    String group_name;

    public ArrayBrowserWindowAction() {
	menu_name=Catalog.get("CTL_ArrayBrowserWindow"); // NOI18N
	group_name=Catalog.get("DEFAULT_MODE_ArrayBrowserWindow"); // NOI18N
    }

    // interface SystemAction
    public String getName() {
	return menu_name;
    }

    // interface SystemAction
    public boolean isEnabled() {
        NativeDebugger debugger = NativeDebuggerManager.get().currentDebugger();
        return debugger != null;
    }

    // interface SystemAction
    protected void initialize() {
	super.initialize();
	setEnabled(false);
    }

    // interface StateListener
    public void update(State state) {
        boolean enable;
        if (!state.isLoaded) {
            enable = false;
        } else {
            if (!state.isProcess && !state.capabAutoRun)
                enable = false;
            else
                enable = state.isListening();
        }

        setEnabled(enable);
    }


    // interface CallableSystemAction
    public void performAction() {
	NativeDebugger debugger = NativeDebuggerManager.get().currentDebugger();
	if (debugger != null) {
	    if (SwingUtilities.isEventDispatchThread()) {
		ArrayBrowserWindow.getDefault().open();
		ArrayBrowserWindow.getDefault().requestActive();
	    } else {
		try {
		    SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
			    ArrayBrowserWindow.getDefault().open();
			    ArrayBrowserWindow.getDefault().requestActive();
			}
		    });
		} catch (Exception e) {
		}
	    }
 	}
    }

    // interface CallableSystemAction
    public boolean asynchronous() {
	return false;
    }

    // interface SystemAction
    public HelpCtx getHelpCtx() {
        return new HelpCtx ("CTL_ArrayBrowserWindow"); // NOI18N
    }

    // interface SystemAction
    protected String iconResource() {
	// ijc FIXUP: create arraybrowser.png file
        return "org/netbeans/modules/cnd/debugger/common2/icons/arraybrowser.png"; // NOI18N
    }
}
