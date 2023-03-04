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

import java.util.Set;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.lang.TestChangingTokenId;
import org.netbeans.lib.lexer.test.simple.SimpleLanguageProvider;

/**
 * Test of Language class.
 *
 * @author Miloslav Metelka
 */
public class LanguageTest extends NbTestCase {

    public LanguageTest(String name) {
        super(name);
    }
    
    public void testTokenIdsChange() {
        Language<?> lang = Language.find(TestChangingTokenId.MIME_TYPE);
        assertNotNull(lang);
        Set<?> ids = lang.tokenIds();
        assertEquals(1, ids.size());
        assertTrue(ids.contains(TestChangingTokenId.TEXT));
        Set<String> cats = lang.tokenCategories();
        assertTrue(cats.isEmpty());
        
        // Refresh
        TestChangingTokenId.change(); // Calls lang.refresh()
        SimpleLanguageProvider.fireLanguageChange();
        lang = Language.find(TestChangingTokenId.MIME_TYPE);
        assertNotNull(lang);
        
        Set<?> changedIds = lang.tokenIds();
        assertEquals(2, changedIds.size());
        assertTrue(changedIds.contains(TestChangingTokenId.TEXT));
        assertTrue(changedIds.contains(TestChangingTokenId.A));
        Set<String> changedCats = lang.tokenCategories();
        assertTrue(changedCats.contains("test"));
    }

}
