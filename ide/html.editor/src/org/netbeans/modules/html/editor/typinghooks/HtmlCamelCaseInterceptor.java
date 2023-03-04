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
package org.netbeans.modules.html.editor.typinghooks;

import java.util.Arrays;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.spi.editor.typinghooks.CamelCaseInterceptor;

/**
 *
 * @author marek
 */
public class HtmlCamelCaseInterceptor implements CamelCaseInterceptor {

    @Override
    public boolean beforeChange(MutableContext context) throws BadLocationException {
        return false;
    }

    @Override
    public void change(MutableContext context) throws BadLocationException {
        context.setNextWordOffset(context.isBackward()
                ? getPreviousWordOffset(context.getComponent(), context.getOffset())
                : getNextWordOffset(context.getComponent(), context.getOffset()));
    }

    @Override
    public void afterChange(MutableContext context) throws BadLocationException {
        //no-op
    }

    @Override
    public void cancelled(MutableContext context) {
        //no-op
    }

    private static int getNextWordOffset(JTextComponent target, int dotPos) throws BadLocationException {
        TokenHierarchy hi = TokenHierarchy.get(target.getDocument());
        TokenSequence<HTMLTokenId> ts = hi.tokenSequence(HTMLTokenId.language());
        if (ts == null) {
            return -1;
        }

        int diff = ts.move(dotPos);
        if (!ts.moveNext()) {
            return -1;
        }

        Token<HTMLTokenId> token = ts.token();
        int offset = -1;
        switch (token.id()) {
            case ARGUMENT:
                //jump from attribute name into its value
                Token next = LexerUtils.followsToken(ts,
                        Arrays.asList(new HTMLTokenId[]{HTMLTokenId.VALUE, HTMLTokenId.VALUE_CSS, HTMLTokenId.VALUE_JAVASCRIPT}),
                    false, false, HTMLTokenId.WS, HTMLTokenId.OPERATOR);
                if (next != null) {
                    offset = ts.offset();
                    if (WebUtils.isValueQuoted(ts.token().text())) {
                        offset++; //jump after the leading quote
                    }
                }
                break;
            case TAG_OPEN_SYMBOL:
                //jump from tag open symbol "<" or "</" after its name
                next = LexerUtils.followsToken(ts,
                        Arrays.asList(new HTMLTokenId[]{HTMLTokenId.TAG_OPEN, HTMLTokenId.TAG_CLOSE}),
                    false, false);
                if (next != null) {
                    offset = ts.offset() + next.length();
                }
                break;

            case TAG_CLOSE_SYMBOL:
                // <tag |> ... <tag >|
                offset = ts.offset() + ts.token().length();
                break;

            case VALUE:
            case VALUE_CSS:
            case VALUE_JAVASCRIPT:
                // <div align="center|" > ... <div align="center"| >
                char c = token.text().charAt(diff);
                if ((token.length() - 1 == diff) && c == '"' || c == '\'') {
                    offset = ts.offset() + token.length();
                }

        }

        return offset;
    }

    private static int getPreviousWordOffset(JTextComponent target, int dotPos) throws BadLocationException {
        TokenHierarchy hi = TokenHierarchy.get(target.getDocument());
        TokenSequence<HTMLTokenId> ts = hi.tokenSequence(HTMLTokenId.language());
        if (ts == null) {
            return -1;
        }

        int diff = ts.move(dotPos);
        if (diff == 0) {
            if (!ts.movePrevious()) {
                return -1;
            }
        } else {
            if (!ts.moveNext()) {
                return -1;
            }
        }

        Token<HTMLTokenId> token = ts.token();
        int offset = -1;
        switch (token.id()) {
            case VALUE:
            case VALUE_CSS:
            case VALUE_JAVASCRIPT:
                if (diff == 0) {
                //we are just after an attribute value, jump to the end of its value if quoted
                if (WebUtils.isValueQuoted(ts.token().text())) {
                    offset = ts.offset() + token.length() - 1;
                }

            } else if (diff == 1 && WebUtils.isValueQuoted(token.text())) {
                //jump from attribute value to its name if we are just after the
                //opening quote
                Token prev = LexerUtils.followsToken(ts,
                        HTMLTokenId.ARGUMENT,
                        true, false, HTMLTokenId.WS, HTMLTokenId.OPERATOR);
                if (prev != null) {
                    offset = ts.offset();
                }
            }
                break;
            case TAG_OPEN:
            case TAG_CLOSE:
                //jump from tag name to its open symbol "<" or "</"
                Token prev = LexerUtils.followsToken(ts,
                        HTMLTokenId.TAG_OPEN_SYMBOL,
                        true, false);
                if (prev != null) {
                    offset = ts.offset();
                }
                break;

            case TAG_CLOSE_SYMBOL:
                // <tag >| ... <tag |>
                offset = ts.offset();

                break;
        }

        return offset;
    }

    @MimeRegistration(mimeType = "text/html", service = CamelCaseInterceptor.Factory.class)
    public static final class Factory implements CamelCaseInterceptor.Factory {

        @Override
        public CamelCaseInterceptor createCamelCaseInterceptor(MimePath mimePath) {
            return new HtmlCamelCaseInterceptor();
        }

    }

}
