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
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtKit.ExtDefaultKeyTypedAction;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Feature.Type;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.Utils;


/**
 *
 * @author Jan Jancura
 */
public class BraceCompletionInsertAction extends ExtDefaultKeyTypedAction {

    protected void insertString (
        BaseDocument        document, 
        int                 offset,
        Caret               caret, 
        String              insertString,
        boolean             overwrite
    ) throws BadLocationException {
        TokenSequence tokenSequence = Utils.getTokenSequence (document, caret.getDot ());
        if (tokenSequence != null) {
            String mimeType = tokenSequence.language ().mimeType ();
            try {
                Language l = LanguagesManager.getDefault ().getLanguage (mimeType);
                List<Feature> completes = l.getFeatureList ().getFeatures ("COMPLETE");
                if (completes != null) {
                    complete (
                        document,
                        offset,
                        caret,
                        insertString,
                        overwrite,
                        tokenSequence,
                        completes 
                    );
                    return;
                }
            } catch (LanguageDefinitionNotFoundException ex) {
            }
        }
        super.insertString (document, offset, caret, insertString, overwrite);
    }
    
    private void complete (
        BaseDocument        document, 
        int                 offset,
        Caret               caret, 
        String              insertString,
        boolean             overwrite,
        TokenSequence       tokenSequence,
        List<Feature>       completes
    ) throws BadLocationException {
        Iterator<Feature> it = completes.iterator ();
        while (it.hasNext ()) {
            Feature complete = it.next ();
            if (complete.getType () == Type.METHOD_CALL) {
                complete.getValue (new Object[] {document, caret, insertString});
                return;
            }
            String bracketPair = (String) complete.getValue ();
            int i = bracketPair.indexOf (':');
            if (i < 0) continue;
            if (caret.getDot () + bracketPair.length () - i - 1 < document.getLength()) {
                String followingText = document.getText (
                    caret.getDot (), 
                    bracketPair.length () - i - 1
                );
                if (bracketPair.endsWith (":" + followingText) && insertString.equals (followingText)) {
                    // skip closing bracket / do not write it again
                    caret.setDot (caret.getDot () + 1);
                    return;
                }
            }
            if (caret.getDot () - i + insertString.length () >= 0) {
                String previousTest = document.getText (
                    caret.getDot () - i + insertString.length (), 
                    i - insertString.length ()
                ) + insertString;
                if (bracketPair.startsWith (previousTest + ":")) {
                    Token token = tokenSequence.token ();
                    boolean startsWithWhiteSpace = false;
                    if (token != null) {
                        char firstCharOfTokenText = token.text ().charAt (0);
                        startsWithWhiteSpace = firstCharOfTokenText == '\n' || firstCharOfTokenText == ' ';
                    }
                    if (
                        token == null ||
                        tokenSequence.offset () == caret.getDot () ||
                        token.id ().name ().indexOf ("whitespace") >= 0 || startsWithWhiteSpace
                    ) { // on the end, between tokens or in whitespace
                        insertString += bracketPair.substring (i + 1);
                        super.insertString (document, offset, caret, insertString, overwrite);
                        caret.setDot (offset + 1);
                        return;
                    }
                }
            }
        }
        super.insertString (document, offset, caret, insertString, overwrite);
    }
}
