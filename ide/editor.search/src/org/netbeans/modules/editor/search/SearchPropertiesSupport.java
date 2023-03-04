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
package org.netbeans.modules.editor.search;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

public final class SearchPropertiesSupport {

    private static final String PREFS_NODE = "SearchProperties";  //NOI18N
    private static SearchPropertiesSupport instance = null;
    private static final Preferences prefs = NbPreferences.forModule(SearchPropertiesSupport.class).node(PREFS_NODE);
    private static final String SEARCH_ID = "searchprops";  //NOI18N
    private static SearchProperties searchProps;
    private static final String REPLACE_ID = "replaceprops";  //NOI18N
    private static SearchProperties replaceProps;
    private static final List<String> EDITOR_FIND_SUPPORT_CONSTANTS = Arrays.asList(EditorFindSupport.FIND_MATCH_CASE, EditorFindSupport.FIND_WHOLE_WORDS, EditorFindSupport.FIND_REG_EXP, EditorFindSupport.FIND_WRAP_SEARCH, EditorFindSupport.FIND_PRESERVE_CASE);

    private SearchPropertiesSupport() {
    }

    private static synchronized SearchPropertiesSupport getInstance() {
        if (instance == null) {
            instance = new SearchPropertiesSupport();
        }
        return instance;
    }

    private Preferences getPrefs() {
        return prefs;
    }

    public static synchronized SearchProperties getSearchProperties() {
        if (searchProps == null) {
            searchProps = createDefaultSearchProperties();
        }
        return searchProps;
    }

    public static synchronized SearchProperties getReplaceProperties() {
        if (replaceProps == null) {
            replaceProps = createDefaultReplaceProperties();
        }
        return replaceProps;
    }

    private static SearchProperties createDefaultSearchProperties() {
        Map<String, Object> props = EditorFindSupport.getInstance().createDefaultFindProperties();
        for (String constant : EDITOR_FIND_SUPPORT_CONSTANTS) {
            props.put(constant, Boolean.parseBoolean(getInstance().getPrefs().get(SEARCH_ID + constant, props.get(constant).toString())));
        }
        return new SearchProperties(props, SEARCH_ID);
    }

    private static SearchProperties createDefaultReplaceProperties() {
        Map<String, Object> props = EditorFindSupport.getInstance().createDefaultFindProperties();
        props.put(EditorFindSupport.FIND_MATCH_CASE, Boolean.TRUE);
        for (String constant : EDITOR_FIND_SUPPORT_CONSTANTS) {
            props.put(constant, Boolean.parseBoolean(getInstance().getPrefs().get(REPLACE_ID + constant, props.get(constant).toString())));
        }
        return new SearchProperties(props, REPLACE_ID);
    }

    public static final class SearchProperties {
        private final Map<String, Object> props;
        private final String id;
        private SearchProperties(Map<String, Object> props, String identification) {
            this.props = props;
            this.id = identification;
        }
        
        public synchronized void saveToPrefs() {
            for (Map.Entry<String, Object> entry : props.entrySet()) {
                String editorFindSupportProperty = entry.getKey();
                Object value = entry.getValue();
                if (value != null) {
                    getInstance().getPrefs().put(id + editorFindSupportProperty, value.toString());
                } else {
                    getInstance().getPrefs().remove(id + editorFindSupportProperty);
                }
            }
        }

        public void setProperty(String editorFindSupportProperty, Object value) {
            if (editorFindSupportProperty.equals(EditorFindSupport.FIND_HIGHLIGHT_SEARCH)) {
                 EditorFindSupport.getInstance().putFindProperty(editorFindSupportProperty, value);
            }
            synchronized (this) {
                props.put(editorFindSupportProperty, value);
            }
        }

        public synchronized Object getProperty(String editorFindSupportProperty) {
            props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, EditorFindSupport.getInstance().getFindProperty(EditorFindSupport.FIND_HIGHLIGHT_SEARCH));
            return props.get(editorFindSupportProperty);
        }

        public synchronized Map<String, Object> getProperties() {
            props.put(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, EditorFindSupport.getInstance().getFindProperty(EditorFindSupport.FIND_HIGHLIGHT_SEARCH));
            return props;
        }
    }
}
