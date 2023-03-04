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
package org.netbeans.modules.java.hints.spiimpl.options;

import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.editor.hints.settings.FileHintPreferences;
import org.netbeans.spi.editor.hints.settings.FileHintPreferences.GlobalHintPreferencesProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.NbPreferences;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Hrebejk
 * @author Jan Lahoda
 */
public abstract class HintsSettings {

    private static final String ENABLED_KEY = "enabled";         // NOI18N
    private static final String OLD_SEVERITY_KEY = "severity";       // NOI18N
    private static final String NEW_SEVERITY_KEY = "hintSeverity";       // NOI18N
//    protected static final String IN_TASK_LIST_KEY = "inTaskList"; // NOI18N

    public abstract boolean isEnabled(HintMetadata hint);
    public abstract void setEnabled(HintMetadata hint, boolean value);
    public abstract Preferences getHintPreferences(HintMetadata hint);
    public abstract Severity getSeverity(HintMetadata hint);
    public abstract void setSeverity(HintMetadata hint, Severity severity);
//    public abstract Iterable<? extends HintDescription> getEnabledHints();
    
    private static final class PreferencesBasedHintsSettings extends HintsSettings {

        private final Preferences preferences;
        private final boolean useDefaultEnabled;
        private final Severity overrideSeverity;

        public PreferencesBasedHintsSettings(Preferences preferences, boolean useDefaultEnabled, Severity overrideSeverity) {
            Parameters.notNull("preferences", preferences);
            this.preferences = preferences;
            this.useDefaultEnabled = useDefaultEnabled;
            this.overrideSeverity = overrideSeverity;
        }

        @Override
        public boolean isEnabled(HintMetadata hint) {
            return getHintPreferences(hint).getBoolean(ENABLED_KEY, useDefaultEnabled && hint.enabled);
        }

        @Override
        public void setEnabled(HintMetadata hint, boolean value) {
            getHintPreferences(hint).putBoolean(ENABLED_KEY, value);
        }

        @Override
        public Preferences getHintPreferences(HintMetadata hint) {
            return preferences.node(hint.id);
        }

        @Override
        public Severity getSeverity(HintMetadata hint) {
            Preferences prefs = getHintPreferences(hint);
            String s = prefs.get(NEW_SEVERITY_KEY, null);
            if (s != null) return Severity.valueOf(s);

            s = prefs.get(OLD_SEVERITY_KEY, null);

            if (s == null) return overrideSeverity != null ? overrideSeverity : hint != null ? hint.severity : null;

            if ("ERROR".equals(s)) return Severity.ERROR;
            else if ("WARNING".equals(s)) return Severity.VERIFIER;
            else if ("CURRENT_LINE_WARNING".equals(s)) return Severity.HINT;

            return overrideSeverity != null ? overrideSeverity : hint != null ? hint.severity : null;
        }
        
        @Override
        public void setSeverity(HintMetadata hint, Severity severity) {
            getHintPreferences(hint).put(NEW_SEVERITY_KEY, severity.name());
        }
    }
    
    public static HintsSettings createPreferencesBasedHintsSettings(Preferences preferences, boolean useDefaultEnabled, Severity overrideSeverity) {
        return new PreferencesBasedHintsSettings(preferences, useDefaultEnabled, overrideSeverity);
    }
    
    public static HintsSettings getSettingsFor(FileObject file) {
        return createPreferencesBasedHintsSettings(FileHintPreferences.getFilePreferences(file, "text/x-java"), true, null);
    }
    
    public static HintsSettings getGlobalSettings() {
        return GLOBAL_SETTINGS;
    }
    
    private static final String DEFAULT_PROFILE = "default"; // NOI18N
    private static final String PREFERENCES_LOCATION = "org/netbeans/modules/java/hints";
    private static final HintsSettings GLOBAL_SETTINGS = createPreferencesBasedHintsSettings(NbPreferences.root().node(PREFERENCES_LOCATION).node(DEFAULT_PROFILE), true, null);
    
    @MimeRegistration(mimeType="text/x-java", service=GlobalHintPreferencesProvider.class)
    public static class GlobalSettingsProvider implements GlobalHintPreferencesProvider {

        @Override
        public Preferences getGlobalPreferences() {
            return NbPreferences.root().node(PREFERENCES_LOCATION).node(DEFAULT_PROFILE);
        }
        
    }
}
