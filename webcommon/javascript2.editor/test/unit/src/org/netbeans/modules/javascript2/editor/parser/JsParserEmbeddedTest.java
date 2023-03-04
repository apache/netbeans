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
package org.netbeans.modules.javascript2.editor.parser;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.html.editor.xhtml.XhtmlElTokenId;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public class JsParserEmbeddedTest extends JsTestBase {

    public JsParserEmbeddedTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestLanguageProvider.register(JsTokenId.javascriptLanguage());
        TestLanguageProvider.register(HTMLTokenId.language());
        TestLanguageProvider.register(XhtmlElTokenId.language());
    }

    public void testEmbeddedSimple1() throws Exception {
        parse("testfiles/parser/embeddedSimple1.xhtml", Collections.<String>emptyList());
    }

//    public void testEmbeddedSimple2() throws Exception {
//        parse("testfiles/parser/embeddedSimple2.xhtml",
//                Collections.singletonList("Expected an operand but found error"));
//    }

    public void testEmbeddedSimple3() throws Exception {
        parse("testfiles/parser/embeddedSimple3.html",
                Collections.singletonList("Expected } but found eof"));
    }

    public void testEmbeddedSimple4() throws Exception {
        parse("testfiles/parser/embeddedSimple4.html",
                Collections.singletonList("Expected } but found eof"));
    }

    private void parse(String file, final List<String> errorStarts) throws Exception {
        FileObject f = getTestFile(file);
        Source source = Source.create(f);

        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                JsParserResult jspr = getJsParserResult(resultIterator);

                assertNotNull(jspr);
                List<? extends org.netbeans.modules.csl.api.Error> parserErrors =
                        jspr.getDiagnostics();
                assertEquals(errorStarts.size(), parserErrors.size());
                for (int i = 0; i < errorStarts.size(); i++) {
                    if (!parserErrors.get(i).getDisplayName().startsWith(errorStarts.get(i))) {
                        fail("Error was expected to start with: " + errorStarts.get(i) + " but was: "
                                + parserErrors.get(i).getDisplayName());
                    }
                }
            }
        });
    }

    private static JsParserResult getJsParserResult(ResultIterator resultIterator) throws ParseException {
        Parser.Result r = resultIterator.getParserResult();
        if (r instanceof JsParserResult) {
            return (JsParserResult) r;
        } else {
            for (Embedding embedding : resultIterator.getEmbeddings()) {
                ResultIterator embeddingIterator = resultIterator.getResultIterator(embedding);
                return getJsParserResult(embeddingIterator);
            }
        }
        return null;
    }
}
