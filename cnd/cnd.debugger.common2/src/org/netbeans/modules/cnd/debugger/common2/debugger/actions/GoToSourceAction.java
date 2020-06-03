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

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerImpl;
import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.StateListener;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=StateListener.class)
public class GoToSourceAction extends CallableSystemAction implements StateListener {

    private final String menu_name;

    public GoToSourceAction() {
	menu_name=Catalog.get("Dis_ACT_Go_To_Source"); // NOI18N
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
	if (debugger instanceof NativeDebuggerImpl) {
            ((NativeDebuggerImpl)debugger).showCurrentSource();
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
        return null;
    }

    // interface SystemAction
    @Override
    protected String iconResource() {
        return null;
    }
}
