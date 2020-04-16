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
package org.netbeans.modules.terminal.nb;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Additional Options for Terminal Windows.
 * 
 * @since 1.9
 * @author lkishalmi
 */
public final class AdditionalTerminalOptions {

    private static AdditionalTerminalOptions INSTANCE;
    public static final String PROP_SAVE_ALL_ON_FOCUS = "saveAllOnFocus"; //NOI18N

    final Preferences prefs;

    AdditionalTerminalOptions(Preferences prefs) {
        this.prefs = prefs;
    }

    public static AdditionalTerminalOptions getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new AdditionalTerminalOptions(NbPreferences.forModule(AdditionalTerminalOptions.class));
        }
        return INSTANCE;
    }

    public void setSaveAllOnFocus(boolean b) {
        prefs.putBoolean(PROP_SAVE_ALL_ON_FOCUS, b);
    }
    
    public boolean getSaveAllOnFocus() {
        return prefs.getBoolean(PROP_SAVE_ALL_ON_FOCUS, false);
    }
}
