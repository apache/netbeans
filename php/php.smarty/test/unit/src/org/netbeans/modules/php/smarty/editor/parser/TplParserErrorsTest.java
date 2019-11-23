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
package org.netbeans.modules.php.smarty.editor.parser;

import java.util.Collections;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.smarty.TplTestBase;
import org.netbeans.modules.php.smarty.editor.lexer.TplTokenId;
import org.netbeans.modules.php.smarty.editor.lexer.TplTopTokenId;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class TplParserErrorsTest extends TplTestBase {

    TplParserResult parserResult;

    public TplParserErrorsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        assert Lookup.getDefault().lookup(TestLanguageProvider.class) != null;

        try {
            TestLanguageProvider.register(HTMLTokenId.language());
            TestLanguageProvider.register(PHPTokenId.language());
            TestLanguageProvider.register(JsTokenId.javascriptLanguage());
            TestLanguageProvider.register(TplTopTokenId.language());
            TestLanguageProvider.register(TplTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
    }

    private void parseSource(Source source) throws ParseException {
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                assertTrue(result instanceof TplParserResult);
                parserResult = (TplParserResult) result;
            }
        });
    }

    public void testParserErrors1() throws Exception {
        assertParserErrors();
    }

    public void testParserErrors2() throws Exception {
        assertParserErrors();
    }

    public void testParserErrors3() throws Exception {
        assertParserErrors();
    }

    public void testParserErrors4() throws Exception {
        assertParserErrors();
    }

    public void testParserErrors5() throws Exception {
        assertParserErrors();
    }

    public void testParserErrors6() throws Exception {
        assertParserErrors();
    }

    public void testParserErrors7() throws Exception {
        assertParserErrors();
    }

    public void testParserErrors8() throws Exception {
        assertParserErrors();
    }

    public void testParserErrors9() throws Exception {
        assertParserErrors();
    }

    public void testParserErrors10() throws Exception {
        assertParserErrors();
    }

    public void testParserErrors11() throws Exception {
        assertParserErrors();
    }

    // no errors
    public void testParserNETBEANS3335_01() throws Exception {
        assertParserErrors();
    }

    public void testParserNETBEANS3335_02() throws Exception {
        assertParserErrors();
    }

    private String getTestFileRelPath() {
        return "testfiles/parserErrors/" + getName() + ".tpl";
    }

    private FileObject getTestFile() {
        return super.getTestFile(getTestFileRelPath());
    }

    private String serializeErrors() {
        StringBuilder errors = new StringBuilder("Detected parser errors in the file:\n");
        for (TplParserResult.Error error : parserResult.getErrors()) {
            errors.append(serializeError(error));
        }
        return errors.toString();
    }

    private String serializeError(TplParserResult.Error error) {
        StringBuilder errorSB = new StringBuilder();
        errorSB.append(error.getDescription())
                .append(" : offset <")
                .append(error.getStartPosition())
                .append(":")
                .append(error.getEndPosition())
                .append(">\n");
        return errorSB.toString();
    }

    private void assertParserErrors() throws Exception {
        parseSource(getTestSource(getTestFile()));
        assertDescriptionMatches(getTestFileRelPath(), serializeErrors(), false, ".errors");
    }

}
