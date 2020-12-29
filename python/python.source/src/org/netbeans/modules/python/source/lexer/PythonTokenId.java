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
package org.netbeans.modules.python.source.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.modules.python.api.PythonMIMEResolver;
import org.netbeans.modules.python.source.PythonUtils;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

import org.netbeans.spi.lexer.TokenFactory;
import org.openide.filesystems.FileObject;
import static org.netbeans.modules.python.source.lexer.PythonLexer.*;

/**
 * @todo add the rest of the tokens
 *
 */
public enum PythonTokenId implements TokenId {
    ERROR(null, ERROR_CAT),
    IDENTIFIER(null, IDENTIFIER_CAT),
    INT_LITERAL(null, NUMBER_CAT),
    FLOAT_LITERAL(null, NUMBER_CAT),
    STRING_LITERAL(null, STRING_CAT),
    WHITESPACE(null, WHITESPACE_CAT),
    NEWLINE(null, WHITESPACE_CAT),
    DECORATOR(null, OPERATOR_CAT), // NOI18N
    COMMENT(null, COMMENT_CAT),
    BUILTIN_FUNCTION(null, KEYWORD_CAT), // NOI18N
    LPAREN("(", SEPARATOR_CAT), // NOI18N
    RPAREN(")", SEPARATOR_CAT), // NOI18N
    LBRACE("{", SEPARATOR_CAT), // NOI18N
    RBRACE("}", SEPARATOR_CAT), // NOI18N
    LBRACKET("[", SEPARATOR_CAT), // NOI18N
    RBRACKET("]", SEPARATOR_CAT), // NOI18N
    STRING_BEGIN(null, STRING_CAT),
    STRING_END(null, STRING_CAT),
    // Cheating: out of laziness just map all keywords returning from Jython
    // into a single KEYWORD token; eventually we will have separate tokens
    // for each here such that the various helper methods for formatting,
    // smart indent, brace matching etc. can refer to specific keywords
    ANY_KEYWORD(null, KEYWORD_CAT),
    ANY_OPERATOR(null, OPERATOR_CAT),
    DEF("def", KEYWORD_CAT), // NOI18N
    CLASS("class", KEYWORD_CAT), // NOI18N
    IF("if", KEYWORD_CAT), // NOI18N
    ELSE("else", KEYWORD_CAT), // NOI18N
    ELIF("elif", KEYWORD_CAT), // NOI18N
    RAISE("raise", KEYWORD_CAT), // NOI18N
    PASS("pass", KEYWORD_CAT), // NOI18N
    RETURN("return", KEYWORD_CAT), // NOI18N
    EXCEPT("except", KEYWORD_CAT), // NOI18N
    FINALLY("finally", KEYWORD_CAT), // NOI18N
    IMPORT("import", KEYWORD_CAT), // NOI18N
    FROM("from", KEYWORD_CAT), // NOI18N
    BOOL(null, KEYWORD_CAT), // NOI18N
    TRY("try", KEYWORD_CAT), // NOI18N
    DOT(".", OPERATOR_CAT), // NOI18N
    COMMA(",", OPERATOR_CAT), // NOI18N
    COLON(":", OPERATOR_CAT), // NOI18N
    ESC("\\", OPERATOR_CAT), // NOI18N

    // Non-unary operators which indicate a line continuation if used at the end of a line
    NONUNARY_OP(null, OPERATOR_CAT);
    private final String fixedText;
    private final String primaryCategory;

    PythonTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }
    private static final Language<PythonTokenId> language =
            new LanguageHierarchy<PythonTokenId>() {
                @Override
                protected String mimeType() {
                    return PythonMIMEResolver.PYTHON_MIME_TYPE;
                }

                @Override
                protected Collection<PythonTokenId> createTokenIds() {
                    return EnumSet.allOf(PythonTokenId.class);
                }

                @Override
                protected Map<String, Collection<PythonTokenId>> createTokenCategories() {
                    Map<String, Collection<PythonTokenId>> cats =
                            new HashMap<>();
                    return cats;
                }

                @Override
                protected Lexer<PythonTokenId> createLexer(LexerRestartInfo<PythonTokenId> info) {
                    FileObject fileObject = (FileObject)info.getAttributeValue(FileObject.class);
                    final TokenFactory<PythonTokenId> tokenFactory = info.tokenFactory();
                    final LexerInput input = info.input();
                    // Lex .rst files just as literal strings
                    if (fileObject != null && PythonUtils.isRstFile(fileObject)) {
                        return new Lexer<PythonTokenId>() {
                            @Override
                            public Token<PythonTokenId> nextToken() {
                                if (input.read() == LexerInput.EOF) {
                                    return null;
                                }
                                while (input.read() != LexerInput.EOF) {
                                    ;
                                }
                                return tokenFactory.createToken(PythonTokenId.STRING_LITERAL, input.readLength());
                            }

                            @Override
                            public Object state() {
                                return null;
                            }

                            @Override
                            public void release() {
                            }
                        };
                    }
                    return new PythonLexer(info);
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<PythonTokenId> token,
                        LanguagePath languagePath, InputAttributes inputAttributes) {
                    PythonTokenId id = token.id();
                    if (id == STRING_LITERAL) {
                        return LanguageEmbedding.create(PythonStringTokenId.language, 0, 0);
                    } else if (id == COMMENT) {
                        return LanguageEmbedding.create(PythonCommentTokenId.language(), 1, 0);
                    }

                    return null; // No embedding
                }
            }.language();

    public static Language<PythonTokenId> language() {
        return language;
    }
}
