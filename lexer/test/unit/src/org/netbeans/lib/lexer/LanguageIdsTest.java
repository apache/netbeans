/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.lib.lexer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.lexer.Language;
import org.netbeans.lib.lexer.lang.TestLineTokenId;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.lib.lexer.lang.TestStringTokenId;
import org.netbeans.lib.lexer.lang.TestTokenId;

/**
 *
 * @author Miloslav Metelka
 */
public class LanguageIdsTest {
    
    public LanguageIdsTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    static final Language<?> langs[] = new Language<?>[] {
            TestTokenId.language(),
            TestPlainTokenId.language(),
            TestStringTokenId.language(),
            TestLineTokenId.language()
    };
    
    static int id(Language<?> language) {
        return (language != null) ? LexerApiPackageAccessor.get().languageId(language) : 0;
    }
    
    static void assertContains(LanguageIds ids, Language<?>... containedLangs) {
        for (int i = 0; i < langs.length; i++) {
            Language<?> lang = langs[i];
            boolean expected = false;
            for (int j = containedLangs.length - 1; !expected && j >= 0; j--) {
                expected |= (lang == containedLangs[j]);
            }
            assertEquals("Lang-index=" + i, expected, ids.containsLanguage(lang));
        }

        // Test by ids
        int maxId = id(langs[langs.length - 1]);
        for (int i = 0; i <= maxId; i++) {
            boolean expected = false;
            for (int j = containedLangs.length - 1; !expected && j >= 0; j--) {
                expected |= (id(containedLangs[j]) == i);
            }
            assertEquals("Lang-id=" + i, expected, ids.containsId(i));
        }
    }

    @Test
    public void testIds() {
        assertEquals(1, id(langs[0]));
        assertEquals(2, id(langs[1])); // Contains several more langs
        assertEquals(6, id(langs[2]));
        assertEquals(7, id(langs[3]));
        
    }

    @Test
    public void testGet() {
        LanguageIds idsNull = LanguageIds.get(null);
        assertContains(idsNull, (Language<?>)null);
        assertSame(LanguageIds.NULL_LANGUAGE_ONLY, idsNull);
        LanguageIds idsNullFromEmpty = LanguageIds.get(LanguageIds.EMPTY, null);
        assertSame(idsNull, idsNullFromEmpty);
        
        LanguageIds ids0 = LanguageIds.get(langs[0]);
        assertContains(ids0, langs[0]);
        
        LanguageIds ids2 = LanguageIds.get(langs[2]);
        assertContains(ids2, langs[2]);

        LanguageIds ids1 = LanguageIds.get(langs[1]);
        assertContains(ids1, langs[1]);
        
        assertEquals(ids0, LanguageIds.get(langs[0]));
        assertEquals(ids1, LanguageIds.get(langs[1]));
        assertEquals(ids2, LanguageIds.get(langs[2]));
        
        LanguageIds ids03 = LanguageIds.get(ids0, langs[3]);
        assertContains(ids03, langs[0], langs[3]);
        LanguageIds ids23 = LanguageIds.get(ids2, langs[3]);
        assertContains(ids23, langs[2], langs[3]);
        LanguageIds ids20 = LanguageIds.get(ids2, langs[0]);
        assertContains(ids20, langs[0], langs[2]);
        LanguageIds ids1Null = LanguageIds.get(ids1, null);
        assertContains(ids1Null, null, langs[1]);
        LanguageIds ids1Null2 = LanguageIds.get(ids1Null, langs[2]);
        assertContains(ids1Null2, null, langs[1], langs[2]);
        
        LanguageIds ids3 = LanguageIds.get(langs[3]);
        LanguageIds ids32 = LanguageIds.get(ids3, langs[2]);
        assertContains(ids32, langs[2], langs[3]);
        LanguageIds ids321 = LanguageIds.get(ids32, langs[1]);
        assertContains(ids321, langs[1], langs[2], langs[3]);
        LanguageIds ids3210 = LanguageIds.get(ids321, langs[0]);
        assertContains(ids3210, langs[0], langs[1], langs[2], langs[3]);
        LanguageIds ids3210Null = LanguageIds.get(ids3210, null);
        assertContains(ids3210Null, null, langs[0], langs[1], langs[2], langs[3]);

        LanguageIds empty = LanguageIds.getRemoved(ids3, langs[3]);
        assertEquals(LanguageIds.EMPTY, empty);
        LanguageIds ids321R3 = LanguageIds.getRemoved(ids321, langs[3]);
        LanguageIds ids321R2 = LanguageIds.getRemoved(ids321, langs[2]);
        LanguageIds ids321R1 = LanguageIds.getRemoved(ids321, langs[1]);
        assertEquals(ids32, ids321R1);
        
    }

}
