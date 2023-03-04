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

package org.netbeans.modules.nashorn.execution.options;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Settings for execution of JavaScript files in Nashorn script engine.
 * 
 * @author Martin Entlicher
 */
public final class Settings {
    
    public static final String PREF_NASHORN_PLATFORM_DISPLAY_NAME = "nashornPlatformDisplayName";   // NOI18N
    public static final String PREF_NASHORN_OPTIONS = "nashornEngineOptions";       // NOI18N
    public static final String PREF_NASHORN_ARGUMENTS = "nashornScriptArguments";   // NOI18N
    public static final String PREF_NASHORN = "nashornIsPreferred";   // NOI18N
    
    private Settings() {}
    
    public static Preferences getPreferences() {
        return NbPreferences.forModule(Settings.class);
    }
    
}
