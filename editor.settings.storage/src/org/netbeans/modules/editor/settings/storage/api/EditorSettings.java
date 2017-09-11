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

package org.netbeans.modules.editor.settings.storage.api;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.swing.text.AttributeSet;
import org.netbeans.modules.editor.settings.storage.EditorSettingsImpl;
import org.netbeans.modules.editor.settings.storage.MimeTypesTracker;


/**
 * This singleton class contains access methods for editor settings like
 * font & colors profiles and keymaps.
 *
 * @author Jan Jancura
 */
public abstract class EditorSettings {

    /**
     * Returns default instance of EditorSettings.
     *
     * @return default instance of EditorSettings
     */
    public static EditorSettings getDefault () {
        return EditorSettingsImpl.getInstance();
    }

    /**
     * Gets all top level mime types registered under the <code>Editors/</code>
     * folder.
     *
     * @return set of mimetypes
     * @since 1.9
     */
    public abstract Set<String> getAllMimeTypes ();
    
    /**
     * Returns set of mimetypes. The set contains only mime types that have
     * <code>coloring.xml</code> defined in the default profile.
     *
     * @return set of mimetypes
     */
    public abstract Set<String> getMimeTypes ();
    
    /**
     * Returns name of language for given mime type.
     *
     * @param mimeType The mime type to translate.
     * 
     * @return name of language for given mime type
     */
    public abstract String getLanguageName (String mimeType);

    
    // FontColors ..............................................................
    
    /** Property name constant. */
    public static final String PROP_CURRENT_FONT_COLOR_PROFILE = "currentFontColorProfile";
    
    /** 
     * The name of the property change event for 'All Languages' font and colors.
     * 
     * @deprecated This has always been meant for internal use only. As per
     * general contract clients should listen on mime type specific Lookup for
     * changes in editor settings.
     */
    public static final String PROP_DEFAULT_FONT_COLORS = "defaultFontColors"; //NOI18N
    
    /** 
     * The name of the property change event for 'Highlighting' font and colors.
     * 
     * @deprecated This has always been meant for internal use only. As per
     * general contract clients should listen on mime type specific Lookup for
     * changes in editor settings.
     */
    public static final String PROP_EDITOR_FONT_COLORS = EditorSettingsImpl.PROP_EDITOR_FONT_COLORS;

    /**
     * @since 1.21
     */
    public static final String PROP_MIME_TYPES = MimeTypesTracker.PROP_MIME_TYPES;
    
    /**
     * Returns set of font & colors profiles.
     *
     * @return set of font & colors profiles
     */
    public abstract Set<String> getFontColorProfiles ();
    
    /**
     * Returns true for user defined profile.
     *
     * @param profile a profile name
     * @return true for user defined profile
     */
    public abstract boolean isCustomFontColorProfile (String profile);
    
    /**
     * Returns name of current font & colors profile.
     *
     * @return name of current font & colors profile
     */
    public abstract String getCurrentFontColorProfile ();
    
    /**
     * Sets current font & colors profile.
     *
     * @param profile a profile name
     */
    public abstract void setCurrentFontColorProfile (String profile);
    
    /**
     * Returns font & color defaults for given profile or null, if the profile
     * is unknown .
     *
     * @param profile a profile name
     * @return font & color defaults for given profile or null
     * 
     * @deprecated Use getFontColorSettings(new String[0]).getAllFontColors(profile) instead.
     */
    public abstract Collection<AttributeSet> getDefaultFontColors (
	String profile
    );
    
    /**
     * Returns default values for font & color defaults for given profile 
     * or null, if the profile is unknown.
     *
     * @param profile a profile name
     * @return font & color defaults for given profile or null
     * 
     * @deprecated Use getFontColorSettings(new String[0]).getAllFontColorsDefaults(profile) instead.
     */
    public abstract Collection<AttributeSet> getDefaultFontColorDefaults (
	String profile
    );
    
    /**
     * Sets font & color defaults for given profile.
     *
     * @param profile a profile name
     * @param fontColors font & color defaults to be used
     * 
     * @deprecated Use getFontColorSettings(new String[0]).setAllFontColors(profile, fontColors) instead.
     */
    public abstract void setDefaultFontColors (
	String profile,
	Collection<AttributeSet> fontColors
    );
    
    /**
     * Returns highlighting properties for given profile or null, if the 
     * profile is not known.
     *
     * @param profile a profile name
     * @return highlighting properties for given profile or null
     */
    public abstract Map<String, AttributeSet> getHighlightings (
	String profile
    );
    
    /**
     * Returns defaults for highlighting properties for given profile,
     * or null if the profile is not known.
     *
     * @param profile a profile name
     * @return highlighting properties for given profile or null
     */
    public abstract Map<String, AttributeSet> getHighlightingDefaults (
	String profile
    );
    
    /**
     * Sets highlighting properties for given profile.
     *
     * @param profile a profile name
     * @param highlightings a highlighting properties to be used
     */
    public abstract void setHighlightings (
	String profile,
	Map<String, AttributeSet> highlightings
    );
    
    /**
     * Returns annotations properties for given profile or null, if the 
     * profile is not known.
     *
     * @param profile a profile name
     * @return annotations properties for given profile or null
     */
    public abstract Map<String, AttributeSet> getAnnotations (
	String profile
    );
    
    /**
     * Returns defaults for annotation properties for given profile,
     * or null if the profile is not known.
     *
     * @param profile a profile name
     * @return annotation properties for given profile or null
     */
    public abstract Map<String, AttributeSet> getAnnotationDefaults (
	String profile
    );
    
    /**
     * Sets annotation properties for given profile.
     *
     * @param profile a profile name
     * @param annotations a annotation properties to be used
     */
    public abstract void setAnnotations (
	String profile,
	Map<String, AttributeSet> annotations
    );
    
    /**
     * Returns FontColorSettings for given mimetypes.
     *
     * @param mimeTypes The mime path to get the settings for.
     * 
     * @return FontColorSettings for given mimetypes
     */
    public abstract FontColorSettingsFactory getFontColorSettings (String[] mimeTypes);
    
    
    // KeyMaps .................................................................
    
    /** Property name constant. */
    public static final String PROP_CURRENT_KEY_MAP_PROFILE = "currentKeyMapProfile";

    
    /**
     * Returns KeyBindingSettings for given mimetypes.
     *
     * @param mimeTypes The mime path to get the settings for.
     * 
     * @return KeyBindingSettings for given mimetypes
     */
    public abstract KeyBindingSettingsFactory getKeyBindingSettings (String[] mimeTypes);
    
    /**
     * Returns set of keymap profiles.
     *
     * @return set of font & colors profiles
     */
    public abstract Set<String> getKeyMapProfiles ();
    
    /**
     * Returns true for user defined profile.
     *
     * @param profile a profile name
     * @return true for user defined profile
     */
    public abstract boolean isCustomKeymapProfile (String profile);
    
    /**
     * Returns name of current keymap profile.
     *
     * @return name of current keymap profile
     */
    public abstract String getCurrentKeyMapProfile ();
    
    /**
     * Sets current keymap profile.
     *
     * @param profile a profile name
     */
    public abstract void setCurrentKeyMapProfile (String profile);

    // Other .................................................................
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be registerred
     */
    public abstract void addPropertyChangeListener (
        PropertyChangeListener l
    );
    
    /**
     * PropertyChangeListener registration.
     *
     * @param l a PropertyChangeListener to be unregisterred
     */
    public abstract void removePropertyChangeListener (
        PropertyChangeListener l
    );
    
    /**
     * PropertyChangeListener registration.
     *
     * @param propertyName  The name of the property to listen on.
     * @param l a PropertyChangeListener to be registerred
     */
    public abstract void addPropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    );
    
    /**
     * PropertyChangeListener registration.
     *
     * @param propertyName  The name of the property to listen on.
     * @param l a PropertyChangeListener to be unregisterred
     */
    public abstract void removePropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    );
}
