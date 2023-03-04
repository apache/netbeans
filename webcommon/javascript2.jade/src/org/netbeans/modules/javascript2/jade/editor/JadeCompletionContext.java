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
package org.netbeans.modules.javascript2.jade.editor;

import java.util.Arrays;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.jade.editor.lexer.JadeTokenId;

/**
 *
 * @author Petr Pisl
 */
public enum JadeCompletionContext {
    NONE, // There shouldn't be any code completion
    TAG,  // offer only html tags
    TAG_AND_KEYWORD, // tags and keywords
    ATTRIBUTE,    // html attributes
    CSS_ID, 
    CSS_CLASS;
    
      
    private static final List<Object[]> KEYWORD_POSITION = Arrays.asList(
        new Object[]{JadeTokenId.EOL},    
        new Object[]{JadeTokenId.EOL, JadeTokenId.WHITESPACE},
        new Object[]{JadeTokenId.EOL, JadeTokenId.TAG},
        new Object[]{JadeTokenId.EOL, JadeTokenId.WHITESPACE, JadeTokenId.TAG}
    );
    
    private static final List<Object[]> TAG_POSITION = Arrays.asList(
        new Object[]{JadeTokenId.TAG},
        new Object[]{JadeTokenId.TAG, JadeTokenId.OPERATOR_COLON, JadeTokenId.WHITESPACE}    
//        new Object[]{JadeTokenId.BRACKET_LEFT_PAREN},
//        new Object[]{JadeTokenId.EOL, JadeTokenId.EOL},
//        new Object[]{JadeTokenId.EOL, JadeTokenId.TAG},
//        new Object[]{JadeTokenId.EOL, JadeTokenId.WHITESPACE},    
//        new Object[]{JadeTokenId.EOL, JadeTokenId.WHITESPACE, JadeTokenId.TAG},    
//        new Object[]{JadeTokenId.TAG, JadeTokenId.OPERATOR_COLON},    
//        new Object[]{JadeTokenId.TAG, JadeTokenId.OPERATOR_COLON},
//        new Object[]{JadeTokenId.TAG, JadeTokenId.WHITESPACE},
//        new Object[]{JadeTokenId.TAG, JadeTokenId.EOL},
//        new Object[]{JadeTokenId.TAG, JadeTokenId.OPERATOR_COLON, JadeTokenId.TAG},
//        
//        new Object[]{JadeTokenId.TAG, JadeTokenId.OPERATOR_COLON, JadeTokenId.WHITESPACE, JadeTokenId.TAG}
    );
    
    private static final List<JadeTokenId> WHITESPACES = Arrays.asList(JadeTokenId.WHITESPACE, JadeTokenId.EOL);
    
    private static final List<Object[]> ATTRIBUTE_POSITION = Arrays.asList(
        new Object[]{JadeTokenId.ATTRIBUTE},
        new Object[]{JadeTokenId.BRACKET_LEFT_PAREN},
        new Object[]{JadeTokenId.BRACKET_LEFT_PAREN, JadeTokenId.EOL},
        new Object[]{JadeTokenId.BRACKET_LEFT_PAREN, JadeTokenId.WHITESPACE}, 
        new Object[]{JadeTokenId.BRACKET_LEFT_PAREN, JadeTokenId.EOL, JadeTokenId.WHITESPACE},
        new Object[]{JadeTokenId.JAVASCRIPT, JadeTokenId.OPERATOR_COMMA},
        new Object[]{JadeTokenId.JAVASCRIPT, JadeTokenId.OPERATOR_COMMA, JadeTokenId.WHITESPACE},
        new Object[]{JadeTokenId.ATTRIBUTE, JadeTokenId.OPERATOR_ASSIGNMENT, JadeTokenId.JAVASCRIPT, JadeTokenId.WHITESPACE},
        new Object[]{JadeTokenId.ATTRIBUTE, JadeTokenId.OPERATOR_ASSIGNMENT, JadeTokenId.JAVASCRIPT}
    );
    
    private static final List<Object[]> CSS_ID_POSITION = Arrays.asList(
        new Object[]{JadeTokenId.CSS_ID},
        new Object[]{JadeTokenId.TAG, JadeTokenId.TEXT},
        new Object[]{JadeTokenId.CSS_CLASS, JadeTokenId.TEXT}
    );
    
    private static final List<Object[]> CSS_CLASS_POSITION = Arrays.asList(
        new Object[]{JadeTokenId.CSS_CLASS},
        new Object[]{JadeTokenId.TAG, JadeTokenId.PLAIN_TEXT_DELIMITER},
        new Object[]{JadeTokenId.CSS_ID, JadeTokenId.PLAIN_TEXT_DELIMITER},
        new Object[]{JadeTokenId.CSS_CLASS, JadeTokenId.PLAIN_TEXT_DELIMITER}
    );
    
    @NonNull
    public static JadeCompletionContext findCompletionContext(ParserResult info, int offset){
        TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
        boolean isEOF = false;
        if (th == null) {
            return NONE;
        }
        TokenSequence<JadeTokenId> ts = th.tokenSequence(JadeTokenId.jadeLanguage());
        if (ts == null) {
            return NONE;
        }
        
        ts.move(offset);
        
        if (!ts.movePrevious()) {
            return TAG_AND_KEYWORD;
        } else if (ts.token().id() == JadeTokenId.TAG) {
            // just check, whether we are on the first tag in the file. see issue #251160
            if (!ts.movePrevious()) {
                return TAG_AND_KEYWORD;
            }
            ts.moveNext();
        }
        if (!ts.moveNext()) {
            if (ts.token() != null && ts.token().id() == JadeTokenId.EOL) {
                while ((ts.token().id() == JadeTokenId.WHITESPACE || ts.token().id() == JadeTokenId.EOL) && ts.movePrevious()) {
                }
                if (acceptTokenChains(ts, ATTRIBUTE_POSITION, false)) {
                    return ATTRIBUTE;
                }
                return TAG_AND_KEYWORD;
            }
            isEOF = true;
        }
        
        Token<JadeTokenId> token = ts.token();
        JadeTokenId id = token.id();
        String text = null;
        switch (id) {
            case ATTRIBUTE: return ATTRIBUTE;
            case TAG: 
                if (acceptTokenChains(ts, KEYWORD_POSITION, false)) {
                    return TAG_AND_KEYWORD;
                }
                return TAG;
            case CSS_ID: return CSS_ID;
            case CSS_CLASS: return CSS_CLASS;
            case TEXT: 
                text = token.text().toString();
                if (JadeCodeCompletion.CSS_ID_PREFIX.equals(text) && (acceptTokenChains(ts, TAG_POSITION, true) || isEOF)) {
                    return CSS_ID;
                }
                break;
            case PLAIN_TEXT_DELIMITER:
                if (acceptTokenChains(ts, TAG_POSITION, true)) {
                    return CSS_CLASS;
                }
                if (isEOF) {
                    return CSS_CLASS;
                }
                break;
            case COMMENT:
                String commentText = token.text().toString();
                int index = offset -  ts.offset() - 1;
                int spaces = 0;
                if (index > -1 && index < commentText.length()) {
                    
                    while (index > -1 && (commentText.charAt(index) == ' ' || commentText.charAt(index) == '\t')) {
                        spaces++;
                        index--;
                    }
                    if (index > -1) {
                        char ch = commentText.charAt(index);
                        if (ch == '\n') {
                            if (spaces == 0) {
                                return TAG_AND_KEYWORD;
                            } else {
                                if (ts.movePrevious() && ts.token().id() == JadeTokenId.COMMENT_DELIMITER && ts.movePrevious()) {
                                    
                                    token = ts.token();
                                    id = token.id();
                                    if (id == JadeTokenId.WHITESPACE && token.length() >= spaces) {
                                        return TAG_AND_KEYWORD;
                                    }
                                }
                            }
                        }
                    }
                }
                return NONE;
        }
        if (id.isKeyword()) {
            return TAG_AND_KEYWORD;
        }
        
        int helpIndex = ts.index();
        while ((id == JadeTokenId.WHITESPACE || id == JadeTokenId.EOL) && ts.movePrevious()) {
            id = ts.token().id();
        }
        if (acceptTokenChains(ts, ATTRIBUTE_POSITION, helpIndex == ts.index())) {
            return ATTRIBUTE;
        }
        if (helpIndex != ts.index()) {
            ts.moveIndex(helpIndex);
            ts.moveNext();
            id = ts.token().id();
        }
        
        if (acceptTokenChains(ts, KEYWORD_POSITION, !isEOF)) {
            return TAG_AND_KEYWORD;
        }
        
        // check tag: ^ position
        if (acceptTokenChains(ts, TAG_POSITION, true)) {
            return TAG;
        }
        
        if (acceptTokenChains(ts, CSS_CLASS_POSITION, true)) {
            return CSS_CLASS;
        }
        if (acceptTokenChains(ts, CSS_ID_POSITION, true)) {
            return CSS_ID;
        }

        boolean isBeginOfLine = false;
        if (ts.movePrevious()) {
            token = ts.token();
            id = token.id();
            if (id == JadeTokenId.WHITESPACE && ts.movePrevious()) {
                token = ts.token();
                id = token.id();
            }
            switch (id) {
                case EOL:
                    isBeginOfLine = true;
                    if (text != null) {
                        if (JadeCodeCompletion.CSS_CLASS_PREFIX.equals(text)) {
                            return CSS_CLASS;
                        }
                        if (JadeCodeCompletion.CSS_ID_PREFIX.equals(text)) {
                            return CSS_ID;
                        }
                    }
                    break;
                case COMMENT: 
                    return TAG_AND_KEYWORD;
                case TEXT:
                    text = token.text().toString();
                    if (JadeCodeCompletion.CSS_ID_PREFIX.equals(text) && acceptTokenChains(ts, KEYWORD_POSITION, true)) {
                        return CSS_ID;
                    }
                    break;
                case PLAIN_TEXT_DELIMITER:
                    if (acceptTokenChains(ts, KEYWORD_POSITION, true)) {
                        return CSS_CLASS;
                    }
                    break;
            }
            
            while (ts.movePrevious()) {
                token = ts.token();
                id = token.id(); 
                if (id == JadeTokenId.TAG) {
                    if (isBeginOfLine) {
                        return TAG_AND_KEYWORD;
                    } else {
                        return NONE;
                    }
                }
                if (id == JadeTokenId.ATTRIBUTE || id == JadeTokenId.BRACKET_LEFT_PAREN) {
                    return ATTRIBUTE;
                }
                
                if (id != JadeTokenId.EOL && id != JadeTokenId.WHITESPACE && id != JadeTokenId.JAVASCRIPT) {
                    if (isBeginOfLine) {
                        return TAG_AND_KEYWORD;
                    }
                    return NONE;
                }
            }  
        }         
        return NONE;
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

           if (tokenID instanceof JadeTokenId) {
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
}
