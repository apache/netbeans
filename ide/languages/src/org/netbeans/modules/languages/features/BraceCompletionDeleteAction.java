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

import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtKit.ExtDeleteCharAction;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.Utils;
import org.openide.ErrorManager;


/**
 *
 * @author Jan Jancura
 */
public class BraceCompletionDeleteAction extends ExtDeleteCharAction {

    public BraceCompletionDeleteAction () {
        super ("delete-previous", false);
    }

    protected void charBackspaced (
        BaseDocument        document, 
        int                 offset, 
        Caret               caret, 
        char                character
    ) throws BadLocationException {
        TokenSequence tokenSequence = Utils.getTokenSequence (document, offset);
        if (tokenSequence != null) {
            String mimeType = tokenSequence.language ().mimeType ();
            try {
                Language l = LanguagesManager.getDefault ().getLanguage (mimeType);
                List<Feature> completes = l.getFeatureList ().getFeatures ("COMPLETE");
                Iterator<Feature> it = completes.iterator ();
                while (it.hasNext ()) {
                    Feature complete = it.next ();
                    if (complete.getType () != Feature.Type.STRING)
                        continue;
                    String s = (String) complete.getValue ();
                    int i = s.indexOf (':');
                    if (i != 1) continue;
                    String ss = document.getText (
                        caret.getDot (), 
                        s.length () - i - 1
                    );
                    if (s.endsWith (ss) && 
                        s.charAt (0) == character
                    ) {
                        document.remove (caret.getDot (), s.length () - i - 1);
                        return;
                    }
                }
            } catch (LanguageDefinitionNotFoundException ex) {
                // ignore the exception
            }
        }
        super.charBackspaced (document, offset, caret, character);
    }
}
