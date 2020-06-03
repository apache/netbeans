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

public final class FaultBreakpoint extends NativeBreakpoint {

    public StringProperty fault =
	new StringProperty(pos, "fault", null, false, null); // NOI18N

    public FaultBreakpoint(int flags) {
	super(new FaultBreakpointType(), flags);
    } 

    public String getFault() {
	return fault.get();
    }

    public void setFault(String newFault) {
	fault.set(newFault);
    }

    public String getSummary() {
	return fault.get();
    }

    protected String getDisplayNameHelp() {
	String summary = null;
	FaultBreakpoint bre = this;
	summary = Catalog.format("Handler_Fault", bre.getFault());
	return summary;
    }

    protected void processOriginalEventspec(String oeventspec) {
	assert IpeUtils.isEmpty(oeventspec);
    }
}
