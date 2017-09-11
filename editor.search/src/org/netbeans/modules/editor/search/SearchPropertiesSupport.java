/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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

    private synchronized static SearchPropertiesSupport getInstance() {
        if (instance == null) {
            instance = new SearchPropertiesSupport();
        }
        return instance;
    }

    private Preferences getPrefs() {
        return prefs;
    }

    public synchronized static SearchProperties getSearchProperties() {
        if (searchProps == null) {
            searchProps = createDefaultSearchProperties();
        }
        return searchProps;
    }

    public synchronized static SearchProperties getReplaceProperties() {
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
            for (String editorFindSupportProperty : props.keySet()) {
                Object value = props.get(editorFindSupportProperty);
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
