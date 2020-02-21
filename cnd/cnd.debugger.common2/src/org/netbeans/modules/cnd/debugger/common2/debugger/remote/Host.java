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
