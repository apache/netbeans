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
public class OneToManyLineParserTest extends NbTestCase {
    private TypedParametersAnnotationLineParser parser;

    public OneToManyLineParserTest(String name) {
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

    public void testReturnValueIsOneToManyParsedLine_01() throws Exception {
        assertTrue(parser.parse("OneToMany") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsOneToManyParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\OneToMany") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsOneToManyParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\OneToMany") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsOneToManyParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\OneToMany(targetEntity=\"Phonenumber\", mappedBy=\"user\", cascade={\"persist\", \"remove\", \"merge\"}, orphanRemoval=true)") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("OneToManys"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("OneToMany");
        assertEquals("OneToMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("OneToMany", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("OneToMany   ");
        assertEquals("OneToMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("OneToMany", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("OneToMany\t\t  ");
        assertEquals("OneToMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("OneToMany", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("OneToMany(targetEntity=\"Phonenumber\", mappedBy=\"user\", cascade={\"persist\", \"remove\", \"merge\"}, orphanRemoval=true)");
        assertEquals("OneToMany", parsedLine.getName());
        assertEquals("(targetEntity=\"Phonenumber\", mappedBy=\"user\", cascade={\"persist\", \"remove\", \"merge\"}, orphanRemoval=true)", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("OneToMany", type1);
        String type2 = types.get(new OffsetRange(24, 35));
        assertEquals("Phonenumber", type2);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\OneToMany(targetEntity=\"Phonenumber\", mappedBy=\"user\", cascade={\"persist\", \"remove\", \"merge\"}, orphanRemoval=true)  \t");
        assertEquals("OneToMany", parsedLine.getName());
        assertEquals("(targetEntity=\"Phonenumber\", mappedBy=\"user\", cascade={\"persist\", \"remove\", \"merge\"}, orphanRemoval=true)", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 21));
        assertEquals("Annotations\\OneToMany", type1);
        String type2 = types.get(new OffsetRange(36, 47));
        assertEquals("Phonenumber", type2);
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\OneToMany(targetEntity=\"Phonenumber\", mappedBy=\"user\", cascade={\"persist\", \"remove\", \"merge\"}, orphanRemoval=true)  \t");
        assertEquals("OneToMany", parsedLine.getName());
        assertEquals("(targetEntity=\"Phonenumber\", mappedBy=\"user\", cascade={\"persist\", \"remove\", \"merge\"}, orphanRemoval=true)", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 18));
        assertEquals("\\Foo\\Bar\\OneToMany", type1);
        String type2 = types.get(new OffsetRange(33, 44));
        assertEquals("Phonenumber", type2);
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("onetomany");
        assertEquals("OneToMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("onetomany", type1);
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\onetomany(targetEntity=\"Phonenumber\", mappedBy=\"user\", cascade={\"persist\", \"remove\", \"merge\"}, orphanRemoval=true)  \t");
        assertEquals("OneToMany", parsedLine.getName());
        assertEquals("(targetEntity=\"Phonenumber\", mappedBy=\"user\", cascade={\"persist\", \"remove\", \"merge\"}, orphanRemoval=true)", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 18));
        assertEquals("\\Foo\\Bar\\onetomany", type1);
        String type2 = types.get(new OffsetRange(33, 44));
        assertEquals("Phonenumber", type2);
    }

    public void testValidUseCase_09() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\onetomany(targetEntity=\\Foo\\Bar\\Phonenumber::class, mappedBy=\"user\", cascade={\"persist\", \"remove\", \"merge\"}, orphanRemoval=true)  \t");
        assertEquals("OneToMany", parsedLine.getName());
        assertEquals("(targetEntity=\\Foo\\Bar\\Phonenumber::class, mappedBy=\"user\", cascade={\"persist\", \"remove\", \"merge\"}, orphanRemoval=true)", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(2, types.size());
        String type1 = types.get(new OffsetRange(0, 18));
        assertEquals("\\Foo\\Bar\\onetomany", type1);
        String type2 = types.get(new OffsetRange(32, 52));
        assertEquals("\\Foo\\Bar\\Phonenumber", type2);
    }

}
