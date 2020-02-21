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
