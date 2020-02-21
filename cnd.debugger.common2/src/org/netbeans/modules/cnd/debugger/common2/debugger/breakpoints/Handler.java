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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

import org.netbeans.modules.cnd.debugger.common2.utils.ListMapItem;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;


/**
 * A Proxy for the Handler object in dbx or a break object in gdb.
 * 
 * Mediates actions and updates on a handler-by-handler basis between
 * the Handler object in dbx and our extensions to Breakpoint.
 *
 * The state is only allowed to change due to messages from the engine.
 * Any change requests from the gui (should) begin with 'post' and
 * all they do is post a message to the engine via the 'engine' object we
 * keep a handle on.
 *
 * was: IpeHandler
 *
 * History:
 * Under sierra/orion IpeHandler and IBE both (as in redundantly) had
 * state that was cached. In mercury/vulcan we moved away from that with
 * the intent of having IpeBreakpointEvent, now Breakpoint, be the sole
 * keeper of state.
 * Also see comment near update().
 */

public final class Handler implements ListMapItem {
    private NativeDebugger debugger;	// back pointer to engine
    private NativeBreakpoint breakpoint;// debuggercore object
					// was: 'IpeHandler.event'

    private boolean isFired = false;
    private int id;

    private String error;	// ... of "broken" breakpoints

    public Handler(NativeDebugger debugger, NativeBreakpoint breakpoint) {
	assert breakpoint != null;
	assert debugger != null;

	this.debugger = debugger;
	this.breakpoint = breakpoint;

	breakpoint.setHandler(this);
	breakpoint.bindTo(debugger);

	// Pushed to caller (HandlerExpert implementations) so we have an
	// id before we using anything.
	// OLD update();
    }

    private static NativeDebuggerManager manager() {
	return NativeDebuggerManager.get();
    } 

    public NativeBreakpoint breakpoint() {
	return breakpoint;
    } 


    /** 
     * Track the error message associated with the breakpoint when it was
     * restored or made defunct.
     */
    public void setError(String error) {
        this.error = error;
        updateAndParent();
    }

    public String getError() {
        return error;
    }


    public void setId(int id) {
	this.id = id;
	breakpoint.setId(id);
	update();
    }

    public int getId() {
	return id;
    }

    // interface Mappable
    @Override
    public boolean hasKey() {
	return id > 0;
    }

    // interface Mappable
    @Override
    public Object getKey() {
	return id;
    }


    public boolean isFired() {
	return isFired;
    } 

    public void setFired(boolean fired) {
	if (isFired == fired)
	    return;
	isFired = fired;
	updateAndParent();	// cause a pull
    } 


    public void setDefunct(boolean on) {
	// we'll be receiving a "why" from dbx LATER
	if (on)
	    setError("defunct"); // NOI18N
	else
	    setError(null);
    }


    /**
     * Post an enable request to the engine.
     */
    public void postEnable(boolean enabled, int routingToken) {
	if (Log.Bpt.enabling)
	    System.out.println(">  H postEnable(" + enabled + ")"); // NOI18N
	if (! breakpoint().hasHandler()) {
	    setEnabled(enabled);
	} else {
	    debugger.bm().provider().postEnableHandler(routingToken, getId(), enabled);
	}
    } 

    /**
     * Commit to having this handler be enabled.
     */
    public void setEnabled(boolean enabled) {
	if (Log.Bpt.enabling)
	    System.out.println("<  H setEnabled(" + enabled + ")"); // NOI18N

	breakpoint.setEnabled(enabled);
    }


    /**
     * Commit to this count
     */
    public void setCount(int count ) {
	breakpoint.setCount(count);
	update();	// cause a pull
    }

    /**
     * Commit to this countLimit
     */
    public void setCountLimit(int limit, boolean b ) {
	breakpoint.setCountLimit(limit, b);
	update();	// cause a pull
    }


    /**
     * Eliminate various resources used by this handler.
     * disconnected it from the rest of the world.
     */

    public void cleanup() {
	breakpoint.cleanup();
	// disconnect bpt from us
	breakpoint().setHandler(null);
	// SHOULD we automatically remove the subBreakpoint?
    } 


    private boolean inProgress;


    /**
     * true if state of handler is changing, usually because information
     * is in transit between the IDE and the engine.
     */

    public boolean isInProgress() {
	return inProgress;
    } 


    /**
     * was: IpeHandler.refresh()
     * was: Handler.update()
     */
    private void update() {
	if (id == 0)
	    return;
	breakpoint().update();
    }

    private void updateAndParent() {
	if (id == 0)
	    return;
	breakpoint().updateAndParent();
    }


    /**
     * Called on NewBreakpoint action.
     */

    public static void postNewHandler(NativeDebugger debugger,
				      NativeBreakpoint newBpt,
				      int routingToken) {

	// always add annotations to the toplevel bpt
	newBpt.seedToplevelAnnotations();

	if (debugger != null) {
            if (!debugger.isConnected()) {
                // debugger is not yet connected - just do nothing
                return;
            }
	    // we have a master/validator debugger, so route bpt
	    // creation through it
	    // SHOULD use isValidatable()?
	    HandlerCommand cmd = debugger.bm().provider().handlerExpert().commandFormNew(newBpt);
	    debugger.bm().postCreateHandler(routingToken, cmd, newBpt);

	} else {
	    manager().breakpointBag().add(newBpt);
	    manager().bringDownDialog();
	}
    }


    /**
     * Called on Customize action.
     *
     * Customizer may clone a NativeBreakpoint:
     * - Both original and cloned NativeBreakpoint point to the same Handler.
     * - Handler points to the cloned NativeBreakpoint.
     * We're called in that Handler.
     */

    public static void postChange(NativeDebugger debugger,
			      NativeBreakpoint targetBreakpoint,
			      NativeBreakpoint editedBreakpoint,
			      Gen gen) {

	assert editedBreakpoint.isEditable() :
	       "Handler.postChange(): " + // NOI18N
	       "changing a NativeBreakpoint which isn't editable"; // NOI18N

	HandlerCommand cmd = debugger.bm().provider().handlerExpert().
	    commandFormCustomize(editedBreakpoint, targetBreakpoint);

	if (targetBreakpoint.isBroken()) {
	    debugger.bm().postRepairHandler(editedBreakpoint,
				       cmd,
				       targetBreakpoint,
				       gen);
	} else {
	    debugger.bm().postChangeHandler(editedBreakpoint,
				       cmd,
				       targetBreakpoint,
				       gen);
	}
    }
}
