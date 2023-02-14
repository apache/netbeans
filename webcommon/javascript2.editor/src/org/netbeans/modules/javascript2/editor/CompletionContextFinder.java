/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.editor;

import java.util.ArrayList;
import org.netbeans.modules.javascript2.editor.spi.CompletionContext;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;

/**
 *
 * @author Petr Pisl
 */
public class CompletionContextFinder {

    private static final List<JsTokenId> WHITESPACES_TOKENS = Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL);

    private static final List<JsTokenId> CHANGE_CONTEXT_TOKENS = Arrays.asList(
            JsTokenId.OPERATOR_SEMICOLON, JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_CURLY);
    private static final List<Object[]> OBJECT_PROPERTY_TOKENCHAINS = Arrays.asList(
        new Object[]{JsTokenId.OPERATOR_DOT},
        new Object[]{JsTokenId.OPERATOR_DOT, JsTokenId.IDENTIFIER},
        new Object[]{JsTokenId.OPERATOR_DOT, JsTokenId.PRIVATE_IDENTIFIER},
        new Object[]{JsTokenId.OPERATOR_OPTIONAL_ACCESS},
        new Object[]{JsTokenId.OPERATOR_OPTIONAL_ACCESS, JsTokenId.IDENTIFIER},
        new Object[]{JsTokenId.OPERATOR_OPTIONAL_ACCESS, JsTokenId.PRIVATE_IDENTIFIER}
    );

    private static final List<Object[]> OBJECT_THIS_TOKENCHAINS = Arrays.asList(
        new Object[]{JsTokenId.KEYWORD_THIS, JsTokenId.OPERATOR_DOT},
        new Object[]{JsTokenId.KEYWORD_THIS, JsTokenId.OPERATOR_DOT, JsTokenId.IDENTIFIER},
        new Object[]{JsTokenId.KEYWORD_THIS, JsTokenId.OPERATOR_DOT, JsTokenId.PRIVATE_IDENTIFIER},
        new Object[]{JsTokenId.KEYWORD_THIS, JsTokenId.OPERATOR_OPTIONAL_ACCESS},
        new Object[]{JsTokenId.KEYWORD_THIS, JsTokenId.OPERATOR_OPTIONAL_ACCESS, JsTokenId.IDENTIFIER},
        new Object[]{JsTokenId.KEYWORD_THIS, JsTokenId.OPERATOR_OPTIONAL_ACCESS, JsTokenId.PRIVATE_IDENTIFIER}
    );

    private static final List<Object[]> NUMBER_TOKENCHAINS = Arrays.asList(
        new Object[]{JsTokenId.NUMBER, JsTokenId.OPERATOR_DOT},
        new Object[]{JsTokenId.NUMBER, JsTokenId.OPERATOR_DOT, JsTokenId.IDENTIFIER},
        new Object[]{JsTokenId.NUMBER, JsTokenId.OPERATOR_DOT, JsTokenId.PRIVATE_IDENTIFIER},
        new Object[]{JsTokenId.NUMBER, JsTokenId.OPERATOR_OPTIONAL_ACCESS},
        new Object[]{JsTokenId.NUMBER, JsTokenId.OPERATOR_OPTIONAL_ACCESS, JsTokenId.IDENTIFIER},
        new Object[]{JsTokenId.NUMBER, JsTokenId.OPERATOR_OPTIONAL_ACCESS, JsTokenId.PRIVATE_IDENTIFIER}
    );

    private static final List<Object[]> STRING_TOKENCHAINS = Arrays.asList(
        new Object[]{JsTokenId.STRING_END, JsTokenId.OPERATOR_DOT},
        new Object[]{JsTokenId.STRING_END, JsTokenId.OPERATOR_DOT, JsTokenId.IDENTIFIER},
        new Object[]{JsTokenId.STRING_END, JsTokenId.OPERATOR_DOT, JsTokenId.PRIVATE_IDENTIFIER},
        new Object[]{JsTokenId.STRING_END, JsTokenId.OPERATOR_OPTIONAL_ACCESS},
        new Object[]{JsTokenId.STRING_END, JsTokenId.OPERATOR_OPTIONAL_ACCESS, JsTokenId.IDENTIFIER},
        new Object[]{JsTokenId.STRING_END, JsTokenId.OPERATOR_OPTIONAL_ACCESS, JsTokenId.PRIVATE_IDENTIFIER}
    );

    private static final List<Object[]> REGEXP_TOKENCHAINS = Arrays.asList(
        new Object[]{JsTokenId.REGEXP_END, JsTokenId.OPERATOR_DOT},
        new Object[]{JsTokenId.REGEXP_END, JsTokenId.OPERATOR_DOT, JsTokenId.IDENTIFIER},
        new Object[]{JsTokenId.REGEXP_END, JsTokenId.OPERATOR_DOT, JsTokenId.PRIVATE_IDENTIFIER},
        new Object[]{JsTokenId.REGEXP_END, JsTokenId.OPERATOR_OPTIONAL_ACCESS},
        new Object[]{JsTokenId.REGEXP_END, JsTokenId.OPERATOR_OPTIONAL_ACCESS, JsTokenId.IDENTIFIER},
        new Object[]{JsTokenId.REGEXP_END, JsTokenId.OPERATOR_OPTIONAL_ACCESS, JsTokenId.PRIVATE_IDENTIFIER}
    );

    @NonNull
    static CompletionContext findCompletionContext(ParserResult info, int offset){
        TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
        if (th == null) {
            return CompletionContext.NONE;
        }
        TokenSequence<JsTokenId> ts = th.tokenSequence(JsTokenId.javascriptLanguage());
        if (ts == null) {
            return CompletionContext.NONE;
        }

        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()){
            return CompletionContext.NONE;
        }

        Token<? extends JsTokenId> token = ts.token();
        JsTokenId tokenId =token.id();

        if (tokenId == JsTokenId.DOC_COMMENT) {
            return CompletionContext.DOCUMENTATION;
        }

        if ((tokenId == JsTokenId.OPERATOR_DOT || tokenId == JsTokenId.OPERATOR_OPTIONAL_ACCESS) && ts.moveNext()) {
            ts.movePrevious();
            ts.movePrevious();
            token = ts.token();
            tokenId =token.id();
        }

        if (tokenId == JsTokenId.STRING || tokenId == JsTokenId.STRING_END) {
            token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.STRING, JsTokenId.STRING_BEGIN, JsTokenId.STRING_END));
            if (token == null) {
                return CompletionContext.IN_STRING;
            }
            token = LexUtilities.findPreviousNonWsNonComment(ts);
            if (token.id() == JsTokenId.BRACKET_LEFT_PAREN && ts.movePrevious()) {
                token = LexUtilities.findPreviousNonWsNonComment(ts);
                tokenId = token.id();
                if (tokenId == JsTokenId.IDENTIFIER) {
                    if ("getElementById".equals(token.text().toString())) {
                        return CompletionContext.STRING_ELEMENTS_BY_ID;
                    } else if ("getElementsByClassName".equals(token.text().toString())) {
                        return CompletionContext.STRING_ELEMENTS_BY_CLASS_NAME;
                    }
                }
            }
            token = LexUtilities.findPreviousToken(ts, Utils.LOOK_FOR_IMPORT_EXPORT_TOKENS);
            if (token.id() == JsTokenId.KEYWORD_EXPORT || token.id() == JsTokenId.KEYWORD_IMPORT) {
                return CompletionContext.IMPORT_EXPORT_MODULE;
            }
            return CompletionContext.IN_STRING;
        }

        if (acceptTokenChains(ts, OBJECT_THIS_TOKENCHAINS, true)) {
            return CompletionContext.OBJECT_MEMBERS;
        }

        if (acceptTokenChains(ts, NUMBER_TOKENCHAINS, tokenId != JsTokenId.OPERATOR_DOT && tokenId != JsTokenId.OPERATOR_OPTIONAL_ACCESS)) {
            return CompletionContext.NUMBER;
        }

        if (acceptTokenChains(ts, STRING_TOKENCHAINS, tokenId != JsTokenId.OPERATOR_DOT && tokenId != JsTokenId.OPERATOR_OPTIONAL_ACCESS)) {
            return CompletionContext.STRING;
        }

        if (acceptTokenChains(ts, REGEXP_TOKENCHAINS, tokenId != JsTokenId.OPERATOR_DOT && tokenId != JsTokenId.OPERATOR_OPTIONAL_ACCESS)) {
            return CompletionContext.REGEXP;
        }

        if (acceptTokenChains(ts, OBJECT_PROPERTY_TOKENCHAINS, tokenId != JsTokenId.OPERATOR_DOT && tokenId != JsTokenId.OPERATOR_OPTIONAL_ACCESS)) {
            return CompletionContext.OBJECT_PROPERTY;
        }

        ts.move(offset);
        if (ts.moveNext()) {
            if (isCallArgumentContext(ts)) {
                return CompletionContext.CALL_ARGUMENT;
            } else {
                ts.move(offset);
                ts.moveNext();
            }
            if (isPropertyNameContext(ts)) {
                return CompletionContext.OBJECT_PROPERTY_NAME;
            }
        }

        ts.move(offset);
        if (!ts.moveNext()) {
            if (!ts.movePrevious()) {
                return CompletionContext.GLOBAL;
            }
        }
        token = ts.token(); tokenId = token.id();
        if (tokenId == JsTokenId.EOL && ts.movePrevious()) {
            token = ts.token(); tokenId = token.id();
        }
        if (tokenId == JsTokenId.IDENTIFIER || tokenId == JsTokenId.PRIVATE_IDENTIFIER || WHITESPACES_TOKENS.contains(tokenId)) {
            if (!ts.movePrevious()) {
                return CompletionContext.GLOBAL;
            }
            token = LexUtilities.findPrevious(ts, WHITESPACES_TOKENS);
        }
        if (CHANGE_CONTEXT_TOKENS.contains(token.id())
                || (WHITESPACES_TOKENS.contains(token.id()) && !ts.movePrevious())) {
            return CompletionContext.GLOBAL;
        }
        token = LexUtilities.findPreviousToken(ts, Utils.LOOK_FOR_IMPORT_EXPORT_TOKENS);
        if (token.id() == JsTokenId.KEYWORD_EXPORT || token.id() == JsTokenId.KEYWORD_IMPORT) {
            return CompletionContext.IMPORT_EXPORT_SPECIAL_TOKENS;
        }
        if (tokenId == JsTokenId.DOC_COMMENT) {
            return CompletionContext.DOCUMENTATION;
        }

        return CompletionContext.EXPRESSION;
    }

    protected static boolean isCallArgumentContext(TokenSequence<JsTokenId> ts) {
         if (ts.movePrevious()) {
            Token<? extends JsTokenId> token = LexUtilities.findPreviousNonWsNonComment(ts);
            if (token != null
                    && (token.id() == JsTokenId.BRACKET_LEFT_PAREN || token.id() == JsTokenId.OPERATOR_COMMA)) {
                int balanceParen = token.id() == JsTokenId.BRACKET_LEFT_PAREN ? 0 : 1;
                int balanceCurly = 0;
                int balanceBracket = 0;
                while (balanceParen != 0 && ts.movePrevious()) {
                    token = ts.token();
                    if (token.id() == JsTokenId.BRACKET_LEFT_PAREN) {
                        balanceParen--;
                    } else if (token.id() == JsTokenId.BRACKET_RIGHT_PAREN) {
                        balanceParen++;
                    } else if (token.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                        balanceCurly--;
                    } else if (token.id() == JsTokenId.BRACKET_RIGHT_CURLY) {
                        balanceCurly++;
                    } else if (token.id() == JsTokenId.BRACKET_LEFT_BRACKET) {
                        balanceBracket--;
                    } else if (token.id() == JsTokenId.BRACKET_RIGHT_BRACKET) {
                        balanceBracket++;
                    }
                }
                if (balanceParen == 0 && balanceCurly == 0 && balanceBracket == 0) {
                    if (ts.movePrevious() && token.id() == JsTokenId.BRACKET_LEFT_PAREN) {
                        token = LexUtilities.findPreviousNonWsNonComment(ts);
                        if (token.id() == JsTokenId.IDENTIFIER || token.id() == JsTokenId.PRIVATE_IDENTIFIER) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    protected static boolean isPropertyNameContext(TokenSequence<JsTokenId> ts) {

        //find the begining of the object literal
        JsTokenId tokenId = ts.token().id();

        if (tokenId == JsTokenId.OPERATOR_COMMA) {
            ts.movePrevious();
        }
        List<JsTokenId> listIds = Arrays.asList(JsTokenId.OPERATOR_COMMA, JsTokenId.OPERATOR_COLON, JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.OPERATOR_SEMICOLON);
        // find previous , or : or { or ;
        Token<? extends JsTokenId> token = LexUtilities.findPreviousToken(ts, listIds);
        tokenId = token.id();
        boolean commaFirst = false;
        if (tokenId == JsTokenId.OPERATOR_COMMA && ts.movePrevious()) {
            List<JsTokenId> checkParentList = new ArrayList<>(listIds);
            List<JsTokenId> parentList = Arrays.asList(JsTokenId.BRACKET_LEFT_PAREN, JsTokenId.BRACKET_RIGHT_PAREN, JsTokenId.BRACKET_RIGHT_CURLY);
            checkParentList.addAll(parentList);
            token = LexUtilities.findPreviousToken(ts, checkParentList);
            tokenId = token.id();
            commaFirst = true;
            if (tokenId == JsTokenId.BRACKET_RIGHT_PAREN || tokenId == JsTokenId.BRACKET_RIGHT_CURLY) {
                balanceBracketBack(ts);
                token = ts.token();
                tokenId = token.id();
            } else if (tokenId == JsTokenId.BRACKET_LEFT_PAREN) {
                return false;
            } else {
                if (tokenId == JsTokenId.OPERATOR_COLON) {
                    // we are in the previous property definition
                    return true;
                }
            }
        }
        if (tokenId == JsTokenId.BRACKET_LEFT_CURLY && ts.movePrevious()) {
            List<JsTokenId> emptyIds = Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.BLOCK_COMMENT);
            // check whether it's the first property in the object literal definion
            token = LexUtilities.findPrevious(ts, emptyIds);
            tokenId = token.id();
            if (tokenId == JsTokenId.BRACKET_LEFT_PAREN || tokenId == JsTokenId.OPERATOR_COMMA || tokenId == JsTokenId.OPERATOR_EQUALS || tokenId == JsTokenId.OPERATOR_COLON) {
                return true;
            } else if (tokenId == JsTokenId.BRACKET_RIGHT_PAREN) {
                // it can be a method definition
                balanceBracketBack(ts);
                token = ts.token();
                tokenId = token.id();
                if (tokenId == JsTokenId.BRACKET_LEFT_PAREN && ts.movePrevious()) {
                    token = LexUtilities.findPrevious(ts, emptyIds);
                    tokenId = token.id();
                    if (tokenId == JsTokenId.KEYWORD_FUNCTION && ts.movePrevious()) {
                        // we found a method definition, now we need to check, whether its in an object literal
                        token = LexUtilities.findPrevious(ts, emptyIds);
                        tokenId = token.id();
                        if (tokenId == JsTokenId.OPERATOR_COLON) {
                            return commaFirst;
                        }
                    }
                }
            }
        }

        return false;
    }

    private static boolean acceptTokenChains(TokenSequence tokenSequence, List<Object[]> tokenIdChains, boolean movePrevious) {
        for (Object[] tokenIDChain : tokenIdChains){
            if (acceptTokenChain(tokenSequence, tokenIDChain, movePrevious)){
                return true;
            }
        }

        return false;
    }

    private static boolean acceptTokenChain(TokenSequence tokenSequence, Object[] tokenIdChain, boolean movePrevious) {
        int orgTokenSequencePos = tokenSequence.offset();
        boolean accept = true;
        boolean moreTokens = movePrevious ? tokenSequence.movePrevious() : true;

        for (int i = tokenIdChain.length - 1; i >= 0; i --){
            Object tokenID = tokenIdChain[i];

            if (!moreTokens){
                accept = false;
                break;
            }

           if (tokenID instanceof JsTokenId) {
                if (tokenSequence.token().id() == tokenID){
                    moreTokens = tokenSequence.movePrevious();
                } else {
                    // NO MATCH
                    accept = false;
                    break;
                }
            } else {
                assert false : "Unsupported token type: " + tokenID.getClass().getName();
            }
        }

        tokenSequence.move(orgTokenSequencePos);
        tokenSequence.moveNext();
       return accept;
    }

    private static void balanceBracketBack(TokenSequence<JsTokenId> ts) {
        JsTokenId tokenId = ts.token().id();
        JsTokenId tokenIdOriginal = ts.token().id();
        List<JsTokenId> lookingFor;
        if (tokenId == JsTokenId.BRACKET_RIGHT_CURLY) {
            lookingFor = Arrays.asList(JsTokenId.BRACKET_LEFT_CURLY);
        } else if (tokenId == JsTokenId.BRACKET_RIGHT_PAREN) {
            lookingFor = Arrays.asList(JsTokenId.BRACKET_LEFT_PAREN);
        } else {
            return;
        }
        int balance = -1;
        while (balance != 0 && ts.movePrevious()) {
            Token<? extends JsTokenId> token = LexUtilities.findPreviousToken(ts, lookingFor);
            tokenId = token.id();
            if (lookingFor.contains(tokenIdOriginal)) {
                balance --;
            } else if (lookingFor.contains(tokenId)) {
                balance++;
            }
        }
    }
}
