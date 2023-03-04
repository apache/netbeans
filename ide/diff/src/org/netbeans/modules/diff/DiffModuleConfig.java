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
package org.netbeans.modules.diff;

import org.openide.util.NbPreferences;
import org.openide.util.Lookup;
import org.netbeans.spi.diff.DiffProvider;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;

import java.util.prefs.Preferences;
import java.util.*;
import java.awt.Color;
import javax.swing.UIManager;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;

/**
 * Module settings for Diff module.
 * 
 * @author Maros Sandor
 */
public class DiffModuleConfig {
                                                                                             
    private static final String PREF_IGNORE_LEADINGTRAILING_WHITESPACE = "ignoreWhitespace"; // NOI18N
    private static final String PREF_IGNORE_INNER_WHITESPACE = "ignoreInnerWhitespace"; // NOI18N
    private static final String PREF_IGNORE_CASE = "ignoreCase"; // NOI18N
    private static final String PREF_ADDED_COLOR = "addedColor"; // NOI18N
    private static final String PREF_CHANGED_COLOR = "changedColor"; // NOI18N
    private static final String PREF_DELETED_COLOR = "deletedColor"; // NOI18N
    private static final String PREF_MERGE_UNRESOLVED_COLOR = "merge.unresolvedColor"; // NOI18N
    private static final String PREF_MERGE_APPLIED_COLOR = "merge.appliedColor"; // NOI18N
    private static final String PREF_MERGE_NOTAPPLIED_COLOR = "merge.notappliedColor"; // NOI18N
    private static final String PREF_SIDEBAR_DELETED_COLOR = "sidebar.deletedColor"; //NOI18N
    private static final String PREF_SIDEBAR_CHANGED_COLOR = "sidebar.changedColor"; //NOI18N
    
    private static final DiffModuleConfig INSTANCE = new DiffModuleConfig();
    
    private final Color defaultAddedColor;
    private final Color defaultChangedColor;
    private final Color defaultDeletedColor;
    private final Color defaultAppliedColor;
    private final Color defaultNotAppliedColor;
    private final Color defaultUnresolvedColor;
    private final Color defaultSidebarDeletedColor;
    private final Color defaultSidebarChangedColor;

    public static DiffModuleConfig getDefault() {
        return INSTANCE;
    }

    private DiffModuleConfig() {
        Color c = UIManager.getColor( "nb.diff.added.color" ); //NOI18N
        if( null == c )
            c = new Color(180, 255, 180);
        defaultAddedColor = c;

        c = UIManager.getColor( "nb.diff.changed.color" ); //NOI18N
        if( null == c )
            c = new Color(160, 200, 255);
        defaultChangedColor = c;

        c = UIManager.getColor( "nb.diff.deleted.color" ); //NOI18N
        if( null == c )
            c = new Color(255, 160, 180);
        defaultDeletedColor = c;

        c = UIManager.getColor( "nb.diff.applied.color" ); //NOI18N
        if( null == c )
            c = new Color(180, 255, 180);
        defaultAppliedColor = c;

        c = UIManager.getColor( "nb.diff.notapplied.color" ); //NOI18N
        if( null == c )
            c = new Color(160, 200, 255);
        defaultNotAppliedColor = c;

        c = UIManager.getColor( "nb.diff.unresolved.color" ); //NOI18N
        if( null == c )
            c = new Color(255, 160, 180);
        defaultUnresolvedColor = c;

        c = UIManager.getColor( "nb.diff.sidebar.deleted.color" ); //NOI18N
        if( null == c )
            c = new Color(255, 225, 232);
        defaultSidebarDeletedColor = c;

        c = UIManager.getColor( "nb.diff.sidebar.changed.color" ); //NOI18N
        if( null == c )
            c = new Color(233, 241, 255);
        defaultSidebarChangedColor = c;
    }
    
    public Color getAddedColor() {
        return getColor(PREF_ADDED_COLOR, defaultAddedColor);
    }
    
    public Color getDefaultAddedColor() {
        return defaultAddedColor;
    }

    public Color getChangedColor() {
        return getColor(PREF_CHANGED_COLOR, defaultChangedColor);
    }

    public Color getDefaultChangedColor() {
        return defaultChangedColor;
    }

    public Color getDeletedColor() {
        return getColor(PREF_DELETED_COLOR, defaultDeletedColor);
    }

    public Color getDefaultDeletedColor() {
        return defaultDeletedColor;
    }

    public Color getAppliedColor() {
        return getColor(PREF_MERGE_APPLIED_COLOR, defaultAppliedColor);
    }

    public Color getDefaultAppliedColor() {
        return defaultAppliedColor;
    }

    public Color getNotAppliedColor() {
        return getColor(PREF_MERGE_NOTAPPLIED_COLOR, defaultNotAppliedColor);
    }

    public Color getDefaultNotAppliedColor() {
        return defaultNotAppliedColor;
    }

    public Color getUnresolvedColor() {
        return getColor(PREF_MERGE_UNRESOLVED_COLOR, defaultUnresolvedColor);
    }

    public Color getDefaultUnresolvedColor() {
        return defaultUnresolvedColor;
    }

    public Color getSidebarDeletedColor () {
        return getColor(PREF_SIDEBAR_DELETED_COLOR, defaultSidebarDeletedColor);
    }

    public Color getDefaultSidebarDeletedColor () {
        return defaultSidebarDeletedColor;
    }

    public Color getSidebarChangedColor () {
        return getColor(PREF_SIDEBAR_CHANGED_COLOR, defaultSidebarChangedColor);
    }

    public Color getDefaultSidebarChangedColor () {
        return defaultSidebarChangedColor;
    }
    
    public void setChangedColor(Color color) {
        putColor(PREF_CHANGED_COLOR, defaultChangedColor.equals(color) ? null : color);
    }

    public void setAddedColor(Color color) {
        putColor(PREF_ADDED_COLOR, defaultAddedColor.equals(color) ? null : color);
    }
   
    public void setDeletedColor(Color color) {
        putColor(PREF_DELETED_COLOR, defaultDeletedColor.equals(color) ? null : color);
    }

    public void setNotAppliedColor(Color color) {
        putColor(PREF_MERGE_NOTAPPLIED_COLOR, defaultNotAppliedColor.equals(color) ? null : color);
    }

    public void setAppliedColor(Color color) {
        putColor(PREF_MERGE_APPLIED_COLOR, defaultAppliedColor.equals(color) ? null : color);
    }

    public void setUnresolvedColor(Color color) {
        putColor(PREF_MERGE_UNRESOLVED_COLOR, defaultUnresolvedColor.equals(color) ? null : color);
    }

    public void setSidebarDeletedColor (Color color) {
        putColor(PREF_SIDEBAR_DELETED_COLOR, defaultSidebarDeletedColor.equals(color) ? null : color);
    }

    public void setSidebarChangedColor (Color color) {
        putColor(PREF_SIDEBAR_CHANGED_COLOR, defaultSidebarChangedColor.equals(color) ? null : color);
    }
    
    private void putColor(String key, Color color) {
        if (color == null) {
            getPreferences().remove(key);
        } else {
            getPreferences().putInt(key, color.getRGB());
        }
    }

    private Color getColor(String key, Color defaultColor) {
        int rgb = getPreferences().getInt(key, defaultColor.getRGB());
        return new Color(rgb);
    }
  
    public DiffProvider getDefaultDiffProvider() {
        Collection<? extends DiffProvider> providers = Lookup.getDefault().lookup(new Lookup.Template<>(DiffProvider.class)).allInstances();
        DiffProvider provider = null;
        for (DiffProvider p : providers) {
            provider = p;
            if (p instanceof BuiltInDiffProvider) {
                ((BuiltInDiffProvider) p).setOptions(getOptions());
                break;
            }
        }
        return provider;
    }

    public void setOptions(BuiltInDiffProvider.Options options) {
        getPreferences().putBoolean(PREF_IGNORE_LEADINGTRAILING_WHITESPACE, options.ignoreLeadingAndtrailingWhitespace);
        getPreferences().putBoolean(PREF_IGNORE_INNER_WHITESPACE, options.ignoreInnerWhitespace);
        getPreferences().putBoolean(PREF_IGNORE_CASE, options.ignoreCase);
        getBuiltinProvider().setOptions(options);
    }

    public BuiltInDiffProvider.Options getOptions() {
        BuiltInDiffProvider.Options options = new BuiltInDiffProvider.Options();
        options.ignoreLeadingAndtrailingWhitespace = getPreferences().getBoolean(PREF_IGNORE_LEADINGTRAILING_WHITESPACE, true);
        options.ignoreInnerWhitespace = getPreferences().getBoolean(PREF_IGNORE_INNER_WHITESPACE, false);
        options.ignoreCase = getPreferences().getBoolean(PREF_IGNORE_CASE, false);
        return options;
    }
        
    private BuiltInDiffProvider getBuiltinProvider() {
        Collection<? extends DiffProvider> diffs = Lookup.getDefault().lookupAll(DiffProvider.class);
        for (DiffProvider diff : diffs) {
            if (diff instanceof BuiltInDiffProvider) {
                return (BuiltInDiffProvider) diff;
            }
        }
        throw new IllegalStateException("No builtin diff provider");
    }

    public boolean isUseInteralDiff() {
        return true;
    }

    // properties ~~~~~~~~~~~~~~~~~~~~~~~~~

    public Preferences getPreferences() {
        return NbPreferences.forModule(DiffModuleConfig.class);
    }

    /**
     * Returns number of spaces replacing a tab in editor
     * @param mimeType
     * @return
     */
    public int getSpacesPerTabFor (String mimeType) {
        int spacesPerTab = 1;
        Preferences pref = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        if (pref != null) {
            spacesPerTab = pref.getInt(SimpleValueNames.TAB_SIZE, 1);
        }
        return spacesPerTab;
    }
}
