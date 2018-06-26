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
