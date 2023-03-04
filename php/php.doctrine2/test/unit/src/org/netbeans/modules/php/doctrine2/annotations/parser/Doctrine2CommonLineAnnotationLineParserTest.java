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
package org.netbeans.modules.php.doctrine2.annotations.parser;

import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class Doctrine2CommonLineAnnotationLineParserTest extends NbTestCase {
    private AnnotationLineParser parser;

    public Doctrine2CommonLineAnnotationLineParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.parser = Doctrine2CommonLineAnnotationLineParser.getDefault();
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@Index(name=\"user_idx\", columns={\"email\"})} ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("indexes={@Index(name=\"user_idx\", columns={\"email\"})}", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(14, 19));
        assertEquals("Index", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@Foo\\Index(name=\"user_idx\", columns={\"email\"})} ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("indexes={@Foo\\Index(name=\"user_idx\", columns={\"email\"})}", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(14, 23));
        assertEquals("Foo\\Index", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@UniqueConstraint(name=\"user_idx\", columns={\"email\"})} ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("indexes={@UniqueConstraint(name=\"user_idx\", columns={\"email\"})}", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(14, 30));
        assertEquals("UniqueConstraint", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@Foo\\UniqueConstraint(name=\"user_idx\", columns={\"email\"})} ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("indexes={@Foo\\UniqueConstraint(name=\"user_idx\", columns={\"email\"})}", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(14, 34));
        assertEquals("Foo\\UniqueConstraint", type1);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@JoinColumn(name=\"user_idx\", columns={\"email\"})} ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("indexes={@JoinColumn(name=\"user_idx\", columns={\"email\"})}", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(14, 24));
        assertEquals("JoinColumn", type1);
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@Foo\\JoinColumn(name=\"user_idx\", columns={\"email\"})} ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("indexes={@Foo\\JoinColumn(name=\"user_idx\", columns={\"email\"})}", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(14, 28));
        assertEquals("Foo\\JoinColumn", type1);
    }

    public void testInvalidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    indexes={@Blah\\Blah(name=\"user_idx\", columns={\"email\"})} ");
        assertNull(parsedLine);
    }

    public void testWithTypedParam_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    repositoryClass=\"MyProject\\UserRepository\", indexes={ @Index(keys={\"username\"=\"desc\"}, options={\"unique\"=true}) }, ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("repositoryClass=\"MyProject\\UserRepository\", indexes={ @Index(keys={\"username\"=\"desc\"}, options={\"unique\"=true}) },", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(21, 45));
        assertEquals("MyProject\\UserRepository", type1);
        String type2 = types.get(new OffsetRange(59, 64));
        assertEquals("Index", type2);
    }

    public void testWithTypedParam_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    targetDocument=\"MyProject\\UserRepository\", indexes={ @Index(keys={\"username\"=\"desc\"}, options={\"unique\"=true}) }, ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("targetDocument=\"MyProject\\UserRepository\", indexes={ @Index(keys={\"username\"=\"desc\"}, options={\"unique\"=true}) },", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(20, 44));
        assertEquals("MyProject\\UserRepository", type1);
        String type2 = types.get(new OffsetRange(58, 63));
        assertEquals("Index", type2);
    }

    public void testWithTypedParam_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    targetEntity=\"MyProject\\UserRepository\", indexes={ @Index(keys={\"username\"=\"desc\"}, options={\"unique\"=true}) }, ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("targetEntity=\"MyProject\\UserRepository\", indexes={ @Index(keys={\"username\"=\"desc\"}, options={\"unique\"=true}) },", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(18, 42));
        assertEquals("MyProject\\UserRepository", type1);
        String type2 = types.get(new OffsetRange(56, 61));
        assertEquals("Index", type2);
    }

    public void testWithTypedParam_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("    targetEntity=MyProject\\UserRepository::class, indexes={ @Index(keys={\"username\"=\"desc\"}, options={\"unique\"=true}) }, ");
        assertNotNull(parsedLine);
        assertEquals("", parsedLine.getName());
        assertEquals("targetEntity=MyProject\\UserRepository::class, indexes={ @Index(keys={\"username\"=\"desc\"}, options={\"unique\"=true}) },", parsedLine.getDescription());
        assertFalse(parsedLine.startsWithAnnotation());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(17, 41));
        assertEquals("MyProject\\UserRepository", type1);
        String type2 = types.get(new OffsetRange(61, 66));
        assertEquals("Index", type2);
    }

}
