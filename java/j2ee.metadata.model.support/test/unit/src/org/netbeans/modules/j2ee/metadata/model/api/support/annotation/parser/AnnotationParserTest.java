/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;

/**
 *
 * @author Andrei Badea
 */
public class AnnotationParserTest extends JavaSourceTestCase {

    public AnnotationParserTest(String testName) {
        super(testName);
    }

    public void testPrimitive() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Annotated.java",
                "@interface Annotation {" +
                "   int intValue();" +
                "   int intValue2();" +
                "   int intValue3();" +
                "   double doubleValue();" +
                "   String stringValue();" +
                "   String stringValue2();" +
                "   String stringValue3();" +
                "}" +
                "@Annotation(intValue = 2, doubleValue = \"error\", stringValue = \"foo\", " +
                "       stringValue2 = @SuppressWarnings(\"error\")" +
                "public class Annotated {" +
                "}");
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement annotated = helper.getCompilationController().getElements().getTypeElement("Annotated");
                AnnotationMirror annotation = annotated.getAnnotationMirrors().iterator().next();
                AnnotationParser parser = AnnotationParser.create(helper);
                parser.expectPrimitive("intValue", Integer.class, AnnotationParser.defaultValue(0));
                parser.expectPrimitive("intValue2", Integer.class, AnnotationParser.defaultValue(Integer.MAX_VALUE));
                parser.expectPrimitive("intValue3", Integer.class, AnnotationParser.defaultValue(Integer.MIN_VALUE));
                parser.expectPrimitive("doubleValue", Double.class, AnnotationParser.defaultValue(0.0));
                parser.expectString("stringValue", AnnotationParser.defaultValue("stringValue"));
                parser.expectString("stringValue2", AnnotationParser.defaultValue("stringValue2"));
                parser.expectString("stringValue3", AnnotationParser.defaultValue("stringValue3"));
                ParseResult parseResult = parser.parse(annotation);
                assertEquals(2, (int)parseResult.get("intValue", Integer.class));
                assertEquals(Integer.MAX_VALUE, (int)parseResult.get("intValue2", Integer.class));
                assertEquals(Integer.MIN_VALUE, (int)parseResult.get("intValue3", Integer.class));
                assertEquals(0.0, (double)parseResult.get("doubleValue", Double.class));
                assertEquals("foo", parseResult.get("stringValue", String.class));
                // XXX does not work
                // assertEquals("stringValue2", parseResult.get("stringValue2", String.class));
                assertEquals("stringValue3", parseResult.get("stringValue3", String.class));
            }
        });
    }

    public void testClass() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Annotated.java",
                "import java.lang.annotation.*;" +
                "@interface Annotation {" +
                "   Class<?> classValue();" +
                "   Class<?> classValue2();" +
                "   Class<?> classValue3();" +
                "}" +
                "@Annotation(classValue = Object.class, classValue2 = \"error\")" +
                "public class Annotated {" +
                "}");
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement annotated = helper.getCompilationController().getElements().getTypeElement("Annotated");
                AnnotationMirror annotation = annotated.getAnnotationMirrors().iterator().next();
                AnnotationParser parser = AnnotationParser.create(helper);
                parser.expectClass("classValue", AnnotationParser.defaultValue("java.lang.Double"));
                parser.expectClass("classValue2", AnnotationParser.defaultValue("java.lang.String"));
                parser.expectClass("classValue3", AnnotationParser.defaultValue("java.lang.Integer"));
                ParseResult parseResult = parser.parse(annotation);
                assertEquals("java.lang.Object", parseResult.get("classValue", String.class));
                assertEquals("java.lang.String", parseResult.get("classValue2", String.class));
                assertEquals("java.lang.Integer", parseResult.get("classValue3", String.class));
            }
        });
    }

    public void testEnumConstant() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Annotated.java",
                "import java.lang.annotation.*;" +
                "@interface Annotation {" +
                "   RetentionPolicy enumValue();" +
                "   RetentionPolicy enumValue2();" +
                "   RetentionPolicy enumValue3();" +
                "}" +
                "@Annotation(enumValue = RetentionPolicy.CLASS, enumValue2 = ElementType.TYPE)" +
                "public class Annotated {" +
                "}");
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                Elements elements = helper.getCompilationController().getElements();
                TypeElement annotated = elements.getTypeElement("Annotated");
                AnnotationMirror annotation = annotated.getAnnotationMirrors().iterator().next();
                AnnotationParser parser = AnnotationParser.create(helper);
                TypeMirror rpType = elements.getTypeElement("java.lang.annotation.RetentionPolicy").asType();
                parser.expectEnumConstant("enumValue", rpType, AnnotationParser.defaultValue("SOURCE"));
                parser.expectEnumConstant("enumValue2", rpType, AnnotationParser.defaultValue("CLASS"));
                parser.expectEnumConstant("enumValue3", rpType, AnnotationParser.defaultValue("RUNTIME"));
                ParseResult parseResult = parser.parse(annotation);
                assertEquals("CLASS", parseResult.get("enumValue", String.class));
                assertEquals("CLASS", parseResult.get("enumValue2", String.class));
                assertEquals("RUNTIME", parseResult.get("enumValue3", String.class));
            }
        });
    }

    public void testAnnotation() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Annotated.java",
                "@interface Annotation {" +
                "   SuppressWarnings annValue();" +
                "   SuppressWarnings annValue2();" +
                "   SuppressWarnings annValue3();" +
                "}" +
                "@Annotation(annValue = @SuppressWarnings(\"unchecked\"), annValue2 = \"error\")" +
                "public class Annotated {" +
                "}");
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                Elements elements = helper.getCompilationController().getElements();
                TypeElement annotated = elements.getTypeElement("Annotated");
                AnnotationMirror annotation = annotated.getAnnotationMirrors().iterator().next();
                AnnotationParser parser = AnnotationParser.create(helper);
                Object defaultValue = "defaultValue", defaultValue2 = "defaultValue2", defaultValue3 = "defaultValue3";
                AnnotationValueHandler swHandler = new AnnotationValueHandler() {
                    public Object handleAnnotation(AnnotationMirror annotation) {
                        // better to use AnnotationParser again here
                        @SuppressWarnings("unchecked")
                        List<AnnotationValue> arrayMembers = (List<AnnotationValue>)annotation.getElementValues().values().iterator().next().getValue();
                        return (String)arrayMembers.iterator().next().getValue();
                    }
                };
                TypeMirror swType = elements.getTypeElement("java.lang.SuppressWarnings").asType();
                parser.expectAnnotation("annValue", swType, swHandler, AnnotationParser.defaultValue(defaultValue));
                parser.expectAnnotation("annValue2", swType, swHandler, AnnotationParser.defaultValue(defaultValue2));
                parser.expectAnnotation("annValue3", swType, swHandler, AnnotationParser.defaultValue(defaultValue3));
                ParseResult parseResult = parser.parse(annotation);
                assertEquals("unchecked", parseResult.get("annValue", String.class));
                assertEquals("defaultValue2", parseResult.get("annValue2", String.class));
                assertEquals("defaultValue3", parseResult.get("annValue3", String.class));
            }
        });
    }

    public void testStringArray() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Annotated.java",
                "@interface Annotation {" +
                "   String[] arrayValue();" +
                "   String[] arrayValue2();" +
                "   String]] arrayValue3();" +
                "}" +
                "@Annotation(arrayValue = { \"foo\", \"bar\" }, arrayValue2 = @Error)" +
                "public class Annotated {" +
                "}");
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                Elements elements = helper.getCompilationController().getElements();
                TypeElement annotated = elements.getTypeElement("Annotated");
                AnnotationMirror annotation = annotated.getAnnotationMirrors().iterator().next();
                AnnotationParser parser = AnnotationParser.create(helper);
                List<String> defaultValue = new ArrayList<String>(), defaultValue2 = new ArrayList<String>(), defaultValue3 = new ArrayList<String>();
                ArrayValueHandler arrayHandler = new ArrayValueHandler() {
                    public Object handleArray(List<AnnotationValue> array) {
                        List<String> result = new ArrayList<String>();
                        for (AnnotationValue arrayMember : array) {
                            result.add((String)arrayMember.getValue());
                        }
                        return result;
                    }
                };
                parser.expectStringArray("arrayValue", arrayHandler, AnnotationParser.defaultValue(defaultValue));
                parser.expectStringArray("arrayValue2", arrayHandler, AnnotationParser.defaultValue(defaultValue2));
                parser.expectStringArray("arrayValue3", arrayHandler, AnnotationParser.defaultValue(defaultValue3));
                ParseResult parseResult = parser.parse(annotation);
                @SuppressWarnings("unchecked")
                List list = (List<String>)parseResult.get("arrayValue", List.class);
                assertEquals(2, list.size());
                assertTrue(list.contains("foo"));
                assertTrue(list.contains("bar"));
                // XXX does not work
                // assertSame(defaultValue2, parseResult.get("arrayValue2", List.class));
                assertSame(defaultValue3, parseResult.get("arrayValue3", List.class));
            }
        });
    }

    public void testAnnotationArray() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Annotated.java",
                "import java.lang.annotation.*;" +
                "@interface Annotation {" +
                "   SuppressWarnings[] annotationValue();" +
                "   SuppressWarnings[] annotationValue2();" +
                "   SuppressWarnings[] annotationValue3();" +
                "}" +
                "@Annotation(annotationValue = { @SuppressWarnings(\"foo\"), @SuppressWarnings({\"bar\", \"baz\"}) }, annotationValue2 = @Retention(RetentionPolicy.SOURCE), annotationValue3 = null)" +
                "public class Annotated {" +
                "}");
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                Elements elements = helper.getCompilationController().getElements();
                TypeElement annotated = elements.getTypeElement("Annotated");
                AnnotationMirror annotation = annotated.getAnnotationMirrors().iterator().next();
                AnnotationParser parser = AnnotationParser.create(helper);
                final AnnotationParser swParser = AnnotationParser.create(helper);
                swParser.expectStringArray("value", new ArrayValueHandler() {
                    public Object handleArray(List<AnnotationValue> arrayMembers) {
                        List<String> result = new ArrayList<String>();
                        for (AnnotationValue arrayMember : arrayMembers) {
                            result.add((String)arrayMember.getValue());
                        }
                        return result;
                    }
                }, AnnotationParser.defaultValue(Collections.emptyList()));
                List<String> defaultValue = new ArrayList<String>();
                ArrayValueHandler arrayHandler = new ArrayValueHandler() {
                    public Object handleArray(List<AnnotationValue> array) {
                        List<String> result = new ArrayList<String>();
                        for (AnnotationValue arrayMember : array) {
                            AnnotationMirror swAnotation = (AnnotationMirror)arrayMember.getValue();
                            @SuppressWarnings("unchecked")
                            List<String> subresult = (List<String>)swParser.parse(swAnotation).get("value", List.class);
                            result.addAll(subresult);
                        }
                        return result;
                    }
                };
                TypeMirror swType = elements.getTypeElement("java.lang.SuppressWarnings").asType();
                parser.expectAnnotationArray("annotationValue", swType, arrayHandler, null);
                parser.expectAnnotationArray("annotationValue2", swType, arrayHandler, null);
                parser.expectAnnotationArray("annotationValue3", swType, arrayHandler, AnnotationParser.defaultValue(defaultValue));
                ParseResult parseResult = parser.parse(annotation);
                @SuppressWarnings("unchecked")
                List list = (List<String>)parseResult.get("annotationValue", List.class);
                assertEquals(3, list.size());
                assertTrue(list.contains("foo"));
                assertTrue(list.contains("bar"));
                assertTrue(list.contains("baz"));
                @SuppressWarnings("unchecked")
                List list2 = (List<String>)parseResult.get("annotationValue2", List.class);
                //assertEquals(0, list2.size());
                assertNull(list2);
                assertEquals(defaultValue, parseResult.get("annotationValue3", List.class));
            }
        });
    }
}
