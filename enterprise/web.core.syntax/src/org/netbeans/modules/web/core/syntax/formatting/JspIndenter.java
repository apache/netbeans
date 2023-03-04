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

package org.netbeans.modules.web.core.syntax.formatting;

import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.web.indent.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.web.indent.api.support.IndenterContextData;
import org.netbeans.modules.web.indent.api.support.MarkupAbstractIndenter;

/**
 *
 * @author david
 */
public class JspIndenter extends MarkupAbstractIndenter<JspTokenId> {

//    private boolean inScriptlet;

    public JspIndenter(Context context) {
        super(JspTokenId.language(), context);
    }

    @Override
    protected boolean isWhiteSpaceToken(Token<JspTokenId> token) {
        return token.id() == JspTokenId.WHITESPACE ||
                (token.id() == JspTokenId.TEXT && token.text().toString().trim().length() == 0);
    }


    @Override
    protected boolean isOpenTagNameToken(Token<JspTokenId> token) {
        return token.id() == JspTokenId.TAG;
    }

    @Override
    protected boolean isCloseTagNameToken(Token<JspTokenId> token) {
        return token.id() == JspTokenId.ENDTAG;
    }

    @Override
    protected boolean isStartTagSymbol(Token<JspTokenId> token) {
        return token.id() == JspTokenId.SYMBOL &&
            (token.text().toString().equals("<") || 
            token.text().toString().equals("<%@"));
    }

    @Override
    protected boolean isStartTagClosingSymbol(Token<JspTokenId> token) {
        return token.id() == JspTokenId.SYMBOL &&
                token.text().toString().equals("</");
    }

    @Override
    protected boolean isEndTagSymbol(Token<JspTokenId> token) {
        return token.id() == JspTokenId.SYMBOL &&
                token.text().toString().equals(">");
    }

    @Override
    protected boolean isEndTagClosingSymbol(Token<JspTokenId> token) {
        return token.id() == JspTokenId.SYMBOL &&
                (token.text().toString().equals("/>") || token.text().toString().equals("%>"));
    }

    @Override
    protected boolean isTagArgumentToken(Token<JspTokenId> token) {
        return token.id() == JspTokenId.ATTRIBUTE;
    }

    @Override
    protected boolean isBlockCommentToken(Token<JspTokenId> token) {
        return token.id() == JspTokenId.COMMENT;
    }

    @Override
    protected boolean isTagContentToken(Token<JspTokenId> token) {
        return token.id() == JspTokenId.TEXT;
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
        return null;
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
    protected boolean isPreservedLine(Token<JspTokenId> token, IndenterContextData<JspTokenId> context) {
        String text = token.text().toString().trim();
        if (token.id() == JspTokenId.COMMENT) {
            if (!text.startsWith("<%--") && !text.startsWith("--%>")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected int getPreservedLineInitialIndentation(JoinedTokenSequence<JspTokenId> ts)
            throws BadLocationException {
        int[] index = ts.index();
        boolean found = false;
        do {
            if (ts.token().id() == JspTokenId.COMMENT) {
                String comment = ts.token().text().toString().trim();
                if (comment.startsWith("<%--")) {
                    found = true;
                    break;
                }
            } else {
                break;
            }
        } while (ts.movePrevious());
        int indent = 0;
        if (found) {
            int lineStart = Utilities.getRowStart(getDocument(), ts.offset());
            // TODO: can comment token start with spaces?? if yes then adjust
            // column to point to first non-whitespace
            int column = ts.offset();
            indent = column - lineStart;
        }
        ts.moveIndex(index);
        ts.moveNext();
        return indent;
    }

    @Override
    protected boolean isForeignLanguageStartToken(Token<JspTokenId> token, JoinedTokenSequence<JspTokenId> ts) {
        return token.id() == JspTokenId.SYMBOL2 && (
                token.text().toString().equals("<%") || 
                token.text().toString().equals("<%=") ||
                token.text().toString().equals("<%!"));
    }

    @Override
    protected boolean isForeignLanguageEndToken(Token<JspTokenId> token, JoinedTokenSequence<JspTokenId> ts) {
        return token.id() == JspTokenId.SYMBOL2 && token.text().toString().equals("%>");
    }

    @Override
    protected boolean isStableFormattingStartToken(Token<JspTokenId> token, JoinedTokenSequence<JspTokenId> ts) {
        return token.id() == JspTokenId.SYMBOL2 && (
                token.text().toString().equals("<%") ||
                token.text().toString().equals("<%=") ||
                token.text().toString().equals("<%!"));
    }

}
