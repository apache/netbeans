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
public class LatteTopLexerTest extends LatteLexerTestBase {

    public LatteTopLexerTest(String testName) {
        super(testName);
    }

    public void testSyntaxLatte() throws Exception {
        performTest("syntax-latte");
    }

    public void testSyntaxDouble() throws Exception {
        performTest("syntax-double");
    }

    public void testSyntaxAsp() throws Exception {
        performTest("syntax-asp");
    }

    public void testSyntaxPython() throws Exception {
        performTest("syntax-python");
    }

    public void testSyntaxSensitiveComment() throws Exception {
        performTest("syntax-sensitive-comment");
    }

    public void testSyntaxOff() throws Exception {
        performTest("syntax-off");
    }

    public void testSyntaxDoubleInCurly() throws Exception {
        performTest("syntax-double-in-curly");
    }

    public void testSyntaxLatteInCurly() throws Exception {
        performTest("syntax-latte-in-curly");
    }

    public void testNHrefDouble() throws Exception {
        performTest("n-href-double");
    }

    public void testNHrefSingle() throws Exception {
        performTest("n-href-single");
    }

    public void testEscapedQuotes() throws Exception {
        performTest("escaped-quotes");
    }

    public void testVariable() throws Exception {
        performTest("variable");
    }

    public void testWsAfterOpeningDelim() throws Exception {
        performTest("ws-after-opening-delim");
    }

    public void testNMultipleInOneElement() throws Exception {
        performTest("n-multiple-in-one-element");
    }

    public void testCommentWithAsterisk() throws Exception {
        performTest("comment-with-asterisk");
    }

    public void testNSyntax() throws Exception {
        performTest("n-syntax");
    }

    public void testWsNotError() throws Exception {
        performTest("ws-not-error");
    }

    public void testSingleCurlyOpenError() throws Exception {
        performTest("single-curly-open-error");
    }

    public void testIssue214777() throws Exception {
        performTest("testIssue214777");
    }

    public void testIndentIssue() throws Exception {
        performTest("indent-issue");
    }

    public void testIssue230530() throws Exception {
        performTest("testIssue230530");
    }

    public void testIssue231352() throws Exception {
        // Check in REAL file before golden file regeneration for CSS errors!
        performTest("testIssue231352");
    }

    public void testIssue231475_NoSpaceAtCssTokenStart() throws Exception {
        // Check in REAL file before golden file regeneration for CSS errors!
        performTest("testIssue231475_NoSpaceAtCssTokenStart");
    }

    public void testIssue2340146_01() throws Exception {
        performTest("testIssue240146_01");
    }

    public void testIssue2340146_02() throws Exception {
        performTest("testIssue240146_02");
    }

    public void testIssue246488() throws Exception {
        performTest("testIssue246488");
    }

    public void testIssueGH5862_01() throws Exception {
        performTest("testIssueGH5862_01");
    }

    @Override
    protected String getTestResult(String filename) throws Exception {
        String content = TestUtils.getFileContent(new File(getDataDir(), "testfiles/lexer/top/" + filename + ".latte"));
        Language<LatteTopTokenId> language = LatteTopTokenId.language();
        TokenHierarchy<?> hierarchy = TokenHierarchy.create(content, language);
        return createResult(hierarchy.tokenSequence(language));
    }

}
