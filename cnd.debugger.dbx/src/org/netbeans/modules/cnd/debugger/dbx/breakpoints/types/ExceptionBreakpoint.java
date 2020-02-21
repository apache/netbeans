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
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.props.ExceptionSpecProperty;
import org.netbeans.modules.cnd.debugger.common2.values.ExceptionSpec;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

/**
 * @deprecated Use the same class from common instead
 */
@Deprecated
public final class ExceptionBreakpoint extends NativeBreakpoint {

    public ExceptionSpecProperty exception =
	new ExceptionSpecProperty(pos, "exception", null, false, ExceptionSpec.ALL); // NOI18N

    public ExceptionBreakpoint(int flags) {
	super(new ExceptionBreakpointType(), flags);
    } 

    public ExceptionSpec getException() {
	return exception.get();
    }

    public void setException(ExceptionSpec e) {
	exception.set(e);
    }

    public String getSummary() {
	if (exception.get() != null)
	    return exception.get().toString();
	else
	    return "";
    }

    protected String getDisplayNameHelp() {
	String summary = null;
	ExceptionBreakpoint bre = this;
	summary = Catalog.format("Handler_Thrown",
		                 bre.getException().toString());
	return summary;
    }

    protected void processOriginalEventspec(String oeventspec) {
	assert IpeUtils.isEmpty(oeventspec);
    }
}
