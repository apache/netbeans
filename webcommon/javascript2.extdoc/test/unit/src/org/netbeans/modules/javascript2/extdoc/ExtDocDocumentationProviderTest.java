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
