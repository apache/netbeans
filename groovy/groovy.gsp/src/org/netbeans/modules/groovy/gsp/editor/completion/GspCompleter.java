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
