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

package org.netbeans.spi.lexer;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.LanguageOperation;
import org.netbeans.lib.lexer.LexerApiPackageAccessor;

/**
 * Description of a particular language embedding including
 * starting and ending skipped regions of a token containing this embedding
 * and a definition of an embedded language hierarchy.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class LanguageEmbedding<T extends TokenId> {
    
    /**
     * Create language embedding that does not join embedded sections.
     *
     * @see #create(Language, int, int, boolean)
     */
    public static <T extends TokenId> LanguageEmbedding<T> create(
    Language<T> language, int startSkipLength, int endSkipLength) {
        return create(language, startSkipLength, endSkipLength, false);
    }

    /**
     * Construct new language embedding for the given parameters
     * or get an existing cached one.
     *
     * @param language non-null language.
     * @param startSkipLength &gt;=0 number of characters in an initial part of the token
     *  for which the language embedding is defined that should be excluded
     *  from the embedded section. The excluded characters will not be lexed
     *  and there will be no tokens created for them.
     * @param endSkipLength &gt;=0 number of characters at the end of the token
     *  for which the language embedding is defined that should be excluded
     *  from the embedded section. The excluded characters will not be lexed
     *  and there will be no tokens created for them.
     * @param joinSections whether sections with this embedding should be joined
     *  across the input source or whether they should stay separate.
     *  See also {@link #joinSections()}.
     * @return non-null language embedding instance.
     */
    public static <T extends TokenId> LanguageEmbedding<T> create(
    Language<T> language, int startSkipLength, int endSkipLength, boolean joinSections) {
        if (language == null) {
            throw new IllegalArgumentException("language may not be null"); // NOI18N
        }
        if (startSkipLength < 0) {
            throw new IllegalArgumentException("startSkipLength=" + startSkipLength + " < 0");
        }
        if (endSkipLength < 0) {
            throw new IllegalArgumentException("endSkipLength=" + endSkipLength + " < 0");
        }

        LanguageOperation<T> op = LexerApiPackageAccessor.get().languageOperation(language);
        return op.getEmbedding(startSkipLength, endSkipLength, joinSections);
    }
    
    private final Language<T> language;
    
    private final int startSkipLength;
    
    private final int endSkipLength;
    
    private final boolean joinSections;
    
    /**
     * Package-private constructor used by lexer spi package accessor.
     */
    LanguageEmbedding(Language<T> language,
    int startSkipLength, int endSkipLength, boolean joinSections) {
        assert (language != null) : "Embedded language may not be null."; // NOI18N
        assert (startSkipLength >= 0 && endSkipLength >= 0);
        this.language = language;
        this.startSkipLength = startSkipLength;
        this.endSkipLength = endSkipLength;
        this.joinSections = joinSections;
    }
    
    /**
     * Get the embedded language.
     *
     * @return non-null embedded language.
     */
    public Language<T> language() {
        return language;
    }

    /**
     * Get length of the initial part of the token (for which the embedding
     * is being created) that should be skipped
     * so it will be excluded from lexing and no tokens will be created for it.
     *
     * @return &gt;=0 number of characters in an initial part of the token
     *  (for which the language embedding is defined) that should be excluded
     *  from the embedded section. The excluded characters will not be lexed
     *  and there will be no tokens created for them.
     */
    public int startSkipLength() {
        return startSkipLength;
    }
    
    /**
     * Get length of the ending part of the token (for which the embedding
     * is being created) that should be skipped
     * so it will be excluded from lexing and no tokens will be created for it.
     *
     * @return &gt;=0 number of characters at the end of the token
     *  (for which the language embedding is defined) that should be excluded
     *  from the embedded section. The excluded characters will not be lexed
     *  and there will be no tokens created for them.
     */
    public int endSkipLength() {
        return endSkipLength;
    }

    /**
     * Whether sections with this embedding should be joined with the other
     * sections with this embedding at the same level.
     * <br>
     * For example for HTML sections embedded in JSP this flag should be true:
     * <pre>
     *  &lt;!-- HTML comment start
     *      &lt;% System.out.println("Hello"); %&gt;
           still in HTML comment --&lt;
     * </pre>
     * <br>
     * Only the embedded sections with the same language path will be joined.
     * <br>
     * When a particular embedded section would get relexed till its end then
     * the next section may get relexed as well. For example if someone would add
     * "--&gt;" at the end of the first line in the example above then the third
     * line that used to be comment will be relexed and it will become an html text.
     * <br>
     * Generally relexing of a next section happens in the following cases:
     * <ul>
     *     <li>
     *     Tokens right to the end of the present section get relexed
     *     and state after the present last token differs from the one that
     *     was there before relexing. Or a state is the same but the last token
     *     of the section was incomplete and after the relexing either token id
     *     or part type of the token differs.
     *     </li>
     *     <li>
     *     One or more sections were removed due to modification and it's necessary
     *     to connect the previous non-removed section with the first one
     *     that follows the removed ones.
     *     </li>
     *     <li>
     *     One or more sections were added due to modification and it's necessary
     *     to connect the previous non-removed section with the first one
     *     that follows the removed ones.
     *     </li>
     * </ul>
     *
     * @return joinSections whether sections with this embedding should be joined
     *  across the input source or whether they should stay separate.
     */
    public boolean joinSections() {
        return joinSections;
    }
    
    @Override
    public String toString() {
        return "language: " + language() + ", skip[" + startSkipLength() // NOI18N
            + ", " + endSkipLength + "];" + (joinSections ? "join" : "no-join"); // NOI18N
    }
    
}
