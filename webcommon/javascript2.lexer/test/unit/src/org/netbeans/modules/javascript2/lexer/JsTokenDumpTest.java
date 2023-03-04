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

package org.netbeans.modules.javascript2.lexer;

import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;

/**
 * Test tokens dump of JavaScript code input. Based on the Ruby one.
 */
public class JsTokenDumpTest extends NbTestCase {

    public JsTokenDumpTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    public void testTestInput() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/testInput.js",
                JsTokenId.javascriptLanguage());
    }

    public void testScriptInput() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/scriptInput.js",
                JsTokenId.javascriptLanguage());
    }

    public void testScriptInputBroken() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/scriptInputBroken.js",
                JsTokenId.javascriptLanguage());
    }

    public void testImportExport01() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/importExport01.js",
                JsTokenId.javascriptLanguage());
    }

    public void testComments() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/comments.js",
                JsTokenId.javascriptLanguage());
    }

    public void testKeywordsAsIdent() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/keywordsAsIdent.js",
                JsTokenId.javascriptLanguage());
    }

    public void testRegularExpression() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/regularExpression.js",
                JsTokenId.javascriptLanguage());
    }

    public void testTemplates() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/templates.js",
                JsTokenId.javascriptLanguage());
    }

    public void testSpread() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/spread.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testExponentiation() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/exponentiation.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testIssue262621() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/issue262621.js",
                JsTokenId.javascriptLanguage());
    }

    public void testPrivateMembers() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/privateClassMembers.js",
                JsTokenId.javascriptLanguage());
    }

    public void testOperators() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/operators.js",
                JsTokenId.javascriptLanguage());
    }

    public void testNumbers() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/numbers.js",
                JsTokenId.javascriptLanguage());
    }
}
