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

package org.netbeans.modules.cnd.debugger.gdb2;

import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeSessionProvider;

public class GdbSessionProvider extends NativeSessionProvider {

    private final GdbDebuggerInfo ddi;

    public GdbSessionProvider(ContextProvider ctx) {
	super(ctx);
	ddi = ctx.lookupFirst(null, GdbDebuggerInfo.class);
    } 

    // interface SessionProvider
    @Override
    public String getTypeID() {
	// "folder" in which lookup will look for 
	// org.netbeans.spi.debugger.DebuggerEngineProvider
	return "netbeans-GdbSession"; // NOI18N
    }
}
