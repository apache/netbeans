/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
