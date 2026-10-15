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

package org.netbeans.modules.php.latte.lexer;

import java.io.File;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.php.latte.utils.TestUtils;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteMarkupLexerTest extends LatteLexerTestBase {

    public LatteMarkupLexerTest() {
        super(null);
    }

    public void testIssue2340146_01() throws Exception {
        performTest("testIssue2340146_01");
    }

    public void testIssue2340146_02() throws Exception {
        performTest("testIssue2340146_02");
    }

    public void testIssue2340146_03() throws Exception {
        performTest("testIssue2340146_03");
    }

    public void testIssueGH5862_01() throws Exception {
        performTest("testIssueGH5862_01");
    }

    @Override
    protected String getTestResult(String filename) throws Exception {
        String content = TestUtils.getFileContent(new File(getDataDir(), "testfiles/lexer/markup/" + filename + ".latte-markup"));
        Language<LatteMarkupTokenId> language = LatteMarkupTokenId.language();
        TokenHierarchy<?> hierarchy = TokenHierarchy.create(content, language);
        return createResult(hierarchy.tokenSequence(language));
    }

}
