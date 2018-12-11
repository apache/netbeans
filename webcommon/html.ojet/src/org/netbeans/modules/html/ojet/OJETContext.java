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
package org.netbeans.modules.html.ojet;

import java.util.Arrays;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.knockout.api.KODataBindTokenId;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author Petr Pisl
 */
public enum OJETContext {

    DATA_BINDING,
    /**
     * Only in empty configuration json object of a component ojComponent: { | }
     */
    COMP_CONF,
    /**
     * In configuration json object of a component, when names of the properties
     * should be displayed ojComponent:{component: 'ojProgressbar', | }
     */
    COMP_CONF_PROP_NAME,
    /**
     * In configuration json object of a component, where list of components
     * should be displayed. ojComponent:{component: '|' } or
     * ojComponent:{component: | }
     */
    COMP_CONF_COMP_NAME,
    /**
     * IN configuration object for binding a module: ojModule: {}
     */
    MODULE_PROP_NAME,
    UNKNOWN;

    private static String COMPONENT = "component"; //NOI18N

    public static OJETContext findContext(Document document, int offset) {
        TokenHierarchy th = TokenHierarchy.get(document);
        TokenSequence<HTMLTokenId> ts = LexerUtils.getTokenSequence(th, offset, HTMLTokenId.language(), false);
        String binding = null;
        
        if (ts != null) {
            int diff = ts.move(offset);
            if (diff == 0 && ts.movePrevious() || ts.moveNext()) {
                if (ts.token().id() == HTMLTokenId.VALUE) {
                    TokenSequence<KODataBindTokenId> dataBindTs = ts.embedded(KODataBindTokenId.language());
                    if (dataBindTs != null) {
                        if (dataBindTs.isEmpty()) {
                            return DATA_BINDING;
                        }
                    } else {
                        return UNKNOWN;
                    }
                    int ediff = dataBindTs.move(offset);
                    if (ediff == 0 && dataBindTs.movePrevious() || dataBindTs.moveNext()) {
                        //we are on a token of ko-data-bind token sequence
                        Token<KODataBindTokenId> etoken = dataBindTs.token();
                        if (etoken.id() == KODataBindTokenId.KEY) {
                            //ke|
                            return DATA_BINDING;
                        }
                        etoken = LexerUtils.followsToken(dataBindTs,
                                Arrays.asList(KODataBindTokenId.COLON, KODataBindTokenId.COMMA, KODataBindTokenId.VALUE),
                                true, false, KODataBindTokenId.WS);
                        if (etoken == null) {
                            // we are at the beginning of the value
                            return DATA_BINDING;
                        }
                        if (etoken.id() == KODataBindTokenId.VALUE) {
                            etoken = LexerUtils.followsToken(dataBindTs, KODataBindTokenId.KEY, true, true, KODataBindTokenId.COLON);
                            if (!(etoken != null && etoken.id() == KODataBindTokenId.KEY
                                    && OJETUtils.OJ_COMPONENT.equals(etoken.text().toString()))) {
                                // continue only if we are in the value
                                return UNKNOWN;
                            }
                        }
                        if (etoken.id() == KODataBindTokenId.COMMA) {
                            return DATA_BINDING;
                        }
                        if (etoken.id() == KODataBindTokenId.COLON) {
                            etoken = LexerUtils.followsToken(dataBindTs, KODataBindTokenId.KEY, true, true, KODataBindTokenId.COLON);
                            if (etoken != null && etoken.id() == KODataBindTokenId.KEY) {
                                String key = etoken.text().toString();
                                if (OJETUtils.OJ_COMPONENT.equals(key) || OJETUtils.OJ_MODULE.equals(key)) {
                                    binding = key;
                                } else {
                                    // continue only if we are in the value of component or module
                                    return UNKNOWN;
                                }
                            } else {
                                return UNKNOWN;
                            }
                        }
                    }
                }
            }
        }
        TokenSequence<JsTokenId> jsTs = LexerUtils.getTokenSequence(th, offset, JsTokenId.javascriptLanguage(), false);
        if (jsTs != null && binding != null) {
            int diff = jsTs.move(offset);
            if (diff == 0 && jsTs.movePrevious() || jsTs.moveNext()) {
                Token<JsTokenId> jsToken = jsTs.token();
                if (jsToken.id() == JsTokenId.UNKNOWN && !jsTs.movePrevious()) {
                    return UNKNOWN;
                }
                jsToken = LexerUtils.followsToken(jsTs,
                        Arrays.asList(JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.OPERATOR_COLON, JsTokenId.OPERATOR_COMMA), true, false, true,
                        JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.STRING, JsTokenId.STRING_BEGIN, JsTokenId.IDENTIFIER);
                if (jsToken == null) {
                    return UNKNOWN;
                }
                if (jsToken.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                    if (OJETUtils.OJ_COMPONENT.equals(binding)) {
                        return COMP_CONF;
                    } else {
                        return MODULE_PROP_NAME;
                    }
                    
                } else if (jsToken.id() == JsTokenId.OPERATOR_COLON) {
                    // we are in the valeu
                    // find the name of property
                    jsToken = LexerUtils.followsToken(jsTs, Arrays.asList(JsTokenId.IDENTIFIER), true, false, JsTokenId.WHITESPACE, JsTokenId.EOL);
                    if (jsToken != null && jsToken.id() == JsTokenId.IDENTIFIER) {
                        if (COMPONENT.equals(jsToken.text().toString())) {
                            return COMP_CONF_COMP_NAME;
                        }
                    }
                }
                if (jsToken != null && jsToken.id() == JsTokenId.OPERATOR_COMMA) {
                    if (OJETUtils.OJ_COMPONENT.equals(binding)) {
                        return COMP_CONF_PROP_NAME;
                    } else {
                        return MODULE_PROP_NAME;
                    }
                }
            }

        }

        return UNKNOWN;
    }

    /**
     * Can return null, if there is no component property defined
     *
     * @param document
     * @param dOffset
     */
    public static String findComponentName(Document document, int offset) {
        String name = null;
        TokenHierarchy th = TokenHierarchy.get(document);
        if (th == null) {
            return name;
        }
        TokenSequence<JsTokenId> ts = LexerUtils.getTokenSequence(th, offset, JsTokenId.javascriptLanguage(), false);
        if (ts != null) {
            // try to find from beginning
            ts.moveStart();
            if (ts.movePrevious() || ts.moveNext()) {
                Token<JsTokenId> jsToken = LexerUtils.followsToken(ts, JsTokenId.IDENTIFIER, false, false, 
                        JsTokenId.WHITESPACE, JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.EOL);
                if (jsToken != null && jsToken.id() == JsTokenId.IDENTIFIER) {
                    if (COMPONENT.equals(jsToken.text().toString())) {
                        // we found the component property, now to find the value
                        jsToken = LexerUtils.followsToken(ts, JsTokenId.STRING, false, false, 
                        JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT, JsTokenId.EOL, JsTokenId.OPERATOR_COLON, JsTokenId.STRING_BEGIN);
                        if (jsToken != null && jsToken.id() == JsTokenId.STRING) {
                            return jsToken.text().toString();
                        }
                    }
                }
            }

        }
        return name;
    }
}
