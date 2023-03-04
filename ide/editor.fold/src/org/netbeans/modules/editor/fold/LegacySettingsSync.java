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
package org.netbeans.modules.editor.fold;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.settings.storage.api.OverridePreferences;

/**
 * The class is responsible for syncing legacy setting keys in the
 * default ("") mimetype. It is also responsible for erasing 
 * folds from a MIME, if the setting for 'override' is not set. This will
 * gradually clean up migrated settings.
 * 
 * @author sdedic
 */
class LegacySettingsSync implements PreferenceChangeListener {
    // logging to catch issue #231362
    private static final Logger PREF_LOG = Logger.getLogger(FoldHierarchy.class.getName() + ".enabled");
    
    private static LegacySettingsSync INSTANCE;
    
    private Reference<Preferences> defaultMimePrefs;
    
    static synchronized LegacySettingsSync get() {
        if (INSTANCE == null) {
            INSTANCE = new LegacySettingsSync();
        }
        return INSTANCE;
    }
    
    synchronized Preferences processMime(String mime) {
        Preferences prefs = MimeLookup.getLookup(mime).lookup(Preferences.class);
        if ("".equals(mime)) { // NOI18N
            Preferences p = defaultMimePrefs == null ? null : defaultMimePrefs.get();
            if (p == prefs) {
                return prefs;
            } else if (p != null) {
                p.removePreferenceChangeListener(this);
            }
            // sync the default values for legacy code
            syncKey(FoldType.MEMBER.code(), prefs);
            syncKey(FoldType.NESTED.code(), prefs);
            syncKey(FoldType.DOCUMENTATION.code(), prefs);
            defaultMimePrefs = new WeakReference(prefs);
            // no weak listener, this instance lives forever, but the defaultMimePrefs
            // reference allows the pref to expire.
            prefs.addPreferenceChangeListener(this);
        } else {
            if (!(prefs instanceof OverridePreferences)) {
                return prefs;
            }
            if (((OverridePreferences)prefs).isOverriden(FoldUtilitiesImpl.PREF_OVERRIDE_DEFAULTS)) {
                // there's a local override, not present in legacy NB, exit
                return prefs;
            }
            processMime("");
            boolean state = prefs.getBoolean(FoldUtilitiesImpl.PREF_OVERRIDE_DEFAULTS, false);
            if (!state) {
                cleanupPreferences(mime, prefs);
            } else {
                clonePreferences(mime, prefs);
            }
        }
        return prefs;
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey() == null || !evt.getKey().startsWith(FoldUtilitiesImpl.PREF_COLLAPSE_PREFIX)) {
            return;
        }
        String k = evt.getKey().substring(FoldUtilitiesImpl.PREF_COLLAPSE_PREFIX.length());
        syncKey(k, evt.getNode());
    }
    
    private void syncKey(String k, Preferences pref) {
        // keep the method in sync with fold options dialog for the "" mimetype (DefaultFoldingOptions in editor.fold.nbui
        String l;
        if (FoldType.MEMBER.code().equals(k)) {
            l = "method"; // NOI18N
        } else if (FoldType.NESTED.code().equals(k)) {
            l = "innerclass"; // NOI18N
        } else if (FoldType.DOCUMENTATION.code().equals(k)) {
            l = "javadoc"; // NOI18N
        } else {
            return;
        }
        String syncKey = FoldUtilitiesImpl.PREF_COLLAPSE_PREFIX + l;
        pref.putBoolean(syncKey, pref.getBoolean(
                FoldUtilitiesImpl.PREF_COLLAPSE_PREFIX + k, false));
    }
    
    private void clonePreferences(String mime, Preferences pref) {
        Collection<? extends FoldType> types = FoldUtilities.getFoldTypes(mime).values();
        for (FoldType ft : types) {
            String key = FoldUtilitiesImpl.PREF_COLLAPSE_PREFIX + ft.code();
            if (!isDefinedLocally(pref, key)) {
                boolean val = pref.getBoolean(key, 
                    ft.parent() == null ? false :
                    pref.getBoolean(FoldUtilitiesImpl.PREF_COLLAPSE_PREFIX + ft.parent().code(), false));
                pref.putBoolean(key, val);
            }
        }
    }
    
    private boolean isDefinedLocally(Preferences pref, String key) {
        return pref instanceof OverridePreferences && 
                ((OverridePreferences)pref).isOverriden(key);
    }
    
   private void cleanupPreferences(String mime, Preferences pref) {
        Collection<? extends FoldType> types = FoldUtilities.getFoldTypes(mime).values();
        String parent = MimePath.parse(mime).getInheritedType();
        if (parent == null) {
            return;
        }
        if (types.isEmpty()) {
            // legacy language, do not erase its settings
            return;
        }
        Preferences pprefs = MimeLookup.getLookup(parent).lookup(Preferences.class);
        for (FoldType ft : types) {
            String key = FoldUtilitiesImpl.PREF_COLLAPSE_PREFIX + ft.code();
            if (isDefinedLocally(pref, key)) {
                if (pprefs.get(key, null) != null) {
                    pref.remove(key);
                }
            }
        }
    }
}
