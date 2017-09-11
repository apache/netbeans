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

package org.netbeans.lib.lexer.lang;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Embedded language for join sections testing.
 *
 * @author mmetelka
 */
public enum TestJoinTextTokenId implements TokenId {
    
    
    /**
     * Text enclosed in (..) including '(' and ')'. <br/>
     * Expicit embedding may be created (preferrably TestJoinTextTokenId.inBracesLanguage).
     */
    PARENS(),
    /**
     * Text enclosed in [..] including '[' and ']'. <br/>
     * Automatic joining embedding of TestPlainTokenId.inBracketsLanguage.
     */
    BRACKETS(),
    /**
     * Text in apostrophes including them. </br>
     * Automatic non-joining embedding of TestPlainTokenId.inApostrophesLanguage.
     */
    APOSTROPHES(),
    /**
     * All other text. <br/>
     * No embedding.
     */
    TEXT();

    private TestJoinTextTokenId() {
    }
    
    public String primaryCategory() {
        return null;
    }

    public static final Language<TestJoinTextTokenId> language
            = new LH("text/x-join-text").language();
            
    public static final Language<TestJoinTextTokenId> inTagLanguage
            = new LH("text/x-join-in-tag").language();
            
    public static final Language<TestJoinTextTokenId> inBracesLanguage
            = new LH("text/x-join-in-braces").language();
            
    public static final Language<TestJoinTextTokenId> inBackquotesLanguage
            = new LH("text/x-join-in-quotes").language();
            
    public static final Language<TestJoinTextTokenId> inPercentsLanguage
            = new LH("text/x-join-in-percents").language();
            
    private static final class LH extends LanguageHierarchy<TestJoinTextTokenId> {

        private String mimeType;
        
        LH(String mimeType) {
            this.mimeType = mimeType;
        }

        @Override
        protected String mimeType() {
            return mimeType;
        }

        @Override
        protected Collection<TestJoinTextTokenId> createTokenIds() {
            return EnumSet.allOf(TestJoinTextTokenId.class);
        }

        @Override
        protected Lexer<TestJoinTextTokenId> createLexer(LexerRestartInfo<TestJoinTextTokenId> info) {
            return new TestJoinTextLexer(info);
        }
        
        @Override
        public LanguageEmbedding<?> embedding(
        Token<TestJoinTextTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            // Test language embedding in the block comment
            switch (token.id()) {
//                case PARENS: - explicit custom embedding
//                    return LanguageEmbedding.create(TestPlainTokenId.inParensLanguage, 1, 1, true);
                case BRACKETS:
                    return LanguageEmbedding.create(TestPlainTokenId.inBracketsLanguage, 1, 1, true);
                case APOSTROPHES:
                    return LanguageEmbedding.create(TestPlainTokenId.inApostrophesLanguage, 1, 1, false);
//                case TEXT:
//                    return LanguageEmbedding.create(TestStringTokenId.language(), 1, 1);
            }
            return null; // No embedding
        }

    }
}
