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
package org.netbeans.modules.php.blade.editor.lexer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import static org.netbeans.modules.php.blade.syntax.antlr4.v10.BladeAntlrLexer.*;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;

/**
 *
 * @author bogdan
 */
public final class BladeLexerUtils {

    private BladeLexerUtils() {

    }
    public static final Integer[] TOKENS_WITH_IDENTIFIABLE_PARAM_VALUES = new Integer[] {
        D_EXTENDS, D_INCLUDE, D_INCLUDE_IF, D_INCLUDE_WHEN,
        D_INCLUDE_UNLESS, D_EACH, D_SECTION, D_HAS_SECTION, D_SECTION_MISSING,
        D_PUSH, D_PUSH_IF, D_PREPEND, D_USE, D_INJECT, D_ASSET_BUNDLER
    };
    public static final Set<Integer> TOKENS_WITH_IDENTIFIABLE_PARAM = new HashSet<>(Arrays.asList(TOKENS_WITH_IDENTIFIABLE_PARAM_VALUES));

    public static TokenSequence<? extends PHPTokenId> getPhpTokenSequence(TokenHierarchy<Document> th, final int offset) {
        return getTokenSequence(th, offset, PHPTokenId.language());
    }

    public static TokenSequence<? extends PHPTokenId> getPhpTokenSequence(final Document document, final int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(document);
        return getTokenSequence(th, offset, PHPTokenId.language());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> TokenSequence<? extends T> getTokenSequence(final TokenHierarchy<?> th,
            final int offset, final Language language) {
        TokenSequence<? extends T> ts = th.tokenSequence(language);
        if (ts == null) {
            List<TokenSequence<?>> list = th.embeddedTokenSequences(offset, true);
            for (TokenSequence t : list) {
                if (t.language() == language) {
                    ts = t;
                    break;
                }
            }
            if (ts == null) {
                list = th.embeddedTokenSequences(offset, false);
                for (TokenSequence t : list) {
                    if (t.language() == language) {
                        ts = t;
                        break;
                    }
                }
            }
        }
        return ts;
    }
}
