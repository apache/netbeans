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
public class EntityLineParserTest extends NbTestCase {
    private TypedParametersAnnotationLineParser parser;

    public EntityLineParserTest(String name) {
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

    public void testReturnValueIsEntityParsedLine_01() throws Exception {
        assertTrue(parser.parse("Entity") instanceof ParsedLine);
    }

    public void testReturnValueIsEntityParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\Entity") instanceof ParsedLine);
    }

    public void testReturnValueIsEntityParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\Entity") instanceof ParsedLine);
    }

    public void testReturnValueIsEntityParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\Entity(repositoryClass=\"MyProject\\UserRepository\")") instanceof ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("Entitys"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Entity");
        assertEquals("Entity", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 6));
        assertEquals("Entity", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Entity   ");
        assertEquals("Entity", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 6));
        assertEquals("Entity", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Entity\t\t  ");
        assertEquals("Entity", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 6));
        assertEquals("Entity", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Entity(repositoryClass=\"MyProject\\UserRepository\")");
        assertEquals("Entity", parsedLine.getName());
        assertEquals("(repositoryClass=\"MyProject\\UserRepository\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 6));
        assertEquals("Entity", type1);
        String type2 = types.get(new OffsetRange(24, 48));
        assertEquals("MyProject\\UserRepository", type2);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\Entity(repositoryClass=\"MyProject\\UserRepository\")  \t");
        assertEquals("Entity", parsedLine.getName());
        assertEquals("(repositoryClass=\"MyProject\\UserRepository\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 18));
        assertEquals("Annotations\\Entity", type1);
        String type2 = types.get(new OffsetRange(36, 60));
        assertEquals("MyProject\\UserRepository", type2);
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\Entity(repositoryClass=\"MyProject\\UserRepository\")  \t");
        assertEquals("Entity", parsedLine.getName());
        assertEquals("(repositoryClass=\"MyProject\\UserRepository\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 15));
        assertEquals("\\Foo\\Bar\\Entity", type1);
        String type2 = types.get(new OffsetRange(33, 57));
        assertEquals("MyProject\\UserRepository", type2);
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("entity");
        assertEquals("Entity", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 6));
        assertEquals("entity", type1);
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\entity(repositoryClass=\"MyProject\\UserRepository\")  \t");
        assertEquals("Entity", parsedLine.getName());
        assertEquals("(repositoryClass=\"MyProject\\UserRepository\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 15));
        assertEquals("\\Foo\\Bar\\entity", type1);
        String type2 = types.get(new OffsetRange(33, 57));
        assertEquals("MyProject\\UserRepository", type2);
    }

    public void testValidUseCase_09() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\entity(repositoryClass=\"MyProject\\UserRepository::class\")  \t");
        assertEquals("Entity", parsedLine.getName());
        assertEquals("(repositoryClass=\"MyProject\\UserRepository::class\")", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 15));
        assertEquals("\\Foo\\Bar\\entity", type1);
        String type2 = types.get(new OffsetRange(33, 57));
        assertEquals("MyProject\\UserRepository", type2);
    }

}
