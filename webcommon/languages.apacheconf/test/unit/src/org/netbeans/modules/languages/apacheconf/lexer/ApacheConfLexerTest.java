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
package org.netbeans.modules.languages.apacheconf.lexer;

import java.io.File;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ApacheConfLexerTest extends ApacheConfLexerTestBase {

    public ApacheConfLexerTest(String name) {
        super(name);
    }

    public void testTest1() throws Exception {
        performTest("test1");
    }

    public void testTest2() throws Exception {
        performTest("test2");
    }

    public void testTest3() throws Exception {
        performTest("test3");
    }

    public void testTest4() throws Exception {
        performTest("test4");
    }

    public void testTest5() throws Exception {
        performTest("test5");
    }

    public void testTest6() throws Exception {
        performTest("test6");
    }

    public void testTest7() throws Exception {
        performTest("test7");
    }

    public void testTest8() throws Exception {
        performTest("test8");
    }

    public void testTest9() throws Exception {
        performTest("test9");
    }

    public void testTest10() throws Exception {
        performTest("test10");
    }

    public void testNegativeFloat() throws Exception {
        performTest("negativeFloat");
    }

    public void testIssue215891() throws Exception {
        performTest("issue215891");
    }

    public void testIssue2236943() throws Exception {
        performTest("issue236943");
    }

    @Override
    protected String getTestResult(String filename) throws Exception {
        String content = ApacheConfLexerUtils.getFileContent(new File(getDataDir(), "testfiles/lexer/" + filename + ".conf"));
        TokenSequence<?> ts = ApacheConfLexerUtils.seqForText(content, ApacheConfTokenId.language());
        return createResult(ts);
    }

    private String createResult(TokenSequence<?> ts) throws Exception {
        StringBuilder result = new StringBuilder();
        while (ts.moveNext()) {
            TokenId tokenId = ts.token().id();
            CharSequence text = ts.token().text();
            result.append("token #");
            result.append(ts.index());
            result.append(" ");
            result.append(tokenId.name());
            result.append(" ");
            result.append(ApacheConfLexerUtils.replaceLinesAndTabs(text.toString()));
            result.append("\n");
        }
        return result.toString();
    }

}
