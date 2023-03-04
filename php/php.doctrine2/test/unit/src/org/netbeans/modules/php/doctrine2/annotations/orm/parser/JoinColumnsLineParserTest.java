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
public class JoinColumnsLineParserTest extends NbTestCase {
    private EncapsulatingAnnotationLineParser parser;

    public JoinColumnsLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.parser = new EncapsulatingAnnotationLineParser();
    }

    public void testIsAnnotationParser() throws Exception {
        assertTrue(parser instanceof AnnotationLineParser);
    }

    public void testReturnValueIsJoinColumnsParsedLine_01() throws Exception {
        assertTrue(parser.parse("JoinColumns") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsJoinColumnsParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\JoinColumns") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsJoinColumnsParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\JoinColumns") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsJoinColumnsParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\JoinColumns(name=\"user\")") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("JoinColumnss"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("JoinColumns");
        assertEquals("JoinColumns", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 11));
        assertEquals("JoinColumns", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("JoinColumns   ");
        assertEquals("JoinColumns", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 11));
        assertEquals("JoinColumns", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("JoinColumns\t\t  ");
        assertEquals("JoinColumns", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 11));
        assertEquals("JoinColumns", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("JoinColumns(name=\"user\")");
        assertEquals("JoinColumns", parsedLine.getName());
        assertEquals("(name=\"user\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 11));
        assertEquals("JoinColumns", type1);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\JoinColumns(name=\"user\")  \t");
        assertEquals("JoinColumns", parsedLine.getName());
        assertEquals("(name=\"user\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 23));
        assertEquals("Annotations\\JoinColumns", type1);
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\JoinColumns(name=\"user\")  \t");
        assertEquals("JoinColumns", parsedLine.getName());
        assertEquals("(name=\"user\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 20));
        assertEquals("\\Foo\\Bar\\JoinColumns", type1);
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("joincolumns");
        assertEquals("JoinColumns", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 11));
        assertEquals("joincolumns", type1);
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\joincolumns(name=\"user\")  \t");
        assertEquals("JoinColumns", parsedLine.getName());
        assertEquals("(name=\"user\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 20));
        assertEquals("\\Foo\\Bar\\joincolumns", type1);
    }

    public void testValidUseCase_09() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\JoinColumns({@JoinColumn(name=\"user_unique\"), @JoinColumn(name=\"user_unique\")})");
        assertEquals("JoinColumns", parsedLine.getName());
        assertEquals("({@JoinColumn(name=\"user_unique\"), @JoinColumn(name=\"user_unique\")})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(3, types.size());
        String type1 = types.get(new OffsetRange(0, 20));
        assertEquals("\\Foo\\Bar\\JoinColumns", type1);
        String type2 = types.get(new OffsetRange(23, 33));
        assertEquals("JoinColumn", type2);
        String type3 = types.get(new OffsetRange(56, 66));
        assertEquals("JoinColumn", type3);
    }

    public void testValidUseCase_10() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\joincolumns({@joincolumn(name=\"user_unique\"), @joincolumn(name=\"user_unique\")})");
        assertEquals("JoinColumns", parsedLine.getName());
        assertEquals("({@joincolumn(name=\"user_unique\"), @joincolumn(name=\"user_unique\")})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(3, types.size());
        String type1 = types.get(new OffsetRange(0, 20));
        assertEquals("\\Foo\\Bar\\joincolumns", type1);
        String type2 = types.get(new OffsetRange(23, 33));
        assertEquals("joincolumn", type2);
        String type3 = types.get(new OffsetRange(56, 66));
        assertEquals("joincolumn", type3);
    }

    public void testValidUseCase_11() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\JoinColumns({@Baz\\JoinColumn(name=\"user_unique\"), @JoinColumn(name=\"user_unique\")})");
        assertEquals("JoinColumns", parsedLine.getName());
        assertEquals("({@Baz\\JoinColumn(name=\"user_unique\"), @JoinColumn(name=\"user_unique\")})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(3, types.size());
        String type1 = types.get(new OffsetRange(0, 20));
        assertEquals("\\Foo\\Bar\\JoinColumns", type1);
        String type2 = types.get(new OffsetRange(23, 37));
        assertEquals("Baz\\JoinColumn", type2);
        String type3 = types.get(new OffsetRange(60, 70));
        assertEquals("JoinColumn", type3);
    }

}
