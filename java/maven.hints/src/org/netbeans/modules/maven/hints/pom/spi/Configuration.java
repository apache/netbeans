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

package org.netbeans.modules.maven.hints.pom.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbPreferences;

/**
 *
 * @author mkleint
 */
public final class Configuration {
    private final String id;
    private final String description;
    private final boolean defaultEnabled;
    public static final String ENABLED_KEY = "enabled";         // NOI18N
    public static final String SEVERITY_KEY = "severity";       // NOI18N
    static final String IN_TASK_LIST_KEY = "inTaskList"; // NOI18N
    private final HintSeverity defaultSeverity;
    private final String displayName;
    private final Map<String, Object> id2Saved = new HashMap<>();

    public Configuration(String id, String displayName, String description, boolean defaultEnabled, HintSeverity defaultSeverity) {
        this.id = id;
        this.description = description;
        this.defaultEnabled = defaultEnabled;
        this.defaultSeverity = defaultSeverity;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Gets preferences node, which stores the options for given hint. It is not
     * necessary to override this method unless you want to create some special
     * behavior. The default implementation will create the the preferences node
     * by calling <code>NbPreferences.forModule(this.getClass()).node(profile).node(getId());</code>
     * @return Preferences node for given hint.
     */

    public String getId() {
        return id;
    }

    public Preferences getPreferences() {
//        Map<String, Preferences> override = HintsSettings.getPreferencesOverride();
//        if (override != null) {
//            Preferences p = override.get(getId());
//            if (p != null) {
//                return p;
//            }
//        }
        return NbPreferences.forModule(this.getClass()).node(getId()); //NOI18N
    }


    public String getDescription() {
        return description;
    }

    /** Finds out whether the rule is currently enabled.
     * @return true if enabled false otherwise.
     */
    public final boolean isEnabled(Preferences p) {
        boolean enabled = p.getBoolean(ENABLED_KEY, defaultEnabled);
        if(!id2Saved.containsKey(ENABLED_KEY)) {
            id2Saved.put(ENABLED_KEY, enabled);
        }
        return enabled;
    }

    public void setEnabled( Preferences p, boolean value ) {
        p.putBoolean(ENABLED_KEY, value);
    }


    /** Gets current severity of the hint.
     * @return Hints severity in current profile.
     */
    public final HintSeverity getSeverity(Preferences p) {
        String s = p.get(SEVERITY_KEY, null );
        HintSeverity severity = s == null ? defaultSeverity : fromPreferenceString(s);
        if(!id2Saved.containsKey(SEVERITY_KEY)) {
            id2Saved.put(SEVERITY_KEY, severity);
        }
        return severity;
    }


    public void setSeverity( Preferences p, Configuration.HintSeverity severity ) {
        p.put(SEVERITY_KEY, severity.toPreferenceString());
    }
    
    public Object getSavedValue(String key) {
        return id2Saved.get(key);
    }

    /**
     * Resets the saved preference values of this configuration.
     * This should be called once saved or if canceled to be ready for the next round of changes.
     */
    public void resetSavedValues() {
        id2Saved.clear();
    }

    /** Severity of hint
     *  <li><code>ERROR</code>  - will show up as error
     *  <li><code>WARNING</code>  - will show up as warning
     */
    public static enum HintSeverity {
        ERROR,
        WARNING;

        public Severity toEditorSeverity() {
            switch ( this ) {
                case ERROR:
                    return Severity.ERROR;
                case WARNING:
                    return Severity.VERIFIER;
                default:
                    return null;
            }
        }

        public String toPreferenceString() {
            switch ( this ) {
                case ERROR:
                    return "error";
                case WARNING:
                    return "warning";
                default:
                    return null;
            }
        }

    }

    public static HintSeverity fromPreferenceString(String sev) {
        if (sev.equals("error")) {
            return HintSeverity.ERROR;
        }
        if (sev.equals("warning")) {
            return HintSeverity.WARNING;
        }
        throw new IllegalStateException(sev);

    }

}
