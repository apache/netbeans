/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
