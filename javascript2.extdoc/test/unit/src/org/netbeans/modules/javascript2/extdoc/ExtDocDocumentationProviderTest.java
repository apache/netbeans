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
package org.netbeans.modules.javascript2.extdoc;

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

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ExtDocDocumentationProviderTest extends JsDocumentationTestBase {

    private static final String TEST_FILE_PATH = "testfiles/extdoc/";
    private static final String FILE_NAME_GENERAL = TEST_FILE_PATH + "classWithExtDoc.js";
    private static final String FILE_NAME_PARAMETERS = TEST_FILE_PATH + "parameterTypes.js";

    private JsDocumentationHolder documentationHolder;
    private JsParserResult parserResult;

    public ExtDocDocumentationProviderTest(String testName) {
        super(testName);
    }

    private void initializeDocumentationHolder(Source source) throws ParseException {
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                assertTrue(result instanceof JsParserResult);
                
                parserResult = (JsParserResult) result;
                documentationHolder = getDocumentationHolder(parserResult, new ExtDocDocumentationProvider());
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

    private void checkDocumentation(Source source, final int offset, final String expected) throws Exception {
        initializeDocumentationHolder(source);
        assertEquals(expected, documentationHolder.getDocumentation(getNodeForOffset(parserResult, offset)));
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

    public void testGetSummaryOfClassFromContextDescription() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "function Rectangle2(width, height) ^{");
        checkFirstSummary(testSource, caretOffset, "Create a new Rectangle instance.");
    }

    public void testGetSummaryOfClassFromClassDescription() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "function ShapeFactory()^{");
        checkFirstSummary(testSource, caretOffset, "This class exists to demonstrate the assignment of a class prototype\n  as an anonymous block.");
    }

    public void testGetReturnTypeForReturn() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "Shape.prototype.clone = function()^{");
        checkReturnType(testSource, caretOffset, Arrays.asList(new TypeUsage("Shape", 2321)));
    }

    public void testGetNullReturnTypeAtNoReturnTypeComment() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "Shape.prototype.clone3 = function()^{");
        checkReturnType(testSource, caretOffset, Collections.<Type>emptyList());
    }

    public void testGetNullReturnTypeAtMissingComment() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "Shape.prototype.clone4 = function()^{");
        checkReturnType(testSource, caretOffset, Collections.<Type>emptyList());
    }

    public void testGetReturnTypeAtFunction() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "function martion () ^{");
        checkReturnType(testSource, caretOffset, Arrays.asList(new TypeUsage("Number", 8587)));
    }

    public void testGetReturnTypeAtObjectFunction() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "getVersion: function() ^{");
        checkReturnType(testSource, caretOffset, Arrays.asList(new TypeUsage("Number", 8881)));
    }

    public void testGetReturnTypeAtType() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "Shape.prototype.getColor = function()^{");
        checkReturnType(testSource, caretOffset, Arrays.asList(new TypeUsage("Color", 1721)));
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
        FakeDocParameter fakeDocParameter = new FakeDocParameter(new Identifier("product", 101), null, "", false,
                Arrays.<Type>asList(new TypeUsage("String", 79), new TypeUsage("Number", 86), new TypeUsage("Object", 93)));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForFullDocOptionalParam() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_PARAMETERS));
        final int caretOffset = getCaretOffset(testSource, "function line3(accessLevel)^{}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter(new Identifier("accessLevel", 164), null, "accessLevel is optional", true,
                Arrays.<Type>asList(new TypeUsage("String", 155)));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForFullDocOptionalDefaultedParam() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_PARAMETERS));
        final int caretOffset = getCaretOffset(testSource, "function line6(accessLevel)^{}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter(new Identifier("accessLevel", 439), "low", "accessLevel is optional with default value", true,
                Arrays.<Type>asList(new TypeUsage("String", 430)));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetParametersForFullDocParam() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_PARAMETERS));
        final int caretOffset = getCaretOffset(testSource, "function line5(accessLevel)^{}");
        FakeDocParameter fakeDocParameter = new FakeDocParameter(new Identifier("accessLevel", 340), null, "accessLevel is not optional", false,
                Arrays.<Type>asList(new TypeUsage("String", 332)));
        checkParameter(testSource, caretOffset, fakeDocParameter);
    }

    public void testGetExtends() throws Exception {
        Source testSource = getTestSource(getTestFile(FILE_NAME_GENERAL));
        final int caretOffset = getCaretOffset(testSource, "function Circle(radius)^{");
        checkExtend(testSource, caretOffset, Collections.singletonList(new TypeUsage("Shape")));
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
        final int caretOffset = getCaretOffset(testSource, "Circle.^PI = 3.14;");
        checkModifiers(testSource, caretOffset, "static");
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
