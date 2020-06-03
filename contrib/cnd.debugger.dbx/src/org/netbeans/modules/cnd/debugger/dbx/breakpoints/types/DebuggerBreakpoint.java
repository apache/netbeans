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

package org.netbeans.modules.cnd.debugger.dbx.breakpoints.types;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.props.DebuggerEventProperty;
import org.netbeans.modules.cnd.debugger.common2.values.DebuggerEvent;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

public final class DebuggerBreakpoint extends NativeBreakpoint {

    public DebuggerEventProperty subEvent =
	new DebuggerEventProperty(pos, "subEvent", null, false, DebuggerEvent.ATTACH); // NOI18N

    public DebuggerBreakpoint(int flags) {
	super(new DebuggerBreakpointType(), flags);
    } 

    public DebuggerEvent getSubEvent() {
	return subEvent.get();
    }

    public void setSubEvent(DebuggerEvent newSubEvent) {
	subEvent.set(newSubEvent);
    }

    public String getSummary() {
	return subEvent.get().toString();
    }

    protected String getDisplayNameHelp() {
	String summary = null;
	DebuggerBreakpoint bre = this;
	summary = Catalog.format("Handler_DebuggerEvent",
		                 bre.getSubEvent().toString());
	return summary;
    }

    protected void processOriginalEventspec(String oeventspec) {
	assert IpeUtils.isEmpty(oeventspec);
    }
}
