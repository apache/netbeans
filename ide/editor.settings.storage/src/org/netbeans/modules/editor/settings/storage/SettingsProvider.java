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

package org.netbeans.modules.editor.settings.storage;

import java.util.prefs.PreferenceChangeEvent;
import org.netbeans.modules.editor.settings.storage.keybindings.KeyBindingSettingsImpl;
import org.netbeans.modules.editor.settings.storage.fontscolors.CompositeFCS;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.editor.settings.storage.fontscolors.FontColorSettingsImpl;
import org.netbeans.modules.editor.settings.storage.preferences.PreferencesImpl;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 *  @author Jan Jancura
 */
@MIMEResolver.Registration(
    displayName="#EditorResolver",
    resource="mime-resolvers.xml",
    position=330
)
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.editor.mimelookup.MimeDataProvider.class)
public final class SettingsProvider implements MimeDataProvider {

    private static final Logger LOG = Logger.getLogger(SettingsProvider.class.getName());
    
    private final Map<MimePath, WeakReference<Lookup>> cache = new WeakHashMap<>();
    
    public SettingsProvider () {
    }
    
    /**
     * Lookup providing mime-type sensitive or global-level data
     * depending on which level this initializer is defined.
     * 
     * @return Lookup or null, if there are no lookup-able objects for mime or global level.
     */
    @Override
    public Lookup getLookup(MimePath mimePath) {
        if (mimePath.size() > 0 && mimePath.getMimeType(0).contains(EditorSettingsImpl.TEXT_BASE_MIME_TYPE)) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, "Won't provide any settings for " + EditorSettingsImpl.TEXT_BASE_MIME_TYPE + //NOI18N
                    " It's been deprecated, use MimePath.EMPTY instead."); //, new Throwable("Stacktrace") //NOI18N
            }
            return null;
        }
        
        synchronized (cache) {
            WeakReference<Lookup> ref = cache.get(mimePath);
            Lookup lookup = ref == null ? null : ref.get();
            
            if (lookup == null) {
                String path = mimePath.getPath();
                if (path.startsWith("test")) { //NOI18N
                    int idx = path.indexOf('_'); //NOI18N
                    if (idx == -1) {
                        throw new IllegalStateException("Invalid mimePath: " + path); //NOI18N
                    }
                    
                    // Get the special test profile name and the real mime path
                    String profile = path.substring(0, idx);
                    MimePath realMimePath = MimePath.parse(path.substring(idx + 1));
                    
                    lookup = new ProxyLookup(new Lookup [] {
                        new MyLookup(realMimePath, profile),
                        Lookups.exclude(
                            MimeLookup.getLookup(realMimePath),
                            new Class [] {
                                FontColorSettings.class,
                                KeyBindingSettings.class
                            })
                    });
                } else {
                    lookup = new MyLookup(mimePath, null);
                }
                
                cache.put(mimePath, new WeakReference<>(lookup));
            }
            
            return lookup;
        }
    }
    
    private static final class MyLookup extends AbstractLookup implements PropertyChangeListener, PreferenceChangeListener {
        
        private final MimePath mimePath;
        private final boolean specialFcsProfile;
        private String fcsProfile;
        
        private final InstanceContent ic;
        private CompositeFCS fontColorSettings = null;
        private Object keyBindingSettings = null;
        private PreferencesImpl preferences = null;
        
        private KeyBindingSettingsImpl kbsi;
        
        public MyLookup(MimePath mimePath, String profile) {
            this(mimePath, profile, new InstanceContent());
        }
        
        // -------------------------------------------------------------------
        // PropertyChangeListener implementation
        // -------------------------------------------------------------------
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (this) {
                boolean fcsChanged = false;
                boolean kbsChanged = false;

//                if (mimePath.getPath().contains("xml")) {
//                    System.out.println("@@@ propertyChange: mimePath = " + mimePath.getPath() + " profile = " + fcsProfile + " property = " + evt.getPropertyName() + " oldValue = " + (evt.getOldValue() instanceof MimePath ? ((MimePath) evt.getOldValue()).getPath() : evt.getOldValue()) + " newValue = " + evt.getNewValue());
//                }
                
                // Determine what has changed
                if (this.kbsi == evt.getSource()) {
                    kbsChanged = true;
                    
                } else if (evt.getPropertyName() == null) {
                    // reset all
                    if (!specialFcsProfile) {
                        String currentProfile = EditorSettings.getDefault().getCurrentFontColorProfile();
                        fcsProfile = FontColorSettingsImpl.get(mimePath).getInternalFontColorProfile(currentProfile);
                    }
                    fcsChanged = true;
                    
                } else if (evt.getPropertyName().equals(EditorSettingsImpl.PROP_HIGHLIGHT_COLORINGS)) {
                    String changedProfile = (String) evt.getNewValue();
                    if (changedProfile.equals(fcsProfile)) {
                        fcsChanged = true;
                    }
                    
                } else if (evt.getPropertyName().equals(EditorSettingsImpl.PROP_TOKEN_COLORINGS)) {
                    String changedProfile = (String) evt.getNewValue();
                    if (changedProfile.equals(fcsProfile)) {
                        MimePath changedMimePath = (MimePath) evt.getOldValue();
                        if (fontColorSettings != null && fontColorSettings.isDerivedFromMimePath(changedMimePath)) {
                            fcsChanged = true;
                        }
                    }
                    
                } else if (evt.getPropertyName().equals(EditorSettingsImpl.PROP_CURRENT_FONT_COLOR_PROFILE)) {
                    if (!specialFcsProfile) {
                        String newProfile = (String) evt.getNewValue();
                        fcsProfile = FontColorSettingsImpl.get(mimePath).getInternalFontColorProfile(newProfile);
                        fcsChanged = true;
                    }
                }
                
                // Update lookup contents
                updateContents(kbsChanged, fcsChanged);
            }
        }

        // -------------------------------------------------------------------
        // PreferenceChangeListener implementation
        // -------------------------------------------------------------------
        
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();
            if (settingName == null || settingName.equals(CompositeFCS.TEXT_ANTIALIASING_PROP)) { //NOI18N
                synchronized (this) {
                    updateContents(false, true);
                }
            }
        }

        // -------------------------------------------------------------------
        // private implementation
        // -------------------------------------------------------------------
        
        private MyLookup(MimePath mimePath, String profile, InstanceContent ic) {
            super(ic);

            this.mimePath = mimePath;
            
            if (profile == null) {
                // Use the selected current profile
                String currentProfile = EditorSettings.getDefault().getCurrentFontColorProfile();
                this.fcsProfile = FontColorSettingsImpl.get(mimePath).getInternalFontColorProfile(currentProfile);
                this.specialFcsProfile = false;
            } else {
                // This is the special test profile derived from the mime path.
                // It will never change.
                this.fcsProfile = profile;
                this.specialFcsProfile = true;
            }
            
            this.ic = ic;
            
            // Start listening
            EditorSettings es = EditorSettings.getDefault();
            es.addPropertyChangeListener(WeakListeners.propertyChange(this, es));
            
            this.kbsi = KeyBindingSettingsImpl.get(mimePath);
            this.kbsi.addPropertyChangeListener(WeakListeners.propertyChange(this, this.kbsi));

            // in fact this could probably be turned into 'assert preferences == null;'
            if (preferences == null) {
                preferences = PreferencesImpl.get(mimePath);
                preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, preferences));
            }

            fontColorSettings = new CompositeFCS(mimePath, fcsProfile, preferences);
            keyBindingSettings = this.kbsi.createInstanceForLookup();

            ic.set(Arrays.asList(new Object [] {
                fontColorSettings,
                keyBindingSettings,
                preferences
            }), null);
        }

        private void updateContents(boolean kbsChanged, boolean fcsChanged) {
            boolean updateContents = false;

            if (fcsChanged && fontColorSettings != null) {
                fontColorSettings = new CompositeFCS(mimePath, fcsProfile, preferences);
                updateContents = true;
            }

            if (kbsChanged  && keyBindingSettings != null) {
                keyBindingSettings = this.kbsi.createInstanceForLookup();
                updateContents = true;
            }

            if (updateContents) {
                List<Object> list = new ArrayList<>();
                if (fontColorSettings != null) {
                    list.add(fontColorSettings);
                }
                if (keyBindingSettings != null) {
                    list.add(keyBindingSettings);
                }
                if (preferences != null) {
                    list.add(preferences);
                }
                ic.set(list, null);
            }
        }
        
    } // End of MyLookup class
}
