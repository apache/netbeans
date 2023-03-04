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
package org.netbeans.modules.php.doctrine2.annotations.odm.parser;

import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.spi.annotation.AnnotationLineParser;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ReferenceOneLineParserTest extends NbTestCase {
    private TypedParametersAnnotationLineParser parser;

    public ReferenceOneLineParserTest(String name) {
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

    public void testReturnValueIsReferenceOneParsedLine_01() throws Exception {
        assertTrue(parser.parse("ReferenceOne") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsReferenceOneParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\ReferenceOne") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsReferenceOneParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\ReferenceOne") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsReferenceOneParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\ReferenceOne(targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("ReferenceOnes"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ReferenceOne");
        assertEquals("ReferenceOne", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 12));
        assertEquals("ReferenceOne", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ReferenceOne   ");
        assertEquals("ReferenceOne", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 12));
        assertEquals("ReferenceOne", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ReferenceOne\t\t  ");
        assertEquals("ReferenceOne", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 12));
        assertEquals("ReferenceOne", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ReferenceOne(targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})");
        assertEquals("ReferenceOne", parsedLine.getName());
        assertEquals("(targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 12));
        assertEquals("ReferenceOne", type1);
        String type2 = types.get(new OffsetRange(29, 43));
        assertEquals("Documents\\Item", type2);
        String type3 = types.get(new OffsetRange(114, 132));
        assertEquals("Documents\\BookItem", type3);
        String type4 = types.get(new OffsetRange(143, 161));
        assertEquals("Documents\\SongItem", type4);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\ReferenceOne(targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})  \t");
        assertEquals("ReferenceOne", parsedLine.getName());
        assertEquals("(targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 24));
        assertEquals("Annotations\\ReferenceOne", type1);
        String type2 = types.get(new OffsetRange(41, 55));
        assertEquals("Documents\\Item", type2);
        String type3 = types.get(new OffsetRange(126, 144));
        assertEquals("Documents\\BookItem", type3);
        String type4 = types.get(new OffsetRange(155, 173));
        assertEquals("Documents\\SongItem", type4);
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\ReferenceOne(targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})  \t");
        assertEquals("ReferenceOne", parsedLine.getName());
        assertEquals("(targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 21));
        assertEquals("\\Foo\\Bar\\ReferenceOne", type1);
        String type2 = types.get(new OffsetRange(38, 52));
        assertEquals("Documents\\Item", type2);
        String type3 = types.get(new OffsetRange(123, 141));
        assertEquals("Documents\\BookItem", type3);
        String type4 = types.get(new OffsetRange(152, 170));
        assertEquals("Documents\\SongItem", type4);
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("referenceone");
        assertEquals("ReferenceOne", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 12));
        assertEquals("referenceone", type1);
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\referenceone(targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})  \t");
        assertEquals("ReferenceOne", parsedLine.getName());
        assertEquals("(targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 21));
        assertEquals("\\Foo\\Bar\\referenceone", type1);
        String type2 = types.get(new OffsetRange(38, 52));
        assertEquals("Documents\\Item", type2);
        String type3 = types.get(new OffsetRange(123, 141));
        assertEquals("Documents\\BookItem", type3);
        String type4 = types.get(new OffsetRange(152, 170));
        assertEquals("Documents\\SongItem", type4);
    }

}
