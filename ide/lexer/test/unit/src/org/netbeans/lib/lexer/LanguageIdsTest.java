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
