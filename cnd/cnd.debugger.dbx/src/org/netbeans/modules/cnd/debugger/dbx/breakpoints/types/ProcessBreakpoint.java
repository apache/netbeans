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
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.props.ProcessEventProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.props.StringProperty;
import org.netbeans.modules.cnd.debugger.common2.values.ProcessEvent;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

public final class ProcessBreakpoint extends NativeBreakpoint {

    public StringProperty exitCode =
	new StringProperty(pos, "exitCode", null, false, null); // NOI18N
    public ProcessEventProperty subEvent =
	new ProcessEventProperty(pos, "subEvent", null, false, ProcessEvent.EXIT); // NOI18N

    public ProcessBreakpoint(int flags) {
	super(new ProcessBreakpointType(), flags);
    } 

    public String getExitCode() {
	return exitCode.get();
    }

    public void setExitCode(String newExitCode) {
	exitCode.set(newExitCode);
    }
    
    public ProcessEvent getSubEvent() {
        return subEvent.get();
    }

    public void setSubEvent(ProcessEvent pe) {
        subEvent.set(pe);
    }

    public String getSummary() {
	return exitCode.get();
    }

    protected String getDisplayNameHelp() {
	String summary = null;
	ProcessBreakpoint bre = this;
	summary = Catalog.format("Handler_ProcessEvent",
		                 bre.getSubEvent().toString());
	return summary;
    }

    protected void processOriginalEventspec(String oeventspec) {
	assert IpeUtils.isEmpty(oeventspec);
    }
}
