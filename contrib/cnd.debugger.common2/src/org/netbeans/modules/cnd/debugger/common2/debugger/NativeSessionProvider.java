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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.SessionProvider;

public abstract class NativeSessionProvider extends SessionProvider {

    private final ContextProvider ctx;

    protected NativeSessionProvider(ContextProvider ctx) {
	this.ctx = ctx;
    } 

    // interface SessionProvider
    @Override
    public String getLocationName() {
	// location == host
	return "placeholder-for-NativeSessionProvider.getLocationName()"; // NOI18N
    }

    // interface SessionProvider
    @Override
    public String getSessionName() {
        return "placeholder-for-NativeSessionProvider.getSessionName()"; // NOI18N
//        NativeDebuggerInfo ndi = ctx.lookupFirst(null, NativeDebuggerInfo.class);
//        if (ndi != null) {
//            String target = ndi.getTarget();
//            if (target != null && !target.isEmpty()) {
//                return CndPathUtilities.getBaseName(target);
//            }
//            long pid = ndi.getPid();
//            String name = nameMap.get(pid);
//            if (name != null) {
//                return name;
//            }
//            String strPid = "" + pid; // NOI18N
//            PsProvider.PsData data = PsProvider.getDefault(Host.byName(ndi.getHostName())).getData(false);
//            for (Vector<String> process : data.processes(Pattern.compile(strPid))) {
//                if (process.get(data.pidColumnIdx()).equals(strPid)) {
//                    String command = process.get(data.commandColumnIdx());
//                    int spaceIdx = command.indexOf(" "); // NOI18N
//                    if (spaceIdx != -1) {
//                        command = command.substring(0, spaceIdx);
//                    }
//                    name = CndPathUtilities.getBaseName(command);
//                    nameMap.put(pid, name);
//                    return name;
//                }
//            }
//        }
//	return ""; // NOI18N
    }

    // interface SessionProvider
    @Override
    public abstract String getTypeID();

    // interface SessionProvider
    @Override
    public Object[] getServices() {
	return new Object[0];
    }
}
