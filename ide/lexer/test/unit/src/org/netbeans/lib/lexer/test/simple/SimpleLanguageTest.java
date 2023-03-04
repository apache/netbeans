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

import org.netbeans.lib.lexer.lang.TestTokenId;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
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
public class SimpleLanguageTest extends TestCase {

    private static final int IDS_SIZE = 18;

    public SimpleLanguageTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testTokenIds() {
        // Check that token ids are all present and correctly ordered
        Language<TestTokenId> language  = TestTokenId.language();
        Set ids = language.tokenIds();
        assertEquals("Invalid ids.size()", IDS_SIZE, ids.size());
        
        TokenId[] idArray = {
            TestTokenId.IDENTIFIER,TestTokenId.PLUS,TestTokenId.MINUS,TestTokenId.PLUS_MINUS_PLUS,TestTokenId.DIV,TestTokenId.STAR,TestTokenId.BLOCK_COMMENT,TestTokenId.BLOCK_COMMENT_INCOMPLETE,TestTokenId.WHITESPACE,TestTokenId.LINE_COMMENT,TestTokenId.ERROR,TestTokenId.PUBLIC,TestTokenId.PRIVATE,TestTokenId.STATIC,TestTokenId.JAVADOC_COMMENT,TestTokenId.JAVADOC_COMMENT_INCOMPLETE,TestTokenId.STRING_LITERAL,TestTokenId.STRING_LITERAL_INCOMPLETE
            
        };

        // Check operations with ids
        Collection testIds = Arrays.asList(idArray);
        LexerTestUtilities.assertCollectionsEqual("Ids do not match with test ones",
                ids, testIds);
        
        // Check that ids.iterator() is ordered by ordinal
        int ind = 0;
        int lastOrdinal = -1;
        for (Iterator it = ids.iterator(); it.hasNext();) {
            TokenId id = (TokenId) it.next();
            if (id.ordinal() == lastOrdinal) {
                fail("Duplicate ordinal for " + id);
            }
            if (id.ordinal() <= lastOrdinal) {
                fail("Token ids not sorted by ordinal: " + id);
            }
            lastOrdinal = id.ordinal();
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
        
        assertEquals(17, language.maxOrdinal());
        
        // Check token categories
        Set cats = language.tokenCategories();
        Collection testCats = Arrays.asList(new String[] {
            "operator", "test-category", "whitespace",
            "incomplete", "error", "comment", "keyword", "string"
        });
        LexerTestUtilities.assertCollectionsEqual("Invalid token categories",
                cats, testCats);
        
        LexerTestUtilities.assertCollectionsEqual(language.tokenCategoryMembers("operator"),Arrays.asList(new TokenId[] {
                    TestTokenId.PLUS,TestTokenId.MINUS,TestTokenId.PLUS_MINUS_PLUS,TestTokenId.STAR,TestTokenId.DIV,
                })
        );
        
        LexerTestUtilities.assertCollectionsEqual(language.tokenCategoryMembers("test-category"),Arrays.asList(new TokenId[] {
                    TestTokenId.PLUS,TestTokenId.MINUS,TestTokenId.IDENTIFIER,
                })
        );

        LexerTestUtilities.assertCollectionsEqual(language.tokenCategoryMembers("whitespace"),Arrays.asList(new TokenId[] {
                    TestTokenId.WHITESPACE,
                })
        );

        LexerTestUtilities.assertCollectionsEqual(language.tokenCategoryMembers("error"),Arrays.asList(new TokenId[] {
                    TestTokenId.ERROR,TestTokenId.BLOCK_COMMENT_INCOMPLETE,TestTokenId.JAVADOC_COMMENT_INCOMPLETE,TestTokenId.STRING_LITERAL_INCOMPLETE,
                })
        );

        LexerTestUtilities.assertCollectionsEqual(language.tokenCategoryMembers("comment"),Arrays.asList(new TokenId[] {
                    TestTokenId.LINE_COMMENT,TestTokenId.BLOCK_COMMENT,TestTokenId.JAVADOC_COMMENT,TestTokenId.BLOCK_COMMENT_INCOMPLETE,TestTokenId.JAVADOC_COMMENT_INCOMPLETE
                })
        );
                
        LexerTestUtilities.assertCollectionsEqual(language.tokenCategories(TestTokenId.IDENTIFIER),
                Arrays.asList(new String[] {
                    "test-category",
                })
        
        );

        LexerTestUtilities.assertCollectionsEqual(language.tokenCategories(TestTokenId.PLUS),
                Arrays.asList(new String[] {
                    "test-category",
                    "operator",
                })
        
        );

        LexerTestUtilities.assertCollectionsEqual(language.tokenCategories(TestTokenId.BLOCK_COMMENT_INCOMPLETE),
                Arrays.asList(new String[] {
                    "error",
                    "incomplete",
                    "comment",
                })
        
        );

        // Check indexedIds()
        LexerTestUtilities.assertCollectionsEqual("Invalid language.indexedIds()",language.merge(java.util.EnumSet.of(TestTokenId.IDENTIFIER),language.merge(language.tokenCategoryMembers("comment"), language.tokenCategoryMembers("error"))),java.util.Arrays.asList(new org.netbeans.api.lexer.TokenId[]{TestTokenId.LINE_COMMENT, TestTokenId.BLOCK_COMMENT, TestTokenId.JAVADOC_COMMENT, TestTokenId.BLOCK_COMMENT_INCOMPLETE, TestTokenId.JAVADOC_COMMENT_INCOMPLETE, TestTokenId.STRING_LITERAL_INCOMPLETE, TestTokenId.ERROR, TestTokenId.IDENTIFIER})
        );

    }

}
