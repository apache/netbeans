/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
