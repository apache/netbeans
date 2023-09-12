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

    private static final GradleExperimentalSettings INSTANCE = new GradleExperimentalSettings(NbPreferences.forModule(GradleExperimentalSettings.class));
    private final Preferences preferences;

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
        String s = getPreferences().get(PROP_NETWORK_PROXY, NetworkProxySettings.ASK.name());
        try {
            return NetworkProxySettings.valueOf(s);
        } catch (IllegalArgumentException ex) {
            return NetworkProxySettings.ASK;
        }
    }
    
    public void setNetworkProxy(NetworkProxySettings s) {
        getPreferences().put(PROP_NETWORK_PROXY, s.name());
    }
}
