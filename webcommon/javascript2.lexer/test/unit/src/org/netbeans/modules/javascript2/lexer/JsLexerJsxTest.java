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
import org.netbeans.lib.lexer.test.dump.TokenDumpCheck;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;

/**
 *
 * @author Petr Pisl
 */
public class JsLexerJsxTest extends NbTestCase {
    
    public JsLexerJsxTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }
    
    public void testSimple01() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/simple01.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testSimple02() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/simple02.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testSimple03() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/simple03.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testSimple04() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/simple04.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testSimple05() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/simple05.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testInner01() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/inner01.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testTemplates01() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/templates01.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testIssue267422() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/issue267422.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testIssue268900() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/issue268900.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testStyleJSX() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/styleInJSXissue.js",
                JsTokenId.javascriptLanguage());
    }
    
    public void testIncLess() throws Exception {
        LexerTestUtilities.checkTokenDump(this, "testfiles/jsx/incLess.js",
                JsTokenId.javascriptLanguage());
    }
}
