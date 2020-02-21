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
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorContextBridge;

import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineCapability;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineDescriptor;
import org.netbeans.modules.cnd.debugger.common2.debugger.assembly.Disassembly;


public class RunToCursorActionProvider extends NativeActionsProvider {

    public RunToCursorActionProvider(ContextProvider ctx) {
	super(ctx);
    }

    /* interface ActionsProvider */
    @Override
    public Set getActions() {
	return Collections.singleton (ActionsManager.ACTION_RUN_TO_CURSOR);
    }

    /* abstract in ActionsProviderSupport */
    @Override
    public void doAction(Object action) {
	int lineNo = EditorContextBridge.getCurrentLineNumber();
        if (lineNo < 0) {
            return;
        }
        if (Disassembly.isInDisasm()) {
            RunToCursorInstAction.runToDisLine(lineNo);
        } else {
            String fileName = EditorContextBridge.getCurrentFilePath();
            if (fileName.trim().equals("")) {
                return;
            }
            getDebugger().runToCursor(fileName, lineNo);
        }
    }
    
    /* interface NativeActionsProvider */
    @Override
    public void update(State state) {
        boolean enable = false;
        NativeDebugger debugger = getDebugger();
        EngineDescriptor descriptor = debugger.getNDI().getEngineDescriptor();
        if (descriptor.hasCapability(EngineCapability.RUN_AUTOSTART)) {
	    enable = state.isListening() && !state.isCore && state.isLoaded;
        } else {
	    enable = state.isListening() && !state.isCore && state.isLoaded && state.isProcess;
	}

	setEnabled(ActionsManager.ACTION_RUN_TO_CURSOR, enable);
    }
}
