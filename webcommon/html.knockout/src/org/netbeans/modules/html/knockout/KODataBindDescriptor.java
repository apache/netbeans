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
package org.netbeans.modules.html.knockout;

import java.util.LinkedList;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Petr Hejl
 */
public class KODataBindDescriptor {

    private static final String NAME_PROPERTY = "name"; // NOI18N

    private static final String DATA_PROPERTY = "data"; // NOI18N

    private static final String FOREACH_PROPERTY = "foreach"; // NOI18N

    private static final String AS_PROPERTY = "as"; // NOI18N

    private final String name;

    private final String data;

    private final boolean isForEach;
    
    private final String alias;

    private KODataBindDescriptor(String name, String data, boolean isForEach, String alias) {
        this.name = name;
        this.data = data;
        this.isForEach = isForEach;
        this.alias = alias;
    }

    public static KODataBindDescriptor getDataBindDescriptor(Snapshot snapshot, TokenSequence<? extends JsTokenId> ts, boolean simpleForEach) {
        if (ts == null) {
            return null;
        }

        ts.moveStart();
        ts.moveNext();
        String name = null;
        String alias = null;
        String data = null;
        boolean forEach = false;
        Token<? extends JsTokenId> token = LexUtilities.findNextNonWsNonComment(ts);
        if (token.id() == JsTokenId.BRACKET_LEFT_CURLY) {
            while ((token = findNext(ts, JsTokenId.IDENTIFIER, false)) != null) {
                String text = token.text().toString();
                if ((NAME_PROPERTY.equals(text) || AS_PROPERTY.equals(text)) && ts.moveNext()) { // NOI18N
                    token = LexUtilities.findNextNonWsNonComment(ts);
                    if (token.id() == JsTokenId.OPERATOR_COLON && ts.moveNext()) {
                        token = LexUtilities.findNextNonWsNonComment(ts);
                        if (token.id() == JsTokenId.STRING_BEGIN && ts.moveNext()) {
                            token = LexUtilities.findNextNonWsNonComment(ts);
                            if (token.id() == JsTokenId.STRING) {
                                if (NAME_PROPERTY.equals(text)) { // NOI18N
                                    name = token.text().toString();
                                } else {
                                    alias = token.text().toString();
                                }
                            }
                        }
                    }
                } else if ((DATA_PROPERTY.equals(text) || FOREACH_PROPERTY.equals(text)) && ts.moveNext()) { // NOI18N
                    token = LexUtilities.findNextNonWsNonComment(ts);
                    if (token.id() == JsTokenId.OPERATOR_COLON && ts.moveNext()) {
                        LexUtilities.findNextNonWsNonComment(ts);
                        int start = ts.offset();
                        token = findNext(ts, JsTokenId.OPERATOR_COMMA, true);
                        if (token != null) {
                            data = snapshot.getText().subSequence(start, ts.offset()).toString().trim();
                            forEach = FOREACH_PROPERTY.equals(text);
                        }
                    }
                }
                if (token == null || token.id() != JsTokenId.OPERATOR_COMMA) {
                    findNext(ts, JsTokenId.OPERATOR_COMMA, false);
                }
            }
            if ((name != null || simpleForEach) && data != null) {
                return new KODataBindDescriptor(name, data, forEach, (forEach || simpleForEach) ? alias : null);
            }
        }

        return null;
    }

    private static Token<? extends JsTokenId> findNext(TokenSequence<? extends JsTokenId> ts, JsTokenId toFind, boolean findEnd) {
        LinkedList<JsTokenId> stack = new LinkedList<>();
        while (ts.moveNext()) {
            Token<? extends JsTokenId> token = LexUtilities.findNextNonWsNonComment(ts);
            JsTokenId id = token.id();
            switch (id) {
                case BRACKET_LEFT_BRACKET:
                case BRACKET_LEFT_CURLY:
                case BRACKET_LEFT_PAREN:
                    stack.push(id);
                    break;
                case BRACKET_RIGHT_BRACKET:
                    if (stack.isEmpty() || stack.pop() != JsTokenId.BRACKET_LEFT_BRACKET) {
                        return null;
                    }
                    break;
                case BRACKET_RIGHT_CURLY:
                    if (stack.isEmpty() && findEnd) {
                        return token;
                    }
                    if (stack.isEmpty() || stack.pop() != JsTokenId.BRACKET_LEFT_CURLY) {
                        return null;
                    }
                    break;
                case BRACKET_RIGHT_PAREN:
                    if (stack.isEmpty() || stack.pop() != JsTokenId.BRACKET_LEFT_PAREN) {
                        return null;
                    }
                    break;
                default:
                    if (toFind == id && stack.isEmpty()) {
                        return token;
                    }
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public boolean isIsForEach() {
        return isForEach;
    }

    public String getAlias() {
        return alias;
    }

}
