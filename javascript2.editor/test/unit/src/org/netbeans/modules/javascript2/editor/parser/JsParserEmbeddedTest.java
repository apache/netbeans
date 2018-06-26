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
