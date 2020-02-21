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

import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.StateListener;

/**
 * Common code for all ActionProvider's in this package.
 */

abstract class NativeActionsProvider
    extends ActionsProviderSupport implements StateListener {

    private ContextProvider ctx;

    public NativeActionsProvider(ContextProvider ctx) {
	this.ctx = ctx;
	if (getDebugger() != null)
	    getDebugger().addStateListener(this);
    }

    // interface ActionsProvider
    // override ActionsProvider
    @Override
    public void postAction (final Object action,
			    final Runnable actionPerformedNotifier) {
	// Comes on the eventQ
	// The default implementation of postAction calls doAction on an RP.
	// We call it in the eventQ.
	try {
	    doAction(action);
	} finally {
	    actionPerformedNotifier.run();
	}
    }
    protected final NativeDebugger getDebugger() {
	// TODO: how do we know this returns the debugger for 
	// the current session?
	if (ctx != null)
	    return ctx.lookupFirst(null, NativeDebugger.class);
	else {
	    NativeDebugger nd = NativeDebuggerManager.get().currentDebugger();
	    return (nd != null) ? nd : null;
	}
    }

    protected NativeDebuggerManager manager() {
	return NativeDebuggerManager.get();
    }
}
