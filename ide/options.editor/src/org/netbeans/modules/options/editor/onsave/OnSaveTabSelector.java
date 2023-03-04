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

package org.netbeans.modules.options.editor.onsave;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


public final class OnSaveTabSelector {

    public static final String ON_SAVE_CUSTOMIZERS_FOLDER = "OptionsDialog/Editor/OnSave/"; //NOI18N

    private final Map<String, PreferencesCustomizer> allCustomizers = new HashMap<String, PreferencesCustomizer>();

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private final OnSaveTabPanelController.PreferencesFactory preferencesFactory;
    
    private HashMap<String,MimeEntry> mimeType2Language;
    
    private List<String> sortedMimeTypes;
    
    private String selectedMimeType;

    OnSaveTabSelector(OnSaveTabPanelController.PreferencesFactory pf) {
        this.preferencesFactory = pf;
    }

    /**
     * Get mime-types sorted by language name.
     * @return non-null mime-types sorted by language name.
     */
    public synchronized List<String> getMimeTypes() {
        checkMimeTypesInited();
        return sortedMimeTypes;
    }
    
    public synchronized String getLanguageName(String mimeType) {
        checkMimeTypesInited();
        MimeEntry mimeEntry = mimeType2Language.get(mimeType);
        return (mimeEntry != null) ? mimeEntry.languageName : null;
    }

    public Preferences getPreferences(String mimeType) {
        return preferencesFactory.getPreferences(mimeType);
    }

    private void checkMimeTypesInited() {
        if (mimeType2Language == null) {
            Set<String> allMimeTypes = EditorSettings.getDefault().getAllMimeTypes();
            mimeType2Language = new HashMap<String, MimeEntry>((allMimeTypes.size() + 1) << 1);
            String allLanguagesMimeType = "";
            String allLanguages = NbBundle.getMessage(OnSaveTabSelector.class, "LBL_AllLanguages");
            mimeType2Language.put(allLanguagesMimeType, new MimeEntry(allLanguagesMimeType, allLanguages)); //NOI18N
            List<MimeEntry> mimeEntries = new ArrayList<MimeEntry>(allMimeTypes.size());
            for (String mimeType : allMimeTypes) {
                MimePath mimePath = MimePath.parse(mimeType);
                if (mimePath.size() > 1 || isCompoundMimeType(mimeType)) { // Only root languages and non-compound
                    continue;
                }
                String language = EditorSettings.getDefault().getLanguageName(mimeType);
                if (language.equals (mimeType)) {
                    continue;
                }
                MimeEntry mimeEntry = new MimeEntry(mimeType, language);
                mimeType2Language.put(mimeType, mimeEntry);
                mimeEntries.add(mimeEntry);
            }
            Collections.sort(mimeEntries);
            sortedMimeTypes = new ArrayList<String>(mimeEntries.size() + 1); // including "All Languages"
            sortedMimeTypes.add(allLanguagesMimeType);
            for (MimeEntry mimeEntry : mimeEntries) {
                sortedMimeTypes.add(mimeEntry.mimeType);
            }

            // Filter out mime types that don't supply customizers
//            for(String mimeType : EditorSettings.getDefault().getAllMimeTypes()) {
//                Lookup l = Lookups.forPath(ON_SAVE_CUSTOMIZERS_FOLDER + mimeType);
//                if (l.lookup(PreferencesCustomizer.Factory.class) != null)
//                    allMimeTypes.add(mimeType);
//            }
        }
    }

    private static boolean isCompoundMimeType(String mimeType) {
        int idx = mimeType.lastIndexOf('+');
        return idx != -1 && idx < mimeType.length() - 1;
    }
    
    public String getSelectedMimeType() {
        return selectedMimeType;
    }

    public synchronized void setSelectedMimeType(String mimeType) {
        assert getMimeTypes().contains(mimeType) : "'" + mimeType + "' is not among " + getMimeTypes(); //NOI18N
        if (selectedMimeType == null || !selectedMimeType.equals(mimeType)) {
            String old = selectedMimeType;
            selectedMimeType = mimeType;
            pcs.firePropertyChange(null, old, mimeType);
        }
    }

    public synchronized PreferencesCustomizer getSelectedCustomizer() {
        return getCustomizer(selectedMimeType);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    String getSavedValue(String mimeType, String key) {
        PreferencesCustomizer prefsCustomizer = getCustomizer(mimeType);
        if (prefsCustomizer != null) {
            Lookup l = Lookups.forPath(ON_SAVE_CUSTOMIZERS_FOLDER + mimeType);
            PreferencesCustomizer.CustomCustomizer customizer = l.lookup(PreferencesCustomizer.CustomCustomizer.class);
            if (customizer != null) {
                return customizer.getSavedValue(prefsCustomizer, key);
            }
        }
        return null;
    }

    private PreferencesCustomizer getCustomizer(String mimeType) {
        PreferencesCustomizer customizer = allCustomizers.get(mimeType);
        if (customizer == null) {
            Preferences prefs = preferencesFactory.getPreferences(mimeType);
            if (mimeType.length() > 0) {
                Lookup l = Lookups.forPath(ON_SAVE_CUSTOMIZERS_FOLDER + mimeType);
                PreferencesCustomizer.Factory factory = l.lookup(PreferencesCustomizer.Factory.class);
                if (factory != null) {
                    customizer = factory.create(prefs);
                }
            } else {
                customizer = null;
            }
            allCustomizers.put(selectedMimeType, customizer);
        }
        return customizer;
    }
    
    private static final class MimeEntry implements Comparable<MimeEntry> {
        
        final String mimeType;
        
        final String languageName;

        public MimeEntry(String mimeType, String languageName) {
            this.mimeType = mimeType;
            this.languageName = languageName;
        }

        @Override
        public int compareTo(MimeEntry mimeEntry) {
            return languageName.compareToIgnoreCase(mimeEntry.languageName);
        }

        @Override
        public String toString() {
            return "mimeType=\"" + mimeType + "\", languageName=\"" + languageName + "\"";
        }

    }

}
