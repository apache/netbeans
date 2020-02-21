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

package org.netbeans.modules.cnd.debugger.common2.debugger.remote;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;

public abstract class Host {
    public static final String localhost = "localhost";		// NOI18N
    
    public abstract ExecutionEnvironment executionEnvironment();
    
    public abstract String getHostName();
    public abstract String getHostLogin();
    public abstract int getPortNum();
    public abstract SecuritySettings getSecuritySettings();
    public abstract String getRemoteStudioLocation();
    
    public abstract boolean isRemote();
    public abstract String getPlatformName();
    
    public static boolean isRemote(String hostName) {
        return !HostInfoUtils.isLocalhost(hostName);
    }
    
    public final boolean isLinux() {
	return getPlatform().isLinux();
    }
    
    public final boolean isSolaris() {
	return getPlatform().isSolaris();
    }
    
    public final Platform getPlatform() {
	String platformname = getPlatformName();

	Platform platform = Platform.byName(platformname);

	if (platform == Platform.Unknown) {
	    platform = Platform.Solaris_Sparc;
        }

	return platform;
    }
    
    public final boolean isLinux64() {
        if (!isLinux()) {
            return false;
        }

        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(executionEnvironment());
            return hostInfo.getOS().getBitness() == HostInfo.Bitness._64;
        } catch (CancellationException ex) {
        } catch (IOException ex) {
        }
        return false;
    }
    
    public static Host byName(String hostName) {
        Host res = null;
        if (NativeDebuggerManager.isStandalone()) {
            CustomizableHostList hostList = NativeDebuggerManager.get().getHostList();
            if (hostList != null) {
                res = hostList.getHostByName(hostName);
                if (res == null) {
                    res = hostList.getHostByDispName(hostName);
                }
            }
        } else {
            res = new ExecHost(ExecutionEnvironmentFactory.fromUniqueID(hostName));
        }
        return res;
    }
    
    public static Host getLocal() {
        if (!NativeDebuggerManager.isStandalone()) {
            return new ExecHost(ExecutionEnvironmentFactory.getLocal());
        } else {
            return byName(localhost);
        }
    }
    
    //
    // A general resource map for associating remote resources with this host
    // A bit like Lookup but we separate test and set in order to forego 
    // instantiation mechanisms.
    // 

    private final Map<Class<?>, Object> resourceMap = new HashMap<Class<?>, Object>();

    @SuppressWarnings("unchecked")
    public <T> T getResource(Class<T> resourceClass) {
	return (T)resourceMap.get(resourceClass);
    }

    public <T> void putResource(Class<T> resourceClass, T resource) {
	resourceMap.put(resourceClass, resource);
    }

    protected void invalidateResources() {
	resourceMap.clear();
    }
}
