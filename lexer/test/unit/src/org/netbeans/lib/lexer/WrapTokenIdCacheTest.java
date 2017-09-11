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
