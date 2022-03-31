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

package org.netbeans.modules.cnd.debugger.common2.debugger.remote;

import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.AuthenticationUtils;

/**
 *
 */
class ExecHost extends Host {
    private final ExecutionEnvironment exEnv;
    private final String hostName;
    private final String platform;

    public ExecHost(ExecutionEnvironment exEnv) {
        this.exEnv = exEnv;
        this.hostName = ExecutionEnvironmentFactory.toUniqueID(exEnv);

        //Convert platform
        PlatformInfo platformInfo = PlatformInfo.getDefault(exEnv);
        int platformx = platformInfo.getPlatform();
        this.platform = platformByCNDId(platformx).name();
    }

    @Override
    public ExecutionEnvironment executionEnvironment() {
        return exEnv;
    }

    @Override
    public String getHostLogin() {
        return exEnv.getUser();
    }

    @Override
    public String getHostName() {
        return exEnv.getHost();
    }

    @Override
    public SecuritySettings getSecuritySettings() {
        return new SecuritySettings(exEnv.getSSHPort(), AuthenticationUtils.getSSHKeyFileFor(exEnv));
    }

    @Override
    public String getPlatformName() {
        return platform;
    }

    @Override
    public int getPortNum() {
        return exEnv.getSSHPort();
    }

    @Override
    public String getRemoteStudioLocation() {
        CompilerSetManager csm = CompilerSetManager.get(exEnv);

        // Try Studio compilers first, then default
        CompilerSet cs = csm.getCompilerSet("OracleSolarisStudio"); //NOI18N
        if (cs == null) {
            cs = csm.getCompilerSet("SunStudio"); //NOI18N
        }
        if (cs == null) {
            cs = csm.getDefaultCompilerSet();
        }

        if (cs != null) {
            String base = cs.getDirectory();
            if (Log.Remote.host)
                System.out.printf("hostFromName() base %s\n", base); // NOI18N
            return base + "/.."; // NOI18N
        } else {
            // explicitly set to null so we don't end up with the
            // default value.
            return null;
            // Executor will fall back an a non glue-based provider
        }
    }

    @Override
    public boolean isRemote() {
        return exEnv.isRemote();
    }
    
    private static Platform platformByCNDId(int id) {
	switch (id) {
	    case PlatformTypes.PLATFORM_LINUX:
		return Platform.Linux_x86;

	    case PlatformTypes.PLATFORM_SOLARIS_INTEL:
		return Platform.Solaris_x86;

	    case PlatformTypes.PLATFORM_SOLARIS_SPARC:
		return Platform.Solaris_Sparc;

	    case PlatformTypes.PLATFORM_MACOSX:
		return Platform.MacOSX_x86;

	    case PlatformTypes.PLATFORM_WINDOWS:
		return Platform.Windows_x86;

	    default:
		return Platform.Unknown;
	}
    }

    @Override
    public String toString() {
        return "ExecHost: " + hostName; //NOI18N
    }
}
