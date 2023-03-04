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
public class ReferenceManyLineParserTest extends NbTestCase {
    private TypedParametersAnnotationLineParser parser;

    public ReferenceManyLineParserTest(String name) {
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

    public void testReturnValueIsReferenceManyParsedLine_01() throws Exception {
        assertTrue(parser.parse("ReferenceMany") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsReferenceManyParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\ReferenceMany") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsReferenceManyParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\ReferenceMany") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsReferenceManyParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\ReferenceMany(strategy=\"set\", targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})") instanceof AnnotationParsedLine.ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("ReferenceManys"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ReferenceMany");
        assertEquals("ReferenceMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 13));
        assertEquals("ReferenceMany", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ReferenceMany   ");
        assertEquals("ReferenceMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 13));
        assertEquals("ReferenceMany", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ReferenceMany\t\t  ");
        assertEquals("ReferenceMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 13));
        assertEquals("ReferenceMany", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("ReferenceMany(strategy=\"set\", targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})");
        assertEquals("ReferenceMany", parsedLine.getName());
        assertEquals("(strategy=\"set\", targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 13));
        assertEquals("ReferenceMany", type1);
        String type2 = types.get(new OffsetRange(46, 60));
        assertEquals("Documents\\Item", type2);
        String type3 = types.get(new OffsetRange(131, 149));
        assertEquals("Documents\\BookItem", type3);
        String type4 = types.get(new OffsetRange(160, 178));
        assertEquals("Documents\\SongItem", type4);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\ReferenceMany(strategy=\"set\", targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})  \t");
        assertEquals("ReferenceMany", parsedLine.getName());
        assertEquals("(strategy=\"set\", targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 25));
        assertEquals("Annotations\\ReferenceMany", type1);
        String type2 = types.get(new OffsetRange(58, 72));
        assertEquals("Documents\\Item", type2);
        String type3 = types.get(new OffsetRange(143, 161));
        assertEquals("Documents\\BookItem", type3);
        String type4 = types.get(new OffsetRange(172, 190));
        assertEquals("Documents\\SongItem", type4);
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\ReferenceMany(strategy=\"set\", targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})  \t");
        assertEquals("ReferenceMany", parsedLine.getName());
        assertEquals("(strategy=\"set\", targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 22));
        assertEquals("\\Foo\\Bar\\ReferenceMany", type1);
        String type2 = types.get(new OffsetRange(55, 69));
        assertEquals("Documents\\Item", type2);
        String type3 = types.get(new OffsetRange(140, 158));
        assertEquals("Documents\\BookItem", type3);
        String type4 = types.get(new OffsetRange(169, 187));
        assertEquals("Documents\\SongItem", type4);
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("referencemany");
        assertEquals("ReferenceMany", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 13));
        assertEquals("referencemany", type1);
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\referencemany(strategy=\"set\", targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})  \t");
        assertEquals("ReferenceMany", parsedLine.getName());
        assertEquals("(strategy=\"set\", targetDocument=\"Documents\\Item\", cascade=\"all\", discriminatorField=\"type\", discriminatorMap={\"book\"=\"Documents\\BookItem\", \"song\"=\"Documents\\SongItem\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(4, types.size());
        String type1 = types.get(new OffsetRange(0, 22));
        assertEquals("\\Foo\\Bar\\referencemany", type1);
        String type2 = types.get(new OffsetRange(55, 69));
        assertEquals("Documents\\Item", type2);
        String type3 = types.get(new OffsetRange(140, 158));
        assertEquals("Documents\\BookItem", type3);
        String type4 = types.get(new OffsetRange(169, 187));
        assertEquals("Documents\\SongItem", type4);
    }

}
