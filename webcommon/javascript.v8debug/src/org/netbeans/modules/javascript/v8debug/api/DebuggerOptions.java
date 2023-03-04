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

package org.netbeans.modules.javascript.v8debug.api;

import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Options for node.js debugging.
 * 
 * @author Martin Entlicher
 */
public final class DebuggerOptions {
    
    private static final String DEBUGGER_PREFS = "nodejs.debugger"; // NOI18N
    private static final String PROP_LIVE_EDIT = "liveEdit";        // NOI18N
    private static final String PROP_BRK_1ST = "breakOn1stLine";    // NOI18N
    
    private static final DebuggerOptions INSTANCE = new DebuggerOptions();
    
    private final Preferences prefs;
    
    private DebuggerOptions() {
        prefs = NbPreferences.forModule(DebuggerOptions.class).node(DEBUGGER_PREFS);
    }
    
    public static DebuggerOptions getInstance() {
        return INSTANCE;
    }
    
    public boolean isLiveEdit() {
        return prefs.getBoolean(PROP_LIVE_EDIT, true);
    }
    
    public void setLiveEdit(boolean doLiveEdit) {
        prefs.putBoolean(PROP_LIVE_EDIT, doLiveEdit);
    }

    /**
     * @since 1.7
     */
    public boolean isBreakAtFirstLine() {
        return prefs.getBoolean(PROP_BRK_1ST, true);
    }

    /**
     * @since 1.7
     */
    public void setBreakAtFirstLine(boolean brk1st) {
        prefs.putBoolean(PROP_BRK_1ST, brk1st);
    }
    
    public void addPreferenceChangeListener(PreferenceChangeListener listener) {
        prefs.addPreferenceChangeListener(listener);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener listener) {
        prefs.removePreferenceChangeListener(listener);
    }

}
