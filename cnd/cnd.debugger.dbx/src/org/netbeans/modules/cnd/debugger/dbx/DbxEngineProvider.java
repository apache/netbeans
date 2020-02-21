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

package org.netbeans.modules.cnd.debugger.dbx;

import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeEngineProvider;

public class DbxEngineProvider extends NativeEngineProvider {

    public DbxEngineProvider(ContextProvider ctx) {
	super(ctx);
    } 

    // interface DebuggerEngineProvider
    public String [] getLanguages() {
	return new String[] {"All languages supported by dbx"}; // NOI18N
    }

    // interface DebuggerEngineProvider
    public String getEngineTypeID() {

	// "folder" in which startDebugging() will look for 
	//	org.netbeans.spi.debugger.ActionsProvider
	// in order to issue Actionsmanager.doAction(ACTION_START) which goes
	// to our StartAction which calls DbxDebuggerImpl.start ...

	return "netbeans-DbxDebuggerEngine"; // NOI18N
    }
}
