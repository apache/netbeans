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
import org.netbeans.modules.cnd.debugger.common2.utils.props.StringProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

public final class SignalBreakpoint extends NativeBreakpoint {

    public StringProperty signal = 
	new StringProperty(pos, "signal", null, false, null); // NOI18N
    public StringProperty subCode = 
	new StringProperty(pos, "subCode", null, false, null); // NOI18N

    public SignalBreakpoint(int flags) {
	super(new SignalBreakpointType(), flags);
    } 

    public String getSignal() {
	return signal.get();
    }

    public void setSignal(String s) {
	signal.set(s);
    }

    public String getSubcode() {
	return subCode.get();
    }

    public void setSubcode(String sc) {
	subCode.set(sc);
    }

    public String getSummary() {
	return signal.get();
    }

    protected String getDisplayNameHelp() {
	String summary = null;
	SignalBreakpoint bre = this;
	if (bre.getSubcode() != null) {
	    summary = Catalog.format("Handler_Signal_Subcode", // NOI18N
			bre.getSignal(),
			bre.getSubcode());
	} else {
	    summary = Catalog.format("Handler_Signal", bre.getSignal());
	}
	return summary;
    }

    protected void processOriginalEventspec(String oeventspec) {
	assert IpeUtils.isEmpty(oeventspec);
    }
}
