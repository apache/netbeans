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

package org.netbeans.lib.lexer.test.dump;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Ids for parsing of the input file of a particular language
 * for the token dump check.
 * <br/>
 * The text of the special tokens is interlarded with dots
 * to eliminate the possibility that the particular special token will clash
 * with the target language.
 *
 * @author mmetelka
 */
public enum TokenDumpTokenId implements TokenId {
    
    /** Single line of text without newline. */
    TEXT(null),
    
    /** Unicode character as line containing ".\.u.XXXX." only where XXXX
     * is sequence of (lowercase or uppercase) hex digits.
     * <code>(Character)Token.getProperty({@link UNICODE_CHAR_TOKEN_PROPERTY}))</code>
     * gives the character.
     */
    UNICODE_CHAR("character"),
    
    /** Line containing ".\.b." only defines \b char in the input.
     * <code>(Character)Token.getProperty({@link UNICODE_CHAR_TOKEN_PROPERTY}))</code>
     * gives the \b character.
     */
    BACKSPACE_CHAR("character"),
    /** Line containing ".\.f." only defines \f char in the input.
     * <code>(Character)Token.getProperty({@link UNICODE_CHAR_TOKEN_PROPERTY}))</code>
     * gives the \f character.
     */
    FORM_FEED_CHAR("character"),
    /** Line containing ".\.b." only defines \b char in the input.
     * <code>(Character)Token.getProperty({@link UNICODE_CHAR_TOKEN_PROPERTY}))</code>
     * gives the \b character.
     */
    CR_CHAR("character"),
    /** Line containing ".\.r." only defines \r char in the input.
     * <code>(Character)Token.getProperty({@link UNICODE_CHAR_TOKEN_PROPERTY}))</code>
     * gives the \r character.
     */
    NEWLINE_CHAR("character"),
    /** Line containing ".\.t." only defines \t char in the input.
     * <code>(Character)Token.getProperty({@link UNICODE_CHAR_TOKEN_PROPERTY}))</code>
     * gives the \t character.
     */
    TAB_CHAR("character"),
    
    /** EOF mark as line containing ".e.o.f." only.
     * It helps to separate tests and test lexer's behavior at the end of buffer.
     */
    EOF_VIRTUAL(null),
    
    /** Test name as line starting with ".t.e.s.t." to help debugging
     *  where a possible problem occurred. It should be used at begining
     *  or inside a test between virtual eofs.
     */
    TEST_NAME(null),
    
    /** Newline '\r', '\n' or '\r\n'.
     * <br/>
     * The test itself will replace this with '\n' because otherwise the output
     * of the token dump would contain the particular line separator depending on the platform
     * where the file would be checked out which would break the test.
     * <br/>
     * To test specific line separators the {@link #CR_CHAR} or {@link #NEWLINE_CHAR} may be used.
     */
    NEWLINE(null);
    
    private String primaryCategory;

    private TokenDumpTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }
    
    public String primaryCategory() {
        return primaryCategory;
    }
    
    private static final Language<TokenDumpTokenId> lang = new LanguageHierarchy<TokenDumpTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-eof-mark";
        }

        @Override
        protected Collection<TokenDumpTokenId> createTokenIds() {
            return EnumSet.allOf(TokenDumpTokenId.class);
        }

        @Override
        protected Lexer<TokenDumpTokenId> createLexer(LexerRestartInfo<TokenDumpTokenId> info) {
            return new TokenDumpLexer(info);
        }
        
    }.language();
    
    public static Language<TokenDumpTokenId> language() {
        return lang;
    }
    
    private static Set<TokenDumpTokenId> charLiterals;
    
    public static boolean isCharLiteral(TokenDumpTokenId id) {
        Set<TokenDumpTokenId> catMembers = charLiterals;
        if (catMembers == null) {
            catMembers = language().tokenCategoryMembers("character");
            charLiterals = catMembers;
        }
        return catMembers.contains(id);
    }

    /**
     * Token property giving the unicode character value.
     */ 
    public static final String UNICODE_CHAR_TOKEN_PROPERTY = "unicode-char";
        
}
