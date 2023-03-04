/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.versioning.system.cvss.installer;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author ondra
 */
class CvsInstallerModuleConfig {
    private static CvsInstallerModuleConfig instance;
    private static final String KEY_DISMISSED = "dismissed"; //NOI18N
    private static final String KEY_CVS_INSTALLED = "cvs.installed"; //NOI18N

    static CvsInstallerModuleConfig getInstance () {
        if (instance == null) {
            instance = new CvsInstallerModuleConfig();
        }
        return instance;
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(CvsInstallerModuleConfig.class);
    }
    
    boolean isIgnored () {
        return getPreferences().getBoolean(KEY_DISMISSED, false);
    }

    void setIgnored (boolean ignored) {
        getPreferences().putBoolean(KEY_DISMISSED, ignored);
    }

    boolean isCvsInstalled () {
        return getPreferences().getBoolean(KEY_CVS_INSTALLED, false);
    }

    void setCvsInstalled (boolean installed) {
        getPreferences().putBoolean(KEY_CVS_INSTALLED, installed);
    }
    
}
