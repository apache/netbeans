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
package org.netbeans.modules.php.editor.completion;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.NavUtils;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTError;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
final class CompletionContextFinder {

    private static final String NAMESPACE_FALSE_TOKEN = "NAMESPACE_FALSE_TOKEN"; //NOI18N
    private static final String GROUP_USE_STATEMENT_TOKENS = "GROUP_USE_STATEMENT_TOKENS"; //NOI18N
    private static final String MULTI_CATCH_EXCEPTION_TOKENS = "MULTI_CATCH_EXCEPTION_TOKENS"; //NOI18N
    private static final String COMBINED_USE_STATEMENT_TOKENS = "COMBINED_USE_STATEMENT_TOKENS"; //NOI18N
    private static final String CONST_STATEMENT_TOKENS = "CONST_STATEMENT_TOKENS"; //NOI18N
    private static final String CONST_DECLARED_TYPE_TOKENS = "CONST_DECLARED_TYPE_TOKENS"; //NOI18N
    private static final String ENUM_CASE_STATEMENT_TOKENS = "ENUM_CASE_STATEMENT_TOKENS"; //NOI18N
    private static final String FIELD_UNION_OR_INTERSECTION_TYPE_TOKENS = "FIELD_UNION_TYPE_TOKENS"; //NOI18N
    private static final String FIELD_MODIFIERS_TOKENS = "FIELD_MODIFIERS_TOKENS"; //NOI18N
    private static final String OBJECT_OPERATOR_TOKEN = "OBJECT_OPERATOR_TOKEN"; //NOI18N
    private static final String TYPE_KEYWORD = "TYPE_KEYWORD"; //NOI18N
    private static final PHPTokenId[] COMMENT_TOKENS = new PHPTokenId[]{
            PHPTokenId.PHP_COMMENT_START, PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_LINE_COMMENT, PHPTokenId.PHP_COMMENT_END};
    private static final PHPTokenId[] PHPDOC_TOKENS = new PHPTokenId[]{
            PHPTokenId.PHPDOC_COMMENT_START, PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END};
    private static final List<Object[]> CLASS_NAME_TOKENCHAINS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_NEW},
            new Object[]{PHPTokenId.PHP_NEW, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_NEW, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN},
            new Object[]{PHPTokenId.PHP_NEW, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING});
    private static final List<Object[]> THROW_TOKEN_CHAINS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_THROW},
            new Object[]{PHPTokenId.PHP_THROW, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_THROW, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN},
            new Object[]{PHPTokenId.PHP_THROW, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING});
    private static final List<Object[]> THROW_NEW_TOKEN_CHAINS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_THROW, PHPTokenId.WHITESPACE, PHPTokenId.PHP_NEW},
            new Object[]{PHPTokenId.PHP_THROW, PHPTokenId.WHITESPACE, PHPTokenId.PHP_NEW, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_THROW, PHPTokenId.WHITESPACE, PHPTokenId.PHP_NEW, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN},
            new Object[]{PHPTokenId.PHP_THROW, PHPTokenId.WHITESPACE, PHPTokenId.PHP_NEW, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING});
    private static final List<Object[]> FUNCTION_NAME_TOKENCHAINS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_FUNCTION},
            new Object[]{PHPTokenId.PHP_FUNCTION, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_FUNCTION, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN},
            new Object[]{PHPTokenId.PHP_FUNCTION, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING});
    private static final List<Object[]> GROUP_USE_KEYWORD_TOKENS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN, GROUP_USE_STATEMENT_TOKENS},
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN, PHPTokenId.WHITESPACE, GROUP_USE_STATEMENT_TOKENS});
    private static final List<Object[]> GROUP_USE_CONST_KEYWORD_TOKENS = Arrays.<Object[]>asList(
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_CONST, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN, GROUP_USE_STATEMENT_TOKENS},
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_CONST, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN, PHPTokenId.WHITESPACE, GROUP_USE_STATEMENT_TOKENS});
    private static final List<Object[]> GROUP_USE_FUNCTION_KEYWORD_TOKENS = Arrays.<Object[]>asList(
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_FUNCTION, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN, GROUP_USE_STATEMENT_TOKENS},
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_FUNCTION, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN, PHPTokenId.WHITESPACE, GROUP_USE_STATEMENT_TOKENS});
    private static final List<Object[]> USE_KEYWORD_TOKENS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_USE},
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN},
            new Object[]{PHPTokenId.PHP_USE, COMBINED_USE_STATEMENT_TOKENS});
    private static final List<Object[]> USE_CONST_KEYWORD_TOKENS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_CONST},
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_CONST, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_CONST, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_CONST, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN},
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_CONST, COMBINED_USE_STATEMENT_TOKENS});
    private static final List<Object[]> USE_FUNCTION_KEYWORD_TOKENS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_FUNCTION},
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_FUNCTION, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_FUNCTION, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_FUNCTION, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN},
            new Object[]{PHPTokenId.PHP_USE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_FUNCTION, COMBINED_USE_STATEMENT_TOKENS});
    private static final List<Object[]> NAMESPACE_KEYWORD_TOKENS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_NAMESPACE},
            new Object[]{PHPTokenId.PHP_NAMESPACE, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_NAMESPACE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_NAMESPACE, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN});
    private static final List<Object[]> INSTANCEOF_TOKENCHAINS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_INSTANCEOF},
            new Object[]{PHPTokenId.PHP_INSTANCEOF, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_INSTANCEOF, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN},
            new Object[]{PHPTokenId.PHP_INSTANCEOF, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING});
    private static final List<Object[]> CATCH_TOKENCHAINS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_CATCH, MULTI_CATCH_EXCEPTION_TOKENS},
            new Object[]{PHPTokenId.PHP_CATCH, PHPTokenId.WHITESPACE, MULTI_CATCH_EXCEPTION_TOKENS}
    );
    private static final List<Object[]> CLASS_MEMBER_TOKENCHAINS = Arrays.asList(
            new Object[]{OBJECT_OPERATOR_TOKEN},
            new Object[]{OBJECT_OPERATOR_TOKEN, PHPTokenId.PHP_STRING},
            new Object[]{OBJECT_OPERATOR_TOKEN, PHPTokenId.PHP_VARIABLE},
            new Object[]{OBJECT_OPERATOR_TOKEN, PHPTokenId.PHP_TOKEN},
            new Object[]{OBJECT_OPERATOR_TOKEN, PHPTokenId.WHITESPACE},
            new Object[]{OBJECT_OPERATOR_TOKEN, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{OBJECT_OPERATOR_TOKEN, PHPTokenId.WHITESPACE, PHPTokenId.PHP_VARIABLE},
            new Object[]{OBJECT_OPERATOR_TOKEN, PHPTokenId.WHITESPACE, PHPTokenId.PHP_TOKEN});
    private static final List<Object[]> STATIC_CLASS_MEMBER_TOKENCHAINS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM},
            new Object[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.PHP_VARIABLE},
            new Object[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.PHP_TOKEN},
            new Object[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.WHITESPACE, PHPTokenId.PHP_VARIABLE},
            new Object[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.WHITESPACE, PHPTokenId.PHP_TOKEN});
    private static final List<Object[]> CLASS_MEMBER_IN_STRING_TOKENCHAINS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE, PHPTokenId.PHP_VARIABLE, OBJECT_OPERATOR_TOKEN},
            new Object[]{PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE, PHPTokenId.PHP_VARIABLE, OBJECT_OPERATOR_TOKEN, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE, PHPTokenId.PHP_VARIABLE, OBJECT_OPERATOR_TOKEN, PHPTokenId.PHP_TOKEN},
            new Object[]{PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE, PHPTokenId.PHP_VARIABLE, OBJECT_OPERATOR_TOKEN, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE, PHPTokenId.PHP_VARIABLE, OBJECT_OPERATOR_TOKEN, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE, PHPTokenId.PHP_VARIABLE, OBJECT_OPERATOR_TOKEN, PHPTokenId.WHITESPACE, PHPTokenId.PHP_TOKEN});
    private static final List<Object[]> METHOD_NAME_TOKENCHAINS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_FUNCTION},
            new Object[]{PHPTokenId.PHP_FUNCTION, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_FUNCTION, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING});
    private static final List<Object[]> FIELD_TYPE_TOKENCHAINS = Arrays.asList(
            new Object[]{FIELD_MODIFIERS_TOKENS, PHPTokenId.WHITESPACE, FIELD_MODIFIERS_TOKENS}, // readonly public, public readonly
            new Object[]{FIELD_MODIFIERS_TOKENS, PHPTokenId.WHITESPACE, FIELD_MODIFIERS_TOKENS, PHPTokenId.WHITESPACE},
            new Object[]{FIELD_MODIFIERS_TOKENS, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN},
            new Object[]{FIELD_MODIFIERS_TOKENS, PHPTokenId.WHITESPACE, PHPTokenId.PHP_TOKEN},
            new Object[]{FIELD_MODIFIERS_TOKENS, PHPTokenId.WHITESPACE, PHPTokenId.PHP_TOKEN, NAMESPACE_FALSE_TOKEN},
            new Object[]{FIELD_MODIFIERS_TOKENS, PHPTokenId.WHITESPACE, FIELD_UNION_OR_INTERSECTION_TYPE_TOKENS}
    );
    private static final List<Object[]> CLASS_CONTEXT_KEYWORDS_TOKENCHAINS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_PRIVATE},
            new Object[]{PHPTokenId.PHP_PRIVATE, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_PRIVATE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_PROTECTED},
            new Object[]{PHPTokenId.PHP_PROTECTED, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_PROTECTED, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_PUBLIC},
            new Object[]{PHPTokenId.PHP_PUBLIC, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_PUBLIC, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_STATIC},
            new Object[]{PHPTokenId.PHP_STATIC, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_STATIC, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_ABSTRACT},
            new Object[]{PHPTokenId.PHP_ABSTRACT, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_ABSTRACT, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_FINAL},
            new Object[]{PHPTokenId.PHP_FINAL, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_FINAL, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_READONLY},
            new Object[]{PHPTokenId.PHP_READONLY, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_READONLY, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_CURLY_OPEN},
            new Object[]{PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_CURLY_CLOSE},
            new Object[]{PHPTokenId.PHP_CURLY_CLOSE, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_CURLY_CLOSE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_CURLY_OPEN},
            new Object[]{PHPTokenId.PHP_CURLY_OPEN, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_CURLY_OPEN, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
            new Object[]{PHPTokenId.PHP_SEMICOLON},
            new Object[]{PHPTokenId.PHP_SEMICOLON, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_SEMICOLON, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING});
    private static final List<Object[]> CONST_TOKENCHAINS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_CONST, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE, CONST_STATEMENT_TOKENS},
            new Object[]{PHPTokenId.PHP_CONST, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, CONST_STATEMENT_TOKENS},
            new Object[]{PHPTokenId.PHP_CONST, PHPTokenId.WHITESPACE, CONST_DECLARED_TYPE_TOKENS, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE, CONST_STATEMENT_TOKENS},
            new Object[]{PHPTokenId.PHP_CONST, PHPTokenId.WHITESPACE, CONST_DECLARED_TYPE_TOKENS, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, CONST_STATEMENT_TOKENS}
    );
    private static final List<Object[]> CONST_TYPE_TOKENCHAINS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_CONST},
            new Object[]{PHPTokenId.PHP_CONST, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_CONST, PHPTokenId.WHITESPACE, NAMESPACE_FALSE_TOKEN},
            new Object[]{PHPTokenId.PHP_CONST, PHPTokenId.WHITESPACE, CONST_DECLARED_TYPE_TOKENS}
    );
    private static final List<Object[]> CONST_NAME_TOKENCHAINS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_CONST, PHPTokenId.WHITESPACE, CONST_DECLARED_TYPE_TOKENS, PHPTokenId.WHITESPACE},
            new Object[]{PHPTokenId.PHP_CONST, PHPTokenId.WHITESPACE, CONST_DECLARED_TYPE_TOKENS, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING}
    );
    private static final List<Object[]> ENUM_CASE_TOKENCHAINS = Arrays.asList(
            new Object[]{PHPTokenId.PHP_CASE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE, CONST_STATEMENT_TOKENS},
            new Object[]{PHPTokenId.PHP_CASE, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING, CONST_STATEMENT_TOKENS}
    );
    private static final List<Object[]> SERVER_ARRAY_TOKENCHAINS = Collections.singletonList(
            new Object[]{PHPTokenId.PHP_VARIABLE, PHPTokenId.PHP_TOKEN});
    private static final List<String> SERVER_ARRAY_TOKENTEXTS =
            Arrays.asList(new String[]{"$_SERVER", "["}); //NOI18N

    public static enum CompletionContext {

        EXPRESSION, GLOBAL_CONST_EXPRESSION, CLASS_CONST_EXPRESSION, MATCH_EXPRESSION, ENUM_CASE_EXPRESSION,
        HTML, CLASS_NAME, INTERFACE_NAME, BACKING_TYPE,
        TYPE_NAME, RETURN_TYPE_NAME, RETURN_UNION_OR_INTERSECTION_TYPE_NAME, FIELD_TYPE_NAME, CONST_TYPE_NAME, VISIBILITY_MODIFIER_OR_TYPE_NAME, STRING,
        CLASS_MEMBER, STATIC_CLASS_MEMBER, PHPDOC, INHERITANCE, EXTENDS, IMPLEMENTS, METHOD_NAME,
        CLASS_MEMBER_PARAMETER_NAME, STATIC_CLASS_MEMBER_PARAMETER_NAME, FUNCTION_PARAMETER_NAME, CONSTRUCTOR_PARAMETER_NAME,
        CLASS_CONTEXT_KEYWORDS, SERVER_ENTRY_CONSTANTS, NONE, NEW_CLASS, GLOBAL, NAMESPACE_KEYWORD,
        GROUP_USE_KEYWORD, GROUP_USE_CONST_KEYWORD, GROUP_USE_FUNCTION_KEYWORD,
        USE_KEYWORD, USE_CONST_KEYWORD, USE_FUNCTION_KEYWORD, DEFAULT_PARAMETER_VALUE, OPEN_TAG, THROW, THROW_NEW, CATCH, CLASS_MEMBER_IN_STRING,
        INTERFACE_CONTEXT_KEYWORDS, USE_TRAITS, ATTRIBUTE, ATTRIBUTE_EXPRESSION
    };

    static enum KeywordCompletionType {

        SIMPLE, CURSOR_INSIDE_BRACKETS, ENDS_WITH_CURLY_BRACKETS,
        ENDS_WITH_SPACE, ENDS_WITH_SEMICOLON, ENDS_WITH_COLON, ENDS_WITH_BRACKETS_AND_CURLY_BRACKETS,
        CURSOR_BEFORE_ENDING_SEMICOLON
    };
    static final Collection<PHPTokenId> CTX_DELIMITERS = Arrays.asList(
            PHPTokenId.PHP_SEMICOLON, PHPTokenId.PHP_CURLY_OPEN, PHPTokenId.PHP_CURLY_CLOSE,
            PHPTokenId.PHP_RETURN, PHPTokenId.PHP_OPERATOR, PHPTokenId.PHP_ECHO,
            PHPTokenId.PHP_EVAL, PHPTokenId.PHP_NEW, PHPTokenId.PHP_NOT, PHPTokenId.PHP_CASE,
            PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF, PHPTokenId.PHP_PRINT,
            PHPTokenId.PHP_FOR, PHPTokenId.PHP_FOREACH, PHPTokenId.PHP_WHILE,
            PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING, PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE,
            PHPTokenId.T_OPEN_TAG_WITH_ECHO, PHPTokenId.PHP_OPENTAG, PHPTokenId.PHP_CASTING);

    private CompletionContextFinder() {
    }

    @NonNull
    static CompletionContext findCompletionContext(ParserResult info, int caretOffset) {
        TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
        if (th == null) {
            return CompletionContext.NONE;
        }
        TokenSequence<PHPTokenId> tokenSequence = LexUtilities.getPHPTokenSequence(th, caretOffset);
        if (tokenSequence == null) {
            return CompletionContext.NONE;
        }
        tokenSequence.move(caretOffset);
        final boolean moveNextSucces = tokenSequence.moveNext();
        if (!moveNextSucces && !tokenSequence.movePrevious()) {
            return CompletionContext.NONE;
        }
        Token<PHPTokenId> token = tokenSequence.token();
        PHPTokenId tokenId = token.id();
        if (tokenId.equals(PHPTokenId.PHP_CLOSETAG) && (tokenSequence.offset() < caretOffset)) {
            return CompletionContext.NONE;
        }
        int tokenIdOffset = tokenSequence.token().offset(th);

        CompletionContext clsIfaceDeclContext = getClsIfaceDeclContext(token, (caretOffset - tokenIdOffset), tokenSequence);
        if (clsIfaceDeclContext != null) {
            return clsIfaceDeclContext;
        }
        if (acceptTokenChains(tokenSequence, THROW_TOKEN_CHAINS, moveNextSucces)) {
            return CompletionContext.THROW;
        } else if (acceptTokenChains(tokenSequence, THROW_NEW_TOKEN_CHAINS, moveNextSucces)) {
            return CompletionContext.THROW_NEW;
        } else if (acceptTokenChains(tokenSequence, CLASS_NAME_TOKENCHAINS, moveNextSucces)) {
            // has to be checked AFTER: THROW_NEW_TOKEN_CHAINS
            return CompletionContext.NEW_CLASS;
        } else if (acceptTokenChains(tokenSequence, CLASS_MEMBER_IN_STRING_TOKENCHAINS, moveNextSucces)) {
            return CompletionContext.CLASS_MEMBER_IN_STRING;
        } else if (acceptTokenChains(tokenSequence, CLASS_MEMBER_TOKENCHAINS, moveNextSucces)) {
            // has to be checked AFTER: CLASS_MEMBER_IN_STRING_TOKENCHAINS
            return CompletionContext.CLASS_MEMBER;
        } else if (acceptTokenChains(tokenSequence, STATIC_CLASS_MEMBER_TOKENCHAINS, moveNextSucces)) {
            return CompletionContext.STATIC_CLASS_MEMBER;
        } else if (tokenId == PHPTokenId.PHP_COMMENT) {
            return getCompletionContextInComment(tokenSequence, caretOffset, info);
        } else if (isPhpDocToken(tokenSequence)) {
            return CompletionContext.PHPDOC;
        } else if (acceptTokenChains(tokenSequence, CATCH_TOKENCHAINS, moveNextSucces)) {
            return CompletionContext.CATCH;
        } else if (acceptTokenChains(tokenSequence, NAMESPACE_KEYWORD_TOKENS, moveNextSucces)) {
            return CompletionContext.NAMESPACE_KEYWORD;
        } else if (acceptTokenChains(tokenSequence, INSTANCEOF_TOKENCHAINS, moveNextSucces)) {
            return CompletionContext.TYPE_NAME;
        } else if (acceptTokenChains(tokenSequence, GROUP_USE_KEYWORD_TOKENS, moveNextSucces)) {
            // #262143 - check previous token for 'const' or 'function'
            while (tokenSequence.movePrevious()) {
                tokenId = tokenSequence.token().id();
                if (tokenId == PHPTokenId.WHITESPACE) {
                    continue;
                } else if (tokenId == PHPTokenId.PHP_CONST) {
                    return CompletionContext.GROUP_USE_CONST_KEYWORD;
                } else if (tokenId == PHPTokenId.PHP_FUNCTION) {
                    return CompletionContext.GROUP_USE_FUNCTION_KEYWORD;
                }
                break;
            }
            return CompletionContext.GROUP_USE_KEYWORD;
        } else if (acceptTokenChains(tokenSequence, GROUP_USE_CONST_KEYWORD_TOKENS, moveNextSucces)) {
            return CompletionContext.GROUP_USE_CONST_KEYWORD;
        } else if (acceptTokenChains(tokenSequence, GROUP_USE_FUNCTION_KEYWORD_TOKENS, moveNextSucces)) {
            return CompletionContext.GROUP_USE_FUNCTION_KEYWORD;
        } else if (isInAttribute(caretOffset, tokenSequence, true)) {
            if (isInAttribute(caretOffset, tokenSequence, false)) {
                return CompletionContext.ATTRIBUTE;
            }
            CompletionContext namedArgumentsContext = getNamedArgumentsContext(caretOffset, tokenSequence);
            if (namedArgumentsContext != null) {
                return namedArgumentsContext;
            }
            return CompletionContext.ATTRIBUTE_EXPRESSION;
        } else if (isInsideInterfaceDeclarationBlock(info, caretOffset, tokenSequence)) {
            CompletionContext paramContext = getParamaterContext(token, caretOffset, tokenSequence);
            if (paramContext != null) {
                return paramContext;
            }
            if (acceptTokenChains(tokenSequence, CONST_TYPE_TOKENCHAINS, moveNextSucces)) {
                return CompletionContext.CONST_TYPE_NAME;
            } else if (acceptTokenChains(tokenSequence, CONST_NAME_TOKENCHAINS, moveNextSucces)) {
                return CompletionContext.NONE;
            } else if (acceptTokenChains(tokenSequence, CONST_TOKENCHAINS, moveNextSucces)) {
                return CompletionContext.CLASS_CONST_EXPRESSION;
            }
            return CompletionContext.INTERFACE_CONTEXT_KEYWORDS;
        } else if (isInsideClassOrTraitOrEnumDeclarationBlock(info, caretOffset, tokenSequence)) {
            if (acceptTokenChains(tokenSequence, USE_KEYWORD_TOKENS, moveNextSucces)) {
                return CompletionContext.USE_TRAITS;
            } else if (acceptTokenChains(tokenSequence, METHOD_NAME_TOKENCHAINS, moveNextSucces)) {
                return CompletionContext.METHOD_NAME;
            } else {
                CompletionContext paramContext = getParamaterContext(token, caretOffset, tokenSequence);
                if (paramContext != null) {
                    return paramContext;
                } else if (acceptTokenChains(tokenSequence, FIELD_TYPE_TOKENCHAINS, moveNextSucces)) {
                    // \Namespace\ClassName, ?, ?ClassName, ?\Namespace\ClassName etc.
                    return CompletionContext.FIELD_TYPE_NAME;
                } else if (acceptTokenChains(tokenSequence, CONST_TYPE_TOKENCHAINS, moveNextSucces)) {
                    return CompletionContext.CONST_TYPE_NAME;
                } else if (acceptTokenChains(tokenSequence, CONST_NAME_TOKENCHAINS, moveNextSucces)) {
                    return CompletionContext.NONE;
                } else if (acceptTokenChains(tokenSequence, CONST_TOKENCHAINS, moveNextSucces)) {
                    return CompletionContext.CLASS_CONST_EXPRESSION;
                } else if (acceptTokenChains(tokenSequence, ENUM_CASE_TOKENCHAINS, moveNextSucces)) {
                    return CompletionContext.ENUM_CASE_EXPRESSION;
                } else if (acceptTokenChains(tokenSequence, CLASS_CONTEXT_KEYWORDS_TOKENCHAINS, moveNextSucces)) {
                    return CompletionContext.CLASS_CONTEXT_KEYWORDS;
                }
                return CompletionContext.NONE;
            }
        } else if (acceptTokenChains(tokenSequence, USE_KEYWORD_TOKENS, moveNextSucces)) {
            return CompletionContext.USE_KEYWORD;
        } else if (acceptTokenChains(tokenSequence, USE_CONST_KEYWORD_TOKENS, moveNextSucces)) {
            return CompletionContext.USE_CONST_KEYWORD;
        } else if (acceptTokenChains(tokenSequence, USE_FUNCTION_KEYWORD_TOKENS, moveNextSucces)) {
            return CompletionContext.USE_FUNCTION_KEYWORD;
        } else if (acceptTokenChains(tokenSequence, FUNCTION_NAME_TOKENCHAINS, moveNextSucces)) {
            return CompletionContext.NONE;
        } else if (isCommonCommentToken(tokenSequence)) {
            return CompletionContext.NONE;
        }

        switch (tokenId) {
            case T_INLINE_HTML:
                return CompletionContext.HTML;
            case PHP_CONSTANT_ENCAPSED_STRING:
                char encChar = tokenSequence.token().text().charAt(0);
                if (encChar == '"') { //NOI18N
                    if (acceptTokenChains(tokenSequence, SERVER_ARRAY_TOKENCHAINS, moveNextSucces)
                            && acceptTokenChainTexts(tokenSequence, SERVER_ARRAY_TOKENTEXTS)) {
                        return CompletionContext.SERVER_ENTRY_CONSTANTS;
                    }
                    return CompletionContext.STRING;
                } else if (encChar == '\'') { //NOI18N
                    if (acceptTokenChains(tokenSequence, SERVER_ARRAY_TOKENCHAINS, moveNextSucces)
                            && acceptTokenChainTexts(tokenSequence, SERVER_ARRAY_TOKENTEXTS)) {
                        return CompletionContext.SERVER_ENTRY_CONSTANTS;
                    }
                }
                return CompletionContext.NONE;
            default:
        }
        if (isEachOfTokens(getLeftPreceedingTokens(tokenSequence), new PHPTokenId[]{PHPTokenId.PHP_GLOBAL, PHPTokenId.WHITESPACE})
                || (isWhiteSpace(token) && isEachOfTokens(getLeftPreceedingTokens(tokenSequence), new PHPTokenId[]{PHPTokenId.PHP_GLOBAL}))) {
            return CompletionContext.GLOBAL;
        }

        CompletionContext paramContext = getParamaterContext(token, caretOffset, tokenSequence);
        if (paramContext != null) {
            return paramContext;
        }
        CompletionContext namedArgumentsContext = getNamedArgumentsContext(caretOffset, tokenSequence);
        if (namedArgumentsContext != null) {
            return namedArgumentsContext;
        }

        if (tokenSequence.movePrevious() && tokenSequence.token().id() == PHPTokenId.PHP_OPENTAG
                && TokenUtilities.textEquals("<?", tokenSequence.token().text()) && (tokenSequence.offset() + 2) == caretOffset) { // NOI18N
            return CompletionContext.OPEN_TAG;
        }
        if (acceptTokenChains(tokenSequence, CONST_TOKENCHAINS, moveNextSucces)) {
            return CompletionContext.GLOBAL_CONST_EXPRESSION;
        }
        if (isInMatchExpression(caretOffset, tokenSequence)) {
            return CompletionContext.MATCH_EXPRESSION;
        }
        return CompletionContext.EXPRESSION;
    }

    private static boolean isPhpDocToken(TokenSequence tokenSequence) {
        return isOneOfTokens(tokenSequence, PHPDOC_TOKENS);
    }

    private static boolean isCommonCommentToken(TokenSequence tokenSequence) {
        return isOneOfTokens(tokenSequence, COMMENT_TOKENS);
    }

    private static boolean isCommentToken(TokenSequence tokenSequence) {
        return isCommonCommentToken(tokenSequence) || isPhpDocToken(tokenSequence);
    }

    private static boolean isOneOfTokens(TokenSequence tokenSequence, PHPTokenId[] tokenIds) {
        TokenId searchedId = tokenSequence.token().id();

        for (TokenId tokenId : tokenIds) {
            if (tokenId.equals(searchedId)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isEachOfTokens(Token[] tokens, PHPTokenId[] tokenIds) {
        Set<PHPTokenId> set = EnumSet.noneOf(PHPTokenId.class);
        for (Token token : tokens) {
            TokenId searchedId = token.id();
            for (PHPTokenId tokenId : tokenIds) {
                if (tokenId.equals(searchedId)) {
                    set.add(tokenId);
                }
            }
        }
        return set.size() == tokenIds.length;
    }

    private static boolean acceptTokenChainTexts(TokenSequence tokenSequence, List<String> tokenTexts) {
        int orgTokenSequencePos = tokenSequence.offset();
        boolean accept = true;
        boolean moreTokens = tokenSequence.movePrevious();

        for (int i = tokenTexts.size() - 1; i >= 0; i--) {
            String tokenTxt = tokenTexts.get(i);

            if (!moreTokens) {
                accept = false;
                break;
            }

            if (TokenUtilities.textEquals(tokenTxt, tokenSequence.token().text())) {
                moreTokens = tokenSequence.movePrevious();
            } else {
                // NO MATCH
                accept = false;
                break;
            }
        }

        tokenSequence.move(orgTokenSequencePos);
        tokenSequence.moveNext();
        return accept;
    }

    private static boolean acceptTokenChains(TokenSequence tokenSequence, List<Object[]> tokenIdChains, boolean movePrevious) {
        for (Object[] tokenIDChain : tokenIdChains) {
            if (acceptTokenChain(tokenSequence, tokenIDChain, movePrevious)) {
                return true;
            }
        }

        return false;
    }

    private static boolean acceptTokenChain(TokenSequence tokenSequence, Object[] tokenIdChain, boolean movePrevious) {
        int orgTokenSequencePos = tokenSequence.offset();
        boolean accept = true;
        boolean moreTokens = movePrevious ? tokenSequence.movePrevious() : true;
        boolean lastTokenWasComment = false;
        for (int i = tokenIdChain.length - 1; i >= 0; i--) {
            Object tokenID = tokenIdChain[i];

            if (!moreTokens) {
                accept = false;
                break;
            }

            if (tokenID instanceof PHPTokenId) {
                if (isCommentToken(tokenSequence)) {
                    i++;
                    moreTokens = tokenSequence.movePrevious();
                    lastTokenWasComment = true;
                    continue;
                } else if (tokenSequence.token().id() == PHPTokenId.WHITESPACE && lastTokenWasComment) {
                    i++;
                    moreTokens = tokenSequence.movePrevious();
                    lastTokenWasComment = false;
                    continue;
                } else {
                    lastTokenWasComment = false;
                }
                if (tokenSequence.token().id() == tokenID) {
                    moreTokens = tokenSequence.movePrevious();
                } else {
                    // NO MATCH
                    accept = false;
                    break;
                }
            } else if (tokenID == NAMESPACE_FALSE_TOKEN) {
                if (!consumeNameSpace(tokenSequence)) {
                    accept = false;
                    break;
                }
            } else if (tokenID == GROUP_USE_STATEMENT_TOKENS) {
                if (!consumeClassesConstFunctionInGroupUse(tokenSequence)) {
                    accept = false;
                    break;
                }
            } else if (tokenID == MULTI_CATCH_EXCEPTION_TOKENS) {
                if (!consumeMultiCatchExceptions(tokenSequence)) {
                    accept = false;
                    break;
                }
            } else if (tokenID == FIELD_MODIFIERS_TOKENS) {
                if (!isFieldModifier(tokenSequence.token())) {
                    accept = false;
                    break;
                }
                moreTokens = tokenSequence.movePrevious();
            } else if (tokenID == FIELD_UNION_OR_INTERSECTION_TYPE_TOKENS) {
                if (!consumeFieldDeclaredTypes(tokenSequence)) {
                    accept = false;
                    break;
                }
            } else if (tokenID == COMBINED_USE_STATEMENT_TOKENS) {
                if (!consumeClassesInCombinedUse(tokenSequence)) {
                    accept = false;
                    break;
                }
            } else if (tokenID == TYPE_KEYWORD) {
                if (!consumeUntilTypeKeyword(tokenSequence)) {
                    accept = false;
                    break;
                }
            } else if (tokenID == CONST_STATEMENT_TOKENS) {
                if (!consumeUntilConstEqual(tokenSequence)) {
                    accept = false;
                    break;
                }
            } else if (tokenID == CONST_DECLARED_TYPE_TOKENS) {
                if (!consumeConstDeclaredTypes(tokenSequence)) {
                    accept = false;
                    break;
                }
            } else if (tokenID == ENUM_CASE_STATEMENT_TOKENS) {
                if (!consumeUntilEnumCaseEqual(tokenSequence)) {
                    accept = false;
                    break;
                }
            } else if (tokenID == OBJECT_OPERATOR_TOKEN) {
                if (!consumeObjectOperator(tokenSequence)) {
                    accept = false;
                    break;
                }
                moreTokens = tokenSequence.movePrevious();
            } else {
                assert false : "Unsupported token type: " + tokenID.getClass().getName();
            }
        }

        tokenSequence.move(orgTokenSequencePos);
        tokenSequence.moveNext();
        return accept;
    }

    // XXX consume aliases!
    private static boolean consumeNameSpace(TokenSequence tokenSequence) {
        boolean hadNSSeparator = false;
        if (tokenSequence.token().id() != PHPTokenId.PHP_NS_SEPARATOR
                && tokenSequence.token().id() != PHPTokenId.PHP_STRING) {
            return false;
        }

        do {

            if (tokenSequence.token().id() == PHPTokenId.PHP_NS_SEPARATOR
                    || tokenSequence.token().id() == PHPTokenId.PHP_STRING) {
                hadNSSeparator = true;
            }

            if (!tokenSequence.movePrevious()) {
                return false;
            }

        } while (tokenSequence.token().id() == PHPTokenId.PHP_NS_SEPARATOR
                || tokenSequence.token().id() == PHPTokenId.PHP_STRING);

        return hadNSSeparator;
    }

    private static boolean consumeComment(TokenSequence tokenSequence) {
        while (tokenSequence.token().id() == PHPTokenId.PHP_COMMENT_START
                || tokenSequence.token().id() == PHPTokenId.PHP_COMMENT_END
                || tokenSequence.token().id() == PHPTokenId.PHP_COMMENT) {
            if (!tokenSequence.movePrevious()) {
                return false;
            }
        }
        return true;
    }

    private static boolean consumeClassesConstFunctionInGroupUse(TokenSequence tokenSequence) {
        if (tokenSequence.token().id() != PHPTokenId.PHP_CURLY_OPEN
                && tokenSequence.token().id() != PHPTokenId.PHP_TOKEN
                && tokenSequence.token().id() != PHPTokenId.PHP_CONST
                && tokenSequence.token().id() != PHPTokenId.PHP_FUNCTION
                && tokenSequence.token().id() != PHPTokenId.WHITESPACE
                && !consumeNameSpace(tokenSequence)) {
            return false;
        }

        boolean hasCurlyOpen = false;
        do {
            // NETBEANS-5849
            if (tokenSequence.token().id() == PHPTokenId.PHP_USE) {
                return false;
            }
            if (tokenSequence.token().id() == PHPTokenId.PHP_CURLY_OPEN) {
                hasCurlyOpen = true;
            }

            if (!tokenSequence.movePrevious()) {
                return false;
            }

            if (hasCurlyOpen) {
                break;
            }

        } while (tokenSequence.token().id() == PHPTokenId.PHP_CURLY_OPEN
                || tokenSequence.token().id() == PHPTokenId.PHP_TOKEN
                || tokenSequence.token().id() == PHPTokenId.PHP_CONST
                || tokenSequence.token().id() == PHPTokenId.PHP_FUNCTION
                || tokenSequence.token().id() == PHPTokenId.WHITESPACE
                || consumeNameSpace(tokenSequence)
                || consumeComment(tokenSequence));

        return hasCurlyOpen;
    }

    private static boolean consumeFieldDeclaredTypes(TokenSequence tokenSequence) {
        if (!isTypeSeparator(tokenSequence.token())
                && tokenSequence.token().id() != PHPTokenId.WHITESPACE
                && tokenSequence.token().id() != PHPTokenId.PHP_STRING
                && !isType(tokenSequence.token())
                && !consumeNameSpace(tokenSequence)) {
            return false;
        }
        boolean isFieldType = false;
        boolean hasTypeSeparator = false;
        do {
            if (isTypeSeparator(tokenSequence.token())) {
                hasTypeSeparator = true;
            }
            if (!tokenSequence.movePrevious()) {
                return false;
            }
        } while (isTypeSeparator(tokenSequence.token())
                || tokenSequence.token().id() == PHPTokenId.WHITESPACE
                || tokenSequence.token().id() == PHPTokenId.PHP_STRING
                || isType(tokenSequence.token())
                || consumeNameSpace(tokenSequence));
        if (hasTypeSeparator && isFieldModifier(tokenSequence.token())) {
            tokenSequence.moveNext();
            isFieldType = true;
        }
        return isFieldType;
    }

    private static boolean consumeConstDeclaredTypes(TokenSequence tokenSequence) {
        if (!isTypeSeparator(tokenSequence.token()) // |&()
                && tokenSequence.token().id() != PHPTokenId.WHITESPACE
                && tokenSequence.token().id() != PHPTokenId.PHP_STRING
                && !isType(tokenSequence.token())
                && !isNullableTypesPrefix(tokenSequence.token())
                && !consumeNameSpace(tokenSequence)) {
            return false;
        }
        boolean isConstType = false;
        TokenId lastTokenId = null;
        Token lastTokenExceptForWS = null;
        do {
            if (lastTokenId == PHPTokenId.WHITESPACE) {
                if (!isTypeSeparator(tokenSequence.token())
                        || (isRightParen(tokenSequence.token()) && !isVerticalBar(lastTokenExceptForWS))) {
                    // check the following case: const string CONST_NAME
                    //                                       ^
                    isConstType = false;
                    break;
                }
            }
            lastTokenId = tokenSequence.token().id();
            if (lastTokenId != PHPTokenId.WHITESPACE) {
                lastTokenExceptForWS = tokenSequence.token();
            }
            if (!tokenSequence.movePrevious()) {
                return false;
            }
        } while (isTypeSeparator(tokenSequence.token()) // |&()
                || tokenSequence.token().id() == PHPTokenId.WHITESPACE
                || tokenSequence.token().id() == PHPTokenId.PHP_STRING
                || isType(tokenSequence.token())
                || isNullableTypesPrefix(tokenSequence.token())
                || consumeNameSpace(tokenSequence));
        if (tokenSequence.token().id() == PHPTokenId.PHP_CONST) {
            tokenSequence.moveNext();
            isConstType = true;
        }
        return isConstType;
    }

    private static boolean isTypeSeparator(Token<PHPTokenId> token) {
        return isVerticalBar(token)
                || isReference(token)
                || isLeftParen(token)
                || isRightParen(token);
    }

    private static boolean consumeMultiCatchExceptions(TokenSequence tokenSequence) {
        if (tokenSequence.token().id() != PHPTokenId.PHP_OPERATOR
                && tokenSequence.token().id() != PHPTokenId.PHP_TOKEN
                && tokenSequence.token().id() != PHPTokenId.WHITESPACE
                && !consumeNameSpace(tokenSequence)) {
            return false;
        }
        boolean hasParenOpen = false;
        boolean first = true;
        do {
            if (first) {
                first = false;
                if (tokenSequence.token().id() == PHPTokenId.WHITESPACE) {
                    if (!tokenSequence.movePrevious()) {
                        return false;
                    }
                    if (consumeNameSpace(tokenSequence)) {
                        return false;
                    }
                }
            }
            if (isLeftParen(tokenSequence.token())) {
                hasParenOpen = true;
            }
            if (!tokenSequence.movePrevious()) {
                return false;
            }
            if (hasParenOpen) {
                break;
            }
        } while (isVerticalBar(tokenSequence.token())
                || isLeftParen(tokenSequence.token())
                || tokenSequence.token().id() == PHPTokenId.WHITESPACE
                || consumeNameSpace(tokenSequence));

        return hasParenOpen;
    }

    private static boolean consumeClassesInCombinedUse(TokenSequence tokenSequence) {
        boolean hasCommaDelimiter = false;
        if (tokenSequence.token().id() != PHPTokenId.PHP_TOKEN
                && tokenSequence.token().id() != PHPTokenId.WHITESPACE
                && !consumeNameSpace(tokenSequence)) {
            return false;
        }

        do {

            if (tokenSequence.token().id() == PHPTokenId.PHP_TOKEN) {
                hasCommaDelimiter = true;
            }

            if (!tokenSequence.movePrevious()) {
                return false;
            }

        } while (tokenSequence.token().id() == PHPTokenId.PHP_TOKEN
                || tokenSequence.token().id() == PHPTokenId.WHITESPACE
                || consumeNameSpace(tokenSequence));

        return hasCommaDelimiter;
    }

    private static boolean consumeUntilTypeKeyword(TokenSequence tokenSequence) {
        boolean result = false;
        do {
            if (tokenSequence.token().id() == PHPTokenId.PHP_CLASS || tokenSequence.token().id() == PHPTokenId.PHP_INTERFACE
                    || tokenSequence.token().id() == PHPTokenId.PHP_TRAIT || tokenSequence.token().id() == PHPTokenId.PHP_EXTENDS
                    || tokenSequence.token().id() == PHPTokenId.PHP_IMPLEMENTS) {
                result = true;
                break;
            }
            if (tokenSequence.token().id() == PHPTokenId.PHP_NAMESPACE) {
                result = false;
                break;
            }
        } while(tokenSequence.movePrevious());
        return result;
    }

    private static boolean consumeUntilConstEqual(TokenSequence tokenSequence) {
        boolean hasEqual = false;
        do {
            if (tokenSequence.token().id() == PHPTokenId.PHP_CONST
                    || tokenSequence.token().id() == PHPTokenId.PHP_SEMICOLON) {
                break;
            }
            if (isEqualSign(tokenSequence.token())) {
                hasEqual = true;
                tokenSequence.movePrevious();
                break;
            }
        } while (tokenSequence.movePrevious());

        return hasEqual;
    }

    private static boolean consumeUntilEnumCaseEqual(TokenSequence tokenSequence) {
        boolean hasEqual = false;
        do {
            if (tokenSequence.token().id() == PHPTokenId.PHP_CASE
                    || tokenSequence.token().id() == PHPTokenId.PHP_SEMICOLON) {
                break;
            }
            if (isEqualSign(tokenSequence.token())) {
                hasEqual = true;
                tokenSequence.movePrevious();
                break;
            }
        } while (tokenSequence.movePrevious());

        return hasEqual;
    }

    private static boolean consumeObjectOperator(TokenSequence tokenSequence) {
        boolean result = false;
        do {
            if (isObjectOperatorToken(tokenSequence.token())) {
                result = true;
                break;
            }
            if (!isCommentToken(tokenSequence) && !isWhiteSpace(tokenSequence.token())) {
                break;
            }
        } while (tokenSequence.movePrevious());
        return result;
    }

    private static Token[] getLeftPreceedingTokens(TokenSequence tokenSequence) {
        Token[] preceedingTokens = getPreceedingTokens(tokenSequence);
        if (preceedingTokens.length == 0) {
            return preceedingTokens;
        }
        Token[] leftPreceedingTokens = new Token[preceedingTokens.length - 1];
        System.arraycopy(preceedingTokens, 1, leftPreceedingTokens, 0, leftPreceedingTokens.length);
        return leftPreceedingTokens;
    }

    private static Token[] getPreceedingTokens(TokenSequence tokenSequence) {
        int orgOffset = tokenSequence.offset();
        LinkedList<Token> tokens = new LinkedList<>();

        boolean success = true;

        // in case we are at the last token
        // include it in the result, see #154055
        if (tokenSequence.moveNext()) {
            success = tokenSequence.movePrevious()
                    && tokenSequence.movePrevious();
        }

        if (success) {
            Token<PHPTokenId> token = tokenSequence.token();
            while (token != null && !CTX_DELIMITERS.contains(token.id())) {
                tokens.addFirst(token);
                if (!tokenSequence.movePrevious()) {
                    break;
                } else {
                    token = tokenSequence.token();
                }
            }
        }

        tokenSequence.move(orgOffset);
        tokenSequence.moveNext();
        return tokens.toArray(new Token[0]);
    }

    @CheckForNull
    private static CompletionContext getClsIfaceDeclContext(Token<PHPTokenId> token, int tokenOffset, TokenSequence<PHPTokenId> tokenSequence) {
        boolean isNew = false;
        boolean isClass = false;
        boolean isTrait = false;
        boolean isEnum = false;
        int openParenthesis = 0;
        boolean isIface = false;
        boolean isExtends = false;
        boolean isImplements = false;
        boolean isNsSeparator = false;
        boolean isString = false;
        boolean isBackingType =false;
        Token<PHPTokenId> stringToken = null;
        boolean nokeywords;
        List<? extends Token<PHPTokenId>> preceedingLineTokens = getPreceedingLineTokens(token, tokenOffset, tokenSequence);
        for (int i = 0; i < preceedingLineTokens.size(); i++) {
            Token<PHPTokenId> cToken = preceedingLineTokens.get(i);
            TokenId id = cToken.id();
            nokeywords = !isIface && !isClass && !isTrait && !isEnum && !isExtends && !isImplements && !isNsSeparator && !isBackingType;
            if (id.equals(PHPTokenId.PHP_TOKEN)
                    && TokenUtilities.textEquals(cToken.text(), ")")) { // NOI18N
                openParenthesis--;
            } else if (id.equals(PHPTokenId.PHP_TOKEN)
                    && TokenUtilities.textEquals(cToken.text(), "(")) { // NOI18N
                openParenthesis++;
            } else if (id.equals(PHPTokenId.PHP_CLASS)) {
                isClass = true;
                // anonymous class?
                int j = i + 1;
                while (j < preceedingLineTokens.size()) {
                    Token<PHPTokenId> tkn = preceedingLineTokens.get(j);
                    ++j;
                    if (tkn.id() == PHPTokenId.WHITESPACE) {
                        continue;
                    }
                    isNew = tkn.id() == PHPTokenId.PHP_NEW;
                    break;
                }
                if (isNew
                        && openParenthesis > 0) {
                    // anonymous class arguments (including e.g. 'new class(foo(|))')
                    return null;
                }
                break;
            } else if (id.equals(PHPTokenId.PHP_TRAIT)) {
                isTrait = true;
                break;
            } else if (id.equals(PHPTokenId.PHP_ENUM)) {
                isEnum = true;
                break;
            } else if (id.equals(PHPTokenId.PHP_INTERFACE)) {
                isIface = true;
                break;
            } else if (id.equals(PHPTokenId.PHP_EXTENDS)) {
                isExtends = true;
            } else if (id.equals(PHPTokenId.PHP_IMPLEMENTS)) {
                isImplements = true;
            } else if (id.equals(PHPTokenId.PHP_NS_SEPARATOR)) {
                isNsSeparator = true;
            } else if (isReturnTypeSeparator(cToken)) {
                isBackingType = true;
            } else if (nokeywords && id.equals(PHPTokenId.PHP_STRING)) {
                isString = true;
                stringToken = cToken;
            } else {
                if (nokeywords && id.equals(PHPTokenId.PHP_CURLY_OPEN)) {
                    return null;
                }
            }
        }
        if (isClass || isIface || isTrait || isEnum) {
            if (isImplements) {
                return CompletionContext.INTERFACE_NAME;
            } else if (isBackingType) {
                if (isString) {
                   return CompletionContext.IMPLEMENTS;
                }
                return CompletionContext.BACKING_TYPE;
            } else if (isExtends) {
                if (isString && isClass && stringToken != null && tokenOffset == 0
                        && preceedingLineTokens.size() > 0 && preceedingLineTokens.get(0).text().equals(stringToken.text())) {
                    return CompletionContext.CLASS_NAME;
                } else if (isString && isClass) {
                    return CompletionContext.IMPLEMENTS;
                } else if (!isString && isClass) {
                    return CompletionContext.CLASS_NAME;
                } else if (isIface) {
                    return CompletionContext.INTERFACE_NAME;
                }
                return !isString
                        ? isClass ? CompletionContext.CLASS_NAME : CompletionContext.INTERFACE_NAME
                        : isClass ? CompletionContext.IMPLEMENTS : CompletionContext.INTERFACE_NAME;
            } else if (isIface) {
                return !isString ? CompletionContext.NONE : CompletionContext.EXTENDS;
            } else if (isEnum) {
                return !isString ? CompletionContext.NONE : CompletionContext.IMPLEMENTS;
            } else if (isClass) {
                if (isString
                        || isNew) {
                    return CompletionContext.INHERITANCE;
                }
                return CompletionContext.NONE;
            }
        } else if (isExtends || isImplements) {
            boolean firstString = false;
            for (Token<PHPTokenId> cToken : preceedingLineTokens) {
                TokenId id = cToken.id();
                if (id == PHPTokenId.PHP_EXTENDS) {
                    return CompletionContext.CLASS_NAME;
                }
                if (id == PHPTokenId.PHP_IMPLEMENTS) {
                    return CompletionContext.INTERFACE_NAME;
                }
                if (id == PHPTokenId.PHP_STRING) {
                    if (!firstString) {
                        firstString = true;
                    } else {
                        break;
                    }
                } else if (id != PHPTokenId.WHITESPACE) {
                    break;
                }

            }
        }
        return null;
    }

    @CheckForNull
    private static CompletionContext getParamaterContext(Token<PHPTokenId> token, int carretOffset, TokenSequence<PHPTokenId> tokenSequence) {
        boolean isFunctionDeclaration = false;
        boolean isCompletionSeparator = false;
        CompletionContext contextForSeparator = null;
        boolean isNamespaceSeparator = false;
        boolean testCompletionSeparator = true;
        boolean checkReturnTypeSeparator = false;
        boolean isUnionOrIntersectionType = false;
        boolean isInConstructor = false;
        int orgOffset = tokenSequence.offset();
        tokenSequence.moveNext();
        boolean first = true;
        while (tokenSequence.movePrevious()) {
            Token<PHPTokenId> cToken = tokenSequence.token();
            PHPTokenId id = cToken.id();
            if (first) {
                first = false;
                if (PHPTokenId.PHP_SEMICOLON.equals(id)
                        || PHPTokenId.PHP_CURLY_OPEN.equals(id)) {
                    // return type right before ";" or "{":
                    continue;
                }
                if (isComma(token)) {
                    // e.g. $param = ^,
                    continue;
                }
            }
            if (isConstructor(cToken)) {
                isInConstructor = true;
            }
            if (CTX_DELIMITERS.contains(id)) {
                // check reference character (&) [unfortunately, cannot distinguish & as a operator and as a reference mark]
                // check "..." (is it really operator?)
                if (!isReference(cToken)
                        && !isNew(cToken)
                        && !isVariadic(cToken)
                        && !isInitilizerToken(cToken) // ($param = '')
                        && !isVerticalBar(cToken) // int|false
                        && !isOrOperator(cToken) // || (int|^|float)
                        && !isAndOperator(cToken) // && (Foo&^&Bar)
                        ) {
                    break;
                }
            }
            if (!isFunctionDeclaration) {
                if (!isCompletionSeparator && testCompletionSeparator) {
                    if (isEqualSign(cToken)) {
                        isCompletionSeparator = true;
                        contextForSeparator = CompletionContext.DEFAULT_PARAMETER_VALUE;
                    } else if (isParamSeparator(cToken)) {
                        isCompletionSeparator = true;
                        contextForSeparator = CompletionContext.VISIBILITY_MODIFIER_OR_TYPE_NAME;
                    } else if (isArray(token)
                            || isCallable(token)
                            || isIterable(token)
                            || isNullableTypesPrefix(cToken)
                            || isVerticalBar(cToken)
                            || isReference(cToken)
                            || isOrOperator(cToken)
                            || isAndOperator(cToken)
                            || isVisibilityModifier(cToken)
                            || isReadonlyModifier(cToken)) {
                        if (isReference(cToken)) {
                            int origOffset = tokenSequence.offset();
                            try {
                                // e.g. function &my_sort5(&^$data) {, function &my_sort5(^&$data) {
                                Token<? extends PHPTokenId> previous = LexUtilities.findPrevious(tokenSequence, Arrays.asList(PHPTokenId.WHITESPACE, PHPTokenId.PHP_OPERATOR));
                                if (isComma(previous) || isLeftParen(previous)) {
                                    int offset = cToken.offset(null) + cToken.text().length();
                                    if (carretOffset >= offset) {
                                        testCompletionSeparator = false;
                                    }
                                    continue;
                                }
                            } finally {
                                tokenSequence.move(origOffset);
                                tokenSequence.moveNext();
                            }
                        }
                        isCompletionSeparator = true;
                        if (isVerticalBar(cToken) || isOrOperator(cToken) || isReference(cToken) || isAndOperator(cToken)) {
                            isUnionOrIntersectionType = true;
                        }
                        if (isVisibilityModifier(token)
                                || isVisibilityModifier(cToken)
                                || isReadonlyModifier(token)
                                || isReadonlyModifier(cToken)) {
                            contextForSeparator = CompletionContext.VISIBILITY_MODIFIER_OR_TYPE_NAME;
                        } else {
                            contextForSeparator = CompletionContext.TYPE_NAME;
                        }
                        checkReturnTypeSeparator = true;
                    } else if (isReturnTypeSeparator(cToken)) {
                        isCompletionSeparator = true;
                        contextForSeparator = CompletionContext.RETURN_TYPE_NAME;
                    } else if (isAcceptedPrefix(cToken)) {
                        if (isNamespaceSeparator(cToken)) {
                            isNamespaceSeparator = true;
                            continue;
                        } else if (!isNamespaceSeparator && isString(cToken)) {
                            int offset = cToken.offset(null) + cToken.text().length();
                            if (carretOffset > offset) {
                                testCompletionSeparator = false;
                            }
                        } else if (isReference(cToken) || isRightParen(cToken) || isVariable(cToken)) {
                            int offset = cToken.offset(null) + cToken.text().length();
                            if (carretOffset >= offset) {
                                testCompletionSeparator = false;
                            }
                        }
                        isNamespaceSeparator = false;
                        continue;
                    } else if (!isCommentToken(tokenSequence) && !isNew(cToken)) {
                        testCompletionSeparator = false;
                    }
                } else if (checkReturnTypeSeparator) {
                    if (!isReturnTypeToken(cToken)) {
                        checkReturnTypeSeparator = false;
                    } else if (isVerticalBar(cToken) || isReference(cToken)) {
                        isUnionOrIntersectionType = true;
                    }
                    if (isReturnTypeSeparator(cToken)) {
                        contextForSeparator = isUnionOrIntersectionType
                                ? CompletionContext.RETURN_UNION_OR_INTERSECTION_TYPE_NAME
                                : CompletionContext.RETURN_TYPE_NAME;
                    }
                } else if (isFunctionDeclaration(cToken)) {
                    isFunctionDeclaration = true;
                    if (!isInConstructor
                            && contextForSeparator == CompletionContext.VISIBILITY_MODIFIER_OR_TYPE_NAME) {
                        contextForSeparator = CompletionContext.TYPE_NAME;
                    }
                    break;
                }
            }
        }
        tokenSequence.move(orgOffset);
        tokenSequence.moveNext();
        return (isFunctionDeclaration && isCompletionSeparator) ? contextForSeparator
                : isFunctionDeclaration ? CompletionContext.NONE : null;
    }

    private static boolean isNamespaceSeparator(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_NS_SEPARATOR);
    }

    private static boolean isFunctionDeclaration(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_FUNCTION)
                || token.id().equals(PHPTokenId.PHP_FN);
    }

    private static boolean isVariable(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_VARIABLE);
    }

    private static boolean isNew(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_NEW);
    }

    private static boolean isReference(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_OPERATOR)
                && TokenUtilities.textEquals(token.text(), Type.SEPARATOR_INTERSECTION);
    }

    private static boolean isVariadic(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_OPERATOR)
                && TokenUtilities.textEquals(token.text(), "..."); // NOI18N
    }

    static boolean isLeftParen(Token<? extends PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN)
                && TokenUtilities.textEquals(token.text(), "("); // NOI18N
    }

    private static boolean isRightParen(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN)
                && TokenUtilities.textEquals(token.text(), ")"); // NOI18N
    }

    private static boolean isLeftBracket(Token<? extends PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN)
                && TokenUtilities.textEquals(token.text(), "["); // NOI18N
    }

    private static boolean isRightBracket(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN)
                && TokenUtilities.textEquals(token.text(), "]"); // NOI18N
    }

    private static boolean isEqualSign(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_OPERATOR)
                && TokenUtilities.textEquals(token.text(), "="); // NOI18N
    }

    private static boolean isParamSeparator(Token<PHPTokenId> token) {
        return isComma(token) || isLeftParen(token);
    }

    private static boolean isReturnTypeSeparator(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN)
                && TokenUtilities.textEquals(token.text(), ":"); // NOI18N
    }

    static boolean isVerticalBar(Token<?extends PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_OPERATOR
                && TokenUtilities.textEquals(token.text(), Type.SEPARATOR);
    }

    private static boolean isOrOperator(Token<PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_OPERATOR
                && TokenUtilities.textEquals(token.text(), "||"); // NOI18N
    }

    private static boolean isAndOperator(Token<PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_OPERATOR
                && TokenUtilities.textEquals(token.text(), "&&"); // NOI18N
    }

    private static boolean isNullableTypesPrefix(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN)
                && TokenUtilities.textEquals(token.text(), "?"); // NOI18N
    }

    private static boolean isMinus(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_OPERATOR)
                && TokenUtilities.textEquals(token.text(), "-"); // NOI18N
    }

    private static boolean isQuoteString(Token<PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING
                && (TokenUtilities.startsWith(token.text(), "'") || (TokenUtilities.startsWith(token.text(), "\""))); // NOI18N
    }

    private static boolean isArray(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_ARRAY);
    }

    private static boolean isCallable(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_CALLABLE);
    }

    private static boolean isIterable(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_ITERABLE);
    }

    private static boolean isAcceptedPrefix(Token<PHPTokenId> token) {
        return isVariable(token) || isReference(token)
                || isRightParen(token) || isString(token) || isWhiteSpace(token) || isNamespaceSeparator(token)
                || isType(token);
    }

    private static boolean isFieldModifier(Token<PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_PRIVATE
                || token.id() == PHPTokenId.PHP_PROTECTED
                || token.id() == PHPTokenId.PHP_PUBLIC
                || token.id() == PHPTokenId.PHP_STATIC
                || token.id() == PHPTokenId.PHP_READONLY
                || token.id() == PHPTokenId.PHP_VAR;
    }

    private static boolean isVisibilityModifier(Token<PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_PRIVATE
                || token.id() == PHPTokenId.PHP_PROTECTED
                || token.id() == PHPTokenId.PHP_PUBLIC;
    }

    private static boolean isReadonlyModifier(Token<PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_READONLY;
    }

    private static boolean isConstructor(Token<PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_STRING
                && TokenUtilities.textEquals(token.text(), "__construct"); // NOI18N
    }

    private static boolean isType(Token<PHPTokenId> token) {
        PHPTokenId id = token.id();
        return id == PHPTokenId.PHP_TYPE_BOOL
                || id == PHPTokenId.PHP_TYPE_FLOAT
                || id == PHPTokenId.PHP_TYPE_INT
                || id == PHPTokenId.PHP_TYPE_STRING
                || id == PHPTokenId.PHP_TYPE_VOID
                || id == PHPTokenId.PHP_TYPE_NEVER
                || id == PHPTokenId.PHP_TYPE_OBJECT
                || id == PHPTokenId.PHP_TYPE_MIXED
                || id == PHPTokenId.PHP_SELF
                || id == PHPTokenId.PHP_PARENT
                || id == PHPTokenId.PHP_STATIC
                || id == PHPTokenId.PHP_NULL
                || id == PHPTokenId.PHP_FALSE
                || id == PHPTokenId.PHP_TRUE
                || id == PHPTokenId.PHP_ARRAY
                || id == PHPTokenId.PHP_ITERABLE
                || id == PHPTokenId.PHP_CALLABLE;
    }

    static boolean isComma(Token<? extends PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_TOKEN)
                && TokenUtilities.textEquals(token.text(), ","); // NOI18N
    }

    private static boolean isWhiteSpace(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.WHITESPACE);
    }

    private static boolean isString(Token<PHPTokenId> token) {
        return token.id().equals(PHPTokenId.PHP_STRING);
    }

    private static boolean isInitilizerToken(Token<PHPTokenId> token) {
        return isQuoteString(token)
                || isEqualSign(token)
                || isMinus(token);
    }

    private static boolean isReturnTypeToken(Token<PHPTokenId> token) {
        return isVerticalBar(token)|| isNullableTypesPrefix(token) || isType(token)
                || isString(token) || isWhiteSpace(token) || isNamespaceSeparator(token);
    }

    private static boolean isObjectOperatorToken(Token<PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_OBJECT_OPERATOR
                || token.id() == PHPTokenId.PHP_NULLSAFE_OBJECT_OPERATOR;
    }

    static boolean lineContainsAny(Token<PHPTokenId> token, int tokenOffset, TokenSequence<PHPTokenId> tokenSequence, List<PHPTokenId> ids) {
        List<? extends Token<PHPTokenId>> preceedingLineTokens = getPreceedingLineTokens(token, tokenOffset, tokenSequence);
        for (Token<PHPTokenId> t : preceedingLineTokens) {
            if (ids.contains(t.id())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return all preceding tokens for current line
     */
    private static List<? extends Token<PHPTokenId>> getPreceedingLineTokens(Token<PHPTokenId> token, int tokenOffset, TokenSequence<PHPTokenId> tokenSequence) {
        int orgOffset = tokenSequence.offset();
        LinkedList<Token<PHPTokenId>> tokens = new LinkedList<>();
        if (token.id() != PHPTokenId.WHITESPACE
                || TokenUtilities.indexOf(token.text().subSequence(0, Math.min(token.text().length(), tokenOffset)), '\n') == -1) { // NOI18N
            while (true) {
                if (!tokenSequence.movePrevious()) {
                    break;
                }
                Token<PHPTokenId> cToken = tokenSequence.token();
                if ((cToken.id() == PHPTokenId.WHITESPACE
                        && TokenUtilities.indexOf(cToken.text(), '\n') != -1) // NOI18N
                        || cToken.id() == PHPTokenId.PHP_LINE_COMMENT) {
                    // e.g.
                    // public bool $bool = true; // line comment
                    // public tru^e $true = true;
                    break;
                }
                tokens.addLast(cToken);
            }
        }

        tokenSequence.move(orgOffset);
        tokenSequence.moveNext();

        return tokens;
    }

    private static synchronized boolean isInsideInterfaceDeclarationBlock(final ParserResult info, final int caretOffset, final TokenSequence tokenSequence) {
        boolean retval = false;
        List<ASTNode> nodePath = NavUtils.underCaret(info, lexerToASTOffset(info, caretOffset));
        int nodesCount = nodePath.size();
        if (nodesCount > 0) {
            ASTNode lastNode = nodePath.get(nodesCount - 1);
            if (lastNode instanceof Block) {
                if (nodesCount > 1) {
                    lastNode = nodePath.get(nodesCount - 2);
                    if (lastNode instanceof InterfaceDeclaration) {
                        retval = true;
                    } else {
                        retval = isUnderInterfaceTokenId(tokenSequence);
                    }
                }
            } else {
                retval = isUnderInterfaceTokenId(tokenSequence);
            }
        }
        return retval;
    }

    private static synchronized boolean isUnderInterfaceTokenId(final TokenSequence tokenSequence) {
        boolean retval = false;
        int curlyBalance = -1;
        int orgOffset = tokenSequence.offset();
        try {
            while (tokenSequence.movePrevious()) {
                Token token = tokenSequence.token();
                TokenId id = token.id();
                if (id.equals(PHPTokenId.PHP_INTERFACE) && curlyBalance == 0) {
                    retval = true;
                    break;
                } else if (id.equals(PHPTokenId.PHP_CURLY_OPEN)) {
                    curlyBalance++;
                } else if (id.equals(PHPTokenId.PHP_CURLY_CLOSE)) {
                    curlyBalance--;
                } else if (id.equals(PHPTokenId.PHP_CLASS) || id.equals(PHPTokenId.PHP_WHILE)
                        || id.equals(PHPTokenId.PHP_IF) || id.equals(PHPTokenId.PHP_FOR)
                        || id.equals(PHPTokenId.PHP_FOREACH) || id.equals(PHPTokenId.PHP_TRY)
                        || id.equals(PHPTokenId.PHP_CATCH) || id.equals(PHPTokenId.PHP_FUNCTION)) {
                    // here could be more tokens which can interrupt interface scope, but theese are good enough
                    retval = false;
                    break;
                }
            }
        } finally {
            tokenSequence.move(orgOffset);
            tokenSequence.moveNext();
        }
        return retval;
    }

    private static synchronized boolean isInsideClassOrTraitOrEnumDeclarationBlock(ParserResult info,
            int caretOffset, TokenSequence tokenSequence) {
        List<ASTNode> nodePath = NavUtils.underCaret(info, lexerToASTOffset(info, caretOffset));
        boolean methDecl = false;
        boolean funcDecl = false;
        boolean typeDecl = false;
        boolean isTypeInsideFunc = false;
        boolean isFuncInsideType = false;
        for (ASTNode aSTNode : nodePath) {
            if (aSTNode instanceof FunctionDeclaration) {
                funcDecl = true;
                if (typeDecl) {
                    isFuncInsideType = true;
                }
            } else if (aSTNode instanceof MethodDeclaration) {
                methDecl = true;
            } else if (aSTNode instanceof ClassDeclaration
                    || aSTNode instanceof TraitDeclaration
                    || aSTNode instanceof EnumDeclaration) {
                if (aSTNode.getEndOffset() != caretOffset) {
                    typeDecl = true;
                    if (funcDecl) {
                        isTypeInsideFunc = true;
                    }
                } else {
                    return false;
                }
            }
        }
        if (funcDecl && !methDecl && !typeDecl) {
            final StringBuilder sb = new StringBuilder();
            new DefaultVisitor() {

                @Override
                public void visit(ASTError astError) {
                    super.visit(astError);
                    sb.append(astError.toString());
                }
            }.scan(Utils.getRoot(info));
            if (sb.length() == 0) {
                return false;
            }
        }
        if (isTypeInsideFunc && !isFuncInsideType) {
            return true;
        }
        int orgOffset = tokenSequence.offset();
        try {
            int curlyOpen = 0;
            int curlyClose = 0;
            while (tokenSequence.movePrevious()) {
                Token token = tokenSequence.token();
                TokenId id = token.id();
                if (id.equals(PHPTokenId.PHP_CURLY_OPEN)) {
                    curlyOpen++;
                } else if (id.equals(PHPTokenId.PHP_CURLY_CLOSE)) {
                    curlyClose++;
                } else if ((id.equals(PHPTokenId.PHP_FUNCTION)
                        || id.equals(PHPTokenId.PHP_WHILE)
                        || id.equals(PHPTokenId.PHP_IF)
                        || id.equals(PHPTokenId.PHP_FOR)
                        || id.equals(PHPTokenId.PHP_FOREACH)
                        || id.equals(PHPTokenId.PHP_TRY)
                        || id.equals(PHPTokenId.PHP_CATCH))
                        && (curlyOpen > curlyClose)) {
                    return false;
                } else if (id.equals(PHPTokenId.PHP_CLASS)
                        || id.equals(PHPTokenId.PHP_TRAIT)
                        || id.equals(PHPTokenId.PHP_ENUM)) {
                    boolean isTypeScope = curlyOpen > 0 && (curlyOpen > curlyClose);
                    return isTypeScope;
                }
            }
        } finally {
            tokenSequence.move(orgOffset);
            tokenSequence.moveNext();
        }
        return false;
    }

    @CheckForNull
    static Token<? extends PHPTokenId> findFunctionInvocationName(TokenSequence<PHPTokenId> ts, int caretOffset) {
        ts.move(caretOffset);
        if (!ts.movePrevious()) {
            return null;
        }
        // find ( or ,
        Token<PHPTokenId> token = ts.token();
        PHPTokenId id = token.id();
        if (id != PHPTokenId.PHP_STRING
                && id != PHPTokenId.WHITESPACE
                && !isParamSeparator(token)) {
            return null;
        }
        Token<? extends PHPTokenId> previousToken = LexUtilities.findPrevious(ts, Arrays.asList(PHPTokenId.PHP_STRING, PHPTokenId.WHITESPACE));
        if (previousToken == null) {
            return null;
        }
        if (isComma(previousToken)) {
            // find (
            int braceBalance = 0;
            int curlyBalance = 0;
            while (ts.movePrevious()) {
                if (TokenUtilities.textEquals(ts.token().text(), "${") // NOI18N
                        || TokenUtilities.textEquals(ts.token().text(), "{")) { // NOI18N
                    curlyBalance++;
                } else if (TokenUtilities.textEquals(ts.token().text(), "}")) { // NOI18N
                    curlyBalance--;
                } else if (TokenUtilities.textEquals(ts.token().text(), "(")) { // NOI18N
                    if (braceBalance == 0) {
                        previousToken = ts.token();
                        break;
                    }
                    braceBalance++;
                } else if (TokenUtilities.textEquals(ts.token().text(), ")")) { // NOI18N
                    braceBalance--;
                }
                if (ts.token().id() == PHPTokenId.PHP_SEMICOLON && curlyBalance == 0) {
                    // e.g. ; is used in labmda function: test(function(){return 1;}, );
                    break;
                }
            }
        }

        if (isLeftParen(previousToken) && ts.movePrevious()) {
            // find a label "label("
            previousToken = LexUtilities.findPrevious(ts, Arrays.asList(PHPTokenId.WHITESPACE));
            if (previousToken == null) {
                return null;
            }
            if (previousToken.id() == PHPTokenId.PHP_STRING) {
                return ts.token();
            }
        }
        return null;
    }

    @CheckForNull
    private static CompletionContext getNamedArgumentsContext(final int caretOffset, final TokenSequence<PHPTokenId> ts) {
        int originalOffset = ts.offset();
        CompletionContext retval = null;
        Token<? extends PHPTokenId> functionName = findFunctionInvocationName(ts, caretOffset);
        if (functionName != null) {
            ts.moveNext();
            if (acceptTokenChains(ts, CLASS_MEMBER_TOKENCHAINS, true)) {
                retval = CompletionContext.CLASS_MEMBER_PARAMETER_NAME;
            } else if (acceptTokenChains(ts, STATIC_CLASS_MEMBER_TOKENCHAINS, true)) {
                retval = CompletionContext.STATIC_CLASS_MEMBER_PARAMETER_NAME;
            } else if (acceptTokenChains(ts, CLASS_NAME_TOKENCHAINS, true)
                    || isInAttribute(caretOffset, ts, true)) {
                retval = CompletionContext.CONSTRUCTOR_PARAMETER_NAME;
            } else {
                retval = CompletionContext.FUNCTION_PARAMETER_NAME;
            }
        }
        ts.move(originalOffset);
        ts.moveNext();
        return retval;
    }

    private static boolean isInMatchExpression(final int caretOffset, final TokenSequence ts) {
        int originalOffset = ts.offset();
        boolean result = false;
        ts.move(caretOffset);
        if (ts.moveNext() && ts.movePrevious()) {
            while (ts.movePrevious()) {
                TokenId tokenId = ts.token().id();
                if (tokenId == PHPTokenId.PHP_SEMICOLON) {
                    break;
                }
                if (tokenId == PHPTokenId.PHP_MATCH) {
                    result = true;
                    break;
                }
            }
        }

        ts.move(originalOffset);
        ts.moveNext();
        return result;
    }

    static boolean isInAttribute(final int caretOffset, final TokenSequence ts, boolean allowInArgs) {
        final int originalOffset = ts.offset();
        // e.g. #[MyAttr^ibute] ("^": caret)
        boolean result = false;
        int bracketBalance = 0;
        int parenBalance = 0;
        ts.move(caretOffset);
        while (ts.movePrevious()) {
            if (isLeftBracket(ts.token())) {
                bracketBalance--;
            } else if (isRightBracket(ts.token())) {
                bracketBalance++;
            } else if (isLeftParen(ts.token())) {
                parenBalance--;
            } else if (isRightParen(ts.token())) {
                parenBalance++;
            }
            TokenId tokenId = ts.token().id();
            if (tokenId == PHPTokenId.PHP_ATTRIBUTE) {
                if (allowInArgs) {
                    result = bracketBalance == 0;
                } else {
                    result = bracketBalance == 0
                            && parenBalance == 0;
                }
                break;
            }
            if (tokenId == PHPTokenId.PHP_SEMICOLON
                    || isFunctionDeclaration(ts.token())
                    || isVisibilityModifier(ts.token())) {
                break;
            }
        }
        ts.move(originalOffset);
        ts.moveNext();
        return result;
    }

    static CompletionContext getCompletionContextInComment(TokenSequence<PHPTokenId> tokenSeq, final int caretOffset, ParserResult info) {
        Token<PHPTokenId> token = tokenSeq.token();
        CharSequence text = token.text();

        if (text == null || text.length() == 0) {
            return CompletionContext.NONE;
        }

        int offset = caretOffset - tokenSeq.offset() - 1;
        char charAt = 0;

        if (offset > -1) {
            charAt = text.charAt(offset--);
            while (-1 < offset && !Character.isWhitespace(charAt) && charAt != '$') {
                charAt = text.charAt(offset);
                offset--;
            }
        }

        if (offset < text.length() && charAt == '$') {
            return CompletionContext.STRING;
        }
        return CompletionContext.TYPE_NAME;
    }

    static int lexerToASTOffset(PHPParseResult result, int lexerOffset) {
//        if (result.getTranslatedSource() != null) {
//            return result.getTranslatedSource().getAstOffset(lexerOffset);
//        }
        return lexerOffset;
    }

    static int lexerToASTOffset(ParserResult info, int lexerOffset) {
        int value = 0;
        if (info instanceof PHPParseResult) {
            PHPParseResult result = (PHPParseResult) info;
            value = lexerToASTOffset(result, lexerOffset);
        }
        return value;
    }
}
