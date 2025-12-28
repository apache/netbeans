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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.javascript2.doc.JsDocumentationTestBase;
import org.netbeans.modules.javascript2.doc.spi.JsComment;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocDescriptionElement;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocElement;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocElementType;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocIdentDescribedElement;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocIdentSimpleElement;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocSimpleElement;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocTypeDescribedElement;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocTypeNamedElement;
import org.netbeans.modules.javascript2.extdoc.model.ExtDocTypeSimpleElement;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.Parser;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ExtDocModelTest extends JsDocumentationTestBase {

    private static DocumentationElementComparator elementComparator = new DocumentationElementComparator();

    public ExtDocModelTest(String testName) {
        super(testName);
    }

    /**
     * The string should look like: <type>||<key1>=<value1>:<key2>=<value2>;<type>
     */
    private static List<ExtDocElement> parseExpected(String expected) {
        List<ExtDocElement> elements = new ArrayList<ExtDocElement>();
        String[] tags = expected.split("[;]+");
        for (String tag : tags) {
            String[] tmp = tag.split("[|][|]");
            FakeExtDocElement element = new FakeExtDocElement(ExtDocElementType.fromString(tmp[0]));
            if (tmp.length > 1) {
                String[] keyValues = tmp[1].split("[:]+");
                for (String keyValue : keyValues) {
                    String[] items = keyValue.split("[=]+");
                    if (items.length == 1) {
                        // in context sensitive cases
                        element.addProperty("desc", items[0]);
                    } else {
                        element.addProperty(items[0], items[1]);
                    }
                }
            }
            elements.add(element);
        }
        return elements;
    }

    private static void checkExtDocElements(String expected, List<? extends ExtDocElement> elements) {
        List<ExtDocElement> expectedTags = parseExpected(expected);
        assertElementsEquality(expectedTags, elements);

    }

    private void checkJsDocBlock(Source source, final int offset, final String expected) throws Exception {
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            public @Override
            void run(ResultIterator resultIterator) throws Exception {
                Parser.Result result = resultIterator.getParserResult();
                assertTrue(result instanceof JsParserResult);
                JsParserResult parserResult = (JsParserResult) result;

                JsDocumentationHolder documentationHolder = getDocumentationHolder(parserResult, new ExtDocDocumentationProvider());
                JsComment comment = documentationHolder.getCommentForOffset(offset, documentationHolder.getCommentBlocks());
                checkExtDocElements(expected, ((ExtDocComment) comment).getTags());
            }
        });
    }

    public void testContextSensitiveDescription() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line1;");
        checkJsDocBlock(source, caretOffset, "description||This should be description.");
    }

    public void testUnknown() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line2;");
        checkJsDocBlock(source, caretOffset, "@anyCustomTag");
    }

    public void testCfg() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line3;");
        checkJsDocBlock(source, caretOffset, "@cfg||type=paramType:name=paramName:desc=paramDescription");
    }

    public void testClass() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line4;");
        checkJsDocBlock(source, caretOffset, "@class||ident=classIdent:desc=desc");
    }

    public void testConstructor() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line6;");
        checkJsDocBlock(source, caretOffset, "@constructor");
    }

    public void testEvent() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line7;");
        checkJsDocBlock(source, caretOffset, "@event||ident=eventIdent:desc=desc");
    }

    public void testExtends() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line8;");
        checkJsDocBlock(source, caretOffset, "@extends||ident=extendsIdent");
    }

    public void testHide() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line9;");
        checkJsDocBlock(source, caretOffset, "@hide");
    }

    public void testIgnore() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line10;");
        checkJsDocBlock(source, caretOffset, "@ignore");
    }

    public void testLink() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line11;");
        checkJsDocBlock(source, caretOffset, "@link||ident=linkIdent:desc=desc");
    }

    public void testMember() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line12;");
        checkJsDocBlock(source, caretOffset, "@member||ident=memberIdent");
    }

    public void testMethod() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line14;");
        checkJsDocBlock(source, caretOffset, "@method");
    }

    public void testNamespace() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line15;");
        checkJsDocBlock(source, caretOffset, "@namespace||ident=namespaceIdent");
    }

    public void testParam() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line16;");
        checkJsDocBlock(source, caretOffset, "@param||type=paramType:name=paramName:desc=paramDescription");
    }

    public void testPrivate() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line17;");
        checkJsDocBlock(source, caretOffset, "@private");
    }

    public void testProperty() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line18;");
        checkJsDocBlock(source, caretOffset, "@property");
    }

    public void testReturn() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line20;");
        checkJsDocBlock(source, caretOffset, "@return||type=returnType:desc=returnDescription");
    }

    public void testSingleton() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line21;");
        checkJsDocBlock(source, caretOffset, "@singleton");
    }

    public void testStatic() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line22;");
        checkJsDocBlock(source, caretOffset, "@static");
    }

    public void testType() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/modelTestFile.js"));
        final int caretOffset = getCaretOffset(source, "var ^line23;");
        checkJsDocBlock(source, caretOffset, "@type||type=typeType");
    }

    public void testParameterWithoutDesc() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(source, "function ^line1(userName){}");
        checkJsDocBlock(source, caretOffset, "@param||type=String:name=userName");
    }

    public void testParameterWithMoreTypes() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(source, "function ^line2(product){}");
        checkJsDocBlock(source, caretOffset, "@param||type=String|Number|Object:name=product");
    }

    public void testParameterOptional() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(source, "function ^line3(accessLevel){}");
        checkJsDocBlock(source, caretOffset, "@param||type=String:name=accessLevel:desc=accessLevel is optional");
    }

    public void testParameterTypeNameDescription() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(source, "function ^line4(userName){}");
        checkJsDocBlock(source, caretOffset, "@param||type=String:name=userName:desc=name of the user");
    }

    public void testParameterNonOptionalWithDescription() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(source, "function ^line5(accessLevel){}");
        checkJsDocBlock(source, caretOffset, "@param||type=String:name=accessLevel:desc=accessLevel is not optional");
    }

    public void testParameterOptionalWithDefaultValue() throws Exception {
        Source source = getTestSource(getTestFile("testfiles/extdoc/parameterTypes.js"));
        final int caretOffset = getCaretOffset(source, "function ^line6(accessLevel){}");
        checkJsDocBlock(source, caretOffset, "@param||type=String:name=accessLevel:desc=accessLevel is optional with default value");
    }

    /**
     * Examples of expected values:
     *
     * @private
     * @alias <ident>
     * @class <ident> <desc>
     * @type <type>
     * @return <type> <desc>
     * @param <type> <name> <desc>
     */
    private static void assertElementEquality(FakeExtDocElement expected, ExtDocElement parsed) {
        assertEquals(expected.getType(), parsed.getType());
        switch (parsed.getType().getCategory()) {
            case IDENT_SIMPLE:
                assertTrue(parsed instanceof ExtDocIdentSimpleElement);
                ExtDocIdentSimpleElement identSimpleElement = (ExtDocIdentSimpleElement) parsed;
                assertEquals(expected.getProperty("ident"), identSimpleElement.getIdentifier());
                break;
            case IDENT_DESCRIBED:
                assertTrue(parsed instanceof ExtDocIdentDescribedElement);
                ExtDocIdentDescribedElement identDescribedElement = (ExtDocIdentDescribedElement) parsed;
                assertEquals(expected.getProperty("ident"), identDescribedElement.getIdentifier());
                assertEquals(expected.getProperty("desc"), identDescribedElement.getDescription());
                break;
            case DESCRIPTION:
                assertTrue(parsed instanceof ExtDocDescriptionElement);
                ExtDocDescriptionElement descElement = (ExtDocDescriptionElement) parsed;
                assertEquals(expected.getProperty("desc"), descElement.getDescription());
                break;
            case SIMPLE:
                assertTrue(parsed instanceof ExtDocSimpleElement);
                break;
            case UNKNOWN:
                assertTrue(parsed instanceof ExtDocDescriptionElement);
                break;
            case TYPE_SIMPLE:
                assertTrue(parsed instanceof ExtDocTypeSimpleElement);
                ExtDocTypeSimpleElement typeSimpleElement = (ExtDocTypeSimpleElement) parsed;
                assertTypesEquality(expected, typeSimpleElement);
                break;
            case TYPE_DESCRIBED:
                assertTrue(parsed instanceof ExtDocTypeDescribedElement);
                ExtDocTypeDescribedElement typeDescElement = (ExtDocTypeDescribedElement) parsed;
                assertEquals(expected.getProperty("desc"), typeDescElement.getTypeDescription());
                assertTypesEquality(expected, typeDescElement);
                break;
            case TYPE_NAMED:
                assertTrue(parsed instanceof ExtDocTypeNamedElement);
                ExtDocTypeNamedElement typeNamedElement = (ExtDocTypeNamedElement) parsed;
                assertEquals(expected.getProperty("name"), typeNamedElement.getTypeName().getName());
                assertEquals(expected.getProperty("desc"), typeNamedElement.getTypeDescription());
                assertTypesEquality(expected, typeNamedElement);
                break;
            default:
                throw new AssertionError();
        }
    }

    private static void assertTypesEquality(FakeExtDocElement expected, ExtDocTypeSimpleElement element) {
        if (expected.getProperty("type").indexOf("|") != -1) {
            String[] splitedType = expected.getProperty("type").split("[|]");
            for (int i = 0; i < splitedType.length; i++) {
                assertEquals(splitedType[i], element.getDeclaredTypes().get(i).getType());
            }
        } else {
            assertEquals(expected.getProperty("type"), element.getDeclaredTypes().get(0).getType());
        }
    }

    private static void assertElementsEquality(List<ExtDocElement> expectedTags, List<? extends ExtDocElement> elements) {
        expectedTags.sort(elementComparator);
        elements.sort(elementComparator);

        assertEquals(expectedTags.size(), elements.size());

        for (int i = 0; i < expectedTags.size(); i++) {
            ExtDocElement expected = expectedTags.get(i);
            ExtDocElement parsed = elements.get(i);
            assertElementEquality((FakeExtDocElement) expected, parsed);
        }
    }

    private static class DocumentationElementComparator implements Comparator<ExtDocElement> {

        @Override
        public int compare(ExtDocElement o1, ExtDocElement o2) {
            return o1.getType().toString().compareTo(o2.getType().toString());
        }
    }

    private static class FakeExtDocElement implements ExtDocElement {

        private final ExtDocElementType type;
        private Map<String, String> properties = new HashMap<String, String>();

        public FakeExtDocElement(ExtDocElementType type) {
            assertNotNull(type);
            this.type = type;
        }

        @Override
        public ExtDocElementType getType() {
            return type;
        }

        public void addProperty(String key, String value) {
            assertNotNull(key);
            assertNotNull(value);
            properties.put(key, value);
        }

        public String getProperty(String key) {
            String property = properties.get(key);
            return property == null ? "" : property;
        }
    }
}
