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
 * Token identifications of the simple plain language.
 *
 * @author mmetelka
 */
public enum TestPlainTokenId implements TokenId {
    
    WORD,
    WHITESPACE("whitespace");

    public static final String MIME_TYPE = "text/x-simple-plain";
    
    private final String primaryCategory;

    TestPlainTokenId() {
        this(null);
    }

    TestPlainTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    public static final Language<TestPlainTokenId> language
            = new LH(MIME_TYPE).language();

    public static final Language<TestPlainTokenId> inParensLanguage
            = new LH("text/x-join-in-parens").language();

    public static final Language<TestPlainTokenId> inBracketsLanguage
            = new LH("text/x-join-in-brackets").language();

    public static final Language<TestPlainTokenId> inApostrophesLanguage
            = new LH("text/x-join-in-apostrophes").language();

    private static final class LH extends LanguageHierarchy<TestPlainTokenId> {

        private String mimeType;

        public LH(String mimeType) {
            this.mimeType = mimeType;
        }

        @Override
        protected Collection<TestPlainTokenId> createTokenIds() {
            return EnumSet.allOf(TestPlainTokenId.class);
        }
        
        @Override
        public Lexer<TestPlainTokenId> createLexer(LexerRestartInfo<TestPlainTokenId> info) {
            return new TestPlainLexer(info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
        Token<TestPlainTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }

        @Override
        protected String mimeType() {
            return mimeType;
        }
        
    }

    public static Language<TestPlainTokenId> language() {
        return language;
    }

}
