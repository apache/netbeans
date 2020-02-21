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

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerInfo;
import org.netbeans.modules.cnd.debugger.dbx.rtc.RtcProfile;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.dbx.options.DbxProfile;
import org.openide.util.lookup.ServiceProvider;


/*
 * Specific version of NativeDebuggerInfo.
 */

public final class DbxDebuggerInfo extends NativeDebuggerInfo {
    private DbxDebuggerInfo() {
	super(DbxEngineCapabilityProvider.getDbxEngineType());
    } 

    private RtcProfile rtcOptions = null;

    /*package*/ RtcProfile getRtcProfile() {
        if (rtcOptions == null) {
            rtcOptions = (RtcProfile) getConfiguration().getAuxObject(RtcProfile.ID);
        }
        return rtcOptions;
    }

    /*package*/ void setRtcProfile(RtcProfile rtcOptions) {
        this.rtcOptions = rtcOptions;
    }
    
    @Override
    protected String getDbgProfileId() {
        return DbxProfile.PROFILE_ID;
    }

    /**
     * additionalArgv is to support the dbx glue protocol clone() message.
     */
    private String additionalArgv[] = null;
    public String[] getAdditionalArgv() {
        return additionalArgv;
    }

    public void setAdditionalArgv(String additionalArgv[]) {
        this.additionalArgv = additionalArgv;
    }


    public String getID() { 
	// See META-INF/services
	// SHOULD this be "netbeans-" or something like "sun-" or what?
	return "netbeans-DbxDebuggerInfo";	// NOI18N
    }

    public static DbxDebuggerInfo create() {
	return new DbxDebuggerInfo();
    }

    @ServiceProvider(service=NativeDebuggerInfo.Factory.class)
    public static final class DbxFactory implements NativeDebuggerInfo.Factory {

        /** public constructor as contract for service providers*/
        public DbxFactory() {
        }

        public NativeDebuggerInfo create(EngineType debuggerType) {
            if (DbxEngineCapabilityProvider.getDbxEngineType().equals(debuggerType)) {
                return DbxDebuggerInfo.create();
            }
            return null;
        }
    }
}
