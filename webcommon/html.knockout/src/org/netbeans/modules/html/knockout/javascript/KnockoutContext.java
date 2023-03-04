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
package org.netbeans.modules.html.knockout.javascript;

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
 * @author Roman Svitanic
 */
public enum KnockoutContext {

    DATA_BINDING,
    /**
     * Simple component binding. All available components should be displayed.
     * component: |
     */
    COMPONENT_EMPTY,
    /**
     * Empty configuration object of a component. Properties 'name' and 'params'
     * should be displayed. component: { | }
     */
    COMPONENT_CONF_EMPTY,
    /**
     * CC for available custom component names. component: {name: | }
     */
    COMPONENT_CONF_NAME,
    /**
     * Configuration object with name. 'params' property should be displayed.
     * component: {name: 'mycomponent', | }
     */
    COMPONENT_CONF_PARAMS,
    /**
     * Value for 'params' in configuration object. All parameters of component
     * specified with 'name' should be offered. component: {name: 'mycomponent',
     * params: { | } }
     */
    COMPONENT_CONF_PARAMS_VALUE,
    UNKNOWN;

    private static final String KO_COMPONENT = "component"; //NOI18N
    private static final String COMPONENT_NAME_PROP = "name"; //NOI18N
    private static final String COMPONENT_PARAMS_PROP = "params"; //NOI18N

    public static KnockoutContext findContext(Document document, int offset) {
        TokenHierarchy th = TokenHierarchy.get(document);
        TokenSequence<HTMLTokenId> ts = LexerUtils.getTokenSequence(th, offset, HTMLTokenId.language(), false);
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
                                true, false, true, KODataBindTokenId.WS);
                        if (etoken == null) {
                            return UNKNOWN;
                        }
                        if (etoken.id() == KODataBindTokenId.VALUE || etoken.id() == KODataBindTokenId.COLON) {
                            etoken = LexerUtils.followsToken(dataBindTs, KODataBindTokenId.KEY, true, true, KODataBindTokenId.COLON);
                            if (!(etoken != null && etoken.id() == KODataBindTokenId.KEY
                                    && KO_COMPONENT.equals(etoken.text().toString()))) {
                                // continue only if we are in the value and key indicates "component" binding
                                return KnockoutContext.UNKNOWN;
                            }
                        }
                        if (etoken.id() == KODataBindTokenId.COMMA) {
                            return DATA_BINDING;
                        }
                    }
                } else {
                    return UNKNOWN;
                }
            }
        }
        TokenSequence<JsTokenId> jsTs = LexerUtils.getTokenSequence(th, offset, JsTokenId.javascriptLanguage(), false);
        if (jsTs != null) {
            int diff = jsTs.move(offset);
            if (diff == 0 && jsTs.movePrevious() || jsTs.moveNext()) {
                Token<JsTokenId> jsToken = jsTs.token();
                if (jsToken.id() == JsTokenId.UNKNOWN && !jsTs.movePrevious()) {
                    return UNKNOWN;
                }
                jsToken = LexerUtils.followsToken(jsTs,
                        Arrays.asList(JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_CURLY, JsTokenId.OPERATOR_COLON, JsTokenId.OPERATOR_COMMA),
                        true, false, true,
                        JsTokenId.WHITESPACE, JsTokenId.EOL, JsTokenId.STRING, JsTokenId.STRING_BEGIN, JsTokenId.IDENTIFIER);
                if (jsToken == null) {
                    return COMPONENT_EMPTY;
                }
                if (jsToken.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                    // ensure that we are in configuration object of a component, not in some other anonymous object
                    jsToken = LexerUtils.followsToken(jsTs, JsTokenId.IDENTIFIER, true, false, JsTokenId.OPERATOR_COLON, JsTokenId.WHITESPACE);
                    if (jsToken == null || (jsToken.id() == JsTokenId.IDENTIFIER && jsToken.text().toString().equals(KO_COMPONENT))) {
                        return COMPONENT_CONF_EMPTY;
                    } else if (jsToken.id() == JsTokenId.IDENTIFIER && COMPONENT_PARAMS_PROP.equals(jsToken.text().toString())) {
                        // we are in empty params: { | } configuration object
                        return COMPONENT_CONF_PARAMS_VALUE;
                    }
                } else if (jsToken.id() == JsTokenId.OPERATOR_COLON) {
                    // we are in the value
                    // find the name of property
                    jsToken = LexerUtils.followsToken(jsTs, Arrays.asList(JsTokenId.IDENTIFIER), true, false, JsTokenId.WHITESPACE, JsTokenId.EOL);
                    if (jsToken != null && jsToken.id() == JsTokenId.IDENTIFIER) {
                        if (COMPONENT_NAME_PROP.equals(jsToken.text().toString())) {
                            return COMPONENT_CONF_NAME;
                        }
                    }
                } else if (jsToken.id() == JsTokenId.BRACKET_RIGHT_CURLY) {
                    // we are after configuration object
                    return UNKNOWN;
                } else if (jsToken.id() == JsTokenId.OPERATOR_COMMA) {
                    // we are after comma, it can be either after name property "name: 'my-component', |"
                    // or it can be in params configuration object "name: 'my-component', params: {param1: value1, |}"
                    // To determine the case, go back to last opening curly bracket and check whether it belongs to the value of params property.
                    jsToken = LexerUtils.followsToken(jsTs, Arrays.asList(JsTokenId.BRACKET_LEFT_CURLY), true, false,
                            JsTokenId.WHITESPACE, JsTokenId.NUMBER, JsTokenId.IDENTIFIER, JsTokenId.STRING, JsTokenId.STRING_BEGIN, JsTokenId.STRING_END, JsTokenId.OPERATOR_COMMA, JsTokenId.OPERATOR_COLON);
                    if (jsToken != null) {
                        jsToken = LexerUtils.followsToken(jsTs, Arrays.asList(JsTokenId.IDENTIFIER), true, false,
                                JsTokenId.WHITESPACE, JsTokenId.OPERATOR_COLON);
                        if (jsToken != null && jsToken.id() == JsTokenId.IDENTIFIER && jsToken.text().toString().equals(COMPONENT_PARAMS_PROP)) {
                            // identifier with 'params' text has been found, we are in params conf. object
                            return COMPONENT_CONF_PARAMS_VALUE;
                        } else {
                            // no identifier before '{' => CC should offer params property
                            return COMPONENT_CONF_PARAMS;
                        }
                    }
                }
            }
        }

        return UNKNOWN;
    }

}
