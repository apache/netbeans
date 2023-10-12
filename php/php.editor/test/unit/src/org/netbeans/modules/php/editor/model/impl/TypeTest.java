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

package org.netbeans.modules.php.editor.model.impl;

import static org.junit.Assert.assertArrayEquals;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class TypeTest extends ModelTestBase {

    public TypeTest(String testName) {
        super(testName);
    }

    public void testIsArray() throws Exception {
        assertTrue(Type.isArray("array"));
        assertTrue(Type.isArray("Bar[]"));
        assertTrue(Type.isArray("\\Foo\\Bar[]"));
    }

    public void testIsNotArray() throws Exception {
        assertFalse(Type.isArray(null));
        assertFalse(Type.isArray("string"));
        assertFalse(Type.isArray("Bar"));
        assertFalse(Type.isArray("\\Foo\\Bar"));
    }

    public void testSplitTypes_01() throws Exception {
        String declaredTypes = "Foo";
        assertArrayEquals(new String[]{"Foo"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypes_02() throws Exception {
        String declaredTypes = "\\Foo";
        assertArrayEquals(new String[]{"\\Foo"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypes_03() throws Exception {
        String declaredTypes = "  \\Foo\\Bar  ";
        assertArrayEquals(new String[]{"\\Foo\\Bar"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesNullableType_01() throws Exception {
        String declaredTypes = "?Foo";
        assertArrayEquals(new String[]{"?Foo"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesNullableType_02() throws Exception {
        String declaredTypes = "?\\Foo\\Bar";
        assertArrayEquals(new String[]{"?\\Foo\\Bar"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesUnionType_01() throws Exception {
        String declaredTypes = "string|int|null";
        assertArrayEquals(new String[]{"string", "int", "null"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesUnionType_02() throws Exception {
        String declaredTypes = "\\Foo\\Bar|\\Baz|null";
        assertArrayEquals(new String[]{"\\Foo\\Bar", "\\Baz", "null"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesUnionType_03() throws Exception {
        String declaredTypes = "\\Foo\\Bar   |  \\Baz | null";
        assertArrayEquals(new String[]{"\\Foo\\Bar", "\\Baz", "null"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesIntersectionType_01() throws Exception {
        String declaredTypes = "string&int&null";
        assertArrayEquals(new String[]{"string", "int", "null"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesIntersectionType_02() throws Exception {
        String declaredTypes = "\\Foo\\Bar&\\Baz&null";
        assertArrayEquals(new String[]{"\\Foo\\Bar", "\\Baz", "null"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesIntersectionType_03() throws Exception {
        String declaredTypes = "\\Foo\\Bar   &  \\Baz & null";
        assertArrayEquals(new String[]{"\\Foo\\Bar", "\\Baz", "null"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesDNFType_01() throws Exception {
        String declaredTypes = "(X&Y)|Z";
        assertArrayEquals(new String[]{"X", "Y", "Z"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesDNFType_02() throws Exception {
        String declaredTypes = "X|(Y&Z)";
        assertArrayEquals(new String[]{"X", "Y", "Z"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesDNFType_03() throws Exception {
        String declaredTypes = "(X&Y)|(Y&Z)";
        assertArrayEquals(new String[]{"X", "Y", "Y", "Z"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesDNFType_04() throws Exception {
        String declaredTypes = "X|(Y&Z)|Z";
        assertArrayEquals(new String[]{"X", "Y", "Z", "Z"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesDNFType_05() throws Exception {
        String declaredTypes = "(\\NS1\\Test1&\\NS2\\Test2)|\\Test3";
        assertArrayEquals(new String[]{"\\NS1\\Test1", "\\NS2\\Test2", "\\Test3"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesDNFType_06() throws Exception {
        String declaredTypes = "\\NS3\\Test3  |  (\\NS1\\Test1&\\NS2\\Test2)";
        assertArrayEquals(new String[]{"\\NS3\\Test3","\\NS1\\Test1", "\\NS2\\Test2"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesDNFType_07() throws Exception {
        String declaredTypes = "(\\NS3\\Test3&Test1)  |  (\\NS1\\Test1&\\NS2\\Test2)";
        assertArrayEquals(new String[]{"\\NS3\\Test3", "Test1", "\\NS1\\Test1", "\\NS2\\Test2"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesDNFType_08() throws Exception {
        String declaredTypes = "(\\NS3\\Test3&Test1)|(\\NS1\\Test1&\\NS2\\Test2)";
        assertArrayEquals(new String[]{"\\NS3\\Test3", "Test1", "\\NS1\\Test1", "\\NS2\\Test2"}, Type.splitTypes(declaredTypes));
    }

    public void testSplitTypesDNFType_09() throws Exception {
        String declaredTypes = "  (\\NS3\\Test3&Test1)|(\\NS1\\Test1&\\NS2\\Test2)  ";
        assertArrayEquals(new String[]{"\\NS3\\Test3", "Test1", "\\NS1\\Test1", "\\NS2\\Test2"}, Type.splitTypes(declaredTypes));
    }

    public void testToTypeTemplate_01() throws Exception {
        String declaredTypes = "Foo";
        assertEquals("%s", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplate_02() throws Exception {
        String declaredTypes = "\\Foo";
        assertEquals("%s", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplate_03() throws Exception {
        String declaredTypes = "  \\Foo\\Bar  ";
        assertEquals("%s", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateNullableType_01() throws Exception {
        String declaredTypes = "?Foo";
        assertEquals("?%s", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateNullableType_02() throws Exception {
        String declaredTypes = "?\\Foo\\Bar";
        assertEquals("?%s", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateUnionType_01() throws Exception {
        String declaredTypes = "string|int|null";
        assertEquals("%s|%s|%s", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateUnionType_02() throws Exception {
        String declaredTypes = "\\Foo\\Bar|\\Baz|null";
        assertEquals("%s|%s|%s", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateUnionType_03() throws Exception {
        String declaredTypes = "\\Foo\\Bar   |  \\Baz | null";
        assertEquals("%s|%s|%s", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateIntersectionType_01() throws Exception {
        String declaredTypes = "string&int&null";
        assertEquals("%s&%s&%s", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateIntersectionType_02() throws Exception {
        String declaredTypes = "\\Foo\\Bar&\\Baz&null";
        assertEquals("%s&%s&%s", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateIntersectionType_03() throws Exception {
        String declaredTypes = "\\Foo\\Bar   &  \\Baz & null";
        assertEquals("%s&%s&%s", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateDNFType_01() throws Exception {
        String declaredTypes = "(X&Y)|Z";
        assertEquals("(%s&%s)|%s", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateDNFType_02() throws Exception {
        String declaredTypes = "X|(Y&Z)";
        assertEquals("%s|(%s&%s)", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateDNFType_03() throws Exception {
        String declaredTypes = "(X&Y)|(Y&Z)";
        assertEquals("(%s&%s)|(%s&%s)", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateDNFType_04() throws Exception {
        String declaredTypes = "X|(Y&Z)|Z";
        assertEquals("%s|(%s&%s)|%s", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateDNFType_05() throws Exception {
        String declaredTypes = "(\\NS1\\Test1&\\NS2\\Test2)|\\Test3";
        assertEquals("(%s&%s)|%s", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateDNFType_06() throws Exception {
        String declaredTypes = "\\NS3\\Test3  |  (\\NS1\\Test1&\\NS2\\Test2)";
        assertEquals("%s|(%s&%s)", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateDNFType_07() throws Exception {
        String declaredTypes = "(\\NS3\\Test3&Test1)  |  (\\NS1\\Test1&\\NS2\\Test2)";
        assertEquals("(%s&%s)|(%s&%s)", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateDNFType_08() throws Exception {
        String declaredTypes = "(\\NS3\\Test3&Test1)|(\\NS1\\Test1&\\NS2\\Test2)";
        assertEquals("(%s&%s)|(%s&%s)", Type.toTypeTemplate(declaredTypes));
    }

    public void testToTypeTemplateDNFType_09() throws Exception {
        String declaredTypes = "  (\\NS3\\Test3&Test1)|(\\NS1\\Test1&\\NS2\\Test2)  ";
        assertEquals("(%s&%s)|(%s&%s)", Type.toTypeTemplate(declaredTypes));
    }
}
