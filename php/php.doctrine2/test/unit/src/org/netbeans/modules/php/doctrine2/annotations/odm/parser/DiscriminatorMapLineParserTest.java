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
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine.ParsedLine;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class DiscriminatorMapLineParserTest extends NbTestCase {
    private TypedParametersAnnotationLineParser parser;

    public DiscriminatorMapLineParserTest(String name) {
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

    public void testReturnValueIsDiscriminatorMapParsedLine_01() throws Exception {
        assertTrue(parser.parse("DiscriminatorMap") instanceof ParsedLine);
    }

    public void testReturnValueIsDiscriminatorMapParsedLine_02() throws Exception {
        assertTrue(parser.parse("Annotations\\DiscriminatorMap") instanceof ParsedLine);
    }

    public void testReturnValueIsDiscriminatorMapParsedLine_03() throws Exception {
        assertTrue(parser.parse("\\Foo\\Bar\\DiscriminatorMap") instanceof ParsedLine);
    }

    public void testReturnValueIsDiscriminatorMapParsedLine_04() throws Exception {
        assertTrue(parser.parse("Annotations\\DiscriminatorMap({\"person\" = \"Person\", \"employee\" = \"Employee\"})") instanceof ParsedLine);
    }

    public void testReturnValueIsNull() throws Exception {
        assertNull(parser.parse("DiscriminatorMaps"));
    }

    public void testValidUseCase_01() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("DiscriminatorMap");
        assertEquals("DiscriminatorMap", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 16));
        assertEquals("DiscriminatorMap", type1);
    }

    public void testValidUseCase_02() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("DiscriminatorMap   ");
        assertEquals("DiscriminatorMap", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 16));
        assertEquals("DiscriminatorMap", type1);
    }

    public void testValidUseCase_03() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("DiscriminatorMap\t\t  ");
        assertEquals("DiscriminatorMap", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 16));
        assertEquals("DiscriminatorMap", type1);
    }

    public void testValidUseCase_04() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("DiscriminatorMap({\"person\" = \"Person\", \"employee\" = \"Employee\"})");
        assertEquals("DiscriminatorMap", parsedLine.getName());
        assertEquals("({\"person\" = \"Person\", \"employee\" = \"Employee\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(3, types.size());
        String type1 = types.get(new OffsetRange(0, 16));
        assertEquals("DiscriminatorMap", type1);
        String type2 = types.get(new OffsetRange(30, 36));
        assertEquals("Person", type2);
        String type3 = types.get(new OffsetRange(53, 61));
        assertEquals("Employee", type3);
    }

    public void testValidUseCase_05() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("Annotations\\DiscriminatorMap({\"person\" = \"Person\", \"employee\" = \"Employee\"})  \t");
        assertEquals("DiscriminatorMap", parsedLine.getName());
        assertEquals("({\"person\" = \"Person\", \"employee\" = \"Employee\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(3, types.size());
        String type1 = types.get(new OffsetRange(0, 28));
        assertEquals("Annotations\\DiscriminatorMap", type1);
        String type2 = types.get(new OffsetRange(42, 48));
        assertEquals("Person", type2);
        String type3 = types.get(new OffsetRange(65, 73));
        assertEquals("Employee", type3);
    }

    public void testValidUseCase_06() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\DiscriminatorMap({\"person\" = \"Person\", \"employee\" = \"Employee\"})  \t");
        assertEquals("DiscriminatorMap", parsedLine.getName());
        assertEquals("({\"person\" = \"Person\", \"employee\" = \"Employee\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(3, types.size());
        String type1 = types.get(new OffsetRange(0, 25));
        assertEquals("\\Foo\\Bar\\DiscriminatorMap", type1);
        String type2 = types.get(new OffsetRange(39, 45));
        assertEquals("Person", type2);
        String type3 = types.get(new OffsetRange(62, 70));
        assertEquals("Employee", type3);
    }

    public void testValidUseCase_07() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("discriminatormap");
        assertEquals("DiscriminatorMap", parsedLine.getName());
        assertEquals("", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(1, types.size());
        String type1 = types.get(new OffsetRange(0, 16));
        assertEquals("discriminatormap", type1);
    }

    public void testValidUseCase_08() throws Exception {
        AnnotationParsedLine parsedLine = parser.parse("\\Foo\\Bar\\discriminatormap({\"person\" = \"Person\", \"employee\" = \"Employee\"})  \t");
        assertEquals("DiscriminatorMap", parsedLine.getName());
        assertEquals("({\"person\" = \"Person\", \"employee\" = \"Employee\"})", parsedLine.getDescription());
        Map<OffsetRange, String> types = parsedLine.getTypes();
        assertNotNull(types);
        assertEquals(3, types.size());
        String type1 = types.get(new OffsetRange(0, 25));
        assertEquals("\\Foo\\Bar\\discriminatormap", type1);
        String type2 = types.get(new OffsetRange(39, 45));
        assertEquals("Person", type2);
        String type3 = types.get(new OffsetRange(62, 70));
        assertEquals("Employee", type3);
    }

}
