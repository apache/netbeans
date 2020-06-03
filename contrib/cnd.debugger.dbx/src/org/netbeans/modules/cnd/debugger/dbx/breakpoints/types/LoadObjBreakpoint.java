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
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.props.DlEventProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.props.StringProperty;
import org.netbeans.modules.cnd.debugger.common2.values.DlEvent;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

public final class LoadObjBreakpoint extends NativeBreakpoint {

    public StringProperty loadObj =
	new StringProperty(pos, "loadObj", null, false, null); // NOI18N
    public DlEventProperty dlEvent =
	new DlEventProperty(pos, "dlEvent", null, false, DlEvent.OPEN); // NOI18N

    public LoadObjBreakpoint(int flags) {
	super(new LoadObjBreakpointType(), flags);
    } 

    public String getLoadObj() {
	return loadObj.get();
    }

    public void setLoadObj(String newLoadObj) {
	loadObj.set(newLoadObj);
    }
    
    public DlEvent getDlEvent() {
        return dlEvent.get();
    }

    public void setDlEvent(DlEvent newDlEvent) {
        dlEvent.set(newDlEvent);
    }

    public String getSummary() {
	return loadObj.get();
    }

    protected String getDisplayNameHelp() {
	String summary = null;
	LoadObjBreakpoint bre = this;
	String obj = bre.getLoadObj();
	if (obj == null) {
	    if (bre.getDlEvent() == DlEvent.OPEN) {
		summary = Catalog.get(
		    "Handler_OpenAllObj"); // NOI18N
	    } else {
		summary = Catalog.get(
		    "Handler_CloseAllObj"); // NOI18N
	    }
	}
	return summary;
    }

    protected void processOriginalEventspec(String oeventspec) {
	assert IpeUtils.isEmpty(oeventspec);
    }
}
