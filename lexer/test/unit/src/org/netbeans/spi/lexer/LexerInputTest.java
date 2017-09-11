/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
  *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.spi.lexer;

import java.util.Collection;
import java.util.EnumSet;
import org.junit.Test;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import static org.junit.Assert.*;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Florian Vogler
 */
public class LexerInputTest {

    public LexerInputTest() {
    }

    @Test
    public void testLexerInput_ReadText_StartEnd() {
        String text = "abcdefg";
        TokenHierarchy hi = TokenHierarchy.create(text, TokenIdImpl.language());
        TokenSequence ts = hi.tokenSequence();
        assertTrue(ts.moveNext());
    }

    private static class LexerImpl implements Lexer<TokenIdImpl> {

        private final LexerRestartInfo<TokenIdImpl> info;
        private final LexerInput input;
        private final TokenFactory<TokenIdImpl> tokenFactory;

        public LexerImpl(LexerRestartInfo<TokenIdImpl> info) {
            this.info = info;
            this.input = this.info.input();
            this.tokenFactory = info.tokenFactory();
        }

        @Override
        public Token<TokenIdImpl> nextToken() {
            int ch;
            while (true) {
                ch = input.read();
                if (ch == LexerInput.EOF) {
                    break;
                }
            }
            String text = String.valueOf(input.readText());

            String subText = text.substring(2, 4);
            assertTrue(subText.contentEquals(input.readText(2, 4)));

            return tokenFactory.createToken(TokenIdImpl.TEXT);
        }

        @Override
        public Object state() {
            return null;
        }

        @Override
        public void release() {
        }
    }

    public static enum TokenIdImpl implements TokenId {

        TEXT;

        TokenIdImpl() {
        }

        @Override
        public String primaryCategory() {
            return "text";
        }

        public static Language<TokenIdImpl> language() {
            return LanguageHierarchyImpl.INSTANCE;
        }
    }

    private static class LanguageHierarchyImpl extends LanguageHierarchy<TokenIdImpl> {

        private static final Language<TokenIdImpl> INSTANCE = new LanguageHierarchyImpl().language();
        public static final String MIME_TYPE = "text/x-LexerInput";

        @Override
        protected String mimeType() {
            return MIME_TYPE;
        }

        @Override
        protected Collection<TokenIdImpl> createTokenIds() {
            return EnumSet.allOf(TokenIdImpl.class);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
                Token<TokenIdImpl> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            return null; // No embedding
        }

        @Override
        protected Lexer<TokenIdImpl> createLexer(LexerRestartInfo<TokenIdImpl> info) {
            return new LexerImpl(info);
        }
    }
}
