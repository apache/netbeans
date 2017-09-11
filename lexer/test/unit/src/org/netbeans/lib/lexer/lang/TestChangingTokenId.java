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
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Language that changes dynamically to test Language.refresh().
 *
 * @author Miloslav Metelka
 */
public enum TestChangingTokenId implements TokenId {
    
    TEXT, // any text
    A; // "a"; added after change only

    private final String primaryCategory;

    TestChangingTokenId() {
        this(null);
    }

    TestChangingTokenId(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public String primaryCategory() {
        return primaryCategory;
    }
    
    private static boolean changed;

    private static Language<TestChangingTokenId> createLanguage() {
        return new LanguageHierarchy<TestChangingTokenId>() {
            @Override
            protected Collection<TestChangingTokenId> createTokenIds() {
                return changed
                        ? EnumSet.allOf(TestChangingTokenId.class)
                        : EnumSet.of(TEXT);
            }

            @Override
            protected Map<String,Collection<TestChangingTokenId>> createTokenCategories() {
                Map<String,Collection<TestChangingTokenId>> cats = null;
                if (changed) {
                    cats = new HashMap<String,Collection<TestChangingTokenId>>();
                    cats.put("test",EnumSet.of(A));
                }
                return cats;
            }

            @Override
            protected Lexer<TestChangingTokenId> createLexer(LexerRestartInfo<TestChangingTokenId> info) {
                return null;
            }

            @Override
            protected String mimeType() {
                return MIME_TYPE;
            }
        }.language();
    }

    private static Language<TestChangingTokenId> language;

    public static Language<TestChangingTokenId> language() {
        if (language == null)
            language = createLanguage();
        return language;
    }
    
    public static void change() {
        changed = true;
        language = null;
    }

    public static final String MIME_TYPE = "text/x-changing";
    
}
