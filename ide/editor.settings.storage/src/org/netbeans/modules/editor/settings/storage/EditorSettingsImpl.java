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

import java.beans.PropertyChangeEvent;
import org.netbeans.modules.editor.settings.storage.keybindings.KeyBindingSettingsImpl;
import org.netbeans.modules.editor.settings.storage.fontscolors.ColoringStorage;
import org.netbeans.modules.editor.settings.storage.fontscolors.FontColorSettingsImpl;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import static org.netbeans.modules.editor.settings.storage.api.EditorSettings.PROP_CURRENT_KEY_MAP_PROFILE;
import org.netbeans.modules.editor.settings.storage.api.FontColorSettingsFactory;
import org.netbeans.modules.editor.settings.storage.api.KeyBindingSettingsFactory;
import org.netbeans.modules.editor.settings.storage.keybindings.KeyMapsStorage;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * This class contains access methods for editor settings like font & colors
 * profiles and keymap profiles. 
 *
 * @author Jan Jancura
 */
public class EditorSettingsImpl extends EditorSettings {

    private static final Logger LOG = Logger.getLogger(EditorSettingsImpl.class.getName());
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /** The name of the property change event for 'Highlighting' font and colors. */
    public static final String PROP_HIGHLIGHT_COLORINGS = "editorFontColors"; //NOI18N
    
    /** The name of the property change event for 'Annotation' font and colors. */
    public static final String PROP_ANNOTATION_COLORINGS = "editorAnnotationFontColors"; //NOI18N

    /** The name of the property change event for 'Token' font and colors. */
    public static final String PROP_TOKEN_COLORINGS = "fontColors"; //NOI18N
        
        
    /** The name of the default profile. */
    public static final String DEFAULT_PROFILE = "NetBeans"; //NOI18N
    
    // XXX: rewrite this using NbPreferences
    private static final String FATTR_CURRENT_FONT_COLOR_PROFILE = "currentFontColorProfile"; // NOI18N
    private static final String FATTR_CURRENT_KEYMAP_PROFILE = "currentKeymap";      // NOI18N

    /** Storage folder for the current font & color profile attribute. */
    public static final String EDITORS_FOLDER = "Editors"; //NOI18N
    /** Storage folder for the current keybindings profile attribute. */
    private static final String KEYMAPS_FOLDER = "Keymaps"; // NOI18N

    public static final String TEXT_BASE_MIME_TYPE = "text/base"; //NOI18N
    private static final String [] EMPTY = new String[0];
    
    private static EditorSettingsImpl instance = null;
    private final Set<String> cacheFontColorProfiles = new HashSet<>();
    
    public static synchronized EditorSettingsImpl getInstance() {
        if (instance == null) {
            instance = new EditorSettingsImpl();
        }
        return instance;
    }

    // ------------------------------------------------------
    // Mime types
    // ------------------------------------------------------

    public void notifyMimeTypesChange(Object old, Object nue) {
        pcs.firePropertyChange(new PropertyChangeEvent(this, PROP_MIME_TYPES, old, nue));
    }

    @Override
    public Set<String> getAllMimeTypes () {
        return MimeTypesTracker.get(null, EDITORS_FOLDER).getMimeTypes();
    }

    /**
     * Returns set of mimetypes.
     *
     * @return set of mimetypes
     */
    // XXX: the API should actually use Collection<String>
    @Override
    public Set<String> getMimeTypes() {
        return MimeTypesTracker.get(ColoringStorage.ID, EDITORS_FOLDER).getMimeTypes();
    }
    
    /**
     * Returns name of language for given mime type.
     *
     * @return name of language for given mime type
     */
    @Override
    public String getLanguageName (String mimeType) {
        return MimeTypesTracker.get(null, EDITORS_FOLDER).getMimeTypeDisplayName(mimeType);
    }

    // ------------------------------------------------------
    // Font Colors
    // ------------------------------------------------------

    public void notifyTokenFontColorChange(MimePath mimePath, String profile) {
        // XXX: this is hack, we should not abuse the event values like that
        pcs.firePropertyChange(PROP_TOKEN_COLORINGS, mimePath, profile);
    }
    
    /**
     * Gets display names of all font & color profiles.
     *
     * @return set of font & colors profiles
     */
    @Override
    public  Set<String> getFontColorProfiles () {
        Set<String> result = new HashSet<>();
        result.addAll(ProfilesTracker.get(ColoringStorage.ID, EDITORS_FOLDER).getProfilesDisplayNames());
        synchronized (this) {
            cacheFontColorProfiles.removeAll(result);
            result.addAll(cacheFontColorProfiles);
        }
	return result;
    }
    
    /**
     * Returns true for user defined profile.
     *
     * @param profile a profile name
     * @return true for user defined profile
     */
    @Override
    public boolean isCustomFontColorProfile(String profile) {
        ProfilesTracker tracker = ProfilesTracker.get(ColoringStorage.ID, EDITORS_FOLDER);
        ProfilesTracker.ProfileDescription pd = tracker.getProfileByDisplayName(profile);
        synchronized(this) {
            boolean inCache = cacheFontColorProfiles.contains(profile);
            return (pd != null && !pd.isRollbackAllowed()) || inCache;
        }
    }

    // XXX: Rewrite this using NbPreferences
    private String currentFontColorProfile;
    
    /**
     * Returns name of current font & colors profile.
     *
     * @return name of current font & colors profile
     */
    @Override
    public synchronized String getCurrentFontColorProfile () {
        if (currentFontColorProfile == null) {
            FileObject fo = FileUtil.getConfigFile(EDITORS_FOLDER);
            if (fo != null) {
                Object o = fo.getAttribute(FATTR_CURRENT_FONT_COLOR_PROFILE);
                if (o instanceof String) {
                    currentFontColorProfile = (String) o;
                }
            }
            if (currentFontColorProfile == null) {
                currentFontColorProfile = DEFAULT_PROFILE;
            }
        }
        if (!getFontColorProfiles ().contains (currentFontColorProfile)) {
            currentFontColorProfile = DEFAULT_PROFILE;
        }
        return currentFontColorProfile;
    }
    
    /**
     * Sets current font & colors profile.
     *
     * @param profile a profile name
     */
    @Override
    public synchronized void setCurrentFontColorProfile (String profile) {
        String oldProfile = getCurrentFontColorProfile ();
        if (oldProfile.equals (profile)) return;

        currentFontColorProfile = profile;
        
        if (!getFontColorProfiles ().contains (currentFontColorProfile)) {
            cacheFontColorProfiles.add(currentFontColorProfile);
        }
        
        // Persist the change
	FileObject fo = FileUtil.getConfigFile (EDITORS_FOLDER);
        if (fo != null) {
            try {
                fo.setAttribute (FATTR_CURRENT_FONT_COLOR_PROFILE, profile);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Can't persist change in current font&colors profile.", ex); //NOI18N
            }
        }

        // Notify others
        pcs.firePropertyChange (PROP_CURRENT_FONT_COLOR_PROFILE, oldProfile, currentFontColorProfile);
    }
    
    /**
     * Returns font & color defaults for given profile or null, if the profile
     * is unknown .
     *
     * @param profile a profile name
     * @return font & color defaults for given profile or null
     * 
     * @deprecated Use getFontColorSettings(new String[0]).getAllFontColors(profile) instead.
     */
    @Deprecated
    @Override
    public Collection<AttributeSet> getDefaultFontColors(String profile) {
        return getFontColorSettings(new String[0]).getAllFontColors(profile);
    }
    
    /**
     * Returns default values for font & color defaults for given profile 
     * or null, if the profile is unknown.
     *
     * @param profile a profile name
     * @return font & color defaults for given profile or null
     * 
     * @deprecated Use getFontColorSettings(new String[0]).getAllFontColorsDefaults(profile) instead.
     */
    @Deprecated
    @Override
    public Collection<AttributeSet> getDefaultFontColorDefaults(String profile) {
        return getFontColorSettings(new String[0]).getAllFontColorDefaults(profile);
    }
    
    /**
     * Sets font & color defaults for given profile.
     *
     * @param profile a profile name
     * @param fontColors font & color defaults to be used
     * 
     * @deprecated Use getFontColorSettings(new String[0]).setAllFontColors(profile, fontColors) instead.
     */
    @Deprecated
    @Override
    public void setDefaultFontColors(String profile, Collection<AttributeSet> fontColors) {
        getFontColorSettings(new String[0]).setAllFontColors(profile, fontColors);
    }
    
    private final Map<String, Map<String, AttributeSet>> highlightings = new HashMap<>();
    private final StorageImpl<String, AttributeSet> highlightingsStorage = new StorageImpl<>(new ColoringStorage(ColoringStorage.FAV_HIGHLIGHT), null);
    
    /**
     * Returns highlighting properties for given profile or null, if the 
     * profile is not known.
     *
     * @param profile a profile name
     * @return highlighting properties for given profile or null
     */
    @Override
    public Map<String, AttributeSet> getHighlightings(String profile) {
        boolean specialProfile = profile.startsWith("test"); //NOI18N
        profile = FontColorSettingsImpl.get(MimePath.EMPTY).getInternalFontColorProfile(profile);

        if (!highlightings.containsKey(profile)) {
            Map<String, AttributeSet> profileColorings = null;
            
            try {
                profileColorings = highlightingsStorage.load(
                    MimePath.EMPTY,
                    specialProfile ? DEFAULT_PROFILE : profile,
                    false
                );
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
            
            Map<String, AttributeSet> defaultProfileColorings = null;
            if (!specialProfile && !DEFAULT_PROFILE.equals(profile)) {
                try {
                    defaultProfileColorings = highlightingsStorage.load(
                        MimePath.EMPTY,
                        DEFAULT_PROFILE,
                        false
                    );
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, null, ioe);
                }
            }
            
            // Add colorings from the default profile that do not exist in
            // the profileColorings. They are normally the same, but when
            // imported from previous version some colorings can be missing.
            // See #119709
            Map<String, AttributeSet> m = new HashMap<>();
            if (defaultProfileColorings != null) {
                m.putAll(defaultProfileColorings);
            }
            if (profileColorings != null) {
                m.putAll(profileColorings);
            }
            profileColorings = Collections.unmodifiableMap(m);
            
            highlightings.put(profile, profileColorings);
        }

        Map<String, AttributeSet> h = highlightings.get(profile);
        return h == null ? Collections.<String, AttributeSet>emptyMap() : h;
    }
    
    /**
     * Returns defaults for highlighting properties for given profile,
     * or null if the profile is not known.
     *
     * @param profile a profile name
     * @return highlighting properties for given profile or null
     */
    @Override
    public Map<String, AttributeSet> getHighlightingDefaults(String profile) {
        profile = FontColorSettingsImpl.get(MimePath.EMPTY).getInternalFontColorProfile(profile);
        try {
            return highlightingsStorage.load(MimePath.EMPTY, profile, true);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
            return Collections.<String, AttributeSet>emptyMap();
        }
    }
    
    /**
     * Sets highlighting properties for given profile.
     *
     * @param profile a profile name
     * @param highlighting a highlighting properties to be used
     */
    @Override
    public void setHighlightings (
	String  profile,
	Map<String, AttributeSet> fontColors
    ) {
        boolean specialProfile = profile.startsWith("test"); //NOI18N
	profile = FontColorSettingsImpl.get(MimePath.EMPTY).getInternalFontColorProfile (profile);
        
        if (fontColors == null) {
            try {
                highlightingsStorage.delete(MimePath.EMPTY, profile, false);
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
            highlightings.remove (profile);
        } else {
            Map<String, AttributeSet> m = NbUtils.immutize(fontColors);

            // 3) save new values to disk
            if (!specialProfile) {
                try {
                    highlightingsStorage.save(MimePath.EMPTY, profile, false, m);
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, null, ioe);
                }
            }
            
            highlightings.put(profile, m);
        }
        
        pcs.firePropertyChange(PROP_HIGHLIGHT_COLORINGS, MimePath.EMPTY, profile);
    }  
    
    private final Map<String, Map<String, AttributeSet>> annotations = new HashMap<>();
    private final StorageImpl<String, AttributeSet> annotationsStorage = new StorageImpl<>(new ColoringStorage(ColoringStorage.FAV_ANNOTATION), null);

    @Override
    public Map<String, AttributeSet> getAnnotations(String profile) {
        boolean specialProfile = profile.startsWith("test"); //NOI18N
        profile = FontColorSettingsImpl.get(MimePath.EMPTY).getInternalFontColorProfile(profile);

        if (!annotations.containsKey(profile)) {
            Map<String, AttributeSet> profileColorings = null;

            try {
                profileColorings = annotationsStorage.load(
                        MimePath.EMPTY,
                        specialProfile ? DEFAULT_PROFILE : profile,
                        false
                );
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }

            Map<String, AttributeSet> defaultProfileColorings = null;
            if (!specialProfile && !DEFAULT_PROFILE.equals(profile)) {
                try {
                    defaultProfileColorings = annotationsStorage.load(
                            MimePath.EMPTY,
                            DEFAULT_PROFILE,
                            false
                    );
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, null, ioe);
                }
            }

            // Add colorings from the default profile that do not exist in
            // the profileColorings. They are normally the same, but when
            // imported from previous version some colorings can be missing.
            // See #119709
            Map<String, AttributeSet> m = new HashMap<>();
            if (defaultProfileColorings != null) {
                m.putAll(defaultProfileColorings);
            }
            if (profileColorings != null) {
                m.putAll(profileColorings);
            }

            //todo prepare annotations.
            profileColorings = Collections.unmodifiableMap(m);

            annotations.put(profile, profileColorings);
        }

        Map<String, AttributeSet> h = annotations.get(profile);
        return h == null ? Collections.<String, AttributeSet>emptyMap() : h;
    }

    @Override
    public Map<String, AttributeSet> getAnnotationDefaults(String profile) {
        profile = FontColorSettingsImpl.get(MimePath.EMPTY).getInternalFontColorProfile(profile);
        try {
            return annotationsStorage.load(MimePath.EMPTY, profile, true);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
            return Collections.<String, AttributeSet>emptyMap();
        }
    }

    @Override
    public void setAnnotations(
            String profile,
            Map<String, AttributeSet> fontColors
    ) {
        boolean specialProfile = profile.startsWith("test"); //NOI18N
        profile = FontColorSettingsImpl.get(MimePath.EMPTY).getInternalFontColorProfile(profile);

        if (fontColors == null) {
            try {
                annotationsStorage.delete(MimePath.EMPTY, profile, false);
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
            annotations.remove(profile);
        } else {
            Map<String, AttributeSet> m = NbUtils.immutize(fontColors);

            // 3) save new values to disk
            if (!specialProfile) {
                try {
                    annotationsStorage.save(MimePath.EMPTY, profile, false, m);
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, null, ioe);
                }
            }

            annotations.put(profile, m);
        }

        pcs.firePropertyChange(PROP_ANNOTATION_COLORINGS, MimePath.EMPTY, profile);
    }
    
    // ------------------------------------------------------
    // Keybindings
    // ------------------------------------------------------

    /**
     * Returns set of keymap profiles.
     *
     * @return set of font & colors profiles
     */
    // XXX: the API should actually use Collection<String>
    @Override
    public Set<String> getKeyMapProfiles () {
	return ProfilesTracker.get(KeyMapsStorage.ID, EDITORS_FOLDER).getProfilesDisplayNames();
    }
    
    /**
     * Returns true for user defined profile.
     *
     * @param profile a profile name
     * @return true for user defined profile
     */
    @Override
    public boolean isCustomKeymapProfile (String profile) {
        ProfilesTracker tracker = ProfilesTracker.get(KeyMapsStorage.ID, EDITORS_FOLDER);
        ProfilesTracker.ProfileDescription pd = tracker.getProfileByDisplayName(profile);
        return pd == null || !pd.isRollbackAllowed();
    }
    
    private volatile String currentKeyMapProfile;
    
    /**
     * Returns name of current keymap profile.
     *
     * @return name of current keymap profile
     */
    @Override
    public String getCurrentKeyMapProfile () {
        if (currentKeyMapProfile == null) {
            FileObject fo = FileUtil.getConfigFile (KEYMAPS_FOLDER);
            if (fo != null) {
                Object o = fo.getAttribute (FATTR_CURRENT_KEYMAP_PROFILE);
                if (o instanceof String) {
                    currentKeyMapProfile = (String) o;
                }
            }
            if (currentKeyMapProfile == null) {
                currentKeyMapProfile = DEFAULT_PROFILE;
            }
            tracker = new KeymapProfileTracker(fo);
        }
        return currentKeyMapProfile;
    }
    
    // just to keep the object alive
    private KeymapProfileTracker tracker;
    
    /**
     * Tracks changes to the keymap profile attribute; changes are refired
     * as editor settings changes and mirrored to the currentKeymapProfile property
     */
    private class KeymapProfileTracker extends FileChangeAdapter {
        private FileObject keymapFolder;

        public KeymapProfileTracker(FileObject keymapFolder) {
            this.keymapFolder = keymapFolder;
            if (keymapFolder != null) {
                keymapFolder.addFileChangeListener(this);
            } else {
                FileUtil.getConfigRoot().addFileChangeListener(this);
            }
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            if (FATTR_CURRENT_KEYMAP_PROFILE.equals(fe.getName())) {
                Object n = fe.getNewValue();
                Object o = fe.getOldValue();
                
                String ov = o == null ? DEFAULT_PROFILE : o.toString();
                String nv = n == null ? DEFAULT_PROFILE : n.toString();
                
                currentKeyMapProfile = nv;
                pcs.firePropertyChange (PROP_CURRENT_KEY_MAP_PROFILE, ov, nv);
            }
        }
        
        
        @Override
        public synchronized void fileFolderCreated(FileEvent fe) {
            FileObject f = fe.getFile();
            if (keymapFolder == null && KEYMAPS_FOLDER.equals(f.getNameExt()) && f.getParent() == FileUtil.getConfigRoot()) {
                // keymap folder is created -> start listening on it
                keymapFolder = f;
                f.addFileChangeListener(this);
            }
        }
        
    }
    
    /**
     * Sets current keymap profile.
     *
     * @param profile a profile name
     */
    @Override
    public void setCurrentKeyMapProfile (String keyMapName) {
        /*
        String oldKeyMap = getCurrentKeyMapProfile ();
        if (oldKeyMap.equals (keyMapName)) return;

        currentKeyMapProfile = keyMapName;
        
        // Persist the change
        try {
            FileObject fo = FileUtil.getConfigFile (KEYMAPS_FOLDER);
            if (fo == null) {
                fo = FileUtil.getConfigRoot ().createFolder (KEYMAPS_FOLDER);
            }
            fo.setAttribute (FATTR_CURRENT_KEYMAP_PROFILE, keyMapName);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Can't persist change in current keybindings profile.", ex); //NOI18N
        }
        
        // Notify others
        pcs.firePropertyChange (PROP_CURRENT_KEY_MAP_PROFILE, oldKeyMap, currentKeyMapProfile);
        */
    }
    
    // support methods .........................................................
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be registerred
     */
    @Override
    public void addPropertyChangeListener (
        PropertyChangeListener l
    ) {
        pcs.addPropertyChangeListener (l);
    }
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be unregisterred
     */
    @Override
    public void removePropertyChangeListener (
        PropertyChangeListener l
    ) {
        pcs.removePropertyChangeListener (l);
    }
    
    /**
     * PropertyChangeListener registration.
     *
     * @param propertyName  The name of the property to listen on.
     * @param l a PropertyChangeListener to be registerred
     */
    @Override
    public void addPropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    ) {
        pcs.addPropertyChangeListener (propertyName, l);
    }
    
    /**
     * PropertyChangeListener registration.
     *
     * @param propertyName  The name of the property to listen on.
     * @param l a PropertyChangeListener to be unregisterred
     */
    @Override
    public void removePropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    ) {
        pcs.removePropertyChangeListener (propertyName, l);
    }

    private EditorSettingsImpl() {
        
    }
    
    @Override
    public KeyBindingSettingsFactory getKeyBindingSettings (String[] mimeTypes) {
        mimeTypes = filter(mimeTypes);
        return KeyBindingSettingsImpl.get(Utils.mimeTypes2mimePath(mimeTypes));
    }

    @Override
    public FontColorSettingsFactory getFontColorSettings (String[] mimeTypes) {
        mimeTypes = filter(mimeTypes);
        return FontColorSettingsImpl.get(Utils.mimeTypes2mimePath(mimeTypes));
    }
    
    private String [] filter(String [] mimeTypes) {
        if (mimeTypes.length > 0) {
            String [] filtered = mimeTypes;
    
            if (mimeTypes[0].contains(TEXT_BASE_MIME_TYPE)) {
                if (mimeTypes.length == 1) {
                    filtered = EMPTY;
                } else {
                    filtered = new String [mimeTypes.length - 1];
                    System.arraycopy(mimeTypes, 1, filtered, 0, mimeTypes.length - 1);
                }
                
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, TEXT_BASE_MIME_TYPE + " has been deprecated, use MimePath.EMPTY instead."); //, new Throwable("Stacktrace") //NOI18N
                }
                
            } else if (mimeTypes[0].startsWith("test")) {
                filtered = new String [mimeTypes.length];
                System.arraycopy(mimeTypes, 0, filtered, 0, mimeTypes.length);
                filtered[0] = mimeTypes[0].substring(mimeTypes[0].indexOf('_') + 1); //NOI18N

                LOG.log(Level.INFO, "Don't use 'test' mime type to access settings through the editor/settings/storage API!", new Throwable("Stacktrace"));
            }
            
            return filtered;
        } else {
            return mimeTypes;
        }
    }

    private MimePath filter(MimePath mimePath) {
        if (mimePath.size() > 0) {
            MimePath filtered = mimePath;
            String first = mimePath.getMimeType(0);
            
            if (first.contains(TEXT_BASE_MIME_TYPE)) {
                if (mimePath.size() == 1) {
                    filtered = MimePath.EMPTY;
                } else {
                    String path = mimePath.getPath().substring(first.length() + 1);
                    filtered = MimePath.parse(path);
                }
                
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, TEXT_BASE_MIME_TYPE + " has been deprecated, use MimePath.EMPTY instead."); //, new Throwable("Stacktrace") //NOI18N
                }
                
            } else if (first.startsWith("test")) {
                String filteredFirst = first.substring(first.indexOf('_') + 1); //NOI18N
                String path = filteredFirst + mimePath.getPath().substring(first.length() + 1);
                filtered = MimePath.parse(path);

                LOG.log(Level.INFO, "Don't use 'test' mime type to access settings through the editor/settings/storage API!", new Throwable("Stacktrace"));
            }
            
            return filtered;
        } else {
            return mimePath;
        }
    }
}
