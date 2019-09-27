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
package org.netbeans.modules.php.api.annotation.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class AnnotationUtilsTest extends NbTestCase {

    private static final String ANNOTATION_NAME = "Annotation";

    public AnnotationUtilsTest(String name) {
        super(name);
    }

    public void testValidUseCase_01() throws Exception {
        assertTrue(AnnotationUtils.isTypeAnnotation("\\Foo\\Bar\\Baz\\" + ANNOTATION_NAME, ANNOTATION_NAME));
    }

    public void testValidUseCase_02() throws Exception {
        assertTrue(AnnotationUtils.isTypeAnnotation("Foo\\Bar\\Baz\\" + ANNOTATION_NAME, ANNOTATION_NAME));
    }

    public void testValidUseCase_03() throws Exception {
        assertTrue(AnnotationUtils.isTypeAnnotation(ANNOTATION_NAME, ANNOTATION_NAME));
    }

    public void testValidUseCase_04() throws Exception {
        assertTrue(AnnotationUtils.isTypeAnnotation(ANNOTATION_NAME.toLowerCase(), ANNOTATION_NAME));
    }

    public void testValidUseCase_05() throws Exception {
        assertTrue(AnnotationUtils.isTypeAnnotation("Foo\\Bar\\Baz\\" + ANNOTATION_NAME.toLowerCase(), ANNOTATION_NAME));
    }

    public void testInvalidUseCase_01() throws Exception {
        assertFalse(AnnotationUtils.isTypeAnnotation(ANNOTATION_NAME + "\\Foo\\Bar\\Baz\\", ANNOTATION_NAME));
    }

    public void testInvalidUseCase_02() throws Exception {
        assertFalse(AnnotationUtils.isTypeAnnotation("\\Foo\\Bar" + ANNOTATION_NAME + "\\Baz\\", ANNOTATION_NAME));
    }

    public void testExtractParamTypes() throws Exception {
        Set<String> discriminatorMapRegexs = new HashSet<String>();
        discriminatorMapRegexs.add(""); //NOI18N
        Map<OffsetRange, String> types = AnnotationUtils.extractTypesFromParameters("DiscriminatorMap({\"person\" = \" Person \", \"employee\" = \" Employee \"})", discriminatorMapRegexs);
        assertNotNull(types);
        assertTrue(!types.isEmpty());
        assertEquals(2, types.size());
        assertTrue(types.containsValue("Person"));
        assertTrue(types.containsValue("Employee"));
        assertTrue(types.containsKey(new OffsetRange(31, 37)));
        assertTrue(types.containsKey(new OffsetRange(56, 64)));
    }

    public void testQualifiedExtractParamTypes() throws Exception {
        Set<String> discriminatorMapRegexs = new HashSet<String>();
        discriminatorMapRegexs.add(""); //NOI18N
        Map<OffsetRange, String> types = AnnotationUtils.extractTypesFromParameters("DiscriminatorMap({\"person\" = \" My\\Person \", \"employee\" = \" \\Full\\Q\\Employee \"})", discriminatorMapRegexs);
        assertNotNull(types);
        assertTrue(!types.isEmpty());
        assertEquals(2, types.size());
        assertTrue(types.containsValue("My\\Person"));
        assertTrue(types.containsValue("\\Full\\Q\\Employee"));
        assertTrue(types.containsKey(new OffsetRange(31, 40)));
        assertTrue(types.containsKey(new OffsetRange(59, 75)));
    }

    public void extractJustSomeParamTypes_01() throws Exception {
        Set<String> manyToOneRegexs = new HashSet<String>();
        manyToOneRegexs.add("targetEntity"); //NOI18N
        Map<OffsetRange, String> types = AnnotationUtils.extractTypesFromParameters("ManyToOne(targetEntity=\"Cart\", cascade={\"all\"}, fetch=\"EAGER\")", manyToOneRegexs);
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("ManyToOne", type1);
        String type2 = types.get(new OffsetRange(24, 28));
        assertEquals("Cart", type2);
    }

    public void extractJustSomeParamTypes_02() throws Exception {
        Set<String> manyToOneRegexs = new HashSet<String>();
        manyToOneRegexs.add("targetEntity"); //NOI18N
        Map<OffsetRange, String> types = AnnotationUtils.extractTypesFromParameters("ManyToOne(targetEntity=\"\\Foo\\Cart\", cascade={\"all\"}, fetch=\"EAGER\")", manyToOneRegexs);
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("ManyToOne", type1);
        String type2 = types.get(new OffsetRange(24, 33));
        assertEquals("\\Foo\\Cart", type2);
    }

    public void testNotNullTypeAnnotation_01() throws Exception {
        try {
            AnnotationUtils.isTypeAnnotation(null, "");
            fail();
        } catch (NullPointerException ex) {}
    }

    public void testNotNullTypeAnnotation_02() throws Exception {
        try {
            AnnotationUtils.isTypeAnnotation("", null);
            fail();
        } catch (NullPointerException ex) {}
    }

    public void testNotNullExtracParamTypes() throws Exception {
        try {
            AnnotationUtils.extractTypesFromParameters(null, Collections.EMPTY_SET);
            fail();
        } catch (NullPointerException ex) {}
    }

    public void testNotNullExtracParamTypesRegexs() throws Exception {
        try {
            AnnotationUtils.extractTypesFromParameters("", null);
            fail();
        } catch (NullPointerException ex) {}
    }

}
