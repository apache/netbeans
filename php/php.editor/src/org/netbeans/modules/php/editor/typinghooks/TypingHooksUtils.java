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
package org.netbeans.modules.php.editor.typinghooks;

import org.netbeans.api.lexer.Token;
import org.netbeans.modules.csl.api.EditorOptions;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class TypingHooksUtils {

    /**
     * Tokens which indicate that we're within a literal string
     */
    private static final PHPTokenId[] STRING_TOKENS = {
        PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING,
        PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE
    };

    private TypingHooksUtils() {
    }

    public static boolean isStringToken(Token<? extends PHPTokenId> token) {
        for (PHPTokenId stringTokenId : STRING_TOKENS) {
            if (token.id() == stringTokenId) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInsertMatchingEnabled() {
        EditorOptions options = EditorOptions.get(FileUtils.PHP_MIME_TYPE);
        if (options != null) {
            return options.getMatchBrackets();
        }
        return true;
    }

}
