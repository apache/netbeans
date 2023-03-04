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

import org.netbeans.api.languages.Language;
import org.netbeans.api.languages.TokenInput;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.languages.ASTToken;
import org.openide.text.NbDocument;

import javax.swing.text.StyledDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.LanguagesManager;


/**
 *
 * @author Jan Jancura
 */
public class EditorTokenInput extends TokenInput {

    private TokenSequence   tokenSequence;
    private Language        language;
    private List<ASTToken>  tokens = new ArrayList<>();
    private int             index = 0;
    private Set<Integer>    filter;
    private StyledDocument  doc;
    private String          mimeType;
    

    public static EditorTokenInput create (
        Set<Integer>        filter,
        StyledDocument      doc
    ) {
        return new EditorTokenInput (filter, doc);
    }

    private EditorTokenInput (
        Set<Integer>        filter,
        StyledDocument      doc
    ) {
        tokenSequence = TokenHierarchy.get (doc).tokenSequence ();
        mimeType = tokenSequence.language ().mimeType ();
        try {
            language = LanguagesManager.get ().getLanguage (mimeType);
        } catch (LanguageDefinitionNotFoundException ex) {
        }
        this.filter = filter;
        this.doc = doc;
    }

    public ASTToken next (int i) {
        while (index + i - 1 >= tokens.size ()) {
            ASTToken token = nextToken ();
            if (token == null) return null;
            tokens.add (token);
        }
        return (ASTToken) tokens.get (index + i - 1);
    }
    
    private ASTToken nextToken () {
        do {
            if (!tokenSequence.moveNext ()) return null;
        } while (
            filter.contains (
                tokenSequence.token ().id ().ordinal ()
            )
        );
        Token token = tokenSequence.token ();
        return ASTToken.create (
            language,
            token.id ().ordinal (),
            token.text ().toString (),
            tokenSequence.offset ()
        );
    }

    public boolean eof () {
        return next (1) == null;
    }

    public int getIndex () {
        return index;
    }

    public int getOffset () {
        ASTToken t = null;
        if (eof ()) {
            if (getIndex () == 0) return 0;
            t = ((ASTToken) tokens.get (tokens.size () - 1));
            return t.getOffset () + t.getLength ();
        } else {
            t = (ASTToken) next (1);
            return t.getOffset ();
        }
    }

    public ASTToken read () {
        ASTToken next = next (1);
        index++;
        return next;
    }

    public void setIndex (int index) {
        this.index = index;
    }

    public String getString (int from) {
        throw new InternalError ();
    }
    
    public String toString () {
        int offset = next (1) == null ?
            doc.getLength () : next (1).getOffset ();
        int lineNumber = NbDocument.findLineNumber (doc, offset);
        return (String) doc.getProperty ("title") + ":" + 
            (lineNumber + 1) + "," + 
            (offset - NbDocument.findLineOffset (doc, lineNumber) + 1);
//        StringBuffer sb = new StringBuffer ();
//        TokenItem t = next;
//        int i = 0;
//        while (i < 10) {
//            if (t == null) break;
//            EditorToken et = (EditorToken) t.getTokenID ();
//            sb.append (Token.create (
//                et.getMimeType (),
//                et.getType (),
//                t.getImage (),
//                null
//            ));
//            t = t.getNext ();
//            i++;
//        }
//        return sb.toString ();
    }
}
