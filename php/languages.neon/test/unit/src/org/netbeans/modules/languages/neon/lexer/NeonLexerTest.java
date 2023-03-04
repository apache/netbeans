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
package org.netbeans.modules.languages.neon.lexer;

import java.io.File;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class NeonLexerTest extends NeonLexerTestBase {

    public NeonLexerTest(String name) {
        super(name);
    }

    public void testAutowiring() throws Exception {
        performTest("autowiring");
    }

    public void testBasic() throws Exception {
        performTest("basic");
    }

    public void testChild() throws Exception {
        performTest("child");
    }

    public void testExtension() throws Exception {
        performTest("extension");
    }

    public void testFactory() throws Exception {
        performTest("factory");
    }

    public void testIncludes() throws Exception {
        performTest("includes");
    }

    public void testInheritance1() throws Exception {
        performTest("inheritance1");
    }

    public void testInheritance2() throws Exception {
        performTest("inheritance2");
    }

    public void testNonshared() throws Exception {
        performTest("nonshared");
    }

    public void testSample() throws Exception {
        performTest("sample");
    }

    public void testScalar1() throws Exception {
        performTest("scalar1");
    }

    public void testServicesCircular() throws Exception {
        performTest("servicesCircular");
    }

    public void testSetup() throws Exception {
        performTest("setup");
    }

    public void testIssue206378() throws Exception {
        performTest("issue206378");
    }

    public void testPipes() throws Exception {
        performTest("pipes");
    }

    public void testReferenceByClass() throws Exception {
        performTest("referenceByClass");
    }

    public void testCloseBracket() throws Exception {
        performTest("closeBracket");
    }

    public void testNotAndSelfKeyword() throws Exception {
        performTest("notAndSelfKeyword");
    }

    public void testMultilineValues() throws Exception {
        performTest("multilineValues");
    }

    public void testIssue209549() throws Exception {
        performTest("issue209549");
    }

    public void testIssue208274() throws Exception {
        performTest("issue208274");
    }

    public void testIssue210049_01() throws Exception {
        performTest("issue210049_01");
    }

    public void testIssue210049_02() throws Exception {
        performTest("issue210049_02");
    }

    public void testIssue210049_03() throws Exception {
        performTest("issue210049_03");
    }

    public void testHashInCommonUse() throws Exception {
        performTest("hashInCommonUse");
    }

    public void testIssue224830() throws Exception {
        performTest("issue224830");
    }

    public void testIssue224847() throws Exception {
        performTest("issue224847");
    }

    public void testIssue224850_01() throws Exception {
        performTest("issue224850_01");
    }

    public void testIssue224850_02() throws Exception {
        performTest("issue224850_02");
    }

    public void testIssue224850_03() throws Exception {
        performTest("issue224850_03");
    }

    public void testIssue229449() throws Exception {
        performTest("issue229449");
    }

    public void testIssue238224() throws Exception {
        performTest("issue238224");
    }

    public void testIssue246906() throws Exception {
        performTest("issue246906");
    }

    @Override
    protected String getTestResult(String filename) throws Exception {
        String content = NeonLexerUtils.getFileContent(new File(getDataDir(), "testfiles/lexer/" + filename + ".neon"));
        TokenSequence<?> ts = NeonLexerUtils.seqForText(content, NeonTokenId.language());
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
            result.append(NeonLexerUtils.replaceLinesAndTabs(text.toString()));
            result.append("\n");
        }
        return result.toString();
    }

}
