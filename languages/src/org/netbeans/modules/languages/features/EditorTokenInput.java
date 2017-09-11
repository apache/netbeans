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
    private List            tokens = new ArrayList ();
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
