/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
