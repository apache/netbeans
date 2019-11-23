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
package org.netbeans.modules.javascript2.sdoc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import org.netbeans.modules.javascript2.doc.JsDocumentationTestBase;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SDocCompletionDocumentationTest extends JsDocumentationTestBase {

    private static final String BASE_PATH = "testfiles/sdoc/completionDocumentation/";

    private JsDocumentationHolder documentationHolder;
    private JsParserResult parserResult;

    public SDocCompletionDocumentationTest(String testName) {
        super(testName);
    }

    private void initializeDocumentationHolder(Source source) throws ParseException {
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                assertTrue(result instanceof JsParserResult);
                parserResult = (JsParserResult) result;
                documentationHolder = getDocumentationHolder(parserResult, new SDocDocumentationProvider());
            }
        });
    }

    private void checkCompletionDocumentation(String relPath, String caretSeeker) throws Exception {
        Source testSource = getTestSource(getTestFile(relPath));
        final int caretOffset = getCaretOffset(testSource, caretSeeker);
        initializeDocumentationHolder(testSource);
        assertDescriptionMatches(relPath, documentationHolder.getDocumentation(getNodeForOffset(parserResult, caretOffset)).getContent(), true, "completionDoc.html");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Files.copy(
            new File(getDataDir(), "../../../testfiles/jsdoc-testfiles/completionDocumentation03.js").toPath(),
            new File(getDataDir(), "testfiles/sdoc/completionDocumentation/completionDocumentation01.js").toPath(),
            StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    protected File getDataFile(String relFilePath) {
        // CslTestBase loads test file and reference file from different locations
        // this breaks our assumption, that we can prepare the JS on-the-fly in the
        // build directory. This redirects the resolution of the reference files
        // to the build directory (they are also copied on test begin)
        return FileUtil.toFile(getTestFile(relFilePath));
    }

    public void testCompletionDocumentation01() throws Exception {
        checkCompletionDocumentation(BASE_PATH + "completionDocumentation01.js", "function Shape()^{");
    }

    public void testCompletionDocumentation02() throws Exception {
        checkCompletionDocumentation(BASE_PATH + "completionDocumentation01.js", "function addReference()^{");
    }

    public void testCompletionDocumentation03() throws Exception {
        checkCompletionDocumentation(BASE_PATH + "completionDocumentation01.js", "function Hexagon(sideLength) ^{");
    }

    public void testCompletionDocumentation04() throws Exception {
        // check throws
        checkCompletionDocumentation(BASE_PATH + "completionDocumentation01.js", "Shape.prototype.setColor = function(color)^{");
    }

    public void testCompletionDocumentation05() throws Exception {
        // check throws
        checkCompletionDocumentation(BASE_PATH + "completionDocumentation01.js", "Shape.prototype.clone3 = function()^{");
    }

    public void testCompletionDocumentation06() throws Exception {
        // check deprecation
        checkCompletionDocumentation(BASE_PATH + "completionDocumentation01.js", "Circle.P^I = 3.14;");
    }

    public void testCompletionDocumentation07() throws Exception {
        // check extends
        checkCompletionDocumentation(BASE_PATH + "completionDocumentation01.js", "function Circle(radius)^{");
    }

    public void testCompletionDocumentation09() throws Exception {
        // check see
        checkCompletionDocumentation(BASE_PATH + "completionDocumentation01.js", "Coordinate.prototype.x^ = 0;");
    }

    public void testCompletionDocumentation10() throws Exception {
        // check see
        checkCompletionDocumentation(BASE_PATH + "completionDocumentation01.js", "Circle.prototype.getRadius = function()^{");
    }

    public void testCompletionDocumentation11() throws Exception {
        // check since
        checkCompletionDocumentation(BASE_PATH + "completionDocumentation01.js", "function addReference()^{");
    }

    public void testCompletionDocumentation12() throws Exception {
        // check example
        checkCompletionDocumentation(BASE_PATH + "completionDocumentation01.js", "function Hexagon(sideLength) ^{");
    }

    public void testCompletionDocumentation13() throws Exception {
        // check example
        checkCompletionDocumentation(BASE_PATH + "completionDocumentation01.js", "Circle.createCircle = function(radius)^{");
    }
}
