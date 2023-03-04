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

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ManyToOneLineParserTest extends NbTestCase {
    private TypedParametersAnnotationLineParser parser;

    public ManyToOneLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.parser = new TypedParametersAnnotationLineParser();
    }

    public void testIsAnnotationParser() throws Exception {
        assertTrue(parser instanceof AnnotationLineParser);
    }

    public void testReturnValueIsManyToOneParsedLine_01() throws Exception {
        assertTrue(parser.parse("ManyToOne") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsManyToOneParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\ManyToOne") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsManyToOneParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\ManyToOne") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsManyToOneParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\ManyToOne(targetEntity=\"Cart\", cascade={\"all\"}, fetch=\"EAGER\")") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("ManyToOnes"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ManyToOne");
        assertEquals("ManyToOne", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("ManyToOne", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ManyToOne   ");
        assertEquals("ManyToOne", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("ManyToOne", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ManyToOne\t\t  ");
        assertEquals("ManyToOne", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("ManyToOne", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ManyToOne(targetEntity=\"Cart\", cascade={\"all\"}, fetch=\"EAGER\")");
        assertEquals("ManyToOne", parsedLine.getName());
        assertEquals("(targetEntity=\"Cart\", cascade={\"all\"}, fetch=\"EAGER\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("ManyToOne", type1);
        String type2 = types.get(new OffsetRange(24, 28));
        assertEquals("Cart", type2);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\ManyToOne(targetEntity=\"Cart\", cascade={\"all\"}, fetch=\"EAGER\")  \t");
        assertEquals("ManyToOne", parsedLine.getName());
        assertEquals("(targetEntity=\"Cart\", cascade={\"all\"}, fetch=\"EAGER\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 21));
        assertEquals("Annotations\\ManyToOne", type1);
        String type2 = types.get(new OffsetRange(36, 40));
        assertEquals("Cart", type2);
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\ManyToOne(targetEntity=\"Cart\", cascade={\"all\"}, fetch=\"EAGER\")  \t");
        assertEquals("ManyToOne", parsedLine.getName());
        assertEquals("(targetEntity=\"Cart\", cascade={\"all\"}, fetch=\"EAGER\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 18));
        assertEquals("\\Foo\\Bar\\ManyToOne", type1);
        String type2 = types.get(new OffsetRange(33, 37));
        assertEquals("Cart", type2);
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("manytoone");
        assertEquals("ManyToOne", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("manytoone", type1);
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\manytoone(targetEntity=\"Cart\", cascade={\"all\"}, fetch=\"EAGER\")  \t");
        assertEquals("ManyToOne", parsedLine.getName());
        assertEquals("(targetEntity=\"Cart\", cascade={\"all\"}, fetch=\"EAGER\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 18));
        assertEquals("\\Foo\\Bar\\manytoone", type1);
        String type2 = types.get(new OffsetRange(33, 37));
        assertEquals("Cart", type2);
    }

    public void testValidUseCase_09() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\manytoone(targetEntity=\"Cart::class\", cascade={\"all\"}, fetch=\"EAGER\")  \t");
        assertEquals("ManyToOne", parsedLine.getName());
        assertEquals("(targetEntity=\"Cart::class\", cascade={\"all\"}, fetch=\"EAGER\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 18));
        assertEquals("\\Foo\\Bar\\manytoone", type1);
        String type2 = types.get(new OffsetRange(33, 37));
        assertEquals("Cart", type2);
    }

}
