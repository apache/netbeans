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

package org.netbeans.modules.cnd.debugger.gdb2.actions;

import java.util.Set;
import java.util.Collections;

import org.netbeans.api.debugger.ActionsManager;

import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb2.GdbDebuggerImpl;
import org.netbeans.modules.cnd.debugger.gdb2.GdbDebuggerInfo;

/**
 * Needs to be registered in
 *	META-INF/debugger/netbeans-GdbDebuggerEngine/
 *		org.netbeans.spi.debugger.ActionsProvider
 */

public class GdbStartActionProvider extends ActionsProvider {

    private final GdbDebuggerImpl debugger;
    private final GdbDebuggerInfo gdi;

    public GdbStartActionProvider(ContextProvider ctx) {
	// typically resolved via 
	//	META-INF/debugger/netbeans-GdbDebuggerEngine/
	//		org.netbeans.modules.cnd.debugger.gdb2.GdbDebuggerImpl

	Object o = ctx.lookupFirst(null, GdbDebuggerImpl.class);

	debugger = (GdbDebuggerImpl) o;

	gdi = ctx.lookupFirst(null, GdbDebuggerInfo.class);
    }

    /* interface ActionsProvider */
    @Override
    public Set getActions() {
	return Collections.singleton (ActionsManager.ACTION_START);
    }

    private static Runnable actionPerformedNotifier = null;

    public static void succeeded() {
        if (actionPerformedNotifier != null) {
            // case of manager.isAsyncStart() == true
            actionPerformedNotifier.run();
            actionPerformedNotifier = null;
        }
    }

    /* interface ActionsProvider */
    @Override
    public void postAction (final Object action,
                            final Runnable actionPerformedNotifier) {

	if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Start.debug) {
	    System.out.printf("gdbStartActionProvider.postAction() on %s\n", // NOI18N
		Thread.currentThread());
	}

	// Gets called on whatever thread DebuggerManager.startDebugging()
	// was called on. 

	NativeDebuggerManager manager = debugger.manager();

	GdbStartActionProvider.actionPerformedNotifier = actionPerformedNotifier;

	// See ../README.startup for an exp of how all this works

	if (NativeDebuggerManager.isAsyncStart()) {

	    // SHOULD disable action so we don't overwrite existing 
	    // this.actionPerformedNotifier with two Start's back-to-back.

	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
		public void run() {
		    doAction(action);
		}
	    } );

	} else {
	    try {
		doAction(action);
	    } finally {
		actionPerformedNotifier.run();
	    }
	}

	/* OLD
        // Comes on the eventQ

        // In 5.x doAction ends up coming on a RP while a setCurrentSession()
        // in debuggercore triggers a session property change notification that
        // has various side-effects that race with debugger startup and
        // cause all kinds of problems.

        DebuggerManager manager = debugger.manager();

        if (manager.isAsyncStart()) {

            // SHOULD disable action so we don't overwrite existing
            // this.actionPerformedNotifier with two Start's back-to-back.
            this.actionPerformedNotifier = actionPerformedNotifier;

            // The following code is just like
            // org.netbeans.spi.debugger.ActionsProviderpostAction
            // We have it duplicated here so we can switch behaviour
            // dynamically based on isAsyncStart()

            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        doAction(action);
                    } finally {
                        // OLD actionPerformedNotifier.run();

                    }
                }
            });

        } else {
            try {
                doAction(action);
            } finally {
                actionPerformedNotifier.run();
            }
        }
	*/
    }

    /* interface ActionsProvider */
    @Override
    public void doAction(Object action) {
	debugger.start(gdi);
    }

    /* interface ActionsProvider */
    @Override
    public boolean isEnabled(Object action) {
	return true;
    }

    /* interface ActionsProvider */
    @Override
    public void addActionsProviderListener (ActionsProviderListener l) {}
    @Override
    public void removeActionsProviderListener (ActionsProviderListener l) {}
}
