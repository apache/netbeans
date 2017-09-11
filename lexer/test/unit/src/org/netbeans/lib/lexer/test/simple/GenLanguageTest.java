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
