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
package org.netbeans.modules.php.blade.syntax.antlr4.v10;

import java.io.File;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.netbeans.modules.php.blade.editor.BladeUtils;

/**
 *
 * @author bogdan
 */
public class AntlrColoringLexerTest extends AntlrLexerTestBase {

    public AntlrColoringLexerTest(String testName) {
        super(testName);
    }

    public void testRawTag_01() throws Exception {
        performTest("lexer/blade/raw_tag");
    }

    public void testContentTag_01() throws Exception {
        performTest("lexer/blade/content_tag");
    }

    public void testEscapedTag_01() throws Exception {
        performTest("lexer/blade/escaped_tag");
    }

    @Override
    protected String getTestResult(String filename) throws Exception {
        String content = BladeUtils.getFileContent(new File(getDataDir(), "testfiles/" + filename + ".blade.php"));
        CharStream stream = CharStreams.fromString(content);
        BladeAntlrColoringLexer lexer = new BladeAntlrColoringLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();
        return createResult(tokens, lexer.getVocabulary());
    }

    private String createResult(CommonTokenStream tokenStream, Vocabulary vocabulary) throws Exception {
        StringBuilder result = new StringBuilder();
        for (Token token : tokenStream.getTokens()) {
            int tokenId = token.getType();
            String text = token.getText();
            result.append("Token #");
            result.append(tokenId);
            result.append(" ");
            result.append(vocabulary.getDisplayName(tokenId));
            String tokenText = BladeUtils.replaceLinesAndTabs(text);
            if (!tokenText.isEmpty()) {
                result.append(" ");
                result.append("[");
                result.append(token);
                result.append("]");
            }
            result.append("\n");
        }

        return result.toString();
    }
}
