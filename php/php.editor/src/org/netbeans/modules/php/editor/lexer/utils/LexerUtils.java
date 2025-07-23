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
package org.netbeans.modules.php.editor.lexer.utils;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;

public final class LexerUtils {

    private static final Collection<PHPTokenId> WS_COMMENT_TOKENS = Set.of(
            PHPTokenId.WHITESPACE,
            PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
            PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
            PHPTokenId.PHP_LINE_COMMENT
    );
    private static final Collection<PHPTokenId> VISIBILITY_TOKENS = Set.of(
            PHPTokenId.PHP_PUBLIC,
            PHPTokenId.PHP_PROTECTED,
            PHPTokenId.PHP_PRIVATE
    );
    private static final Collection<PHPTokenId> SET_VISIBILITY_TOKENS = Set.of(
            PHPTokenId.PHP_PUBLIC_SET,
            PHPTokenId.PHP_PROTECTED_SET,
            PHPTokenId.PHP_PRIVATE_SET
    );
    private static final Collection<PHPTokenId> ALL_VISIBILITY_TOKENS = Set.of(
            PHPTokenId.PHP_PUBLIC,
            PHPTokenId.PHP_PROTECTED,
            PHPTokenId.PHP_PRIVATE,
            PHPTokenId.PHP_PUBLIC_SET,
            PHPTokenId.PHP_PROTECTED_SET,
            PHPTokenId.PHP_PRIVATE_SET
    );

    private LexerUtils() {
    }

    /**
     * Check whether a token has the curly open brace ("{"). i.e. "{" or "${"
     *
     * @param token a token
     * @return {@code true} if a token is "{" or "${", {@code false} otherwise
     */
    public static boolean hasCurlyOpen(Token<? extends PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_CURLY_OPEN || isDollarCurlyOpen(token);
    }

    /**
     * Check whether a token is the dollar curly open brace ("${").
     *
     * @param token a token
     * @return {@code true} if a token is "${", {@code false} otherwise
     */
    public static boolean isDollarCurlyOpen(Token<? extends PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(token.text(), "${"); // NOI18N
    }

    /**
     * Check whether a token is the open parenthesis ("(").
     *
     * @param token a token
     * @return {@code true} if a token is "(", {@code false} otherwise
     */
    public static boolean isOpenParen(Token<? extends PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(token.text(), "("); // NOI18N
    }

    /**
     * Check whether a token is the close parenthesis (")").
     *
     * @param token a token
     * @return {@code true} if a token is ")", {@code false} otherwise
     */
    public static boolean isCloseParen(Token<? extends PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(token.text(), ")"); // NOI18N
    }

    /**
     * Check whether a token is the open bracket ("[").
     *
     * @param token a token
     * @return {@code true} if a token is "[", {@code false} otherwise
     */
    public static boolean isOpenBracket(Token<? extends PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(token.text(), "["); // NOI18N
    }

    /**
     * Check whether a token is the close bracket ("]").
     *
     * @param token a token
     * @return {@code true} if a token is "]", {@code false} otherwise
     */
    public static boolean isCloseBracket(Token<? extends PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(token.text(), "]"); // NOI18N
    }

    /**
     * Check whether a token is the comma (",").
     *
     * @param token a token
     * @return {@code true} if a token is ",", {@code false} otherwise
     */
    public static boolean isComma(Token<? extends PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(token.text(), ","); // NOI18N
    }

    /**
     * Check whether a token is the colon (":").
     *
     * @param token a token
     * @return {@code true} if a token is ":", {@code false} otherwise
     */
    public static boolean isColon(Token<? extends PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(token.text(), ":"); // NOI18N
    }

    /**
     * Check whether a token is the colon (":").
     *
     * @param token a token
     * @return {@code true} if a token is ":", {@code false} otherwise
     */
    public static boolean isEqual(Token<? extends PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_OPERATOR && TokenUtilities.textEquals(token.text(), "="); // NOI18N
    }

    /**
     * Check whether a token is the double arrow operator ("=>").
     *
     * @param token a token
     * @return {@code true} if a token is "=>", {@code false} otherwise
     */
    public static boolean isDoubleArrow(Token<? extends PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_OPERATOR && TokenUtilities.textEquals(token.text(), "=>"); // NOI18N
    }

    /**
     * Check whether a token is a whitespace or a comments.
     *
     * @param token a token
     * @return {@code true} if a token is a whitespace or a comment,
     * {@code false} otherwise
     */
    public static boolean isWhitespaceOrCommentToken(Token<? extends PHPTokenId> token) {
        return token == null ? false : WS_COMMENT_TOKENS.contains(token.id());
    }

    /**
     * Check whether a token is a visibility token ({@code public},
     * {@code protected}, {@code private}).
     *
     * @param token a token can be {@code null}
     * @return {@code true} if it is a visibility token, {@code false} otherwise
     */
    public static boolean isVisibilityToken(@NullAllowed Token<? extends PHPTokenId> token) {
        return token == null ? false : VISIBILITY_TOKENS.contains(token.id());
    }

    /**
     * Check whether a token is a set visibility token ({@code public(set)},
     * {@code protected(set)}, {@code private(set)}).
     *
     * @param token a token can be {@code null}
     * @return {@code true} if it is a set visibility token, {@code false}
     * otherwise
     */
    public static boolean isSetVisibilityToken(@NullAllowed Token<? extends PHPTokenId> token) {
        return token == null ? false : SET_VISIBILITY_TOKENS.contains(token.id());
    }

    /**
     * Check whether a token is one of all visibility tokens ({@code public},
     * {@code protected}, {@code private}), ({@code public(set)},
     * {@code protected(set)}, {@code private(set)}).
     *
     * @param token a token can be {@code null}
     * @return {@code true} if it is one of all visibility tokens, {@code false}
     * otherwise
     */
    public static boolean isGetOrSetVisibilityToken(@NullAllowed Token<? extends PHPTokenId> token) {
        return token == null ? false : ALL_VISIBILITY_TOKENS.contains(token.id());
    }

    /**
     * Get whitespace and comment token ids.
     *
     * @return whitespace and comment token ids.
     */
    public static List<PHPTokenId> getWSCommentTokens() {
        return List.copyOf(WS_COMMENT_TOKENS);
    }

    /**
     * Get all visibility token ids.
     *
     * @return all visibility token ids
     */
    public static List<PHPTokenId> getAllVisibilityTokens() {
        return List.copyOf(ALL_VISIBILITY_TOKENS);
    }
}
