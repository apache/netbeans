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
import org.netbeans.modules.cnd.debugger.common2.utils.props.StringProperty;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

public final class InstructionBreakpoint extends NativeBreakpoint {

    public StringProperty instruction =
	new StringProperty(pos, "instruction", null, false, null); // NOI18N

    public InstructionBreakpoint(int flags) {
	super(new InstructionBreakpointType(), flags);
    } 

    public String getAddress() {
	return instruction.get();
    }

    public void setAddress(String in) {
	instruction.set(in);
    }

    @Override
    public String getSummary() {
	return instruction.get();
    }

    @Override
    protected String getDisplayNameHelp() {
	return Catalog.format("Handler_Instruction", getAddress()); // NOI18N
    }

    @Override
    protected void processOriginalEventspec(String oeventspec) {
	assert IpeUtils.isEmpty(oeventspec);
    }
}
