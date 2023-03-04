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
package org.netbeans.modules.csl.core;

import java.util.Map;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.Acceptor;
import org.netbeans.modules.editor.settings.storage.spi.StorageFilter;
import org.netbeans.modules.editor.settings.storage.spi.TypedValue;


/**
 * The classes in here no one should ever implement, as I would think all this
 * information could be defined in a more declarative way: either via a simple interface
 * implementation where returning specific flags enables a set of settings
 * or a table or some external xml-like file.
 * Maybe that is all there already in NetBeans but I could not find it.
 * This is called from the ModuleInstall class and it's key for the editor to work.
 *
 * vstejskal:
 * - all the settings defined in afterLoad can be suppied through an
 *   XML file registered in module's layer in Editors/&lt;mime-type&gt;/Preferences/Defaults.
 *   The format of the file is described in http://www.netbeans.org/dtds/EditorPreferences-1_0.dtd
 * - the values supplied from afterLoad are the default values for __all__ GSF based languages,
 *   but each language cna override them by registering its own values throught
 *   the XML file described above
 */
public class GsfEditorSettings extends StorageFilter<String, TypedValue> {

    public GsfEditorSettings() {
        super("Preferences"); //NOI18N
    }

    // -----------------------------------------------------------------------
    // StorageFilter implementation
    // -----------------------------------------------------------------------
    
    @Override
    public void afterLoad(Map<String, TypedValue> map, MimePath mimePath, String profile, boolean defaults) {
        if (mimePath.size() == 1) {
            if (null != LanguageRegistry.getInstance().getLanguageByMimeType(mimePath.getPath())) {
                // this is a GSF language
                if (!map.containsKey("word-match-match-case")) { //NOI18N
                    map.put("word-match-match-case", new TypedValue("true", Boolean.class.getName())); //NOI18N
                }
                if (!map.containsKey("reindent-with-text-before")) { //NOI18N
                    map.put("reindent-with-text-before", new TypedValue("false", Boolean.class.getName())); //NOI18N
                }
                if (!map.containsKey("abbrev-reset-acceptor")) { //NOI18N
                    map.put("abbrev-reset-acceptor", new TypedValue(getClass().getName() + ".getAbbrevResetAcceptor", "methodvalue")); //NOI18N
                }
            }
        }
    }

    @Override
    public void beforeSave(Map<String, TypedValue> map, MimePath mimePath, String profile, boolean defaults) {
        // save everything
    }

    public static final Acceptor defaultAbbrevResetAcceptor = new Acceptor() {
        public final boolean accept(char ch) {
            return !Character.isJavaIdentifierPart(ch) && ch != ':' && ch != '-' && ch != '=' && ch != '#'; //NOI18N
        }
    };
    public static Acceptor getAbbrevResetAcceptor(MimePath mimePath, String settingName) {
        return defaultAbbrevResetAcceptor;
    }

//    public void updateSettingsMap(Class kitClass, Map settingsMap) {
//        if (kitClass == null) {
//            return;
//        }
//
//        if (kitClass == GsfEditorKitFactory.GsfEditorKit.class) {
//            // This is wrong; I should be calling Formatter.indentSize() to get the default,
//            // but I can't get to the mime type from here. In 6.0 the editor settings are
//            // being redone so I can hopefully fix this soon.
//            settingsMap.put(SettingsNames.SPACES_PER_TAB, Integer.valueOf(2));
//            //settingsMap.put(SettingsNames.INDENT_SHIFT_WIDTH, Integer.valueOf(2));
////            settingsMap.put(ExtSettingsNames.CARET_SIMPLE_MATCH_BRACE, Boolean.FALSE);
////            settingsMap.put(ExtSettingsNames.HIGHLIGHT_MATCH_BRACE, Boolean.TRUE);
//            settingsMap.put(SettingsNames.WORD_MATCH_MATCH_CASE, Boolean.TRUE);
//            settingsMap.put(ExtSettingsNames.REINDENT_WITH_TEXT_BEFORE, Boolean.FALSE);
////            settingsMap.put(ExtSettingsNames.COMPLETION_AUTO_POPUP, Boolean.TRUE);
////            settingsMap.put(SettingsNames.PAIR_CHARACTERS_COMPLETION, Boolean.TRUE);
//                    
//            settingsMap.put(SettingsNames.ABBREV_RESET_ACCEPTOR, defaultAbbrevResetAcceptor);
//
//        }
//    }
}
