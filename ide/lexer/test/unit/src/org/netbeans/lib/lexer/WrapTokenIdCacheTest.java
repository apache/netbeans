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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.netbeans.api.lexer.Language;
import org.netbeans.lib.lexer.lang.TestLineTokenId;
import org.netbeans.lib.lexer.lang.TestPlainTokenId;
import org.netbeans.lib.lexer.lang.TestStringTokenId;
import org.netbeans.lib.lexer.lang.TestTokenId;

/**
 *
 * @author my
 */
public class WrapTokenIdCacheTest {
    
    public WrapTokenIdCacheTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    static final Language<?> langs[] = new Language<?>[]{
        TestTokenId.language(),
        TestPlainTokenId.language(),
        TestStringTokenId.language(),
        TestLineTokenId.language()
    };

    static int id(Language<?> language) {
        return (language != null) ? LexerApiPackageAccessor.get().languageId(language) : 0;
    }

    /**
     * Test of plainId method, of class WrapTokenIdCache.
     */
    @Test
    public void testIds() {
        Language<TestTokenId> language = TestTokenId.language();
        TestTokenId id = TestTokenId.BLOCK_COMMENT;
        WrapTokenIdCache<TestTokenId> cache = WrapTokenIdCache.get(language);
        assertNotNull(cache);
        WrapTokenId<TestTokenId> plainId = cache.plainWid(id);
        assertNotNull(plainId);
        assertSame(plainId, cache.plainWid(id));
        assertSame(plainId, cache.plainWid(id));

        WrapTokenId<TestTokenId> ndeId = cache.noDefaultEmbeddingWid(id);
        assertNotNull(ndeId);
        assertSame(ndeId, cache.noDefaultEmbeddingWid(id));
        assertSame(ndeId, cache.noDefaultEmbeddingWid(id));
        assertNotSame(ndeId, plainId);

        LanguageIds ids0 = LanguageIds.get(langs[0]);
        LanguageIds ids1 = LanguageIds.get(langs[1]);
        LanguageIds ids2 = LanguageIds.get(langs[2]);
        LanguageIds ids3 = LanguageIds.get(langs[3]);
        LanguageIds ids31 = LanguageIds.get(ids3, langs[1]);
        // Test cache line's four items
        WrapTokenId<TestTokenId> wid0 = cache.wid(id, ids0);
        assertSame(wid0, cache.wid(id, ids0));
        assertSame(wid0, cache.findWid(id, ids0));
        WrapTokenId<TestTokenId> wid1 = cache.wid(id, ids1);
        assertSame(wid1, cache.wid(id, ids1));
        assertSame(wid0, cache.wid(id, ids0));
        WrapTokenId<TestTokenId> wid2 = cache.wid(id, ids2);
        assertSame(wid2, cache.wid(id, ids2));
        WrapTokenId<TestTokenId> wid3 = cache.wid(id, ids3);
        // id1 will go out of the cache
        WrapTokenId<TestTokenId> wid31 = cache.wid(id, ids31);
        WrapTokenId<TestTokenId> wid1C = cache.wid(id, ids1);
        assertNotSame(wid1, wid1C);
    }

}
