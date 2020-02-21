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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.props.FunctionSubEventProperty;
import org.netbeans.modules.cnd.debugger.common2.values.FunctionSubEvent;
import org.netbeans.modules.cnd.debugger.common2.utils.props.StringProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

public final class FunctionBreakpoint extends NativeBreakpoint {

    public StringProperty function =
	new StringProperty(pos, "function", null, false, null); // NOI18N
    public StringProperty qfunction =
	new StringProperty(pos, "qfunction", null, false, null); // NOI18N
    /* TMP
    public StringProperty qfunction =
	new StringProperty.Tracking(pos, "qfunction", null, false, null);
    */

    public FunctionSubEventProperty subEvent =
	new FunctionSubEventProperty(pos, "subEvent", null, false, FunctionSubEvent.IN); // NOI18N

    public FunctionBreakpoint(int flags) {
	super(new FunctionBreakpointType(), flags);
    } 

    public void setFunction(String function) {
        if (function == null) {
            function = "<func>"; // NOI18N
        }
	this.function.set(function);
    } 

    public String getFunction() {
	return function.get();
    } 

    public void setQfunction(String qfunc) {
	qfunction.set(qfunc);
    }

    public String getQfunction() {
	return qfunction.get();
    }

    public void setSubEvent(FunctionSubEvent se) {
	subEvent.set(se);
    }

    public FunctionSubEvent getSubEvent() {
	return subEvent.get();
    }

    @Override
    protected final String getSummary() {
	return getFunction();
    } 

    @Override
    protected String getDisplayNameHelp() {
	String summary = null;
	FunctionBreakpoint fb = this;
	FunctionSubEvent se = fb.getSubEvent();
	if (se.equals(FunctionSubEvent.IN)) {
	    summary = fb.getFunction();
	} else if (se.equals(FunctionSubEvent.INFUNCTION)) {
	    summary = Catalog.format("Handler_AllFunc", fb.getFunction()); // NOI18N
	} else if (se.equals(FunctionSubEvent.RETURNS)) {
	    summary = Catalog.format("Handler_ReturnFrom", fb.getFunction()); // NOI18N
	} else {
	    summary = fb.getFunction();
	}
	return summary;
    }

    @Override
    protected void processOriginalEventspec(String oeventspec) {
	assert !IpeUtils.isEmpty(oeventspec);
	this.function.set(oeventspec);
    }
}
