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

package org.netbeans.api.lexer;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author vita
 */
public class LanguagePathTest extends NbTestCase {

    /** Creates a new instance of LanguagePathTest */
    public LanguagePathTest(String name) {
        super(name);
    }

    public void testMimePath() {
        Language<TestTokenId> jspLang = new Lang("text/x-jsp").language();
        Language<TestTokenId> javaLang = new Lang("text/x-java").language();
        Language<TestTokenId> javadocLang = new Lang("text/x-javadoc").language();
        
        LanguagePath jspPath = LanguagePath.get(jspLang);
        assertEquals("Invalid mime path for jspLang", "text/x-jsp", jspPath.mimePath());
        
        LanguagePath javaPath = LanguagePath.get(javaLang);
        assertEquals("Invalid mime path for javaLang", "text/x-java", javaPath.mimePath());
        
        LanguagePath jspJavaPath = LanguagePath.get(LanguagePath.get(jspLang), javaLang);
        assertEquals("Invalid mime path for jspLang",
                "text/x-jsp/text/x-java", jspJavaPath.mimePath());
        
        LanguagePath javaJavadocPath = LanguagePath.get(LanguagePath.get(javaLang), javadocLang);
        assertEquals("Invalid mime path for javaLang/javadocLang", 
                "text/x-java/text/x-javadoc", javaJavadocPath.mimePath());

        LanguagePath jspJavaJavadocPath = LanguagePath.get(LanguagePath.get(jspPath, javaLang), javadocLang);
        assertEquals("Invalid mime path for jspLang/javaLang/javadocLang", 
                "text/x-jsp/text/x-java/text/x-javadoc", jspJavaJavadocPath.mimePath());

//        LanguagePath jspJavaNullJavadocPath = LanguagePath.get(LanguagePath.get(LanguagePath.get(jspPath, javaLang), nullLang), javadocLang);
//        assertEquals("Invalid mime path for jspLang/javaLang/nullLang/javadocLang", 
//                "text/x-jsp/text/x-java/text/x-javadoc", jspJavaNullJavadocPath.mimePath());

//        LanguagePath nullPath = LanguagePath.get(nullLang);
//        assertEquals("Invalid mime path for nullLang", "", nullPath.mimePath());
        
        // Test endsWith()
        assertTrue(jspPath.endsWith(jspPath));
        assertTrue(jspJavaPath.endsWith(javaPath));
        assertFalse(jspJavaPath.endsWith(jspPath));
        assertFalse(javaPath.endsWith(jspJavaPath));
        assertTrue(jspJavaJavadocPath.endsWith(javaJavadocPath));
        
        assertTrue(jspPath.subPath(0) == jspPath);
        assertTrue(jspJavaPath.subPath(0) == jspJavaPath);
        assertTrue(jspJavaPath.subPath(1) == javaPath);
        assertTrue(jspJavaJavadocPath.subPath(1, 2) == javaPath);
        
        LanguagePath mergedPath = jspPath.embedded(javaPath);
        // Paths should be equal and even same instances
        assertSame(jspJavaPath, mergedPath);
        mergedPath = jspPath.embedded(javaJavadocPath);
        assertSame(jspJavaJavadocPath, mergedPath);
    }
    
    private static enum TestTokenId implements TokenId {
        
        TOKEN_ID1,
        TOKEN_ID2;
        
        private TestTokenId() {
            
        }

        public String primaryCategory() {
            return null;
        }
    } // End of TestTokenId
    
    private static final class Lang extends LanguageHierarchy<TestTokenId> {
        private String mimeType;
        
        public Lang(String mimeType) {
            this.mimeType = mimeType;
        }
        
        protected Lexer<TestTokenId> createLexer(LexerRestartInfo<TestTokenId> info) {
            return null;
        }

        protected Collection<TestTokenId> createTokenIds() {
            return EnumSet.allOf(TestTokenId.class);
        }
        
        public String mimeType() {
            return mimeType;
        }
    } // End of Lang class
}
