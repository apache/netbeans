/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
