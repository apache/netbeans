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
package org.netbeans.modules.editor.fold.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.api.MemoryPreferences;
import org.netbeans.modules.editor.settings.storage.api.OverridePreferences;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 * Controller for the Folding options tab.
 * The controller manages the MemoryPreferences storage, which is then passed to individual folding
 * customizer panels. It also tracks 'dirty' status of all the language panels: if one of them becomes dirty,
 * the whole page starts to report dirty = true.
 * <p/>
 * This class manages only the language switch + overall 'enable folding' setting. All settings
 * for a given language are handled by the language-specific panel.
 * <p/>
 * Threading: applyChanges and preferenceChange notifications run in their own dedicated threads. All other accesses
 * run in EDT (another thread). Access to 'preferences' Map must be synchronized; Preference instances are thread-safe.
 * 
 * @author sdedic
 */
@OptionsPanelController.SubRegistration(
    displayName="org.netbeans.modules.editor.fold.ui.Bundle#CTL_OptionsDisplayName",
    keywords="org.netbeans.modules.editor.fold.ui.Bundle#KW_Options",
    keywordsCategory="Editor/Folding",
    id="Folding", // XXX used anywhere?
    location=OptionsDisplayer.EDITOR,
    position=110
//    toolTip="org.netbeans.modules.options.editor.Bundle#CTL_General_ToolTip"
)
public class FoldOptionsController extends OptionsPanelController implements PreferenceChangeListener {
    // logging to catch issue #231362
    private static final Logger PREF_LOG = Logger.getLogger(FoldHierarchy.class.getName() + ".enabled");
    static final Map<String, String> LEGACY_SETTINGS_MAP = new HashMap<String, String>();
    
    /**
     * The main panel.
     */
    private FoldOptionsPanel panel;
    
    /**
     * True, if some of the created panels became dirty
     */
    private volatile boolean changed;
    
    /**
     * for firing PROP_DIRTY
     */
    private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

    /**
     * Preferences created for individual MIME types, as they are displaye by the user. 
     */
    // @GuardedBy(this)
    private final Map<String, MemoryPreferences>    preferences = new HashMap<String, MemoryPreferences>();
    
    static {
        // keep this in sync with LegacySettingMap in Fold implementation.
        LEGACY_SETTINGS_MAP.put(FoldType.MEMBER.code(), "method"); // NOI18N
        LEGACY_SETTINGS_MAP.put(FoldType.NESTED.code(), "innerclass"); // NOI18N
        LEGACY_SETTINGS_MAP.put(FoldType.DOCUMENTATION.code(), "javadoc"); // NOI18N
    }
    
    @Override
    public void update() {
        clearContents();
        initLanguages();
        if (panel != null) {
            panel.update();
        }
        changed = false;
    }
    
    private Collection<String> updatedLangs = Collections.emptySet();
    private Collection<String> legacyLangs  = Collections.emptySet();
    
    private void initLanguages() {
        Set<String> mimeTypes = EditorSettings.getDefault().getAllMimeTypes();
        
        Collection<String> legacy = new HashSet<String>();
        Collection<String> updated = new HashSet<String>();
        
        for (String s : mimeTypes) {
            if (FoldUtilities.getFoldTypes(s).values().isEmpty()) {
                legacy.add(s);
            } else {
                updated.add(s);
            }
        }
        this.updatedLangs = updated;
        this.legacyLangs = legacy;
    }
    
    public Collection<String> getUpdatedLanguages() {
        return updatedLangs;
    }
    
    private static String[] LEGACY_SETTINGS = {
        "import", "initial-comment", "innerclass", "javadoc", "method", "tags"
    };
    
    @Override
    public void applyChanges() {
        Map<String, MemoryPreferences> copy;
        synchronized (preferences) {
            changed = false;
            copy = new HashMap<>(preferences);
        }
        for (String s : copy.keySet()) {
            MemoryPreferences p = copy.get(s);
            try {
                if (PREF_LOG.isLoggable(Level.FINE)) {
                    if ((p.getPreferences() instanceof OverridePreferences) &&
                        ((OverridePreferences)p.getPreferences()).isOverriden(FoldUtilitiesImpl.PREF_CODE_FOLDING_ENABLED)) {
                        PREF_LOG.log(Level.FINE, "Setting fold enable: {0} = {1}", new Object[] {
                            s, p.getPreferences().get(FoldUtilitiesImpl.PREF_CODE_FOLDING_ENABLED, null)
                        });
                    }
                }
                if ("".equals(s)) {
                    // first remove the legacy keys from the map:
                    for (String k : LEGACY_SETTINGS_MAP.values()) {
                        p.getPreferences().remove(FoldUtilitiesImpl.PREF_COLLAPSE_PREFIX + k);
                    }
                }
                p.getPreferences().flush();
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        Preferences basePrefs = MimeLookup.getLookup("").lookup(Preferences.class);
        // backward compatibility: for each non-updated MIME type, copy over preferences from the "" mime:
        for (String mime : legacyLangs) {
            Preferences prefs = MimeLookup.getLookup(mime).lookup(Preferences.class);
            
            prefs.putBoolean(FoldUtilitiesImpl.PREF_CODE_FOLDING_ENABLED, 
                basePrefs.getBoolean(FoldUtilitiesImpl.PREF_CODE_FOLDING_ENABLED, true));

            for (String s : LEGACY_SETTINGS) {
                String k = FoldUtilitiesImpl.PREF_COLLAPSE_PREFIX + s;
                prefs.putBoolean(k, basePrefs.getBoolean(k, false));
            }
        }
        propSupport.firePropertyChange(PROP_CHANGED, true, false);
    }

    private boolean suppressPrefChanges;
    
    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (suppressPrefChanges == Boolean.TRUE) {
            return;
        }
        boolean ch = detectIsChanged();
        MemoryPreferences defMime;
        synchronized (preferences) {
            defMime = preferences.get(""); // NOI18N
        }
        if (defMime != null && defMime.getPreferences() == evt.getNode()) {
            if (FoldUtilitiesImpl.PREF_CODE_FOLDING_ENABLED.equals(evt.getKey())) {
                // propagate to all preferences, suppress events
                suppressPrefChanges = true;
                Collection<MemoryPreferences> col;
                
                synchronized (preferences) {
                    col = new ArrayList<>(preferences.values());
                }
                try {
                    for (MemoryPreferences p : col) {
                        if (p != defMime) {
                            if (((OverridePreferences)p.getPreferences()).isOverriden(FoldUtilitiesImpl.PREF_CODE_FOLDING_ENABLED)) {
                                p.getPreferences().remove(FoldUtilitiesImpl.PREF_CODE_FOLDING_ENABLED);
                            }
                        } 
                    }
                } finally {
                    suppressPrefChanges = false;
                }
            }
        }
        if (ch != changed) {
            propSupport.firePropertyChange(PROP_CHANGED, !ch, ch);
            changed = true;
        }
    }
    
    private PreferenceChangeListener weakChangeL = WeakListeners.create(PreferenceChangeListener.class, this, null);
    
    void globalEnableFolding(boolean enable) {
        PREF_LOG.log(Level.FINE, "Globally set folding-enable: " + enable);
        prefs("").putBoolean(SimpleValueNames.CODE_FOLDING_ENABLE, enable); // NOI18N
        for (String mime : EditorSettings.getDefault().getAllMimeTypes()) {
            prefs(mime).remove(SimpleValueNames.CODE_FOLDING_ENABLE);
        }
    }
    
    /* called from the FoldOptionsPanel */
    Preferences prefs(String mime) {
        synchronized (preferences) {
            MemoryPreferences cached = preferences.get(mime);
            if (cached != null) {
                return cached.getPreferences();
            }
            MimePath path = MimePath.parse(mime);
            Preferences result = MimeLookup.getLookup(mime).lookup(Preferences.class);

            if (!mime.equals("")) { // NOI18N
                String parentMime = path.getInheritedType();
                /*
                result = new InheritedPreferences(
                        prefs(parentMime), result);
                        */
                cached = MemoryPreferences.getWithInherited(this, 
                    prefs(parentMime),
                    result);
            } else {
                cached = MemoryPreferences.get(this, result);
            }
            cached.getPreferences().addPreferenceChangeListener(weakChangeL);
            preferences.put(mime, cached);
            return cached.getPreferences();
        }
    }
    
    private void clearContents() {
        synchronized (preferences) {
            // clear the old preference values and recreate the panel
            for (MemoryPreferences m : preferences.values()) {
                m.getPreferences().removePreferenceChangeListener(weakChangeL);
                m.destroy();
            }
            preferences.clear();
            changed = false;
        }
        if (panel != null) {
            panel.clear();
        }
    }

    @Override
    public void cancel() {
        clearContents();
    }

    @Override
    public boolean isValid() {
        return true;
        
    }

    @Override
    public boolean isChanged() {
        return getPanel().isChanged();
        
    }
    
    private boolean detectIsChanged() {
        Collection<MemoryPreferences> cp;
        synchronized (preferences) {
            cp = new ArrayList<>(preferences.values());
        }
        for (MemoryPreferences cached : cp) {
            if (cached.isDirty(cached.getPreferences())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx ("netbeans.optionsDialog.editor.folding"); // NOI18N
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propSupport.removePropertyChangeListener(l);
    }
    
    private FoldOptionsPanel getPanel() {
        if (panel == null) {
            panel = new FoldOptionsPanel(this);
        }
        return panel;
    }
    
}
