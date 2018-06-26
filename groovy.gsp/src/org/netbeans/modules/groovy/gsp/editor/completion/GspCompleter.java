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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.gsp.editor.completion;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.completion.CompletionHandler;
import org.netbeans.modules.groovy.gsp.lexer.GspLexerLanguage;
import org.netbeans.modules.groovy.gsp.lexer.GspTokenId;

/**
 * GSP code completer. Currently just completes Groovy code for embedded
 * scriptlets, GStrings etc.
 *
 * @author Tor Norbye
 * @author Martin Adamek
 */
public class GspCompleter extends CompletionHandler {

    @Override
    public CodeCompletionResult complete(CodeCompletionContext context) {
        final ParserResult info = context.getParserResult();
        final int caretOffset = context.getCaretOffset();
        final Document doc = info.getSnapshot().getSource().getDocument(false);
        
        if (doc != null && isGroovyCompletion(doc, caretOffset)) {
            return super.complete(context);
        }
        return CodeCompletionResult.NONE;
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        final Document doc = component.getDocument();
        final int caretOffset =  component.getCaret().getDot();
        
        if (isGroovyCompletion(doc, caretOffset)) {
            return super.getAutoQuery(component, typedText);
        }
        return QueryType.NONE;
    }

    private boolean isGroovyCompletion(Document doc, int offset) {
        TokenHierarchy<Document> tokenHierarchy = TokenHierarchy.get(doc);
        TokenSequence<GspTokenId> tokenSequence = tokenHierarchy.tokenSequence(GspLexerLanguage.getLanguage());

        tokenSequence.move(offset);
        if (tokenSequence.moveNext() || tokenSequence.movePrevious()) {
            GspTokenId tokenID = tokenSequence.token().id();
            if (tokenID.isGroovyContent()) {
                return true;
            }

            // maybe the caret is placed just before the ending script delimiter?
            if (isGroovyEndTag(tokenID)) {
                
                // move the caret to the content
                tokenSequence.movePrevious();

                if (tokenID.isGroovyContent()) {
                    return true;
                }
            }
        }

        return false;
    }
    
    private boolean isGroovyEndTag(GspTokenId tokenID) {
        if (tokenID == GspTokenId.GSTRING_END
                || tokenID == GspTokenId.SCRIPTLET_END
                || tokenID == GspTokenId.SCRIPTLET_OUTPUT_VALUE_END) {
            return true;
        }
        return false;
    }
}
