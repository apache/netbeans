/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.el.lexer.api;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.el.lexer.ELLexer;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Token IDs of Expression Language
 *
 * @author Petr Pisl
 * @author Marek.Fukala@Sun.COM
 */
public enum ELTokenId implements TokenId {
    
    //operators
    LT("<", ELTokenCategories.OPERATORS.categoryName),
    GT(">", ELTokenCategories.OPERATORS.categoryName),
    DOT(".", ELTokenCategories.OPERATORS.categoryName),
    COMMA(",", ELTokenCategories.OPERATORS.categoryName),
    QUESTION("?", ELTokenCategories.OPERATORS.categoryName),
    EQ("=", ELTokenCategories.OPERATORS.categoryName),
    PLUS("+", ELTokenCategories.OPERATORS.categoryName),
    CONCAT("+=", ELTokenCategories.OPERATORS.categoryName),
    MINUS("-", ELTokenCategories.OPERATORS.categoryName),
    MUL("*", ELTokenCategories.OPERATORS.categoryName),
    DIV("/", ELTokenCategories.OPERATORS.categoryName),
    MOD("%", ELTokenCategories.OPERATORS.categoryName),
    EQ_EQ("==", ELTokenCategories.OPERATORS.categoryName),
    LT_EQ("<=", ELTokenCategories.OPERATORS.categoryName),
    GT_EQ(">=", ELTokenCategories.OPERATORS.categoryName),
    NOT_EQ("!=", ELTokenCategories.OPERATORS.categoryName),
    AND_AND("&&", ELTokenCategories.OPERATORS.categoryName),
    OR_OR("||", ELTokenCategories.OPERATORS.categoryName),
    COLON(":", ELTokenCategories.OPERATORS.categoryName),
    SEMICOLON(";", ELTokenCategories.OPERATORS.categoryName),
    NOT("!", ELTokenCategories.OPERATORS.categoryName),
    LPAREN("(", ELTokenCategories.OPERATORS.categoryName),
    RPAREN(")", ELTokenCategories.OPERATORS.categoryName),
    LBRACKET("[", ELTokenCategories.OPERATORS.categoryName),
    RBRACKET("]", ELTokenCategories.OPERATORS.categoryName),
    LAMBDA("->", ELTokenCategories.OPERATORS.categoryName),
    
    //keywords
    AND_KEYWORD("and", ELTokenCategories.KEYWORDS.categoryName),
    DIV_KEYWORD("div", ELTokenCategories.KEYWORDS.categoryName),
    EMPTY_KEYWORD("empty", ELTokenCategories.KEYWORDS.categoryName),
    EQ_KEYWORD("eq", ELTokenCategories.KEYWORDS.categoryName),
    FALSE_KEYWORD("false", ELTokenCategories.KEYWORDS.categoryName),
    GE_KEYWORD("ge", ELTokenCategories.KEYWORDS.categoryName),
    GT_KEYWORD("gt", ELTokenCategories.KEYWORDS.categoryName),
    INSTANCEOF_KEYWORD("instanceof", ELTokenCategories.KEYWORDS.categoryName),
    LE_KEYWORD("le", ELTokenCategories.KEYWORDS.categoryName),
    LT_KEYWORD("lt", ELTokenCategories.KEYWORDS.categoryName),
    MOD_KEYWORD("mod", ELTokenCategories.KEYWORDS.categoryName),
    NE_KEYWORD("ne", ELTokenCategories.KEYWORDS.categoryName),
    NOT_KEYWORD("not", ELTokenCategories.KEYWORDS.categoryName),
    NULL_KEYWORD("null", ELTokenCategories.KEYWORDS.categoryName),
    OR_KEYWORD("or", ELTokenCategories.KEYWORDS.categoryName),
    TRUE_KEYWORD("true", ELTokenCategories.KEYWORDS.categoryName),
    
    //literals
    WHITESPACE(null, "whitespace"),
    EOL("\n", "eol"),
    STRING_LITERAL(null, "string"),
    TAG_LIB_PREFIX(null, "tag-lib-prefix"),
    IDENTIFIER(null, "identifier"),
    CHAR_LITERAL(null, "char-literal"),
    
    //numeric literals
    /** Java integer literal e.g. 1234 */
    INT_LITERAL(null, "int-literal"),
    /** Java long literal e.g. 12L */
    LONG_LITERAL(null, "long-literal"),
    /** Java hexadecimal literal e.g. 0x5a */
    HEX_LITERAL(null, "hex-literal"),
    /** Java octal literal e.g. 0123 */
    OCTAL_LITERAL(null, "octal-literal"),
    /** Java float literal e.g. 1.5e+20f */
    FLOAT_LITERAL(null, "float-literal"),
    /** Java double literal e.g. 1.5e+20 */
    DOUBLE_LITERAL(null, "double-literal"),
    // Incomplete and error token-ids
    INVALID_OCTAL_LITERAL(null, "invalid-octal-literal"),
    INVALID_CHAR(null, "invalid-char");
    
    
    /** EL token categories enum.*/
    public static enum ELTokenCategories {
        
        /** Token category for EL keywords like and, false etc. */
        KEYWORDS("keyword"),
        /** Token category for EL operators like ==, => etc. */
        OPERATORS("operators"),
        /** Token category for EL numeric literals. */
        NUMERIC_LITERALS("numeric-literals"),
        /** Token category for EL errors. */
        ERRORS("error");
        
        private final String categoryName;
        
        ELTokenCategories(String categoryName) {
            this.categoryName = categoryName;
        }

        public boolean hasCategory(TokenId id) {
            return id.primaryCategory().equals(categoryName);
        }
        
    }
    
    private final String fixedText; // Used by lexer for production of flyweight tokens
    
    private final String primaryCategory;
    
    ELTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }
    
    /** Get fixed text of the token. */
    public String fixedText() {
        return fixedText;
    }
    
    /**
     * Get name of primary token category into which this token belongs.
     * <br/>
     * Other token categories for this id can be defined in the language hierarchy.
     *
     * @return name of the primary token category into which this token belongs
     *  or null if there is no primary category for this token.
     */
    
    public String primaryCategory() {
        return primaryCategory;
    }
    
    private static final Language<ELTokenId> language = new LanguageHierarchy<ELTokenId>() {
        @Override
        protected Collection<ELTokenId> createTokenIds() {
            return EnumSet.allOf(ELTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<ELTokenId>> createTokenCategories() {
            Map<String,Collection<ELTokenId>> cats = new HashMap<String,Collection<ELTokenId>>();
            
            cats.put(ELTokenCategories.NUMERIC_LITERALS.categoryName, EnumSet.of(
                    ELTokenId.INT_LITERAL,
                    ELTokenId.LONG_LITERAL,
                    ELTokenId.HEX_LITERAL,
                    ELTokenId.OCTAL_LITERAL,
                    ELTokenId.FLOAT_LITERAL,
                    ELTokenId.DOUBLE_LITERAL));
            
            cats.put(ELTokenCategories.ERRORS.categoryName, EnumSet.of(
                    ELTokenId.INVALID_OCTAL_LITERAL,
                    ELTokenId.INVALID_CHAR));
            
            return cats;
        }
        
        @Override
        protected Lexer<ELTokenId> createLexer(LexerRestartInfo<ELTokenId> info) {
            return new ELLexer(info);
        }
        
        @Override
        public LanguageEmbedding<?> embedding(
                Token<ELTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }
        
        @Override
        protected String mimeType() {
            return "text/x-el"; //???
        }
    }.language();
    
    /** Gets a LanguageDescription describing a set of token ids
     * that comprise the given language.
     *
     * @return non-null LanguageDescription
     */
    public static Language<ELTokenId> language() {
        return language;
    }
    
    
}
