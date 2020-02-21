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
