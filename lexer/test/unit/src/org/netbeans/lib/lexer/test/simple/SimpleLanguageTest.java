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
