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

package org.netbeans.modules.csl.spi;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Tomas Stupka
 */
public class LanguageRegistrationTest extends NbTestCase {

    public LanguageRegistrationTest(String name) {
        super(name);
    }

    public static void testUseMultiview() {
        Language l = LanguageRegistry.getInstance().getLanguageByMimeType("text/x-test");
        assertNotNull(l);
        assertEquals("text/x-test", l.getMimeType());
        assertTrue(l.useMultiview());
    }
   
    @LanguageRegistration(
        mimeType="text/x-test",
        useMultiview=true
    )
    public static class TestLanguage extends DefaultLanguageConfig {

        @Override
        public org.netbeans.api.lexer.Language getLexerLanguage() {
            return new Lang("text/x-test").language();
        }

        @Override
        public String getDisplayName() {
            return "test language";
        }
        
    }
    
    private static enum TestTokenId implements TokenId {

        TOKEN_ID1,
        TOKEN_ID2;

        private TestTokenId() {
        }

        @Override
        public String primaryCategory() {
            return null;
        }
    } // End of TestTokenId

    private static final class Lang extends LanguageHierarchy<TestTokenId> {

        private String mimeType;

        public Lang(String mimeType) {
            this.mimeType = mimeType;
        }

        @Override
        protected Lexer<TestTokenId> createLexer(LexerRestartInfo<TestTokenId> info) {
            return null;
        }

        @Override
        protected Collection<TestTokenId> createTokenIds() {
            return EnumSet.allOf(TestTokenId.class);
        }

        @Override
        public String mimeType() {
            return mimeType;
        }
    } // End of Lang class
    
}
