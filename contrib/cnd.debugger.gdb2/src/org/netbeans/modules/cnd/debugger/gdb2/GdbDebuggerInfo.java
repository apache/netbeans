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

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerInfo;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;
import org.netbeans.modules.cnd.debugger.gdb2.options.GdbProfile;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.openide.util.lookup.ServiceProvider;

/*
 * Specific version of NativeDebuggerInfo.
 */

public final class GdbDebuggerInfo extends NativeDebuggerInfo {
    
    static{
        GdbDebuggerInfoAccessor.setDefault(new GdbDebuggerInfoAccessorImpl());
    }
    private String targetCommand = null;

    private GdbDebuggerInfo() {
	super(GdbEngineCapabilityProvider.getGdbEngineType());
    } 

    @Override
    public String getID() { 
	// See META-INF/services
	// SHOULD this be "netbeans-" or something like "sun-" or what?
	return "netbeans-GdbDebuggerInfo";	// NOI18N
    }

    @Override
    protected String getDbgProfileId() {
        return GdbProfile.PROFILE_ID;
    }

    private static GdbDebuggerInfo create() {
	return new GdbDebuggerInfo();
    }
    
    public String getTargetCommand() {
        return targetCommand;
    }

    public void setTargetCommand(String targetCommand) {
        this.targetCommand = targetCommand;
    }

    @ServiceProvider(service = NativeDebuggerInfo.Factory.class)
    public static final class GdbFactory implements NativeDebuggerInfo.Factory {

        /** public constructor as contract for service providers*/
        public GdbFactory() {
        }

        @Override
        public NativeDebuggerInfo create(EngineType debuggerType) {
            if (GdbEngineCapabilityProvider.getGdbEngineType().equals(debuggerType)) {
                return GdbDebuggerInfo.create();
            }
            return null;
        }
    }
    
    private static class GdbDebuggerInfoAccessorImpl extends GdbDebuggerInfoAccessor {

        @Override
        public GdbDebuggerInfo create(DebugTarget dt, String hostName, Configuration conf, int action, String targetCommand) {
            GdbDebuggerInfo gdi = GdbDebuggerInfo.create();
            gdi.setDebugTarget(dt);
            gdi.setHostName(hostName); //NOI18N
            gdi.setConfiguration(conf);
            gdi.setAction(action);
            gdi.setTargetCommand(targetCommand);
            return gdi;
        }
    
    }
}
