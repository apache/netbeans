/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.editor.options;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.editor.api.CodeStyle;

/**
 *
 */
public class PreviewPreferencesModel {

    public enum Filter {
        TabsAndIndents,
        Alignment,
        Braces,
        BlankLines,
        Spaces,
        All
    }

    private final Map<CodeStyle.Language, Map<String, PreviewPreferences>> preferences = new ConcurrentHashMap<CodeStyle.Language, Map<String, PreviewPreferences>>();
    private final Map<CodeStyle.Language, String> defaultStyles = new ConcurrentHashMap<CodeStyle.Language, String>();

    public PreviewPreferencesModel() {
    }

    public Map<String, PreviewPreferences> getPrefences(CodeStyle.Language language) {
        return preferences.get(language);
    }

    public synchronized void initLanguageMap(CodeStyle.Language language) {
        for (String style : EditorOptions.getAllStyles(language)) {
            initLanguageStylePreferences(language, style);
        }
        String def = defaultStyles.get(language);
        if (def == null) {
            def = EditorOptions.getCurrentProfileId(language, null);
            defaultStyles.put(language, def);
        }
    }

    public Map<String, PreviewPreferences> getLanguagePreferences(CodeStyle.Language language) {
        return preferences.get(language);
    }

    public String getLanguageDefaultStyle(CodeStyle.Language language) {
        return defaultStyles.get(language);
    }

    public void setLanguageDefaultStyle(CodeStyle.Language language, String def) {
        defaultStyles.put(language, def);
    }

    private void initLanguageStylePreferences(CodeStyle.Language language, String styleId) {
        Map<String, PreviewPreferences> prefs = preferences.get(language);
        if (prefs == null) {
            prefs = new HashMap<String, PreviewPreferences>();
            preferences.put(language, prefs);
        }
        PreviewPreferences clone = prefs.get(styleId);
        if (clone == null) {
            clone = new PreviewPreferences(EditorOptions.getPreferences(language, styleId), language, styleId);
            prefs.put(styleId, clone);
        }
    }

    public Map<String, PreviewPreferences> clonePreferences(CodeStyle.Language language) {
        Map<String, PreviewPreferences> newAllPreferences = new HashMap<String, PreviewPreferences>();
        for (Map.Entry<String, PreviewPreferences> entry2 : getLanguagePreferences(language).entrySet()) {
            PreviewPreferences pref = entry2.getValue();
            PreviewPreferences newPref = new PreviewPreferences(pref, language, entry2.getKey());
            newAllPreferences.put(entry2.getKey(), newPref);
        }
        return newAllPreferences;
    }

    public void resetPreferences(CodeStyle.Language language, Map<String, PreviewPreferences> newPreferences) {
        preferences.put(language, newPreferences);
    }

    public void clear(CodeStyle.Language language) {
        preferences.remove(language);
        defaultStyles.remove(language);
    }
}
