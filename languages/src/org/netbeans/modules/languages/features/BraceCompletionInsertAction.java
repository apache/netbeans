/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
