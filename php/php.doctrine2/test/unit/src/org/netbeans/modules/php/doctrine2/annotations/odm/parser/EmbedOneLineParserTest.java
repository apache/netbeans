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
public class EmbedOneLineParserTest extends NbTestCase {
    private TypedParametersAnnotationLineParser parser;

    public EmbedOneLineParserTest(String name) {
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

    public void testReturnValueIsEmbedOneParsedLine_01() throws Exception {
        assertTrue(parser.parse("EmbedOne") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsEmbedOneParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\EmbedOne") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsEmbedOneParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\EmbedOne") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsEmbedOneParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\EmbedOne(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("EmbedOnes"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("EmbedOne");
        assertEquals("EmbedOne", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 8));
        assertEquals("EmbedOne", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("EmbedOne   ");
        assertEquals("EmbedOne", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 8));
        assertEquals("EmbedOne", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("EmbedOne\t\t  ");
        assertEquals("EmbedOne", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 8));
        assertEquals("EmbedOne", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("EmbedOne(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})");
        assertEquals("EmbedOne", parsedLine.getName());
        assertEquals("(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 8));
        assertEquals("EmbedOne", type1);
        String type2 = types.get(new OffsetRange(25, 30));
        assertEquals("Money", type2);
        String type3 = types.get(new OffsetRange(102, 119));
        assertEquals("Documents\\BookTag", type3);
        String type4 = types.get(new OffsetRange(130, 147));
        assertEquals("Documents\\SongTag", type4);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\EmbedOne(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})  \t");
        assertEquals("EmbedOne", parsedLine.getName());
        assertEquals("(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 20));
        assertEquals("Annotations\\EmbedOne", type1);
        String type2 = types.get(new OffsetRange(37, 42));
        assertEquals("Money", type2);
        String type3 = types.get(new OffsetRange(114, 131));
        assertEquals("Documents\\BookTag", type3);
        String type4 = types.get(new OffsetRange(142, 159));
        assertEquals("Documents\\SongTag", type4);
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\EmbedOne(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})  \t");
        assertEquals("EmbedOne", parsedLine.getName());
        assertEquals("(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 17));
        assertEquals("\\Foo\\Bar\\EmbedOne", type1);
        String type2 = types.get(new OffsetRange(34, 39));
        assertEquals("Money", type2);
        String type3 = types.get(new OffsetRange(111, 128));
        assertEquals("Documents\\BookTag", type3);
        String type4 = types.get(new OffsetRange(139, 156));
        assertEquals("Documents\\SongTag", type4);
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("embedone");
        assertEquals("EmbedOne", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 8));
        assertEquals("embedone", type1);
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\embedone(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})  \t");
        assertEquals("EmbedOne", parsedLine.getName());
        assertEquals("(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 17));
        assertEquals("\\Foo\\Bar\\embedone", type1);
        String type2 = types.get(new OffsetRange(34, 39));
        assertEquals("Money", type2);
        String type3 = types.get(new OffsetRange(111, 128));
        assertEquals("Documents\\BookTag", type3);
        String type4 = types.get(new OffsetRange(139, 156));
        assertEquals("Documents\\SongTag", type4);
    }

}
