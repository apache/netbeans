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
package org.netbeans.modules.j2ee.persistence.jpqleditor.lexer;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;

/**
 *
 * @author sp153251
 */
public enum JPQLTokenId implements TokenId {

    ERROR(null, "error"),
    IDENTIFIER(null, "identifier"),
 
    ABS, 
    ALL, 
    AND, 
    ANY, 
    AS, 
    ASC, 
    AVG, 
    BETWEEN,
    BIT_LENGTH, 
    BOTH, 
    BY, 
    CASE, 
    CHAR_LENGTH, 
    CHARACTER_LENGTH, 
    CLASS, 
    COALESCE,
    CONCAT, 
    COUNT, 
    CURRENT_DATE, 
    CURRENT_TIME, 
    CURRENT_TIMESTAMP,
    DELETE, 
    DESC, 
    DISTINCT, 
    ELSE, 
    EMPTY, 
    END, 
    ENTRY, 
    ESCAPE, 
    EXISTS, 
    FALSE, 
    FETCH,
    FROM, 
    FUNCTION, 
    GROUP, 
    HAVING, 
    IN, 
    INDEX, 
    INNER, 
    IS, 
    JOIN, 
    KEY, 
    LEADING, 
    LEFT,
    LENGTH, 
    LIKE, 
    LOCATE, 
    LOWER, 
    MAX, 
    MEMBER, 
    MIN, 
    MOD, 
    NEW, 
    NOT, 
    NULL, 
    NULLIF,
    OBJECT, 
    OF, 
    ON, 
    OR, 
    ORDER, 
    OUTER, 
    POSITION, 
    SELECT, 
    SET, 
    SIZE, 
    SOME, 
    SQRT, 
    SUBSTRING,
    SUM, 
    THEN, 
    TRAILING, 
    TREAT, 
    TRIM, 
    TRUE, 
    TYPE, 
    UNKNOWN, 
    UPDATE, 
    UPPER,
    VALUE, 
    WHEN, 
    WHERE,
    
    REF(null, "othres"),
    INT_LITERAL(null, "number"),
    LONG_LITERAL(null, "number"),
    FLOAT_LITERAL(null, "number"),
    DOUBLE_LITERAL(null, "number"),
    CHAR_LITERAL(null, "character"),
    STRING_LITERAL(null, "string"),
 
    LPAREN("(", "separator"),
    RPAREN(")", "separator"),
    LBRACE("{", "separator"),
    RBRACE("}", "separator"),
    LBRACKET("[", "separator"),
    RBRACKET("]", "separator"),
    SEMICOLON(";", "separator"),
    COMMA(",", "separator"),
    DOT(".", "separator"),
    EQ("=", "operator"),
    GT(">", "operator"),
    LT("<", "operator"),
    BANG("!", "operator"),
    TILDE("~", "operator"),
    QUESTION("?", "operator"),
    COLON(":", "operator"),
    LTEQ("<=", "operator"),
    GTEQ(">=", "operator"),
    BANGEQ("!=", "operator"),
    AMPAMP("&&", "operator"),
    BARBAR("||", "operator"),
    PLUSPLUS("++", "operator"),
    MINUSMINUS("--", "operator"),
    PLUS("+", "operator"),
    MINUS("-", "operator"),
    STAR("*", "operator"),
    SLASH("/", "operator"),
    AMP("&", "operator"),
    BAR("|", "operator"),
    CARET("^", "operator"),
    PERCENT("%", "operator"),
    LTLT("<<", "operator"),
    GTGT(">>", "operator"),
    GTGTGT(">>>", "operator"),
    PLUSEQ("+=", "operator"),
    MINUSEQ("-=", "operator"),
    STAREQ("*=", "operator"),
    SLASHEQ("/=", "operator"),
    AMPEQ("&=", "operator"),
    BAREQ("|=", "operator"),
    CARETEQ("^=", "operator"),
    PERCENTEQ("%=", "operator"),
    LTLTEQ("<<=", "operator"),
    GTGTEQ(">>=", "operator"),
    GTGTGTEQ(">>>=", "operator"),
    ELLIPSIS("...", "special"),
    AT("@", "special"),
    WHITESPACE(null, "whitespace");
    private final String primaryCategory;
    private  String text;

    JPQLTokenId() {
        this(null, "keyword");
        text = name().toLowerCase();
    }
    
    JPQLTokenId(
            String text,
            String primaryCategory) {
        this.text = text;
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }
    
    public String getText() {
        return text;
    }

    public static Language<JPQLTokenId> getLanguage() {
        return new JPQLLanguageHierarchy().language();
    }
}
