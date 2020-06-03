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
