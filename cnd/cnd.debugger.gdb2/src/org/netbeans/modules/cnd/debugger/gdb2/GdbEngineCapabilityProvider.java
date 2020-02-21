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

import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineCapability;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineTypeManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.spi.EngineCapabilityProvider;
import org.netbeans.modules.cnd.debugger.gdb2.options.GdbProfile;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.DebuggerDescriptor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=EngineCapabilityProvider.class, position=100)
public final class GdbEngineCapabilityProvider implements EngineCapabilityProvider {
    /*package*/ final static String ID = "gdb"; // NOI18N
    private final static EngineType GDB_ENGINE_TYPE = EngineTypeManager.create(ID, Catalog.get("GdbEngineDisplayName")); // NOI18N

    @Override
    public boolean hasCapability(EngineType et, EngineCapability capability) {
        if (ID.equals(et.getDebuggerID())) {
            switch (capability) {
                case DERIVE_EXECUTABLE:
                case RTC_SUPPORT:
                    return false;
                case RUN_AUTOSTART:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    public EngineType engineType() {
        return GDB_ENGINE_TYPE;
    }

    public static EngineType getGdbEngineType() {
        return GDB_ENGINE_TYPE;
    }
    
    /*package*/ static boolean isSupportedImpl(DebuggerDescriptor descriptor) {
        if (descriptor == null) {
            return false;
        }
        final String id = descriptor.getID();
        return "GNU".equalsIgnoreCase(id);//NOI18N
    }    

    @Override
    public boolean isSupported(DebuggerDescriptor descriptor) {
        return isSupportedImpl(descriptor);
    }

    @Override
    public String debuggerProfileID() {
        return GdbProfile.PROFILE_ID;
    }
}
