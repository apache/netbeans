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

package org.netbeans.modules.cnd.debugger.common2.debugger.api;

import org.netbeans.modules.cnd.debugger.common2.debugger.options.EngineNodeProp;
import org.netbeans.modules.cnd.debugger.common2.debugger.spi.EngineCapabilityProvider;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.DebuggerDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public final class EngineTypeManager {
    private final static EngineType INHERIT = new EngineType(true, "INHERIT", NbBundle.getMessage(EngineNodeProp.class, "InheritedFromToolCollection"));// NOI18N

    private EngineTypeManager() {
    }

    public static EngineType create(String debuggerID, String displayName) {
        return new EngineType(false, debuggerID, displayName);
    }

    public static EngineType getInherited() {
        return INHERIT;
    }

    public static EngineType getEngineTypeByID(String debuggerID) {
        return getEngineTypeByIDImpl(debuggerID, true);
    }
    
    private static EngineType getEngineTypeByIDImpl(String debuggerID, boolean useFallback) {
        if (debuggerID != null && debuggerID.equalsIgnoreCase(INHERIT.getDebuggerID())) {
            return INHERIT;
        }
        Collection<EngineType> engineTypes = getEngineTypes(false);
        assert !engineTypes.isEmpty() : "at least one engine is expected to be registered";
        if (debuggerID != null) {
            for (EngineType engineType : engineTypes) {
                if (debuggerID.equalsIgnoreCase(engineType.getDebuggerID())) {
                    return engineType;
                }
            }
        }
        if (useFallback) {
            // return the first to prevent "null"
            return engineTypes.iterator().next();
        } else {
            return null;
        }
    }

    public static EngineType getEngineTypeForDebuggerDescriptor(DebuggerDescriptor descriptor) {
        Collection<? extends EngineCapabilityProvider> services = Lookup.getDefault().lookupAll(EngineCapabilityProvider.class);
        for (EngineCapabilityProvider provider : services) {
            if (provider.isSupported(descriptor)) {
                return provider.engineType();
            }
        }
        return null;
    }

    public static EngineType getEngineTypeByDisplayName(String debuggerDispalyName) {
        if (debuggerDispalyName != null && debuggerDispalyName.equals(INHERIT.getDisplayName())) {
            return INHERIT;
        }
        Collection<EngineType> engineTypes = getEngineTypes(false);
        assert !engineTypes.isEmpty() : "at least one engine is expected to be registered";
        if (debuggerDispalyName != null) {
            for (EngineType engineType : engineTypes) {
                if (debuggerDispalyName.equals(engineType.getDisplayName())) {
                    return engineType;
                }
            }
        }
        // return the first to prevent "null"
        return engineTypes.iterator().next();
    }

    public static EngineType getOverrideEngineType() {
        return getEngineTypeByIDImpl(System.getProperty("cnd.nativedebugger"), false);
    }

    public static EngineType getFallbackEnineType() {
        return getEngineTypeByIDImpl(null, true);
    }
    
    public static Collection<EngineType> getEngineTypes(boolean withInherited) {
        Collection<EngineType> out = new ArrayList<EngineType>();
        if (withInherited) {
            out.add(INHERIT);
        }
        Collection<? extends EngineCapabilityProvider> services = Lookup.getDefault().lookupAll(EngineCapabilityProvider.class);
        for (EngineCapabilityProvider provider : services) {
            out.add(provider.engineType());
        }
        return out;
    }

    public static String engine2DebugProfileID(EngineType engine) {
        Collection<EngineType> engineTypes = getEngineTypes(false);
        assert !engineTypes.isEmpty() : "at least one engine is expected to be registered";
        Collection<? extends EngineCapabilityProvider> services = Lookup.getDefault().lookupAll(EngineCapabilityProvider.class);
        for (EngineCapabilityProvider provider : services) {
            if (provider.engineType().equals(engine)) {
                return provider.debuggerProfileID();
            }
        }
        assert false : "unexpected engine " + engine;
        return services.iterator().next().debuggerProfileID();
    }
}
