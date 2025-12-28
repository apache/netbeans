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
package org.netbeans.modules.java.hints.spiimpl.options;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
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

            return switch (s) {
                case "ERROR" -> Severity.ERROR;
                case "WARNING" -> Severity.VERIFIER;
                case "CURRENT_LINE_WARNING" -> Severity.HINT;
                default -> overrideSeverity != null ? overrideSeverity : hint != null ? hint.severity : null;
            };
        }
        
        @Override
        public void setSeverity(HintMetadata hint, Severity severity) {
            getHintPreferences(hint).put(NEW_SEVERITY_KEY, severity.name());
        }
    }
    
    public static HintsSettings createPreferencesBasedHintsSettings(Preferences preferences, boolean useDefaultEnabled, Severity overrideSeverity) {
        return new PreferencesBasedHintsSettings(
            new HintPreferencesCache(preferences), useDefaultEnabled, overrideSeverity
        );
    }
    
    public static HintsSettings getSettingsFor(FileObject file) {
        return createPreferencesBasedHintsSettings(FileHintPreferences.getFilePreferences(file, "text/x-java"), true, null);
    }
    
    public static HintsSettings getGlobalSettings() {
        return GLOBAL_SETTINGS;
    }
    
    private static final String DEFAULT_PROFILE = "default"; // NOI18N
    private static final String PREFERENCES_LOCATION = "org/netbeans/modules/java/hints";
    private static final HintsSettings GLOBAL_SETTINGS = new PreferencesBasedHintsSettings(
            NbPreferences.root().node(PREFERENCES_LOCATION).node(DEFAULT_PROFILE), true, null
    );
    
    @MimeRegistration(mimeType="text/x-java", service=GlobalHintPreferencesProvider.class)
    public static class GlobalSettingsProvider implements GlobalHintPreferencesProvider {

        @Override
        public Preferences getGlobalPreferences() {
            return NbPreferences.root().node(PREFERENCES_LOCATION).node(DEFAULT_PROFILE);
        }
        
    }

    /// Caches properties on read while writing to the delegate directly.
    /// Partial implementation. Only intended to be used for hints, since their context is one time use only.
    /// Without caching, some frequently invoked hints were seen to spend half of their time blocking on Mutex#writeAccess.
    /// @see org.netbeans.modules.java.hints.spiimpl.hints.HintsInvoker
    /// and {@link HintsSettings#getSettingsFor(org.openide.filesystems.FileObject)} usages
    private static final class HintPreferencesCache extends Preferences {
    
        @SuppressWarnings("RedundantStringConstructorCall")
        private final static String MASKED_NULL = new String("MASKED_NULL");
        
        private final Preferences delegate;
        private final Map<String, String> cache;
        private final Map<String, HintPreferencesCache> nodes;
        private final Preferences parent;
        
        public HintPreferencesCache(Preferences delegate) {
            this(null, delegate);
        }
        
        public HintPreferencesCache(Preferences parent, Preferences delegate) {
            this.parent = parent;
            this.delegate = delegate;
            this.cache = new HashMap<>();
            this.nodes = new HashMap<>();
        }

        private static String maskNull(String value) {
            return value == null ? MASKED_NULL : value;
        }

        @Override
        public Preferences node(String pathName) {
            return nodes.computeIfAbsent(pathName, pn -> new HintPreferencesCache(this, delegate.node(pn)));
        }

        @Override
        public Preferences parent() {
            return parent != null ? parent : delegate.parent();
        }

        @Override
        @SuppressWarnings("StringEquality")
        public String get(String key, String def) {
            String ret = cache.computeIfAbsent(key, k -> {
                String val = delegate.get(k, def);
                return maskNull(val); // cache null as internal token
            });
            return ret == MASKED_NULL ? def : ret;
        }

        @Override
        @SuppressWarnings("StringEquality")
        public boolean getBoolean(String key, boolean def) {
            String ret = cache.computeIfAbsent(key, k -> Boolean.toString(delegate.getBoolean(k, def)));
            return ret == MASKED_NULL ? def : Boolean.parseBoolean(ret); 
        }

        @Override
        public void put(String key, String value) {
            cache.put(key, maskNull(value));
            delegate.put(key, value);
        }

        @Override
        public void putBoolean(String key, boolean value) {
            cache.put(key, Boolean.toString(value));
            delegate.putBoolean(key, value);
        }

        @Override
        public void remove(String key) {
            delegate.remove(key);
            cache.remove(key);
        }

        @Override
        public void clear() throws BackingStoreException {
            cache.clear();
            nodes.clear();
            delegate.clear();
        }

        
        // rest isn't cached and simply delegates

        @Override
        public int getInt(String key, int def) {
            return delegate.getInt(key, def);
        }

        @Override
        public void putLong(String key, long value) {
            delegate.putLong(key, value);
        }

        @Override
        public long getLong(String key, long def) {
            return delegate.getLong(key, def);
        }

        @Override
        public void putFloat(String key, float value) {
            delegate.putFloat(key, value);
        }

        @Override
        public float getFloat(String key, float def) {
            return delegate.getFloat(key, def);
        }

        @Override
        public byte[] getByteArray(String key, byte[] def) {
            return delegate.getByteArray(key, def);
        }

        @Override
        public void putDouble(String key, double value) {
            delegate.putDouble(key, value);
        }

        @Override
        public double getDouble(String key, double def) {
            return delegate.getDouble(key, def);
        }

        @Override
        public void putInt(String key, int value) {
            delegate.putInt(key, value);
        }

        @Override
        public void putByteArray(String key, byte[] value) {
            delegate.putByteArray(key, value);
        }

        @Override
        public String[] keys() throws BackingStoreException {
            return delegate.keys();
        }

        @Override
        public String[] childrenNames() throws BackingStoreException {
            return delegate.childrenNames();
        }

        @Override
        public boolean nodeExists(String pathName) throws BackingStoreException {
            return delegate.nodeExists(pathName);
        }

        @Override
        public void removeNode() throws BackingStoreException {
            delegate.removeNode();
        }

        @Override
        public String name() {
            return delegate.name();
        }

        @Override
        public String absolutePath() {
            return delegate.absolutePath();
        }

        @Override
        public boolean isUserNode() {
            return delegate.isUserNode();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public void flush() throws BackingStoreException {
            delegate.flush();
        }

        @Override
        public void sync() throws BackingStoreException {
            delegate.sync();
        }

        @Override
        public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
            delegate.addPreferenceChangeListener(pcl);
        }

        @Override
        public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
            delegate.removePreferenceChangeListener(pcl);
        }

        @Override
        public void addNodeChangeListener(NodeChangeListener ncl) {
            delegate.addNodeChangeListener(ncl);
        }

        @Override
        public void removeNodeChangeListener(NodeChangeListener ncl) {
            delegate.removeNodeChangeListener(ncl);
        }

        @Override
        public void exportNode(OutputStream os) throws IOException, BackingStoreException {
            delegate.exportNode(os);
        }

        @Override
        public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {
            delegate.exportSubtree(os);
        }

    }
}
