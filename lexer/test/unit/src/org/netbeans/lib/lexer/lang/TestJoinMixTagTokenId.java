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
 * Language that recognizes tags in the text allowing to test certain anomalies
 * such as mixing join and non-join embeddings.
 * Text of tag tokens is joined and 
 *
 * @author Miloslav Metelka
 */
public enum TestJoinMixTagTokenId implements TokenId {

    /**
     * Text enclosed in &lt;..&gt; including them. <br/>
     * But not an empty "<>" (used for embedding testing).
     * Implicit joining embedding of TestJoinMixTextTokenId.inTagLanguage.
     */
    TAG(),
    /**
     * Everything else.
     * Implicit embedding of TestPlainTokenId.inQuotesLanguage
     */
    TEXT();

    
    /**
     * Allow to control whether embedding should be returned or not (null embedding).
     * Switching allowance of embedding simulates an errorneous state
     * which the lexer framework should attempt to overcome.
     */
    private static boolean allowEmbedding = true;

    /**
     * Allow to control whether sections joining is on or off.
     * Switching allowance of sections joining simulates an errorneous state
     * which the lexer framework should attempt to overcome.
     */
    private static boolean joinSections = true;

    public static boolean isJoinSections() {
        return joinSections;
    }

    public static void setJoinSections(boolean joinSections) {
        TestJoinMixTagTokenId.joinSections = joinSections;
    }

    public static boolean isAllowEmbedding() {
        return allowEmbedding;
    }

    public static void setAllowEmbedding(boolean allowEmbedding) {
        TestJoinMixTagTokenId.allowEmbedding = allowEmbedding;
    }
    
    private TestJoinMixTagTokenId() {
    }
    
    public String primaryCategory() {
        return null;
    }

    private static final Language<TestJoinMixTagTokenId> language
    = new LanguageHierarchy<TestJoinMixTagTokenId>() {

        @Override
        protected String mimeType() {
            return "text/x-join-mix-tag";
        }

        @Override
        protected Collection<TestJoinMixTagTokenId> createTokenIds() {
            return EnumSet.allOf(TestJoinMixTagTokenId.class);
        }

        @Override
        protected Lexer<TestJoinMixTagTokenId> createLexer(LexerRestartInfo<TestJoinMixTagTokenId> info) {
            return new TestJoinMixTagLexer(info);
        }
        
        @Override
        public LanguageEmbedding<?> embedding(
        Token<TestJoinMixTagTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            if (!allowEmbedding) {
                return null;
            }
            // Test language embedding in the block comment
            switch (token.id()) {
                case TAG:
                    return LanguageEmbedding.create(TestJoinMixTextTokenId.language, 1, 1, joinSections);
                case TEXT:
                    return null;
            }
            return null; // No embedding
        }

    }.language();

    public static Language<TestJoinMixTagTokenId> language() {
        return language;
    }

}
