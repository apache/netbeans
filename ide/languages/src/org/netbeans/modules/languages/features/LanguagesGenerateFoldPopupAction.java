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

package org.netbeans.modules.languages.features;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.text.JTextComponent;

import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.editor.NbEditorKit.GenerateFoldPopupAction;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;


/**
 *
 * @author Jan Jancura
 */
public class LanguagesGenerateFoldPopupAction extends GenerateFoldPopupAction {

    public static final String EXPAND_PREFIX = "Expand:";
    public static final String COLLAPSE_PREFIX = "Collapse:";
    
    protected void addAdditionalItems (JTextComponent target, JMenu menu) {
        try {
            String mimeType = (java.lang.String) target.getDocument ().getProperty ("mimeType");
            Language l = LanguagesManager.getDefault ().getLanguage (mimeType);
            Set expands = new HashSet ();
            addFoldTypes (target, menu, l, expands);
            Iterator<Language> it = l.getImportedLanguages ().iterator ();
            while (it.hasNext ())
                addFoldTypes (target, menu, it.next (), expands);
        } catch (ParseException ex) {
        }
    }

    private void addFoldTypes (JTextComponent target, JMenu menu, Language language, Set expands) {
        List<Feature> features = language.getFeatureList ().getFeatures (LanguagesFoldManager.FOLD);
        Iterator<Feature> it = features.iterator ();
        while (it.hasNext ()) {
            Feature fold = it.next ();
            String expand = LocalizationSupport.localize (language, (String) fold.getValue ("expand_type_action_name"));
            if (expand == null) continue;
            if (expands.contains (expand))
                continue;
            expands.add (expand);
            String collapse = LocalizationSupport.localize (language, (String) fold.getValue ("collapse_type_action_name"));
            if (collapse == null) continue;
            addAction (target, menu, EXPAND_PREFIX + expand);
            addAction (target, menu, COLLAPSE_PREFIX + collapse);
            setAddSeparatorBeforeNextAction (true);
        }
    }
}
    
