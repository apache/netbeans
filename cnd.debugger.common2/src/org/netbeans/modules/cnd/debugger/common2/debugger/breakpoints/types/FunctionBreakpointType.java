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

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointPanel;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpointType;

//@BreakpointType.Registration(displayName="#LBL_Function")
public class FunctionBreakpointType extends NativeBreakpointType {

    // interface BreakpointType
    @Override
    public NativeBreakpoint newInstance(int flags) {
	return new FunctionBreakpoint(flags);
    }

    // interface BreakpointType
    @Override
    public String getTypeDisplayName() {
	return Catalog.get("LBL_Function"); // NOI18N
    }

    // interface BreakpointType
    @Override
    public boolean isDefault() {
	return true;
    } 

    // interface NativeBreakpointType
    @Override
    public BreakpointPanel getCustomizer(NativeBreakpoint editable) {
	if (editable == null)
	    return new FunctionBreakpointPanel();
	else
	    return new FunctionBreakpointPanel(editable);
    }
    
    @Override
    public String id() {
        return "Function"; //NOI18N
    }
}
