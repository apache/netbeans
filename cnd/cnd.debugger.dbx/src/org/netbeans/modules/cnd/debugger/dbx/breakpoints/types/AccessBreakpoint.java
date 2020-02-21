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
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.props.AccessBAProperty;
import org.netbeans.modules.cnd.debugger.common2.values.AccessBA;
import org.netbeans.modules.cnd.debugger.common2.utils.props.StringProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.props.BooleanProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

public final class AccessBreakpoint extends NativeBreakpoint {

    public StringProperty address = 
	new StringProperty(pos, "address", null, false, null); // NOI18N
    public StringProperty size = 
	new StringProperty(pos, "size", null, false, null); // NOI18N
    public BooleanProperty read = 
	new BooleanProperty(pos, "read", null, false, false); // NOI18N
    public BooleanProperty write = 
	new BooleanProperty(pos, "write", null, false, true); // NOI18N
    public BooleanProperty execute = 
	new BooleanProperty(pos, "execute", null, false, false); // NOI18N
    public AccessBAProperty when =
	new AccessBAProperty(pos, "when", null, false, AccessBA.BEFORE); // NOI18N

    public AccessBreakpoint(int flags) {
	super(new AccessBreakpointType(), flags);
    } 

    public String getAddress() {
	return address.get();
    }

    public void setAddress(String newAddress) {
	address.set(newAddress);
    }

    public String getSize() {
	return size.get();
    }

    public void setSize(String newSize) {
	size.set(newSize);
    }

    public boolean isRead() {
	return read.get();
    }

    public void setRead(boolean newRead) {
	read.set(newRead);
    }
    
    public boolean isWrite() {
	return write.get();
    }

    public void setWrite(boolean newWrite) {
	write.set(newWrite);
    }

    public boolean isExecute() {
	return execute.get();
    }

    public void setExecute(boolean newExecute) {
	execute.set(newExecute);
    }

    public AccessBA getWhen() {
        return when.get();
    }

    public void setWhen(AccessBA ba) {
	when.set(ba);
    }

    public String getSummary() {
	return address.toString();
    }

    protected String getDisplayNameHelp() {
	String summary = null;
	AccessBreakpoint bre = this;
	StringBuffer sb = new StringBuffer(30);
	if (bre.getWhen() == AccessBA.BEFORE) {
	    sb.append(Catalog.get("Handler_Before")); // NOI18N
	} else {
	    sb.append(Catalog.get("Handler_After")); // NOI18N
	}
	if (bre.isRead()) {
	    sb.append(' ');
	    sb.append(Catalog.get("Handler_read")); // NOI18N
	}
	if (bre.isWrite()) {
	    sb.append(' ');
	    sb.append(Catalog.get("Handler_write")); // NOI18N
	}
	if (bre.isExecute()) {
	    sb.append(' ');
	    sb.append(Catalog.get("Handler_execute")); // NOI18N
	}
	sb.append(' ');
	sb.append(bre.getAddress());
	if (bre.getSize() != null) {
	    sb.append(',');
	    sb.append(bre.getSize());
	}
	summary = sb.toString();
	return summary;
    }

    protected void processOriginalEventspec(String oeventspec) {
	assert IpeUtils.isEmpty(oeventspec);
    }
}
