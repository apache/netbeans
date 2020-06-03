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
import org.netbeans.modules.cnd.debugger.common2.utils.props.FloatProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

public final class TimerBreakpoint extends NativeBreakpoint {

    public FloatProperty seconds =
	new FloatProperty(pos, "seconds", null, false, 1); // NOI18N

    public TimerBreakpoint(int flags) {
	super(new TimerBreakpointType(), flags);
    } 

    public float getSeconds() {
	return seconds.get();
    }

    public void setSeconds(float l) {
	seconds.set(l);
    }

    public String getSummary() {
	return seconds.toString();
    }

    protected String getDisplayNameHelp() {
	String summary = null;
	TimerBreakpoint bre = this;
	summary = Catalog.format("Handler_Timer",
		                 Float.toString(bre.getSeconds()));
	return summary;
    }

    protected void processOriginalEventspec(String oeventspec) {
	assert IpeUtils.isEmpty(oeventspec);
    }
}
