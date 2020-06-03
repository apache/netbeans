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
import org.netbeans.modules.cnd.debugger.common2.utils.props.BooleanProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

public final class ClassMethodBreakpoint extends NativeBreakpoint {

    public StringProperty className =
	new StringProperty(pos, "className", null, false, null); // NOI18N
    public StringProperty qclassName =
	new StringProperty(pos, "qclassName", null, false, null); // NOI18N
    public StringProperty method =
	new StringProperty(pos, "method", null, false, null); // NOI18N
    public StringProperty qmethod =
	new StringProperty(pos, "qmethod", null, false, null); // NOI18N

    // Include methods in base classes?
    public BooleanProperty recurse =
	new BooleanProperty(pos, "recurse", null, false, false); // NOI18N

    public ClassMethodBreakpoint(int flags) {
	super(new ClassMethodBreakpointType(), flags);
    } 

    public void setClassName(String className) {
	this.className.set(className);
    }

    public String getClassName() {
	return className.get();
    } 

    public void setQclassName(String cls) {
	qclassName.set(cls);
    }

    public String getQclassName() {
	return qclassName.get();
    } 

    public void setMethodName(String sc) {
	method.set(sc);
    } 

    public String getMethodName() {
	return method.get();
    } 

    public void setQmethodName(String sc) {
	qmethod.set(sc);
    } 

    public String getQmethodName() {
	return qmethod.get();
    } 

    /** Should we include methods in parent classes? */
    public void setRecurse(boolean r) {
	recurse.set(r);
    }

    /** Should we include methods in parent classes? */
    public boolean isRecurse() {
	return recurse.get();
    }

    protected final String getSummary() {
	return getClassName();
    } 

    protected String getDisplayNameHelp() {
	String summary = null;
	ClassMethodBreakpoint bre = this;
	if (bre.getClassName() != null) {
	    if (bre.getMethodName() != null) {
		// XXX -should- be language sensitive, e.g. "." instead
		// of "::" for Java
		summary = bre.getClassName() + "::" + // NOI18N
		    bre.getMethodName();
	    } else {
		summary = Catalog.format("Handler_AllMeth", bre.getClassName());
	    }
	} else {
	    // we don't allow both to be null
	    summary = Catalog.format("Handler_AllClass", bre.getMethodName());
	}
	return summary;
    }

    protected void processOriginalEventspec(String oeventspec) {
	assert IpeUtils.isEmpty(oeventspec);
    }
}
