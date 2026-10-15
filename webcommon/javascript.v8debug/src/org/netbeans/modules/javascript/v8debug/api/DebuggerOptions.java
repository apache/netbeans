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

package org.netbeans.modules.javascript.v8debug.api;

import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Options for node.js debugging.
 * 
 * @author Martin Entlicher
 * @deprecated Use {@link org.netbeans.modules.javascript.nodejs.api.DebuggerOptions}
 */
public final class DebuggerOptions {
    
    private static final DebuggerOptions INSTANCE = new DebuggerOptions();
    
    private DebuggerOptions() {
    }
    
    public static DebuggerOptions getInstance() {
        return INSTANCE;
    }
    
    public boolean isLiveEdit() {
        return org.netbeans.modules.javascript.nodejs.api.DebuggerOptions.getInstance().isLiveEdit();
    }
    
    public void setLiveEdit(boolean doLiveEdit) {
        org.netbeans.modules.javascript.nodejs.api.DebuggerOptions.getInstance().setLiveEdit(doLiveEdit);
    }

    /**
     * @since 1.7
     */
    public boolean isBreakAtFirstLine() {
        return org.netbeans.modules.javascript.nodejs.api.DebuggerOptions.getInstance().isBreakAtFirstLine();
    }

    /**
     * @since 1.7
     */
    public void setBreakAtFirstLine(boolean brk1st) {
        org.netbeans.modules.javascript.nodejs.api.DebuggerOptions.getInstance().setBreakAtFirstLine(brk1st);
    }
    
    public void addPreferenceChangeListener(PreferenceChangeListener listener) {
        org.netbeans.modules.javascript.nodejs.api.DebuggerOptions.getInstance().addPreferenceChangeListener(listener);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener listener) {
        org.netbeans.modules.javascript.nodejs.api.DebuggerOptions.getInstance().removePreferenceChangeListener(listener);
    }

}
