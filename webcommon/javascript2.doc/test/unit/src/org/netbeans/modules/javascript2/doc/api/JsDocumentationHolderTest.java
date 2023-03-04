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
package org.netbeans.modules.javascript2.doc.api;

import java.util.Collections;
import org.netbeans.modules.javascript2.doc.JsDocumentationTestBase;
import org.netbeans.modules.javascript2.doc.spi.JsComment;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.types.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocumentationHolderTest extends JsDocumentationTestBase {

    public JsDocumentationHolderTest(String testName) {
        super(testName);
    }

    private void checkCommentExist(Source source, final int offset, final boolean exists, final int expectedParamsCount) throws Exception {
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                assertTrue(result instanceof ParserResult);
                ParserResult parserResult = (ParserResult) result;

                JsDocumentationHolder documentationHolder = getDocumentationHolder(parserResult);
                JsComment comment = documentationHolder.getCommentForOffset(offset, documentationHolder.getCommentBlocks());
                assertEquals(exists, comment != null);
                if (exists) {
                    assertEquals(expectedParamsCount, comment.getParameters().size());
                }
            }
        });
    }

    public void testGetCommentWithBracesOnNextLine() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/doc/holder/testGetCommentWithBracesOnVariousLine.js"));
        final int caretOffset = getCaretOffset(source, "^{");
        checkCommentExist(source, caretOffset, true, 2);
    }

    public void testGetCommentWithBracesOnTheSameLine() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/doc/holder/testGetCommentWithBracesOnVariousLine.js"));
        final int caretOffset = getCaretOffset(source, "function test2 (a) ^{");
        checkCommentExist(source, caretOffset, true, 1);
    }

    public void testGetCommentWhereNotPossible_1() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/doc/holder/testGetCommentWhereNotPossible.js"));
        final int caretOffset = getCaretOffset(source, "function test2 (a) ^{");
        checkCommentExist(source, caretOffset, false, 0);
    }

    public void testGetCommentWhereNotPossible_2() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/doc/holder/testGetCommentWhereNotPossible.js"));
        final int caretOffset = getCaretOffset(source, "function test3 (a) ^{");
        checkCommentExist(source, caretOffset, false, 0);
    }

    public void testGetCorrectComment_1() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/doc/holder/testGetCorrectComment.js"));
        final int caretOffset = getCaretOffset(source, "function test3 (a, b) ^{");
        checkCommentExist(source, caretOffset, false, 0);
    }
}
