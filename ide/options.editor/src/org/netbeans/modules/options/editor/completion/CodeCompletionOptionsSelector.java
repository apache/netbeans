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

package org.netbeans.modules.options.editor.completion;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.prefs.Preferences;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;


public final class CodeCompletionOptionsSelector {

    public static final String CODE_COMPLETION_CUSTOMIZERS_FOLDER = "OptionsDialog/Editor/CodeCompletion/"; //NOI18N

    private final Map<String, PreferencesCustomizer> allCustomizers = new HashMap<String, PreferencesCustomizer>();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final CodeCompletionOptionsPanelController.PreferencesFactory pf;
    private Set<String> mimeTypes;
    private String selectedMimeType;

    CodeCompletionOptionsSelector(CodeCompletionOptionsPanelController.PreferencesFactory pf) {
        this.pf = pf;
    }

    public synchronized Set<? extends String> getMimeTypes() {
        if (mimeTypes == null) {
            mimeTypes = new HashSet<String>();
            mimeTypes.add(""); //NOI18N

            // filter out mime types that don't supply customizers
            for(String mimeType : EditorSettings.getDefault().getAllMimeTypes()) {
                Lookup l = Lookups.forPath(CODE_COMPLETION_CUSTOMIZERS_FOLDER + mimeType);
                if (l.lookup(PreferencesCustomizer.Factory.class) != null)
                    mimeTypes.add(mimeType);
            }
        }
        return mimeTypes;
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
            Lookup l = Lookups.forPath(CODE_COMPLETION_CUSTOMIZERS_FOLDER + mimeType);
            PreferencesCustomizer.CustomCustomizer customizer;
            if (mimeType.isEmpty()) {
                customizer = l.lookup(GeneralCompletionOptionsPanelController.CustomCustomizerImpl.class);
            } else {
                customizer = l.lookup(PreferencesCustomizer.CustomCustomizer.class);
            }
            if (customizer != null) {
                return customizer.getSavedValue(prefsCustomizer, key);
            }
        }
        return null;
    }

    private PreferencesCustomizer getCustomizer(String mimeType) {
        PreferencesCustomizer customizer = allCustomizers.get(mimeType);
        if (customizer == null) {
            Preferences prefs = pf.getPreferences(mimeType);
            if (mimeType.length() > 0) {
                Lookup l = Lookups.forPath(CODE_COMPLETION_CUSTOMIZERS_FOLDER + mimeType);
                PreferencesCustomizer.Factory factory = l.lookup(PreferencesCustomizer.Factory.class);
                if (factory != null)
                    customizer = factory.create(prefs);
            } else {
                customizer = new GeneralCompletionOptionsPanelController(prefs);
            }
            allCustomizers.put(selectedMimeType, customizer);
        }
        return customizer;
    }
}
