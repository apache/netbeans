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

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;

public final class LexerUtils {

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
}
