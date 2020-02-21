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

import java.util.Set;
import java.util.Collections;

import org.netbeans.api.debugger.ActionsManager;

import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineCapability;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineDescriptor;

// registered in META-INF/debugger/netbeans-DbxDebuggerEngine
// registered in META-INF/debugger/netbeans-GdbDebuggerEngine

public class ContinueActionProvider extends NativeActionsProvider {

    public ContinueActionProvider(ContextProvider ctx) {
	super(ctx);
    }

    /* interface ActionsProvider */
    @Override
    public Set getActions() {
	return Collections.singleton (ActionsManager.ACTION_CONTINUE);
    }

    /* abstract in ActionsProviderSupport */
    @Override
    public void doAction(Object action) {
	getDebugger().go();
    }

    /* interface NativeActionsProvider */
    @Override
    public void update(State state) {
	boolean enable = false;
	NativeDebugger debugger = getDebugger();
        if (debugger != null && 
                debugger.getNDI().getEngineDescriptor().hasCapability(EngineCapability.RUN_AUTOSTART)) {
//	EngineDescriptor descriptor = debugger.getNDI().getEngineDescriptor();
//	if (descriptor.hasCapability(EngineCapability.RUN_AUTOSTART)) {
	    enable = state.isListening() && !state.isCore && state.isLoaded;
	} else {
	    enable = state.isListening() && !state.isCore && state.isLoaded && state.isProcess ;
	}
	setEnabled(ActionsManager.ACTION_CONTINUE, enable);
    }
}
