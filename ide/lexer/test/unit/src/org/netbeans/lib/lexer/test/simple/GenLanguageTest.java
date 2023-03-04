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

package org.netbeans.lib.lexer.test.simple;

import org.netbeans.lib.lexer.lang.TestGenLanguage;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;


/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class GenLanguageTest extends TestCase {

    private static final int IDS_SIZE = 13;

    public GenLanguageTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testTokenIds() {
        // Check that token ids are all present and correctly ordered
        Language<TokenId> language  = TestGenLanguage.language();
        Set<TokenId> ids = language.tokenIds();
        assertTrue("Invalid ids.size() - expected " + IDS_SIZE, ids.size() == IDS_SIZE);
        
        TokenId[] idArray = {
            TestGenLanguage.IDENTIFIER_ID,TestGenLanguage.PLUS_ID,TestGenLanguage.MINUS_ID,TestGenLanguage.PLUS_MINUS_PLUS_ID,TestGenLanguage.SLASH_ID,TestGenLanguage.STAR_ID,TestGenLanguage.ML_COMMENT_ID,TestGenLanguage.WHITESPACE_ID,TestGenLanguage.SL_COMMENT_ID,TestGenLanguage.ERROR_ID,TestGenLanguage.PUBLIC_ID,TestGenLanguage.PRIVATE_ID,TestGenLanguage.STATIC_ID,
        };

        // Check operations with ids
        Collection<TokenId> testIds = Arrays.asList(idArray);
        LexerTestUtilities.assertCollectionsEqual("Ids do not match with test ones",
                ids, testIds);
        
        // Check that ids.iterator() is ordered by ordinal
        int ind = 0;
        for (TokenId id : ids) {
            assertTrue("Token ids not sorted by ordinal at index=" + ind, id == idArray[ind]);
            ind++;
            assertSame(language.tokenId(id.name()), id);
            assertSame(language.tokenId(id.ordinal()), id);
            assertSame(language.validTokenId(id.name()), id);
            assertSame(language.validTokenId(id.ordinal()), id);
        }
        
        try {
            language.validTokenId("invalid-name");
            fail("Error: exception not thrown");
        } catch (IllegalArgumentException e) {
            // OK
        }
        
        try {
            language.validTokenId(-1);
            fail("Error: exception not thrown");
        } catch (IndexOutOfBoundsException e) {
            // OK
        }
        
        try {
            language.validTokenId(20);
            fail("Error: exception not thrown");
        } catch (IndexOutOfBoundsException e) {
            // OK
        }
        
        assertEquals(15, language.maxOrdinal());
        
        // Check token categories
        Set cats = language.tokenCategories();
        Collection testCats = Arrays.asList(new String[] { "operator", "test-category", "whitespace", "error", "comment", "keyword" });
        LexerTestUtilities.assertCollectionsEqual("Invalid token categories",
                cats, testCats);
        
        LexerTestUtilities.assertCollectionsEqual(
                Arrays.asList(new TokenId[] {
                    TestGenLanguage.PLUS_ID,TestGenLanguage.MINUS_ID,TestGenLanguage.PLUS_MINUS_PLUS_ID,TestGenLanguage.STAR_ID,TestGenLanguage.SLASH_ID,
                }),
                language.tokenCategoryMembers("operator")
        
        );
        
        LexerTestUtilities.assertCollectionsEqual(
                Arrays.asList(new TokenId[] {
                    TestGenLanguage.PLUS_ID,TestGenLanguage.MINUS_ID,TestGenLanguage.IDENTIFIER_ID,
                }),
                language.tokenCategoryMembers("test-category")
        
        );

        LexerTestUtilities.assertCollectionsEqual(
                Arrays.asList(new TokenId[] {
                    TestGenLanguage.WHITESPACE_ID,
                }),
                language.tokenCategoryMembers("whitespace")
        
        );

        LexerTestUtilities.assertCollectionsEqual(
                Arrays.asList(new TokenId[] {
                    TestGenLanguage.ERROR_ID,
                }),
                language.tokenCategoryMembers("error")
        
        );

        LexerTestUtilities.assertCollectionsEqual(
                Arrays.asList(new TokenId[] {
                    TestGenLanguage.ML_COMMENT_ID,TestGenLanguage.SL_COMMENT_ID,
                }),
                language.tokenCategoryMembers("comment")
        
        );
                
        List<String> testIdCats
            = language.tokenCategories(TestGenLanguage.IDENTIFIER_ID);
        LexerTestUtilities.assertCollectionsEqual(
                Arrays.asList(new String[] {
                    "test-category",
                }),
                testIdCats
        );

        List<String> testIdCats2
            = language.tokenCategories(TestGenLanguage.PLUS_ID);
        LexerTestUtilities.assertCollectionsEqual(
                Arrays.asList(new String[] {
                    "test-category",
                    "operator",
                }),
                testIdCats2
        );


        // Check Language.merge()
        Collection<TokenId> mergedIds
                = language.merge(
                    Arrays.asList(new TokenId[] { TestGenLanguage.IDENTIFIER_ID }),
                    language.merge(language.tokenCategoryMembers("comment"),
                        language.tokenCategoryMembers("error"))
                
                );
        LexerTestUtilities.assertCollectionsEqual("Invalid language.merge()",Arrays.asList(new TokenId[] {
                    TestGenLanguage.ML_COMMENT_ID,TestGenLanguage.SL_COMMENT_ID,TestGenLanguage.ERROR_ID,TestGenLanguage.IDENTIFIER_ID,
                }),
                mergedIds
        
        );

    }

}
