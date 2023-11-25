/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.java.lexer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;


/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class JavaLanguageTest extends TestCase {

    private static final int IDS_SIZE = 10;
    
    public JavaLanguageTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testTokenIds() {
        // Check that token ids are all present and correctly ordered
        Language language = JavaTokenId.language();

        // Check token categories
        Set testCats = language.tokenCategories();
        Collection cats = Arrays.asList(new String[] {
            "error", "identifier", "operator", "separator", "whitespace", "error", "comment",
            "keyword", "string", "character", "number", "literal", "annotation-separator", "keyword-directive"
        });
        LexerTestUtilities.assertCollectionsEqual("Invalid categories", cats, testCats);
        
        LexerTestUtilities.assertCollectionsEqual("Invalid category members",
                language.tokenCategoryMembers("whitespace"),
                Arrays.asList(new TokenId[] {
                    JavaTokenId.WHITESPACE,
                })
        );
        
                
    }

}
