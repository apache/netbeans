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

package org.netbeans.lib.lexer.test.dump;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class TokenDumpTest extends NbTestCase {

    public TokenDumpTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testTokenDump() throws Exception {
        // The testfiles/TokenDumpTestFile.txt in the code below should be located under
        // $module/test/unit/data directory e.g. $cvs/lexer/test/unit/data/testfiles/TokenDumpTestFile.txt
        // ("Files" view by Ctrl+2 can be used
        // to locate/create appropriate dirs/files).
        // The TokenDumpTestFile.txt.tokens.txt will be created by the test
        // if it does not exist (see also javadoc of LexerTestUtilities.checkTokenDump()).
        LexerTestUtilities.checkTokenDump(this,
                "testfiles/TokenDumpTestFile.txt",
                TextAsSingleTokenTokenId.language());
    }

}
