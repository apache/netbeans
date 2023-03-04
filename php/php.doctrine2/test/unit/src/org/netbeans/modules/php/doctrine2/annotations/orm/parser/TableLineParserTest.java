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
public class TableLineParserTest extends NbTestCase {
    private EncapsulatingAnnotationLineParser parser;

    public TableLineParserTest(String name) {
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

    public void testReturnValueIsTableParsedLine_01() throws Exception {
        assertTrue(parser.parse("Table") instanceof ParsedLine);
    }

    public void testReturnValueIsTableParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\Table") instanceof ParsedLine);
    }

    public void testReturnValueIsTableParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\Table") instanceof ParsedLine);
    }

    public void testReturnValueIsTableParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\Table(name=\"user\")") instanceof ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("Tables"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Table");
        assertEquals("Table", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 5));
        assertEquals("Table", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Table   ");
        assertEquals("Table", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 5));
        assertEquals("Table", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Table\t\t  ");
        assertEquals("Table", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 5));
        assertEquals("Table", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Table(name=\"user\")");
        assertEquals("Table", parsedLine.getName());
        assertEquals("(name=\"user\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 5));
        assertEquals("Table", type1);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\Table(name=\"user\")  \t");
        assertEquals("Table", parsedLine.getName());
        assertEquals("(name=\"user\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 17));
        assertEquals("Annotations\\Table", type1);
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\Table(name=\"user\")  \t");
        assertEquals("Table", parsedLine.getName());
        assertEquals("(name=\"user\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 14));
        assertEquals("\\Foo\\Bar\\Table", type1);
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("table");
        assertEquals("Table", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 5));
        assertEquals("table", type1);
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\table(name=\"user\")  \t");
        assertEquals("Table", parsedLine.getName());
        assertEquals("(name=\"user\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 14));
        assertEquals("\\Foo\\Bar\\table", type1);
    }

    public void testValidUseCase_09() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\Table(name=\"user\", uniqueConstraints={@UniqueConstraint(name=\"user_unique\",columns={\"username\"})}, indexes={@Index(name=\"user_idx\", columns={\"email\"})})");
        assertEquals("Table", parsedLine.getName());
        assertEquals("(name=\"user\", uniqueConstraints={@UniqueConstraint(name=\"user_unique\",columns={\"username\"})}, indexes={@Index(name=\"user_idx\", columns={\"email\"})})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(3, types.size());
        String type1 = types.get(new OffsetRange(0, 14));
        assertEquals("\\Foo\\Bar\\Table", type1);
        String type2 = types.get(new OffsetRange(48, 64));
        assertEquals("UniqueConstraint", type2);
        String type3 = types.get(new OffsetRange(118, 123));
        assertEquals("Index", type3);
    }

    public void testValidUseCase_10() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\table(name=\"user\", uniqueConstraints={@uniqueconstraint(name=\"user_unique\",columns={\"username\"})}, indexes={@index(name=\"user_idx\", columns={\"email\"})})");
        assertEquals("Table", parsedLine.getName());
        assertEquals("(name=\"user\", uniqueConstraints={@uniqueconstraint(name=\"user_unique\",columns={\"username\"})}, indexes={@index(name=\"user_idx\", columns={\"email\"})})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(3, types.size());
        String type1 = types.get(new OffsetRange(0, 14));
        assertEquals("\\Foo\\Bar\\table", type1);
        String type2 = types.get(new OffsetRange(48, 64));
        assertEquals("uniqueconstraint", type2);
        String type3 = types.get(new OffsetRange(118, 123));
        assertEquals("index", type3);
    }

    public void testValidUseCase_11() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\Table(name=\"user\", uniqueConstraints={@Baz\\UniqueConstraint(name=\"user_unique\",columns={\"username\"})}, indexes={@Index(name=\"user_idx\", columns={\"email\"})})");
        assertEquals("Table", parsedLine.getName());
        assertEquals("(name=\"user\", uniqueConstraints={@Baz\\UniqueConstraint(name=\"user_unique\",columns={\"username\"})}, indexes={@Index(name=\"user_idx\", columns={\"email\"})})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(3, types.size());
        String type1 = types.get(new OffsetRange(0, 14));
        assertEquals("\\Foo\\Bar\\Table", type1);
        String type2 = types.get(new OffsetRange(48, 68));
        assertEquals("Baz\\UniqueConstraint", type2);
        String type3 = types.get(new OffsetRange(122, 127));
        assertEquals("Index", type3);
    }

}
