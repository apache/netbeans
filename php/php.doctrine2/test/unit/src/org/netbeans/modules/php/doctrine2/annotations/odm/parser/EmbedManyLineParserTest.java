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
public class EmbedManyLineParserTest extends NbTestCase {
    private TypedParametersAnnotationLineParser parser;

    public EmbedManyLineParserTest(String name) {
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

    public void testReturnValueIsEmbedManyParsedLine_01() throws Exception {
        assertTrue(parser.parse("EmbedMany") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsEmbedManyParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\EmbedMany") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsEmbedManyParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\EmbedMany") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsEmbedManyParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\EmbedMany(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("EmbedManys"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("EmbedMany");
        assertEquals("EmbedMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("EmbedMany", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("EmbedMany   ");
        assertEquals("EmbedMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("EmbedMany", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("EmbedMany\t\t  ");
        assertEquals("EmbedMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("EmbedMany", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("EmbedMany(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})");
        assertEquals("EmbedMany", parsedLine.getName());
        assertEquals("(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("EmbedMany", type1);
        String type2 = types.get(new OffsetRange(26, 31));
        assertEquals("Money", type2);
        String type3 = types.get(new OffsetRange(103, 120));
        assertEquals("Documents\\BookTag", type3);
        String type4 = types.get(new OffsetRange(131, 148));
        assertEquals("Documents\\SongTag", type4);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\EmbedMany(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})  \t");
        assertEquals("EmbedMany", parsedLine.getName());
        assertEquals("(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 21));
        assertEquals("Annotations\\EmbedMany", type1);
        String type2 = types.get(new OffsetRange(38, 43));
        assertEquals("Money", type2);
        String type3 = types.get(new OffsetRange(115, 132));
        assertEquals("Documents\\BookTag", type3);
        String type4 = types.get(new OffsetRange(143, 160));
        assertEquals("Documents\\SongTag", type4);
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\EmbedMany(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})  \t");
        assertEquals("EmbedMany", parsedLine.getName());
        assertEquals("(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 18));
        assertEquals("\\Foo\\Bar\\EmbedMany", type1);
        String type2 = types.get(new OffsetRange(35, 40));
        assertEquals("Money", type2);
        String type3 = types.get(new OffsetRange(112, 129));
        assertEquals("Documents\\BookTag", type3);
        String type4 = types.get(new OffsetRange(140, 157));
        assertEquals("Documents\\SongTag", type4);
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("embedmany");
        assertEquals("EmbedMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 9));
        assertEquals("embedmany", type1);
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\embedmany(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})  \t");
        assertEquals("EmbedMany", parsedLine.getName());
        assertEquals("(targetDocument=\"Money\", strategy=\"set\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookTag\", \"song\"=\"Documents\\SongTag\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 18));
        assertEquals("\\Foo\\Bar\\embedmany", type1);
        String type2 = types.get(new OffsetRange(35, 40));
        assertEquals("Money", type2);
        String type3 = types.get(new OffsetRange(112, 129));
        assertEquals("Documents\\BookTag", type3);
        String type4 = types.get(new OffsetRange(140, 157));
        assertEquals("Documents\\SongTag", type4);
    }

}
