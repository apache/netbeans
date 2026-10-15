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
package org.netbeans.modules.editor.java;

import javax.swing.text.BadLocationException;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.bracesmatching.api.BracesMatchingTestUtils;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 *
 */
public class JavaBracesMatcherTest extends NbTestCase {

    public JavaBracesMatcherTest(String name) {
        super(name);
    }

    public void testStringTemplateBrackets() throws Exception {
        assertMatches2("\"\\^{test^}\"");
    }

    public void testMultilineStringBrackets() throws Exception {
        assertMatches2(
                  "\"\"\"\n"
                + "^(\n"
                + "^)\n"
                + "\"\"\"");
    }

    //from CslTestBase:
    protected void assertMatches2(String original) throws Exception {
        int caretPos = original.indexOf('^');
        original = original.substring(0, caretPos) + original.substring(caretPos+1);

        int matchingCaretPos = original.indexOf('^');

        original = original.substring(0, matchingCaretPos) + original.substring(matchingCaretPos+1);

        BaseDocument doc = getDocument(original);

        computeAndAssertMatches(doc, caretPos, false, matchingCaretPos);
        computeAndAssertMatches(doc, caretPos + 1, true, matchingCaretPos);
        computeAndAssertMatches(doc, matchingCaretPos, false, caretPos);
        computeAndAssertMatches(doc, matchingCaretPos + 1, true, caretPos);
    }

    private void computeAndAssertMatches(BaseDocument doc, int pos, boolean backwards, int matchingPos) throws BadLocationException, InterruptedException {
        BracesMatcherFactory factory = new JavaBracesMatcher();
        MatcherContext context = BracesMatchingTestUtils.createMatcherContext(doc, pos, backwards, 1);
        BracesMatcher matcher = factory.createMatcher(context);
        int[] origin = matcher.findOrigin();
        int[] matches = matcher.findMatches();

        assertNotNull("Did not find origin for " + " position " + pos, origin);
        assertNotNull("Did not find matches for " + " position " + pos, matches);

        int expectedPos = backwards ? pos - 1 : pos;

        assertEquals("Incorrect origin", expectedPos, origin[0]);
        assertEquals("Incorrect origin", expectedPos + 1, origin[1]);
        assertEquals("Incorrect matches", matchingPos, matches[0]);
        assertEquals("Incorrect matches", matchingPos + 1, matches[1]);
    }

    private BaseDocument getDocument(String content) throws Exception {
        BaseDocument doc = new BaseDocument(true, "text/x-java") {
        };

        doc.putProperty(org.netbeans.api.lexer.Language.class, JavaTokenId.language());

        doc.insertString(0, content, null);

        return doc;
    }
}
