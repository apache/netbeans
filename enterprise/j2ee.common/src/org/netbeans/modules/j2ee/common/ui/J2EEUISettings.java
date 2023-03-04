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

package org.netbeans.modules.j2ee.common.ui;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

public class J2EEUISettings  {
    private static final J2EEUISettings INSTANCE = new J2EEUISettings();
    private static final String SHOW_AGAIN_BROKEN_REF_ALERT = "showAgainBrokenRefAlert"; // NOI18N
    private static final String SHOW_AGAIN_BROKEN_SERVER_ALERT = "showAgainBrokenServerAlert"; // NOI18N
    private static final String SHOW_AGAIN_BROKEN_DATASOURCE_ALERT = "showAgainBrokenDatasourceAlert"; // NOI18N
    private static final String SHOW_AGAIN_BROKEN_SERVER_LIBS_ALERT = "showAgainBrokenServerLibsAlert"; // NOI18N

    public String displayName() {
        return "J2EEUISettings"; // NOI18N (not shown in UI)
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(J2EEUISettings.class);
    }
    
    public boolean isShowAgainBrokenRefAlert() {
        return getPreferences().getBoolean(SHOW_AGAIN_BROKEN_REF_ALERT, true);
    }
    
    public void setShowAgainBrokenRefAlert(boolean again) {
        getPreferences().putBoolean(SHOW_AGAIN_BROKEN_REF_ALERT, again);
    }
    
    public boolean isShowAgainBrokenServerAlert() {
        return getPreferences().getBoolean(SHOW_AGAIN_BROKEN_SERVER_ALERT, true);
    }
    
    public void setShowAgainBrokenServerAlert(boolean again) {
        getPreferences().putBoolean(SHOW_AGAIN_BROKEN_SERVER_ALERT, again);
    }

    public boolean isShowAgainBrokenServerLibsAlert() {
        return getPreferences().getBoolean(SHOW_AGAIN_BROKEN_SERVER_LIBS_ALERT, true);
    }

    public void setShowAgainBrokenServerLibsAlert(boolean again) {
        getPreferences().putBoolean(SHOW_AGAIN_BROKEN_SERVER_LIBS_ALERT, again);
    }

    public boolean isShowAgainBrokenDatasourceAlert() {
        return getPreferences().getBoolean(SHOW_AGAIN_BROKEN_DATASOURCE_ALERT, true);
    }
    
    public void setShowAgainBrokenDatasourceAlert(boolean again) {
        getPreferences().putBoolean(SHOW_AGAIN_BROKEN_DATASOURCE_ALERT, again);
    }
    
    
    public static J2EEUISettings getDefault() {
        return INSTANCE;
    }
    
}
