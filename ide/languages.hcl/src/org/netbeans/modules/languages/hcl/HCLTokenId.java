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
package org.netbeans.modules.languages.hcl;

import org.netbeans.api.lexer.TokenId;

/**
 *
 * @author lkishalmi
 */
public enum HCLTokenId implements TokenId {
    LINE_COMMENT(null, "comment"),
    BLOCK_COMMENT(null, "comment"),

    TRUE("true", "boolean"),
    FALSE("false", "boolean"),
    NULL("null", "keyword"),

    LEGACY_INDEX(null, "number"),
    NUMERIC_LIT(null, "number"),
    
    IDENTIFIER(null,"identifier"),

    FOR("for", "keyword"),
    IF("if", "keyword"),
    IN("in", "keyword"),


    LBRACE("{", "group-separator"),
    RBRACE("}", "group-separator"),
    LBRACK("[", "group-separator"),
    RBRACK("]", "group-separator"),

    LPAREN("(", "separator"),
    RPAREN(")", "separator"),
    COLON(":", "separator"),
    COMMA(",", "separator"),
    DOT(".", "separator"),
    EQUAL("=", "separator"),
    INTERPOLATION_START("${", "separator"),
    INTERPOLATION_END("}", "separator"),
    RARROW("=>", "separator"),
    TEMPLATE_START("%{", "separator"),
    TEMPLATE_END("}", "separator"),

    AND("&&", "operator"),
    ELLIPSIS("...", "operator"),
    EQUALS("--", "operator"),
    GT(">", "operator"),
    GTE(">=", "operator"),
    LT("<", "operator"),
    LTE("<=", "operator"),
    MINUS("-", "operator"),
    NOT("!", "operator"),
    NOT_EQUALS("!=", "operator"),
    OR("||", "operator"),
    PERCENT("%", "operator"),
    PLUS("+", "operator"),
    QUESTION("?", "operator"),
    SLASH("/", "operator"),
    STAR("*", "operator"),

    QUOTE("\"", "string"),

    HEREDOC_START(null, "heredoc-guard"),
    HEREDOC_END(null, "heredoc-guard"),

    HEREDOC(null, "heredoc"),

    STRING(null, "string"),

    INTERPOLATION(null, "interpolation"),
    TEMPLATE(null, "interpolation"),

    WS(null, "whitespace"),
    NL(null, "whitespace"),

    ERROR(null, "error");

    private final String fixedText;
    private final String category;

    private HCLTokenId(String fixedText, String category) {
        this.fixedText = fixedText;
        this.category = category;
    }

    public String getFixedText() {
        return fixedText;
    }
    
    @Override
    public String primaryCategory() {
        return category;
    }

    public static boolean isGroupOpen(HCLTokenId id) {
        return (id == LBRACE) || (id == LBRACK) || (id == LPAREN);
    }

    public static boolean isGroupClose(HCLTokenId id) {
        return (id == RBRACE) || (id == RBRACK) || (id == RPAREN);
    }

}
