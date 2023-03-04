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
public class ManyToManyLineParserTest extends NbTestCase {
    private TypedParametersAnnotationLineParser parser;

    public ManyToManyLineParserTest(String name) {
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

    public void testReturnValueIsManyToManyParsedLine_01() throws Exception {
        assertTrue(parser.parse("ManyToMany") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsManyToManyParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\ManyToMany") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsManyToManyParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\ManyToMany") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsManyToManyParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\ManyToMany(targetEntity=\"Group\", inversedBy=\"features\")") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("ManyToManys"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ManyToMany");
        assertEquals("ManyToMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 10));
        assertEquals("ManyToMany", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ManyToMany   ");
        assertEquals("ManyToMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 10));
        assertEquals("ManyToMany", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ManyToMany\t\t  ");
        assertEquals("ManyToMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 10));
        assertEquals("ManyToMany", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ManyToMany(targetEntity=\"Group\", inversedBy=\"features\")");
        assertEquals("ManyToMany", parsedLine.getName());
        assertEquals("(targetEntity=\"Group\", inversedBy=\"features\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 10));
        assertEquals("ManyToMany", type1);
        String type2 = types.get(new OffsetRange(25, 30));
        assertEquals("Group", type2);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\ManyToMany(targetEntity=\"Group\", inversedBy=\"features\")  \t");
        assertEquals("ManyToMany", parsedLine.getName());
        assertEquals("(targetEntity=\"Group\", inversedBy=\"features\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 22));
        assertEquals("Annotations\\ManyToMany", type1);
        String type2 = types.get(new OffsetRange(37, 42));
        assertEquals("Group", type2);
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\ManyToMany(targetEntity=\"Group\", inversedBy=\"features\")  \t");
        assertEquals("ManyToMany", parsedLine.getName());
        assertEquals("(targetEntity=\"Group\", inversedBy=\"features\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 19));
        assertEquals("\\Foo\\Bar\\ManyToMany", type1);
        String type2 = types.get(new OffsetRange(34, 39));
        assertEquals("Group", type2);
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("manytomany");
        assertEquals("ManyToMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 10));
        assertEquals("manytomany", type1);
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\manytomany(targetEntity=\"Group\", inversedBy=\"features\")  \t");
        assertEquals("ManyToMany", parsedLine.getName());
        assertEquals("(targetEntity=\"Group\", inversedBy=\"features\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 19));
        assertEquals("\\Foo\\Bar\\manytomany", type1);
        String type2 = types.get(new OffsetRange(34, 39));
        assertEquals("Group", type2);
    }

    public void testValidUseCase_09() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\manytomany(targetEntity=\"\\Foo\\Bar\\Group::class\", inversedBy=\"features\")  \t");
        assertEquals("ManyToMany", parsedLine.getName());
        assertEquals("(targetEntity=\"\\Foo\\Bar\\Group::class\", inversedBy=\"features\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 19));
        assertEquals("\\Foo\\Bar\\manytomany", type1);
        String type2 = types.get(new OffsetRange(34, 48));
        assertEquals("\\Foo\\Bar\\Group", type2);
    }

}
