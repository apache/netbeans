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

import org.netbeans.lib.lexer.lang.TestHTMLTagTokenId;
import org.netbeans.lib.lexer.lang.TestJavadocLexer;
import org.netbeans.lib.lexer.test.simple.*;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
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
 * Token ids for simple javadoc language 
 * - copied from JavadocTokenId.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */
public enum TestJavadocTokenId implements TokenId {

    IDENT("comment"),
    TAG("javadoc-tag"),
    HTML_TAG("html-tag"),
    DOT("comment"),
    HASH("comment"),
    OTHER_TEXT("comment");

    private final String primaryCategory;

    TestJavadocTokenId() {
        this(null);
    }

    TestJavadocTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<TestJavadocTokenId> language
    = new LanguageHierarchy<TestJavadocTokenId>() {
        @Override
        protected Collection<TestJavadocTokenId> createTokenIds() {
            return EnumSet.allOf(TestJavadocTokenId.class);
        }
        
        @Override
        protected Map<String,Collection<TestJavadocTokenId>> createTokenCategories() {
            return null; // no extra categories
        }

        @Override
        protected Lexer<TestJavadocTokenId> createLexer(LexerRestartInfo<TestJavadocTokenId> info) {
            return new TestJavadocLexer(info);
        }

        @Override
        protected LanguageEmbedding<?> embedding(
        Token<TestJavadocTokenId> token, LanguagePath languagePath, InputAttributes inputAttributes) {
            switch (token.id()) {
                case HTML_TAG:
                    return LanguageEmbedding.create(TestHTMLTagTokenId.language(), 0, 0);
            }
            return null; // No embedding
        }

        @Override
        protected String mimeType() {
            return "text/x-javadoc";
        }
    }.language();

    public static Language<TestJavadocTokenId> language() {
        return language;
    }

}
