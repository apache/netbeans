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

import org.netbeans.modules.cnd.debugger.dbx.options.DbxProfile;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineCapability;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineTypeManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.spi.EngineCapabilityProvider;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.DebuggerDescriptor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 *
 */
@ServiceProvider(service=EngineCapabilityProvider.class, position=50)
public final class DbxEngineCapabilityProvider implements EngineCapabilityProvider {
    /*package*/ final static String ID = "dbx"; // NOI18N
    private final static EngineType DBX_ENGINE_TYPE = EngineTypeManager.create(ID, Catalog.get("DbxEngineDisplayName")); // NOI18N
    public boolean hasCapability(EngineType et, EngineCapability capability) {
        if (ID.equals(et.getDebuggerID())) {
            switch (capability) {
                case DERIVE_EXECUTABLE:
                case RTC_SUPPORT:
		case RUN_AUTOSTART:
		case STACK_VERBOSE:
		case STACK_MAXFRAME:
		case DYNAMIC_TYPE:
		case INHERITED_MEMBERS:
		case STATIC_MEMBERS:
		case PRETTY_PRINT:
		case MAX_OBJECT:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    public EngineType engineType() {
        return DBX_ENGINE_TYPE;
    }

    public static EngineType getDbxEngineType() {
        return DBX_ENGINE_TYPE;
    }
    
    /*package*/ static boolean isSupportedImpl(DebuggerDescriptor descriptor) {
        if (descriptor == null) {
            return false;
        }
        final String id = descriptor.getID();
        return "SunStudio".equalsIgnoreCase(id) || "OracleSolarisStudio".equalsIgnoreCase(id);//NOI18N
    }

    public boolean isSupported(DebuggerDescriptor descriptor) {
        return isSupportedImpl(descriptor);
    }

    public String debuggerProfileID() {
        return DbxProfile.PROFILE_ID;
    }
}
