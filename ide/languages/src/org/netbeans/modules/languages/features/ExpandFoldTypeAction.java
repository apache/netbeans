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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.languages.ParseException;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;


/**
 *
 * @author Jan Jancura
 */
public class ExpandFoldTypeAction extends BaseAction {

    public ExpandFoldTypeAction (String name) {
        super (name);
        //putValue(SHORT_DESCRIPTION, NbBundle.getBundle(JavaKit.class).getString("expand-all-code-block-folds"));
        //putValue(BaseAction.POPUP_MENU_TEXT, NbBundle.getBundle(JavaKit.class).getString("popup-expand-all-code-block-folds"));
    }

    public void actionPerformed (ActionEvent evt, JTextComponent target) {
        FoldHierarchy hierarchy = FoldHierarchy.get (target);
        // Hierarchy locking done in the utility method
        try {
            String mimeType = (java.lang.String) target.getDocument ().getProperty ("mimeType");
            Language l = LanguagesManager.getDefault ().getLanguage (mimeType);
            if (expand (hierarchy, l)) return;
            Iterator<Language> it = l.getImportedLanguages ().iterator ();
            while (it.hasNext ())
                if (expand (hierarchy, it.next ()))
                    return;
        } catch (ParseException ex) {
        }
    }

    private boolean expand (FoldHierarchy hierarchy, Language language) {
        List<Feature> folds = language.getFeatureList ().getFeatures (LanguagesFoldManager.FOLD);
        Iterator<Feature> it = folds.iterator ();
        while (it.hasNext ()) {
            Feature fold = it.next ();
            String expand = LocalizationSupport.localize (language, (String) fold.getValue ("expand_type_action_name"));
            if (expand == null) continue;
            if (!expand.equals (getValue (NAME)))
                continue;
            String collapse = LocalizationSupport.localize (language, (String) fold.getValue ("collapse_type_action_name"));
            if (collapse == null) continue;
            List<FoldType> types = new ArrayList<FoldType> ();
            types.add (Folds.getFoldType (collapse));
            FoldUtilities.expand (hierarchy, types);
            return true;
        }
        return false;
    }
}

