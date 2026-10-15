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
package org.netbeans.modules.gradle.options;

import java.util.prefs.Preferences;
import org.netbeans.modules.gradle.spi.execute.JavaRuntimeManager;
import org.netbeans.modules.gradle.spi.execute.JavaRuntimeManager.JavaRuntime;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 *
 * @author lkishalmi
 */
public final class GradleExperimentalSettings {
    public static final String PROP_DISABLE_CACHE = "disableCache";
    public static final String PROP_LAZY_OPEN_GROUPS = "lazyOpen";
    public static final String PROP_BUNDLED_LOADING = "bundledLoading";
    public static final String PROP_NETWORK_PROXY = "networkProxy";
    public static final String PROP_JAVA_RUNTIME_ID = "javaRuntimeId";

    private static final GradleExperimentalSettings INSTANCE = new GradleExperimentalSettings(NbPreferences.forModule(GradleExperimentalSettings.class));
    private final Preferences preferences;

    /**
     * Specifies how should be proxies handled by default, if no setting is given.
     */
    private static final String SYSPROP_DEFAULT_PROXY_BEHAVIOUR = "netbeans.networkProxy";
    
    private static final NetworkProxySettings DEFAULT_PROXY_BEHAVIOUR;
    
    static {
        NetworkProxySettings def;
        try {
            def = NetworkProxySettings.valueOf(System.getProperty(SYSPROP_DEFAULT_PROXY_BEHAVIOUR, NetworkProxySettings.ASK.name()).toUpperCase());
        } catch (IllegalArgumentException e) {
            def = NetworkProxySettings.ASK;
        }
        DEFAULT_PROXY_BEHAVIOUR = def;
    }

    public static GradleExperimentalSettings getDefault() {
        return INSTANCE;
    }

    GradleExperimentalSettings(Preferences preferences) {
        this.preferences = preferences;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void setOpenLazy(boolean b) {
        getPreferences().putBoolean(PROP_LAZY_OPEN_GROUPS, b);
    }

    public boolean isOpenLazy() {
        return getPreferences().getBoolean(PROP_LAZY_OPEN_GROUPS, false);
    }

    public void setCacheDisabled(boolean b) {
        getPreferences().putBoolean(PROP_DISABLE_CACHE, b);
    }

    public boolean isCacheDisabled() {
        return getPreferences().getBoolean(PROP_DISABLE_CACHE, false);
    }

    public void setBundledLoading(boolean b) {
        getPreferences().putBoolean(PROP_BUNDLED_LOADING, b);
    }

    public boolean isBundledLoading() {
        return getPreferences().getBoolean(PROP_BUNDLED_LOADING, false);
    }
    
    public NetworkProxySettings getNetworkProxy() {
        String s = getPreferences().get(PROP_NETWORK_PROXY, DEFAULT_PROXY_BEHAVIOUR.name());
        try {
            return NetworkProxySettings.valueOf(s);
        } catch (IllegalArgumentException ex) {
            return DEFAULT_PROXY_BEHAVIOUR;
        }
    }
    
    public void setNetworkProxy(NetworkProxySettings s) {
        getPreferences().put(PROP_NETWORK_PROXY, s.name());
    }

    public JavaRuntime getDefaultJavaRuntime() {
        String id = getPreferences().get(PROP_JAVA_RUNTIME_ID, null);
        JavaRuntimeManager mgr = Lookup.getDefault().lookup(JavaRuntimeManager.class);
        JavaRuntime ret = mgr.getAvailableRuntimes().get(id);
        ret = ret != null ? ret : mgr.getAvailableRuntimes().get(JavaRuntimeManager.DEFAULT_RUNTIME_ID);
        return ret ;
    }
    
    public void setDefaultJavaRuntime(JavaRuntime rt) {
        if (rt != null) {
            getPreferences().put(PROP_JAVA_RUNTIME_ID, rt.getId());
        } else {
            getPreferences().remove(PROP_JAVA_RUNTIME_ID);
        }
        
    }
}
