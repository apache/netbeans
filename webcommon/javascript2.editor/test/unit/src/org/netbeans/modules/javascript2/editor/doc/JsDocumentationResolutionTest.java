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
package org.netbeans.modules.javascript2.editor.doc;

import java.util.Collections;
import org.netbeans.modules.javascript2.doc.api.JsDocumentationSupport;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocumentationResolutionTest extends JsTestBase {

    JsParserResult parserResult;

    public JsDocumentationResolutionTest(String testName) {
        super(testName);
    }

    private void parseSource(Source source) throws ParseException {
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                assertTrue(result instanceof JsParserResult);
                parserResult = (JsParserResult) result;
            }
        });
    }

    public void testResolverForJsDocCompleteFile() throws ParseException {
        parseSource(getTestSource(getTestFile("../../../testfiles/jsdoc-testfiles/classWithJsDoc.js")));
        JsDocumentationHolder documentationHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);
        assertEquals("org.netbeans.modules.javascript2.jsdoc.JsDocDocumentationHolder", documentationHolder.getClass().getName());
    }

    public void testResolverForSDocCompleteFile() throws ParseException {
        parseSource(getTestSource(getTestFile("../../../testfiles/jsdoc-testfiles/classWithSDoc.js")));
        JsDocumentationHolder documentationHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);
        assertEquals("org.netbeans.modules.javascript2.sdoc.SDocDocumentationHolder", documentationHolder.getClass().getName());
    }

    public void testResolverForExtDocCompleteFile() throws ParseException {
        parseSource(getTestSource(getTestFile("../../../testfiles/jsdoc-testfiles/classWithExtDoc.js")));
        JsDocumentationHolder documentationHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);
        assertEquals("org.netbeans.modules.javascript2.extdoc.ExtDocDocumentationHolder", documentationHolder.getClass().getName());
    }

    public void testResolverForJsDocWithSeveralSDocTags() throws ParseException {
        parseSource(getTestSource(getTestFile("../../../testfiles/jsdoc-testfiles/jsdocWithSDocTags.js")));
        JsDocumentationHolder documentationHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);
        assertEquals("org.netbeans.modules.javascript2.jsdoc.JsDocDocumentationHolder", documentationHolder.getClass().getName());
    }

    public void testResolverForSDocWithSeveralJsDocTags() throws ParseException {
        parseSource(getTestSource(getTestFile("../../../testfiles/jsdoc-testfiles/sdocWithJsDocTags.js")));
        JsDocumentationHolder documentationHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);
    assertEquals("org.netbeans.modules.javascript2.sdoc.SDocDocumentationHolder", documentationHolder.getClass().getName());
    }

    public void testResolverForExtDocWithSeveralSDocTags() throws ParseException {
        parseSource(getTestSource(getTestFile("../../../testfiles/jsdoc-testfiles/extdocWithSDocTags.js")));
        JsDocumentationHolder documentationHolder = JsDocumentationSupport.getDocumentationHolder(parserResult);
        assertEquals("org.netbeans.modules.javascript2.extdoc.ExtDocDocumentationHolder", documentationHolder.getClass().getName());
    }
}
