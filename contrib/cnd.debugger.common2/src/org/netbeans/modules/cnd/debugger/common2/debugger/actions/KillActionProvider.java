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

// registered in META-INF/debugger/netbeans-DbxDebuggerEngine
// registered in META-INF/debugger/netbeans-GdbDebuggerEngine

public class KillActionProvider extends NativeActionsProvider {

    public KillActionProvider(ContextProvider ctx) {
	super(ctx);
	if (Log.Action.iz80352) {
	    System.out.println("KillActionProvider.<init>(): ctx =\n" + // NOI18N
		ctx);
	    System.out.println("KillActionProvider.<init>(): debugger =\n" + // NOI18N
		getDebugger());
	}
    }

    /* interface ActionsProvider */
    @Override
    public Set getActions() {
	return Collections.singleton (ActionsManager.ACTION_KILL);
    }

    /* abstract in ActionsProviderSupport */
    @Override
    public void postAction (final Object action,
			    final Runnable actionPerformedNotifier) {
	// Comes on the eventQ
	// See StartAction.postAction.
	// We won't bother with manager.isAsyncStart

	this.actionPerformedNotifier = actionPerformedNotifier;

	// workaround for IZ 80352
	// Multiple ACTION_KILLs get delivered to the same engine
	setEnabled(ActionsManager.ACTION_KILL, false);

	try {
	    doAction(action);
	} finally {
	    // LATER actionPerformedNotifier.run();
	    done();
	}
    }

    private Runnable actionPerformedNotifier;

    public void done() {
	actionPerformedNotifier.run();
    }

    /* abstract in ActionsProviderSupport */
    @Override
    public void doAction(Object action) {
	// IZ 160052 describes how postAction isn't even called.
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
	    public void run() {
		getDebugger().postKill();
	    }
	});
    }

    /* interface NativeActionsProvider */
    @Override
    public void update(State state) {
	boolean enable = state.isLoaded;
	setEnabled(ActionsManager.ACTION_KILL, true);
    }
}
