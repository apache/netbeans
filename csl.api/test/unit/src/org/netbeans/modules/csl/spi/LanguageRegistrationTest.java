/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
