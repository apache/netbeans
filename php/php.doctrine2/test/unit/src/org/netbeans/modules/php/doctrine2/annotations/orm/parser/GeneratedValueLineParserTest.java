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
package org.netbeans.modules.php.doctrine2.annotations.orm.parser;

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
public class GeneratedValueLineParserTest extends NbTestCase {
    private ParameterizedAnnotationLineParser parser;

    public GeneratedValueLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.parser = new ParameterizedAnnotationLineParser();
    }

    public void testIsAnnotationParser() throws Exception {
        assertTrue(parser instanceof AnnotationLineParser);
    }

    public void testReturnValueIsGeneratedValueParsedLine_01() throws Exception {
        assertTrue(parser.parse("GeneratedValue") instanceof ParsedLine);
    }

    public void testReturnValueIsGeneratedValueParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\GeneratedValue") instanceof ParsedLine);
    }

    public void testReturnValueIsGeneratedValueParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\GeneratedValue") instanceof ParsedLine);
    }

    public void testReturnValueIsGeneratedValueParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\GeneratedValue(strategy=\"IDENTITY\")") instanceof ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("GeneratedValues"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("GeneratedValue");
        assertEquals("GeneratedValue", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 14));
        assertEquals("GeneratedValue", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("GeneratedValue   ");
        assertEquals("GeneratedValue", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 14));
        assertEquals("GeneratedValue", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("GeneratedValue\t\t  ");
        assertEquals("GeneratedValue", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 14));
        assertEquals("GeneratedValue", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("GeneratedValue(strategy=\"IDENTITY\")");
        assertEquals("GeneratedValue", parsedLine.getName());
        assertEquals("(strategy=\"IDENTITY\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 14));
        assertEquals("GeneratedValue", type1);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\GeneratedValue(strategy=\"IDENTITY\")  \t");
        assertEquals("GeneratedValue", parsedLine.getName());
        assertEquals("(strategy=\"IDENTITY\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 26));
        assertEquals("Annotations\\GeneratedValue", type1);

    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\GeneratedValue(strategy=\"IDENTITY\")  \t");
        assertEquals("GeneratedValue", parsedLine.getName());
        assertEquals("(strategy=\"IDENTITY\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 23));
        assertEquals("\\Foo\\Bar\\GeneratedValue", type1);
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("generatedvalue");
        assertEquals("GeneratedValue", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 14));
        assertEquals("generatedvalue", type1);
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\generatedvalue(strategy=\"IDENTITY\")  \t");
        assertEquals("GeneratedValue", parsedLine.getName());
        assertEquals("(strategy=\"IDENTITY\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 23));
        assertEquals("\\Foo\\Bar\\generatedvalue", type1);
    }

}
