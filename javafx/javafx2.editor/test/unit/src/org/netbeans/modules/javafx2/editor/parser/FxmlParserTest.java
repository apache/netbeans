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
package org.netbeans.modules.javafx2.editor.parser;

import java.util.Collection;
import java.util.Collections;
import junit.framework.TestSuite;
import org.netbeans.modules.javafx2.editor.ErrorMark;
import org.netbeans.modules.javafx2.editor.GoldenFileTestBase;
import org.netbeans.modules.javafx2.editor.sax.XmlLexerParser;
import org.netbeans.modules.javafx2.editor.completion.model.FxmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author sdedic
 */
public class FxmlParserTest extends GoldenFileTestBase {
    private Collection<ErrorMark>   problems;

    public FxmlParserTest(String testName) {
        super(testName);
    }
    
    public void testParserInvocation() throws Exception {
        Source fxmlSource = Source.create(document);
        
        ParserManager.parse(Collections.singleton(fxmlSource), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                
                if (result instanceof FxmlParserResult) {
                    FxmlParserResult fxResult = (FxmlParserResult)result;
                    appendParsedTree(fxResult, report);
                    appendErrors(fxResult, report);
                    assertContents(report);
                }
            }
        });
    }
    
    public static TestSuite suite() {
        TestSuite s = new TestSuite();
        s.addTest(new FxmlParserTest("testUnresolvedThings"));
        s.addTest(new FxmlParserTest("testDefinitionsResolved"));
        s.addTest(new FxmlParserTest("testInlineScript"));
        return s;
    }
    
    public void testDefinitionsResolved() throws Exception {
        Source fxmlSource = Source.create(document);
        
        ParserManager.parse(Collections.singleton(fxmlSource), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                
                FxmlParserResult fxResult = (FxmlParserResult)result;
                appendParsedTree(fxResult, report);
                appendErrors(fxResult, report);
                assertContents(report);
            }
        });
    }
    
    public void testInlineScript() throws Exception {
        Source fxmlSource = Source.create(document);
        
        ParserManager.parse(Collections.singleton(fxmlSource), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                
                FxmlParserResult fxResult = (FxmlParserResult)result;
                appendParsedTree(fxResult, report);
                appendErrors(fxResult, report);
                assertContents(report);
            }
        });
    }

    public void testUnresolvedThings() throws Exception {
        Source fxmlSource = Source.create(document);
        
        ParserManager.parse(Collections.singleton(fxmlSource), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                
                FxmlParserResult fxResult = (FxmlParserResult)result;
                StringBuilder sb = new StringBuilder();
                appendErrors(fxResult, sb);
                assertContents(sb);
            }
        });
    }

    private FxModelBuilder builder;
    
    private void defaultTestContents() throws Exception {
        XmlLexerParser parser = new XmlLexerParser(hierarchy);
        builder = new FxModelBuilder();
        parser.setContentHandler(builder);
        parser.parse();

        StringBuilder sb = report;
        sb.append("\n\n");
        for (ErrorMark em : builder.getErrors()) {
            sb.append(em).append("\n");
        }
        
        assertContents(sb);
    }
}
