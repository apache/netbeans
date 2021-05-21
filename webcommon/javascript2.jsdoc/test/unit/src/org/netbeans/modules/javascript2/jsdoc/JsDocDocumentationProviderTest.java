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
package org.netbeans.modules.javascript2.jsdoc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.javascript2.doc.JsDocumentationTestBase;
import org.netbeans.modules.javascript2.doc.spi.JsModifier;
import org.netbeans.modules.javascript2.doc.spi.DocParameter;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.Type;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
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
public class JsDocDocumentationProviderTest extends JsDocumentationTestBase {

    private static final String TEST_FILE_PATH = "testfiles/jsdoc/";
    private static final String FILE_NAME_GENERAL = TEST_FILE_PATH + "classWithJsDoc.js";
    private static final String FILE_NAME_RETURNS = TEST_FILE_PATH + "returnTypes.js";
    private static final String FILE_NAME_PARAMETERS = TEST_FILE_PATH + "parameterTypes.js";

    private JsDocumentationHolder documentationHolder;
    private JsParserResult parserResult;

    public JsDocDocumentationProviderTest(String testName) {
        super(testName);
    }

    private void initializeDocumentationHolder(Source source) throws ParseException {
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                assertTrue(result instanceof JsParserResult);
                
                parserResult = (JsParserResult) result;
                documentationHolder = getDocumentationHolder(parserResult, new JsDocDocumentationProvider());
            }
        });
    }

    private void checkReturnType(Source source, final int offset, final List<? extends Type> expected) throws Exception {
        initializeDocumentationHolder(source);
        if (expected == null) {
            assertNull(documentationHolder.getReturnType(getNodeForOffset(parserResult, offset)));
        } else {
            for (int i = 0; i < expected.size(); i++) {
                assertEquals(expected.get(i), documentationHolder.getReturnType(getNodeForOffset(parserResult, offset)).get(i));
            }
        }
    }

    private void checkExtend(Source source, final int offset, final List<? extends Type> expected) throws Exception {
        initializeDocumentationHolder(source);
        if (expected == null) {
            assertTrue(documentationHolder.getExtends(getNodeForOffset(parserResult, offset)).isEmpty());
        } else {
            for (int i = 0; i < expected.size(); i++) {
                assertEquals(expected.get(i), documentationHolder.getExtends(getNodeForOffset(parserResult, offset)).get(i));
            }
        }
    }

    private void checkParameter(Source source, final int offset, final FakeDocParameter expectedParam) throws Exception {
        initializeDocumentationHolder(source);
        if (expectedParam == null) {
            assertNull(documentationHolder.getParameters(getNodeForOffset(parserResult, offset)));
        } else {
            List<DocParameter> parameters = documentationHolder.getParameters(getNodeForOffset(parserResult, offset));
            assertEquals(expectedParam.getDefaultValue(), parameters.get(0).getDefaultValue());
            assertEquals(expectedParam.getParamDescription(), parameters.get(0).getParamDescription());
            assertEquals(expectedParam.getParamName(), parameters.get(0).getParamName());
            assertEquals(expectedParam.isOptional(), parameters.get(0).isOptional());
            for (int i = 0; i < expectedParam.getParamTypes().size(); i++) {
                assertEquals(expectedParam.getParamTypes().get(i), parameters.get(0).getParamTypes().get(i));
            }
        }
    }

    private void checkDocumentation(Source source, final int offset, final String expected) throws Exception {
        initializeDocumentationHolder(source);
        assertEquals(expected, documentationHolder.getDocumentation(getNodeForOffset(parserResult, offset)));
    }

    private void checkDeprecated(Source source, final int offset, final boolean expected) throws Exception {
        initializeDocumentationHolder(source);
        assertEquals(expected, documentationHolder.isDeprecated(getNodeForOffset(parserResult, offset)));
    }

    private void checkModifiers(Source source, final int offset, final String expectedModifiers) throws Exception {
        initializeDocumentationHolder(source);
        Set<JsModifier> realModifiers = documentationHolder.getModifiers(getNodeForOffset(parserResult, offset));
        if (expectedModifiers == null) {
            assertEquals(0, realModifiers.size());
        } else {
            String[] expModifiers = expectedModifiers.split("[|]");
            assertEquals(expModifiers.length, realModifiers.size());
            for (int i = 0; i < expModifiers.length; i++) {
                assertTrue(realModifiers.contains(JsModifier.fromString(expModifiers[i])));
            }
        }
    }

    private void checkFirstSummary(Source source, int offset, String summary) throws ParseException {
        initializeDocumentationHolder(source);
        assertEquals(summary, documentationHolder.getCommentForOffset(offset, documentationHolder.getCommentBlocks()).getSummary().get(0));
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Files.copy(
            new File(getDataDir(), "../../../testfiles/jsdoc-testfiles/classWithJsDoc.js").toPath(),
            new File(getDataDir(), "testfiles/jsdoc/classWithJsDoc.js").toPath(),
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

    public void testGetSummaryOfClassFromContextDescription() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "function Rectangle2(width, height) ^{");
        checkFirstSummary(testSource, caretOffset, "Create a new Rectangle2 instance.");
    }

    public void testGetSummaryOfClassFromDescription() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "function Rectangle3(width, height) ^{");
        checkFirstSummary(testSource, caretOffset, "Create a new Rectangle3 instance.");
    }

    public void testGetReturnTypeForReturn() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "Shape.prototype.clone = function()^{");
        checkReturnType(testSource, caretOffset, Arrays.asList(new TypeUsage("Shape", 3605)));
    }

    public void testGetReturnTypeForReturns() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));

        final int caretOffset = getCaretOffset(testSource, "Shape.prototype.clone2 = function()^{");
        checkReturnType(testSource, caretOffset, Arrays.asList(new TypeUsage("Shape", 3759)));
    }

    public void testGetReturnTypeForType() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));

        final int caretOffset = getCaretOffset(testSource, "Rectangle.prototype.getClassName= function()^{");
        checkReturnType(testSource, caretOffset, Arrays.asList(new TypeUsage("String", 5079)));
    }

    public void testGetNullReturnTypeAtNoReturnTypeComment() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));

        final int caretOffset = getCaretOffset(testSource, "Shape.prototype.clone3 = function()^{");
        checkReturnType(testSource, caretOffset, Collections.<Type>emptyList());
    }

    public void testGetNullReturnTypeByMissingComment() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_RETURNS));

        final int caretOffset = getCaretOffset(testSource, "Shape.prototype.clone4 = function()^{");
        checkReturnType(testSource, caretOffset, Collections.<Type>emptyList());
    }

    public void testGetReturnTypeAtFunction() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_RETURNS));

        final int caretOffset = getCaretOffset(testSource, "function martion () ^{");
        checkReturnType(testSource, caretOffset, Arrays.asList(new TypeUsage("Number", 571)));
    }

    public void testGetReturnTypeAtObjectFunction() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_RETURNS));

        final int caretOffset = getCaretOffset(testSource, "getVersion: function() ^{");
        checkReturnType(testSource, caretOffset, Arrays.asList(new TypeUsage("Number", 478)));
    }

    public void testGetReturnTypeAtProperty() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_RETURNS));

        final int caretOffset = getCaretOffset(testSource, "Math.E^");
        checkReturnType(testSource, caretOffset, Arrays.asList(new TypeUsage("Number", 654)));
    }

    public void testGetParametersForOnlyNameParam() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_PARAMETERS));
        final int caretOffset = getCaretOffset(testSource, "function line5(accessLevel)^{}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter(new Identifier("accessLevel", 348), null, "", false,
                Collections.<Type>emptyList());
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetExtends() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "function Circle(radius)^{");
        checkExtend(testSource, caretOffset, Collections.singletonList(new TypeUsage("Shape", 7234)));
    }

    public void testGetParametersForNameAndTypeParam() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_PARAMETERS));
        final int caretOffset = getCaretOffset(testSource, "function line1(userName)^{}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter(new Identifier("userName", 23), null, "", false,
                Arrays.<Type>asList(new TypeUsage("String", 15)));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForNameAndMoreTypesParam() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_PARAMETERS));
        final int caretOffset = getCaretOffset(testSource, "function line2(product)^{}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter(new Identifier("product", 94), null, "", false,
                Arrays.<Type>asList(new TypeUsage("String", 79), new TypeUsage("Number", 86)));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForFullDocParam() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_PARAMETERS));
        final int caretOffset = getCaretOffset(testSource, "function line6(userName)^{}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter(new Identifier("userName", 418), null, "name of the user", false,
                Arrays.<Type>asList(new TypeUsage("String", 410)));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForFullDocOptionalParam() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_PARAMETERS));
        final int caretOffset = getCaretOffset(testSource, "function line3(accessLevel)^{}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter(new Identifier("accessLevel", 157), null, "accessLevel is optional", true,
                Arrays.<Type>asList(new TypeUsage("String", 148)));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForDefaultValueParam() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_PARAMETERS));
        final int caretOffset = getCaretOffset(testSource, "function line4(accessLevel)^{}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter(new Identifier("accessLevel", 253), "\"author\"", "accessLevel is optional", true,
                Arrays.<Type>asList(new TypeUsage("String", 244)));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForNameAndTypeArgument() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_PARAMETERS));
        final int caretOffset = getCaretOffset(testSource, "function line7(userName)^{}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter(new Identifier("userName", 502), null, "", false,
                Arrays.<Type>asList(new TypeUsage("String", 494)));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForDefaultValueArgument() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_PARAMETERS));
        final int caretOffset = getCaretOffset(testSource, "function line8(userName)^{}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter(new Identifier("userName", 570), "\"Jackie\"", "userName is optional", true,
                Arrays.<Type>asList(new TypeUsage("String", 561)));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForDefaultValueWithSpacesArgument() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_PARAMETERS));
        final int caretOffset = getCaretOffset(testSource, "function line9(userName)^{}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter(new Identifier("userName", 669), "\"for example Jackie Chan\"", "userName is optional", true,
                Arrays.<Type>asList(new TypeUsage("String", 660)));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testDeprecated01() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "function Add(One, Two)^{");
        checkDeprecated(testSource, caretOffset, true);
    }

    public void testDeprecated02() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "Circle.^PI = 3.14;");
        checkDeprecated(testSource, caretOffset, true);
    }

    public void testDeprecated03() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "Rectangle.prototype.^width = 0;");
        checkDeprecated(testSource, caretOffset, false);
    }

    public void testDeprecated04() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "Coordinate.prototype.getX = function()^{");
        checkDeprecated(testSource, caretOffset, false);
    }

    public void testModifiers01() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "Rectangle.prototype.^width = 0;");
        checkModifiers(testSource, caretOffset, "private");
    }

    public void testModifiers02() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "Rectangle.prototype.getWidth = function()^{");
        checkModifiers(testSource, caretOffset, null);
    }

    public void testModifiers03() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "Rectangle.prototype.setWidth = function(width)^{");
        checkModifiers(testSource, caretOffset, "public");
    }

    public void testModifiers04() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "Circle.^PI = 3.14;");
        checkModifiers(testSource, caretOffset, "static");
    }

    public void testModifiers05() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "Circle.createCircle = function(radius)^{");
        checkModifiers(testSource, caretOffset, "static|public");
    }

    public void testIssue224759() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "function Issue224759() ^{");
        checkFirstSummary(testSource, caretOffset, "Issue224759 This is not visible in Help");
    }

    private static class FakeDocParameter implements DocParameter {

        Identifier paramName;
        String defaultValue, paramDesc;
        boolean optional;
        List<Type> paramTypes;

        public FakeDocParameter(Identifier paramName, String defaultValue, String paramDesc, boolean optional, List<Type> paramTypes) {
            this.paramName = paramName;
            this.defaultValue = defaultValue;
            this.paramDesc = paramDesc;
            this.optional = optional;
            this.paramTypes = paramTypes;
        }
        @Override
        public Identifier getParamName() {
            return paramName;
        }

        @Override
        public String getDefaultValue() {
            return defaultValue;
        }

        @Override
        public boolean isOptional() {
            return optional;
        }

        @Override
        public String getParamDescription() {
            return paramDesc;
        }

        @Override
        public List<Type> getParamTypes() {
            return paramTypes;
        }

    }
}
