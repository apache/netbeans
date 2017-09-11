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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
