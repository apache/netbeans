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
package org.netbeans.modules.php.apigen.annotations.parser;

import java.util.Collections;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine.ParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class SeeLineParserTest extends NbTestCase {

    private SeeLineParser parser;

    public SeeLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parser = new SeeLineParser();
    }

    public void testIsAnnotationLineParser() throws Exception {
        assertTrue(parser instanceof AnnotationLineParser);
    }

    public void testReturnValueIsSeeParsedLine() throws Exception {
        assertTrue(parser.parse("see") instanceof ParsedLine);
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see");
        assertEquals("see", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see Foo");
        assertEquals("see", parsedLine.getName());
        assertEquals("Foo", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(4, offsetRange.getStart());
            assertEquals(7, offsetRange.getEnd());
            assertEquals("Foo", typeName);
        }
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see\tFoo");
        assertEquals("see", parsedLine.getName());
        assertEquals("Foo", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(4, offsetRange.getStart());
            assertEquals(7, offsetRange.getEnd());
            assertEquals("Foo", typeName);
        }
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see Foo::bar()");
        assertEquals("see", parsedLine.getName());
        assertEquals("Foo::bar()", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(4, offsetRange.getStart());
            assertEquals(7, offsetRange.getEnd());
            assertEquals("Foo", typeName);
        }
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see\t\tFoo::bar()");
        assertEquals("see", parsedLine.getName());
        assertEquals("Foo::bar()", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(5, offsetRange.getStart());
            assertEquals(8, offsetRange.getEnd());
            assertEquals("Foo", typeName);
        }
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see Foo::$myProperty");
        assertEquals("see", parsedLine.getName());
        assertEquals("Foo::$myProperty", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(4, offsetRange.getStart());
            assertEquals(7, offsetRange.getEnd());
            assertEquals("Foo", typeName);
        }
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see \\Omg\\My\\Foo::$myProperty");
        assertEquals("see", parsedLine.getName());
        assertEquals("\\Omg\\My\\Foo::$myProperty", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(4, offsetRange.getStart());
            assertEquals(15, offsetRange.getEnd());
            assertEquals("\\Omg\\My\\Foo", typeName);
        }
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see Omg\\My\\Foo::$myProperty");
        assertEquals("see", parsedLine.getName());
        assertEquals("Omg\\My\\Foo::$myProperty", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(4, offsetRange.getStart());
            assertEquals(14, offsetRange.getEnd());
            assertEquals("Omg\\My\\Foo", typeName);
        }
    }

    public void testValidUseCase_09() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see   \t  Omg\\My\\Foo::$myProperty   \t\t  ");
        assertEquals("see", parsedLine.getName());
        assertEquals("Omg\\My\\Foo::$myProperty", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(9, offsetRange.getStart());
            assertEquals(19, offsetRange.getEnd());
            assertEquals("Omg\\My\\Foo", typeName);
        }
    }

    public void testValidUseCase_10() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see Omg\\My\\Foo::$myProperty My super description.");
        assertEquals("see", parsedLine.getName());
        assertEquals("Omg\\My\\Foo::$myProperty My super description.", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        for (Map.Entry<OffsetRange, String> entry : types.entrySet()) {
            OffsetRange offsetRange = entry.getKey();
            String typeName = entry.getValue();
            assertEquals(4, offsetRange.getStart());
            assertEquals(14, offsetRange.getEnd());
            assertEquals("Omg\\My\\Foo", typeName);
        }
    }

    public void testInvalidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("see http://www.example.com");
        assertEquals("see", parsedLine.getName());
        assertEquals("http://www.example.com", parsedLine.getDescription());
        assertEquals(Collections.EMPTY_MAP, parsedLine.getTypes());
    }

    public void testInvalidUseCase_02() throws Exception {
        assertNull(parser.parse("omg"));
    }

}
