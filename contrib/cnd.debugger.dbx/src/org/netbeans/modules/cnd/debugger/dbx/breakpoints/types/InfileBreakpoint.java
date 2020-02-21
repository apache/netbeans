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

public final class InfileBreakpoint extends NativeBreakpoint {

    public StringProperty fileName =
	new StringProperty(pos, "fileName", null, false, ""); // NOI18N

    public InfileBreakpoint(int flags) {
	super(new InfileBreakpointType(), flags);
    } 

    public String getFileName() {
	return fileName.get();
    }

    public void setFileName(String fileName) {
	if (IpeUtils.sameString(this.fileName.toString(), fileName))
	    return;
	this.fileName.set(fileName);
    }

    public String getSummary() {
	return Catalog.format("CTL_Infile_event_name", getFileName());
    }

    protected String getDisplayNameHelp() {
	String summary = null;
	InfileBreakpoint bre = this;
	summary = Catalog.format("Handler_Infile", bre.getFileName());
	return summary;
    }

    protected void processOriginalEventspec(String oeventspec) {
	assert IpeUtils.isEmpty(oeventspec);
    }
}
