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

package org.netbeans.modules.editor.settings.storage.fontscolors;

import java.io.IOException;
import org.netbeans.modules.editor.settings.storage.*;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.settings.storage.api.EditorSettingsStorage;
import org.netbeans.modules.editor.settings.storage.api.FontColorSettingsFactory;

/**
 *
 * @author Jan Jancura, Vita Stejskal
 */
public final class FontColorSettingsImpl extends FontColorSettingsFactory {

    private static final Logger LOG = Logger.getLogger(FontColorSettingsImpl.class.getName());

    private static final Map<MimePath, WeakReference<FontColorSettingsImpl>> INSTANCES =
        new WeakHashMap<MimePath, WeakReference<FontColorSettingsImpl>>();
    
    public static synchronized FontColorSettingsImpl get(MimePath mimePath) {
        WeakReference<FontColorSettingsImpl> reference = INSTANCES.get(mimePath);
        FontColorSettingsImpl result = reference == null ? null : reference.get();
        
        if (result == null) {
            result = new FontColorSettingsImpl(mimePath);
            INSTANCES.put(mimePath, new WeakReference<>(result));
        }
        
        return result;
    }
    
    private final MimePath mimePath;
    
    /**
     * Construction prohibited for API clients.
     */
    private FontColorSettingsImpl(MimePath mimePath) {
        this.mimePath = mimePath;
    }
    
    public MimePath getMimePath() {
        return mimePath;
    }
    
    /**
     * Translates profile's display name to its Id. If the profile's display name
     * can't be translated this method will simply return the profile's display name
     * without translation.
     */
    public String getInternalFontColorProfile(String profile) {
        ProfilesTracker tracker = ProfilesTracker.get(ColoringStorage.ID, EditorSettingsImpl.EDITORS_FOLDER);
        ProfilesTracker.ProfileDescription pd = tracker.getProfileByDisplayName(profile);
        return pd == null ? profile : pd.getId();
    }
    
    //-----------------------------------------------------------------------
    // FontColorSettingsFactory implementation
    //-----------------------------------------------------------------------
    
    /**
     * Gets all token font and colors for given profile or null, if 
     * profile does not exists. 
     * 
     * @param profile the name of profile
     *
     * @return AttributeSet describing the font and colors or null, if 
     *                      profile does not exists
     */
    @Override
    public Collection<AttributeSet> getAllFontColors (String profile) {
        profile = getInternalFontColorProfile(profile);
	Map<String, AttributeSet> m = getColorings(profile);
        return Collections.unmodifiableCollection(m.values());
    }
    
    /**
     * Gets default values for all font & colors for given profile, or null
     * if profile does not exist or if it does not have any defaults. 
     * 
     * @param profile the name of profile
     *
     * @return AttributeSet describing the font and colors or null, if 
     *                      profile does not exists
     */
    @Override
    public Collection<AttributeSet> getAllFontColorDefaults(String profile) {
        profile = getInternalFontColorProfile(profile);
        Map<String, AttributeSet> profileColorings = getDefaultColorings(profile);
        Map<String, AttributeSet> defaultProfileColorings = null;
        if (!EditorSettingsImpl.DEFAULT_PROFILE.equals(profile)) {
            defaultProfileColorings = getDefaultColorings(EditorSettingsImpl.DEFAULT_PROFILE);
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
        return profileColorings.values();
    }
    
    /**
     * Sets all token font and colors for given profile. 
     * 
     * @param profile the name of profile
     * @param fontColors new colorings
     */
    @Override
    public void setAllFontColors (
        String profile,
        Collection<AttributeSet> fontColors
    ) {
        EditorSettingsStorage<String, AttributeSet> ess = EditorSettingsStorage.<String, AttributeSet>get(ColoringStorage.ID);
        boolean specialProfile = profile.startsWith("test"); //NOI18N
        profile = getInternalFontColorProfile(profile);
        
        if (fontColors == null) {
            try {
                ess.delete(mimePath, profile, false);
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
            colorings.remove (profile);
        } else {
            Map<String, AttributeSet> map = NbUtils.immutize(fontColors);
            
            if (!specialProfile) {
                try {
                    ess.save(mimePath, profile, false, map);
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, null, ioe);
                }
            }
            
            colorings.put(profile, map);
        }
        
        EditorSettingsImpl.getInstance().notifyTokenFontColorChange(mimePath, profile);
    }
    
    /**
     * Sets default values for all token font and colors for given scheme. 
     * 
     * @param profile the name of profile
     * @param fontColors new colorings
     */
    public void setAllFontColorsDefaults (
        String profile,
        Collection<AttributeSet> fontColors
    ) {
        EditorSettingsStorage<String, AttributeSet> ess = EditorSettingsStorage.<String, AttributeSet>get(ColoringStorage.ID);
        boolean specialProfile = profile.startsWith("test"); //NOI18N
        profile = getInternalFontColorProfile(profile);
        
        try {
            if (fontColors == null) {
                ess.delete(mimePath, profile, true);
            } else {
                ess.save(mimePath, profile, true, NbUtils.immutize(fontColors));
            }
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }
        
        EditorSettingsImpl.getInstance().notifyTokenFontColorChange(mimePath, profile);
    }
    
    //-----------------------------------------------------------------------
    // private implementation
    //-----------------------------------------------------------------------
    
    private final Map<String, Map<String, AttributeSet>> colorings = new HashMap<>();
    
    /* package */ Map<String, AttributeSet> getColorings (String profile) {
	if (!colorings.containsKey (profile)) {
            boolean specialProfile = profile.startsWith("test"); //NOI18N
            EditorSettingsStorage<String, AttributeSet> ess = EditorSettingsStorage.<String, AttributeSet>get(ColoringStorage.ID);
            
            Map<String, AttributeSet> profileColorings = null;
            try {
                profileColorings = ess.load(
                    mimePath, 
                    specialProfile ? EditorSettingsImpl.DEFAULT_PROFILE : profile,
                    false
                );
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
            
            Map<String, AttributeSet> defaultProfileColorings = null;
            if (!specialProfile && !EditorSettingsImpl.DEFAULT_PROFILE.equals(profile)) {
                try {
                    defaultProfileColorings = ess.load(
                        mimePath, 
                        EditorSettingsImpl.DEFAULT_PROFILE,
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
            
            colorings.put(profile, profileColorings);
	}
        
        Map<String, AttributeSet> c = colorings.get(profile);
	return c == null ? Collections.<String, AttributeSet>emptyMap() : c;
    }
    
    /* package */ Map<String, AttributeSet> getDefaultColorings (String profile) {
        EditorSettingsStorage<String, AttributeSet> ess = EditorSettingsStorage.<String, AttributeSet>get(ColoringStorage.ID);
        try {
            return ess.load(mimePath, profile, true);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
            return Collections.<String, AttributeSet>emptyMap();
        }
    }

}
