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
package org.netbeans.modules.javascript2.requirejs.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Petr Pisl
 */
public class EditorUtils {

    public static final String DEFINE = "define";    //NOI18N
    public static final String REQUIRE = "require";    //NOI18N
    public static final String REQUIREJS = "requirejs"; // NOI18N
    public static final String PATHS = "paths";        //NOI18N
    public static final String BASE_URL = "baseUrl";    //NOI8N
    public static final String PACKAGES = "packages";    //NOI8N
    public static final String CONFIG_METHOD_NAME = "config"; //NOI18N
    
    private static final String REQUIRE_JS_ICON_PATH = "org/netbeans/modules/javascript2/requirejs/resources/requirejs.png"; //NOI18N
    private static ImageIcon REQUIREJS_ICON = null;
    
    public static ImageIcon getRequireJsIcon () {
        if (REQUIREJS_ICON == null) {
            REQUIREJS_ICON = ImageUtilities.loadImageIcon(REQUIRE_JS_ICON_PATH, false); //NOI18N
        }
        return REQUIREJS_ICON;
    }
    /**
     * Returns true if at the offset there is a string and the string is in a
     * call of define or require method.
     *
     * @param snapshot
     * @param offset
     * @return
     */
    public static boolean isFileReference(final Snapshot snapshot, int offset) {
        CodeCompletionContext context = findContext(snapshot, offset);
        return context == CodeCompletionContext.CONFIG_BASE_URL_VALUE 
                || context == CodeCompletionContext.CONFIG_PATHS_VALUE
                || context == CodeCompletionContext.REQUIRE_MODULE;
    }

    public static enum CodeCompletionContext {

        /**
         * in configuration object after baseUrl: ''
         */
        CONFIG_BASE_URL_VALUE,
        /**
         * Define file path in paths object in the configuration object
         */
        CONFIG_PATHS_VALUE,
        /**
         * name of properties in the configuration object
         */
        CONFIG_PROPERTY_NAME,
        /**
         * names and paths of modules in require, requirejs, define etc.
         */
        REQUIRE_MODULE,
        /**
         * other
         */
        UNKNOWN
    };

    public static CodeCompletionContext findContext(final Snapshot snapshot, final int offset) {
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(snapshot, offset);
        if (ts == null) {
            return CodeCompletionContext.UNKNOWN;
        }

        ts.move(offset);
        if (ts.moveNext()) {
            Token<? extends JsTokenId> token = ts.token();
            if (token.id() == JsTokenId.STRING || token.id() == JsTokenId.STRING_END) {
                token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.BLOCK_COMMENT, JsTokenId.LINE_COMMENT,
                        JsTokenId.STRING_BEGIN, JsTokenId.STRING, JsTokenId.STRING_END, JsTokenId.OPERATOR_COMMA));
                if (token.id() == JsTokenId.BRACKET_LEFT_BRACKET || token.id() == JsTokenId.OPERATOR_COLON || token.id() == JsTokenId.BRACKET_LEFT_PAREN) {
                    token = LexUtilities.findPreviousToken(ts, Arrays.asList(JsTokenId.IDENTIFIER));
                    if (token.id() == JsTokenId.IDENTIFIER) {
                        if (DEFINE.equals(token.text().toString()) || REQUIRE.equals(token.text().toString()) || REQUIREJS.equals(token.text().toString())) {
                            return CodeCompletionContext.REQUIRE_MODULE;
                        } else if (BASE_URL.equals(token.text().toString())) {
                            return CodeCompletionContext.CONFIG_BASE_URL_VALUE;
                        } else if (PATHS.equals(token.text().toString())) {
                            // in the case, when the property of path are written as string
                            return CodeCompletionContext.CONFIG_PATHS_VALUE;
                        }
                        token = LexUtilities.findPreviousToken(ts, Arrays.asList(JsTokenId.BRACKET_LEFT_CURLY));
                        if (token.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                            token = LexUtilities.findPreviousToken(ts, Arrays.asList(JsTokenId.IDENTIFIER));
                            if (token.id() == JsTokenId.IDENTIFIER && PATHS.equals(token.text().toString())) {
                                return CodeCompletionContext.CONFIG_PATHS_VALUE;
                            }
                        }
                    }
                }
            } else {
                // can be property name?
                List<JsTokenId> listIds = Arrays.asList(JsTokenId.OPERATOR_COMMA, JsTokenId.OPERATOR_COLON, JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.OPERATOR_SEMICOLON, JsTokenId.BRACKET_RIGHT_CURLY);
                // find previous , or : or { or ;
                token = LexUtilities.findPreviousToken(ts, listIds);

                boolean commaFirst = false;
                boolean isPropertyName = false;
                int balance = 1;
                while (token.id() == JsTokenId.OPERATOR_COMMA && ts.movePrevious()) {
                    token = LexUtilities.findPreviousToken(ts, listIds);
                    commaFirst = true;
                    if (token.id() == JsTokenId.BRACKET_RIGHT_CURLY) {
                        while (ts.movePrevious() && balance > 0) {
                            token = ts.token();
                            if (token.id() == JsTokenId.BRACKET_RIGHT_CURLY) {
                                balance++;
                            } else if (token.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                                balance--;
                            }
                        }
                        token = LexUtilities.findPreviousToken(ts, listIds);
                    }
                    if (token.id() == JsTokenId.OPERATOR_COLON) {
                        // we are in the previous property definition
                        isPropertyName = true;
                        break;
                    }
                }
                List<JsTokenId> emptyIds = Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.BLOCK_COMMENT);
                if (token.id() == JsTokenId.BRACKET_LEFT_CURLY && ts.movePrevious()) {
                    
                    // check whether it's the first property in the object literal definion
                    token = LexUtilities.findPrevious(ts, emptyIds);
                    if (token.id() == JsTokenId.BRACKET_LEFT_PAREN || token.id() == JsTokenId.OPERATOR_COMMA || token.id() == JsTokenId.OPERATOR_EQUALS) {
                        isPropertyName = true;
                    } else if (token.id() == JsTokenId.BRACKET_RIGHT_PAREN) {
                        // it can be a method definition
                        balance = 1;
                        while (ts.movePrevious() && balance > 0) {
                            token = ts.token();
                            if (token.id() == JsTokenId.BRACKET_RIGHT_PAREN) {
                                balance++;
                            } else if (token.id() == JsTokenId.BRACKET_LEFT_PAREN) {
                                balance--;
                            }
                        }
                        if (balance == 0) {
                            token = LexUtilities.findPrevious(ts, emptyIds);
                            if (token.id() == JsTokenId.KEYWORD_FUNCTION && ts.movePrevious()) {
                                // we found a method definition, now we need to check, whether its in an object literal
                                token = LexUtilities.findPrevious(ts, emptyIds);
                                if (token.id() == JsTokenId.OPERATOR_COLON) {
                                    isPropertyName = commaFirst;
                                }
                            }
                        }
                    }
                }
                if (isPropertyName) {
                    if (token.id() == JsTokenId.OPERATOR_COLON) {
                        balance = 1;
                        while (ts.movePrevious() && balance > 0) {
                            token = ts.token();
                            if (token.id() == JsTokenId.BRACKET_RIGHT_CURLY) {
                                balance++;
                            } else if (token.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                                balance--;
                            }
                        }
                        token = LexUtilities.findPrevious(ts, emptyIds);
                    }
                    if (token.id() == JsTokenId.BRACKET_LEFT_PAREN && ts.movePrevious()) {
                        token = LexUtilities.findPrevious(ts, emptyIds);
                        if (token.id() == JsTokenId.IDENTIFIER && CONFIG_METHOD_NAME.equals(token.text().toString())) {
                            return CodeCompletionContext.CONFIG_PROPERTY_NAME;
                        }
                    }
                }
            }
        }
        return CodeCompletionContext.UNKNOWN;
    }

    public static Collection<String> getUsedFileInDefine(final Snapshot shanpshot, final int offset) {
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(shanpshot, offset);
        if (ts == null) {
            return Collections.emptyList();
        }
        ts.move(0);
        if (!ts.moveNext()) {
            return Collections.emptyList();
        }
        Token<? extends JsTokenId> token = LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.IDENTIFIER));
        while (token.id() == JsTokenId.IDENTIFIER && !DEFINE.equals(token.text().toString()) && ts.moveNext()) {
            token = LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.IDENTIFIER));
        }
        if (token.id() == JsTokenId.IDENTIFIER && DEFINE.equals(token.text().toString())) {
            // we are probably found the define method
            List<String> paths = new ArrayList<String>();
            token = LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.BRACKET_LEFT_BRACKET, JsTokenId.KEYWORD_FUNCTION, JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_PAREN));
            if (token.id() == JsTokenId.BRACKET_LEFT_BRACKET) {
                do {
                    token = LexUtilities.findNextToken(ts, Arrays.asList(JsTokenId.STRING, JsTokenId.OPERATOR_COMMA, JsTokenId.BRACKET_RIGHT_PAREN));
                    if (token.id() == JsTokenId.STRING) {
                        paths.add(token.text().toString());
                    }
                } while ((token.id() != JsTokenId.BRACKET_RIGHT_PAREN && token.id() != JsTokenId.OPERATOR_SEMICOLON && !token.id().isKeyword()) && ts.moveNext());
                return paths;
            }
        }
        return Collections.emptyList();
    }
}
