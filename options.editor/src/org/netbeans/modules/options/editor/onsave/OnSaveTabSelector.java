/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
