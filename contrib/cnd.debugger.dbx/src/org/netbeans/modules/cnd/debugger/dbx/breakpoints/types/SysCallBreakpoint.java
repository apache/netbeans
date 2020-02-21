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
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.props.SysCallEEProperty;
import org.netbeans.modules.cnd.debugger.common2.values.SysCallEE;
import org.netbeans.modules.cnd.debugger.common2.utils.props.StringProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

/**
 * @deprecated Use the same class from common instead
 */
@Deprecated
public final class SysCallBreakpoint extends NativeBreakpoint {

    public StringProperty sysCall =
	new StringProperty(pos, "sysCall", null, false, null); // NOI18N
    public SysCallEEProperty entryExit =
	new SysCallEEProperty(pos, "entryExit", null, false, SysCallEE.ENTRY); // NOI18N

    public SysCallBreakpoint(int flags) {
	super(new SysCallBreakpointType(), flags);
    } 

    public String getSysCall() {
	return sysCall.get();
    }

    public void setSysCall(String newSysCall) {
	sysCall.set(newSysCall);
    }

    public SysCallEE getEntryExit() {
        return entryExit.get();
    }

    public void setEntryExit(SysCallEE ee) {
        entryExit.set(ee);
    }
    
    public String getSummary() {
	return sysCall.get();
    }

    protected String getDisplayNameHelp() {
	String summary = null;
	SysCallBreakpoint bre = this;
	String call = bre.getSysCall();
	if (call == null) {
	    if (bre.getEntryExit() == SysCallEE.EXIT) {
		summary = Catalog.get("Handler_SysoutAny");
	    } else {
		summary = Catalog.get("Handler_SysinAny");
	    }
	} else {
	    if (bre.getEntryExit() == SysCallEE.EXIT) {
		summary = Catalog.format("Handler_Sysout", call);
	    } else {
		summary = Catalog.format("Handler_Sysin", call);
	    }
	}
	return summary;
    }

    protected void processOriginalEventspec(String oeventspec) {
	assert IpeUtils.isEmpty(oeventspec);
    }
}
