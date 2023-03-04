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

package org.netbeans.modules.options.generaleditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;


public class Model {

    // code folding options

    boolean isShowCodeFolding () {
        return getParameter (SimpleValueNames.CODE_FOLDING_ENABLE, true);
    }

    boolean isFoldImports () {
        return getParameter("code-folding-collapse-import", false); //NOI18N
    }
    
    boolean isFoldInitialComment () {
        return getParameter("code-folding-collapse-initial-comment", false); //NOI18N
    }
    
    boolean isFoldTag () {
        return getParameter("code-folding-collapse-tags", false); //NOI18N
    }

    boolean isFoldInnerClasses () {
        return getParameter("code-folding-collapse-innerclass", false); //NOI18N
    }
    
    boolean isFoldJavaDocComments () {
        return getParameter ("code-folding-collapse-javadoc", false); //NOI18N
    }
    
    boolean isFoldMethods () {
        return getParameter("code-folding-collapse-method", false); //NOI18N
    }
    
    void setFoldingOptions (
        boolean showCodeFolding,
        boolean foldImports,
        boolean foldInitialComent,
        boolean foldInnerClasses,
        boolean foldJavaDoc,
        boolean foldMethods,
        boolean foldTags
    ) {
        Collection<String> mimeTypes = new ArrayList<String>(EditorSettings.getDefault().getAllMimeTypes());
        mimeTypes.add("");
        for(String mimeType : mimeTypes) {
            Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
            
            prefs.putBoolean(SimpleValueNames.CODE_FOLDING_ENABLE, Boolean.valueOf(showCodeFolding));
            prefs.putBoolean("code-folding-collapse-import", foldImports); //NOI18N
            prefs.putBoolean("code-folding-collapse-initial-comment", foldInitialComent); //NOI18N
            prefs.putBoolean("code-folding-collapse-innerclass", foldInnerClasses); //NOI18N
            prefs.putBoolean("code-folding-collapse-javadoc", foldJavaDoc); //NOI18N
            prefs.putBoolean("code-folding-collapse-method", foldMethods); //NOI18N
            prefs.putBoolean("code-folding-collapse-tags", foldTags); //NOI18N
        }
    }
    
    Boolean isCamelCaseJavaNavigation() {
        Preferences p = NbPreferences.root ();
        if ( p == null ) {
            return null;
        }
        return p.getBoolean("useCamelCaseStyleNavigation", true) ? Boolean.TRUE : Boolean.FALSE; // NOI18N
    }
    
    void setCamelCaseNavigation(boolean value) {
        NbPreferences.root ().putBoolean("useCamelCaseStyleNavigation", value); // NOI18N
    }
    
    // braces outline settings
    Boolean isBraceOutline() {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.BRACE_SHOW_OUTLINE, true);
    }
    
    Boolean isBraceTooltip() {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.BRACE_FIRST_TOOLTIP, true);
    }
    
    void setBraceOutline(Boolean show) {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        prefs.putBoolean(SimpleValueNames.BRACE_SHOW_OUTLINE, show);
    }
    
    void setBraceTooltip(Boolean show) {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        prefs.putBoolean(SimpleValueNames.BRACE_FIRST_TOOLTIP, show);
    }
    
    String getEditorSearchType() {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        return prefs.get(SimpleValueNames.EDITOR_SEARCH_TYPE, "default");
    }

    void setEditorSearchType(String value) {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        prefs.put(SimpleValueNames.EDITOR_SEARCH_TYPE, value);
    }

    // private helper methods ..................................................
    
    private static final List<String> PRIVILEDGED_MIME_TYPES = Arrays.asList(new String [] {
        "text/x-java", //NOI18N
        "text/x-c++", //NOI18N
        "text/x-c", //NOI18N
        "text/x-ruby", //NOI18N
        "text/x-php5", //NOI18N
    });
    
    private boolean getParameter(String parameterName, boolean defaultValue) {
        Set<String> mimeTypes = EditorSettings.getDefault().getAllMimeTypes();
        List<String> list = new ArrayList<String>(PRIVILEDGED_MIME_TYPES);
        list.addAll(mimeTypes);
        
        for(String mimeType : list) {
            Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
            
            String value = prefs.get(parameterName, null);
            if (value != null) {
                return prefs.getBoolean(parameterName, false);
            }
        }
        
        return defaultValue;
    }
    
    private Preferences getJavaModulePreferenes() {
        try {
            ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
            Class accpClass = cl.loadClass("org.netbeans.modules.editor.java.AbstractCamelCasePosition"); // NOI18N
            if (accpClass == null) {
                return null;
            }
            return NbPreferences.forModule(accpClass);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
    
}


