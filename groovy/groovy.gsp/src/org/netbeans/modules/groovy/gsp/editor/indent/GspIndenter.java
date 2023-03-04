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

package org.netbeans.modules.groovy.gsp.editor.indent;

import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.groovy.gsp.lexer.GspLexerLanguage;
import org.netbeans.modules.groovy.gsp.lexer.GspTokenId;
import org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.web.indent.api.support.IndenterContextData;
import org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter;

/**
 *
 * @author Martin Janicek
 */
public class GspIndenter extends MarkupAbstractIndenter<GspTokenId> {

    public GspIndenter(Context context) {
        super(GspLexerLanguage.getLanguage(), context);
    }


    @Override
    protected boolean isOpenTagNameToken(Token<GspTokenId> token) {
        return token.id() == GspTokenId.GTAG_OPENING_NAME;
    }

    @Override
    protected boolean isCloseTagNameToken(Token<GspTokenId> token) {
        return token.id() == GspTokenId.GTAG_CLOSING_NAME;
    }

    @Override
    protected boolean isStartTagSymbol(Token<GspTokenId> token) {
        return token.id() == GspTokenId.GTAG_OPENING_START;
    }

    @Override
    protected boolean isStartTagClosingSymbol(Token<GspTokenId> token) {
        return token.id() == GspTokenId.GTAG_CLOSING_START;
    }

    @Override
    protected boolean isEndTagSymbol(Token<GspTokenId> token) {
        return token.id() == GspTokenId.GTAG_OPENING_END
            || token.id() == GspTokenId.GTAG_CLOSING_END;
    }

    @Override
    protected boolean isEndTagClosingSymbol(Token<GspTokenId> token) {
        return token.id() == GspTokenId.GTAG_INDEPENDENT_END;
    }

    @Override
    protected boolean isTagArgumentToken(Token<GspTokenId> token) {
        return false;
    }

    @Override
    protected boolean isBlockCommentToken(Token<GspTokenId> token) {
        return token.id().isComment();
    }

    @Override
    protected boolean isTagContentToken(Token<GspTokenId> token) {
        return token.id() == GspTokenId.GTAG_ATTRIBUTE_NAME || token.id() == GspTokenId.GTAG_ATTRIBUTE_VALUE;
    }

    @Override
    protected boolean isClosingTagOptional(CharSequence tagName) {
        return false;
    }

    @Override
    protected boolean isOpeningTagOptional(CharSequence tagName) {
        return false;
    }

    @Override
    protected Boolean isEmptyTag(CharSequence tagName) {
        return Boolean.FALSE;
    }

    @Override
    protected boolean isTagContentUnformattable(CharSequence tagName) {
        return false;
    }

    @Override
    protected Set<String> getTagChildren(CharSequence tagName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean isPreservedLine(Token<GspTokenId> token, IndenterContextData<GspTokenId> context) {
        if (token.id().isComment()) {
            return true;
        }
        return false;
    }

    @Override
    protected int getPreservedLineInitialIndentation(JoinedTokenSequence<GspTokenId> ts) throws BadLocationException {
        int[] index = ts.index();
        boolean found = false;
        do {
            if (ts.token().id().isComment()) {
                found = true;
                break;
            } else {
                break;
            }
        } while (ts.movePrevious());
        int indent = 0;
        if (found) {
            int lineStart = Utilities.getRowStart(getDocument(), ts.offset());
            int column = ts.offset();
            indent = column - lineStart;
        }
        ts.moveIndex(index);
        ts.moveNext();
        return indent;
    }

    @Override
    protected boolean isForeignLanguageStartToken(Token<GspTokenId> token, JoinedTokenSequence<GspTokenId> ts) {
        return token.id().isStartDelimiter();
    }

    @Override
    protected boolean isForeignLanguageEndToken(Token<GspTokenId> token, JoinedTokenSequence<GspTokenId> ts) {
        return token.id().isEndDelimiter();
    }

    @Override
    protected boolean isWhiteSpaceToken(Token<GspTokenId> token) {
        return token.id() == GspTokenId.WHITESPACE
            || (token.id() == GspTokenId.HTML && token.text().toString().trim().length() == 0);
    }
}
