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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

import org.netbeans.spi.viewmodel.TreeExpansionModel;

/**
 * Registered, for non-global breakpoints, under
 *	META-INF/debugger/netbeans-DbxDebuggerEngine/BreakpointsView/
 *	META-INF/debugger/netbeans-GdbDebuggerEngine/BreakpointsView/
 * and for global breakpoints under
 *	META-INF/debugger/BreakpointsView/
 * in
 *	org.netbeans.spi.viewmodel.TreeExpansionModel
 *
 * See IZ's:
 * http://www.netbeans.org/issues/show_bug.cgi?id=79940 (need filter!)
 * http://www.netbeans.org/issues/show_bug.cgi?id=79951
 */

public final class BreakpointTreeExpansionModel 
    implements TreeExpansionModel {

    // interface TreeExpansionModel
    @Override
    public boolean isExpanded(Object o) {
	boolean retval = false;
	if (o instanceof NativeBreakpoint) {
	    NativeBreakpoint nb = (NativeBreakpoint) o;
	    retval = nb.isExpanded();
	    if (nb.isToplevel()) {
		/* DEBUG
		System.out.print("BreakpointTreeExpansionModel.isExpanded(): ");
		System.out.println("\t" + retval);
		*/
	    }
	}
	return retval;
    }

    // interface TreeExpansionModel
    @Override
    public void nodeCollapsed(Object o) {
	/* DEBUG
	System.out.print("BreakpointTreeExpansionModel.nodeCollapsed(): ");
	*/
	if (o instanceof NativeBreakpoint) {
	    // DEBUG System.out.println("ours");
	    NativeBreakpoint nb = (NativeBreakpoint) o;
	    nb.setExpanded(false);
	} else {
	    // DEBUG System.out.println("other");
	}
    }

    // interface TreeExpansionModel
    @Override
    public void nodeExpanded(Object o) {
	/* DEBUG
	System.out.print("BreakpointTreeExpansionModel.nodeExpanded(): ");
	*/
	if (o instanceof NativeBreakpoint) {
	    // DEBUG System.out.println("ours");
	    NativeBreakpoint nb = (NativeBreakpoint) o;
	    nb.setExpanded(true);
	} else {
	    // DEBUG System.out.println("other");
	}
    }
}

