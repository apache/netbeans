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
package org.netbeans.modules.editor.lib;

import org.netbeans.modules.editor.lib2.EditorPreferencesKeys;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenID;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
public final class ColoringMap {

    private static final Logger LOG = Logger.getLogger(ColoringMap.class.getName());
    
    public static final String PROP_COLORING_MAP = "ColoringMap.PROP_COLORING_MAP"; //NOI18N

    public static ColoringMap get(String mimeType) {
        if (!IN_GET.get()) {
            IN_GET.set(true);
            try {
                return getInternal(mimeType);
            } finally {
                IN_GET.set(false);
            }
        } else {
            return EMPTY;
        }
    }
    
    public Map<String, Coloring> getMap() {
        synchronized (LOCK) {
            if (map == null) {
                map = loadTheMap(
                    legacyNonTokenColoringNames, 
                    lexerLanguage, 
                    syntaxLanguages, 
                    lookupResult.allInstances()
                );
            }
            
            return map;
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        PCS.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        PCS.removePropertyChangeListener(l);
    }
    
    // ---------------------------------------------------------
    // Private implementation
    // ---------------------------------------------------------

    private static final Map<MimePath, ColoringMap> CACHE = new WeakHashMap<MimePath, ColoringMap>();
    private static final ColoringMap EMPTY = new ColoringMap();
    private static final ThreadLocal<Boolean> IN_GET = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    private final List<String> legacyNonTokenColoringNames;
    private final Language<?> lexerLanguage;
    private final List<? extends TokenContext> syntaxLanguages;
    private final Lookup.Result<FontColorSettings> lookupResult;
    private final LookupListener lookupListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            synchronized (LOCK) {
                map = null;
            }
            
            PCS.firePropertyChange(PROP_COLORING_MAP, null, null);
        }
    };
    
    private final PropertyChangeSupport PCS = new PropertyChangeSupport(this);
    
    private final String LOCK = new String("ColoringMap.LOCK"); //NOI18N
    
    private Map<String, Coloring> map = null;
    
    private ColoringMap() {
        this.legacyNonTokenColoringNames = null;
        this.lexerLanguage = null;
        this.syntaxLanguages = null;
        this.lookupResult = null;
        this.map = Collections.<String, Coloring>emptyMap();
    }
    
    private ColoringMap(
        List<String> legacyNonTokenColoringNames,
        Language<?> lexerLanguage, 
        List<? extends TokenContext> syntaxLanguages, 
        Lookup.Result<FontColorSettings> lookupResult
    ) {
        this.legacyNonTokenColoringNames = legacyNonTokenColoringNames;
        this.lexerLanguage = lexerLanguage;
        this.syntaxLanguages = syntaxLanguages;
        this.lookupResult = lookupResult;
        
        this.map = loadTheMap(
            legacyNonTokenColoringNames, 
            lexerLanguage, 
            syntaxLanguages, 
            lookupResult.allInstances()
        );
        
        this.lookupResult.addLookupListener(WeakListeners.create(LookupListener.class, lookupListener, this.lookupResult));
    }

    private static ColoringMap getInternal(String mimeType) {
        MimePath mimePath = mimeType == null || mimeType.length() == 0 ? 
            MimePath.EMPTY : MimePath.parse(mimeType);
        
        synchronized (CACHE) {
            ColoringMap cm = CACHE.get(mimePath);
            
            if (cm != null) {
                return cm;
            }
        }

        List<String> legacyNonTokenColoringNames = findLegacyNonTokenColoringNames(mimePath);
        Lookup.Result<FontColorSettings> lookupResult = 
            MimeLookup.getLookup(mimePath).lookupResult(FontColorSettings.class);

        Language<?> lexerLanguage = null;
        List<? extends TokenContext> syntaxLanguage = null;

        if (mimePath.size() > 0) {
            lexerLanguage = Language.find(mimePath.getPath());
            syntaxLanguage = findSyntaxLanguage(mimePath);
        }

        LOG.fine("Creating ColoringMap for '" + mimeType + "' ---------------------------"); //NOI18N
        ColoringMap myCm = new ColoringMap(
            legacyNonTokenColoringNames, 
            lexerLanguage, 
            syntaxLanguage, 
            lookupResult
        );
        LOG.fine("----------------------------------------------------------------------"); //NOI18N
                
        synchronized (CACHE) {
            ColoringMap cm = CACHE.get(mimePath);
            
            if (cm == null) {
                cm = myCm;
                CACHE.put(mimePath, cm);
            }
            
            return cm;
        }
    }
    
    private static Map<String, Coloring> loadTheMap(
        List<String> legacyNonTokenColoringNames,
        Language<?> lexerLanguage, 
        List<? extends TokenContext> syntaxLanguages, 
        Collection<? extends FontColorSettings> fontsColors
    ) {
        HashMap<String, Coloring> coloringMap = new HashMap<String, Coloring>();
        
        if (!fontsColors.isEmpty()) {
            FontColorSettings fcs = fontsColors.iterator().next();
        
            if (legacyNonTokenColoringNames != null) {
                collectLegacyNonTokenColorings(coloringMap, legacyNonTokenColoringNames, fcs);
            }
            
            collectNonTokenColorings(coloringMap, fcs);
            
            if (syntaxLanguages != null) {
                collectLegacyTokenColorings(coloringMap, syntaxLanguages, fcs);
            }
            
            if (lexerLanguage != null) {
                collectTokenColorings(coloringMap, lexerLanguage, fcs);
            }
        }
        
        return Collections.unmodifiableMap(coloringMap);
    }

    private static final List<String> FONT_COLOR_NAMES_COLORINGS = 
            Arrays.asList(
            FontColorNames.DEFAULT_COLORING, 
            FontColorNames.LINE_NUMBER_COLORING, 
            FontColorNames.GUARDED_COLORING, 
            FontColorNames.CODE_FOLDING_COLORING, 
            FontColorNames.CODE_FOLDING_BAR_COLORING,
            FontColorNames.SELECTION_COLORING, 
            FontColorNames.HIGHLIGHT_SEARCH_COLORING, 
            FontColorNames.INC_SEARCH_COLORING,
            FontColorNames.BLOCK_SEARCH_COLORING,
            FontColorNames.STATUS_BAR_COLORING,
            FontColorNames.STATUS_BAR_BOLD_COLORING,
            FontColorNames.CARET_ROW_COLORING,
            FontColorNames.TEXT_LIMIT_LINE_COLORING,
            FontColorNames.CARET_COLOR_INSERT_MODE,
            FontColorNames.CARET_COLOR_OVERWRITE_MODE,
            FontColorNames.DOCUMENTATION_POPUP_COLORING);
    private static void collectNonTokenColorings(
        HashMap<String, Coloring> coloringMap, 
        FontColorSettings fcs
    ) {
        for (String coloringName: FONT_COLOR_NAMES_COLORINGS) {
            AttributeSet attribs = fcs.getFontColors(coloringName);
            if (attribs != null) {
                LOG.fine("Loading coloring '" + coloringName + "'"); //NOI18N
                coloringMap.put(coloringName, Coloring.fromAttributeSet(attribs));
            }
        }
    }

    private static void collectLegacyNonTokenColorings(
        HashMap<String, Coloring> coloringMap, 
        List<String> legacyNonTokenColoringNames,
        FontColorSettings fcs
    ) {
        for (int i = legacyNonTokenColoringNames.size() - 1; i >= 0; i--) {
            String coloringName = legacyNonTokenColoringNames.get(i);
            AttributeSet attribs = fcs.getFontColors(coloringName);
            if (attribs != null) {
                LOG.fine("Loading legacy coloring '" + coloringName + "'"); //NOI18N
                coloringMap.put(coloringName, Coloring.fromAttributeSet(attribs));
            }
        }
    }
    
    private static void collectTokenColorings(
        HashMap<String, Coloring> coloringMap, 
        Language<?> lexerLanguage,
        FontColorSettings fcs
    ) {
        // Add token-categories colorings
        for (String category : lexerLanguage.tokenCategories()) {
            AttributeSet attribs = fcs.getTokenFontColors(category);
            if (attribs != null) {
                LOG.fine("Loading token coloring '" + category + "'"); //NOI18N
                coloringMap.put(category, Coloring.fromAttributeSet(attribs));
            }
        }

        // Add token-ids colorings
        for (TokenId tokenId : lexerLanguage.tokenIds()) {
            AttributeSet attribs = fcs.getTokenFontColors(tokenId.name());
            if (attribs != null) {
                LOG.fine("Loading token coloring '" + tokenId.name() + "'"); //NOI18N
                coloringMap.put(tokenId.name(), Coloring.fromAttributeSet(attribs));
            }
        }
    }
    
    private static void collectLegacyTokenColorings(
        HashMap<String, Coloring> coloringMap, 
        List<? extends TokenContext> tokenContextList, 
        FontColorSettings fcs
    ) {
        for (int i = tokenContextList.size() - 1; i >= 0; i--) {
            TokenContext tc = tokenContextList.get(i);
            TokenContextPath[] allPaths = tc.getAllContextPaths();
            for (int j = 0; j < allPaths.length; j++) {
                TokenContext firstContext = allPaths[j].getContexts()[0];

                // Add token-categories colorings
                TokenCategory[] tokenCategories = firstContext.getTokenCategories();
                for (int k = 0; k < tokenCategories.length; k++) {
                    String fullName = allPaths[j].getFullTokenName(tokenCategories[k]);
                    AttributeSet attribs = fcs.getTokenFontColors(fullName);
                    if (attribs != null) {
                        LOG.fine("Loading legacy token coloring '" + fullName + "'"); //NOI18N
                        coloringMap.put(fullName, Coloring.fromAttributeSet(attribs));
                    }
                }

                // Add token-ids colorings
                TokenID[] tokenIDs = firstContext.getTokenIDs();
                for (int k = 0; k < tokenIDs.length; k++) {
                    String fullName = allPaths[j].getFullTokenName(tokenIDs[k]);
                    AttributeSet attribs = fcs.getTokenFontColors(fullName);
                    if (attribs != null) {
                        LOG.fine("Loading legacy token coloring '" + fullName + "'"); //NOI18N
                        coloringMap.put(fullName, Coloring.fromAttributeSet(attribs));
                    }
                }
            }
        }        
    }

    private static List<String> findLegacyNonTokenColoringNames(MimePath mimePath) {
        List<String> legacyNonTokenColoringNames = new ArrayList<>();

        Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
        if (prefs != null) {
            String namesList = prefs.get(EditorPreferencesKeys.COLORING_NAME_LIST, null); //NOI18N

            if (namesList != null && namesList.length() > 0) {


                for (StringTokenizer t = new StringTokenizer(namesList, ","); t.hasMoreTokens();) { //NOI18N
                    String coloringName = t.nextToken().trim();
                    legacyNonTokenColoringNames.add(coloringName);
                }
            }
        }
        
        return legacyNonTokenColoringNames;
    }
    
    private static List<? extends TokenContext> findSyntaxLanguage(MimePath mimePath) {
        Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
        
        @SuppressWarnings("unchcecked")
        List<? extends TokenContext> languages = (List<? extends TokenContext>) SettingsConversions.callFactory(
                prefs, mimePath, "token-context-list", null); //NOI18N
        
        return languages;
    }
}
