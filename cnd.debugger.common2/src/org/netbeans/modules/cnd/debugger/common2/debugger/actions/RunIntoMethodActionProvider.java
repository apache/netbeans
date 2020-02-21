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
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;

public class RunIntoMethodActionProvider extends NativeActionsProvider {

    public RunIntoMethodActionProvider(ContextProvider ctx) {
	super(ctx);
    }

    /* interface ActionsProvider */
    @Override
    public Set getActions() {
	return Collections.singleton (ActionsManager.ACTION_RUN_INTO_METHOD);
    }

    /* abstract in ActionsProviderSupport */
    @Override
    public void doAction(Object action) {
	getDebugger().stepTo(EditorBridge.getCurrentSelection());
    }

    /* interface NativeActionsProvider */
    @Override
    public void update(State state) {
	boolean enable = state.isListening() && !state.isCore && state.isLoaded;
	setEnabled(ActionsManager.ACTION_RUN_INTO_METHOD, enable);
    }
}
