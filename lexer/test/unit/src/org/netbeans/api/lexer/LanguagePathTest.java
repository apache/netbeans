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
